/* Receiver used inside platforms of a ptides domain.

 Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.apps.ptides.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.Time;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

/**
 * Receiver used inside platforms of a ptides domain. This Receiver will not
 * work with non-opaque actors inside a platform.
 *
 * @author Patricia Derler
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Yellow (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PtidesActorReceiver extends PtidesReceiver {

    /**
     * Creates a new Ptides platform receiver.
     */
    public PtidesActorReceiver() {
        super();
    }

    /**
     * Construct an empty DEReceiver with the specified container.
     *
     * @param container
     *            The container.
     * @exception IllegalActionException
     *                If the container does not accept this receiver.
     */
    public PtidesActorReceiver(IOPort container) throws IllegalActionException {
        super(container);
    }

    /**
     * Return true if there is at least one token available to the get() method.
     *
     * @return True if there are more tokens.
     */
    public boolean hasToken() {
        IOPort port = getContainer();
        return (port.isOutput() && super.hasToken())
                || (hasToken(getModelTime()));
    }

    /**
     * Put a token into this receiver.
     *
     * @param token
     *            The token to be put, or null to put no token.
     */
    public void put(Token token) {
        if (token == null) {
            return;
        }
        IOPort containerPort = getContainer();
        Actor containerActor = (Actor) containerPort.getContainer();
        Director dir;
        if (containerActor instanceof CompositeActor) {
            dir = containerActor.getExecutiveDirector();
        } else {
            dir = containerActor.getDirector();
        }
        Time modelTime = dir.getModelTime();
        put(token, modelTime);
    }

    /**
     * Put a token into this receiver and post a trigger event to the director.
     * The director will be responsible to dequeue the trigger event at the
     * correct timestamp and microstep and invoke the corresponding actor whose
     * input port contains this receiver. This receiver may contain more than
     * one events.
     *
     * @param token
     *            The token to be put.
     * @param time
     *            The time stamp for the token.
     */
    public void put(Token token, Time time) {
        super.put(token, time);
    }

    /**
     * Puts a token into all receivers.
     *
     * @param token
     *            The token to be put, or null to put no token.
     * @param receivers
     *            The receivers that get the token.
     * @param time
     *            The time stamp for the token.
     * @exception NoRoomException
     *                Thrown if the receiver is full.
     * @exception IllegalActionException
     *                Thrown if container cannot convert token.
     */
    public void putToAll(Token token, Receiver[] receivers, Time time)
            throws NoRoomException, IllegalActionException {
        if (token == null) {
            return;
        }
        for (int j = 0; j < receivers.length; j++) {
            IOPort container = receivers[j].getContainer();

            // If there is no container, then perform no
            // conversion.
            if (container != null) {
                ((PtidesActorReceiver) receivers[j]).put(container
                        .convert(token), time);
            }
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    /**
     * Remove events from the queue which have a timestamp equal to the given
     * time.
     *
     * @param modelTime
     *            Given model time.
     */
    public void removeEvents(Time modelTime) {
        while (_queue.size() > 0 && _queue.first()._timeStamp.equals(modelTime)) {
            _queue.remove(_queue.first());
        }
    }
}
