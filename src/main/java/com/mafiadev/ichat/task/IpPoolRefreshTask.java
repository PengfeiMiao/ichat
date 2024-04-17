package com.mafiadev.ichat.task;

import com.mafiadev.ichat.crawler.IpPoolCrawler;
import com.mafiadev.ichat.util.CrawlerUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IpPoolRefreshTask {
    public IpPoolRefreshTask() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        IpPoolCrawler ipPoolCrawler = new IpPoolCrawler();
        Runnable task = () -> {
            ipPoolCrawler.refresh();
            CrawlerUtil.IP_PORT_THREAD_LOCAL.set(ipPoolCrawler.load());
        };

        executor.scheduleAtFixedRate(task, 0, 2, TimeUnit.HOURS);
    }
}
