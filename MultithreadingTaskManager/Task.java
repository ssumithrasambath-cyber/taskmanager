
public class Task extends Thread {

    private int taskId;
    private String taskName;
    private TaskManagerGUI gui;

    private boolean started = false;
    private boolean paused = false;
    private boolean stopped = false;

    private int currentProgress = 0;

    public Task(int id, String name, TaskManagerGUI gui) {
        this.taskId = id;
        this.taskName = name;
        this.gui = gui;
        setName("Thread-" + id);
    }

    public synchronized void startTask() {
        if (!started && !stopped) {
            started = true;
            start();
        }
    }

    public void pauseTask() {
        synchronized (this) {
            if (!started || stopped || !isAlive() || paused) {
                return;
            }
            paused = true;
        }

        gui.updateTask(taskId, "Paused", currentProgress, getName());
    }

    public void resumeTask() {
        synchronized (this) {
            if (!paused || stopped) {
                return;
            }

            paused = false;
            notifyAll();
        }

        gui.updateTask(taskId, "Running", currentProgress, getName());
    }

    public void stopTask() {
        synchronized (this) {
            if (stopped) {
                return;
            }

            stopped = true;
            paused = false;
            notifyAll();
        }

        interrupt();

        gui.updateTask(taskId, "Stopped", currentProgress, getName());
    }

    
@Override
public void run() {

    for (int i = 1; i <= 100; i++) {

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Stop status is checked below.
        }

        synchronized (this) {

            // Wait while task is paused
            while (paused && !stopped) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    if (stopped) {
                        break;
                    }
                }
            }

            if (stopped) {
                break;
            }

            currentProgress = i;

            // Update while holding the lock to avoid
            // an old Running update overwriting Paused.
            gui.updateTask(
                taskId, "Running", currentProgress, getName()
            );
        }
    }

    if (stopped) {

        gui.updateTask(
            taskId, "Stopped", currentProgress, getName()
        );

    } else {

        gui.updateTask(
            taskId, "Completed", 100, getName()
        );

        gui.taskCompleted();
    }
}

}
