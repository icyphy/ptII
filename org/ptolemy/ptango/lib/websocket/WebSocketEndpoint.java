/* A class that stores a reference to a WebSocket connection and notifies 
 the parent WebSocketService of incoming messages.

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

package org.ptolemy.ptango.lib.websocket; 

import java.util.HashSet;

import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;

///////////////////////////////////////////////////////////////////
////WebSocketEndpoint

/** A class that keeps a reference to the WebSocket connection, notifies
 *  services of incoming messages and allows services to write to the WebSocket.
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

public class WebSocketEndpoint implements OnTextMessage {
    
    /** Create a new WebSocketEndpoint with the given parent service.
     * 
     * @param parentService  The WebSocketService to be notified of messages.
     */
    public WebSocketEndpoint(WebSocketService parentService) {
        _connection = null;
        _isOpen = false;
        _parentServices = new HashSet();
        _parentServices.add(parentService);
    }
    
    /** Add a parent WebSocketService of this endpoint.
     * 
     * @param service  A parent WebSocketService of this endpoint.
     * @see #removeParentService(WebSocketService)
     */
    public void addParentService(WebSocketService service) {
        _parentServices.add(service);
    }
    
    /** Return the Connection object for this WebSocket.  Used for sending
     *  messages.
     *  
     * @return  The Connection object for this WebSocket.
     */
    public Connection getConnection() {
        return _connection;
    }
    
    /** Return true if the connection is open; false otherwise. 
     * 
     * @return True if the connection is open; false otherwise.
     */
    public boolean isOpen() {
        return _isOpen;
    }
    
    /** Upon close, set the connection to null.
     * 
     *  @param statusCode The status code of the closed connection.
     *  @param statusMessage The status message of the closed connection.
     */
    @Override
    public void onClose(int statusCode, String statusMessage) {
        _connection = null;
        _isOpen = false;
    }
    
    /** Notify the parent service about a new message.
     * 
     * @param message The message that was received.
     */
    @Override
    public void onMessage(String message) {
        if (_parentServices != null) {
            for (WebSocketService service : _parentServices) {
                service.onMessage(this, message);
            }
        }
    }

    /** Upon opening, save a reference to the connection.
     * 
     * @param connection The connection that was opened.
     */
    @Override
    public void onOpen(Connection connection) {
        _connection = connection;
        _isOpen = true;
    }
    
    /** Remove a parent WebSocketService of this endpoint.
     * 
     * @param service  A parent WebSocketService of this endpoint.
     * @see #addParentService(WebSocketService)
     */
    public void removeParentService(WebSocketService service) {
        _parentServices.remove(service);
    }
    
    // TODO:  Do something onError? 
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The WebSocket connection */
    private Connection _connection;
    
    /** Flag indicating if the connection is open. */
    private boolean _isOpen;
    
    /** The parent services to be notified of incoming messages. */
    private HashSet<WebSocketService> _parentServices;
}
