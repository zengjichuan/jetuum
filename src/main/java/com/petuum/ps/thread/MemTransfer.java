package com.petuum.ps.thread;

import com.petuum.ps.common.comm.CommBus;

/**
* Transfer memory of msg (of type MemBlock) ownership to thread recv_id, who is
* responsible for destroying the received MemBlock via DestroyTransferredMem().
* Return true if the memory is transferred without copying otherwise return false.
* MemBlock is released from msg regardless wether or not the memory is copied.
* Created by ZengJichuan on 2014/8/26.
*/
public class MemTransfer {
    public static boolean transferMem(CommBus commBus, int recvId, ArbitrarySizedMsg msg){
        if (commBus.isLocalEntity(recvId)){
            MemTransferMsg memTransferMsg = new MemTransferMsg();
            InitMemTransferMsg(memTransferMsg, msg);
            int sentSize = commBus.sendInproc(recvId, memTransferMsg.getMem());
            return true;
        }else{
            int sentSize = commBus.sendInterproc(recvId, msg.getMem());
            return false;
        }
    }

    /**
     * Use msg's content to construct a MemTransferMsg to transfer memory
     * ownership between threads. That means if msg's mem should not be destroyed
     * by the sender. Therefore, InitMemTransferMsg lets msg release its control
     * on the memory.
     * @param memTransferMsg
     * @param msg
     */
    private static void InitMemTransferMsg(MemTransferMsg memTransferMsg, ArbitrarySizedMsg msg) {
        memTransferMsg.setMemPtr(msg.releaseMem());
    }
}
