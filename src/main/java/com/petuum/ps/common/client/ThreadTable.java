package com.petuum.ps.common.client;
import com.google.common.cache.Cache;
import com.petuum.ps.common.Row;
import com.petuum.ps.common.oplog.RowOpLog;
import com.petuum.ps.common.util.IntBox;
import com.petuum.ps.oplog.TableOpLog;
import com.petuum.ps.oplog.TableOpLogIndex;
import com.petuum.ps.thread.GlobalContext;
import org.apache.commons.lang3.SerializationUtils;

import java.util.*;

/**
 * @author Yuxin Su
 * @version 1.0
 * @created 19-??-2014 18:11:45
 */
public class ThreadTable {

	private Vector<Set<Integer>> opLogIndex;
    private Map<Integer, RowOpLog> opLogMap;
	private Map<Integer, Row> rowStorage;
	private Row sampleRow;

	public ThreadTable(Row sampleRow){
        this.sampleRow = sampleRow;
        opLogIndex = new Vector<Set<Integer>>();
        for(int i = 0; i < GlobalContext.getNumBgThreads(); i++){
            opLogIndex.add(new HashSet<Integer>());
        }
        rowStorage = new HashMap<Integer, Row>();
	}

	public void finalize() throws Throwable {

	}

	/**
	 * 
	 * @param row_id
	 * @param deltas
	 */
	public void batchInc(int row_id, final Map<Integer, Double> deltas){

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
	public void inc(int row_id, int column_id, final Double delta){

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
        Row copyRow = SerializationUtils.clone(toInsert);
        rowStorage.putIfAbsent(rowId, copyRow);
        RowOpLog rowOpLog = opLogMap.get(rowId);
        if(rowOpLog != null) {
            IntBox columnId = new IntBox();
            Double delta = rowOpLog.beginIterate(columnId);
            while (delta != null){
                copyRow.applyInc(columnId.intValue, delta);
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