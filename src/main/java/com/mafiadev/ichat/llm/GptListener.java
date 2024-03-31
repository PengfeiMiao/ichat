package com.mafiadev.ichat.llm;

import com.mafiadev.ichat.Claptrap;
import com.mafiadev.ichat.util.CommonUtil;
import com.meteor.wechatbc.entitiy.message.Message;
import com.meteor.wechatbc.event.EventHandler;
import com.meteor.wechatbc.impl.HttpAPI;
import com.meteor.wechatbc.impl.event.Listener;
import com.meteor.wechatbc.impl.event.sub.ReceiveMessageEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class GptListener implements Listener {

    private final Claptrap plugin;
    private final HttpAPI sender;
    private final String ownerName;

    private static final int MAX_LENGTH = 600;

    public GptListener(Claptrap plugin) {
        this.plugin = plugin;
        this.sender = plugin.getWeChatClient().getWeChatCore().getHttpAPI();
        this.ownerName = plugin.getWeChatClient().getWeChatCore().getSession().getWxInitInfo().getUser().getNickName();
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

    public Request getRequest(GptSession session, String msg) {
        if (session.strict) {
            String ownerLoc = "@" + ownerName;
            if (!msg.contains(ownerLoc) && !msg.startsWith("\\gpt ")) return null;
            else msg = msg.replaceFirst(ownerLoc, "");
        }
        if (msg.startsWith("#image") || CommonUtil.isSimilar(msg, "画个", 0.5)) {
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
                textArr.add(removeLast(sb));
                sb.setLength(0);
            }
            sb.append(it).append("\n");
        }
        if (sb.length() > 0) {
            textArr.add(removeLast(sb));
        }
        return textArr;
    }

    private String removeLast(StringBuilder sb) {
        if(sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    @EventHandler
    public void onReceiveMessage(ReceiveMessageEvent receiveMessageEvent) {
        Message message = receiveMessageEvent.getMessage();
        String content = receiveMessageEvent.getContent();

        String senderUserName;
        String sessionId;
        try {
            senderUserName = message.getFromUserName() != null ?
                    message.getFromUserName() : message.getSenderUserName();
            sessionId = Base64.getEncoder().encodeToString(
                    (message.getFromUserName() + "&" + message.getSenderUserName()).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return;
        }
        GptSession gptSession = GptService.INSTANCE.initSession(sessionId, content);

        if (senderUserName == null || gptSession == null) {
            return;
        }
        if (gptSession.getTips() != null) {
            sender.sendMessage(senderUserName, gptSession.getTips());
        }
        if (!gptSession.getLogin()) {
            return;
        }

        Optional<Request> optionalRequest = Optional.ofNullable(getRequest(gptSession, content));
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
