package com.petuum.ps.common.client;
import com.petuum.ps.common.Row;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Yuxin Su
 * @version 1.0
 * @created 19-??-2014 18:11:45
 */
public class ThreadTable {

	private List<Set<Integer>> oplog_index_;
	private Map<Integer, Row> row_storage_;
	private Row sample_row_;

	public ThreadTable(){

	}

	public void finalize() throws Throwable {

	}

	/**
	 * 
	 * @param sample_row
	 */
	public ThreadTable(final Row sample_row){

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
	 * @param row_id
	 */
	public Row getRow(int row_id){
		return null;
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
	 * @param row_id
	 */
	public void indexUpdate(int row_id){

	}

	/**
	 * 
	 * @param row_id
	 * @param to_insert
	 */
	public void insertRow(int row_id, final Row to_insert){

	}

}