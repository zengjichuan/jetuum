package com.petuum.ps.common.test.zmq;

import com.petuum.ps.common.comm.ZmqUtil;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.nio.ByteBuffer;

/**
 * Created by suyuxin on 14-9-4.
 */
public class ZMQRouterTest {
    public static ZContext zmqContext = new ZContext(1);
    public static class ZMQData {
        ZMQ.Socket sock;
    }
    public static ThreadLocal<ZMQData> threadInfo = new ThreadLocal<ZMQData>();

    private static void setUpRouterSocket(ZMQ.Socket socket, int id,
                                          int numBytesSendBuff, int numBytesRecvBuff){
        int myId = ZmqUtil.entityID2ZmqID(id);
        ZmqUtil.zmqSetSocketOpt(socket, ZmqUtil.ZMQ_IDENTITY, myId);

        Integer socketMandatory = 1;
        ZmqUtil.zmqSetSocketOpt(socket, ZmqUtil.ZMQ_ROUTER_MANDATORY, socketMandatory);

        if (numBytesSendBuff != 0){
            ZmqUtil.zmqSetSocketOpt(socket, ZmqUtil.ZMQ_SNDBUF, numBytesSendBuff);
        }

        if (numBytesRecvBuff != 0){
            ZmqUtil.zmqSetSocketOpt(socket, ZmqUtil.ZMQ_RCVBUF, numBytesRecvBuff);
        }
    }

    public static Thread server = new Thread(new Runnable() {
        public void run() {
            threadInfo.set(new ZMQData());
            //  Socket to talk to clients
            threadInfo.get().sock = zmqContext.createSocket(ZMQ.ROUTER);
            ZMQ.Socket responder = threadInfo.get().sock;
            //setUpRouterSocket(responder, 0, 10, 10);
            responder.bind("inproc://test");
            System.out.println("Begin to receive....");
            while (!Thread.currentThread().isInterrupted()) {
                // Wait for next request from the client
                System.out.println(responder.recvStr());
            }
            responder.close();
        }
    });

    public static Thread client = new Thread(new Runnable() {
        public void run() {
            threadInfo.set(new ZMQData());
            //  Socket to talk to server
            System.out.println("Connecting to hello world server…");
            threadInfo.get().sock = zmqContext.createSocket(ZMQ.REQ);
            ZMQ.Socket requester = threadInfo.get().sock;
            //setUpRouterSocket(requester, 1, 10, 10);
            requester.bind("inproc://test");

            for (int requestNbr = 0; requestNbr != 10; requestNbr++) {
                String request = "Hello";
                System.out.println("Sending Hello " + requestNbr);
                requester.send(request.getBytes(), 0);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            requester.close();
        }
    });


    public static void main(String[] args) throws InterruptedException {

        server.start();
        Thread.sleep(2000);
        client.start();
        server.join();
        client.join();
        zmqContext.close();
    }
}
