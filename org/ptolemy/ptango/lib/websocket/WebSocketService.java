/* An interface for actors that handle HTTP requests */

package org.ptolemy.ptango.lib.websocket;

import java.net.URI;

import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.ptolemy.ptango.lib.WebServer;


/*
 Copyright (c) 1997-2013 The Regents of the University of California.
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

/** An interface for actors that handle WebSocket requests.  The interface
 *  allows the relative path for the WebSocket request to be set and obtained,
 *  and provides a servlet to handle requests. If this interface is
 *  implemented by an actor or attribute in a model that contains
 *  an instance of {@link WebServer}, then requests to that server
 *  that match the relative path set by the {@link #setRelativePath(URI)}
 *  method of this interface will be delegated to that actor or attribute.
 *  <p>
 *  @see WebServer
 *  @author Elizabeth Latronico and Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 *  @see org.ptolemy.ptango.lib.WebServer
 */

public interface WebSocketService {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Returns the relative path that this WebSocketService is mapped to,
     *  which is the value set previously by a call to
     *  {@link #setRelativePath(URI)}.
     *  @return The relative path that this HttpService is mapped to.
     *  @see #setRelativePath(URI)
     */
    public URI getRelativePath();

    /** Notify service of a WebSocket message event.
     * @param sender The PtolemyWebSocketEndpoint that sent the message
     * @param message The message that was received
     */
    public void onMessage(WebSocketEndpoint sender, String message);

    /** Set the websocket connection that the service should use.
     * @param The connection that the service should use.
     */
    public void setConnection(Connection connection);

    /** Set the relative path that this WebSocketService is mapped to.
     *  @param relativePath The relative path that this HttpService is mapped to.
     *  @see #getRelativePath()
     */
    public void setRelativePath(URI relativePath);
}
