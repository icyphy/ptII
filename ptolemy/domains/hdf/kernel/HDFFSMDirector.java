/* A HDFFSMDirector governs the execution of the finite state
   machine in heterochronous dataflow model.

   Copyright (c) 1999-2004 The Regents of the University of California.
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

package ptolemy.domains.hdf.kernel;

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
import ptolemy.domains.fsm.kernel.Action;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// HDFFSMDirector
/**
   This director extends FSMDirector by supporting production and
   consumption of multiple tokens on a port in a firing and by
   restricting the 
   
   FIXME: Refactor into two directors, with the base class
   MultirateFSMDirector supporting multirate.
   
   An HDFFSMDirector governs the execution of a finite state machine
   (FSM) in a heterochronous dataflow (HDF) or synchronous dataflow
   (SDF) model according to the *charts [1] semantics. *charts is a
   family of models of computation that specifies an operational
   semantics for composing hierarchical FSMs with various concurrency
   models.
   <p>
   This director is used with a modal model that consumes and produces
   any number of tokens on its ports. The number of tokens consumed
   and produced is determined by the refinement of the current state.
   FIXME: Figure out what it actually does and what it should do.

   The subset of *charts that this class supports is HDF inside FSM
   inside HDF, SDF inside FSM inside HDF, and SDF inside FSM inside SDF.
   This class must be used as the director of an FSM or ModalModel
   when the FSM refines
   an HDF or SDF composite actor, unless all the ports rates are always 1,
   in which case, the base class FSMDirector can be used. This director
   can also be used in an FSM or ModalModel in DDF.
   <p>
   This director assumes that every state has exactly one refinement, with one
   exception. A state may have no refinement if upon being entered, it has
   an outgoing transition with a guard that is true. This will be treated as a
   "transient state," in that the FSM will progress through that state
   to the next state until it encounters a state with a refinement.
   <p>
   <b>Usage</b>
   <p>
   Hierarchical compositions of HDF with FSMs can be quite complex to
   represent textually, even for simple models. It is therefore
   recommended that a graphical model editor like Vergil be used to
   construct the model.
   <p>
   The executive director must be HDF, SDF, or HDFFSMDirector.
   Otherwise an exception will occur. An HDF or SDF composite actor that
   refines to an FSM will use this class as the FSM's local director.
   All states in the FSM must refine to either another FSM, an HDF model
   or a SDF model. That is, all refinement actors must be opaque and must
   externally have HDF or SDF semantics. There is no constraint on
   the number of levels in the hierarchy.
   <p>
   To use this director, create a ModalModel and specify this director
   as its director.  Then look inside to populate the controller
   with states. Create one TypedComposite actor as a refinement
   for each state in the FSM.
   <p>
   You must explicitly specify the initial state of the controller FSM.
   The guard expression on each transition is evaluated only after a
   "Type B firing" [1], which is the last firing of the HDF actor
   in the current global iteration of the current HDF schedule. A state
   transition will occur if the guard expression evaluates to true
   after a "Type B firing."
   <p>
   <b>References</b>
   <p>
   <OL>
   <LI>
   A. Girault, B. Lee, and E. A. Lee,
   ``<A HREF="http://ptolemy.eecs.berkeley.edu/papers/98/starcharts">
   Hierarchical Finite State Machines with Multiple Concurrency Models</A>,
   '' April 13, 1998.</LI>
   </ol>

   @author Rachel Zhou and Brian K. Vogel
   @version $Id$
   @Pt.ProposedRating Red (zhouye)
   @Pt.AcceptedRating Red (cxh)
   @see HDFDirector
*/
public class HDFFSMDirector extends FSMDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public HDFFSMDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public HDFFSMDirector(Workspace workspace) {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *  with the specified container.
     *  @exception NameDuplicationException If the container is not a
     *  CompositeActor and the name collides with an entity in the container.
     */
    public HDFFSMDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Examine the non-preemptive transitions from the given state.
     *  If there is more than one transition enabled, an exception is
     *  thrown. If there is exactly one non-preemptive transition
     *  enabled, then it is chosen and the choice actions contained by
     *  transition are executed. Return the destination state. If no
     *  transition is enabled, return the current state.
     *  @return The destination state, or the current state if no
     *   transition is enabled.
     */
    public State chooseStateTransition(State state)
            throws IllegalActionException {

        FSMActor controller = getController();
        State destinationState;
        Transition transition = null;
        if (state.getRefinement() != null 
                && state.preemptiveTransitionList().size() != 0) {
            throw new IllegalActionException(this,
                    state.getName() + " cannot have outgoing preemptive " +
                    "transitions because the state has a refinement.");
        }
        if (state.getRefinement() == null) {
            transition = _chooseTransition(state.preemptiveTransitionList());
        }
        if (transition == null) {
            // No preemptiveTransition enabled. Choose nonpreemptiveTransition.
            transition = _chooseTransition(state.nonpreemptiveTransitionList());
        }
        if (transition != null) {
            destinationState = transition.destinationState();
            TypedActor[] trRefinements = (transition.getRefinement());

            Actor[] actors = transition.getRefinement();
            if (actors != null) {
                throw new IllegalActionException(this,
                      "HDFFSM Director does not support transition refinements.");
            }
            _readOutputsFromRefinement();
            //execute the output actions
            Iterator actions = transition.choiceActionList().iterator();
            while (actions.hasNext()) {
                Action action = (Action)actions.next();
                action.execute();
            }
        } else {
            destinationState = state;
        }
        return destinationState;
    }

