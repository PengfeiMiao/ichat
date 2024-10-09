package com.mafiadev.ichat.service;

import com.mafiadev.ichat.dao.MessageRepository;
import com.mafiadev.ichat.util.CommonUtil;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MessageService {
    private static final ChatMemoryStore chatMemoryStore = new InMemoryChatMemoryStore();

    private final MessageRepository messageRepository = new MessageRepository();
    private final SessionService sessionService = new SessionService();

    public ChatMemoryStore getChatMemoryStore() {
        return chatMemoryStore;
    }

    public MessageWindowChatMemory buildChatMemory(String userName) {
        return MessageWindowChatMemory.builder()
                .maxMessages(100)
                .chatMemoryStore(chatMemoryStore)
                .id(getShortName(userName))
                .build();
    }

    public List<ChatMessage> getMessages(String userName) {
        return chatMemoryStore.getMessages(getShortName(userName));
    }

    public void removeMessages(String userName) {
        chatMemoryStore.deleteMessages(getShortName(userName));
    }

    public void addMessage(String userName, ChatMessage message) {
        getMessages(userName).add(message);
    }

    public void loadMessages() {
        sessionService.getSessions().keySet().forEach(userName ->
                chatMemoryStore.updateMessages(getShortName(userName), messageRepository.findMessages(userName)));
    }

    public void saveMessages() {
        sessionService.getSessions().keySet().forEach(userName ->
                messageRepository.saveMessages(userName, getMessages(userName)));
    }

    @NotNull
    private static String getShortName(String userName) {
        return CommonUtil.tail(userName, 64);
    }
}
