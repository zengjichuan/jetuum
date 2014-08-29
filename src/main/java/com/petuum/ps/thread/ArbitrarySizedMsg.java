package com.petuum.ps.thread;

import com.petuum.ps.common.NumberedMsg;
import sun.security.x509.AVA;
import zmq.Msg;

import java.nio.ByteBuffer;

/**
 * Created by suyuxin on 14-8-25.
 */
public class ArbitrarySizedMsg extends NumberedMsg {

    protected static final int AVAI_SIZE_OFFSET = NumberedMsg.getSize();

    public ArbitrarySizedMsg(Msg msg) {
        super(msg);
    }

    public int getAvaiSize() {
        return sequence.getInt(AVAI_SIZE_OFFSET);
    }
    public static int getHeaderSize() {
        return NumberedMsg.getSize() + INT_LENGTH;
    }

}
