package taskcomp;

import qssim.Task;

import java.util.Comparator;

/**
 * Created by HerrSergio on 17.08.2016.
 */
public class TaskComparatorByDeadline extends TaskComparator {

    @Override
    public int compare(Task task, Task t1) {
        return Double.compare(task.getDeadlineTime(), t1.getDeadlineTime());
    }
}
