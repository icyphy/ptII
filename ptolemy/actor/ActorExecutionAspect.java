/* An interface for objects that can intervene in the execution of actors.

@Copyright (c) 2011-2013 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

import ptolemy.actor.ExecutionAspectListener.ExecutionEventType;
import ptolemy.actor.util.Time;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/** An interface for objects that can intervene in the execution of actors.
 *  A director that executes actors decorated by a resource scheduler has to
 *  consult the resource scheduler before firing the actor. If the resource
 *  scheduler returns that there are not enough resources available to
 *  fire the actor, the firing must be postponed.
 *  <p>
 *  For example, a resource scheduler could represent a CPU and actors scheduled
 *  on a CPU have execution times. The resource scheduler takes care of scheduling
 *  the actors according to a given scheduling strategy and keeping track of the
 *  remaining execution times.
 *
 *  @author Patricia Derler, Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public interface ActorExecutionAspect extends Decorator {

    /** Add schedule listener. If necessary, initialize list of actors
     *  scheduled by this resource scheduler.
     *  @param listener The listener to be added.
     *  @exception IllegalActionException If an error occurs in the initialization
     *  of actors scheduled by this resource scheduler.
     */
    public void addExecutingListener(ExecutionAspectListener listener) throws IllegalActionException;

    /** Iterate through all entities deeply contained by the container,
     *  record for each that it is not executing.
     *  @exception IllegalActionException If the decorator parameters cannot be read.
     */
    public void initializeDecoratedActors() throws IllegalActionException;

    /** Get the execution time of an actor. If the actor does not have an attribute
     *  specifying the execution time, return the minimum execution time.
     * @param actor The actor.
     * @return The execution time.
     * @exception IllegalActionException Thrown in attribute or token cannot be read.
     */
    public double getExecutionTime(Actor actor) throws IllegalActionException;

    /** Return whether an actor is currently waiting for a resource.
     * @param actor The actor that might be waiting for a resource.
     * @return True if the actor is waiting for a resource.
     */
    public boolean isWaitingForResource(Actor actor);

    /** Check whether last actor that was scheduled on this resource
     *  scheduler finished execution.
     *  @return True if last actor that requested to be scheduled
     *  finished.
     */
    public boolean lastScheduledActorFinished();

    /** Notify execution listeners about rescheduling events.
     * @param entity Entity that is being scheduled.
     * @param time Time when entity is being scheduled.
     * @param eventType Type of event.
     */
    public void notifyExecutionListeners(NamedObj entity, Double time, ExecutionEventType eventType);

    /** Perform rescheduling actions when no new actor requests to be
         *  scheduled.
         * @param environmentTime The outside time.
         * @return Relative time when this Scheduler has to be executed
         *    again to perform rescheduling actions.
         * @exception IllegalActionException Thrown in subclasses.
         */
        public Time schedule(Time environmentTime) throws IllegalActionException;

    /** Schedule the actor. In this base class, do nothing.  Derived
     *  classes should schedule the actor.
     *  @param actor The actor to be scheduled.
     *  @param environmentTime The current platform time.
     *  @param deadline The deadline timestamp of the event to be scheduled.
     *  This can be the same as the environmentTime.
     *  @param executionTime The execution time of the actor.
     *  @return Relative time when this Scheduler has to be executed
     *    again to perform rescheduling actions.  In this base class, null
     *    is returned.
     *  @exception IllegalActionException Thrown if actor parameters such
     *    as execution time or priority cannot be read.
     */
    public Time schedule(Actor actor, Time environmentTime, Time deadline,
            Time executionTime) throws IllegalActionException;

    /** Remove schedule listener.
     * @param listener The listener to be removed.
     */
    public void removeExecutionListener(ExecutionAspectListener listener);

}
