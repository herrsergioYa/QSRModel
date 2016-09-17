package qsgm;

/**
 * Created by HerrSergio on 17.09.2016.
 */
public class InputData {
    private double alphas[] ;
    private double lambdas[];
    private double nus[];
    private double mu;

    public InputData() {
    }

    public InputData(double[] alphas, double[] lambdas, double[] nus, double mu) {
        this.alphas = alphas;
        this.lambdas = lambdas;
        this.nus = nus;
        this.mu = mu;
    }

    public double[] getAlphas() {
        return alphas;
    }

    public void setAlphas(double[] alphas) {
        this.alphas = alphas;
    }

    public double[] getLambdas() {
        return lambdas;
    }

    public void setLambdas(double[] lambdas) {
        this.lambdas = lambdas;
    }

    public double[] getNus() {
        return nus;
    }

    public void setNus(double[] nus) {
        this.nus = nus;
    }

    public double getMu() {
        return mu;
    }

    public void setMu(double mu) {
        this.mu = mu;
    }
}
