/* An integrator for the continuous domain.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

import java.util.Collection;
import java.util.LinkedList;

import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.continuous.ContinuousStatefulComponent;
import ptolemy.actor.continuous.ContinuousStepSizeController;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.DefaultCausalityInterface;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ContinuousIntegrator

/**
 The integrator in the continuous domain.  The <i>derivative</i> port
 receives the derivative of the state of the integrator with respect
 to time. The <i>state</i> output port shows the state of the
 integrator. So an ordinary differential equation (ODE), dx/dt = f(x,
 t), can be built as follows:
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

 <P> An integrator also has a port-parameter called
 <i>initialState</i>. The parameter provides the initial state for
 integration during the initialization stage of execution. If during
 execution an input token is provided on the port, then the state of
 the integrator will be reset at that time to the value of the
 token. The default value of the parameter is 0.0.

 <P> An integrator also has an input port named <i>impulse</i>.  When
 present, a token at the <i>impulse</i> input port is interpreted as
 the weight of a Dirac delta function.  It causes an
 increment or decrement to the state at the time of
 the arrival of the value.  If both <i>impulse</i> and
 <i>initialState</i> have data on the same microstep,
 then <i>initialState</i> dominates.

 <P> Note that both <i>impulse</i> and <i>reset</i> expect to
 receive discrete inputs. To preserve continuity, this means
 that those inputs should be present only when the solver
 step size is zero.
 If this assumption is violated, then this actor will throw
 an exception.

 <P> An integrator can generate an output (its current state) before
 the derivative input is known, and hence can be used in feedback
 loops like that above without creating a causality loop.  Since
 <i>impulse</i> and <i>initialState</i> inputs affect the output
 immediately, using them in feedback loops may require inclusion
 of a TimeDelay actor.

 <P> For different ODE solving methods, the functionality of an
 integrator may be different. The delegation and strategy design
 patterns are used in this class, the abstract ODESolver class, and
 the concrete ODE solver classes. Some solver-dependent methods of
 integrators delegate to the concrete ODE solvers.

 <P> An integrator can possibly have several auxiliary variables for
 the ODE solvers to use. The ODE solver class provides the number
 of variables needed for that particular solver.  The auxiliary
 variables can be set and get by setAuxVariables() and
 getAuxVariables() methods.

 <p> This class is based on the CTBaseIntegrator by Jie Liu and
 Haiyang Zheng, but it has more ports and provides more functionality.

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
     *  @exception NameDuplicationException If the name is used by
     *  another actor in the container.
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

        _causalityInterface = new IntegratorCausalityInterface(this,
                BooleanDependency.OTIMES_IDENTITY);
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
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == initialState) {
            _tentativeState = ((DoubleToken) initialState.getToken())
                    .doubleValue();
            _state = _tentativeState;
            if (_debugging) {
                _debug("initialState changed. Updating state to " + _state);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same ports as the original, but
     *  no connections and no container.  A container must be set before
     *  much can be done with this actor.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new ComponentEntity.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ContinuousIntegrator newObject = (ContinuousIntegrator) super
                .clone(workspace);
        newObject._auxVariables = null;
        newObject._causalityInterface = new IntegratorCausalityInterface(
                newObject, BooleanDependency.OTIMES_IDENTITY);
        return newObject;
    }

    /** If the value at the <i>derivative</i> port is known, and the
     *  current step size is bigger than 0, perform an integration.
     *  If the <i>impulse</i> port is known and has data, then add the
     *  value provided to the state; if the <i>initialState</i> port
     *  is known and has data, then reset the state to the provided
     *  value. If both <i>impulse</i> and <i>initialState</i> have
     *  data, then <i>initialState</i> dominates. If either is
     *  unknown, then simply return, leaving the output unknown. Note
     *  that the signals provided at these two ports are required to
     *  be purely discrete.  This is enforced by throwing an exception
     *  if the current microstep is zero when they have
     *  input data.
     *  @exception IllegalActionException If the input is infinite or
     *   not a number, or if thrown by the solver,
     *   or if data is present at either <i>impulse</i>
     *   or <i>initialState</i> and the step size is greater than zero.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        ContinuousDirector dir = (ContinuousDirector) getDirector();
        double stepSize = dir.getCurrentStepSize();
        int microstep = dir.getIndex();

        if (_debugging) {
            Time currentTime = dir.getModelTime();
            _debug("Fire at time " + currentTime + " and microstep "
                    + microstep + " with step size " + stepSize);
        }
        // First handle the impulse input.
        if (impulse.getWidth() > 0 && impulse.hasToken(0)) {
            double impulseValue = ((DoubleToken) impulse.get(0)).doubleValue();
            if (_debugging) {
                _debug("-- impulse input received with value " + impulseValue);
            }
            if (impulseValue != 0.0) {
                if (microstep == 0 && !_firstFiring) {
                    throw new IllegalActionException(this,
                            "Signal at the impulse port is not purely discrete.");
                }
                double currentState = getState() + impulseValue;
                setTentativeState(currentState);
                if (_debugging) {
                    _debug("-- Due to impulse input, set state to "
                            + currentState);
                }
            }
        }
        // Next handle the initialState port.
        ParameterPort initialStatePort = initialState.getPort();
        if (initialStatePort.getWidth() > 0 && initialStatePort.hasToken(0)) {
            double initialValue = ((DoubleToken) initialStatePort.get(0))
                    .doubleValue();
            if (_debugging) {
                _debug("-- initialState input received with value "
                        + initialValue);
            }
            if (microstep == 0.0) {
                throw new IllegalActionException(this,
                        "Signal at the initialState port is not purely discrete.");
            }
            setTentativeState(initialValue);
            if (_debugging) {
                _debug("-- Due to initialState input, set state to "
                        + initialValue);
            }
        }

        // Produce the current _tentativeState as output, if it
        // has not already been produced.
        if (!state.isKnown()) {
            double tentativeOutput = getTentativeState();
            // If the round has not updated since the last output, then
            // just produce the same output as last time.
            int currentRound = dir._getODESolver()._getRound();
            if (_lastRound == currentRound) {
                tentativeOutput = _lastOutput;
            }

            if (_debugging) {
                _debug("** Sending output " + tentativeOutput);
            }
            _lastOutput = tentativeOutput;
            state.broadcast(new DoubleToken(tentativeOutput));
        }

        // The _tentativeSate is committed only in postfire(),
        // but multiple rounds will occur before postfire() is called.
        // At each round, this fire() method may be called multiple
        // times, and we want to make sure that the integration step
        // only runs once in the step.
        if (derivative.isKnown() && derivative.hasToken(0)) {
            int currentRound = dir._getODESolver()._getRound();
            if (_lastRound < currentRound) {
                // This is the first fire() in a new round
                // where the derivative input is known and present.
                // Update the tentative state. Note that we will
                // have already produced an output, and so we
                // will not read the updated _tentativeState
                // again in subsequent invocations of fire()
                // in this round. So it is safe to update
                // _tentativeState.
                _lastRound = currentRound;
                double currentDerivative = getDerivative();
                if (Double.isNaN(currentDerivative)
                        || Double.isInfinite(currentDerivative)) {
                    throw new IllegalActionException(this,
                            "The provided derivative input is invalid: "
                                    + currentDerivative);
                }
                if (stepSize > 0.0) {
                    // The following method changes the tentative state.
                    dir._getODESolver().integratorIntegrate(this);
                }
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

    /** Return a causality interface for this actor. This causality
     *  interface expresses dependencies that are instances of
     *  BooleanDependency that declare that the <i>state</i> output
     *  port does not depend on any of the input ports at this
     *  microstep. Moreover, the  <i>initialState</i> and <i>impulse</i> ports are
     *  equivalent (to process inputs at either, you need to know
     *  about inputs at the other).  You do not need to know about
     *  inputs at <i>derivative</i>.
     *  @return A representation of the dependencies between input ports
     *   and output ports.
     */
    @Override
    public CausalityInterface getCausalityInterface() {
        return _causalityInterface;
    }

    /** Get the current value of the derivative input port.
     *  @return The current value at the derivative input port.
     *  @exception NoTokenException If reading the input throws it.
     *  @exception IllegalActionException If thrown while reading
     *  the input.
     */
    public double getDerivative() throws NoTokenException,
    IllegalActionException {
        double result = ((DoubleToken) derivative.get(0)).doubleValue();
        if (_debugging) {
            _debug("Read input: " + result);
        }
        return result;
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

    /** Initialize the integrator. Check for the existence of a
     *  director and an ODE solver. Set the state to the value given
     *  by <i>initialState</i>.
     *  @exception IllegalActionException If there is no director,
     *   or the director has no ODE solver, or the initialState
     *   parameter does not contain a valid token, or the superclass
     *   throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        if (!(getDirector() instanceof ContinuousDirector)) {
            throw new IllegalActionException(this,
                    "This actor can only be run in" + " a ContinuousDirector.");
        }
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
        _firstFiring = true;

        if (_debugging) {
            _debug("Initialize: initial state = " + _tentativeState);
        }

        // The number of auxiliary variables that are used depends on
        // the solver.
        int n = solver.getIntegratorAuxVariableCount();
        if (_auxVariables == null || _auxVariables.length != n) {
            _auxVariables = new double[n];
        }
    }

    /** Return true if the state is resolved successfully.
     *  If the input is not available, or the input is a result of
     *  divide by zero, a NumericalNonconvergeException is thrown.
     *  @return True if the state is resolved successfully.
     */
    @Override
    public boolean isStepSizeAccurate() {
        ContinuousODESolver solver = ((ContinuousDirector) getDirector())
                ._getODESolver();
        _successful = solver.integratorIsAccurate(this);
        return _successful;
    }

    /** Return false. This actor can produce some outputs even the
     *  derivative input is unknown. This actor is crucial at breaking feedback
     *  loops during simulation.
     *  The impulse and initialState ports, have to be known for prefire to
     *  return true (if they are connected).
     *  @return False.
     */
    @Override
    public boolean isStrict() {
        return false;
    }

    /** Update the state. This commits the tentative state.
     *  @return True always.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _lastRound = -1;
        _firstFiring = false;

        if (_debugging) {
            _debug("Postfire called");
        }
        _state = _tentativeState;
        if (_debugging) {
            _debug("-- Committing the state: " + _state);
        }
        return super.postfire();
    }

    /** If either the <i>impulse</i> or <i>initialState</i> input is unknown,
     *  then return false. Otherwise, return true.
     *  @return True If the actor is ready to fire.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        boolean result = super.prefire();
        if ((impulse.getWidth() == 0 || impulse.isKnown(0))
                && (initialState.getPort().getWidth() == 0 || initialState
                .getPort().isKnown(0))) {
            return result;
        }
        return false;
    }

    /** Return the suggested next step size. This method delegates to
     *  the integratorPredictedStepSize() method of the current ODESolver.
     *  @return The suggested next step size.
     */
    @Override
    public double suggestedStepSize() {
        ContinuousODESolver solver = ((ContinuousDirector) getDirector())
                ._getODESolver();
        return solver.integratorSuggestedStepSize(this);
    }

    /** Return the estimation of the refined next step size.
     *  If this integrator considers the current step to be accurate,
     *  then return the current step size, otherwise return half of the
     *  current step size.
     *  @return The refined step size.
     */
    @Override
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
    @Override
    public void rollBackToCommittedState() {
        if (_debugging) {
            _debug("Rolling back to state: " + _state);
        }
        _lastRound = -1;
        _tentativeState = _state;
    }

    /** Set the value of an auxiliary variable. The index indicates
     *  which auxiliary variable in the auxVariables array. If the
     *  index is out of the bound of the auxiliary variable array, an
     *  InvalidStateException is thrown to indicate an inconsistency
     *  in the ODE solver.
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

    /** The custom causality interface. */
    private CausalityInterface _causalityInterface;

    /** Indicator that this is the first firing after initialize(). */
    private boolean _firstFiring;

    /** The last output produced in the same round. */
    private double _lastOutput;

    /** The last round this integrator is fired. */
    private int _lastRound;

    /** The state of the integrator. */
    private double _state;

    /** Indicate whether the latest step is successful from this
     *  integrator's point of view.
     */
    private boolean _successful = false;

    /** The tentative state. */
    private double _tentativeState;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Custom causality interface that fine tunes the equivalent ports
     *  and removes the dependence of the state output on the derivative
     *  input. Ensure that only the impulse and initialState inputs are
     *  equivalent (the base class will make all ports equivalent because
     *  the initialState input is a ParameterPort).
     */
    private static class IntegratorCausalityInterface extends
    DefaultCausalityInterface {
        public IntegratorCausalityInterface(ContinuousIntegrator actor,
                Dependency defaultDependency) {
            super(actor, defaultDependency);
            _actor = actor;
            _derivativeEquivalents.add(actor.derivative);
            _otherEquivalents.add(actor.impulse);
            _otherEquivalents.add(actor.initialState.getPort());

            removeDependency(actor.derivative, actor.state);
        }

        /** Override the base class to declare that the
         *  <i>initialState</i> and <i>impulse</i> inputs are
         *  equivalent, but not the <i>derivative</i> input port.
         *  This is because to react to inputs at either
         *  <i>initialState</i> or <i>impulse</i>, we have to know
         *  what the input at the other is.  But the input at
         *  <i>derivative</i> does not need to be known.  It will
         *  affect the future only.
         *  @param input The port to find the equivalence class of.
         *  @exception IllegalArgumentException If the argument is not
         *   contained by the associated actor.
         */
        @Override
        public Collection<IOPort> equivalentPorts(IOPort input) {
            if (input == _actor.derivative) {
                return _derivativeEquivalents;
            }
            return _otherEquivalents;
        }

        private ContinuousIntegrator _actor;
        private LinkedList<IOPort> _derivativeEquivalents = new LinkedList<IOPort>();
        private LinkedList<IOPort> _otherEquivalents = new LinkedList<IOPort>();
    }
}
