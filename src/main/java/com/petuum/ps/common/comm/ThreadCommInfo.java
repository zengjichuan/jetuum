package com.petuum.ps.common.comm;

/**
 * Created by ZengJichuan on 2014/8/11.
 */
public class ThreadCommInfo {
    public int entityId;
    public int lType;
    public int pollSize;

    public int numBytesInprocSendBuff;
    public int numBytesInprocRecvBuff;
    public int numBytesInterprocSendBuff;
    public int numBytesInterprocRecvBuff;

}
