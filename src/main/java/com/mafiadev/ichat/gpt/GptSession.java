package com.mafiadev.ichat.gpt;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.image.ImageModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GptSession {
    public static final List<ChatMessage> defaultMessages = new ArrayList<>();
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
    List<ChatMessage> messages = defaultMessages;
    ChatLanguageModel chatModel;
    ImageModel imageModel;
    String tips;

    public GptSession(String userName, Boolean login, ChatLanguageModel chatModel, ImageModel imageModel, String tips) {
        this.userName = userName;
        this.login = login;
        this.chatModel = chatModel;
        this.imageModel = imageModel;
        this.tips = tips;
    }

    public void clear() {
        this.setMessages(defaultMessages);
    }

    public void reset() {
        this.setChatModel(null);
        this.setImageModel(null);
        this.setLogin(false);
        this.setTips("bye");
    }
}
