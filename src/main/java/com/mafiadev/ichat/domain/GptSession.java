package com.mafiadev.ichat.domain;

import com.mafiadev.ichat.annotation.FieldA;
import com.mafiadev.ichat.annotation.TableA;
import com.mafiadev.ichat.util.CommonUtil;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.image.ImageModel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@TableA("SESSION")
public class GptSession {
    @FieldA("USER_NAME")
    String userName;
    @FieldA("LOGIN")
    Boolean login;
    ChatLanguageModel chatModel;
    ImageModel imageModel;
    @FieldA("TIPS")
    String tips;
    @FieldA("STRICT")
    Boolean strict;
    String shortName;
    @FieldA("GPT4_MODEL")
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
