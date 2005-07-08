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
package ptolemy.domains.tm.kernel;

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
     */
    public void event(String actorName, double time, int scheduleEvent);

    static final int RESET_DISPLAY = -1;

    static final int TASK_SLEEPING = 1;

    static final int TASK_BLOCKED = 2;

    static final int TASK_RUNNING = 3;
}
