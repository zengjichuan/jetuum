package com.petuum.ps;

import com.petuum.ps.common.NumberedMsg;
import com.petuum.ps.common.util.IntBox;

import java.nio.*;
import java.util.Objects;

/**
 * Created by admin on 2014/8/6.
 */
public class MyTest {

    public static void sum(IntBox one) {
        one.intValue++;
    }
    public static void main(String args[]){
        ByteBuffer one = ByteBuffer.allocate(4);

        one.putInt(10);
        System.out.println(one.getInt(0));

    }

}
