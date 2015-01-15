/* Provide a base class for QSS methods for solving ordinary differential equations.

Copyright (c) 2014 The Regents of the University of California.
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


package org.ptolemy.qss.solver;


import java.util.LinkedList;
import java.util.List;

import org.ptolemy.qss.util.DerivativeFunction;
import org.ptolemy.qss.util.ModelPolynomial;
import org.ptolemy.qss.util.PolynomialRoot;

import ptolemy.actor.util.Time;


//////////////////////////////////////////////////////////////////////////
//// QSSBase


/** Provide a base class for QSS methods for solving ordinary differential equations.
 *
 * <p>A QSS integrator implements one of the "quantized state systems" methods
 * for solving an initial-value problem (IVP) of a system of ordinary differential
 * equations (ODEs).</p>
 *
 * <p>A single QSS integrator may be responsible for an entire system of ODEs.
 * Alternately, it may be responsible for a subset of a larger system.
 * The latter approach allows building up a system from smaller components
 * that represent interacting subsystems.
 * Furthermore, each of those subsystems can be integrated by a different
 * variation on the QSS family of methods.</p>
 *
 * <p>The ODEs are represented using the <code>DerivativeFcn</code> interface.</p>
 *
 * <p>Class <code>QSSBase</code> provides a general framework for running a
 * single QSS integrator.
 * This framework must be supplemented in a number of ways:</p>
 * <ul>
 * <li>Implementing a specific member of the QSS family requires extending the
 * base class.
 * The subclass must implement the abstract methods declared here.
 * Those abstract "worker" methods provide the particulars associated with
 * the specific member of the QSS family.</li>
 * <li>Simulating a system comprising multiple QSS integrators requires
 * the supervisor to connect the integrators.
 * That is, it must match the states predicted by one integrator to the
 * input variables used by another integrator.</li>
 * <li>Simulating a system requires some supervisory control, for example to
 * regulate the time steps, and to sequence the exchange of information between
 * integrators.
 * See the API notes below.</li>
 * </ul>
 *
 *
 * <h2>Background</h2>
 *
 * <p>Quantized State System (QSS) methods solve a system of ordinary differential
 * equations of the form</p>
 * <p><i>xdot = f{t, x, u}</i></p>
 *
 * <p>where</p>
 * <ul>
 * <li><i>t</i>, simulation time.</li>
 * <li><i>x</i>, vector of state variables, <i>x{t}</i>.</li>
 * <li><i>u</i>, vector of input variables, <i>u{t}</i>.</li>
 * <li><i>xdot</i>, vector of time rates of change of the state variables.
 * That is, <i>xdot = dx/dt</i>.</li>
 * <li><i>f</i>, vector-valued derivative function.</li>
 * <li>The notation <i>g{y}</i> means that <i>g</i> is a function
 * of <i>y</i>.</li>
 * </ul>
 *
 * <p>To solve this system, QSS methods rewrite the system as</p>
 * <p><i>xdot = f{t, q, mu}</i></p>
 *
 * <p>where</p>
 * <ul>
 * <li><i>q</i>, vector of quantized state variables, <i>q{t}</i>.</li>
 * <li><i>mu</i>, vector of quantized input variables, <i>u{t}</i>.</li>
 * </ul>
 *
 * <p>The quantized state, and the quantized input variables, are discretized
 * versions of the state and input variables.
 * The quantized version is a piecewise-continuous approximation, with a
 * functional form chosen to simplify the integration of <i>f</i>.</p>
 *
 * <p>The implementation here uses polynomial models to quantize the state
 * and input variables.
 * For example, QSS1 quantizes the state using a 0th-order polynomial (that
 * is, as a constant).
 * This means that for purposes of the integration, the state is held constant
 * over discrete intervals of time.
 * The method name, QSS1, arises since an internal, continuous version of the
 * state is maintained as a 1st-order polynomial (that is, as a line).</p>
 *
 * <p>The details of the methods are beyond the scope of this documentation.
 * See:</p>
 * <ul>
 * <li>Kofman-2002</li>
 * <li>Cellier-2006.</li>
 * <li>Migoni-2009.</li>
 * <li>Migoni-2013.</li>
 * </ul>
 *
 *
 * <h2>External vs internal state</h2>
 *
 * <p>Quantized state system methods expose a <i>quantized state</i> to users.
 * Internally, however, they track a <i>continuous state</i>.
 * Both are modeled using polynomials.
 * The polynomial representing the internal, continuous state has one more
 * coefficient than that for the external, quantized state (i.e., it is of
 * order one greater).
 * For example, QSS1 represents the quantized state as constant, but the
 * continuous state as a linear function of time.</p>
 *
 * <p>For the most part, the user does not need to be aware of this distinction.
 * In fact, for the most part the user only has access to the quantized state.
 * In particular, the user can access the <code>ModelPoly</code> objects used
 * to track the quantized states.
 * However, the QSS integrator reserves to itself the right to change the
 * parameters that define those polynomial models.</p>
 *
 * <p>In general, the API refers to an abstract "state" rather than to the
 * "quantized state" or the "continuous state".
 * However, some methods distinguish between the quantized and continuous
 * states.
 * For example:
 * <ul>
 * <li>Method {@link #evaluateStateModel(int, Time)} evaluates the quantized state model.
 * Since the user can access that model directly, this is mainly a convenience
 * method.</li>
 * <li>Method {@link #evaluateStateModelContinuous(int, Time)} evaluates the
 * internal, continuous state model.
 * This is intended mainly for testing.
 * However, since the internal model does represent the state as a continuous
 * function of time, for some reporting purposes the internal state may be
 * preferred.</li>
 * </ul>
 *
 *
 * <h2>Terminology</h2>
 *
 * <p>The API and documentation presented here use some terms in specific ways.
 * Furthermore these terms do not always correspond exactly with those used in
 * the QSS method literature.</p>
 *
 * <p>The QSS method literature refers to "states" and "quantized states".
 * In order to avoid ambiguity, the code here refers to these as
 * "continuous states" and "quantized states", respectively.</p>
 *
 * <p>Variables for continuous states have names like <code>cStateMdl</code>.
 * Quantized states have names like <code>qStateMdl</code>.</p>
 *
 * <p>TODO: Mention terms "rate-events", "state-events", and "quantization-events".</p>
 *
 *
 * <h2>A tour of the public API</h2>
 * 
 * <p>The simplest use of this class is as follows. First, initialize the solver
 * as follows:
 * <ol>
 * <li> Create an instance of a class that implements {@link DerivativeFunction}, and pass
 *      that into the {@link #initialize(DerivativeFunction, Time, Time, double, double, int)}
 *      method.</li>
 * <li> Then set the initial values of the state variables using
 *      {@link #setStateValue(int, double)}.</li>
 * <li> Then set initial input values by updating the models returned by
 *      {@link #getInputVariableModel(int)}.</li>
 * <li> Then trigger quantization events using
 *      {@link #triggerQuantizationEvents(boolean)}.</li>
 * <li> Finally, trigger rate events using
 *      {@link #triggerRateEvent()}.</li>
 * </ol>
 * At this point, you can determine the first time at which a quantization event
 * will occur by calling {@link #predictQuantizationEventTimeEarliest()}.
 * Then, at each time step,
 * <ol>
 * <li> Advance to the current simulation time by calling {@link #advanceToTime(Time)}.
 *      This will return a list of state indexes that experience a quantization event
 *      at the new simulation time, and you can retrieve the new values of those
 *      state variables using {@link #getStateModel(int)}.
 * <li> Then set input values by updating the models returned by
 *      {@link #getInputVariableModel(int)}.</li>
 * <li> Finally, trigger rate events using
 *      {@link #triggerRateEvent()}.</li>
 * </ol>
 * At this point, you can determine the next time at which a quantization event
 * will occur by calling {@link #predictQuantizationEventTimeEarliest()}.
 * That should be the time of the next time step.
 *
 * <p>The following methods initialize a new integrator.
 * They must be called before doing any work with the integrator:</p>
 * <ul>
 * <li>{@link #initializeDerivativeFunction(DerivativeFunction)}</li>
 * <li>{@link #initializeSimulationTime(Time)}</li>
 * </ul>
 *
 * <p>The following methods inquire about fixed integrator parameters:</p>
 * <ul>
 * <li>{@link #getStateCount()}</li>
 * <li>{@link #getInputVariableCount()}</li>
 * <li>{@link #getStateModelOrder()}</li>
 * </ul>
 *
 * <p>The following methods set up the exchange of models between an integrator
 * and the rest of the simulation environment.
 * In general, they should be called before starting a simulation.
 * However, they may also be called during an integration:</p>
 * <ul>
 * <li>{@link #getStateModel(int)}</li>
 * <li>{@link #needInputVariableModelIndex()}</li>
 * <li>{@link #setInputVariableModel(int, ModelPolynomial)}</li>
 * </ul>
 *
 * <p>The following methods configure the integrator.
 * In general, they should be called before its first use.
 * However, they may also be called during an integration:</p>
 * <ul>
 * <li>{@link #setQuantizationTolerance(int, double, double)}</li>
 * <li>{@link #setQuantizationTolerances(double, double)}</li>
 * <li>{@link #setCurrentSimulationTime(Time)}</li>
 * <li>{@link #setStateValue(int, double)}</li>
 * <li>{@link #setQuantizationEventTimeMaximum(Time)}</li>
 * <li>{@link #validate()}</li>
 * </ul>
 *
 * <p>The following methods inquire about current values during a simulation:</p>
 * <ul>
 * <li>{@link #getCurrentSimulationTime()}</li>
 * <li>{@link #evaluateStateModel(int, Time)}</li>
 * <li>{@link #evaluateStateModelContinuous(int, Time)}</li>
 * </ul>
 *
 * <p>The following methods prepare the integrator to take the next time step:</p>
 * <ul>
 * <li>{@link #needQuantizationEventIndex()}</li>
 * <li>{@link #needQuantizationEventIndexes(boolean[])}</li>
 * <li>{@link #triggerQuantizationEvent(int)}</li>
 * <li>{@link #triggerQuantizationEvents(boolean)}</li>
 * <li>{@link #needRateEvent()}</li>
 * <li>{@link #triggerRateEvent()}</li>
 * <li>{@link #predictQuantizationEventTime(int)}</li>
 * <li>{@link #predictQuantizationEventTimeEarliest()}</li>
 * </ul>
 *
 * <p>The following methods take a time step:</p>
 * <ul>
 * <li>{@link #stepToTime(Time)}</li>
 * </ul>
 *
 * <p>The following methods primarily facilitate testing:</p>
 * <ul>
 * <li>{@link #stringifyStateModel(int)}</li>
 * <li>{@link #stringifyStateModelContinuous(int)}</li>
 * <li>{@link #findDq(int)}</li>
 * </ul>
 *
 * <p>TODO: Describe the general time-stepping model.
 * Steps only accomplished via method {@link #stepToTime(Time)}.
 * All other methods elaborate on what happens between time steps.</p>
 *
 * <p>The abstract methods that each subclass must fill in have names ending
 * in <code>_work</code>.
 * This is meant to help distinguish them from the general entry-points
 * provided by this base class.</p>
 *
 *
 * <h2>References</h2>
 * <ol>
 * <li> [Kofman-Junco-2001].
 * Kofman, E. and S. Junco (2001). "Quantized-State Systems: 
 * A {DEVS} Approach for Continuous System Simulation."
 * Trans. of The Society for Modeling and Simulation International 18(1): 2-8.
 *
 * <li> [Kofman-2002].
 * Ernesto Kofman,
 * "A second-order approximation for DEVS simulation of continuous systems",
 * Simulation, v.78, n.2, pp.76-89, 2002.</p>
 *
 * <li> [Cellier-2006].
 * Francois E. Cellier and Ernesto Kofman,
 * "Continuous System Simulation",
 * Springer, 2006.</p>
 *
 * <li> [Migoni-2009].
 * G. Migoni and E. Kofman,
 * "Linearly implicit discrete event methods for stiff ODE's",
 * Latin American Applied Research, v.39, pp.245–254, 2009.</p>
 *
 * <li> Floros, X., et al. (2010). "Discretizing Time or States?
 * A Comparative Study between DASSL and QSS - Work in Progress Paper,"
 * Workshop on Equation-Based Object-Oriented Modeling Languages and Tools (EOOLT),
 * Oslo, Norway, Linkoping University.
 * 
 * <li> [Migoni-2013].
 * Gustavo Migoni, Mario Bortolotto, Ernesto Kofman, and Francois E. Cellier,
 * "Linearly implicit quantization-based integration methods for stiff ordinary
 * differential equations",
 * Simulation Modelling Practice and Theory, v.35, pp.118–136, 2013.</p>
 * 
 * </ol>
 *
 * @author David M. Lorenzetti, Contributor: Thierry S. Nouidui, Edward A. Lee
 * @version $id$
 * @since Ptolemy II 10.2  // FIXME: Check version number.
 * @Pt.ProposedRating red (dmlorenzetti)
 * @Pt.AcceptedRating red (reviewmoderator)  // FIXME: Fill in.
 */
