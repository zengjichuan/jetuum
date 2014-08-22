package com.petuum.ps.thread;

import com.google.common.base.Preconditions;
import com.petuum.ps.common.Row;
import com.petuum.ps.common.client.ClientTable;
import com.petuum.ps.common.comm.CommBus;
import com.petuum.ps.common.oplog.RowOpLog;
import com.petuum.ps.common.util.IntBox;
import com.petuum.ps.common.util.RecordBuff;
import com.petuum.ps.common.util.VectorClock;
import com.petuum.ps.common.util.VectorClockMT;
import com.petuum.ps.server.CallBackSubs;
import zmq.Msg;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zjc on 2014/8/14.
 */
public class BgWorkers {
    private class BgContext {
        /**
         * version of the data, increment when a set of OpLogs
         * are sent out; may wrap around
         * More specifically, version denotes the version of the
         * OpLogs that haven't been sent out.
         */
        public int version;
        public RowRequestOpLogMgr rowRequestOpLogMgr;
        /**
         * initialized by BgThreadMain(), used in CreateSendOpLogs()
         * For server x, table y, the size of serialized OpLog is ...
         */
        public Map<Integer, Map<Integer, Integer>> serverTableOpLogSizeMap;
        /**
         * The OpLog msg to each server
         */
        public Map<Integer, ClientSendOpLogMsg> serverOpLogMsgMap;
        /**
         * map server id to oplog msg size
         */
        public Map<Integer, Integer> serverOpLogMsgSizeMap;
        /**
         * size of oplog per table, reused across multiple tables
         */
        public Map<Integer, Integer> tableServerOpLogSizeMap;
        /**
         * Data members needed for server push
         */
        public VectorClock serverVectorClock;
    }

    private static Method myCreateClientRow;
    private static Method getRowOpLog;
    /* Functions for SSPValue */
    private static void handleClockMsg(boolean clockAdvanced){

    }
    //function pointer GetRowOpLogFunc

    private static Vector<Runnable> threads;
    private static Vector<Integer> threadIds;
    private static Map<Integer, ClientTable> tables;
    private static int idStart;

    private static CyclicBarrier initBarrier;
    private static CyclicBarrier createTableBarrier;

    private static ThreadLocal<BgContext> bgContext;
    private static CommBus commBus;
    //function pointers
    private static Method commBusRecvAny;
    private static Method commBusRecvTimeOutAny;
    private static Method commBusSendAny;
    private static Method commBusRecvAsyncAny;
    private static Method commBusRecvAnyWrapper;


    private static AtomicInteger systemClock;
    private static VectorClockMT bgServerClock;
    private static ReentrantLock systemClockLock;

    private static void commBusRecvAnyBusy(Integer senderId, Msg msg){
        boolean received = commBus.commBusRecvAsyncAny(senderId, msg);
        while (!received){
            received = commBus.commBusRecvAsyncAny(senderId, msg);
        }
    }

