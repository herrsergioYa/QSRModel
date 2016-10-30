package qsa;

/**
 * Created by HerrSergio on 17.09.2016.
 */
public class InputData {
    private long executorsCount;
    private long queueLimit;
    private Momenta lambda;
    private Momenta mu;
    private Momenta nu;

    public InputData() {
    }

    /*public InputData(long n, long m, double lambda, double nulambda, double mu, double numu, double nu) {
        this.executorsCount = n;
        this.queueLimit = m;
        this.lambda = lambda;
        this.nulambda = nulambda;
        this.mu = mu;
        this.numu = numu;
        this.nu = nu;
    }*/

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
        return lambda.getHazard();
    }

    public void setLambda(double lambda) {
        this.lambda.setHazard(lambda);
    }

    public double getNulambda() {
        return lambda.getCov();
    }

    public void setNulambda(double nulambda) {
        this.lambda.setCov(nulambda);
    }

    public double getMu() {
        return mu.getHazard();
    }

    public void setMu(double mu) {
        this.mu.setHazard(mu);
    }

    public double getNumu() {
        return mu.getCov();
    }

    public void setNumu(double numu) {
        this.mu.setCov(numu);
    }

    public double getNu() {
        return nu.getHazard();
    }

    public void setNu(double nu) {
        this.nu.setHazard(nu);
    }
    
    public double getNunu() {
        return nu.getCov();
    }

    public void setNunu(double nunu) {
        this.nu.setCov(nunu);
    }
}
