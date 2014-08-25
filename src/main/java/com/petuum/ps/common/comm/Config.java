package com.petuum.ps.common.comm;

/**
 * Created by ZengJichuan on 2014/8/23.
 */
public class Config{
    /**
     *  My thread id.
     */
    public int entityId;
    /**
     * What should I listen to?
     */
    public int lType;

    /**
     *  In the format of "ip:port", such as "192.168.1.1:9999". It must be set
     *  if ((ltype_ & kInterProc) == true)
     */
    public String networkAddr;

    public int numBytesInprocSendBuff;
    public int numBytesInprocRecvBuff;
    public int numBytesInterprocSendBuff;
    public int numBytesInterprocRecvBuff;

    public Config() {
        this.entityId = 0;
        this.lType = CommBus.K_NONE;
        this.numBytesInprocSendBuff = 0;
        this.numBytesInprocRecvBuff = 0;
        this.numBytesInterprocSendBuff = 0;
        this.numBytesInterprocRecvBuff = 0;
    }

    public Config(int entityId, int lType, String networkAddr) {
        this.entityId = entityId;
        this.lType = lType;
        this.networkAddr = networkAddr;
        this.numBytesInprocSendBuff = 0;
        this.numBytesInprocRecvBuff = 0;
        this.numBytesInterprocSendBuff = 0;
        this.numBytesInterprocRecvBuff = 0;
    }
}
