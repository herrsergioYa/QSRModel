package qsgm;

import java.util.Arrays;

/**
 * Created by HerrSergio on 17.09.2016.
 */
public class InputData {
   /*private double alphas[] ;
    private double lambdas[];
    private double nus[];*/
    private Branch[] lambda;
    private double mu;

    public InputData() {
    }

    /*public InputData(double[] alphas, double[] lambdas, double[] nus, double mu) {
        this.alphas = alphas;
        this.lambdas = lambdas;
        this.nus = nus;
        this.mu = mu;
    }*/

    public double[] getAlphas() {
        return Arrays.stream(lambda).mapToDouble(b -> b.getAlpha()).toArray();
    }

  /*  public void setAlphas(double[] alphas) {
        this.alphas = alphas;
    }*/

    public double[] getLambdas() {
        return Arrays.stream(lambda).mapToDouble(b -> b.getHazard()).toArray();
    }

 /*   public void setLambdas(double[] lambdas) {
        this.lambdas = lambdas;
    }*/

    public double[] getNus() {
        return Arrays.stream(lambda).mapToDouble(b -> b.getCov()).toArray();
    }

  /*  public void setNus(double[] nus) {
        this.nus = nus;
    }*/

    public Branch[] getLambda() {
        return lambda;
    }

    public void setLambda(Branch[] lambda) {
        this.lambda = lambda;
    }
    
    public double getMu() {
        return mu;
    }

    public void setMu(double mu) {
        this.mu = mu;
    }
}