public abstract class QSSBase {


    ///////////////////////////////////////////////////////////////////
    ////                         public methods

    /** Advance simulation time to the specified time.
     *  This is a convenience method that encapsulates a typical usage pattern
     *  of the other methods in this class and provides more error checking.
     *  This method will trigger rate events and quantization events if necessary.
     *  @param nextSimTime Global simulation time to which to step.
     *  @return A list of indexes of states for which new time matches
     *   a quantization event, or an empty list if there are none.
     *  @throws IllegalArgumentException If the specified time is not
     *   strictly greater than the current simulation time, or if the
     *   specified time is past the next event time, or if
     *   there are states requiring a quantization event.
     *  @throws Exception If triggering a rate event causes an error
     *   (this is dependent on the concrete implementation of this class).
     */
    public final List<Integer> advanceToTime(final Time nextSimTime)
	    throws Exception {

	Time predictedEventTime = predictQuantizationEventTimeEarliest();
	if( nextSimTime.compareTo(predictedEventTime) > 1 ) {
	    throw new IllegalArgumentException(
		    "Proposed simulation time of "
			    + nextSimTime
			    + " is past the next event time of "
			    + predictedEventTime);
	}
	// The following checks other possible error conditions,
	// triggers a rate event if needed, marks states that
	// need quantization events at the next time, and sets
	// the current simulation time to match the argument.
	stepToTime(nextSimTime);

	int index = needQuantizationEventIndex();
	List<Integer> result = new LinkedList<Integer>();
	while (index >= 0) {
	    result.add(index);
	    triggerQuantizationEvent(index);
	    index = needQuantizationEventIndex();
	}
	return result;
    }
    
    /** Initialize this solver, associating it with the specified
     *  derivativeFunction object, which determines the number of state
     *  variables and input variables and provides a method for calculating
     *  the derivatives of the state variables. This method also initializes
     *  all input and state variables to zero. These can be the initialized
     *  to some other value by calling {@link #setStateValue(int, double)} and
     *  {@link #setInputVariableModel(int, ModelPolynomial)}.
     *  The caller of this method should then, after setting state and
     *  input values, call {@link #triggerQuantizationEvents(boolean)}
     *  with argument true.
     *  <p>
     *  This is a convenience method wrapping a sequence of calls to
     *  more detailed methods.
     *  </p>
     *  
     *  @param derivativeFunction The object implementing the
     *   function that provides the derivatives for
     *   state variables that this solver is responsible for integrating.
     *   This object also provides a method specifying the number of state
     *   variables and the number of input variables.
     *  @param startTime The start time for the solver.
     *  @param maximumTime The maximum time for predicted events (e.g. the stop
     *   time of the simulation). This may be infinite.
     *  @param absoluteTolerance The absolute tolerance for all state variables
     *   (these can be modified later for individual states
     *   using {@link #setQuantizationTolerance(int, double, double)}).
     *  @param relativeTolerance The relative tolerance for all state variables
     *   (these can be modified later for individual states
     *   using {@link #setQuantizationTolerance(int, double, double)}).
     *  @param inputVariableOrder The order (the number of derivatives provided)
     *   for each input variable. If these differ by input variable, then the
     *   caller may later modify the input variable models by calling
     *   {@link QSSBase#setInputVariableModel(int, ModelPolynomial)}.
     */
    public final void initialize(
	    DerivativeFunction derivativeFunction,
	    Time startTime,
	    Time maximumTime,
	    double absoluteTolerance,
	    double relativeTolerance,
	    int inputVariableOrder) {
	initializeDerivativeFunction(derivativeFunction);
        initializeSimulationTime(startTime);
        setQuantizationEventTimeMaximum(maximumTime);
        
        // Initialize states.
        for (int i=0; i<_stateCt; i++) {
            setStateValue(i, 0.0);
            setQuantizationTolerance(0, absoluteTolerance, relativeTolerance);
        }
        
        // Create and initialize input variable models.
        for (int i = 0; i < getInputVariableCount(); i++) {
            // Create a model polynomial for the input.
            ModelPolynomial input = new ModelPolynomial(inputVariableOrder);
            // Indicate that at least one object (the specified derivativeFunction)
            // has write access and will update its values.
            input.claimWriteAccess();
            // Set the time of the input model.
            input.tMdl = startTime;
            // Associate the model polynomial with the input.
            setInputVariableModel(i, input);
        }
        
        // Validate the integrator, checking the above setup.
        final String failMsg = validate();
        assert(failMsg == null);
    }

    /** Initialize a QSS integrator to use a {@link DerivativeFunction} object.
     *
     * <p>This method must be called before doing any work with the integrator.
     * Furthermore, it can be called only once.</p>
     *
     *
     * <h2>Design intent</h2>
     *
     * <p>The derivative function is central to the integrator, and could very
     * well be passed to the constructor.
     * However, to accommodate a wide range of downstream users, a zero-argument
     * constructor was desired.
     * Of course, a constructor that takes the derivative function as an
     * argument could be provided; it would simply call this method.</p>
     *
     * @param derivFcn Object that implements the DerivativeFcn interface.
     */
    public final void initializeDerivativeFunction(final DerivativeFunction derivFcn) {

        // Check inputs.
        if( null == derivFcn ) {
            throw new IllegalArgumentException("Require a valid derivFcn");
            }

        // Check status.
        if( _derivFcn != null ) {
            throw new IllegalStateException("Method init_derivFcn() can be called only once");
        }

        _initializeDerivativeFunction(derivFcn);
        _initializeStates();
        _initializeInputVariables();
        _initializeQuanta();
        _initializeTimes();
        initializeWorker();

    }


    /** Initialize a QSS integrator with an initial time.
     *
     * <p>This method must be called before doing any work with the integrator.
     * Furthermore, it can be called only once.</p>
     *
     *
     * <h2>Design intent</h2>
     *
     * <p>For flexibility, the integrator represents time using objects of
     * class <code>Time</code> (rather than, for example, a more
     * traditional double-precision variable).
     * This leaves the implementation open to the user.
     * However, it also means that the integrator cannot assume an exact
     * form of constructor for creating <code>Time</code> objects.
     * Therefore the user has to construct and provide the initial time.</p>
     *
     * @param initSimTime The initial time.
     */
    public final void initializeSimulationTime(final Time initSimTime) {
        // FIXME: Remove the underscore.  Rename to initializeSimulationTime.
        
        // Check inputs.
        if( null == initSimTime ) {
            throw new IllegalArgumentException("Require a valid initSimTime");
        }

        // Check status.
        if( _currSimTime != null ) {
            throw new IllegalStateException("Method init_simTime() can be called only once");
        }

        _currSimTime = initSimTime;

    }


