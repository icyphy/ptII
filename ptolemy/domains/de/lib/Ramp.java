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
import ptolemy.data.expr.Parameter;
import ptolemy.graph.*;
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
public class Ramp extends TypedAtomicActor {

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
    public Ramp(TypedCompositeActor container, String name,
            double value, double step)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // set the parameters.
        _value = new Parameter(this, "value", new DoubleToken(value));
        _step = new Parameter(this, "step", new DoubleToken(step));
        // create an output port
        output = new TypedIOPort(this, "output", false, true);
        // create an input port
        input = new TypedIOPort(this, "input", true, false);
        input.setDeclaredType(Token.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Describe me
     */
    public void initialize() throws IllegalActionException {

        Class valueClass = _value.getToken().getClass();
        Class stepClass = _value.getToken().getClass();
        int compare = TypeLattice.compare(valueClass, stepClass);
        // FIXME: this might not work if user change the parameter during
        // simulation.
        if (compare == CPO.INCOMPARABLE) {
            throw new InvalidStateException("Bad parameter type in Ramp.initialize()");
        }
        if (compare == CPO.LOWER) {
            output.setDeclaredType(stepClass);
        } else {
            output.setDeclaredType(valueClass);
        }
        _stateToken = _value.getToken();
    }



    /** Produce the next ramp output with the same time stamp as the current
     *  input.
     *  FIXME: better exception tags needed.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void fire() throws IllegalActionException {
        // output the state token.
        while (input.hasToken(0)) {
            input.get(0);
            output.broadcast(_stateToken);
            
            // increment the state.
            _stateToken = _stateToken.add(_step.getToken());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // the intial value and increment
    private Token _stateToken;
    private Parameter _value;
    private Parameter _step;

    // the ports.
    public TypedIOPort output;
    public TypedIOPort input;
}
