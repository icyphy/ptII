/* A Scheduler for the SDF domain

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.domains.sdf.kernel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.Receiver;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.ConstVariableModelAnalysis;
import ptolemy.actor.util.DFUtilities;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.Fraction;

///////////////////////////////////////////////////////////////////
//// SDFScheduler

/**

 A scheduler that implements basic scheduling of SDF graphs.  This
 class calculates the SDF schedule in two phases.  First, the balance
 equations for the rates between actors are solved to determine the
 <i>firing vector</i> (also known as the repetitions vector).  The
 firing vector is the least integer solution such that the number of
 tokens created on each channel of each relation is equal to the number
 of tokens consumed.  In some cases, no solution exists.  Such graphs
 are not executable under SDF.
 <p>
 Then the actors are ordered such that each actor only fires when the
 scheduler has determined that enough tokens will be present on its
 input ports to allow it to fire.  In cases where the dataflow graph is
 cyclic, a valid firing vector exists, but no actor can fire, since
 they all depend on the output of another actor.  This situation is
 known as <i>deadlock</i>.  Deadlock must be prevented in SDF by manually
 inserting delay actors, which represent initial tokens on each
 relation.  Such delay actors are responsible for creating tokens
 during initialization that will prevent deadlock.  These actors
 set the <i>tokenInitProduction</i> parameter of their output ports
 to represent the number of
 tokens they will create during initialization.  The SDFScheduler uses
 this parameter to break the dependency in a cyclic
 graph.
 <p>
 In addition, an input port may initially have available input tokens.
 This is indicated by a <i>tokenInitConsumption</i> parameter on the
 input port.
 <p>
 Note that this scheduler only ensures that the number of firings is
 minimal.  Most notably, it does not attempt to minimize the size of
 the buffers that are associated with each relation.  The resulting
 schedule is a linear schedule (as opposed to a looped schedule) and is
 not suitable for multiprocessing environments.
 <p>
 Any actors may be
 scheduled by this scheduler, which will, by default, assume
 homogeneous behavior for each actor.  (i.e. each output port produces
 one token for each firing, and each input port consumes one token on
 each firing, and no tokens are created during initialization.)  If
 this is not the case then parameters named <i>tokenConsumptionRate</i>,
 <i>tokenProductionRate</i>, <i>tokenInitProduction</i>, and
 <i>tokenInitConsumption</i> must be set.
 The SDFIOPort class provides easier access to these parameters.
 <p>
 Note that reconstructing the schedule is expensive, so the schedule is
 locally cached for as long as possible, and mutations under SDF should
 be avoided.
 <p>
 Note that this scheduler supports actors with 0-rate ports as long as
 the graph is not equivalent to a disconnected graph. This scheduler
 is somewhat conservative in this respect.
 <p>Disconnected graphs are supported if the SDF Director parameter
 <i>allowDisconnectedGraphs</i> is true.

 @see ptolemy.actor.sched.Scheduler
 @see ptolemy.domains.sdf.lib.SampleDelay

 @author Stephen Neuendorffer and Brian Vogel
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Green (neuendor)
 */
public class SDFScheduler extends BaseSDFScheduler implements ValueListener {
    /** Construct a scheduler with no container(director)
     *  in the default workspace, the name of the scheduler is
     *  "Scheduler".
     */
    public SDFScheduler() {
        super();
        _init();
    }

