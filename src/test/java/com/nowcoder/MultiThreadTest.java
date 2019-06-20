package com.nowcoder;


import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实现多线程的方法:
 * [1]自定义一个线程，继承了Thread类，然后 复写 run方法
 * [2]实现Runnable(),实现run()方法
 */
class MyThread extends Thread {
    private int tid;    //线程的ID

    public MyThread(int tid) {    //构造方法
        this.tid = tid;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 10; i++) {      //从0打印到10
                Thread.sleep(1000); //线程 睡眠1秒。注意单位是：毫秒
                System.out.println(String.format("%d:%d", tid, i));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


/**
 * 消费者线程
 */
class Consumer implements Runnable {
    private BlockingQueue<String> q;    //阻塞队列

    public Consumer(BlockingQueue<String> q) {
        this.q = q;
    }

    @Override
    public void run() {
        try {
            while (true) {    //一直从队列中去取（一直消费），然后打印
                System.out.println(Thread.currentThread().getName() + ":" + q.take());  //打印当前线程名称，并从阻塞队列中取出一个元素
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}

/**
 * 生产者线程
 */
class Producer implements Runnable {
    private BlockingQueue<String> q;    //通过BlockingQueue来实现异步，一边插入，一边消费

    public Producer(BlockingQueue<String> q) {
        this.q = q;
    }

    @Override
    public void run() {
        try {
            //插入
            for (int i = 0; i < 100; i++) {
                Thread.sleep(1000); //睡1秒
                q.put(String.valueOf(i));   //本轮循环，往队列中，放入一个数字i
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}

public class MultiThreadTest {




    public static void testThread() {
        /*实现多线程方式1 */
        for (int i = 0; i < 10; i++) {
            //new MyThread(i).start();    //启动10个线程，其中i是线程的id
        }

        /*实现多线程方式2 */
        for (int i = 0; i < 10; i++) {
            final int finalI = i;

            new Thread(new Runnable() {    //不用继承Thread了，直接通过匿名内部类，实现Thread
                @Override
                public void run() {
                    try {
                        for (int j = 0; j < 10; j++) {      //从0打印到10
                            Thread.sleep(1000); //线程 睡眠1秒。注意单位是：毫秒
                            System.out.println(String.format("T2 %d: %d", finalI, j));//在匿名内部类中，final变量才可见，普通的i不可见
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start(); //匿名内部类


        }

    }

    private static Object obj = new Object();

    public static void testSynchronized1() {
        synchronized (obj) {
            try {
                for (int j = 0; j < 10; j++) {      //从0打印到10
                    Thread.sleep(1000); //线程 睡眠1秒。注意单位是：毫秒
                    System.out.println(String.format("T3 %d", j));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void testSynchronized2() {
        synchronized (obj) {
            try {
                for (int j = 0; j < 10; j++) {      //从0打印到10
                    Thread.sleep(1000); //线程 睡眠1秒。注意单位是：毫秒
                    System.out.println(String.format("T4 %d", j));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void testSynchronized() {
        //在这个地方起各种线程去调用上面的方法
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    testSynchronized1();
                    testSynchronized2();    //输出结果中，T3全打完之后，T4才进来。T3释放了obj，T4才能进去
                }
            }).start();
        }

    }

    public static void testBlockingQueue() {
        BlockingQueue<String> q = new ArrayBlockingQueue<String>(10);//队列长度为10
        new Thread(new Producer(q)).start();
        new Thread(new Consumer(q), "Consumer1").start();
        new Thread(new Consumer(q), "Consumer2").start();//两条线程，交替去消费插入到BlockingQueue里面的数据

    }

    //ThreadLocal线程本地变量，即使是一个这种变量被static修饰，它也是每个变量都有一份
    public static ThreadLocal<Integer> threadLocalUserIds = new ThreadLocal<>();//线程本地变量，每一条变量都有自己的副本
    public static int userId;//仅仅是一个静态变量。只有一份

    public static void testThreadLocal() {

        for (int i = 0; i < 10; i++) {
            final int finalI = i;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        threadLocalUserIds.set(finalI); //起10条线程，每条线程设置线程ID。线程本地变量，每个线程有一个副本。
                        Thread.sleep(1000); //睡1秒
                        System.out.println("ThreadLocal:" + threadLocalUserIds.get()); //打印线程ID
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }

        for (int i = 0; i < 10; i++) {  //起10个线程
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        userId = finalI;//静态变量。只有一份
                        Thread.sleep(1000); //睡1秒
                        System.out.println("UserId:" + userId); //执行结果：UserId全是9。因为在睡一秒的过程中，userId已经被修改成了9，因为所有线程都访问这个 静态变量UserId，所以所有线程都打印9
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static void testExecutor() {
        /*线程池方便管理，将所有的东西提交给线程池。一开始new好，后面就不用再创建线程池了*/

        //ExecutorService service= Executors.newSingleThreadExecutor();//单线程的执行框架。把第一个任务执行完了之后，才去执行第二个任务
        ExecutorService service = Executors.newFixedThreadPool(2);//多线程的执行框架。本例有线程池中有两条线程。如果有两条任务，那就会每个线程去执行一个任务
        service.submit(new Runnable() {//提交任务
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        Thread.sleep(1000);//每隔一秒，打印一个数字i
                        System.out.println("Executor1:" + i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        service.submit(new Runnable() {//提交任务
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        Thread.sleep(1000);//每隔一秒，打印一个数字i
                        System.out.println("Executor2:" + i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        service.shutdown();//当前面提交的任务被执行完了以后，再关闭服务。而不是强制关闭。新的任务不会被接受

        //轮询，看service结束了没有
        while (!service.isTerminated()) {  //若service还没有shutdown()关闭，就不停地轮询

            try {
                //每隔一秒就去查 thread有没有关闭掉，不然就打印
                Thread.sleep(1000);
                System.out.println("Wait for terminated");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //计数器
    private static int counter = 0;
    //原子计数器
    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    private static void testWithoutAtomic() {
        for (int i = 0; i < 10; i++) {  //起10个线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        for (int j = 0; j < 10; j++) {
                            counter++;
                            System.out.println(counter);//可能两个线程同时对7进行自增，然后两个线程都写回8。这样计数本来应该是9，却变成8了。凭空少了1个数！！！
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }
    }

    private static void testWithAtomic() {
        for (int i = 0; i < 10; i++) {  //起10个线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        for (int j = 0; j < 10; j++) {
                            System.out.println(atomicInteger.incrementAndGet());//原子性的操作
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }
    }

    public static void testFuture(){
        ExecutorService service= Executors.newSingleThreadExecutor();//单线程的执行框架。把第一个任务执行完了之后，才去执行第二个任务
        Future<Integer> future = service.submit(new Callable<Integer>() {    //将任务提交到service里面
            @Override
            public Integer call() throws Exception {
                Thread.sleep(1000); //等1秒。用来模拟一个要执行很长时间的任务
                return 1;

                //throw new IllegalArgumentException("一个异常");
            }
        });

        service.shutdown();

        try {
            //System.out.println(future.get());//future.get()等待提交的任务执行完成后，才执行
            System.out.println(future.get(100,TimeUnit.MILLISECONDS));//设置等待超时时间为100毫秒。100毫秒内就返回
        } catch (Exception e) {
            e.printStackTrace();//这里可以捕捉到 上面的线程中抛出的异常
        }

        //ExecutorService service = Executors.newFixedThreadPool(2);//多线程的执行框架。本例有线程池中有两条线程。如果有两条任务，那就会每个线程去执行一个任务


    }

    public static void main(String[] args) {
        //testThread();     //测试1
        //testSynchronized();   //测试2
        //testBlockingQueue();  //测试3
        //testThreadLocal();
        //testExecutor();
        //testWithoutAtomic();//打印出的数字是乱序的
        //testWithAtomic();
        testFuture();
    }

}
