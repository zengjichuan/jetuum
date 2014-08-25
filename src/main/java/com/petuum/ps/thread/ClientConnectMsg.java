package com.petuum.ps.thread;

import com.petuum.ps.common.NumberedMsg;
import zmq.Msg;

/**
 * Created by suyuxin on 14-8-25.
 */
public class ClientConnectMsg extends NumberedMsg {

    protected static final int CLIENT_ID_OFFSET = NumberedMsg.getSize();
    public ClientConnectMsg(Msg msg) {
        super(msg);
        sequence[MSG_TYPE_OFFSET] = K_CLIENT_CONNECT;
    }

    public int getClientID() {
        return sequence[CLIENT_ID_OFFSET];
    }

    protected static int getSize() {
        return CLIENT_ID_OFFSET + 1;
    }
}
