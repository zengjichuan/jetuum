package com.petuum.ps.common;

import zmq.Msg;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * Created by suyuxin on 14-8-24.
 */
public class NumberedMsg {
    public static final int K_CLIENT_CONNECT = 0;
    public static final int K_SERVER_CONNECT = 1;
    public static final int K_APP_CONNECT = 2;
    public static final int K_BG_CREATE_TABLE = 3;
    public static final int K_CREATE_TABLE = 4;
    public static final int K_CREATE_TABLE_REPLY = 5;
    public static final int K_CREATED_ALL_TABLES = 6;
    public static final int K_ROW_REQUEST = 7;
    public static final int K_ROW_REQUEST_REPLY = 8;
    public static final int K_SERVER_ROW_REQUEST_REPLY = 9;
    public static final int K_BG_BLOCK = 10;
    public static final int K_BG_SEND_OP_LOG = 11;
    public static final int K_CLIENT_SEND_OP_LOG = 12;
    public static final int K_CONNECT_SERVER = 13;
    public static final int K_CLIENT_START = 14;
    public static final int K_APP_THREAD_DEREG = 15;
    public static final int K_CLIENT_SHUT_DOWN = 16;
    public static final int K_SERVER_SHUT_DOWN_ACK = 17;
    public static final int K_SERVER_PUSH_ROW = 18;
    public static final int K_MEM_TRANSFER = 50;

    protected static final int MSG_TYPE_OFFSET = 0;
    protected static final int SEQ_NUM_OFFSET = 1;
    protected static final int ACK_NUM_OFFSET = 2;

    protected static int getSize() {
        return ACK_NUM_OFFSET + 1;
    }
    public NumberedMsg(Msg msg) {
        sequence = ByteBuffer.wrap(msg.data()).asIntBuffer().array();
    }

    public int getMsgType() {
        return sequence[MSG_TYPE_OFFSET];
    }

    public int getSeqNum() {
        return sequence[SEQ_NUM_OFFSET];
    }

    public int getAckNum() {
        return sequence[ACK_NUM_OFFSET];
    }

    protected int[] sequence;
}

