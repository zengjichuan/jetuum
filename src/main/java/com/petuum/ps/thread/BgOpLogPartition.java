package com.petuum.ps.thread;

import com.petuum.ps.common.oplog.RowOpLog;
import com.petuum.ps.common.util.IntBox;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zjc on 2014/8/14.
 */
public class BgOpLogPartition {
    private HashMap<Integer, RowOpLog> opLogMap;
    private int tableId;
    private int updateSize;

    public BgOpLogPartition(int tableId, int updateSize) {
        this.tableId = tableId;
        this.updateSize = updateSize;
    }
    public RowOpLog findOpLog(int rowId){
        return opLogMap.get(rowId);
    }
    public void insertOpLog(int rowId, RowOpLog rowOpLog){
        opLogMap.put(rowId, rowOpLog);
    }

    public void serializedByServer(Map<Integer, ByteBuffer> bufferByServer) {
        Map<Integer, Integer> offsetByServer = new HashMap<Integer, Integer>();
        // init number of rows to 0
        for (int serverId : GlobalContext.getServerIds()){
            offsetByServer.put(serverId, Integer.SIZE);
            bufferByServer.get(serverId).putInt(0);
        }
        for (HashMap.Entry<Integer, RowOpLog> entry : opLogMap.entrySet()){
            int rowId = entry.getKey();
            int serverId = GlobalContext.getRowPartitionServerId(tableId, rowId);
            RowOpLog rowOpLog = entry.getValue();

            ByteBuffer mem = bufferByServer.get(serverId);
            mem.position(offsetByServer.get(serverId));

            mem.putInt(rowId);                          //rowId
            int numUpdates = rowOpLog.getSize();
            mem.putInt(numUpdates);             //mem update number
            int memColIdOffset = mem.position();
            int memUpdatesOffset = mem.position() + numUpdates * Integer.SIZE;

            IntBox columnId = new IntBox();
            Object update = rowOpLog.beginIterate(columnId);
            while(update != null){
                mem.putInt(memColIdOffset, columnId.intValue);
                memColIdOffset += Integer.SIZE;

                mem.put(SerializationUtils.serialize((Serializable) update), memUpdatesOffset, updateSize);
                update = rowOpLog.next(columnId);
                memUpdatesOffset += updateSize;
            }
            offsetByServer.put(serverId, offsetByServer.get(serverId) + Integer.SIZE + Integer.SIZE +
                    (Integer.SIZE + updateSize) * numUpdates);
            mem.putInt(0, mem.getInt(0)+1);         //row num add 1
        }
    }
}
