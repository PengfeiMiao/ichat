package com.mafiadev.ichat.llm;

import com.mafiadev.ichat.util.CommonUtil;
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
    Boolean strict;
    String shortName;

    public GptSession(String userName, Boolean login, ChatLanguageModel chatModel, ImageModel imageModel, String tips, Boolean strict) {
        this.userName = userName;
        this.login = login;
        this.chatModel = chatModel;
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
