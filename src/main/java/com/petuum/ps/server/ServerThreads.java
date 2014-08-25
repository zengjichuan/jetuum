/*
package com.petuum.ps.server;

import com.petuum.ps.common.comm.CommBus;

import java.util.Vector;
import java.util.concurrent.CyclicBarrier;

*/
/**
 * Created by admin on 2014/8/13.
 *//*

public class ServerThreads {
    class ServerContext{
        Vector<Integer> bgThreadIds;
        Server serverObj;
        int numShutdownBgs;
    }
//    private static ServerPushRowFunc serverPushRow;
//    private static RowSubscribeFunc rowSubscribe;
    private static CyclicBarrier initBarrier;
    //static std::vector<pthread_t> threads_;
    private static Vector<Integer> threadIds;
    private static ThreadLocal<ServerContext> serverContext;
//    static CommBus::RecvFunc CommBusRecvAny;
//    static CommBus::RecvTimeOutFunc CommBusRecvTimeOutAny;
//    static CommBus::SendFunc CommBusSendAny;
//    static CommBus::RecvAsyncFunc CommBusRecvAsyncAny;
//    static CommBus::RecvWrapperFunc CommBusRecvAnyWrapper;

//    static void CommBusRecvAnyBusy(int32_t *sender_id, zmq::message_t *zmq_msg);
//    static void CommBusRecvAnySleep(int32_t *sender_id, zmq::message_t *zmq_msg);
//
    static CommBus comm_bus;
    private static void ServerThreadMain(Object serverThreadInfo){

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
    */
/**
     * Functions that operate on the particular thread's specific ServerContext.
     *//*

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
*/
