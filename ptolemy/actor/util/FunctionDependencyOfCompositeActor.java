/* An instance of FunctionDependencyOfCompositeActor describes the
   function dependency between the outputs and the inputs of a composite actor.

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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.lib.Sink;
import ptolemy.actor.lib.Source;
import ptolemy.graph.DirectedGraph;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
//// FunctionDependenceOfCompositeActor

/** An instance of FunctionDependencyOfCompositeActor describes the function
    dependency between the outputs and inputs of a composite actor. In
    particular, which outputs depend on which inputs. An output is depend on
    an input if the token sent through an output port depends on the token
    received from the input port in the same iteration.

    <p> This class provides both an abstracted view, which gives the function
    dependency the output ports of a composite actor have on input ports, and
    a detailed view from which the abstruct view is constructed.
    The detailed view is a graph where the nodes correspond
    to the ports of a composite actor and to the ports of all deeply
    contained opaque actors, and the edges represent either the communication
    dependencies implied by the connections within this composite actor or
    the function dependencies of the contained opaque actors.
    The detailed view is typically used by a director to construct a
    schedule while the abstracted view is abstracted from the detailed view
    for composition.
    <p>
    The detailed view may reveal dependency loops, which in many domains
    means that the model cannot be executed.
    To check whether there are such loops, use the getCycleNodes() method.
    The method returns an array of IOPorts in such loops, or an empty
    array if there are no such loops.

    @see FunctionDependency
    @author Haiyang Zheng
    @version $Id$
    @since Ptolemy II 4.0
    @Pt.ProposedRating Green (hyzheng)
    @Pt.AcceptedRating Green (zhouye)
*/
public class FunctionDependencyOfCompositeActor extends FunctionDependency {
    /** Construct a FunctionDependency for the given actor.
     *  @param compositeActor The composite actor with which this function
     *  dependency is associated.
     *  @param name The name for this attribute.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the attribute is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public FunctionDependencyOfCompositeActor(CompositeActor compositeActor,
            String name) throws IllegalActionException, NameDuplicationException {
        super(compositeActor, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is a dependency loop in the dependency graph of this
     *  FunctionDependency object, return the nodes in the
     *  dependency loop. If there are multiple loops, the nodes in all the
     *  loops will be returned. If there is no loop, return an empty array.
     *  The elements of the returned array are instances of IOPort.
     *  Note that the returned result is actually an object array instead
     *  of an array of IOPorts.
     *  @return An array that contains the IOPorts in dependency loops, or an
     *   empty array if there are no such loops.
     */
    public Object[] getCycleNodes() {
        _validate();

        // NOTE that this method actually returns an array of objects instead
        // of IOPorts. The reason is because the cycleNodes() method of the
        // DirectedGraph returns an array of objects.
        return _detailedDependencyGraph.cycleNodes();
    }

