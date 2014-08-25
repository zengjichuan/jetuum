package com.petuum.ps.thread;

import com.google.common.base.Preconditions;
import com.petuum.ps.common.ClientTableConfig;
import com.petuum.ps.common.HostInfo;
import com.petuum.ps.common.Row;
import com.petuum.ps.common.client.ClientTable;
import com.petuum.ps.common.comm.CommBus;
import com.petuum.ps.common.comm.Config;
import com.petuum.ps.common.oplog.RowOpLog;
import com.petuum.ps.common.util.IntBox;
import com.petuum.ps.common.util.RecordBuff;
import com.petuum.ps.common.util.VectorClock;
import com.petuum.ps.common.util.VectorClockMT;
import com.petuum.ps.server.CallBackSubs;
import com.sun.deploy.util.SessionState;
import com.sun.org.apache.xpath.internal.operations.Bool;
import zmq.Msg;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zjc on 2014/8/14.
 */
public class BgWorkers {
    private static class BgContext {
        private BgContext() {
            serverTableOpLogSizeMap = new HashMap<Integer, Map<Integer, Integer>>();
            serverOpLogMsgMap = new HashMap<Integer, ClientSendOpLogMsg>();
            serverOpLogMsgSizeMap = new HashMap<Integer, Integer>();
            tableServerOpLogSizeMap = new HashMap<Integer, Integer>();


        }

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
//        public Map<Integer, ClientSendOpLogMsg> serverOpLogMsgMap;
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

    private static Condition systemClockCv;
    private static HashMap<Integer, HashMap<Integer, Boolean>> tableOpLogIndex;

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

        //condition variable
        systemClockLock = new ReentrantLock();
        systemClockCv = systemClockLock.newCondition();

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

