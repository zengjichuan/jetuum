package com.petuum.ps.common;
import com.petuum.TableOpLog;
import com.petuum.ProcessStorage;

/**
 * @author Yuxin Su
 * @version 1.0
 * @created 19-??-2014 15:26:08
 */
public class ClientTable {

	private ConsistencyController consistency_controller_;
	private TableOpLog oplog_;
	private TableOpLogIndex oplog_index_;
	private int row_type_;
	private final Row sample_row_;
	private int table_id_;
	private boost::thread_specific_ptr<ThreadTable> thread_cache_;
	private ProcessStorage process_storage_;

	public ClientTable(){

	}

	public void finalize() throws Throwable {

	}

	/**
	 * 
	 * @param table_id
	 * @param config
	 */
	public ClientTable(int32_t table_id, final ClientTableConfig& config){

	}

	/**
	 * 
	 * @param row_id
	 * @param column_ids
	 * @param updates
	 * @param num_updates
	 */
	public void BatchInc(int32_t row_id, final int32_t* column_ids, final void* updates, int32_t num_updates){

	}

	public ~ClientTable(){

	}

	public void Clock(){

	}

	public void FlushThreadCache(){

	}

	/**
	 * 
	 * @param row_id
	 * @param row_accessor
	 */
	public void Get(int32_t row_id, RowAccessor* row_accessor){

	}

	public TableOpLog& get_oplog(){
		return null;
	}

	public ProcessStorage& get_process_storage(){
		return null;
	}

	public int32_t get_row_type(){
		return null;
	}

	public final AbstractRow* get_sample_row(){
		return null;
	}

	/**
	 * 
	 * @param client_table
	 */
	public cuckoohash_map<int32_t, bool> * GetAndResetOpLogIndex(int32_t client_table){
		return null;
	}

	/**
	 * 
	 * @param row_id
	 */
	public void GetAsync(int32_t row_id){

	}

	/**
	 * 
	 * @param row_id
	 * @param column_id
	 * @param update
	 */
	public void Inc(int32_t row_id, int32_t column_id, final void* update){

	}

	public void RegisterThread(){

	}

	/**
	 * 
	 * @param row_id
	 * @param column_ids
	 * @param updates
	 * @param num_updates
	 */
	public void ThreadBatchInc(int32_t row_id, final int32_t* column_ids, final void* updates, int32_t num_updates){

	}

	/**
	 * 
	 * @param row_id
	 * @param row_accessor
	 */
	public void ThreadGet(int32_t row_id, ThreadRowAccessor* row_accessor){

	}

	/**
	 * 
	 * @param row_id
	 * @param column_id
	 * @param update
	 */
	public void ThreadInc(int32_t row_id, int32_t column_id, final void* update){

	}

	public void WaitPendingAsyncGet(){

	}

}