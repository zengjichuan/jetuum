package com.petuum.ps.common.storage;

import com.petuum.ps.common.Row;
import org.apache.commons.lang3.SerializationUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by ZengJichuan on 2014/9/2.
 */
public class SparseRow<V extends Number> implements Row<V>, Iterable<Map.Entry<Integer, V>> {

    private ReadWriteLock lock;

    private int updateSize;

    Map<Integer, V> rowData;

    public SparseRow(V sampleElem) {
        this.lock = new ReentrantReadWriteLock();
        this.updateSize = SerializationUtils.serialize(sampleElem).length;
        this.rowData = new HashMap<Integer, V>();
    }

    public V get(int columnId){
        try {
            lock.readLock().lock();
            return rowData.getOrDefault(columnId, (V)(Integer.valueOf(0)));
        }finally {
            lock.readLock().unlock();
        }
    }

    public int numEntries(){
        try{
            lock.readLock().lock();
            return rowData.size();
        }finally {
            lock.readLock().unlock();
        }
    }

    public V addUpdates(int column_id, V update1, V update2) {
        // Ignore column_id
        double sum = ((V)update1).doubleValue()+((V)update2).doubleValue();
        return (V)Double.valueOf(sum);
    }

    public void applyBatchInc(Map<Integer, V> updateBatch) {
        try{
            lock.writeLock().lock();
            applyBatchIncUnsafe(updateBatch);
        }finally {
            lock.writeLock().unlock();
        }

    }

    public void applyBatchIncUnsafe(Map<Integer, V> updateBatch) {
        for (Map.Entry<Integer, V> entry : updateBatch.entrySet()){
            int columnId = entry.getKey();
            rowData.put(columnId, (V)Double.valueOf(
                    rowData.getOrDefault(columnId, (V) Double.valueOf(0)).doubleValue()
                    + ((V)entry.getValue()).doubleValue()));
            if (Math.abs(rowData.get(columnId).doubleValue()) < 1e-9){
                rowData.remove(columnId);
            }
        }
    }

    public void applyInc(int columnId, V update) {
        try {
            lock.writeLock().lock();
            applyIncUnsafe(columnId, update);
        }finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * convert Object to V, then to double and plus
     * @param column_id
     * @param update
     */
    public void applyIncUnsafe(int column_id, V update) {
        rowData.put(column_id, (V)Double.valueOf(update.doubleValue() +
                rowData.getOrDefault(column_id, (V)(Double.valueOf(0))).doubleValue()));
        if(Math.abs(rowData.get(column_id).doubleValue()) < 1e-9){
            rowData.remove(column_id);
        }
    }

    /**
     * the size of V is not what you can see, i.e. Integer:81, Short:77, Double:84, Float:79
     * @return
     */
    public int getUpdateSize() {
        return updateSize;
    }

    public void init(int capacity) {

    }

    public void initUpdate(int column_id, V zero) {

    }


    public V subtractUpdates(int column_id, V update1, V update2) {
        // Ignore column_id
        double sum = update1.doubleValue() - update2.doubleValue();
        return (V)Double.valueOf(sum);
    }

    // ======== const_iterator Implementation ========
    public Iterator<Map.Entry<Integer, V>> iterator() {
        final Iterator<Map.Entry<Integer, V>> iter = new Iterator<Map.Entry<Integer, V>>() {
            private Iterator<Map.Entry<Integer, V>> mapIter = rowData.entrySet().iterator();
            public boolean hasNext() {
                return mapIter.hasNext();
            }

            public Map.Entry<Integer, V> next() {
                return mapIter.next();
            }
        };
        return iter;
    }
}
