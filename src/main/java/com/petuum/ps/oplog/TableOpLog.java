package com.petuum.ps.oplog;

import com.petuum.ps.common.Row;
import com.petuum.ps.common.oplog.RowOpLog;
import com.petuum.ps.thread.GlobalContext;

import java.util.Map;
import java.util.Vector;

/**
 * Created by ZengJichuan on 2014/8/26.
 */
public class TableOpLog {
    private int tableId;
    private Vector<OpLogPartition> opLogPartitions;

    public TableOpLog(int tableId, Row sampleRow) {
        this.tableId = tableId;
        this.opLogPartitions = new Vector<OpLogPartition>(GlobalContext.getNumBgThreads());
        for (int i = 0; i < GlobalContext.getNumBgThreads(); i++){
            opLogPartitions.set(i, new OpLogPartition(sampleRow, tableId));
        }
    }

    public void inc(int rowId, int columnId, Object delta){
        int partitionNum = GlobalContext.getBgPartitionNum(rowId);
        opLogPartitions.get(partitionNum).inc(rowId, columnId, delta);
    }

    public void batchInc(int rowId, Map<Integer, Object> deltaBatch){
        int partitionNum = GlobalContext.getBgPartitionNum(rowId);
        opLogPartitions.get(partitionNum).batchInc(rowId, deltaBatch);
    }

    public RowOpLog findOpLog(int rowId){
        int partitionNum = GlobalContext.getBgPartitionNum(rowId);
        return opLogPartitions.get(partitionNum).findOpLog(rowId);
    }

    public RowOpLog findInsertOpLog(int rowId){
        int partitionNum = GlobalContext.getBgPartitionNum(rowId);
        return opLogPartitions.get(partitionNum).findInsertOpLog(rowId);
    }

    public RowOpLog getEraseOpLog(int rowId){
        int partitionNum = GlobalContext.getBgPartitionNum(rowId);
        return opLogPartitions.get(partitionNum).getEraseOpLog(rowId);
    }
}
