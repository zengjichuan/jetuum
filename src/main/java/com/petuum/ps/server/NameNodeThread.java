package com.petuum.ps.server;

import com.petuum.ps.common.HostInfo;
import com.petuum.ps.common.NumberedMsg;
import com.petuum.ps.common.comm.CommBus;
import com.petuum.ps.common.util.IntBox;
import com.petuum.ps.thread.ClientConnectMsg;
import com.petuum.ps.thread.ConnectServerMsg;
import com.petuum.ps.thread.GlobalContext;
import com.petuum.ps.thread.ThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by suyuxin on 14-8-23.
 */

class CreateTableInfo {
    public int numClientsReplied;
    public int numServersReplied;
    public Queue<Integer> bgsToReply;

    public CreateTableInfo() {
        numClientsReplied = 0;
        numServersReplied = 0;
        bgsToReply.clear();
    }

    public boolean receviedFromAllServers() {
        return (numServersReplied == GlobalContext.getNumServers());
    }

    public boolean repliedToAllClients() {
        return (numClientsReplied == GlobalContext.getNumClients());
    }
}

class NameNodeContext {
    public int[] bgThreadIDs;
    public Map<Integer, CreateTableInfo> createTableMap;
    public Server serverObj;
    public int numShutdownBgs;
}

public class NameNodeThread {
    private static CyclicBarrier latch;
    private static ThreadLocal<NameNodeContext> nameNodeContext = new ThreadLocal<NameNodeContext>();

    private static Method commBusRecvAny;
    private static Method commBusRecvTimeOutAny;
    private static Method commBusSendAny;
    private static CommBus commbus;
    private static Logger log = LogManager.getLogger(NameNodeThread.class);

    private static Thread thread = new Thread(new Runnable() {//NameNodeThreadMain
        public void run() {
            int myID = GlobalContext.getNameNodeId();

            ThreadContext.registerThread(myID);

            //set up thread-specific server context
            setupNameNodeContext();
            setupCommBus();

            try {
                latch.await();
                initNameNode();
            } catch (InvocationTargetException e) {
                log.error(e.getMessage());
            } catch (IllegalAccessException e) {
                log.error(e.getMessage());
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            } catch (BrokenBarrierException e) {
                log.error(e.getMessage());
            }

            while(true) {

            }
        }
    });

    private static class ConnectionResult {
        public int senderID;
        public boolean isClient;
        public int clientID;
    }

    public static void init() throws NoSuchMethodException, InterruptedException, BrokenBarrierException {
        latch = new CyclicBarrier(2);
        commbus = GlobalContext.commBus;

        if(GlobalContext.getNumClients() == 1) {
            commBusRecvAny = CommBus.class.getMethod("recvInproc", IntBox.class);
        } else {
            commBusRecvAny = CommBus.class.getMethod("recv", IntBox.class);
        }

        if(GlobalContext.getNumClients() == 1) {
            commBusRecvTimeOutAny = CommBus.class.getMethod("recvInprocTimeout", IntBox.class, long.class);
        } else {
            commBusRecvTimeOutAny = CommBus.class.getMethod("recvTimeOut", IntBox.class, long.class);
        }

        if(GlobalContext.getNumClients() == 1) {
            commBusSendAny = CommBus.class.getMethod("sendInproc", int.class, ByteBuffer.class);
        } else {
            commBusSendAny = CommBus.class.getMethod("send", int.class, ByteBuffer.class);
        }
        thread.start();
        latch.await();
    }

    public static void shutDown() throws InterruptedException {
        thread.join();
    }

    private static void setupNameNodeContext() {
        nameNodeContext.set(new NameNodeContext());
        nameNodeContext.get().bgThreadIDs = new int[GlobalContext.getNumTotalBgThreads()];
        nameNodeContext.get().numShutdownBgs = 0;
        nameNodeContext.get().serverObj = new Server();
    }

    private static void setupCommBus() {
        int myID = ThreadContext.getId();
        CommBus.Config config = new CommBus.Config(myID, CommBus.K_IN_PROC, "");

        if(GlobalContext.getNumClients() > 1) {
            config.lType = CommBus.K_IN_PROC | CommBus.K_INTER_PROC;
            HostInfo hostInfo = GlobalContext.getHostInfo(myID);
            config.networkAddr = hostInfo.ip + ":" + hostInfo.port;
        }

        commbus.threadRegister(config);
        log.info("NameNode is ready to accept connections!");
    }

    private static void initNameNode() throws InvocationTargetException, IllegalAccessException {
        int numBgs = 0;
        int numServers = 0;
        int numExpectedConns = GlobalContext.getNumTotalBgThreads() + GlobalContext.getNumServers();
        log.info("Number totalBgThreads() = " + String.valueOf(GlobalContext.getNumTotalBgThreads()));
        log.info("Number totalServerThreads() = " + String.valueOf(GlobalContext.getNumServers()));
        for(int numConnections = 0; numConnections < numExpectedConns; numConnections++) {
            ConnectionResult cResult = getConnection();
            if(cResult.isClient) {
                nameNodeContext.get().bgThreadIDs[numBgs] = cResult.senderID;
                numBgs++;
                nameNodeContext.get().serverObj.addClientBgPair(cResult.clientID, cResult.senderID);
                log.info("Name node get client " + String.valueOf(cResult.senderID));
            } else {
                numServers++;
                log.info("Name node gets server " + String.valueOf(cResult.senderID));
            }
        }

        assert numBgs == GlobalContext.getNumTotalBgThreads();
        nameNodeContext.get().serverObj.init(0);
        log.info("Has received connections from all clients and servers, sending out connectServerMsg");

        sendToAllBgThreads(new ConnectServerMsg(null));
        log.info("Send ConnectServerMsg done");
        sendToAllBgThreads(new ClientConnectMsg(null));
        log.info("initNameNode done");
    }

    private static ConnectionResult getConnection() throws InvocationTargetException, IllegalAccessException {
        IntBox senderID = new IntBox();
        ByteBuffer msgBuf = (ByteBuffer) commBusRecvAny.invoke(commbus, senderID);

        NumberedMsg msg = new NumberedMsg(msgBuf);
        ConnectionResult result = new ConnectionResult();
        if(msg.getMsgType() == NumberedMsg.K_CLIENT_CONNECT) {
            ClientConnectMsg cMsg = new ClientConnectMsg(msgBuf);
            result.isClient = true;
            result.clientID = cMsg.getClientID();
        } else {
            assert msg.getMsgType() == NumberedMsg.K_SERVER_CONNECT;
            result.isClient = false;
        }
        result.senderID = senderID.intValue;
        return result;
    }

    private static void sendToAllBgThreads(NumberedMsg msg) throws InvocationTargetException, IllegalAccessException {
        for(int i = 0; i < GlobalContext.getNumTotalBgThreads(); i++) {
            int bdID = nameNodeContext.get().bgThreadIDs[i];
            if(!(Boolean)commBusSendAny.invoke(commbus, bdID, msg.getByteBuffer())) {
                log.error("fails to send to bg thread with Id = " + String.valueOf(bdID));
            }
        }
    }
}
