package com.tdt.springbatch.thread;

import com.tdt.springbatch.service.OrderService;

public class SimpleThread implements Runnable {

    private OrderService orderService;

    private String threadName;

    private int recordsStart;

    private int recordsStop;

    public SimpleThread(String name, int recordsStart, int recordsStop, OrderService orderService) {
        this.orderService = orderService;
        this.threadName = name;
        this.recordsStart = recordsStart;
        this.recordsStop = recordsStop;
        System.out.println("Creating " + threadName);
    }

    @Override
    public void run() {
        System.out.println("Running " + threadName);
        Long a = System.currentTimeMillis();
        orderService.batchInsert(this.recordsStart, this.recordsStop);
        Long b = System.currentTimeMillis();
        System.out.println("Batch insert thread " + threadName + " : " + (b-a)*1000);
    }

    public void start() {
        System.out.println("Starting " + threadName);
        this.run();
    }
}
