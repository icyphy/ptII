/* Philosopher in Sieve of Eratosthenes demo.

 Copyright (c) 1998 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (nsmyth@eecs.berkeley.edu)

*/

package ptolemy.domains.csp.lib;

import ptolemy.domains.csp.kernel.*;
import ptolemy.domains.csp.demo.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import collections.LinkedList;
import java.util.Enumeration;
import java.util.Random;

//////////////////////////////////////////////////////////////////////////
//// Philosopher in Sieve of Eratosthenes demo.
/**
A philosopher sits at a table with 4 other philososphers. Between 
each two philosophers there is a fork. A Philosopher eats when he has 
both forks next to him. A Philosopher thinks for a while, then tries 
to eat. When he suceeds in obtaining both forks he eats for a while, 
then puts both forks back on the table and continues thinking.
<p>
@author Neil Smyth
@version 

 */
public class CSPPhilosopher extends CSPActor {
    
    public CSPPhilosopher(CompositeActor cont, String name)
        throws IllegalActionException, NameDuplicationException {
        super(cont, name);
        leftIn = new IOPort(this, "leftIn", true, false);
        leftOut = new IOPort(this, "leftOut", false, true);
        rightIn = new IOPort(this, "rightIn", true, false);
        rightOut = new IOPort(this, "rightOut", false, true);
        
        _eating = new Parameter(this, "eatingRate");
        _eating.setExpression("1.0");

        _thinking = new Parameter(this, "thinkingRate");
        _thinking.setExpression("1.0");
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    public void fire() {
        Random rand = new Random();
        Token t = new IntToken(0);
        double interval = 0.0;
        double rate = 1;
        int count = 0;
        try {
            while (count < 20 ) {
                rate = ((DoubleToken)_thinking.getToken()).doubleValue();
                interval = (int)(rand.nextDouble()*rate*1000);
                interval = interval/1000;
                System.out.println(getName() + count + ": thinking for "
                        + interval);   
                Thread th = Thread.currentThread();
                th.sleep((long)interval*1000);
                delay(interval);

                // Obtain the forks
                if (rand.nextDouble() > 0.5) {
                    leftIn.get(0);
                    gotLeft = true;
                    notifyListeners();
                    waitingRight = true;
                    rightIn.get(0);
                    gotRight = true;
                    waitingRight = false;
                    notifyListeners();
                } else {
                    rightIn.get(0);
                    gotRight = true;
                    notifyListeners();
                    waitingLeft = true;
                    leftIn.get(0);
                    gotLeft = true;
                    waitingLeft = false;
                    notifyListeners();
                }
                rate = ((DoubleToken)_eating.getToken()).doubleValue();
                interval = (int)(rand.nextDouble()*rate*2000);
                interval = interval/1000;
                System.out.println(getName() + ": eating for " + interval);
                th.sleep((long)interval*1000);
                delay(interval);

                // Release the forks.
                leftOut.send(0, t);
                gotLeft = false;
                rightOut.send(0,t);
                gotRight = false;
                   
                notifyListeners();
                    
                count++;
            }
            return;
        } catch (IllegalActionException ex) {
            System.out.println(getName() + ": invalid get, exiting...");
        } catch (NoTokenException ex) {
            System.out.println(getName() + ": invalid get, exiting...");
        } catch (InterruptedException ex) {
            System.out.println(getName() + ": invalid get, exiting...");
        }
    }

    public boolean postfire() {
        return false;
    }
    
    /** Register a PhilosospherListener with this Philosopher.
     */
     public void addPhilosopherListener(PhilosopherListener newListener) {
         if (_listeners == null) {
             _listeners = new LinkedList();
         }
         _listeners.insertLast(newListener);
     }
     
    /*  Notify any PhilosospherListeners that have registered an
     *  interest/dependency in this Philosospher.
     */
    protected void notifyListeners() {
        if (_listeners == null) {
            // No listeners to notify.
            return;
        }
        Enumeration list = _listeners.elements();
        while (list.hasMoreElements()) {
            ((PhilosopherListener)list.nextElement()).philosopherChanged();
        }
    }


    public IOPort leftIn;
    public IOPort leftOut;
    public IOPort rightIn;
    public IOPort rightOut;

    public boolean gotLeft = false;
    public boolean gotRight = false;
    public boolean waitingLeft = false;
    public boolean waitingRight = false;

    private LinkedList _listeners;
    private Parameter _eating;
    private Parameter _thinking;
    
}
