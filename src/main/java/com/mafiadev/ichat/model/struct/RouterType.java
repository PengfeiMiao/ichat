package com.mafiadev.ichat.model.struct;

import dev.langchain4j.model.output.structured.Description;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum RouterType {
    @Description("时间、日期类信息")
    TIME,

    @Description("微博热搜")
    WEIBO,

    @Description("天气、新闻、微博热搜等实时信息 或 明确要求使用搜索引擎查询")
    SEARCH,

    @Description("以 #image 开头、绘画 或 生成图片")
    IMAGE,

    @Description("创建定时任务、闹钟、延时提醒")
    TASK_ADD,

    @Description("取消定时任务、闹钟、延时提醒")
    TASK_DEL,

    @Description("查看定时任务、闹钟、延时提醒")
    TASK_LS,

    @Description("和以上类型都无关")
    OTHER;

    @Getter
    private static final Map<String, String> descriptions = Arrays.stream(values())
                .collect(Collectors.toMap(RouterType::name, RouterType::getDescription));

    private static String getDescription(RouterType routerType) {
        try {
            java.lang.Class<RouterType> clazz = RouterType.class;
            java.lang.reflect.Field field = clazz.getField(routerType.name());
            Description description = field.getAnnotation(Description.class);
            return description != null ? description.value()[0] : "";
        } catch (NoSuchFieldException | SecurityException e) {
            return "";
        }
    }
}
