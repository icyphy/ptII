/* Language independent code generator for Ptolemy Ports.

 Copyright (c) 2008-2009 The Regents of the University of California.
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

package ptolemy.cg.kernel.generic;

import ptolemy.kernel.util.IllegalActionException;

/**
 * @author Man-kit Leung, Bert Rodiers
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */

public interface PortCodeGenerator {

    /** Generate the get code.
     *  @param channel The channel for which the get code is generated.
     *  @return The code that gets data from the channel.
     */
    public String generateCodeForGet(String channel) throws IllegalActionException;

    /** Generate the send code.
     *  @param channel The channel for which the send code is generated.
     *  @param dataToken The token to be sent
     *  @return The code that sends the dataToken on the channel.
     */
    public String generateCodeForSend(String channel, String dataToken)
        throws IllegalActionException;

    /** Get the read offset of a channel of the port.
     *  @param channelNumber The number of the channel.
     *  @return The read offset.
     *  @see #setReadOffset(int, Object)
     *  FIXME rodiers: this is SDF specific
     */
//    public Object getReadOffset(int channelNumber)
//        throws IllegalActionException;

    /** Get the write offset of a channel of the port.
     *  @param channelNumber The number of the channel.
     *  @return The write offset.
     *  @exception IllegalActionException If thrown while getting the channel.
     *  @see #setWriteOffset(int, Object)
     *  FIXME rodiers: this is SDF specific
     */
//    public Object getWriteOffset(int channelNumber)
//        throws IllegalActionException;

    /** Set the read offset of a channel of the port.
     *  @param channelNumber The number of the channel that is being set.
     *  @param readOffset The offset.
     *  @see #getReadOffset(int)
     *  FIXME rodiers: this is SDF specific
     */
//    public void setReadOffset(int channelNumber, Object readOffset);

    /** Set the write offset of a channel of the port.
     *  @param channelNumber The number of the channel that is being set.
     *  @param writeOffset The offset.
     *  FIXME rodiers: this is SDF specific
     */
//    public void setWriteOffset(int channelNumber, Object writeOffset);

}
