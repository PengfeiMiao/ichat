package com.mafiadev.ichat.llm.tool;

import com.mafiadev.ichat.crawler.SearchCrawler;
import com.mafiadev.ichat.crawler.WeiBoCrawler;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WebPageTool {
    public static final String[] weekDays = new String[] {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    @Tool("IF USER INPUT `热搜、头条、新闻` 等话题 ELSE YOU OUTPUT `序号) 热搜标题` CONDITION 每条热搜需要换行")
    public String getNews(@ToolMemoryId String userName,
                             @P("关键字：关于什么的微博热搜") String keyword) {
        return WeiBoCrawler.crawlWeiboTops();
    }

    // google,
    @Tool("IF USER INPUT `time-sensitive (recent or newest) question`, ELSE YOU search from web")
    public String getWebPage(@ToolMemoryId String userName,
                             @P("The origin query") String query,
                             @P("Name of the search engine: [baidu, bing, 360, sogou, quark]") String engine) {
        return SearchCrawler.crawlFromEngine(engine, query);
    }

    @Tool("Get time-related information")
    public String getTimeClock(@ToolMemoryId String userName, @P("Time-related query") String query) {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        int dayOfWeekValue = dayOfWeek.getValue();
        return "当前时间:" + now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ", " + weekDays[dayOfWeekValue];
    }
}
