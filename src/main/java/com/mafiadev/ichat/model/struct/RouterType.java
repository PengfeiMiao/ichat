package com.mafiadev.ichat.model.struct;

import dev.langchain4j.model.output.structured.Description;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum RouterType {
    @Description("日期时间、实时信息、搜索引擎 或 新闻热搜")
    TIME,

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

    public static String[] getAll() {
        return Arrays.stream(RouterType.values())
                .map(RouterType::name)
                .collect(Collectors.toList())
                .toArray(new String[] {});
    }
}
