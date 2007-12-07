/* An integrator for the continuous domain.

 Copyright (c) 1998-2007 The Regents of the University of California.
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
package ptolemy.domains.continuous.kernel;

import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
//// ContinuousIntegrator

/**
 The integrator in the continuous domain.
 The <i>derivative</i> port receives the derivative of the state of the integrator
 with respect to time. The <i>state</i> output port shows the state of the
 integrator. So an ordinary differential equation (ODE),
 dx/dt = f(x, t), can be built as follows:
 <P>
 <pre>
 +---------------+
 dx/dt  |               |   x
 +--------->|   Integrator  |---------+----->
 |          |               |         |
 |          +----^-----^----+         |
 |                                    |
 |             |---------|            |
 +-------------| f(x, t) |<-----------+
 |---------|
 </pre>
 <P>
 An integrator also has a port-parameter called <i>initialState</i>. The
 parameter provides the initial state for integration during the initialization
 stage of execution. If during execution an input token is provided on
 the port, then the state of the integrator will be reset at that time
 to the value of the token. The default value of the parameter is 0.0.
 <P>
 An integrator also has an input port named <i>impulse</i>.
 When present, a token at the <i>impulse</i> input
 port is interpreted as the weight of a Dirac delta function.
 It cause an instantaneous increment or decrement to the state.
 If both <i>impulse</i> and <i>initialState</i> have data, then
 <i>initialState</i> dominates.
 <P>
 An integrator can generate an output (its current state) before
 the derivative input is known, and hence can be used in feedback
 loops like that above without creating a causality loop.
 The <i>impulse</i> and <i>initialState</i> inputs
 ports must be known, however, before an output can be produced.
 The effect of data at these inputs on the output is instantaneous.
 <P>
 For different ODE solving methods, the functionality
 of an integrator may be different. The delegation and strategy design
 patterns are used in this class, the abstract ODESolver class, and the
 concrete ODE solver classes. Some solver-dependent methods of integrators
 delegate to the concrete ODE solvers.
 <P>
 An integrator can possibly have several auxiliary variables for the
 the ODE solvers to use. The ODE solver class provides the number of
 variables needed for that particular solver.
 The auxiliary variables can be set and get by setAuxVariables()
 and getAuxVariables() methods.
 <p>
 This class is based on the CTBaseIntegrator by Jie Liu and Haiyang Zheng,
 but it has more ports and provides more functionality.

 @author Haiyang Zheng and Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (yuhong)
 */
