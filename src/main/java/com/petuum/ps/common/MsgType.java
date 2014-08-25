package com.petuum.ps.common;

import zmq.Msg;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * Created by suyuxin on 14-8-24.
 */
public enum MsgType implements Serializable{
    K_CLIENT_CONNECT,
    K_SERVER_CONNECT,
    K_APP_CONNECT,
    K_BG_CREATE_TABLE,
    K_CREATE_TABLE,
    K_CREATE_TABLE_REPLY,
    K_CREATED_ALL_TABLES,
    K_ROW_REQUEST,
    K_ROW_REQUEST_REPLY,
    K_SERVER_ROW_REQUEST_REPLY,
    K_BG_BLOCK,
    K_BG_SEND_OP_LOG,
    K_CLIENT_SEND_OP_LOG,
    K_CONNECT_SERVER,
    K_CLIENT_START,
    K_APP_THREAD_DEREG,
    K_CLIENT_SHUT_DOWN,
    K_SERVER_SHUT_DOWN_ACK,
    K_SERVER_PUSH_ROW,
    K_MEM_TRANSFER;

    public static MsgType convertFromMsg(Msg msg){
        String string = ByteBuffer.wrap(msg.data()).asCharBuffer().toString();
        return valueOf(string);
    }
}
