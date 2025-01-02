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
    ChatLanguageModel toolModel;
    ImageModel imageModel;
    String tips;
    Boolean strict;
    Boolean multiple;

    public String getShortName() {
        return CommonUtil.digest(userName);
    }

    public void reset(String tips) {
        this.setChatModel(null);
        this.setToolModel(null);
        this.setImageModel(null);
        this.setToolModel(null);
        this.setLogin(false);
        this.setTips(tips);
        this.setStrict(false);
    }
}