    /** Return the count of states predicted by the integrator.
     *
     * @return Count of states.
     */
    public final int getStateCount() {
        return(_stateCt);
    }


    /** Return the count of input variables to the integrator.
     *
     * @return Count of input variables.
     */
    public final int getInputVariableCount() {
        return(_ivCt);
    }

    /** Return the input variable model for the specified index.
     *  @param input The index of the input variable.
     *  @see #setInputVariableModel(int, ModelPolynomial)
     */
    public final ModelPolynomial getInputVariableModel(int input) {
	assert(input < _ivCt);
	return _ivMdls[input];
    }

    /** Get the order of the external, quantized state models exposed by the integrator.
     *
     * <p>This method returns the order of the {@link ModelPolynomial} objects
     * that the integrator exposes to the user.</p>
     *
     * <p>These states are the quantized state predictions (as opposed to the
     * continuous state predictions the integrator uses internally).
     * Therefore the order is <em>one less</em> than the nominal order of the
     * QSS method.</p>
     *
     * <p>For example, QSS1 uses a first-order (linear) polynomial to model each
     * state variable.
     * However, it does not expose that internal representation to the user.
     * The external, quantized, representation of the state is a constant (i.e.,
     * a 0th-order polynomial).
     * Therefore QSS1 should return <code>0</code> for this method.</p>
     *
     * @return Order of the external, quantized state models.
     */
    public abstract int getStateModelOrder();
        // Note would really like this to be a static method, since it should
        // not access any instance variables.  However, can't have an
        // "abstract static" method signature.


    /** Get the external, quantized state model for a state predicted by the integrator.
     *
     * <p>The QSS integrator uses this model to share the predicted state as a
     * function of time, when integrating the derivative function.</p>
     *
     * <p>The initial state model is constant at a value of 0.
     * Use method {@link #setStateValue(int, double)} to change this initial value.</p>
     *
     * <p>Never change the model parameters directly.
     * The QSS integrator claims exclusive write access to the model.</p>
     *
     *
     * <h2>Details</h2>
     *
     * <p>Notes on "write access" for the state model:</p>
     * <ul>
     * <li>"Write access" on a model means that some object has asserted that
     * it plans to change the parameters of the <code>ModelPoly</code>.
     * This means it will control the trajectory of that model over time.
     * See method ModelPoly.claimWriteAccess().</li>
     * <li>The QSS integrator will change the model parameters as it integrates
     * the derivative function.
     * Therefore the integrator asserts write access on its own state models.</li>
     * <li>The QSS integrator requires exclusive write access on its own
     * state models.
     * In order for the integrator to run, every state model must have exactly
     * one claim of "write access" made against it.
     * Therefore the user must not assert write access on any state model.</li>
     * <li>The user should <em>never</em> write new parameters to the
     * state model.</li>
     * <li>The user should call method {@link #setStateValue(int, double)}, in order
     * to initialize the state, <em>before</em> starting the integration.</li>
     * </ul>
     *
     * <p>Notes on sharing models between roles in the integration:</p>
     * <ul>
     * <li>It is legal to use a state model as an input variable model of one
     * or more QSS integrators.
     * The model can even be added as an input variable model of the same
     * integrator for which it is a state model (although this should be
     * unusual).</li>
     * <li>It is legal to add the same model to multiple input variables.</li>
     * </ul>
     *
     *
     * <h2>Design intent</h2>
     *
     * <p>The design intention behind exposing the state models directly to the
     * rest of the system is to make sharing state predictions as cheap as
     * possible.</p>
     *
     * <p>From an encapsulation viewpoint, the integrator does not have to
     * expose its state models to the rest of the simulation.
     * It could, instead, force the user to evaluate the quantized state models
     * using method {@link #evaluateStateModel(int, Time)}.
     * Alternately, it could copy the quantized state model to a user-supplied
     * model object, thus keeping the integrator's private copy hidden.
     * However, both these approaches are relatively high overhead, compared
     * to simply exposing the model for the user to evaluate as needed.</p>
     *
     * @param stateIdx The state index, 0 &le; stateIdx &lt; this.getStateCt().
     * @param qStateMdl The model to use.
     */
    public final ModelPolynomial getStateModel(final int stateIdx) {
        return( _qStateMdls[stateIdx] );
    }


    /** Return the index of an input variable for which the user has yet to add a model.
     *
     * <p>The user must call {@link #setInputVariableModel(int, ModelPolynomial)} at least once for
     * every input variable taken by the derivative function.
     * This method checks whether that requirement has been met.</p>
     *
     * @return Index of an input variable for which the user has yet to add a
     *   model, 0 &le; idx &le; this.getArgCt().  Return -1 if all models have been
     *   added (or if the derivative function takes no input variables).
     */
    public final int needInputVariableModelIndex() {

        // Initialize.
        int missingIdx = -1;

        for( int ii=0; ii<_ivCt; ++ii ) {
            if( _ivMdls[ii] == null ) {
                missingIdx = ii;
                break;
            }
        }

        return( missingIdx );

    }


    /** Set the model for an input variable to the derivative function.
     *
     * <p>Add a {@link ModelPolynomial} for an input variable to the
     * derivative function.
     * The QSS method will use this model to evaluate the input variable as a
     * function of time, when integrating the derivative function.</p>
     *
     * <p>The user must call this method at least once for every input variable
     * taken by the derivative function.
     * This must be done before starting the integration.</p>
     *
     *
     * <h2>Details</h2>
     *
     * <p>Notes on "write access" for the state model:</p>
     * <ul>
     * <li>"Write access" means that some object has asserted that it plans to
     * change the parameters of the {@link ModelPolynomial}.
     * This means it will control the trajectory of that model over time.
     * See {@link ModelPolynomial#claimWriteAccess()}.</li>
     * <li>The user of each integrator is responsible for setting the parameters
     * of each input variable model (i.e., of changing the trajectory of the
     * input variable over time).</li>
     * <li>That said, it is OK for the user to delegate the task of changing
     * the input model parameters to some other agent.
     * For example, an input variable can be the state predicted by an
     * integrator.</li>
     * <li>In order for the integrator to run, every input variable model must
     * have exactly one claim of "write access" made against it.
     * If the input variable model is also a state model (for this or any other
     * QSS integrator), then the QSS integrator will automatically make that
     * claim.
     * Otherwise, the user may have to make that claim.</li>
     * <li>Note that the QSS integrator should never change the parameters of a
     * model added as an input variable.
     * If it does, this is a bug.</li>
     * </ul>
     *
     * <p>Notes on selecting the input variable model order:</p>
     * <ul>
     * <li>The input variable model may have any valid order.</li>
     * <li>In particular, its order does not need to match that of the
     * QSS method for which it serves as an input variable.</li>
     * </ul>
     *
     * <p>Notes on sharing models between roles in the integration:</p>
     * <ul>
     * <li>See the notes for method {@link #getStateModel(int)}.</li>
     * </ul>
     *
     * <p>Notes on providing a different model at a later time:</p>
     * <ul>
     * <li>It is OK to change the input variable model before starting the
     * integration (e.g., to change the model's order).</li>
     * <li>It is probably <em>not</em> OK to change the model after starting
     * the integration.
     * That is, even if the integrator appears to work, this capability is not
     * a design goal, and may be lost in the future.</li>
     * </ul>
     *
     * @param ivIdx The index of input variable, 0 &le; ivIdx &lt; this.getInputVarCt().
     * @param ivMdl The model to use.
     * @throws IllegalArgumentException If the argument is null.
     * @see #getInputVariableModel(int)
     */
    public final void setInputVariableModel(final int ivIdx, final ModelPolynomial ivMdl) {

        // Check inputs.
        if( ivMdl == null ) {
            // Remove any model already at index {ivIdx}.
            //   Purpose-- leave the integrator unable to run until the root
            // cause of this problem has been fixed.
            _ivMdls[ivIdx] = null;
            throw new IllegalArgumentException("Require a valid model");
        }

        // Save reference to model.
        _ivMdls[ivIdx] = ivMdl;
    }  


     // TODO: Add method that allows user to indicate when a state variable is not
     // actually used by the derivative function.
     // This will allow efficiency when experience a quantization-event for that
     // state (because won't then have to trigger a rate-event for the same component).


    /** Set the parameters used to determine the quantum for a state.
     * <p>
     * The quantum for each state variable gets calculated as
     * </p><p>
     *       q[j] = max{Ta, Tr*abs{x[j]}}
     * </p><p>
     * <p>where</p>
     * <ul>
     * <li> q[j] is the quantum for element j,</li>
     * <li> Ta is the absoluteTolerance,</li>
     * <li> Tr is the relativeTolerance, and </li>
     * <li> x[j] is the value of element j the last time a new state
     *      model was formed.</li>
     * </ul>
     *
     * <p>This method sets the tolerances used to find the quantum.
     * It also updates the quantum to reflect the new tolerances.</p>
     *
     * @param stateIndex The state index, 0 &le; stateIdx &lt; this.getStateCt().
     * @param absoluteTolerance The absolute tolerance, which is required to be &gt; 0 [units of <i>x[j]</i>].
     * @param relativeTolerance The relative tolerance, which is required to be &ge; 0 [1].
     * @throws IllegalArgumentException If the absolute tolerance is not strictly positive, or
     *  if the relative tolerance is negative.
     */
    public final void setQuantizationTolerance(
	    final int stateIndex, final double absoluteTolerance, final double relativeTolerance) {

        // Check inputs.
        if( absoluteTolerance <= 0 ) {
            throw new IllegalArgumentException("Require absoluteTolerance > 0; got "
        	    + absoluteTolerance);
        }
        if( relativeTolerance < 0 ) {
            throw new IllegalArgumentException("Require relativeTolerance >= 0; got "
        	    + relativeTolerance);
        }

        // Set status to note future needs.
        _need_quantEvts[stateIndex] = true;

        // Change tolerances.
        _dqAbsTols[stateIndex] = absoluteTolerance;
        _dqRelTols[stateIndex] = relativeTolerance;
        _dqs[stateIndex] = findDq(stateIndex);

    }  


