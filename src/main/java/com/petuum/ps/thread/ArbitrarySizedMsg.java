package com.petuum.ps.thread;

import com.petuum.ps.common.NumberedMsg;
import zmq.Msg;

/**
 * Created by suyuxin on 14-8-25.
 */
public class ArbitrarySizedMsg extends NumberedMsg {

    public ArbitrarySizedMsg(Msg msg) {
        super(msg);
    }

    public int getAvaiSize() {
        return sequence[super.getSize()];
    }
    public int getHeaderSize() {
        return super.getSize() + 1;
    }

}
