package com.petuum.ps.thread;

import com.petuum.ps.common.NumberedMsg;
import zmq.Msg;

/**
 * Created by suyuxin on 14-8-27.
 */
public class AppThreadDeregMsg extends NumberedMsg {
    public AppThreadDeregMsg(Msg msg) {
        super(msg);
        sequence.putInt(MSG_TYPE_OFFSET, K_APP_THREAD_DEREG);
    }
}
