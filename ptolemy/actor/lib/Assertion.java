/* An actor that does assertion.

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

@ProposedRating Red (hyzheng@eecs.berkeley.edu)
@AcceptedRating Red (hyzheng@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParseTreeTypeInference;
import ptolemy.data.expr.ParseTreeFreeVariableCollector;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.ScopeExtender;
import ptolemy.data.expr.Variable;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.*;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// Assertion
/**
The Assertion actor evaluates an assertion that includes references
to the inputs. The ports are referenced by the variables that have 
the same name as the port. If the assertion is satisfied, nothing 
happens. If not, an exception will be thrown.
To use this class, instantiate it, then add ports (instances of TypedIOPort).
In vergil, you can add ports by right clicking on the icon and selecting
"Configure Ports".  In MoML you can add ports by just including ports
of class TypedIOPort, set to be inputs, as in the following example:
<p>
<pre>
   &lt;entity name="assertion" class="ptolemy.actor.lib.Assertion"&gt;
      &lt;port name="in" class="ptolemy.actor.TypedIOPort"&gt;
          &lt;property name="input"/&gt;
      &lt;/port&gt;
   &lt;/entity&gt;
</pre>
<p>
The <i>assertion</i> parameter specifies an assertion that can
refer to the inputs by name.  By default, the assertion
is empty, and attempting
to execute the actor without setting it triggers an exception.
<p>
The <i>errorTolerance</i> parameter specifies the accuracy of
inputs referenced by the assertion. By default, the errorTolerance
is 1e-4.
<p>
NOTE: There are a number of limitations in the current implementation.
First, the errorTolerance adds constraints on the accuracy of the
inputs. An alternative way is to leave the evaluator to handle
the errorTolerance. Which one is better is still under discussion.
Second, the code redundency issues need resolved by refactoring
with the Expression actor.

@author Haiyang Zheng
@version $Id$
@since Ptolemy II 2.0
*/

