package com.petuum.ps.common.util;

/**
 * Created by suyuxin on 14-9-3.
 */

public interface MatrixLoader {
    public class Element {
        public long row;
        public long col;
        public float value;
        public boolean isLastEl;
    }

    public Element getNextEl(int workerId);

    public long getN();

    public long getM();

    public long getNNZ();

}
