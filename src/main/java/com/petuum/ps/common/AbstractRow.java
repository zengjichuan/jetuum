package com.petuum.ps.common;

import java.util.Map;

/**
 * Created by zengjichuan on 2014/8/7.
 */
public interface AbstractRow {
    void init(int capacity);
    int getUpdateSize();
    int getSerializedSize();
    int serialize(Byte[] bytes);
    boolean deserialize(Byte[] bytes);
    void applyInc(int columnID, Object[] update);
    void applyBatchInc(Map<Integer, Object> batches);
    void applyIncUnsafe(int columnID, Object update);
    void applyBatchIncUnsafe(Map<Integer, Object> batches);
    void addUpdates(int columnID, Object update1, Object update2);
    void subtractUpdate(int columnID, Object update1, Object update2);
    void initUpdate(int columnID, Object zero);
}