public class ContinuousIntegrator extends TypedAtomicActor implements
        ContinuousStatefulComponent, ContinuousStepSizeController {

    /** Construct an integrator with the specified name and a container.
     *  The integrator is in the same workspace as the container.
     *  @param container The container.
     *  @param name The name.
     *  @exception NameDuplicationException If the name is used by another actor in the container.
     *  @exception IllegalActionException If ports can not be created, or
     *   thrown by the super class.
     */
    public ContinuousIntegrator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        impulse = new TypedIOPort(this, "impulse", true, false);
        impulse.setTypeEquals(BaseType.DOUBLE);
        StringAttribute cardinality = new StringAttribute(impulse, "_cardinal");
        cardinality.setExpression("SOUTH");

        derivative = new TypedIOPort(this, "derivative", true, false);
        derivative.setTypeEquals(BaseType.DOUBLE);

        state = new TypedIOPort(this, "state", false, true);
        state.setTypeEquals(BaseType.DOUBLE);

        initialState = new PortParameter(this, "initialState", new DoubleToken(
                0.0));
        initialState.setTypeEquals(BaseType.DOUBLE);
        cardinality = new StringAttribute(initialState.getPort(), "_cardinal");
        cardinality.setExpression("SOUTH");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The impulse input port. This is a single port of type double.
     */
    public TypedIOPort impulse;

    /** The derivative port. This is a single port of type double.
     */
    public TypedIOPort derivative;

    /** The state port. This is a single port of type double.
     */
    public TypedIOPort state;

    /** The initial state of type DoubleToken. The default value is 0.0.
     */
    public PortParameter initialState;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>initialState</i>, then reset
     *  the state of the integrator to its value.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the new parameter value
     *  is not valid.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == initialState) {
            _tentativeState = ((DoubleToken) initialState.getToken())
                    .doubleValue();
            _state = _tentativeState;
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** If the value at the <i>derivative</i> port is known, and the current
     *  step size is bigger than 0, perform an integration.
     *  If the <i>impulse</i> port is known and has data, then add the
     *  value provided to the state; if the <i>initialState</i> port
     *  is known and has data, then reset the state to the provided
     *  value. If both <i>impulse</i> and <i>initialState</i> have
     *  data, then <i>initialState</i> dominates. If either is unknown,
     *  then simply return, leaving the output unknown. Note that the signals
     *  provided at these two ports are required to be purely discrete.
     *  This is enforced by throwing an exception if the current step
     *  size is greater than zero when they have input data.
     *  @exception IllegalActionException If the input is infinite or
     *   not a number, or if thrown by the solver,
     *   or if data is present at either <i>impulse</i>
     *   or <i>initialState</i> and the step size is greater than zero.
     */
    public void fire() throws IllegalActionException {
        if (!impulse.isKnown() || !initialState.getPort().isKnown()) {
            // Cannot do anything until these inputs are known.
            return;
        }
        // Question: should the above go to the prefire() method?
        // Answer: No, because of the design of the fixed point director.
        // In particular, since this actor is a nonstrict actor, it is "ready
        // to fire". If this actor returns false from the prefire() method,
        // then all outputs will be cleared (set to absent). This is not
        // what we want to do. In this case, we allow the actor to be fired,
        // and the fire() method should do the right thing.
        // Similar cases arise from any composite actor (including modal
        // models), which may contain some source actor inside while the
        // interface ports indicate the actor is a strict actor. Therefore,
        // the prefire() actor of any composite actor should return true
        // at any time, and they should be ready to be fired at any time.

        ContinuousDirector dir = (ContinuousDirector) getDirector();
        double stepSize = dir.getCurrentStepSize();

        // The state at the current model time depends on the inputs from both
        // impulse and initialState ports (causal relation), but not on the
        // derivative at the current model time.
        for (int i = 0; i < impulse.getWidth(); i++) {
            if (impulse.hasToken(i)) {
                if (stepSize != 0.0) {
                    throw new IllegalActionException(this,
                            "Signal at the impulse port is not purely discrete.");
                }
                double currentState = getState()
                        + ((DoubleToken) impulse.get(i)).doubleValue();
                setTentativeState(currentState);
            }
        }
        // The input from the initialState port overwrites the input from
        // the impulse port.
        TypedIOPort statePort = initialState.getPort();
        for (int i = 0; i < statePort.getWidth(); i++) {
            if (statePort.hasToken(i)) {
                if (stepSize != 0.0) {
                    throw new IllegalActionException(this,
                            "Signal at the initialState port is not purely discrete.");
                }
                double currentState = ((DoubleToken) statePort.get(i))
                        .doubleValue();
                setTentativeState(currentState);
            }
        }

        // Prefire() resets receivers, also each firing of this actor changes
        // the _tentativeState variable, therefore the following statements
        // may broadcast the newly calculated _tentativeState at a wrong time.
        // For example, an opaque composite actor containing an integrator is
        // scheduled to run multiple times during the same round.

        // Since prefire() can be called any number of times, and the semantics
        // of prefire() requires the results not to be affected by the numbers
        // of callings. Therefore, the following statements do not work.
        //        if (!state.isKnown()) {
        //            state.broadcast(new DoubleToken(getTentativeState()));
        //        }
        // Instead, we use the round number to guard the outputs.
        if (!state.isKnown()) {
            int currentRound = dir._getODESolver()._getRound();
            if (_lastRound < currentRound) {
                _lastRound = currentRound;
                _lastRoundOutput = getTentativeState();
            }
            state.broadcast(new DoubleToken(_lastRoundOutput));
        }

        if (derivative.isKnown() && derivative.hasToken(0)) {
            double currentDerivative = getDerivative();
            if (Double.isNaN(currentDerivative)
                    || Double.isInfinite(currentDerivative)) {
                throw new IllegalActionException(this,
                        "The provided derivative input is invalid: "
                                + currentDerivative);
            }
            if (stepSize > 0.0) {
                // The following method changes the tentative state but
                // should not expose the tentative state.
                dir._getODESolver().integratorIntegrate(this);
            }
        }
    }

    /** Return the auxiliary variables in a double array.
     *  The auxiliary variables are created in the prefire() method and
     *  may be set during each firing of the actor. Return null if the
     *  auxiliary variables have never been created.
     *
     *  @return The auxiliary variables in a double array.
     *  @see #setAuxVariables
     */
    public double[] getAuxVariables() {
        return _auxVariables;
    }

    /** Get the current value of the derivative input port.
     *  @return The current value at the derivative input port.
     *  @exception NoTokenException If reading the input throws it.
     *  @exception IllegalActionException If thrown while reading
     *  the input.
     */
    public double getDerivative() throws NoTokenException,
            IllegalActionException {
        return ((DoubleToken) derivative.get(0)).doubleValue();
    }

    /** Return the state of the integrator. The returned state is the
     *  latest confirmed state.
     *  @return The state of the integrator.
     */
    public final double getState() {
        return _state;
    }

    /** Return the tentative state.
     *  @return The tentative state.
     *  @see #setTentativeState
     */
    public double getTentativeState() {
        return _tentativeState;
    }

    /** Initialize the integrator. Check for the existence of a director and
     *  an ODE solver. Set the state to the value given by <i>initialState</i>.
     *  @exception IllegalActionException If there is no director,
     *   or the director has no ODE solver, or the initialState
     *   parameter does not contain a valid token, or the superclass
     *   throws it.
     */
    public void initialize() throws IllegalActionException {
        ContinuousDirector dir = (ContinuousDirector) getDirector();

        if (dir == null) {
            throw new IllegalActionException(this, " no director available");
        }

        ContinuousODESolver solver = dir._getODESolver();

        if (solver == null) {
            throw new IllegalActionException(this, " no ODE solver available");
        }

        super.initialize();
        _lastRound = -1;
        _tentativeState = ((DoubleToken) initialState.getToken()).doubleValue();
        _state = _tentativeState;
        _lastRoundOutput = _tentativeState;

        if (_debugging) {
            _debug("Initialize: initial state = " + _tentativeState);
        }

        // The number of auxiliary variables that are used depends on the solver.
        int n = solver.getIntegratorAuxVariableCount();
        if ((_auxVariables == null) || (_auxVariables.length != n)) {
            _auxVariables = new double[n];
        }
    }

    /** Return true if the state is resolved successfully.
     *  If the input is not available, or the input is a result of
     *  divide by zero, a NumericalNonconvergeException is thrown.
     *  @return True if the state is resolved successfully.
     */
    public boolean isStepSizeAccurate() {
        ContinuousODESolver solver = ((ContinuousDirector) getDirector())
                ._getODESolver();
        _successful = solver.integratorIsAccurate(this);
        return _successful;
    }

    /** Return false. This actor can produce some outputs even the inputs
     *  are unknown. This actor is crucial at breaking feedback loops during
     *  simulation.
     *
     *  @return False.
     */
    public boolean isStrict() {
        return false;
    }

    /** Update the state. This commits the tentative state.
     *  @return True always.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        _state = _tentativeState;
        _lastRound = -1;
        if (_debugging) {
            _debug("Committing the state: " + _state);
        }
        return true;
    }

    /** Return the suggested next step size. This method delegates to
     *  the integratorPredictedStepSize() method of the current ODESolver.
     *  @return The suggested next step size.
     */
    public double suggestedStepSize() {
        ContinuousODESolver solver = ((ContinuousDirector) getDirector())
                ._getODESolver();
        return solver.integratorSuggestedStepSize(this);
    }

    /** Override the base class to declare that the <i>output</i>
     *  does not depend on the <i>input</i> in a firing.
     */
    public void pruneDependencies() {
        super.pruneDependencies();
        removeDependency(derivative, state);
    }

    /** Return the estimation of the refined next step size.
     *  If this integrator considers the current step to be accurate,
     *  then return the current step size, otherwise return half of the
     *  current step size.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        double step = ((ContinuousDirector) getDirector()).getCurrentStepSize();

        if (_successful) {
            return step;
        } else {
            return 0.5 * step;
        }
    }

    /** Roll back to committed state. This resets the tentative state
     *  to the current state.
     */
    public void rollBackToCommittedState() {
        _lastRound = -1;
        _tentativeState = _state;
    }

    /** Set the value of an auxiliary variable. The index indicates which
     *  auxiliary variable in the auxVariables array. If the index is out of
     *  the bound of the auxiliary variable array, an InvalidStateException
     *  is thrown to indicate an inconsistency in the ODE solver.
     *
     *  @param index The index in the auxVariables array.
     *  @param value The value to be set.
     *  @exception InvalidStateException If the index is out of the range
     *  of the auxiliary variable array.
     *  @see #getAuxVariables
     */
    public void setAuxVariables(int index, double value)
            throws InvalidStateException {
        try {
            _auxVariables[index] = value;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new InvalidStateException(this,
                    "index out of the range of the auxVariables.");
        }
    }

    /** Set the tentative state. Tentative state is the state that
     *  the ODE solver resolved in one step. This may not
     *  be the final state due to error control or event detection.
     *  @param value The value to be set.
     *  @see #getTentativeState()
     */
    public final void setTentativeState(double value) {
        _tentativeState = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Auxiliary variable array. This is used by the solver to
     *  record intermediate values in a multi-step solver algorithm.
     */
    private double[] _auxVariables;

    /** The last round this integrator is fired. */
    private int _lastRound;

    /** The output of last round this integrator is fired. */
    private double _lastRoundOutput;

    /** The state of the integrator. */
    private double _state;

    /** Indicate whether the latest step is successful from this
     *  integrator's point of view.
     */
    private boolean _successful = false;

    /** The tentative state. */
    private double _tentativeState;
}
