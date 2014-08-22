package com.petuum.ps.server;


import com.google.common.primitives.Ints;
import com.petuum.ps.common.AbstractRow;
import com.petuum.ps.common.Row;
import com.petuum.ps.common.util.RecordBuff;

import java.util.HashMap;
import java.util.Objects;

/**
* Disallow copy to avoid shared ownership of row_data.
* Allow move sematic for it to be stored in STL containers.
* Created by Zengjichuan on 2014/8/5.
*/
public class ServerRow {
    private CallBackSubs callBackSubs;
    private Row rowData;
    private int numClientsSubscribed;
    private boolean dirty;

    public ServerRow(boolean dirty) {
        this.dirty = dirty;
    }

    public ServerRow(Row rowData) {
        this.rowData = rowData;
        this.numClientsSubscribed=0;
        this.dirty=false;
    }
    public void applyBatchInc(Integer columnIds, Object updateBatch, int numUpdate){
        rowData.applyBatchIncUnsafe(columnIds, updateBatch, numUpdate);
        dirty = true;
    }
    public int serializedSize(){   //const
        return rowData.getSerializedSize();
    }
    public int serialize(Byte[] bytes){      //const
        return rowData.serialize(bytes);
    }
    public void subscribe(int clientId){
        if(callBackSubs.subscribe(clientId))
            ++numClientsSubscribed;
    }
    public boolean noClientSubscribed(){
        return (numClientsSubscribed==0);
    }
    public void unsubscribe(int clientId){
        if (callBackSubs.unsubscribe(clientId))
            --numClientsSubscribed;
    }
    public boolean appendRowToBuffs(int clientIdSt,
                             HashMap<Integer, RecordBuff> buffs,
                             Objects rowData, int rowSize, int rowId,
                             Integer failedBgId, Integer failedClientId){
        return callBackSubs.appendRowToBuffs(clientIdSt, buffs, rowData, rowSize, rowId,
                failedBgId, failedClientId);
    }
    public boolean isDirty(){
        return dirty;
    }
    public void resetDirty(){
        dirty = false;
    }
}
