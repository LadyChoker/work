package es.tid.pce.tests;

public class MyTester {
    public static void main(String[] args) {
        Runnable task1 = new Runnable() {
            @Override
            public void run() {
                System.out.println("here");
            }
        };
        Thread t1 = new Thread(task1);
        t1.start();
    }
}
