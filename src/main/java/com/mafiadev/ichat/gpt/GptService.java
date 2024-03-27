package com.mafiadev.ichat.gpt;

import com.mafiadev.ichat.Claptrap;
import com.mafiadev.ichat.util.FileUtil;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiImageModel;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.mafiadev.ichat.constant.Constant.FILE_PATH;

public class GptService {
    public static GptService INSTANCE;

    public static void init(Claptrap plugin) {
        INSTANCE = new GptService(plugin);
    }

    private static final Map<String, GptSession> sessionHashMap = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY = 200;

    public final String BASE_URL;
    public final String KEY;

    private GptService(Claptrap plugin) {
        this.BASE_URL = plugin.getConfig().getString("baseUrl");
        this.KEY = plugin.getConfig().getString("key");
    }

    public GptSession initSession(String userName, String msg) {
        GptSession session = sessionHashMap.get(userName);
        if (msg.startsWith("\\gpt")) {
            msg = msg.replace("\\gpt", "").trim();
            if (msg.startsWith("start")) {
                session = login(userName);
            }
            if (msg.startsWith("clear")) {
                session.clear();
            }
            if (msg.startsWith("end")) {
                session.reset();
            }
            return session;
        }
        if (session != null) {
            session.setTips(null);
        }
        return session;
    }

    public String textDialog(GptSession session, String userMsg) {
        ChatLanguageModel chatModel = session.getChatModel();
        List<ChatMessage> messages = session.getMessages();
        messages.add(UserMessage.from(userMsg));

        String systemMsg = chatModel.generate(messages).content().text();

        messages.add(SystemMessage.from(systemMsg));
        if (messages.size() > MAX_HISTORY) {
            messages.subList(0, MAX_HISTORY / 2).clear();
        }
        session.setMessages(messages);
        return systemMsg;
    }

    public File imageDialog(GptSession session, String userMsg) {
        FileUtil.pngCleaner(FILE_PATH);
        ImageModel client = session.getImageModel();
        URI response = client.generate(userMsg).content().url();
        return FileUtil.pngConverter(response);
    }

    private GptSession login(String userName) {
        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(KEY)
                .build();
        ImageModel imageModel = OpenAiImageModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(KEY)
                .modelName("dall-e-2")
                .responseFormat("b64_json")
                .withPersisting()
                .persistTo(FILE_PATH)
                .build();
        GptSession session = new GptSession(userName, true, chatModel, imageModel, null);
        sessionHashMap.put(userName, session);
        return session;
    }
}
