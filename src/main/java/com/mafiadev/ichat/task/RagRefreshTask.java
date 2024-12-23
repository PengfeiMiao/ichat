package com.mafiadev.ichat.task;

import com.mafiadev.ichat.crawler.WeiBoCrawler;
import com.mafiadev.ichat.llm.rag.CachedEmbeddingStore;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.mafiadev.ichat.task.TaskTrigger.TASK_EXEC;

public class RagRefreshTask {
    public RagRefreshTask() {
        Runnable task = () -> {
            CachedEmbeddingStore.removeDocument("from", "weibo");
            CachedEmbeddingStore.addDocument(WeiBoCrawler.crawlWeiboTops(), new HashMap<String, Object>() {{
                put("from", "weibo");
                put("keyword", "微博热搜");
            }});
        };

        TASK_EXEC.scheduleAtFixedRate(task, 0, 1, TimeUnit.HOURS);
    }
}
