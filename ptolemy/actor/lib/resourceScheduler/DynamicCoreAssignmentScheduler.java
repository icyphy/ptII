/* This resource scheduler dynamically assigns actors to other
 * resource schedulers.

@Copyright (c) 2008-2013 The Regents of the University of California.
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

package ptolemy.actor.lib.resourceScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.util.Time;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/** This resource scheduler dynamically assigns actors to other
 * resource schedulers.
 * @author Patricia Derler
   @version $Id$
   @since Ptolemy II 0.2

   @Pt.ProposedRating Red (derler)
   @Pt.AcceptedRating Red (derler)
 */
public class DynamicCoreAssignmentScheduler extends ResourceScheduler {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public DynamicCoreAssignmentScheduler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    //                           public methods                      //

    /** Initialize local variables.
     * @exception IllegalActionException Thrown if list of actors
     *   scheduled by this scheduler cannot be retrieved.
     */
    @Override
    public Time initialize() throws IllegalActionException {
        // TODO Auto-generated method stub
        super.initialize();
        _remainingTimeOnCore = new HashMap<ResourceScheduler, Time>();
        return null;
    }

    /** Schedule a new actor for execution on the next available
     *  scheduler and return the next time
     *  this scheduler has to perform a reschedule.
     *  @param actor The actor to be scheduled.
     *  @param currentPlatformTime The current platform time.
     *  @param deadline The deadline of the event.
     *  @param executionTime The execution time of the actor.
     *  @return Relative time when this Scheduler has to be executed
     *    again.
     *  @exception IllegalActionException Thrown if actor paramaters such
     *    as execution time or priority cannot be read.
     */
    @Override
    public Time _schedule(Actor actor, Time currentPlatformTime,
            Time deadline, Time executionTime) throws IllegalActionException {
        super._schedule(actor, currentPlatformTime, deadline, executionTime);
        Time minimumRemainingTime = null;
        // Check if is already executing somewhere.
        for (NamedObj schedulerActor : _actors) {
            ResourceScheduler scheduler = (ResourceScheduler) schedulerActor;
            if (scheduler.getRemainingTime(actor) != null
                    && scheduler.getRemainingTime(actor).getDoubleValue() > 0.0) {
                // This actor is currently executing on this scheduler.
                Time time = scheduler._schedule(actor, currentPlatformTime,
                        deadline, executionTime);
                event(scheduler, currentPlatformTime.getDoubleValue(),
                        ExecutionEventType.START);
                if (time.getDoubleValue() == 0.0) {
                    event(scheduler, currentPlatformTime.getDoubleValue(),
                            ExecutionEventType.STOP);
                }
                _remainingTimeOnCore.put(scheduler, time);
                _lastActorFinished = scheduler.lastActorFinished();
                return time;
            }
        }

        // Its not executing anywhere, find free core.
        for (NamedObj schedulerActor : _actors) {
            ResourceScheduler scheduler = (ResourceScheduler) schedulerActor;
            if (scheduler == this) {
                continue;
            }
            Time remainingTime = _remainingTimeOnCore.get(scheduler);
            if (remainingTime == null || remainingTime.getDoubleValue() == 0.0) {
                Time time = scheduler._schedule(actor, currentPlatformTime,
                        deadline, executionTime);
                event(scheduler, currentPlatformTime.getDoubleValue(),
                        ExecutionEventType.START);
                if (time.getDoubleValue() == 0.0) {
                    event(scheduler, currentPlatformTime.getDoubleValue(),
                            ExecutionEventType.STOP);
                }
                _remainingTimeOnCore.put(scheduler, time);
                _lastActorFinished = scheduler.lastActorFinished();
                return time;
            } else {
                if (minimumRemainingTime == null
                        || minimumRemainingTime.compareTo(remainingTime) > 0) {
                    minimumRemainingTime = remainingTime;
                }
            }
        }
        return minimumRemainingTime;
    }

    ///////////////////////////////////////////////////////////////////
    //                        protected methods                      //

    /** Override the base class to list all contained resource schedulers
     *  instead of actors.
     *  @exception IllegalActionException Thrown if actor parameters
     *    cannot be read.
     */
    @Override
    protected void _initializeActorsToSchedule()
            throws IllegalActionException {
        _actors = new ArrayList<NamedObj>();

        for (Attribute attribute : (List<Attribute>) this.attributeList()) {
            if (attribute instanceof Parameter) {
                Token paramToken = ((Parameter) attribute).getToken();
                if (paramToken instanceof ObjectToken) {
                    Object paramObject = ((ObjectToken) paramToken).getValue();
                    if (paramObject instanceof ResourceScheduler) {
                        // FIXME: Shouldn't these resource schedulers be initialized?
                        _actors.add((ResourceScheduler) paramObject);
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //                      private variables                        //

    private HashMap<ResourceScheduler, Time> _remainingTimeOnCore;

}
