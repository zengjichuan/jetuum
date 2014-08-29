package com.petuum.ps.server;

import com.petuum.ps.common.comm.CommBus;
import com.petuum.ps.thread.*;

import java.lang.reflect.Method;
import java.util.Vector;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by admin on 2014/8/13.
 */

class ServerContext{
    Vector<Integer> bgThreadIds;
    Server serverObj;
    int numShutdownBgs;
}

public class ServerThreads {

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

    private static Thread serverThreadMain = new Thread(new Runnable() {
        public void run() {

        }
    });

    private static class ServerThread implements Runnable {

        public void run() {

        }
    }

    public static void init(int idST) {

        initBarrier = new CyclicBarrier(GlobalContext.getNumLocalServerThreads() + 1);
        threads = new Vector<ServerThread>(GlobalContext.getNumLocalServerThreads());
        threadIDs = new Vector<Integer>(GlobalContext.getNumLocalServerThreads());
        comm_bus = GlobalContext.commBus;

        if(GlobalContext.getNumClients() == 1) {
            commBusRecvAny =
        }

    }

    private static void SSPPushServerPushRow(){

    }
    private static void SSPServerPushRow(){

    }
    // communication function
    // assuming the caller is not name node
    private static void connectToNameNode(){

    }

    private static int getConnection(boolean isClient, int clientId){

    }

/**
     * Functions that operate on the particular thread's specific ServerContext.
     */

    private static void setupServerContext(){

    }
    private static void setupCommBus(){

    }
    private static void initServer(int serverId){

    }
    private static void sendToAllBgThreads(byte[] msg, int msgSize){

    }
    private static boolean handleShutDownMsg(){

    }
    private static void handleCreateTable(int senderId, CreateTableMsg createTableMsg){

    }
    private static void handleRowRequest(int senderId, RowRequestMsg rowRequestMsg){

    }
    private static void replyRowRequest(int bgId, ServerRow serverRow, int tableId,
                                        int rowId, int serverClock, int version){

    }
    private static void handleOpLogMsg(int senderId, ClientSendOpLogMsg clientSendOpLogMsg){

    }
    private static void sendServerPushRowMsg(int bgId, ServerPushRowMsg msg, boolean lastMsg){

    }

}
