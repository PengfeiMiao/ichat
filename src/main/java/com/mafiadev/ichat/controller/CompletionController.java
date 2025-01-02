package com.mafiadev.ichat.controller;

import com.mafiadev.ichat.llm.GptService;
import com.mafiadev.ichat.model.GptSession;
import com.mafiadev.ichat.util.SessionUtil;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

public class CompletionController {

    private static final GptService gptService = GptService.getInstance();

    public static void completions(@NotNull Context ctx) {
        String message = ctx.bodyValidator(TextMessage.class)
                .check(obj -> obj.getMessage() != null && !obj.getMessage().isEmpty(),
                        "message must not be empty")
                .get().getMessage();

        GptSession session = SessionUtil.refresh(ctx);

        String result = gptService.textDialog(session, message, false);
        ctx.json(TextMessage.builder().message(result).build());
    }
}
