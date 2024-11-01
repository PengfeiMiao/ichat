package com.mafiadev.ichat.model.type;

import dev.langchain4j.model.output.structured.Description;

public enum RouterType {
    @Description("日期时间、实时信息、搜索引擎 或 新闻热搜")
    TIME,

    @Description("以 #image 开头、绘画 或 生成图片")
    IMAGE,

    @Description("定时任务、闹钟、延时提醒")
    TASK,

    @Description("和以上类型都无关")
    OTHER
}
