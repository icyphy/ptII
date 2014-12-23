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


import org.ptolemy.qss.util.DerivativeFcn;
import org.ptolemy.qss.util.ModelPoly;
import org.ptolemy.qss.util.PolyRoot;

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
 * <li>Method {@link #evalStateMdl()} evaluates the quantized state model.
 * Since the user can access that model directly, this is mainly a convenience
 * method.</li>
 * <li>Method {@link #evalStateMdl_cont()} evaluates the
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
 * <p>The following methods initialize a new integrator.
 * They must be called before doing any work with the integrator:</p>
 * <ul>
 * <li>{@link #init_derivFcn()}</li>
 * <li>{@link #init_simTime()}</li>
 * </ul>
 *
 * <p>The following methods inquire about fixed integrator parameters:</p>
 * <ul>
 * <li>{@link #getStateCt()}</li>
 * <li>{@link #getInputVarCt()}</li>
 * <li>{@link #getStateMdlOrder()}</li>
 * </ul>
 *
 * <p>The following methods set up the exchange of models between an integrator
 * and the rest of the simulation environment.
 * In general, they should be called before starting a simulation.
 * However, they may also be called during an integration:</p>
 * <ul>
 * <li>{@link #getStateMdl()}</li>
 * <li>{@link #needInputVarMdlIdx()}</li>
 * <li>{@link #addInputVarMdl()}</li>
 * </ul>
 *
 * <p>The following methods configure the integrator.
 * In general, they should be called before its first use.
 * However, they may also be called during an integration:</p>
 * <ul>
 * <li>{@link #setDqTol()}</li>
 * <li>{@link #setDqTols()}</li>
 * <li>{@link #setCurrSimTime()}</li>
 * <li>{@link #setStateValue()}</li>
 * <li>{@link #setQuantEvtTimeMax()}</li>
 * <li>{@link #validate()}</li>
 * </ul>
 *
 * <p>The following methods inquire about current values during a simulation:</p>
 * <ul>
 * <li>{@link #getCurrSimTime()}</li>
 * <li>{@link #evalStateMdl()}</li>
 * <li>{@link #evalStateMdl_cont()}</li>
 * </ul>
 *
 * <p>The following methods prepare the integrator to take the next time step:</p>
 * <ul>
 * <li>{@link #needQuantEvtIdx()}</li>
 * <li>{@link #needQuantEvtIdxs()}</li>
 * <li>{@link #triggerQuantEvt()}</li>
 * <li>{@link #triggerQuantEvts()}</li>
 * <li>{@link #needRateEvt()}</li>
 * <li>{@link #triggerRateEvt()}</li>
 * <li>{@link #predictQuantEvtTime()}</li>
 * <li>{@link #predictQuantEvtTime_earliest()}</li>
 * </ul>
 *
 * <p>The following methods take a time step:</p>
 * <ul>
 * <li>{@link #stepToTime()}</li>
 * </ul>
 *
 * <p>The following methods primarily facilitate testing:</p>
 * <ul>
 * <li>{@link #stringifyStateMdl()}</li>
 * <li>{@link #stringifyStateMdl_cont()}</li>
 * <li>{@link #findDq()}</li>
 * </ul>
 *
 * <p>TODO: Describe the general time-stepping model.
 * Steps only accomplished via method {@link #stepToTime()}.
 * All other methods elaborate on what happens between time steps.</p>
 *
 * <p>The abstract methods that each subclass must fill in have names ending
 * in <code>_work</code>.
 * This is meant to help distinguish them from the general entry-points
 * provided by this base class.</p>
 *
 *
 * <h2>References</h2>
 *
 * <p>Reference [Kofman-2002].
 * Ernesto Kofman,
 * "A second-order approximation for DEVS simulation of continuous systems",
 * Simulation, v.78, n.2, pp.76-89, 2002.</p>
 *
 * <p>Reference [Cellier-2006].
 * Francois E. Cellier and Ernesto Kofman,
 * "Continuous System Simulation",
 * Springer, 2006.</p>
 *
 * <p>Reference [Migoni-2009].
 * G. Migoni and E. Kofman,
 * "Linearly implicit discrete event methods for stiff ODE's",
 * Latin American Applied Research, v.39, pp.245–254, 2009.</p>
 *
 * <p>Reference [Migoni-2013].
 * Gustavo Migoni, Mario Bortolotto, Ernesto Kofman, and Francois E. Cellier,
 * "Linearly implicit quantization-based integration methods for stiff ordinary
 * differential equations",
 * Simulation Modelling Practice and Theory, v.35, pp.118–136, 2013.</p>
 *
 *
 * @author David M. Lorenzetti
 * @version $id$
 * @since Ptolemy II 10.2  // FIXME: Check version number.
 * @Pt.ProposedRating red (dmlorenzetti)
 * @Pt.AcceptedRating red (reviewmoderator)  // FIXME: Fill in.
 */
public abstract class QSSBase {


    ///////////////////////////////////////////////////////////////////
    ////                         public methods


    /** Initialize a QSS integrator to use a <code>DerivativeFcn</code> object.
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
    public final void init_derivFcn(final DerivativeFcn derivFcn) {

        // Check inputs.
        if( null == derivFcn ) {
            throw new IllegalArgumentException("Require a valid derivFcn");
            }

        // Check status.
        if( _derivFcn != null ) {
            throw new IllegalStateException("Method init_derivFcn() can be called only once");
        }

        _initDerivFcn(derivFcn);
        _initStates();
        _initInputVars();
        _initQuanta();
        _initTimes();
        _init_work();

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
     * @param derivFcn Object that implements the DerivativeFcn interface.
     */
    public final void init_simTime(final Time initSimTime) {

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
    public final int getStateCt() {
        return(_stateCt);
    }


    /** Return the count of input variables to the integrator.
     *
     * @return Count of input variables.
     */
    public final int getInputVarCt() {
        return(_ivCt);
    }


    /** Get the order of the external, quantized state models exposed by the integrator.
     *
     * <p>This method returns the order of the <code>ModelPoly</code> objects
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
    public abstract int getStateMdlOrder();
        // Note would really like this to be a static method, since it should
        // not access any instance variables.  However, can't have an
        // "abstract static" method signature.


    /** Get the external, quantized state model for a state predicted by the integrator.
     *
     * <p>The QSS integrator uses this model to share the predicted state as a
     * function of time, when integrating the derivative function.</p>
     *
     * <p>The initial state model is constant at a value of 0.
     * Use method {@link #setStateValue()} to change this initial value.</p>
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
     * <li>The user should call method {@link #setStateValue()}, in order
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
     * using method {@link #evalStateMdl()}.
     * Alternately, it could copy the quantized state model to a user-supplied
     * model object, thus keeping the integrator's private copy hidden.
     * However, both these approaches are relatively high overhead, compared
     * to simply exposing the model for the user to evaluate as needed.</p>
     *
     * @param stateIdx Index of state, 0 <= stateIdx < this.getStateCt().
     * @param qStateMdl Model to use.
     */
    public final ModelPoly getStateMdl(final int stateIdx) {
        return( _qStateMdls[stateIdx] );
    }


    /** Return the index of an input variable for which the user has yet to add a model.
     *
     * <p>The user must call {@link #addInputVarMdl()} at least once for
     * every input variable taken by the derivative function.
     * This method checks whether that requirement has been met.</p>
     *
     * @return Index of an input variable for which the user has yet to add a
     *   model, 0 <= idx < this.getArgCt().  Return -1 if all models have been
     *   added (or if the derivative function takes no input variables).
     */
    public final int needInputVarMdlIdx() {

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


    /** Add the model for an input variable to the derivative function.
     *
     * <p>Add a <code>ModelPoly</code> for an input variable to the
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
     * change the parameters of the <code>ModelPoly</code>.
     * This means it will control the trajectory of that model over time.
     * See method ModelPoly.claimWriteAccess().</li>
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
     * <li>See the notes for method {@link #getStateMdl()}.</li>
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
     * @param ivIdx Index of input variable, 0 <= ivIdx < this.getInputVarCt().
     * @param ivMdl Model to use.
     */
    public final void addInputVarMdl(final int ivIdx, final ModelPoly ivMdl) {

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

    }  // End method addInputVarMdl().


     // TODO: Add method that allows user to indicate when a state variable is not
     // actually used by the derivative function.
     // This will allow efficiency when experience a quantization-event for that
     // state (because won't then have to trigger a rate-event for the same component).


    /** Set the parameters used to determine the quantum for a state.
     *
     * <p>The quantum for each state variable gets calculated as</p>
     * <p><i>dq[j] = max{absTol, relTol*abs{x[j]}}</i></p>
     *
     * <p>where</p>
     * <ul>
     * <li><i>dq[j]</i>, quantum for element <i>j</i>.</li>
     * <li><i>x[j]</i>, value of element <i>j</i> the last time a new state
     * model was formed.</li>
     * </ul>
     *
     * <p>This method sets the tolerances used to find the quantum.
     * It also updates the quantum to reflect the new tolerances.</p>
     *
     * @param stateIdx Index of state, 0 <= stateIdx < this.getStateCt().
     * @param absTol Absolute tolerance, absTol > 0 [units of <i>x[j]</i>].
     * @param relTol Relative tolerance, relTol >= 0 [1].
     */
    public final void setDqTol(final int stateIdx, final double absTol, final double relTol) {

        // Check inputs.
        if( absTol <= 0 ) {
            throw new IllegalArgumentException("Require absTol>0; got " +absTol);
        }
        if( relTol < 0 ) {
            throw new IllegalArgumentException("Require relTol>=0; got " +relTol);
        }

        // Set status to note future needs.
        _need_quantEvts[stateIdx] = true;

        // Change tolerances.
        _dqAbsTols[stateIdx] = absTol;
        _dqRelTols[stateIdx] = relTol;
        _dqs[stateIdx] = findDq(stateIdx);

    }  // End method setDqTol().


    /** Set the parameters used to determine the quantum for all states.
     *
     * <p>Apply the same tolerances to all the states the integrator predicts.
     * For details, see method {@link #setDqTol()}.</p>
     *
     * @param absTol Absolute tolerance, absTol > 0 [units of <i>x[j]</i>].
     * @param relTol Relative tolerance, relTol >= 0 [1].
     */
    public final void setDqTols(final double absTol, final double relTol) {

        for( int ii=0; ii<_stateCt; ++ii ) {
            setDqTol(ii, absTol, relTol);
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
     *
     * @param newSimTime New time for the QSS integrator.
     */
    public final void setCurrSimTime(final Time newSimTime) {

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
     * formed, and creates a jump discontinuity in the state variable.</p>
     *
     *
     * <h2>Warning</h2>
     *
     * <p>The user may be tempted to set the value of the state variable
     * directly in the state model, without going through the QSS integrator.
     * However, doing so prevents the integrator from making the appropriate
     * internal adjustments to the change in state.</p>
     *
     * @param stateIdx Index of state, 0 <= stateIdx < this.getStateCt().
     * @param newValue New value of x[stateIdx].
     */
    public final void setStateValue(final int stateIdx, final double newValue) {

        // Set status to note future needs.
        _need_rateEvt = true;
        _need_quantEvts[stateIdx] = true;
        // _need_predQuantEvtTimes[stateIdx] = true;  // This will follow from changes above.

        // Make the quantized state model constant at {newValue}.
        final ModelPoly qStateMdl = _qStateMdls[stateIdx];
        _makeMdlConstant(qStateMdl, newValue, _qStateMdlOrder);

        // Make the continuous state model constant at {newValue}.
        _makeMdlConstant(_cStateMdls[stateIdx], newValue, _qStateMdlOrder+1);

        // Update quantum.
        //   To keep consistent with the constant coefficient of the
        // external, quantized state model.
        _dqs[stateIdx] = findDq(stateIdx);

    }  // End method setStateValue().


    // TODO: Consider adding a method that sets all the state variables from a vector.


    /** Reset the maximum time for predicted quantization-events.
     *
     * <p>The integrator will not predict quantization-event times past this
     * time.
     * Default value <code>Time.POSITIVE_INFINITY</code>.</p>
     *
     * @param quantEvtTimeMax Maximum time for predicted quantization-events.
     */
    public final void setQuantEvtTimeMax(final Time quantEvtTimeMax) {

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
     * method {@link #stepToTime()} was called.
     * Exceptions:</p>
     * <ul>
     * <li>When the integrator is first instantiated, it is set to 0.</li>
     * <li>Method {@link #setCurrSimTime()} changes the value outright.</li>
     * </ul>
     *
     * @return Current simulation time for the QSS integrator.
     */
    public final Time getCurrSimTime() {
        return( _currSimTime );
    }


    /** Get the value of a state variable.
     *
     * <p>Evaluate the external, quantized state model at a specified time.</p>
     *
     * <p>Note this method evaluates the external, quantized state model.
     * Alternately, the user could acquire the model, via
     * method {@link #getStateMdl()}, and evaluate that model directly.</p>
     *
     * @param stateIdx Index of state, 0 <= stateIdx < this.getStateCt().
     * @param simTime Global simulation time.
     * @return Value of the state model at <code>simTime</code>.
     */
    public final double evalStateMdl(final int stateIdx, final Time simTime) {
        return( _qStateMdls[stateIdx].eval(simTime) );
    }


    /** Get the internal value of a state variable.
     *
     * <p>Evaluate the internal, continuous state model at a specified time.</p>
     *
     * @param stateIdx Index of state, 0 <= stateIdx < this.getStateCt().
     * @param simTime Global simulation time.
     * @return Value of the state model at <code>simTime</code>.
     */
    public final double evalStateMdl_cont(final int stateIdx, final Time simTime) {
        return( _cStateMdls[stateIdx].eval(simTime) );
    }


    /** Return the index of a state that needs a quantization-event.
     *
     * <p>The integrator tracks which states need a quantization-event as a
     * result of a time step.
     * This method returns the index, if any, of such states.</p>
     *
     * <p>The user should trigger the quantization-event, e.g., using
     * method {@link #triggerQuantEvt()}.</p>
     *
     * <p>TODO: Put under unit test.</p>
     *
     * @return Index of a state that needs a quantization-event,
     *   0 <= idx < this.getStateCt().  Return -1 if all external, quantized
     *   state models are valid.
     */
    public final int needQuantEvtIdx() {

        // Initialize.
        int needQuantEvtIdx = -1;

        for( int ii=0; ii<_stateCt; ++ii ) {
            if( _need_quantEvts[ii] ) {
                needQuantEvtIdx = ii;
                break;
            }
        }

        return( needQuantEvtIdx );

    }  // End method needQuantEvtIdx().


    /** Return array of indices of all states that need a quantization-event.
     *
     * <p>See comments to method {@link #needQuantEvtIdx()}.</p>
     *
     * <p>TODO: Put under unit test.</p>
     *
     * @param needQuantEvtIdxs (output) Vector showing <code>true</code> for
     *   each integrator state that needs a quantization-event.
     */
    public final void needQuantEvtIdxs(final boolean[] needQuantEvtIdxs) {
        System.arraycopy(_need_quantEvts, 0, needQuantEvtIdxs, 0, _stateCt);
    }


    /** Form a new external, quantized state model.
     *
     * <p>Force the QSS integrator to form a new external, quantized state model
     * (i.e., to experience a quantization-event).
     * The new model will be available to the user immediately.</p>
     *
     * <p>Note method {@link #triggerQuantEvts()} can requantize multiple
     * state models at once, and can requantize only those states that need it.</p>
     *
     * <p>Form the model about the current simulation time, as returned by
     * method {@link #getCurrSimTime()}.</p>
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
     * method {@link #triggerRateEvt()}.</p>
     *
     * <p>The proper sequence in which to call method {@link #triggerRateEvt()}
     * and method {@link #triggerQuantEvt()} is a fraught topic.
     * In general, should requantize all states first, then trigger rate-events.
     * Also, after trigger a rate-event, get new predicted quantization-time.
     * TODO: Write up a higher-level description of the problem.</p>
     *
     * @param stateIdx Index of state, 0 <= stateIdx < this.getStateCt().
     */
    public final void triggerQuantEvt(final int stateIdx) {

        // Set status to note future needs.
        _need_rateEvt = true;
        _need_predQuantEvtTimes[stateIdx] = true;

        // Perform work defined by specific member of the QSS family.
        _triggerQuantEvt_work(stateIdx);

        // Update quantum.
        //   To keep consistent with the constant coefficient of the
        // external, quantized state model.
        _dqs[stateIdx] = findDq(stateIdx);

        // Set status to note satisfied needs.
        _need_quantEvts[stateIdx] = false;

    }  // End method triggerQuantEvt().


    /** Form new external, quantized state models.
     *
     * <p>Convenience method to call method {@link #triggerQuantEvt()} on
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
     * See method {@link #predictQuantEvtTime()}.</li>
     * <li>TODO: Provide, probably in top-level comments, an overview of when
     * an integrator state needs to have a quantization-event.
     * Following this list.
     * Then just cross-reference that discussion here, and in places like
     * description of method {@link #triggerRateEvt()}.</p>
     * </ul>
     *
     * <p>To determine state(s) that need to be requantized, use either
     * method {@link #needQuantEvtIdx()} or
     * method {@link #needQuantEvtIdxs()}.</p>
     *
     * @param forceAll If true, requantize all state models.
     */
    public final void triggerQuantEvts(final boolean forceAll) {

        for( int ii=0; ii<_stateCt; ++ii ) {
            if( forceAll || _need_quantEvts[ii] ) {
                triggerQuantEvt(ii);
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
    public final boolean needRateEvt() {
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
     * method {@link #getCurrSimTime()}.</p>
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
     * method {@link #triggerQuantEvt()}.</p>
     *
     * <p>The proper sequence in which to call method {@link #triggerRateEvt()}
     * and method {@link #triggerQuantEvt()} is a fraught topic.
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
    public final void triggerRateEvt()
        throws Exception {

        // Set status to note future needs.
        for( int ii=0; ii<_stateCt; ++ii ) {
            _need_predQuantEvtTimes[ii] = true;
        }

        // Perform work defined by specific member of the QSS family.
        _triggerRateEvt_work();

        // Set status to note satisfied needs.
        _need_rateEvt = false;

    }  // End method triggerRateEvt().


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
    // to triggerQuantEvt(), telling it to handle rate-event at same time if
    // necessary.


    /** Get the predicted quantization-event time for a state.
     *
     * TODO: Get this method under unit test.
     *
     * @param stateIdx Index of state, 0 <= stateIdx < this.getStateCt().
     * @return Next time at which, in the absence of other events, the
     *   external state model must be re-formed,
     *   0 <= time <= Time.POSITIVE_INFINITY.
     */
    public final Time predictQuantEvtTime(final int stateIdx) {

        Time predQuantEvtTime;

        if( _need_predQuantEvtTimes[stateIdx] ) {
            // Perform work defined by specific member of the QSS family.
            predQuantEvtTime = _predictQuantEvtTime_work(stateIdx, _quantEvtTimeMax);
            assert(
                predQuantEvtTime.compareTo(_cStateMdls[stateIdx].tMdl)==1
                ||
                predQuantEvtTime.compareTo(_quantEvtTimeMax)==0
                );
            _predQuantEvtTimes[stateIdx] = predQuantEvtTime;
            _need_predQuantEvtTimes[stateIdx] = false;
        } else {
            predQuantEvtTime = _predQuantEvtTimes[stateIdx];
        }

        return( predQuantEvtTime );

    }  // End method predictQuantEvtTime().


    /** Get the earliest predicted quantization-event time for all states.
     *
     * TODO: Get this method under unit test.
     *
     * @return Earliest predicted quantization-event time from among all states
     *   predicted by the integrator.
     */
    public final Time predictQuantEvtTime_earliest() {

        // Initialize.
        Time predQuantEvtTime = predictQuantEvtTime(0);

        // Run through remaining elements.
        for( int ii=1; ii<_stateCt; ++ii ) {
            final Time newTime = predictQuantEvtTime(ii);
            if( newTime.compareTo(predQuantEvtTime) == -1 ) {
                predQuantEvtTime = newTime;
            }
        }

        return( predQuantEvtTime );

    }  // End method predictQuantEvtTime_earliest().


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
    public final Time predictQuantEvtTime_earliest(final boolean[] quantEvtElts) {

        // Initialize.
        final Time predQuantEvtTime = predictQuantEvtTime_earliest();

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

    }  // End method predictQuantEvtTime_earliest().


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
        if( needQuantEvtIdx() != -1 ) {
            throw new IllegalStateException("State models waiting to be quantized");
        }

        // Update state models if necessary.
        if( _need_rateEvt ) {
            triggerRateEvt();
            assert( _need_rateEvt == false );
        }

        // Determine which, if any, state models will require requantization at
        // the end of this step.
        for( int ii=0; ii<_stateCt; ++ii ) {
            assert( _need_quantEvts[ii] == false );
            final Time predQuantEvtTime = predictQuantEvtTime(ii);
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

    }  // End method stepToTime().


    /** Get the internal, continuous state model for a state predicted by the integrator.
     *
     * <p>This method is the equivalent of method {@link #getStateMdl()},
     * except that it retrieves the internal, continuous state model rather
     * than the external, quantized state model.</p>
     *
     * <p>This method is provided primarily for testing.
     * In principle, the user should not even have to be aware of the existence
     * of the internal, continuous state model, let alone have access to it.</p>
     *
     * @param stateIdx Index of state, 0 <= stateIdx < this.getStateCt().
     * @param qStateMdl Model to use.
     */
    // public final ModelPoly getStateMdl_cont(final int stateIdx) {
    //     return( _cStateMdls[stateIdx] );
    // }


    /** Get a string representation of the model for a state.
     *
     * <p>Invoke method ModelPoly.toString() on the
     * external, quantized state model.</p>
     *
     * @param stateIdx Index of state, 0 <= stateIdx < this.getStateCt().
     */
    public final String stringifyStateMdl(final int stateIdx) {
        return( _qStateMdls[stateIdx].toString() );
    }


    /** Get a string representation of the internal model for a state.
     *
     * <p>Invoke method ModelPoly.toString() on the
     * internal, continuous state model.</p>
     *
     * @param stateIdx Index of state, 0 <= stateIdx < this.getStateCt().
     */
    public final String stringifyStateMdl_cont(final int stateIdx) {
        return( _cStateMdls[stateIdx].toString() );
    }


    /** Find the quantum for a state.
     *
     * <p>Finds the quantum, i.e., the maximum allowable difference between
     * the external, quantized state model shared with the user, and the
     * internal, continuous state model used by the integrator.</p>
     *
     * <p>To change the parameters used to find the quantum, use
     * method {@link #setDqTol()} or method {@link #setDqTols()}.</p>
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
     * @param stateIdx Index of state, 0 <= stateIdx < this.getStateCt().
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
    protected abstract void _init_work();


    /** Form a new external, quantized state model (QSS-specific).
     *
     * <p>See comments to method {@link #triggerQuantEvt()}.</p>
     *
     * <p>The implementation of this "worker" method depends on the
     * specific member of the QSS family.</p>
     *
     * @param stateIdx Index of state, 0 <= stateIdx < this.getStateCt().
     */
    protected abstract void _triggerQuantEvt_work(final int stateIdx);


    /** Form new internal, continuous state models (QSS-specific).
     *
     * <p>See comments to method {@link #triggerRateEvt()}.</p>
     *
     * <p>The implementation of this "worker" method depends on the
     * specific member of the QSS family.</p>
     */
    protected abstract void _triggerRateEvt_work()
        throws Exception;


    /** Get the predicted quantization-event time for a state (QSS-specific).
     *
     * <p>See comments to method {@link #predictQuantEvtTime()}.</p>
     *
     * <p>The implementation of this "worker" method depends on the
     * specific member of the QSS family.</p>
     *
     *
     * <h2>Implementation notes</h2>
     *
     * <p>The method should not alter any instance variables.</p>
     *
     * @param stateIdx Index of state, 0 <= stateIdx < this.getStateCt().
     * @param quantEvtTimeMax Maximum time for the return value.  May be
     *   Time.POSITIVE_INFINITY.
     * @return Next time at which, in the absence of other events, the
     *   external state model must be re-formed, time <= quantEvtTimeMax.
     */
    protected abstract Time _predictQuantEvtTime_work(
        final int stateIdx, final Time quantEvtTimeMax);


    /** Get the delta-time to the predicted quantization-event for a state under QSS2.
     *
     * <p>Utility method for use by {@link #_predictQuantEvtTime_work()}.</p>
     *
     * <p>Find the time step, from the most recent quantization-event time, of the
     * predicted quantization-event for a state under QSS2.
     * Assume the quantized state model was derived from the
     * continuous state model, and therefore has the same value and slope at
     * the quantization-event time.</p>
     *
     * <p>TODO: Put this method under direct unit test.
     * Currently tested indirectly, through method {@link #_predictQuantEvtTime_work()}
     * of each solver.
     * Testing directly will make it easier to check results, and will make it
     * easier to add testing for slope-aware quant-evt predictions.</p>
     *
     * @param cStateMdl Model of internal, continuous state.
     * @param qStateMdl Model of external, quantized state.
     * @param dq Quantum, i.e., the critical difference between the models, at
     *   which the external state model must be re-formed.
     * @return dt Delta-time at which, in the absence of other events, the
     *   external state model must be re-formed.
     *   Note 0 <= dt <= Double.POSITIVE_INFINITY.
     *   A value of 0 means need a quantization-event as soon as possible.
     */
    protected final static double _predictQuantEvtDeltaTime_qss2_qFromC(
        final ModelPoly qStateMdl, final ModelPoly cStateMdl, final double dq) {

        // Check internal consistency.
        //   QSS2 uses linear quantized state model, and quadratic
        // continuous state model.  Allow higher-order solvers to call this
        // method also.
        assert( qStateMdl.getMaxOrder() >= 1 );
        assert( cStateMdl.getMaxOrder() == qStateMdl.getMaxOrder()+1 );
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

    }  // End method _predictQuantEvtDeltaTime_qss2_qFromC().


    /** Get the delta-time to the predicted quantization-event for a state under QSS2.
     *
     * <p>Utility method for use by {@link #_predictQuantEvtTime_work()}.</p>
     *
     * <p>Find the time step, from the most recent state-event time, of the
     * predicted quantization-event for a state under QSS2.
     * Do not assume the quantized state model bears any particular relationship
     * to the continuous state model.</p>
     *
     * <p>TODO: Put this method under direct unit test.
     * Currently tested indirectly, through method {@link #_predictQuantEvtTime_work()}
     * of each solver.
     * Testing directly will make it easier to check results, and will make it
     * easier to add testing for slope-aware quant-evt predictions.</p>
     *
     * @param cStateMdl Model of internal, continuous state.
     * @param qStateMdl Model of external, quantized state.
     * @param dq Quantum, i.e., the critical difference between the models, at
     *   which the external state model must be re-formed.
     * @return dt Delta-time at which, in the absence of other events, the
     *   external state model must be re-formed.
     *   Note 0 <= dt <= Double.POSITIVE_INFINITY.
     *   A value of 0 means need a quantization-event as soon as possible.
     */
    protected final static double _predictQuantEvtDeltaTime_qss2_general(
        final ModelPoly qStateMdl, final ModelPoly cStateMdl, final double dq) {

        // Check internal consistency.
        //   QSS2 uses linear quantized state model, and quadratic
        // continuous state model.  Allow higher-order solvers to call this
        // method also.
        assert( qStateMdl.getMaxOrder() >= 1 );
        assert( cStateMdl.getMaxOrder() == qStateMdl.getMaxOrder()+1 );
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
            final double dtAddDq = PolyRoot.findMinPosRoot2(qea, qeb, qecConst+dq);
            assert( dtAddDq >= 0 );
            final double dtSubDq = PolyRoot.findMinPosRoot2(qea, qeb, qecConst-dq);
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

    }  // End method _predictQuantEvtDeltaTime_qss2_general().


    /** Get the delta-time to the predicted quantization-event for a state under QSS3.
     *
     * <p>Utility method for use by {@link #_predictQuantEvtTime_work()}.</p>
     *
     * <p>Find the time step, from the most recent quantization-event time, of the
     * predicted quantization-event for a state under QSS3.
     * Assume the quantized state model was derived from the
     * continuous state model, and therefore has the same value slope, and
     * second derivative at the quantization-event time.</p>
     *
     * <p>TODO: Put this method under direct unit test.
     * Currently tested indirectly, through method {@link #_predictQuantEvtTime_work()}
     * of each solver.
     * Testing directly will make it easier to check results, and will make it
     * easier to add testing for slope-aware quant-evt predictions.</p>
     *
     * @param cStateMdl Model of internal, continuous state.
     * @param qStateMdl Model of external, quantized state.
     * @param dq Quantum, i.e., the critical difference between the models, at
     *   which the external state model must be re-formed.
     * @return dt Delta-time at which, in the absence of other events, the
     *   external state model must be re-formed.
     *   Note 0 <= dt <= Double.POSITIVE_INFINITY.
     *   A value of 0 means need a quantization-event as soon as possible.
     */
    protected final static double _predictQuantEvtDeltaTime_qss3_qFromC(
        final ModelPoly qStateMdl, final ModelPoly cStateMdl, final double dq) {

        // Check internal consistency.
        //   QSS2 uses linear quantized state model, and quadratic
        // continuous state model.  Allow higher-order solvers to call this
        // method also.
        assert( qStateMdl.getMaxOrder() >= 2 );
        assert( cStateMdl.getMaxOrder() == qStateMdl.getMaxOrder()+1 );
        assert( qStateMdl.tMdl.compareTo(cStateMdl.tMdl) > 0 );  // Require {qStateMdl} more recent.
        assert( dq > 0 );

        double dt;

        // Initialize.
        final double cea = cStateMdl.coeffs[3];  // Note this is 1/6 the second derivative component of the rate model.

        if( cea != 0 ) {
            // Here, the internal, continuous state model has a third derivative.
            dt = Math.pow( dq / Math.abs(cea), 1.0/3.0 );
        } else {
            dt = _predictQuantEvtDeltaTime_qss2_qFromC(qStateMdl, cStateMdl, dq);
        }

        return( dt );

    }  // End method _predictQuantEvtDeltaTime_qss3_qFromC().


    /** Get the delta-time to the predicted quantization-event for a state under QSS3.
     *
     * <p>Utility method for use by {@link #_predictQuantEvtTime_work()}.</p>
     *
     * <p>Find the time step, from the most recent state-event time, of the
     * predicted quantization-event for a state under QSS3.
     * Do not assume the quantized state model bears any particular relationship
     * to the continuous state model.</p>
     *
     * <p>TODO: Put this method under direct unit test.
     * Currently tested indirectly, through method {@link #_predictQuantEvtTime_work()}
     * of each solver.
     * Testing directly will make it easier to check results, and will make it
     * easier to add testing for slope-aware quant-evt predictions.</p>
     *
     * @param cStateMdl Model of internal, continuous state.
     * @param qStateMdl Model of external, quantized state.
     * @param dq Quantum, i.e., the critical difference between the models, at
     *   which the external state model must be re-formed.
     * @return dt Delta-time at which, in the absence of other events, the
     *   external state model must be re-formed.
     *   Note 0 <= dt <= Double.POSITIVE_INFINITY.
     *   A value of 0 means need a quantization-event as soon as possible.
     */
    protected final static double _predictQuantEvtDeltaTime_qss3_general(
        final ModelPoly qStateMdl, final ModelPoly cStateMdl, final double dq) {

        // Check internal consistency.
        //   QSS2 uses linear quantized state model, and quadratic
        // continuous state model.  Allow higher-order solvers to call this
        // method also.
        assert( qStateMdl.getMaxOrder() >= 2 );
        assert( cStateMdl.getMaxOrder() == qStateMdl.getMaxOrder()+1 );
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
            final double absTol = 1e-15;
            final double relTol = 1e-9;
            final double dtAddDq = PolyRoot.findMinPosRoot3(cea, ceb, cec, cedConst+dq, absTol, relTol);
            assert( dtAddDq >= 0 );
            final double dtSubDq = PolyRoot.findMinPosRoot3(cea, ceb, cec, cedConst-dq, absTol, relTol);
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

    }  // End method _predictQuantEvtDeltaTime_qss3_general().


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
        assert( _stateCt == _derivFcn.getStateCt() );
        assert( _ivCt == _derivFcn.getInputVarCt() );
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
            final ModelPoly currMdl = _ivMdls[ii];
            if( null == currMdl ) {
                return( String.format("Need model for input variable %d", ii) );
            }
            if( currMdl.getWriterCt() != 1 ) {
                return( String.format("Need 1 writer for input variable %d; got %d",
                    ii, currMdl.getWriterCt()
                    ) );
            }
            if( null == currMdl.tMdl ) {
                return( String.format("Need initialization for input variable %d", ii) );
            }
        }

        // Report valid.
        return( null );

    }  // End method _validate().


    /* Make a model represent a constant.
     *
     * @param constMdl Model to make constant.
     * @param constValue New value for model.
     * @param maxOrd Maximum order of the model, maxOrd==constMdl.getMaxOrder().
     */
    private final void _makeMdlConstant(final ModelPoly constMdl, final double constValue,
        final int maxOrd) {

        // Check assumptions.
        assert( maxOrd == constMdl.getMaxOrder() );

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

    }  // End method _makeMdlConstant().


    /** Initialize fields related to the derivative function.
     *
     * @param derivFcn Object that implements the DerivativeFcn interface.
     */
    private final void _initDerivFcn(final DerivativeFcn derivFcn) {

        // Check internal consistency.
        assert( _derivFcn == null );

        // Derivative function.
        _derivFcn = derivFcn;
        _stateCt = derivFcn.getStateCt();
        _ivCt = derivFcn.getInputVarCt();

        assert( _stateCt > 0 );
        assert( _ivCt >= 0 );

    }  // End method _initDerivFcn().


    /** Initialize fields related to tracking the state.
     */
    private final void _initStates() {

        // Check internal consistency.
        assert( _cStateMdls == null );
        assert( _stateCt > 0 );

        // Internal, continuous state models.
        //   Note these are owned by the integrator.  Therefore allocate
        // the actual models here.  However, the model times do not get set
        // until the initial value is set.  Therefore no need to allocate
        // {Time} objects here.
        _cStateMdls = new ModelPoly[_stateCt];
        final int cStateOrder = _qStateMdlOrder + 1;
        assert( cStateOrder >= 1 );
        for( int ii=0; ii<_stateCt; ++ii ) {
            final ModelPoly cStateMdl = new ModelPoly(cStateOrder);
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
        _qStateMdls = new ModelPoly[_stateCt];
        assert( _qStateMdlOrder >= 0 );
        for( int ii=0; ii<_stateCt; ++ii ) {
            final ModelPoly qStateMdl = new ModelPoly(_qStateMdlOrder);
            qStateMdl.claimWriteAccess();
            _qStateMdls[ii] = qStateMdl;
        }

        // Force quantization-event in all state models.
        _need_quantEvts = new boolean[_stateCt];
        for( int ii=0; ii<_stateCt; ++ii ) {
            _need_quantEvts[ii] = true;
        }

    }  // End method _initStates().


    /** Initialize fields related to the input variables.
     */
    private final void _initInputVars() {

        // Check internal consistency.
        assert( _ivMdls == null );
        assert( _ivCt >= 0 );

        // Input variable models.
        //   Note these are references to user-supplied models.  Therefore no
        // need to allocate the actual models.
        if( _ivCt > 0 ) {
            _ivMdls = new ModelPoly[_ivCt];
        }

    }  // End method _initInputVars().


    /** Initialize fields related to quantization.
     */
    private final void _initQuanta() {

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
        setDqTols(dqAbsTolDefault, dqRelTolDefault);

        // TODO: The default tolerances used above are SWAGS.  Should do some testing to
        // figure out good default values.
        //   Note that the current FMUQSS code in Ptolemy essentially sets relTol=1e-4,
        // and sets absTol based on a "nominal" value of the state:
        // absTol=relTol*_fmiModelDescription.continuousStates.get(i).nominal;
        //   A relative tolerance of 1e-4 seems extremely generous/loose.
        //   The method of setting the absolute tolerance seems good, under the
        // assumption you know a "typical" or "nominal" value for the state variable.
        // Here, you don't, so need a good default.
        //   That said, users could be encouraged to set absTol based on the nominal value.
        // Perhaps provide a method that does this explicitly.

        // TODO: Consider passing in tolerances as arguments to this method.
        // That would give individual methods the ability to set their own default tolerances.
        // For example, QSS2 might have different "good defaults" than QSS1.

    }  // End method _initQuanta().


    /** Initialize fields related to tracking time.
     */
    private final void _initTimes() {

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

    }  // End method _initTimes().


    ///////////////////////////////////////////////////////////////////
    ////                         protected and private variables

    // Identify specific member of the QSS family.
    private final int _qStateMdlOrder = getStateMdlOrder();

    // Derivative function.
    protected DerivativeFcn _derivFcn;
    protected int _stateCt, _ivCt;

    // States.
    protected ModelPoly[] _cStateMdls;  // Internal, continuous state models.
    protected ModelPoly[] _qStateMdls;  // External, quantized state models.
    private boolean _need_rateEvt;  // True if, in order to step forward
        // from {_currSimTime}, need to trigger a rate-event (i.e., need to
        // (form new internal, continuous state models).
    private boolean[] _need_quantEvts;  // True if, in order to step forward
        // from {_currSimTime}, need to trigger a quantization-event (i.e.,
        // need to form a new external, quantized state model).

    // Input variables.
    protected ModelPoly[] _ivMdls;

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

}  // End class QSSBase.
