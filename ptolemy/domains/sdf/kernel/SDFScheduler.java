/* A Scheduler for the SDF domain

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Green (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.kernel;

import java.util.*;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.Receiver;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.ConstVariableModelAnalysis;
import ptolemy.actor.util.DependencyDeclaration;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.Fraction;

///////////////////////////////////////////////////////////
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
<i>tokenProductionRate</i>, and <i>tokenInitProduction</i> must be set.
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
@see ptolemy.domains.sdf.kernel.SDFIOPort
@see ptolemy.domains.sdf.lib.SampleDelay

@author Stephen Neuendorffer and Brian Vogel
@version $Id$
@since Ptolemy II 0.2
*/
public class SDFScheduler extends BaseSDFScheduler implements ValueListener {
    /** Construct a scheduler with no container(director)
     *  in the default workspace, the name of the scheduler is
     *  "Scheduler".
     */
    public SDFScheduler() {
        super();
        _localMemberInitialize();
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
        _localMemberInitialize();
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
        _localMemberInitialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Declare the rate dependency on any external ports of the model.
     *  SDF directors should invoke this method once during preinitialize.
     */
    public void declareRateDependency() throws IllegalActionException {
        ConstVariableModelAnalysis analysis =
            ConstVariableModelAnalysis.getAnalysis(this);
        SDFDirector director = (SDFDirector)getContainer();
        CompositeActor model = (CompositeActor)director.getContainer();
        for(Iterator ports = model.portList().iterator();
            ports.hasNext();) {
            IOPort port = (IOPort) ports.next();
            if(!(port instanceof ParameterPort)) {
                if(port.isInput()) {
                    _declareDependency(analysis, port, "tokenConsumptionRate",
                            _rateVariables);
                } 
                if(port.isOutput()) {
                    _declareDependency(analysis, port, "tokenProductionRate",
                            _rateVariables);
                    _declareDependency(analysis, port, "tokenInitProduction",
                            _rateVariables);
                }
            }
        }
    }
    
    /** Get the external port rates.
     */
    public Map getExternalRates() {
        return _externalRates;
    }

    /** Create the schedule.  Return the number of times that the given
     *  entity will fire in a single iteration of the system.
     */
    public int getFiringCount(Entity entity)
            throws IllegalActionException {
        getSchedule();
        return _getFiringCount(entity);
    }

    /** This method simply calls _saveContainerRates(Map externalRates). 
     *  It is used in HDF when a cached schedule is used instead of 
     *  computing a new schedule.
     *  @param externalRates A map from external port to the rate of that
     *  port.
     *  @throws NotSchedulableException If an external port is both
     *  an input and an output, or neither an input or an output, or
     *  connected on the inside to ports that have different
     *  tokenInitProduction.
     *  @throws IllegalActionException If any called method throws it.
     */
    public void setContainerRates(Map externalRates)
        throws NotSchedulableException, IllegalActionException  {
        _saveContainerRates(externalRates);
    }

    /** React to the fact that the specified Settable has changed.
     *  This class removes itself as a value listener from all rate
     *  parameters, and invalidates the schedule.
     *  @param settable The object that has changed value.
     */
    public void valueChanged(Settable settable) {
        // FIXME: causes 
        //for(Iterator variables = _rateVariables.iterator();
//             variables.hasNext();) {
//             Variable variable = (Variable)variables.next();
//             variable.removeValueListener(this);
//         }
//        System.out.println("rate value changed: " + settable.getFullName());
        //   _rateVariables.clear();
        setValid(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

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
    protected Schedule _getSchedule()
            throws NotSchedulableException, IllegalActionException {
        SDFDirector director =
            (SDFDirector)getContainer();
        CompositeActor model = (CompositeActor)director.getContainer();
        
        _checkDynamicRateVariables(model, _rateVariables);
        
        int vectorizationFactor = 1;
        if (director instanceof SDFDirector) {
            Token token =
                ((SDFDirector)director).vectorizationFactor.getToken();
            vectorizationFactor = ((IntToken)token).intValue();
        }
        if (vectorizationFactor < 1) {
            throw new NotSchedulableException(this,
                    "The supplied vectorizationFactor must be " +
                    "a positive integer. " +
                    "The given value was: " + vectorizationFactor);
        }
        CompositeActor container = (CompositeActor)director.getContainer();

        // A linked list containing all the actors.
        LinkedList allActorList = new LinkedList();
        // Populate it.
        for (Iterator entities = container.deepEntityList().iterator();
             entities.hasNext();) {
            ComponentEntity entity = (ComponentEntity)entities.next();

            // Fill allActorList with the list of things that we can schedule
            // FIXME: What if other things can be scheduled than actors?
            if (entity instanceof Actor) {
                allActorList.addLast(entity);
            }
        }

        // externalRates maps from external
        // ports to the number of tokens that that port
        // will produce or consume in each firing.
        // It gets populated with the fractional production ratios
        // and is used in the end to set final rates on external ports.
        // This map is initialized to zero.
        Map externalRates = new TreeMap(new SDFUtilities.NamedObjComparator());

        // Initialize externalRates to zero.
        for (Iterator ports = container.portList().iterator();
             ports.hasNext();) {
            IOPort port = (IOPort) ports.next();
            externalRates.put(port, Fraction.ZERO);
        }

        // An association between all the relations in a simulation and
        // and array of the maximum number of tokens that are ever
        // waiting on that relation.
        Map minimumBufferSize = 
            new TreeMap(new SDFUtilities.NamedObjComparator());

        // Initialize the buffer size of each relation to zero.
        for (Iterator relations = container.relationList().iterator();
             relations.hasNext();) {
            Relation relation = (Relation)relations.next();
            minimumBufferSize.put(relation, new Integer(0));
        }

        // First solve the balance equations
        Map entityToFiringsPerIteration =
            _solveBalanceEquations(container, allActorList, externalRates);

        if (_debugging && VERBOSE) {
            _debug("Firing Ratios: " + entityToFiringsPerIteration.toString());
        }

        // A list that contains actors that do not fire.
        LinkedList deadActorList = new LinkedList();
        LinkedList liveActorList = new LinkedList();
        // Populate deadActorList.
        for (Iterator actors = allActorList.iterator();
             actors.hasNext();) {
            ComponentEntity actor = (ComponentEntity)actors.next();
            // Remove this actor from the firing sequence if it will
            // not be fired.
            Fraction firing = (Fraction)entityToFiringsPerIteration.get(actor);
            if (_debugging && VERBOSE) {
                _debug("Actor " + actor.getName() +
                        "fires " + firing.getNumerator() +
                        " times.");
            }
            if (firing.getNumerator() == 0) {
                if (_debugging && VERBOSE) {
                    _debug("and will be removed because "
                            + "it is not being fired.");
                }
                deadActorList.add(actor);
            } else {
                liveActorList.add(actor);
            }
        }

        // Normalize the number of for each actor using the
        // vectorizationFactor.
        _normalizeFirings(vectorizationFactor, entityToFiringsPerIteration,
                externalRates);

        // Set the firing vector.
        _setFiringVector(entityToFiringsPerIteration);

        if (_debugging) {
            _debug("Normalized Firing Counts:");
            _debug(entityToFiringsPerIteration.toString());
        }

        // Schedule all the actors using the calculated firings.
        Schedule result =
            _scheduleConnectedActors(minimumBufferSize, externalRates,
                    liveActorList, container, allActorList);

        if (_debugging && VERBOSE) {
            _debug("Firing Vector:");
            _debug(entityToFiringsPerIteration.toString());
        }

        // Set parameters on each actor that contain the number
        // of firings in an iteration.
        _saveFiringCounts(entityToFiringsPerIteration);
        
        // Set parameters on each relation that contain the maximum
        // buffer sizes necessary during execution.
        _saveBufferSizes(minimumBufferSize);

        // Set the rate parameters of any external ports.
        _saveContainerRates(externalRates);

        // Set the schedule to be valid.
        setValid(true);
        _externalRates = externalRates;
        return result;
    }
    
    /** Initialize the local data members of this object.
     */
    protected void _localMemberInitialize() {
        _firingVector = 
            new TreeMap(new SDFUtilities.NamedObjComparator());
        _firingVectorValid = true;
    }

    /** Solve the balance equations for the list of connected Actors.
     *  For each actor, determine the ratio that determines the rate at
     *  which it should fire relative to the other actors for the graph to
     *  be live and operate within bounded memory. This ratio is known as the
     *  fractional firing of the actor.
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
            List actorList, Map externalRates)
            throws NotSchedulableException, IllegalActionException {

        // The map that we will return.
        // This will be populated with the fraction firing ratios for
        // each actor.
        Map entityToFiringsPerIteration = 
            new TreeMap(new SDFUtilities.NamedObjComparator());

        // The pool of Actors that have not been touched
        // yet. (i.e. all their firingsPerIteration are still set to
        // Fraction equal to -1/1)
        LinkedList remainingActors = new LinkedList();

        // The pool of actors that have their firingsPerIteration set,
        // but have not had their ports explored yet.
        LinkedList pendingActors = new LinkedList();

        // Initialize remainingActors to contain all the actors we were given .
        remainingActors.addAll(actorList);

        // Initialize entityToFiringsPerIteration for each actor to -1.
        for (Iterator actors = remainingActors.iterator();
             actors.hasNext();) {
            ComponentEntity entity = (ComponentEntity) actors.next();
            entityToFiringsPerIteration.put(entity, _minusOne);
        }

        if (remainingActors.size() == 0) {
            // If we've been given
            // no actors to do anything with, return an empty Map.
            return entityToFiringsPerIteration;
        }

        StaticSchedulingDirector director =
            (StaticSchedulingDirector)getContainer();

        boolean allowDisconnectedGraphs = false;
        if (director instanceof SDFDirector) {
            Token token =
                ((SDFDirector)director).allowDisconnectedGraphs.getToken();
            allowDisconnectedGraphs = ((BooleanToken)token).booleanValue();
        }

        if (allowDisconnectedGraphs) {
            // Ned Stoffel's change to support disconnected graphs:

            // Finally, the schedule can jump from one island to
            // another among the disconnected graphs. There is nothing
            // to force the scheduler to finish executing all actors
            // on one island before firing actors on another
            // island. However, the order of execution within an
            // island should be correct.

            while (!remainingActors.isEmpty()) {
                ComponentEntity actor =
                    _pickZeroRatePortActor(remainingActors);
                if (actor == null) {
                    actor = (ComponentEntity)remainingActors.removeFirst();
                } else {
                    remainingActors.remove(actor);
                }

                entityToFiringsPerIteration.put(actor, new Fraction(1));
                pendingActors.addLast(actor);

                while (!pendingActors.isEmpty()) {
                    Actor currentActor = (Actor) pendingActors.removeFirst();
                    Iterator AllPorts =
                        ((ComponentEntity) currentActor).portList().iterator();
                    while (AllPorts.hasNext()) {
                        IOPort currentPort = (IOPort) AllPorts.next();
                        _propagatePort(container, currentPort, 
                                entityToFiringsPerIteration,
                                externalRates, remainingActors, pendingActors);
                    }
                }
            }
            return entityToFiringsPerIteration;
        }

        // Pick an actor as a reference
        // Should pick the reference actor to be one
        // that contains 0-valued ports, if there exists one.
        ComponentEntity actor = _pickZeroRatePortActor(remainingActors);
        if (actor == null) {
            // We did not find an actor with any 0-rate ports,
            // so just pick a reference actor arbitrarily.
            actor = (ComponentEntity)remainingActors.removeFirst();
        } else {
            // We found an actor with at least one 0-rate port.
            remainingActors.remove(actor);
        }
        // Set it's rate to one per iteration
        entityToFiringsPerIteration.put(actor, new Fraction(1));
        // Start the list to recurse over.
        pendingActors.addLast(actor);

        // Loop until we run out of actors that have not been
        // propagated.
        while (!pendingActors.isEmpty()) {
            if (_debugging && VERBOSE) {
                _debug("pendingActors: ");
                _debug(pendingActors.toString());
            }
            // Get the next actor to recurse over
            Actor currentActor = (Actor) pendingActors.removeFirst();
            if (_debugging && VERBOSE) {
                _debug("Balancing from " +
                        ((ComponentEntity) currentActor).getName());
            }

            // Traverse all the input and output ports, setting the
            // firingsPerIteration for the actor(s)????
            // connected to each port relative to currentActor.
            Iterator AllPorts =
                ((ComponentEntity) currentActor).portList().iterator();
            while (AllPorts.hasNext()) {
                IOPort currentPort = (IOPort) AllPorts.next();
                _propagatePort(container, currentPort, 
                        entityToFiringsPerIteration, externalRates,
                        remainingActors, pendingActors);
            }
        }
        if (!remainingActors.isEmpty()) {
            // If there are any actors left that we didn't get to, then
            // this is not a connected graph, and we throw an exception.
            StringBuffer messageBuffer =
                new StringBuffer("SDF scheduler found disconnected actors! "
                        + "Usually, disconnected actors in an SDF model "
                        + "indicates an error.  If this is not an error, try "
                        + "setting the SDFDirector parameter "
                        + "allowDisconnectedGraphs to true.\n"
                        + "Reached Actors:\n");
            List reachedActorList = new LinkedList();
            reachedActorList.addAll(actorList);
            reachedActorList.removeAll(remainingActors);
            for (Iterator actors = reachedActorList.iterator();
                 actors.hasNext();) {
                Entity entity = (Entity)actors.next();
                messageBuffer.append(entity.getFullName() + "\n");
            }
            messageBuffer.append("Unreached Actors:\n");
            Iterator unreachedActors = remainingActors.iterator();
            while (unreachedActors.hasNext()) {
                NamedObj unreachedActor = (NamedObj)(unreachedActors.next());
                messageBuffer.append(unreachedActor.getFullName() + " ");
            }
            throw new NotSchedulableException(messageBuffer.toString());
        }
        return entityToFiringsPerIteration;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _assertDynamicRateVariable(CompositeActor model,
            Variable variable,
            List rateVariables, 
            ConstVariableModelAnalysis analysis) 
            throws IllegalActionException {
        boolean allowRateChanges = ((BooleanToken)
                ((SDFDirector)getContainer()).allowRateChanges.getToken()).booleanValue();
        if(allowRateChanges) {
            // The schedule depends on the rate parameter.
            if(!rateVariables.contains(variable)) {
                variable.addValueListener(this);
                rateVariables.add(variable);
            }
        } else {
            throw new IllegalActionException(variable,
                    "The SDF rate parameter may change." + 
                    " This is not allowed in SDF models " +
                    "that will be run through the code " +
                    "generator.  If you don't care about " +
                    "code generation, then you might " +
                    "consider setting the allowRateChanges " +
                    "parameter of the SDF director to false.");
        }
        Entity changeContext = 
            analysis.getChangeContext(variable);
        if(!(changeContext == model || 
                   changeContext.deepContains(model))) {
            throw new IllegalActionException(variable, 
                    "The SDF rate parameter changes during " +
                    "execution of the schedule!");
        }
    }

    // Populate the given set with the dynamic rate variables in the model.
    protected void _checkDynamicRateVariables(
            CompositeActor model, List rateVariables) 
            throws IllegalActionException {
        // Check for rate parameters which are dynamic.
        ConstVariableModelAnalysis analysis =
            ConstVariableModelAnalysis.getAnalysis(
                    (SDFDirector)getContainer());
        Entity scheduleChangeContext = (Entity)toplevel();
        for(Iterator entities = model.deepEntityList().iterator();
            entities.hasNext();) {
            Entity entity = (Entity)entities.next();
            for(Iterator ports = entity.portList().iterator();
                ports.hasNext();) {
                Port port = (Port) ports.next();
                Set set = analysis.getNotConstVariables(port);
                Variable variable;
                variable = SDFUtilities._getRateVariable(
                        port, "tokenInitProduction");
                if(set.contains(variable)) {
                    _assertDynamicRateVariable(
                            model, variable, rateVariables, analysis);
                } 
                variable = SDFUtilities._getRateVariable(
                        port, "tokenConsumptionRate");
                if(set.contains(variable)) {
                    _assertDynamicRateVariable( 
                            model, variable, rateVariables, analysis);
                } 
                variable = SDFUtilities._getRateVariable(
                        port, "tokenProductionRate");
                if(set.contains(variable)) {
                    _assertDynamicRateVariable(
                            model, variable, rateVariables, analysis);
                } 
            }
        }
    }

    /** Determine the number of times the given actor can fire, based on
     *  the number of tokens that are present on its inputs according to
     *  the given map.
     *
     *  @param currentActor The actor.
     *  @param waitingTokens A map between each input IOPort and the number of
     *  tokens in the queue for that port.
     *  @return The number of times the actor can fire.
     *  @exception IllegalActionException If the rate parameters are invalid.
     */
    private int _computeMaximumFirings(ComponentEntity currentActor,
            Map waitingTokens) throws IllegalActionException {
        int maximumFirings = Integer.MAX_VALUE;

        // Update the number of tokens waiting on the actor's input ports.
        Iterator inputPorts =
            ((Actor) currentActor).inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            int[] tokens = (int []) waitingTokens.get(inputPort);
            int tokenRate = SDFUtilities.getTokenConsumptionRate(inputPort);
            // Ignore zero rate ports.. they don't limit the number of times
            // we can fire their actors.
            if (tokenRate == 0) {
                continue;
            }
            for (int channel = 0;
                 channel < inputPort.getWidth();
                 channel++) {
                int firings = tokens[channel] / tokenRate;

                // keep track of whether or not this actor can fire again
                // immediately
                if (firings < maximumFirings) {
                    maximumFirings = firings;
                }
            }
        }
        return maximumFirings;
    }

    /** Count the number of input ports in the given actor that must be
     *  fulfilled before the actor can fire.  Ports that are connected
     *  to actors that we are not scheduling right now are assumed to
     *  be fulfilled.  Ports that have more tokens waiting on each of
     *  their channels than their input consumption rate are also
     *  already fulfilled.  All other ports are considered to be
     *  unfulfilled.
     *  @param actor The actor
     *  @param actorList The list of actors that we are scheduling.
     *  @param waitingTokens The Map of tokens currently waiting on all the
     *  input ports.
     *  @return The number of unfulfilled inputs of the given actor.
     *  @exception IllegalActionException If any called method throws it.
     */
    private int _countUnfulfilledInputs(Actor actor,
            LinkedList actorList, Map waitingTokens)
            throws IllegalActionException {
        if (_debugging && VERBOSE) {
            _debug("Counting unfulfilled inputs for " +
                    ((Entity) actor).getFullName());
        }
        int count = 0;
        for (Iterator inputPorts = actor.inputPortList().iterator();
             inputPorts.hasNext();) {
            IOPort inputPort = (IOPort) inputPorts.next();
            if (_debugging && VERBOSE) {
                _debug("Checking input " +
                        inputPort.getFullName());
            }

            int threshold = SDFUtilities.getTokenConsumptionRate(inputPort);
            if (_debugging && VERBOSE) {
                _debug("Threshold = " + threshold);
            }

            int[] tokens = (int [])waitingTokens.get(inputPort);

            // Loop over all the channels.  If one of the channels is
            // not fulfilled, then the port is not fulfilled, so
            // increment the count of unfulfilled ports.
            boolean isFulfilled = true;
            for (int channel = 0;
                 channel < inputPort.getWidth();
                 channel++) {
                if (_debugging && VERBOSE) {
                    _debug("Channel = " + channel);
                    _debug("Waiting Tokens = " + tokens[channel]);
                }

                if (tokens[channel] < threshold) {
                    isFulfilled = false;
                    break;
                }
            }
            if (!isFulfilled) {
                count++;
            }
        }
        return count;
    }

    /** Return the number of firings associated with the given entity.   The
     *  number of firings is stored in the _firingVector map, indexed
     *  by the entity.
     *  @param entity One of the actors we are scheduling.
     */
    private int _getFiringCount(Entity entity) {
        return ((Integer) _firingVector.get(entity)).intValue();
    }

    /** Normalize fractional firing ratios into a firing vector that
     *  corresponds to a single SDF iteration.   Multiply all of the
     *  fractions by the least common multiple (LCM)
     *  of their denominators and by the given
     *  vectorizationFactor.  This factor is normally the
     *  integer value of the vectorizationFactor parameter of the
     *  director.  Also multiply the production and consumption rates
     *  of the external ports of the model by the same amount.
     *
     *  @param vectorizationFactor An integer scaling factor to multiply
     *  the firing vector by.
     *  @param entityToFiringsPerIteration 
     *  Map of firing ratios to be normalized.
     *  @param externalRates Map of token production rates that will
     *  be scaled along with the entityToFiringsPerIteration map.
     *  @exception InternalErrorException If the calculated LCM does not
     *  normalize all of the fractions.
     */
    private void _normalizeFirings(int vectorizationFactor,
            Map entityToFiringsPerIteration, Map externalRates) {
        int lcm = 1;

        if (_debugging && VERBOSE) {
            _debug("Normalizing Firings");
            _debug("vectorizationFactor = " + vectorizationFactor);
        }

        // First find the lcm of all the denominators of all the
        // computed firingsPerIteration.
        for (Iterator unnormalizedFirings = 
                 entityToFiringsPerIteration.values().iterator();
             unnormalizedFirings.hasNext();) {
            Fraction fraction = (Fraction)unnormalizedFirings.next();
            int denominator = fraction.getDenominator();
            lcm = Fraction.lcm(lcm, denominator);
        }

        if (_debugging && VERBOSE) {
            _debug("lcm = " + (new Integer(lcm)).toString());
        }

        Fraction lcmFraction = new Fraction(lcm * vectorizationFactor);

        // Go back through and multiply by the lcm we just found, which
        // should normalize all the fractions to integers.
        for (Iterator actors = entityToFiringsPerIteration.keySet().iterator();
             actors.hasNext();) {
            Object actor = actors.next();
            if (_debugging && VERBOSE) {
                _debug("Normalizing Actor " +
                        ((ComponentEntity) actor).getName());
            }
            Fraction repetitions = 
                (Fraction)entityToFiringsPerIteration.get(actor);
            repetitions = repetitions.multiply(lcmFraction);
            if (repetitions.getDenominator() != 1) {
                throw new InternalErrorException(
                        "Failed to properly perform " +
                        "fraction normalization");
            }
            entityToFiringsPerIteration.put(actor, 
                    new Integer(repetitions.getNumerator()));
        }

        // Go through the ports and normalize the external production
        // and consumption rates by the same factor.
        for (Iterator ports = externalRates.keySet().iterator();
             ports.hasNext();) {
            Object port = ports.next();
            if (_debugging && VERBOSE) {
                _debug("Normalizing Rate for " +
                        ((ComponentPort) port).getName());
            }
            Fraction rate = (Fraction) externalRates.get(port);
            rate = rate.multiply(lcmFraction);
            if (rate.getDenominator() != 1) {
                throw new InternalErrorException(
                        "Failed to properly perform " +
                        "fraction normalization");
            }
            externalRates.put(port, new Integer(rate.getNumerator()));
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
        for (Iterator actors = actorList.iterator();
             actors.hasNext();) {
            ComponentEntity actor = (ComponentEntity)actors.next();
            // Check if this actor has any ports with rate of zero.
            for (Iterator ports = actor.portList().iterator();
                 ports.hasNext();) {
                IOPort port = (IOPort)ports.next();
                if (SDFUtilities._getRate(port) == 0) {
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
     *
     *  @exception NotSchedulableException If the model is not
     *  schedulable.
     *  @exception IllegalActionException If the expression for a
     *  rate parameter is not valid.
     */
    private void _propagatePort(CompositeActor container,
            IOPort currentPort,
            Map entityToFiringsPerIteration,
            Map externalRates,
            LinkedList remainingActors,
            LinkedList pendingActors)
            throws NotSchedulableException, IllegalActionException {

        ComponentEntity currentActor =
            (ComponentEntity) currentPort.getContainer();

        // First check to make sure that this port is not connected to
        // any other output ports on the outside.
        // This results in a non-deterministic merge and is illegal.
        // Do not do this test for output ports where we are propagating
        // inwards instead of outwards.
        if (currentPort.isOutput() && currentPort.getContainer() != container) {
            Iterator connectedPorts =
                currentPort.deepConnectedPortList().iterator();

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
                    throw new NotSchedulableException(
                            currentPort, connectedPort,
                            "Output ports drive the same relation. " +
                            "This is not legal in SDF.");
                } else if (connectedPort.isInput()
                        && connectedPort.getContainer() == container) {
                    throw new NotSchedulableException(
                            currentPort, connectedPort,
                            "Output port drives the same relation " +
                            "as the external input port. " +
                            "This is not legal in SDF.");
                }
            }
        }

        // Next check to make sure that if this port is an external
        // input port, then it does not drive the same relation as some
        // other output port or some other external input port.
        // This results in a non-deterministic merge and is illegal.
        if (currentPort.isInput() && currentPort.getContainer() == container) {
            Iterator connectedPorts =
                currentPort.deepInsidePortList().iterator();
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
                    throw new NotSchedulableException(
                            currentPort, connectedPort,
                            "External input port drive the same relation " +
                            "as an output port. " +
                            "This is not legal in SDF.");
                } else if (connectedPort.isInput()
                        && connectedPort.getContainer() == container) {
                    throw new NotSchedulableException(
                            currentPort, connectedPort,
                            "External input port drives the same relation " +
                            "as another external input port. " +
                            "This is not legal in SDF.");
                }
            }
        }

        // Get the rate of this port.
        int currentRate = SDFUtilities._getRate(currentPort);

        // Port rates of less than zero are not valid.
        if (currentRate < 0) {
            throw new NotSchedulableException(
                    currentPort, "Rate cannot be less than zero.  It was: " +
                    currentRate);
        }

        // Propagate to anything that this port is connected to.  For
        // external ports, this is anything that is connected on the
        // inside.  For ports of actors that are being scheduled, this is
        // anything that is connected on the outside.
        Iterator connectedPorts;
        if (currentPort.getContainer() == container) {
            // Find all the ports that are deeply connected to
            // current port on the inside.
            connectedPorts = currentPort.deepInsidePortList().iterator();
            if (_debugging && VERBOSE) {
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
            IOPort connectedPort = (IOPort)connectedPorts.next();

            ComponentEntity connectedActor =
                (ComponentEntity) connectedPort.getContainer();

            if (_debugging && VERBOSE) {
                _debug("Propagating " + currentPort
                        + " to " + connectedActor.getName());
            }

            int connectedRate = SDFUtilities._getRate(connectedPort);

            // currentFiring is the firing ratio that we've already
            // calculated for currentActor
            Fraction currentFiring =
                (Fraction) entityToFiringsPerIteration.get(currentActor);

            // Compute the firing ratio that we think connected actor
            // should have, based on its connection to currentActor
            Fraction desiredFiring;
            // HDF actors might have zero rates...
            if ((currentRate == 0) && (connectedRate > 0)) {
                // The current port of the current actor has a rate
                // of 0, and the current connected port of the
                // connected actor has a positive integer rate.
                // therefore, we must set the firing count of
                // the connected actor to 0 so that it will
                // not appear in the final static schedule.
                desiredFiring = Fraction.ZERO;
            } else if ((currentRate > 0) && (connectedRate == 0)) {
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
            } else if ((currentRate == 0) && (connectedRate == 0)) {
                // Give the connected actor the same rate as the
                // current actor.
                desiredFiring = currentFiring;
            } else {
                // Both the rates are non zero, so we can just do the
                // regular actor propagation.
                desiredFiring = currentFiring.multiply(
                        new Fraction(currentRate, connectedRate));
            }

            // Now, compare the firing ratio that was computed before
            // with what we just determined.
            // This should be either
            // the firing that we computed previously, or null
            // if the port is an external port, or _minusOne if
            // we have not computed the firing ratio for this actor yet.
            Fraction presentFiring = 
                (Fraction)entityToFiringsPerIteration.get(connectedActor);
            if (_debugging && VERBOSE) {
                _debug("presentFiring of connectedActor "
                        + connectedActor + " = " + presentFiring);
            }
            if (presentFiring == null) {
                // We've gotten out to an external port.
                // Temporarily create the entry in the firing table.
                // This is possibly rather fragile.
                entityToFiringsPerIteration.put(connectedActor, desiredFiring);

                // Compute the external rate for this port.
                Fraction rate = currentFiring.multiply(
                        new Fraction(currentRate, 1));
                Fraction previousRate =
                    (Fraction) externalRates.get(connectedPort);
               
                if (previousRate.equals(Fraction.ZERO)) {
                    externalRates.put(connectedPort, rate);
                } else if (!rate.equals(previousRate)) {
                    // The rates don't match.
                    throw new NotSchedulableException("No solution " +
                            "exists for the balance equations.\n" +
                            "Graph is not " +
                            "consistent under the SDF domain " +
                            "detected on external port " +
                            connectedPort.getFullName());
                }
                _propagatePort(container, connectedPort, 
                        entityToFiringsPerIteration,
                        externalRates, remainingActors, pendingActors);
                entityToFiringsPerIteration.remove(connectedActor);
            } else if (presentFiring.equals(_minusOne)) {
                // So we are propagating here for the first time.
                // Create the entry in the firing table.
                entityToFiringsPerIteration.put(connectedActor, desiredFiring);
                // Remove them from remainingActors.
                remainingActors.remove(connectedActor);
                // and add them to the pendingActors.
                pendingActors.addLast(connectedActor);

            } else if (!presentFiring.equals(desiredFiring)) {
                // So we've already propagated here, but the
                // firingsPerIteration don't match.
                throw new NotSchedulableException("No solution " +
                        "exists for the balance equations.\n" +
                        "Graph is not " +
                        "consistent under the SDF domain " +
                        "detected on external port " +
                        connectedPort.getFullName());
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
     *  @param minimumBufferSize A map from relation to an Integer
     *  representing the minimum size buffer necessary for the computed
     *  schedule.  The map will be populated during the execution of this
     *  method.
     *  @param externalRates Map from external port to an Integer
     *  representing the number of tokens produced or consumed from
     *  that port during the course of an iteration.
     *  @param actorList The actors that need to be scheduled.
     *  @param allActorList All the actors, including those that do
     *  not need to be scheduled.  These actors will still be
     *  initialized, which means we must take their initial tokens
     *  into account when calculating buffer sizes.
     *  @return An instance of the Schedule class, indicating the order
     *  in which actors should fire.
     *  @exception NotSchedulableException If the algorithm encounters an SDF
     *  graph that is not consistent with the firing vector, or detects an
     *  inconsistent internal state, or detects a graph that cannot be
     *  scheduled.
     */
    private Schedule _scheduleConnectedActors(Map minimumBufferSize,
            Map externalRates, LinkedList actorList,
            CompositeActor container, LinkedList allActorList)
            throws NotSchedulableException {

        // A linked list containing all the actors that have no inputs.
        LinkedList readyToScheduleActorList = new LinkedList();
        // A linked list that will contain our new schedule.
        Schedule newSchedule = new Schedule();

        // An association between each actor and the number of firings
        // for that actor that remain to be simulated.
        Map firingsRemainingVector = 
            new TreeMap(new SDFUtilities.NamedObjComparator());

        // Initialized the firingsRemainingVector to the current
        // firing vector.
        firingsRemainingVector.putAll(_firingVector);

        // A list of all that actors that we have not yet completely scheduled.
        LinkedList unscheduledActorList = new LinkedList();
        unscheduledActorList.addAll(actorList);

        // An association between all the input ports in a simulation and an
        // array of the number of tokens waiting on each relation of that port.
        Map waitingTokens = 
            new TreeMap(new SDFUtilities.NamedObjComparator());
        try {
            // Initialize waitingTokens
            // at all the input ports to zero
            for (Iterator actors = allActorList.iterator();
                 actors.hasNext();) {
                Actor actor = (Actor)actors.next();

                for (Iterator inputPorts = actor.inputPortList().iterator();
                     inputPorts.hasNext();) {
                    IOPort inputPort = (IOPort) inputPorts.next();
                    int[] tokenCount = new int[inputPort.getWidth()];
                    for (int channel = 0;
                         channel < tokenCount.length;
                         channel++) {
                        tokenCount[channel] = 0;
                    }
                    waitingTokens.put(inputPort, tokenCount);

                }
            }

            // Initiailize waitingTokens at
            // all output ports of the model to zero.
            for (Iterator outputPorts = container.outputPortList().iterator();
                 outputPorts.hasNext();) {
                IOPort outputPort = (IOPort) outputPorts.next();

                // Compute the width of relations connected to the inside.
                // Wouldn't it be nice if IOPort did this?
                Iterator relations =
                    outputPort.insideRelationList().iterator();
                int portInsideWidth = 0;
                while (relations.hasNext()) {
                    IORelation relation = (IORelation)relations.next();
                    portInsideWidth += relation.getWidth();
                }

                int[] tokenCount = new int[portInsideWidth];
                for (int channel = 0;
                     channel < tokenCount.length;
                     channel++) {
                    tokenCount[channel] = 0;
                }
                waitingTokens.put(outputPort, tokenCount);
            }

            // simulate the creation of initialization tokens (delays).
            for (Iterator actors = allActorList.iterator();
                 actors.hasNext();) {
                Actor actor = (Actor)actors.next();

                Iterator outputPorts = actor.outputPortList().iterator();
                while (outputPorts.hasNext()) {
                    IOPort outputPort = (IOPort) outputPorts.next();
                    int count = 
                        SDFUtilities.getTokenInitProduction(outputPort);
                    if (_debugging && VERBOSE) {
                        _debug("Simulating " + count
                                + " tokens created on " + outputPort);
                    }
                    if (count > 0) {
                        _simulateTokensCreated(outputPort,
                                count,
                                allActorList,
                                readyToScheduleActorList,
                                waitingTokens,
                                minimumBufferSize);
                    }
                }
            }

            // Simulate a number of tokens initially present on each
            // external input port.
            for (Iterator inputPorts = container.inputPortList().iterator();
                 inputPorts.hasNext();) {
                IOPort port = (IOPort)inputPorts.next();
                int count = ((Integer)externalRates.get(port)).intValue();
                _simulateExternalInputs(port,
                        count,
                        actorList,
                        waitingTokens,
                        minimumBufferSize);
            }

            // Fill readyToScheduleActorList with all the actors that have
            // no unfulfilled input ports, and are thus ready to fire.
            for (Iterator actors = actorList.iterator();
                 actors.hasNext();) {
                Actor actor = (Actor)actors.next();

                int inputCount = _countUnfulfilledInputs(actor, actorList,
                        waitingTokens);
                if (inputCount == 0) {
                    readyToScheduleActorList.addFirst((ComponentEntity) actor);
                }

                if (_debugging && VERBOSE) {
                    _debug("Actor " + ((ComponentEntity) actor).getName() +
                            " has " + inputCount + " unfulfilledInputs.");
                }
            }

            // While we have actors left, pick one that is ready and fire it.
            while (readyToScheduleActorList.size() > 0) {
                if (_debugging && VERBOSE) {
                    _debug("\nwaitingTokens: ");
                    for (Iterator ports = waitingTokens.keySet().iterator();
                         ports.hasNext();) {
                        IOPort port = (IOPort)ports.next();
                        int tokenCount[] = (int[])waitingTokens.get(port);
                        _debug("Port " + port.getFullName());
                        _debug("Number of channels = " +
                                tokenCount.length);
                        for (int channel = 0;
                             channel < tokenCount.length;
                             channel++) {
                            _debug("Channel " + channel + " has " +
                                    tokenCount[channel] + " tokens.");
                        }
                    }
                    _debug("Actors that can be scheduled:");
                    for (Iterator readyActors =
                             readyToScheduleActorList.iterator();
                         readyActors.hasNext();) {
                        Entity readyActor = (Entity)readyActors.next();
                        _debug(readyActor.getFullName());
                    }
                    _debug("Actors with firings left:");
                    for (Iterator remainingActors =
                             unscheduledActorList.iterator();
                         remainingActors.hasNext();) {
                        Entity remainingActor = (Entity)remainingActors.next();
                        _debug(remainingActor.getFullName());
                    }
                }

                // Pick an actor that is ready to fire.
                ComponentEntity currentActor
                    = (ComponentEntity) readyToScheduleActorList.getFirst();

                // Remove it from the list of actors we are waiting to fire.
                while (readyToScheduleActorList.remove(currentActor));

                // Determine the number of times currentActor can fire.
                int numberOfFirings =
                    _computeMaximumFirings(currentActor, waitingTokens);

                // We should never schedule something more than the number
                // of times expected by the balance equations.  This might
                // happen because we assume an infinite number of tokens
                // are waiting on external ports.
                int firingsRemaining =
                    ((Integer) firingsRemainingVector.get(currentActor)).
                    intValue();
                if (numberOfFirings > firingsRemaining) {
                    numberOfFirings = firingsRemaining;
                }

                if (_debugging && VERBOSE) {
                    _debug("Scheduling actor " + currentActor.getName() +
                            " " + numberOfFirings + " times.");
                }

                // Update the firingsRemainingVector for this actor.
                firingsRemaining -= numberOfFirings;
                firingsRemainingVector.put(currentActor,
                        new Integer(firingsRemaining));

                if (_debugging && VERBOSE) {
                    _debug(currentActor.getName() + " should fire " +
                            firingsRemaining + " more times.");
                }

                // Simulate the tokens that are consumed by the actors
                // input ports.
                _simulateInputConsumption(currentActor,
                        waitingTokens, numberOfFirings);

                // Add it to the schedule numberOfFirings times.
                Firing firing = new Firing();
                firing.setActor((Actor)currentActor);
                firing.setIterationCount(numberOfFirings);
                newSchedule.add(firing);

                // Get all its outputPorts
                // and simulate the proper production of tokens.
                for (Iterator outputPorts =
                         ((Actor) currentActor).outputPortList().iterator();
                     outputPorts.hasNext();) {
                    IOPort outputPort = (IOPort) outputPorts.next();

                    int count = 
                        SDFUtilities.getTokenProductionRate(outputPort);

                    _simulateTokensCreated(outputPort,
                            count * numberOfFirings,
                            unscheduledActorList,
                            readyToScheduleActorList,
                            waitingTokens,
                            minimumBufferSize);
                }

                // Figure out what to do with the actor, now that it has been
                // scheduled.
                if (firingsRemaining < 0) {
                    // If we screwed up somewhere, and fired this more
                    // times than we thought we should have
                    // then throw an exception.
                    // This should never happen.
                    throw new InternalErrorException("Balance Equation " +
                            "solution does not agree with " +
                            "scheduling algorithm!");
                } else {
                    if (firingsRemaining == 0) {
                        // If we've fired this actor all the
                        // times that it should, then
                        // we get rid of it entirely.
                        if (_debugging && VERBOSE) {
                            _debug("Actor = " + currentActor +
                                    " is done firing.");
                        }

                        // Remove the actor from the unscheduledActorList
                        // since we don't need to fire it any more.
                        while (unscheduledActorList.remove(currentActor));

                        if (_debugging && VERBOSE) {
                            _debug("Remaining actors:");
                            for (Iterator readyActors =
                                     readyToScheduleActorList.iterator();
                                 readyActors.hasNext();) {
                                Entity entity = (Entity)readyActors.next();
                                _debug(entity.getFullName());
                            }
                        }
                    } else {
                        // Otherwise the actor still has firings left.
                        // Count the number of unfulfilled inputs.
                        int inputCount =
                            _countUnfulfilledInputs((Actor)currentActor,
                                    unscheduledActorList,
                                    waitingTokens);
                        // We've already removed currentActor from
                        // readyToSchedule actors, and presumably
                        // fired it until it can be fired no more.
                        // This check is here for robustness...
                        // if the actor can still be scheduled
                        // i.e. all its inputs are satisfied, and it
                        // appears in the unscheduled actors list
                        // then put it at the END of readyToScheduleActorList.

                        if (inputCount <= 0 &&
                                unscheduledActorList.contains(currentActor)) {
                            readyToScheduleActorList.addFirst(currentActor);
                        }
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
        } finally {
            if (_debugging && VERBOSE) {
                _debug("finishing loop");
            }
        }

        // If there are any actors left when we're done, then report the
        // error.
        if (unscheduledActorList.size() > 0) {
            String string =
                new String("Actors remain that cannot be scheduled!\n");

            string += "Scheduled actors:\n";
            List scheduledActorList = new LinkedList();
            scheduledActorList.addAll(actorList);
            scheduledActorList.removeAll(unscheduledActorList);
            for (Iterator actors = scheduledActorList.iterator();
                 actors.hasNext();) {
                Entity entity = (Entity)actors.next();
                string += entity.getFullName() + "\n";
            }

            string += "Unscheduled actors:\n";
            for (Iterator actors = unscheduledActorList.iterator();
                 actors.hasNext();) {
                Entity entity = (Entity)actors.next();
                string += entity.getFullName() + "\n";
            }

            throw new NotSchedulableException(string);
        }

        if (_debugging) {
            _debug("Schedule is:");
            _debug(newSchedule.toString());
        }

        return newSchedule;
    }

    /** Set the firing vector, which is a map associating an actor
     *  with the number of times that it will fire during an SDF iteration.
     *  Every actor that this scheduler is responsible for should have an
     *  entry, even if it is -1 indicating that the actor has not yet had
     *  its firings determined.
     *
     *  @param newFiringVector A map from ComponentEntity to Integer.
     */
    private void _setFiringVector(Map newFiringVector) {
        _firingVector = newFiringVector;
        _firingVectorValid = true;
    }

    /**
     * Simulate the consumption of tokens from the given external input
     * port.  This sets the values in the waiting tokens map to be a
     * very large number, since we assume that each external input port
     * is automatically fulfilled.
     */
    private void _simulateExternalInputs(IOPort port,
            int count,
            LinkedList actorList,
            Map waitingTokens,
            Map minimumBufferSize)
            throws IllegalActionException {

        Receiver[][] receivers = port.deepGetReceivers();

        if (_debugging && VERBOSE) {
            _debug("Simulating external input tokens from "
                    + port.getFullName());
            _debug("inside channels = " + receivers.length);
        }

        int sourceChannel = 0;
        Iterator relations = port.insideRelationList().iterator();
        while (relations.hasNext()) {
            IORelation relation = (IORelation) relations.next();
            // A null link (supported since indexed links) might
            // yield a null relation here.
            // FIXME: tests for null links.
            if (relation == null) {
                continue;
            }

            // The bufferSize for the current relation.  This is
            // put back into the buffer at the end after (possibly)
            // being updated.
            Integer bufferSize = (Integer) minimumBufferSize.get(relation);

            int width = relation.getWidth();
            // loop through all of the channels of that relation.
            for (int i = 0; i < width; i++, sourceChannel++) {
                if (_debugging && VERBOSE) {
                    _debug("destination receivers for relation "
                            + relation.getName() + " channel "
                            + sourceChannel + ": "
                            + receivers[sourceChannel].length);
                }

                for (int destinationIndex = 0;
                     destinationIndex < receivers[sourceChannel].length;
                     destinationIndex++) {
                    IOPort connectedPort = (IOPort)
                        receivers[sourceChannel][destinationIndex].
                        getContainer();
                    ComponentEntity connectedActor =
                        (ComponentEntity) connectedPort.getContainer();
                    // Only proceed if the connected actor is something we are
                    // scheduling.  The most notable time when this will not be
                    // true is when a connection is made to the
                    // inside of an opaque port.
                    if (actorList.contains(connectedActor)) {
                        int destinationChannel = _getChannel(connectedPort,
                                receivers[sourceChannel][destinationIndex]);
                        int[] tokens = (int[]) waitingTokens.get(connectedPort);
                        tokens[destinationChannel] = count;

                        // Update the buffer size, if necessary.
                        // if bufferSize is null, then ignore, since we don't
                        // care about that relation.
                        if (bufferSize != null &&
                                tokens[destinationChannel] >
                                bufferSize.intValue()) {
                            bufferSize =
                                new Integer(tokens[destinationChannel]);
                        }

                        if (_debugging && VERBOSE) {
                            _debug("Channel " + destinationChannel
                                    + " of " + connectedPort.getName());
                        }
                    }
                }
            }
            // update the map of buffer sizes.
            minimumBufferSize.put(relation, bufferSize);
        }
    }

    /** Simulate the consumption of tokens by the actor during execution of
     *  the given number of firings.
     *  The entries in the waitingTokens map will be modified
     *  to reflect the number of
     *  tokens still waiting after the actor has consumed the minimum required
     *  number of tokens for all firings.
     *  Also determine if enough tokens still remain at the inputs of the actor
     *  for it to fire again immediately.
     *
     *  @param currentActor The actor that is being simulated.
     *  @param waitingTokens A map between each input IOPort and the number of
     *  tokens in the queue for that port.  This will be updated to reflect
     *  the new number of waiting tokens.
     *  @param firingCount The number of firings to simulate.
     *  @return true If the actor can fire again right away
     *  after it has consumed tokens.
     *  @exception IllegalActionException If the rate parameters are invalid.
     */
    private boolean _simulateInputConsumption(ComponentEntity currentActor,
            Map waitingTokens, int firingCount) throws IllegalActionException {
        boolean stillReadyToSchedule = true;

        // Update the number of tokens waiting on the actor's input ports.
        Iterator inputPorts =
            ((Actor) currentActor).inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            int[] tokens = (int []) waitingTokens.get(inputPort);
            int tokenRate = 
                SDFUtilities.getTokenConsumptionRate(inputPort);
            for (int channel = 0;
                 channel < inputPort.getWidth();
                 channel++) {
                tokens[channel] -= (tokenRate * firingCount);

                // keep track of whether or not this actor can fire again
                // immediately
                if (tokens[channel] < tokenRate) {
                    stillReadyToSchedule = false;
                }
            }
        }
        return stillReadyToSchedule;
    }

    /**
     * Simulate the creation of tokens by the given output port when
     * its actor fires.  If any actors that receive tokens are then ready to
     * fire, given that only actors in the actor list are being scheduled, then
     * add those actors to the list of actors that are ready to schedule.
     * update the waiting tokens map with the tokens available on each
     * channel of each port.
     * @param outputPort The port that is creating the tokens.
     * @param createdTokens The number of tokens to create.
     * @param actorList The list of actors that are being scheduled.
     * @param readyToScheduleActorList The list of actors that are ready
     * to be scheduled.  This will be updated if any actors that receive
     * tokens from outputPort are now ready to fire.
     * @param waitingTokens A map from port to an array of integers
     * representing the number of tokens in each receiver of an input port.
     * This will be updated according to the tokens that are created.
     */
    private void _simulateTokensCreated(IOPort outputPort,
            int createdTokens,
            LinkedList actorList,
            LinkedList readyToScheduleActorList,
            Map waitingTokens,
            Map minimumBufferSize)
            throws IllegalActionException {

        Receiver[][] receivers = outputPort.getRemoteReceivers();

        if (_debugging && VERBOSE) {
            _debug("Creating " + createdTokens + " tokens on "
                    + outputPort.getFullName());
            _debug("source channels = " + receivers.length);
            _debug("width = " + outputPort.getWidth());
        }

        int sourceChannel = 0;
        Iterator relations = outputPort.linkedRelationList().iterator();
        while (relations.hasNext()) {
            IORelation relation = (IORelation) relations.next();
            // A null link (supported since indexed links) might
            // yield a null relation here.
            // FIXME: tests for null links.
            if (relation == null) {
                continue;
            }

            // The bufferSize for the current relation.  This is
            // put back into the buffer at the end after (possibly)
            // being updated.
            Integer bufferSize = (Integer) minimumBufferSize.get(relation);

            int width = relation.getWidth();
            // loop through all of the channels of that relation.
            for (int i = 0; i < width; i++, sourceChannel++) {
                if (receivers[sourceChannel] == null) {
                    // There is nothing connected on the other side of
                    // transparent hierarchy... just ignore.
                    continue;
                }
                
                if (_debugging && VERBOSE) {
                    _debug("destination receivers for relation "
                            + relation.getName() + " channel "
                            + sourceChannel + ": "
                            + receivers[sourceChannel].length);
                }

                for (int destinationIndex = 0;
                     destinationIndex < receivers[sourceChannel].length;
                     destinationIndex++) {
                    IOPort connectedPort = (IOPort)
                        receivers[sourceChannel][destinationIndex].
                        getContainer();
                    ComponentEntity connectedActor =
                        (ComponentEntity) connectedPort.getContainer();

                    // The channel of the destination port that is
                    // connected to sourceChannel.
                    int destinationChannel = _getChannel(connectedPort,
                            receivers[sourceChannel][destinationIndex]);

                    // Increment the number of waiting tokens.
                    int[] tokens = (int[]) waitingTokens.get(connectedPort);
                    tokens[destinationChannel] += createdTokens;

                    // Update the buffer size, if necessary.
                    // if bufferSize is null, then ignore, since we don't
                    // care about that relation.
                    if (bufferSize != null &&
                            tokens[destinationChannel] >
                            bufferSize.intValue()) {
                        bufferSize = new Integer(tokens[destinationChannel]);
                    }
                    if (_debugging && VERBOSE) {
                        _debug("Channel "
                                + destinationChannel
                                + " of "
                                + connectedPort.getName());
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
                                (Actor)connectedActor,
                                actorList,
                                waitingTokens);
                        int firingsRemaining = _getFiringCount(connectedActor);
                        // If so, then add it to the proper list.
                        // Note that the
                        // actor may appear more than once.
                        // This is OK, since we
                        // remove all of the appearances from
                        // the list when the
                        // actor is actually scheduled.
                        if ((inputCount < 1) && (firingsRemaining > 0)) {
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
            // update the map of buffer sizes.
            minimumBufferSize.put(relation, bufferSize);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The firing vector.  A map from actor to an integer representing the
    // number of times the actor will fire.
    private Map _firingVector;
    // True if the firing vector contains valid information.
    private boolean _firingVectorValid;
    // A fraction equal to -1.  Used in several places to indicate an
    // actor for which we have not determined the number of times it will
    // fire.
    private Fraction _minusOne = new Fraction(-1);
    
    private Map _externalRates = 
    new TreeMap(new SDFUtilities.NamedObjComparator());

    private List _rateVariables = new LinkedList();
}
