/* A servlet that manages locally hosted websockets.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

package org.ptolemy.ptango.lib.webSocket;


import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

///////////////////////////////////////////////////////////////////
//// PtolemyWebSocketServlet

/** A servlet that manages locally hosted websockets. There is one servlet per
 *  URI path.  Each servlet maintains a set of WebSockets affiliated with this
 *  path.  WebSockets are pairwise (client - server).
 *
 *  Uses Jetty 8 WebSocket classes.  Note that Jetty 9 has substantial WebSocket
 *  revisions that are not backward compatible.
 *
 *  Based on http://java.dzone.com/articles/creating-websocket-chat
 *
 *  @author Elizabeth Latronico
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 */

public class PtolemyWebSocketServlet extends WebSocketServlet {

    /** Create a new servlet associated with the given endpoint. */
    public PtolemyWebSocketServlet(WebSocketEndpoint endpoint) {
        _endpoint = endpoint;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Upon receiving a connect request, create a new websocket endpoint to
     * manage communication.
     *
     * @param request The request.
     * @param protocol The protocol of the request.
     * @return A websocket to handle the new connection.
     */
    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request,
            String protocol) {
        return _endpoint;
        
        // TODO:  How to have messages go directly to the endpoint then?
        // Open connection here?
        // Call endpoint.onOpen()?  Or is that called somehow by the connection?
        // Look at example on internet again...
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The endpoint (WebSocket implementer) associated with this servlet. */
    private WebSocketEndpoint _endpoint;
}
