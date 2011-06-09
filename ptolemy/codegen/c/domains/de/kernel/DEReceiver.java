/* C codegen implementation of a ptolemy.domains.de.kernel.DEReceiver.

Below is the copyright agreement for the Ptolemy II system.

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
*/
package ptolemy.codegen.c.domains.de.kernel;

import ptolemy.actor.IOPort;
import ptolemy.codegen.util.PartialResult;
import ptolemy.kernel.util.IllegalActionException;

/**
 *  C codegen implementation of a ptolemy.domains.de.kernel.DEReceiver.
 *  @author Jia Zou, Man-Kit Leung, Isaac Liu
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 */
public class DEReceiver extends ptolemy.codegen.c.actor.Receiver {

    /** Construct the code generator helper associated with the given DEReceiver.
     *  @param receiver The associated receiver
     */
    public DEReceiver(ptolemy.domains.de.kernel.DEReceiver receiver) {
        super(receiver);
    }

    /**
     * Return the code for getting data from the specific port channel.
     * @return the code for getting data from the specific port channel,
     * in this case, return the empty string.
     * @exception IllegalActionException Not thrown here.
     */
    public String generateCodeForGet() throws IllegalActionException {
        return "";
    }

    /** Generate code to check if the receiver has a token.
     *  @param channel The channel for which to generate the get code.
     *  @return the hasToken code.
     *  @exception IllegalActionException If an error occurs when
     *  getting the receiver, its container or while generating a port name.
     */
    public String generateCodeForHasToken(int channel)
            throws IllegalActionException {
        IOPort port = getReceiver().getContainer();
        return "Event_Head_" + generateName(port) + "[" + channel + "] != NULL";
    }

    /**
     * Return the code for sending data.
     * @param token The token to be sent.
     * @return the code for sending data, in this case, return the
     * empty string.
     * @exception IllegalActionException Not thrown here.
     */
    public String generateCodeForPut(PartialResult token)
            throws IllegalActionException {
        return "";
    }

}
