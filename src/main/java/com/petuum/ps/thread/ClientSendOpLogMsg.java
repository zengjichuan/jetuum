package com.petuum.ps.thread;

import com.petuum.ps.common.NumberedMsg;
import zmq.Msg;

import java.nio.ByteBuffer;

/**
 * Created by suyuxin on 14-8-27.
 */
public class ClientSendOpLogMsg extends ArbitrarySizedMsg {

    protected static final int IS_CLOCK_OFFSET = ArbitrarySizedMsg.getHeaderSize();
    protected static final int CLIENT_ID_OFFSET = IS_CLOCK_OFFSET + 1;//IS_CLOCK_OFFSET is boolean type
    protected static final int VERSION_OFFSET = CLIENT_ID_OFFSET + INT_LENGTH;

    public ClientSendOpLogMsg(Msg msg) {
        super(msg);
        if(msg == null)
            sequence = ByteBuffer.allocate(ArbitrarySizedMsg.getSize());
        sequence.putInt(MSG_TYPE_OFFSET, K_CLIENT_SEND_OP_LOG);
    }

    public boolean getIsClock() {
        return sequence.get(IS_CLOCK_OFFSET) == 1;
    }

    public int getClientId() {
        return sequence.getInt(CLIENT_ID_OFFSET);
    }

    public int getVersion() {
        return sequence.getInt(VERSION_OFFSET);
    }

    public static int getHeaderSize() {
        return VERSION_OFFSET + INT_LENGTH;
    }
    public ByteBuffer getData() {
        byte[] byteList = sequence.array();
        return ByteBuffer.wrap(byteList, getHeaderSize(), byteList.length - getHeaderSize());
    }
}
