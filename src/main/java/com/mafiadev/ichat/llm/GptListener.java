package com.mafiadev.ichat.llm;

import com.mafiadev.ichat.Claptrap;
import com.meteor.wechatbc.entitiy.message.Message;
import com.meteor.wechatbc.event.EventHandler;
import com.meteor.wechatbc.impl.HttpAPI;
import com.meteor.wechatbc.impl.event.Listener;
import com.meteor.wechatbc.impl.event.sub.ReceiveMessageEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GptListener implements Listener {

    private final Claptrap plugin;

    private final HttpAPI sender;

    private static final int MAX_LENGTH = 600;

    public GptListener(Claptrap plugin) {
        this.plugin = plugin;
        this.sender = plugin.getWeChatClient().getWeChatCore().getHttpAPI();
    }

    /**
     * 注册监听器
     */
    public void register() {
        GptService.init(plugin);
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

    public Request getRequest(String msg) {
        if (msg.startsWith("#image")) {
            return new Request(AnswerType.IMAGE, msg.replaceFirst("#image", "").trim());
        }
        return new Request(AnswerType.TEXT, msg);
    }

    @NotNull
    public List<String> handleLongMessage(String text) {
        String[] texts = text.split("\n");
        List<String> textArr = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (String it : texts) {
            if (sb.length() + it.length() >= MAX_LENGTH) {
                textArr.add(sb.toString());
                sb.setLength(0);
            }
            sb.append(it);
        }
        if (sb.length() > 0) {
            textArr.add(sb.toString());
        }
        return textArr;
    }

    @EventHandler
    public void onReceiveMessage(ReceiveMessageEvent receiveMessageEvent) {
        Message message = receiveMessageEvent.getMessage();
        String content = receiveMessageEvent.getContent();

        String senderUserName;
        try {
            senderUserName = message.getSenderUserName() == null ?
                    message.getSenderUserName() : message.getFromUserName();
        } catch (Exception e) {
            return;
        }

        GptSession gptSession = GptService.INSTANCE.initSession(senderUserName, content);

        if (senderUserName == null || gptSession == null) {
            return;
        }
        if (gptSession.getTips() != null) {
            sender.sendMessage(senderUserName, gptSession.getTips());
        }
        if (!gptSession.getLogin()) {
            return;
        }

        Optional<Request> optionalRequest = Optional.ofNullable(getRequest(content));
        optionalRequest.ifPresent(request -> {
            String prompt = request.getPrompt();
            if (request.getAnswerType() == AnswerType.TEXT) {
                String text = GptService.INSTANCE.textDialog(gptSession, prompt);
                if (text.length() < MAX_LENGTH) {
                    sender.sendMessage(senderUserName, text);
                } else {
                    List<String> textArr = handleLongMessage(text);
                    textArr.forEach(it -> sender.sendMessage(senderUserName, it));
                }
            } else {
                File image = GptService.INSTANCE.imageDialog(gptSession, prompt);
                sender.sendImage(senderUserName, image);
            }
        });
    }
}
