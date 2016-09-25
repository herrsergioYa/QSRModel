/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rnsim;

import distributions.Distribution;

/**
 *
 * @author HerrSergio
 */
public class NodeChar {
    private String name;
    private Distribution lambda;
    private Distribution mu;
    private int installedRedunnduncy, stashedRedundancy;

    public NodeChar() {
    }

    public NodeChar(String name, Distribution lambda, Distribution mu, int installedRedunnduncy, int stashedRedundancy) {
        this.name = name;
        this.lambda = lambda;
        this.mu = mu;
        this.installedRedunnduncy = installedRedunnduncy;
        this.stashedRedundancy = stashedRedundancy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Distribution getLambda() {
        return lambda;
    }

    public void setLambda(Distribution lambda) {
        this.lambda = lambda;
    }

    public Distribution getMu() {
        return mu;
    }

    public void setMu(Distribution mu) {
        this.mu = mu;
    }

    public int getInstalledRedunnduncy() {
        return installedRedunnduncy;
    }

    public void setInstalledRedunnduncy(int installedRedunnduncy) {
        this.installedRedunnduncy = installedRedunnduncy;
    }

    public int getStashedRedundancy() {
        return stashedRedundancy;
    }

    public void setStashedRedundancy(int stashedRedundancy) {
        this.stashedRedundancy = stashedRedundancy;
    }
    
    
}
