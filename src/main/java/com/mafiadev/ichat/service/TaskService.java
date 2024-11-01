package com.mafiadev.ichat.service;

import com.mafiadev.ichat.model.struct.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskService {
    private static final Map<String, List<Task>> taskHashMap = new ConcurrentHashMap<>();

    public Map<String, List<Task>> loadTasks() {
        return taskHashMap;
    }

    public void updateTasks(String sessionId, List<Task> tasks) {
        taskHashMap.put(sessionId, tasks);
    }

    public void saveTask(String sessionId, Task task) {
        List<Task> userTasks = taskHashMap.getOrDefault(sessionId, new ArrayList<>());
        userTasks.add(task);
        taskHashMap.put(sessionId, userTasks);
    }
}
