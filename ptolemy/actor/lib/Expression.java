/* An actor that evaluates expressions.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.ScopeExtender;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.*;

import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


//////////////////////////////////////////////////////////////////////////
//// Expression
/**
On each firing, evaluate an expression that may include references
to the inputs, current time, and a count of the firing.  The ports are
referenced by the variables that have the same name as the port.
To use this class, instantiate it, then add ports (instances of TypedIOPort).
In vergil, you can add ports by right clicking on the icon and selecting
"Configure Ports".  In MoML you can add ports by just including ports
of class TypedIOPort, set to be inputs, as in the following example:
<p>
<pre>
   &lt;entity name="exp" class="ptolemy.actor.lib.Expression"&gt;
      &lt;port name="in" class="ptolemy.actor.TypedIOPort"&gt;
          &lt;property name="input"/&gt;
      &lt;/port&gt;
   &lt;/entity&gt;
</pre>
<p>
The type is polymorphic, with the only constraint that the
types of the inputs must all be less than (in the type order)
the type of the output.  What this means (loosely) is that
the types of the input tokens can be converted losslessly into
tokens with the type of the output.
<p>
The <i>expression</i> parameter specifies an expression that can
refer to the inputs by name.  By default, the expression
is empty, and attempting
to execute the actor without setting it triggers an exception.
<p>
The expression language understood by this actor is the same as
<a href="../../../../expression.htm">that used to set any parameter value</a>,
with the exception that
the expressions evaluated by this actor can refer to the values
of inputs, and to the current
time by the variable name "time", and to the current iteration count
by the variable named "iteration."
<p>
This actor can be used instead of many of the arithmetic actors,
such as AddSubtract, MultiplyDivide, and TrigFunction.  However, those actors
will be usually be more efficient, and sometimes more convenient to use.
<p>
NOTE: There are a number of limitations in the current implementation.
First, the type constraints on the ports are the default, that input
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

@author Xiaojun Liu, Edward A. Lee, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
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
    public Expression(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);
        expression = new StringAttribute(this, "expression");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port. */
    public TypedIOPort output;

    /** The parameter that is evaluated to produce the output.
     *  Typically, this parameter evaluates an expression involving
     *  the inputs.
     */
    public StringAttribute expression;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in the value of an attribute. 
     *  @param attribute The attribute whose type changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if(attribute == expression) {
            _parseTree = null;
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */

    public Object clone(Workspace workspace)
 	    throws CloneNotSupportedException {
        Expression newObject = (Expression)super.clone(workspace);
        newObject._iterationCount = 1;
        return newObject;
    }

    /** Evaluate the expression and send its result to the output.
     *  @exception IllegalActionException If the evaluation of the expression
     *   triggers it, or the evaluation yields a null result, or the evaluation
     *   yields an incompatible type, or if there is no director.
     */
    public void fire() throws IllegalActionException {
        Iterator inputPorts = inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort port = (IOPort)(inputPorts.next());
            // FIXME: Handle multiports
            if (port.getWidth() > 0) {
                if (port.hasToken(0)) {
                    Token inputToken = port.get(0);
                    _tokenMap.put(port.getName(), inputToken);
                }
            }
        }
        Token result;
        try {
            if(_parseTree == null) {
                PtParser parser = new PtParser();
                _parseTree = parser.generateParseTree(
                        expression.getExpression());
            }
            if(_parseTreeEvaluator == null) {
                _parseTreeEvaluator = new ParseTreeEvaluator();
            }
            if(_scope == null) {
                _scope = new VariableScope();
            }
            result = _parseTreeEvaluator.evaluateParseTree(
                    _parseTree, _scope);
        } catch (IllegalActionException ex) {
            // Chain exceptions to get the actor that threw the exception.
            throw new IllegalActionException(this, ex, "Expression invalid.");
        }

        if (result == null) {
            throw new IllegalActionException(this,
                    "Expression yields a null result: " +
                    expression.getExpression());
        }
        output.send(0, result);
    }

    /** Initialize the iteration count to 1.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iterationCount = 1;
        _tokenMap = new HashMap();
    }

    /** Increment the iteration count.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean postfire() throws IllegalActionException {
        _iterationCount++;
        // This actor never requests termination.
        return true;
    }

    private class VariableScope implements ParserScope {

        /** Look up and return the attribute with the specified name in the
         *  scope. Return null if such an attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         */
        public Token get(String name) throws IllegalActionException {
            Variable result = null;

            if(name.equals("time")) {
                return new DoubleToken(getDirector().getCurrentTime());
            } else if(name.equals("iteration")) {
                return new IntToken(_iterationCount);
            }

            Token token = (Token)_tokenMap.get(name);
            if(token != null) {
                return token;
            }
                     
            NamedObj container = (NamedObj)Expression.this;
            while (container != null) {
                result = _searchIn(container, name);
                if (result != null) {
                    return result.getToken();
                } else {
                    container = (NamedObj)container.getContainer();
                }
            }
            return null;
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         */
        public Type getType(String name) throws IllegalActionException {
            Variable result = null;

            if(name.equals("time")) {
                return BaseType.DOUBLE;
            } else if(name.equals("iteration")) {
                return BaseType.INT;
            }

            // Check the port names.
            TypedIOPort port = (TypedIOPort)getPort(name);
            if(port != null) {
                return port.getType();
            }
                     
            NamedObj container = (NamedObj)Expression.this;
            while (container != null) {
                result = _searchIn(container, name);
                if (result != null) {
                    return result.getType();
                } else {
                    container = (NamedObj)container.getContainer();
                }
            }
            return null;
        }

        /** Return the list of attributes within the scope.
         *  @return The list of attributes within the scope.
         */
        public NamedList variableList() {
            return null;
        }

        // Search in the container for an attribute with the given name.
        // Search recursively in any instance of ScopeExtender in the
        // container.
        private Variable _searchIn(NamedObj container, String name) {
            Attribute result = container.getAttribute(name);
            if (result != null && result instanceof Variable)
                return (Variable)result;
            Iterator extenders =
                    container.attributeList(ScopeExtender.class).iterator();
            while (extenders.hasNext()) {
                ScopeExtender extender = (ScopeExtender)extenders.next();
                result = extender.getAttribute(name);
                if (result != null && result instanceof Variable)
                    return (Variable)result;
            }
            return null;
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _iterationCount = 1;
    private ParseTreeEvaluator _parseTreeEvaluator = null;
    private ASTPtRootNode _parseTree = null;
    private VariableScope _scope = new VariableScope();
    private Map _tokenMap;
}
