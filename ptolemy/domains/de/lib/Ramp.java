/* An actor that outputs monotonically increasing values.

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
//// Ramp
/**
An actor that produces an output event with a monotonically increasing value
when stimulated by an input event. The value of the output event starts at
<code>value</code> and increases by <code>step</code> each time the actor
fires.

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class Ramp extends AtomicActor {

    /** Constructor.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @param value The initial output event value.
     *  @param step The step size by which to increase the output event values.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Ramp(CompositeActor container, String name,
            double value, double step)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // create an output port
        output = new IOPort(this, "output", false, true);
        // create an input port
        input = new IOPort(this, "input", true, false);
        // set the interval between events.
        _value = value;
        _step = step;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Produce the next ramp output with the same time stamp as the current
     *  input.
     *  FIXME: better exception tags needed.
     *  @exception CloneNotSupportedException Error when cloning event.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void fire()
            throws CloneNotSupportedException, IllegalActionException {
	// get the input token from the input port.
        DoubleToken inputToken;
        try {
            inputToken = (DoubleToken)(input.get(0));
        } catch (NoSuchItemException e) {
            // this can't happen
            throw new InternalErrorException(
                "DERamp actor fired with no input.");
        }
        // produce the output token.
        DoubleToken outputToken = new DoubleToken(_value);
        _value += _step;
        output.broadcast(outputToken);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // the intial value and increment
    private double _value;
    private double _step;

    // the ports.
    public IOPort output;
    public IOPort input;
}
