package com.mafiadev.ichat.llm;

import com.mafiadev.ichat.Claptrap;
import com.mafiadev.ichat.llm.admin.AdminService;
import com.mafiadev.ichat.constant.GlobalThreadPool;
import com.mafiadev.ichat.llm.agent.Assistant;
import com.mafiadev.ichat.llm.agent.Router;
import com.mafiadev.ichat.llm.tool.WebPageTool;
import com.mafiadev.ichat.model.GptSession;
import com.mafiadev.ichat.service.MessageService;
import com.mafiadev.ichat.service.SessionService;
import com.mafiadev.ichat.util.CommonUtil;
import com.mafiadev.ichat.util.ConfigUtil;
import com.mafiadev.ichat.util.FileUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.service.AiServices;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mafiadev.ichat.constant.Constant.FILE_PATH;

public class GptService {
    public static GptService INSTANCE;
    public static void init(Claptrap plugin) {
        INSTANCE = new GptService(plugin);
    }

    private final String BASE_URL;
    private final List<String> KEYS;
    private final List<String> MODELS;

    private final SessionService sessionService = new SessionService();
    private final MessageService messageService = new MessageService();

    private GptService(Claptrap plugin) {
        this.BASE_URL = plugin.getConfig().getString("baseUrl");
        this.KEYS = ConfigUtil.getConfigArr("keys");
        this.MODELS = ConfigUtil.getConfigArr("models");
        sessionService.loadSessions();
    }

    public GptSession initSession(String userName, String msg) {
        GptSession session = sessionService.getSession(userName);
        if (msg.startsWith("\\gpt")) {
            msg = msg.replace("\\gpt", "").trim();
            if (msg.startsWith("start")) {
                boolean strict = msg.startsWith("start -s");
                session = login(userName, strict);
            }
            if (msg.startsWith("clear")) {
                messageService.removeMessages(userName);
            }
            if (msg.startsWith("end")) {
                session.reset();
                sessionService.updateSession(session);
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
        if(toolRouter(session, userMsg)) {
            chatModel = session.getGpt4Model();
        }
        String userName = session.getUserName();
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .tools(new WebPageTool())
                .chatMemoryProvider(memoryId -> messageService.buildChatMemory(userName))
                .build();
        List<String> failureWords = Arrays.asList("抱歉", "由于网络问题", "请稍后再尝试");
//        chatMemoryStore.getMessages(userName).forEach(item ->
//                System.out.println(item.toString().replace("\n", "\\n")));
        try {
            result = assistant.chat(session.getShortName(), userMsg);
        } catch (Exception e) {
            try {
                result = chatModel.generate(
                        SystemMessage.from(
                                "IF USER INPUT `请求最新信息` OR INPUT `请求使用搜索引擎` AND CONDITION TOOL `查询失败`\n" +
                                        "ELSE YOU OUTPUT `" + String.join("`, `", failureWords) +
                                        "` AROUND YOUR ANSWER"),
                        UserMessage.from(userMsg)).content().text();
                messageService.addMessage(userName, AiMessage.from(result));
            } catch (Exception e1) {
                return String.join(", ", failureWords);
            }
//            result += "\n Details => e: " + e.getMessage();
        }
        filterNoise(session, failureWords);
        return result;
    }

    private void filterNoise(GptSession session, List<String> keywords) {
        GlobalThreadPool.CACHED_EXECUTOR.submit(() -> {
            List<ChatMessage> chatMessages = messageService.getMessages(session.getUserName());
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
                .apiKey(KEYS.get(0))
                .modelName(MODELS.get(0))
                .build();
        ChatLanguageModel gpt4Model = OpenAiChatModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(KEYS.get(1))
                .modelName(MODELS.get(1))
                .build();
        ImageModel imageModel = OpenAiImageModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(KEYS.get(0))
                .modelName("dall-e-2")
                .responseFormat("b64_json")
                .withPersisting()
                .persistTo(FILE_PATH)
                .build();
        GptSession session = new GptSession(userName, true, chatModel, gpt4Model, imageModel, null, strict);
        sessionService.saveSession(session);
        return session;
    }

    public GptService() {
        this.BASE_URL = ConfigUtil.getConfig("baseUrl");
        this.KEYS = ConfigUtil.getConfigArr("keys");
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
            if (question.startsWith("\\admin")) {
                System.out.println(new AdminService().handler(gptSession.getUserName(), question));
                System.out.println();
                continue;
            }
            System.out.println("AI Answer: " + gptService.textDialog(gptSession, question));
            System.out.println();
        }
    }
}
