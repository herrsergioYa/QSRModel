package qssim;

import qssim.Task;

import java.util.Comparator;

/**
 * Created by HerrSergio on 17.08.2016.
 */
class DropTaskComparator implements Comparator<Task> {
    @Override
    public int compare(Task task, Task t1) {
        if(task == t1)
            return 0;
        if(task == null)
            return Integer.MAX_VALUE;
        if (t1 == null)
            return Integer.MIN_VALUE;
        int ret = Double.compare(task.getDeadlineTime(), t1.getDeadlineTime());
        if(ret == 0)
            ret = Double.compare(task.getIncomeTime(), t1.getIncomeTime());
        return ret;
    }
}
