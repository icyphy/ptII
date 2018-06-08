/* A receiver for modeling train control systems.

 Copyright (c) 2015-2016 The Regents of the University of California.
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
package ptolemy.domains.tcs.kernel;

import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.data.Token;
import ptolemy.domains.de.kernel.DEReceiver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/** A receiver for modeling train control systems.
 *  This receiver checks the containing actor, and if it can reject inputs,
 *  then uses it to determine whether to accept a token.
 *  @author Marjan Sirjani and Edward A. Lee
@version $Id$
@since Ptolemy II 11.0
 */
public class TCSReceiver extends DEReceiver {
    /** Construct an empty DEReceiver with no container.
     */
    public TCSReceiver() {
        super();
    }

    /** Construct an empty DEReceiver with the specified container.
     *  @param container The container.
     *  @exception IllegalActionException If the container does
     *  not accept this receiver.
     */
    public TCSReceiver(IOPort container) throws IllegalActionException {
        super(container);
    }

    /** Put a token into this receiver and post a trigger event to the director.
     *  The director will be responsible to dequeue the trigger event at
     *  the correct timestamp and microstep and invoke the corresponding actor
     *  whose input port contains this receiver. This receiver may contain
     *  more than one events.
     *  @param token The token to be put, or null to put no token.
     *  @exception IllegalActionException If cannot get the director or if
     *   the current microstep is zero.
     *  @exception NoRoomException Not thrown in this class.
     */
    @Override
    public void put(Token token)
            throws IllegalActionException, NoRoomException {
        IOPort port = getContainer();

        if (port != null) {
            NamedObj actor = port.getContainer();
            if (actor instanceof Rejecting) {
                if (((Rejecting) actor).reject(token, port)) {
                    throw new NoRoomException(actor,
                            "Rejected input on port " + port.getName());
                }
            }
        }
        super.put(token);
    }
}
