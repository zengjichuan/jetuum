package com.petuum.ps.common.client;
import com.petuum.ps.common.util.VectorClockMT;
import com.petuum.ps.common.ClientTableConfig;
import com.petuum.ps.common.TableGroupConfig;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Yuxin Su
 * @version 1.0
 * @created 19-??-2014 15:26:05
 */
public class TableGroup {

	/**
	 * Max staleness among all tables.
	 */
	private  int max_table_staleness_;
	private  AtomicInteger num_app_threads_registered_;
	private  Map<Integer, ClientTable> tables_;
	private  VectorClockMT vector_clock_;

	/**
	 * 
	 * @param table_group_config
	 * @param table_access
	 */
	public TableGroup(final TableGroupConfig table_group_config, boolean table_access){

	}

	public void clock(){

	}

	private void clockAggressive(){

	}

	private void clockConservative(){

	}

	/**
	 * 
	 * @param table_id
	 * @param table_config
	 */
	public boolean createTable(int table_id, final ClientTableConfig table_config){
        return false;
	}

	public void createTableDone(){

	}

	public void deregisterThread(){

	}

	/**
	 * 
	 * @param table_id
	 */
	public ClientTable getTableOrDie(int table_id){
        return null;
	}

	public void globalBarrier(){

	}

	public int registerThread(){
        return 0;
	}

	public void waitThreadRegister(){

	}

}