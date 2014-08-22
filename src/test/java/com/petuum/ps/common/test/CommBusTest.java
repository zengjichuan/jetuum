package com.petuum.ps.common.test;

import com.petuum.ps.common.comm.CommBus;
import com.petuum.ps.common.util.IntBox;
import org.junit.Test;
import zmq.Msg;

import java.nio.*;

/**
 * Created by Yuxin Su on 2014/8/21.
 */
public class CommBusTest {
    private static CommBus commBus = new CommBus(0, 1, 1);

    public static Thread client0 = new Thread(new Runnable() {
        @Override
        public void run() {
            CommBus.Config config = new CommBus.Config(0, CommBus.K_IN_PROC, "127.0.0.1:10000");
            commBus.threadRegister(config);
            System.out.println("Client 0 is ready to accept connection");
            IntBox box = new IntBox();
            Msg msg = new Msg();
            while(true) {
                commBus.recv(box, msg);
                System.out.println(box.intValue);
            }
        }
    });

    public static Thread client1 = new Thread(new Runnable() {
        @Override
        public void run() {
            CommBus.Config config = new CommBus.Config(1, CommBus.K_IN_PROC, "127.0.0.1:10001");
            commBus.threadRegister(config);
            System.out.println("Client 1 is ready to accept connection");
            //send
            commBus.send(0, new Msg());
        }
    });

    public static void main(String[] args) throws InterruptedException {
        client0.start();
        client1.start();
        client0.join();
    }
}