package com.example.taskscheduler.logic;

import com.example.taskscheduler.model.Task;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class ReminderService {
    private final Timer timer = new Timer("TaskReminders", true);
    private final Map<String, TimerTask> scheduled = new HashMap<>();

    public synchronized void scheduleFor(Task task) {
        cancel(task.getId());
        if (task.getDeadline() == null) return;
        if (task.isCompleted()) return;

        Integer lead = task.getReminderMinutesBefore();
        if (lead == null) return;

        LocalDateTime trigger = task.getDeadline().minusMinutes(lead);
        long delayMs = Duration.between(LocalDateTime.now(), trigger).toMillis();
        if (delayMs <= 0) {
            // If time already passed but not done, show immediately
            showReminder(task);
            return;
        }

        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                showReminder(task);
                synchronized (ReminderService.this) {
                    scheduled.remove(task.getId());
                }
            }
        };
        scheduled.put(task.getId(), tt);
        timer.schedule(tt, delayMs);
    }

    private void showReminder(Task task) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                null,
                "Reminder: " + task.getTitle() +
                        (task.getDeadline() != null ? "\nDue: " + task.getDeadline() : ""),
                "Task Reminder",
                JOptionPane.INFORMATION_MESSAGE
        ));
    }

    public synchronized void cancel(String taskId) {
        TimerTask tt = scheduled.remove(taskId);
        if (tt != null) tt.cancel();
    }

    public synchronized void rescheduleAll(Collection<Task> tasks) {
        for (TimerTask tt : scheduled.values()) {
            tt.cancel();
        }
        scheduled.clear();
        for (Task t : tasks) {
            scheduleFor(t);
        }
    }

    public void shutdown() {
        timer.cancel();
    }
}
