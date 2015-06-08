package com.laxture.lib.task;

import com.laxture.lib.RuntimeContext;
import com.laxture.lib.task.AbstractAsyncTask.MyFutureTask;
import com.laxture.lib.util.Checker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TaskManager {

    private static final int KEEP_ALIVE = 0;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "Task #" + mCount.getAndIncrement());
        }
    };

    /**
     * An {@link Executor} that can be used to execute tasks in queue.
     */
    private static final ThreadPoolExecutor QUEUE_EXECUTOR =
            new ManagedThreadPoolExecutor(RuntimeContext.getConfig().getTaskQueueCosumingLimit(),
                    RuntimeContext.getConfig().getTaskQueueCosumingLimit(),
                    KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                    sThreadFactory);

    /**
     * An {@link Executor} that executes tasks one at a time in serial
     * order.
     */
    private static final ThreadPoolExecutor SERIAL_EXECUTOR =
            new ManagedThreadPoolExecutor(1, 1, KEEP_ALIVE, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(), sThreadFactory);

    /**
     * An {@link Executor} that executes tasks immediately.
     */
    private static final ThreadPoolExecutor IMMEDIATE_EXECUTOR =
            new ManagedThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), sThreadFactory);

    //*************************************************************************
    // ManagedThreadPoolExecutor
    //*************************************************************************

    private static List<AbstractAsyncTask<?>> sRunningPool;

    static class ManagedThreadPoolExecutor extends ThreadPoolExecutor {

        public ManagedThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                    threadFactory);
            sRunningPool = Collections.synchronizedList(new ArrayList<AbstractAsyncTask<?>>());
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected void beforeExecute(Thread thread, Runnable runnable) {
            MyFutureTask future = (MyFutureTask) runnable;

            // it's possible that the task is finished during this for loop, and
            // task will be remove from sRunningPool
            // it might cause ConcurrentModificationException
            synchronized (TaskManager.class) {
                sRunningPool.add(future.getTask());
            }
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected void afterExecute(Runnable runnable, Throwable throwable) {
            MyFutureTask future = (MyFutureTask) runnable;

            // it's possible that the task is finished during this for loop, and
            // task will be remove from sRunningPool
            // it might cause ConcurrentModificationException
            synchronized (TaskManager.class) {
                sRunningPool.remove(future.getTask());
            }
        }
    }

    //*************************************************************************
    // Overall Methods
    //*************************************************************************

    public static int getRunningTaskCount() {
        return QUEUE_EXECUTOR.getActiveCount()
                + SERIAL_EXECUTOR.getActiveCount()
                + IMMEDIATE_EXECUTOR.getActiveCount();
    }

    public static int getPendingTaskCount() {
        return QUEUE_EXECUTOR.getQueue().size()
                + SERIAL_EXECUTOR.getQueue().size();
    }

    public static ArrayList<AbstractAsyncTask<?>> getRunningTasks() {
        return new ArrayList<AbstractAsyncTask<?>>(sRunningPool);
    }

    @SuppressWarnings("rawtypes")
    public static ArrayList<AbstractAsyncTask<?>> getAllTasks() {
        ArrayList<AbstractAsyncTask<?>> tasks
                = new ArrayList<AbstractAsyncTask<?>>(sRunningPool);
        for (Runnable runnable : QUEUE_EXECUTOR.getQueue()) {
            MyFutureTask future = (MyFutureTask) runnable;
            tasks.add(future.getTask());
        }
        for (Runnable runnable : SERIAL_EXECUTOR.getQueue()) {
            MyFutureTask future = (MyFutureTask) runnable;
            tasks.add(future.getTask());
        }
        return tasks;
    }

    @SuppressWarnings("rawtypes")
    public static AbstractAsyncTask findTask(String id) {
        if (Checker.isEmpty(id)) return null;
        for (AbstractAsyncTask task : getAllTasks()) {
            if (id.equals(task.getId())) return task;
        }
        return null;
    }

    //*************************************************************************
    //  Queue Executor
    //*************************************************************************

    @SuppressWarnings("rawtypes")
    public static ArrayList<AbstractAsyncTask<?>> listTasksInQueue() {
        ArrayList<AbstractAsyncTask<?>> tasks
                = new ArrayList<AbstractAsyncTask<?>>(sRunningPool);
        for (Runnable runnable : QUEUE_EXECUTOR.getQueue()) {
            MyFutureTask future = (MyFutureTask) runnable;
            tasks.add(future.getTask());
        }
        return tasks;
    }

    @SuppressWarnings("rawtypes")
    public static AbstractAsyncTask<?> findTaskInQueue(String id) {
        if (Checker.isEmpty(id)) return null;
        for (Runnable runnable : QUEUE_EXECUTOR.getQueue()) {
            MyFutureTask future = (MyFutureTask) runnable;
            if (id.equals(future.getTask().getId())) return future.getTask();
        }
        return null;
    }

    /**
     * Put task to queue.
     */
    public static void queue(AbstractAsyncTask<?> task) {
        synchronized (TaskManager.class) {
            // no existed task, add to queue.
            if (reuseTask(task) == null)
                task.executeOnExecutor(QUEUE_EXECUTOR);
        }
    }

    /**
     * Put an head of the queue.
     */
    @SuppressWarnings("rawtypes")
    public static void push(AbstractAsyncTask task) {
        synchronized (TaskManager.class) {
            MyFutureTask future = reuseTask(task);

            if (future == null)
                future = task.executeOnExecutor(QUEUE_EXECUTOR);

            // move added executor to the head of the queue
            if (future != null && future.getTask().getState() != AbstractTask.State.Running) {
                List<Runnable> tempQueue = new ArrayList<Runnable>();
                QUEUE_EXECUTOR.getQueue().drainTo(tempQueue);
                tempQueue.remove(future);
                QUEUE_EXECUTOR.getQueue().offer(future);
                QUEUE_EXECUTOR.getQueue().addAll(tempQueue);
            }
        }
    }

    /**
     * reuse existing executor. if it exists, reset its listener so
     * current UI shall response.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static MyFutureTask reuseTask(AbstractAsyncTask<?> task) {
        if (Checker.isEmpty(task.getId())) return null;

        synchronized (sRunningPool) {
            // connect latest TaskListener to existing task
            for (AbstractAsyncTask<?> runningTask : sRunningPool) {
                if (!task.getId().equals(runningTask.getId())) continue;
                // found!
                runningTask.addAllTaskListeners(task.getTaskListeners());
                return new RunningFutureStub(task);
            }
        }

        // find in pending queue
        for (Runnable runnable : QUEUE_EXECUTOR.getQueue()) {
            MyFutureTask future = (MyFutureTask) runnable;
            if (!task.getId().equals(future.getTask().getId())) continue;
            // found!
            future.getTask().addAllTaskListeners(task.getTaskListeners());
            return future;
        }

        return null;
    }

    /**
     * A fake Future return by {@link TaskManager#reuseTask(AbstractAsyncTask)}
     */
    static class RunningFutureStub<V> extends MyFutureTask<V> {
        public RunningFutureStub(AbstractAsyncTask<V> task) {
            super(task, new Callable<V>() {
                @Override
                public V call() throws Exception {
                    return null;
                }
            });
        }
    }

    //*************************************************************************
    //  Serial/Immediate Executor
    //*************************************************************************

    public static void runInSerial(AbstractAsyncTask<?> task) {
        task.executeOnExecutor(SERIAL_EXECUTOR);
    }

    public static void runImmediately(AbstractAsyncTask<?> task) {
        task.executeOnExecutor(IMMEDIATE_EXECUTOR);
    }

    //*************************************************************************
    //  Serial/Immediate Executor
    //*************************************************************************

    @SuppressWarnings("rawtypes")
    public static void cancelAll() {
        synchronized (TaskManager.class) {
            for (AbstractAsyncTask<?> runningTask : sRunningPool) runningTask.cancel();

            // cancel pending queue tasks
            for (Runnable runnable : QUEUE_EXECUTOR.getQueue()) {
                MyFutureTask future = (MyFutureTask) runnable;
                future.getTask().cancel();
            }
            QUEUE_EXECUTOR.purge();

            // cancel pending serial tasks
            for (Runnable runnable : SERIAL_EXECUTOR.getQueue()) {
                MyFutureTask future = (MyFutureTask) runnable;
                future.getTask().cancel();
            }
            SERIAL_EXECUTOR.purge();

            // no need to cancel immediate executor since those tasks
            // are all in sRunningPool
        }
    }

    @SuppressWarnings("rawtypes")
    public static void cancelByTag(Object tag) {
        if (tag == null) return;

        synchronized (TaskManager.class) {
            for (AbstractAsyncTask<?> runningTask : sRunningPool) {
                if (tag.equals(runningTask.getTag())) runningTask.cancel();
            }

            // cancel pending queue tasks
            for (Runnable runnable : QUEUE_EXECUTOR.getQueue()) {
                MyFutureTask future = (MyFutureTask) runnable;
                if (tag.equals(future.getTask().getTag())) future.getTask().cancel();
            }
            QUEUE_EXECUTOR.purge();

            // cancel pending serial tasks
            for (Runnable runnable : SERIAL_EXECUTOR.getQueue()) {
                MyFutureTask future = (MyFutureTask) runnable;
                if (tag.equals(future.getTask().getTag())) future.getTask().cancel();
            }
            SERIAL_EXECUTOR.purge();

            // no need to cancel immediate executor since those tasks
            // are all in sRunningPool
        }
    }

    @SuppressWarnings("rawtypes")
    public static void cancel(AbstractAsyncTask<?> task) {
        if (task == null) return;

        synchronized (TaskManager.class) {
            for (AbstractAsyncTask<?> runningTask : sRunningPool) {
                if (task == runningTask) runningTask.cancel();
                return;
            }

            // cancel pending queue tasks
            for (Runnable runnable : QUEUE_EXECUTOR.getQueue()) {
                MyFutureTask future = (MyFutureTask) runnable;
                if (task == future.getTask()) {
                    future.getTask().cancel();
                    QUEUE_EXECUTOR.purge();
                    return;
                }
            }

            // cancel pending serial tasks
            for (Runnable runnable : SERIAL_EXECUTOR.getQueue()) {
                MyFutureTask future = (MyFutureTask) runnable;
                if (task == future.getTask()) {
                    future.getTask().cancel();
                    SERIAL_EXECUTOR.purge();
                    return;
                }
            }

            // no need to cancel immediate executor since those tasks
            // are all in sRunningPool
        }
    }

}

