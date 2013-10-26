/* Interface for real-time system tasks.

@Copyright (c) 2013 The Regents of the University of California.
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

package ptolemy.apps.hardrealtime;

import ptolemy.actor.Actor;
import ptolemy.actor.util.Time;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Task

/**
 <p>In a real-time system the contained tasks and the scheduler communicate through
 the set of released jobs: the tasks release jobs in that set and the scheduler
 executes them.
 </p><p>
 Since many interesting task types are aware of when they
 will release their next job in the future, the single thread of control
 implementation presented in {@link EDF}, advances time in a way that respects
 those release times of tasks. For that, it has to be possible to query
 all tasks included in a real-time system when they will release their next job.
 That functionality is guaranteed by this interface.
 </p>

 @author Christos Stergiou
 @version $Id$
 @Pt.ProposedRating Red (chster)
 @Pt.AcceptedRating Red (chster)
 */
public interface Task extends Actor {
    /** Return the next time that the task will fire, or release a new job.
     *  @return The next time that the task releases a job.
     *  @throws IllegalActionException If the next time cannot be computed.
     */
    public Time nextFireTime() throws IllegalActionException;

}
