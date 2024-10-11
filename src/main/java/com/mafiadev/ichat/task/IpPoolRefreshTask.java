package com.mafiadev.ichat.task;

import com.mafiadev.ichat.crawler.IpPoolCrawler;
import com.mafiadev.ichat.crawler.IpPort;
import com.mafiadev.ichat.util.ConfigUtil;
import com.mafiadev.ichat.util.CrawlerUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.mafiadev.ichat.task.TaskTrigger.TASK_EXEC;

@Slf4j
public class IpPoolRefreshTask {
    public IpPoolRefreshTask() {
        if(!Objects.equals(ConfigUtil.getConfig("ipSwitch"), "true")) {
            return;
        }

        IpPoolCrawler ipPoolCrawler = new IpPoolCrawler();
        Runnable task1 = () -> {
            List<IpPort> list = ipPoolCrawler.refresh();
            log.info("[IpPoolRefreshTask] task, size: " + list.size());
            CrawlerUtil.IP_PORT_THREAD_LOCAL.set(list);
        };
        Runnable task2 = () -> {
            CrawlerUtil.IP_PORT_THREAD_LOCAL.set(ipPoolCrawler.recheck(CrawlerUtil.IP_PORT_THREAD_LOCAL.get()));
        };

        TASK_EXEC.scheduleAtFixedRate(task1, 0, 2, TimeUnit.HOURS);
        TASK_EXEC.scheduleAtFixedRate(task2, 10, 10, TimeUnit.MINUTES);
    }
}
