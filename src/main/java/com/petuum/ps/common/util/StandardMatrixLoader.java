package com.petuum.ps.common.util;

import java.util.Vector;

/**
 * Created by suyuxin on 14-9-3.
 */
public class StandardMatrixLoader implements MatrixLoader{
    private long n_;
    private long m_;
    private Vector<Long> xRow;
    private Vector<Long> xCol;
    private Vector<Float> xVal;

    private int numWorkers;
    private Vector<Integer> workerNextElPos;

    public StandardMatrixLoader(String inputFile, int numWorkers) {
        this.numWorkers = numWorkers;
        this.workerNextElPos = new Vector<Integer>(numWorkers);
    }
}
