package com.mafiadev.ichat.entity.mapper;

import com.mafiadev.ichat.entity.MessageEntity;
import com.mafiadev.ichat.entity.SessionEntity;
import com.mafiadev.ichat.model.GptSession;
import com.mafiadev.ichat.model.ModelConfig;
import com.mafiadev.ichat.model.ModelFactory;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.image.ImageModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.mafiadev.ichat.entity.mapper.MessageType.AI;
import static com.mafiadev.ichat.entity.mapper.MessageType.SYSTEM;
import static com.mafiadev.ichat.entity.mapper.MessageType.USER;
import static com.mafiadev.ichat.entity.mapper.MessageType.of;

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
    @Mapping(target = "toolModel", expression = "java(serializeChatModel(session.getToolModel()))")
    SessionEntity convertSessionModelToEntity(GptSession session);

    @Mapping(target = "chatModel", expression = "java(deserializeChatModel(sessionEntity.getChatModel()))")
    @Mapping(target = "imageModel", expression = "java(deserializeImageModel(sessionEntity.getImageModel()))")
    @Mapping(target = "toolModel", expression = "java(deserializeChatModel(sessionEntity.getToolModel()))")
    GptSession convertSessionEntityToModel(SessionEntity sessionEntity);

    List<GptSession> convertSessionEntitiesToModels(List<SessionEntity> sessionEntities);

    default String serializeChatModel(ChatLanguageModel chatModel) {
        return ModelFactory.buildModelName(chatModel);
    }

    default String serializeImageModel(ImageModel imageModel) {
        return ModelFactory.buildModelName(imageModel);
    }

    default ChatLanguageModel deserializeChatModel(String modelName) {
        ModelConfig modelConfig = ModelFactory.buildModelConfig(modelName);
        return ModelFactory.buildChatModel(modelConfig);
    }

    default ImageModel deserializeImageModel(String modelName) {
        ModelConfig imageConfig = ModelFactory.buildModelConfig("gpt-4o-mini");
        imageConfig.setName(modelName);
        return ModelFactory.buildImageModel(imageConfig);
    }

    default String getChatType(ChatMessage chatMessage) {
        if (chatMessage instanceof SystemMessage) {
            return SYSTEM.getName();
        }
        if (chatMessage instanceof UserMessage) {
            return USER.getName();
        }
        return AI.getName();
    }

    default ChatMessage getChatMessage(MessageEntity messageEntity) {
        MessageType type = of(messageEntity.getType());
        switch (type) {
            case SYSTEM:
                return SystemMessage.from(messageEntity.getText());
            case USER:
                return UserMessage.from(messageEntity.getText());
            default:
                return AiMessage.from(messageEntity.getText());
        }
    }
}
