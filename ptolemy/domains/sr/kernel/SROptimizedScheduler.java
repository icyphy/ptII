/* An optimized scheduler for the SR domain.

 Copyright (c) 2000-2001 The Regents of the University of California.
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
import ptolemy.actor.IOPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.graph.DirectedGraph;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// SROptimizedScheduler
/**
A scheduler the Synchronous Reactive (SR) domain.
<p>
FIXME: Add comments.

@author Paul Whitaker
@version $Id$
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
     *  method will call it when the schedule is invalid.  So it is not
     *  synchronized on the workspace.
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

        List actorList = compositeActor.deepEntityList();

        DirectedGraph dependencyGraph = new DirectedGraph();
        
        HashMap inputPortToActor = new HashMap();
        HashMap outputPortToActor = new HashMap();

        // Build maps from ports to actors and add all output ports to
        // the dependency graph
        Iterator actorIterator = actorList.iterator();
        while (actorIterator.hasNext()) {
            Actor actor = (Actor) actorIterator.next();
            List outputList = actor.outputPortList();
            Iterator outputIterator = outputList.iterator();
            while (outputIterator.hasNext()) {
                IOPort outputPort = (IOPort) outputIterator.next();
                outputPortToActor.put(outputPort, actor);
                dependencyGraph.add(outputPort);
            }
            List inputList = actor.inputPortList();
            Iterator inputIterator = inputList.iterator();
            while (inputIterator.hasNext()) {
                IOPort inputPort = (IOPort) inputIterator.next();
                inputPortToActor.put(inputPort, actor);
            }
        }

        // Add edges in the dependency graph corresponding to output ports
        // that depend on other outputs
        actorIterator = actorList.iterator();
        while (actorIterator.hasNext()) {
            Actor actor = (Actor) actorIterator.next();
            List outputList = actor.outputPortList();
            Iterator outputIterator = outputList.iterator();
            while (outputIterator.hasNext()) {
                IOPort outputPort = (IOPort) outputIterator.next();
                List inputList = outputPort.deepConnectedInPortList();
                Iterator inputIterator = inputList.iterator();
                while (inputIterator.hasNext()) {
                    IOPort inputPort = (IOPort) inputIterator.next();
                    Actor dependentActor =
                        (Actor) inputPortToActor.get(inputPort);
                    List dependentOutputList = dependentActor.outputPortList();
                    Iterator dependentOutputIterator = 
                        dependentOutputList.iterator();
                    while (dependentOutputIterator.hasNext()) {
                        IOPort dependentOutput =
                            (IOPort) dependentOutputIterator.next();
                        IOPort[] successors = (IOPort[])
                            dependencyGraph.successorSet(outputPort);
                        boolean alreadySuccessor = false;
                        for (int i = 0; i < successors.length; i++) {
                            if (successors[i] == dependentOutput)
                                alreadySuccessor = true;
                        }
                        if (!alreadySuccessor) {
                            dependencyGraph.addEdge(outputPort,
                                    dependentOutput);
                        }
                    }
                }
            }
        }

        List outputPortSchedule = _scheduleDependencyGraph(dependencyGraph);

        Schedule schedule = new Schedule();
        Iterator outputPortScheduleIterator = outputPortSchedule.iterator();

        while (outputPortScheduleIterator.hasNext()) {
            IOPort outputPort = (IOPort) outputPortScheduleIterator.next();
            Actor actor = (Actor) outputPortToActor.get(outputPort);
            Firing firing = new Firing();
            firing.setActor(actor);
            schedule.add(firing);
        }
        return schedule;

    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                 ////

    /** Return a list corresponding to the schedule of the dependency graph.
     *  @return A List representing the scheduling sequence.
     */
    private List _scheduleDependencyGraph(DirectedGraph dependencyGraph) {

        List schedule = new LinkedList();
        return schedule;

    }

}










