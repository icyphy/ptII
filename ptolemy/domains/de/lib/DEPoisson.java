/* An actor that generates events according to Poisson process.

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
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEPoisson
/**
Generate events according to Poisson process. The first event is
always at time zero. The mean inter-arrival time and value of the
events are given as parameters.

@author Lukito Muliadi
@version $Id$
*/
public class DEPoisson extends AtomicActor {

    /** Constructor.
     *  @param container The composite actor that this actor belongs to.
     *  @param name The name of this actor.
     *  @param value The value of the output events.
     *  @param lambda The mean of the inter-arrival times.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEPoisson(CompositeActor container,
            String name, double value, double lambda)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        output = new IOPort(this, "output", false, true);
        _lambda = lambda;
        _value = value;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Produce the initializer event that will cause the generation of
     *  the first event at time zero.
     *  @exception CloneNotSupportedException If the base class throws it.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize()
            throws CloneNotSupportedException, IllegalActionException {
        // FIXME: This should be just DEDirector
        // FIXME: This class should be derived from DEActor, which should
        // ensure that this cast is valid.
        super.initialize();
        DECQDirector dir = (DECQDirector)getDirector();
        dir.enqueueEvent(this,0.0,0);
    }

    /** Produce an output event at the current time, and then schedule
     *  a firing in the future.
     *  @exception CloneNotSupportedException If there is more than one
     *   destination and the output token cannot be cloned.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire()
            throws CloneNotSupportedException, IllegalActionException {
        DECQDirector dir = (DECQDirector)getDirector();
        output.broadcast(new DoubleToken(_value));

        // compute an exponential random variable.
        double exp = -Math.log((1-Math.random()))*_lambda;
        dir.enqueueEvent(this,exp,0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public IOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // the mean inter-arrival time and value
    private double _lambda = 1.0;
    private double _value = 1.0;
}











