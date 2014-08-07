package com.petuum.ps.common;

import java.util.Map;

/**
 * Created by Su Yuxin on 2014/8/5.
 */
public interface ConsistencyController {

    void getAsync(int row_id);
    void waitPendingAsyncGet();
    Row get(int row_id);
    void inc(int row_id, int column_id, Object delta);
    void batchInc(int row_id, Map<Integer, Object> updates);
}
