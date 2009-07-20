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
 *  @since Ptolemy II 7.1
 *  @Pt.ProposedRating Red (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 */
public class DEReceiver extends ptolemy.codegen.c.actor.Receiver {

    public DEReceiver(ptolemy.domains.de.kernel.DEReceiver receiver) {
        super(receiver);
        // TODO Auto-generated constructor stub
    }

    public String generateCodeForGet() throws IllegalActionException {
        return "";
    }

    public String generateCodeForHasToken(int channel)
            throws IllegalActionException {
        IOPort port = getReceiver().getContainer();
        return "Event_Head_" + generateName(port) + "[" + channel + "] != NULL";
    }

    public String generateCodeForPut(PartialResult token)
            throws IllegalActionException {
        return "";
    }

}
