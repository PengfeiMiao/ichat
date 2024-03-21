package com.mafiadev.ichat.gpt;

import com.mafiadev.ichat.Claptrap;
import com.mafiadev.ichat.util.URIToPNGConverter;
import dev.ai4j.openai4j.OpenAiClient;
import dev.ai4j.openai4j.chat.ChatCompletionRequest;
import dev.ai4j.openai4j.chat.Message;
import dev.ai4j.openai4j.chat.SystemMessage;
import dev.ai4j.openai4j.chat.UserMessage;
import dev.ai4j.openai4j.image.GenerateImagesRequest;
import dev.ai4j.openai4j.image.GenerateImagesResponse;

import java.io.File;
import java.net.URI;
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
        session.setMessages(messages);
        sessionHashMap.put(session.getUserName(), session);

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

        GenerateImagesResponse response = client.imagesGeneration(request).execute();
        URI localImage = response.data().get(0).url();

        return URIToPNGConverter.convert(localImage);
    }

    private void login(String userName) {
        sessionHashMap.put(userName, new GptSession(userName, true, buildClient(userName), null));
    }

    private void clearHistory(String userName) {
        GptSession session = sessionHashMap.get(userName);
        session.setMessages(GptSession.defaultMessages);
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
                .connectTimeout(Duration.ofSeconds(60))
                .readTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(60))
                .organizationId(UUID.randomUUID().toString())
                .withPersisting()
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
        sessionHashMap.put(testUser, new GptSession(testUser, true, client, null));
        service.imageDialog(sessionHashMap.get(testUser), "draw a cat");
        service.clear(testUser);
    }
}
