package com.mafiadev.ichat.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@SuppressWarnings("ALL")
public class TaskTrigger {
    public static final ScheduledExecutorService TASK_EXEC =  Executors.newScheduledThreadPool(1);

    public TaskTrigger() {
        new CacheResetTask();
        new IpPoolRefreshTask();
    }

    public static void main(String[] args) {
        new TaskTrigger();
    }
}
