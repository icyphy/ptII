/* A DE actor that generate events at regular intervals, starting at time zero.

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
//// DEClock
/**
Generate events at regular intervals, starting at time zero.

@author Lukito Muliadi, Edward A. Lee
@version $Id$
*/
public class DEClock extends AtomicActor {

    /** Construct a clock that generates events with the specified values
     *  at the specified interval.
     *  @param container The container.
     *  @param name The name of the actor.
     *  @param value The value of the output.
     *  @param interval The interval between clock ticks.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    // FIXME: The value should be an attribute, as should the interval.
    // FIXME: Should the value be a double? Probably not...
    public DEClock(CompositeActor container, String name,
            double value, double interval)
            throws IllegalActionException, NameDuplicationException  {
        super(container, name);
        output = new IOPort(this, "output", false, true);
        _interval = interval;
        _value = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Produce the initializer event that will cause the generation of
     *  the first output at time zero.
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
        dir.enqueueEvent(this, 0.0, 0);
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
        dir.enqueueEvent(this, _interval, 0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    // The output port.
    public IOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // the interval between events
    private double _interval;
    // the output value.
    private double _value;
}