   // private static boolean SSPGetRowOpLog(TableOpLog tableOpLog, int rowId, RowOpLog rowOpLog){
   //     return tableOpLog.getEraseOpLog(rowId, rowOpLog);
   // }
    private BgOpLog getOpLogAndIndex(){
        Vector<Integer> serverIds = GlobalContext.getServerIds();
        int localBgIndex = ThreadContext.getId() - idStart;
        // get thread-specific data structure to assist oplog message creation
        // those maps may contain legacy data from previous runs
     //   Map<Integer, Map<Integer, Integer>> serverTableOpLogSizeMap = bgContext.serverTableOpLogSizeMap;
     //   Map<Integer, Integer> tableNumBytesByServer = bgContext.tableServerOpLogSizeMap;

        BgOpLog bgOplog = new BgOpLog();
        for(Map.Entry<Integer, ClientTable> entry : tables.entrySet()){
            int tableId = entry.getKey();
       //     TableOpLog tableOpLog = entry.getValue().getOpLog();

            //Get OpLog index
            /**
             * ...
             */
         //   int tableUpdataSize = entry.getValue().getSampleRow().getUpdateSize();
         //   BgOpLogPartition bgTableOpLog = new BgOpLogPartition(tableId, tableUpdataSize);

            for (int i = 0; i < GlobalContext.getNumServers(); i++) {
           //     tableNumBytesByServer.put(serverIds.get(i), Integer.SIZE);
            }
            /**
             * ...
             */
            //for ( Map.Entry<Integer, Integer> serverEntry : tableNumBytesByServer.entrySet()){
            //    serverTableOpLogSizeMap.get(serverEntry.getKey()).put(tableId, serverEntry.getValue());
            //}
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
//                                    system_clock_cv_.notify_all();            //condition variable
                                    systemClockCv.signalAll();
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

        private static void checkForwardRowRequestToServer(IntBox senderId, RowRequest rowRequestMsg) {
            int tableId = rowRequestMsg.getTableId();
            int rowId = rowRequestMsg.getRowId();
            int clock = rowRequestMsg.getClock();
            ClientTable table = tables.get(tableId);
            ProcessStorage tableStorage = table.getProcessStorage();
            RowAccessor rowAccessor =
        }

        private static void shutdownClean() {
            //delete bg_context_->row_request_oplog_mgr;
        }

        private static void handleCreateTables() {
            for (int numCreatedTables = 0; numCreatedTables < GlobalContext.getNumTables();
                 numCreatedTables++) {
                int tableId;
                IntBox senderId = new IntBox();
                ClientTableConfig clientTableConfig = new ClientTableConfig();
                {
                    Msg zmqMsg = new Msg();
                    commBus.recvInproc(senderId, zmqMsg);
                    MsgType msgType = MsgBase.getMsgType(zmqMsg.buf());
                    Preconditions.checkArgument(msgType == MsgType.kBgCreateTable);
                    BgCreateTableMsg bgCreateTableMsg = new BgCreateTableMsg(zmqMsg.buf());
                    //set up client table config
                    clientTableConfig.tableInfo.tableStaleness = bgCreateTableMsg.getStaleness();
                    clientTableConfig.tableInfo.rowType = bgCreateTableMsg.getRowType();
                    clientTableConfig.processCacheCapacity =
                            bgCreateTableMsg.getProcessCacheCapacity();
                    clientTableConfig.threadCacheCapacity =
                            bgCreateTableMsg.getThreadCacheCapacity();
                    clientTableConfig.opLogCapacity = bgCreateTableMsg.getOpLogCapacity();

                    CreateTableMsg createTableMsg = new CreateTableMsg();
                    createTableMsg.setTableId(bgCreateTableMsg.getTableId());
                    createTableMsg.setStaleness(bgCreateTableMsg.getStaleness());
                    createTableMsg.setRowType(bgCreateTableMsg.getRowType());
                    createTableMsg.setRowCapacity(bgCreateTableMsg.getRowCapacity());
                    tableId = createTableMsg.getTableId();

                    //send msg to name node
                    int nameNodeId = GlobalContext.getNameNodeId();
                    try {
                        int sendSize = commBusSendAny.invoke(commBus,
                                new Object[]{nameNodeId, createTableMsg.getMem()});
                        Preconditions.checkArgument(sendSize == createTableMsg.getSize());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
                //wait for response from name node
                {
                    Msg zmqMsg = new Msg();
                    IntBox nameNodeId = new IntBox();
                    try {
                        commBusRecvAny.invoke(commBus, new Object[]{nameNodeId, zmqMsg});
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    MsgType msgType = MsgBase.getMsgType(zmqMsg.buf());
                    Preconditions.checkArgument(msgType == MsgType.kCreateTableReply);
                    CreateTableReplyMsg createTableReplyMsg = new CreateTableReplyMsg(zmqMsg.buf());
                    Preconditions.checkArgument(createTableReplyMsg.getTableId() == tableId);
                    //Create ClientTable
                    ClientTable clientTable = new ClientTable(tableId, clientTableConfig);
                    tables.put(tableId, clientTable);   //not thread safe
                    int sentSize = commBus.sendInproc(senderId.intValue, zmqMsg.buf());
                }
            }
            {
                Msg zmqMsg = new Msg();
                IntBox senderId = new IntBox();
                try {
                    commBusRecvAny.invoke(commBus, new Object[]{senderId, zmqMsg});
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                MsgType msgType = MsgBase.getMsgType(zmqMsg.buf());
                Preconditions.checkArgument(msgType == MsgType.kCreatedAllTables);
            }
        }

        private static void recvAppInitThreadConnection(IntBox numConnectedAppThreads) {
            Msg zmqMsg = new Msg();
            IntBox senderId = new IntBox();
            commBus.recvInproc(senderId, zmqMsg);
            MsgType msgType = MsgBase.getMsgType(zmqMsg.buf());
            Preconditions.checkArgument(msgType == MsgType.kAppConnect);
            numConnectedAppThreads.intValue++;
            Preconditions.checkArgument(
                    numConnectedAppThreads.intValue <= GlobalContext.getNumAppThreads());
        }

        /**
         * Connect to namenode and each server
         */
        private static void bgServerHandshake() {
            //connect to the namenode
            int nameNodeId = GlobalContext.getNameNodeId();
            connectToNameNodeOrServer(nameNodeId);
            //wait for connectServerMsg
            Msg zmqMsg = new Msg();
            IntBox senderId = new IntBox();
            if(commBus.isLocalEntity(nameNodeId)){
                commBus.recvInproc(senderId, zmqMsg);
            }else{
                commBus.recvInterproc(senderId, zmqMsg);
            }
            MsgType msgType = MsgBase.getMsgType(zmqMsg.buf());
            Preconditions.checkArgument(senderId.intValue == nameNodeId);
            Preconditions.checkArgument(msgType == MsgType.kConnectServer);

            //connect to servers
            int numServers = GlobalContext.getNumServers();
            Vector<Integer> serverIds = GlobalContext.getServerIds();
            for (int serverId : serverIds){
                connectToNameNodeOrServer(serverId);
            }

            //get message from servers for permission to start
            for (int numStartedServers = 0; numStartedServers < GlobalContext.getNumServers();
                 numStartedServers++){
                Msg zmqMsg_ = new Msg();
                IntBox senderId_ = new IntBox();
                try {
                    commBusRecvAny.invoke(commBus, new Object []{senderId_, zmqMsg_});
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                MsgType msgType_ = MsgBase.getMsgType(zmqMsg_.buf());
                Preconditions.checkArgument(msgType_ == MsgType.kClientStart);
            }
        }

        private static void connectToNameNodeOrServer(int serverId) {
            ClientConnectMsg clientConnectMsg = new ClientConncetMsg();
            clientConnectMsg.setClientId(GlobalContext.getClientId());
            ByteBuffer msg = clientConnectMsg.getMem();

            if (commBus.isLocalEntity(serverId)){
                commBus.connectTo(serverId, msg);
            }else {
                HostInfo serverInfo = GlobalContext.getHostInfo(serverId);
                String serverAddr = new String(serverInfo.ip+":"+serverInfo.port);
                commBus.connectTo(serverId, serverAddr, msg);
            }
        }

        private static void initCommBus(int myId) {
            Config commConfig = new Config();
            commConfig.entityId = myId;
            commConfig.lType = CommBus.K_IN_PROC;
            commBus.threadRegister(commConfig);
        }

        /**
         * initialize local storage
         */
        private static void initBgContext() {
            bgContext.set(new BgContext());
            bgContext.get().version = 0;
            switch (GlobalContext.getConsistencyModel()){
                case SSP:
                    bgContext.get().rowRequestOpLogMgr = new SSPRowRequestOpLogMgr();
                    break;
                case SSPPush:
                    bgContext.get().rowRequestOpLogMgr = new SSPPushRowRequestOpLogMgr();
                    break;
                default:
            }
            Vector<Integer> serverIds = GlobalContext.getServerIds();
            for (int serverId : serverIds){
                bgContext.get().serverTableOpLogSizeMap.put(serverId, new HashMap<Integer, Integer>());
                bgContext.get().serverOpLogMsgMap.put(serverId, null);
                bgContext.get().serverOpLogMsgSizeMap.put(serverId, null);
                bgContext.get().tableServerOpLogSizeMap.put(serverId, 0);
                bgContext.get().serverVectorClock.addClock(serverId, 0);
            }
        }
    }
}
