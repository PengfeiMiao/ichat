package com.mafiadev.ichat.gpt;

import com.mafiadev.ichat.Claptrap;
import dev.ai4j.openai4j.OpenAiClient;
import dev.ai4j.openai4j.chat.ChatCompletionModel;
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

import static dev.ai4j.openai4j.chat.ChatCompletionModel.GPT_3_5_TURBO;
import static java.time.Duration.ofSeconds;

public class GptService {

    private final String KEY;
    private final String BASE_URL;

    private static final Map<String, OpenAiClient> openAiClientHashMap = new ConcurrentHashMap<>();

    public static final Map<String, ChatCompletionModel> enable40Map = new ConcurrentHashMap<>();

    public static final Map<String, List<Message>> historyMap = new ConcurrentHashMap<>();

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

    public OpenAiClient getClient(String userName) {
        openAiClientHashMap.putIfAbsent(userName, buildClient(userName));
        return openAiClientHashMap.get(userName);
    }

    public List<Message> getHistory(String userName) {
        historyMap.putIfAbsent(userName, new ArrayList<>());
        return historyMap.get(userName);
    }

    public String textDialog(String userName, String userMsg) {
        OpenAiClient client = getClient(userName);
        List<Message> messages = getHistory(userName);
        messages.add(UserMessage.from(userMsg));
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(enable40Map.getOrDefault(userName, GPT_3_5_TURBO))
                .messages(messages)
                .build();

        String systemMsg = client.chatCompletion(chatCompletionRequest).execute().content();
        messages.add(SystemMessage.from(systemMsg));
        historyMap.put(userName, messages);

        return systemMsg;
    }

    public String imageDialog(String userName, String userMsg) {
        OpenAiClient client = getClient(userName);
        GenerateImagesRequest request = GenerateImagesRequest
                .builder()
                .prompt(userMsg)
                .build();

        GenerateImagesResponse response = client.imagesGeneration(request).execute();

        URI remoteImage = response.data().get(0).url();

        return remoteImage.toString();
    }

    public void clear(String userName) {
        openAiClientHashMap.put(userName, buildClient(userName));
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
        new GptService();
    }
}
