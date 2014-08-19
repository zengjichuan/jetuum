package com.petuum.ps.thread;

import com.petuum.ps.common.oplog.RowOpLog;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zjc on 2014/8/14.
 */
public class BgOpLogPartition {
    private ConcurrentHashMap<Integer, RowOpLog> opLogMap;
    private int tableId;
    private int updateSize;

    public BgOpLogPartition(int tableId, int updateSize) {
        this.tableId = tableId;
        this.updateSize = updateSize;
    }
    public RowOpLog findOpLog(int rowId){
        return opLogMap.get(rowId);
    }
    public void insertOpLog(int rowId, RowOpLog rowOpLog){
        opLogMap.put(rowId, rowOpLog);
    }

}
