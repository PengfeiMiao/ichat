package com.mafiadev.ichat.model;

import com.mafiadev.ichat.util.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Request {
    private AnswerType answerType;
    private String prompt;

    public static Request build(GptSession session, String ownerName, String msg) {
        if (session.getStrict()) {
            String ownerLoc = "@" + ownerName;
            if (!msg.contains(ownerLoc) && !msg.startsWith("\\gpt ")) {
                return null;
            } else {
                msg = msg.replaceFirst(ownerLoc, "");
            }
        }
        if (msg.contains("#image") || CommonUtil.isSimilar(msg, "画个", 0.33)) {
            return new Request(AnswerType.IMAGE, msg.replaceFirst("#image", "").trim());
        }
        return new Request(AnswerType.TEXT, msg);
    }

    public enum AnswerType {
        TEXT, IMAGE
    }
}