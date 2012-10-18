/* This is a first come first serve scheduler.

@Copyright (c) 2008-2012 The Regents of the University of California.
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

package ptolemy.domains.ptides.lib;

import ptolemy.actor.Actor;
import ptolemy.actor.ResourceScheduler;
import ptolemy.actor.util.Time;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/** This is a first come first serve scheduler.
 *
 * @author Patricia Derler
   @version $Id$
   @since Ptolemy II 0.2

   @Pt.ProposedRating Red (derler)
   @Pt.AcceptedRating Red (derler)
 */
public class NPFCFSCore extends ResourceScheduler {

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
    public NPFCFSCore(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    //                      public methods                           //

    /** Schedule a new actor for execution and return the next time
     *  this scheduler has to perform a reschedule.
     *  @param actor The actor to be scheduled.
     *  @param currentPlatformTime The current platform time.
     *  @param deadline The deadline - not used here.
     *  @param executionTime The execution time of the actor.
     *  @return Relative time when this Scheduler has to be executed
     *    again.
     *  @exception IllegalActionException Thrown if actor paramaters such
     *    as execution time or priority cannot be read.
     */
    @Override
    public Time schedule(Actor actor, Time currentPlatformTime,
            Double deadline, Time executionTime) throws IllegalActionException {
        super.schedule(actor, currentPlatformTime, deadline, executionTime);
        _lastActorFinished = false;
        if (currentlyExecuting == null) {
            currentlyExecuting = actor;
            event((NamedObj) currentlyExecuting,
                    currentPlatformTime.getDoubleValue(),
                    ExecutionEventType.START);
        }

        Time remainingTime = null;
        if (_remainingTimes.get(currentlyExecuting) == null) { // hasn't been scheduled
            remainingTime = executionTime;
            _remainingTimes.put(currentlyExecuting, executionTime);
        } else { //has been scheduled
            Time lasttime = _lastTimeScheduled.get(currentlyExecuting);
            Time timePassed = currentPlatformTime.subtract(lasttime);
            remainingTime = _remainingTimes.get(currentlyExecuting).subtract(
                    timePassed);
            _remainingTimes.put(currentlyExecuting, remainingTime);
        }

        _lastTimeScheduled.put(currentlyExecuting, currentPlatformTime);

        if (remainingTime.getDoubleValue() == 0.0) {
            event((NamedObj) currentlyExecuting,
                    currentPlatformTime.getDoubleValue(),
                    ExecutionEventType.STOP);

            _remainingTimes.put(currentlyExecuting, null);
            currentlyExecuting = null;
            _lastActorFinished = true;
        }
        return remainingTime;
    }

    ///////////////////////////////////////////////////////////////////
    //                      private variables                        //

    /** Currently executing actor. */
    private Actor currentlyExecuting;

}
