package com.petuum.ps.common.client;

/**
 * Created by admin on 2014/8/15.
 */
public interface AbstractTableGroup {
    public boolean createTable(int tableId, ClientTableConfig tableConfig);
    public void createTableDone();
    public void waitThreadRegister();
    public AbstractClientTable getTableOrDie(int tableId);
    public int registerThread();
    public void deRegisterThread();
    public void Clock();
    public void globalBarrier();
}
