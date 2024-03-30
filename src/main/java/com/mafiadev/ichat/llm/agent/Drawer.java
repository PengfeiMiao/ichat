package com.mafiadev.ichat.llm.agent;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.UserName;
import dev.langchain4j.service.V;

public interface Drawer {

    @UserMessage("Whether `{{message}}` 与 `绘画` 或 `生成图片` 强相关")
    boolean route(@UserName String userName, @UserMessage @V("message") String message);
}
