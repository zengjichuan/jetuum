package com.petuum.ps.common.storage;

import com.petuum.ps.common.Row;

import java.util.Map;

/**
 * Created by ZengJichuan on 2014/9/2.
 */
//TODO(yxsu): I will write DenseRow

public class DenseRow implements Row {


    public Double addUpdates(int column_id, Double update1, Double update2) {
        return null;
    }

    public void applyBatchInc(Map<Integer, Double> update_batch) {

    }

    public void applyBatchIncUnsafe(Map<Integer, Double> update_batch) {

    }

    public void applyInc(int column_id, Double update) {

    }

    public void applyIncUnsafe(int column_id, Double update) {

    }

    public int getUpdateSize() {
        return 0;
    }

    public void init(int capacity) {

    }

    public void initUpdate(int column_id, Double zero) {

    }

    public Double subtractUpdates(int column_id, Double update1, Double update2) {
        return null;
    }

    public Double get(int columnId){
        return null;
    }
}
