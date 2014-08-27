package com.petuum.ps.server;

import static com.google.common.base.Preconditions.*;
import com.petuum.ps.common.Constants;
import com.petuum.ps.common.TableInfo;
import com.petuum.ps.common.util.VectorClock;
import com.petuum.ps.thread.GlobalContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
/**
 * Created by admin on 2014/8/7.
 */
public class Server {
    private VectorClock clientClocks;
    private Map<Integer, VectorClock> clientVectorClockMap;
    private Map<Integer, Vector<Integer>> clientBgMap;
    private HashMap<Integer, ServerTable> tables;
    // mapping <clock, table id> to an array of read requests
  //  private Map<Integer, HashMap<Integer, Vector<ServerRowRequest>>> clockBgRowRequests;
    private Vector<Integer> clinetIds;
    // latest oplog version that I have received from a bg thread
    private Map<Integer, Integer> bgVersionMap;
    // Assume a single row does not exceed this size!
    private static final int kPushRowMsgSizeInit = 4 *
            Constants.K_TOW_TO_POWER_TEN * Constants.K_NUM_BITS_PER_BYTE;
    private int pushRowMsgDataSize;
    private int serverId;

    public Server() {
    }
    public void init(int serverId){
        for (Map.Entry<Integer, Vector<Integer>> entry : clientBgMap.entrySet()){
            VectorClock vectorClock = new VectorClock(entry.getValue());
            clientVectorClockMap.put(entry.getKey(), vectorClock);
            for (int bg : entry.getValue()){
                bgVersionMap.put(bg, -1);
            }
        }
        pushRowMsgDataSize = kPushRowMsgSizeInit;
        this.serverId = serverId;
    }
    public void addClientBgPair(int clientId, int bgId){
        clientBgMap.get(clientId).add(bgId);
        clinetIds.add(clientId);
        clientClocks.addClock(clientId, 0);
    }
    public void CreateTable(int tableId, TableInfo tableInfo){
        tables.put(tableId, new ServerTable(tableInfo));//putIfAbsent

        if (GlobalContext.getResumeClock() > 0){
          //  tables.get(tableId).readSnapShot(GlobalContext.getResumeDir(),
         //           serverId, tableId, GlobalContext.getReusmeClock());
        }
    }
    public ServerRow findCreateRow(int tableId, int rowId){
        ServerTable serverTable = checkNotNull(tables.get(tableId));
        ServerRow serverRow = serverTable.findRow(rowId);
        if(serverRow != null){
            return serverRow;
        }
        serverRow = serverTable.createRow(rowId);
        return serverRow;
    }
    public boolean clock(int clientId, int bgId){
        int newClock = clientVectorClockMap.get(clientId).tick(bgId);
        if (newClock == 0)
            return false;
        newClock = clientClocks.tick(clientId);
        if(newClock != 0){
            if (GlobalContext.getSnapshotClock() <= 0
                    || newClock % GlobalContext.getSnapshotClock() != 0){
                return true;
            }
            for (Map.Entry<Integer, ServerTable> entry : tables.entrySet()){
                entry.getValue().takeSnapshot(GlobalContext.getSnapshotDir(),serverId,
                        entry.getKey(), newClock );
            }
            return true;
        }
        return false;
    }
}
