package com.mafiadev.ichat.task;

import com.mafiadev.ichat.admin.AdminService;
import com.mafiadev.ichat.llm.GptService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.mafiadev.ichat.llm.GptService.chatMemoryStore;
import static com.mafiadev.ichat.llm.GptService.sessionHashMap;

public class CacheResetTask {
    public CacheResetTask() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runnable task = () -> {
            if (AdminService.INSTANCE != null && GptService.INSTANCE != null) {
                AdminService.clear(sessionHashMap, chatMemoryStore);
            }
        };

        executor.scheduleAtFixedRate(task, 12, 12, TimeUnit.HOURS);
    }
}
