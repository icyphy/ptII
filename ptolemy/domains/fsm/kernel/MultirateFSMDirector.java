/* A MultirateFSM director that extends FSMDirector by supporting production
   and consumption of multiple tokens on a port in a firing.

   Copyright (c) 2004-2005 The Regents of the University of California.
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
package ptolemy.domains.fsm.kernel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.ConstVariableModelAnalysis;
import ptolemy.actor.util.DFUtilities;
import ptolemy.actor.util.DependencyDeclaration;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;


/**
   This director extends FSMDirector by supporting production and consumption
   of multiple tokens on a port in a firing. This director assumes that every
   state has exactly one refinement, with one exception. A state may have no
   refinement if upon being entered, it has an outgoing transition with a guard
   that is true. This will be treated as a "transient state." Transient states
   can have preemptive and non-preemptive transitions, while non-transient
   states are assumed to have only non-preemptive transitions. When a modal
   model reaches a transient state, it will progress through that state to the
   next state until it encounters a state with a refinement. This procedure is
   done in the preinitialize() method and the postfire() method. Hence each
   time when a modal model is fired, the current state always has a state
   refinement.
   <p>
   The number of tokens to be transferred from an input port of the modal model
   is at most the token consumption rate inferred by the inside port of the
   current state refinement. The number of tokens to be transferred from an
   output port of the state refinement is exactly the token production rate
   inferred by the state refinement. If there are not enough tokens available
   from the refinement, an exception is thrown. The default token consumption
   and production rate of a port is 1.
   <p>
   When a state transition occurs, this director compares the port rates of
   the destination state refinement with that of the current state refinement.
   If the rates are different, then invalidate the schedule of the executive
   director of the modal model. Update the port rates of the modal model to be
   the port rates of the destination state refinement.
   <p>
   This director does not support transition refinements.

   @author Ye Zhou
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
   @see FSMDirector
*/
public class MultirateFSMDirector extends FSMDirector {
    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of this director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public MultirateFSMDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /////////////////////////////////////////////////////////////////////
    ////                           public methods                    ////

    /** Choose the next non-transient state given the current state.
     *  @param currentState The current state.
     *  @exception IllegalActionException If a transient state is reached
     *   but no further transition is enabled.
     */
    public void chooseNextNonTransientState(State currentState)
            throws IllegalActionException {
        State state = chooseTransition(currentState);
        Actor[] actors = state.getRefinement();
        Transition transition;

        while (actors == null) {
            // Commit the transition.
            super.postfire();
            state = chooseTransition(state);
            transition = _getLastChosenTransition();

            if (transition == null) {
                throw new IllegalActionException(this,
                        "Reached a state without a refinement: " + state.getName());
            }

            actors = (transition.destinationState()).getRefinement();
        }
    }

    /** Choose an enabled transition leaving the given state.
     *  If the transition refinement is null, then first evaluate preemptive
     *  transitions. If no preemptive transition is enabled or the transition
     *  refinement is not null, then evaluate nonpreemptive transitions. If
     *  more than one transition is enabled, an exception is thrown. If there
     *  is exactly one transition enabled, then it is chosen and the choice
     *  actions contained by that transition are executed. The destination
     *  state is returned. If no transition is enabled, the current state is
     *  returned.
     *  @param state The given state.
     *  @return The destination state, or the current state if no
     *   transition is enabled.
     *  @exception IllegalActionException If a non-transient state has
     *   preemptive transitions, or if a transition has refinement.
     */
    public State chooseTransition(State state) throws IllegalActionException {
        State destinationState;
        Transition transition = null;

        if ((state.getRefinement() != null)
                && (state.preemptiveTransitionList().size() != 0)) {
            throw new IllegalActionException(this,
                    state.getName() + " cannot have outgoing preemptive "
                    + "transitions because the state has a refinement.");
        }

        if (state.getRefinement() == null) {
            transition = _chooseTransition(state.preemptiveTransitionList());
        }

        if (transition == null) {
            // No preemptiveTransition enabled. Choose nonpreemptiveTransition.
            transition = _chooseTransition(state.nonpreemptiveTransitionList());
        }

        if (transition == null) {
            destinationState = state;
        } else {
            destinationState = transition.destinationState();
            Actor[] actors = transition.getRefinement();

            if (actors != null) {
                throw new IllegalActionException(this,
                        "MultirateFSM Director does not support "
                        + "transition refinements.");
            }

            _readOutputsFromRefinement();

            //execute the output actions
            Iterator actions = transition.choiceActionList().iterator();

            while (actions.hasNext()) {
                Action action = (Action) actions.next();
                action.execute();
            }
        }

        return destinationState;
    }

