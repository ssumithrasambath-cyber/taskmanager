
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class TaskManagerGUI extends JFrame {

    private JTextField taskInput;
    private DefaultTableModel model;
    private JTable table;

    private JLabel runningLabel;
    private JLabel completedLabel;
    private JLabel totalLabel;
    private JLabel pausedLabel;
    private JLabel stoppedLabel;

    private ArrayList<Task> tasks = new ArrayList<>();

    private int taskCounter = 1;
    private int completedCount = 0;

    public TaskManagerGUI() {

        setTitle("Multithreading Task Management System");
        setSize(950, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        // Heading

        JLabel heading = new JLabel(
            "MULTITHREADING TASK MANAGER",
            SwingConstants.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 22));
        main.add(heading, BorderLayout.NORTH);

        // Input Panel

        JPanel inputPanel = new JPanel();

        taskInput = new JTextField(20);

        JButton addButton = new JButton("Add Task");
        JButton startButton = new JButton("Start All");

        inputPanel.add(new JLabel("Task Name:"));
        inputPanel.add(taskInput);
        inputPanel.add(addButton);
        inputPanel.add(startButton);

        // Table

        String[] columns = {
            "ID", "Task Name", "Status",
            "Progress", "Thread Name"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        // Progress Bar Renderer

        table.getColumnModel().getColumn(3).setCellRenderer(
            new JProgressBarRenderer()
        );

        // Control Buttons

        JButton pauseButton = new JButton("Pause");
        JButton resumeButton = new JButton("Resume");
        JButton stopButton = new JButton("Stop");
        JButton clearButton = new JButton("Clear");

        JPanel controlPanel = new JPanel();

        controlPanel.add(pauseButton);
        controlPanel.add(resumeButton);
        controlPanel.add(stopButton);
        controlPanel.add(clearButton);

        // Center Panel

        JPanel center = new JPanel(new BorderLayout(10, 10));

        center.add(inputPanel, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(controlPanel, BorderLayout.SOUTH);

        main.add(center, BorderLayout.CENTER);

        // Statistics Dashboard

        JPanel statusPanel = new JPanel(
            new GridLayout(1, 5, 10, 10)
        );

        statusPanel.setBorder(
        BorderFactory.createTitledBorder("TASK STATISTICS")
        );

        totalLabel = new JLabel("Total: 0", SwingConstants.CENTER);
        runningLabel = new JLabel("Running: 0", SwingConstants.CENTER);
        pausedLabel = new JLabel("Paused: 0", SwingConstants.CENTER);  
        completedLabel = new JLabel("Completed: 0", SwingConstants.CENTER);
        stoppedLabel = new JLabel("Stopped: 0", SwingConstants.CENTER);

        Font statsFont = new Font("Arial", Font.BOLD, 14);

        totalLabel.setFont(statsFont);
        runningLabel.setFont(statsFont);
        pausedLabel.setFont(statsFont);
        completedLabel.setFont(statsFont);
        stoppedLabel.setFont(statsFont);

        statusPanel.add(totalLabel);
        statusPanel.add(runningLabel);
        statusPanel.add(pausedLabel);
        statusPanel.add(completedLabel);
        statusPanel.add(stoppedLabel);

        statusPanel.setPreferredSize(new Dimension(900, 75));

        main.add(statusPanel, BorderLayout.SOUTH); 

        add(main);

        // Button Actions

        addButton.addActionListener(e -> addTask());

        startButton.addActionListener(e -> startAllTasks());

        pauseButton.addActionListener(e -> {
            Task task = getSelectedTask();
            if (task != null) {
                task.pauseTask();
            }
        });

        resumeButton.addActionListener(e -> {
            Task task = getSelectedTask();
            if (task != null) {
                task.resumeTask();
            }
        });

        stopButton.addActionListener(e -> {
            Task task = getSelectedTask();
            if (task != null) {
                task.stopTask();
            }
        });

        clearButton.addActionListener(e -> clearTasks());
    }

    // Add Task

    private void addTask() {

        String name = taskInput.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Enter task name!"
            );
            return;
        }

        Task task = new Task(taskCounter, name, this);

        tasks.add(task);

        model.addRow(new Object[]{
            taskCounter, name, "Pending", "0%", "-"
        });

        taskCounter++;

        taskInput.setText("");

        updateStatistics();
    }

    // Start All Tasks

    private void startAllTasks() {

        if (tasks.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Add tasks first!"
            );
            return;
        }

        for (Task task : tasks) {
            task.startTask();
        }
    }

    // Get Selected Task

    private Task getSelectedTask() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                this, "Please select a task first!"
            );
            return null;
        }

        int modelRow = table.convertRowIndexToModel(row);

        int id = (int) model.getValueAt(modelRow, 0);

        for (Task task : tasks) {

            if (task.getName().equals("Thread-" + id)) {
                return task;
            }
        }

        return null;
    }

    // Clear Tasks

    private void clearTasks() {

        for (Task task : tasks) {

            if (task.isAlive()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Stop or complete all running tasks first!"
                );
                return;
            }
        }

        tasks.clear();

        model.setRowCount(0);

        completedCount = 0;

        updateStatistics();

        JOptionPane.showMessageDialog(
            this, "All tasks cleared successfully!"
        );
    }

    // Update Task Details

    public void updateTask(
        int id, String status, int progress, String threadName
    ) {

        SwingUtilities.invokeLater(() -> {

            for (int i = 0; i < model.getRowCount(); i++) {

                int currentId =
                    (int) model.getValueAt(i, 0);

                if (currentId == id) {

                    model.setValueAt(status, i, 2);
                    model.setValueAt(progress + "%", i, 3);
                    model.setValueAt(threadName, i, 4);

                    break;
                }
            }

            updateStatistics();
        });
    }

    // Statistics Dashboard Update

    private void updateStatistics() {

        int total = model.getRowCount();

        int running = 0;
        int paused = 0;
        int completed = 0;
        int stopped = 0;

        for (int i = 0; i < total; i++) {

            String status =
                model.getValueAt(i, 2).toString();

            if (status.equals("Running")) {
                running++;
            }
            else if (status.equals("Paused")) {
                paused++;
            }
            else if (status.equals("Completed")) {
                completed++;
            }
            else if (status.equals("Stopped")) {
                stopped++;
            }
        }

        totalLabel.setText("Total: " + total);
        runningLabel.setText("Running: " + running);
        pausedLabel.setText("Paused: " + paused);
        completedLabel.setText("Completed: " + completed);
        stoppedLabel.setText("Stopped: " + stopped);
    }

    // Compatibility with Task.java

    public void taskCompleted() {

        SwingUtilities.invokeLater(() -> {
            updateStatistics();
        });
    }

    // Main Method

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            new TaskManagerGUI().setVisible(true);

        });
    }
}
