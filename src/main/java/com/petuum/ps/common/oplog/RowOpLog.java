package com.petuum.ps.common.oplog;

import com.petuum.ps.common.Row;
import com.petuum.ps.common.storage.DenseRow;
import com.petuum.ps.common.util.IntBox;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Created by admin on 2014/8/18.
 */
public class RowOpLog implements Serializable {
//    private int updateSize;
    private HashMap<Integer, Double> opLogs;
    private Method initUpdate;
    private Iterator<Map.Entry<Integer, Double>> iter;
    public RowOpLog(Method initUpdate){
        this.initUpdate = initUpdate;
        opLogs = new HashMap<Integer, Double>();
    }

    public Double beginIterate(IntBox columnId){
        iter = opLogs.entrySet().iterator();
        if(!iter.hasNext()) return null;
        Map.Entry<Integer, Double> entry = iter.next();
        columnId.intValue = entry.getKey();
        return entry.getValue();
    }

    public Double find(int columnId){
        return opLogs.get(columnId);
    }

    public Double findCreate(int columnId, Row sampleRow){
        Double rst = opLogs.get(columnId);
        if(rst == null){
            Double update = new Double(0);
            try {
                initUpdate.invoke(sampleRow, columnId, update);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            opLogs.put(columnId, update);
            return update;
        }
        return rst;
    }
    public Double next(IntBox columnId){
        if(!iter.hasNext()) return null;
        Map.Entry<Integer, Double> entry = iter.next();
        columnId.intValue = entry.getKey();
        return entry.getValue();
    }

    public int getSize(){
        return opLogs.size();
    }

    public void insert(int columnId,Double update){
        opLogs.put(columnId, update);
    }

    public HashMap<Integer, Double> getMap() {
        return opLogs;
    }
}