   /** Choose the next non-transient state given the current state.
    * @param currentState The current state.
    * @throws IllegalActionException If a transient state is reached
    *  but no further transition is enabled.
    */
    public void chooseTransitions(State currentState) 
            throws IllegalActionException {
        State state = chooseStateTransition(currentState);
        Actor[] actors = state.getRefinement();
        Transition transition;
        while (actors == null) {
            super.postfire();
            state = chooseStateTransition(state);
            transition = _getLastChosenTransition();
            if (transition == null) {
                throw new IllegalActionException(this,
                		"Reached a state without a refinement: "
                        + state.getName());
            }
            actors = (transition.destinationState()).getRefinement();
        }
    }

    /** Set the values of input variables in the mode controller.
     *  If the refinement of the current state of the mode controller
     *  is ready to fire, then fire the current refinement.
     *  Choose a transition if this FSM is embedded in SDF, otherwise
     *  request to choose a transition to the manager.
     *  @exception IllegalActionException If there is no controller.
     */
    public void fire() throws IllegalActionException {
        CompositeActor container = (CompositeActor)getContainer();
        FSMActor controller = getController();
        controller.setNewIteration(_sendRequest);
        _readInputs();
        Transition transition;
        State currentState = controller.currentState();
        _lastIntransientState = currentState;
        Actor[] actors = currentState.getRefinement();
        
        // NOTE: Paranoid coding.
        if (actors == null || actors.length != 1) {
        	throw new IllegalActionException(this,
                    "Current state is required to have exactly one refinement: "
                    + currentState.getName());
        }

        for (int i = 0; i < actors.length; ++ i) {
            if (_stopRequested) break;
            if (actors[i].prefire()) {
                actors[i].fire();
                actors[i].postfire();
            }
        }

        _readOutputsFromRefinement();

        if (!_embeddedInHDF) {
            chooseTransitions(currentState);
        } else if (_sendRequest) {
            ChangeRequest request =
                new ChangeRequest(this, "choose a transition") {
                    protected void _execute() throws KernelException, 
                            IllegalActionException{
                        FSMActor controller = getController();
                        State currentState = controller.currentState();
                        chooseTransitions(currentState);
                    }
                };
            request.setPersistent(false);
            container.requestChange(request);
        }
        return;
    }

