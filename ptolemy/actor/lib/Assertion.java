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
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.*;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// Assertion
/**

@author Haiyang Zheng
@version $Id$
@since Ptolemy II 2.0
*/

public class Assertion extends TypedAtomicActor implements CTStepSizeControlActor {

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
	threshold = new Parameter(this, "threshold");

        _time = new Variable(this, "time", new DoubleToken(0.0));
        _iteration = new Variable(this, "iteration", new IntToken(1));

        _errorTolerance = (double)1e-4;
        errorTolerance = new Parameter(this, "errorTolerance",
				       new DoubleToken(_errorTolerance));

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The parameter that is evaluated to produce the output.
     *  Typically, this parameter evaluates an assertion involving
     *  the inputs.
     */
    public Parameter assertion;
    public Parameter threshold;

    /** The parameter of error tolerance of type double. By default,
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
        newObject._iterationCount = 1;
        newObject._time = (Variable)newObject.getAttribute("time");
        newObject._iteration = (Variable)newObject.getAttribute("iteration");
        return newObject;
    }

    /** Evaluate the assertion. If the evaluation returns false, throw a run-time
     *  IllegalActionException.
     *  @exception IllegalActionException If the evaluation of the assertion
     *   triggers it, or the evaluation yields a null result, or the evaluation
     *   yields an incompatible type, or if there is no director.
     */
    public void fire() throws IllegalActionException {
	// consume the input.
	// The assertion actor always has only one input port.
	TypedIOPort input = (TypedIOPort) inputPortList().get(0);
	_thisInput = ((DoubleToken) input.get(0)).doubleValue();
        _threshold = ((DoubleToken) threshold.getToken()).doubleValue();
		
	if (_debugging)
	    _debug(getFullName() + " consuming input Token" + _thisInput);
    }

    /** Return true if this step does not cross the threshold.
     *  The current trigger
     *  token will be compared to the previous trigger token. If they
     *  cross the level threshold, this step is not accurate.
     *  A special case is taken care so that if the previous trigger
     *  and the current trigger both equal to the level value,
     *  then no new event is
     *  detected. If this step crosses the level threshold,
     *  then the refined integration
     *  step size is computed by linear interpolation.
     *  If this is the first iteration after initialize() is called,
     *  then always return false, since there is no history to compare with.
     *  @return True if the trigger input in this integration step
     *          does not cross the level threshold.
     */
    public boolean isThisStepAccurate() {
	if (_first) {
            _first = false;
            return true;
        }
        if (_debugging) {
            _debug(this.getFullName() + " This input " + _thisInput);
            _debug(this.getFullName() + " The last input " + _lastInput);
        }
        if (Math.abs(_thisInput - _threshold) < _errorTolerance) {
            if (_enabled) {
                _enabled = false;
            }
            _eventMissed = false;
            return true;
        } else {
            if (!_enabled) {  // if last step is a level, always accurate.
                _enabled = true;
            } else {
                if ((_lastInput - _threshold) * (_thisInput - _threshold)
		    < 0.0) {

                    CTDirector dir = (CTDirector)getDirector();
                    _eventMissed = true;
                    // The refined step size is a linear interpolation.
                    _refineStep = (Math.abs(_lastInput - _threshold)
				   *dir.getCurrentStepSize())
                        /Math.abs(_thisInput-_lastInput);
                    if (_debugging) _debug(getFullName() +
					   " Refined step at" +  _refineStep);
                    return false;
                }
            }
            _eventMissed = false;
            return true;
        }
    }

    /** Initialize the iteration count to 1.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _eventMissed = false;
        _iterationCount = 1;
        _iteration.setToken(new IntToken(_iterationCount));
    }

    /** Increment the iteration count.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean postfire() throws IllegalActionException {

	_lastInput = _thisInput;

        Director director = getDirector();
        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }
        _time.setToken(new DoubleToken(director.getCurrentTime()));
        Iterator inputPorts = inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort port = (IOPort)(inputPorts.next());
            // FIXME: Handle multiports
            if (port.getWidth() > 0) {
                if (port.hasToken(0)) {
                    Token inputToken = port.get(0);

		    // round the value of a token to the specified precision
		    if (inputToken.getType() == BaseType.DOUBLE) {

			double value = ((DoubleToken) inputToken).doubleValue();

			// FIXME: The print comments are not removed because the
			// attributeTypeChanged method is not called correctly.
			// After that bug fixed, they will be removed.

			//System.out.println("before  " + value);

			// calculate the precison specified by the error tolerance
			// log10(errorTolerance) = logE(errorTolerance)/logE(10)
			double precision = Math.log(_errorTolerance)/Math.log(10);
			int decimalPlace = (int) Math.abs(Math.round(precision));

			//System.out.println("precision  " + decimalPlace);
			BigDecimal valueBD = new BigDecimal(value);
			valueBD = valueBD.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
			value = valueBD.doubleValue();
			//System.out.println("after  " + value);
			inputToken = new DoubleToken(value);
		    }

                    Variable var =
                        (Variable)(getAttribute(port.getName()));
                    var.setToken(inputToken);
                }
            }
        }

        BooleanToken result = (BooleanToken) assertion.getToken();

        if (!result.booleanValue()) {
            throw new IllegalActionException(this,
					     "Assertion fails! " +
					     assertion.getExpression());
        }

        _iterationCount++;
        _iteration.setToken(new IntToken(_iterationCount));
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

    /** Return the maximum Double, since this actor does not predict
     *  step size.
     *  @return java.Double.MAX_VALUE.
     */
    public double predictedStepSize() {
        return java.lang.Double.MAX_VALUE;
    }

    /** Return the refined step size if there is a missed event,
     *  otherwise return the current step size.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        if (_eventMissed) {
            return _refineStep;
        }
        return ((CTDirector)getDirector()).getCurrentStepSize();
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
    ////                         private variables                 ////

    // Parameter, the error tolerance, local copy
    private double _errorTolerance;

    private double _thisInput;
    private double _lastInput;

    private double _threshold;

    // flag indicating if the event detection is enable for this step
    private boolean _enabled;

    // flag indicating if this is the first iteration in the execution,
    private boolean _first = true;

    // flag for indicating a missed event
    private boolean _eventMissed = false;

    // refined step size.
    private double _refineStep;

    private Variable _time;
    private Variable _iteration;
    private int _iterationCount = 1;
}
