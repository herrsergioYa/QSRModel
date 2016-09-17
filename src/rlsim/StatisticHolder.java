package rlsim;

/**
 * Created by HerrSergio on 17.08.2016.
 */
public class StatisticHolder {
    private int value;
    private double lastChangeTime;
    private double square;

    public StatisticHolder() {
    }

    public StatisticHolder(int value) {
        this.value = value;
    }

    public void setValue(int value, double currentTime) {
        if(currentTime < lastChangeTime)
            throw new IllegalStateException();
        square += this.value * (currentTime - lastChangeTime);
        this.value = value;
        this.lastChangeTime = currentTime;
    }

    public int getValue() {
        return value;
    }

    public void add(int amount, double currentTime) {
        setValue(getValue() + amount, currentTime);
    }

    public void increment(double currentTime) {
        add(1, currentTime);
    }

    public void decrement(double currentTime) {
        add(-1, currentTime);
    }

    public double getAverage(double currentTime) {
        setValue(getValue(), currentTime);
        return square / currentTime;
    }
}
