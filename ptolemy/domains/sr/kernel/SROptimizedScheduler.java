/* An optimized scheduler for the SR domain.

 Copyright (c) 2000-2003 The Regents of the University of California.
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

@ProposedRating Red (pwhitake@eecs.berkeley.edu)
@AcceptedRating Red (pwhitake@eecs.berkeley.edu)
*/

package ptolemy.domains.sr.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.FunctionDependency;
import ptolemy.actor.IOPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Node;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// SROptimizedScheduler
/**
A scheduler the Synchronous Reactive (SR) domain.  This scheduler returns
a static schedule for the graph.  The schedule guarantees that the values will
converge to a fixed-point.
<p>
The recursive scheduling algorithm is due to Stephen Edwards, and is
described in his Ph.D. thesis.  First, a dependency graph is constructed, and
the strongly connected components (SCC) are determined.  A schedule for each
SCC is obtained by separating the sub-graph into a head and a tail, and then
recursively applying this algorithm to both the head and the tail.  The
schedule for the SCC is (<i>TH</i>)<super><i>n</i></super><i>T</i> where
<i>H</i> and <i>T</i> are the schedules of the head and tail, respectively, and
<i>n</i> is the number of nodes in the head and represents the number of
repetitions of the parenthesized expression.  Finally, the schedules of the
top-level SCCs are concatenated in topological order to obtain the schedule
for the entire graph.

@author Paul Whitaker
@version $Id$
@since Ptolemy II 2.0
@see ptolemy.domains.sr.kernel.SRDirector
*/
public class SROptimizedScheduler extends Scheduler {

    /** Construct a SR scheduler with no container (director)
     *  in the default workspace.
     */
    public SROptimizedScheduler() {
        super();
    }

    /** Construct a SR scheduler in the given workspace.
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace Object for synchronization and version tracking.
     */
    public SROptimizedScheduler(Workspace workspace) {
        super(workspace);
    }

