/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rnsim;

import java.util.Random;

/**
 *
 * @author HerrSergio
 */
public class Node {
    private boolean ok;
    private NodeChar ch;
    private double nextEventTime; 

    public Node(NodeChar ch) {
        this.ch = ch;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public NodeChar getCh() {
        return ch;
    }

    public void setCh(NodeChar ch) {
        this.ch = ch;
    }

    public double getNextEventTime() {
        return nextEventTime;
    }

    public void setNextEventTime(double nextEventTime) {
        this.nextEventTime = nextEventTime;
    }
    
    public void generateEvent(Random r) {
        ok = !ok;
        if(ok) {
            nextEventTime += ch.getLambda().next(r);
        } else {
            nextEventTime += ch.getMu().next(r);
        }
    }
}