    /** Set the parameters used to determine the quantum for all states.
     *
     * <p>Apply the same tolerances to all the states the integrator predicts.
     * For details, see method {@link #setQuantizationTolerance(int, double, double)}.</p>
     *
     * @param absoluteTolerance The absolute tolerance, which is required to be &gt; 0 [units of <i>x[j]</i>].
     * @param relativeTolerance The relative tolerance, which is required to be &ge; 0 [1].
     */
    public final void setQuantizationTolerances(
	    final double absoluteTolerance, final double relativeTolerance) {
        for( int ii=0; ii<_stateCt; ++ii ) {
            setQuantizationTolerance(ii, absoluteTolerance, relativeTolerance);
        }
    }


    // TODO: Add method to install "listeners" that help check whether had
    // rate-event.  A listener can be one of three levels:
    // (1) Can not have a listener installed.  This means the integrator keeps
    // track of its own changes in the quantized state (which feed back as
    // deriv fcn args).  User is entirely responsible for re-evaluating
    // models.  That is what currently have.
    // (2) Can install a listener that only pays attention to the time
    // element of a model.  It decides whether a model changed by just looking
    // at its time component.
    // (3) Can install a listener that pays attention to all parameters of
    // the model.  It decides whether a model changed by examining every
    // model parameter.  Why would you do this?  Might want to chase changes
    // around a loop-- a model starts out having just the state correct, then
    // in response this integrator requantizes, then in response another
    // integrator requantizes, which changes the slope of its model.  So then
    // that derivative function argument changes its first derivative coefficient,
    // but not its time or its constant coefficient.  And in that case, would
    // need the listener to pay attention to all the model coefficients.


    /** Set or reset the integrator's current time.
     *  This method sets flags indicating that a rate event is needed
     *  and that quantization events are needed for all states.
     *  @param newSimTime New time for the QSS integrator.
     */
    public final void setCurrentSimulationTime(final Time newSimTime) {
        // Set status to note future needs.
        _need_rateEvt = true;
        for( int ii=0; ii<_stateCt; ++ii ) {
            _need_quantEvts[ii] = true;
        }

        _currSimTime = newSimTime;
    }


    /** Set the value of a state variable.
     *
     * <p>Change the value component of the model for element <i>stateIdx</i>
     * of the state vector.
     * Note that this re-initializes the integrator for that component.
     * That is, it discards any state models that the integrator might have
     * formed, and creates a jump discontinuity in the state variable.
     * This has the side effect of setting flags indicating that a
     * new rate event is needed and that the specified state needs
     * a quantization event.</p>
     *
     *
     * <h2>Warning</h2>
     *
     * <p>The user may be tempted to set the value of the state variable
     * directly in the state model, without going through the QSS integrator.
     * However, doing so prevents the integrator from making the appropriate
     * internal adjustments to the change in state.</p>
     *
     * @param stateIdx The state index, 0 <= stateIdx < this.getStateCt().
     * @param newValue The new value of x[stateIdx].
     */
    public final void setStateValue(final int stateIdx, final double newValue) {

        // Set status to note future needs.
        _need_rateEvt = true;
        _need_quantEvts[stateIdx] = true;
        // _need_predQuantizationEventTimes[stateIdx] = true;  // This will follow from changes above.

        // Make the quantized state model constant at {newValue}.
        final ModelPolynomial qStateMdl = _qStateMdls[stateIdx];
        _makeModelConstant(qStateMdl, newValue, _qStateMdlOrder);

        // Make the continuous state model constant at {newValue}.
        _makeModelConstant(_cStateMdls[stateIdx], newValue, _qStateMdlOrder+1);

        // Update quantum.
        //   To keep consistent with the constant coefficient of the
        // external, quantized state model.
        _dqs[stateIdx] = findDq(stateIdx);
    } 

    // TODO: Consider adding a method that sets all the state variables from a vector.


    /** Reset the maximum time for predicted quantization-events.
     *
     * <p>The integrator will not predict quantization-event times past this
     * time.
     * Default value <code>Time.POSITIVE_INFINITY</code>.</p>
     *
     * @param quantEvtTimeMax The maximum time for predicted quantization-events.
     */
    public final void setQuantizationEventTimeMaximum(final Time quantEvtTimeMax) {

        // Check inputs.
        if( quantEvtTimeMax.getDoubleValue() <= 0 ) {
            throw new IllegalArgumentException("Require quantEvtTimeMax>0");
        }

        // Set status to note future needs.
        for( int ii=0; ii<_stateCt; ++ii ) {
            _need_quantEvts[ii] = true;
        }

        _quantEvtTimeMax = quantEvtTimeMax;

    }


    /** Validate the QSS integrator has been properly set up.
     *
     * <p>This method diagnoses setup problems with the integrator.
     * For example, if running the integrator causes a <code>NullPointerException</code>,
     * then this method can pinpoint problems originating with the integrator.</p>
     *
     * <p>It is <em>not</em> necessary to run this method in order to run the
     * integrator.</p>
     *
     * @return `null` if the integrator is ready to be used in a simulation, or
     *   an error message diagnosing the problem.
     */
    public final String validate() {
        return( _validate() );
    }


    /** Get the current simulation time for the QSS integrator.
     *
     * <p>This is generally the last global simulation time for which
     * method {@link #stepToTime(Time)} was called.
     * Exceptions:</p>
     * <ul>
     * <li>When the integrator is first instantiated, it is set to 0.</li>
     * <li>Method {@link #setCurrentSimulationTime(Time)} changes the value outright.</li>
     * </ul>
     *
     * @return Current simulation time for the QSS integrator.
     */
    public final Time getCurrentSimulationTime() {
        return( _currSimTime );
    }


    /** Get the value of a state variable.
     *
     * <p>Evaluate the external, quantized state model at a specified time.</p>
     *
     * <p>Note this method evaluates the external, quantized state model.
     * Alternately, the user could acquire the model, via
     * method {@link #getStateModel(int)}, and evaluate that model directly.</p>
     *
     * @param stateIdx The state index, 0 <= stateIdx < this.getStateCt().
     * @param simTime Global simulation time.
     * @return Value of the state model at <code>simTime</code>.
     */
    public final double evaluateStateModel(final int stateIdx, final Time simTime) {
        return( _qStateMdls[stateIdx].evaluate(simTime) );
    }


    /** Get the internal value of a state variable.
     *
     * <p>Evaluate the internal, continuous state model at a specified time.</p>
     *
     * @param stateIdx The state index, 0 <= stateIdx < this.getStateCt().
     * @param simTime Global simulation time.
     * @return Value of the state model at <code>simTime</code>.
     */
    public final double evaluateStateModelContinuous(final int stateIdx, final Time simTime) {
        return( _cStateMdls[stateIdx].evaluate(simTime) );
    }


    /** Return the first index of a state that needs a quantization-event.
     * This method can be called after {@link #stepToTime(Time)} repeatedly
     * to iterate over the states on which {@link #triggerQuantizationEvent(int)}
     * should be called. See {@link #advanceToTime(Time)} for a typical usage
     * pattern.
     * <p>
     * The integrator tracks which states need a quantization-event as a
     * result of a time step.
     * This method returns the index, if any, of such states.
     * </p><p>
     * The user should trigger the quantization-event, e.g., using
     * method {@link #triggerQuantizationEvent(int)}.
     * </p><p>
     * TODO: Put under unit test.</p>
     *
     * @return Index of a state that needs a quantization-event,
     *   0 <= idx < this.getStateCt().  Return -1 if all external, quantized
     *   state models are valid.
     */
    public final int needQuantizationEventIndex() {
        for( int ii=0; ii<_stateCt; ++ii ) {
            if( _need_quantEvts[ii] ) {
                return ii;
            }
        }
        return( -1 );
    }  

    /** Return array of booleans indicating all states that need a quantization-event.
     *
     * <p>See comments to method {@link #needQuantizationEventIndex()}.</p>
     *
     * <p>TODO: Put under unit test.</p>
     *
     * @param needQuantEvtIdxs (output) Vector showing <code>true</code> for
     *   each integrator state that needs a quantization-event.
     */
    public final void needQuantizationEventIndexes(final boolean[] needQuantEvtIdxs) {
        System.arraycopy(_need_quantEvts, 0, needQuantEvtIdxs, 0, _stateCt);
    }


    /** Form a new external, quantized state model.
     *
     * <p>Force the QSS integrator to form a new external, quantized state model
     * (i.e., to experience a quantization-event).
     * The new model will be available to the user immediately.</p>
     *
     * <p>Note method {@link #triggerQuantizationEvents(boolean)} can requantize multiple
     * state models at once, and can requantize only those states that need it.</p>
     *
     * <p>Form the model about the current simulation time, as returned by
     * method {@link #getCurrentSimulationTime()}.</p>
     *
     * <p>Note this method can be invoked even if the external, quantized state model
     * is still within quantum of the internal, continuous state model.
     * The integrator will still update the quantized state model about the
     * current time.
     * Doing so can only improve the accuracy of the simulation (at the cost
     * of some extra processing).</p>
     *
     * <p>Note the state model of interest here is the
     * external, quantized state model.
     * In order to re-form the internal, continuous state model, use
     * method {@link #triggerRateEvent()}.</p>
     *
     * <p>The proper sequence in which to call method {@link #triggerRateEvent()}
     * and method {@link #triggerQuantizationEvent(int)} is a fraught topic.
     * In general, should requantize all states first, then trigger rate events.
     * Also, after triggering a rate event, get the new predicted quantization time.
     * TODO: Write up a higher-level description of the problem.</p>
     *
     * @param stateIdx The state index, 0 <= stateIdx < this.getStateCt().
     */
    public final void triggerQuantizationEvent(final int stateIdx) {

        // Set status to note future needs.
        _need_rateEvt = true;
        _need_predQuantEvtTimes[stateIdx] = true;

        // Perform work defined by specific member of the QSS family.
        _triggerQuantizationEventWorker(stateIdx);

        // Update quantum.
        //   To keep consistent with the constant coefficient of the
        // external, quantized state model.
        _dqs[stateIdx] = findDq(stateIdx);

        // Set status to note satisfied needs.
        _need_quantEvts[stateIdx] = false;
    }  


