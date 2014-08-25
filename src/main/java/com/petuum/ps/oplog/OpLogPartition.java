package com.petuum.ps.oplog;

import com.google.common.util.concurrent.Striped;
import com.petuum.ps.common.Row;
import com.petuum.ps.common.oplog.RowOpLog;
import com.petuum.ps.thread.GlobalContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

/**
 * Created by ZengJichuan on 2014/8/25.
 */
public class OpLogPartition {
    private int updateSize;
    private HashMap<Integer, RowOpLog> opLogMap;
    private Striped<Lock> locks;
    private Row sampleRow;
    private int tableId;

    public OpLogPartition(int updateSize, Row sampleRow, int tableId) {
        this.updateSize = updateSize;
        this.opLogMap = new HashMap<Integer, RowOpLog>();
        this.locks = Striped.lazyWeakLock(GlobalContext.getLockPoolSize());
        this.sampleRow = sampleRow;
        this.tableId = tableId;
    }

    public void inc(int rowId, int columnId, Object delta){
        Lock lock = locks.get(rowId);
        try{
            lock.lock();
            RowOpLog rowOpLog = opLogMap.get(rowId);
            if (rowOpLog == null){
                rowOpLog = createRowOpLog();
                opLogMap.put(rowId, rowOpLog);
            }
            Object opLogDelta = rowOpLog.findCreate(columnId, sampleRow);
            sampleRow.addUpdates(columnId, opLogDelta, delta);
        }finally {
            lock.unlock();
        }
    }

    private RowOpLog createRowOpLog() {
        RowOpLog rowOpLog = null;
        try {
            rowOpLog = new RowOpLog(Row.class.getMethod("initUpdate", new Class[]{int.class, Object.class}));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return rowOpLog;
    }

    public void batchInc(int rowId, Map<Integer, Object> deltaBatch){
        Lock lock = locks.get(rowId);
        try {
            lock.lock();

            RowOpLog rowOpLog = opLogMap.get(rowId);
            if (rowOpLog == null) {
                rowOpLog = createRowOpLog();
                opLogMap.put(rowId, rowOpLog);
            }
            for (Map.Entry<Integer, Object> entry : deltaBatch.entrySet()) {
                Object opLogDelta = rowOpLog.findCreate(entry.getKey(), sampleRow);
                sampleRow.addUpdates(entry.getKey(), opLogDelta, entry.getValue());
            }
        }finally {
            lock.unlock();
        }
    }

    public RowOpLog findOpLog(int rowId){
        return opLogMap.get(rowId);
    }

    public RowOpLog findInsertOpLog(int rowId){
        Lock lock = locks.get(rowId);
        RowOpLog rowOpLog = null;
        try {
            lock.lock();
            rowOpLog = opLogMap.get(rowId);
            if (rowOpLog == null){
                rowOpLog = createRowOpLog();
                opLogMap.put(rowId, rowOpLog);
            }
        }finally {
            lock.unlock();
        }
        return rowOpLog;
    }

    /**
     *
     * @param rowId
     * @return null means not that item
     */
    public RowOpLog getEraseOpLog(int rowId){
        Lock lock = locks.get(rowId);
        RowOpLog rowOpLog = null;
        try {
            lock.lock();
            rowOpLog = opLogMap.remove(rowId);
        }finally {
            lock.unlock();
        }
        return rowOpLog;
    }

    //GetEraseOpLogIf
}
