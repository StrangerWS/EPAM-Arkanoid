package com.strangerws.arkanoid.model;

public class Counter implements Runnable{

    private int counter = 0;
    private int score = 0;


    public int getCounter() {
        return counter;
    }
    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public void run() {
        try {
            counter++;
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
