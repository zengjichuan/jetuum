package com.petuum.ps.thread;

import com.petuum.ps.common.util.IntBox;

import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Administrator on 2014/8/20.
 */
public class SSPRowRequestOpLogMgr implements RowRequestOpLogMgr {
    /**
     * map <table_id, row_id> to a list of requests
     * The list is in increasing order of clock
     */
    private Map<TableRowIndex, List<RowRequestInfo>> pendingRowRequest;
    /**
     * version -> (table_id, OpLogPartition)
     * The version number of a request means that all oplogs up to and including
     * this version have been applied to this row.
     * An OpLogPartition of version V is needed for requests sent before the oplog
     * is sent. This means requests of version V - 1, V - 2, ...
     */
    private Map<Integer, BgOpLog> versionOpLogMap;


    // used for OpLogIter

    private Map<Integer, Integer> versionRequestCntMap;

    public boolean addRowRequest(RowRequestInfo request, int tableId, int rowId) {
        return false;
    }

    public int informReply(int tableId, int rowId, int clock, int currentVersion, Vector<Integer> appThreadIds) {
        return 0;
    }

    public BgOpLog getOpLog(int version) {
        return null;
    }

    public void informVersionInc() {

    }

    public void serverAcknowledgeVersion(int serverId, int version) {

    }

    public boolean addOpLog(int version, BgOpLog opLog) {
        return false;
    }

    public BgOpLog opLogIterInit(int startVersion, int endVersion) {
        return null;
    }

    public BgOpLog opLogiterNext(IntBox version) {
        return null;
    }
}

