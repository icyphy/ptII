/* A scheduler for fixed point directors.

 Copyright (c) 2006-2007 The Regents of the University of California.
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
package ptolemy.actor.sched;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.FunctionDependency;
import ptolemy.actor.util.FunctionDependencyOfCompositeActor;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;

//////////////////////////////////////////////////////////////////////////
//// FixedPointScheduler

/**
 A scheduler for the FixedPointDirector.  This scheduler constructs
 a static schedule for a model by performing a topological sort on the port
 dependency graph of that model.  The schedule may mention an actor more
 than once if the dependencies require it.
 <p>
 FIXME: This scheduler is currently very inefficient.  An actor will
 be mentioned in the schedule as many times as it has ports. E.g., if
 it has three ports, it will appear in the schedule three times.
 Some of these appearances in the schedule will be redundant (e.g.,
 a firing will occur after another firing even though
 there is no new information at the input ports), and
 some could be postponed and consolidated with a later appearance.
 Conjecture: The optimal schedule fires every actor as late
 as possible, i.e. not before the firing is essential for the
 schedule to proceed. This seems to be what the DE run-time
 scheduler does.  Another way to state this is that the actor
 should not be fired until there is no other actor firing possible
 that could provide it with new information. Note that a firing
 of an actor can provide new information to itself, if there
 is a self loop from one of its output ports to an input port.

 @author Haiyang Zheng and Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (reviewModerator)
 */
