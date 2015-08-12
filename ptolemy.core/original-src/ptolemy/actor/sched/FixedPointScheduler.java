/* A scheduler for fixed point directors.

 Copyright (c) 2006-2014 The Regents of the University of California.
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

import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// FixedPointScheduler

/**
 A scheduler for the FixedPointDirector.  This scheduler constructs
 a static schedule for a model by performing a topological sort on
 actors. Currently, in this class, each actor appears only once, but
 a more sophisticated scheduler may mention an actor more
 than once if the dependencies require it.

 @author Haiyang Zheng and Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
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
    public FixedPointScheduler(StaticSchedulingDirector container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the schedule. This method attempts to construct a schedule based
     *  on a topological sort of the graph (which uses causality interfaces).
     *  If there are cycles, no such sort is possible, and this method simply
     *  returns a schedule that lists the actors in their natural order in the
     *  container, which is the order in which they were created (unless that
     *  order has been modified through "bring to front" or "send to back").
     *  This method should not be called directly, but rather the getSchedule()
     *  method (which is defined in the superclass) will call it when the
     *  schedule is invalid.  This method is not synchronized on the workspace.
     *  @return A schedule.
     */
    @Override
    protected Schedule _getSchedule() {
        StaticSchedulingDirector director = (StaticSchedulingDirector) getContainer();
        if (director == null) {
            throw new NotSchedulableException(this, "No director.  ");
        }
        CompositeActor compositeActor = (CompositeActor) director
                .getContainer();
        if (compositeActor == null) {
            throw new NotSchedulableException(this, "No container.");
        }
        CausalityInterfaceForComposites causality = (CausalityInterfaceForComposites) compositeActor
                .getCausalityInterface();
        List<Actor> sortedActors;
        try {
            sortedActors = causality.topologicalSort();
        } catch (IllegalActionException ex) {
            sortedActors = compositeActor.deepEntityList();
        }
        Schedule schedule = new Schedule();
        if (_debugging) {
            _debug("## Schedule generated:");
        }
        for (Actor actor : sortedActors) {
            Firing firing = new Firing(actor);
            schedule.add(firing);
            if (_debugging) {
                _debug(" - " + actor.getFullName());
            }
        }
        if (_debugging) {
            _debug("## End of schedule.");
        }
        setValid(true);
        return schedule;
    }
}
