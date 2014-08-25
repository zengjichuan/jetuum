package com.petuum.ps.thread;

import java.nio.ByteBuffer;

/**
 * Created by ZengJichuan on 2014/8/22.
 */
public class MsgBase {
    public static MsgType getMsgType(ByteBuffer buf) {
        return null;
    }
}
enum MsgType{
    kClientConnect,
    kServerConnect,
    kAppConnect,
    kBgCreateTable,
    kCreateTable,
    kCreateTableReply,
    kCreatedAllTables,
    kRowRequest,
    kRowRequestReply,
    kServerRowRequestReply,
    kBgClock,
    kBgSendOpLog,
    kClientSendOpLog,
    kConnectServer,
    kClientStart,
    kAppThreadDereg,
    kClientShutDown,
    kServerShutDownAck,
    kServerPushRow,
    kMemTransfer,
}