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
import ptolemy.kernel.Port;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Expression
/**
On each firing, evaluate an expression that may include references
to the inputs, current time, and a count of the firing.  The ports are
referenced by the variables that have the same name as the port.
To use this class, instantiate it, then add ports (instances of TypedIOPort).
The type is polymorphic, with the only constraint that the
types of the inputs must all be less than (in the type order)
the type of the output.  What this means (loosely) is that
the types of the input tokens can be converted losslessly into
tokens with the type of the output.
<p>
The <i>expression</i> parameter should be set using its
setExpression() method.  By default, it is empty, and attempting
to execute the actor without setting it triggers an exception.
<p>
NOTE: There are a number of limitations in the current implementation.
First, the type constaints on the ports are the default, that input
ports must have type that be losslessly converted to the type of the
output.  The type constraints have nothing to do with the expression.
This is a severe limitation, but removing it depends on certain
extensions to the Ptolemy II type system which are in progress.
Second, multiports are not supported. Also, if name duplications occur,
for example if a parameter and a port have the same name, then
the results are unpredictable.  They will depend on the order
in which things are defined, which may not be the same in the
constructor as in the clone method.  This class attempts to
detect name duplications and throw an exception.

@author Xiaojun Liu, Edward A. Lee
@version $Id$
*/

public class Expression extends TypedAtomicActor {

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
        expression = new Parameter(this, "expression");
        _time = new Variable(this, "time", new DoubleToken(0.0));
        _firing = new Variable(this, "firing", new IntToken(1));
        // FIXME: This has to be initialized with a token or type resolution
        // fails.
        _expression = new Variable(this, "_expression", new IntToken(1));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ports and parameters              ////

    /** The output port. */
    public TypedIOPort output;

    /** The value produced by the ramp on its first firing.
     *  This parameter contains a DoubleToken, initially with value 0.
     */
    public Parameter expression;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to allow arbitrary type changes
     *  for the variables and parameters.
     */
    public void attributeTypeChanged(Attribute attribute) {
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            Expression newobj = (Expression)super.clone(ws);
            newobj._firingCount = 1;
            newobj.output = (TypedIOPort)newobj.getPort("output");
            expression = (Parameter)newobj.getAttribute("expression");
	    newobj._time = (Variable)newobj.getAttribute("time");
	    newobj._firing = (Variable)newobj.getAttribute("firing");
	    newobj._expression = (Variable)newobj.getAttribute("_expression");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Initialize the firing count to 1.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _firingCount = 1;
    }

    /** Evaluate the expression and broadcast its result to the output.
     *  @exception IllegalActionException If the evaluation of the expression
     *   triggers it, or the evaluation yields a null result, or the evaluation
     *   yields an incompatible type, or if there is no director.
     */
    public void fire() throws IllegalActionException {
        // NOTE: It is important the expr not be evaluated, since
        // the variables it refers to are not in its scope.  Thus,
        // we copy the expression into a variable.
        String expr = expression.getExpression();
        _expression.setExpression(expr);
        Director dir = getDirector();
        if (dir == null) {
            throw new IllegalActionException(this, "No director!");
        }
        _time.setToken(new DoubleToken(dir.getCurrentTime()));
        Enumeration inputPorts = inputPorts();
        while(inputPorts.hasMoreElements()) {
            IOPort port = (IOPort)(inputPorts.nextElement());
            // FIXME: Handle multiports
            if(port.hasToken(0)) {
                Token inputToken = port.get(0);
                Variable var =
                    (Variable)(getAttribute(port.getName()));
                var.setToken(inputToken);
            }
        }
        Token result = _expression.getToken();
        if (result == null) {
            throw new IllegalActionException(this,
            "Expression yields a null result: " + _expression.getExpression());
        }
        output.broadcast(result);
    }

    /** Increment the firing count.
     *  @IllegalActionException If the superclass throws it.
     */
    public boolean postfire() throws IllegalActionException {
        _firingCount++;
        _firing.setToken(new IntToken(_firingCount));
        // This actor never requests termination.
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to create a variable with the same name
     *  as the port.  If the port is an input, then the variable serves
     *  as a repository for received tokens.  If it is an output, then
     *  the variable contains the most recently transmitted token.
     *  @param port The port being added.
     *  @exception IllegalActionException If the port has no name, or
     *   if the variable is rejected for some reason, or if the port
     *   is not a TypedIOPort.
     *  @exception NameDuplicationException If the port name collides with a
     *   name already in the entity.
     */
    protected void _addPort(Port port)
            throws IllegalActionException, NameDuplicationException {
        if (!(port instanceof TypedIOPort)) {
            throw new IllegalActionException(this,
            "Cannot add an input port that is not a TypedIOPort: "
            + port.getName());
        }
        super._addPort(port);
        String portName = port.getName();
        // The new variable goes on the list of attributes, unless it is
        // already there.
        Attribute there = getAttribute(portName);
        if (there == null) {
            // FIXME: Have to initialize with a token or type
            // resolution failse.
            new Variable(this, portName, new IntToken(1));
        } else if ((there instanceof Parameter)
                || !(there instanceof Variable)) {
            throw new IllegalActionException(this, "Port name collides with"
            + " another attribute name: " + portName);
        }
        // NOTE: We assume that if there is already a variable with
        // this name then that is the variable we are intended to use.
        // The variable will already be there, for example, if the
        // actor was created through cloning.
    }

    /** Override the base class to remove the variable with the same name
     *  as the port.
     *  @param port The port being removed.
     */
    protected void _removePort(Port port) {
        super._removePort(port);
        String portName = port.getName();
        Attribute attribute = getAttribute(portName);
        if (attribute instanceof Variable) {
            try {
                attribute.setContainer(null);
            } catch (KernelException ex) {
                throw new InternalErrorException(ex.getMessage());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Variable _time;
    private Variable _firing;
    private Variable _expression;
    private int _firingCount = 1;
}
