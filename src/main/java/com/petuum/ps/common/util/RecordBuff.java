package com.petuum.ps.common.util;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * A buffer that allows appending records to it. Here we use java.nio.ByteBuffer.
 * Created by zengjichuan on 2014/8/6.
 */
public class RecordBuff {
    private byte[] mem;
    private int memSize;
    private int offset;

    public RecordBuff(byte[] mem, int memSize) {
        this.mem = mem;
        this.memSize = memSize;
        this.offset = 0;
    }

    public byte[] resetMem(byte[] mem, int size){
        byte[] oldMem = mem;
        memSize = size;
        offset = 0;
        return oldMem;
    }

    public boolean append(int rowId, Objects rowData, int recordSize) {
        if(offset + recordSize +Integer.SIZE +Integer.SIZE> memSize){
            return false;
        }

        return false;
    }
}
