package com.petuum.app;

import com.petuum.ps.common.ClientTableConfig;
import com.petuum.ps.common.PSTableGroup;
import com.petuum.ps.common.TableGroupConfig;
import com.petuum.ps.common.client.TableGroup;
import com.petuum.ps.common.consistency.ConsistencyModel;

import java.io.IOException;

/**
* Created by suyuxin on 14-8-23.
*/
public class MatrixFact {
    private static String hostFile = "localserver";
    private static String dataFile = "3x3_9blocks";
    private static String outputPrefix = "test";
    private static double lambda = 0.0;
    private static double initStepSize = 0.5;
    private static double stepSizeOffset = 100;
    private static double stepSizePow = 0.5;
    private static int rngSeed = 967234;
    private static int numClient = 1;
    private static int numWorkerThreads = 1;
    private static int clientID = 0;
    private static int K = 100;
    private static int numIterations = 100;
    private static int staleness = 0;

    public static void main(String[] args) throws Exception {
        //configure Petuum PS
        TableGroupConfig tableGroupconfig = new TableGroupConfig();
        tableGroupconfig.numTotalServerThreads = numClient;
        tableGroupconfig.numTotalBgThreads = numClient;
        tableGroupconfig.numTotalClients = numClient;
        tableGroupconfig.numTables = 3;//L_table, R_table, loss_table
        tableGroupconfig.getHostInfos(hostFile);
        tableGroupconfig.consistencyModel = ConsistencyModel.SSPPush;
        //local parameters for this process
        tableGroupconfig.numLocalServerThreads = 1;
        tableGroupconfig.numLocalBgThreads = 1;
        tableGroupconfig.numLocalAppThreads = numWorkerThreads + 1;
        tableGroupconfig.clientId = clientID;
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
        tableConfig.tableInfo.rowCapacity = K;
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
