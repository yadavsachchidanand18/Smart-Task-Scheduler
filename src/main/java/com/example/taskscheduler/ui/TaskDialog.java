package com.example.taskscheduler.ui;

import com.example.taskscheduler.model.Priority;
import com.example.taskscheduler.model.Task;
import com.example.taskscheduler.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class TaskDialog extends JDialog {
    private final JTextField titleField = new JTextField();
    private final JTextArea descArea = new JTextArea(5, 20);
    private final JComboBox<Priority> priorityCombo = new JComboBox<>(Priority.values());
    private final JTextField deadlineField = new JTextField(); // "yyyy-MM-dd HH:mm" or blank
    private final JCheckBox completedCheck = new JCheckBox("Completed");
    private final JTextField reminderField = new JTextField(); // minutes before, blank if none

    private Task working;

    public TaskDialog(Frame owner, String title, Task existing) {
        super(owner, title, true);
        setSize(500, 380);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0;

        form.add(new JLabel("Title:"), gc); gc.gridx = 1; gc.weightx = 1;
        form.add(titleField, gc);

        gc.gridx = 0; gc.gridy++;
        gc.weightx = 0;
        form.add(new JLabel("Description:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        JScrollPane sp = new JScrollPane(descArea);
        form.add(sp, gc);

        gc.gridx = 0; gc.gridy++; gc.weightx = 0;
        form.add(new JLabel("Priority:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(priorityCombo, gc);

        gc.gridx = 0; gc.gridy++;
        gc.weightx = 0;
        form.add(new JLabel("Deadline (yyyy-MM-dd HH:mm):"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(deadlineField, gc);

        gc.gridx = 0; gc.gridy++;
        form.add(new JLabel("Reminder minutes before (optional):"), gc);
        gc.gridx = 1;
        form.add(reminderField, gc);

        gc.gridx = 0; gc.gridy++;
        form.add(new JLabel("Status:"), gc);
        gc.gridx = 1;
        form.add(completedCheck, gc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        buttons.add(ok);
        buttons.add(cancel);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        ok.addActionListener(e -> onOK());
        cancel.addActionListener(e -> onCancel());

        if (existing != null) {
            working = cloneTask(existing);
            populate(working);
        } else {
            working = new Task();
        }
    }

    private Task cloneTask(Task t) {
        Task c = new Task();
        c.setId(t.getId());
        c.setTitle(t.getTitle());
        c.setDescription(t.getDescription());
        c.setPriority(t.getPriority());
        c.setDeadline(t.getDeadline());
        c.setCreatedAt(t.getCreatedAt());
        c.setCompleted(t.isCompleted());
        c.setReminderMinutesBefore(t.getReminderMinutesBefore());
        return c;
    }

    private void populate(Task t) {
        titleField.setText(t.getTitle());
        descArea.setText(t.getDescription());
        priorityCombo.setSelectedItem(t.getPriority());
        deadlineField.setText(t.getDeadline() == null ? "" : DateUtil.formatDateTime(t.getDeadline()));
        completedCheck.setSelected(t.isCompleted());
        reminderField.setText(t.getReminderMinutesBefore() == null ? "" : String.valueOf(t.getReminderMinutesBefore()));
    }

    private void onOK() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title is required.");
            return;
        }
        working.setTitle(title);
        working.setDescription(descArea.getText());
        working.setPriority((Priority) priorityCombo.getSelectedItem());

        String deadlineText = deadlineField.getText().trim();
        if (!deadlineText.isEmpty()) {
            LocalDateTime dt = DateUtil.parseDateTime(deadlineText);
            if (dt == null) {
                JOptionPane.showMessageDialog(this, "Invalid deadline format. Use yyyy-MM-dd HH:mm");
                return;
            }
            working.setDeadline(dt);
        } else {
            working.setDeadline(null);
        }

        String remText = reminderField.getText().trim();
        if (!remText.isEmpty()) {
            try {
                int minutes = Integer.parseInt(remText);
                if (minutes < 0) throw new NumberFormatException();
                working.setReminderMinutesBefore(minutes);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Reminder minutes must be a non-negative integer.");
                return;
            }
        } else {
            working.setReminderMinutesBefore(null);
        }

        working.setCompleted(completedCheck.isSelected());

        dispose();
    }

    private void onCancel() {
        working = null;
        dispose();
    }

    public Task showDialog() {
        setVisible(true);
        return working;
    }
}
