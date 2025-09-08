package com.example.taskscheduler.data;

import com.example.taskscheduler.model.Priority;
import com.example.taskscheduler.model.Task;
import com.example.taskscheduler.util.JsonUtil;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskStorage {
    private final Path filePath;

    public TaskStorage(Path filePath) {
        this.filePath = filePath;
    }

    public List<Task> load() {
        try {
            if (!Files.exists(filePath)) return new ArrayList<>();
            String json = Files.readString(filePath);
            return JsonUtil.parseTasks(json);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void save(List<Task> tasks) {
        try {
            if (!Files.exists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
            }
            String json = JsonUtil.toJson(tasks);
            Files.writeString(filePath, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
