/* An event indicating the beginning and end of processing an event.

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

import ptolemy.domains.modal.kernel.StateEvent;

//////////////////////////////////////////////////////////////////////////
//// EventDebugEvent

/**
 An event indicating the beginning and end of processing an event. This event
 can be used for debugging.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class EventDebugEvent extends StateEvent {

    /** Construct an event with the specified source and destination Ptera
     *  event.
     *
     *  @param source The source of this state event.
     *  @param event The Ptera event.
     *  @param isProcessed Whether the Ptera event is processed or not.
     */
    public EventDebugEvent(PteraController source, Event event,
            boolean isProcessed) {
        super(source, event);
        _isProcessed = isProcessed;
    }

    /** Get the Ptera event.
     *
     *  @return The Ptera event.
     */
    public Event getEvent() {
        return (Event) getState();
    }

    /** Return whether the Ptera event is processed.
     *
     *  @return Whether the Ptera event is processed.
     */
    public boolean isProcessed() {
        return _isProcessed;
    }

    /** Whether the Ptera event is processed.
     */
    private boolean _isProcessed;
}
