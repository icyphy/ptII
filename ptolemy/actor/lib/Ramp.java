/* An actor that outputs a sequence with a given step in values.

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
Produce an output token on each firing with a value that is
incremented by the specified step each time the actor fires. The
first output and the step value are given by parameters.
The type of the output is inferred from the types of the parameters
at the time that the actor is initialized. The type of the output
is the type of the result of adding the two parameters.
Thus, this actor is
polymorphic in the sense that its output data type can be that
of any token type that supports addition.

@author Yuhong Xiong, Edward A. Lee
@version $Id$
*/

public class Ramp extends TypedAtomicActor {

    /** Construct a Ramp with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Ramp(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);

        init = new Parameter(this, "init");
        step = new Parameter(this, "step");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The output port. */
    public TypedIOPort output;

    /** The value produced by the ramp on its first firing.
     *  This parameter contains a DoubleToken, initially with value 0.
     */
    public Parameter init;

    /** The amount by which the ramp output is incremented on each firing.
     *  This parameter contains a DoubleToken, initially with value 1.
     */
    public Parameter step;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            Ramp newobj = (Ramp)super.clone(ws);
            newobj.output = new TypedIOPort(this, "output", false, true);
            newobj.init = new Parameter(this, "init");
            newobj.step = new Parameter(this, "step");
            return newobj;
        } catch (KernelException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Internal error: " + ex.getMessage());
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Infer the type of the output from the types of the parameters.
     *  If the parameters have not been set, then they are set
     *  to type DoubleToken with value 0.0 for init and 1.0 for step.
     *  @exception IllegalActionException If the parameters cannot be
     *   added together.
     */
    public void initialize() throws IllegalActionException {

	_stateToken = init.getToken();
        if(_stateToken == null) {
            _stateToken = new DoubleToken(0.0);
            init.setToken(_stateToken);
        }

        Token stepToken = step.getToken();
        if(stepToken == null) {
            stepToken = new DoubleToken(1.0);
            step.setToken(stepToken);
        }

	// Add up the value and step parameters and use the type of
	// the result token as the type of the output port.
	Class type = (_stateToken.add(stepToken)).getClass();
        output.setDeclaredType(type);
    }

    /** Send out the next ramp output.
     */
    public void fire() {
        try {
            output.broadcast(_stateToken);
        } catch (IllegalActionException ex) {
            // Should not be thrown because this is an output port.
            throw new InternalErrorException(ex.getMessage());
        }
    }

    /** Update the state.
     */
    public boolean postfire() {
        try {
            _stateToken = _stateToken.add(step.getToken());
        } catch (IllegalActionException ex) {
            // Should not be thrown because
            // we have already verified that the tokens can be added.
            throw new InternalErrorException(ex.getMessage());
        }
        // This actor never requests termination.
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Token _stateToken = null;
}

