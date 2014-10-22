/* A randomized scheduler for the SR domain.

 Copyright (c) 2000-2014 The Regents of the University of California.
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
package ptolemy.domains.sr.kernel;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SRRandomizedScheduler

/**
 A scheduler the Synchronous Reactive (SR) domain.  This schedule is simply
 a random ordering of all the actors.  The director should cycle through this
 schedule repeatedly, deciding which actors to fire, until it determines
 that the given iteration has converged.  The ordering of the schedule has
 no effect on the results of an iteration.

 @author Paul Whitaker
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Green (pwhitake)
 @Pt.AcceptedRating Green (pwhitake)
 @see ptolemy.domains.sr.kernel.SRDirector
 */
public class SRRandomizedScheduler extends Scheduler {
    /** Construct a SR scheduler with no container (director)
     *  in the default workspace.
     */
    public SRRandomizedScheduler() {
        super();
    }

    /** Construct a SR scheduler in the given workspace.
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace Object for synchronization and version tracking.
     */
    public SRRandomizedScheduler(Workspace workspace) {
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
    public SRRandomizedScheduler(Director container, String name)
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
    @Override
    protected Schedule _getSchedule() throws NotSchedulableException {
        StaticSchedulingDirector director = (StaticSchedulingDirector) getContainer();

        if (director == null) {
            throw new NotSchedulableException(this, "SRRandomizedScheduler "
                    + "cannot schedule graph with no director.");
        }

        CompositeActor compositeActor = (CompositeActor) director
                .getContainer();

        if (compositeActor == null) {
            throw new NotSchedulableException(this, "SRRandomizedScheduler "
                    + "cannot schedule graph with no container.");
        }

        List actorList = compositeActor.deepEntityList();

        Collections.shuffle(actorList);

        Schedule schedule = new Schedule();
        Iterator actorIterator = actorList.iterator();

        while (actorIterator.hasNext()) {
            Actor actor = (Actor) actorIterator.next();
            Firing firing = new Firing();
            firing.setActor(actor);
            schedule.add(firing);
        }

        return schedule;
    }
}
