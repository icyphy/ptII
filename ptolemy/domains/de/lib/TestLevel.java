/* A DE star.

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
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// TestLevel
/**
Detect threshold crossings if the crossingsOnly parameter is TRUE. Otherwise,
it simply compares the input agains the "threshold".
If crossingsOnly is TRUE, then .....
FIXME: copy from the manual

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class TestLevel extends AtomicActor {
    /** Construct a DERamp star.
     *
     * @param value The initial output event value.
     * @param step The step size by which to increase the output event values.
     * @param container The composite actor that this actor belongs too.
     * @param name The name of this actor.
     *
     * @exception NameDuplicationException Other star already had this name
     * @exception IllegalActionException internal problem
     */
    public TestLevel(boolean crossingsOnly, double threshold,
            CompositeActor container,
            String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // create an output port
        output = new IOPort(this, "output", false, true);
        // create an input port
        input = new IOPort(this, "input", true, false);
        // set the parameters
        _crossingsOnly = crossingsOnly;
        _threshold = threshold;
	_prev = threshold;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Produce the next event at _interval unit-time aparts.
     *
     * @exception CloneNotSupportedException Error when cloning event.
     * @exception IllegalActionException Not thrown in this class.
     */
    public void fire() throws CloneNotSupportedException, IllegalActionException{
	// get the input token from the input port.
        double inputValue;
        try {
            inputValue = ((DoubleToken)(input.get(0))).getValue();
        } catch (NoSuchItemException e) {
            // this can't happen
            throw new InvalidStateException("Bug in DERamp.fire()");
        }

        // produce the output token.

	if (_crossingsOnly) {
	    if (_prev <= _threshold && inputValue > _threshold) {
		output.broadcast(new DoubleToken(1.0));
	    } else if (_prev >= _threshold && inputValue < _threshold) {
		output.broadcast(new DoubleToken(0.0));
	    }
	    _prev = inputValue;

	} else {
	    if (inputValue > _threshold) {
		output.broadcast(new DoubleToken(1.0));
	    } else {
		output.broadcast(new DoubleToken(0.0));
	    }
	    _prev = inputValue;
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // the intial value and increment
    private double _prev;
    private boolean _crossingsOnly;
    private double _threshold;

    // the ports.
    public IOPort output;
    public IOPort input;
}










