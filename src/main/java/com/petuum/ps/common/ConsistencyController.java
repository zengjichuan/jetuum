package com.petuum.ps.common;

import com.google.common.cache.LoadingCache;
import java.util.Map;

/**
 * Interface for consistency controller modules. For each table we associate a
 * consistency controller (e.g., SSPController) that's essentially the "brain"
 * that maintains a prescribed consistency policy upon each table action. All
 * functions should be fully thread-safe.
 * @author Yuxin Su
 * @version 1.0
 * @updated 19-??-2014 14:25:28
 */
public abstract class ConsistencyController {

	/**
	 * common class members for all controller modules. Process cache, highly
	 * concurrent.
	 */
	protected LoadingCache process_storage_;
	/**
	 * We use sample_row_.AddUpdates(), SubstractUpdates() as static method.
	 */
	protected Row sample_row_;
	protected int table_id;

	public ConsistencyController(){

	}

	/**
	 * 
	 * @param tableID
	 * @param sample_row    sample_row
	 */
	public ConsistencyController(int tableID, final Row sample_row){

	}

	/**
	 * 
	 * @exception Throwable Throwable
	 */
	public void finalize()
	  throws Throwable{

	}

	/**
	 * 
	 * @param row_id
	 * @param updates    updates
	 */
	public abstract void BatchInc(int row_id, final Map<Integer, Object> updates);

	public abstract void Clock();

	public abstract void FlushThreadCache();

	/**
	 * 
	 * @param row_id    row_id
	 */
	public abstract RowAccessor Get(int row_id);

	/**
	 * 
	 * @param row_id    row_id
	 */
	public abstract void GetAsync(int row_id);

	/**
	 * 
	 * @param row_id
	 * @param column_id
	 * @param delta    delta
	 */
	public abstract void Inc(int row_id, int column_id, final Object delta);

	/**
	 * 
	 * @param row_id
	 * @param updates    updates
	 */
	public abstract void ThreadBatchInc(int row_id, final Map<Integer, Object> updates);

	/**
	 * 
	 * @param row_id    row_id
	 */
	public abstract RowAccessor ThreadGet(int row_id);

	/**
	 * 
	 * @param row_id
	 * @param column_id
	 * @param delta    delta
	 */
	public abstract void ThreadInc(int row_id, int column_id, final Object delta);

	public abstract void WaitPendingAsnycGet();

}