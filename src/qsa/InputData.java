package qsa;

/**
 * Created by HerrSergio on 17.09.2016.
 */
public class InputData {
    private long executorsCount;
    private long queueLimit;
    private double lambda;
    private double nulambda;
    private double mu;
    private double numu;
    private double nu;

    public InputData() {
    }

    public InputData(long n, long m, double lambda, double nulambda, double mu, double numu, double nu) {
        this.executorsCount = n;
        this.queueLimit = m;
        this.lambda = lambda;
        this.nulambda = nulambda;
        this.mu = mu;
        this.numu = numu;
        this.nu = nu;
    }

    public long getN() {
        return executorsCount;
    }

    public void setN(long n) {
        this.executorsCount = n;
    }

    public long getM() {
        return queueLimit;
    }

    public void setM(long m) {
        this.queueLimit = m;
    }

    public double getLambda() {
        return lambda;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    public double getNulambda() {
        return nulambda;
    }

    public void setNulambda(double nulambda) {
        this.nulambda = nulambda;
    }

    public double getMu() {
        return mu;
    }

    public void setMu(double mu) {
        this.mu = mu;
    }

    public double getNumu() {
        return numu;
    }

    public void setNumu(double numu) {
        this.numu = numu;
    }

    public double getNu() {
        return nu;
    }

    public void setNu(double nu) {
        this.nu = nu;
    }
}
