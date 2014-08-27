package com.petuum.ps.thread;

import com.petuum.ps.common.NumberedMsg;
import zmq.Msg;

import java.nio.ByteBuffer;

/**
 * Created by suyuxin on 14-8-25.
 */
public class ClientConnectMsg extends NumberedMsg {

    protected static final int CLIENT_ID_OFFSET = NumberedMsg.getSize();
    public ClientConnectMsg(Msg msg) {
        super(msg);
        if(msg == null)
            sequence = ByteBuffer.allocate(getSize());
        sequence.putInt(MSG_TYPE_OFFSET, K_CLIENT_CONNECT);
    }

    public int getClientID() {
        return sequence.getInt(CLIENT_ID_OFFSET);
    }

    protected static int getSize() {
        return CLIENT_ID_OFFSET + INT_LENGTH;
    }
}
