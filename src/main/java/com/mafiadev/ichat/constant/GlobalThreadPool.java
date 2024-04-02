package com.mafiadev.ichat.constant;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public interface GlobalThreadPool {
    ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newScheduledThreadPool(5);

    ExecutorService CACHED_EXECUTOR = Executors.newCachedThreadPool();
}