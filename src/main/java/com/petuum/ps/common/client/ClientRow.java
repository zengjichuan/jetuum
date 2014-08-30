package com.petuum.ps.common.client;

import com.petuum.ps.common.Row;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class aims to simplify the RowAccess and ClientRow.
 * @author Yuxin Su
 * @version 1.0
 * @created 19-??-2014 21:08:18
 */
public abstract class ClientRow {

	private AtomicInteger num_refs;
	private Row row_data;

	public ClientRow(){

	}

    public void setRowData(Row row) {
        row_data = row;
    }

	public void finalize() throws Throwable {

	}

	/**
	 * 
	 * @param clock
	 * @param row_data
	 */
	public ClientRow(int clock, Row row_data){

	}

	public void decRef(){

	}

	public int getClock(){
		return 0;
	}

	public Row getRowData(){
		return null;
	}

	public void incRef(){

	}

	/**
	 * 
	 * @param clock
	 */
	public void setClock(int clock){

	}

}