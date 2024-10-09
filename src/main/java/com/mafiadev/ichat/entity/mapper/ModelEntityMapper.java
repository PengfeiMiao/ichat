package com.mafiadev.ichat.entity.mapper;

import com.mafiadev.ichat.entity.SessionEntity;
import com.mafiadev.ichat.model.GptSession;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ModelEntityMapper {
    ModelEntityMapper MAPPER = Mappers.getMapper(ModelEntityMapper.class);

    @Mapping(target = "userName", source = "userName")
    @Mapping(target = "chatModel", expression = "java(serializeChatModel(session.getChatModel()))")
    @Mapping(target = "imageModel", expression = "java(serializeImageModel(session.getImageModel()))")
    @Mapping(target = "gpt4Model", expression = "java(serializeChatModel(session.getGpt4Model()))")
    SessionEntity convertSessionModelToEntity(GptSession session);

    @Mapping(target = "chatModel", ignore = true)
    @Mapping(target = "imageModel", ignore = true)
    @Mapping(target = "gpt4Model", ignore = true)
    GptSession convertSessionEntityToModel(SessionEntity sessionEntity);


    default String serializeChatModel(ChatLanguageModel chatModel) {
        if (chatModel == null) {
            return null;
        }
        return ((OpenAiChatModel) chatModel).modelName();
    }

    default String serializeImageModel(ImageModel chatModel) {
        if (chatModel == null) {
            return null;
        }
        return ((OpenAiImageModel) chatModel).modelName();
    }
}
