package com.petuum.ps.server;

import com.petuum.ps.common.HostInfo;
import com.petuum.ps.common.NumberedMsg;
import com.petuum.ps.common.TableInfo;
import com.petuum.ps.common.comm.CommBus;
import com.petuum.ps.common.consistency.ConsistencyModel;
import com.petuum.ps.common.util.IntBox;
import com.petuum.ps.thread.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zmq.Msg;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Vector;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by ZengJichuan on 2014/8/13.
 */

class ServerContext{
    Vector<Integer> bgThreadIds;
    Server serverObj;
    int numShutdownBgs;
}

public class ServerThreads {

    private static Logger log = LogManager.getLogger(ServerThread.class);
    private static CyclicBarrier initBarrier;
    private static Vector<Integer> threadIDs;
    private static Vector<ServerThread> threads;
    private static ThreadLocal<ServerContext> serverContext;
    private static Method commBusRecvAny;
    private static Method commBusRecvTimeOutAny;
    private static Method commBusSendAny;
    private static Method commBusRecvAsyncAny;
    private static Method commBusRecvAnyWrapper;
    private static Method commBusRecvAnyBusy;
    private static Method commBusRecvAnySleep;

    private static Method serverPushRow;
    private static Method rowSubscribe;

    private static CommBus comm_bus;

    private static class ConnectionResult {
        public int senderID;
        public boolean isClient;
        public int clientID;
    }

    private class ServerThread extends Thread {

        private int threadID;

        public void run() {
            ThreadContext.registerThread(threadID);
            //set up thread-specific server context
            setupServerContext();
            setupCommBus();
            try {
                initBarrier.await();
                initServer(threadID);

                IntBox senderID = new IntBox();
                Msg zmqMsg = new Msg();
                boolean destroy_mem = false;

                while(true) {
                    commBusRecvAnyWrapper.invoke(comm_bus, senderID, zmqMsg);
                    int msgType = new NumberedMsg(zmqMsg).getMsgType();

                    switch (msgType) {
                        case NumberedMsg.K_CLIENT_SHUT_DOWN:
                            log.info("get ClientShutDown from bg " + String.valueOf(senderID.intValue));
                            if(handleShutDownMsg()) {
                                log.info("Server shutdown");
                                comm_bus.threadDeregister();
                                return;
                            }
                        case NumberedMsg.K_CREATE_TABLE:
                            handleCreateTable(senderID.intValue, new CreateTableMsg(zmqMsg));
                            break;
                        case NumberedMsg.K_ROW_REQUEST:
                            handleRowRequest(senderID.intValue, new RowRequestMsg(zmqMsg));
                            break;
                        case NumberedMsg.K_CLIENT_SEND_OP_LOG:
                            handleOpLogMsg(senderID.intValue, new ClientSendOpLogMsg(zmqMsg));
                            break;
                        default:
                            log.error("Unrecognized message type " + String.valueOf(msgType));
                    }

                }

            } catch (InterruptedException e) {
                log.error(e.getMessage());
            } catch (BrokenBarrierException e) {
                log.error(e.getMessage());
            } catch (IllegalAccessException e) {
                log.error(e.getMessage());
            } catch (InvocationTargetException e) {
                log.error(e.getMessage());
            }

        }

        public void setThreadID(int id) {
            threadID = id;
        }
    }

    public static void init(int idST) throws NoSuchMethodException, BrokenBarrierException, InterruptedException {

        initBarrier = new CyclicBarrier(GlobalContext.getNumLocalServerThreads() + 1);
        threads = new Vector<ServerThread>(GlobalContext.getNumLocalServerThreads());
        threadIDs = new Vector<Integer>(GlobalContext.getNumLocalServerThreads());
        comm_bus = GlobalContext.commBus;

        if(GlobalContext.getNumClients() == 1) {
            commBusRecvAny = CommBus.class.getMethod("recvInproc", IntBox.class, Msg.class);
            commBusRecvAsyncAny = CommBus.class.getMethod("recvInprocAsync", IntBox.class, Msg.class);
            commBusRecvTimeOutAny = CommBus.class.getMethod("recvInprocTimeout", IntBox.class, Msg.class, long.class);
            commBusSendAny = CommBus.class.getMethod("sendInproc", int.class, ByteBuffer.class);
        }else {
            commBusRecvAny = CommBus.class.getMethod("recv", IntBox.class, Msg.class);
            commBusRecvAsyncAny = CommBus.class.getMethod("recvAsync", IntBox.class, Msg.class);
            commBusRecvTimeOutAny = CommBus.class.getMethod("recvTimeOut", IntBox.class, Msg.class, long.class);
            commBusSendAny = CommBus.class.getMethod("send", int.class, ByteBuffer.class);
        }

        //
        ConsistencyModel consistency_model = GlobalContext.getConsistencyModel();
        switch(consistency_model) {
            case SSP:
                serverPushRow = ServerThreads.class.getMethod("SSPPushServerPushRow");
                rowSubscribe = ServerThreads.class.getMethod("SSPRowSubscribe");
                break;
            case SSPPush:
                serverPushRow = ServerThreads.class.getMethod("SSPPushServerPushRow");
                rowSubscribe = ServerThreads.class.getMethod("SSPPushRowSubscribe");
                log.info("RowSubscribe = SSPPushRowSubscribe");
                break;
            default:
                log.error("Unrecognized consistency model " + consistency_model.toString());
        }

        if(GlobalContext.isAggressiveCpu()) {
            commBusRecvAnyWrapper = commBusRecvAnyBusy;
        } else {
            commBusRecvAnyWrapper = commBusRecvAnySleep;
        }

        for(int i = 0; i < GlobalContext.getNumLocalServerThreads(); i++) {
            threadIDs.set(i, idST + i);
            log.info("Create server thread " + String.valueOf(i));
            threads.get(i).setThreadID(idST + i);
            threads.get(i).start();
        }
        initBarrier.await();
    }

