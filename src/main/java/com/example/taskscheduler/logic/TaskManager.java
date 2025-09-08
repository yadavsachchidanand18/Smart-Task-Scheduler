package com.example.taskscheduler.logic;

import com.example.taskscheduler.model.Task;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TaskManager {
    private final PriorityQueue<Task> queue;
    private final Map<String, Task> byId;

    public TaskManager() {
        this.queue = new PriorityQueue<>(Task::urgencyCompareTo);
        this.byId = new HashMap<>();
    }

    public synchronized void setAll(Collection<Task> tasks) {
        queue.clear();
        byId.clear();
        for (Task t : tasks) {
            add(t);
        }
    }

    public synchronized void add(Task task) {
        queue.add(task);
        byId.put(task.getId(), task);
    }

    public synchronized void update(Task updated) {
        Task existing = byId.get(updated.getId());
        if (existing != null) {
            queue.remove(existing);
            queue.add(updated);
            byId.put(updated.getId(), updated);
        }
    }

    public synchronized void delete(String id) {
        Task existing = byId.remove(id);
        if (existing != null) {
            queue.remove(existing);
        }
    }

    public synchronized List<Task> getAll() {
        return new ArrayList<>(queue).stream()
                .sorted(Task::urgencyCompareTo)
                .collect(Collectors.toList());
    }

    public synchronized Task getById(String id) {
        return byId.get(id);
    }

    public synchronized List<Task> filter(Predicate<Task> predicate) {
        return getAll().stream().filter(predicate).collect(Collectors.toList());
    }
}
