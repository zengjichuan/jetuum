package com.petuum.ps.thread;

import com.petuum.ps.common.client.ClientTable;
import com.petuum.ps.common.comm.CommBus;
import com.petuum.ps.common.util.VectorClock;
import zmq.Msg;

import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CyclicBarrier;

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

    private static void commBusRecvAnyBusy(Integer senderId, Msg msg){
        boolean received = commBus.commBusRecvAsyncAny(senderId, msg);
        while (!received){
            received = commBus.commBusRecvAsyncAny(senderId, msg);
        }
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

}
