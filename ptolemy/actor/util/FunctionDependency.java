/* A FunctionDependency is an abstract class that describes the function
   dependency relation between the externally visible inputs and outputs 
   of an actor.

   Copyright (c) 2003-2004 The Regents of the University of California.
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
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// FunctionDependency
/** A FunctionDependency is an abstract class that describes the internal 
    function dependency relation between the externally visible input and 
    output ports of an actor. 
    <p>
    An output port is dependent on an input port of the same actor if the 
    token sent by the output port depends on the token got from the input
    port in the same firing.  
    <p> 
    This class uses a graph to describe the function dependency relation of
    an actor, where the nodes corespond to the actor ports and the edges 
    indicate the dependency relationship between ports. The edges have 
    directions, indicating the destination port depends on the source port 
    but not the other way. 
    <p>
    The dependency graph by default is constructed in such a way that 
    each output port depends on each input port, which is a conservative
    approximation. Subclasses need to override the protected method 
    {@link #_constructDependencyGraph} to construct a more accurate 
    dependency graph. See the {@link FunctionDependencyOfAtomicActor}, 
    {@link FunctionDependencyOfCompositeActor} for example implementations. 
 
    @see FunctionDependencyOfAtomicActor
    @see FunctionDependencyOfCompositeActor
    @see ptolemy.domains.fsm.kernel.FunctionDependencyOfFSMActor
    @see ptolemy.domains.de.kernel.DEEDirector
    @author Haiyang Zheng
    @version $Id$
    @since Ptolemy II 4.0
    @Pt.ProposedRating Red (hyzheng)
    @Pt.AcceptedRating Red (hyzheng)
*/
public abstract class FunctionDependency {

    /** Construct a FunctionDependency object for the given actor.
     *  @param actor The actor.
     */
    public FunctionDependency(Actor actor) {
        // Only actors have function dependencies.
        // Other entities, such as a State, do not.
        _actor = actor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the dependency graph representing the function 
     *  dependency of an actor. It only includes the externally visible 
     *  ports of the actor as nodes. This graph is used to construct 
     *  the function dependency for the container of the actor. 
     *  @return A dependency graph reflecting the function 
     *  dependency information.
     */
    public DirectedGraph getDependencyGraph() {
        _validate();
        return _dependencyGraph;
    }
    
    /** Get the associated actor.
     *  @return The associated actor.
     */
    public Actor getActor() {
        return _actor;
    }

    /** Get the output ports that depend on the given input port.
     *  @param inputPort The given input port.
     *  @return A set of output ports that depend on the input.
     */
    public Set getDependentOutputPorts(IOPort inputPort) {
        _validate();
        // ensure that the input port is inside the dependency graph
        if (!inputPort.getContainer().equals(_actor)) {
            throw new InternalErrorException("The input port " +
                inputPort.getName() + " does not belong to the " +
                "actor " + ((NamedObj)_actor).getName());    
        }
        Collection reachableOutputs =
            _dependencyGraph.reachableNodes(
            _dependencyGraph.node(inputPort));
        Set dependentOutputPorts = new HashSet();
        Iterator outputs = reachableOutputs.iterator();
        while (outputs.hasNext()) {
            Node node = (Node)outputs.next();
            dependentOutputPorts.add(node.getWeight());
        }
        return dependentOutputPorts;
    }

    /** Get the input ports on which the given output port is dependent.
     *  @param outputPort The given output port.
     *  @return A set of input ports.
     */
    public Set getInputPortsDependentOn(IOPort outputPort) {
        _validate();
        // ensure that the output port is inside the dependency graph
        if (!outputPort.getContainer().equals(_actor)) {
            throw new InternalErrorException("The output port " +
                outputPort.getName() + " does not belong to the " +
                "actor " + ((NamedObj)_actor).getName());    
        }
        Collection backwardReachableInputs =
            _dependencyGraph.backwardReachableNodes(
            _dependencyGraph.node(outputPort));
        Set dependentInputPorts = new HashSet();
        Iterator inputs = backwardReachableInputs.iterator();
        while (inputs.hasNext()) {
            Node node = (Node)inputs.next();
            dependentInputPorts.add(node.getWeight());
        }
        return dependentInputPorts;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    /** Construct the dependency graph associated with the actor.
     *  This base class provides a connected graph. The 
     *  subclasses may need to overwrite its implementation.
     *  This method should only be called from by the protected method
     *  _validate(). 
     */
    protected void _constructDependencyGraph() {
        _dependencyGraph = 
            _initializeConnectedDependencyGraph();
    }

    /** Initialize a dependency graph by adding all the externally
     *  visible ports of the associated actor as nodes, where all 
     *  the nodes are disconnected.
     *  @return A dependency graph with its nodes disconncted.
     */
    protected final DirectedGraph _initializeDisconnectedDependencyGraph() {
        // construct new directed graph
        DirectedGraph dependencyGraph = new DirectedGraph();

        // include all the externally visible ports of the associated actor
        // as nodes in the graph
        Iterator inputs = _actor.inputPortList().listIterator();
        while (inputs.hasNext()) {
            IOPort input = (IOPort)inputs.next();
            dependencyGraph.addNodeWeight(input);
        }
        Iterator outputs = _actor.outputPortList().listIterator();
        while (outputs.hasNext()) {
            IOPort output = (IOPort)outputs.next();
            dependencyGraph.addNodeWeight(output);
        }
        return dependencyGraph;
    }

    /** Initialize a dependency graph by adding all the externally visible
     *  ports of the associated actor as nodes and adding edges going from 
     *  each input-port node to each output-port node.
     *  @return A dependency graph with all input-port nodes conncted
     *  to all the output-port nodes. 
     */
    protected final DirectedGraph _initializeConnectedDependencyGraph() {
        // construct new directed graph
        DirectedGraph dependencyGraph = 
            _initializeDisconnectedDependencyGraph();

        // For each input and output port pair, add a directed 
        // edge going from the input port to the output port.
        Iterator inputs = _actor.inputPortList().listIterator();
        while (inputs.hasNext()) {
            IOPort inputPort = (IOPort) inputs.next();
            Iterator outputs = 
                _actor.outputPortList().listIterator();
            while (outputs.hasNext()) {
                // add an edge from the input port to the output port
                dependencyGraph.addEdge(inputPort, outputs.next());
            }
        }
        
        return dependencyGraph;
    }

    /** Check the validity of the FunctionDependency object. 
     *  If it is invalid, reconstruct it. Otherwise, do nothing.
     */
    protected final void _validate() {
        Workspace workspace = ((NamedObj)_actor).workspace();
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

    /** The dependency graph of the associated actor. */
    protected DirectedGraph _dependencyGraph;

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    // The associated actor of this FunctionDependency object.
    private Actor _actor;

    // The version of the FunctionDependency, which is synchronized
    // to the version of the workspace.
    private long _functionDependencyVersion = -1;
}
