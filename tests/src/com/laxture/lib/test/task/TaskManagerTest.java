package com.laxture.lib.test.task;

import android.test.ActivityInstrumentationTestCase2;

import com.laxture.lib.task.AbstractAsyncTask;
import com.laxture.lib.task.AbstractTask;
import com.laxture.lib.task.TaskManager;
import com.laxture.lib.test.TestActivity;
import com.laxture.lib.util.LLog;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class TaskManagerTest extends ActivityInstrumentationTestCase2<TestActivity> {

    public TaskManagerTest() {
        super(TestActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testActivityTestCaseSetUpProperly() {
        assertNotNull("activity should be launched successfully", getActivity());
    }

    public void testOnTaskExecution() {
        CountDownLatch latch = new CountDownLatch(1);
        final SimulatedTimerTask task = new SimulatedTimerTask(500);
        task.testThreadLatch = latch;
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TaskManager.runImmediately(task);
                }
            });
            latch.await();
        } catch (Throwable e) {
            LLog.e("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        LLog.v("Start assertion");
        assertTrue("OnTaskStart is not called", task.started);
        assertFalse("OnTaskCancelled shouldn't be called", task.cancelled);
        assertFalse("isCancelled shouldn't be true", task.isCancelled());
        assertTrue("OnTaskFinished is not called", task.finished);
        assertFalse("OnTaskFailed shouldn't be called", task.failed);
        assertEquals("onTaskProgressUpdated is not called", 100, task.totalSize);
        assertEquals("onTaskProgressUpdated is not called", 50, task.currentSize);
        assertEquals("SimulatorTask Finished", task.getResult());

        waitAWhile();
        assertEquals(AbstractTask.State.Finished, task.getState());
    }

    public void testOnTaskCancelled() {
        CountDownLatch latch = new CountDownLatch(1);
        final SimulatedTimerTask task = new SimulatedTimerTask(500);
        task.testCancel = true;
        task.testThreadLatch = latch;
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TaskManager.runImmediately(task);
                }
            });
            latch.await();
        } catch (Throwable e) {
            LLog.e("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        LLog.v("Start assertion");
        assertTrue("OnTaskStart is not called", task.started);
        assertTrue("OnTaskCancelled is not called", task.cancelled);
        assertTrue("isCancelled should be true", task.isCancelled());
        assertFalse("OnTaskFinished shouldn't be called", task.finished);
        assertFalse("OnTaskFailed shouldn't be called", task.failed);
        assertEquals("onTaskProgressUpdated shouldn't be called", 0, task.totalSize);
        assertEquals("onTaskProgressUpdated shouldn't be called", 0, task.currentSize);
        assertEquals("SimulatorTask Cancelled", task.getResult());

        waitAWhile();
        assertEquals(AbstractTask.State.Cancelled, task.getState());
    }

    public void testOnTaskFailed() {
        CountDownLatch latch = new CountDownLatch(1);
        final SimulatedTimerTask task = new SimulatedTimerTask(500);
        task.testFail = true;
        task.testThreadLatch = latch;
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TaskManager.runImmediately(task);
                }
            });
            latch.await();
        } catch (Throwable e) {
            LLog.e("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        LLog.v("Start assertion");
        assertTrue("OnTaskStart is not called", task.started);
        assertFalse("OnTaskCancelled shouldn't be called", task.cancelled);
        assertFalse("isCancelled shouldn't be true", task.isCancelled());
        assertFalse("OnTaskFinished shouldn't be called", task.finished);
        assertTrue("OnTaskFailed is not called", task.failed);
        assertEquals("onTaskProgressUpdated is not called", 100, task.totalSize);
        assertEquals("onTaskProgressUpdated is not called", 50, task.currentSize);
        assertEquals("SimulatorTask Failed", task.getResult());

        waitAWhile();
        assertEquals(AbstractTask.State.Failed, task.getState());
    }

    public void testRunImmediately() {
        CountDownLatch latch = new CountDownLatch(5);
        try {
            for (int i=0; i<5; i++) {
                final SimulatedLatchTask task = new SimulatedLatchTask();
                task.testThreadLatch = latch;
                runTestOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TaskManager.runImmediately(task);
                    }
                });
            }
        } catch (Throwable e) {
            LLog.e("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        waitAWhile();

        LLog.v("assertion for <testRunImmediately> ");
        // all 5 tasks should be start immdediately.
        assertEquals(5, TaskManager.getRunningTaskCount());
        ArrayList<AbstractAsyncTask<?>> tasks = TaskManager.getRunningTasks();
        assertEquals(5, tasks.size());

        LLog.v("Release waiting tasks");
        for (AbstractAsyncTask<?> task : tasks) {
            SimulatedLatchTask latchTask = (SimulatedLatchTask) task;
            latchTask.greenLight();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            LLog.e("loch latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        LLog.v("assertion after release latch");
        assertEquals(0, TaskManager.getRunningTaskCount());
        tasks = TaskManager.getRunningTasks();
        assertEquals(0, tasks.size());
    }

    public void testRunInSerial() {
        CountDownLatch latch = new CountDownLatch(5);
        try {
            for (int i=0; i<5; i++) {
                final SimulatedLatchTask task = new SimulatedLatchTask();
                task.testThreadLatch = latch;
                runTestOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TaskManager.runInSerial(task);
                    }
                });
            }
        } catch (Throwable e) {
            LLog.e("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        // wait a while so task will be put in the running queue
        waitAWhile();

        LLog.v("assertion for <testRunInSerial> ");
        assertEquals(1, TaskManager.getRunningTaskCount());
        assertEquals(4, TaskManager.getPendingTaskCount());

        SimulatedLatchTask runningTask = (SimulatedLatchTask) TaskManager.getRunningTasks().get(0);
        runningTask.greenLight();
        waitAWhile();
        assertEquals(1, TaskManager.getRunningTaskCount());
        assertEquals(3, TaskManager.getPendingTaskCount());

        runningTask = (SimulatedLatchTask) TaskManager.getRunningTasks().get(0);
        runningTask.greenLight();
        waitAWhile();
        assertEquals(1, TaskManager.getRunningTaskCount());
        assertEquals(2, TaskManager.getPendingTaskCount());

        runningTask = (SimulatedLatchTask) TaskManager.getRunningTasks().get(0);
        runningTask.greenLight();
        waitAWhile();
        assertEquals(1, TaskManager.getRunningTaskCount());
        assertEquals(1, TaskManager.getPendingTaskCount());

        runningTask = (SimulatedLatchTask) TaskManager.getRunningTasks().get(0);
        runningTask.greenLight();
        waitAWhile();
        assertEquals(1, TaskManager.getRunningTaskCount());
        assertEquals(0, TaskManager.getPendingTaskCount());

        runningTask = (SimulatedLatchTask) TaskManager.getRunningTasks().get(0);
        runningTask.greenLight();
        waitAWhile();
        assertEquals(0, TaskManager.getRunningTaskCount());
        assertEquals(0, TaskManager.getPendingTaskCount());
    }

    public void testQueueTask() {
        CountDownLatch latch = new CountDownLatch(5);
        try {
            for (int i=0; i<5; i++) {
                final SimulatedLatchTask task = new SimulatedLatchTask();
                task.setId(Integer.toString(i));
                task.testThreadLatch = latch;
                runTestOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TaskManager.queue(task);
                    }
                });
            }
        } catch (Throwable e) {
            LLog.e("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        // wait a while so task will be put in the running queue
        waitAWhile();

        LLog.v("assertion for <testQueueTask> ");
        assertEquals(3, TaskManager.getRunningTaskCount());
        assertEquals(2, TaskManager.getPendingTaskCount());

        // task_0 is running
        SimulatedLatchTask task_0 = (SimulatedLatchTask) TaskManager.findTask("0");
        assertEquals(AbstractTask.State.Running, task_0.getState());
        SimulatedLatchTask task_1 = (SimulatedLatchTask) TaskManager.findTask("1");
        assertEquals(AbstractTask.State.Running, task_1.getState());
        SimulatedLatchTask task_2 = (SimulatedLatchTask) TaskManager.findTask("2");
        assertEquals(AbstractTask.State.Running, task_2.getState());
        // task_3 is pending in head of the queue
        SimulatedLatchTask task_3 = (SimulatedLatchTask) TaskManager.findTask("3");
        assertEquals(AbstractTask.State.Pending, task_3.getState());
        SimulatedLatchTask task_4 = (SimulatedLatchTask) TaskManager.findTask("4");
        assertEquals(AbstractTask.State.Pending, task_4.getState());
        // let the task_0 go
        task_0.greenLight();
        waitAWhile();
        assertEquals(3, TaskManager.getRunningTaskCount());
        assertEquals(1, TaskManager.getPendingTaskCount());
        // task_3 should be started now
        assertEquals(AbstractTask.State.Running, task_3.getState());

        // push a new task_6 to head of waiting queue
        try {
            final SimulatedLatchTask task = new SimulatedLatchTask();
            task.setId("5");
            task.testThreadLatch = latch;
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TaskManager.push(task);
                }
            });
        } catch (Throwable e) {
            LLog.e("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        waitAWhile();
        assertEquals(3, TaskManager.getRunningTaskCount());
        assertEquals(2, TaskManager.getPendingTaskCount());

        // let the task_3 go
        task_3.greenLight();
        waitAWhile();
        assertEquals(AbstractTask.State.Finished, task_3.getState());
        // task_5 should be started now
        SimulatedLatchTask task_5 = (SimulatedLatchTask) TaskManager.findTask("5");
        assertEquals(AbstractTask.State.Running, task_5.getState());
    }

    public void testResueTask() {
        CountDownLatch latch = new CountDownLatch(5);
        try {
            for (int i=0; i<5; i++) {
                final SimulatedLatchTask task = new SimulatedLatchTask();
                task.setId(Integer.toString(i));
                task.testThreadLatch = latch;
                runTestOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TaskManager.queue(task);
                    }
                });
            }
        } catch (Throwable e) {
            LLog.e("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        // wait a while so task will be put in the running queue
        waitAWhile();

        LLog.v("assertion for <testQueueTask> ");
        assertEquals(3, TaskManager.getRunningTaskCount());
        assertEquals(2, TaskManager.getPendingTaskCount());

        // task_0 is running
        SimulatedLatchTask task_0 = (SimulatedLatchTask) TaskManager.findTask("0");
        assertEquals(AbstractTask.State.Running, task_0.getState());
        SimulatedLatchTask task_1 = (SimulatedLatchTask) TaskManager.findTask("1");
        assertEquals(AbstractTask.State.Running, task_1.getState());
        SimulatedLatchTask task_2 = (SimulatedLatchTask) TaskManager.findTask("2");
        assertEquals(AbstractTask.State.Running, task_2.getState());
        // task_3 is pending in head of the queue
        SimulatedLatchTask task_3 = (SimulatedLatchTask) TaskManager.findTask("3");
        assertEquals(AbstractTask.State.Pending, task_3.getState());
        SimulatedLatchTask task_4 = (SimulatedLatchTask) TaskManager.findTask("4");
        assertEquals(AbstractTask.State.Pending, task_4.getState());

        // queue a new task_0, the original task_0 should be reuse.
        try {
            final SimulatedLatchTask task = new SimulatedLatchTask();
            task.setId("0");
            task.testThreadLatch = latch;
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TaskManager.queue(task);
                }
            });
        } catch (Throwable e) {
            LLog.e("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        // queue should be the same.
        LLog.v("assertion for");
        assertEquals(3, TaskManager.getRunningTaskCount());
        assertEquals(2, TaskManager.getPendingTaskCount());
        // task_0 shouldn't be changed.
        SimulatedLatchTask task_0_2 = (SimulatedLatchTask) TaskManager.findTask("0");
        assertSame(task_0_2, task_0);

        // push a new task_4, the original task_0 should be reuse.
        try {
            final SimulatedLatchTask task = new SimulatedLatchTask();
            task.setId("4");
            task.testThreadLatch = latch;
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TaskManager.push(task);
                }
            });
        } catch (Throwable e) {
            LLog.e("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        // queue should be the same.
        LLog.v("assertion for");
        assertEquals(3, TaskManager.getRunningTaskCount());
        assertEquals(2, TaskManager.getPendingTaskCount());
        // task_4 shouldn't be changed.
        SimulatedLatchTask task_4_2 = (SimulatedLatchTask) TaskManager.findTask("4");
        assertSame(task_4_2, task_4);
    }

    public void testCancelByTag() {
        CountDownLatch latch = new CountDownLatch(5);
        try {
            for (int i=0; i<5; i++) {
                final SimulatedLatchTask task = new SimulatedLatchTask();
                task.setId(Integer.toString(i));
                if (i % 2 == 0) task.setTag("even");
                task.testThreadLatch = latch;
                runTestOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TaskManager.queue(task);
                    }
                });
            }
        } catch (Throwable e) {
            LLog.e("lock latch failed", e);
            assertFalse(e.getMessage(), true);
        }

        // wait a while so task will be put in the running queue
        waitAWhile();

        LLog.v("assertion for <testQueueTask> ");
        assertEquals(3, TaskManager.getRunningTaskCount());
        assertEquals(2, TaskManager.getPendingTaskCount());

        SimulatedLatchTask task_0 = (SimulatedLatchTask) TaskManager.findTask("0");
        assertEquals(AbstractTask.State.Running, task_0.getState());
        SimulatedLatchTask task_1 = (SimulatedLatchTask) TaskManager.findTask("1");
        assertEquals(AbstractTask.State.Running, task_1.getState());
        SimulatedLatchTask task_2 = (SimulatedLatchTask) TaskManager.findTask("2");
        assertEquals(AbstractTask.State.Running, task_2.getState());
        // task_3 is pending in head of the queue
        SimulatedLatchTask task_3 = (SimulatedLatchTask) TaskManager.findTask("3");
        assertEquals(AbstractTask.State.Pending, task_3.getState());
        SimulatedLatchTask task_4 = (SimulatedLatchTask) TaskManager.findTask("4");
        assertEquals(AbstractTask.State.Pending, task_4.getState());

        LLog.v("assertion for cancelByTag ");
        TaskManager.cancelByTag("even");
        waitAWhile();
        assertEquals(2, TaskManager.getAllTasks().size());
        // even task might be finished before cancelled
        assertTrue(AbstractTask.State.Cancelled == task_0.getState());
        assertTrue(AbstractTask.State.Cancelled == task_2.getState());
        assertTrue(AbstractTask.State.Cancelled == task_4.getState());
        // task 1 keep running
        assertEquals(AbstractTask.State.Running, task_1.getState());
        // task 3 start to run
        assertTrue(AbstractTask.State.Running ==task_3.getState()
                || AbstractTask.State.Pending == task_3.getState());
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TaskManager.cancelAll();
        waitAWhile();
        assertEquals(0, TaskManager.getRunningTaskCount());
        assertEquals(0, TaskManager.getPendingTaskCount());
    }

    private static final long WAIT_FOR_QUEUE = 200;

    public void waitAWhile() {
        try {
            Thread.sleep(WAIT_FOR_QUEUE);
        } catch (InterruptedException e) {
            LLog.e("loch latch failed", e);
            assertFalse(e.getMessage(), true);
        }
    }
}
