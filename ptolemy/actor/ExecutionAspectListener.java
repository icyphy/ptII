/* This is an interface for an execution aspect.

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

package ptolemy.actor;

import java.util.List;

import ptolemy.kernel.util.NamedObj;

/** This is an interface for an execution aspect. Classes that implement
 *  this interface are interested in schedule events created by
 *  resource schedulers.
 *
 * @author Patricia Derler
   @version $Id$
   @since Ptolemy II 10.0

   @Pt.ProposedRating Red (derler)
   @Pt.AcceptedRating Red (derler)
 */
public interface ExecutionAspectListener {

    ///////////////////////////////////////////////////////////////////
    //                         public variables                      //

    /** Execution time event types. */
    public static enum ExecutionEventType {
        /** Started the execution of an actor. */
        START,
        /** Stopped the execution of an actor. */
        STOP,
        /** Preempted the execution of an actor. */
        PREEMPTED
    }

    /** Initialize listener.
     * @param actors Actors to be scheduled.
     * @param scheduler Resource scheduler scheduling actors.
     */
    public void initialize(List<NamedObj> actors,
            ActorExecutionAspect scheduler);

    /** Plot a new execution event for an actor (i.e. an actor
     *  started/finished execution, was preempted or resumed).
     * @param actor The actor.
     * @param physicalTime The physical time when this scheduling event occurred.
     * @param scheduleEvent The scheduling event.
     */
    public void event(final NamedObj actor, double physicalTime,
            ExecutionEventType scheduleEvent);
}
