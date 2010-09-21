/* Language independent code generator for Ptolemy Ports.

 Copyright (c) 2009 The Regents of the University of California.
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

package ptolemy.codegen.kernel;

import ptolemy.actor.Director;
import ptolemy.kernel.util.IllegalActionException;

/**
 * Language independent code generator for Ptolemy ports.
 * @author Man-kit Leung
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public interface PortCodeGenerator extends ComponentCodeGenerator {
    /**
     * Generate the expression that represents the offset in the generated
     * code.
     * @param offset The specified offset from the user.
     * @param channel The referenced port channel.
     * @param isWrite Whether to generate the write or read offset.
     * @param directorHelper The helper of the director associated with the port.
     * @return The expression that represents the offset in the generated code.
     * @exception IllegalActionException If there is problems getting the port
     *  buffer size or the offset in the channel and offset map.
     */
    public String generateOffset(String offset, int channel, boolean isWrite,
            Director directorHelper) throws IllegalActionException;

    /**
     * Generate code for replacing the get() macro.
     * This delegates to the receiver adapter for the specified
     * channel and asks it to generate the get code.
     * @param channel The channel for which the get code is generated.
     * @return The code that gets data from the channel.
     * @exception IllegalActionException If the director adapter class cannot be found.
     * FIXME: potentially, we could also pass in a boolean that indicates whether
     * the port the channel resides is a multiport, if it is, then only a static
     * variable is needed instead of an array of length 1.
     */
    public String generateCodeForGet(String channel)
            throws IllegalActionException;

    /**
     * Generate code for replacing the send() macro.
     * @param channel The channel for which the send code is generated.
     * @param dataToken The token to be sent
     * @return The code that sends the dataToken on the channel.
     * @exception IllegalActionException If the director adapter class cannot be found.
     * FIXME: potentially, we could also pass in a boolean that indicates whether
     * the port the channel resides is a multiport, if it is, then only a static
     * variable is needed instead of an array of length 1.
     */
    public String generateCodeForSend(String channel, String dataToken)
            throws IllegalActionException;

    /** Get the buffer size of channel of the port.
     *  @param channelNumber The number of the channel that is being set.
     *  @return return The size of the buffer.
     *  @see #setBufferSize(int, int)
     *  @exception IllegalActionException If the buffer size cannot be set.
     */
    public int getBufferSize(int channelNumber) throws IllegalActionException;

    /** Get the read offset of a channel of the port.
     *  @param channelNumber The number of the channel.
     *  @return The read offset.
     *  @exception IllegalActionException If thrown while getting the channel.
     *  @see #setReadOffset(int, Object)
     */
    public Object getReadOffset(int channelNumber)
            throws IllegalActionException;

    /** Get the write offset of a channel of the port.
     *  @param channelNumber The number of the channel.
     *  @return The write offset.
     *  @exception IllegalActionException If thrown while getting the channel.
     *  @see #setWriteOffset(int, Object)
     */
    public Object getWriteOffset(int channelNumber)
            throws IllegalActionException;

    /** Set the buffer size of channel of the port.
     *  @param channelNumber The number of the channel that is being set.
     *  @param bufferSize The size of the buffer.
     *  @see #getBufferSize(int)
     */
    public void setBufferSize(int channelNumber, int bufferSize);

    /** Set the read offset of a channel of the port.
     *  @param channelNumber The number of the channel that is being set.
     *  @param readOffset The offset.
     *  @see #getReadOffset(int)
     */
    public void setReadOffset(int channelNumber, Object readOffset);

    /** Set the write offset of a channel of the port.
     *  @param channelNumber The number of the channel that is being set.
     *  @param writeOffset The offset.
     *  @see #getWriteOffset(int)
     */
    public void setWriteOffset(int channelNumber, Object writeOffset);

    /** Update the read offset.
     *  @param rate  The rate of the channels.
     *  @param directorHelper The Director helper
     *  @return The offset.
     *  @exception IllegalActionException If thrown while getting the channel
     *  or updating the offset.
     */
    public String updateOffset(int rate, Director directorHelper)
            throws IllegalActionException;

    /** Update the write offset of the [multiple] connected ports.
     *  @param rate  The rate of the channels.
     *  @param director The Director helper.
     *  @return The offset.
     *  @exception IllegalActionException If thrown while getting the channel
     *  or updating the offset.
     */
    public String updateConnectedPortsOffset(int rate, Director director)
            throws IllegalActionException;

    /** Initialize the offsets.
     *  @return The code to initialize the offsets.
     *  @exception IllegalActionException If thrown while getting the channel
     *  or initializing the offset.
     */
    public String initializeOffsets() throws IllegalActionException;
}