    private static void SSPPushServerPushRow() throws NoSuchMethodException {

        serverContext.get().serverObj.createSendServerPushRowMsgs(ServerThreads.class.getMethod("sendServerPushRowMsg"));

    }
    private static void SSPRowSubscribe(){

    }
    // communication function
    // assuming the caller is not name node
    private static void connectToNameNode(){
        int nameNodeID = GlobalContext.getNameNodeId();

        if(comm_bus.isLocalEntity(nameNodeID)) {
            log.info("Connect to local name node");
            comm_bus.connectTo(nameNodeID, new ServerConnectMsg(null).getByteBuffer());
        } else {
            log.info("Connect to remote name node");
            HostInfo nameNodeInfo = GlobalContext.getHostInfo(nameNodeID);
            String nameNodeAddr = nameNodeInfo.ip + ":" + nameNodeInfo.port;
            log.info("name_node_addr = " + String.valueOf(nameNodeAddr));
            comm_bus.connectTo(nameNodeID, nameNodeAddr, new ServerConnectMsg(null).getByteBuffer());
        }
    }

    private static ConnectionResult getConnection() throws InvocationTargetException, IllegalAccessException {
        IntBox senderID = new IntBox();
        Msg zmqMsg = new Msg();
        ConnectionResult result = new ConnectionResult();
        commBusRecvAny.invoke(comm_bus, senderID, zmqMsg);
        NumberedMsg msg = new NumberedMsg(zmqMsg);
        if(msg.getMsgType() == NumberedMsg.K_CLIENT_CONNECT) {
            ClientConnectMsg cMsg = new ClientConnectMsg(zmqMsg);
            result.isClient = true;
            result.clientID = cMsg.getClientID();
        } else {
            assert msg.getMsgType() == NumberedMsg.K_SERVER_CONNECT;
            result.isClient = false;
        }
        result.senderID = senderID.intValue;
        return result;
    }

/**
     * Functions that operate on the particular thread's specific ServerContext.
     */

