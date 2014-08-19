package com.petuum.ps.thread;

import java.util.Map;

/**
 * Created by zjc on 2014/8/14.
 */
public class BgOpLog {
    private Map<Integer, BgOpLogPartition> tableOpLogMap;

    public BgOpLog() {
    }

    public void add(int tableId, BgOpLogPartition bgOpLogPartition){
        tableOpLogMap.put(tableId, bgOpLogPartition);
    }
    public BgOpLogPartition get(int tableId){
        return tableOpLogMap.get(tableId);
    }
}
