package com.mafiadev.ichat.gpt;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.image.ImageModel;
import lombok.Data;

@Data
public class GptSession {
    String userName;
    Boolean login;
    ChatLanguageModel chatModel;
    ImageModel imageModel;
    String tips;
    ChatMemory chatMemory;

    public GptSession(String userName, Boolean login, ChatLanguageModel chatModel, ImageModel imageModel, String tips) {
        this.userName = userName;
        this.login = login;
        this.chatModel = chatModel;
        this.imageModel = imageModel;
        this.tips = tips;
        this.chatMemory = MessageWindowChatMemory.withMaxMessages(100);
    }

    public void clear() {
        this.chatMemory.clear();
    }

    public void reset() {
        this.setChatModel(null);
        this.setImageModel(null);
        this.chatMemory.clear();
        this.setLogin(false);
        this.setTips("bye");
    }
}
