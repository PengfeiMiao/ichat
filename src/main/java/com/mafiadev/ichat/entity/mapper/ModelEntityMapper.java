package com.mafiadev.ichat.entity.mapper;

import com.mafiadev.ichat.entity.SessionEntity;
import com.mafiadev.ichat.model.GptSession;
import com.mafiadev.ichat.util.ConfigUtil;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static com.mafiadev.ichat.constant.Constant.FILE_PATH;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ModelEntityMapper {
    ModelEntityMapper MAPPER = Mappers.getMapper(ModelEntityMapper.class);

    @Mapping(target = "userName", source = "userName")
    @Mapping(target = "chatModel", expression = "java(serializeChatModel(session.getChatModel()))")
    @Mapping(target = "imageModel", expression = "java(serializeImageModel(session.getImageModel()))")
    @Mapping(target = "gpt4Model", expression = "java(serializeChatModel(session.getGpt4Model()))")
    SessionEntity convertSessionModelToEntity(GptSession session);

    @Mapping(target = "chatModel", expression = "java(deserializeChatModel(sessionEntity.getChatModel()))")
    @Mapping(target = "imageModel", expression = "java(deserializeImageModel(sessionEntity.getImageModel()))")
    @Mapping(target = "gpt4Model", expression = "java(deserializeChatModel(sessionEntity.getChatModel()))")
    GptSession convertSessionEntityToModel(SessionEntity sessionEntity);

    List<GptSession> convertSessionEntitiesToModels(List<SessionEntity> sessionEntities);

    default String serializeChatModel(ChatLanguageModel chatModel) {
        return chatModel == null ? null : ((OpenAiChatModel) chatModel).modelName();
    }

    default String serializeImageModel(ImageModel imageModel) {
        return imageModel == null ? null : ((OpenAiImageModel) imageModel).modelName();
    }

    default ChatLanguageModel deserializeChatModel(String modelName) {
        int index = modelName.startsWith("gpt-3") ? 0 : 1;
        return OpenAiChatModel.builder()
                .baseUrl(ConfigUtil.getConfig("baseUrl"))
                .apiKey(ConfigUtil.getConfigArr("keys").get(index))
                .modelName(modelName)
                .build();
    }

    default ImageModel deserializeImageModel(String modelName) {
        return OpenAiImageModel.builder()
                .baseUrl(ConfigUtil.getConfig("baseUrl"))
                .apiKey(ConfigUtil.getConfigArr("keys").get(0))
                .modelName(modelName)
                .responseFormat("b64_json")
                .withPersisting()
                .persistTo(FILE_PATH)
                .build();
    }
}
