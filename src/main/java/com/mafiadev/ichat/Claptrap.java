package com.mafiadev.ichat;

import com.mafiadev.ichat.constant.Constant;
import com.mafiadev.ichat.llm.GptListener;
import com.mafiadev.ichat.task.ScheduledTask;
import com.mafiadev.ichat.task.TaskTrigger;
import com.mafiadev.ichat.util.ConfigUtil;
import com.mafiadev.ichat.util.FileUtil;
import com.meteor.wechatbc.impl.plugin.BasePlugin;

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
    }
}
