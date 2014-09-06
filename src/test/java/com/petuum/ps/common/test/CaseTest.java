package com.petuum.ps.common.test;

/**
 * Created by suyuxin on 14-8-30.
 */
public class CaseTest {
    public static void main(String[] args) {
       String one = "0";
        System.out.println(one);
        String two = String.valueOf(0);
        if(one.equals(two)) {
            System.out.println("equal");
        }
    }
}
