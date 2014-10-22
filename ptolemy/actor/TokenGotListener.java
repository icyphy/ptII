/* A listener for TokenGotEvents from an IOPort.

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.actor;

//////////////////////////////////////////////////////////////////////////
//// TokenGotListener

/**
 A TokenGotListener listens for events that are issued during the
 get(),  and getInside() function calls of an IOPort.  In
 general, an object that implements this interface will be collecting
 tokens for storage.
 <p>
 Currently this class is being used in Kepler by the
 ProvenanceExecutionListener for saving tokens with provenance
 information.  In the future, listeners will use this interface
 to save tokens in a cache for use during "smart" reruns or for
 fault tolerance.

 @author Oscar Barney
 @version $Id$
 @since Ptolemy II 6.1
 @deprecated Use IOPortEventListener instead.
 @Pt.ProposedRating Red (barney)
 @Pt.AcceptedRating
 @see IOPort
 */
@Deprecated
public interface TokenGotListener {

    /** Report an that a token was received with get().  This method
     *  will be called when a token has been taken from the port's
     *  queue at an input IOPort.
     *  @param event The event to report.
     */
    public void tokenGotEvent(TokenGotEvent event);
}
