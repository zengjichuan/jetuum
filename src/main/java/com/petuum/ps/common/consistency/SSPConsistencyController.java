package com.petuum.ps.common.consistency;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.petuum.ps.common.Row;
import com.petuum.ps.common.client.ClientRow;
import com.petuum.ps.common.client.ThreadTable;
import com.petuum.ps.common.oplog.RowOpLog;
import com.petuum.ps.oplog.TableOpLog;
import com.petuum.ps.oplog.TableOpLogIndex;
import com.petuum.ps.thread.BgWorkers;
import com.petuum.ps.thread.ThreadContext;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author Su Yuxin
 * @version 1.0
 * @created 19-??-2014 20:18:33
 */
public class SSPConsistencyController extends ConsistencyController {

	/**
	 * SSP staleness parameter.
	 */
	protected int staleness;
	protected ThreadTable threadCache;
    /**
     * Controller will only write to oplog_ but never read from it, as
     * all local updates are reflected in the row values.
     */
    protected TableOpLog opLog;
    protected TableOpLogIndex opLogIndex;

	public SSPConsistencyController(){
        processStorage = CacheBuilder.newBuilder()
                .build(
                    new CacheLoader<Integer, ClientRow>() {
                        @Override
                        public ClientRow load(Integer key) throws Exception {
                            int stalest_clock = ThreadContext.getClock() - staleness;
                            if(stalest_clock < 0){
                                stalest_clock = 0;
                            }
//                            BgWorkers.RequestRow(table_id, key, stalest_clock);
                            //need receive row data
                            return null;
                        }
                    }
                );
	}

	public void finalize() throws Throwable {
		super.finalize();
	}

	/**
	 * 
	 * @param tableId
	 * @param sampleRow
	 * @param threadCache
	 */
	public SSPConsistencyController(int tableId, final Row sampleRow, ThreadTable threadCache){
        this.threadCache = threadCache;
        this.opLog = new TableOpLog(tableId, sampleRow);
        this.opLogIndex = new TableOpLogIndex();
	}

	/**
	 * 
	 * @param row_id
	 * @param updates
	 */
	public void batchInc(int row_id, Map<Integer, Object> updates){

    }

	public void clock(){

    }

	public void flushThreadCache(){

    }

	/**
	 * 
	 * @param row_id
	 */
	public ClientRow get(int row_id, int clock) throws ExecutionException { //how to get the clock? use a list
        int stalest_clock = ThreadContext.getClock() - staleness;
        if(stalest_clock < 0){
            stalest_clock = 0;
        }
        ClientRow row = processStorage.get(row_id);

        if(clock >= stalest_clock) {
            return row;
        }else {
            processStorage.invalidate(row_id);
            return processStorage.get(row_id);
        }
    }

	/**
	 * 
	 * @param row_id
	 */
	public void getAsync(int row_id) {

    }

	/**
	 * 
	 * @param row_id
	 * @param column_id
	 * @param delta
	 */
	public void inc(int row_id, int column_id, Object delta) {
        threadCache.indexUpdate(row_id);
        RowOpLog rowOpLog = opLog.findInsertOpLog(row_id);
        Object opLogDelta = rowOpLog.findCreate(column_id, sampleRow);
        sampleRow.addUpdates(column_id, opLogDelta, delta);

        //update to process_storage
        ClientRow clientRow = processStorage.get(row_id);

    }

	/**
	 * 
	 * @param rowId
	 * @param updates
	 */
	public void threadBatchInc(int rowId, Map<Integer, Object> updates){
        threadCache.batchInc(rowId, updates);
    }

	/**
	 * 
	 * @param rowId
	 */
	public Row threadGet(int rowId){
        Row rowData = threadCache.getRow(rowId);
        if (rowData != null){
            return rowData;
        }

        return null;
    }

	/**
	 * 
	 * @param rowId
	 * @param columnId
	 * @param delta
	 */
	public void threadInc(int rowId, int columnId, Object delta){
        threadCache.inc(rowId, columnId, delta);
    }

	public void waitPendingAsnycGet(){

    }

    public Map<Integer, Boolean> getAndResetOpLogIndex(int clientTable){
        return opLogIndex.resetPartition(clientTable);
    }
}