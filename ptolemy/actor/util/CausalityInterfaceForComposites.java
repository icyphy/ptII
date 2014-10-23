/* Interface representing a dependency between ports of a composite actor.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.actor.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// CausalityInterfaceForComposites

/**
 This class elaborates its base class by providing an algorithm for inferring
 the causality interface of a composite actor from the causality interfaces
 of its component actors and their interconnection topology.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class CausalityInterfaceForComposites extends DefaultCausalityInterface {

    /** Construct a causality interface for the specified actor.
     *  @param actor The actor for which this is a causality interface.
     *   This is required to be an instance of CompositeEntity.
     *  @param defaultDependency The default dependency of an output
     *   port on an input port.
     *  @exception IllegalArgumentException If the actor parameter is not
     *  an instance of CompositeEntity.
     */
    public CausalityInterfaceForComposites(Actor actor,
            Dependency defaultDependency) throws IllegalArgumentException {
        super(actor, defaultDependency);
        if (!(actor instanceof CompositeEntity)) {
            throw new IllegalArgumentException("Cannot create an instance of "
                    + "CausalityInterfaceForComposites for "
                    + actor.getFullName() + ", which is not a CompositeEntity.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check the associated composite actor for causality cycles.
     *  If a cycle is found, throw an exception. This method has as
     *  a side effect computing the depths of actors and ports
     *  within the composite, so subsequent queries for those depths
     *  will be low cost.
     *  @exception IllegalActionException If a cycle is found.
     */
    public void checkForCycles() throws IllegalActionException {
        _computeActorDepth();
    }

    /** Return a collection of the ports in the associated actor that depend on
     *  or are depended on by the specified port. A port X depends
     *  on a port Y if X is an output and Y is an input and
     *  getDependency(X,Y) returns oTimesIdentity() of the default
     *  dependency specified in the constructor.
     *  <p>
     *  This class presumes (but does not check) that the
     *  argument is a port contained by the associated actor.
     *  If the actor is an input, then it returns a collection of
     *  all the outputs. If the actor is output, then it returns
     *  a collection of all the inputs.
     *  <p>
     *  Derived classes may override this, but they may need to
     *  also override {@link #getDependency(IOPort, IOPort)}
     *  and {@link #equivalentPorts(IOPort)} to be consistent.
     *  @param port The port to find the dependents of.
     *  @return a collection of ports that depend on or are depended on
     *  by the specified port.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public Collection<IOPort> dependentPorts(IOPort port)
            throws IllegalActionException {
        // FIXME: This does not support ports that are both input and output.
        // Should it?
        HashSet<IOPort> result = new HashSet<IOPort>();
        if (port.isOutput()) {
            List<IOPort> inputs = _actor.inputPortList();
            if (inputs.size() != 0) {
                // Make sure _dependency is computed.
                getDependency(inputs.get(0), port);
                Map<IOPort, Dependency> map = _reverseDependencies.get(port);
                if (map != null) {
                    result.addAll(map.keySet());
                }
            }
        } else {
            List<IOPort> outputs = _actor.outputPortList();
            if (outputs.size() != 0) {
                // Make sure _dependency is computed.
                getDependency(port, outputs.get(0));
                Map<IOPort, Dependency> map = _forwardDependencies.get(port);
                if (map != null) {
                    result.addAll(map.keySet());
                }
            }
        }
        return result;
    }

    /** Return a string that describes the depths of actors and their ports.
     *  @return s string that describes the depths of actors and their ports.
     *  @exception IllegalActionException If there is a causality loop.
     *  @see #getDepthOfActor(Actor)
     *  @see #getDepthOfPort(IOPort)
     */
    public String describeDepths() throws IllegalActionException {
        _computeActorDepth();
        StringBuffer result = new StringBuffer();
        List<Actor> actors = ((CompositeEntity) _actor).deepEntityList();
        for (Actor actor : actors) {
            result.append(actor.getFullName());
            result.append(": ");
            result.append(_actorToDepth.get(actor));
            result.append("\n");
            List<IOPort> ports = ((Entity) actor).portList();
            for (IOPort port : ports) {
                result.append("   ");
                result.append(port.getName());
                result.append(": ");
                result.append(_portToDepth.get(port));
                result.append("\n");
            }
        }
        return result.toString();
    }

    /** Return a set of the input ports in this actor that are
     *  in an equivalence class with the specified input.
     *  The returned result includes the specified input port.
     *  <p>
     *  An equivalence class is defined as follows.
     *  If input ports X and Y each have a dependency equal to
     *  oTimesIdentity() on any common port
     *  or on two equivalent ports
     *  or on the state of the associated actor, then they
     *  are in an equivalence class. That is,
     *  there is a causal dependency. They are also in
     *  the same equivalence class if there is a port Z
     *  in an equivalence class with X and in an equivalence
     *  class with Y. Moreover, if the actor has any instance
     *  of ParameterPort among its input ports, then all input
     *  ports are in an equivalence class.
     *  Otherwise, they are not in the same
     *  equivalence class.
     *  In this base class, we assume the actor has no
     *  state and return the equivalence classes determined
     *  only by the common dependence of output ports.
     *  @param input The port to find the equivalence class of.
     *  @return set of the input ports in this actor that are
     *  in an equivalence class with the specified input.
     *  @exception IllegalActionException If the argument is not
     *   contained by the associated actor.
     */
    @Override
    public Collection<IOPort> equivalentPorts(IOPort input)
            throws IllegalActionException {
        if (input.getContainer() != _actor || !input.isInput()) {
            throw new IllegalActionException(input, _actor,
                    "equivalentPort() called with argument "
                            + input.getFullName()
                            + " which is not an input port of "
                            + _actor.getFullName());
        }
        // Make sure the data structures are up to date.
        getDependency(input, null);
        // The following must include at least the specified input port.
        return _equivalenceClasses.get(input);
    }

    /** Return the dependency between the specified input port
     *  and the specified output port.  This is done by traversing
     *  the network of actors from the input ports. For each output
     *  port reachable from an input port, its dependency on the
     *  input port is determined by composing the dependencies along
     *  all paths from the input port to the output port using
     *  oPlus() and oTimes() operators of the dependencies.
     *  For any output port that is not reachable from an input
     *  port, the dependency on that input port is set to
     *  the oPlusIdentity() of the default dependency given
     *  in the constructor.
     *  <p>
     *  When called for the first time since a change in the model
     *  structure, this method performs the complete analysis of
     *  the graph and caches the result. Subsequent calls just
     *  look up the result. Note that the complete analysis
     *  can be quite expensive. For each input port, it traverses
     *  the graph to find all ports reachable from that input port,
     *  and tracks the dependencies. In the worst case, the
     *  complexity can be N*M^2, where N is the number of
     *  input ports and M is the total number of ports in the
     *  composite (including the ports of all contained actors).
     *  The algorithm used, however, is optimized for typical
     *  Ptolemy II models, so in most cases the algorithm completes
     *  in time on the order of N*D, where D is the length of
     *  the longest chain of ports from an input port to an
     *  output port.
     *  @param input The input port.
     *  @param output The output port, or null to update the
     *   dependencies (and record equivalence classes) without
     *   requiring there to be an output port.
     *  @return The dependency between the specified input port
     *   and the specified output port, or null if a null output
     *   is port specified.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public Dependency getDependency(IOPort input, IOPort output)
            throws IllegalActionException {
        // Cast is safe because this is checked in the constructor
        CompositeEntity actor = (CompositeEntity) _actor;

        // If the dependency is not up-to-date, then update it.
        long workspaceVersion = actor.workspace().getVersion();
        if (_dependencyVersion != workspaceVersion) {
            // Need to update dependencies. The cached version
            // is obsolete.
            try {
                actor.workspace().getReadAccess();
                _reverseDependencies = new HashMap<IOPort, Map<IOPort, Dependency>>();
                _forwardDependencies = new HashMap<IOPort, Map<IOPort, Dependency>>();
                _equivalenceClasses = new HashMap<IOPort, Collection<IOPort>>();
                // The following map keeps track for each port in the model which input
                // ports of the associated actor it depends on. This is used to build
                // the equivalence classes.
                Map<IOPort, Collection<IOPort>> dependsOnInputsMap = new HashMap<IOPort, Collection<IOPort>>();
                Iterator inputPorts = _actor.inputPortList().iterator();
                boolean hasPortParameter = false;
                while (inputPorts.hasNext()) {
                    IOPort inputPort = (IOPort) inputPorts.next();
                    if (inputPort instanceof ParameterPort) {
                        hasPortParameter = true;
                    }
                    // Make sure that equivalentPorts() always returns at least a set
                    // with one element.
                    Set<IOPort> justTheInput = new HashSet<IOPort>();
                    justTheInput.add(inputPort);
                    _equivalenceClasses.put(inputPort, justTheInput);

                    // Construct a map of dependencies from this inputPort
                    // to all reachable ports.
                    Map<IOPort, Dependency> map = new HashMap<IOPort, Dependency>();
                    Collection<IOPort> portsToProcess = inputPort
                            .insideSinkPortList();
                    // Set the initial dependency of all the portsToProcess.
                    Iterator ports = portsToProcess.iterator();
                    while (ports.hasNext()) {
                        IOPort port = (IOPort) ports.next();
                        map.put(port, _defaultDependency.oTimesIdentity());
                    }
                    if (!portsToProcess.isEmpty()) {
                        _setDependency(inputPort, map, portsToProcess,
                                dependsOnInputsMap);
                    }
                }
                // Adjust the equivalent ports list if any ParameterPort was found.
                if (hasPortParameter) {
                    List<IOPort> allInputs = _actor.inputPortList();
                    for (IOPort inputPort : allInputs) {
                        _equivalenceClasses.put(inputPort, allInputs);
                    }
                }
            } finally {
                actor.workspace().doneReading();
            }
            _dependencyVersion = workspaceVersion;
        }
        if (output == null) {
            return null;
        }
        Map<IOPort, Dependency> inputMap = _forwardDependencies.get(input);
        if (inputMap != null) {
            Dependency result = inputMap.get(output);
            if (result != null) {
                return result;
            }
        }
        // If there is no recorded dependency, then reply
        // with the additive identity (which indicates no
        // dependency).
        return _defaultDependency.oPlusIdentity();
    }

    /** Return the depth of an actor contained (deeply) by the
     *  associated composite actor.
     *  The depth of an actor is the minimum depth of the output ports.
     *  If there are no output ports, then the depth of
     *  the actor is the maximum depth of the input ports.
     *  If there are no input ports or output ports, the depth is zero.
     *  @see #getDepthOfPort(IOPort)
     *  @param actor An actor whose depth is requested.
     *  @return An integer indicating the depth of the given actor.
     *  @exception IllegalActionException If the actor is not within
     *   the associated actor.
     */
    public int getDepthOfActor(Actor actor) throws IllegalActionException {
        _computeActorDepth();
        Integer depth = _actorToDepth.get(actor);
        if (depth != null) {
            return depth.intValue();
        }
        throw new IllegalActionException(_actor,
                "Attempt to get depth of actor " + actor.getFullName()
                + " that was not sorted. It is probably not"
                + " contained by " + _actor.getFullName());
    }

    /** Return the depth of a port of the associated actor
     *  or an actor contained by it.
     *  The depth of an output port is
     *  the maximum of the depth of all the input ports
     *  it directly depends on, or zero if there are no such
     *  input ports.
     *  The depth of an input port is maximum of the
     *  "source depths" of all the input ports in the
     *  same equivalence class with the specified port.
     *  The "source depth" of an input port is one plus the maximum
     *  depth of all output ports that directly source data
     *  to it, or zero if there are no such ports.
     *  @param ioPort A port whose depth is requested.
     *  @return An integer representing the depth of the specified ioPort.
     *  @exception IllegalActionException If the ioPort does not have
     *   a depth (this should not occur if the ioPort is under the control
     *   of this director).
     *  @see #getDepthOfActor(Actor)
     */
    public int getDepthOfPort(IOPort ioPort) throws IllegalActionException {
        _computeActorDepth();
        Integer depth = _portToDepth.get(ioPort);

        if (depth != null) {
            return depth.intValue();
        }
        throw new IllegalActionException("Attempt to get depth of ioPort "
                + ((NamedObj) ioPort).getFullName() + " that was not sorted.");
    }

    /** Indicate that the cached causality information is invalid.
     */
    public void invalidate() {
        _actorDepthVersion = -1;
        _dependencyVersion = -1;
    }

    /** Remove the dependency that the specified output port has
     *  on the specified input port. Specifically, calling this
     *  method ensures that subsequent calls to
     *  getDependency(inputPort, outputPort)
     *  will return defaultDependency.oPlusIdentity().
     *  It also adjusts what is returned by
     *  {@link #equivalentPorts(IOPort)} and
     *  {@link #dependentPorts(IOPort)}.
     *  @see #getDependency(IOPort, IOPort)
     *  @param inputPort The input port.
     *  @param outputPort The output port that does not depend on the
     *   input port.
     */
    @Override
    public void removeDependency(IOPort inputPort, IOPort outputPort) {
        // First ensure that all dependencies are calculated.
        try {
            getDependency(inputPort, outputPort);
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
        Map<IOPort, Dependency> outputPorts = _forwardDependencies
                .get(inputPort);
        if (outputPorts != null) {
            outputPorts.remove(outputPort);
        }
        Map<IOPort, Dependency> inputPorts = _reverseDependencies
                .get(outputPort);
        if (inputPorts != null) {
            inputPorts.remove(inputPort);
        }
    }

    /** Return a list of the actors deeply contained within
     *  the associated composite actor sorted by actor depth.
     *  For actors that have the same depth, the ordering is
     *  the order returned by
     *  {@link CompositeEntity#deepEntityList()}.
     *  This method always creates a new list.
     *  @return A sorted list of actors.
     *  @exception IllegalActionException If a cycle is found.
     */
    public List<Actor> topologicalSort() throws IllegalActionException {
        // Ensure the cache is up to date and check for cycles.
        checkForCycles();
        List<Actor> actors = ((CompositeEntity) _actor).deepEntityList();
        // Create a copy to sort.
        // FIXME: Would it be more efficient to just create a TreeSet?
        List<Actor> sorted = new LinkedList<Actor>(actors);
        ActorComparator comparator = new ActorComparator();
        Collections.sort(sorted, comparator);
        return sorted;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Workspace version when actor depth was last computed. */
    protected long _actorDepthVersion = -1;

    /** A table giving the depths of actors. */
    protected Map<Actor, Integer> _actorToDepth = null;

    /** The workspace version where the dependency was last updated. */
    protected long _dependencyVersion;

    /** Computed equivalence classes of input ports. */
    protected Map<IOPort, Collection<IOPort>> _equivalenceClasses;

    /** Computed dependencies between input ports and output ports of the associated actor. */
    protected Map<IOPort, Map<IOPort, Dependency>> _forwardDependencies;

    /** Computed reverse dependencies (the key is now an output port). */
    protected Map<IOPort, Map<IOPort, Dependency>> _reverseDependencies;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Compute the depth of ports and actors.
     *  The actor depth is typically used to prioritize firings in response
     *  to pure events (fireAt() calls). The port depth is used
     *  to prioritize firings due to input events. Lower depths
     *  translate into higher priorities, with the lowest
     *  value being zero.
     *  The depth of an actor is the minimum depth of the output ports.
     *  This typically causes the actor to fire as early as possible
     *  to produce an output on those output ports in response
     *  to a pure event, but no earlier than the firings of
     *  actors that may source it data that the firing may
     *  depend on. If there are no output ports, then the depth of
     *  the actor it is the maximum depth of the input ports.
     *  This typically delays the response to fireAt() until all input
     *  events with the same tag have arrived.
     *  If there are no input ports or output ports, the depth is zero.
     *  @exception IllegalActionException If a zero-delay loop is found.
     */
    protected void _computeActorDepth() throws IllegalActionException {
        if (_actorDepthVersion == ((NamedObj) _actor).workspace().getVersion()) {
            return;
        }
        _portToDepth = new HashMap<IOPort, Integer>();
        _actorToDepth = new HashMap<Actor, Integer>();

        // To ensure that the associated actor has the lowest priority,
        // assign it the largest integer.
        _actorToDepth.put(_actor, Integer.MAX_VALUE);

        // First iterate over all actors, ensuring that their
        // ports all have depths. Note that each time an actor
        // is visited, the depths of all upstream ports will be
        // set, so by the time we get to the end of the list
        // of actors, there will be nothing to do on them.
        // But we have to visit all of them to support disconnected
        // graphs. If the actors happen to be ordered in topological
        // sort order, then this will be quite efficient.
        // Otherwise, many actors will be visited twice.
        List<Actor> actors = ((CompositeEntity) _actor).deepEntityList();
        for (Actor actor : actors) {
            // If the actor already has a depth, skip it.
            Integer actorDepth = _actorToDepth.get(actor);
            if (actorDepth != null) {
                continue;
            }
            // First compute the depth of the input ports,
            // recording the maximum value.
            Integer maximumInputDepth = Integer.valueOf(0);
            Iterator inputPorts = actor.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();
                Integer inputPortDepth = _portToDepth.get(inputPort);
                if (inputPortDepth == null) {
                    // Keep track of ports that have been visited in
                    // to detect causality loops. Have to separately
                    // keep track of inputs and outputs because a port
                    // may be both, but the output may not depend on the
                    // input (an example is the ChannelPort in a wireless channel).
                    Set<IOPort> visitedInputs = new HashSet<IOPort>();
                    Set<IOPort> visitedOutputs = new HashSet<IOPort>();
                    _computeInputDepth(inputPort, visitedInputs, visitedOutputs);
                    inputPortDepth = _portToDepth.get(inputPort);
                }
                // Record the maximum and continue to the next port.
                if (inputPortDepth == null) {
                    throw new InternalErrorException(inputPort, null,
                            "inputPortDepth is null?");
                }
                if (inputPortDepth.compareTo(maximumInputDepth) > 0) {
                    maximumInputDepth = inputPortDepth;
                }
            }

            // Next set the depth of the output ports.
            Integer minimumOutputDepth = null;
            Iterator outputPorts = actor.outputPortList().iterator();
            while (outputPorts.hasNext()) {
                IOPort outputPort = (IOPort) outputPorts.next();
                Integer outputPortDepth = _portToDepth.get(outputPort);
                if (outputPortDepth == null) {
                    // Keep track of ports that have been visited in
                    // to detect causality loops. Have to separately
                    // keep track of inputs and outputs because a port
                    // may be both, but the output may not depend on the
                    // input (an example is the ChannelPort in a wireless channel).
                    Set<IOPort> visitedInputs = new HashSet<IOPort>();
                    Set<IOPort> visitedOutputs = new HashSet<IOPort>();
                    _computeOutputPortDepth(outputPort, visitedInputs,
                            visitedOutputs);
                    outputPortDepth = _portToDepth.get(outputPort);
                }
                // Record the minimum and continue to the next port.
                if (minimumOutputDepth == null
                        || outputPortDepth.compareTo(minimumOutputDepth) < 0) {
                    minimumOutputDepth = outputPortDepth;
                }
            }

            // Finally, set the depth of the actor.
            if (minimumOutputDepth != null) {
                // There are output ports.
                _actorToDepth.put(actor, minimumOutputDepth);
            } else {
                _actorToDepth.put(actor, maximumInputDepth);
            }
        }
        // Next need to set the depth of all output ports of the
        // actor.
        List<IOPort> outputPorts = _actor.outputPortList();
        for (IOPort outputPort : outputPorts) {
            // The depth of each output port is one plus the maximum
            // depth of all the output ports (or input ports) that source
            // it data, or zero if there are no such ports.
            int depth = 0;
            List<IOPort> sourcePorts = outputPort.insideSourcePortList();
            for (IOPort sourcePort : sourcePorts) {
                Integer sourceDepth = _portToDepth.get(sourcePort);
                if (sourceDepth == null) {
                    if (!sourcePort.isOutput()) {
                        // This is another external input.
                        // Should this be checked?
                        sourceDepth = Integer.valueOf(0);
                        _portToDepth.put(sourcePort, sourceDepth);
                    } else {
                        // Source port is an output. Have to separately
                        // keep track of inputs and outputs because a port
                        // may be both, but the output may not depend on the
                        // input (an example is the ChannelPort in a wireless channel).
                        Set<IOPort> visitedInputs = new HashSet<IOPort>();
                        Set<IOPort> visitedOutputs = new HashSet<IOPort>();
                        _computeOutputPortDepth(sourcePort, visitedInputs,
                                visitedOutputs);
                        sourceDepth = _portToDepth.get(sourcePort);
                        if (sourceDepth == null) {
                            throw new InternalErrorException(
                                    "Failed to compute port depth for "
                                            + sourcePort.getFullName());
                        }
                    }
                }
                int newDepth = sourceDepth.intValue() + 1;
                if (newDepth > depth) {
                    depth = newDepth;
                }
            }
            _portToDepth.put(outputPort, Integer.valueOf(depth));
        }
        _actorDepthVersion = ((NamedObj) _actor).workspace().getVersion();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Compute the depth of the specified input port.
     *  The depth of an input port is maximum of the
     *  "source depths" of all the input ports in the
     *  same equivalence class with the specified port.
     *  An equivalence class is defined as follows.
     *  If ports X and Y each have a dependency not equal to the
     *  default dependency's oPlusIdentity(), then they
     *  are in an equivalence class. That is,
     *  there is a causal dependency. They are also in
     *  the same equivalence class if there is a port Z
     *  in an equivalence class with X and in an equivalence
     *  class with Y. Otherwise, they are not in the same
     *  equivalence class. If there are no
     *  output ports, then all the input ports
     *  are in a single equivalence class.
     *  <p>
     *  The "source depth" of an input port is one plus the maximum
     *  depth of all output ports that directly source data
     *  to it, or zero if there are no such ports.
     *  This delays the firing of this actor until
     *  all source actors have fired. As a side
     *  effect, this method also sets the depth of all the
     *  input ports in the same equivalence class, as well
     *  as all upstream ports up to a break with non-causal
     *  relationship. It also detects and reports dependency
     *  cycles.  The entry point
     *  to this method is _computeActorDepth().
     *  @see #_computeActorDepth()
     *  @param inputPort The port to compute the depth of.
     *  @param visitedInputs The set of input ports that have been visited
     *   in this round.
     *  @param visitedOutputs The set of output ports that have been visited
     *   in this round.
     *  @exception IllegalActionException If a zero-delay loop is found.
     */
    private void _computeInputDepth(IOPort inputPort,
            Set<IOPort> visitedInputs, Set<IOPort> visitedOutputs)
                    throws IllegalActionException {
        int depth = 0;
        // Iterate over all the ports in the equivalence class.
        Actor actor = (Actor) inputPort.getContainer();
        CausalityInterface causality = actor.getCausalityInterface();
        Collection<IOPort> equivalentPorts = causality
                .equivalentPorts(inputPort);
        for (IOPort equivalentPort : equivalentPorts) {
            visitedInputs.add(equivalentPort);
            // Iterate over the source ports to compute the source depths.
            List<IOPort> sourcePorts = equivalentPort.sourcePortList();
            for (IOPort sourcePort : sourcePorts) {
                // NOTE: source port may be an input port, in which case
                // the port is an external input and should have depth 0.
                // The input port depends directly on this output port.
                // Find the depth of the output port.
                Integer sourcePortDepth = _portToDepth.get(sourcePort);
                if (sourcePortDepth == null) {
                    // Have to compute the depth of the output port.
                    if (visitedOutputs.contains(sourcePort)) {
                        // Found a causality loop.
                        throw new IllegalActionException(_actor, actor,
                                "Found a zero delay loop containing "
                                        + actor.getFullName());
                    }
                    if (!sourcePort.isOutput()) {
                        // This had better be an external input.
                        // Should this be checked?
                        // Set the depth to zero.
                        sourcePortDepth = Integer.valueOf(0);
                        _portToDepth.put(sourcePort, sourcePortDepth);
                    } else {
                        // Source port is an output (it may also be
                        // an input).
                        _computeOutputPortDepth(sourcePort, visitedInputs,
                                visitedOutputs);
                        sourcePortDepth = _portToDepth.get(sourcePort);
                        if (sourcePortDepth == null) {
                            throw new InternalErrorException(
                                    "Failed to compute port depth for "
                                            + sourcePort.getFullName());
                        }
                    }
                }
                // The depth needs to be one greater than any
                // output port it depends on.
                int newDepth = sourcePortDepth.intValue() + 1;
                if (depth < newDepth) {
                    depth = newDepth;
                }
            }
        }
        // We have now found the depth for the equivalence class.
        // Set it.
        for (IOPort equivalentPort : equivalentPorts) {
            _portToDepth.put(equivalentPort, Integer.valueOf(depth));
        }
    }

    /** Compute the depth of the specified output port.
     *  The depth of an output port is
     *  the maximum of the depth of all the input ports
     *  it directly depends on, or zero if there are no such
     *  input ports.  The entry point
     *  to this method is _computeActorDepth().
     *  @see #_computeActorDepth()
     *  @param outputPort The actor to compute the depth of.
     *  @param visitedInputs The set of input ports that have been visited
     *   in this round.
     *  @param visitedOutputs The set of output ports that have been visited
     *   in this round.
     *  @exception IllegalActionException If a zero-delay loop is found.
     */
    private void _computeOutputPortDepth(IOPort outputPort,
            Set<IOPort> visitedInputs, Set<IOPort> visitedOutputs)
                    throws IllegalActionException {
        visitedOutputs.add(outputPort);
        int depth = 0;
        // Iterate over the input ports of the same actor that
        // this output directly depends on.
        Actor actor = (Actor) outputPort.getContainer();
        CausalityInterface causality = actor.getCausalityInterface();
        for (IOPort inputPort : causality.dependentPorts(outputPort)) {
            // if outputPort is an IO port then inputPort can be an output
            // port as well. Check here and ignore if inputPort is actually
            // an output port.
            if (inputPort.isInput()) {

                // The output port depends directly on this input port.
                // Find the depth of the input port.
                Integer inputPortDepth = _portToDepth.get(inputPort);
                if (inputPortDepth == null) {
                    // Have to compute the depth of the input port.
                    if (visitedInputs.contains(inputPort)) {
                        // Found a causality loop.
                        throw new IllegalActionException(_actor, actor,
                                "Found a zero delay loop containing "
                                        + actor.getFullName());
                    }
                    // The following computes only the source depth,
                    // which may not be the final depth. As a consequence,
                    // _computeActorDepth(), the output depth that
                    // we calculate here may need to be modified.
                    _computeInputDepth(inputPort, visitedInputs, visitedOutputs);
                    inputPortDepth = _portToDepth.get(inputPort);
                    if (inputPortDepth == null) {
                        throw new InternalErrorException(
                                "Failed to compute port depth for "
                                        + inputPort.getFullName());
                    }
                }

                int newDepth = inputPortDepth.intValue();
                if (depth < newDepth) {
                    depth = newDepth;
                }
            }
        }
        _portToDepth.put(outputPort, Integer.valueOf(depth));
    }

    /** Record a dependency of the specified port on the specified
     *  input port in the specified map. The map records all the
     *  dependencies for this particular input port.
     *  If there was a prior dependency already
     *  that was less than this one, then update the dependency
     *  using its oPlus() method. If the dependency is equal
     *  to the oPlusIdentity(), then do not record it and return false.
     *  Return true if the dependency was newly set or modified from
     *  a previously recorded dependency. Return false if no change
     *  was made to a previous dependency.
     *  @param inputPort The source port.
     *  @param port The destination port, which may be an output
     *   port of the associated actor, or any port in a contained
     *   actor.
     *  @param map The map in which to record the dependency.
     *  @param dependency The dependency map for ports reachable from the input port.
     *  @param dependsOnInputsMap The map from ports in the model to input ports
     *   that they depend on, used to construct the equivalence classes.
     *  @return True if the dependency was changed.
     */
    private boolean _recordDependency(IOPort inputPort, IOPort port,
            Map<IOPort, Dependency> map, Dependency dependency,
            Map<IOPort, Collection<IOPort>> dependsOnInputsMap)
                    throws IllegalActionException {
        if (dependency.equals(_defaultDependency.oPlusIdentity())) {
            return false;
        }
        // First, update the equivalence classes.
        // Construct a set that merges all known input port dependencies
        // for this port with any known equivalents of the input port.
        Collection<IOPort> merged = _equivalenceClasses.get(inputPort);
        if (merged == null) {
            merged = new HashSet<IOPort>();
            merged.add(inputPort);
            _equivalenceClasses.put(inputPort, merged);
        }

        // If the port is not already entered in the dependsOnInputsMap,
        // then enter it. The entry will eventually be
        // all the actor input ports that this port depends on.
        Collection<IOPort> dependsOn = dependsOnInputsMap.get(port);
        if (dependsOn != null) {
            // Make sure to include any previously found dependencies.
            merged.addAll(dependsOn);
        }
        // If this port has equivalents, and those have dependencies,
        // then those dependencies need to be added. It can only have
        // equivalents if it is an input port.
        if (port.isInput()) {
            Collection<IOPort> equivalents = ((Actor) port.getContainer())
                    .getCausalityInterface().equivalentPorts(port);
            for (IOPort equivalent : equivalents) {
                // This is guaranteed to include port.
                Collection<IOPort> otherInputs = dependsOnInputsMap
                        .get(equivalent);
                if (otherInputs != null) {
                    merged.addAll(otherInputs);
                    // For each of the other inputs, it may have
                    // equivalents. Add those.
                    for (IOPort dependentInputPort : otherInputs) {
                        // Get the equivalence class for another port depended on.
                        Collection<IOPort> equivalenceClass = _equivalenceClasses
                                .get(dependentInputPort);
                        if (equivalenceClass != null) {
                            merged.addAll(equivalenceClass);
                        }
                    }
                }
            }
        }

        // For every input port in the merged set, record the equivalence
        // to the merged set.
        for (IOPort mergedInput : merged) {
            _equivalenceClasses.put(mergedInput, merged);
        }
        dependsOnInputsMap.put(port, merged);

        // Next update the forward and reverse dependencies.
        // If the port belongs to the associated actor,
        // make a permanent record.
        Map<IOPort, Dependency> forward = null;
        Map<IOPort, Dependency> reverse = null;
        if (port.getContainer() == _actor) {
            forward = _forwardDependencies.get(inputPort);
            if (forward == null) {
                forward = new HashMap<IOPort, Dependency>();
                _forwardDependencies.put(inputPort, forward);
            }
            forward.put(port, dependency);

            reverse = _reverseDependencies.get(port);
            if (reverse == null) {
                reverse = new HashMap<IOPort, Dependency>();
                _reverseDependencies.put(port, reverse);
            }
            reverse.put(inputPort, dependency);
        }
        Dependency priorDependency = map.get(port);
        if (priorDependency == null) {
            map.put(port, dependency);
            return true;
        }
        // There is a prior dependency.
        Dependency newDependency = priorDependency.oPlus(dependency);
        if (!newDependency.equals(priorDependency)) {
            // Update the dependency.
            map.put(port, newDependency);
            if (port.getContainer() == _actor) {
                // Have to also change the forward and reverse dependencies.
                reverse.put(inputPort, newDependency);
                forward.put(port, newDependency);
            }
            return true;
        }
        // No change made to the dependency.
        return false;
    }

    /** Set the dependency from the specified inputPort to all
     *  ports that are reachable via the portsToProcess ports.
     *  The results are stored in the specified map.
     *  @param inputPort An input port of this actor.
     *  @param map A map of dependencies from this input port to all reachable ports,
     *   built by this method. The map is required to contain all ports in portsToProcess
     *   on entry.
     *  @param portsToProcess Ports connected to the input port directly or indirectly.
     *  @param dependsOnInputsMap The map from ports in the model to input ports
     *   that they depend on, used to construct the equivalence classes.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    private void _setDependency(IOPort inputPort, Map<IOPort, Dependency> map,
            Collection<IOPort> portsToProcess,
            Map<IOPort, Collection<IOPort>> dependsOnInputsMap)
                    throws IllegalActionException {
        Set<IOPort> portsToProcessNext = new HashSet<IOPort>();
        for (IOPort port : portsToProcess) {
            // The argument map is required to contain this dependency.
            Dependency dependency = map.get(port);
            // Next, check whether we have gotten to an output port of this actor.
            if (port.getContainer() == _actor) {
                // Port is owned by this actor. If it is
                // output port, then it is dependent on this
                // input port by the given dependency. It should
                // not normally be an input port, but we tolerate
                // that here in case some domain uses it someday.
                // In that latter case, there is no dependency.
                if (port.isOutput()) {
                    // We have a path from an input to an output.
                    // Record the dependency.
                    _recordDependency(inputPort, port, map, dependency,
                            dependsOnInputsMap);
                }
            } else {
                // The port presumably belongs to an actor inside this actor.
                _recordDependency(inputPort, port, map, dependency,
                        dependsOnInputsMap);
                // Next record the dependency that all output ports of
                // the actor containing the port have on the input port.
                Actor actor = (Actor) port.getContainer();
                CausalityInterface causality = actor.getCausalityInterface();
                Iterator outputPorts = actor.outputPortList().iterator();
                while (outputPorts.hasNext()) {
                    IOPort outputPort = (IOPort) outputPorts.next();
                    Dependency actorDependency = causality.getDependency(port,
                            outputPort);
                    Dependency newDependency = dependency
                            .oTimes(actorDependency);
                    if (_recordDependency(inputPort, outputPort, map,
                            newDependency, dependsOnInputsMap)) {
                        // Dependency of this output port has been set or
                        // changed.  Add ports to the set of ports to be
                        // processed next.
                        Collection sinkPorts = outputPort.sinkPortList();
                        Iterator sinkPortsIterator = sinkPorts.iterator();
                        while (sinkPortsIterator.hasNext()) {
                            IOPort sinkPort = (IOPort) sinkPortsIterator.next();
                            _recordDependency(inputPort, sinkPort, map,
                                    newDependency, dependsOnInputsMap);
                            if (sinkPort.getContainer() != _actor) {
                                // Port is not owned by this actor.
                                // Further processing will be needed.
                                portsToProcessNext.add(sinkPort);
                            }
                        }
                    }
                }
            }
        }
        if (!portsToProcessNext.isEmpty()) {
            _setDependency(inputPort, map, portsToProcessNext,
                    dependsOnInputsMap);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A table giving the depths of ports. */
    private Map<IOPort, Integer> _portToDepth = null;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Comparator used to sort the actors. */
    private class ActorComparator implements Comparator<Actor> {
        /** Compare the depths of two actors.
         *  NOTE: This method assumes and does not check that the
         *  depth cache is up to date and contains both specified actors.
         */
        @Override
        public int compare(Actor actor1, Actor actor2) {
            Integer level1 = _actorToDepth.get(actor1);
            Integer level2 = _actorToDepth.get(actor2);
            return level1.compareTo(level2);
        }
    }
}
