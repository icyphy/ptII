/*
 Adapter for ProxyModelListener.

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

package ptserver.communication;

import ptserver.communication.ProxyModelInfrastructure.ProxyModelListener;
import ptserver.data.RemoteEventToken;

///////////////////////////////////////////////////////////////////
//// ProxyModelAdapter

/**
 * Adapter for ProxyModelListener.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class ProxyModelAdapter implements ProxyModelListener {
    /** Notify listener about the expiration of the model connection to another remote model.
     *  @param proxyModelInfrastructure The infrastructure whose connection expired.
     */
    @Override
    public void modelConnectionExpired(
            ProxyModelInfrastructure proxyModelInfrastructure) {
    }

    /** Notify all model listeners that the simulation has experienced an exception.
     *  @param proxyModelInfrastructure The infrastructure where the exception happened.
     *  @param message The message explaining what has happened.
     *  @param exception The exception that triggered this event.
     */
    @Override
    public void modelException(
            ProxyModelInfrastructure proxyModelInfrastructure, String message,
            Throwable exception) {
    }

    /**
     * Notify the listener about server event received from the remote ProxyModelInfrastructure.
     * @param proxyModelInfrastructure The proxyModelInfrastructure that received the event
     * @param event The remote event
     */
    @Override
    public void onRemoteEvent(
            ProxyModelInfrastructure proxyModelInfrastructure,
            RemoteEventToken event) {

    }

}
