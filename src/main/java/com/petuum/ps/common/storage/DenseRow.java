package com.petuum.ps.common.storage;

import com.petuum.ps.common.Row;

import java.util.Map;

/**
 * Created by ZengJichuan on 2014/9/2.
 */
public class DenseRow implements Row {
    public void addUpdates(int column_id, Object update1, Object update2) {

    }

    public void applyBatchInc(Map<Integer, Object> update_batch) {

    }

    public void applyBatchIncUnsafe(Map<Integer, Object> update_batch) {

    }

    public void applyInc(int column_id, Object update) {

    }

    public void applyIncUnsafe(int column_id, Object update) {

    }

    public int getUpdateSize() {
        return 0;
    }

    public void init(int capacity) {

    }

    public void initUpdate(int column_id, Object zero) {

    }

    public void subtractUpdates(int column_id, Object update1, Object update2) {

    }
}
