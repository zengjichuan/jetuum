package com.petuum.ps.common.test;

import com.petuum.ps.common.comm.CommBus;
import org.junit.Test;
/**
 * Created by Yuxin Su on 2014/8/21.
 */
public class CommBusTest {
    private static CommBus commBus;
    private static CommBus.Config config;
    public static void comm() throws InterruptedException {
        commBus = new CommBus(0, 1, 1);
        config = new CommBus.Config(0, CommBus.K_NONE, "127.0.0.1:10000");
        commBus.threadRegister(config);
        System.out.println("Client 0 is ready to accept connection");
        while(true) {
            Thread.sleep(1000);
        }
    }

    public static void main(String[] args) {
        try {
            comm();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}