    /** Construct a scheduler in the given workspace with the name
     *  "Scheduler".
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking.
     */
    public SDFScheduler(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Construct a scheduler in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public SDFScheduler(Director container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** If true, then buffer sizes are fixed according to the schedule,
     *  and attempts to write to the buffer that cause the buffer to
     *  exceed the schedule size result in an exception. This method
     *  works by setting the capacity of the receivers if the value is
     *  true. This parameter is a boolean that defaults to true.
     */
    public Parameter constrainBufferSizes;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the scheduler into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new scheduler with no container, and no valid schedule.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new Scheduler.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SDFScheduler newObject = (SDFScheduler) super.clone(workspace);
        newObject._firingVector = new HashMap();
        newObject._externalRates = new HashMap();
        newObject._rateVariables = new LinkedList();
        return newObject;
    }

    /** Declare the rate dependency on any external ports of the model.
     *  SDF directors should invoke this method once during preinitialize.
     */
    @Override
    public void declareRateDependency() throws IllegalActionException {
        ConstVariableModelAnalysis analysis = ConstVariableModelAnalysis
                .getAnalysis(this);
        SDFDirector director = (SDFDirector) getContainer();
        CompositeActor model = (CompositeActor) director.getContainer();

        for (Iterator ports = model.portList().iterator(); ports.hasNext();) {
            IOPort port = (IOPort) ports.next();

            if (!(port instanceof ParameterPort)) {
                if (port.isInput()) {
                    _declareDependency(analysis, port, "tokenConsumptionRate",
                            _rateVariables);
                    _declareDependency(analysis, port, "tokenInitConsumption",
                            _rateVariables);
                }

                if (port.isOutput()) {
                    _declareDependency(analysis, port, "tokenProductionRate",
                            _rateVariables);
                    _declareDependency(analysis, port, "tokenInitProduction",
                            _rateVariables);
                }
            }
        }
    }

    /** Get the external port rates.
     *  @return a Map from external ports to the number of tokens that
     *  that port will produce or consume in each firing.
     */
    public Map getExternalRates() {
        return _externalRates;
    }

    /** Create the schedule.  Return the number of times that the given
     *  entity will fire in a single iteration of the system.
     *  @param entity The entity that is being fired.
     *  @return The number of times that the given entity will fire
     *  @exception IllegalActionException If thrown by getSchedule().

     */
    public int getFiringCount(Entity entity) throws IllegalActionException {
        getSchedule();
        return _getFiringCount(entity);
    }

    /** React to the fact that the specified Settable has changed by
     *  invalidating the schedule.
     *  @param settable The object that has changed value.
     */
    @Override
    public void valueChanged(Settable settable) {
        setValid(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Populate the given set with the dynamic rate variables in the model.
     *  @param model The model.
     *  @param rateVariables A list of rate variables.  Each element
     *  is a Variable.
     *  @exception IllegalActionException If throw while looking for dynamic
     *  rate parameters.
     */
    protected void _checkDynamicRateVariables(CompositeActor model,
            List rateVariables) throws IllegalActionException {
        // Check for rate parameters which are dynamic.
        ConstVariableModelAnalysis analysis = ConstVariableModelAnalysis
                .getAnalysis(getContainer());

        // Save the rate variables that we already listen to.
        LinkedList oldList = new LinkedList();
        oldList.addAll(rateVariables);

        LinkedList newList = new LinkedList();
        for (Iterator entities = model.deepEntityList().iterator(); entities
                .hasNext();) {
            Entity entity = (Entity) entities.next();

            for (Iterator ports = entity.portList().iterator(); ports.hasNext();) {
                Port port = (Port) ports.next();
                Set set = analysis.getNotConstVariables(port);
                Variable variable;
                variable = DFUtilities.getRateVariable(port,
                        "tokenInitProduction");
                _listenToRateVariable(variable, rateVariables);
                newList.add(variable);

                if (set.contains(variable)) {
                    _assertDynamicRateVariable(model, variable, rateVariables,
                            analysis);
                }

                variable = DFUtilities.getRateVariable(port,
                        "tokenInitConsumption");
                _listenToRateVariable(variable, rateVariables);
                newList.add(variable);

                if (set.contains(variable)) {
                    _assertDynamicRateVariable(model, variable, rateVariables,
                            analysis);
                }

                variable = DFUtilities.getRateVariable(port,
                        "tokenConsumptionRate");
                _listenToRateVariable(variable, rateVariables);
                newList.add(variable);

                if (set.contains(variable)) {
                    _assertDynamicRateVariable(model, variable, rateVariables,
                            analysis);
                }

                variable = DFUtilities.getRateVariable(port,
                        "tokenProductionRate");
                _listenToRateVariable(variable, rateVariables);
                newList.add(variable);

                if (set.contains(variable)) {
                    _assertDynamicRateVariable(model, variable, rateVariables,
                            analysis);
                }
            }
        }

        // Don't listen to old rate variables anymore.
        oldList.removeAll(newList);
        for (Iterator oldRateVariables = oldList.iterator(); oldRateVariables
                .hasNext();) {
            Variable variable = (Variable) oldRateVariables.next();
            if (_debugging) {
                _debug("No longer listening to rate variable " + variable);
            }
            variable.removeValueListener(this);
            rateVariables.remove(variable);
        }
    }

    /** Determine the number of times the given actor can fire, based on
     *  the number of tokens that are present on its inputs.
     *  @param currentActor The actor.
     *  @return The number of times the actor can fire.
     *  @exception IllegalActionException If the rate parameters are invalid.
     */
    protected int _computeMaximumFirings(Actor currentActor)
            throws IllegalActionException {
        int result = Integer.MAX_VALUE;

        // Update the number of tokens waiting on the actor's input ports.
        Iterator inputPorts = currentActor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            int tokenRate = DFUtilities.getTokenConsumptionRate(inputPort);

            // Ignore zero rate ports.. they don't limit the number of times
            // we can fire their actors.
            if (tokenRate == 0) {
                continue;
            }

            Receiver[][] receivers = inputPort.getReceivers();

            for (int channel = 0; channel < receivers.length; channel++) {
                if (receivers[channel] == null) {
                    continue;
                }

                for (int copy = 0; copy < receivers[channel].length; copy++) {
                    if (!(receivers[channel][copy] instanceof SDFReceiver)) {
                        // This should only occur if it is null.
                        continue;
                    }

                    SDFReceiver receiver = (SDFReceiver) receivers[channel][copy];

                    int firings = receiver._waitingTokens / tokenRate;

                    // Keep track of whether or not this actor can fire again immediately.
                    if (firings < result) {
                        result = firings;
                    }
                }
            }
        }

        return result;
    }

    /** Count the number of input ports in the given actor that must be
     *  fulfilled before the actor can fire.  Ports that are connected
     *  to actors that we are not scheduling right now are assumed to
     *  be fulfilled.  Ports that have more tokens waiting on each of
     *  their channels than their input consumption rate are also
     *  already fulfilled.  All other ports are considered to be
     *  unfulfilled.
     *  @param actor The actor.
     *  @param actorList The list of actors that we are scheduling.
     *  @param resetCapacity If true, then reset the capacity of each
     *   receiver to infinite capacity (do this during initialization).
     *  @return The number of unfulfilled input ports of the given actor.
     *  @exception IllegalActionException If any called method throws it.
     */
    @SuppressWarnings("unused")
    protected int _countUnfulfilledInputs(Actor actor, List actorList,
            boolean resetCapacity) throws IllegalActionException {
        if (_debugging && VERBOSE) {
            _debug("Counting unfulfilled inputs for " + actor.getFullName());
        }

        int count = 0;

        Iterator inputPorts = actor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();

            if (_debugging && VERBOSE) {
                _debug("Checking input " + inputPort.getFullName());
            }

            int threshold = DFUtilities.getTokenConsumptionRate(inputPort);

            if (_debugging && VERBOSE) {
                _debug("Threshold = " + threshold);
            }

            Receiver[][] receivers = inputPort.getReceivers();
            boolean isFulfilled = true;

            for (int channel = 0; channel < receivers.length; channel++) {
                if (receivers[channel] == null) {
                    continue;
                }

                for (int copy = 0; copy < receivers[channel].length; copy++) {
                    if (!(receivers[channel][copy] instanceof SDFReceiver)) {
                        // This should only occur if it is null.
                        continue;
                    }

                    SDFReceiver receiver = (SDFReceiver) receivers[channel][copy];

                    if (resetCapacity) {
                        receiver.setCapacity(SDFReceiver.INFINITE_CAPACITY);
                    }

                    if (receiver._waitingTokens < threshold) {
                        isFulfilled = false;

                        if (!resetCapacity) {
                            break;
                        }
                    }
                }

                if (!isFulfilled) {
                    // No point in continuing.
                    break;
                }
            }

            if (!isFulfilled) {
                count++;
            }
        }

        return count;
    }

    /** Return the number of firings associated with the given entity. The
     *  number of firings is stored in the _firingVector map, indexed
     *  by the entity.
     *  @param entity One of the actors we are scheduling.
     *  @return The number of firings.
     */
    protected int _getFiringCount(Entity entity) {
        return ((Integer) _firingVector.get(entity)).intValue();
    }

    /** Return the scheduling sequence.  An exception will be thrown if the
     *  graph is not schedulable.  This occurs in the following circumstances:
     *  <ul>
     *  <li>The graph is not a connected graph.
     *  <li>No integer solution exists for the balance equations.
     *  <li>The graph contains cycles without delays (deadlock).
     *  <li>Multiple output ports are connected to the same broadcast
     *  relation. (equivalent to a non-deterministic merge)
     *  <li>The vectorizationFactor parameter of the director does
     *  not contain a positive integer.
     *  </ul>
     *
     *  @return A schedule of the deeply contained opaque entities
     *  in the firing order.
     *  @exception NotSchedulableException If the rates specified for
     *  the model imply that the model is not statically schedulable.
     *  @exception IllegalActionException If the rate parameters
     *  of the model are not correct, or the computed rates for
     *  external ports are not correct.
     */
    @Override
    @SuppressWarnings("unused")
    protected Schedule _getSchedule() throws NotSchedulableException,
    IllegalActionException {
        SDFDirector director = (SDFDirector) getContainer();
        CompositeActor model = (CompositeActor) director.getContainer();

        _checkDynamicRateVariables(model, _rateVariables);

        int vectorizationFactor = 1;

        Token token = director.vectorizationFactor.getToken();
        vectorizationFactor = ((IntToken) token).intValue();

        if (vectorizationFactor < 1) {
            throw new NotSchedulableException(this,
                    "The supplied vectorizationFactor must be "
                            + "a positive integer. The given value was: "
                            + vectorizationFactor);
        }

        CompositeActor container = (CompositeActor) director.getContainer();

        // A linked list containing all the actors.
        List allActorList = container.deepEntityList();

        // externalRates maps from external
        // ports to the number of tokens that that port
        // will produce or consume in each firing.
        // It gets populated with the fractional production ratios
        // and is used in the end to set final rates on external ports.
        // This map is initialized to zero.
        // NOTE: This used to be a TreeMap using DFUtilities.NamedObjComparator().
        // However, that comparator is very slow.
        // FIXME: Why not get this via the container of the receivers?
        // or better yet, cache it in the receivers?
        Map externalRates = new HashMap();

        // Initialize externalRates to zero.
        for (Iterator ports = container.portList().iterator(); ports.hasNext();) {
            IOPort port = (IOPort) ports.next();
            externalRates.put(port, Fraction.ZERO);
        }

        // First solve the balance equations
        Map entityToFiringsPerIteration = _solveBalanceEquations(container,
                allActorList, externalRates);

        if (_debugging && VERBOSE) {
            _debug("Firing Ratios: " + entityToFiringsPerIteration.toString());
        }

        // Multiply the number of firings for each actor by the
        // vectorizationFactor.
        _vectorizeFirings(vectorizationFactor, entityToFiringsPerIteration,
                externalRates);

        // Set the firing vector.
        _firingVector = entityToFiringsPerIteration;

        if (_debugging) {
            _debug("Normalized Firing Counts:");
            _debug(entityToFiringsPerIteration.toString());
        }

        // Schedule all the actors using the calculated firings.
        Schedule result = _scheduleConnectedActors(externalRates, allActorList,
                container);

        if (_debugging && VERBOSE) {
            _debug("Firing Vector:");
            _debug(entityToFiringsPerIteration.toString());
        }

        // Set parameters on each actor that contain the number
        // of firings in an iteration.
        _saveFiringCounts(entityToFiringsPerIteration);

        // Set the rate parameters of any external ports.
        _saveContainerRates(externalRates);

        // Set the schedule to be valid.
        setValid(true);
        _externalRates = externalRates;
        return result;
    }

    /** Solve the balance equations for the list of connected Actors.
     *  For each actor, determine the ratio that determines the rate at
     *  which it should fire relative to the other actors for the graph to
     *  be live and operate within bounded memory. Normalize this ratio
     *  into integer, which is the minimum number of firings of the actor
     *  to satisfy the balance equations.
     *
     *  @param container The container that is being scheduled.
     *  @param actorList The actors that we are interested in.
     *  @param externalRates A map from external ports of container to
     *  the fractional rates of that port.  This starts out initialized with
     *  Fraction.ZERO and will be populated during this method.
     *  @return A map from each actor to its fractional
     *  firing.
     *  @exception NotSchedulableException If the graph is not consistent
     *  under the synchronous dataflow model, or if the graph is not connected.
     *  @exception IllegalActionException If any called method throws it.
     */
    protected Map _solveBalanceEquations(CompositeActor container,
            List actorList, Map externalRates) throws NotSchedulableException,
            IllegalActionException {
        // The map that we will return.
        // This will be populated with the fraction firing ratios for
        // each actor.
        // NOTE: This used to be a TreeMap using DFUtilities.NamedObjComparator().
        // However, that comparator is very slow.
        // Map entityToFiringsPerIteration = new TreeMap(
        //        new DFUtilities.NamedObjComparator());
        Map entityToFiringsPerIteration = new HashMap();

        if (actorList.size() == 0) {

            _checkDirectInputOutputConnection(container, externalRates);
            // If we've been given
            // no actors to do anything with, return an empty Map.
            return entityToFiringsPerIteration;
        }

        // The pool of actors that have their firingsPerIteration set,
        // but have not had their ports explored yet.
        LinkedList pendingActors = new LinkedList();

        // Set of actors that belong to the same cluster.
        Set clusteredActors = new HashSet();

        // Set of external ports that are conneted to
        // actors of the same cluster.
        Set clusteredExternalPorts = new HashSet();

        // The pool of Actors that have not been touched
        // yet. (i.e. all their firingsPerIteration are still set to
        // Fraction equal to -1/1)
        LinkedList remainingActors = new LinkedList();

        // Initialize remainingActors to contain all the actors we were given.
        remainingActors.addAll(actorList);

        // Initialize entityToFiringsPerIteration for each actor to -1.
        for (Iterator actors = remainingActors.iterator(); actors.hasNext();) {
            ComponentEntity entity = (ComponentEntity) actors.next();
            entityToFiringsPerIteration.put(entity, _minusOne);
        }

        StaticSchedulingDirector director = (StaticSchedulingDirector) getContainer();

        boolean allowDisconnectedGraphs = false;

        if (director instanceof SDFDirector) {
            allowDisconnectedGraphs = ((SDFDirector) director)._allowDisconnectedGraphs;
        }

        // Ned Stoffel's change to support disconnected graphs:
        // Finally, the schedule can jump from one island to
        // another among the disconnected graphs. There is nothing
        // to force the scheduler to finish executing all actors
        // on one island before firing actors on another
        // island. However, the order of execution within an
        // island should be correct.
        while (!remainingActors.isEmpty()) {
            clusteredActors.clear();
            clusteredExternalPorts.clear();

            ComponentEntity actor = _pickZeroRatePortActor(remainingActors);

            if (actor == null) {
                actor = (ComponentEntity) remainingActors.removeFirst();
            } else {
                remainingActors.remove(actor);
            }

            clusteredActors.add(actor);

            entityToFiringsPerIteration.put(actor, new Fraction(1));
            pendingActors.addLast(actor);

            while (!pendingActors.isEmpty()) {
                Actor currentActor = (Actor) pendingActors.removeFirst();
                Iterator actorPorts = ((ComponentEntity) currentActor)
                        .portList().iterator();

                while (actorPorts.hasNext()) {
                    IOPort currentPort = (IOPort) actorPorts.next();
                    _propagatePort(container, currentPort,
                            entityToFiringsPerIteration, externalRates,
                            remainingActors, pendingActors, clusteredActors,
                            clusteredExternalPorts);
                }
            }

            // Now we have _clusteredActors, which contains actors in
            // one cluster (they are connected). Find the LCM of their
            // denominator and normalize their firings. This means firings
            // of actors are only normalized within their cluster.
            int lcm = 1;

            for (Iterator actors = clusteredActors.iterator(); actors.hasNext();) {
                Actor currentActor = (Actor) actors.next();
                Fraction fraction = (Fraction) entityToFiringsPerIteration
                        .get(currentActor);
                int denominator = fraction.getDenominator();
                lcm = Fraction.lcm(lcm, denominator);
            }

            // Got the normalizing factor.
            Fraction lcmFraction = new Fraction(lcm);

            for (Iterator actors = clusteredActors.iterator(); actors.hasNext();) {
                Actor currentActor = (Actor) actors.next();
                Fraction repetitions = ((Fraction) entityToFiringsPerIteration
                        .get(currentActor)).multiply(lcmFraction);

                if (repetitions.getDenominator() != 1) {
                    throw new InternalErrorException(
                            "Failed to properly perform"
                                    + " fraction normalization.");
                }

                entityToFiringsPerIteration.put(currentActor, repetitions);
            }

            for (Iterator externalPorts = clusteredExternalPorts.iterator(); externalPorts
                    .hasNext();) {
                IOPort port = (IOPort) externalPorts.next();
                Fraction rate = ((Fraction) externalRates.get(port))
                        .multiply(lcmFraction);

                if (rate.getDenominator() != 1) {
                    throw new InternalErrorException(
                            "Failed to properly perform"
                                    + " fraction normalization.");
                }

                externalRates.put(port, rate);
            }

            clusteredActors.clear();
            clusteredExternalPorts.clear();

            if (!allowDisconnectedGraphs) {
                break;
            }
        }

        _checkDirectInputOutputConnection(container, externalRates);

        if (!remainingActors.isEmpty()) {
            // If there are any actors left that we didn't get to, then
            // this is not a connected graph, and we throw an exception.
            StringBuffer messageBuffer = new StringBuffer(
                    "SDF scheduler found disconnected actors! "
                            + "Usually, disconnected actors in an SDF model "
                            + "indicates an error.  If this is not an error, try "
                            + "setting the SDFDirector parameter "
                            + "allowDisconnectedGraphs to true.");

            // Look through all the unreached actors.  If any of them are
            // in transparent composite actors that contain PortParameters,
            // print a message.
            // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4086

            // We only print messages about the first 99 PortParameters.
            int count = 0;
            StringBuffer portParameterMessageBuffer = new StringBuffer();
            Set portParametersFound = new HashSet();
            Set containersSeen = new HashSet();
            for (Iterator actors = actorList.iterator(); actors.hasNext()
                    && count < 100; count++) {
                NamedObj actor = (NamedObj) actors.next();
                NamedObj actorContainer = actor.getContainer();
                if (actorContainer instanceof CompositeActor
                        && !((CompositeActor) actorContainer).isOpaque()
                        && !containersSeen.contains(actorContainer)) {
                    containersSeen.add(actorContainer);
                    List portParameters = actorContainer
                            .attributeList(PortParameter.class);
                    for (Object portParameter : portParameters) {
                        if (!portParametersFound.contains(portParameter)) {
                            portParametersFound.add(portParameter);
                            portParameterMessageBuffer
                            .append(((PortParameter) portParameter)
                                    .getFullName() + " ");
                            if (count > 100) {
                                break;
                            }
                        }
                    }
                }
            }
            if (portParameterMessageBuffer.length() > 0) {
                messageBuffer
                .append("Note that some of the unreached actors are in "
                        + "transparent composite actors that have PortParameters.  "
                        + "A transparent composite actor is composite actor that has "
                        + "no local director.  Transparent composite actors and "
                        + "PortParameters are not compatible, the workaround is to "
                        + "insert a director or remove the PortParameter.  "
                        + "\nThe PortParameters:\n"
                        + portParameterMessageBuffer.toString());
                if (count >= 99) {
                    messageBuffer.append("...");
                }
            }

            messageBuffer.append("\nUnreached Actors:\n");
            count = 0;
            for (Iterator unreachedActors = remainingActors.iterator(); unreachedActors
                    .hasNext() && count < 100; count++) {
                NamedObj unreachedActor = (NamedObj) unreachedActors.next();
                messageBuffer.append(unreachedActor.getFullName() + " ");
            }

            if (count >= 99) {
                messageBuffer.append("...");
            }
            messageBuffer.append("\nReached Actors:\n");

            List reachedActorList = new LinkedList();
            reachedActorList.addAll(actorList);
            reachedActorList.removeAll(remainingActors);

            count = 0;
            for (Iterator actors = reachedActorList.iterator(); actors
                    .hasNext() && count < 100; count++) {
                Entity entity = (Entity) actors.next();
                messageBuffer.append(entity.getFullName() + " ");
            }

            if (count >= 99) {
                messageBuffer.append("...");
            }
            throw new NotSchedulableException(this, messageBuffer.toString());
        }

        return entityToFiringsPerIteration;
    }

    /** Multiply all of the repetition rates
     *  by the given vectorizationFactor.  This factor
     *  is normally the integer value of the vectorizationFactor
     *  parameter of the director.  Also multiply the production and
     *  consumption rates of the external ports of the model by the
     *  same amount. Also, convert the two maps in the arguments to
     *  contain Integers rather than Fractions.
     *  @param vectorizationFactor An integer scaling factor to multiply
     *   the firing vector by.
     *  @param entityToFiringsPerIteration Map representing the firing vector.
     *  @param externalRates Map representing production rates of
     *  external ports.
     */
    @SuppressWarnings("unused")
    protected void _vectorizeFirings(int vectorizationFactor,
            Map entityToFiringsPerIteration, Map externalRates) {
        // Note: after we have called the _solveBalanceEquations(),
        // all the fractual firings and external rates have been
        // normalized to integers, but we still represent them
        // as fractions. After the _vectorizeFirings(), they
        // are saved as integers.
        if (_debugging && VERBOSE) {
            _debug("Multiplying firings by vectorizationFactor = "
                    + vectorizationFactor);
        }

        Fraction lcmFraction = new Fraction(vectorizationFactor);

        // Use entrySet here for performance reasons.
        for (Iterator actorMapEntries = entityToFiringsPerIteration.entrySet()
                .iterator(); actorMapEntries.hasNext();) {
            Map.Entry actors = (Map.Entry) actorMapEntries.next();
            Fraction repetitions = (Fraction) actors.getValue();
            repetitions = repetitions.multiply(lcmFraction);

            // FIXME: Doing the conversion to Integer here is bizarre,
            // since they are integers coming in.
            actors.setValue(Integer.valueOf(repetitions.getNumerator()));
        }

        // Go through the ports and normalize the external production
        // and consumption rates by the same factor.
        // Use entrySet here for performance reasons.
        for (Iterator portMapEntries = externalRates.entrySet().iterator(); portMapEntries
                .hasNext();) {
            Map.Entry ports = (Map.Entry) portMapEntries.next();
            Fraction rate = (Fraction) ports.getValue();
            rate = rate.multiply(lcmFraction);

            // FIXME: Doing the conversion to Integer here is bizarre,
            // since they are integers coming in.
            ports.setValue(Integer.valueOf(rate.getNumerator()));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void _assertDynamicRateVariable(CompositeActor model,
            Variable variable, List rateVariables,
            ConstVariableModelAnalysis analysis) throws IllegalActionException {
        boolean allowRateChanges = ((BooleanToken) ((SDFDirector) getContainer()).allowRateChanges
                .getToken()).booleanValue();

        if (!allowRateChanges) {
            throw new IllegalActionException(variable,
                    "The SDF rate parameter may change."
                            + " This is not allowed in SDF models "
                            + "that will be run through the code "
                            + "generator.  If you don't care about "
                            + "code generation, then you might "
                            + "consider setting the allowRateChanges "
                            + "parameter of the SDF director to false.");
        }

        /* FIXME: This is bogus. It rules out perfectly legitimate models.
        Entity changeContext = analysis.getChangeContext(variable);

        if (!((changeContext == model) || changeContext.deepContains(model))) {
            throw new IllegalActionException(variable,
                    "The SDF rate parameter changes during "
                            + "execution of the schedule!");
        }
         */
    }

    /** Update the The external rates of those directly connected input
     *  and output ports to be 1. So a direct connection will transfer
     *  one token in each execution of the schedule.
     *
     *  @param container The container that is being scheduled.
     *  @param externalRates A map from external ports of container to
     *  the fractional rates of that port.  The external rates of
     *  those directly connected input and output ports will be updated
     *  to be 1 during this method.
     */
    private void _checkDirectInputOutputConnection(CompositeActor container,
            Map externalRates) {

        Iterator inputPorts = container.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            Fraction rate = (Fraction) externalRates.get(inputPort);
            if (rate.equals(Fraction.ZERO)) {

                // Check to make sure that if this port is an external
                // input port, then it does not drive the same relation as some
                // other output port or some other external input port.
                // This results in a non-deterministic merge and is illegal.

                Iterator connectedPorts = inputPort.deepInsidePortList()
                        .iterator();

                // Make sure any connected output ports are connected on
                // the inside.
                while (connectedPorts.hasNext()) {
                    IOPort connectedPort = (IOPort) connectedPorts.next();

                    // connectPort might be connected on the inside to the
                    // currentPort, which is legal.  The container argument
                    // is always the container of the director, so any port
                    // that has that container must be connected on the inside.
                    if (connectedPort.isOutput()
                            && connectedPort.getContainer() != container) {
                        throw new NotSchedulableException(inputPort,
                                connectedPort,
                                "External input port drive the same relation "
                                        + "as an output port. "
                                        + "This is not legal in SDF.");
                    } else if (connectedPort.isInput()
                            && connectedPort.getContainer() == container) {
                        throw new NotSchedulableException(inputPort,
                                connectedPort,
                                "External input port drives the same relation "
                                        + "as another external input port. "
                                        + "This is not legal in SDF.");
                    }
                }

                boolean isDirectionConnection = true;
                List insideSinkPorts = inputPort.insideSinkPortList();

                // A dangling port has zero rate.
                if (insideSinkPorts.isEmpty()) {
                    isDirectionConnection = false;
                } else {
                    // If the zero external port rate is due to the rate
                    // propagation from a contained actor (i.e., connected to the
                    // zero rate port of the actor), then the zero external rate
                    // must be preserved.
                    Iterator sinkPorts = insideSinkPorts.iterator();
                    while (sinkPorts.hasNext()) {
                        IOPort sinkPort = (IOPort) sinkPorts.next();
                        if (sinkPort.getContainer() != container) {
                            isDirectionConnection = false;
                            break;
                        }
                    }
                }

                if (isDirectionConnection) {
                    externalRates.put(inputPort, new Fraction(1));
                    Iterator sinkPorts = insideSinkPorts.iterator();
                    while (sinkPorts.hasNext()) {
                        IOPort sinkPort = (IOPort) sinkPorts.next();
                        externalRates.put(sinkPort, new Fraction(1));
                    }
                }
            }
        }
    }

    /** Create the parameter constrainBufferSizes and set its default
     *  value and type constraints.
     */
    private void _init() {
        try {
            constrainBufferSizes = new Parameter(this, "constrainBufferSizes");
            constrainBufferSizes.setTypeEquals(BaseType.BOOLEAN);
            constrainBufferSizes.setExpression("true");
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
    }

    /** Add this scheduler as a value listener to the given variable
     * and add the variable to the given list.  If the list already
     * includes the variable, or the variable is null, then do
     * nothing.
     * @param variable A variable, which is a rate variable that this scheduler
     * uses for scheduling.
     * @param rateVariables A list of rate variables.
     */
    private void _listenToRateVariable(Variable variable, List rateVariables) {
        // The schedule depends on the rate parameter.
        if (variable != null && !rateVariables.contains(variable)) {
            if (_debugging) {
                _debug("Listening to rate variable " + variable);
            }

            variable.addValueListener(this);
            rateVariables.add(variable);
        }
    }

    /** Search the given list of actors for one that contains at least
     *  one port that has zero rate.
     *
     *  @param actorList The list of all of the actors to search.
     *  @return An actor that contains at least one zero rate port, or null
     *  if no actor has a zero rate port.
     */
    private ComponentEntity _pickZeroRatePortActor(List actorList)
            throws IllegalActionException {
        for (Iterator actors = actorList.iterator(); actors.hasNext();) {
            ComponentEntity actor = (ComponentEntity) actors.next();

            // Check if this actor has any ports with rate of zero.
            for (Iterator ports = actor.portList().iterator(); ports.hasNext();) {
                IOPort port = (IOPort) ports.next();

                if (DFUtilities.getRate(port) == 0) {
                    return actor;
                }
            }
        }

        return null;
    }

    /** Propagate the number of fractional firings decided for this actor
     *  through the specified port.  Compute the fractional
     *  firing ratio for each actor that is connected to the given port.
     *  If we have not previously computed the ratio for an
     *  actor, then store the value in the given map of firing ratios and move
     *  the actor from the remainingActors list to the pendingActors list.
     *  If the value has been previously computed and is not the same,
     *  then the model is not schedulable and an exception will be thrown.
     *  Note that ports directly contained by the given container are
     *  handled slightly differently from other ports.  Most importantly,
     *  their rates are propagated to ports they are connected to on the
     *  inside, as opposed to ports they are connected to on the outside.
     *
     *  @param container The actor that is being scheduled.
     *  @param currentPort The port that we are propagating from.
     *  @param entityToFiringsPerIteration The current Map of
     *  fractional firing ratios for each actor.  This map will be
     *  updated if the ratio for any actor has not been previously
     *  computed.
     *  @param externalRates A map from external ports of container to
     *  the fractional rates of that port.  This will be updated
     *  during this method.
     *  @param remainingActors The set of actors that have not had their
     *  fractional firing set.  This will be updated during this method.
     *  @param pendingActors The set of actors that have had their rate
     *  set, but have not been propagated onwards.  This will be updated
     *  during this method.
     *  @param clusteredActors The set of actors that are within one
     *  cluster, i.e., they are connected.
     *  @param clusteredExternalPorts The set of external ports that
     *  are connected with the same cluster of actors.
     *
     *  @exception NotSchedulableException If the model is not
     *  schedulable.
     *  @exception IllegalActionException If the expression for a
     *  rate parameter is not valid.
     */
    @SuppressWarnings("unused")
    private void _propagatePort(CompositeActor container, IOPort currentPort,
            Map entityToFiringsPerIteration, Map externalRates,
            LinkedList remainingActors, LinkedList pendingActors,
            Set clusteredActors, Set clusteredExternalPorts)
                    throws NotSchedulableException, IllegalActionException {
        ComponentEntity currentActor = (ComponentEntity) currentPort
                .getContainer();

        // First check to make sure that this port is not connected to
        // any other output ports on the outside.
        // This results in a non-deterministic merge and is illegal.
        // Do not do this test for output ports where we are propagating
        // inwards instead of outwards.
        if (currentPort.isOutput() && currentPort.getContainer() != container) {
            Iterator connectedPorts = currentPort.deepConnectedPortList()
                    .iterator();

            // Make sure any connected output ports are connected on
            // the inside.
            while (connectedPorts.hasNext()) {
                IOPort connectedPort = (IOPort) connectedPorts.next();

                // connectedPort might be connected on the inside to the
                // currentPort, which is legal.  The container argument
                // is always the container of the director, so any port
                // that has that container must be connected on the inside.
                if (connectedPort.isOutput()
                        && connectedPort.getContainer() != container) {
                    throw new NotSchedulableException(currentPort,
                            connectedPort,
                            "Output ports drive the same relation. "
                                    + "This is not legal in SDF.");
                } else if (connectedPort.isInput()
                        && connectedPort.getContainer() == container) {
                    throw new NotSchedulableException(currentPort,
                            connectedPort,
                            "Output port drives the same relation "
                                    + "as the external input port. "
                                    + "This is not legal in SDF.");
                }
            }
        }

        // Next check to make sure that if this port is an external
        // input port, then it does not drive the same relation as some
        // other output port or some other external input port.
        // This results in a non-deterministic merge and is illegal.
        if (currentPort.isInput() && currentPort.getContainer() == container) {
            Iterator connectedPorts = currentPort.deepInsidePortList()
                    .iterator();

            // Make sure any connected output ports are connected on
            // the inside.
            while (connectedPorts.hasNext()) {
                IOPort connectedPort = (IOPort) connectedPorts.next();

                // connectPort might be connected on the inside to the
                // currentPort, which is legal.  The container argument
                // is always the container of the director, so any port
                // that has that container must be connected on the inside.
                if (connectedPort.isOutput()
                        && connectedPort.getContainer() != container) {
                    throw new NotSchedulableException(currentPort,
                            connectedPort,
                            "External input port drive the same relation "
                                    + "as an output port. "
                                    + "This is not legal in SDF.");
                } else if (connectedPort.isInput()
                        && connectedPort.getContainer() == container) {
                    throw new NotSchedulableException(currentPort,
                            connectedPort,
                            "External input port drives the same relation "
                                    + "as another external input port. "
                                    + "This is not legal in SDF.");
                }
            }
        }

        Director director = (Director) getContainer();
        CompositeActor model = (CompositeActor) director.getContainer();

        // Get the rate of this port.
        int currentRate;

        if (currentActor == model) {
            currentRate = 1;
        } else {
            currentRate = DFUtilities.getRate(currentPort);
        }

        // Port rates of less than zero are not valid.
        if (currentRate < 0) {
            throw new NotSchedulableException(currentPort,
                    "Rate cannot be less than zero.  It was: " + currentRate);
        }

        // Propagate to anything that this port is connected to.  For
        // external ports, this is anything that is connected on the
        // inside.  For ports of actors that are being scheduled, this is
        // anything that is connected on the outside.
        Iterator connectedPorts;

        if (currentPort.getContainer() == container) {
            // Find all the ports that are deeply connected to
            // current port on the inside.

            if (_debugging && VERBOSE) {
                // Move this inside and avoid FindBugs Dead Local Store
                connectedPorts = currentPort.deepInsidePortList().iterator();
                _debug("deepInsidePortList of " + currentPort);

                while (connectedPorts.hasNext()) {
                    _debug(connectedPorts.next().toString());
                }
            }

            connectedPorts = currentPort.deepInsidePortList().iterator();
        } else {
            connectedPorts = currentPort.deepConnectedPortList().iterator();
        }

        // For every port we are connected to.
        while (connectedPorts.hasNext()) {
            IOPort connectedPort = (IOPort) connectedPorts.next();

            ComponentEntity connectedActor = (ComponentEntity) connectedPort
                    .getContainer();

            if (_debugging && VERBOSE) {
                _debug("Propagating " + currentPort + " to "
                        + connectedActor.getName());
            }

            int connectedRate;

            if (connectedActor == model) {
                connectedRate = 1;
            } else {
                connectedRate = DFUtilities.getRate(connectedPort);
            }

            // currentFiring is the firing ratio that we've already
            // calculated for currentActor
            Fraction currentFiring = (Fraction) entityToFiringsPerIteration
                    .get(currentActor);

            // Compute the firing ratio that we think connected actor
            // should have, based on its connection to currentActor
            Fraction desiredFiring;

            // HDF actors might have zero rates...
            if (currentRate == 0 && connectedRate > 0) {
                // The current port of the current actor has a rate
                // of 0, and the current connected port of the
                // connected actor has a positive integer rate.
                // therefore, we must set the firing count of
                // the connected actor to 0 so that it will
                // not appear in the final static schedule.
                desiredFiring = Fraction.ZERO;
            } else if (currentRate > 0 && connectedRate == 0) {
                // The current port of the current actor has a
                // positive integer rate, and the current
                // connected port of the connected actor has
                // rate of 0. therefore, we set the firing
                // count of the current actor to 0 so that
                // it will not appear in the final static schedule.
                currentFiring = Fraction.ZERO;

                // Update the entry in the firing table.
                entityToFiringsPerIteration.put(currentActor, currentFiring);

                // Set the firing count of the connected actor to
                // be 1.
                desiredFiring = new Fraction(1);
            } else if (currentRate == 0 && connectedRate == 0) {
                // Give the connected actor the same rate as the
                // current actor.
                desiredFiring = currentFiring;
            } else {
                // Both the rates are non zero, so we can just do the
                // regular actor propagation.
                desiredFiring = currentFiring.multiply(new Fraction(
                        currentRate, connectedRate));
            }

            // Now, compare the firing ratio that was computed before
            // with what we just determined.
            // This should be either
            // the firing that we computed previously, or null
            // if the port is an external port, or _minusOne if
            // we have not computed the firing ratio for this actor yet.
            Fraction presentFiring = (Fraction) entityToFiringsPerIteration
                    .get(connectedActor);

            if (_debugging && VERBOSE) {
                _debug("presentFiring of connectedActor " + connectedActor
                        + " = " + presentFiring);
            }

            // if (presentFiring == null) {
            // Make sure to check for presentFiring == null here so that
            // we avoid a NullPointerException if the model is ill formed.
            // I had problems here with a bug in Publisher.clone() and
            // Subscriber.clone() where presentFiring was null.
            // Getting a NullPointerException is bad, we should check
            // for null and try to give a better message.
            if (connectedActor == model || presentFiring == null) {
                // We've gotten out to an external port.
                // Temporarily create the entry in the firing table.
                // This is possibly rather fragile.
                entityToFiringsPerIteration.put(connectedActor, desiredFiring);

                // Compute the external rate for this port.
                Fraction rate = currentFiring.multiply(new Fraction(
                        currentRate, 1));
                Fraction previousRate = (Fraction) externalRates
                        .get(connectedPort);

                if (previousRate == null) {
                    // This can happen if we somehow have a link to a port
                    // within a class definition.
                    // Give better error message than null pointer exception.
                    throw new InternalErrorException(
                            "Invalid connection found between ports "
                                    + currentPort.getFullName()
                                    + " and "
                                    + connectedPort.getFullName()
                                    + ". The rate of the "
                                    + connectedPort.getFullName()
                                    + " was not found in the map from external ports of the container"
                                    + " to the fractional rates of that port, or is null.  "
                                    + " Perhaps there is a link to a port within a class "
                                    + "definition? The container of "
                                    + currentPort.getFullName()
                                    + (((Entity) currentPort.getContainer())
                                            .isWithinClassDefinition() ? " is"
                                                    : " is not")
                                                    + " within an actor oriented class definition. "
                                                    + "The container of "
                                                    + connectedPort.getFullName()
                                                    + (((Entity) connectedPort.getContainer())
                                                            .isWithinClassDefinition() ? " is"
                                                                    : " is not")
                                                                    + " within an actor oriented class definition.");

                }

                //if (previousRate.equals(Fraction.ZERO)) {
                if (!clusteredExternalPorts.contains(connectedPort)) {
                    clusteredExternalPorts.add(connectedPort);
                    externalRates.put(connectedPort, rate);

                    _propagatePort(container, connectedPort,
                            entityToFiringsPerIteration, externalRates,
                            remainingActors, pendingActors, clusteredActors,
                            clusteredExternalPorts);
                } else if (!rate.equals(previousRate)) {
                    // The rates don't match.
                    throw new NotSchedulableException(this, "No solution "
                            + "exists for the balance equations.\n"
                            + "Graph is not "
                            + "consistent under the SDF domain "
                            + "detected on external port "
                            + connectedPort.getFullName());
                }

                // _propagatePort(container, connectedPort,
                //         entityToFiringsPerIteration, externalRates,
                //         remainingActors, pendingActors, clusteredActors,
                //        clusteredExternalPorts);
                // entityToFiringsPerIteration.remove(connectedActor);
            } else if (presentFiring.equals(_minusOne)) {
                // So we are propagating here for the first time.
                // Create the entry in the firing table.
                entityToFiringsPerIteration.put(connectedActor, desiredFiring);

                // Remove them from remainingActors.
                remainingActors.remove(connectedActor);
                clusteredActors.add(connectedActor);

                // and add them to the pendingActors.
                pendingActors.addLast(connectedActor);
            } else if (!presentFiring.equals(desiredFiring)) {
                // So we've already propagated here, but the
                // firingsPerIteration don't match.
                throw new NotSchedulableException(this, "No solution "
                        + "exists for the balance equations.\n"
                        + "Graph is not " + "consistent under the SDF domain "
                        + "detected on external port "
                        + connectedPort.getFullName());
            }

            if (_debugging && VERBOSE) {
                _debug("New Firing: ");
                _debug(entityToFiringsPerIteration.toString());
            }
        }
    }

    /** Create a schedule for a set of actors.  Given a valid
     *  firing vector, simulate the scheduling of the actors until the
     *  end of one synchronous dataflow iteration.
     *  Each actor will appear in the schedule exactly the number of times that
     *  minimally solves the balance equations and in an order where each
     *  actor has sufficient tokens on its inputs to fire.   Note that no
     *  claim is made that this is an optimal solution in any other sense.
     *
     *  @param externalRates Map from external port to an Integer
     *   representing the number of tokens produced or consumed from
     *   that port during the course of an iteration.
     *  @param actorList The actors that need to be scheduled.
     *  @param container The container.
     *  @return An instance of the Schedule class, indicating the order
     *   in which actors should fire.
     *  @exception NotSchedulableException If the algorithm encounters an SDF
     *   graph that is not consistent with the firing vector, or detects an
     *   inconsistent internal state, or detects a graph that cannot be
     *   scheduled.
     */
    @SuppressWarnings("unused")
    private Schedule _scheduleConnectedActors(Map externalRates,
            List actorList, CompositeActor container)
                    throws NotSchedulableException {
        // A linked list containing all the actors that have no inputs.
        LinkedList readyToScheduleActorList = new LinkedList();

        Schedule newSchedule = new Schedule();

        // An association between each actor and the number of firings
        // for that actor that remain to be simulated.
        // NOTE: This used to be a TreeMap using DFUtilities.NamedObjComparator().
        // However, that comparator is very slow.
        // Map firingsRemainingVector = new TreeMap(
        //        new DFUtilities.NamedObjComparator());
        Map firingsRemainingVector = new HashMap();

        // Initialized the firingsRemainingVector to the current
        // firing vector.
        firingsRemainingVector.putAll(_firingVector);

        // A list of all that actors that we have not yet completely scheduled.
        // FIXME: Is this list needed?
        LinkedList unscheduledActorList = new LinkedList();
        unscheduledActorList.addAll(actorList);

        try {

            // Initializing waitingTokens at all the input ports of actors and
            // output ports of the model to zero is not necessary because
            // SDFReceiver.clear() does it.

            // The above statement seems incorrect to me. The only place where
            // SDFReceiver.clear() is called is during initialization.
            // For models that need to recompute the schedule during the
            // execution, _waitingTokens is not cleared to zero, because
            // initialize() is called only once at the beginning of the
            // execution. Plus, in code generation, initialize() is never
            // called. so I'm adding code to clear the _waitingTokes to zero
            // here:
            // --Gang Zhou
            Iterator actorsIterator = actorList.iterator();
            while (actorsIterator.hasNext()) {
                Actor actor = (Actor) actorsIterator.next();
                Iterator inputPorts = actor.inputPortList().iterator();
                while (inputPorts.hasNext()) {
                    IOPort inputPort = (IOPort) inputPorts.next();
                    Receiver[][] receivers = inputPort.getReceivers();
                    if (receivers != null) {
                        for (int m = 0; m < receivers.length; m++) {
                            for (int n = 0; n < receivers[m].length; n++) {
                                Receiver r = receivers[m][n];
                                while (r instanceof IntermediateReceiver) {
                                    r = ((IntermediateReceiver) r)._receiver;
                                }
                                ((SDFReceiver) r)._waitingTokens = 0;
                            }
                        }
                    }
                }
            }
            Iterator externalOutputPorts = container.outputPortList()
                    .iterator();
            while (externalOutputPorts.hasNext()) {
                IOPort outputPort = (IOPort) externalOutputPorts.next();
                Receiver[][] receivers = outputPort.getInsideReceivers();
                if (receivers != null) {
                    for (int m = 0; m < receivers.length; m++) {
                        for (int n = 0; n < receivers[m].length; n++) {
                            Receiver r = receivers[m][n];
                            while (r instanceof IntermediateReceiver) {
                                r = ((IntermediateReceiver) r)._receiver;
                            }
                            ((SDFReceiver) r)._waitingTokens = 0;
                        }
                    }
                }
            }

            // Simulate the creation of initialization tokens (delays).
            // Fill readyToScheduleActorList with all the actors that have
            // no unfulfilled input ports, and are thus ready to fire.
            // This includes actors with no input ports and those
            // whose input ports have consumption rates of zero.
            Iterator actors = actorList.iterator();

            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                int firingsRemaining = ((Integer) firingsRemainingVector
                        .get(actor)).intValue();

                if (firingsRemaining == 0) {
                    unscheduledActorList.remove(actor);
                    continue;
                }

                int inputCount = _countUnfulfilledInputs(actor, actorList, true);

                if (inputCount == 0) {
                    readyToScheduleActorList.addFirst(actor);
                }

                if (_debugging && VERBOSE) {
                    _debug("Actor " + ((ComponentEntity) actor).getName()
                            + " has " + inputCount + " unfulfilledInputs.");
                }
            }

            // Simulate production of initial tokens.
            actors = actorList.iterator();

            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                Iterator outputPorts = actor.outputPortList().iterator();

                while (outputPorts.hasNext()) {
                    IOPort outputPort = (IOPort) outputPorts.next();
                    int count = DFUtilities.getTokenInitProduction(outputPort);

                    if (_debugging && VERBOSE) {
                        _debug("Simulating " + count
                                + " initial tokens created on " + outputPort);
                    }

                    if (count > 0) {
                        _simulateTokensCreated(outputPort, count, actorList,
                                readyToScheduleActorList);
                    }
                }

                // Also simulate initially available tokens on input ports.
                Iterator inputPorts = actor.inputPortList().iterator();

                while (inputPorts.hasNext()) {
                    IOPort inputPort = (IOPort) inputPorts.next();
                    int count = DFUtilities.getTokenInitConsumption(inputPort);

                    // If the port is an input port that sends initial tokens
                    // to the inside, then those tokens are represented by
                    // an init _production_ parameter. This needs to be taken
                    // into account too.
                    count += DFUtilities.getTokenInitProduction(inputPort);

                    if (_debugging && VERBOSE) {
                        _debug("Simulating " + count
                                + " initial tokens on input " + inputPort);
                    }

                    if (count > 0) {
                        _simulateInitialTokens(inputPort, count, actorList,
                                readyToScheduleActorList);
                    }
                }
            }

            // Simulate a number of tokens initially present on each
            // external input port.
            for (Iterator inputPorts = container.inputPortList().iterator(); inputPorts
                    .hasNext();) {
                IOPort port = (IOPort) inputPorts.next();
                int count = ((Integer) externalRates.get(port)).intValue();

                // The input port may have initial tokens (SubcriberPort supports this).
                count += DFUtilities.getTokenInitProduction(port);

                if (count > 0) {
                    _simulateExternalInputs(port, count, actorList,
                            readyToScheduleActorList);
                }
            }

            // Simulate a number of tokens initially present on each
            // external output port. An output port may have initial
            // tokens on its inside receiver ready to be transported
            // to the outside upon initialization. E.g., a publisher
            // port with initial tokens.
            for (Iterator outputPorts = container.outputPortList().iterator(); outputPorts
                    .hasNext();) {
                IOPort port = (IOPort) outputPorts.next();
                int count = DFUtilities.getTokenInitProduction(port);
                if (count > 0) {
                    _simulateInitialOutputTokens(port, count);
                }
            }

            // While we have actors left, pick one that is ready and fire it.
            while (readyToScheduleActorList.size() > 0) {
                if (_debugging && VERBOSE) {
                    _debug("Actors that can be scheduled:");

                    for (Iterator readyActors = readyToScheduleActorList
                            .iterator(); readyActors.hasNext();) {
                        Entity readyActor = (Entity) readyActors.next();
                        _debug(readyActor.getFullName());
                    }

                    _debug("Actors with firings left:");

                    for (Iterator remainingActors = unscheduledActorList
                            .iterator(); remainingActors.hasNext();) {
                        Entity remainingActor = (Entity) remainingActors.next();
                        _debug(remainingActor.getFullName());
                    }
                }

                // Pick an actor that is ready to fire.
                Actor currentActor = (Actor) readyToScheduleActorList
                        .getFirst();

                // Remove it from the list of actors we are waiting to fire.
                while (readyToScheduleActorList.remove(currentActor)) {
                }

                // Determine the number of times currentActor can fire.
                int numberOfFirings = _computeMaximumFirings(currentActor);

                // We should never schedule something more than the number
                // of times expected by the balance equations.  This might
                // happen because we assume an infinite number of tokens
                // are waiting on external ports.
                int firingsRemaining = ((Integer) firingsRemainingVector
                        .get(currentActor)).intValue();

                if (numberOfFirings > firingsRemaining) {
                    numberOfFirings = firingsRemaining;
                }

                if (_debugging && VERBOSE) {
                    _debug("Scheduling actor " + currentActor.getName() + " "
                            + numberOfFirings + " times.");
                }

                // Update the firingsRemainingVector for this actor.
                firingsRemaining -= numberOfFirings;
                firingsRemainingVector.put(currentActor,
                        Integer.valueOf(firingsRemaining));

                if (_debugging && VERBOSE) {
                    _debug(currentActor.getName() + " should fire "
                            + firingsRemaining + " more times.");
                }

                // Simulate the tokens that are consumed by the actors
                // input ports.
                _simulateInputConsumption(currentActor, numberOfFirings);

                // Add it to the schedule numberOfFirings times.
                Firing firing = new Firing();
                firing.setActor(currentActor);
                firing.setIterationCount(numberOfFirings);
                newSchedule.add(firing);

                // Get all its outputPorts
                // and simulate the proper production of tokens.
                for (Iterator outputPorts = currentActor.outputPortList()
                        .iterator(); outputPorts.hasNext();) {
                    IOPort outputPort = (IOPort) outputPorts.next();

                    int count = DFUtilities.getTokenProductionRate(outputPort);

                    _simulateTokensCreated(outputPort, count * numberOfFirings,
                            unscheduledActorList, readyToScheduleActorList);
                }

                // Figure out what to do with the actor, now that it has been
                // scheduled.
                if (firingsRemaining < 0) {
                    // If we screwed up somewhere, and fired this more
                    // times than we thought we should have
                    // then throw an exception.
                    // This should never happen.
                    throw new InternalErrorException("Balance Equation "
                            + "solution does not agree with "
                            + "scheduling algorithm!");
                }

                if (firingsRemaining == 0) {
                    // If we've fired this actor all the
                    // times that it should, then
                    // we get rid of it entirely.
                    if (_debugging && VERBOSE) {
                        _debug("Actor = " + currentActor + " is done firing.");
                    }

                    // Remove the actor from the unscheduledActorList
                    // since we don't need to fire it any more.
                    while (unscheduledActorList.remove(currentActor)) {
                        ;
                    }

                    if (_debugging && VERBOSE) {
                        _debug("Remaining actors:");

                        for (Iterator readyActors = readyToScheduleActorList
                                .iterator(); readyActors.hasNext();) {
                            Entity entity = (Entity) readyActors.next();
                            _debug(entity.getFullName());
                        }
                    }
                } else {
                    // Otherwise the actor still has firings left.
                    // Count the number of unfulfilled inputs.
                    int inputCount = _countUnfulfilledInputs(currentActor,
                            unscheduledActorList, false);

                    // We've already removed currentActor from
                    // readyToSchedule actors, and presumably
                    // fired it until it can be fired no more.
                    // This check is here for robustness...
                    // if the actor can still be scheduled
                    // i.e. all its inputs are satisfied, and it
                    // appears in the unscheduled actors list
                    // then put it on the readyToScheduleActorList.
                    if (inputCount <= 0
                            && unscheduledActorList.contains(currentActor)) {
                        readyToScheduleActorList.addFirst(currentActor);
                    }
                }
            }
        } catch (IllegalActionException ex) {
            // This could happen if we call getTokenConsumptionRate on a
            // port that isn't a part of the actor.   This probably means
            // the graph is screwed up, or somebody else is mucking
            // with it.
            throw new InternalErrorException(this, ex,
                    "SDF Scheduler Failed internal consistency check.");
        }

        // If there are any actors left when we're done, then report the
        // error.
        if (unscheduledActorList.size() > 0) {
            StringBuffer message = new StringBuffer(
                    "Actors remain that cannot be scheduled!\n"
                            + "\nThere are several possible reasons:\n"
                            + "* SDF Graphs with feedback loops should have an actor "
                            + "with a delay in the loop, such as a SampleDelay.\n"
                            + "* The SDF director has an \"allowDisconnectedGraphs\""
                            + "parameter, which, when true, permits disconnected "
                            + "SDF graphs.\n"
                            + "* The token consumption rate and production rates might "
                            + "be mismatched.\nUsually, actors produce one token or consume "
                            + "one token on a port.  To produce or consume multiple tokens "
                            + "per firing, add a \"tokenConsumptionRate\" or "
                            + "\"tokenProductionRate\" parameter to the appropriate port.\n"
                            + "Unscheduled actors:\n");

            // Only display the first 100 connected or disconnected actors.
            int count = 0;
            for (Iterator actors = unscheduledActorList.iterator(); actors
                    .hasNext() && count < 100; count++) {
                Entity entity = (Entity) actors.next();
                message.append(entity.getFullName() + " ");
            }

            if (count >= 99) {
                message.append("...");
            }
            message.append("\nScheduled actors:\n");
            List scheduledActorList = new LinkedList();
            scheduledActorList.addAll(actorList);
            scheduledActorList.removeAll(unscheduledActorList);

            count = 0;

            for (Iterator actors = scheduledActorList.iterator(); actors
                    .hasNext() && count < 100; count++) {
                Entity entity = (Entity) actors.next();
                message.append(entity.getFullName() + " ");
            }

            if (count >= 99) {
                message.append("...");
            }
            throw new NotSchedulableException(this, message.toString());
        }

        if (_debugging) {
            _debug("Schedule is:");
            _debug(newSchedule.toString());
        }

        return newSchedule;
    }

    /** Simulate the consumption of tokens from the given external input
     *  port.  This assumes the input ports have the number of tokens given
     *  by their rate.
     *  @param port The external input port.
     *  @param count The number of tokens assumed to be on that port.
     *  @param actorList The list of actors.
     *  @param readyToScheduleActorList The list of actors that are ready
     *   to be scheduled.  This will be updated if any actors that receive
     *   tokens from outputPort are now ready to fire.
     *  @exception IllegalActionException If thrown while reading a token,
     *  setting the capacity of a receiver or counting unfulfilled input.s
     */
    @SuppressWarnings("unused")
    protected void _simulateExternalInputs(IOPort port, int count,
            List actorList, LinkedList readyToScheduleActorList)
                    throws IllegalActionException {
        Receiver[][] receivers = port.deepGetReceivers();

        if (_debugging && VERBOSE) {
            _debug("Simulating external input tokens from "
                    + port.getFullName());
            _debug("number of inside channels = " + receivers.length);
        }

        for (int channel = 0; channel < receivers.length; channel++) {
            if (receivers[channel] == null) {
                continue;
            }

            for (int copy = 0; copy < receivers[channel].length; copy++) {
                if (!(receivers[channel][copy] instanceof SDFReceiver)) {
                    // This should only occur if it is null.
                    continue;
                }

                SDFReceiver receiver = (SDFReceiver) receivers[channel][copy];
                IOPort connectedPort = receivers[channel][copy].getContainer();
                ComponentEntity connectedActor = (ComponentEntity) connectedPort
                        .getContainer();

                // If the connected port has an initial tokens for consumption,
                // those need to be added to the count.
                count += DFUtilities.getTokenInitConsumption(connectedPort);

                receiver._waitingTokens = count;

                // Update the buffer size, if necessary.
                boolean enforce = ((BooleanToken) constrainBufferSizes
                        .getToken()).booleanValue();

                if (enforce) {
                    int capacity = receiver.getCapacity();

                    if (capacity == SDFReceiver.INFINITE_CAPACITY
                            || receiver._waitingTokens > capacity) {
                        receiver.setCapacity(count);
                    }
                }

                // Determine whether the connectedActor can now be scheduled.
                // Only proceed if the connected actor is something we are
                // scheduling.  The most notable time when this will not be
                // true is when a connection is made to the inside of an opaque port.
                if (actorList.contains(connectedActor)) {
                    int inputCount = _countUnfulfilledInputs(
                            (Actor) connectedActor, actorList, false);
                    int firingsRemaining = _getFiringCount(connectedActor);

                    // If so, then add it to the proper list.
                    // Note that the actor may appear more than once.
                    // This is OK, since we remove all of the appearances from
                    // the list when the actor is actually scheduled.
                    if (inputCount < 1 && firingsRemaining > 0) {
                        // Ned Stoffel suggested changing this from
                        // addLast() to addFirst() so as to minimize
                        // the number of tokens in transit.  "This leads
                        // to a markedly more serial schedule, as can
                        // be demonstrated by animating the simulations"
                        readyToScheduleActorList.addFirst(connectedActor);
                    }
                }
            }
        }
    }

    /** Simulate the consumption of tokens by the actor during execution of
     *  the given number of firings. Also determine whether enough tokens
     *  still remain at the inputs of the actor  for it to fire again immediately.
     *  @param currentActor The actor to be fired.
     *  @param firingCount The number of firings.
     *  @return true If the actor can fire again.
     *  @exception IllegalActionException If the rate parameters are invalid.
     */
    protected boolean _simulateInputConsumption(Actor currentActor,
            int firingCount) throws IllegalActionException {
        boolean stillReadyToSchedule = true;

        // Update the number of tokens waiting on the actor's input ports.
        Iterator inputPorts = currentActor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            int tokenRate = DFUtilities.getTokenConsumptionRate(inputPort);

            Receiver[][] receivers = inputPort.getReceivers();

            for (int channel = 0; channel < receivers.length; channel++) {
                if (receivers[channel] == null) {
                    continue;
                }

                for (int copy = 0; copy < receivers[channel].length; copy++) {
                    if (!(receivers[channel][copy] instanceof SDFReceiver)) {
                        // This should only occur if it is null.
                        continue;
                    }

                    SDFReceiver receiver = (SDFReceiver) receivers[channel][copy];
                    receiver._waitingTokens -= tokenRate * firingCount;

                    if (receiver._waitingTokens < tokenRate) {
                        stillReadyToSchedule = false;
                    }
                }
            }
        }

        return stillReadyToSchedule;
    }

