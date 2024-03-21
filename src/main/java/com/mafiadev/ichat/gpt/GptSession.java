package com.mafiadev.ichat.gpt;

import dev.ai4j.openai4j.OpenAiClient;
import dev.ai4j.openai4j.chat.ChatCompletionModel;
import dev.ai4j.openai4j.chat.Message;
import dev.ai4j.openai4j.chat.SystemMessage;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GptSession {
    public static final List<Message> defaultMessages = new ArrayList<>();
    static {
        defaultMessages.add(SystemMessage.from(
                "- 当我询问`如何使用机器人`或 `如何使用AI Bot`等类似问题时，请给出如下帮助文档:\n" +
                "```\n" +
                "1) \\gpt start: 开始会话\n" +
                "2) \\gpt end: 结束会话\n" +
                "3) \\gpt clear: 清空会话记录\n" +
                "4) #image + 文本: 图片生成请求，需要在会话中执行才可生效\n" +
                "```\n" +
                "- 当我询问`如何结束会话`时，给出 \"\\gpt end\" 命令提示；\n" +
                "- 当我询问`如何清空会话记录`时，给出 \"\\gpt clear\" 命令提示；\n" +
                "- 当我询问`如何生成图片`时，给出 \"\\#image + 文本\" 命令提示；"));
    }

    String userName;
    Boolean login;
    List<Message> messages = defaultMessages;
    ChatCompletionModel model = ChatCompletionModel.GPT_3_5_TURBO;
    OpenAiClient client;
    String tips;

    public GptSession(String userName, Boolean login, OpenAiClient client, String tips) {
        this.userName = userName;
        this.login = login;
        this.client = client;
        this.tips = tips;
    }
}
