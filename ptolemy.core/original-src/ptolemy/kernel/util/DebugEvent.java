/* Interface for debug events.

 Copyright (c) 2000-2014 The Regents of the University of California.
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
package ptolemy.kernel.util;

///////////////////////////////////////////////////////////////////
//// DebugEvent

/**
 An interface for events that can be used for debugging.  These events will
 generally be subclassed to create events with more meaning (such as
 a FiringEvent).  Debug events should always have a useful string
 representation, so that the generic listeners (such as StreamListener)
 can display them reasonably.  This string representation should be
 provided by the toString() method.

 @author  Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Green (neuendor)
 @see DebugListener
 @see Debuggable
 @see ptolemy.actor.FiringEvent
 */
public interface DebugEvent {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the source of the event.
     *  @return The ptolemy object that published this event.
     */
    public NamedObj getSource();

    /** Return a string representation of this event.
     *  @return A user-readable string describing the event.
     */
    @Override
    public String toString();
}
