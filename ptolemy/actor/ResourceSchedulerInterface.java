/* This is the interface for a resource scheduler.

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

package ptolemy.actor;

import ptolemy.actor.util.Time;
import ptolemy.kernel.util.IllegalActionException;


/** This is the interface for a resource scheduler.
*
* @author Patricia Derler
  @version $Id$
  @since Ptolemy II 0.2

  @Pt.ProposedRating Red (derler)
  @Pt.AcceptedRating Red (derler)
*/
public interface ResourceSchedulerInterface {

    /** Initialize local variables and if this resource
     *  scheduler wants to be fired at a future time, return
     *  this time.
     * @return Next time this scheduler requests a firing.
     * @exception IllegalActionException Thrown if list of actors
     *   scheduled by this scheduler cannot be retrieved.
     */
    public Time initialize() throws IllegalActionException;

    /** Clean up.
     *  @exception IllegalActionException Thrown by super class.
     */
    public void wrapup() throws IllegalActionException;

    /** Schedule a new actor for execution and return the next time
     *  this scheduler has to perform a reschedule. Derived classes
     *  must implement this method to actually schedule actors, this
     *  base class implementation just creates events for scheduler
     *  activity that is displayed in the plotter.
     *  @param actor The actor to be scheduled.
     *  @param currentPlatformTime The current platform time.
     *  @param deadline The deadline of the event.
     *  @param executionTime The execution time of the actor.
     *  @return Relative time when this Scheduler has to be executed
     *    again.
     *  @exception IllegalActionException Thrown if actor parameters such
     *    as execution time or priority cannot be read.
     */
    public Time schedule(Actor actor, Time currentPlatformTime,
            Double deadline, Time executionTime) throws IllegalActionException;

    /** If the last actor that was scheduled finished execution
     *  then this method returns true.
     *  @return True if last actor that was scheduled finished
     *   execution.
     */
    public boolean lastScheduledActorFinished();
}
