package taskcomp;

import qssim.Task;

import java.util.Comparator;

/**
 * Created by HerrSergio on 17.08.2016.
 */
public class TaskComparatorFastest extends TaskComparator {
    @Override
    public int compare(Task task, Task t1) {
        return Double.compare(task.getServiceTime(), t1.getServiceTime());
    }
}
