package rlsim;

/**
 * Created by HerrSergio on 17.09.2016.
 */
public class Value {
    private StochasticValue possibility;
    private StochasticValue duration;

    /*public Value() {
        this.possibility = new StochasticValue();
        this.duration = new StochasticValue();
    }*/

    public Value(double possibility, double duration) {
        this.possibility = new StochasticValue(possibility);
        this.duration = new StochasticValue(duration);
    }

    public Value(StochasticValue possibility, StochasticValue duration) {
        this.possibility = possibility;
        this.duration = duration;
    }

    public StochasticValue getPossibility() {
        return possibility;
    }

    public void setPossibility(StochasticValue possibility) {
        this.possibility = possibility;
    }

    public StochasticValue getDuration() {
        return duration;
    }

    public void setDuration(StochasticValue duration) {
        this.duration = duration;
    }

    public Value join(Value value) {
        return new Value(this.getPossibility().join(value.getPossibility()), this.getDuration().join(value.getDuration()));
    }
}