    public static void init(Map<Integer, ClientTable> rTables){
        threads.setSize(GlobalContext.getNumBgThreads());
        threadIds.setSize(GlobalContext.getNumBgThreads());
        tables = rTables;
        idStart = GlobalContext.getHeadBgId(GlobalContext.getClientId());
        commBus = GlobalContext.commBus;

        int myClientId = GlobalContext.getClientId();
        int myHeadBgId = GlobalContext.getHeadBgId(myClientId);
        for (int i = 0; i < GlobalContext.getNumBgThreads(); i++){
            bgServerClock.addClock(myHeadBgId + i, 0);
        }
        initBarrier = new CyclicBarrier(GlobalContext.getNumBgThreads() + 1);
        createTableBarrier = new CyclicBarrier(2);
        try {
            if (GlobalContext.getNumClients() == 1) {
                commBusRecvAny = commBus.getClass().getMethod("recvInProc",
                        new Class[]{Integer.class, Msg.class});
                commBusRecvAsyncAny = commBus.getClass().getMethod("recvInprocAsync",
                        new Class[]{Integer.class, Msg.class});
                commBusRecvTimeOutAny = commBus.getClass().getMethod("recvInprocTimeout",
                        new Class[]{Integer.class, Msg.class, long.class});
                commBusSendAny = commBus.getClass().getMethod("sendInproc",
                        new Class[]{int.class, ByteBuffer.class});
            }else{
                commBusRecvAny = commBus.getClass().getMethod("recv",
                        new Class[]{Integer.class, Msg.class});
                commBusRecvAsyncAny = commBus.getClass().getMethod("recvAsync",
                        new Class[]{Integer.class, Msg.class});
                commBusRecvTimeOutAny = commBus.getClass().getMethod("recvTimeout",
                        new Class[]{Integer.class, Msg.class, long.class});
                commBusSendAny = commBus.getClass().getMethod("send",
                        new Class[]{int.class, ByteBuffer.class});
            }
        }catch (NoSuchMethodException e) {
                e.printStackTrace();
        }
        Method bgThreadMain;
        ConsistencyModel consistencyModel = GlobalContext.getConsistencyModel();
        try {
            switch (consistencyModel) {
                case SSP:
//                    bgThreadMain = BgWorkers.class.getMethod("sspBgThreadMain");
                    myCreateClientRow = BgWorkers.class.getMethod("createSSPClientRow",
                            new Class[]{int.class, Row.class});
                    getRowOpLog = BgWorkers.class.getMethod("sspGetRowOpLog",
                            new Class[]{TableOpLog.class, int.class, RowOpLog.class});//RowOpLog **row_oplog_ptr
                    break;
                case SSPPush:
//                    bgThreadMain = BgWorkers.class.getMethod("sspGbThreadMain");
                    myCreateClientRow = BgWorkers.class.getMethod("createClientRow",
                            new Class[]{int.class, Row.class});
                    getRowOpLog = BgWorkers.class.getMethod("sspgetRowOpLog",
                            new Class[]{TableOpLog.class, int.class, RowOpLog.class});
                    break;
            }
            if (GlobalContext.isAggressiveCpu()){
                commBusRecvAnyWrapper = BgWorkers.class.getMethod("commBusRecvAnyBusy",
                        new Class[]{IntBox.class, Msg.class});
            }else{
                commBusRecvAnyWrapper = BgWorkers.class.getMethod("commBusRecvAnySleep",
                        new Class[]{IntBox.class, Msg.class});
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        ExecutorService threadPool = Executors.newFixedThreadPool(GlobalContext.getNumBgThreads());
        for (int i = 0; i < GlobalContext.getNumBgThreads(); i++) {
            threadPool.execute(new BgThread(threadIds.get(i)));
        }
        try {
            initBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
        threadRegister();
    }
    private static void commBusRecvAnySleep(Integer senderId, Msg msg){
        commBus.commBusRecvAny(senderId, msg);
    }

    private static boolean SSPGetRowOpLog(TableOpLog tableOpLog, int rowId, RowOpLog rowOpLog){
        return tableOpLog.getEraseOpLog(rowId, rowOpLog);
    }
    private BgOpLog getOpLogAndIndex(){
        Vector<Integer> serverIds = GlobalContext.getServerIds();
        int localBgIndex = ThreadContext.getId() - idStart;
        // get thread-specific data structure to assist oplog message creation
        // those maps may contain legacy data from previous runs
        Map<Integer, Map<Integer, Integer>> serverTableOpLogSizeMap = bgContext.serverTableOpLogSizeMap;
        Map<Integer, Integer> tableNumBytesByServer = bgContext.tableServerOpLogSizeMap;

        BgOpLog bgOplog = new BgOpLog();
        for(Map.Entry<Integer, ClientTable> entry : tables.entrySet()){
            int tableId = entry.getKey();
            TableOpLog tableOpLog = entry.getValue().getOpLog();

            //Get OpLog index
            /**
             * ...
             */
            int tableUpdataSize = entry.getValue().getSampleRow().getUpdateSize();
            BgOpLogPartition bgTableOpLog = new BgOpLogPartition(tableId, tableUpdataSize);

            for (int i = 0; i < GlobalContext.getNumServers(); i++) {
                tableNumBytesByServer.put(serverIds.get(i), Integer.SIZE);
            }
            /**
             * ...
             */
            for ( Map.Entry<Integer, Integer> serverEntry : tableNumBytesByServer.entrySet()){
                serverTableOpLogSizeMap.get(serverEntry.getKey()).put(tableId, serverEntry.getValue());
            }
        }
        return bgOplog;
    }
    static class BgThread implements Runnable{
        private int myId;
        BgThread(int threadId) {
            myId = threadId;
        }

        public void run() {
//        STATS_REGISTER_THREAD(kBgThread);
            ThreadContext.registerThread(myId);

            initBgContext();

            initCommBus(myId);

            bgServerHandshake();

            try {
                initBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            IntBox numConnectedAppThreads = new IntBox(0);
            IntBox numDeregisteredAppThreads = new IntBox(0);
            IntBox numShutdownAckedServers = new IntBox(0);

            recvAppInitThreadConnection(numConnectedAppThreads);

            if(myId == idStart){
                handleCreateTables();
                try {
                    createTableBarrier.await();         //there are 2 createTable threads?
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
            Msg zmqMsg = new Msg();
            IntBox senderId = new IntBox();
            MsgType msgType;
            ByteBuffer msgMem;
            boolean destroyMem = false;
            while(true){
                try {
                    commBusRecvAnyWrapper.invoke(BgWorkers.class,
                            new Object []{senderId, zmqMsg});
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                msgType = MsgBase.getMsgType(zmqMsg.buf());
                destroyMem = false;
                if (msgType == MsgType.kMemTransfer) {
                    MemTransferMsg memTransferMsg = new MemTransferMsg(zmqMsg.buf());
                    msgMem = memTransferMsg.getMem();
                    msgType = MsgBase.getMsgType(msgMem);
                    destroyMem = true;
                }else{
                    msgMem = zmqMsg.buf();
                }

                switch (msgType){
                    case kAppConnect:
                    {
                        numConnectedAppThreads.intValue++;
                        Preconditions.checkArgument(
                                numConnectedAppThreads.intValue<=GlobalContext.getNumAppThreads());
                    }
                    break;
                    case kAppThreadDereg:
                    {
                        numDeregisteredAppThreads.intValue++;
                        if (numDeregisteredAppThreads.intValue == GlobalContext.getNumAppThreads()){
                            try {
                                ClientShutdownMsg msg = new ClientShutdownMsg();
                                int nameNodeId = GlobalContext.getNameNodeId();
                                commBusSendAny.invoke(commBus,
                                        new Object[]{nameNodeId, msg.getMem()});
                                int numServers = GlobalContext.getNumServers();
                                Vector<Integer> serverIds = GlobalContext.getServerIds();
                                for(int i = 0 ; i < numServers; i++){
                                    commBusSendAny.invoke(commBus,
                                            new Object[]{serverIds.get(i), msg.getMem()});
                                }
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                    case kServerShutDownAck:
                    {
                        numShutdownAckedServers.intValue++;
                        if(numShutdownAckedServers.intValue == GlobalContext.getNumServers() + 1){
                            commBus.threadDeregister();
                            shutdownClean();
                            return;
                        }
                    }
                    break;
                    case kRowRequest:
                    {
                        RowRequestMsg rowRequestMsg = new RowRequestMsg(msgMem);
                        checkForwardRowRequestToServer(senderId, rowRequestMsg);
                    }
                    break;
                    case kServerRowRequestReply:
                    {
                        ServerRowRequestReplyMsg serverRowRequestReplyMsg =
                                new ServerRowRequestReplyMsg(msgMem);
                        handleServerRowRequestReply(senderId, serverRowRequestReplyMsg);
                    }
                    break;
                    case kBgClock:
                    {
                        handleClockMsg(true);
                        //STATS_BG_CLOCK();
                    }
                    break;
                    case kBgSendOpLog:
                    {
                        handleClockMsg(false);
                    }
                    break;
                    case kServerPushRow:
                    {
                        ServerPushRowMsg serverPushRowMsg = new ServerPushRowMsg(msgMem);
                        int version = serverPushRowMsg.getVersion();
                        bgContext.rowRequestOpLogMgr.serverAcknowledgeVersion(senderId, version);
                        applyServerPushedRow(version, serverPushRowMsg.getData());
//                        STATS_BG_ADD_PER_CLOCK_SERVER_PUSH_ROW_SIZE(
//                                server_push_row_msg.get_size());
                        boolean isClock = serverPushRowMsg.getIsClock();
                        if (isClock){
                            int serverClock = serverPushRowMsg.getClock();
                            Preconditions.checkArgument(
                                    bgContext.serverVectorClock.getClock(senderId)+1 == serverClock);
                            int newClock = bgContext.serverVectorClock.tick(senderId);
                            if (newClock != 0){
                                int newSystemClock = bgServerClock.tick(myId);
                                if (newSystemClock != 0){
                                    systemClock.incrementAndGet();
                                    systemClockLock.lock();
//                                    system_clock_cv_.notify_all();            //condition variable
                                }
                            }
                        }
                    }
                    break;
                    default:
                }
                if (destroyMem){
                    MemTransfer.destroyTransferredMem(msgMem);
                }
            }
            return;
        }
    }
}
