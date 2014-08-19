package com.petuum.ps.common.client;
import com.petuum.ps.common.ClientTableConfig;
import com.petuum.ps.common.Row;
import com.petuum.ps.common.consistency.ConsistencyController;

import java.util.Map;

/**
 * @author Yuxin Su
 * @version 1.0
 * @created 19-??-2014 18:29:05
 */
public class ClientTable {

	private ConsistencyController consistency_controller_;
	private int row_type_;
	private Row sample_row_;
	private int table_id_;
	private ThreadTable thread_cache_;

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
	public void BatchInc(int row_id, Map<Integer, Object> updates){

	}

	public void Clock(){

	}

	public void FlushThreadCache(){

	}

	/**
	 * 
	 * @param row_id
	 */
	public Row Get(int row_id){
		return null;
	}

	public int get_row_type(){
		return 0;
	}

	public final Row get_sample_row(){
		return null;
	}

	/**
	 * 
	 * @param client_table    client_table
	 */
	public Map<Integer, Boolean> GetAndResetOpLogIndex(int client_table){
		return null;
	}

	/**
	 * 
	 * @param row_id    row_id
	 */
	public void GetAsync(int row_id){

	}

	/**
	 * 
	 * @param row_id
	 * @param column_id
	 * @param update    update
	 */
	public void Inc(int row_id, int column_id, Object update){

	}

	public void RegisterThread(){

	}

	/**
	 * @param column_ids
	 * @param num_updates
	 * 
	 * @param row_id
	 * @param updates
	 */
	public void ThreadBatchInc(int row_id, Map<Integer, Object> updates){

	}

	/**
	 * @param row_accessor
	 * 
	 * @param row_id
	 */
	public Row ThreadGet(int row_id){
		return null;
	}

	/**
	 * 
	 * @param row_id
	 * @param column_id
	 * @param update    update
	 */
	public void ThreadInc(int row_id, int column_id, Object update){

	}

	public void WaitPendingAsyncGet(){

	}

}