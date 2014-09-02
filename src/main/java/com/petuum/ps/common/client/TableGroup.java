package com.petuum.ps.common.client;
import com.petuum.ps.common.comm.CommBus;
import com.petuum.ps.common.util.VectorClockMT;
import com.petuum.ps.common.ClientTableConfig;
import com.petuum.ps.common.TableGroupConfig;
import com.petuum.ps.server.NameNodeThread;
import com.petuum.ps.server.ServerThreads;
import com.petuum.ps.thread.BgWorkers;
import com.petuum.ps.thread.GlobalContext;
import com.petuum.ps.thread.ThreadContext;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
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
	private  int maxTableStaleness;
	private  AtomicInteger numAppThreadsRegistered;
	private  Map<Integer, ClientTable> tables;
	private  VectorClockMT vectorClock;
    private Method clockInternal;
    private CyclicBarrier registerBarrier;

	/**
	 *
	 * @param tableGroupConfig
	 * @param tableAccess
	 */
	public TableGroup(final TableGroupConfig tableGroupConfig, boolean tableAccess, Integer initThreadID) throws NoSuchMethodException, InterruptedException, BrokenBarrierException {
        GlobalContext.init(tableGroupConfig.numTotalServerThreads,
                tableGroupConfig.numLocalServerThreads,
                tableGroupConfig.numLocalAppThreads,
                tableAccess ? tableGroupConfig.numLocalAppThreads : tableGroupConfig.numLocalAppThreads - 1,
                tableGroupConfig.numLocalBgThreads,
                tableGroupConfig.numTotalBgThreads,
                tableGroupConfig.numTables,
                tableGroupConfig.numTotalClients,
                tableGroupConfig.serverIds,
                tableGroupConfig.hostMap,
                tableGroupConfig.clientId,
                tableGroupConfig.serverRingSize,
                tableGroupConfig.consistencyModel,
                tableGroupConfig.aggressiveClock);
        numAppThreadsRegistered.set(1);
        int localIDMin = GlobalContext.getThreadIdMin(tableGroupConfig.clientId);
        initThreadID = localIDMin + GlobalContext.K_INIT_THREAD_ID_OFFSET;
        CommBus.Config config = new CommBus.Config(initThreadID, CommBus.K_NONE, "");
        GlobalContext.commBus.threadRegister(config);

        if(GlobalContext.getNameNodeClientId() == tableGroupConfig.clientId) {
            NameNodeThread.init();
            ServerThreads.init(localIDMin + 1);
        } else {
            ServerThreads.init(localIDMin);
        }

        BgWorkers.init(tables);
        ThreadContext.registerThread(initThreadID);
        if(tableAccess) {
            vectorClock.addClock(initThreadID, 0);
        }
        if(tableGroupConfig.aggressiveClock) {
            clockInternal = TableGroup.class.getMethod("clockAggressive");
        } else {
            clockInternal = TableGroup.class.getMethod("clockConservative");
        }
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
        if(table_config.tableInfo.tableStaleness > maxTableStaleness) {
            maxTableStaleness = table_config.tableInfo.tableStaleness;
        }

        boolean suc = BgWorkers.createTable(table_id, table_config);
        if(suc && (GlobalContext.getNumAppThreads() == GlobalContext.getNumTableThreads())) {
            tables.get(table_id).registerThread();
        }
        return suc;
	}

	public void createTableDone() throws BrokenBarrierException, InterruptedException {

        BgWorkers.waitCreateTable();
        registerBarrier = new CyclicBarrier(GlobalContext.getNumTableThreads());

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

	public int registerThread() throws BrokenBarrierException, InterruptedException {
        int appThreadIdOffset = numAppThreadsRegistered.getAndIncrement();

        int threadId = GlobalContext.getLocalIdMin() + GlobalContext.K_INIT_THREAD_ID_OFFSET + appThreadIdOffset;

        CommBus.Config config = new CommBus.Config(threadId, CommBus.K_NONE, "");
        GlobalContext.commBus.threadRegister(config);

        ThreadContext.registerThread(threadId);

        BgWorkers.threadRegister();
        vectorClock.addClock(threadId, 0);

        for(Map.Entry<Integer, ClientTable> table : tables.entrySet()) {
            table.getValue().registerThread();
        }

        registerBarrier.await();
        return threadId;
	}

	public void waitThreadRegister() throws BrokenBarrierException, InterruptedException {

        if(GlobalContext.getNumTableThreads() == GlobalContext.getNumAppThreads()) {
            registerBarrier.await();
        }

	}

}