    /** Construct a scheduler in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this scheduler.
     *  @exception IllegalActionException If the scheduler is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public SROptimizedScheduler(Director container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the scheduling sequence.
     *  Overrides _getSchedule() method in the base class.
     *
     *  This method should not be called directly, rather the getSchedule()
     *  method (which is defined in the superclass) will call it when the
     *  schedule is invalid.  This method is not synchronized on the workspace.
     *
     *  @return A schedule representing the scheduling sequence.
     *  @exception NotSchedulableException If the CompositeActor is not
     *   schedulable.
     */
    protected Schedule _getSchedule() throws NotSchedulableException {

        StaticSchedulingDirector director =
            (StaticSchedulingDirector) getContainer();

        if (director == null) {
            throw new NotSchedulableException(this, "SROptimizedScheduler "
                    + "cannot schedule graph with no director.");
        }

        CompositeActor compositeActor =
            (CompositeActor) (director.getContainer());

        if (compositeActor == null) {
            throw new NotSchedulableException(this, "SROptimizedScheduler "
                    + "cannot schedule graph with no container.");
        }

        FunctionDependency functionDependency = 
            compositeActor.getFunctionDependencies();
            
        Object[] cycleNodes = functionDependency.getCycleNodes();
        if (cycleNodes.length != 0) {
            StringBuffer names = new StringBuffer();
            for (int i = 0; i < cycleNodes.length; i++) {
                if (cycleNodes[i] instanceof Nameable) {
                    if (i > 0) names.append(", ");
                    names.append(((Nameable)cycleNodes[i])
                        .getContainer().getFullName());
                }
            }
            MessageHandler.error("There are strict cycle loops in the model:"
                 + names.toString() + "\n"
                 +  " The results may contain unknowns.  This optimized" +
                    "scheduler does not handle this model. Try the " +
                    "randomized scheduler instead.");
        }

        DirectedAcyclicGraph dependencyGraph = 
            functionDependency.getDetailedPortsGraph().toDirectedAcyclicGraph();
            
        if (_debugging) {
            _debug("## dependency graph is:" + dependencyGraph.toString());
        }
        Object[] sort = (Object[]) dependencyGraph.topologicalSort();
        if (_debugging) {
            _debug("## Result of topological sort (highest depth to lowest):");
        }
        
        Schedule schedule = new Schedule();
        Actor lastActor = null;
        
        for (int i = 0; i < sort.length; i++) {
            IOPort ioPort = (IOPort)sort[i];
            Actor actor = (Actor) ioPort.getContainer();
            // We record the information of last actor.
            // If some consecutive ports belong to the
            // same actor, we only schedule that actor once.
            if (lastActor == null) {
                lastActor = actor;
            } else {
                if (lastActor.equals(actor)) {
                   continue; 
                } else {
                    lastActor = actor;
                }
            }
            Firing firing = new Firing(actor);
            schedule.add(firing);
            if (_debugging) _debug(((Nameable)actor).getFullName(),
                    "depth: " + i);
        }

        if (_debugging) _debug("## End of topological sort.");

        return schedule;

//        List actorList = compositeActor.deepEntityList();
//
//        DirectedGraph dependencyGraph = new DirectedGraph();
//
//        HashMap inputPortToActor = new HashMap();
//        HashMap outputPortToActor = new HashMap();
//
//        // Build maps from ports to actors and add all output ports to
//        // the dependency graph
//        Iterator actorIterator = actorList.iterator();
//        while (actorIterator.hasNext()) {
//            Actor actor = (Actor) actorIterator.next();
//            List outputList = actor.outputPortList();
//            if (outputList.isEmpty()) {
//                // The actor must still fire even though it has no output
//                // ports, so we add the actor itself to the dependency graph.
//                dependencyGraph.addNodeWeight(actor);
//            } else {
//                Iterator outputIterator = outputList.iterator();
//                while (outputIterator.hasNext()) {
//                    IOPort outputPort = (IOPort) outputIterator.next();
//                    outputPortToActor.put(outputPort, actor);
//                    dependencyGraph.addNodeWeight(outputPort);
//                }
//            }
//            List inputList = actor.inputPortList();
//            Iterator inputIterator = inputList.iterator();
//            while (inputIterator.hasNext()) {
//                IOPort inputPort = (IOPort) inputIterator.next();
//                inputPortToActor.put(inputPort, actor);
//            }
//        }
//
//        // Add edges in the dependency graph corresponding to output ports
//        // that depend on other outputs
//        actorIterator = actorList.iterator();
//        while (actorIterator.hasNext()) {
//            Actor actor = (Actor) actorIterator.next();
//            List outputList = actor.outputPortList();
//            Iterator outputIterator = outputList.iterator();
//            while (outputIterator.hasNext()) {
//                IOPort outputPort = (IOPort) outputIterator.next();
//                int referenceDepth = outputPort.depthInHierarchy();
//                List inputList = outputPort.deepConnectedInPortList();
//                Iterator inputIterator = inputList.iterator();
//                while (inputIterator.hasNext()) {
//                    IOPort inputPort = (IOPort) inputIterator.next();
//                    if (inputPort.depthInHierarchy() < referenceDepth) {
//                        // Port is higher in the hierarchy... skip.
//                        continue;
//                    }
//                    Actor dependentActor =
//                        (Actor) inputPortToActor.get(inputPort);
//                    List dependentOutputList = dependentActor.outputPortList();
//                    if (dependentOutputList.isEmpty()) {
//                        if (!dependencyGraph.edgeExists(outputPort,
//                                dependentActor)) {
//                            dependencyGraph.addEdge(outputPort,
//                                    dependentActor);
//                        }
//                    } else {
//                        Iterator dependentOutputIterator =
//                            dependentOutputList.iterator();
//                        while (dependentOutputIterator.hasNext()) {
//                            Object dependentOutput =
//                                dependentOutputIterator.next();
//                            if (!dependencyGraph.edgeExists(outputPort,
//                                    dependentOutput)) {
//                                dependencyGraph.addEdge(outputPort,
//                                        dependentOutput);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        List scheduleList = _scheduleDependencyGraph(dependencyGraph);
//
//        Schedule schedule = new Schedule();
//        Iterator scheduleIterator = scheduleList.iterator();
//
//        while (scheduleIterator.hasNext()) {
//            Object element = ((Node) scheduleIterator.next()).getWeight();
//            Actor actor;
//            if (element instanceof Actor) {
//                actor = (Actor) element;
//            } else {
//                actor = (Actor) outputPortToActor.get(element);
//            }
//            Firing firing = new Firing();
//            firing.setActor(actor);
//            schedule.add(firing);
//        }
//
//        return schedule;

    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

//    /** Return a list corresponding to the nodes in the head of an SCC.
//     *  @return A List representing the head of an SCC.
//     */
//    private Object[] _headOf(DirectedGraph dependencyGraph) {
//
//        Object[] nodes = dependencyGraph.nodes().toArray();
//        Node bestNode = (Node)(nodes[0]);
//        int smallestSuccessorSet = nodes.length - 1;
//
//        for (int i = 0; i < nodes.length; i++) {
//            Node node = (Node)(nodes[i]);
//            int numberOfSuccessors = dependencyGraph.successors(node).size();
//            if (numberOfSuccessors < smallestSuccessorSet) {
//                smallestSuccessorSet = numberOfSuccessors;
//                bestNode = node;
//            }
//        }
//
//        return dependencyGraph.successors(bestNode).toArray();
//    }
//
//    /** Return a list corresponding to the schedule of the dependency graph.
//     *  @return A List representing the scheduling sequence.
//     */
//    private List _scheduleDependencyGraph(DirectedGraph dependencyGraph) {
//
//        List scheduleList = new LinkedList();
//
//        DirectedGraph[] sccs = dependencyGraph.sccDecomposition();
//        // The graph package returns the sccs in topological order.
//
//        for (int i = 0; i < sccs.length; i++) {
//
//            DirectedGraph scc = sccs[i];
//
//            if (scc.nodeCount() == 1) {
//                Object[] nodes = scc.nodes().toArray();
//                scheduleList.add(nodes[0]);
//            } else {
//                Object[] head = _headOf(scc);
//                Object[] allNodes = scc.nodes().toArray();
//                int sizeOfGraph = scc.nodeCount();
//                int sizeOfHead = head.length;
//                int sizeOfTail = sizeOfGraph - sizeOfHead;
//                Object[] tail = new Object[sizeOfTail];
//                int counter = 0;
//                for (int j = 0; j < sizeOfGraph; j++) {
//                    Object node = allNodes[j];
//                    boolean inHead = false;
//                    for (int k = 0; k < sizeOfHead; k++) {
//                        Object headNode = head[k];
//                        if (headNode == node) inHead = true;
//                    }
//                    if (!inHead) {
//                        tail[counter] = node;
//                        counter++;
//                    }
//                }
//                DirectedGraph headGraph = (DirectedGraph)
//                    (scc.subgraph(Arrays.asList(head)));
//                DirectedGraph tailGraph = (DirectedGraph)
//                    (scc.subgraph(Arrays.asList(tail)));
//                List headScheduleList = _scheduleDependencyGraph(headGraph);
//                List tailScheduleList = _scheduleDependencyGraph(tailGraph);
//                for (int j = 0; j < sizeOfHead; j++) {
//                    scheduleList.addAll(tailScheduleList);
//                    scheduleList.addAll(headScheduleList);
//                }
//                scheduleList.addAll(tailScheduleList);
//            }
//        }
//
//        return scheduleList;
//
//    }

}

