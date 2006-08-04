package ptolemy.domains.wireless.kernel;

import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

/* Interface for token processors in the wireless domain.

 Copyright (c) 2006 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// TokenProcessor

/**
Interface for token processors.  Token processors
are components that register with the channel a callback that they can
inspect the token passed in a transmission. They can
register to inspect the token from a
specific port, or they can register to inspect all tokens that pass
through the channel. Note that if multiple token processors are
registered that can operate on a given transmission, then the order
in which they are applied is arbitrary.
<p>
Anything that needs to know the locations of the sender and receiver
and optionally the value of the token can implement this interface.
For example, if one wants to visualize the radio link between the sender 
and the receiver then one can use a token processor to do this.

@see WirelessChannel
@author Heather Taylor
@version $Id$
@since Ptolemy II 4.0
@Pt.ProposedRating Red (htaylor)
@Pt.AcceptedRating Red (htaylor)
*/

public interface TokenProcessor {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Modify the transmission properties and return a new token with the
     *  modifications. Implementers may also return the specified token
     *  unchanged.
     *  @param properties The transmission properties.
     *  @param token The token to be processed.
     *  @param source The sending port.
     *  @param destination The receiving port.
     *  @exception IllegalActionException If the properties cannot be
     *   transformed for some reason.
     */
    public void processTokens(RecordToken properties,
            Token token, WirelessIOPort source, WirelessIOPort destination)
            throws IllegalActionException;
}
