package com.mafiadev.ichat.llm.agent;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.UserName;
import dev.langchain4j.service.V;

public interface Router {
    @UserMessage("Whether `{{message}}` 与 `日期时间、实时信息、搜索引擎 或 新闻热搜` 强相关")
    boolean routeTool(@UserName String userName, @V("message") String message);

    @UserMessage("Whether `{{message}}` 与 `绘画 或 生成图片` 强相关")
    boolean routeDraw(@UserName String userName, @V("message") String message);
}
