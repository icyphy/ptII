/* A philosopher in the Dining Philosophers demo.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.csp.demo.DiningPhilosophers;

// Ptolemy imports.
import ptolemy.domains.csp.kernel.*;
import ptolemy.domains.csp.demo.DiningPhilosophers.*;
import ptolemy.actor.*;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

// Java imports.
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

//////////////////////////////////////////////////////////////////////////
//// Philosopher in the Dining Philosophers demo.
/**
A philosopher sits at a table with 4 other philosophers. Between
each two philosophers there is a fork. A Philosopher eats when he has
both forks next to him. A Philosopher thinks for a while, then tries
to eat. When he succeeds in obtaining both forks he eats for a while,
then puts both forks back on the table and continues thinking.
<p>
Due to the rendezvous nature of communication in the CSP domain, a
philosopher stalls if it tries to get a chopstick but cannot. When
it acquires the chopstick, it eats for a while and then sends a
message to the chopstick to say that it is finished using it.
Note this actor has been slowed down with Thread.sleep() statements to
mimic the eating nature of the philosophers in real time.
<p>
This actor is parameterized by three parameters: "eatingRate" which
controls the distribution of the eating times, and "thinkingRate"
which controls the distribution of the thinking times. Both these
rates characterize a uniform distribution between 0 and the rate.
<p>
@author Neil Smyth
@version $Id$
 */
public class Philosopher extends CSPActor {

    /** Construct a Philosopher in the specified container with the
     *  specified name.  The name must be unique within the container or
     *  an exception is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  The actor is created with two input ports and two output
     *  ports, all of width one. The input ports are called "leftIn"
     *  and "rightIn", and similarly, the output ports are called "leftOut"
     *  and "rightOut".
     *  <p>
     *  The default values of the eatingRate and thinkingRate
     *  parameters are 1.0.
     *  <p>
     *  @param container The TypedCompositeActor that contains this actor.
     *  @param name The actor's name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name argument coincides with
     *   an entity already in the container.
     */
    public Philosopher(TypedCompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);
        leftIn = new TypedIOPort(this, "leftIn", true, false);
        leftOut = new TypedIOPort(this, "leftOut", false, true);
        rightIn = new TypedIOPort(this, "rightIn", true, false);
        rightOut = new TypedIOPort(this, "rightOut", false, true);

	leftIn.setTypeEquals(BaseType.GENERAL);
	rightIn.setTypeEquals(BaseType.GENERAL);
	leftOut.setTypeEquals(BaseType.INT);
	rightOut.setTypeEquals(BaseType.INT);

        eating = new Parameter(this, "eatingRate");
        eating.setExpression("1.0");

        thinking = new Parameter(this, "thinkingRate");
        thinking.setExpression("1.0");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The port through which this philosopher
     *  is granted access to the left chopstick.
     */
    public TypedIOPort leftIn;

    /** The port through which this philosopher
     *  releases access to the left chopstick.
     */
    public TypedIOPort leftOut;

    /** The port through which this philosopher
     *  is granted access to the right chopstick.
     */
    public TypedIOPort rightIn;

    /** The port through which this philosopher
     *  releases access to the right chopstick.
     */
    public TypedIOPort rightOut;

    /** This parameter determines the rate at which
     *  this philosopher will spend time eating.
     */
    public Parameter eating;

    /** This parameter determines the rate at which
     *  this philosopher will spend time thinking.
     */
    public Parameter thinking;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Register a PhilosopherListener with this Philosopher.
     */
    public void addPhilosopherListener(PhilosopherListener newListener) {
        if (_listeners == null) {
            _listeners = new LinkedList();
        }
        _listeners.add(newListener);
    }

    /** Executes the code in this actor. This actor randomly chooses
     *  whether to grab the chopstick to the left or right of it first.
     *  When it has one chopstick, it then tries to grab the other
     *  chopstick beside it.
     *  This process mimics the eating action of the philosopher twenty
     *  times, and then finishes normally.
     *  @exception IllegalActionException If an error occurs during
     *   executing the process.
     *  @exception ProcessTerminationException If the director requests
     *   termination.
     */
    public void fire() throws IllegalActionException {
        Token t = new IntToken(0);
        double interval = 0.0;
        double rate = 1;
        try {
            while (true) {
                rate = ((DoubleToken)thinking.getToken()).doubleValue();
                interval = (int)(_random.nextDouble()*rate*1000);
                interval = interval/1000;
                Thread th = Thread.currentThread();
                th.sleep((long)interval*1000);
                delay(interval);

                // Obtain the forks
                if (_random.nextDouble() > 0.5) {
                    leftIn.get(0);
                    gotLeft = true;
                    _notifyListeners();
                    waitingRight = true;
                    rightIn.get(0);
                    gotRight = true;
                    waitingRight = false;
                    _notifyListeners();
                } else {
                    rightIn.get(0);
                    gotRight = true;
                    _notifyListeners();
                    waitingLeft = true;
                    leftIn.get(0);
                    gotLeft = true;
                    waitingLeft = false;
                    _notifyListeners();
                }
                rate = ((DoubleToken)eating.getToken()).doubleValue();
                interval = (int)(_random.nextDouble()*rate*2000);
                interval = interval/1000;
                th.sleep((long)interval*1000);
                delay(interval);

                // Release the forks.
                leftOut.send(0, t);
                gotLeft = false;
                rightOut.send(0, t);
                gotRight = false;

                _notifyListeners();
            }
        } catch (NoTokenException ex) {
            throw new IllegalActionException(getName() + ": cannot " +
                    "get token.");
        } catch (InterruptedException ex) {
            // Terminating the fire().
        }
    }

    /** Return true if this actor is enabled to proceed with additional
     *  iterations; return false otherwise.
     *  @return True if continued execution is enabled; false otherwise.
     */
    public boolean postfire() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // Variables that are used by the applet to get the state of
    // the philosopher.
    public boolean gotLeft = false;
    public boolean gotRight = false;
    public boolean waitingLeft = false;
    public boolean waitingRight = false;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Notify any PhilosopherListeners that have registered an
     *  interest/dependency in this Philosopher.
     */
    protected void _notifyListeners() {
        if (_listeners == null) {
            // No listeners to notify.
            return;
        }
        Iterator listeners = _listeners.iterator();
        while (listeners.hasNext()) {
            ((PhilosopherListener)listeners.next()).philosopherChanged();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private List _listeners;

    private static Random _random = new Random();
}
