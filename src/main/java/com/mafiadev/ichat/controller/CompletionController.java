package com.mafiadev.ichat.controller;

import com.mafiadev.ichat.llm.GptService;
import com.mafiadev.ichat.model.GptSession;
import com.mafiadev.ichat.service.SessionService;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class CompletionController {

    private static final GptService gptService = GptService.getInstance();

    private static final SessionService sessionService = new SessionService();

    public static void completions(@NotNull Context ctx) {
        String message = ctx.bodyValidator(TextMessage.class)
                .check(obj -> obj.getMessage() != null, "message must not be null")
                .get().getMessage();
        String sessionId = Optional.ofNullable(ctx.cookie("session-id")).orElse("");

        GptSession session;
        if (sessionId.isEmpty() || (session = sessionService.getSession(sessionId)) == null) {
            sessionId = UUID.randomUUID().toString();
            session = gptService.login(sessionId, false, false);
        }

        String result = gptService.textDialog(session, message, false);
        ctx.cookie("session-id", sessionId);
        ctx.json(TextMessage.builder().message(result).build());
    }
}
