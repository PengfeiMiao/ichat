package com.mafiadev.ichat.llm.agent;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.UserName;
import dev.langchain4j.service.V;

public interface Drawer {

    @UserMessage("Whether question `{{message}}` is requested to draw somthing")
    boolean route(@UserName String userName, @UserMessage @V("message") String message);
}
