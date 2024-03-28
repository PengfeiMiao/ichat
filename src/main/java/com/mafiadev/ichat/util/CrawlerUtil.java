package com.mafiadev.ichat.util;

import com.mafiadev.ichat.constant.Constant;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CrawlerUtil {

    public static final List<String> IGNORE_URLS = Arrays.asList(
            "google",
            "baidu.com",
            "bing.com",
            "sogou.com",
            "so.com",
            "quark.sm.cn",
            "miit.gov.cn",
            "microsoft.com"
    );

    public static final Map<String, String> SEARCH_ENGINES = new HashMap<String, String>() {{
        put("google", "https://www.google.com/search?q=");
        put("baidu", "https://www.baidu.com/s?wd=");
        put("sogou", "https://www.sogou.com/web?query=");
        put("360", "https://www.so.com/s?q=");
        put("bing", "https://cn.bing.com/search?q=");
        put("quark", "https://quark.sm.cn/s?safe=1&q=");
    }};

    public static final int TIMEOUT = 3000;
    public static final Safelist SAFELIST = Safelist.basic();

    public static String crawlContent(String url, boolean isPersist) {
        String rootHtml = "";
        try {
            Document doc = Jsoup.connect(url).timeout(TIMEOUT).get();
            rootHtml = Jsoup.clean(doc.html(), SAFELIST);

            if (isPersist) {
                Path filePath = Paths.get(Constant.FILE_PATH.toString(), URLEncoder.encode(url, "utf-8") + ".html");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
                    writer.write(rootHtml);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rootHtml;
    }

    public static List<String> crawlTreeContent(String url, boolean isPersist) {
        List<String> htmls = new ArrayList<>();
        if (!url.startsWith("https://")) return htmls;
        try {
            Document doc = Jsoup.connect(url).timeout(TIMEOUT).get();
            String rootHtml = doc.html();
            htmls.add(Jsoup.clean(rootHtml, SAFELIST));
            if (isPersist) {
                Path filePath = Paths.get(Constant.FILE_PATH.toString(), URLEncoder.encode(url, "utf-8") + ".html");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
                    writer.write(rootHtml);
                }
            }
            crawlTreeUrls(doc, 2).forEach(item -> {
                System.out.println(item);
                htmls.add(crawlContent(item, isPersist));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(htmls);
        return htmls;
    }

    @NotNull
    public static List<String> crawlTreeUrls(String url, int limit) {
        List<String> list = new ArrayList<>();
        if (!url.startsWith("https://")) return list;
        try {
            Document doc = Jsoup.connect(url).timeout(TIMEOUT).get();
            list.add(url);
            list = crawlTreeUrls(doc, limit).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @NotNull
    private static Stream<String> crawlTreeUrls(Document doc, int limit) {
        return doc.select("a[href]").stream()
                .map(link -> link.attr("href"))
                .filter(item -> item.startsWith("https://") && IGNORE_URLS.stream().noneMatch(item::contains))
                .distinct()
                .limit(limit);
    }
}
