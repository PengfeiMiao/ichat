package com.mafiadev.ichat.gpt.tool;

import com.mafiadev.ichat.util.CrawlerUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class WebPageTool {
    public static final String[] weekDays = new String[] {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    public static final Map<String, String> searchEngines = new HashMap<String, String>() {{
//        put("google", "https://www.google.com/search?q=");
        put("baidu", "https://www.baidu.com/s?wd=");
        put("sogou", "https://www.sogou.com/web?query=");
        put("360", "https://www.so.com/s?q=");
        put("bing", "https://cn.bing.com/search?q=");
        put("quark", "https://quark.sm.cn/s?safe=1&q=");
    }};

    @Tool("查询微博热搜")
    public String getNews(@ToolMemoryId String userName,
                             @P("关键字：关于什么的微博热搜") String keyword) {
        return CrawlerUtil.crawlWeiboTops();
    }

    // google,
    @Tool("Get time-sensitive (recent or newest) information from webpage, but not query time directly")
    public String getWebPage(@ToolMemoryId String userName,
                             @P("Name of the search engine: baidu, bing, 360, sogou, quark") String engine,
                             @P("Something need newest information") String query) {
        String webUrl = searchEngines.get(engine.toLowerCase());
        if (webUrl == null) {
            int seek = LocalDateTime.now().getSecond();
            String[] urls = searchEngines.values().toArray(new String[]{});
            webUrl = urls[seek % urls.length];
        }
        return CrawlerUtil.crawlContent(webUrl + query, false);
    }

    @Tool("Get time-related information")
    public String getTimeClock(@ToolMemoryId String userName, @P("Time-related query") String query) {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        int dayOfWeekValue = dayOfWeek.getValue();
        return "当前时间:" + now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ", " + weekDays[dayOfWeekValue];
    }
}