public class Assertion extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Assertion(CompositeEntity container, String name)
	throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        assertion = new Parameter(this, "assertion");

        _errorTolerance = (double)1e-4;
        errorTolerance = new Parameter(this, "errorTolerance",
				       new DoubleToken(_errorTolerance));

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The parameter that is evaluated.
     *  Typically, this parameter evaluates an assertion involving
     *  the inputs and other parameters.
     */
    public Parameter assertion;

    /** The parameter of error tolerance. By default,
     *  it contains a DoubleToken of 1e-4.
     */
    public Parameter errorTolerance;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the attribute if it has been changed. If the attribute
     *  is <i>errorTolerance<i> then update the local cache.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the attribute change failed.
     */
    public void attributeTypeChanged(Attribute attribute)
	throws IllegalActionException {
        if (attribute == errorTolerance) {
	    double p = ((DoubleToken)errorTolerance.getToken()).doubleValue();
	    if (p <= 0) {
		throw new IllegalActionException(this,
						 "Error tolerance must be greater than 0.");
	    }
	    _errorTolerance = p;
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
        Assertion newObject = (Assertion)super.clone(workspace);
        newObject._tokenMap = null;
        newObject._errorTolerance = (double)1e-4;
        newObject._parseTree = null;
        newObject._parseTreeEvaluator = null;
        newObject._scope = null;
        return newObject;
    }

    /** Evaluate the assertion.
     *  @exception IllegalActionException If the assertion is not valid.
     */
    public Token evaluate() throws IllegalActionException {
        Token result;
        try {
            if(_parseTree == null) {
                PtParser parser = new PtParser();
                _parseTree = parser.generateParseTree(
                        assertion.getExpression());
            }
            if(_parseTreeEvaluator == null) {
                _parseTreeEvaluator = new ParseTreeEvaluator();
            }
            if(_scope == null) {
                _scope = new VariableScope();
            }
            result = _parseTreeEvaluator.evaluateParseTree(
                    _parseTree, _scope);
	    return result;
        } catch (IllegalActionException ex) {
            // Chain exceptions to get the actor that threw the exception.
            throw new IllegalActionException(this, ex, "Assertion invalid.");
        }
    }

    /** Consume tokens in input ports.
     *  @exception IllegalActionException If the evaluation of the assertion
     *   triggers it, or if there is no director, or if a
     *   connected input has no tokens.    
     */

    public void fire() throws IllegalActionException {

        Director director = getDirector();
        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }

        Iterator inputPorts = inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort port = (IOPort)(inputPorts.next());
            if (port.getWidth() > 0) {
                if (port.hasToken(0)) {
                    Token inputToken = port.get(0);
		    // if the token has type as double, round the value of a token
		    // to the precision of error tolerance.
		    if (inputToken.getType() == BaseType.DOUBLE) {

			double value = ((DoubleToken) inputToken).doubleValue();

			// calculate the precison specified by the error tolerance
			// log10(errorTolerance) = logE(errorTolerance)/logE(10)
			double precision = Math.log(_errorTolerance)/Math.log(10);
			int preciseBits = (int) Math.abs(Math.round(precision));

			BigDecimal valueBD = new BigDecimal(value);
			valueBD = valueBD.setScale(preciseBits,BigDecimal.ROUND_HALF_UP);
			value = valueBD.doubleValue();
			inputToken = new DoubleToken(value);

			if (_debugging) {
			    _debug("the modified input is" + ((DoubleToken)inputToken).doubleValue());
			}
		    }

		    // update the local copy of the input values
                    _tokenMap.put(port.getName(), inputToken);
                }
            }
        }
    }

    /** Initialize the actor.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _tokenMap = new HashMap();
    }

    /** Evaluation of the assertion.
     *  @exception IllegalActionException If the assertion fails.
     */
    public boolean postfire() throws IllegalActionException {

        BooleanToken result = (BooleanToken) evaluate();

        if (!result.booleanValue()) {
            throw new IllegalActionException(this,
					     "Assertion fails! " +
					     assertion.getExpression());
        }

        // This actor never requests termination.
        return true;
    }

    /** Create receivers and validate the attributes contained by this
     *  actor and the ports contained by this actor.  This method overrides
     *  the base class to not throw exceptions if the parameters of this
     *  actor cannot be validated.  This is done because the assertion
     *  depends on the input values, which may not be valid before type
     *  resolution occurs.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void preinitialize() throws IllegalActionException {
        try {
            super.preinitialize();
        }
        catch(Exception ex) {
        }
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
            // FIXME: Have to initialize with a token since vergil
            // evaluates variables at start-up.  This needs to be a double
            // so that assertions that use java.Math work.
            Variable variable =
                new Variable(this, portName, new DoubleToken(1.0));
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
    ////                       private methods                     ////

    // Find the variable with the given name in the scope of
    // this actor, and return it.  If there is no such
    // variable, then return null.
    private Variable _findVariable(String name) {
        NamedObj container = (NamedObj)Assertion.this;
        while (container != null) {
            Variable result = _searchIn(container, name);
            if (result != null) {
                return result;
            } else {
                container = (NamedObj)container.getContainer();
            }
        }
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

    private class VariableScope implements ParserScope {

        /** Look up and return the attribute with the specified name in the
         *  scope. Return null if such an attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         */
        public Token get(String name) throws IllegalActionException {

            Token token = (Token)_tokenMap.get(name);
            if(token != null) {
                return token;
            }
            
            Variable result = _findVariable(name);     
            if (result != null) {
                return result.getToken();
            }
            return null;
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         */
        public Type getType(String name) throws IllegalActionException {

            // Check the port names.
            TypedIOPort port = (TypedIOPort)getPort(name);
            if(port != null) {
                return port.getType();
            }
              
            Variable result = _findVariable(name);
            if(result != null) {
                return result.getType();
            }
            return null;
        }

        /** Return the list of attributes within the scope.
         *  @return The list of attributes within the scope.
         */
        public NamedList variableList() {
            return null;
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Parameter, the error tolerance, local copy
    protected double _errorTolerance;
    
    // The local copy of the input tokens.
    private Map _tokenMap;

    private ParseTreeEvaluator _parseTreeEvaluator = null;
    private ASTPtRootNode _parseTree = null;
    private VariableScope _scope = new VariableScope();
}
