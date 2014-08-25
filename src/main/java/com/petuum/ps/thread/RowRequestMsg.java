package com.petuum.ps.thread;

import com.petuum.ps.common.NumberedMsg;
import zmq.Msg;

/**
 * Created by suyuxin on 14-8-25.
 */
public class RowRequestMsg extends NumberedMsg {

    protected static final int TABLE_ID_OFFSET = NumberedMsg.getSize();
    protected static final int ROW_ID_OFFSET = TABLE_ID_OFFSET + 1;
    protected static final int CLOCK_OFFSET = ROW_ID_OFFSET + 2;
    public RowRequestMsg(Msg msg) {
        super(msg);
        sequence[MSG_TYPE_OFFSET] = K_ROW_REQUEST;
    }

    public int getTableId() {
        return sequence[TABLE_ID_OFFSET];
    }

    public int getRowId() {
        return sequence[ROW_ID_OFFSET];
    }

    public int getClock() {
        return sequence[CLOCK_OFFSET];
    }

}
