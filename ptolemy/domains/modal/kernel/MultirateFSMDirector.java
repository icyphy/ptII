/* A MultirateFSM director that extends FSMDirector by supporting production
 and consumption of multiple tokens on a port in a firing.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.domains.modal.kernel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
import ptolemy.actor.util.Time;
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
 state has exactly one refinement, unless the state has an immediate
 transition whose guard evaluates to true in the iteration in which the
 state is entered. In addition, preemptive transitions are not allowed.
 Hence, each time when a modal model is fired,
 the current state always has a state refinement that is fired and
 will consume and produce outputs.
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
 If the rates are different, then it invalidate the schedule of the executive
 director of the modal model and updates the port rates of the modal model to be
 the port rates of the destination state refinement.

 @author Ye Zhou and Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire the modal model.
     *  If the refinement of the current state of the mode controller is
     *  ready to fire, then fire the current refinement.
     *  @exception IllegalActionException If there is no controller or
     *   the current state has no or more than one refinement, or if
     *   the current state has any preemptive transitions.
     */
    @Override
    public void fire() throws IllegalActionException {
        FSMActor controller = getController();
        State currentState = controller.currentState();
        Actor[] actors = currentState.getRefinement();
        if (actors == null || actors.length != 1) {
            throw new IllegalActionException(currentState,
                    "Current state is required to have exactly one refinement: "
                            + currentState.getName());
        }
        if (currentState.preemptiveTransitionList().size() > 0) {
            throw new IllegalActionException(currentState,
                    "Preemptive transitions are not allowed by MultirateFSMDirector: "
                            + currentState.getName());
        }
        super.fire();
    }

    /** Override the base class to ignore the fireAt() call if the specified
     *  actor is the controller and the time is the current time.
     *  The controller calls fireAt()
     *  if the destination state is enabled, but this director already handles
     *  transient states.
     *  @param actor The actor scheduled to be fired.
     *  @return If the argument is the controller, then return Time.NEGATIVE_INFINITY,
     *   to indicate that the request is being ignored. Otherwise, return what the
     *   superclass returns.
     *  @exception IllegalActionException If thrown by the executive director.
     */
    @Override
    public Time fireAt(Actor actor, Time time) throws IllegalActionException {
        FSMActor controller = getController();
        Time currentTime = getModelTime();
        if (actor != controller || !currentTime.equals(time)) {
            return super.fireAt(actor, time);
        }
        return Time.NEGATIVE_INFINITY;
    }

    /** Override the base class to ignore the fireAt() call if the specified
     *  actor is the controller. The controller calls fireAtCurrentTime()
     *  if the destination state is enabled, but this director already handles
     *  transient states.
     *  @param actor The actor scheduled to be fired.
     *  @return If the argument is the controller, then return Time.NEGATIVE_INFINITY,
     *   to indicate that the request is being ignored. Otherwise, return what the
     *   superclass returns.
     *  @exception IllegalActionException If thrown by the executive director.
     */
    @Override
    public Time fireAtCurrentTime(Actor actor) throws IllegalActionException {
        FSMActor controller = getController();
        if (actor != controller) {
            return super.fireAtCurrentTime(actor);
        }
        return Time.NEGATIVE_INFINITY;
    }

    /** Initialize the mode controller and all the refinements. Notify updates
     *  of port rates to the upper level director, and invalidate the upper
     *  level schedule.
     *  @exception IllegalActionException If the refinement has no or more
     *   than one refinement, or the initialize() method of one of the
     *   associated actors throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        try {
            _inInitialize = true;
            // Initialize all the refinements in the sub-layer
            // FSMDirectors and recompute the schedule.
            // Note that this will set the state to the
            // initial state, or to states reached from that
            // by immediate transitions.
            super.initialize();

            // Set the production and consumption rates of the ports
            // according to the current refinement.
            // This has to be after initialize because the initial
            // immediate transitions may affect the rates.
            _setProductionConsumptionRates();

            invalidateSchedule();
        } finally {
            _inInitialize = false;
        }
    }

    /** Return a new receiver of a type compatible with this director.
     *  This returns an instance of SDFReceiver.
     *  @return A new SDFReceiver.
     */
    @Override
    public Receiver newReceiver() {
        return new SDFReceiver();
    }

    /** Postfire the modal model and commit the transition.
     *  @return True if the postfire() method of current state refinement
     *   and that of the controller are both true.
     *  @exception IllegalActionException If a refinement throws it, or
     *   if there is no controller.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        return _doPostfire();
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
    @Override
    public void preinitialize() throws IllegalActionException {
        // The following is just a check to make sure the top-level
        // director is not a MultirateFSMDirector. It will throw
        // an exception if it is.
        _getEnclosingDomainActor();

        super.preinitialize();

        // Note that the following is done again initialize().
        // But it seems to be necessary to do it here the first time
        // to cause the schedule to be computed. I.e, we might get
        // the scheduler throwing an exception before we even get
        // to initialize because the current state refinement is
        // not compatible (e.g., in SDF, balance equations don't solve).
        _setProductionConsumptionRates();
    }

    /** Return a boolean to indicate whether a ModalModel under control
     *  of this director supports multirate firing.
     *  @return True indicating a ModalModel under control of this director
     *   does support multirate firing.
     */
    @Override
    public boolean supportMultirateFiring() {
        return true;
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
    @Override
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
                if (insideReceivers != null && insideReceivers[i] != null) {
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
    @Override
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
            if (insideReceivers != null && insideReceivers[i] != null) {
                for (int k = 0; k < rate; k++) {
                    // Only transfer number of tokens declared by the port
                    // rate. Throw exception if there are not enough tokens.
                    try {
                        Token token = port.getInside(i);
                        port.send(i, token);
                    } catch (NoTokenException ex) {
                        // Do not throw an exception if we are in initialize
                        // because in that case, these are initial tokens.
                        if (!_inInitialize) {
                            throw new InternalErrorException(
                                    "Director.transferOutputs: "
                                            + "Not enough tokens for port "
                                            + port.getName() + " " + ex);
                        }
                    }
                }
            }

            transferred = true;
        }

        return transferred;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

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
        Variable variable = DFUtilities.getRateVariable(port, name);
        DependencyDeclaration declaration = (DependencyDeclaration) variable
                .getAttribute("_MultirateFSMRateDependencyDeclaration",
                        DependencyDeclaration.class);

        if (declaration == null) {
            try {
                declaration = new DependencyDeclaration(variable,
                        "_MultirateFSMRateDependencyDeclaration");
            } catch (NameDuplicationException ex) {
                throw new InternalErrorException(variable, ex,
                        "Failed to create DependencyDeclaration "
                                + "_MultirateFSMRateDependencyDeclaration");
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
            ConstVariableModelAnalysis analysis, IOPort port,
            String parameterName) throws IllegalActionException {
        List refinementRateVariables = _getRefinementRateVariables(port,
                parameterName);
        _declareDependency(analysis, port, parameterName,
                refinementRateVariables);

        boolean isConstantAndIdentical = true;
        Token value = null;

        Iterator variables = refinementRateVariables.iterator();

        while (variables.hasNext() && isConstantAndIdentical) {
            Variable rateVariable = (Variable) variables.next();
            isConstantAndIdentical = isConstantAndIdentical
                    && analysis.getChangeContext(rateVariable) == null;

            if (isConstantAndIdentical) {
                Token newValue = analysis.getConstantValue(rateVariable);

                if (value == null) {
                    value = newValue;
                } else {
                    isConstantAndIdentical = isConstantAndIdentical
                            && newValue.equals(value);
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

    /** Postfire the modal model and commit the transition.
     *  If a transition is taken, the update the token production and consumption
     *  rates according to the new refinement.
     *  @return True if the super class postfire() method returns true.
     *  @exception IllegalActionException If there is no controller, or if the
     *   destination state has no or more than one refinement, or if the state
     *   refinement throws it.
     */
    protected boolean _doPostfire() throws IllegalActionException {
        // Note: This is a protected method so that HDFFSMDirector can defer
        // the actual work of postfire to occur between iterations.

        // Commit the transition. This will postfire the refinement
        // director.
        boolean superPostfire = super.postfire();

        if (getController().wasTransitionTaken()) {
            _setProductionConsumptionRates();
        }
        return superPostfire && !_finishRequested;
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
        if (isEmbedded()) {
            // Keep moving up towards the toplevel of the hierarchy until
            // we find an executive director that is not an instance of
            // FSMDirector or until we reach the toplevel composite actor.
            CompositeActor container = (CompositeActor) getContainer();
            Director director = container.getExecutiveDirector();

            while (director != null) {
                if (director instanceof FSMDirector) {
                    if (!director.isEmbedded()) {
                        break;
                    }
                    // Move up another level in the hierarchy.
                    container = (CompositeActor) container.getContainer();
                    director = container.getExecutiveDirector();
                } else {
                    return container;
                }
            }
        }

        throw new IllegalActionException(this,
                "MultirateFSMDirector director must be contained within another domain.");
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

        while (insidePorts.hasNext()) {
            IOPort insidePort = (IOPort) insidePorts.next();
            Variable variable = DFUtilities.getRateVariable(insidePort,
                    parameterName);

            if (variable != null) {
                list.add(variable);
            }
        }

        return list;
    }

    /** Set the production and consumption rates based on the
     *  refinement of the current state, after descending through a hierarchy of
     *  state machines.
     *  @exception IllegalActionException If the initial state does not
     *   have exactly one refinement.
     */
    protected void _setProductionConsumptionRates()
            throws IllegalActionException {
        FSMActor controller = getController();
        State currentState = controller.currentState();

        // Check that the state has a refinement.
        TypedActor[] currentRefinements = currentState.getRefinement();
        if (currentRefinements == null || currentRefinements.length != 1) {
            throw new IllegalActionException(this,
                    "Destination state (after any immediate transitions)"
                            + " is required to have exactly one refinement: "
                            + currentState.getName());
        }

        TypedCompositeActor currentRefinement = (TypedCompositeActor) currentRefinements[0];
        Director refinementDir = currentRefinement.getDirector();
        // If the refinement is a nested state machine, recursively descend until we
        // find a refinement that is not.
        while (refinementDir instanceof FSMDirector && refinementDir != this) {
            controller = ((FSMDirector) refinementDir).getController();
            currentState = controller.currentState();
            currentRefinements = currentState.getRefinement();
            if (currentRefinements == null || currentRefinements.length != 1) {
                throw new IllegalActionException(
                        this,
                        "Initial state (after any immediate transitions)"
                                + " is required to have exactly one refinement: "
                                + currentState.getName());
            }
            // Set the refinement and its director.
            currentRefinement = (TypedCompositeActor) currentRefinements[0];
            refinementDir = currentRefinement.getDirector();
        }

        if (refinementDir instanceof StaticSchedulingDirector
                && refinementDir != this) {
            // Force the schedule to be computed, if necessary.
            // Update the refinement's production and consumption rates.
            refinementDir.invalidateSchedule();
            ((StaticSchedulingDirector) refinementDir).getScheduler()
            .getSchedule();
        }

        // Record consumption and production rates in the ports of this actor.
        boolean inputRateChanged = _updateInputTokenConsumptionRates(currentRefinement);
        boolean outputRateChanged = _updateOutputTokenProductionRates(currentRefinement);
        // Tell the upper level scheduler that the current schedule
        // is no longer valid.
        // FIXME: Apparently, this can't work because that
        // director is in the middle of an iteration if a reset
        // transition is being taken. Hopefully, the reconfiguration
        // constraints set below will catch this.
        if (inputRateChanged || outputRateChanged) {
            CompositeActor actor = _getEnclosingDomainActor();
            Director director = actor.getExecutiveDirector();
            director.invalidateSchedule();
        }

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

                // FIXME: Name match is an expensive check. Depth in hierarchy?
                if (thisPortContainer.getFullName().equals(
                        refineInPortContainer.getFullName())) {
                    // set the outside port rate equal to the port rate
                    // of the refinement.
                    int previousPortRate = DFUtilities
                            .getTokenConsumptionRate(inputPortOutside);
                    int portRateToSet = DFUtilities
                            .getTokenConsumptionRate(refineInPort);

                    if (previousPortRate != portRateToSet) {
                        inputRateChanged = true;
                    }
                    // Do the following even if rates haven't changed to ensure that
                    // the variables exist.

                    DFUtilities.setTokenConsumptionRate(inputPortOutside,
                            portRateToSet);

                    // Also set the rate of the controller.
                    FSMActor controller = getController();
                    IOPort controllerPort = (IOPort) controller
                            .getPort(inputPortOutside.getName());
                    if (controllerPort != null) {
                        DFUtilities.setTokenConsumptionRate(controllerPort,
                                portRateToSet);
                    }
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

                if (thisPortContainer.getFullName().equals(
                        refineOutPortContainer.getFullName())) {
                    // set the outside port rate equal to the port rate
                    // of the refinement.
                    int previousPortRate = DFUtilities
                            .getTokenProductionRate(outputPortOutside);
                    int portRateToSet = DFUtilities
                            .getTokenProductionRate(refineOutPort);
                    int portInitRateToSet = DFUtilities
                            .getTokenInitProduction(refineOutPort);

                    if (previousPortRate != portRateToSet) {
                        outputRateChanged = true;
                    }
                    // Do the following even if rates haven't changed to ensure that
                    // the variables exist.
                    DFUtilities.setTokenProductionRate(outputPortOutside,
                            portRateToSet);
                    DFUtilities.setTokenInitProduction(outputPortOutside,
                            portInitRateToSet);

                    // Also set the rate of the controller.
                    // Note that we set the _consumption_ rate, because this an input port
                    // (as well as an output port) for the controller.
                    FSMActor controller = getController();
                    IOPort controllerPort = (IOPort) controller
                            .getPort(outputPortOutside.getName());
                    if (controllerPort != null) {
                        DFUtilities.setTokenConsumptionRate(controllerPort,
                                portRateToSet);
                    }
                }
            }
        }

        return outputRateChanged;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Indicator that we are in initialize. */
    private boolean _inInitialize;
}
