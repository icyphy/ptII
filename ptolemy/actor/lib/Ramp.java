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

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// Ramp
/**
An actor that produces output token with monotonically increasing value.
The value of the output event starts at <code>value</code> and increases
by <code>step</code> each time the actor fires.

@author Yuhong Xiong
@version $Id$
*/

public class Ramp extends TypedAtomicActor {

    /** Constructor.
     *  @param container The container.
     *  @param name The name of this actor.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Ramp(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        _initValue = new Parameter(this, "Value", new IntToken(0));
        _step = new Parameter(this, "Step", new IntToken(1));
        _output = new TypedIOPort(this, "Output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Evaluate the value and step size parameters and set the
     *  declared type.
     *  @exception IllegalActionException Not thrown in this class.
     */
    // FIXME: this might not work if user change the parameter during
    // simulation.
    public void initialize()
	    throws IllegalActionException {

	_stateToken = _initValue.getToken();

	// Add up the value and step parameters and use the type of
	// the result token as the type of the output port.
	Class type = (_stateToken.add(_step.getToken())).getClass();
        _output.setDeclaredType(type);
    }

    /** Send out the next ramp output.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void fire()
	    throws IllegalActionException {
        _output.broadcast(_stateToken);
	_stateToken = _stateToken.add(_step.getToken());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Parameter _initValue = null;
    private Parameter _step = null;
    private Token _stateToken = null;

    private TypedIOPort _output;
}

