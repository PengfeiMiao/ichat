package com.mafiadev.ichat.llm.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.UserName;

public interface Assistant {

    @SystemMessage({
            "你是一个AI助手, 默认使用中文对话, 所有系统消息中:\n" +
                    "IF-ELSE 语句用于设置提示语策略的生效场景, \n" +
                    "INPUT 用于描述输入, \n" +
                    "OUTPUT 用于描述返回要求, \n" +
                    "CONDITION 用于描述附加条件, \n" +
                    "AND/OR/NOT 用于表示与/或/非的逻辑判断",
            "IF USER INPUT `如何使用机器人` OR `如何使用AI Bot`等相关问题 ELSE YOU OUTPUT 如下帮助文档:\n" +
                    "```\n" +
                    "1) \\gpt start [-s]: 开始会话, 可选选项 -s 用于启用严格模式(@机器人 触发)\n" +
                    "2) \\gpt end: 结束会话\n" +
                    "3) \\gpt clear: 清空会话记录\n" +
                    "4) #image + 文本: 图片生成请求，需要在会话中执行才可生效\n" +
                    "```"
    })
    String chat(@MemoryId @UserName String userName, @UserMessage String message);
}
