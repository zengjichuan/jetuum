package com.petuum.ps.common;

import com.petuum.ps.common.client.ClientTable;
import com.petuum.ps.common.client.TableGroup;

import java.util.concurrent.BrokenBarrierException;

/**
 * Created by suyuxin on 14-8-23.
 */
public class PSTableGroup {
    private static TableGroup tableGroup;

    public static void init(TableGroupConfig config, boolean tableAccess) throws NoSuchMethodException, InterruptedException, BrokenBarrierException {
        Integer initThreadID = new Integer(0);
        tableGroup = new TableGroup(config, tableAccess, initThreadID);
    }

    public static void shutDown() {
        tableGroup = null;
    }

    public static boolean createTable(int tabldID, ClientTableConfig config) {
        return tableGroup.createTable(tabldID, config);
    }

    public static void createTableDone() throws BrokenBarrierException, InterruptedException {
        tableGroup.createTableDone();
    }

    public static void waitThreadRegister() throws BrokenBarrierException, InterruptedException {
        tableGroup.waitThreadRegister();
    }

    public static ClientTable getTableOrDie(int tableID) {
        return tableGroup.getTableOrDie(tableID);
    }

    public static int registerThread() throws BrokenBarrierException, InterruptedException {
        return tableGroup.registerThread();
    }

    public static void deregisterThread() {
        tableGroup.deregisterThread();
    }

    public static void clock() {
        tableGroup.clock();
    }

    public static void globalBarrier() {
        tableGroup.globalBarrier();
    }
}