    private static void setupServerContext(){

        serverContext.set(new ServerContext());
        serverContext.get().bgThreadIds = new Vector<Integer>(GlobalContext.getNumTotalBgThreads());
        serverContext.get().numShutdownBgs = 0;

    }
    private static void setupCommBus(){

        int myID = ThreadContext.getId();
        CommBus.Config config = new CommBus.Config();
        config.entityId = myID;
        log.info("ServerThreads num_clients = " + String.valueOf(GlobalContext.getNumClients()));
        log.info("my id = " + String.valueOf(myID));

        if(GlobalContext.getNumClients() > 1) {
            config.lType = CommBus.K_IN_PROC | CommBus.K_INTER_PROC;
            HostInfo hostInfo = GlobalContext.getHostInfo(myID);
            config.networkAddr = hostInfo.ip + ":" + hostInfo.port;
            log.info("network addr = " + config.networkAddr);
        } else {
            config.lType = CommBus.K_IN_PROC;
        }

        comm_bus.threadRegister(config);
        log.info("Server thread registered CommBus");

    }
    private static void initServer(int serverId) throws InvocationTargetException, IllegalAccessException {

        connectToNameNode();

        for(int numBgs = 0; numBgs < GlobalContext.getNumTotalBgThreads(); numBgs++) {
            ConnectionResult result = getConnection();
            assert result.isClient;
            serverContext.get().bgThreadIds.set(numBgs, result.senderID);
            serverContext.get().serverObj.addClientBgPair(result.clientID, result.senderID);
        }

        serverContext.get().serverObj.init(serverId);
        sendToAllBgThreads(new ClientStartMsg(null));
        log.info("InitNonNameNode done");

    }
    private static void sendToAllBgThreads(NumberedMsg msg) throws InvocationTargetException, IllegalAccessException {
        for(int i = 0; i < GlobalContext.getNumTotalBgThreads(); i++) {
            int bgId = serverContext.get().bgThreadIds.get(i);
            commBusSendAny.invoke(comm_bus, bgId, msg.getByteBuffer());
        }
    }
    private static boolean handleShutDownMsg() throws InvocationTargetException, IllegalAccessException {
        int numShutdownBgs = serverContext.get().numShutdownBgs;
        numShutdownBgs++;
        if(numShutdownBgs == GlobalContext.getNumTotalBgThreads()) {
            ServerShutDownAckMsg shutDownAckMsg = new ServerShutDownAckMsg(null);
            for(int i = 0; i < GlobalContext.getNumTotalBgThreads(); i++) {
                int bgId = serverContext.get().bgThreadIds.get(i);
                commBusSendAny.invoke(comm_bus, bgId, shutDownAckMsg.getByteBuffer());
            }
            return true;
        }
        return false;
    }
    private static void handleCreateTable(int senderId, CreateTableMsg createTableMsg) throws InvocationTargetException, IllegalAccessException {

        int tableId = createTableMsg.getTableId();

        CreateTableReplyMsg createTableReplyMsg = new CreateTableReplyMsg(null);
        createTableReplyMsg.setTableId(tableId);
        commBusSendAny.invoke(comm_bus, createTableReplyMsg.getByteBuffer());

        TableInfo tableInfo = new TableInfo();
        tableInfo.tableStaleness = createTableMsg.getStaleness();
        tableInfo.rowType = createTableMsg.getRowType();
        tableInfo.rowCapacity = createTableMsg.getRowCapacity();
        serverContext.get().serverObj.CreateTable(tableId, tableInfo);

    }
    private static void handleRowRequest(int senderId, RowRequestMsg rowRequestMsg) throws InvocationTargetException, IllegalAccessException {
        int tableId = rowRequestMsg.getTableId();
        int rowId = rowRequestMsg.getRowId();
        int clock = rowRequestMsg.getClock();
        int serverClock = serverContext.get().serverObj.getMinClock();
        if(serverClock < clock) {
            serverContext.get().serverObj.addRowRequest(senderId, tableId, rowId, clock);
            return;
        }

        int version = serverContext.get().serverObj.getBgVersion(senderId);

        ServerRow serverRow = serverContext.get().serverObj.findCreateRow(tableId, rowId);
        rowSubscribe.invoke(ServerThreads.class, serverRow, GlobalContext.threadId2ClientId(senderId));
        replyRowRequest(senderId, serverRow, tableId, rowId, serverClock, version);
    }
    private static void replyRowRequest(int bgId, ServerRow serverRow, int tableId,
                                        int rowId, int serverClock, int version){

        ByteBuffer serverRowBuffer = serverRow.serialize();
        ServerRowRequestReplyMsg serverRowRequestReplyMsg = new ServerRowRequestReplyMsg(serverRowBuffer);
        serverRowRequestReplyMsg.setTableId(tableId);
        serverRowRequestReplyMsg.setRowId(rowId);
        serverRowRequestReplyMsg.setClock(serverClock);
        serverRowRequestReplyMsg.setVersion(version);
        serverRowRequestReplyMsg.setrowSize(serverRowBuffer.capacity());

        //TransferMem ...

    }
    private static void handleOpLogMsg(int senderId, ClientSendOpLogMsg clientSendOpLogMsg) throws InvocationTargetException, IllegalAccessException {

        int clientId = clientSendOpLogMsg.getClientId();
        boolean isClock = clientSendOpLogMsg.getIsClock();
        int version = clientSendOpLogMsg.getVersion();

        serverContext.get().serverObj.applyOpLog(clientSendOpLogMsg.getData(), senderId, version);

        if(isClock) {
            boolean clockChanged = serverContext.get().serverObj.clock(clientId, senderId);
            if(clockChanged) {
                Vector<ServerRowRequest> requests = new Vector<ServerRowRequest>();
                serverContext.get().serverObj.getFulfilledRowRequests(requests);
                for(ServerRowRequest request : requests) {
                    int tableId = request.tableId;
                    int rowId = request.rowId;
                    int bgId = request.bgId;
                    int version2 = serverContext.get().serverObj.getBgVersion(bgId);
                    ServerRow serverRow = serverContext.get().serverObj.findCreateRow(tableId, rowId);
                    rowSubscribe.invoke(ServerThreads.class, serverRow, GlobalContext.threadId2ClientId(bgId));
                    int serverClock = serverContext.get().serverObj.getMinClock();
                    replyRowRequest(bgId, serverRow, tableId, rowId, serverClock, version2);
                }
                serverPushRow.invoke(ServerThreads.class);
            }
        }
    }
    private static void sendServerPushRowMsg(int bgId, ServerPushRowMsg msg, boolean lastMsg) throws InvocationTargetException, IllegalAccessException {

        msg.setVersion(serverContext.get().serverObj.getBgVersion(bgId));

        if(lastMsg) {
            msg.setIsClock(true);
            msg.setClock(serverContext.get().serverObj.getMinClock());
        } else {
            msg.setIsClock(false);
            commBusSendAny.invoke(comm_bus, bgId, msg.getByteBuffer());
        }

    }

}
