/* Put a token to corresponding receivers.

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.kernel;

import java.util.LinkedList;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ChannelOutput
/**
Put a token to corresponding receivers.

@author Yang
@version $Id$
@since Ptolemy II 3.1
*/
public class ChannelOutput extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ChannelOutput(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        //fromEnv = new TypedIOPort(this, "fromEnv", false, true);
        reception = new TypedIOPort(this, "reception", true, false);
        reception.setTypeEquals(BaseType.OBJECT);
        //receivers = new TypedIOPort(this, "toEnv", true, false);
        //receivers.setTypeEquals(new ArrayType(BaseType.OBJECT));

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.
     */
    public TypedIOPort reception;

    //public TypedIOPort receivers;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Iterate over the receivers list and put the token and properties
     *  to each receiver in the list.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) _debug("invoking fire, try to send data. \n");
        if (reception.hasToken(0)) {
            ObjectToken t = (ObjectToken)reception.get(0);
            Reception rec = (Reception)t.getValue();
            LinkedList recvs = (LinkedList) rec.receivers;
            for (int i = 0; i < recvs.size(); i++) {
                WirelessReceiver r = (WirelessReceiver)recvs.removeFirst();
                _transmitTo(rec.token, r, rec.properties);
            }
        }
    }


    /** Transmit the specified token to the specified receiver.
     *  If necessary, the token will be converted to the resolved
     *  type of the port containing the specified receiver.
     *  @param token The token to transmit, or null to clear
     *   the specified receiver.
     *  @param receiver The receiver to which to transmit.
     *  @param properties The transmit properties (ignored in this base class).
     *  @exception IllegalActionException If the token cannot be converted
     *   or if the token argument is null and the destination receiver
     *   does not support clear.
     */
    protected void _transmitTo(
            Token token,
            WirelessReceiver receiver,
            Token properties)
            throws IllegalActionException {
        if (_debugging) {
            _debug(" * transmitting to: "
                    + receiver.getContainer().getFullName());
        }
        if (token != null) {
            if (receiver.hasRoom()) {
                WirelessIOPort destination = (WirelessIOPort)
                    receiver.getContainer();
                Token newToken = destination.convert(token);
                // Bundle the properties.
                receiver.put(newToken, properties);
            }
        } else {
            receiver.clear();
        }
    }
}