    /** Return the change context being made explicit.  This class
     *  overrides the implementation in the FSMDirector base class to
     *  report that HDF models only make state transitions between
     *  toplevel iterations.
     */
    public Entity getContext() {
        // Set the flag indicating whether we're in an SDF model or
        // not.
        try {
            _getEnclosingDomainActor();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex);
        }
        if(!_embeddedInHDF) {
            return super.getContext();
        } else {
            return (Entity)toplevel();
        }
    }

    /** If this method is called immediately after preinitialize(),
     *  initialize the mode controller and all the refinements.
     *  If this is a reinitialization, it typically means this
     *  is a sub-layer HDFFSMDirector and a "reset" has been called
     *  at the upper-level HDFFSMDirector. This method will then
     *  reinitialize all the refinements in the sub-layer, recompute
     *  the schedule of the initial state in the sub-layer, and notify
     *  update of port rates to the upper level director.
     *  @exception IllegalActionException If the initialize() method of
     *  one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        State currentState;
        FSMActor controller = getController();
        currentState = controller.currentState();
        State initialState = controller.getInitialState();
        if (!_reinitialize) {
            super.initialize();
            _reinitialize = true;
            if (initialState != _nextIntransientState) {
                // Initial state is a transient (null) state.
                // Set the next intransient state as the current state.
                _setCurrentState(_nextIntransientState);
                _setCurrentConnectionMap();
                _currentLocalReceiverMap =
                    (Map)_localReceiverMaps.get(_nextIntransientState);
            }
        } else {
            // This is a sub-layer HDFFSMDirector.
            // Reinitialize all the refinements in the sub-layer
            // HDFFSMDirector and recompute the schedule.
            super.initialize();
            _sendRequest = true;
            controller.setNewIteration(_sendRequest);
            // NOTE: The following will throw an exception if
            // the state does not have a refinement, so after
            // this call, we can assume the state has a refinement.
            currentState = transientStateTransition();
            TypedActor[] curRefinements = currentState.getRefinement();
            if (curRefinements == null || curRefinements.length != 1) {
                throw new IllegalActionException(this,
                        "Multiple refinements are not supported."
                        + " Found multiple refinements in: "
                        + currentState.getName());
            }
            TypedCompositeActor curRefinement =
                (TypedCompositeActor)(curRefinements[0]);
            Director refinementDir = curRefinement.getDirector();
            if (refinementDir instanceof HDFFSMDirector) {
                refinementDir.initialize();
            } else if (refinementDir instanceof StaticSchedulingDirector) {
                // Recompute the schedule if the refinement domain has a schedule.
                refinementDir.invalidateSchedule();
                ((StaticSchedulingDirector)refinementDir).getScheduler().getSchedule();
            }
            _updateInputTokenConsumptionRates(curRefinement);
            _updateOutputTokenProductionRates(curRefinement);
            // Tell the upper level scheduler that the current schedule
            // is no longer valid.
            CompositeActor hdfActor = _getEnclosingDomainActor();
            Director director = hdfActor.getExecutiveDirector();
            ((StaticSchedulingDirector)director).invalidateSchedule();
        }
    }

    /** Set up new state and connection map if exactly
     *  one transition is enabled. Get the schedule of the current
     *  refinement and propagate its port rates to the outside.
     *  @return True if the super class method returns true.
     *  @exception IllegalActionException If a refinement throws it,
     *  if there is no controller, or if an inconsistency in port
     *  rates is detected between refinement actors.
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
        if (lastChosenTransition  == null) {
            // No transition enabled. Remain in the current state.
            TypedActor[] actors = currentState.getRefinement();
            if (actors == null || actors.length != 1) {
                throw new IllegalActionException(this,
                        "Current state is required to have exactly one refinement: "
                        + currentState.getName());
            }
            actor  = (TypedCompositeActor)(actors[0]);
            superPostfire = super.postfire();
            
        } else {
            // Make a state transition.
            State newState = lastChosenTransition.destinationState();
            _setCurrentState(newState);
            superPostfire = super.postfire();
            currentState = newState;
            // Get the new current refinement actor.
            TypedActor[] actors = currentState.getRefinement();
            if (actors == null || actors.length != 1) {
                throw new IllegalActionException(this,
                        "Current state is required to have exactly one refinement: "
                        + currentState.getName());
            }
            actor = (TypedCompositeActor)(actors[0]);
            refinementDir = actor.getDirector();
            if (refinementDir instanceof HDFFSMDirector) {
                refinementDir.postfire();
            } else if (refinementDir instanceof StaticSchedulingDirector) {
                refinementDir.invalidateSchedule();
                ((StaticSchedulingDirector)refinementDir).getScheduler().getSchedule();
            }
        }
        // Even when the finite state machine remains in the
        // current state, the schedule may change. This occurs
        // in cases of multi-level HDFFSM model. The sup-mode
        // remains the same but the sub-mode has changed.
        _updateInputTokenConsumptionRates(actor);
        _updateOutputTokenProductionRates(actor);

        CompositeActor hdfActor = _getEnclosingDomainActor();
        Director director = hdfActor.getExecutiveDirector();
        director.invalidateSchedule();
        return superPostfire;
    }

    /** Return a new receiver of a type compatible with this director.
     *  This returns an instance of SDFReceiver.
     *  @return A new SDFReceiver.
     */
    public Receiver newReceiver() {
        return new SDFReceiver();
    }

    /** Make a state transition if this FSM is embedded in SDF.
     *  Otherwise, request a change of state transition to the manager.
     *  <p>
     *  @return True if the FSM is inside SDF and the super class
     *  method returns true; otherwise return true if the postfire of
     *  the current state refinement returns true.
     *  @exception IllegalActionException If a refinement throws it,
     *  if there is no controller.
     */
    public boolean postfire() throws IllegalActionException {
        FSMActor controller = getController();
        CompositeActor container = (CompositeActor)getContainer();
        TypedActor[] currentRefinement = _lastIntransientState.getRefinement();
        
        // NOTE: We have already checked that there is exactly
        // one refinement of the current state.

        boolean postfireReturn = currentRefinement[0].postfire();

        if (_sendRequest && _embeddedInHDF) {
            _sendRequest = false;
            ChangeRequest request =
                new ChangeRequest(this, "make a transition") {
                    protected void _execute() throws KernelException {
                        _sendRequest = true;
                        makeStateTransition();
                    }
                };
            request.setPersistent(false);
            container.requestChange(request);
        }
        if (!_embeddedInHDF) {
            makeStateTransition();
        }

        return postfireReturn;
    }

    /** Return true if the mode controller is ready to fire.
     *  @exception IllegalActionException If there is no controller.
     */
    public boolean prefire() throws IllegalActionException {
        return getController().prefire();
    }

    /** Preinitialize() methods of all actors deeply contained by the
     *  container of this director. The HDF/SDF preinitialize method
     *  will compute the initial schedule. Propagate the consumption
     *  and production rates of the current state out to the
     *  corresponding ports of the container of this director.
     *  @exception IllegalActionException If the preinitialize()
     *  method of one of the associated actors throws it, or there
     *  is no controller.
     */
    public void preinitialize() throws IllegalActionException {
        _sendRequest = true;
        _reinitialize = false;
        _getEnclosingDomainActor();

        FSMActor controller = getController();
        State initialState = controller.getInitialState();
        _setCurrentState(initialState);
        
        // NOTE: The following will throw an exception if
        // the state does not have a refinement, so after
        // this call, we can assume the state has a refinement.
        _nextIntransientState = transientStateTransition();
        super.preinitialize();
        _setCurrentState(_nextIntransientState);
        TypedActor[] currentRefinements
                = _nextIntransientState.getRefinement();
        if (currentRefinements == null || currentRefinements.length != 1) {
            throw new IllegalActionException(this,
                    "Current state is required to have exactly one refinement: "
                    + controller.currentState().getName());
        }
        
        TypedCompositeActor curRefinement
                = (TypedCompositeActor)(currentRefinements[0]);
        Director refinementDir = curRefinement.getDirector();
        
        _updateInputTokenConsumptionRates(curRefinement);
        _updateOutputTokenProductionRates(curRefinement);
           
        // Declare reconfiguration constraints on the ports of the
        // actor.  The constraints indicate that the ports are
        // reconfigured whenever any refinement rate parameter of
        // a corresponding port is reconfigured.  Additionally,
        // all rate parameters are reconfigured every time the
        // controller makes a state transition, unless the
        // corresponding refinement rate parameters are constant,
        // and have the same value.  (Note that the controller
        // itself makes transitions less often if it is contained
        // in an HDF model, rather than in an SDF model.)
        ConstVariableModelAnalysis analysis =
            ConstVariableModelAnalysis.getAnalysis(this);
        CompositeActor model = (CompositeActor)getContainer();
        for (Iterator ports = model.portList().iterator();
             ports.hasNext();) {
            IOPort port = (IOPort) ports.next();
            if (!(port instanceof ParameterPort)) {
                if (port.isInput()) {
                    _declareReconfigurationDependencyForRefinementRateVariables(
                            analysis, port, "tokenConsumptionRate");                   
                }
                if (port.isOutput()) {
                    _declareReconfigurationDependencyForRefinementRateVariables(
                            analysis, port, "tokenProductionRate"); 
                    _declareReconfigurationDependencyForRefinementRateVariables(
                            analysis, port, "tokenInitProduction"); 
                }
            }
        }
    }

    /** Return true if data are transferred from the input port of
     *  the container to the connected ports of the controller and
     *  of the current refinement actor.
     *  <p>
     *  This method will transfer all of the available tokens on each
     *  input channel. The port argument must be an opaque input port.
     *  If any channel of the input port has no data, then that
     *  channel is ignored. Any token left not consumed in the ports
     *  to which data are transferred is discarded.
     *  @param port The input port to transfer tokens from.
     *  @return True if data are transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *  input port.
     */
    public boolean transferInputs(IOPort port)
            throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque" +
                    "input port.");
        }
        boolean transferred = false;
        // The receivers of the current refinement that receive data
        // from "port."
        Receiver[][] insideReceivers = _currentLocalReceivers(port);
        
        int rate = DFUtilities.getTokenConsumptionRate(port);
        for (int i = 0; i < port.getWidth(); i++) {
            // For each channel
            try {
                if (insideReceivers != null
                        && insideReceivers[i] != null) {
                    for (int j = 0; j < insideReceivers[i].length; j++) {
                        while (insideReceivers[i][j].hasToken()) {
                            // clear tokens.
                            insideReceivers[i][j].get();
                        }
                    }
                    for (int k = 0; k < rate; k++) {
                        if (port.hasToken(i)) {
                            ptolemy.data.Token t = port.get(i);
                            for (int j = 0; j < insideReceivers[i].length; j++) {
                                insideReceivers[i][j].put(t);
                            } 
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
     *  to the ports it is connected to on the outside. This method differs
     *  from the base class method in that this method will transfer <i>k</i> 
     *  tokens in the receivers, where <i>k</i> is the port rate if it is
     *  declared by the port. If the port rate is not declared, this method
     *  behaves like the base class method and will transfer at most one token.
     *  This behavior is required to handle the case of multi-rate actors.
     *  The port argument must be an opaque output port.
     *  @exception IllegalActionException If the port is not an opaque
     *  output port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */

    public boolean transferOutputs(IOPort port)
            throws IllegalActionException {

        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "HDFFSMDirector: transferOutputs():" +
                    "  port argument is not an opaque output port.");
        }
        boolean transferred = false;
        int rate = DFUtilities.getRate(port);
        Receiver[][] insideReceivers = port.getInsideReceivers();
        for (int i = 0; i < port.getWidth(); i ++) {
            if (insideReceivers != null && insideReceivers[i] != null) {
                for (int j = 0; j < insideReceivers[i].length; j++) {
                    for (int k = 0; k < rate; k ++) {
                        // Only transfer number of tokens declared 
                        // by the port rate.
                        try {
                            ptolemy.data.Token t =
                                insideReceivers[i][j].get();
                            port.send(i, t);
                        } catch (NoTokenException ex) {
                            throw new InternalErrorException(
                                    "Director.transferOutputs: " +
                                    "Not enough tokens for port " 
                                    + port.getName() + " " + ex);
                        }
                    }
                }
            }
            transferred = true;
        }
        return transferred;
    }
    
    /** Get the current state. If it does not have any refinement,
     *  consider it as a transient state and make a state transition
     *  until a state with a refinement is
     *  found. Set that non-transient state to be the current state
     *  and return it.
     * @throws IllegalActionException If a transient state is reached
     *  while no further transition is enabled.
     * @return The intransient state.
     */
    public State transientStateTransition()
                throws IllegalActionException {
        FSMActor controller = getController();
        State currentState = controller.currentState();
        TypedActor[] currentRefinements = currentState.getRefinement();
        while (currentRefinements == null) {
            chooseStateTransition(currentState);
            super.postfire();
            currentState = controller.currentState();
            Transition lastChosenTransition = _getLastChosenTransition();
            if (lastChosenTransition == null) {
                throw new IllegalActionException(this,
                    "Reached a transient state " +
                    "without an enabled transition.");
            }
            else {
                currentRefinements = currentState.getRefinement();
            }
        }
        return currentState;            
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Add a DependencyDeclaration (with the name
     * "_HDFFSMRateDependencyDeclaration") to the variable with the given
     * name in the given port that declares the variable is dependent
     * on the given list of variables.  If a dependency declaration
     * with that name already exists, then simply set its dependents
     * list to the given list.
     */
    protected void _declareDependency(ConstVariableModelAnalysis analysis,
            IOPort port, String name, List dependents)
            throws IllegalActionException {
        Variable variable =
            (Variable)DFUtilities.getRateVariable(port, name);
        DependencyDeclaration declaration = (DependencyDeclaration)
            variable.getAttribute(
                    "_HDFFSMRateDependencyDeclaration",
                    DependencyDeclaration.class);
        if (declaration == null) {
            try {
                declaration = new DependencyDeclaration(variable,
                        "_HDFFSMRateDependencyDeclaration");
            } catch (NameDuplicationException ex) {
                // Ignore... should not happen.
            }
        }
        declaration.setDependents(dependents);
        analysis.addDependencyDeclaration(declaration);
    }

    // Declare the reconfiguration dependency in the given analysis
    // associated wiht the parameter name of the given port.
    private void _declareReconfigurationDependencyForRefinementRateVariables(
            ConstVariableModelAnalysis analysis,
            IOPort port, 
            String parameterName) throws IllegalActionException {
        List refinementRateVariables = 
            _getRefinementRateVariables(port, parameterName);
        _declareDependency(
                analysis, port, parameterName,
                refinementRateVariables);
        boolean isConstantAndIdentical = true;
        Token value = null;
        for(Iterator variables = refinementRateVariables.iterator();
            variables.hasNext() && isConstantAndIdentical;) {
            Variable rateVariable = (Variable)variables.next();
            isConstantAndIdentical = isConstantAndIdentical &&
                (analysis.getChangeContext(rateVariable) == null);
            if(isConstantAndIdentical) {
                Token newValue = analysis.getConstantValue(rateVariable);
                if(value == null) {
                    value = newValue;
                } else {
                    isConstantAndIdentical = isConstantAndIdentical &&
                        (newValue.equals(value));
                }
            }
        }
        if(!isConstantAndIdentical) {
            // Has this as ChangeContext.
            // System.out.println("Found rate parameter " + parameterName + " of port " + port.getFullName() + " that changes.");
            // FIXME: Declare this somehow so that we can check it.
        }
    }        

    /** Return the set of variables with the given name that are
     * contained by ports connected to the given port on the inside.
     */
    private List _getRefinementRateVariables(
            IOPort port, String parameterName)
            throws IllegalActionException {
        List list = new LinkedList();
        for(Iterator insidePorts = port.deepInsidePortList().iterator();
            insidePorts.hasNext();) {
            IOPort insidePort = (IOPort)insidePorts.next();
            Variable variable = (Variable)
                DFUtilities.getRateVariable(insidePort, parameterName);
            if(variable != null) {
                list.add(variable);
            }
        }
        return list;
    }

    /** If the container of this director does not have an
     *  HDFFSMDirector as its executive director, then return the container.
     *  Otherwise, move up the hierarchy until we reach a container
     *  actor that does not have an HDFFSMDirector director for its
     *  executive director, and return that container.
     *  @exception IllegalActionException If the top-level director
     *   is an HDFFSMDirector. This director is intended for use only
     *   inside some other domain.
     */
    private CompositeActor _getEnclosingDomainActor()
            throws IllegalActionException {
        // Keep moving up towards the toplevel of the hierarchy until
        // we find either an SDF or HDF executive director or we reach
        // the toplevel composite actor.
        CompositeActor container = (CompositeActor)getContainer();
        Director director = container.getExecutiveDirector();
        while (director != null) {
        	if (director instanceof HDFDirector) {
                _embeddedInHDF = true;
                return container;
            } else if (director instanceof HDFFSMDirector) {
                // Move up another level in the hierarchy.
                container = (CompositeActor)(container.getContainer());
                director = container.getExecutiveDirector();
            } else {
                return container;
            }
        }
        throw new IllegalActionException(this,
                "This director must be contained within another domain.");
    }

    /** Extract the token consumption rates from the input
     *  ports of the current refinement and update the
     *  rates of the input ports of the HDF opaque composite actor
     *  containing the refinment. The resulting mutation will cause
     *  the SDF scheduler to compute a new schedule using the
     *  updated rate information.
     *  @param actor The current refinement.
     */
    private void _updateInputTokenConsumptionRates(
            TypedCompositeActor actor) throws IllegalActionException {

        FSMActor ctrl = getController();
        // Get the current refinement's container.
        CompositeActor refineInPortContainer =
            (CompositeActor) actor.getContainer();
        Transition lastChosenTr = _getLastChosenTransition();

        // Get all of the input ports of the container of this director.
        List containerPortList = refineInPortContainer.inputPortList();
        // Set all of the port rates to zero.
        Iterator containerPorts = containerPortList.iterator();
        while (containerPorts.hasNext()) {
            IOPort containerPort = (IOPort)containerPorts.next();
            DFUtilities.setTokenConsumptionRate(
                    containerPort, 0);
        }
        // Get all of its input ports of the current refinement actor.
        Iterator refineInPorts = actor.inputPortList().iterator();
        while (refineInPorts.hasNext()) {
            IOPort refineInPort =
                (IOPort)refineInPorts.next();
            // Get all of the input ports this port is linked to on
            // the outside (should only consist of 1 port).

            // Iterator inPorts = inputPortList().iterator();
            // while (inPorts.hasNext()) {
            //     IOPort inPort = (IOPort)inPorts.next();
            Iterator inPortsOutside =
                refineInPort.deepConnectedInPortList().iterator();
            if (!inPortsOutside.hasNext()) {
                throw new IllegalActionException("Current " +
                        "state's refining actor has an input port not" +
                        "connected to an input port of its container.");
            }
            while (inPortsOutside.hasNext()) {
                IOPort inputPortOutside =
                    (IOPort)inPortsOutside.next();

                // Check if the current port is contained by the
                // container of the current refinement.
                ComponentEntity thisPortContainer =
                    (ComponentEntity)inputPortOutside.getContainer();
                String temp = refineInPortContainer.getFullName()
                    + "._Controller";

                if (thisPortContainer.getFullName() ==
                        refineInPortContainer.getFullName() ||
                        temp.equals(thisPortContainer.getFullName())) {
                    // set the outside port rate equal to the port rate
                    // of the refinement.
                    int portRateToSet = DFUtilities
                        .getTokenConsumptionRate(refineInPort);
                    DFUtilities.setTokenConsumptionRate
                        (inputPortOutside, portRateToSet);
                } else {
                    State curState = ctrl.currentState();
                    List transitionList =
                        curState.nonpreemptiveTransitionList();
                    Iterator transitions = transitionList.iterator();
                    while (transitions.hasNext()) {
                        Transition transition =
                            (Transition)transitions.next();
                        if (transition != null) {
                            TypedActor[] trRefinements
                                = (transition.getRefinement());
                            if (trRefinements != null) {
                                for (int i = 0;
                                     i < trRefinements.length; i ++) {
                                    TypedCompositeActor trRefinement
                                        = (TypedCompositeActor)
                                        (trRefinements[i]);
                                    String trRefinementName
                                        = trRefinement.getFullName();
                                    if (thisPortContainer.getFullName()
                                            == trRefinementName) {
                                        int portRateToSet =
                                            DFUtilities
                                            .getTokenConsumptionRate
                                            (refineInPort);
                                        int transitionPortRate =
                                            DFUtilities.
                                            getTokenConsumptionRate
                                            (inputPortOutside);
                                        if (portRateToSet
                                                != transitionPortRate) {
                                            throw new IllegalActionException(
                                                    this, "Consumption rate of"
                                                    + "transition refinement "
                                                    + "not consistent with the"
                                                    + "consumption rate of the"
                                                    + "state refinement.");

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** Extract the token production rates from the output
     *  ports of the current refinement and update the
     *  production and initial production rates of the output
     *  ports of the HDF opaque composite actor
     *  containing the refinment. The resulting mutation will cause
     *  the SDF scheduler to compute a new schedule using the
     *  updated rate information.
     *  @param actor The current refinement.
     */
    private void _updateOutputTokenProductionRates(
            TypedCompositeActor actor) throws IllegalActionException {

        // Get the current refinement's container.
        CompositeActor refineOutPortContainer =
            (CompositeActor) actor.getContainer();
        // Get all of the output ports of the container of this director.
        List containerPortList = refineOutPortContainer.outputPortList();
        // Set all of the external port rates to zero.
        Iterator containerPorts = containerPortList.iterator();
        while (containerPorts.hasNext()) {
            IOPort containerPort = (IOPort)containerPorts.next();
            DFUtilities.setTokenProductionRate(
                    containerPort, 0);
        }
        // Get all of the current refinement's output ports.
        Iterator refineOutPorts = actor.outputPortList().iterator();
        while (refineOutPorts.hasNext()) {
            IOPort refineOutPort =
                (IOPort)refineOutPorts.next();
            // Get all of the output ports this port is
            // linked to on the outside (should only consist
            // of 1 port).
            Iterator outPortsOutside =
                refineOutPort.deepConnectedOutPortList().iterator();
            //if (!outPortsOutside.hasNext()) {
            //throw new IllegalActionException("Current " +
            //          "state's refining actor has an output " +
            //          "port not connected to an output port " +
            //          "of its container.");
            //}
            while (outPortsOutside.hasNext()) {
                IOPort outputPortOutside =
                    (IOPort)outPortsOutside.next();
                // Check if the current port is contained by the
                // container of the current refinment.
                ComponentEntity thisPortContainer =
                    (ComponentEntity)outputPortOutside.getContainer();
                String temp = refineOutPortContainer.getFullName()
                    + "._Controller";
                if (thisPortContainer.getFullName() ==
                        refineOutPortContainer.getFullName()) {
                    // set the outside port rate equal to the port rate
                    // of the refinement.
                    int portRateToSet = DFUtilities
                        .getTokenProductionRate(refineOutPort);
                    int portInitRateToSet = DFUtilities
                        .getTokenInitProduction(refineOutPort);
                    DFUtilities.setTokenProductionRate
                        (outputPortOutside, portRateToSet);
                    DFUtilities.setTokenInitProduction
                        (outputPortOutside, portInitRateToSet);
                } else if (temp.equals(thisPortContainer.getFullName())) {
                    // set the outside port rate equal to the port rate of
                    // the refinement.
                    int portRateToSet = DFUtilities
                        .getTokenProductionRate(refineOutPort);
                    DFUtilities.setTokenConsumptionRate
                        (outputPortOutside, portRateToSet);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A flag indicating whether the FSM can send a change request.
    // An FSM in HDF can only send one request per global iteration.
    private boolean _sendRequest;

    /** A flag indicating whether this FSM is embedded in HDF.
     *  If the flag is true, the transitions are not allowed
     *  between arbitrary firings. They are allowed only between
     *  iterations.
     */
    private boolean _embeddedInHDF = false;

    // A flag indicating whether the initialize method is
    // called due to reinitialization.
    private boolean _reinitialize;
    
    // The next intransient state found after making transition
    // from transient states.
    private State _nextIntransientState;
    
    // The last intransient state reached. This referes to the
    // current state if it is intransient.
    private State _lastIntransientState;
}
