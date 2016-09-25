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
@Deprecated
public class Junction {
    private JunctionChar ch;

    public Junction() {
    }

    public Junction(JunctionChar ch) {
        this.ch = ch;
    }

    public JunctionChar getCh() {
        return ch;
    }

    public void setCh(JunctionChar ch) {
        this.ch = ch;
    }
    
    
}
