package com.petuum.ps.common.test;

import com.petuum.ps.common.MsgType;
import com.petuum.ps.common.comm.CommBus;
import com.petuum.ps.common.util.IntBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import zmq.Msg;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.*;
import java.util.Objects;

/**
 * Created by Yuxin Su on 2014/8/21.
 */
public class CommBusTest {
    private static CommBus commBus = new CommBus(0, 200, 1);

    public static Thread client0 = new Thread(new Runnable() {
        public void run() {
            CommBus.Config config = new CommBus.Config(0, CommBus.K_IN_PROC, "");
            commBus.threadRegister(config);
            System.out.println("Client 0 is ready to accept connection");
            IntBox box = new IntBox();
            Msg msg = new Msg();
            while(true) {
                commBus.recvInproc(box, msg);
                System.out.println(box.intValue);
            }
        }
    });

    public static Thread client1 = new Thread(new Runnable() {
        public void run() {
            CommBus.Config config = new CommBus.Config(1, CommBus.K_IN_PROC, "0");
            commBus.threadRegister(config);
            System.out.println("Client 1 is ready to accept connection");
            //send
            commBus.sendInproc(0, new Msg());
        }
    });

    public static void main(String[] args) throws InterruptedException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        client0.start();
        client1.start();
        client0.join();
        Method CommBusSendAny = CommBus.class.getMethod("Send", Integer.class, Msg.class);
        CommBusSendAny.invoke(commBus, 10, new Msg());
        ByteBuffer bb = ByteBuffer.allocate(4);
        Msg msg = new Msg();

    }
}