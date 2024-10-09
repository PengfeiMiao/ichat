package com.mafiadev.ichat.service;

import com.mafiadev.ichat.util.CommonUtil;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;

import java.util.List;

public class MessageService {
    private static final ChatMemoryStore chatMemoryStore = new InMemoryChatMemoryStore();

    public ChatMemoryStore getChatMemoryStore() {
        return chatMemoryStore;
    }

    public MessageWindowChatMemory buildChatMemory(String userName) {
        return MessageWindowChatMemory.builder()
                .maxMessages(100)
                .chatMemoryStore(chatMemoryStore)
                .id(CommonUtil.tail(userName, 64))
                .build();
    }

    public List<ChatMessage> getMessages(String userName) {
        return chatMemoryStore.getMessages(CommonUtil.tail(userName, 64));
    }

    public void removeMessages(String userName) {
        chatMemoryStore.deleteMessages(CommonUtil.tail(userName, 64));
    }

    public void addMessage(String userName, ChatMessage message) {
        chatMemoryStore.getMessages(CommonUtil.tail(userName, 64)).add(message);
    }
}
