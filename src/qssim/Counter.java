package qssim;

/**
 * Created by HerrSergio on 17.08.2016.
 */
public class Counter {
    private StatisticHolder statisticHolder = new StatisticHolder();
    private int income;
    private int outcome;

    public void in(int count, double currentTime) {
        if(count < 0)
            throw new IllegalArgumentException();
        statisticHolder.add(count, currentTime);
        income += count;
    }

    public void in(double currentTime) {
        in(1, currentTime);
    }

    public void out(int count, double currentTime) {
        if(count < 0)
            throw new IllegalArgumentException();
        statisticHolder.add(-count, currentTime);
        outcome += count;
    }

    public void out(double currentTime) {
        out(1, currentTime);
    }

    public int getIncome() {
        return income;
    }

    public int getOutcome() {
        return outcome;
    }

    public int getCount() {
        return income - outcome;
    }

    public double getThroughCount() {
        return (income + 0.0 + outcome) / 2;
    }

    public double getAverageCount(double currentTime) {
        return statisticHolder.getAverage(currentTime);
    }

    public double getAverageSojournTime(double currentTime) {
        return getAverageCount(currentTime) * currentTime / getThroughCount();
    }
}
