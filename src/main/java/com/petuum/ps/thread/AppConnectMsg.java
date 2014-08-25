package com.petuum.ps.thread;

import com.petuum.ps.common.NumberedMsg;
import zmq.Msg;

/**
 * Created by suyuxin on 14-8-25.
 */
public class AppConnectMsg extends NumberedMsg {

    public AppConnectMsg(Msg msg) {
        super(msg);
        sequence[MSG_TYPE_OFFSET] = K_APP_CONNECT;
    }
}