    /** Form new external, quantized state models.
     *
     * <p>Convenience method to call method {@link #triggerQuantizationEvent(int)} on
     * all states predicted by this integrator.</p>
     *
     * <p>Can apply only to those states that are marked for requantization,
     * or can apply to all states.</p>
     *
     * <p>Note that a state gets marked for requantization:</p>
     * <ul>
     * <li>At initialization.</li>
     * <li>When a time step carries the integrator up to, or past, its predicted
     * quantization-event time.
     * See method {@link #predictQuantizationEventTime(int)}.</li>
     * <li>TODO: Provide, probably in top-level comments, an overview of when
     * an integrator state needs to have a quantization-event.
     * Following this list.
     * Then just cross-reference that discussion here, and in places like
     * description of method {@link #triggerRateEvent()}.</p>
     * </ul>
     *
     * <p>To determine state(s) that need to be requantized, use either
     * method {@link #needQuantizationEventIndex()} or
     * method {@link #needQuantizationEventIndexes(boolean[])}.</p>
     *
     * @param forceAll If true, requantize all state models.
     */
    public final void triggerQuantizationEvents(final boolean forceAll) {

        for( int ii=0; ii<_stateCt; ++ii ) {
            if( forceAll || _need_quantEvts[ii] ) {
                triggerQuantizationEvent(ii);
            }
            assert( _need_quantEvts[ii] == false );
        }

    }


    /** Determine whether the integrator needs a rate-event.
     *
     * <p>This method only checks whether the integrator needs a rate-event
     * due to a quantization-event.
     * The integrator does not track changes in the parameters of its
     * input variable models, so the integrator cannot warn of the need to
     * trigger a rate-event after an input variable changes.</p>
     *
     * TODO: Statement above will change if provide ability to install listeners
     * on input variables.
     */
    public final boolean needRateEvent() {
        return(_need_rateEvt);
    }


    /** Form new internal, continuous state models.
     *
     * <p>Force the QSS integrator to form new internal, continuous state models
     * (i.e., to experience a rate-event).
     * The rate models also get updated.</p>
     *
     * <p>In general, this needs to be done whenever an argument to the
     * derivative function has changed.
     * A "change" in an argument to the derivative function means a change in
     * any of the parameters to an argument's model.
     * This may happen, for example, due to a quantization-event in the
     * integrator that predicts an argument to this integrator.
     * It may also happen due to a change in a boundary condition, for example
     * provided by an external file.</p>
     *
     * <p>Form the model about the current simulation time, as returned by
     * method {@link #getCurrentSimulationTime()}.</p>
     *
     * <p>Note this method can be invoked even if no argument to the
     * derivative function has had a change in its parameters.
     * The integrator will still update the rate and state models about the
     * current time.
     * Doing so can only improve the accuracy of the simulation (at the cost
     * of some extra processing).</p>
     *
     * <p>Note the state model of interest here is the
     * internal, continuous state model.
     * In order to re-form the external, quantized state model, use
     * method {@link #triggerQuantizationEvent(int)}.</p>
     *
     * <p>The proper sequence in which to call method {@link #triggerRateEvent()}
     * and method {@link #triggerQuantizationEvent(int)} is a fraught topic.
     * In general, should requantize all states first, then trigger rate-events.
     * Also, after trigger a rate-event, get new predicted quantization-time.
     * TODO: Write up a higher-level description of the problem.</p>
     *
     *
     * <h2>Implementation notes</h2>
     *
     * <p>The way to handle a rate-event varies depending on the particular
     * QSS method.
     * Therefore this is an abstract method.
     * Subclasses are expected take care of the following:</p>
     * <ul>
     * <li>Reset flags.</li>
     * <li>Make consistent with the quantized state model, if there was also
     * a quantization-event.
     * Assuming that's possible.
     * Probably better to phrase this in the inverse-- that method quantize()
     * should be sure to refresh the state model, since in this implementation
     * a quantization-event implies a rate-event.</li>
     * <li>TODO: Finish out this list.</li>
     * </ul>
     *
     * <p>Note it's tempting to say should that handling a rate-event should
     * force a quantization-event if one is needed, before handle any rate-event.
     * The logic being that the quantization-event will then induce another
     * rate-event that needs to be handled, anyway.
     * The problem with that logic is that it imposes a policy on how to deal
     * with potential loops in updating cycles.
     * Thus it removes the user's ability to control when quantization happens,
     * which might be important.
     * Of course, if there are no loops, then it is certainly best to requantize
     * before finding new rate and state models.
     * That's because requantizing, always creates a rate-event (since quantized
     * outputs are always arguments to the derivative function).
     *
     * TODO: Consider returning integer status, equal to return status of
     * the derivative function (which should be zero if successful).  Then get
     * rid of the exception.
     *
     * TODO: Add a "force" flag so only triggers if needed.
     */
    public final void triggerRateEvent()
        throws Exception {

        // Set status to note future needs.
        for( int ii=0; ii<_stateCt; ++ii ) {
            _need_predQuantEvtTimes[ii] = true;
        }

        // Perform work defined by specific member of the QSS family.
        _triggerRateEventWorker();

        // Set status to note satisfied needs.
        _need_rateEvt = false;
    }  

    // TODO: Add method to allow user to "mark for handling a rate-event".  This
    // would tell the integrator that a rate-event happened, but allow the
    // integrator to defer handling the rate-event until ready to take a time step.
    //   The purpose of doing this is that you might know an integrator will have
    // a rate-event, but not yet have found all the new inputs that will affect
    // that update operation.  So you may want to just tell it to handle them when
    // you tell it to step.  Still, this seems excessive, like unnecessary work
    // to check the status flags (compared to just having the user tell it to
    // update when desired).  Because there are lots of interactions between the
    // udpated state models and the requantizations and the predicted
    // next quantization-event time.
    //   Another way to do this might be to add a flag to triggerRateEvt(),
    // telling it to requantize first if necessary.  And a corresponding flag
    // to triggerQuantizationEvent(int), telling it to handle rate-event at same time if
    // necessary.


    /** Get the predicted quantization-event time for a state.
     *
     * TODO: Get this method under unit test.
     *
     * @param stateIdx The state index, 0 <= stateIdx < this.getStateCt().
     * @return Next time at which, in the absence of other events, the
     *   external state model must be re-formed,
     *   0 <= time <= Time.POSITIVE_INFINITY.
     */
    public final Time predictQuantizationEventTime(final int stateIdx) {

        Time predQuantEvtTime;

        if( _need_predQuantEvtTimes[stateIdx] ) {
            // Perform work defined by specific member of the QSS family.
            predQuantEvtTime = _predictQuantizationEventTimeWorker(stateIdx, _quantEvtTimeMax);
            assert(
                predQuantEvtTime.compareTo(_cStateMdls[stateIdx].tMdl) > 0
                ||
                predQuantEvtTime.compareTo(_quantEvtTimeMax)==0
                );
            _predQuantEvtTimes[stateIdx] = predQuantEvtTime;
            _need_predQuantEvtTimes[stateIdx] = false;
        } else {
            predQuantEvtTime = _predQuantEvtTimes[stateIdx];
        }

        return( predQuantEvtTime );
    }  

    /** Get the earliest predicted quantization-event time for all states.
     *
     * TODO: Get this method under unit test.
     *
     * @return Earliest predicted quantization-event time from among all states
     *   predicted by the integrator.
     */
    public final Time predictQuantizationEventTimeEarliest() {

        // Initialize.
        Time predQuantEvtTime = predictQuantizationEventTime(0);

        // Run through remaining elements.
        for( int ii=1; ii<_stateCt; ++ii ) {
            final Time newTime = predictQuantizationEventTime(ii);
            if( newTime.compareTo(predQuantEvtTime) < 0 ) {
                predQuantEvtTime = newTime;
            }
        }

        return( predQuantEvtTime );
    }  


    /** Get the earliest predicted quantization-event time for all states.
     *
     * TODO: Get this method under unit test.
     *
     * @param quantEvtElts (output) Vector showing <code>true</code> for those
     *   elements whose predicted quantization-event time is the minimum
     *   from among all elements.  At least one such element must be marked.
     * @return Earliest predicted quantization-event time from among all states
     *   predicted by the integrator.
     */
    public final Time predictQuantizationEventTimeEarliest(final boolean[] quantEvtElts) {

        // Initialize.
        final Time predQuantEvtTime = predictQuantizationEventTimeEarliest();

        // Mark matching elements.
        for( int ii=0; ii<_stateCt; ++ii ) {
            assert(_need_predQuantEvtTimes[ii] == false);
            if( predQuantEvtTime.compareTo(_predQuantEvtTimes[ii]) == 0 ) {
                quantEvtElts[ii] = true;
            } else {
                quantEvtElts[ii] = false;
            }
        }

        return( predQuantEvtTime );

    } 