    /** Return a detailed dependency graph representing the function
     *  dependency information. The graph includes both the ports of
     *  this composite actor and the ports of its deeply
     *  contained opaque actors (both composite and atomic).
     *  @return A detailed dependency graph reflecting the dependency
     *  information between the input and output ports.
     */
    public DirectedGraph getDetailedDependencyGraph() {
        _validate();
        return _detailedDependencyGraph;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Construct a dependency graph from a detailed dependency graph by
     *  excluding the internal ports. The returned graph has an abstract
     *  view.
     *  @exception IllegalActionException Not thrown in this class.
     */
    protected void _constructDependencyGraph() {
        // First, construct the detailed dependency graph
        _constructDetailedDependencyGraph();

        // get associated actor
        Actor actor = (Actor) getContainer();

        Attribute attribute = getContainer().getAttribute("Atomic");

        if (attribute != null) {
            super._constructDependencyGraph();
        } else {
            // Initialize the dependency graph
            _dependencyGraph = _constructDisconnectedDependencyGraph();

            // add an edge from input to output
            // if the output depends on the input
            Iterator inputs = actor.inputPortList().listIterator();

            while (inputs.hasNext()) {
                IOPort inputPort = (IOPort) inputs.next();
                Collection reachableOutputs = _detailedDependencyGraph
                    .reachableNodes(_detailedDependencyGraph.node(
                                            inputPort));
                Iterator outputs = actor.outputPortList().listIterator();

                while (outputs.hasNext()) {
                    IOPort outputPort = (IOPort) outputs.next();

                    if (reachableOutputs.contains(_detailedDependencyGraph.node(
                                                          outputPort))) {
                        _dependencyGraph.addEdge(inputPort, outputPort);
                    }
                }
            }
        }
    }

    /** Get a list of embedded entities for function dependency calculation.
     *  This list of entities appear in the detailed view. Subclasses may
     *  need to override this method to exclude some entities from calculation
     *  of function dependency.
     *  @return A list of embedded entities for the calculation of function
     *  dependency.
     */
    protected List _getEntities() {
        return ((CompositeActor) getContainer()).deepEntityList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Categorize the given list of actors into three kinds: sinks, sources,
    // and transformers.
    private void _categorizeActors(List actorList) {
        Iterator actors = actorList.listIterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            if (actor instanceof AtomicActor) {
                // Atomic actors have type.
                if (actor instanceof Source) {
                    _sourceActors.add(actor);
                } else if (actor instanceof Sink) {
                    _sinkActors.add(actor);
                } else {
                    _transformers.add(actor);
                }
            } else {
                // Composite actors are categorized based
                // on their ports
                int numberOfInputs = actor.inputPortList().size();
                int numberOfOutputs = actor.outputPortList().size();

                if (numberOfInputs == 0) {
                    _sourceActors.add(actor);
                } else if (numberOfOutputs == 0) {
                    _sinkActors.add(actor);
                } else {
                    _transformers.add(actor);
                }
            }
        }
    }

    // Construct a directed graph with the nodes representing input and
    // output ports, and directed edges representing dependencies. This
    // graph includes both the ports of this actor and the ports of all
    // deeply contained opaque actors.
    private void _constructDetailedDependencyGraph() {
        // get the actor
        CompositeActor actor = (CompositeActor) getContainer();

        // initialize the detailed dependency graph
        _detailedDependencyGraph = _constructDisconnectedDependencyGraph();

        // NOTE: for special domains, like Giotto, the inputs
        // and outputs are always independent if they are not
        // directly connected. Such domains have no use for
        // function dependency information.
        // Here we add constraints on which actors to be used to
        // construct graph. For example, in a composite actor, all
        // the contained atomic and composite actors are included
        // to construct the function dependency. While in a modal
        // model, only the refinement(s) of the current state is
        // considered.
        // FIXME: the situation that a state has multiple refinements
        // has to be considered.
        List embeddedActors = _getEntities();

        // initialize the list of actors
        // Sink actors
        _sinkActors = new LinkedList();

        // Source actors
        _sourceActors = new LinkedList();

        // Transformer actors.
        _transformers = new LinkedList();

        // catogrize the embedded actors into three kinds:
        _categorizeActors(embeddedActors);

        // Constuct the portsGraph according to the communication dependencies,
        // i.e., data go from source actors to transformers, and then to sink
        // actors.
        _mergeActorsGraph(_sourceActors);
        _mergeActorsGraph(_transformers);
        _mergeActorsGraph(_sinkActors);

        // To avoid the potential memory leakage,
        // clear the sink, source, and transformer lists explictly here.
        _sinkActors = null;
        _sourceActors = null;
        _transformers = null;

        // Next, create the directed edges according to the connections at
        // the container level, communication dependencies between internal
        // actors
        List outputPorts = actor.outputPortList();
        Iterator embeddedActorsIterator = embeddedActors.iterator();

        // iterate all embedded actors (including opaque composite actors
        // and flattening transparent composite actors)
        while (embeddedActorsIterator.hasNext()) {
            Actor embeddedActor = (Actor) embeddedActorsIterator.next();

            // Find the successor of the output ports of current actor.
            Iterator successors = embeddedActor.outputPortList().iterator();

            while (successors.hasNext()) {
                IOPort outPort = (IOPort) successors.next();

                // Find the inside ports connected to outPort.
                // NOTE: sinkPortList() is an expensive operation,
                // and it may return ports that are not physically
                // connected (as in wireless ports).  Hence, we
                // use getRemoteReceivers() here. EAL
                Receiver[][] receivers = outPort.getRemoteReceivers();

                for (int i = 0; i < receivers.length; i++) {
                    // FIXME: For ParameterPort, it is possible that
                    // the downstream receivers are null. It is a
                    // unresolved issue about the semantics of Parameter
                    // Port considering the lazy evaluation of variables.
                    if (receivers[i] != null) {
                        for (int j = 0; j < receivers[i].length; j++) {
                            IOPort ioPort = receivers[i][j].getContainer();

                            if (embeddedActors.contains(ioPort.getContainer())
                                    || outputPorts.contains(ioPort)) {
                                _detailedDependencyGraph.addEdge(outPort, ioPort);
                            }
                        }
                    }
                }
            }
        }

        // Last, connect the container inputs to the inside
        // ports receiving tokens from these inputs.
        Iterator inputs = actor.inputPortList().listIterator();

        while (inputs.hasNext()) {
            IOPort inputPort = (IOPort) inputs.next();

            // Find the inside ports connected to this input port.
            // NOTE: insideSinkPortList() is an expensive operation,
            // and it may return ports that are not physically
            // connected (as in wireless ports).  Hence, we
            // use deepGetReceivers() here. EAL
            Receiver[][] receivers = inputPort.deepGetReceivers();

            for (int i = 0; i < receivers.length; i++) {
                for (int j = 0; j < receivers[i].length; j++) {
                    IOPort ioPort = receivers[i][j].getContainer();
                    Actor ioPortContainer = (Actor) ioPort.getContainer();

                    // The receivers may belong to either the inputs of
                    // contained actors, or the outputs of the container.
                    if (embeddedActors.contains(ioPortContainer)
                            || actor.equals(ioPortContainer)) {
                        _detailedDependencyGraph.addEdge(inputPort, ioPort);
                    }
                }
            }
        }
    }

    // Merge dependency graphs of the internal actors into
    // the dependency graph of the actor.
    private void _mergeActorsGraph(List actorList) {
        Iterator actors = actorList.iterator();

        while (actors.hasNext()) {
            Actor embeddedActor = (Actor) actors.next();
            FunctionDependency functionDependency = embeddedActor
                .getFunctionDependency();

            if (functionDependency != null) {
                _detailedDependencyGraph.addGraph(functionDependency
                        .getDependencyGraph());
            } else {
                throw new InternalErrorException("FunctionDependency can "
                        + "not be null. Check all four types of function "
                        + "dependencies. There must be something wrong.");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The detailed dependency graph that includes both the ports of
    // this actor and the ports of all deeply contained opaque actors.
    private DirectedGraph _detailedDependencyGraph;

    // Sink actors
    private List _sinkActors;

    // Source actors
    private List _sourceActors;

    // Transformer actors.
    private List _transformers;
}
