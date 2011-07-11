/* Base class implementation of a ptolemy.actor.Receiver.

Copyright (c) 2009-2011 The Regents of the University of California.
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
package ptolemy.codegen.c.actor;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * Base class implementation of a ptolemy.actor.Receiver.
 * @author Jia Zou, Man-Kit Leung, Isaac Liu
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class Receiver extends CCodeGeneratorHelper {

    /** Construct the Receiver helper.
     *  @param receiver The ptolemy.actor.receiver that corresponds
     *  with this helper.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public Receiver(ptolemy.actor.Receiver receiver) {
        super(receiver);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Generate code for getting tokens from the receiver.
     * @param channel The channel for which to generate the get code.
     * @return The code to check if the reciever has a token, in this
     * base class, the empty string is returned.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateCodeForGet(int channel) throws IllegalActionException {
        return "";
    }

    /**
     * Generate code to check if the receiver has a token.
     * @param channel The channel for which to generate the hasToken() code.
     * @return The code to check if the reciever has a token, in this
     * case, the String "1" is returned, which indicates that the receiver
     * always has a token.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateCodeForHasToken(int channel)
            throws IllegalActionException {
        return "1";
    }

    /**
     * Generate code for putting tokens to the receiver.
     * Note the type conversion is also done in this put method.
     * @param token The token to be sent.
     * @return The code to put tokens to the receiver, in this case,
     * the empty string is returned.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateCodeForPut(String token)
            throws IllegalActionException {
        return "";
    }

    /** Get the corresponding component.
     *  @return the component that corresponds with this receiver.
     */
    public ptolemy.actor.Receiver getReceiver() {
        return (ptolemy.actor.Receiver) getObject();
    }
}
