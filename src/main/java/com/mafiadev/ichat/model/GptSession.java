package com.mafiadev.ichat.model;

import com.mafiadev.ichat.util.CommonUtil;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.image.ImageModel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GptSession {
    String userName;
    Boolean login;
    ChatLanguageModel chatModel;
    ImageModel imageModel;
    String tips;
    Boolean strict;
    String shortName;
    ChatLanguageModel gpt4Model;

    public GptSession(String userName, Boolean login, ChatLanguageModel chatModel, ChatLanguageModel gpt4Model, ImageModel imageModel, String tips, Boolean strict) {
        this.userName = userName;
        this.login = login;
        this.chatModel = chatModel;
        this.gpt4Model = gpt4Model;
        this.imageModel = imageModel;
        this.tips = tips;
        this.strict = strict;
        this.shortName = CommonUtil.tail(userName, 64);
    }

    public void reset() {
        this.setChatModel(null);
        this.setImageModel(null);
        this.setLogin(false);
        this.setTips("bye");
        this.setStrict(false);
    }
}