    /** Simulate the creation of tokens by the given output port when
     *  its actor fires.  If any actors that receive tokens are then ready to
     *  fire, given that only actors in the actor list are being scheduled, then
     *  add those actors to the list of actors that are ready to schedule.
     *  @param outputPort The port that is creating the tokens.
     *  @param createdTokens The number of tokens to create.
     *  @param actorList The list of actors that are being scheduled.
     *  @param readyToScheduleActorList The list of actors that are ready
     *   to be scheduled.  This will be updated if any actors that receive
     *   tokens from outputPort are now ready to fire.
     */
    @SuppressWarnings("unused")
    private void _simulateTokensCreated(IOPort outputPort, int createdTokens,
            List actorList, LinkedList readyToScheduleActorList)
                    throws IllegalActionException {
        // FIXME: Why are the actor lists lists rather than sets?
        Receiver[][] receivers = outputPort.getRemoteReceivers();

        if (_debugging && VERBOSE) {
            _debug("Creating " + createdTokens + " tokens on "
                    + outputPort.getFullName());
            _debug("source channels = " + receivers.length);
            _debug("width = " + outputPort.getWidth());
        }

        for (int channel = 0; channel < receivers.length; channel++) {
            if (receivers[channel] == null) {
                continue;
            }

            for (int copy = 0; copy < receivers[channel].length; copy++) {
                if (!(receivers[channel][copy] instanceof SDFReceiver)) {
                    // NOTE: This should only occur if it is null.
                    continue;
                }

                SDFReceiver receiver = (SDFReceiver) receivers[channel][copy];
                IOPort connectedPort = receivers[channel][copy].getContainer();
                ComponentEntity connectedActor = (ComponentEntity) connectedPort
                        .getContainer();

                // Increment the number of waiting tokens.
                receiver._waitingTokens += createdTokens;

                // Update the buffer size, if necessary.
                boolean enforce = ((BooleanToken) constrainBufferSizes
                        .getToken()).booleanValue();

                if (enforce) {
                    int capacity = receiver.getCapacity();

                    if (capacity == SDFReceiver.INFINITE_CAPACITY
                            || receiver._waitingTokens > capacity) {
                        receiver.setCapacity(receiver._waitingTokens);
                    }
                }

                // Only proceed if the connected actor is
                // something we are scheduling.
                // The most notable time when this will not be
                // true is when a connection is made to the
                // inside of an opaque port.
                if (actorList.contains(connectedActor)) {
                    // Check and see whether the connectedActor
                    // can be scheduled.
                    int inputCount = _countUnfulfilledInputs(
                            (Actor) connectedActor, actorList, false);
                    int firingsRemaining = _getFiringCount(connectedActor);

                    // If so, then add it to the proper list.
                    // Note that the actor may appear more than once.
                    // This is OK, since we remove all of the appearances from
                    // the list when the actor is actually scheduled.
                    if (inputCount < 1 && firingsRemaining > 0) {
                        // Ned Stoffel suggested changing this from
                        // addLast() to addFirst() so as to minimize
                        // the number of tokens in transit.  "This leads
                        // to a markedly more serial schedule, as can
                        // be demonstrated by animating the simulations"
                        readyToScheduleActorList.addFirst(connectedActor);
                    }
                }
            }
        }
    }

