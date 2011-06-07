/*
 RemoteSource that acts as a proxy source
 Accepts communication token, unpackage as regular tokens
 and send them to the appropriate ports

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
import ptolemy.data.Token;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptserver.communication.RemoteSourceData;
import ptserver.data.CommunicationToken;

///////////////////////////////////////////////////////////////////
////RemoteSource
/**
 * RemoteSource that acts as a proxy source.
 * Accepts communication token, unpackages as regular tokens
 * and send them to the appropriate ports.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class RemoteSource extends RemoteActor {

    /**
     * Create new instance of the RemoteActor without doing any actor replacement.
     * @param container The container.
     * @param name The name of this actor within the container.
     * @exception IllegalActionException If this actor cannot be contained
     *  by the proposed container (see the setContainer() method).
     * @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public RemoteSource(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * Replace the targetSource with the RemoteSource instance.
     * @see RemoteActor
     * @param container The container
     * @param targetSource The target source
     * @param replaceTargetEntity replaceTargetEntity true to replace the target entity with the proxy,
     * otherwise replace all entities connecting to it with one proxy
     * @param portTypes Map of ports and their resolved types
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     * @exception CloneNotSupportedException If port cloning is not supported
     */
    public RemoteSource(CompositeEntity container,
            ComponentEntity targetSource, boolean replaceTargetEntity,
            HashMap<String, String> portTypes) throws IllegalActionException,
            NameDuplicationException, CloneNotSupportedException {
        super(container, targetSource, replaceTargetEntity, portTypes);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Unpackage the last CommunicationToken on the queue
     * and send tokens from it to the ports specified in the CommunicationToken.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        CommunicationToken token;

        //Block the thread until either the queue has an element or the model is stopped.
        synchronized (this) {
            while ((token = getRemoteSourceData().getTokenQueue().poll()) == null
                    && !getRemoteSourceData().getRemoteModel().isStopped()) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    throw new IllegalActionException(this, e,
                            "The remote source was inturrupted");
                }
            }
            //If token is null, then it means that the model was stopped, just return in this case.
            if (token == null) {
                return;
            }
        }
        for (Object portObject : this.portList()) {
            if (portObject instanceof IOPort) {
                IOPort port = (IOPort) portObject;
                int width = port.getWidth();
                for (int channel = 0; channel < width; channel++) {
                    Token[] tokens = token.getTokens(port.getName(), channel);
                    port.send(channel, tokens, tokens.length);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Return true if connectingPort is output port.
     * @see ptserver.actor.RemoteActor#isValidConnectingPort(ptolemy.actor.IOPort)
     */
    @Override
    protected boolean isValidConnectingPort(IOPort connectingPort) {
        return connectingPort.isOutput();
    }

    /**
     * Set the remote source data structure used for synchronization and thread blocking.
     * @param remoteSourceData the remoteSourceData containing the instance
     * @see #getRemoteSourceData()
     */
    public void setRemoteSourceData(RemoteSourceData remoteSourceData) {
        this._remoteSourceData = remoteSourceData;
    }

    /**
     * Return the remoteSourceData containing the instance.
     * @return the remoteSourceData containing the instance.
     * @see #setRemoteSourceData(RemoteSourceData)
     */
    public RemoteSourceData getRemoteSourceData() {
        return _remoteSourceData;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * RemoteSourceData containing metadata needed for the RemoteSource.
     */
    private RemoteSourceData _remoteSourceData;

}
