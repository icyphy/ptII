/* An actor that evaluates expressions.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.PassiveVariable;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Expression
/**
Evaluate an expression that may include references to the inputs,
current time, and a count of the firing.  The inputs are
referenced by the variables input<i>n</i>, where <i>n</i>
ranges from zero to the width of the input port minus one.
The type is polymorphic, with the only constaint that the
types of the inputs must all be less than (in the type order)
the type of the output.  What this means (loosely) is that
the types of the input tokens can be converted losslessly into
tokens with the type of the output.

FIXME: Rework above.  Inputs must be of type TypedIOPort.

@author Xiaojun Liu, Edward A. Lee
@version $Id$
*/

public class Expression extends TypedAtomicActor {

    // FIXME: This class won't deal with mutations properly.

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Expression(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);
        // FIXME: The following should not be done... The type should
        // be inferred, ideally...
        output.setDeclaredType(DoubleToken.class);

        expression = new Parameter(this, "expression", new StringToken(""));

        _time = new PassiveVariable(this, "time", new DoubleToken(0.0));
        _firing = new PassiveVariable(this, "firing", new IntToken(0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The output port. */
    public TypedIOPort output;

    /** The value produced by the ramp on its first firing.
     *  This parameter contains a DoubleToken, initially with value 0.
     */
    public Parameter expression;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            _firingCount = 1;
            Expression newobj = (Expression)super.clone(ws);
            newobj.output = new TypedIOPort(this, "output", false, true);
            expression = new Parameter(this, "expression", new StringToken(""));
            newobj._time =
                new PassiveVariable(this, "time", new DoubleToken(0.0));
            newobj._firing =
                new PassiveVariable(this, "firing", new IntToken(0));
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

    /** Set up the list of variables within the scope of the expression.
     *  All input ports are included, plus the <i>time</i> and
     *  <i>firing</i> variables.  If any input bears the name <i>time</i>
     *  or <i>firing</i>, then it will shadow these variables.
     */
    public void initialize() {
        // NOTE: There is lots of try...catch in this code.
        // One of these, IllegalActionException, could be passed up instead.
        _variables = new NamedList();
        Enumeration inputPorts = inputPorts();
        try {
            while(inputPorts.hasMoreElements()) {
                TypedIOPort port = (TypedIOPort)(inputPorts.nextElement());
                // FIXME: deal with multiports.
                try {
                    // Have to remove any attribute with the name of the port
                    // first.
                    String portname = port.getName();
                    Attribute attr = getAttribute(portname);
                    if (attr != null) {
                        attr.setContainer(null);
                    }
                    PassiveVariable pvar =
                        new PassiveVariable(this, portname);
                    _variables.prepend(pvar);
                } catch (IllegalActionException ex) {
                    // Not expected because a variable can be added to this
                    // container.
                    throw new InternalErrorException(ex.getMessage());
                }
            }
        } catch (NameDuplicationException ex) {
            // Can't occur because input names are unique.
            throw new InternalErrorException(ex.getMessage());
        }
        try {
            try {
                _variables.prepend(_time);
            } catch (NameDuplicationException ex) {
                // Ignore to get shadowing.
            }
            try {
                _variables.prepend(_firing);
            } catch (NameDuplicationException ex) {
                // Ignore to get shadowing.
            }
            Attribute attr = getAttribute("_expression");
            try {
                if (attr != null) {
                    attr.setContainer(null);
                }
                _expression = new PassiveVariable(this, "_expression");
            } catch (NameDuplicationException ex) {
                // Can't occur, since we just removed it...
                throw new InternalErrorException(ex.getMessage());
            }
        } catch (IllegalActionException ex) {
            // Can't occur since these variables have names.
            throw new InternalErrorException(ex.getMessage());
        }
        String expr = ((StringToken)(expression.getToken())).stringValue();
        _expression.setExpression(expr);
        _expression.addToScope(_variables);
    }

    /** Evaluate the expression and broadcast its result to the output.
     *  @exception IllegalActionException If the evaluation of the expression
     *   triggers is.
     */
    public void fire() throws IllegalActionException {
        Director dir = getDirector();
        _time.setToken(new DoubleToken(dir.getCurrentTime()));
        _expression.evaluate();
        output.broadcast(_expression.getToken());
    }

    /** Update the state.
     */
    public boolean postfire() {
        _firing.setToken(new IntToken(_firingCount++));
        // This actor never requests termination.
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private NamedList _variables;
    private PassiveVariable _time, _firing, _expression;
    private int _firingCount = 1;
}