    /** Step to the next knot in the global simulation.
     *
     * <p>Don't complain if stepping past a predicted quantization-event time.
     * Just report need to trigger a quantization-event before the next step.</p>
     *
     * @param nextSimTime Global simulation time to which to step,
     *   nextSimTime > this.getCurrSimTime().
     */
    public final void stepToTime(final Time nextSimTime)
        throws Exception {

        // Check inputs.
        if( nextSimTime.compareTo(_currSimTime) < 1 ) {
            throw new IllegalArgumentException("Simulation time must advance");
        }

        // Check status.
        if( needQuantizationEventIndex() != -1 ) {
            throw new IllegalStateException("State models waiting to be quantized");
        }

        // Update state models if necessary.
        if( _need_rateEvt ) {
            triggerRateEvent();
            assert( _need_rateEvt == false );
        }

        // Determine which, if any, state models will require requantization at
        // the end of this step.
        for( int ii=0; ii<_stateCt; ++ii ) {
            assert( _need_quantEvts[ii] == false );
            final Time predQuantEvtTime = predictQuantizationEventTime(ii);
            if( predQuantEvtTime.compareTo(nextSimTime) <= 0 ) {
                _need_quantEvts[ii] = true;
            }
        }

        // Take step.
        _currSimTime = nextSimTime;

        // Note there is no need to update the internal, continuous state models
        // to reflect the step just took.  Those models only need to be updated
        // when there's a rate-event.  If the step induces a rate-event, will
        // expect user to call method triggerRateEvt().

    } 


    /** Get the internal, continuous state model for a state predicted by the integrator.
     *
     * <p>This method is the equivalent of method {@link #getStateModel(int)},
     * except that it retrieves the internal, continuous state model rather
     * than the external, quantized state model.</p>
     *
     * <p>This method is provided primarily for testing.
     * In principle, the user should not even have to be aware of the existence
     * of the internal, continuous state model, let alone have access to it.</p>
     *
     * @param stateIdx The state index, 0 <= stateIdx < this.getStateCount().
     * @param qStateMdl The model to use.
     */
    // public final ModelPoly getStateMdl_cont(final int stateIdx) {
    //     return( _cStateMdls[stateIdx] );
    // }


    /** Get a string representation of the model for a state.
     *
     * <p>Invoke method ModelPoly.toString() on the
     * external, quantized state model.</p>
     *
     * @param stateIdx The state index, 0 <= stateIdx < this.getStateCt().
     */
    public final String stringifyStateModel(final int stateIdx) {
        return( _qStateMdls[stateIdx].toString() );
    }


    /** Get a string representation of the internal model for a state.
     *
     * <p>Invoke method ModelPoly.toString() on the
     * internal, continuous state model.</p>
     *
     * @param stateIdx The state index, 0 <= stateIdx < this.getStateCt().
     */
    public final String stringifyStateModelContinuous(final int stateIdx) {
        return( _cStateMdls[stateIdx].toString() );
    }


