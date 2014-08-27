package com.petuum.ps.thread;

import com.petuum.ps.common.NumberedMsg;
import zmq.Msg;

import java.nio.ByteBuffer;

/**
 * Created by suyuxin on 14-8-25.
 */
public class ArbitrarySizedMsg extends NumberedMsg {

    public ArbitrarySizedMsg(Msg msg) {
        super(msg);
    }

    public int getAvaiSize() {
        return sequence.getInt(super.getSize());
    }
    public static int getHeaderSize() {
        return NumberedMsg.getSize() + INT_LENGTH;
    }

}
