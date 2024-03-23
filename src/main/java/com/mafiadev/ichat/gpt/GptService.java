package com.mafiadev.ichat.gpt;

import com.mafiadev.ichat.Claptrap;
import com.mafiadev.ichat.util.FileUtil;
import dev.ai4j.openai4j.OpenAiClient;
import dev.ai4j.openai4j.chat.ChatCompletionRequest;
import dev.ai4j.openai4j.chat.Message;
import dev.ai4j.openai4j.chat.SystemMessage;
import dev.ai4j.openai4j.chat.UserMessage;
import dev.ai4j.openai4j.image.GenerateImagesRequest;
import dev.ai4j.openai4j.image.GenerateImagesResponse;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static dev.ai4j.openai4j.image.ImageModel.DALL_E_2;
import static dev.ai4j.openai4j.image.ImageModel.DALL_E_RESPONSE_FORMAT_B64_JSON;

public class GptService {
    private final String KEY;
    private final String BASE_URL;
    private static final Map<String, GptSession> sessionHashMap = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY = 200;
    private static final Path FILE_PATH = Paths.get(System.getProperty("java.io.tmpdir"));

    public GptSession initSession(String userName, String msg) {
        GptSession session = sessionHashMap.get(userName);
        if (msg.startsWith("\\gpt")) {
            msg = msg.replace("\\gpt", "").trim();
            if (msg.startsWith("start")) {
                session = login(userName, buildClient(userName));
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
        OpenAiClient client = session.getClient();
        List<Message> messages = session.getMessages();
        messages.add(UserMessage.from(userMsg));
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(session.getModel())
                .messages(messages)
                .build();

        String systemMsg = client.chatCompletion(chatCompletionRequest).execute().content();
        messages.add(SystemMessage.from(systemMsg));
        if (messages.size() > MAX_HISTORY) {
            messages.subList(0, MAX_HISTORY / 2).clear();
        }
        session.setMessages(messages);

        return systemMsg;
    }

    public File imageDialog(GptSession session, String userMsg) {
        OpenAiClient client = session.getClient();
        GenerateImagesRequest request = GenerateImagesRequest
                .builder()
                .model(DALL_E_2)
                .responseFormat(DALL_E_RESPONSE_FORMAT_B64_JSON)
                .prompt(userMsg)
                .build();

        FileUtil.pngCleaner(FILE_PATH);
        GenerateImagesResponse response = client.imagesGeneration(request).execute();
        URI localImage = response.data().get(0).url();

        return FileUtil.pngConverter(localImage);
    }

    private GptSession login(String userName, OpenAiClient client) {
        GptSession session = new GptSession(userName, true, client, null);
        sessionHashMap.put(userName, session);
        return session;
    }

    private OpenAiClient buildClient(String userName) {
        return OpenAiClient.builder()
                .baseUrl(BASE_URL)
                .openAiApiKey(KEY)
                .callTimeout(Duration.ofSeconds(60))
                .connectTimeout(Duration.ofSeconds(60))
                .readTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(60))
                .organizationId(UUID.randomUUID().toString())
                .withPersisting()
                .persistTo(FILE_PATH)
                .build();
    }

    private GptService(Claptrap plugin) {
        this.BASE_URL = plugin.getConfig().getString("baseUrl");
        this.KEY = plugin.getConfig().getString("key");
    }

    public static GptService INSTANCE;

    public static void init(Claptrap plugin) {
        INSTANCE = new GptService(plugin);
        (new GptListener(plugin)).register();
    }

    private GptService() {
        this.BASE_URL = "";
        this.KEY = "";
    }

    public static void main(String[] args) throws Exception {
        GptService service = new GptService();
        String testUser = "testUser";
        OpenAiClient client = service.buildClient(testUser);
        service.login(testUser, client);
        GptSession session = sessionHashMap.get(testUser);
        session.setMessages(new ArrayList<>());

        session.clear();
        System.out.println("clear");
        System.out.println(sessionHashMap.get(testUser).getMessages());

        service.textDialog(session, "123");
        System.out.println("textDialog");
        System.out.println(sessionHashMap.get(testUser).getMessages());

        session.reset();
        System.out.println("logout");
        System.out.println(sessionHashMap.get(testUser).getLogin());
        System.out.println(sessionHashMap.get(testUser).getMessages());
        System.out.println(sessionHashMap.get(testUser).getTips());
    }
}
