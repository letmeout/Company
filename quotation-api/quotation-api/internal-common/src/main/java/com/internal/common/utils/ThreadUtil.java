package com.internal.common.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zdliu
 */
public class ThreadUtil {

    // 创建一个固定大小的线程池
    private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static int activeTasks = 0; // 活动任务计数
    private static final ReentrantLock lock = new ReentrantLock(); // 使用重入锁

    // 提交一个任务到线程池，并返回 Future 对象
    public static Future<?> runInThread(Runnable task) {
        lock.lock();  // 获取锁
        try {
            activeTasks++;
            return executorService.submit(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    // 处理任务执行中的异常
                    System.err.println("Task encountered an exception: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    taskCompleted();
                }
            });
        } finally {
            lock.unlock(); // 确保释放锁
        }
    }

    // 处理任务完成的逻辑
    private static void taskCompleted() {
        lock.lock(); // 获取锁
        try {
            activeTasks--;
            // 如果需要关闭，请使用 shutdown() 方法
        } finally {
            lock.unlock(); // 确保释放锁
        }
    }

    // 关闭线程池的方法
    public static void shutdown() {
        lock.lock(); // 获取锁
        try {
            if (!executorService.isShutdown()) {
                executorService.shutdown(); // 关闭线程池，不再接受新任务
                try {
                    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                        executorService.shutdownNow(); // 如果没有在指定时间内完成，强制关闭
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow(); // 如果当前线程被中断，强制关闭
                    Thread.currentThread().interrupt(); // 重新设置中断状态
                }
            }
        } finally {
            lock.unlock(); // 确保释放锁
        }
    }
}