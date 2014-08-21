package com.petuum.ps.common.comm;

import com.google.common.base.Preconditions;
import com.petuum.ps.common.util.IntBox;
import org.zeromq.ZMQ;
import zmq.Msg;

import java.nio.ByteBuffer;

/**
 * Created by admin on 2014/8/11.
 */
public class ZmqUtil {
    public static final int ZMQ_IDENTITY = 0;
    public static final int ZMQ_ROUTER_MANDATORY = 1;
    public static final int ZMQ_SNDBUF = 2;
    public static final int ZMQ_RCVBUF = 3;


    public static int entityID2ZmqID(int entityId){
        return (entityId << 4 | 0x1);
    }

    public static int zmqID2EntityID(int zmqId){
        return zmqId >> 4;
    }

    public static void zmqSetSocketOpt(ZMQ.Socket socket, int option, int optVal){
        switch (option){
            case ZMQ_IDENTITY:
                socket.setIdentity(ByteBuffer.allocate(Integer.SIZE).putInt(optVal).array());break;
            case ZMQ_ROUTER_MANDATORY:
                socket.setRouterMandatory(optVal != 0 ? true : false);break;
            case ZMQ_SNDBUF:
                socket.setSendBufferSize(optVal);break;
            case ZMQ_RCVBUF:
                socket.setReceiveBufferSize(optVal);break;
        }
    }

    public static void zmqBind(ZMQ.Socket socket, String connectAddr){
        socket.bind(connectAddr);
        //catch zma::error_t
    }

    public static void zmqConnectSend(ZMQ.Socket socket, String connectAddr, int zmqId,
                                 ByteBuffer msgBuf){
        socket.connect(connectAddr);
        boolean suc = false;

        do{
            suc = socket.send(Integer.toString(zmqId).getBytes(), 0);
            if (suc == true) {
                socket.sendByteBuffer(msgBuf, 0);
                break;
            }
            try {
                Thread.sleep(0, 500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while(true);
    }

    // True for received, false for not
    public static boolean zmqRecvAsync(ZMQ.Socket socket, Msg msg){
        boolean received = false;
        msg.put(socket.recv(ZMQ.DONTWAIT));
        if(msg.size()!=0)
            received = true;
        return received;

    }
    public static boolean zmqRecvAsync(ZMQ.Socket socket, IntBox zmqId, Msg msg){
        Msg msgZid = new Msg();
        boolean received = zmqRecvAsync(socket, msgZid);
        if (!received)
            return false;
        zmqId.intValue = ByteBuffer.wrap(msgZid.data()).getInt();
        zmqRecv(socket, msg);
        return true;
    }
    public static void zmqRecv(ZMQ.Socket sock, Msg msg) {
        boolean received = false;
        msg.put(sock.recv());
        Preconditions.checkArgument(received);
    }
    public static void zmqRecv(ZMQ.Socket sock, IntBox zmqId, Msg msg) {
        Msg msgZid = new Msg();
        zmqRecv(sock, msgZid);
        zmqId.intValue = ByteBuffer.wrap(msgZid.data()).getInt();
        zmqRecv(sock, msg);
    }

    /**
     * return number of bytes sent
     * @param sock
     * @param data
     * @param flag
     * @return
     */
    public static int zmqSend(ZMQ.Socket sock, ByteBuffer data, int flag){
        return sock.sendByteBuffer(data, flag);
    }

    /**
     * 0 means cannot be sent, try again;
     0 should not happen unless flag = ZMQ_DONTWAIT
     * @param sock
     * @param zmqId
     * @param data
     * @param flag
     * @return
     */
    public static int zmqSend(ZMQ.Socket sock, int zmqId, ByteBuffer data, int flag){
        int zidSend = zmqSend(sock, ByteBuffer.allocate(Integer.SIZE).putInt(zmqId), flag | ZMQ.SNDMORE);
        if (zidSend == 0)   return 0;
        return zmqSend(sock, data, flag);
    }
    public static int zmqSend(ZMQ.Socket sock, int zmqId, Msg msg, int flag){
        int zidSentSize = zmqSend(sock, ByteBuffer.allocate(Integer.SIZE).putInt(zmqId) ,flag | ZMQ.SNDMORE);
        if (zidSentSize == 0) return 0;
        return zmqSend(sock, msg,flag);
    }

    private static int zmqSend(ZMQ.Socket sock, Msg msg, int flag) {
        return sock.sendByteBuffer(msg.buf(), flag);
    }
}
