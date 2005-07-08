/* Interface for objects that can attach debug listeners.

 Copyright (c) 1997-2005 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// Debuggable

/**
 This is an interface for objects that debug listeners can be attached to.

 @author Jie Liu
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Green (neuendor)
 @see DebugListener
 @see DebugEvent
 */
public interface Debuggable {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a debug listener.
     *  If the listener is already in the list, do not add it again.
     *  @param listener The listener to which to send debug messages.
     *  @see #removeDebugListener(DebugListener)
     */
    public void addDebugListener(DebugListener listener);

    /** Unregister a debug listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     *  @see #addDebugListener(DebugListener)
     */
    public void removeDebugListener(DebugListener listener);
}
