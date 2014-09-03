package com.petuum.ps.common.storage;

import com.petuum.ps.common.Row;

import java.util.Map;

/**
 * Created by ZengJichuan on 2014/9/2.
 */
//TODO(yxsu): I will write DenseRow

public class DenseRow implements Row {
    public double addUpdates(int column_id, double update1, double update2) {
        return  null;
    }

    public void applyBatchInc(Map<Integer, Double> update_batch) {

    }

    public int getUpdateSize() {
        return Double.SIZE;
    }

    public void applyBatchIncUnsafe(Map<Integer, Double> update_batch) {

    }

    public void applyInc(int column_id, double update) {

    }

    public void applyIncUnsafe(int column_id, double update) {

    }

    public void init(int capacity) {

    }

    public void initUpdate(int column_id, double zero) {

    }

    public double get(int columnId) {

    }

    public double subtractUpdates(int column_id, double update1, double update2) {
        return ;
    }
}
