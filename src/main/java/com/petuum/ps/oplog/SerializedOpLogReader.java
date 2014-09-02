package com.petuum.ps.oplog;

import com.petuum.ps.common.util.BoolBox;
import com.petuum.ps.common.util.IntBox;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by ZengJichuan on 2014/8/30.
 */
public class SerializedOpLogReader {
    private ByteBuffer serializedOpLogBuf;
    private int updateSize;
    /**
     * bytes to be read next
     */
    private int offset;
    /**
     * number of tables that I have not finished
     */
    private int numTableLeft;
    private int currentTableId;
    private int numRowsLeftInCurrentTable;

    private static Logger log = LogManager.getLogger(SerializedOpLogReader.class);

    public SerializedOpLogReader(ByteBuffer serializedOpLogBuf) {
        this.serializedOpLogBuf = serializedOpLogBuf;
    }

    public boolean restart(){
        offset = 0;
        numTableLeft = serializedOpLogBuf.getInt(offset);
        offset += Integer.SIZE;
        log.info("SerializedOpLogReader Restart(), num_tables_left = " + numTableLeft);
        if(numTableLeft == 0)
            return false;
        startNewTable();
        return true;
    }

    public Map<Integer, Object> next(IntBox tableId, IntBox rowId, BoolBox startedNewTable){
        // I have read all
        if(numTableLeft == 0)   return null;
        startedNewTable.boolValue = false;
        int numUpdates = 0;
        Map<Integer, Object> updates;
        while(true){
            // can read from current row
            if(numRowsLeftInCurrentTable > 0){
                updates = new HashMap<Integer, Object>();
                tableId.intValue = currentTableId;
                rowId.intValue = serializedOpLogBuf.getInt(offset);
                offset += Integer.SIZE;
                numUpdates = serializedOpLogBuf.getInt(offset);
                offset += Integer.SIZE;
                for (int i = 0 ; i< numUpdates; i++){
                    int columnId = serializedOpLogBuf.getInt(offset + i * Integer.SIZE);
                    byte [] updateBytes = new byte[updateSize];
                    serializedOpLogBuf.get(updateBytes,
                            offset + numUpdates * Integer.SIZE + i * updateSize, updateSize);
                    Object update = SerializationUtils.deserialize(updateBytes);
                    updates.put(columnId, update);
                }
                offset += numUpdates * (Integer.SIZE + updateSize);
                return updates;
            }else{
                numTableLeft --;
                if(numTableLeft > 0){
                    startNewTable();
                    startedNewTable.boolValue = true;
                    continue;
                }else
                    return null;
            }
        }
    }
    private void startNewTable() {
        currentTableId = serializedOpLogBuf.getInt(offset);
        offset += Integer.SIZE;
        updateSize = serializedOpLogBuf.getInt(offset);
        offset += Integer.SIZE;

        numRowsLeftInCurrentTable = serializedOpLogBuf.getInt(offset);
        offset += Integer.SIZE;
        log.info("current_table_id = " + currentTableId + " update_size = "+ updateSize +
                " rows_left_in_current_table_ = "+numRowsLeftInCurrentTable);
    }
}
