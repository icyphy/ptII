/* A debug event for Ptera models.

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

import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// PteraDebugEvent

/**
 A debug event for Ptera models.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PteraDebugEvent implements DebugEvent {

    /** Construct a debug event with the source and a message.
     *
     *  @param source The source of the debug event.
     *  @param message The message.
     */
    public PteraDebugEvent(NamedObj source, String message) {
        _source = source;
        _message = message;
    }

    /** Return the source of the debug event.
     *
     *  @return The source.
     */
    @Override
    public NamedObj getSource() {
        return _source;
    }

    /** Return the message.
     *
     *  @return The message.
     */
    @Override
    public String toString() {
        return _source.getName() + ": " + _message;
    }

    /** Return a string that includes both the name of the source (within the
     *  given container) and the message.
     *
     *  @param sourceContainer Container of the source that is used to retrieve
     *   part of the full name of the source.
     *  @return The string.
     */
    public String toString(NamedObj sourceContainer) {
        return _source.getName(sourceContainer) + ": " + _message;
    }

    /** The message.
     */
    private String _message;

    /** The source.
     */
    private NamedObj _source;

}
