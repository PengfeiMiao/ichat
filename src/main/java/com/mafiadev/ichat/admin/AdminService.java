package com.mafiadev.ichat.admin;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mafiadev.ichat.Claptrap;
import com.mafiadev.ichat.llm.GptService;
import com.mafiadev.ichat.domain.GptSession;
import com.mafiadev.ichat.util.CacheUtil;
import com.mafiadev.ichat.util.CommonUtil;
import com.mafiadev.ichat.util.FileUtil;
import com.meteor.wechatbc.entitiy.contact.Contact;
import com.meteor.wechatbc.impl.contact.ContactManager;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mafiadev.ichat.constant.Constant.FILE_PATH;

public class AdminService {
    public static AdminService INSTANCE;
    public static final Path JSON_PATH = Paths.get(FILE_PATH.toString(), "history.json");

    public static void init(Claptrap plugin) {
        INSTANCE = new AdminService(plugin);
    }

    private static final List<String> admins = new ArrayList<>();
    private static final Set<String> sessionIds = new ConcurrentHashSet<>();
    private final String password;
    private final ContactManager contactManager;

    private AdminService(Claptrap plugin) {
        this.password = plugin.getConfig().getString("adminPwd");
        this.contactManager = plugin.getWeChatClient().getContactManager();
        load(this.contactManager, GptService.sessionHashMap, GptService.chatMemoryStore);
    }

    public String handler(String sessionId, String msg) {
        sessionIds.add(sessionId);
        if (msg.startsWith("\\admin")) {
            msg = msg.replace("\\admin", "").trim();
            if (msg.startsWith("login " + password)) {
                admins.add(sessionId);
                return "login success";
            }
            if (admins.contains(sessionId)) {
                Map<String, GptSession> sessionHashMap = GptService.sessionHashMap;
                ChatMemoryStore chatMemoryStore = GptService.chatMemoryStore;
                if (msg.startsWith("load")) {
                    load(this.contactManager, sessionHashMap, chatMemoryStore);
                    return "load success";
                }
                if (msg.startsWith("store")) {
                    store(sessionHashMap, chatMemoryStore);
                    return "store success";
                }
                if (msg.startsWith("clear")) {
                    clear(sessionHashMap, chatMemoryStore);
                    return "clear success";
                }
                if (msg.startsWith("output")) {
                    return output(chatMemoryStore);
                }
            }
            return "please login";
        }
        return null;
    }

    private static void load(ContactManager contactManager,
                             Map<String, GptSession> sessionMap,
                             ChatMemoryStore chatMemoryStore) {
        String json = FileUtil.readJson(JSON_PATH);
        History history = JSONObject.parseObject(json, History.class);
        if (history != null) {
            sessionIds.addAll(history.getHistories().keySet());
            sessionIds.forEach(sessionId -> {
                String[] sessionInfo = sessionId.split("&");
                if (sessionInfo.length > 2) {
                    Contact currentUser = contactManager.getContactByNickName(sessionInfo[1]);
                    String newSessionId = CommonUtil.encode(currentUser.getUserName() + "&" + currentUser.getNickName());
                    sessionMap.put(newSessionId,
                            GptService.INSTANCE.login(newSessionId, "true".equals(sessionInfo[2])));
                    chatMemoryStore.updateMessages(CommonUtil.tail(newSessionId, 64),
                            history.getMessages(sessionId));
                }
            });
        }
    }

    public static void clear(Map<String, GptSession> sessionMap, ChatMemoryStore chatMemoryStore) {
        clear(sessionMap, chatMemoryStore, 1);
    }

    public static void clear(Map<String, GptSession> sessionMap, ChatMemoryStore chatMemoryStore, double rate) {
        sessionMap.keySet().forEach(sessionId -> {
            List<ChatMessage> messages = chatMemoryStore.getMessages(CommonUtil.tail(sessionId, 64));
            // 3 是初始化的3条系统消息
            if (messages.size() > 3) {
                int toIndex = (int) ((messages.size() - 3) * rate + 3);
                while(messages.size() > toIndex && !(messages.get(toIndex) instanceof UserMessage)) {
                    toIndex++;
                }
                messages.subList(3, toIndex).clear();
            }
        });
        CacheUtil.reset();
        FileUtil.delete(JSON_PATH);
    }

    private static void store(Map<String, GptSession> sessionMap,
                              ChatMemoryStore chatMemoryStore) {
        History history = new History();
        sessionIds.stream().filter(sessionMap::containsKey).forEach(sessionId -> {
            String sessionInfo = CommonUtil.decode(sessionId);
            String[] sessionFields = sessionInfo.split("&");
            if (sessionFields.length > 1 && !"null".equals(sessionFields[1])) {
                GptSession gptSession = sessionMap.get(sessionId);
                String key = sessionInfo + "&" + gptSession.getStrict();
                history.setMessages(key, chatMemoryStore.getMessages(gptSession.getShortName()));
            }
        });
        FileUtil.writeJson(JSON_PATH, JSON.toJSONString(history));
    }

    @NotNull
    private static String output(ChatMemoryStore chatMemoryStore) {
        StringBuilder sb = new StringBuilder();
        for (String sessionId : sessionIds) {
            String msgId = CommonUtil.tail(sessionId, 64);
            if (chatMemoryStore.getMessages(msgId) == null) {
                continue;
            }
            List<String> lines = chatMemoryStore.getMessages(msgId).stream()
                    .map(ChatMessage::toString).collect(Collectors.toList());
            String sessionInfo = CommonUtil.decode(sessionId);
            int start = Math.max(sessionInfo.indexOf("&"), 0);
            int end = Math.min(sessionInfo.length() - start, 10) + start;
            sb.append(sessionInfo, start, end).append(":\n").append(String.join("\n", lines));
        }
        return sb.toString();
    }

    private static String getChatType(ChatMessage chatMessage) {
        if (chatMessage instanceof SystemMessage) {
            return "system";
        }
        if (chatMessage instanceof AiMessage && !((AiMessage) chatMessage).hasToolExecutionRequests()) {
            return "ai";
        }
        if (chatMessage instanceof UserMessage) {
            return "user";
        }
        return null;
    }

    @NoArgsConstructor
    @Data
    static class CustomMessage {
        String type;
        String text;

        public CustomMessage(ChatMessage chatMessage) {
            this.type = getChatType(chatMessage);
            this.text = chatMessage.text();
        }

        public ChatMessage toChatMessage() {
            switch (this.type) {
                case "system":
                    return SystemMessage.from(this.text);
                case "ai":
                    return AiMessage.from(this.text);
                case "user":
                    return UserMessage.from(this.text);
                default:
                    return null;
            }
        }
    }

    @Data
    static class History {
        Map<String, List<CustomMessage>> histories;

        History() {
            this.histories = new HashMap<>();
        }

        void setMessages(String key, List<ChatMessage> messages) {
            List<CustomMessage> customMessages = messages.stream()
                    .filter(it -> getChatType(it) != null)
                    .map(CustomMessage::new).collect(Collectors.toList());
            this.histories.put(key, customMessages);
        }

        List<ChatMessage> getMessages(String name) {
            return this.histories.get(name).stream().map(CustomMessage::toChatMessage).collect(Collectors.toList());
        }
    }
}
