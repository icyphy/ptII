/* Interface for the debug listeners that receives event insertion and removal
 messages.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.domains.ptera.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.util.Time;
import ptolemy.data.Token;
import ptolemy.kernel.util.DebugListener;

//////////////////////////////////////////////////////////////////////////
//// EventQueueDebugListener

/**
 Interface for the debug listeners that receives event insertion and removal
 messages.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public interface EventQueueDebugListener extends DebugListener {

    /** Invoked when an actor is inserted into the event queue.
     *
     *  @param position The position at which the actor is inserted.
     *  @param time The time at which the actor is scheduled to fire.
     *  @param actor The actor.
     *  @param arguments Arguments to the actor, which must be either an
     *   ArrayToken or a RecordToken, or null.
     */
    public void insertActor(int position, Time time, Actor actor,
            Token arguments);

    /** Invoked when an event is inserted into the event queue.
     *
     *  @param position The position at which the event is inserted.
     *  @param time The time at which the event is scheduled to fire.
     *  @param event The event.
     *  @param arguments Arguments to the event, which must be either an
     *   ArrayToken or a RecordToken, or null.
     */
    public void insertEvent(int position, Time time, Event event,
            Token arguments);

    /** Invoked when an event or actor is removed from the event queue.
     *
     *  @param position The position of the event or actor.
     *  @param isCancelled Whether the removal is due to cancellation or
     *   successful processing.
     */
    public void removeEvent(int position, boolean isCancelled);
}
