package com.mafiadev.ichat.admin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mafiadev.ichat.Claptrap;
import com.mafiadev.ichat.llm.GptService;
import com.mafiadev.ichat.util.FileUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
    private static Set<String> users = new HashSet<>();
    private final String password;

    private AdminService(Claptrap plugin) {
        this.password = plugin.getConfig().getString("adminPwd");
    }

    public String handler(String sessionId, String msg) {
        users.add(sessionId);
        if (msg.startsWith("\\admin")) {
            msg = msg.replace("\\admin", "").trim();
            if (msg.startsWith("login " + password)) {
                admins.add(sessionId);
                FileUtil.mkFile(JSON_PATH);
                return "login success";
            }
            if (admins.contains(sessionId)) {
                ChatMemoryStore chatMemoryStore = GptService.INSTANCE.getChatMemoryStore();
                if (msg.startsWith("load")) {
                    String json = FileUtil.readJson(JSON_PATH);
                    History history = JSONObject.parseObject(json, History.class);
                    if (history != null) {
                        users = history.getHistories().keySet();
                        users.forEach(user -> chatMemoryStore.updateMessages(user, history.getMessages(user)));
                    }
                    return "load success";
                }
                if (msg.startsWith("store")) {
                    History history = new History();
                    users.forEach(user -> history.setMessages(user, chatMemoryStore.getMessages(user)));
                    FileUtil.writeJson(JSON_PATH, JSON.toJSONString(history));
                    return "store success";
                }
                if (msg.startsWith("output")) {
                    StringBuilder sb = new StringBuilder();
                    for (String user : users) {
                        if (chatMemoryStore.getMessages(user) == null) {
                            continue;
                        }
                        List<String> lines = chatMemoryStore.getMessages(user).stream()
                                .map(ChatMessage::toString).collect(Collectors.toList());
                        sb.append(user, 0, 10).append(":\n").append(String.join("\n", lines));
                    }
                    return sb.toString();
                }
            }
        }
        return null;
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

        void setMessages(String name, List<ChatMessage> messages) {
            List<CustomMessage> customMessages = messages.stream()
                    .filter(it -> getChatType(it) != null)
                    .map(CustomMessage::new).collect(Collectors.toList());
            this.histories.put(name, customMessages);
        }

        List<ChatMessage> getMessages(String name) {
            return this.histories.get(name).stream().map(CustomMessage::toChatMessage).collect(Collectors.toList());
        }
    }

    public static void main(String[] args) {
        List<ChatMessage> messages = Arrays.asList(
                UserMessage.from("start"),
                AiMessage.from("hello")
        );
        History history = new History();
        history.setMessages("test", messages);
        String json = JSON.toJSONString(history);
        System.out.println(json);
        History history1 = JSONObject.parseObject(json, History.class);
        System.out.println(history1);
    }
}
