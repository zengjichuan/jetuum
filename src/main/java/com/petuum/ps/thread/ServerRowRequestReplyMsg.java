package com.petuum.ps.thread;

import zmq.Msg;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Created by suyuxin on 14-8-27.
 */
public class ServerRowRequestReplyMsg extends ArbitrarySizedMsg {

    protected static final int TABLE_ID_OFFSET = ArbitrarySizedMsg.getHeaderSize();
    protected static final int ROW_ID_OFFSET = TABLE_ID_OFFSET + 1 * INT_LENGTH;
    protected static final int CLOCK_OFFSET = TABLE_ID_OFFSET + 2 * INT_LENGTH;
    protected static final int VERSION_OFFSET = TABLE_ID_OFFSET + 3 * INT_LENGTH;
    protected static final int ROW_SIZE_OFFSET = TABLE_ID_OFFSET + 4 * INT_LENGTH;

    public ServerRowRequestReplyMsg(Msg msg) {
        super(msg);
        if(msg == null)
            sequence = ByteBuffer.allocate(ArbitrarySizedMsg.getSize());
        sequence.putInt(MSG_TYPE_OFFSET, K_SERVER_ROW_REQUEST_REPLY);
    }

    public int getTableId() {
        return sequence.getInt(TABLE_ID_OFFSET);
    }

    public int getRowId() {
        return sequence.getInt(ROW_ID_OFFSET);
    }

    public int getClock() {
        return sequence.getInt(CLOCK_OFFSET);
    }

    public int getVersion() {
        return sequence.getInt(VERSION_OFFSET);
    }

    public int getRowSize() {
        return sequence.getInt(ROW_SIZE_OFFSET);
    }

    public static int getHeaderSize() {
        return ROW_SIZE_OFFSET + INT_LENGTH;
    }

    public ByteBuffer getRowData() {
        byte[] byteList = sequence.array();
        return ByteBuffer.wrap(byteList, getHeaderSize(), byteList.length - getHeaderSize());
    }
}
