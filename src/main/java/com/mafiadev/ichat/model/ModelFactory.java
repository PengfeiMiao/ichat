package com.mafiadev.ichat.model;

import com.mafiadev.ichat.util.CommonUtil;
import com.mafiadev.ichat.util.ConfigUtil;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiImageModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.mafiadev.ichat.constant.Constant.FILE_PATH;
import static dev.langchain4j.model.openai.OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_SMALL;

public class ModelFactory {
    public static ModelConfig buildModelConfig(String name) {
        List<ModelConfig> modelConfigs = ConfigUtil.getConfigArr("models", ModelConfig.class);
        return Optional.ofNullable(modelConfigs).orElse(new ArrayList<>()).stream()
                .filter(it -> Objects.equals(it.getName(), name)).findFirst().orElse(null);
    }

    public static ChatLanguageModel buildChatModel(ModelConfig modelConfig) {
        if (modelConfig == null) {
            return null;
        }
        try {
            if ("openai".equals(modelConfig.getType())) {
                return OpenAiChatModel.builder()
                        .modelName(modelConfig.getName())
                        .baseUrl(modelConfig.getBaseUrl())
                        .apiKey(modelConfig.getApiKey())
                        .build();
            }
            if ("ollama".equals(modelConfig.getType())) {
                return OllamaChatModel.builder()
                        .modelName(modelConfig.getName())
                        .baseUrl(modelConfig.getBaseUrl())
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ImageModel buildImageModel(ModelConfig modelConfig) {
        if (modelConfig == null) {
            return null;
        }
        try {
            return OpenAiImageModel.builder()
                    .modelName(modelConfig.getName())
                    .baseUrl(modelConfig.getBaseUrl())
                    .apiKey(modelConfig.getApiKey())
                    .responseFormat("b64_json")
                    .withPersisting()
                    .persistTo(FILE_PATH)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static EmbeddingModel buildEmbeddingModel(ModelConfig modelConfig) {
        if (modelConfig == null) {
            return null;
        }
        try {
            if ("openai".equals(modelConfig.getType())) {
                return new OpenAiEmbeddingModel.OpenAiEmbeddingModelBuilder()
                        .baseUrl(modelConfig.getBaseUrl())
                        .apiKey(modelConfig.getApiKey())
                        .modelName(TEXT_EMBEDDING_3_SMALL)
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String buildModelName(Object model) {
        if (model == null) {
            return null;
        }
        return CommonUtil.getFieldValue(model, "modelName", String.class);
    }
}
