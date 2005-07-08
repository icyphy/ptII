/* A FunctionDependency is an abstract class that describes the function
 dependency relation between the externally visible inputs and outputs
 of an actor.

 Copyright (c) 2003-2005 The Regents of the University of California.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Node;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// FunctionDependency

/** A FunctionDependency is an abstract class that describes the
 function dependency that output ports of an associated actor
 have on its input ports.  The associated actor is specified by
 the constructor argument and cannot be changed once this object
 is constructed.
 <p>
 An output port has a function dependency on an input port if
 in its fire method, it sends tokens on the output port that
 depend on tokens gotten from the input port.
 <p>
 This class uses a graph to describe the function dependency,
 where the nodes of the graph correspond to the ports and an edge
 indicates a function dependency. The edges go from input ports
 to output ports that depend on them.
 <p>
 The dependency graph by default indicates complete dependency,
 which means that each output port depends on all input ports.
 This default dependency graph is constructed in the protected
 method {@link #_constructDependencyGraph()}, which can be
 overridden in subclasses to construct a more accurate
 dependency graph. See {@link FunctionDependencyOfAtomicActor}
 and {@link FunctionDependencyOfCompositeActor} for example
 concrete subclasses. The composite actor version analyzes
 the contained model to determine from the function dependencies
 of the contained actors and from the connections what the
 function dependencies of the composite actor are.

 @author Haiyang Zheng
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (eal)
 */
public abstract class FunctionDependency extends Attribute {
    /** Construct a FunctionDependency object for the given actor.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the attribute is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public FunctionDependency(Entity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // NOTE:
        // Only actors have function dependencies.
        // Other entities, such as a State, do not.
        setPersistent(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////

    /** A final and static string variable for this attribute. It should
     *  not be modified.
     */
    public final static String UniqueName = "FUNCTIONDEPENDENCY";

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the dependency graph representing the function
     *  dependency of an actor. The nodes of the graph are the
     *  ports of the actor. Each edge indicates a function
     *  dependency between an input port and an output port.
     *  @return A dependency graph for the associated actor.
     */
    public DirectedGraph getDependencyGraph() {
        _validate();
        return _dependencyGraph;
    }

    /** Get the output ports that depend on the given input port.
     *  @param inputPort The given input port.
     *  @return A set of output ports that depend on the input port.
     *   The elements of the set are all instances of IOPort.
     */
    public Set getDependentOutputPorts(IOPort inputPort) {
        _validate();

        // ensure that the input port is inside the dependency graph
        if (!inputPort.getContainer().equals(getContainer())) {
            throw new InternalErrorException("The input port "
                    + inputPort.getName() + " does not belong to the "
                    + "actor " + getContainer().getName());
        }

        Collection reachableOutputs = _dependencyGraph
                .reachableNodes(_dependencyGraph.node(inputPort));
        Set dependentOutputPorts = new HashSet();
        Iterator outputs = reachableOutputs.iterator();

        while (outputs.hasNext()) {
            Node node = (Node) outputs.next();
            dependentOutputPorts.add(node.getWeight());
        }

        return dependentOutputPorts;
    }

    /** Get the input ports on which the given output port is dependent.
     *  @param outputPort The given output port.
     *  @return A set of input ports on which the output port is dependent.
     *   The elements of the set are all instances of IOPort.
     */
    public Set getInputPortsDependentOn(IOPort outputPort) {
        _validate();

        // ensure that the output port is inside the dependency graph
        if (!outputPort.getContainer().equals(getContainer())) {
            throw new InternalErrorException("The output port "
                    + outputPort.getName() + " does not belong to the "
                    + "actor " + getContainer().getName());
        }

        Collection backwardReachableInputs = _dependencyGraph
                .backwardReachableNodes(_dependencyGraph.node(outputPort));
        Set dependentInputPorts = new HashSet();
        Iterator inputs = backwardReachableInputs.iterator();

        while (inputs.hasNext()) {
            Node node = (Node) inputs.next();
            dependentInputPorts.add(node.getWeight());
        }

        return dependentInputPorts;
    }

