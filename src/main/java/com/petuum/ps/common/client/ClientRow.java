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

//	private AtomicInteger numRefs;
	private Row rowData;

    public void setRowData(Row row) {
        rowData = row;
    }

	public void finalize() throws Throwable {

	}

	/**
	 * 
	 * @param clock
	 * @param rowData
	 */
	public ClientRow(int clock, Row rowData){
        this.rowData = rowData;
	}

	public void decRef(){

	}

	public int getClock(){
		return -1;
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