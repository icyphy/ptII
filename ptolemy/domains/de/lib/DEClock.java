/* A DE star that generate events at regular intervals, starting at time zero.

 Copyright (c) 1997- The Regents of the University of California.
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
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEClock
/** 
An actor that generate events at regular intervals, starting at time zero.
The actor has 2 output ports and 1 input ports. One input port and one
output port is connected together forming a loop. The loop is used by the
actor to schedule itself in the future.

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class DEClock extends AtomicActor {
    /** Construct the DEClock star.
     *  The default output value is 0.
     * @see CTActor#CTActor()
     * @param container The CTSubSystem this star belongs to
     * @param name 
     * @exception NameDuplicationException Other star already had this name
     * @exception IllegalActionException internal problem
     */	
    public DEClock(double interval, CompositeActor container, String name) 
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // create an output port
        output = new IOPort(this, "output", false, true);
        // create a self loop
        _loopIn = new IOPort(this, "loop input" , true , false);
        _loopOut = new IOPort(this, "loop output", false, true );
        // now connect loopIn and loopOut
        _loopRelation = new IORelation(container, name+" loop relation");
        _loopIn.link(_loopRelation);
        _loopOut.link(_loopRelation);
        // set the interval between events.
        _interval = interval;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Produce the initializer event which will cause the generation of
     *  the first event at time zero.
     *
     *  @exception CloneNotSupportedException Not thrown in this base class.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void initialize() 
            throws CloneNotSupportedException, IllegalActionException {
        // The initializer event is at _interval behind time zero.
        DEToken initEvent = new DEToken(1.0, new DETag(0.0-_interval,0));
        // Send out via the self loop output port.
        _loopOut.send(0, initEvent);
        
    }

    /** Produce the next event at _interval unit-time aparts.
     * 
     * @exception CloneNotSupportedException Error when cloning event.
     * @exception IllegalActionException Not thrown in this class.
     */	
    public void fire() throws CloneNotSupportedException, IllegalActionException{
        System.out.println("Firing DEClock");
        // get the input token from the self loop input port.
        DEToken inputToken;
        try {
            inputToken = (DEToken)(_loopIn.get(0));
        } catch (NoSuchItemException e) {
            // this can't happen
            throw new InvalidStateException("Bug in DEClock.fire()");
        }
        
        // produce the output token which is delayed by _interval.
        double a = inputToken.getValue();
        DETag b = (DETag)(inputToken.getTag());
        DEToken outputToken = new DEToken(a, b.increment(_interval,0));
    
        // send the output token via _loopOut and output IOPort.
        _loopOut.send(0, outputToken);
        output.broadcast((Token)outputToken.clone());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // the interval between events
    private double _interval;

    // the ports
    public IOPort output;
    private IOPort _loopIn;
    private IOPort _loopOut;
    private IORelation _loopRelation;
}