public class FixedPointScheduler extends Scheduler {
    /** Construct a scheduler in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this scheduler.
     *  @exception IllegalActionException If the scheduler is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public FixedPointScheduler(FixedPointDirector container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the schedule.
     *  This method should not be called directly, but rather the getSchedule()
     *  method (which is defined in the superclass) will call it when the
     *  schedule is invalid.  This method is not synchronized on the workspace.
     *
     *  @return A schedule.
     *  @exception NotSchedulableException If the model is not
     *   schedulable because of a dependency loop, or because there
     *   is no containing director, or because the containing director
     *   has no container.
     */
    protected Schedule _getSchedule() throws NotSchedulableException {
        StaticSchedulingDirector director = (StaticSchedulingDirector) getContainer();

        if (director == null) {
            throw new NotSchedulableException(this, "No director.  ");
        }

        CompositeActor compositeActor = (CompositeActor) (director
                .getContainer());

        if (compositeActor == null) {
            throw new NotSchedulableException(this, "No container.");
        }

        FunctionDependencyOfCompositeActor functionDependency = (FunctionDependencyOfCompositeActor) compositeActor
                .getFunctionDependency();

        Object[] cycleNodes = functionDependency.getCycleNodes();

        if (cycleNodes.length != 0) {
            StringBuffer names = new StringBuffer();

            for (int i = 0; i < cycleNodes.length; i++) {
                if (cycleNodes[i] instanceof Nameable) {
                    if (i > 0) {
                        names.append(", ");
                    }
                    names.append(((Nameable) cycleNodes[i]).getContainer()
                            .getFullName());
                }
            }
            throw new NotSchedulableException(director,
                    "There are dependency loops in the model:"
                            + names.toString() + "\n"
                            + " The results may contain unknowns.  This "
                            + "scheduler cannot handle this model.");
        }

        DirectedAcyclicGraph dependencyGraph = functionDependency
                .getDetailedDependencyGraph().toDirectedAcyclicGraph();

        if (_debugging) {
            _debug("## dependency graph is:" + dependencyGraph.toString());
        }

        boolean useOptimizedSchedule = false;
        if (useOptimizedSchedule) {
            return _constructSchedule(dependencyGraph);
        } else {
            return _constructNaiveSchedule(dependencyGraph);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Based on the depths of IO ports, calculate the depths of actors.
     *  The results are cached in a hashtable _actorToDepth.
     *  Update the depths of existing events in the event queue.
     */
    private void _computeActorDepth() {
        CompositeActor container = (CompositeActor) getContainer();
        LinkedList actors = (LinkedList) container.deepEntityList();
        // Add container.
        actors.add(container);
        int numberOfActors = actors.size();
        _actorToDepth = new Hashtable(numberOfActors);

        Iterator actorsIterator = actors.iterator();

        // The depth of an actor starts with a negative number.
        int defaultActorDepth = -numberOfActors;

        while (actorsIterator.hasNext()) {
            Actor actor = (Actor) actorsIterator.next();

            // Calculate the depth of the given actor, which is the
            // smallest depth of all the input and output ports.
            // Why?
            // Here is the example: A model with a feedback loop, which
            // contains a non-zero TimedDelay actor. When the TimedDelay actor
            // requests a refiring, the depth of the event should have the
            // depth of its output.
            // The reason to include the depths of input ports for calculation
            // is to reduce unnecessary number of firings. In particular,
            // if an actor receives a trigger event that has the same tag as
            // one of its pure events, one firing is sufficient.
            int depth = -1;
            Iterator inputs = actor.inputPortList().iterator();
            while (inputs.hasNext()) {
                IOPort inputPort = (IOPort) inputs.next();
                int inputDepth = ((Integer) _portToDepth.get(inputPort))
                        .intValue();
                if ((inputDepth < depth) || (depth == -1)) {
                    depth = inputDepth;
                }
            }
            Iterator outputs = actor.outputPortList().iterator();
            while (outputs.hasNext()) {
                IOPort outputPort = (IOPort) outputs.next();
                int outputDepth = ((Integer) _portToDepth.get(outputPort))
                        .intValue();
                if ((outputDepth < depth) || (depth == -1)) {
                    depth = outputDepth;
                }
            }

            // Note that if an actor has no ports, the defaultActorDepth,
            // which is a negative number, will be used such that each
            // actor has a unique depth.
            if (depth == -1) {
                depth = defaultActorDepth;
            }

            _actorToDepth.put(actor, Integer.valueOf(depth));
            // Increment the default depth value for the next actor.
            defaultActorDepth++;
        }
    }

    /** Perform a topological sort on the directed graph and use the result
     *  to set the depth for each IO port. A new Hashtable is created each
     *  time this method is called.
     *  @param dependencyGraph A graph contains the function dependency
     *   information.
     */
    private void _computePortDepth(DirectedAcyclicGraph dependencyGraph) {
        // local variable to turn on/off debugging messages
        boolean verbose = false;

        // The following topologicalSort can be smarter.
        // In particular, the dependency between ports belonging
        // to the same actor may be considered.
        Object[] sort = dependencyGraph.topologicalSort();
        int numberOfPorts = sort.length;

        if (_debugging && verbose) {
            _debug("## Result of topological sort (highest depth to lowest):");
        }

        // Allocate a new hash table with the size equal to the
        // number of IO ports sorted.
        _portToDepth = new Hashtable(numberOfPorts);

        LinkedList ports = new LinkedList();

        // Assign depths to ports based on the topological sorting result.
        // Each port is assigned a depth.
        for (int i = 0; i < numberOfPorts; i++) {
            IOPort ioPort = (IOPort) sort[i];
            ports.add(ioPort);
            int depth = i;
            Actor portContainer = (Actor) ioPort.getContainer();
            // The output ports of the composite actor that contains
            // this director are set to the highest depth
            // (the lowest priority) and the input ports are given
            // the highest priorities because they are sources.
            if (portContainer.equals(getContainer())) {
                if (ioPort.isOutput()) {
                    depth += numberOfPorts;
                } else {
                    depth -= numberOfPorts;
                }
            }

            // Insert the hashtable entry.
            _portToDepth.put(ioPort, Integer.valueOf(depth));
            if (_debugging && verbose) {
                _debug(((Nameable) ioPort).getFullName(), "depth: " + depth);
            }
        }

        if (_debugging && verbose) {
            _debug("## adjusting port depths based "
                    + "on the strictness constraints.");
        }

        LinkedList actorsWithPortDepthsAdjusted = new LinkedList();

        // Adjust the port depths based on the strictness constraints.
        // The ruls is that if an output depends on several inputs directly,
        // all inputs must have the same depth, the biggest one.
        for (int i = sort.length - 1; i >= 0; i--) {
            IOPort ioPort = (IOPort) sort[i];
            // Get the container actor of the current output port.
            Actor portContainer = (Actor) ioPort.getContainer();
            // we skip the ports of the container and their depths are handled
            // by the upper level executive director of this container.
            if (portContainer.equals(getContainer())) {
                continue;
            }
            // Normally, we adjust port depths based on output ports.
            // However, if this input port belongs to a sink actor, and
            // the sink actor has more than one input ports, adjust the depths
            // of all the input ports to their maximum value.
            // For exmaple, the XYPlotter in the WirelessSoundDetection demo.
            // By default, all composite actors are non-strict. However, if
            // a composite actor declares its strictness with an attribute,
            // we adjust its input ports depths to their maximum. One example
            // is the ModalModel.
            // A third case is that if a composite actor has some of its input
            // ports as parameter ports and the others as reguler IO ports,
            // we need to adjust the depths of paramter ports also.
            // The TimedSinewave (with SDF implementation) is an example.
            // Since this actor is supposed to be a strict actor, we need to
            // add a strictness marker such that the depths of all its inputs
            // are adjusted to their maximum value.
            // For non-strict composite actors, one solution is to iterate
            // each output port and find all the parameter ports that affect
            // that output port. Note that a parameter may depend on another
            // parameter at the same level of hierarchy, which makes the
            // analysis harder. One reference will be the context analysis by
            // Steve.
            // I prefer to leave the parameter analysis to be independent of the
            // function dependency analysis. 02/2005 hyzheng

            // Find the set of the ports that need depths adjusted.
            List inputPorts;
            if (ioPort.isInput()) {
                boolean depthNeedsAdjusted = false;
                int numberOfOutputPorts = portContainer.outputPortList().size();
                // If an actor has no output ports, adjustment is necessary.
                if (numberOfOutputPorts == 0) {
                    depthNeedsAdjusted = true;
                }
                // If the actor declares itself as a strict actor,
                // adjustment is necessary.
                if (portContainer.isStrict()) {
                    depthNeedsAdjusted = true;
                }
                if (depthNeedsAdjusted) {
                    // If depth needs adjusted:
                    inputPorts = portContainer.inputPortList();
                    if (inputPorts.size() <= 1) {
                        // If the sink actor has only one input port, there is
                        // no need to adjust its depth.
                        continue;
                    }
                    // If the depths of the input ports of this acotr
                    // have been adjusted, skip it. Otherwise, add it into the
                    // list actorsWithPortDepthsAdjusted.
                    if (actorsWithPortDepthsAdjusted.contains(portContainer)) {
                        continue;
                    } else {
                        actorsWithPortDepthsAdjusted.add(portContainer);
                    }
                } else {
                    continue;
                }
            } else {
                // The ioPort is an output port.
                // Get the function dependency of the container actor
                FunctionDependency functionDependency = portContainer
                        .getFunctionDependency();
                inputPorts = functionDependency
                        .getInputPortsDependentOn(ioPort);
            }
            // Iterate all input ports the current output depends on,
            // find their maximum depth.
            Iterator inputsIterator = inputPorts.iterator();
            int maximumPortDepth = -1;
            while (inputsIterator.hasNext()) {
                Object object = inputsIterator.next();
                IOPort input = (IOPort) object;
                int inputPortDepth = ports.indexOf(input);
                if (maximumPortDepth < inputPortDepth) {
                    maximumPortDepth = inputPortDepth;
                }
            }
            // Set the depths of the input ports to the maximum one.
            inputsIterator = inputPorts.iterator();
            while (inputsIterator.hasNext()) {
                IOPort input = (IOPort) inputsIterator.next();
                if (_debugging && verbose) {
                    _debug(((Nameable) input).getFullName(),
                            "depth is adjusted to: " + maximumPortDepth);
                }
                // Insert the port the hashtable entry.
                _portToDepth.put(input, Integer.valueOf(maximumPortDepth));
            }
        }
        if (_debugging) {
            _debug("## End of topological sort of ports.");
        }
    }

    /** Construct a naive schedule based on the given dependency graph that
     *  contains the function dependency information.
     *  @param dependencyGraph A graph contains the function dependency
     *   information.
     *  @return A naive schedule generated based on the dependency graph.
     */
    private Schedule _constructNaiveSchedule(
            DirectedAcyclicGraph dependencyGraph) {
        Schedule schedule = new Schedule();
        Object[] sort = dependencyGraph.topologicalSort();
        if (_debugging) {
            _debug("## Schedule generated:");
        }
        Actor actor = null;
        Actor previouslySeenActor = null;
        for (int i = 0; i < sort.length; i++) {
            IOPort ioPort = (IOPort) sort[i];
            // If this ioPort is an input but has no connections, ignore it.
            // NOTE: In case we ever support ports that are both input
            // and output, we do want to list a port if it is an output,
            // regardless of whether it is also an unconnected input.
            if (ioPort.isInput() && (ioPort.sourcePortList().size() == 0)) {
                continue;
            }
            actor = (Actor) ioPort.getContainer();
            if (previouslySeenActor == actor) {
                if (ioPort.isOutput()) {
                    continue;
                }
            } else {
                previouslySeenActor = actor;
            }
            // If the actor is the container of this director,
            // meaning this director is not at the top level,
            // then skip this actor. The container of the director
            // should not be listed in the schedule.
            if (actor == getContainer().getContainer()) {
                continue;
            }
            Firing firing = new Firing(actor);
            schedule.add(firing);
            if (_debugging) {
                _debug(actor.getFullName() + " scheduled at position: " + i);
            }
        }
        if (_debugging) {
            _debug("## End of schedule.");
        }
        // Set the schedule to be valid.
        setValid(true);
        return schedule;
    }

    /** Construct a schedule based on the given dependency graph that
     *  contains the function dependency information.
     *  @param dependencyGraph A graph contains the function dependency
     *   information.
     *  @return A schedule generated based on the dependency graph.
     */
    private Schedule _constructSchedule(DirectedAcyclicGraph dependencyGraph) {
        _computePortDepth(dependencyGraph);
        _computeActorDepth();

        // FIXME: not much done here... optimization is needed.

        Schedule schedule = new Schedule();
        Object[] sort = dependencyGraph.topologicalSort();
        if (_debugging) {
            _debug("## Schedule generated:");
        }
        Actor actor = null;
        for (int i = 0; i < sort.length; i++) {
            IOPort ioPort = (IOPort) sort[i];

            // If this ioPort is an input but has no connections, ignore it.
            // NOTE: In case we ever support ports that are both input
            // and output, we do want to list a port if it is an output,
            // regardless of whether it is also an unconnected input.
            if (!ioPort.isOutput() && (ioPort.sourcePortList().size() == 0)) {
                continue;
            }

            actor = (Actor) ioPort.getContainer();

            // If the actor is the container of this director,
            // meaning this director is not at the top level,
            // then skip this actor. The container of the director
            // should not be listed in the schedule.
            if (actor == getContainer().getContainer()) {
                continue;
            }

            Firing firing = new Firing(actor);
            schedule.add(firing);

            if (_debugging) {
                _debug(actor.getFullName() + " scheduled at position: " + i);
            }
        }
        if (_debugging) {
            _debug("## End of schedule.");
        }
        // Set the schedule to be valid.
        setValid(true);
        return schedule;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** A hashtable that caches the depths of actors. */
    private Hashtable _actorToDepth = null;

    /** A hashtable that caches the depths of ports. */
    private Hashtable _portToDepth = null;
}
