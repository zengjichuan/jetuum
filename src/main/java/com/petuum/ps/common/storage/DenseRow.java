package com.petuum.ps.common.storage;

import com.petuum.ps.common.Row;

import java.util.Map;

/**
 * Created by ZengJichuan on 2014/9/2.
 */
//TODO(yxsu): I will write DenseRow

public class DenseRow<V extends Number> implements Row<V> {
    public V addUpdates(int column_id, V update1, V update2) {
        return  null;
    }

    public void applyBatchInc(Map<Integer, V> update_batch) {

    }

    public void applyBatchIncUnsafe(Map<Integer, V> update_batch) {

    }

    public void applyInc(int column_id, V update) {

    }

    public void applyIncUnsafe(int column_id, V update) {

    }

    public void init(int capacity) {

    }

    public void initUpdate(int column_id, V zero) {

    }

    public V get(int columnId) {

    }

    public V subtractUpdates(int column_id, V update1, V update2) {
        return null;
    }
}
