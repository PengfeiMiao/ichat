package com.mafiadev.ichat.llm;

import com.mafiadev.ichat.Claptrap;
import com.mafiadev.ichat.admin.AdminService;
import com.mafiadev.ichat.constant.GlobalThreadPool;
import com.mafiadev.ichat.llm.agent.Assistant;
import com.mafiadev.ichat.llm.agent.Router;
import com.mafiadev.ichat.llm.tool.WebPageTool;
import com.mafiadev.ichat.util.CommonUtil;
import com.mafiadev.ichat.util.ConfigUtil;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mafiadev.ichat.constant.Constant.FILE_PATH;

public class GptService {
    public static GptService INSTANCE;

    public static void init(Claptrap plugin) {
        INSTANCE = new GptService(plugin);
    }

    public static final Map<String, GptSession> sessionHashMap = new ConcurrentHashMap<>();
    public static final ChatMemoryStore chatMemoryStore = new InMemoryChatMemoryStore();

    private final String BASE_URL;
    private final String KEY;
    private final List<String> MODELS;

    private GptService(Claptrap plugin) {
        this.BASE_URL = plugin.getConfig().getString("baseUrl");
        this.KEY = plugin.getConfig().getString("key");
        this.MODELS = ConfigUtil.getConfigArr("models");
    }

    public GptSession initSession(String userName, String msg) {
        GptSession session = sessionHashMap.get(userName);
        if (msg.startsWith("\\gpt")) {
            msg = msg.replace("\\gpt", "").trim();
            if (msg.startsWith("start")) {
                boolean strict = msg.startsWith("start -s");
                session = login(userName, strict);
            }
            if (msg.startsWith("clear")) {
                chatMemoryStore.deleteMessages(CommonUtil.tail(userName, 64));
            }
            if (msg.startsWith("end")) {
//                chatMemoryStore.deleteMessages(userName);
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
        String result = "";
        ChatLanguageModel chatModel = session.getChatModel();
        String userName = session.getShortName();
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .tools(new WebPageTool())
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .maxMessages(100)
                        .chatMemoryStore(chatMemoryStore)
                        .id(userName)
                        .build())
                .build();
        List<String> failureWords = Arrays.asList("抱歉", "由于网络问题", "请稍后再尝试");
//        System.out.println("++++++++++");
//        chatMemoryStore.getMessages(userName).forEach(item ->
//                System.out.println(item.toString().replace("\n", "\\n")));
//        System.out.println("----------");
        try {
            result = assistant.chat(userName, userMsg);
        } catch (Exception e) {
            try {
                result = chatModel.generate(
                        SystemMessage.from(
                                "IF USER INPUT `请求最新信息` OR INPUT `请求使用搜索引擎` AND CONDITION TOOL `查询失败`\n" +
                                        "ELSE YOU OUTPUT `" + String.join("`, `", failureWords) +
                                        "` AROUND YOUR ANSWER"),
                        UserMessage.from(userMsg)).content().text();
                chatMemoryStore.getMessages(userName).add(AiMessage.from(result));
            } catch (Exception e1) {
                return String.join(", ", failureWords);
            }
//            result += "\n Details => e: " + e.getMessage();
        }
        filterNoise(session, failureWords);
        return result;
    }

    private static void filterNoise(GptSession session, List<String> keywords) {
        GlobalThreadPool.CACHED_EXECUTOR.submit(() -> {
            List<ChatMessage> chatMessages = chatMemoryStore.getMessages(session.getShortName());
            List<Integer> intervals = IntStream.range(0, chatMessages.size())
                    .filter(i -> chatMessages.get(i) instanceof UserMessage)
                    .boxed().collect(Collectors.toList());
            Set<ChatMessage> toDeletedMessages = new HashSet<>();
            for (int i = 0; i < chatMessages.size(); i++) {
                ChatMessage it = chatMessages.get(i);
                if (it instanceof AiMessage &&
                        ((AiMessage) it).text() != null &&
                        keywords.stream().anyMatch(
                                word -> CommonUtil.isSimilar(((AiMessage) it).text(), word, 0.5))) {
                    for (int j = 0; j < intervals.size() - 1; j++) {
                        Integer start = intervals.get(j);
                        Integer end = intervals.get(j + 1);
                        if (i > start && i < end) {
                            toDeletedMessages.addAll(chatMessages.subList(start, end));
                            break;
                        }
                    }
                }
            }
            chatMessages.removeAll(toDeletedMessages);
        });
    }

    public File imageDialog(GptSession session, String userMsg) {
        FileUtil.pngCleaner(FILE_PATH);
        ImageModel client = session.getImageModel();
        URI response = client.generate(userMsg).content().url();
        return FileUtil.pngConverter(response);
    }

    public boolean toolRouter(GptSession session, String userMsg) {
        ChatLanguageModel chatModel = session.getChatModel();
        Router router = AiServices.builder(Router.class).chatLanguageModel(chatModel).build();
        return router.routeTool(session.getShortName(), userMsg);
    }

    public boolean imageRouter(GptSession session, String userMsg) {
        ChatLanguageModel chatModel = session.getChatModel();
        Router router = AiServices.builder(Router.class).chatLanguageModel(chatModel).build();
        return router.routeDraw(session.getShortName(), userMsg);
    }

    public GptSession login(String userName, boolean strict) {
        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(KEY)
                .modelName(MODELS.get(0))
                .build();
        ImageModel imageModel = OpenAiImageModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(KEY)
                .modelName("dall-e-2")
                .responseFormat("b64_json")
                .withPersisting()
                .persistTo(FILE_PATH)
                .build();
        GptSession session = new GptSession(userName, true, chatModel, imageModel, null, strict);
        sessionHashMap.put(userName, session);
        return session;
    }

    public GptService() {
        this.BASE_URL = ConfigUtil.getConfig("baseUrl");
        this.KEY = ConfigUtil.getConfig("key");
        this.MODELS = ConfigUtil.getConfigArr("models");
    }

    public static void main(String[] args) {
        GptService gptService = new GptService();
        GptSession gptSession = gptService.login("test1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", false);
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String question = scanner.nextLine();
            if (question == null || question.isEmpty()) {
                continue;
            }
            if (question.equals("admin clear")) {
                AdminService.clear(sessionHashMap, chatMemoryStore);
                continue;
            }
            System.out.println("AI Answer: " + gptService.textDialog(gptSession, question));
            System.out.println();
        }
    }
}
