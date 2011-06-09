/*
 RemoteSink that acts as a proxy sink and publishes tokens it receives as
 CommunicationToken to its queue.

 Copyright (c) 2011 The Regents of the University of California.
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

import java.util.HashMap;

import ptolemy.actor.IOPort;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptserver.communication.TokenPublisher;
import ptserver.data.CommunicationToken;

///////////////////////////////////////////////////////////////////
////RemoteSink
/**
 * A Remote Sink that acts as a proxy sink and publishes tokens it receives as
 * one CommunicationToken per fire to its queue.
 * @author ahuseyno
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class RemoteSink extends RemoteActor {

    /**
     * Create a new instance of the RemoteActor without doing any actor replacement.
     * @param container The container.
     * @param name The name of this actor within the container.
     * @exception IllegalActionException If this actor cannot be contained
     *  by the proposed container (see the setContainer() method).
     * @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public RemoteSink(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * Replace the targetSink with the RemoteSink instance.
     * @param container The container
     * @param targetSink The target sink
     * @param replaceTargetEntity replaceTargetEntity true to replace the target entity with the proxy,
     * otherwise replace all entities connecting to it with one proxy
     * @param portTypes Map of ports and their resolved types
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     * @exception CloneNotSupportedException If port cloning is not supported
     * @see RemoteActor
     */
    public RemoteSink(CompositeEntity container, ComponentEntity targetSink,
            boolean replaceTargetEntity, HashMap<String, String> portTypes)
            throws IllegalActionException, NameDuplicationException,
            CloneNotSupportedException {
        super(container, targetSink, replaceTargetEntity, portTypes);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Read tokens from all ports, package them as one CommunicationToken and send
     * it via tokenPublisher.
     * @exception IllegalActionException parameters.getToken, port.get throw it
     * @see TokenPublisher
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        CommunicationToken token = new CommunicationToken(getTargetEntityName());
        for (Object portObject : portList()) {
            if (portObject instanceof IOPort) {
                IOPort port = (IOPort) portObject;
                int consumptionRate = 1;
                Parameter tokenConsumptionRate = (Parameter) port
                        .getAttribute("tokenConsumptionRate");
                if (tokenConsumptionRate != null) {
                    consumptionRate = ((IntToken) tokenConsumptionRate
                            .getToken()).intValue();
                }
                int width = port.getWidth();
                token.addPort(port.getName(), width);
                for (int channel = 0; channel < width; channel++) {
                    token.putTokens(port.getName(), channel,
                            port.get(channel, consumptionRate));
                }
            }
        }
        getTokenPublisher().sendToken(token);
    }

    /**
     * Return TokenPublisher that would be used to publish
     * CommunicationTokens produced by this actor on fire.
     * @return TokenPublisher the token publisher
     * @see #setTokenPublisher(TokenPublisher)
     */
    public TokenPublisher getTokenPublisher() {
        return _tokenPublisher;
    }

    /**
     * Check if tokens are available on all ports.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        super.prefire();
        for (Object portObject : portList()) {
            if (portObject instanceof IOPort) {
                IOPort port = (IOPort) portObject;
                int consumptionRate = 1;
                Parameter tokenConsumptionRate = (Parameter) port
                        .getAttribute("tokenConsumptionRate");
                if (tokenConsumptionRate != null) {
                    consumptionRate = ((IntToken) tokenConsumptionRate
                            .getToken()).intValue();
                }
                int width = port.getWidth();
                for (int channel = 0; channel < width; channel++) {
                    if (!port.hasToken(channel, consumptionRate)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Set the token publisher that would be used to send
     * communication tokens.
     * @param tokenPublisher the token publisher to be used for sending tokens.
     * @see #getTokenPublisher()
     */
    public void setTokenPublisher(TokenPublisher tokenPublisher) {
        _tokenPublisher = tokenPublisher;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Return true if connectingPort is inputPort.
     * @see ptserver.actor.RemoteActor#isValidConnectingPort(ptolemy.actor.IOPort)
     */
    @Override
    protected boolean isValidConnectingPort(IOPort connectingPort) {
        return connectingPort.isInput();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Token Publisher is used to publish CommunicationTokens to a queue for serializing
     * into a binary and batching
     */
    private TokenPublisher _tokenPublisher;

}
