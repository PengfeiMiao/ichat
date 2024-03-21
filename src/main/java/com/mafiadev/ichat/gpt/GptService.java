package com.mafiadev.ichat.gpt;

import com.mafiadev.ichat.Claptrap;
import dev.ai4j.openai4j.OpenAiClient;
import dev.ai4j.openai4j.chat.ChatCompletionRequest;
import dev.ai4j.openai4j.chat.Message;
import dev.ai4j.openai4j.chat.SystemMessage;
import dev.ai4j.openai4j.chat.UserMessage;
import dev.ai4j.openai4j.image.GenerateImagesRequest;
import dev.ai4j.openai4j.image.GenerateImagesResponse;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.Duration.ofSeconds;

public class GptService {

    private final String KEY;
    private final String BASE_URL;

    private static final Map<String, GptSession> sessionHashMap = new ConcurrentHashMap<>();

    public GptSession initSession(String userName, String msg) {
        if (msg.startsWith("\\gpt")) {
            msg = msg.replace("\\gpt", "").trim();
            if (msg.startsWith("start")) {
                login(userName);
            }
            if (msg.startsWith("clear")) {
                clearHistory(userName);
            }
            if (msg.startsWith("end")) {
                clear(userName);
            }
            return sessionHashMap.get(userName);
        }
        GptSession session = sessionHashMap.get(userName);
        session.setTips(null);
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
        session.setMessages(messages);
        sessionHashMap.put(session.getUserName(), session);

        return systemMsg;
    }

    public String imageDialog(GptSession session, String userMsg) {
        OpenAiClient client = session.getClient();
        GenerateImagesRequest request = GenerateImagesRequest
                .builder()
                .prompt(userMsg)
                .build();

        GenerateImagesResponse response = client.imagesGeneration(request).execute();

        URI remoteImage = response.data().get(0).url();

        return remoteImage.toString();
    }

    private void login(String userName) {
        sessionHashMap.put(userName, new GptSession(userName, true, buildClient(userName), "gpt startup"));
    }

    private void clearHistory(String userName) {
        GptSession session = sessionHashMap.get(userName);
        session.setMessages(new ArrayList<>());
        session.setTips("cleared");
        sessionHashMap.put(userName, session);
    }

    private void clear(String userName) {
        GptSession session = sessionHashMap.get(userName);
        session.getClient().shutdown();
        session.setClient(null);
        session.setLogin(false);
        session.setTips("bye");
        sessionHashMap.put(userName, session);
    }

    private OpenAiClient buildClient(String userName) {
        return OpenAiClient.builder()
                .baseUrl(BASE_URL)
                .openAiApiKey(KEY)
                .callTimeout(Duration.ofSeconds(60))
                .connectTimeout(ofSeconds(60))
                .readTimeout(ofSeconds(60))
                .organizationId(UUID.randomUUID().toString())
                .writeTimeout(ofSeconds(60)).build();
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
        sessionHashMap.put(testUser, new GptSession(testUser, true, client, null));
        service.imageDialog(sessionHashMap.get(testUser), "draw a bird");
        service.clear(testUser);
    }
}
