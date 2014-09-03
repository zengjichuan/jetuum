package com.petuum.ps.common.test;

import com.petuum.ps.common.storage.SparseRow;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ZengJichuan on 2014/9/3.
 */
public class TestStorage {
    public static void main(String args []){
         fRow = new SparseRow<Float>(0f);
        fRow.applyInc(0, 1f);
        Map<Integer, Float> batch = new HashMap<Integer, Float>();
        batch.put(1,22f);batch.put(2,33f);batch.put(3, 44f);
        fRow.applyBatchInc(batch);
        System.out.println(fRow.get(0)+" "+fRow.get(1)+" "+fRow.get(2)+" "+fRow.get(3));
    }
}
