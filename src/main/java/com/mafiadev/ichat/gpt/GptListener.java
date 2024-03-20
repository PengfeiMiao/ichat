package com.mafiadev.ichat.gpt;

import com.mafiadev.ichat.Claptrap;
import com.meteor.wechatbc.entitiy.message.Message;
import com.meteor.wechatbc.event.EventHandler;
import com.meteor.wechatbc.impl.event.Listener;
import com.meteor.wechatbc.impl.event.sub.ReceiveMessageEvent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class GptListener implements Listener {

    private final Claptrap plugin;

    private static final Map<String, Boolean> sessionHashMap = new ConcurrentHashMap<>();

    public GptListener(Claptrap plugin) {
        this.plugin = plugin;
    }

    /**
     * 注册监听器
     */
    public void register() {
        plugin.getWeChatClient().getEventManager().registerPluginListener(plugin, this);
    }

    public enum AnswerType {
        TEXT, IMAGE
    }

    @AllArgsConstructor
    @Data
    public static class Request {
        private AnswerType answerType;
        private String prompt;
    }

    public boolean initSession(Message message, String msg) {
        String fromUserName = message.getFromUserName();
        if (msg.startsWith("\\gpt")) {
            msg = msg.replace("\\gpt", "").trim();
            if (msg.startsWith("start")) {
                plugin.getWeChatClient().getWeChatCore().getHttpAPI().sendMessage(fromUserName, "gpt bot");
                sessionHashMap.put(fromUserName, true);
            }
            if (msg.startsWith("end")) {
                sessionHashMap.put(fromUserName, false);
            }
        }
        return sessionHashMap.getOrDefault(fromUserName, false);
    }

    public Request getRequest(String msg) {
        if (msg.startsWith("#image")) {
            return new Request(AnswerType.IMAGE, msg.replaceFirst("#image", "").trim());
        }
        return new Request(AnswerType.TEXT, msg);
    }

    @EventHandler
    public void onReceiveMessage(ReceiveMessageEvent receiveMessageEvent) {
        Message message = receiveMessageEvent.getMessage();
        String content = receiveMessageEvent.getContent();

        String senderUserName = message.getSenderUserName();
        if (senderUserName == null || !initSession(message, content)) {
            return;
        }

        Optional<Request> optionalRequest = Optional.ofNullable(getRequest(content));

        optionalRequest.ifPresent(request -> {
            String prompt = request.getPrompt();
            String result = request.getAnswerType() == AnswerType.TEXT
                    ? GptService.INSTANCE.textDialog(senderUserName, prompt)
                    : GptService.INSTANCE.imageDialog(senderUserName, prompt);
            plugin.getWeChatClient().getWeChatCore().getHttpAPI().sendMessage(message.getFromUserName(), result);
        });
    }

}
