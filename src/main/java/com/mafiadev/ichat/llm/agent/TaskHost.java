package com.mafiadev.ichat.llm.agent;

import com.mafiadev.ichat.model.struct.Task;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.UserName;
import dev.langchain4j.service.V;

import java.util.List;

public interface TaskHost {
    @UserMessage("根据 `{{message}}` 完成以下任务: " +
            "=> 生成 cronExpr (任务的 cron 表达式, 格式: 秒 分 时 日 月 星期 年, 注意必须以秒开始, 分清时和分位置), " +
            "生成规则如下: " +
            "IF 只包含当前时间, 无法确定任务创建或取消的时间, THEN cronExpr 赋值为 null, " +
            "IF 不要求每天执行, 默认情况 -> ONCE - 一次性任务, THEN 默认生成不带 * 的 cron 表达式, e.g. 0 59 23 1 12 ? 2024, " +
            "IF 强调每一段时间执行 -> RECURRING - 周期性任务, THEN 生成带 * 的 cron 表达式, e.g. 0 59 23 * * ? *, " +
            "=> 生成 type (任务类型, 可选值: ONCE, RECURRING, CANCEL - 只有明确取消、删除等命令时才是 CANCEL); " +
            "=> 生成 content (任务内容, 任务目的); " +
            "=> 生成 createdTips (任务创建结果的提示语), " +
            "生成规则如下: " +
            "IF cronExpr == null THEN 给出失败提示, " +
            "IF type == CANCEL THEN 给出任务取消提示 AND 不要用将来时态; " +
            "IF cronExpr != null AND type != CANCEL THEN 给出创建成功提示并包含任务触发时间, " +
            "=> 生成 triggerTips (任务触发成功的提示语, 结合 content 给出口语化提示, 注意不要用将来时态); " +
            "=> 最终生成 Task 对象")
    Task schedule(@UserName String userName, @V("message") String message);

    @UserMessage("根据 `{{tasks}}` 中每个 Task 的 cronExpr 和 content, " +
            "格式：`yyyy-MM-dd HH:mm:ss - content 的口语化表达`, content 部分不要包含时间信息, " +
            "最后按编号返回所有任务的精简信息")
    String list(@UserName String userName, @V("tasks")List<Task> tasks);
}
