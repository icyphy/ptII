/* Interface for execution time listener.
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

/** This interface defines the method that has to be implemented by
 *  an execution time listener.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (derler)
 *  @Pt.AcceptedRating
 */
public interface ExecutionTimeListener {

    /** The event is displayed.
     *  @param actor The actor where the event happened. This parameter can be
     *     null if the event is TRANSFEROUTPUT or TRANSFERINPUT.
     *  @param oracleTime The oracle time when the event happened.
     *  @param physicalTime The physical time when the event happened.
     *  @param modelTime The model time when the event happened.
     *  @param event The type of the event.
     */
    public void event(Actor actor, double oracleTime,
            double physicalTime, double modelTime, ExecutionEventType event);

    /** The event is displayed.
     *  @param actor The actor where the event happened. This parameter can be
     *     null if the event is TRANSFEROUTPUT or TRANSFERINPUT.
     *  @param time The physical time when the event happened.
     *  @param event The type of the event.
     *  @param core The core where the event happened.
     */
    public void event(Actor actor, double time, ExecutionEventType event,
            int core);

    /** Execution time event type. */
    public static enum ExecutionEventType {
        /** Started the execution of an actor. */
        START,
        /** Stopped the execution of an actor. */
        STOP,
        /** Preempted the execution of an actor. */
        PREEMPTED
    }
}