    /** Simulate the availability of initial tokens on the given input port when
     *  its actor first fires.  If its actors becomes ready to fire with these
     *  initial tokens, given that only actors in the actor list are being scheduled, then
     *  add its actors to the list of actors that are ready to schedule.
     *  @param inputPort The port that will have initial tokens.
     *  @param initialTokens The number of initial tokens.
     *  @param actorList The list of actors that are being scheduled.
     *  @param readyToScheduleActorList The list of actors that are ready
     *   to be scheduled.  This will be updated if the actor of this input
     *   port becomes ready to fire.
     */
    @SuppressWarnings("unused")
    private void _simulateInitialTokens(IOPort inputPort, int initialTokens,
            List actorList, LinkedList readyToScheduleActorList)
                    throws IllegalActionException {
        Receiver[][] receivers = inputPort.getReceivers();

        if (_debugging && VERBOSE) {
            _debug("Initializing with " + initialTokens + " tokens on "
                    + inputPort.getFullName());
            _debug("input channels = " + receivers.length);
            _debug("width = " + inputPort.getWidth());
        }

        for (int channel = 0; channel < receivers.length; channel++) {
            if (receivers[channel] == null) {
                continue;
            }

            for (int copy = 0; copy < receivers[channel].length; copy++) {
                if (!(receivers[channel][copy] instanceof SDFReceiver)) {
                    // NOTE: This should only occur if it is null.
                    continue;
                }

                SDFReceiver receiver = (SDFReceiver) receivers[channel][copy];
                IOPort itsPort = receivers[channel][copy].getContainer();
                ComponentEntity itsActor = (ComponentEntity) itsPort
                        .getContainer();

                // Increment the number of waiting tokens.
                receiver._waitingTokens += initialTokens;

                // Update the buffer size, if necessary.
                boolean enforce = ((BooleanToken) constrainBufferSizes
                        .getToken()).booleanValue();

                if (enforce) {
                    int capacity = receiver.getCapacity();

                    if (capacity == SDFReceiver.INFINITE_CAPACITY
                            || receiver._waitingTokens > capacity) {
                        receiver.setCapacity(receiver._waitingTokens);
                    }
                }

                // Only proceed if the connected actor is
                // something we are scheduling.
                // The most notable time when this will not be
                // true is when a connection is made to the
                // inside of an opaque port.
                if (actorList.contains(itsActor)) {
                    // Check and see whether the connectedActor
                    // can be scheduled.
                    int inputCount = _countUnfulfilledInputs((Actor) itsActor,
                            actorList, false);
                    int firingsRemaining = _getFiringCount(itsActor);

                    // If so, then add it to the proper list.
                    // Note that the actor may appear more than once.
                    // This is OK, since we remove all of the appearances from
                    // the list when the actor is actually scheduled.
                    if (inputCount < 1 && firingsRemaining > 0) {
                        // Ned Stoffel suggested changing this from
                        // addLast() to addFirst() so as to minimize
                        // the number of tokens in transit.  "This leads
                        // to a markedly more serial schedule, as can
                        // be demonstrated by animating the simulations"
                        readyToScheduleActorList.addFirst(itsActor);
                    }
                }
            }
        }
    }

