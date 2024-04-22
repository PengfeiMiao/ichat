package com.mafiadev.ichat.task;

import com.mafiadev.ichat.crawler.IpPoolCrawler;
import com.mafiadev.ichat.util.CrawlerUtil;

import java.util.concurrent.TimeUnit;

import static com.mafiadev.ichat.task.TaskTrigger.TASK_EXEC;

public class IpPoolRefreshTask {
    public IpPoolRefreshTask() {
        IpPoolCrawler ipPoolCrawler = new IpPoolCrawler();
        Runnable task1 = () -> {
            CrawlerUtil.IP_PORT_THREAD_LOCAL.set(ipPoolCrawler.refresh());
        };
        Runnable task2 = () -> {
            CrawlerUtil.IP_PORT_THREAD_LOCAL.set(ipPoolCrawler.recheck(CrawlerUtil.IP_PORT_THREAD_LOCAL.get()));
        };

        TASK_EXEC.scheduleAtFixedRate(task1, 0, 2, TimeUnit.HOURS);
        TASK_EXEC.scheduleAtFixedRate(task2, 10, 10, TimeUnit.MINUTES);
    }
}
