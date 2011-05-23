/*
 RemoteSource that acts as a proxy source
 It accepts communication tokens, unpackages them as a regular tokens
 and sends them to appropriate ports
 
 Copyright (c) 2002-2010 The Regents of the University of California.
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

package ptserver.actor;

import java.util.concurrent.ArrayBlockingQueue;

import ptolemy.actor.IOPort;
import ptolemy.data.Token;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptserver.data.CommunicationToken;

/**
 *  RemoteSource that acts as a proxy source
 *  It accepts communication tokens, unpackages them as a regular tokens
 *  and sends them to appropriate ports
 * @author ahuseyno
 * @version $Id$ 
 *
 */
public class RemoteSource extends RemoteActor {


    /**
     * Replaces the targetSource with the RemoteSource instance
     * @see RemoteActor
     * @param container The container
     * @param targetSource The target source
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     * @exception CloneNotSupportedException If port cloning is not supported
     */
    public RemoteSource(CompositeEntity container, ComponentEntity targetSource)
            throws IllegalActionException, NameDuplicationException,
            CloneNotSupportedException {
        super(container, targetSource);
    }

    /**
     * Unpackage last communication token on the queue
     * and send tokens from it to ports specified in it
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        CommunicationToken token = getTokenQueue().poll();
        for (Object p : this.portList()) {
            if (p instanceof IOPort) {
                IOPort port = (IOPort) p;
                int width = port.getWidth();
                for (int channel = 0; channel < width; channel++) {
                    Token[] tokens = token.getTokens(port.getName(), channel);
                    port.send(channel, tokens, tokens.length);
                }
            }
        }
    }

    /**
    * Gets the token queue that this actor uses to receive
    * CommunicationTokens
    * @return ArrayBlockingQueue<CommunicationToken> the tokenQueue
    * @see #setTokenQueue(ArrayBlockingQueue)
    */
    public ArrayBlockingQueue<CommunicationToken> getTokenQueue() {
        return tokenQueue;
    }

    /**
     * Checks if any communication tokens are available on its queue
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        super.prefire();
        return !getTokenQueue().isEmpty();
    }

    /**
     * Sets the token queue that this actor uses to receive
     * CommunicationTokens
     * @param tokenQueue
     * @see #getTokenQueue()
     */
    public void setTokenQueue(ArrayBlockingQueue<CommunicationToken> tokenQueue) {
        this.tokenQueue = tokenQueue;
    }

    /**
     * TokenQueue used to receive CommnunicationTokens
     */
    private ArrayBlockingQueue<CommunicationToken> tokenQueue;
}
