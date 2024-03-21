package com.mafiadev.ichat.gpt;

import com.mafiadev.ichat.Claptrap;
import com.meteor.wechatbc.entitiy.message.Message;
import com.meteor.wechatbc.event.EventHandler;
import com.meteor.wechatbc.impl.HttpAPI;
import com.meteor.wechatbc.impl.event.Listener;
import com.meteor.wechatbc.impl.event.sub.ReceiveMessageEvent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.util.Optional;

public class GptListener implements Listener {

    private final Claptrap plugin;

    private final HttpAPI sender;

    public GptListener(Claptrap plugin) {
        this.plugin = plugin;
        this.sender = plugin.getWeChatClient().getWeChatCore().getHttpAPI();
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

        String senderUserName = message.getSenderUserName() == null ?
                message.getSenderUserName() : message.getFromUserName();

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
                sender.sendMessage(senderUserName, text);
            } else {
                File image = GptService.INSTANCE.imageDialog(gptSession, prompt);
                sender.sendImage(senderUserName, image);
            }
        });
    }

}
