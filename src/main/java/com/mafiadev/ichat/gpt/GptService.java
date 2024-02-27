package com.mafiadev.ichat.gpt;

import com.mafiadev.ichat.Claptrap;
import dev.ai4j.openai4j.OpenAiClient;
import dev.ai4j.openai4j.chat.ChatCompletionModel;
import dev.ai4j.openai4j.chat.ChatCompletionRequest;
import dev.ai4j.openai4j.image.GenerateImagesRequest;
import dev.ai4j.openai4j.image.GenerateImagesResponse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static dev.ai4j.openai4j.chat.ChatCompletionModel.GPT_3_5_TURBO;
import static java.time.Duration.ofSeconds;

public class GptService {

    private String KEY;
    private String BASE_URL;

    public void setKEY(String KEY) {
        this.KEY = KEY;
    }

    public void setBASE_URL(String BASE_URL) {
        this.BASE_URL = BASE_URL;
    }

    /**
     * 每个微信用户单独开一个会话
     */
    private static final Map<String, OpenAiClient> openAiClientHashMap = new ConcurrentHashMap<>();

    public static Map<String, ChatCompletionModel> enable40Map = new ConcurrentHashMap<>();

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

    public static GptService INSTANCE;

    public void clear(String userName) {
        openAiClientHashMap.put(userName, buildClient(userName));
    }

    private GptService(Claptrap plugin) {
        this.BASE_URL = plugin.getConfig().getString("baseUrl");
        this.KEY = plugin.getConfig().getString("key");
    }

    public static void init(Claptrap plugin) {
        INSTANCE = new GptService(plugin);
        (new GptListener(plugin)).register();
    }

    private GptService() {
        this.BASE_URL = "";
        this.KEY = "";
    }

    public static void main(String[] args) {
        INSTANCE = new GptService();
        OpenAiClient client = INSTANCE.getClient("test");
//        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
//                .model(GptService.enable40Map.getOrDefault("test", GPT_3_5_TURBO))
//                .addUserMessage("说：这是一个测试")
//                .build();
//
//        String execute = client.chatCompletion(chatCompletionRequest).execute().content();
        GenerateImagesRequest generateImagesRequest = GenerateImagesRequest.builder()
                .prompt("帮我画一只鸟")
                .build();
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        client.imagesGeneration(generateImagesRequest)
                .onResponse(generateImagesResponse -> {
                    List<String> imageDataList = new ArrayList<>();
                    for (GenerateImagesResponse.ImageData datum : generateImagesResponse.data()) {
                        System.out.println("generating...");
                        imageDataList.add(datum.b64Json());
                    }
                    future.complete(imageDataList);
                })
                .onError(future::completeExceptionally);

        try {
            System.out.println("generate image start");
            List<String> imageDataList = future.get(60, TimeUnit.SECONDS);
            System.out.println("generate image end");
            System.out.println(imageDataList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
