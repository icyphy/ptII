/*
@Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.apps.apes;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import ptolemy.actor.Actor;

// ////////////////////////////////////////////////////////////////////////
// // ScheduleListener

/**
 * A schedule listener reacts to given events.
 * 
 * @author Patricia Derler
 */
public interface TaskExecutionListener {
    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * React to the given event.
     * 
     * @param node
     *                The node where the event happened.
     * @param actor
     *                The actor where the event happened. This parameter can be
     *                null if the event is TRANSFEROUTPUT or TRANSFERINPUT.
     * @param time
     *                The physical time when the event happened.
     * @param scheduleEvent
     *                The type of the event.
     */
    public void event(Actor actor, double time, 
            ScheduleEventType scheduleEvent);

	/**
	 * Type of schedule event.
	 */
    public static enum ScheduleEventType {
        /** Started the execution of an actor. */
        START, 
        /** Stopped the execution of an actor. */
        STOP,
        WAIT,
        PREEMPTED, 
        AP
    }

}
