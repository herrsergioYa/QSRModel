package rla;

/**
 * Created by HerrSergio on 17.09.2016.
 */
public class InputData {
    private double lambda;
    private double mu;
    private int m;
    private int n;
    private int l;
    private int r;
    private int s;
    private int p;

    public InputData() {
    }

    public InputData(double lambda, double mu, int m, int n, int l, int r, int s, int p) {
        this.lambda = lambda;
        this.mu = mu;
        this.m = m;
        this.n = n;
        this.l = l;
        this.r = r;
        this.s = s;
        this.p = p;
    }

    public double getLambda() {
        return lambda;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    public double getMu() {
        return mu;
    }

    public void setMu(double mu) {
        this.mu = mu;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getL() {
        return l;
    }

    public void setL(int l) {
        this.l = l;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getS() {
        return s;
    }

    public void setS(int s) {
        this.s = s;
    }

    public int getP() {
        return p;
    }

    public void setP(int p) {
        this.p = p;
    }
    
}
