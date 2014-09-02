package com.petuum.ps.common.client;
import com.google.common.cache.Cache;
import com.petuum.ps.common.Row;
import com.petuum.ps.common.oplog.RowOpLog;
import com.petuum.ps.common.util.IntBox;
import com.petuum.ps.oplog.TableOpLog;
import com.petuum.ps.oplog.TableOpLogIndex;
import com.petuum.ps.thread.GlobalContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Yuxin Su
 * @version 1.0
 * @created 19-??-2014 18:11:45
 */
public class ThreadTable {

	private List<Set<Integer>> opLogIndex;
    private Map<Integer, RowOpLog> opLogMap;
	private Map<Integer, Row> rowStorage;
	private Row sampleRow;

	public ThreadTable(Row sampleRow){
        this.sampleRow = sampleRow;
	}

	public void finalize() throws Throwable {

	}

	/**
	 * 
	 * @param row_id
	 * @param deltas
	 */
	public void batchInc(int row_id, final Map<Integer, Object> deltas){

	}

	/**
	 * 
	 * @param rowId
	 */
	public Row getRow(int rowId){
		return rowStorage.get(rowId);
	}

	/**
	 * 
	 * @param row_id
	 * @param column_id
	 * @param delta
	 */
	public void inc(int row_id, int column_id, final Object delta){

	}

	/**
	 * 
	 * @param rowId
	 */
	public void indexUpdate(int rowId){
        int partitionNum = GlobalContext.getBgPartitionNum(rowId);
        opLogIndex.get(partitionNum).add(rowId);
	}

	/**
	 * 
	 * @param rowId
	 * @param toInsert
	 */
	public void insertRow(int rowId, final Row toInsert){
        //clone
        rowStorage.put(rowId, toInsert);
        RowOpLog rowOpLog = opLogMap.get(rowId);
        if(rowOpLog != null) {
            IntBox columnId = new IntBox();
            Object delta = rowOpLog.beginIterate(columnId);
            while (delta != null){
                toInsert.applyInc(columnId.intValue, delta);
                delta = rowOpLog.next(columnId);
            }
        }

	}

    public void flushCache(Cache<Integer, ClientRow> processStorage, TableOpLog opLog, Row sampleRow) {
    }

    public void flushOpLogIndex(TableOpLogIndex tableOpLogIndex) {
        for (int i = 0; i < GlobalContext.getNumBgThreads(); i++) {
            tableOpLogIndex.addIndex(i, opLogIndex.get(i));
            opLogIndex.get(i).clear();
        }
    }
}