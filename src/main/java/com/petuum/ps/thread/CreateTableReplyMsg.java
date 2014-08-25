package com.petuum.ps.thread;

import com.petuum.ps.common.NumberedMsg;
import zmq.Msg;

/**
 * Created by suyuxin on 14-8-25.
 */
public class CreateTableReplyMsg extends NumberedMsg {

    protected static final int TABLE_ID_OFFSET = NumberedMsg.getSize();
    public CreateTableReplyMsg(Msg msg) {
        super(msg);
        sequence[MSG_TYPE_OFFSET] = K_CREATE_TABLE_REPLY;
    }

    public int getTableId() {
        return sequence[TABLE_ID_OFFSET];
    }
}
