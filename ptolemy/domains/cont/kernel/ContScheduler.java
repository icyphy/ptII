/* The static scheduler for the continuous time domain.

 Copyright (c) 1998-2006 The Regents of the University of California.
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
package ptolemy.domains.cont.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.util.FunctionDependencyOfCompositeActor;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// ContScheduler

/**
 The Static scheduler for the CT domain.
 A continuous-time (sub)system can be mathematically represented as:<Br>
 <pre>
 <pre>    dx/dt = f(x, u, t)<Br>
 <pre>    y = g(x, u, t)<BR>
 </pre></pre></pre>
 where x is the state of the system, u is the input, y is the output,
 f() is the state transition map and g() is the output map.
 <P>
 The system is built using actors. That is, all the functions, f() and g(),
 are built up by chains of actors.  For higher order systems,
 x is a vector, built using more than one integrator.
 In general, actors that have the functionality of integration
 from their inputs to their outputs are called <I>dynamic actors</I>.
 Other actors are called <I>arithmetic actors</I>.
 <P>
 In continuous-time simulation, time progresses in a discrete way.
 The distance between two consecutive time points is called the
 <I>integration step size</I> or step size, for short. Some actors
 may put constraints on the choice of the step size.
 These actors are called <I>step size control actors</I>. Examples of step
 size control actors include integrators, which control the
 accuracy and speed of numerical ODE solutions, and event generators,
 which produce discrete events.
 <P>
 If there are loops of arithmetic actors,
 then the (sub)system are not schedulable, and a NotSchedulableException
 will be thrown when schedules are requested.
 <P>
 This scheduler is based on the CTScheduler by Jie Liu, Haiyang Zheng, and
 Ye Zhou. It eliminates the classification of actors into multiple categories
 that that scheduler relied on.

 @author Haiyang Zheng and Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 @see ptolemy.actor.sched.Scheduler
 */
public class ContScheduler extends Scheduler {
    
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
    public ContScheduler(Director container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the full name of this scheduler.
     *  @return The full name of this scheduler.
     */
    public String toString() {
        return getFullName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

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
        ContDirector director = (ContDirector) getContainer();
    
        if (director == null) {
            throw new NotSchedulableException(this, "SROptimizedScheduler "
                    + "cannot schedule graph with no director.");
        }
    
        CompositeActor compositeActor = (CompositeActor) (director
                .getContainer());
    
        if (compositeActor == null) {
            throw new NotSchedulableException(this, "SROptimizedScheduler "
                    + "cannot schedule graph with no container.");
        }
    
        FunctionDependencyOfCompositeActor functionDependency = 
            (FunctionDependencyOfCompositeActor) compositeActor
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
    
            MessageHandler.error("There are strict cycle loops in the model:"
                    + names.toString() + "\n"
                    + " The results may contain unknowns.  This optimized "
                    + "scheduler does not handle this model. Try the "
                    + "randomized scheduler instead.");
        }
    
        DirectedAcyclicGraph dependencyGraph = functionDependency
                .getDetailedDependencyGraph().toDirectedAcyclicGraph();
    
        if (_debugging) {
            _debug("## dependency graph is:" + dependencyGraph.toString());
        }
    
        Object[] sort = dependencyGraph.topologicalSort();
    
        if (_debugging) {
            _debug("## Result of topological sort (highest depth to lowest):");
        }
    
        Schedule schedule = new Schedule();
        Actor lastActor = null;
        Actor actor = null;
    
        for (int i = 0; i < sort.length; i++) {
            IOPort ioPort = (IOPort) sort[i];
    
            // If this ioPort is input but has no connections,
            // we ignore it.
            if (ioPort.isInput() && (ioPort.numLinks() == 0)) {
                continue;
            }
    
            actor = (Actor) ioPort.getContainer();
    
            // If the actor is the container of this director (which
            // can occur if this director is not at the top level),
            // then skip this actor. The container of the director
            // should not be listed in the schedule.
            if (actor == compositeActor) {
                continue;
            }
    
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
    
            if (_debugging) {
                _debug(((Nameable) actor).getFullName(), "depth: " + i);
            }
        }
    
        if (_debugging) {
            _debug("## End of topological sort.");
        }
    
        return schedule;
    }
}
