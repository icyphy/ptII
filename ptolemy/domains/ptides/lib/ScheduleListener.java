/* Interface for listeners that receive schedule messages.

 Copyright (c) 2001-2005 The Regents of the University of California.
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
package ptolemy.domains.ptides.lib;

import java.util.Hashtable;

import ptolemy.actor.Actor;

//////////////////////////////////////////////////////////////////////////
//// ScheduleListener

/**
 Interface for listeners that receive schedule messages.

 @author  Johan Eker
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (johane)
 @Pt.AcceptedRating Red (johane)
 @see ptolemy.kernel.util.NamedObj
 */
public interface ScheduleListener {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to the given scheduling event.
     *  @param actorName The name of the actor involved in the event.
     *  @param time The time of the event.
     *  @param scheduleEvent One of {@link #RESET_DISPLAY},
     *  {@link #TASK_SLEEPING}, {@link #TASK_BLOCKED} or {@link #TASK_RUNNING}.
     */
    public void event(Actor node, Actor actor, double time, int scheduleEvent);
    
    public void initialize(Hashtable nodesActors);

    /** Reset display message. */
    static final int START = 0;

    /** Task sleeping message. */
    static final int STOP = 1;
    
    static final int TRANSFEROUTPUT = 2;
    
    static final int TRANSFERINPUT = 3;
    
    static final int MISSEDEXECUTION = 4;

}
