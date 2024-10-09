package com.mafiadev.ichat.dao;

import cn.hutool.db.sql.Condition;
import cn.hutool.db.sql.ConditionBuilder;
import com.mafiadev.ichat.dao.helper.SqliteHelper;
import com.mafiadev.ichat.entity.MessageEntity;
import com.mafiadev.ichat.entity.mapper.ModelEntityMapper;
import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

public class MessageRepository {
    public boolean saveMessages(String sessionId, List<ChatMessage> messages) {
        List<MessageEntity> messageEntities =
                ModelEntityMapper.MAPPER.convertChatMessagesToEntities(sessionId, messages);
        SqliteHelper.batchInsert(messageEntities);
        return messageEntities.size() == 0 || messageEntities.get(0).getId() != null;
    }

    public List<ChatMessage> findMessages(String sessionId) {
        List<MessageEntity> messageEntities = SqliteHelper.select(MessageEntity.class,
                ConditionBuilder.of(
                        new Condition("USER_NAME", sessionId)
                ));
        return ModelEntityMapper.MAPPER.convertMessageEntitiesToModels(messageEntities);
    }
}
