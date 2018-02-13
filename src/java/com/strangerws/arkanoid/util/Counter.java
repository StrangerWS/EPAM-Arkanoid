package com.strangerws.arkanoid.util;

public class Counter implements Runnable{
    private long counter = 0;

    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    @Override
    public void run() {
        counter++;
    }
}
