package com.mafiadev.ichat.llm.admin;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.mafiadev.ichat.Claptrap;
import com.mafiadev.ichat.model.GptSession;
import com.mafiadev.ichat.service.MessageService;
import com.mafiadev.ichat.service.SessionService;
import com.mafiadev.ichat.util.CacheUtil;
import com.mafiadev.ichat.util.CommonUtil;
import com.mafiadev.ichat.util.ConfigUtil;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminService {
    public static AdminService INSTANCE;

    public static void init(Claptrap plugin) {
        INSTANCE = new AdminService(plugin);
    }

    private static final List<String> admins = new ArrayList<>();
    private static final Set<String> sessionIds = new ConcurrentHashSet<>();
    private final String password;
    private final SessionService sessionService;
    private final MessageService messageService;

    public AdminService(Claptrap plugin) {
        this.password = plugin.getConfig().getString("adminPwd");
        this.sessionService = new SessionService();
        this.messageService = new MessageService();
    }

    public AdminService() {
        this.password = ConfigUtil.getConfig("adminPwd");
        this.sessionService = new SessionService();
        this.messageService = new MessageService();
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
                Map<String, GptSession> sessionHashMap = sessionService.getSessions();
                ChatMemoryStore chatMemoryStore = messageService.getChatMemoryStore();
                if (msg.startsWith("load")) {
                    sessionService.loadSessions();
                    messageService.loadMessages();
                    return "load success";
                }
                if (msg.startsWith("store")) {
                    messageService.saveMessages();
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

    public static void clear(Map<String, GptSession> sessionMap, ChatMemoryStore chatMemoryStore) {
        clear(sessionMap, chatMemoryStore, 1);
    }

    public static void clear(Map<String, GptSession> sessionMap, ChatMemoryStore chatMemoryStore, double rate) {
        sessionMap.keySet().forEach(sessionId -> {
            List<ChatMessage> messages = chatMemoryStore.getMessages(CommonUtil.digest(sessionId));
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
//        FileUtil.delete(JSON_PATH);
    }

    @NotNull
    private static String output(ChatMemoryStore chatMemoryStore) {
        StringBuilder sb = new StringBuilder();
        for (String sessionId : sessionIds) {
            String msgId = CommonUtil.digest(sessionId);
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
}
