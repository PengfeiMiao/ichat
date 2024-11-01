package com.mafiadev.ichat.llm.agent;

import com.mafiadev.ichat.model.struct.Task;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.UserName;
import dev.langchain4j.service.V;

public interface TaskHost {
    @UserMessage("根据 `{{message}}` 生成任务的 cron 表达式: " +
            "如果不强调每天执行，则默认生成不带 * 的 cron 表达式, e.g. 59 59 23 1 12 ? 2024, " +
            "如果要求每天执行，则生成带 * 的 cron 表达式, e.g. 59 59 23 * * ? * " +
            "(格式: ss mm HH dd MM week yyyy); " +
            "生成任务创建成功的提示语(需要包含任务下次触发时间); 任务触发成功的提示语(不要用将来时态); " +
            "最终生成 Task")
    Task schedule(@UserName String userName, @V("message") String message);
}