    /** Simulate the availability of initial tokens on the inside
     *  of the given output port.  These initial tokens have no
     *  effect on the schedule at this level of the hierarchy,
     *  but they do affect the capacity of the inside receivers
     *  on such ports.
     */
    @SuppressWarnings("unused")
    private void _simulateInitialOutputTokens(IOPort outputPort,
            int initialTokens) throws IllegalActionException {
        Receiver[][] receivers = outputPort.getInsideReceivers();

        if (_debugging && VERBOSE) {
            _debug("Initializing with " + initialTokens
                    + " tokens on the inside of " + outputPort.getFullName());
            _debug(" input channels = " + receivers.length);
            _debug(" width = " + outputPort.getWidthInside());
        }

        for (int channel = 0; channel < receivers.length; channel++) {
            if (receivers[channel] == null) {
                continue;
            }

            for (int copy = 0; copy < receivers[channel].length; copy++) {
                if (!(receivers[channel][copy] instanceof SDFReceiver)) {
                    // NOTE: This should only occur if it is null.
                    continue;
                }

                SDFReceiver receiver = (SDFReceiver) receivers[channel][copy];

                // Increment the number of waiting tokens.
                receiver._waitingTokens += initialTokens;

                // Update the buffer size, if necessary.
                boolean enforce = ((BooleanToken) constrainBufferSizes
                        .getToken()).booleanValue();

                if (enforce) {
                    int capacity = receiver.getCapacity();

                    if (capacity == SDFReceiver.INFINITE_CAPACITY
                            || receiver._waitingTokens > capacity) {
                        receiver.setCapacity(receiver._waitingTokens);
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A map from actors to an integer representing the
     *  number of times the actor will fire.
     */
    protected Map _firingVector = new HashMap();

    /** A fraction equal to -1.  Used in several places to indicate an
     * actor for which we have not determined the number of times it will
     * fire.
     */
    private Fraction _minusOne = new Fraction(-1);

    /** Mmaps from external
     * ports to the number of tokens that that port
     * will produce or consume in each firing.
     * It gets populated with the fractional production ratios
     * and is used in the end to set final rates on external ports.
     */
    protected Map _externalRates = new HashMap();

    // NOTE: This used to be a TreeMap using DFUtilities.NamedObjComparator().
    // However, that comparator is very slow.

    //private Set _clusteredActors = new HashSet();
    //private Set _clusteredExternalPorts = new HashSet();

    /** The list of rate variables that this scheduler is listening to
     * for rate changes.
     */
    protected List _rateVariables = new LinkedList();
}
