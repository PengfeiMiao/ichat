package com.mafiadev.ichat.llm;

import com.mafiadev.ichat.Claptrap;
import com.mafiadev.ichat.llm.admin.AdminService;
import com.mafiadev.ichat.model.GptSession;
import com.mafiadev.ichat.util.CommonUtil;
import com.meteor.wechatbc.entitiy.contact.Contact;
import com.meteor.wechatbc.entitiy.message.Message;
import com.meteor.wechatbc.event.EventHandler;
import com.meteor.wechatbc.impl.HttpAPI;
import com.meteor.wechatbc.impl.contact.ContactManager;
import com.meteor.wechatbc.impl.event.Listener;
import com.meteor.wechatbc.impl.event.sub.ReceiveMessageEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GptListener implements Listener {

    private final Claptrap plugin;
    private final HttpAPI sender;
    private final String ownerName;
    private final ContactManager contactManager;

    private static final int MAX_LENGTH = 600;

    public GptListener(Claptrap plugin) {
        this.plugin = plugin;
        this.sender = plugin.getWeChatClient().getWeChatCore().getHttpAPI();
        this.ownerName = plugin.getWeChatClient().getWeChatCore().getSession().getWxInitInfo().getUser().getNickName();
        this.contactManager = plugin.getWeChatClient().getContactManager();
    }

    /**
     * 注册监听器
     */
    public void register() {
        GptService.init(plugin);
        AdminService.init(plugin);
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
        if (session.getStrict()) {
            String ownerLoc = "@" + ownerName;
            if (!msg.contains(ownerLoc) && !msg.startsWith("\\gpt ")) {
                return null;
            } else {
                msg = msg.replaceFirst(ownerLoc, "");
            }
        }
        if (msg.startsWith("#image") || CommonUtil.isSimilar(msg, "画个", 0.33)) {
            return new Request(AnswerType.IMAGE, msg.replaceFirst("#image", "").trim());
        }
        return new Request(AnswerType.TEXT, msg);
    }

    @NotNull
    public List<String> handleLongMessage(String text) {
        if (text.length() < MAX_LENGTH) {
            return Collections.singletonList(text);
        }
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
        if (sb.length() > 0) {
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
            String nickName = Optional.ofNullable(contactManager.getContact(senderUserName))
                    .map(Contact::getNickName).orElse("null");
            sessionId = CommonUtil.encode(senderUserName + "&" + nickName);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String adminMsg = AdminService.INSTANCE.handler(sessionId, content);
        if (adminMsg != null) {
            sendLongMessage(senderUserName, adminMsg);
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
                sendLongMessage(senderUserName, text);
            } else {
                File image = GptService.INSTANCE.imageDialog(gptSession, prompt);
                sender.sendImage(senderUserName, image);
            }
        });
    }

    private void sendLongMessage(String senderUserName, String text) {
        handleLongMessage(text).forEach(it -> {
            sender.sendMessage(senderUserName, it);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
