/* This actor implements a Network Bus.

@Copyright (c) 2010 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.domains.ptides.demo.PtidesAirplaneFuelControl;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.wireless.kernel.AtomicWirelessChannel;
import ptolemy.domains.wireless.kernel.WirelessDirector;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * A wireless channel which sends tokens to receivers specified in the port that
 * sends a token.
 * 
 * @author Patricia Derler
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Yellow (derler)
 * @Pt.AcceptedRating Red (derler)
 */
public class Bus extends AtomicWirelessChannel {

    /**
     * Construct a relation with the given name contained by the specified
     * entity. The container argument must not be null, or a
     * NullPointerException will be thrown. This relation will use the workspace
     * of the container for synchronization and version counts. If the name
     * argument is null, then the name is set to the empty string. This
     * constructor write-synchronizes on the workspace.
     * 
     * @param container
     *            The container.
     * @param name
     *            The name of the relation.
     * @exception IllegalActionException
     *                If the container is incompatible with this relation.
     * @exception NameDuplicationException
     *                If the name coincides with a relation already in the
     *                container.
     */
    public Bus(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * Send a token from source port to target. The target port is specified in
     * the attribute <i>receiver</i> of the source port.
     * 
     * @param token
     *            The token sent from the source port.
     * @param port
     *            The source port.
     * @param properties
     *            This actor does not use the properties parameter.
     * @exception IllegalActionException
     *                If a conflict occurs.
     */
    public void transmit(Token token, WirelessIOPort port,
            RecordToken properties) throws IllegalActionException {
        try {
            workspace().getReadAccess();
            if (!(getDirector() instanceof WirelessDirector)) {
                throw new IllegalActionException(this,
                        "AtomicWirelessChannel can only work "
                                + "with a WirelessDirector.");
            }

            Parameter parameter = (Parameter) port.getAttribute("receiver");
            Object receiverValue = getContainer().getAttribute(
                    parameter.getDefaultExpression());

            // Quick hack to deal with port parameters.
            if (receiverValue instanceof PortParameter) {
                ((PortParameter) receiverValue).setCurrentValue(token);
            } else {
                Port receiverPort = (Port) ((ObjectToken) parameter.getToken())
                        .getValue();
                if (((IOPort) receiverPort).getReceivers().length > 0) {
                    _transmitTo(
                            token,
                            port,
                            (Receiver) ((IOPort) receiverPort).getReceivers()[0][0],
                            properties);
                }
            }
        } finally {
            workspace().doneReading();
        }
    }

    /**
     * Send a token to specified receiver and notify listeners after the
     * transmission.
     * 
     * @param token
     *            The token that is sent.
     * @param sender
     *            The sender of the token.
     * @param receiver
     *            The receiver of the token.
     * @param properties
     *            Properties of the transmission.
     * @exception IllegalActionException
     *                If the token cannot be converted or if the token argument
     *                is null and the destination receiver does not support
     *                clear.
     */
    protected void _transmitTo(Token token, WirelessIOPort sender,
            Receiver receiver, RecordToken properties)
            throws IllegalActionException {
        if (_debugging) {
            _debug(" * transmitting to: "
                    + receiver.getContainer().getFullName());
        }

        if (token != null) {
            if (receiver.hasRoom()) {
                WirelessIOPort destination = (WirelessIOPort) receiver
                        .getContainer();
                Token newToken = destination.convert(token);
                receiver.put(newToken);
                // Notify any channel listeners after the transmission occurs.
                channelNotify(properties, token, sender, destination);
            }
        } else {
            receiver.clear();
        }
    }

}