    /** Fire the modal model.
     *  If the refinement of the current state of the mode controller is
     *  ready to fire, then fire the current refinement. Choose the next
     *  non-transient state.
     *  @exception IllegalActionException If there is no controller or
     *   the current state has no or more than one refinement.
     */
    public void fire() throws IllegalActionException {
        FSMActor controller = getController();
        _readInputs();

        State currentState = controller.currentState();

        Actor[] actors = currentState.getRefinement();

        // NOTE: Paranoid coding.
        if ((actors == null) || (actors.length != 1)) {
            throw new IllegalActionException(this,
                    "Current state is required to have exactly one refinement: "
                    + currentState.getName());
        }
        if (!_stopRequested) {
            if (actors[0].prefire()) {
                if (_debugging) {
                    _debug(getFullName(), " fire refinement",
                            ((ptolemy.kernel.util.NamedObj) actors[0]).getName());
                }
                actors[0].fire();
                _refinementPostfire = actors[0].postfire();
            }
        }
        _readOutputsFromRefinement();

        chooseNextNonTransientState(currentState);

    }

    /** Return the current state if it has a refinement. Otherwise, make
     *  state transitions until a state with a refinement is found. Set that
     *  non-transient state to be the current state and return it.
     *  @return The non-transient state.
     *  @exception IllegalActionException If a transient state is reached
     *   while no further transition is enabled.
     */
    public State getNonTransientState() throws IllegalActionException {
        FSMActor controller = getController();
        State currentState = controller.currentState();
        TypedActor[] currentRefinements = currentState.getRefinement();

        while (currentRefinements == null) {
            chooseTransition(currentState);
            // Commit the transition.
            super.postfire();
            currentState = controller.currentState();

            Transition lastChosenTransition = _getLastChosenTransition();

            if (lastChosenTransition == null) {
                throw new IllegalActionException(this,
                        "Reached a transient state "
                        + "without an enabled transition.");
            }

            currentRefinements = currentState.getRefinement();
        }

        return currentState;
    }

    /** Initialize the modal model. If this method is called immediately
     *  after preinitialize(), initialize the mode controller and all the
     *  refinements. If this is a reinitialization, it typically means this
     *  is a sub-layer MultirateFSMDirector and a "reset" has been called
     *  at the upper-level MultirateFSMDirector. This method will then
     *  reinitialize all the refinements in the sub-layer. Notify updates
     *  of port rates to the upper level director, and invalidate the upper
     *  level schedule.
     *  @exception IllegalActionException If the refinement has no or more
     *   than one refinement, or the initialize() method of one of the
     *   associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        State currentState;
        FSMActor controller = getController();
        currentState = controller.currentState();

        State initialState = controller.getInitialState();

        if (!_reinitialize) {
            super.initialize();
            _reinitialize = true;

            if (initialState != currentState) {
                // Initial state is a transient (null) state.
                // Set the next intransient state as the current state.
                _setCurrentState(currentState);
                _setCurrentConnectionMap();
                _currentLocalReceiverMap = (Map) _localReceiverMaps.get(currentState);
            }
        } else {
            // This is a sub-layer MultirateFSMDirector.
            // Reinitialize all the refinements in the sub-layer
            // MultirateFSMDirector and recompute the schedule.
            super.initialize();

            // NOTE: The following will throw an exception if
            // the state does not have a refinement, so after
            // this call, we can assume the state has a refinement.
            currentState = getNonTransientState();

            TypedActor[] currentRefinements = currentState.getRefinement();

            if ((currentRefinements == null) || (currentRefinements.length != 1)) {
                throw new IllegalActionException(this,
                        "Multiple refinements are not supported."
                        + " Found multiple refinements in: "
                        + currentState.getName());
            }

            TypedCompositeActor currentRefinement
                = (TypedCompositeActor) (currentRefinements[0]);
            Director refinementDir = currentRefinement.getDirector();

            if (refinementDir instanceof MultirateFSMDirector) {
                refinementDir.initialize();
            }
            /*boolean inputRateChanged = */
            _updateInputTokenConsumptionRates(currentRefinement);
            /* boolean outputRateChanged = */
            _updateOutputTokenProductionRates(currentRefinement);

