/* A DE star that generate events according to Poisson process.

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
//// DEPoisson
/** 
An actor that generate events according to Poisson process. The first event is
generated at time zero. The mean inter-arrival time and magnitude of the 
events are given as parameters.

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class DEPoisson extends AtomicActor {
    /** Construct a DEPoisson star.
     *
     * @param lambda The mean of the inter-arrival times.
     * @param magnitude The magnitude of the output events.
     * @param container The composite actor that this actor belongs to.
     * @param name The name of this actor.
     * @exception NameDuplicationException Other star already had this name
     * @exception IllegalActionException internal problem
     */	
    public DEPoisson(double lambda, 
            double magnitude, 
            CompositeActor container, 
            String name) throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // create an output port
        output = new IOPort(this, "output", false, true);
        // create a self loop
        _loopIn = new IOPort(this, "loop input" , true , false);
        _loopOut = new IOPort(this, "loop output", false, true );
        // now connect loopIn and loopOut
        _loopRelation = new IORelation(container, "loop relation");
        _loopIn.link(_loopRelation);
        _loopOut.link(_loopRelation);
        // set the interval between events.
        _lambda = lambda;
        _magnitude = magnitude;

    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Produce the initializer event which will cause the generation of
     *  the first event at time zero.
     *
     *  @exception CloneNotSupportedException Not thrown in this base class.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void initialize() throws CloneNotSupportedException, IllegalActionException {
        // The initializer event is at time zero.
        DEToken initEvent = new DEToken(_magnitude, new DETag(0.0,0));
        // Send out via the self loop output port.
        _loopOut.send(0, initEvent);
        
    }

    /** Produce the next event T time unit in the future, where T is a random
     *  variable with exponential distribution.
     * 
     * @exception CloneNotSupportedException Error when cloning event.
     * @exception IllegalActionException Not thrown in this class.
     */	
    public void fire() throws CloneNotSupportedException, IllegalActionException{
        System.out.println("Firing DEPoisson");
        // get the input token from the self loop input port.
        DEToken inputToken;
        try {
            inputToken = (DEToken)(_loopIn.get(0));
        } catch (NoSuchItemException e) {
            // this can't happen
            throw new InvalidStateException("Bug in DEPoisson.fire()");
        }

        // compute T
        double T = -Math.log((1-Math.random()))*_lambda;

        // produce the output token which is delayed by _interval.
        
        DETag b = (DETag)(inputToken.getTag());
        DEToken outputToken;
        if (_firstTime) {
            outputToken = new DEToken(_magnitude, b.increment(0,0));
            _firstTime = false;
        } else {
            outputToken = new DEToken(_magnitude, b.increment(T,0));
        }
    
        // send the output token via _loopOut and output IOPort.
        _loopOut.send(0, outputToken);
        output.broadcast((Token)outputToken.clone());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // the mean inter-arrival time and magnitude
    private double _lambda = 1.0;
    private double _magnitude = 1.0;

    // an aux variable to make sure we have the first event at time zero.
    private boolean _firstTime = true;
    
    // the ports.
    public IOPort output;
    private IOPort _loopIn;
    private IOPort _loopOut;
    private IORelation _loopRelation;
}










