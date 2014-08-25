package com.petuum.ps.common.oplog;

import com.petuum.ps.common.Row;
import com.petuum.ps.common.util.IntBox;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Created by admin on 2014/8/18.
 */
public class RowOpLog {
//    private int updateSize;
    private Map<Integer, Object> opLogs;
    private Method initUpdate;
    private Iterator<Map.Entry<Integer, Object>> iter;
    public RowOpLog(Method initUpdate){
        this.initUpdate = initUpdate;
    }

    public Object beginIterate(IntBox columnId){
        iter = opLogs.entrySet().iterator();
        if(!iter.hasNext()) return null;
        Map.Entry<Integer, Object> entry = iter.next();
        columnId.intValue = entry.getKey();
        return entry.getValue();
    }

    public Object find(int columnId){
        return opLogs.get(columnId);
    }

    public Object findCreate(int columnId, Row sampleRow){
        Object rst = opLogs.get(columnId);
        if(rst == null){
            Object update = new Object();
            try {
                initUpdate.invoke(sampleRow, new Object[]{columnId, update});
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
    public Object next(IntBox columnId){
        if(!iter.hasNext()) return null;
        Map.Entry<Integer, Object> entry = iter.next();
        columnId.intValue = entry.getKey();
        return entry.getValue();
    }

    public int getSize(){
        return opLogs.size();
    }
}
