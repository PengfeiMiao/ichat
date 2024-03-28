package com.mafiadev.ichat.gpt;

import com.mafiadev.ichat.Claptrap;
import com.mafiadev.ichat.gpt.agent.Assistant;
import com.mafiadev.ichat.gpt.tool.WebPageTool;
import com.mafiadev.ichat.util.FileUtil;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.service.AiServices;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.mafiadev.ichat.constant.Constant.FILE_PATH;

public class GptService {
    public static GptService INSTANCE;

    public static void init(Claptrap plugin) {
        INSTANCE = new GptService(plugin);
    }

    private static final Map<String, GptSession> sessionHashMap = new ConcurrentHashMap<>();

    private final String BASE_URL;
    private final String KEY;

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
        String userName = session.userName;
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .tools(new WebPageTool())
                .chatMemoryProvider(memoryId -> session.getChatMemory())
                .build();
        System.out.println("getChatMemory: " + session.getChatMemory().messages());
        try {
            return assistant.chat(userName, userMsg);
        } catch (Exception e) {
            return chatModel.generate(
                    SystemMessage.from("如果我请求获取最新信息或明确请求使用搜索引擎，告知我搜索失败，返回的答案不具有时效性"),
                    UserMessage.from(userMsg)).content().text();
        }
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

    public GptService() {
        this.BASE_URL = System.getenv("AI_URL");
        this.KEY = System.getenv("AI_KEY");
    }

    public static void main(String[] args) {
        String question = "用搜狗帮我搜索，《菊花台》是谁的歌";
        GptService gptService = new GptService();
        GptSession gptSession = gptService.login("test");
        System.out.println(gptService.textDialog(gptSession, question));
    }
}
