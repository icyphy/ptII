/* An interruptible server.

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
*/

package ptolemy.domains.de.lib;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEInterruptibleServer
/**
Emulate a server. Input events are delayed by fixed minimum delay plus the
amount of interrupt processing time needed.

@author Lukito Muliadi
@version $Id$
@see DEActor
*/
public class DEInterruptibleServer extends DEActor {

    public static boolean DEBUG = false;

    /** Construct a DEServer star with the default service time equal to 1.0.
     *
     * @param serviceTime The service time
     * @param container The composite actor that this actor belongs too.
     * @param name The name of this actor.
     *
     * @exception NameDuplicationException Other star already had this name
     * @exception IllegalActionException internal problem
     */
    public DEInterruptibleServer(TypedCompositeActor container,
            String name)
            throws NameDuplicationException, IllegalActionException  {
        this(container, name, 1.0, 0.5);
    }

    /** Construct a DEServer star.
     *
     * @param serviceTime The service time
     * @param container The composite actor that this actor belongs too.
     * @param name The name of this actor.
     *
     * @exception NameDuplicationException Other star already had this name
     * @exception IllegalActionException internal problem
     */
    public DEInterruptibleServer(TypedCompositeActor container,
            String name,
            double minServiceTime, 
            double interruptServiceTime)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // create an output port
        output = new DEIOPort(this, "output", false, true);
        //output.setDeclaredType(DoubleToken.class);
        // create input ports
        input = new DEIOPort(this, "input", true, false);
        //input.setDeclaredType(DoubleToken.class);
        interrupt = new DEIOPort(this, "interrupt", true, false);
        interrupt.setDeclaredType(Token.class);

        // set the parameters.
        _minimumServiceTime = new Parameter(this, 
                "MinimumServiceTime", 
                new DoubleToken(minServiceTime));
        _interruptServiceTime = new Parameter(this,
                "InterruptServiceTime", 
                new DoubleToken(interruptServiceTime));
        
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Produce the output event according to whether the server is busy or
     *  not.
     *
     * @exception IllegalActionException Not thrown in this class.
     */
    public void fire() throws IllegalActionException{
        
        double interruptServiceTime = 
            ((DoubleToken)_interruptServiceTime.getToken()).doubleValue();
        double minimumServiceTime =
            ((DoubleToken)_minimumServiceTime.getToken()).doubleValue();
        double currentTime = getCurrentTime();

        boolean busy = _busyUntil > currentTime;
        
        if (busy) {
            // The interrupt event(s) delay the total execution time of
            // the current input events by even more.
            while (interrupt.hasToken(0)) {
               
                interrupt.get(0);
                _busyUntil += interruptServiceTime;
                if (DEBUG) {
                    System.out.println("DEInterruptibleServer: Interrupted " + 
                            "while busy at time " + 
                            currentTime + " and will be busy until " + 
                            _busyUntil + " .");
                }
            }
            fireAt(_busyUntil);
            

        } else {

            // if it's not busy, then accept refire.
            if (_tokenBeingServed != null) {
                output.broadcast(_tokenBeingServed);
                _tokenBeingServed = null;
                if (DEBUG) {
                    System.out.println("DEInterruptibleServer: Output at " + 
                            "time " + currentTime + " .");
                }
            }

            // process all the interrupts.
            while (interrupt.hasToken(0)) {
                interrupt.get(0);

                // due to these interrupts, it might become busy.
                // If that's the case then add the time wrt to the _busyUntil
                // value. Othewise add it wrt to the current time.
                if (_busyUntil > currentTime) {
                    _busyUntil += interruptServiceTime;
                } else {   
                    _busyUntil = currentTime + interruptServiceTime;
                }

                if (DEBUG) {
                    System.out.println("DEInterruptibleServer: Interrupted " + 
                            "while not busy at time " + 
                            currentTime + " and will be busy until " + 
                            _busyUntil + " .");
                }
                
            }

            // process input event after the interrupts.
            if (input.hasToken(0)) {
                _tokenBeingServed = input.get(0);
                
                // If due to the interrupts above, the _busyUntil has
                // got incremented then add the time to that value.
                // Otherwise, it will busy starting from currentTime processing
                // the input event.
                if (_busyUntil > currentTime) {
                    _busyUntil += minimumServiceTime;
                } else {
                    _busyUntil = currentTime + minimumServiceTime;
                }

                if (DEBUG) {
                    System.out.println("DEInterruptibleServer: Input at " + 
                            "time " + currentTime + " .");
                }
                fireAt(_busyUntil);
            }

            
        }
    }

    /**
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        input.allowPendingTokens(true);
        while (input.hasToken(0)) {
            input.get(0);
        }
        _busyUntil = Double.NEGATIVE_INFINITY;
        _tokenBeingServed = null;
    }

    /** Indicate whether this actor is ready to fire. The actor is ready to
     *  fire whenever there is interrupt event or if it's not busy and
     *  there is input event.
     *  <p>
     *  Note that this method is called by director due to either of 
     *  the following three reasons: event in input port, 
     *  event in interrut port, or a pure event (produced by fireAfterDelay).
     *  @return true is ready to fire, false otherwise.
     */
    public boolean prefire() throws IllegalActionException {
        boolean busy = _busyUntil > getCurrentTime();

        if (busy) {
            // if it's busy, only interrupt event is processed.
            if (interrupt.hasToken(0)) {
                return true;
            } else {
                return false;
            }
        } else {
            // if it's not busy, then admit everything.
            return true;
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // the intial value and increment
    private boolean _firstInput = true;
    private double _doneTime = 0.0;
    private double _busyUntil = Double.NEGATIVE_INFINITY;
    private Token _tokenBeingServed = null;

    // the ports.
    public DEIOPort output;
    public DEIOPort input;
    public DEIOPort interrupt;

    // Actor's parameters.
    private Parameter _minimumServiceTime;
    private Parameter _interruptServiceTime;
}