    /** Add this attribute to the given container.
     */
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        super.setContainer(container);
        _functionDependencyVersion = -1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Construct a complete dependency graph with all the
     *  ports of the associated actor as nodes and edges going from
     *  each input port node to each output port node.
     *  This is provided as a convenience for subclasses that override
     *  _constructDependencyGraph() to build a starting point.
     *  @return A complete dependency graph.
     */
    protected final DirectedGraph _constructConnectedDependencyGraph() {
        // construct new directed graph
        DirectedGraph dependencyGraph = _constructDisconnectedDependencyGraph();

        // For each input and output port pair, add a directed
        // edge going from the input port to the output port.
        Iterator inputs = ((Actor) getContainer()).inputPortList()
                .listIterator();

        while (inputs.hasNext()) {
            IOPort inputPort = (IOPort) inputs.next();
            Iterator outputs = ((Actor) getContainer()).outputPortList()
                    .listIterator();

            while (outputs.hasNext()) {
                // add an edge from the input port to the output port
                dependencyGraph.addEdge(inputPort, outputs.next());
            }
        }

        return dependencyGraph;
    }

    /** Construct the dependency graph for the associated actor.
     *  This base class provides a complete dependency graph. The
     *  subclasses may need to override this implementation.
     *  This method should only be called from by the protected method
     *  _validate(). It is protected only so that subclasses can
     *  override it, not so they can call it.
     *  @exception IllegalActionException Not thrown in this class.
     */
    protected void _constructDependencyGraph() {
        _dependencyGraph = _constructConnectedDependencyGraph();
    }

    /** Construct and return a dependency graph containing all the
     *  ports of the associated actor as nodes and no edges.
     *  This is provided as a convenience for subclasses that override
     *  _constructDependencyGraph() to build a starting point.
     *  @return A dependency graph with nodes for ports but no edges.
     */
    protected final DirectedGraph _constructDisconnectedDependencyGraph() {
        // construct new directed graph
        DirectedGraph dependencyGraph = new DirectedGraph();

        // include all the externally visible ports of the associated actor
        // as nodes in the graph
        Iterator inputs = ((Actor) getContainer()).inputPortList()
                .listIterator();

        while (inputs.hasNext()) {
            IOPort input = (IOPort) inputs.next();
            dependencyGraph.addNodeWeight(input);
        }

        Iterator outputs = ((Actor) getContainer()).outputPortList()
                .listIterator();

        while (outputs.hasNext()) {
            IOPort output = (IOPort) outputs.next();
            dependencyGraph.addNodeWeight(output);
        }

        return dependencyGraph;
    }

    /** Update the FunctionDependency object. If it has not been
     *  previously constructed, then construct it. If the topology
     *  has been changed since it was last constructed (as indicated
     *  by the getVersion() method of the workspace of the associated
     *  actor), then reconstruct it. Otherwise, do nothing.
     *  @see ptolemy.kernel.util.Workspace#getVersion()
     */
    protected final void _validate() {
        Workspace workspace = getContainer().workspace();
        long workspaceVersion = workspace.getVersion();

        if (_functionDependencyVersion != workspaceVersion) {
            try {
                workspace.getReadAccess();
                _constructDependencyGraph();
                _functionDependencyVersion = workspaceVersion;
            } finally {
                workspace.doneReading();
            }
        }

        // NOTE: the current design of the version control is
        // to synchronize with the workspace. It is based on the
        // assumption that only the topology change increases the
        // workspace version.
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The dependency graph of the associated actor. */
    protected DirectedGraph _dependencyGraph;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The version of the FunctionDependency, which is synchronized
    // to the version of the workspace.
    private long _functionDependencyVersion = -1;
}
