/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qsa;

/**
 *
 * @author HerrSergio
 */
public class Momenta {
    private double hazard;
    private double cov;

    public Momenta(double hazard, double cov) {
        this.hazard = hazard;
        this.cov = cov;
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
