package com.petuum.ps.thread;

/**
 * Created by ZengJichuan on 2014/8/22.
 */
public class MsgBase {
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