package com.example.taskscheduler.model;

public enum Priority {
    HIGH(3),
    MEDIUM(2),
    LOW(1);

    private final int weight;

    Priority(int weight) {
        this.weight = weight;
    }

    public int weight() {
        return weight;
    }

    public static Priority fromString(String s) {
        if (s == null) return MEDIUM;
        switch (s.trim().toUpperCase()) {
            case "HIGH": return HIGH;
            case "LOW": return LOW;
            default: return MEDIUM;
        }
    }
}
