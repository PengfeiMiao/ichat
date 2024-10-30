package com.mafiadev.ichat.model;

import com.mafiadev.ichat.util.CommonUtil;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.image.ImageModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GptSession {
    String userName;
    Boolean login;
    ChatLanguageModel chatModel;
    ImageModel imageModel;
    String tips;
    Boolean strict;
    ChatLanguageModel toolModel;

    public GptSession(String userName, Boolean login, ChatLanguageModel chatModel, ChatLanguageModel toolModel, ImageModel imageModel, String tips, Boolean strict) {
        this.userName = userName;
        this.login = login;
        this.chatModel = chatModel;
        this.toolModel = toolModel;
        this.imageModel = imageModel;
        this.tips = tips;
        this.strict = strict;
    }

    public String getShortName() {
        return CommonUtil.digest(userName);
    }

    public void reset() {
        this.setChatModel(null);
        this.setImageModel(null);
        this.setToolModel(null);
        this.setLogin(false);
        this.setTips("bye");
        this.setStrict(false);
    }
}

