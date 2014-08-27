package com.petuum.ps.thread;

import com.petuum.ps.common.NumberedMsg;
import zmq.Msg;

/**
 * Created by suyuxin on 14-8-25.
 */
public class ServerConnectMsg extends NumberedMsg {

    public ServerConnectMsg(Msg msg) {
        super(msg);
        sequence.putInt(MSG_TYPE_OFFSET, K_SERVER_CONNECT);
    }
}
