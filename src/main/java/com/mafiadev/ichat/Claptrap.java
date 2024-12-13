package com.mafiadev.ichat;

import com.mafiadev.ichat.constant.Constant;
import com.mafiadev.ichat.controller.CompletionController;
import com.mafiadev.ichat.llm.GptListener;
import com.mafiadev.ichat.task.ScheduledTask;
import com.mafiadev.ichat.task.TaskTrigger;
import com.mafiadev.ichat.util.ConfigUtil;
import com.mafiadev.ichat.util.FileUtil;
import com.meteor.wechatbc.impl.plugin.BasePlugin;
import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.config.JavalinConfig;
import io.javalin.http.UnauthorizedResponse;

import java.util.function.Consumer;

@SuppressWarnings("ALL")
public class Claptrap extends BasePlugin {
    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        FileUtil.mkDir(Constant.FILE_PATH);
        new TaskTrigger();
//        this.saveDefaultConfig();
        ConfigUtil.loadCustomConfig(this.getPluginDescription().getName());
        new GptListener(this).register();
        new ScheduledTask(this);
        this.startServer();
    }

    private void startServer() {
        Consumer<JavalinConfig> javalinConfigConsumer = config -> {
            config.jetty.modifyServer(server -> server.setStopTimeout(5_000));
            config.router.apiBuilder(() -> {
                ApiBuilder.path("/mafia-ai", () -> {
                    ApiBuilder.path("/completions", () -> {
                        ApiBuilder.post(CompletionController::completions);
                    });
                });
            });
        };

        String configKey = ConfigUtil.getConfig("api.key");
        int port = ConfigUtil.getConfig("api.port", Integer.class);

        Javalin app = Javalin.create(javalinConfigConsumer)
                .get("/", ctx -> ctx.result("Hello MafiaAI"))
                .start("127.0.0.1", port);
        app.before(ctx -> {
            String apiKey = ctx.header("api-key");
            if (apiKey == null || !apiKey.equals(configKey)) {
                throw new UnauthorizedResponse();
            }
        });
    }
}
