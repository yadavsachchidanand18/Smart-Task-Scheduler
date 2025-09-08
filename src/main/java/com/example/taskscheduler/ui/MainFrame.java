package com.example.taskscheduler.ui;

import com.example.taskscheduler.data.TaskStorage;
import com.example.taskscheduler.logic.ReminderService;
import com.example.taskscheduler.logic.TaskManager;
import com.example.taskscheduler.model.Priority;
import com.example.taskscheduler.model.Task;
import com.example.taskscheduler.util.DateUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {

    private final TaskManager taskManager = new TaskManager();
    private final ReminderService reminderService = new ReminderService();
    private final TaskStorage storage = new TaskStorage(Path.of(System.getProperty("user.home"),
            ".smart-task-scheduler", "tasks.json"));

    private final TaskTableModel tableModel = new TaskTableModel();
    private final JTable table = new JTable(tableModel);

    private final JComboBox<String> filterCombo = new JComboBox<>(new String[]{
            "All", "Today", "High Priority", "Upcoming (7 days)", "Overdue", "Completed"
    });
    private final JComboBox<String> sortCombo = new JComboBox<>(new String[]{
            "Urgency (Queue)", "Deadline", "Priority"
    });
    private final JTextField searchField = new JTextField();

    public MainFrame() {
        super("Smart Task Scheduler");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);

        // Load data
        List<Task> loaded = storage.load();
        taskManager.setAll(loaded);
        reminderService.rescheduleAll(taskManager.getAll());

        setLayout(new BorderLayout());
        add(buildTopBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(false);
        table.setRowHeight(24);

        refreshTable();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout(8, 8));

        JPanel left = new JPanel(new GridLayout(1, 4, 8, 8));
        left.add(new JLabel("Filter:"));
        left.add(filterCombo);
        left.add(new JLabel("Sort:"));
        left.add(sortCombo);

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.add(new JLabel("Search:"), BorderLayout.WEST);
        right.add(searchField, BorderLayout.CENTER);

        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.CENTER);

        filterCombo.addActionListener(e -> refreshTable());
        sortCombo.addActionListener(e -> refreshTable());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { refreshTable(); }
            public void removeUpdate(DocumentEvent e) { refreshTable(); }
            public void insertUpdate(DocumentEvent e) { refreshTable(); }
        });

        return p;
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton add = new JButton("Add");
        JButton edit = new JButton("Edit");
        JButton del = new JButton("Delete");
        JButton complete = new JButton("Mark Complete");
        JButton refresh = new JButton("Refresh");

        add.addActionListener(e -> onAdd());
        edit.addActionListener(e -> onEdit());
        del.addActionListener(e -> onDelete());
        complete.addActionListener(e -> onComplete());
        refresh.addActionListener(e -> refreshTable());

        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(add);
        p.add(edit);
        p.add(del);
        p.add(complete);
        p.add(refresh);
        return p;
    }

    private void onAdd() {
        TaskDialog dialog = new TaskDialog(this, "Add Task", null);
        Task t = dialog.showDialog();
        if (t != null) {
            taskManager.add(t);
            reminderService.scheduleFor(t);
            saveAndRefresh();
        }
    }

    private void onEdit() {
        Task selected = getSelectedTask();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a task to edit.");
            return;
        }
        TaskDialog dialog = new TaskDialog(this, "Edit Task", selected);
        Task updated = dialog.showDialog();
        if (updated != null) {
            taskManager.update(updated);
            reminderService.scheduleFor(updated);
            saveAndRefresh();
        }
    }

    private void onDelete() {
        Task selected = getSelectedTask();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a task to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected task?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            reminderService.cancel(selected.getId());
            taskManager.delete(selected.getId());
            saveAndRefresh();
        }
    }

    private void onComplete() {
        Task selected = getSelectedTask();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a task to mark complete.");
            return;
        }
        selected.setCompleted(true);
        taskManager.update(selected);
        reminderService.cancel(selected.getId());
        saveAndRefresh();
    }

    private Task getSelectedTask() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int modelRow = row;
        return tableModel.getAt(modelRow);
    }

    private void refreshTable() {
        Predicate<Task> filter = buildFilter();
        List<Task> tasks = taskManager.filter(filter);
        String sort = (String) sortCombo.getSelectedItem();
        if ("Deadline".equals(sort)) {
            tasks = tasks.stream().sorted((a,b) -> {
                if (a.getDeadline() == null && b.getDeadline() != null) return 1;
                if (a.getDeadline() != null && b.getDeadline() == null) return -1;
                if (a.getDeadline() == null) return 0;
                return a.getDeadline().compareTo(b.getDeadline());
            }).collect(Collectors.toList());
        } else if ("Priority".equals(sort)) {
            tasks = tasks.stream().sorted((a,b) ->
                    Integer.compare(b.getPriority().weight(), a.getPriority().weight())
            ).collect(Collectors.toList());
        } else {
            tasks = tasks.stream().sorted(Task::urgencyCompareTo).collect(Collectors.toList());
        }
        tableModel.setTasks(tasks);
    }

    private Predicate<Task> buildFilter() {
        String f = (String) filterCombo.getSelectedItem();
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        Predicate<Task> base = t -> q.isEmpty() || (t.getTitle() != null && t.getTitle().toLowerCase().contains(q));

        LocalDate today = LocalDate.now();
        switch (f) {
            case "Today":
                return t -> !t.isCompleted()
                        && t.getDeadline() != null
                        && t.getDeadline().toLocalDate().isEqual(today)
                        && base.test(t);
            case "High Priority":
                return t -> !t.isCompleted() && t.getPriority() == Priority.HIGH && base.test(t);
            case "Upcoming (7 days)":
                return t -> {
                    if (t.isCompleted() || t.getDeadline() == null) return false;
                    LocalDate d = t.getDeadline().toLocalDate();
                    return (d.isEqual(today) || (d.isAfter(today) && !d.isAfter(today.plusDays(7)))) && base.test(t);
                };
            case "Overdue":
                return t -> !t.isCompleted()
                        && t.getDeadline() != null
                        && t.getDeadline().isBefore(LocalDateTime.now())
                        && base.test(t);
            case "Completed":
                return t -> t.isCompleted() && base.test(t);
            case "All":
            default:
                return t -> base.test(t);
        }
    }

    private void saveAndRefresh() {
        storage.save(taskManager.getAll());
        refreshTable();
    }

    private void onExit() {
        storage.save(taskManager.getAll());
        reminderService.shutdown();
        dispose();
        System.exit(0);
    }

    private static class TaskTableModel extends AbstractTableModel {
        private final String[] cols = {"Title", "Priority", "Deadline", "Created", "Completed", "Reminder (min)"};
        private List<Task> tasks = new ArrayList<>();

        public void setTasks(List<Task> tasks) {
            this.tasks = tasks;
            fireTableDataChanged();
        }

        public Task getAt(int row) {
            if (row < 0 || row >= tasks.size()) return null;
            return tasks.get(row);
        }

        @Override public int getRowCount() { return tasks.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Task t = tasks.get(rowIndex);
            switch (columnIndex) {
                case 0: return t.getTitle();
                case 1: return t.getPriority().name();
                case 2: return t.getDeadline() == null ? "" : DateUtil.formatDateTime(t.getDeadline());
                case 3: return DateUtil.formatDateTime(t.getCreatedAt());
                case 4: return t.isCompleted() ? "Yes" : "No";
                case 5: return t.getReminderMinutesBefore() == null ? "" : t.getReminderMinutesBefore();
                default: return "";
            }
        }
    }
}
