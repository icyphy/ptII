/* A wireless channel with zero delay.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (pjb2e@eecs.berkeley.edu)
*/

package ptolemy.domains.sensor.lib;

import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.apps.superb.sensor.kernel.BaseChannel;
import ptolemy.domains.de.kernel.DEReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// WirelessChannel

/**
Model of a wireless channel.
FIXME: More details.

@author Philip Baldwin, Xiaojun Liu, and Edward A. Lee
@version $Id$
*/
public class WirelessChannel extends BaseChannel {

    /** Construct a channel with the given name and container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown. If the name argument
     *  is null, then the name is set to the empty string.
     *  @param container The container.
     *  @param name The name of the channel.
     *  @exception IllegalActionException If the container is incompatible.
     *  @exception NameDuplicationException If the name coincides with
     *   a relation already in the container.
     */
    public WirelessChannel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /* Collect candidates:
     * Name match, reachable, set delay.
     *
     * @see ptolemy.actor.IORelation#deepReceivers(ptolemy.actor.IOPort)
     */
    public Receiver[][] deepReceivers(IOPort except) {
        double range = 0.0;
        try {
            range = rangeOf(except, "signalRadius");
        } catch (IllegalActionException ex) {
            // ignore
        }
        if (range < 0.0) return EMPTY_RECEIVERS;

        LinkedList receiverList = new LinkedList();
        Iterator ports = potentialDestinationsOf(except).iterator();
        while (ports.hasNext()) {
            IOPort port = (IOPort)ports.next();
            if (port.getContainer() == except.getContainer()) continue;
            double distance = Double.MAX_VALUE;
            try {
                distance = distanceBetween(locationOf(except), locationOf(port));
            } catch (IllegalActionException ex) {
                // ignore
            }
            if (range >= distance) {
                Receiver[][] receivers = null;
                if (port.getContainer() == getContainer() && port.isOutput()) {
                    receivers = port.getInsideReceivers();
                } else if (port.isInput()){
                    receivers = port.getReceivers();
                }
                //if (receivers == null) continue;
                try {
                    ((DEReceiver)receivers[0][0]).setDelay(0.0);
                    receiverList.add(receivers[0][0]);
                } catch (IllegalActionException e) {
                    throw new InternalErrorException(
                            "Failed to set delay of wireless receiver.");
                }
            }
        }
        Receiver[][] receivers = new Receiver[1][receiverList.size()];
        Iterator receiverIterator = receiverList.iterator();
        int i = 0;
        while (receiverIterator.hasNext()) {
            Receiver element = (Receiver)receiverIterator.next();
            receivers[0][i] = element;
            ++i;
        }
        return receivers;
    }
}
