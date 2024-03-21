package com.mafiadev.ichat.gpt;

import dev.ai4j.openai4j.OpenAiClient;
import dev.ai4j.openai4j.chat.ChatCompletionModel;
import dev.ai4j.openai4j.chat.Message;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GptSession {
    String userName;
    Boolean login;
    List<Message> messages = new ArrayList<>();
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
