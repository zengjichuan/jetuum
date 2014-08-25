package com.petuum.ps.server;

import com.google.common.io.ByteStreams;
import com.petuum.ps.common.TableInfo;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Objects;

import static com.google.common.base.Preconditions.*;

/**
 * Created by admin on 2014/8/7.
 */
public class ServerTable {
    private TableInfo tableInfo;
    private HashMap<Integer, ServerRow> storage;

    ByteBuffer tmpRowBuff;
    private int tmpRowBuffSize;
    private static final int K_TMP_ROW_BUFF_SIZE_INIT = 512;
    private int currRowSize;

    public ServerTable(TableInfo tableInfo) {
        this.tableInfo = tableInfo;
        tmpRowBuffSize = K_TMP_ROW_BUFF_SIZE_INIT;
    }

    public ServerRow findRow(int rowId){
        ServerRow row = storage.get(rowId);
        return row;         //could be null
    }

    public ServerRow createRow(int rowId){
//        int rowType = tableInfo.rowType;
//        AbstractRow rowData = ClassRegeistry<AbstractRow>.GetRegistry().CreateObject(rowType);
//        rowData.init(tableInfo.rowCapacity);
//        storage.put(rowId, new ServerRow(rowData));
        return storage.get(rowId);
    }

    public boolean applyRowOpLog(int rowId, Integer columnIds, Object updates, int numUpdates){
        ServerRow serverRow = storage.get(rowId);
        if (serverRow == null){
            return false;
        }
        serverRow.applyBatchInc(columnIds, updates, numUpdates);
        return true;
    }
    public void makeSnapshotFileName(String snapshotDir, int serverId, int tableId, int clock,
                                     StringBuffer filename){
        checkArgument(filename.length() == 0);
        filename.append(snapshotDir).append("/server_table").append(".server-").append(serverId)
                .append(".table-").append(tableId).append(".clock-").append(clock).append(".db");
    }
    public void takeSnapshot(String snapshotDir, int serverId, int tableId, int clock){
        StringBuffer dbName = new StringBuffer();
        makeSnapshotFileName(snapshotDir, serverId, tableId, clock, dbName);
        Options options = new Options();
        options.createIfMissing(true);
        try {
            DB db = Iq80DBFactory.factory.open(new File(dbName.toString()), options);
            //for
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
