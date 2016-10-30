/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qsgm;

/**
 *
 * @author HerrSergio
 */
public class Branch {
    private double alpha;
    private double hazard;
    private double cov;

    public Branch(double alpha, double hazard, double cov) {
        this.alpha = alpha;
        this.hazard = hazard;
        this.cov = cov;
    }
    
    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getHazard() {
        return hazard;
    }

    public void setHazard(double hazard) {
        this.hazard = hazard;
    }

    public double getCov() {
        return cov;
    }

    public void setCov(double cov) {
        this.cov = cov;
    }
    
    
}
