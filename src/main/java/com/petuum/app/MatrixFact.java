package com.petuum.app;

import com.petuum.ps.common.ClientTableConfig;
import com.petuum.ps.common.PSTableGroup;
import com.petuum.ps.common.TableGroupConfig;
import com.petuum.ps.common.client.ClientTable;
import com.petuum.ps.common.client.TableGroup;
import com.petuum.ps.common.consistency.ConsistencyModel;
import com.petuum.ps.common.storage.DenseRow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
* Created by suyuxin on 14-8-23.
*/
public class MatrixFact {
    private static String hostFile = "localserver";
    private static String dataFile = "3x3_9blocks";
    private static String outputPrefix = "test";
    private static float lambda = 0.0f;
    private static float initStepSize = 0.5f;
    private static float stepSizeOffset = 100f;
    private static float stepSizePow = 0.5f;
    private static int rngSeed = 967234;
    private static int numClient = 1;
    private static int numWorkerThreads = 1;
    private static int clientID = 0;
    private static int K = 100;
    private static int numIterations = 100;
    private static int staleness = 0;
//TODO(yxsu): write the working thread of MF App

    private void sgdElement(int i , int j, float xij, float stepSize, int globalWorkerId,
                            ClientTable tableL, ClientTable tableR, ClientTable tableLoss) {
//        //read L(i, :) and R(:, j) from Petuum PS
//        DenseRow<Float> li = (DenseRow)tableL.threadGet(i);
//        DenseRow<Float> rj = (DenseRow)tableR.threadGet(j);
//        //compute L(i, : ) * R(:, j)
//        float liRj = 0;
//        for(int k = 0; k < K; k++) {
//            liRj += li.get(k) * rj.get(k);
//        }
//        // Update the loss function (does not include L2 regularizer term)
//        tableLoss.inc(0, globalWorkerId, Math.pow(xij - liRj, 2));
//        // Now update L(i,:) and R(:,j) based on the loss function at X(i,j).
//        // The non-regularized loss function at X(i,j) is ( X(i,j) - L(i,:)*R(:,j) )^2.
//        //
//        // The non-regularized gradient w.r.t. L(i,k) is -2*X(i,j)R(k,j) + 2*L(i,:)*R(:,j)*R(k,j).
//        // The non-regularized gradient w.r.t. R(k,j) is -2*X(i,j)L(i,k) + 2*L(i,:)*R(:,j)*L(i,k).
//        Map<Integer, Float> liUpdate = new HashMap<Integer, Float>();
//        Map<Integer, Float> rjUpdate = new HashMap<Integer, Float>();
//        for(int k = 0; k < K; k++) {
//            float gradient = 0;
//            //compute update for L(i,k)
//            gradient = -2 * (xij - liRj) * rj.get(k) + lambda * 2 * li.get(k);
//            liUpdate.put(k, -gradient * stepSize);
//            //compute update for R(k, j)
//            gradient = -2 * (xij - liRj) * li.get(k) + lambda * 2 * rj.get(k);
//            rjUpdate.put(k, -gradient * stepSize);
//        }
//        //commit updates to Petuum PS
//        tableL.batchInc(i, liUpdate);
//        tableR.batchInc(j, rjUpdate);
    }

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
