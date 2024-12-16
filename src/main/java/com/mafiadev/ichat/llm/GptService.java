package com.mafiadev.ichat.llm;

import com.mafiadev.ichat.constant.GlobalThreadPool;
import com.mafiadev.ichat.llm.admin.AdminService;
import com.mafiadev.ichat.llm.agent.Assistant;
import com.mafiadev.ichat.llm.agent.Router;
import com.mafiadev.ichat.llm.agent.TaskHost;
import com.mafiadev.ichat.llm.tool.WebPageTool;
import com.mafiadev.ichat.model.GptSession;
import com.mafiadev.ichat.model.ModelConfig;
import com.mafiadev.ichat.model.ModelFactory;
import com.mafiadev.ichat.model.struct.RouterType;
import com.mafiadev.ichat.model.struct.Task;
import com.mafiadev.ichat.service.MessageService;
import com.mafiadev.ichat.service.SessionService;
import com.mafiadev.ichat.service.TaskService;
import com.mafiadev.ichat.util.CommonUtil;
import com.mafiadev.ichat.util.ConfigUtil;
import com.mafiadev.ichat.util.FileUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mafiadev.ichat.constant.Constant.DB_PATH;
import static com.mafiadev.ichat.constant.Constant.FILE_PATH;

@Slf4j
public class GptService {
    private static volatile GptService instance;

    public static GptService getInstance() {
        if (instance == null) {
            synchronized (GptService.class) {
                if (instance == null) {
                    instance = new GptService();
                }
            }
        }
        return instance;
    }

    public static void init() {
        instance = GptService.getInstance();
    }

    private final ModelConfig TOOL_CONFIG;
    private final ModelConfig CHAT_CONFIG;

    private final SessionService sessionService = new SessionService();
    private final MessageService messageService = new MessageService();
    private final TaskService taskService = new TaskService();

    private GptService() {
        TOOL_CONFIG = ModelFactory.buildModelConfig(ConfigUtil.getConfig("toolModel"));
        CHAT_CONFIG = ModelFactory.buildModelConfig(ConfigUtil.getConfig("chatModel"));
        sessionService.loadSessions();
    }

    public GptSession initSession(String userName, String msg) {
        GptSession session = sessionService.getSession(userName);
        String[] groupUser = CommonUtil.decode(userName).split("&&");
        boolean inGroup = groupUser.length > 1 && !groupUser[0].equals(groupUser[1]);
        String groupName = CommonUtil.encode(groupUser[0]);
        if (msg.startsWith("\\gpt")) {
            boolean multiple = inGroup && CommonUtil.isMatch(msg, "^\\\\gpt .*-g");
            msg = msg.replace("\\gpt", "").trim();
            if (msg.startsWith("start")) {
                boolean strict = CommonUtil.isMatch(msg, "^start .*-s");
                if (multiple && session != null) {
                    session.reset(null);
                    sessionService.updateSession(session);
                }
                session = login(multiple ? groupName : userName, strict, multiple);
            }
            if (multiple) {
                session = sessionService.getSession(groupName);
            }
            if (msg.startsWith("clear")) {
                messageService.removeMessages(multiple ? groupName : userName);
            }
            if (msg.startsWith("end")) {
                session.reset("bye");
                sessionService.updateSession(session);
            }
            return session;
        }
        if (inGroup && (session == null || !session.getLogin())) {
            session = sessionService.getSession(groupName);
        }
        if (session != null) {
            session.setTips(null);
        }
        return session;
    }

