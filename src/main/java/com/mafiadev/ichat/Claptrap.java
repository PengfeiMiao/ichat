package com.mafiadev.ichat;

import com.mafiadev.ichat.gpt.GptService;
import com.meteor.wechatbc.impl.plugin.BasePlugin;

public class Claptrap extends BasePlugin {
    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        GptService.init(this);
    }
}
