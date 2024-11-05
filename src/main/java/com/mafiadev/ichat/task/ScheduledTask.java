package com.mafiadev.ichat.task;

import com.alibaba.fastjson.JSON;
import com.mafiadev.ichat.Claptrap;
import com.mafiadev.ichat.model.struct.Task;
import com.mafiadev.ichat.service.TaskService;
import com.mafiadev.ichat.util.CommonUtil;
import com.meteor.wechatbc.impl.HttpAPI;
import com.meteor.wechatbc.impl.contact.ContactManager;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.mafiadev.ichat.task.TaskTrigger.TASK_EXEC;

public class ScheduledTask {
    private final TaskService taskService = new TaskService();

    public ScheduledTask(Claptrap plugin) {
        HttpAPI sender = plugin.getWeChatClient().getWeChatCore().getHttpAPI();
        ContactManager contactManager = plugin.getWeChatClient().getContactManager();

        Runnable task = () -> {
            Map<String, List<Task>> taskMap = taskService.loadTasks();
            taskMap.keySet().forEach(sessionId -> {
                List<Task> userTasks = taskMap.get(sessionId);
                String[] groupUser = CommonUtil.decode(sessionId).split("&&");
                boolean inGroup = groupUser.length > 1 && !groupUser[0].equals(groupUser[1]);
                String[] fromUser = (inGroup ? groupUser[0] : groupUser[1]).split("&");
                String fromUserName =
                        Optional.ofNullable(contactManager.getContactByNickName(fromUser[1]).getUserName())
                                .orElse(fromUser[0]);
                Iterator<Task> iterator = userTasks.iterator();
                while (iterator.hasNext()) {
                    Task it = iterator.next();
                    int cronMatch = CommonUtil.isCronMatch(it.getCronExpr(), new Date());
                    System.out.println(cronMatch + " => " + JSON.toJSONString(it));
                    if (cronMatch > 0) {
                        sender.sendMessage(fromUserName, it.getTriggerTips());
                        if ("ONCE".equals(it.getType())) {
                            iterator.remove();
                        }
                    } else if (cronMatch < 0) {
                        iterator.remove();
                    }
                }
                taskService.updateTasks(sessionId, userTasks);
            });
        };
        TASK_EXEC.scheduleAtFixedRate(task, 1, 1, TimeUnit.MINUTES);
    }
}
