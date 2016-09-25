package qssim;

import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Created by HerrSergio on 19.08.2016.
 */
public class StochasticValue {

    //private static double[] zArray = {235.8, 19.2, 9.2, 6.6, 5.5, 4.9, 4.5, 4.3, 4.1, 4 };

    private static NavigableMap<Integer, Double> zSet = new TreeMap<Integer, Double>() {
        {
            put(0, Double.POSITIVE_INFINITY);
            put(1, 235.8);
            put(2, 19.2);
            put(3, 9.2);
            put(4, 6.6);
            put(5, 5.5);
            put(6, 4.9);
            put(7, 4.5);
            put(8, 4.3);
            put(9, 4.1);
            put(10, 4.0);
            put(12, 3.8);
            put(13, 3.7);
            put(15, 3.6);
            put(18, 3.5);
            put(23, 3.4);
            put(32, 3.3);
            put(52, 3.2);
            put(152, 3.1);
            put(Integer.MAX_VALUE, 3.0);
        }
    };

    public static double getZ(int dof) {

        return zSet.ceilingEntry(dof).getValue();

        /*if(dof < 1)
            return Double.POSITIVE_INFINITY;
        else if(dof < 11)
            return zArray[dof - 1];
        else if(dof < 19) {
            if (dof < 14) {
                if (dof < 13)
                    return 3.8;
                else
                    return 3.7;
            } else {
                if (dof < 16)
                    return 3.6;
                else
                    return 3.5;
            }
        } else if (dof < 33) {
            if (dof < 24)
                return 3.4;
            else
                return 3.3;
        } else if (dof < 153) {
            if (dof < 53)
                return 3.2;
            else
                return 3.1;
        } else {
            return 3.0;
        }*/

    }

    private double sum;
    private double sumSqr;
    private int count;

    public StochasticValue() {

    }
    
    public StochasticValue(double value) {
        this.sum = value;
        this.sumSqr = value * value;
        this.count = 1;
    }

    public StochasticValue(StochasticValue a, double b) {
        this.sum = a.sum + b;
        this.sumSqr = a.sumSqr + b * b;
        this.count = a.count + 1;
    }

    public StochasticValue(StochasticValue a, StochasticValue b) {
        this.sum = a.sum + b.sum;
        this.sumSqr = a.sumSqr + b.sumSqr;
        this.count = a.count + b.count;
    }

    public double getAverage() {
        return sum / count;
    }

    public double getAverageSquare() {
        return sumSqr / count;
    }

    public double getVariance(boolean ofMean, boolean unbiased) {
        if(count < 2)
            return Double.POSITIVE_INFINITY;
        double avg = getAverage();
        double variance = getAverageSquare() - avg * avg;
        if(unbiased) {
            variance /= count - 1.0;
            if(!ofMean) {
                variance *= count;
            }
        } else {
            if (ofMean) {
                variance /= count;
            }
        }
        return variance;
    }

    public double getStandardDeviation(boolean ofMean, Boolean corrected) {
        if(count < 2)
            return Double.POSITIVE_INFINITY;
        if(corrected != null) {
            return Math.sqrt(getVariance(ofMean, corrected));
        } else {
            return Math.sqrt(getVariance(ofMean, false) * count / (count - 1.5));
        }
    }

    public double getError(boolean relative, double z) {
        double error = z * getStandardDeviation(true, null);
        if(relative && error > 0) {
            error /= Math.abs(getAverage());
        }
        return error;
    }

    public double getError(boolean relative) {
        return getError(relative, getZ(count - 1));
    }

    public StochasticValue join(double value) {
        return new StochasticValue(this, value);
    }

    public StochasticValue join(StochasticValue stochasticValue) {
        return new StochasticValue(this, stochasticValue);
    }

    @Override
    public String toString() {
        return getAverage() + "±" + getError(true, 3.0) * 100 + "%";
    }
}
