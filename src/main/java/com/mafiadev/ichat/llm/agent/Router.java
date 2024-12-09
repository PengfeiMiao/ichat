package com.mafiadev.ichat.llm.agent;

import com.mafiadev.ichat.model.struct.RouterType;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.UserName;
import dev.langchain4j.service.V;

public interface Router {
    @UserMessage("判断 `{{message}}` 与哪种类型相关, 必须返回其中一种：{{types}}")
    RouterType route(@UserName String userName, @V("message") String message, @V("types") String[] types);
}
