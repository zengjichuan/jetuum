package com.petuum.ps.thread;

import com.petuum.ps.common.NumberedMsg;
import zmq.Msg;

/**
 * Created by suyuxin on 14-8-27.
 */
public class BgClockMsg extends NumberedMsg {
    public BgClockMsg(Msg msg) {
        super(msg);
        sequence.putInt(MSG_TYPE_OFFSET, K_BG_CLOCK);
    }
}
