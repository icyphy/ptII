/* Base class implementation of a ptolemy.actor.Receiver.
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
package ptolemy.codegen.c.actor;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * Base class implementation of a ptolemy.actor.Receiver.
 * @author Jia Zou, Man-Kit Leung, Isaac Liu
 * @version $Id$
 * @since Ptolemy II 7.1
 *
 */
public class Receiver extends CCodeGeneratorHelper {

    public Receiver(ptolemy.actor.Receiver receiver) {
        super(receiver);
    }

    public String generateCodeForGet(int channel) throws IllegalActionException {
        return "";
    }

    public String generateCodeForHasToken(int channel)
            throws IllegalActionException {
        return "1";
    }

    public String generateCodeForPut(String token)
            throws IllegalActionException {
        return "";
    }

    public ptolemy.actor.Receiver getReceiver() {
        return (ptolemy.actor.Receiver) getObject();
    }

}
