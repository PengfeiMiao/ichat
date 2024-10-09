package com.mafiadev.ichat.entity.mapper;

import com.mafiadev.ichat.entity.MessageEntity;
import com.mafiadev.ichat.entity.SessionEntity;
import com.mafiadev.ichat.model.GptSession;
import com.mafiadev.ichat.util.ConfigUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.mafiadev.ichat.constant.Constant.FILE_PATH;
import static com.mafiadev.ichat.entity.mapper.MessageType.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ModelEntityMapper {
    ModelEntityMapper MAPPER = Mappers.getMapper(ModelEntityMapper.class);

    default List<MessageEntity> convertChatMessagesToEntities(String userName, List<ChatMessage> chatMessages) {
        return chatMessages.stream()
                .map(message -> MessageEntity.builder()
                        .userName(userName).type(getChatType(message)).text(message.text())
                        .build())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    default List<ChatMessage> convertMessageEntitiesToModels(List<MessageEntity> messageEntities) {
        return messageEntities.stream()
                .map(this::getChatMessage)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

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

    default String getChatType(ChatMessage chatMessage) {
        if (chatMessage instanceof SystemMessage) {
            return SYSTEM.getName();
        }
        if (chatMessage instanceof AiMessage && !((AiMessage) chatMessage).hasToolExecutionRequests()) {
            return AI.getName();
        }
        if (chatMessage instanceof UserMessage) {
            return USER.getName();
        }
        return null;
    }

    default ChatMessage getChatMessage(MessageEntity messageEntity) {
        MessageType type = of(messageEntity.getType());
        switch (type) {
            case SYSTEM:
                return SystemMessage.from(messageEntity.getText());
            case AI:
                return AiMessage.from(messageEntity.getText());
            case USER:
                return UserMessage.from(messageEntity.getText());
            default:
                return null;
        }
    }
}
