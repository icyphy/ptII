/* A Scheduler for the GR domain

 Copyright (c) 2000 The Regents of the University of California.
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

@ProposedRating red (chf@eecs.berkeley.edu)
@AcceptedRating red (chf@eecs.berkeley.edu)
*/

package ptolemy.domains.gr.kernel;

import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.*;
import ptolemy.data.*;
import ptolemy.graph.*;

import java.util.*;

/////////////////////////////////////////////////////////////////////
//// GRScheduler
/**

A scheduler that implements scheduling of the active parts of a GR
scene graph. The scene graph is assumed to be a directed-acyclic-graph.
Scheduling is done by performing a topological sort on all the actors.

@see ptolemy.actor.sched.Scheduler

@author C. Fong
@version $Id$
*/
public class GRScheduler extends Scheduler {

    /** Construct a scheduler with no container (director)
     *  in the default workspace, the name of the scheduler is
     *  "Scheduler".
     */
    public GRScheduler() {
        super();
    }

    /** Construct a scheduler in the given workspace with the name
     *  "Scheduler".
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking.
     */
    public GRScheduler(Workspace workspace) {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the scheduling sequence.  An exception will be thrown if the
     *  graph is not schedulable.  This occurs in the following circumstances:
     *  <ul>
     *  <li>The graph is not a connected graph. // FIXME. not checked
     *  <li>The graph is not acyclic 
     *  <li>Multiple output ports are connected to the same broadcast
     *  relation. (equivalent to a non-deterministic merge) // FIXME: check
     *  </ul>
     *
     * @return An Enumeration of the deeply contained opaque entities
     *  in the firing order.
     * @exception NotScheduleableException If the CompositeActor is not
     *  schedulable.
     */
    protected Enumeration _schedule() throws NotSchedulableException {
      
       // Clear the graph
        DirectedAcyclicGraph dag = new DirectedAcyclicGraph();

        GRDirector director = (GRDirector)getContainer();
        if(director == null) {
            return null;
        }

        // If there is no container, there are no actors
        CompositeActor container = (CompositeActor)(director.getContainer());
        if(container == null) {
            return null;
        }

        CompositeActor castContainer = (CompositeActor) container;
        int count = 0;

        // First, include all actors as nodes in the graph.
        // get all the contained actors.
        List entities = castContainer.deepEntityList();
        Iterator actors = entities.iterator();
        int actorCount =  entities.size();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            dag.add(actor);
        }
        
        actors = castContainer.deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            // Find the successors of the actor
            LinkedList successors = new LinkedList();
            Iterator outports = actor.outputPortList().iterator();
            while(outports.hasNext()) {
                IOPort outPort = (IOPort) outports.next();
                Iterator inPorts = outPort.deepConnectedInPortList().iterator();
                while(inPorts.hasNext()) {
                    IOPort inPort = (IOPort)inPorts.next();
                    Actor post = (Actor)inPort.getContainer();
                    if(!successors.contains(post)) {
                        successors.addLast(post);
                    }
                }
            }
        
            // Add the edge in the DAG
            Iterator succeedingActors = successors.iterator();
            while (succeedingActors.hasNext()) {
                Actor connectedActor = (Actor) succeedingActors.next();
                dag.addEdge(actor, connectedActor);
            }
        }
        
        
        // NOTE: The following may be a very costly test, which is why
        // it it done at the end.  However, this means that we cannot
        // report an actor in the directed cycle.  Probably DirectedGraph
        // should be modified to enable such reporting.
        if (!dag.isAcyclic()) {
            Object[] cycleNodes = dag.cycleNodes();
            StringBuffer names = new StringBuffer();
            for (int i = 0; i < cycleNodes.length; i++) {
                if (cycleNodes[i] instanceof Nameable) {
                    if (i > 0) names.append(", ");
                    names.append(((Nameable)cycleNodes[i]).getFullName());
                }
            }
            throw new NotSchedulableException(this,
                    "GR graph is not acyclic: " + names.toString());
        }

        if (dag.top() == null) {
            // FIXME: throw exception here
        }
        
        
        LinkedList result = new LinkedList();
        Object[] sorted = dag.topologicalSort();
        for(int counter=0; counter < actorCount ;counter++) {
            result.addLast(sorted[counter]);
        }

        setValid(true);
        return Collections.enumeration(result);
    }
}
