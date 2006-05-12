/* A scheduler for fixed point directors.

 Copyright (c) 2000-2006 The Regents of the University of California.
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

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
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
 @since Ptolemy II 6.0
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
            throw new NotSchedulableException(this, "No director.");
        }

        CompositeActor compositeActor = (CompositeActor) (director
                .getContainer());

        if (compositeActor == null) {
            throw new NotSchedulableException(this, "No container.");
        }

        Schedule schedule = new Schedule();

        FunctionDependencyOfCompositeActor functionDependency
                = (FunctionDependencyOfCompositeActor) compositeActor
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
            if (!ioPort.isOutput() && (ioPort.numberOfSources() == 0)) {
                continue;
            }

            actor = (Actor) ioPort.getContainer();

            // If the actor is the container of this director, 
            // meaning this director is not at the top level,
            // then skip this actor. The container of the director
            // should not be listed in the schedule.
            if (actor == compositeActor) {
                continue;
            }

            Firing firing = new Firing(actor);
            schedule.add(firing);

            if (_debugging) {
                _debug(actor.getFullName()
                        + " scheduled at position: " + i);
            }
        }

        if (_debugging) {
            _debug("## End of schedule.");
        }

        // Set the schedule to be valid.
        setValid(true);
        return schedule;
    }
}
