/* An attemp for a polymorphic actor.

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
//// DEPassGate
/** If the gate is open, then particles pass from "input" to "output."
 *  When the gate is closed, no outputs are produced. If input particles
 *  arrive while the gate is closed, the most recent one will be passed to
 *  "output" when the gate is reopened.

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class DEPassGate extends AtomicActor {
    /** Construct a DEPassGate star.
     *
     * @param container The composite actor that this actor belongs too.
     * @param name The name of this actor.
     *
     * @exception NameDuplicationException Other star already had this name
     * @exception IllegalActionException internal problem
     */
    public DEPassGate(CompositeActor container,
            String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // create an output port
        output = new DEIOPort(this, "output", false, true);

        // create input ports
        input = new DEIOPort(this, "data input", true, false);
        gate = new DEIOPort(this, "gate input", true, false);

	// Assert priorities
        gate.before(input);
        input.triggers(output);
	gate.triggers(output);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** If there's an event in the "input" port, pass it depending on
     *  the "gate" input; if there's no event in the input port, but only
     *  in the "gate" input, pass event when the gate reopens.
     *
     * @exception CloneNotSupportedException Error when cloning event.
     * @exception IllegalActionException Not thrown in this class.
     */
    public void fire()
            throws CloneNotSupportedException, IllegalActionException{
        // Check if there's an event in the "input" port.
        if (input.hasToken(0)) {
            DoubleToken inputToken = null;
            try {
                inputToken = (DoubleToken)(input.get(0));
            } catch (NoSuchItemException e) {
                // Can't occur.
                throw new InvalidStateException("Check DEPassGate.fire()"+
                        " for bug." + e.getMessage());
            }
	    // If the gate also has token, change the state of the gate.
            if (gate.hasToken(0)) {
		DoubleToken gateToken = null;
                try {
		    gateToken=(DoubleToken)(gate.get(0));
                } catch (NoSuchItemException e) {
                    // Can't occur
                    throw new InvalidStateException("Check DEPassGate.fire()"+
                            " for bug (2)" + e.getMessage());
                }
		if (gateToken.getValue() == 0.0) {
		    // gate is closing (or closed)
		    _gateOpen = false;

		} else {
		    _gateOpen = true;
		}
            }

	    // pass the token if the gate is open
	    if (_gateOpen) {
		output.broadcast(inputToken);
		return;
	    } else {
		// record the value.
		_lastToken = inputToken;
	    }
	} else if (gate.hasToken(0)) {
	    // No token on input, only on gate.
	    DoubleToken gateToken = null;
	    try {
                gateToken = (DoubleToken)(gate.get(0));
            } catch (NoSuchItemException e) {
                // Can't occur.
                throw new IllegalStateException("Check DEPassGate.fire() "+
                        "for bug (3)" + e.getMessage());

            }
	    if (gateToken.getValue() != 0.0 && _gateOpen == false) {
		// gate just reoopened.
		_gateOpen = true;
		output.broadcast(_lastToken);
		return;
	    } else if (gateToken.getValue() == 0 && _gateOpen == true) {
		_gateOpen = false;
		return;
	    }
        } else {
            // if both inputs are empty, then the scheduler is wrong.
            throw new InvalidStateException("DEPassGate.fire(), "+
                    "bad scheduling");
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // the state of the gate
    private boolean _gateOpen = true;

    // the last token seen in the input port.
    private DoubleToken _lastToken = null;

    // the ports.
    public DEIOPort output;
    public DEIOPort input;
    public DEIOPort gate;
}






