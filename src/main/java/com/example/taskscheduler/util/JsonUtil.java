package com.example.taskscheduler.util;

import com.example.taskscheduler.model.Priority;
import com.example.taskscheduler.model.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal JSON utility specialized for Task serialization without external dependencies.
 * Stores a simple JSON array of task objects with fixed fields.
 */
public class JsonUtil {

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public static String toJson(List<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Task t : tasks) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{");
            sb.append("\"id\":\"").append(escape(t.getId())).append("\",");
            sb.append("\"title\":\"").append(escape(t.getTitle())).append("\",");
            sb.append("\"description\":\"").append(escape(t.getDescription())).append("\",");
            sb.append("\"priority\":\"").append(t.getPriority().name()).append("\",");
            sb.append("\"deadline\":").append(t.getDeadline() == null ? "null" : "\"" + escape(DateUtil.formatDateTime(t.getDeadline())) + "\"").append(",");
            sb.append("\"createdAt\":\"").append(escape(DateUtil.formatDateTime(t.getCreatedAt()))).append("\",");
            sb.append("\"completed\":").append(t.isCompleted()).append(",");
            sb.append("\"reminderMinutesBefore\":").append(t.getReminderMinutesBefore() == null ? "null" : t.getReminderMinutesBefore());
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    public static List<Task> parseTasks(String json) {
        // Very small hand-rolled parser specialized to our generated format.
        // Assumes well-formed JSON produced by toJson().
        List<Task> list = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return list;

        String s = json.trim();
        if (!s.startsWith("[") || !s.endsWith("]")) return list;
        s = s.substring(1, s.length() - 1).trim();
        if (s.isEmpty()) return list;

        // Split objects at top-level commas
        int depth = 0;
        StringBuilder cur = new StringBuilder();
        List<String> objs = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') depth++;
            if (c == '}') depth--;
            if (c == ',' && depth == 0) {
                objs.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) objs.add(cur.toString());

        for (String obj : objs) {
            String o = obj.trim();
            if (!o.startsWith("{") || !o.endsWith("}")) continue;
            o = o.substring(1, o.length() - 1);

            Task t = new Task();
            // Split fields by commas not inside quotes
            List<String> fields = splitTopLevel(o);
            for (String f : fields) {
                int idx = f.indexOf(':');
                if (idx < 0) continue;
                String key = stripQuotes(f.substring(0, idx).trim());
                String val = f.substring(idx + 1).trim();
                switch (key) {
                    case "id":
                        t.setId(stripQuotes(val));
                        break;
                    case "title":
                        t.setTitle(unescape(stripQuotes(val)));
                        break;
                    case "description":
                        t.setDescription(unescape(stripQuotes(val)));
                        break;
                    case "priority":
                        t.setPriority(Priority.fromString(stripQuotes(val)));
                        break;
                    case "deadline":
                        if ("null".equals(val)) {
                            t.setDeadline(null);
                        } else {
                            String dv = unescape(stripQuotes(val));
                            t.setDeadline(DateUtil.parseDateTime(dv));
                        }
                        break;
                    case "createdAt":
                        String cv = unescape(stripQuotes(val));
                        // createdAt always present in our format
                        t.setCreatedAt(DateUtil.parseDateTime(cv));
                        break;
                    case "completed":
                        t.setCompleted("true".equalsIgnoreCase(val));
                        break;
                    case "reminderMinutesBefore":
                        if ("null".equals(val)) {
                            t.setReminderMinutesBefore(null);
                        } else {
                            try {
                                t.setReminderMinutesBefore(Integer.parseInt(val));
                            } catch (NumberFormatException ignored) {
                                t.setReminderMinutesBefore(null);
                            }
                        }
                        break;
                }
            }
            if (t.getCreatedAt() == null) {
                // If older file missing createdAt, set now
                t.setCreatedAt(java.time.LocalDateTime.now());
            }
            if (t.getPriority() == null) t.setPriority(com.example.taskscheduler.model.Priority.MEDIUM);
            list.add(t);
        }

        return list;
    }

    private static List<String> splitTopLevel(String s) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inStr = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) {
                inStr = !inStr;
            }
            if (c == ',' && !inStr) {
                parts.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) parts.add(cur.toString());
        return parts;
    }

    private static String stripQuotes(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static String unescape(String s) {
        return s.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