    /** Find the quantum for a state.
     *
     * <p>Finds the quantum, i.e., the maximum allowable difference between
     * the external, quantized state model shared with the user, and the
     * internal, continuous state model used by the integrator.</p>
     *
     * <p>To change the parameters used to find the quantum, use
     * method {@link #setQuantizationTolerance(int, double, double)} or method {@link #setQuantizationTolerances(double, double)}.</p>
     *
     * <p>The user should never have to call this method directly.
     * The QSS integrator invokes it as needed.</p>
     *
     *
     * <h2>Implementation notes</h2>
     *
     * <p>This method does not store the quantum calculated.
     * The QSS integrator is expected to take care of this.</p>
     *
     * <p>The nominal policy is to find the quantum whenever the
     * external, quantized state model gets updated.
     * This is because, as calculated here, the quantum depends on the constant
     * coefficient of the external, quantized state model.</p>
     *
     * <p>In principle, the quantum could be based on the current value of the
     * one of the state models, calculated at the time the quantum is needed.
     * This differs from the policy outlined above, in that the current value
     * of a state generally differs from its constant coefficient.</p>
     *
     * <p>However, this more complicated policy would induce a lot of mostly
     * needless calculations.
     * If the external, quantized state changes by a large amount, but still
     * tracks the internal, continuous state model well, then the quantum for
     * this element doesn't matter much.
     * If, on the other hand, the two models don't agree, then there will be a
     * quantization-event, which will trigger a new call of this method.</p>
     *
     * @param stateIdx The state index, 0 <= stateIdx < this.getStateCt().
     * @return Quantum for the state variable.
     */
    public final double findDq(final int stateIdx) {

        final double stateVal = _qStateMdls[stateIdx].coeffs[0];
        double dq = _dqRelTols[stateIdx]*Math.abs(stateVal);
        final double dqAbs = _dqAbsTols[stateIdx];
        if( dq < dqAbs ) {
            dq = dqAbs;
        }
        assert( dq > 0 );

        return( dq );

    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods


    /** Initialize object fields (QSS-specific).
     *
     * <p>Perform one-time initializations at the beginning of the object
     * lifetime.</p>
     *
     * <p>The implementation of this "worker" method depends on the
     * specific member of the QSS family.</p>
     */
    protected abstract void initializeWorker();


    /** Form a new external, quantized state model (QSS-specific).
     *
     * <p>See comments to method {@link #triggerQuantizationEvent(int)}.</p>
     *
     * <p>The implementation of this "worker" method depends on the
     * specific member of the QSS family.</p>
     *
     * @param stateIdx The state index, 0 <= stateIdx < this.getStateCount().
     */
    protected abstract void _triggerQuantizationEventWorker(final int stateIdx);


    /** Form new internal, continuous state models (QSS-specific).
     *
     * <p>See comments to method {@link #triggerRateEvent()}.</p>
     *
     * <p>The implementation of this "worker" method depends on the
     * specific member of the QSS family.</p>
     */
    protected abstract void _triggerRateEventWorker()
        throws Exception;


    /** Get the predicted quantization-event time for a state (QSS-specific).
     *
     * <p>See comments to method {@link #predictQuantizationEventTime(int)}.</p>
     *
     * <p>The implementation of this "worker" method depends on the
     * specific member of the QSS family.</p>
     *
     *
     * <h2>Implementation notes</h2>
     *
     * <p>The method should not alter any instance variables.</p>
     *
     * @param stateIdx The state index, 0 <= stateIdx < this.getStateCt().
     * @param quantEvtTimeMax The maximum time for the return value.  May be
     *   Time.POSITIVE_INFINITY.
     * @return Next time at which, in the absence of other events, the
     *   external state model must be re-formed, time <= quantEvtTimeMax.
     */
    protected abstract Time _predictQuantizationEventTimeWorker(
        final int stateIdx, final Time quantEvtTimeMax);


    /** Get the delta-time to the predicted quantization-event for a state under QSS2.
     *
     * <p>Utility method for use by {@link #_predictQuantizationEventTimeWorker()}.</p>
     *
     * <p>Find the time step, from the most recent quantization-event time, of the
     * predicted quantization-event for a state under QSS2.
     * Assume the quantized state model was derived from the
     * continuous state model, and therefore has the same value and slope at
     * the quantization-event time.</p>
     *
     * <p>TODO: Put this method under direct unit test.
     * Currently tested indirectly, through method {@link #_predictQuantizationEventTimeWorker()}
     * of each solver.
     * Testing directly will make it easier to check results, and will make it
     * easier to add testing for slope-aware quant-evt predictions.</p>
     *
     * @param cStateMdl The model of internal, continuous state.
     * @param qStateMdl The model of external, quantized state.
     * @param dq The quantum, i.e., the critical difference between the models, at
     *   which the external state model must be re-formed.
     * @return dt The delta-time at which, in the absence of other events, the
     *   external state model must be re-formed.
     *   Note 0 <= dt <= Double.POSITIVE_INFINITY.
     *   A value of 0 means need a quantization-event as soon as possible.
     */
    protected final static double _predictQuantizationEventDeltaTimeQSS2QFromC(
        final ModelPolynomial qStateMdl, final ModelPolynomial cStateMdl, final double dq) {

        // Check internal consistency.
        //   QSS2 uses linear quantized state model, and quadratic
        // continuous state model.  Allow higher-order solvers to call this
        // method also.
        assert( qStateMdl.getMaximumOrder() >= 1 );
        assert( cStateMdl.getMaximumOrder() == qStateMdl.getMaximumOrder()+1 );
        assert( qStateMdl.tMdl.compareTo(cStateMdl.tMdl) > 0 );  // Require {qStateMdl} more recent.
        assert( dq > 0 );

        double dt;

        // Initialize.
        final double qea = cStateMdl.coeffs[2];  // Note this is 1/2 the slope of the rate model.

        if( qea != 0 ) {
            // Here, the internal, continuous state model has curvature.
            dt = Math.sqrt( dq / Math.abs(qea) );
        } else {
            // Here, the continuous state model may be a line or a constant.
            //   For a line, fall back on method from QSS1.
            final double cStateDeriv = cStateMdl.coeffs[1];
            if( cStateDeriv != 0 ) {
                dt = dq / Math.abs(cStateDeriv);
            } else {
                // Here, the continuous state model is a constant.
                // Since the quantized state model was formed based on that
                // model, it too is a constant with the same value.
                dt = Double.POSITIVE_INFINITY;
            }
        }

        return( dt );

    }  


    /** Get the delta-time to the predicted quantization-event for a state under QSS2.
     *
     * <p>Utility method for use by {@link #_predictQuantizationEventTimeWork()}.</p>
     *
     * <p>Find the time step, from the most recent state-event time, of the
     * predicted quantization-event for a state under QSS2.
     * Do not assume the quantized state model bears any particular relationship
     * to the continuous state model.</p>
     *
     * <p>TODO: Put this method under direct unit test.
     * Currently tested indirectly, through method {@link #_predictQuantizationEventTimeWork()}
     * of each solver.
     * Testing directly will make it easier to check results, and will make it
     * easier to add testing for slope-aware quant-evt predictions.</p>
     *
     * @param cStateMdl The model of internal, continuous state.
     * @param qStateMdl The model of external, quantized state.
     * @param dq The quantum, i.e., the critical difference between the models, at
     *   which the external state model must be re-formed.
     * @return dt The delta-time at which, in the absence of other events, the
     *   external state model must be re-formed.
     *   Note 0 <= dt <= Double.POSITIVE_INFINITY.
     *   A value of 0 means need a quantization-event as soon as possible.
     */
    protected final static double _predictQuantizationEventDeltaTimeQSS2General(
        final ModelPolynomial qStateMdl, final ModelPolynomial cStateMdl, final double dq) {

        // Check internal consistency.
        //   QSS2 uses linear quantized state model, and quadratic
        // continuous state model.  Allow higher-order solvers to call this
        // method also.
        assert( qStateMdl.getMaximumOrder() >= 1 );
        assert( cStateMdl.getMaximumOrder() == qStateMdl.getMaximumOrder()+1 );
        assert( dq > 0 );

        double dt;

        // Get coefficients of the quadratic equation that defines the step.
        final double hStar = qStateMdl.tMdl.subtractToDouble(cStateMdl.tMdl);
        final double qecConst =
            cStateMdl.coeffs[0] - qStateMdl.coeffs[0] + hStar*qStateMdl.coeffs[1];
        final double qeb =
            cStateMdl.coeffs[1] - qStateMdl.coeffs[1];
        final double qea =
            cStateMdl.coeffs[2];  // Note this is 1/2 the slope of the rate model.
        if( Math.abs(qecConst) >= dq ) {
            // Here, last step had a numerical problem that let it violate the quantum.
            //   Initiate a quantization-event as early as possible.
            dt = 0;
        } else if( qea==0 && qeb==0 ) {
            // Note that an alternate approach when {qea==0} would be to use
            // the QSS1 solution.  However, leave that decision up to caller.
            dt = Double.POSITIVE_INFINITY;
        } else {
            final double dtAddDq = PolynomialRoot.findMinimumPositiveRoot2(qea, qeb, qecConst+dq);
            assert( dtAddDq >= 0 );
            final double dtSubDq = PolynomialRoot.findMinimumPositiveRoot2(qea, qeb, qecConst-dq);
            assert( dtSubDq >= 0 );
            if( dtAddDq>0
                &&
                (dtSubDq<=0 || dtAddDq<dtSubDq)
                ) {
                // Here, {dtAddDq} is positive and a better choice than {dtSubDq}.
                dt = dtAddDq;
            } else {
                // Here, either {dtSubDq} a better choice than {dtAddDq}, or both are zero.
                dt = dtSubDq;
            }
            assert( dt >= 0 );
        }

        return( dt );

    }  


    /** Get the delta-time to the predicted quantization-event for a state under QSS3.
     *
     * <p>Utility method for use by {@link #_predictQuantizationEventTimeWork()}.</p>
     *
     * <p>Find the time step, from the most recent quantization-event time, of the
     * predicted quantization-event for a state under QSS3.
     * Assume the quantized state model was derived from the
     * continuous state model, and therefore has the same value slope, and
     * second derivative at the quantization-event time.</p>
     *
     * <p>TODO: Put this method under direct unit test.
     * Currently tested indirectly, through method {@link #_predictQuantizationEventTimeWork()}
     * of each solver.
     * Testing directly will make it easier to check results, and will make it
     * easier to add testing for slope-aware quant-evt predictions.</p>
     *
     * @param cStateMdl The model of internal, continuous state.
     * @param qStateMdl The model of external, quantized state.
     * @param dq The quantum, i.e., the critical difference between the models, at
     *   which the external state model must be re-formed.
     * @return dt The delta-time at which, in the absence of other events, the
     *   external state model must be re-formed.
     *   Note 0 <= dt <= Double.POSITIVE_INFINITY.
     *   A value of 0 means need a quantization-event as soon as possible.
     */
    protected final static double _predictQuantizationEventDeltaTimeQSS3QFromC(
        final ModelPolynomial qStateMdl, final ModelPolynomial cStateMdl, final double dq) {

        // Check internal consistency.
        //   QSS2 uses linear quantized state model, and quadratic
        // continuous state model.  Allow higher-order solvers to call this
        // method also.
        assert( qStateMdl.getMaximumOrder() >= 2 );
        assert( cStateMdl.getMaximumOrder() == qStateMdl.getMaximumOrder()+1 );
        assert( qStateMdl.tMdl.compareTo(cStateMdl.tMdl) > 0 );  // Require {qStateMdl} more recent.
        assert( dq > 0 );

        double dt;

        // Initialize.
        final double cea = cStateMdl.coeffs[3];  // Note this is 1/6 the second derivative component of the rate model.

        if( cea != 0 ) {
            // Here, the internal, continuous state model has a third derivative.
            dt = Math.pow( dq / Math.abs(cea), 1.0/3.0 );
        } else {
            dt = _predictQuantizationEventDeltaTimeQSS2QFromC(qStateMdl, cStateMdl, dq);
        }

        return( dt );

    }  


    /** Get the delta-time to the predicted quantization-event for a state under QSS3.
     *
     * <p>Utility method for use by {@link #_predictQuantizationEventTimeWork()}.</p>
     *
     * <p>Find the time step, from the most recent state-event time, of the
     * predicted quantization-event for a state under QSS3.
     * Do not assume the quantized state model bears any particular relationship
     * to the continuous state model.</p>
     *
     * <p>TODO: Put this method under direct unit test.
     * Currently tested indirectly, through method {@link #_predictQuantizationEventTimeWork()}
     * of each solver.
     * Testing directly will make it easier to check results, and will make it
     * easier to add testing for slope-aware quant-evt predictions.</p>
     *
     * @param cStateMdl The model of internal, continuous state.
     * @param qStateMdl The model of external, quantized state.
     * @param dq The quantum, i.e., the critical difference between the models, at
     *   which the external state model must be re-formed.
     * @return dt The delta-time at which, in the absence of other events, the
     *   external state model must be re-formed.
     *   Note 0 <= dt <= Double.POSITIVE_INFINITY.
     *   A value of 0 means need a quantization-event as soon as possible.
     */
    protected final static double _predictQuantizationEventDeltaTimeQSS3General(
        final ModelPolynomial qStateMdl, final ModelPolynomial cStateMdl, final double dq) {

        // Check internal consistency.
        //   QSS2 uses linear quantized state model, and quadratic
        // continuous state model.  Allow higher-order solvers to call this
        // method also.
        assert( qStateMdl.getMaximumOrder() >= 2 );
        assert( cStateMdl.getMaximumOrder() == qStateMdl.getMaximumOrder()+1 );
        assert( dq > 0 );

        double dt;

        // Get coefficients of the cubic equation that defines the step.
        final double hStar = qStateMdl.tMdl.subtractToDouble(cStateMdl.tMdl);
        final double cedConst =
            cStateMdl.coeffs[0] - qStateMdl.coeffs[0] + hStar*(qStateMdl.coeffs[1] - hStar*qStateMdl.coeffs[2]);
        final double cec =
            cStateMdl.coeffs[1] - qStateMdl.coeffs[1] + 2*hStar*qStateMdl.coeffs[2];
        final double ceb =
            cStateMdl.coeffs[2] - qStateMdl.coeffs[2];
        final double cea =
            cStateMdl.coeffs[3];  // Note this is 1/6 the second derivative component of the rate model.
        if( Math.abs(cedConst) >= dq ) {
            // Here, last step had a slight numerical problem such that it
            // violated the quantum.
            //   Initiate a quantization-event as early as possible.
            dt = 0;
        } else if( cea==0 && ceb==0 && cec==0 ) {
            // Note that an alternate approach when {cea==0} would be to use
            // the QSS2 solution.  However, leave that decision up to caller.
            // TODO: Need to run through math, make sure this special case
            // catches all necessary possibilities.
            dt = Double.POSITIVE_INFINITY;
        } else {
            final double absoluteTolerance = 1e-15;
            final double relativeTolerance = 1e-9;
            final double dtAddDq = PolynomialRoot.findMinimumPositiveRoot3(cea, ceb, cec, cedConst+dq, absoluteTolerance, relativeTolerance);
            assert( dtAddDq >= 0 );
            final double dtSubDq = PolynomialRoot.findMinimumPositiveRoot3(cea, ceb, cec, cedConst-dq, absoluteTolerance, relativeTolerance);
            assert( dtSubDq >= 0 );
            if( dtAddDq>0
                &&
                (dtSubDq<=0 || dtAddDq<dtSubDq)
                ) {
                // Here, {dtAddDq} is positive and a better choice than {dtSubDq}.
                dt = dtAddDq;
            } else {
                // Here, either {dtSubDq} a better choice than {dtAddDq}, or both are zero.
                dt = dtSubDq;
            }
            assert( dt >= 0 );
        }

        return( dt );

    }  


    ///////////////////////////////////////////////////////////////////
    ////                         private methods


    /** Validate the QSS integrator has been properly set up.
     *
     * <p>See comments to method {@link #validate()}.</p>
     *
     * @return `null` if the integrator is ready to be used in a simulation, or
     *   an error message diagnosing the problem.
     */
    private final String _validate() {

        // Require integrator has a derivative function.
        //   I.e., require called method init_derivFcn().
        if( null == _derivFcn ) {
            return( "Must call init_derivFcn()" );
        }

        // Check assumptions about method init_derivFcn().
        assert( _derivFcn != null );
        assert( _stateCt == _derivFcn.getStateCount() );
        assert( _ivCt == _derivFcn.getInputVariableCount() );
        assert( _cStateMdls!=null && _cStateMdls.length==_stateCt );
        assert( _qStateMdls!=null && _qStateMdls.length==_stateCt );
        assert( _need_quantEvts!=null && _need_quantEvts.length==_stateCt );
        assert(
            (_ivMdls==null && _ivCt==0)
            ||
            (_ivMdls!=null && _ivMdls.length==_ivCt)
            );
        assert( _dqs!=null && _dqs.length==_stateCt );
        assert( _dqAbsTols!=null && _dqAbsTols.length==_stateCt );
        assert( _dqRelTols!=null && _dqRelTols.length==_stateCt );
        assert( _predQuantEvtTimes!=null && _predQuantEvtTimes.length==_stateCt );
        assert( _need_predQuantEvtTimes!=null && _need_predQuantEvtTimes.length==_stateCt );

        // Require integrator has a valid simulation time.
        //   I.e., require called method init_simTime().
        if( null == _currSimTime ) {
            return( "Must call init_simTime()" );
        }

        // Require state models have valid times.
        //   I.e., require called method setStateValue() on each state, because
        // the model time gets set to {_currSimTime} when set the state.
        for( int ii=0; ii<_stateCt; ++ii ) {
            if( null==_cStateMdls[ii].tMdl || null==_qStateMdls[ii].tMdl ) {
                // Note test above should be redundant.  Both models should get
                // the same initial time at the same point in the worflow, and
                // after that the model time should never return to {null}.
                return( String.format("Need initial value for state %d", ii) );
            }
        }

        // Require have valid models for all input variables.
        for( int ii=0; ii<_ivCt; ++ii ) {
            final ModelPolynomial currMdl = _ivMdls[ii];
            if( null == currMdl ) {
                return( String.format("Need model for input variable %d", ii) );
            }
            if( currMdl.getWriterCount() != 1 ) {
                return( String.format("Need 1 writer for input variable %d; got %d",
                    ii, currMdl.getWriterCount()
                    ) );
            }
            if( null == currMdl.tMdl ) {
                return( String.format("Need initialization for input variable %d", ii) );
            }
        }

        // Report valid.
        return( null );

    }  


    /** Make a model represent a constant.
     *
     * @param constMdl Model to make constant.
     * @param constValue The new value for model.
     * @param maxOrd The maximum order of the model, maxOrd==constMdl.getMaxOrder().
     */
    private final void _makeModelConstant(final ModelPolynomial constMdl, final double constValue,
        final int maxOrd) {

        // Check assumptions.
        assert( maxOrd == constMdl.getMaximumOrder() );

        // Initialize.
        final double[] coeffs = constMdl.coeffs;

        // Set value.
        coeffs[0] = constValue;

        // Set derivatives to zero.
        for( int ii=1; ii<=maxOrd; ++ii ) {
            coeffs[ii] = 0;
        }

        // Set the model time.
        //   This serves several purposes:
        // (1) When called from method setStateValue(), it replaces the
        // {null} model time with what should be a non-null value.  Thus it
        // acts as a check that the initial state has been set.
        // (2) It might help with debugging, or otherwise making output more
        // clear.
        constMdl.tMdl = _currSimTime;

    }  


    /** Initialize fields related to the derivative function.
     *
     * @param derivFcn Object that implements the DerivativeFcn interface.
     */
    private final void _initializeDerivativeFunction(final DerivativeFunction derivFcn) {

        // Check internal consistency.
        assert( _derivFcn == null );

        // Derivative function.
        _derivFcn = derivFcn;
        _stateCt = derivFcn.getStateCount();
        _ivCt = derivFcn.getInputVariableCount();

        assert( _stateCt > 0 );
        assert( _ivCt >= 0 );

    }  


    /** Initialize fields related to tracking the state.
     */
    private final void _initializeStates() {

        // Check internal consistency.
        assert( _cStateMdls == null );
        assert( _stateCt > 0 );

        // Internal, continuous state models.
        //   Note these are owned by the integrator.  Therefore allocate
        // the actual models here.  However, the model times do not get set
        // until the initial value is set.  Therefore no need to allocate
        // {Time} objects here.
        _cStateMdls = new ModelPolynomial[_stateCt];
        final int cStateOrder = _qStateMdlOrder + 1;
        assert( cStateOrder >= 1 );
        for( int ii=0; ii<_stateCt; ++ii ) {
            final ModelPolynomial cStateMdl = new ModelPolynomial(cStateOrder);
            cStateMdl.claimWriteAccess();
            _cStateMdls[ii] = cStateMdl;
        }

        // Force rate-event for integrator.
        _need_rateEvt = true;  // This is redundant, since also forcing a
            // quantization-event, which in turn will force a rate-event.

        // External, quantized state models.
        //   Note these are owned by the integrator.  Therefore allocate
        // the actual models here.  However, the model times do not get set
        // until the initial value is set.  Therefore no need to allocate
        // {Time} objects here.
        _qStateMdls = new ModelPolynomial[_stateCt];
        assert( _qStateMdlOrder >= 0 );
        for( int ii=0; ii<_stateCt; ++ii ) {
            final ModelPolynomial qStateMdl = new ModelPolynomial(_qStateMdlOrder);
            qStateMdl.claimWriteAccess();
            _qStateMdls[ii] = qStateMdl;
        }

        // Force quantization-event in all state models.
        _need_quantEvts = new boolean[_stateCt];
        for( int ii=0; ii<_stateCt; ++ii ) {
            _need_quantEvts[ii] = true;
        }

    }  


    /** Initialize fields related to the input variables.
     */
    private final void _initializeInputVariables() {

        // Check internal consistency.
        assert( _ivMdls == null );
        assert( _ivCt >= 0 );

        // Input variable models.
        //   Note these are references to user-supplied models.  Therefore no
        // need to allocate the actual models.
        if( _ivCt > 0 ) {
            _ivMdls = new ModelPolynomial[_ivCt];
        }

    } 


    /** Initialize fields related to quantization.
     */
    private final void _initializeQuanta() {

        // Check internal consistency.
        assert( _dqs == null );
        assert( _stateCt > 0 );

        // Quanta.
        _dqs = new double[_stateCt];

        // Tolerances for setting the quanta.
        final double dqAbsTolDefault = 1e-20;
        final double dqRelTolDefault = 1e-16;
        _dqAbsTols = new double[_stateCt];
        _dqRelTols = new double[_stateCt];
        setQuantizationTolerances(dqAbsTolDefault, dqRelTolDefault);

        // TODO: The default tolerances used above are SWAGS.  Should do some testing to
        // figure out good default values.
        //   Note that the current FMUQSS code in Ptolemy essentially sets relTol=1e-4,
        // and sets absoluteTolerance based on a "nominal" value of the state:
        // absoluteTolerance = relativeTolerance * _fmiModelDescription.continuousStates.get(i).nominal;
        //   A relative tolerance of 1e-4 seems extremely generous/loose.
        //   The method of setting the absolute tolerance seems good, under the
        // assumption you know a "typical" or "nominal" value for the state variable.
        // Here, you don't, so need a good default.
        //   That said, users could be encouraged to set absoluteTolerance based on the nominal value.
        // Perhaps provide a method that does this explicitly.

        // TODO: Consider passing in tolerances as arguments to this method.
        // That would give individual methods the ability to set their own default tolerances.
        // For example, QSS2 might have different "good defaults" than QSS1.

    }  


    /** Initialize fields related to tracking time.
     */
    private final void _initializeTimes() {

        // Check internal consistency.
        assert( _currSimTime == null );
        assert( _predQuantEvtTimes == null );
        assert( _stateCt > 0 );

        // Simulation time of last call.
        //   Note in order to avoid directly calling a constructor here,
        // require user to initialize {_currSimTime} via method _init_simTime().

        // Predicted quantization-event times.
        //   Note these are references to objects that get created when the
        // value gets calculated.  Therefore no need to allocate the actual
        // objects.
        _predQuantEvtTimes = new Time[_stateCt];

        // Force recalculation of quantization-event times.
        _need_predQuantEvtTimes = new boolean[_stateCt];
        for( int ii=0; ii<_stateCt; ++ii ) {
            _need_predQuantEvtTimes[ii] = true;
        }
    }  

    ///////////////////////////////////////////////////////////////////
    ////                         protected and private variables

    // Identify specific member of the QSS family.
    private final int _qStateMdlOrder = getStateModelOrder();

    // Derivative function.
    protected DerivativeFunction _derivFcn;
    protected int _stateCt, _ivCt;

    // States.
    protected ModelPolynomial[] _cStateMdls;  // Internal, continuous state models.
    protected ModelPolynomial[] _qStateMdls;  // External, quantized state models.
    private boolean _need_rateEvt;  // True if, in order to step forward
        // from {_currSimTime}, need to trigger a rate-event (i.e., need to
        // (form new internal, continuous state models).
    private boolean[] _need_quantEvts;  // True if, in order to step forward
        // from {_currSimTime}, need to trigger a quantization-event (i.e.,
        // need to form a new external, quantized state model).

    // Input variables.
    protected ModelPolynomial[] _ivMdls;

    // Quanta.
    protected double[] _dqs;  // Quantum for each state.
        // Policy:
        // (-) Invariant: consistent with the constant coefficient of the
        //   external, quantized state model.  This means whenever the
        //   quantized state model changes, {_dqs} needs an update.
        // (-) Invariant: consistent with the tolerances {_dqAbsTols} and
        //   {_dqRelTols}.
        // (-) Set only to values returned by method findDq().
    private double[] _dqAbsTols, _dqRelTols;  // Tolerances for finding the quantum of each state.
        // Policy:
        // (-) Can be set only by method setDqTol().

    // Times.
    protected Time _currSimTime;  // Simulation time of last call.
    private Time _quantEvtTimeMax = Time.POSITIVE_INFINITY;  // Maximum
        // time for predicted quantization-event times.
    private Time[] _predQuantEvtTimes;  // Predicted quantization-event time for each state.
    private boolean[] _need_predQuantEvtTimes;  // True if need to recalculate the
        // predicted quantization-event time for the state.

}  
