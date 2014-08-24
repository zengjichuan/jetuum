package com.petuum.ps;

import com.petuum.ps.common.MsgType;
import com.petuum.ps.common.util.IntBox;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by admin on 2014/8/6.
 */
public class MyTest {

    public static void sum(IntBox one) {
        one.intValue++;
    }
    public static void main(String args[]){
        IntBox tint = new IntBox();
        System.out.println(tint.intValue);

        sum(tint);


        System.out.println(tint.intValue);

        MsgType mt = MsgType.valueOf(Objects.toString(MsgType.K_APP_CONNECT));
        if(mt.equals(MsgType.K_APP_CONNECT)){
            System.out.println("Success");
        }

    }

}
