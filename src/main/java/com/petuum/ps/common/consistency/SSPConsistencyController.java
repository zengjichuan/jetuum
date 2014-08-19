package com.petuum.ps.common.consistency;
import com.petuum.ps.common.Row;
import com.petuum.ps.common.client.ThreadTable;

import java.util.Map;

/**
 * @author Su Yuxin
 * @version 1.0
 * @created 19-??-2014 20:18:33
 */
public class SSPConsistencyController extends ConsistencyController {

	/**
	 * SSP staleness parameter.
	 */
	protected int staleness_;
	protected ThreadTable thread_cache_;

	public SSPConsistencyController(){

	}

	public void finalize() throws Throwable {
		super.finalize();
	}

	/**
	 * 
	 * @param table_id
	 * @param sample_row
	 * @param thread_cache
	 */
	public SSPConsistencyController(int table_id, final Row sample_row, ThreadTable thread_cache){

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
	public Row Get(int row_id) {
        return null;
    }

	/**
	 * 
	 * @param row_id
	 */
	public void GetAsync(int row_id) {

    }

	/**
	 * 
	 * @param row_id
	 * @param column_id
	 * @param delta
	 */
	public void Inc(int row_id, int column_id, Object delta) {

    }

	/**
	 * 
	 * @param row_id
	 * @param updates
	 */
	public void ThreadBatchInc(int row_id, Map<Integer, Object> updates){

    }

	/**
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
	 * @param delta
	 */
	public void ThreadInc(int row_id, int column_id, Object delta){

    }

	public void WaitPendingAsnycGet(){

    }

}