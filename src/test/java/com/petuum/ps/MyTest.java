package com.petuum.ps;

import com.petuum.ps.common.NumberedMsg;
import com.petuum.ps.common.util.IntBox;

import java.util.Objects;

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

        NumberedMsg mt = NumberedMsg.valueOf(Objects.toString(NumberedMsg.K_APP_CONNECT));
        if(mt.equals(NumberedMsg.K_APP_CONNECT)){
            System.out.println("Success");
        }

    }

}
