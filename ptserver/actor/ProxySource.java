/*
 ProxySource that acts as a proxy actor to a set of actors that were replaced.
 It could replace one source actor or all actors connected to the source actor.

 Accepts communication token, unpackage as regular tokens
 and send them to the appropriate ports

 Copyright (c) 2011-2014 The Regents of the University of California.
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
import ptserver.communication.ProxyModelInfrastructure;
import ptserver.communication.ProxySourceData;
import ptserver.data.CommunicationToken;

///////////////////////////////////////////////////////////////////
////ProxySource
/**
 * ProxySource that acts as a proxy actor to a set of actors that were replaced.
 * It could replace one source actor or all actors connected to the source actor.
 * Accepts communication token, unpackage as regular tokens
 * and send them to the appropriate ports
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class ProxySource extends ProxyActor {

    /**
     * Create a new instance of the ProxySource without doing any actor replacement.
     * @param container The container.
     * @param name The name of this actor within the container.
     * @exception IllegalActionException If this actor cannot be contained
     *  by the proposed container (see the setContainer() method).
     * @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ProxySource(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * Replace the targetSource with the ProxySource instance.
     * @param container The container of the actor
     * @param targetSource The target source to be processed.
     * @param replaceTargetEntity if true replace the target entity with the proxy,
     * otherwise replace all entities connecting to it with one proxy
     * @param portTypes Map of ports and their resolved types
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     * @exception CloneNotSupportedException If port cloning is not supported
     * @see ProxyActor
     */
    public ProxySource(CompositeEntity container, ComponentEntity targetSource,
            boolean replaceTargetEntity, HashMap<String, String> portTypes)
                    throws IllegalActionException, NameDuplicationException,
                    CloneNotSupportedException {
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
            while ((token = getProxySourceData().getTokenQueue().poll()) == null
                    && !_proxyModelInfrastructure.isStopped()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    break;
                }
            }
            // If the token is null, then it means that the model was stopped, just return in this case.
            if (token == null) {
                return;
            }
        }
        for (Object portObject : portList()) {
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

    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        synchronized (this) {
            getProxySourceData().getTokenQueue().clear();
        }
    }

    /**
     * Set the ProxySourceData structure used for synchronization and thread blocking.
     * @param remoteSourceData the remoteSourceData containing the instance
     * @see #getProxySourceData()
     */
    public void setProxySourceData(ProxySourceData remoteSourceData) {
        _proxySourceData = remoteSourceData;
    }

    /**
     * Return the ProxySourceData instance containing the current instance.
     * @return the ProxySourceData instance containing the current instance.
     * @see #setProxySourceData(ProxySourceData)
     */
    public ProxySourceData getProxySourceData() {
        return _proxySourceData;
    }

    /**
     * Set the ProxyModelInfrastructure instance controlling distributed model
     * execution.
     * @param proxyModelInfrastructure the the ProxyModelInfrastructure instance controlling distributed model
     * execution.
     */
    public void setProxyModelInfrastructure(
            ProxyModelInfrastructure proxyModelInfrastructure) {
        // Note: FindBugs reports inconsistent synchronization bug pattern which
        // does not apply here since this field is set before ProxySource is fired.
        _proxyModelInfrastructure = proxyModelInfrastructure;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * RemoteSourceData containing metadata needed for the RemoteSource.
     */
    private ProxySourceData _proxySourceData;
    /**
     * The proxy model infrastructure that created the source.
     * The reference to it is needed in order to unblock the thread when the
     * execution is stopped/paused.
     */
    private ProxyModelInfrastructure _proxyModelInfrastructure;

}
