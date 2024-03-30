package com.mafiadev.ichat.llm.agent;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.UserName;
import dev.langchain4j.service.V;

public interface TimelyAnalyzer {

    @UserMessage("whether {{message}} is time sensitive, return true or false")
    boolean judge(@UserName String userName, @UserMessage @V("message") String message);
}
