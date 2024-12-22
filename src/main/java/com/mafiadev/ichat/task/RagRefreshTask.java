package com.mafiadev.ichat.task;

import com.mafiadev.ichat.crawler.WeiBoCrawler;
import com.mafiadev.ichat.llm.rag.CachedEmbeddingStore;

import java.util.concurrent.TimeUnit;

import static com.mafiadev.ichat.task.TaskTrigger.TASK_EXEC;

public class RagRefreshTask {
    public RagRefreshTask() {
        Runnable task = () -> {
            CachedEmbeddingStore.addDocument(WeiBoCrawler.crawlWeiboTops());
        };

        TASK_EXEC.scheduleAtFixedRate(task, 0, 1, TimeUnit.HOURS);
    }
}
