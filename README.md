# Smart Task Scheduler with Priority Queues (Java + Swing)

A lightweight desktop task manager that automatically prioritizes tasks by urgency using a PriorityQueue. 
Add, edit, delete, complete, filter, and search tasks in a clean Swing UI. 
get reminder popups before deadlines; and persist everything to a local JSON file.

## Features

- Urgency ordering: higher priority first, then earlier deadline, then older creation time.

- Task operations: add, edit, delete, mark complete.

- Filters: All, Today, High Priority, Upcoming (7 days), Overdue, Completed.

- Sorting & search: by urgency, deadline, priority + quick title search.

- Reminders: popup alerts scheduled before deadlines.

- Persistence: saves to {user.home}/.smart-task-scheduler/tasks.json.

## Tech Stack

- Java 17, Swing (JFrame, JTable, dialogs)
- Data structures: PriorityQueue, HashMap
- Scheduling: Timer/TimerTask, SwingUtilities (EDT)
- Build: Maven (compiler) or IntelliJ Artifact

## Getting Started

### Prerequisites

JDK 17 installed (JAVA_HOME set)

Maven installed (optional if building via IntelliJ)

IntelliJ IDEA (Community/Ultimate) recommended

### Linking

git remote add origin https://github.com/yadavsachchidanand18/smart-task-scheduler.git

git remote -v

### Run in IntelliJ

Open the folder (import as Maven project if prompted).

Ensure Project SDK is JDK 17.

Create/run Application configuration with Main class:

com.example.taskscheduler.Main

The app window should launch.
