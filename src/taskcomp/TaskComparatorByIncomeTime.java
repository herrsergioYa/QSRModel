package taskcomp;

import qssim.Task;

import java.util.Comparator;
import java.util.function.ToLongFunction;

/**
 * Created by HerrSergio on 17.08.2016.
 */
public class TaskComparatorByIncomeTime extends TaskComparator {
    @Override
    public int compare(Task task, Task t1) {
        return Double.compare(task.getIncomeTime(), t1.getIncomeTime());
    }
}
