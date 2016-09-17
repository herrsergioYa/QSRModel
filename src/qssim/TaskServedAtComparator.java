package qssim;

import qssim.Task;

import java.util.Comparator;

/**
 * Created by HerrSergio on 17.08.2016.
 */
class TaskServedAtComparator implements Comparator<Task> {
    @Override
    public int compare(Task task, Task t1) {
        return Double.compare(task.getServedAt(), t1.getServedAt());
    }
}
