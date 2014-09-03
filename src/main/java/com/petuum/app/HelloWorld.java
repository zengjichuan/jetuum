package com.petuum.app;

import com.petuum.ps.common.ClientTableConfig;
import com.petuum.ps.common.PSTableGroup;
import com.petuum.ps.common.TableGroupConfig;
import com.petuum.ps.common.consistency.ConsistencyModel;

import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * Created by ZengJichuan on 2014/9/3.
 */
public class HelloWorld {
    private static Path hostFile = FileSystems.getDefault().getPath("localserver");
    private static int numTotalServerThread = 1;
    private static int numTotalNumClient = 1;
    private static int numTotalBgThread = 1;
    private static int numTable = 1;
    private static int numServerThreads = 1;
    private static int numAppThread = 1;
    private static int numBgThread = 1;

    private static int clientId = 0;
//    private static int K = 100;
    private static int numIterations = 100;
    private static int staleness = 0;

    public static void main(String[] args) throws Exception{
        //configure Petuum PS
        TableGroupConfig tableGroupconfig = new TableGroupConfig();
        tableGroupconfig.numTotalServerThreads = numTotalServerThread;
        tableGroupconfig.numTotalBgThreads = numTotalBgThread;
        tableGroupconfig.numTotalClients = numTotalNumClient;
        tableGroupconfig.numTables = numTable;
        tableGroupconfig.getHostInfos(hostFile);

        tableGroupconfig.consistencyModel = ConsistencyModel.SSP;
        //local parameters for this process
        tableGroupconfig.numLocalServerThreads = numServerThreads;
        tableGroupconfig.numLocalBgThreads = numBgThread;
        tableGroupconfig.numLocalAppThreads = numAppThread + 1;
        tableGroupconfig.clientId = clientId;
        //need to register row type
        //register DenseRow<float> as 0.

        //next..
        PSTableGroup.init(tableGroupconfig, false);
        //load data


        //config ps table
        ClientTableConfig tableConfig = new ClientTableConfig();
        tableConfig.tableInfo.rowType = 0; //dense row
        tableConfig.opLogCapacity = 100;
        tableConfig.tableInfo.tableStaleness = staleness;
        tableConfig.tableInfo.rowCapacity = 100;
        tableConfig.processCacheCapacity = 100;
        PSTableGroup.createTable(0, tableConfig);
        //..

        //finished creating tables
        PSTableGroup.createTableDone();

        //run threads

        PSTableGroup.waitThreadRegister();

        //join

        //cleanup
        PSTableGroup.shutDown();
    }
}
class ThreadContext{
    private int numClients;
    private int clientId;
    private int appThreadIdOffset;
    private int numAppThreadsPerClient;
    private int numIterations;
    private int staleness;
    private int numTables;
    private int resumeClock;
}