            // Tell the upper level scheduler that the current schedule
            // is no longer valid.
            /*
            if (inputRateChanged || outputRateChanged) {
                CompositeActor actor = _getEnclosingDomainActor();
                Director director = actor.getExecutiveDirector();
                director.invalidateSchedule();
            }*/
        }
    }

    /** Commit transition and set up new state and connection map if exactly
     *  one transition is enabled. Get the schedule of the current refinement
     *  and propagate its port rates to the outside.
     *  @return True if the super class postfire() method returns true.
     *  @exception IllegalActionException If there is no controller, or if the
     *   destination state has no or more than one refinement, or if the state
     *   refinement throws it.
     */
    public boolean makeStateTransition() throws IllegalActionException {
        // Note: This method is called in postfire() method. We have checked
        // that the current state is not null in initialize() method or
        // in the fire() method.
        FSMActor controller = getController();
        State currentState = controller.currentState();
        Transition lastChosenTransition = _getLastChosenTransition();
        TypedCompositeActor actor;
        Director refinementDir;
        boolean superPostfire;

        // Commit the transition.
        superPostfire = super.postfire();
        currentState = controller.currentState();
        TypedActor[] actors = currentState.getRefinement();

        if ((actors == null) || (actors.length != 1)) {
            throw new IllegalActionException(this,
                    "Current state is required to have exactly "
                    + "one refinement: " + currentState.getName());
        }

        actor = (TypedCompositeActor) (actors[0]);

        if (lastChosenTransition != null) {
            refinementDir = actor.getDirector();

            if (refinementDir instanceof MultirateFSMDirector) {
                refinementDir.postfire();
            } else if (refinementDir instanceof StaticSchedulingDirector) {
                // Get the refinement schedule so we can update the
                // external rates.
                refinementDir.invalidateSchedule();
                ((StaticSchedulingDirector) refinementDir).getScheduler()
                    .getSchedule();
            }
        }

        // Even when the finite state machine remains in the
        // current state, rate signatures may change. This occurs
        // in cases of multi-level MultirateFSM model. The upper level state
        // remains the same but the lower level state has changed.
        /* boolean inputRateChanged = */
        _updateInputTokenConsumptionRates(actor);
        /* boolean outputRateChanged = */
        _updateOutputTokenProductionRates(actor);
        /*
        if (inputRateChanged || outputRateChanged) {
            CompositeActor compositeActor = _getEnclosingDomainActor();
            Director director = compositeActor.getExecutiveDirector();
            director.invalidateSchedule();
        }*/

        return superPostfire;
    }

    /** Return a new receiver of a type compatible with this director.
     *  This returns an instance of SDFReceiver.
     *  @return A new SDFReceiver.
     */
    public Receiver newReceiver() {
        return new SDFReceiver();
    }

    /** Postfire the modal model and commit the transition.
     *  @return True if the postfire() method of current state refinement
     *   and that of the controller are both true.
     *  @exception IllegalActionException If a refinement throws it, or
     *   if there is no controller.
     */
    public boolean postfire() throws IllegalActionException {
        boolean controllerPostfire = makeStateTransition();
        return _refinementPostfire && controllerPostfire;
    }

    /** Preinitialize all actors deeply contained by the container
     *  of this director. Find the "non-transient initial state", which is the
     *  first non-transient state reached from the initial state. Propagate the
     *  consumption and production rates of the non-transient initial state out
     *  to corresponding ports of the container of this director.
     *  @exception IllegalActionException If there is no controller, or if the
     *   non-transient initial state has no or more than one refinement, or if
     *   the preinitialize() method of one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        _reinitialize = false;
        _getEnclosingDomainActor();

        FSMActor controller = getController();
        State initialState = controller.getInitialState();
        _setCurrentState(initialState);

        // NOTE: The following will throw an exception if
        // the state does not have a refinement, so after
        // this call, we can assume the state has a refinement.
        State currentState = getNonTransientState();
        super.preinitialize();
        _setCurrentState(currentState);

        TypedActor[] currentRefinements = currentState.getRefinement();

        if ((currentRefinements == null) || (currentRefinements.length != 1)) {
            throw new IllegalActionException(this,
                    "Current state is required to have exactly one refinement: "
                    + controller.currentState().getName());
        }

        TypedCompositeActor currentRefinement
            = (TypedCompositeActor) (currentRefinements[0]);
        Director refinementDir = currentRefinement.getDirector();

        _updateInputTokenConsumptionRates(currentRefinement);
        _updateOutputTokenProductionRates(currentRefinement);

        // Declare reconfiguration constraints on the ports of the
        // actor.  The constraints indicate that the ports are
        // reconfigured whenever any refinement rate parameter of
        // a corresponding port is reconfigured.  Additionally,
        // all rate parameters are reconfigured every time the
        // controller makes a state transition, unless the
        // corresponding refinement rate parameters are constant,
        // and have the same value.  (Note that the controller
        // itself makes transitions less often if its executive director
        // is an HDFFSMDirector, which is a subclass of MultirateFSMDirector.
        ConstVariableModelAnalysis analysis = ConstVariableModelAnalysis
            .getAnalysis(this);
        CompositeActor model = (CompositeActor) getContainer();

        for (Iterator ports = model.portList().iterator(); ports.hasNext();) {
            IOPort port = (IOPort) ports.next();

            if (!(port instanceof ParameterPort)) {
                if (port.isInput()) {
                    _declareReconfigurationDependencyForRefinementRateVariables(analysis,
                            port, "tokenConsumptionRate");
                }

                if (port.isOutput()) {
                    _declareReconfigurationDependencyForRefinementRateVariables(analysis,
                            port, "tokenProductionRate");
                    _declareReconfigurationDependencyForRefinementRateVariables(analysis,
                            port, "tokenInitProduction");
                }
            }
        }
    }

    /** Transfer data from the input port of the container to the
     *  ports connected to the inside of the input port and on the
     *  mode controller or the refinement of its current state. This
     *  method overrides the base class method by transferring at most
     *  the number of tokens specified by the input consumption rate.
     *  @param port The input port to transfer tokens from.
     *  @return True if data are transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque"
                    + "input port.");
        }

        boolean transferred = false;
        Receiver[][] insideReceivers = _currentLocalReceivers(port);
        int rate = DFUtilities.getTokenConsumptionRate(port);

        for (int i = 0; i < port.getWidth(); i++) {
            // For each channel
            try {
                if ((insideReceivers != null) && (insideReceivers[i] != null)) {
                    for (int j = 0; j < insideReceivers[i].length; j++) {
                        // Since we only transfer number of tokens
                        // declared by the port rate, we should be
                        // safe to clear the receivers.  Maybe we
                        // should move this step to prefire() or
                        // postfire(), as in FSMDirector. But if the
                        // port consumes more tokens than the
                        // refinement actually consumes (The SDF
                        // sneaky trick), then perhaps we are in great
                        // trouble.
                        insideReceivers[i][j].clear();
                    }

                    // Transfer number of tokens at most the declared port rate.
                    // Note: we don't throw exception if there are fewer tokens
                    // available. The prefire() method of the refinement simply
                    // return false.
                    for (int k = 0; k < rate; k++) {
                        if (port.hasToken(i)) {
                            Token token = port.get(i);
                            port.sendInside(i, token);
                        }
                    }

                    // Successfully transferred data, so return true.
                    transferred = true;
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(
                        "Director.transferInputs: Internal error: " + ex);
            }
        }

        return transferred;
    }

    /** Transfer data from an output port of the current refinement actor
     *  to the ports it is connected to on the outside. This method overrides
     *  the base class method in that this method will transfer exactly <i>k</i>
     *  tokens in the receivers, where <i>k</i> is the port rate if it is
     *  declared by the port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     */
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "MultirateFSMDirector: transferOutputs():"
                    + "  port argument is not an opaque output port.");
        }

        boolean transferred = false;
        int rate = DFUtilities.getRate(port);
        Receiver[][] insideReceivers = port.getInsideReceivers();

        for (int i = 0; i < port.getWidth(); i++) {
            if ((insideReceivers != null) && (insideReceivers[i] != null)) {
                for (int k = 0; k < rate; k++) {
                    // Only transfer number of tokens declared by the port
                    // rate. Throw exception if there are not enough tokens.
                    try {
                        Token token = port.getInside(i);
                        port.send(i, token);
                    } catch (NoTokenException ex) {
                        throw new InternalErrorException(
                                "Director.transferOutputs: "
                                + "Not enough tokens for port " + port.getName()
                                + " " + ex);
                    }
                }
            }

            transferred = true;
        }

        return transferred;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Add a DependencyDeclaration (with the name
     *  "_MultirateFSMRateDependencyDeclaration") to the variable with the
     *  given name in the given port that declares the variable is dependent
     *  on the given list of variables.  If a dependency declaration with that
     *  name already exists, then simply set its dependents list to the given
     *  list.
     *  @param analysis The object that contains the dependency declarations.
     *  @param port The IOPort to get rate variables from.
     *  @param name The name of the IOPort.
     *  @param dependents The dependents that the dependency declaration of the
     *  given IO port depends on.
     *  @exception IllegalActionException If a valid rate variable from the
     *  given port can not be found, or the variable does not contain a
     *  DependencyDeclaration attribute, or a new DependencyDeclaration object
     *  can not be created, or can not associated with the analysis object the
     *  newly created DependencyDeclaration object.
     */
    protected void _declareDependency(ConstVariableModelAnalysis analysis,
            IOPort port, String name, List dependents)
            throws IllegalActionException {
        Variable variable = (Variable) DFUtilities.getRateVariable(port, name);
        DependencyDeclaration declaration = (DependencyDeclaration) variable
            .getAttribute("_MultirateFSMRateDependencyDeclaration",
                    DependencyDeclaration.class);

        if (declaration == null) {
            try {
                declaration = new DependencyDeclaration(variable,
                        "_MultirateFSMRateDependencyDeclaration");
            } catch (NameDuplicationException ex) {
                // Ignore... should not happen.
            }
        }

        declaration.setDependents(dependents);
        analysis.addDependencyDeclaration(declaration);
    }

    /** Declare the reconfiguration dependency in the given analysis
     *  associated with the parameter name of the given port.
     *  @param analysis The object that contains the dependency declarations.
     *  @param port The IOPort to get refinement rate variables from.
     *  @param parameterName The name of the rate variables.
     *  @exception IllegalActionException If can not get the refinement rate
     *  variables from the given port, or can not add the dependency declaration
     *  of the given port to the analysis object, or a declared constant rate
     *  variable does not contain a constant value.
     */
    protected void _declareReconfigurationDependencyForRefinementRateVariables(
            ConstVariableModelAnalysis analysis, IOPort port, String parameterName)
            throws IllegalActionException {
        List refinementRateVariables = _getRefinementRateVariables(port,
                parameterName);
        _declareDependency(analysis, port, parameterName,
                refinementRateVariables);

        boolean isConstantAndIdentical = true;
        Token value = null;

        Iterator variables = refinementRateVariables.iterator(); 
        while (variables.hasNext() && isConstantAndIdentical){
            Variable rateVariable = (Variable) variables.next();
            isConstantAndIdentical = isConstantAndIdentical
                && (analysis.getChangeContext(rateVariable) == null);

            if (isConstantAndIdentical) {
                Token newValue = analysis.getConstantValue(rateVariable);

                if (value == null) {
                    value = newValue;
                } else {
                    isConstantAndIdentical = isConstantAndIdentical
                        && (newValue.equals(value));
                }
            }
        }

        //if (!isConstantAndIdentical) {
            // Has this as ChangeContext.
            // System.out.println("Found rate parameter " + parameterName
            //    + " of port " + port.getFullName() + " that changes.");
            // FIXME: Declare this somehow so that we can check it.
        //}
    }

    /** If the container of this director does not have an MultirateFSMDirector
     *  as its executive director, then return the container. Otherwise,
     *  move up the hierarchy until we reach a container actor that does
     *  not have a MultirateFSMDirector director for its executive director,
     *  and return that container.
     *  @return a composite actor that does not contain a MultirateFSMDirector
     *  object.
     *  @exception IllegalActionException If the top-level director is an
     *   MultirateFSMDirector. This director is intended for use only inside
     *   some other domain.
     */
    protected CompositeActor _getEnclosingDomainActor()
            throws IllegalActionException {
        // Keep moving up towards the toplevel of the hierarchy until
        // we find an executive director that is not an instance of
        // MultirateFSMDirector or until we reach the toplevel composite actor.
        CompositeActor container = (CompositeActor) getContainer();
        Director director = container.getExecutiveDirector();

        while (director != null) {
            if (director instanceof MultirateFSMDirector) {
                // Move up another level in the hierarchy.
                container = (CompositeActor) (container.getContainer());
                director = container.getExecutiveDirector();
            } else {
                return container;
            }
        }

        throw new IllegalActionException(this,
                "This director must be contained within another domain.");
    }

    /** Return the set of variables with the given parameter name that are
     *  contained by ports connected to the given port on the inside.
     *  @param port The given port.
     *  @param parameterName The given parameter name.
     *  @return A list of the variables with the given parameter name.
     *  @exception IllegalActionException If can not get a rate variable
     *  from the port that is connected to the given port from inside.
     */
    protected List _getRefinementRateVariables(IOPort port, String parameterName)
            throws IllegalActionException {
        List list = new LinkedList();

        Iterator insidePorts = port.deepInsidePortList().iterator();
        while(insidePorts.hasNext()) {
            IOPort insidePort = (IOPort) insidePorts.next();
            Variable variable = (Variable) DFUtilities.getRateVariable(insidePort,
                    parameterName);

            if (variable != null) {
                list.add(variable);
            }
        }

        return list;
    }

    /** Extract the token consumption rates from the input ports of the current
     *  refinement and update the rates of the input ports of the modal model
     *  containing the refinement.
     *  @param actor The current refinement.
     *  @return True if any input token consumption rate is changed from its
     *   previous value.
     *  @exception IllegalActionException If can not find the controller, or
     *  the port connections between controller and refinements are not
     *  correct, or can not get valid token consumption rates for input ports.
     */
    protected boolean _updateInputTokenConsumptionRates(
            TypedCompositeActor actor) throws IllegalActionException {
        boolean inputRateChanged = false;

        // Get the current refinement's container.
        CompositeActor refineInPortContainer = (CompositeActor) actor
            .getContainer();

        // Get all of its input ports of the current refinement actor.
        Iterator refineInPorts = actor.inputPortList().iterator();

        while (refineInPorts.hasNext()) {
            IOPort refineInPort = (IOPort) refineInPorts.next();

            // Get all of the input ports this port is linked to on
            // the outside (should only consist of 1 port).
            Iterator inPortsOutside = refineInPort.deepConnectedInPortList()
                .iterator();

            if (!inPortsOutside.hasNext()) {
                throw new IllegalActionException("Current "
                        + "state's refining actor has an input port not"
                        + "connected to an input port of its container.");
            }

            while (inPortsOutside.hasNext()) {
                IOPort inputPortOutside = (IOPort) inPortsOutside.next();

                // Check if the current port is contained by the
                // container of the current refinement.
                ComponentEntity thisPortContainer = (ComponentEntity) inputPortOutside
                    .getContainer();

                if (thisPortContainer.getFullName().equals(refineInPortContainer
                        .getFullName())) {
                    // set the outside port rate equal to the port rate
                    // of the refinement.
                    int previousPortRate = DFUtilities.getTokenConsumptionRate(inputPortOutside);
                    int portRateToSet = DFUtilities.getTokenConsumptionRate(refineInPort);

                    if (previousPortRate != portRateToSet) {
                        inputRateChanged = true;
                    }

                    DFUtilities.setTokenConsumptionRate(inputPortOutside,
                            portRateToSet);
                }
            }
        }

        return inputRateChanged;
    }

    /** Extract the token production rates from the output ports of the current
     *  refinement and update the production and initial production rates of the
     *  output ports of the modal model containing the refinement.
     *  @param actor The current refinement.
     *  @return True if any of the output token production rate is changed from
     *   its previous value.
     *  @exception IllegalActionException If we cannot get valid token
     *  consumption rates for input ports.
     */
    protected boolean _updateOutputTokenProductionRates(
            TypedCompositeActor actor) throws IllegalActionException {
        boolean outputRateChanged = false;

        // Get the current refinement's container.
        CompositeActor refineOutPortContainer = (CompositeActor) actor
            .getContainer();

        // Get all of the current refinement's output ports.
        Iterator refineOutPorts = actor.outputPortList().iterator();

        while (refineOutPorts.hasNext()) {
            IOPort refineOutPort = (IOPort) refineOutPorts.next();

            Iterator outPortsOutside = refineOutPort.deepConnectedOutPortList()
                .iterator();

            while (outPortsOutside.hasNext()) {
                IOPort outputPortOutside = (IOPort) outPortsOutside.next();

                // Check if the current port is contained by the
                // container of the current refinment.
                ComponentEntity thisPortContainer = (ComponentEntity) outputPortOutside
                    .getContainer();

                if (thisPortContainer.getFullName().equals(refineOutPortContainer
                        .getFullName())) {
                    // set the outside port rate equal to the port rate
                    // of the refinement.
                    int previousPortRate = DFUtilities.getTokenProductionRate(outputPortOutside);
                    int portRateToSet = DFUtilities.getTokenProductionRate(refineOutPort);
                    int portInitRateToSet = DFUtilities.getTokenInitProduction(refineOutPort);

                    if (previousPortRate != portRateToSet) {
                        outputRateChanged = true;
                    }

                    DFUtilities.setTokenProductionRate(outputPortOutside,
                            portRateToSet);
                    DFUtilities.setTokenInitProduction(outputPortOutside,
                            portInitRateToSet);
                }
            }
        }

        return outputRateChanged;
    }

    /////////////////////////////////////////////////////////////////////////
    ////                       protected variables                       ////

    /** A flag indicating whether the initialize method is called due
     *  to reinitialization.
     */
    protected boolean _reinitialize;

    /** The returned value of the postfire() method of the currentRefinement.
     */
    protected boolean _refinementPostfire;
}