    public RouterType router(GptSession session, String userMsg) {
        ChatLanguageModel chatModel = session.getChatModel();
        Router router = AiServices.builder(Router.class).chatLanguageModel(chatModel).build();
        try {
            return router.route(session.getShortName(), userMsg, RouterType.getDescriptions());
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.contains("Unknown enum value")) {
                Optional<RouterType> typeOptional = Arrays.stream(RouterType.values())
                        .filter(it -> message.contains(it.name())).findAny();
                if(typeOptional.isPresent()) {
                    return typeOptional.get();
                }
            }
        }
        return RouterType.OTHER;
    }

    public Object multiDialog(GptSession session, String userMsg) {
        RouterType router = router(session, userMsg);
        log.info(router.name());
        switch (router) {
            case TIME:
                return textDialog(session, userMsg, true);
            case SEARCH:
                return searchDialog(session, userMsg);
            case IMAGE:
                return imageDialog(session, userMsg);
            case TASK_ADD:
                return taskDialog(session, userMsg, RouterType.TASK_ADD);
            case TASK_DEL:
                return taskDialog(session, userMsg, RouterType.TASK_DEL);
            case TASK_LS:
                return taskDialog(session, userMsg, RouterType.TASK_LS);
            default:
                return textDialog(session, userMsg, false);
        }
    }

    public String textDialog(GptSession session, String userMsg, boolean useTool) {
        String result = "";
        ChatLanguageModel chatModel = useTool ? session.getToolModel() : session.getChatModel();
        String[] extra = useTool ? new String[] {"当前时间: " + new Date()} : new String[0];
        String userName = session.getUserName();
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .tools(new WebPageTool())
                .chatMemoryProvider(memoryId -> messageService.buildChatMemory(userName))
                .build();
        List<String> failureWords = Arrays.asList("抱歉", "由于网络问题", "请稍后再尝试");
        try {
            result = assistant.chat(session.getShortName(), userMsg, extra);
        } catch (Exception e) {
            try {
                result = session.getChatModel().generate(
                        SystemMessage.from(
                                "IF USER INPUT `请求最新信息` OR INPUT `请求使用搜索引擎` AND CONDITION TOOL `查询失败`\n" +
                                        "ELSE YOU OUTPUT `" + String.join("`, `", failureWords) +
                                        "` AROUND YOUR ANSWER"),
                        UserMessage.from(userMsg)).content().text();
                messageService.addMessage(userName, AiMessage.from(result));
            } catch (Exception e1) {
                return String.join(", ", failureWords);
            }
        }
        filterNoise(session, failureWords);
        return result;
    }

    public String taskDialog(GptSession session, String userMsg, RouterType type) {
        ChatLanguageModel chatModel = session.getChatModel();
        TaskHost host = AiServices.builder(TaskHost.class).chatLanguageModel(chatModel).build();
        String shortName = session.getShortName();
        switch (type) {
            case TASK_LS:
                return host.list(shortName, taskService.findTasks(session.getUserName()));
            case TASK_DEL:
                List<Task> tasks = taskService.findTasks(session.getUserName());
                Task taskDel = host.delete(shortName, tasks, userMsg + " \n当前时间: " + new Date());
                if (!taskDel.getCronExpr().isEmpty() && !taskDel.getContent().isEmpty()) {
                    tasks.removeIf(it ->
                            it.getCronExpr().equals(taskDel.getCronExpr()) || it.getContent().equals(taskDel.getContent()));
                    taskService.updateTasks(shortName, tasks);
                }
                return taskDel.getCreatedTips();
            default:
                Task taskAdd = host.schedule(shortName, userMsg + " \n当前时间: " + new Date());
                if (taskAdd != null && taskAdd.getCronExpr() != null && !taskAdd.getCronExpr().isEmpty()) {
                    taskService.saveTask(session.getUserName(), taskAdd);
                    return taskAdd.getCreatedTips();
                }
                return "创建失败";
        }
    }

    public String searchDialog(GptSession session, String userMsg) {
        EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        ContentRetriever embeddingStoreContentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.6)
                .build();
        WebSearchEngine webSearchEngine = TavilyWebSearchEngine.builder()
                .apiKey(ConfigUtil.getConfig("tavily.key")) // get a free key: https://app.tavily.com/sign-in
                .build();
        ContentRetriever webSearchContentRetriever = WebSearchContentRetriever.builder()
                .webSearchEngine(webSearchEngine)
                .maxResults(3)
                .build();
        QueryRouter queryRouter = new DefaultQueryRouter(embeddingStoreContentRetriever, webSearchContentRetriever);
        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryRouter(queryRouter)
                .build();

        ChatLanguageModel chatModel = session.getChatModel();
        String userName = session.getUserName();
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemoryProvider(memoryId -> messageService.buildChatMemory(userName))
                .build();
        return assistant.chat(session.getShortName(), userMsg);
    }

    public File imageDialog(GptSession session, String userMsg) {
        FileUtil.pngCleaner(FILE_PATH);
        ImageModel client = session.getImageModel();
        URI response = client.generate(userMsg).content().url();
        return FileUtil.pngConverter(response);
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

    public GptSession login(String userName, boolean strict, boolean multiple) {
        ChatLanguageModel chatModel = ModelFactory.buildChatModel(CHAT_CONFIG);
        ChatLanguageModel gpt4Model = ModelFactory.buildChatModel(TOOL_CONFIG);

        ModelConfig imageConfig = ModelFactory.buildModelConfig("gpt-4o-mini");
        imageConfig.setName("dall-e-2");
        ImageModel imageModel = ModelFactory.buildImageModel(imageConfig);
        GptSession session = new GptSession(userName, true, chatModel, gpt4Model, imageModel, null, strict, multiple);
        sessionService.saveSession(session);
        return session;
    }

    public static void main(String[] args) {
        System.out.println("DB url: jdbc:sqlite:" + DB_PATH);
        GptService gptService = new GptService();
        String userName = "ODM2MzUyNDkxJm1hZmlhMjMzJiY4MzYzNDkzNzMmTVBG";
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String question = scanner.nextLine();
            if (question == null || question.isEmpty()) {
                continue;
            }
            GptSession gptSession = gptService.initSession(userName, question);
            if (question.startsWith("\\admin")) {
                log.info("\n" + new AdminService().handler(gptSession.getUserName(), question));
                continue;
            }
            if (gptSession != null && gptSession.getLogin()) {
//                System.out.println(gptService.router(gptSession, question));
//                System.out.println(gptService.taskDialog(gptSession, question, RouterType.TASK_ADD));
                System.out.println("\n" + gptService.multiDialog(gptSession, question));
            }
        }
    }
}
