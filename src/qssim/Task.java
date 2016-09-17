package qssim;

/**
 * Created by HerrSergio on 17.08.2016.
 */
public class Task {
    private double incomeTime;
    private double serviceTime;
    private double deadlineTime;
    private double servedAt;

    public Task(double incomeTime, double serviceTime, double deadlineTime) {
        this.incomeTime = incomeTime;
        this.serviceTime = serviceTime;
        this.deadlineTime = deadlineTime;
    }

    public double getIncomeTime() {
        return incomeTime;
    }

    public void setIncomeTime(double incomeTime) {
        this.incomeTime = incomeTime;
    }

    public double getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(double serviceTime) {
        this.serviceTime = serviceTime;
    }

    public double getDeadlineTime() {
        return deadlineTime;
    }

    public void setDeadlineTime(double deadlineTime) {
        this.deadlineTime = deadlineTime;
    }

    public double getServedAt() {
        return servedAt;
    }

    public void setServedAt(double servedAt) {
        this.servedAt = servedAt;
    }
}
