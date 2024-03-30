package com.mafiadev.ichat.llm;

import com.mafiadev.ichat.Claptrap;
import com.mafiadev.ichat.constant.GlobalThreadPool;
import com.mafiadev.ichat.llm.agent.Assistant;
import com.mafiadev.ichat.llm.tool.WebPageTool;
import com.mafiadev.ichat.util.CommonUtil;
import com.mafiadev.ichat.util.FileUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.mafiadev.ichat.constant.Constant.FILE_PATH;

public class GptService {
    public static GptService INSTANCE;

    public static void init(Claptrap plugin) {
        INSTANCE = new GptService(plugin);
    }

    private static final Map<String, GptSession> sessionHashMap = new ConcurrentHashMap<>();
    private static final ChatMemoryStore chatMemoryStore = new InMemoryChatMemoryStore();

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
                chatMemoryStore.deleteMessages(userName);
            }
            if (msg.startsWith("end")) {
                chatMemoryStore.deleteMessages(userName);
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
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .maxMessages(100)
                        .chatMemoryStore(chatMemoryStore)
                        .id(userName)
                        .build())
                .build();
        List<String> failureWords = Arrays.asList("抱歉，", "由于网络问题", "请稍后再尝试");
        filterNoise(session, failureWords);
//        System.out.println("++++++++++");
//        chatMemoryStore.getMessages(session.userName).forEach(item ->
//                System.out.println(item.toString().replace("\n", "\\n")));
//        System.out.println("----------");
        try {
            return assistant.chat(userName, userMsg);
        } catch (Exception e) {
            String result = chatModel.generate(
                    SystemMessage.from(
                            "IF USER INPUT `请求最新信息` OR INPUT `请求使用搜索引擎` AND CONDITION TOOL `查询失败`\n" +
                                    "ELSE YOU OUTPUT `" + String.join("`, `", failureWords) + "` AROUND YOUR ANSWER"),
                    UserMessage.from(userMsg)).content().text();
            chatMemoryStore.getMessages(session.userName).add(AiMessage.from(
                    CommonUtil.isSimilar(result, "抱歉，", 0.5) ? "" : result));
            return result;
        }
    }

    private static void filterNoise(GptSession session, List<String> keywords) {
        GlobalThreadPool.SCHEDULED_EXECUTOR.schedule(() -> {
            List<ChatMessage> chatMessages = chatMemoryStore.getMessages(session.userName);
            for (int i = 0; i < chatMessages.size(); i++) {
                ChatMessage it = chatMessages.get(i);
                if (it instanceof AiMessage &&
                        ((AiMessage) it).text() != null &&
                        keywords.stream().anyMatch(
                                word -> CommonUtil.isSimilar(((AiMessage) it).text(), word, 0.5))) {
                    chatMessages.set(i, AiMessage.from(""));
                }
            }
        }, 3, TimeUnit.SECONDS);
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
                .modelName("gpt-3.5-turbo")
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
//        String question = "今日白羊星座分析";
        GptService gptService = new GptService();
        GptSession gptSession = gptService.login("test");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String question = scanner.nextLine();
            if (question == null || question.isEmpty()) {
                continue;
            }
            System.out.println("AI Answer: " + gptService.textDialog(gptSession, question));
            System.out.println();
        }
    }
}
