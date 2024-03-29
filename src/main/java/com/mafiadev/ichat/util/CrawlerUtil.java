package com.mafiadev.ichat.util;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.mafiadev.ichat.constant.BrowserSettings;
import com.mafiadev.ichat.constant.Constant;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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

    public static String crawlWeiboTops() {
        String html = "";
        try {
            String tid = crawlWeiboTid();
            String cookie = crawlWeiBoCookie(tid);

            OkHttpClient httpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://s.weibo.com/top/summary?cate=realtimehot")
                    .addHeader("Cookie", cookie)
                    .addHeader("User-Agent", BrowserSettings.USER_AGENTS.get(0))
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                ResponseBody responseBody = response.body();

                if (response.isSuccessful() && responseBody != null) {
                    html = responseBody.string();
//                    System.out.println(html);
                    Document document = Jsoup.parse(html);
                    Element item = document.getElementsByTag("tbody").first();
                    if(item != null) {
                        List<String> news = item.getElementsByTag("tr").stream()
                                .map(ele -> String.format("%s (https://s.weibo.com%s)", ele.text(), ele.select("a[href]").attr("href")))
                                .filter(it -> !it.contains("javascript:void(0);"))
                                .collect(Collectors.toList());
                        return String.join("\n", news);
                    }
                } else {
                    System.out.println("Request failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return html;
    }

    @NotNull
    private static String crawlWeiBoCookie(String tid) {
        String subUrl = "https://passport.weibo.com/visitor/visitor";
        Map<String, Object> params2 = new HashMap<>();
        params2.put("a", "incarnate");
        params2.put("t", tid);
        params2.put("w", "3");
        params2.put("c", "100");
        params2.put("cb", "cross_domain");
        params2.put("from", "weibo");
        String str2 = HttpUtil.get(subUrl, params2, 3000);
        String resultStr = str2.substring(str2.indexOf("(") + 1, str2.indexOf(")"));
        String sub = "";
        String subp = "";
        if (!resultStr.isEmpty()) {
            JSONObject result = JSONObject.parseObject(resultStr);
            if (result.getIntValue("retcode") == 20000000) {
                sub = result.getJSONObject("data").getString("sub");
                subp = result.getJSONObject("data").getString("subp");
            }
        }
        return "SUB=" + sub + ";SUBP=" + subp + ";";
    }

    private static String crawlWeiboTid() {
        String tidUrl = "https://passport.weibo.com/visitor/genvisitor";
        Map<String, Object> params = new HashMap<>();
        params.put("cb", "gen_callback");
        String str = HttpUtil.get(tidUrl, params, 3000);
        String quStr = str.substring(str.indexOf("(") + 1, str.indexOf(")"));
        String tid = "";
        if (!quStr.isEmpty()) {
            JSONObject result = JSONObject.parseObject(quStr);
            if (result.getIntValue("retcode") == 20000000) {
                tid = result.getJSONObject("data").getString("tid");
            }
        }
        return tid;
    }

    public static void main(String[] args) {
        CrawlerUtil.crawlWeiboTops();
    }
}
