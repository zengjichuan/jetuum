package com.petuum.ps.common.client;
import com.petuum.ps.common.ClientTableConfig;
import com.petuum.ps.common.Row;
import com.petuum.ps.common.consistency.ConsistencyController;
import com.petuum.ps.oplog.TableOpLog;

import java.util.Map;

/**
 * @author Yuxin Su
 * @version 1.0
 * @created 19-??-2014 18:29:05
 */
public class ClientTable {

	private ConsistencyController consistency_controller_;
	private int rowType;
	private Row sampleTow;
	private int tableId;
	private ThreadLocal<ThreadTable> threadCache;
    private TableOpLog opLog;

	public ClientTable(){

	}

	/**
	 * 
	 * @param table_id
	 * @param config    config
	 */
	public ClientTable(int table_id, ClientTableConfig config){

	}

	/**
	 * 
	 * @exception Throwable
	 */
	public void finalize()
	  throws Throwable{

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
	 * @param rowId
	 */
	public Row get(int rowId){
		return null;
	}

	public int getRowType(){
		return 0;
	}

    /**
     *
     * @return opLog
     */
    public TableOpLog getOpLog(){
        return opLog;
    }

	public final Row getSampleRow(){
		return null;
	}

	/**
	 * 
	 * @param clientTable    client_table
	 */
	public Map<Integer, Boolean> getAndResetOpLogIndex(int clientTable){
		return null;
	}

	/**
	 * 
	 * @param rowId    row_id
	 */
	public void getAsync(int rowId){

	}

	/**
	 * 
	 * @param row_id
	 * @param column_id
	 * @param update    update
	 */
	public void inc(int row_id, int column_id, Object update){

	}

	public void registerThread(){

	}

	/**
	 * 
	 * @param rowId
	 * @param updates
	 */
	public void threadBatchInc(int rowId, Map<Integer, Object> updates){

	}

	/**
	 * 
	 * @param rowId
	 */
	public Row threadGet(int rowId){
		return null;
	}

	/**
	 * 
	 * @param row_id
	 * @param column_id
	 * @param update    update
	 */
	public void threadInc(int row_id, int column_id, Object update){

	}

	public void waitPendingAsyncGet(){

	}

}