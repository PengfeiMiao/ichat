package com.mafiadev.ichat.llm;

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

    public GptSession(String userName, Boolean login, ChatLanguageModel chatModel, ImageModel imageModel, String tips) {
        this.userName = userName;
        this.login = login;
        this.chatModel = chatModel;
        this.imageModel = imageModel;
        this.tips = tips;
    }

    public void reset() {
        this.setChatModel(null);
        this.setImageModel(null);
        this.setLogin(false);
        this.setTips("bye");
    }
}
