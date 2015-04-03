/* Language independent code generator for Ptolemy Ports.

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

package ptolemy.cg.kernel.generic;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////
////PortCodeGenerator

/** An interface for Port Adapters to generate port specific code.
 *  All cg port adapter implementations should implement this interface.
 *  @author Man-kit Leung, Bert Rodiers
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */

public interface PortCodeGenerator {

    /** Generate the code to get a token from a port.
     *  @param channel The channel for which the get code is generated.
     *  @param offset The offset in the array representation of the port.
     *  @return The code that gets data from the channel.
     *  @exception IllegalActionException If the director adapter class cannot be found.
     */
    public String generateGetCode(String channel, String offset)
            throws IllegalActionException;

    /** Generate the code to get a token from a port, but don't
     *  include the type, for example payload.Int.
     *  @param channel The channel for which the get code is generated.
     *  @param offset The offset in the array representation of the port.
     *  @return The code that gets data from the channel.
     *  @exception IllegalActionException If the director adapter class cannot be found.
     */
    public String generateGetCodeWithoutType(String channel, String offset)
            throws IllegalActionException;

    /** Generate the code to put a token to a port.
     *  @param channel The channel for which the send code is generated.
     *  @param offset The offset in the array representation of the port.
     *  @param dataToken The token to be sent
     *  @return The code that sends the dataToken on the channel.
     *  @exception IllegalActionException If the director adapter class cannot be found.
     */
    public String generatePutCode(String channel, String offset,
            String dataToken) throws IllegalActionException;

    /** Generate the code to put a token to a port's local inside receivers.
     *  @param channel The channel for which the send code is generated.
     *  @param offset The offset in the array representation of the port.
     *  @param dataToken The token to be sent
     *  @return The code that sends the dataToken on the channel.
     *  @exception IllegalActionException If the director adapter class cannot be found.
     */
    public String generatePutLocalInsideCode(String channel, String offset,
            String dataToken) throws IllegalActionException;

    /** Generates code to check the receiver has a token.
     *  @param channel The channel for which the get code is generated.
     *  @param offset The offset in the array representation of the port.
     *  @return The code that generates has token from the channel.
     *  FIXME: potentially, we could also pass in a boolean that indicates whether
     *  the port the channel resides is a multiport, if it is, then only a static
     *  variable is needed instead of an array of length 1.
     *  @exception IllegalActionException If code can't be generated.
     */
    public String generateHasTokenCode(String channel, String offset)
            throws IllegalActionException;

}
