package com.mafiadev.ichat.task;

import com.mafiadev.ichat.admin.AdminService;
import com.mafiadev.ichat.llm.GptService;
import com.mafiadev.ichat.service.MessageService;
import com.mafiadev.ichat.service.SessionService;

import java.util.concurrent.TimeUnit;

import static com.mafiadev.ichat.task.TaskTrigger.TASK_EXEC;

public class CacheResetTask {
    public CacheResetTask() {
        Runnable task = () -> {
            if (AdminService.INSTANCE != null && GptService.INSTANCE != null) {
                AdminService.clear(new SessionService().getSessions(), new MessageService().getChatMemoryStore(), 0.5);
            }
        };

        TASK_EXEC.scheduleAtFixedRate(task, 6, 6, TimeUnit.HOURS);
    }
}
