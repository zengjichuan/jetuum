package com.petuum.ps.thread;

import com.petuum.ps.common.NumberedMsg;
import zmq.Msg;

/**
 * Created by suyuxin on 14-8-25.
 */
public class CreateTableMsg extends NumberedMsg {

    protected static final int TABLE_ID_OFFSET = NumberedMsg.getSize();
    protected static final int STALENESS_OFFSET = TABLE_ID_OFFSET + 1;
    protected static final int ROW_TYPE_OFFSET = TABLE_ID_OFFSET + 2;
    protected static final int ROW_CAPACITY_OFFSET = TABLE_ID_OFFSET + 3;

    public CreateTableMsg(Msg msg) {
        super(msg);
        sequence[MSG_TYPE_OFFSET] = K_CREATE_TABLE;
    }

    public static int getSize() {
        return ROW_CAPACITY_OFFSET + 1;
    }

    public int getTableId() {
        return sequence[TABLE_ID_OFFSET];
    }

    public int getStaleness() {
        return sequence[STALENESS_OFFSET];
    }

    public int getRowType() {
        return sequence[ROW_TYPE_OFFSET];
    }

    public int getRowCapacity() {
        return sequence[ROW_CAPACITY_OFFSET];
    }
}
