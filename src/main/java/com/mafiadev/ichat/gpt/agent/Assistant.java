package com.mafiadev.ichat.gpt.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.UserName;

public interface Assistant {

    @SystemMessage({
            "请使用中文对话",
            "当遇到`如何使用机器人`或`如何使用AI Bot`等类似问题时，请给出如下帮助文档:\n" +
            "```\n" +
            "1) \\gpt start: 开始会话\n" +
            "2) \\gpt end: 结束会话\n" +
            "3) \\gpt clear: 清空会话记录\n" +
            "4) #image + 文本: 图片生成请求，需要在会话中执行才可生效\n" +
            "```",
            "当遇到`start`时，请给出欢迎提示"
    })
    String chat(@MemoryId @UserName String userName, @UserMessage String message);
}
