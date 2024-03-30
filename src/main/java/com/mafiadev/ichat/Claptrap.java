package com.mafiadev.ichat;

import com.mafiadev.ichat.constant.Constant;
import com.mafiadev.ichat.llm.GptListener;
import com.mafiadev.ichat.util.FileUtil;
import com.meteor.wechatbc.impl.plugin.BasePlugin;

public class Claptrap extends BasePlugin {
    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        FileUtil.mkDir(Constant.FILE_PATH);
        this.saveDefaultConfig();
        new GptListener(this).register();
    }
}
