/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rnsim;

/**
 *
 * @author HerrSergio
 */
public class JunctionChar {
    private String from, to;
    private boolean bidirectional;

    public JunctionChar() {
    }

    public JunctionChar(String from, String to, boolean bidirectional) {
        this.from = from;
        this.to = to;
        this.bidirectional = bidirectional;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public boolean isBidirectional() {
        return bidirectional;
    }

    public void setBidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
    }
    
    
}
