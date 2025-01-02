package com.mafiadev.ichat.util;

import com.mafiadev.ichat.llm.GptService;
import com.mafiadev.ichat.model.GptSession;
import com.mafiadev.ichat.service.SessionService;
import io.javalin.http.Context;

import java.util.Optional;
import java.util.UUID;

public class SessionUtil {
    public static GptSession refresh(Context ctx) {
        GptSession session = ctx.sessionAttribute("session");
        if (session != null) {
            return session;
        }
        SessionService sessionService = new SessionService();
        GptService gptService = GptService.getInstance();
        String sessionId = Optional.ofNullable(ctx.cookie("session-id")).orElse("");
        if (sessionId.isEmpty() || (session = sessionService.getSession(sessionId)) == null) {
            sessionId = UUID.randomUUID().toString();
            session = gptService.login(sessionId, false, false);
        }
        ctx.sessionAttribute("session", session);
        return session;
    }
}
