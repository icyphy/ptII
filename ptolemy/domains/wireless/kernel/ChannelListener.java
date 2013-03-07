/* Interface for channel listeners in the wireless domain.

 Copyright (c) 2006-2013 The Regents of the University of California.
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

package ptolemy.domains.wireless.kernel;

import ptolemy.data.RecordToken;
import ptolemy.data.Token;

///////////////////////////////////////////////////////////////////
//// ChannelListener
/**
 Interface for channel listeners.  If a ChannelListener is added to a channel,
 then for each transmission, it is notified and given all pertinent
 information, including the transmission properties, the token transmitted,
 source port and the destination port.

 @see WirelessChannel
 @author Heather Taylor, Elaine Cheong
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (celaine)
 @Pt.AcceptedRating Yellow (celaine)
 */

public interface ChannelListener {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a transmission on the channel on which this is listening.
     *  The channel will call this method after a transmission occurs,
     *  which means that the transmission properties seen by this method
     *  are they same as those seen by the destination.
     *
     *  @param properties The transmission properties after they have
     *  been transformed by any registered PropertyTransformers.
     *  @param token The token transmitted.
     *  @param source The sending port.
     *  @param destination The receiving port.
     */
    public void channelNotify(RecordToken properties, Token token,
            WirelessIOPort source, WirelessIOPort destination);
}
