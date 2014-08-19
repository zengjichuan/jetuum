package com.petuum.ps.common;
import com.petuum.TableInfo;

/**
 * ClientTableConfig is used by client only.
 * @author Yuxin Su
 * @version 1.0
 * @created 19-??-2014 15:36:42
 */
public class ClientTableConfig {

	/**
	 * Estimated upper bound # of pending oplogs in terms of # of rows. For SSP this
	 * is the # of rows all threads collectively touches in a Clock().
	 */
	public int oplog_capacity;
	/**
	 * In # of rows.
	 */
	public int process_cache_capacity;
	public TableInfo table_info;
	/**
	 * In # of rows.
	 */
	public int thread_cache_capacity;

	public ClientTableConfig(){

	}

	public void finalize() throws Throwable {

	}

}