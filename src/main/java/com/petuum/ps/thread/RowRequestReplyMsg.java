package com.petuum.ps.thread;

import com.petuum.ps.common.NumberedMsg;
import zmq.Msg;

/**
 * Created by suyuxin on 14-8-25.
 */
public class RowRequestReplyMsg extends NumberedMsg {

    public RowRequestReplyMsg(Msg msg) {
        super(msg);
        sequence[MSG_TYPE_OFFSET] = K_ROW_REQUEST_REPLY;
    }
}
