/* A FunctionDependency is an abstract class that describes the function
   dependency relation between the inputs and outputs of an actor.

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

package ptolemy.actor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Node;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// FunctionDependency
/** A FunctionDependency is an abstract class that describes the internal 
    function dependency relation between the external visible input and 
    output ports of an actor. 
    <p>
    An output port is dependent on an input of the same actor if the token 
    inside the receiver of the output port has the same tag with that of 
    the token inside the input port receiver, or in other words, these tokens
    are generated in the same firing. This class does not talk about the 
    possible dependency relation between the ports from outside of the actor. 
    <p> 
    This class uses two graphs to describe the dependency relation, where 
    the nodes corespond to the ports 
    of the actor and the edges indicate the dependency relationship between 
    ports. The edges have directions, indicating the destination port depends
    on the source port but not the other way. 
    <p> 
    The first graph is an <i>abstract</i> graph, which only includes the
    external visible ports of the actor as nodes. The other graph is an 
    <i>detailed</i> graph, which not only includes the external visible ports 
    but also the internal ports of the contained actors. It the actor is an
    atomic actor, there is no difference between these two graphs. For an 
    composite actor, the abstract graph is part of the detailed graph. 
    <p>
    The implementation of how to construct a detailed or an abstract graph is 
    left undefined and the subclasses need to implement the protected 
    method _constructDetailedPortGraph. See the 
    {@link FunctionDependencyOfAtomicActor}, 
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

    /** Construct a FunctionDependency object for the given container.
     *  @param container The container.
     */
    public FunctionDependency(Actor container) {
        // Only actors have function dependencies.
        // Other entities, such as a State, do not.
        _container = container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return an abstract port graph representing the function dependency
     *  information. The port graph includes only the container ports but not
     *  those of contained actors. This information is used to construct the 
     *  function dependency for the upper level container of this container. 
     *  <p>
     *  The validity of the FunctionDependency object is checked at the
     *  beginning of this method.
     *  @return An abstract port graph reflecting the function dependency
     *  information that excludes the internal ports.
     *  @see #getDetailedPortGraph
     */
    public DirectedGraph getAbstractPortGraph() {
        _validate();
        return _abstractPortGraph;
    }
    
    /** Get the container.
     *  @return The container.
     */
    public Actor getContainer() {
        return _container;
    }

    /** Return a detailed port graph representing the function dependency
     *  information. The port graph includes both the container ports and
     *  those of contained actors (if any). This information is used
     *  by a director to construct a schedule.
     *  <p>
     *  The validity of the FunctionDependency object is checked at the
     *  beginning of this method.
     *  @return A detailed port graph reflecting the input output dependency
     *  information that includes the internal ports.
     *  @see #getAbstractPortGraph
     */
    public DirectedGraph getDetailedPortGraph() {
        _validate();
        return _detailedPortGraph;
    }

    /** Get the output ports that depend on the given input port.
     *  @param inputPort The given input port.
     *  @return A set of output ports that depend on the input.
     */
    //  FIXME: This will be removed when the DEDirector redesign finishes.
    public Set getDependentOutputPorts(IOPort inputPort) {
        _validate();
        Collection reachableOutputs =
            _abstractPortGraph.reachableNodes(
            _abstractPortGraph.node(inputPort));
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
        Collection backwardReachableInputs =
            _abstractPortGraph.backwardReachableNodes(
            _abstractPortGraph.node(outputPort));
        Set dependentInputPorts = new HashSet();
        Iterator inputs = backwardReachableInputs.iterator();
        while (inputs.hasNext()) {
            Node node = (Node)inputs.next();
            dependentInputPorts.add(node.getWeight());
        }
        return dependentInputPorts;
    }

//    /** Make the FunctionDependency object invalid. Note that the
//     *  FunctionDependency
//     *  object is used to help a director to construct a valid schedule.
//     *  When a model changes, e.g. the topology change, the director has
//     *  to reconstruct the FunctionDependency object and schedule. So
//     *  this method is called to force a reconstruction.
//     *  @see ptolemy.domains.de.kernel.DEDirector
//     */
//    public void invalidate() {
//        // FIXME: question about whether this method is necessary.
//        // If there exist some cases that some changes make the schedule
//        // and the funciton dependency invalid but not increase the
//        // workspace version, this method is necessary. It has to be
//        // public. 
//        // FIXME: how about the invalidateSchedule method in DEDirector?
//        _functionDependencyVersion = -1;
//    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    /** Construct an abstract port graph from a detailed port graph by 
        excluding the internal ports. This method is abstract and the 
        subclasses should give the detalied implementation.
     */
    protected abstract void _constructAbstractPortGraph();

    /** Construct a directed graph with the nodes representing input and
     *  output ports, and directed edges representing dependencies.
     *  This method is left undefined and the subclasses provide
     *  the detailed implementation.
     */
    protected abstract void _constructDetailedPortGraph();

    /** Check the validity of the FunctionDependency object. 
     *  If it is invalid, reconstruct it. Otherwise, do nothing.
     */
    protected void _validate() {
        Workspace workspace = ((NamedObj)_container).workspace();
        long workspaceVersion = workspace.getVersion();
        if (_functionDependencyVersion != workspaceVersion) {
            try {
                workspace.getReadAccess();
                _constructFunctionDependency();
                _functionDependencyVersion = workspaceVersion;
            } finally {
                workspace.doneReading();
            }
        }
        // FIXME: the current design of the version control is
        // to synchronize with the workspace. It is based on the
        // assumption that only the topology change increases the 
        // workspace version. However, the assumption may not be
        // always true. Consequently, this method may be called 
        // but unnecessarily.
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private methods                      ////
    
    // Construct both an abstract and detailed port graph.
    private void _constructFunctionDependency() {
        _initializeDirectedGraphs();
        // firstly construct the detailed port graph,
        // then the abstract graph.
        _constructDetailedPortGraph();
        _constructAbstractPortGraph();
    }
    
    /** Initialize the directed graphs by adding the ports of the 
     *  container.
     */
    private void _initializeDirectedGraphs() {
        // construct new directed graphs
        _detailedPortGraph = new DirectedGraph();
        _abstractPortGraph = new DirectedGraph();

        // include all the ports of the container
        // as nodes in the graphs
        Iterator inputs = _container.inputPortList().listIterator();
        while (inputs.hasNext()) {
            IOPort input = (IOPort)inputs.next();
            _detailedPortGraph.addNodeWeight(input);
            _abstractPortGraph.addNodeWeight(input);
        }
        Iterator outputs = _container.outputPortList().listIterator();
        while (outputs.hasNext()) {
            IOPort output = (IOPort)outputs.next();
            _detailedPortGraph.addNodeWeight(output);
            _abstractPortGraph.addNodeWeight(output);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    protected variables                    ////

    /** The abstract directed graph of the input and output ports. */
    protected DirectedGraph _abstractPortGraph;

    /** The detailed directed graph of the input and output ports. */
    protected DirectedGraph _detailedPortGraph;

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

//    /** The abstract directed graph of the input and output ports. */
//    private DirectedGraph _abstractPortGraph;

    /** The container of this FunctionDependency object. */
    private Actor _container;

//    /** The detailed directed graph of the input and output ports. */
//    private DirectedGraph _detailedPortGraph;

    // The version of the FunctionDependency.
    private long _functionDependencyVersion = -1;
}
