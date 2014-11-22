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

package org.ptolemy.ptango.lib.webSocket;

import java.io.IOException;
import java.util.HashSet;

import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.ptolemy.ptango.lib.webSocket.WebSocketService;


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
    * @param service  The WebSocketService to be notified of messages.
    */
   public WebSocketEndpoint() {
       _connection = null;
       _isOpen = false;
       _subscribers = new HashSet();
   }
    
    /** Create a new WebSocketEndpoint with the given parent service.
     * @param service  The WebSocketService to be notified of messages.
     */
    public WebSocketEndpoint(WebSocketService service) {
        _connection = null;
        _isOpen = false;
        _subscribers = new HashSet();
        _subscribers.add(service);
    }

    /** Add a subscriber to this endpoint.
     * @param service  A parent WebSocketService of this endpoint.
     * @see #removeSubscriber(WebSocketService)
     */
    public void addSubscriber(WebSocketService service) {
        _subscribers.add(service);
    }
    
    /** Close the connection. _connection will be assigned to null in onClose(),
     * which is invoked automatically after a close().
     */
    public void close(){
        if (_connection != null){
            _connection.close();
        }
    }

    /** Return the number of subscribers this endpoint has.
     * @return The number of subscribers this endpoint has.
     */
    public int getSubscriberCount() {
        return _subscribers.size();
    }
    
    /** Return true if the connection is open; false otherwise.
     * @return True if the connection is open; false otherwise.
     */
    public boolean isOpen() {
        return _isOpen;
    }

    /** Upon close, set the connection to null.  Note that the connection might 
     *  be closed unexpectedly and close() is not necessarily called. 
     *  Synchronized so that onOpen() and onClose() can't be interleaved.
     *  @param statusCode The status code of the closed connection.
     *  @param statusMessage The status message of the closed connection.
     *  @see #onClose(int, String)
     */
    @Override
    public synchronized void onClose(int statusCode, String statusMessage) {
        _connection = null;
        _isOpen = false;
    }

    /** Notify subscribers about a new message.
     * @param message The message that was received.
     */
    @Override
    public void onMessage(String message) {
        if (_subscribers != null) {
            for (WebSocketService service : _subscribers) {
                service.onMessage(message);
            }
        }
    }

    /** Upon opening, save a reference to the connection. Synchronized so that
     * onOpen() and onClose() can't be interleaved.
     * @param connection The connection that was opened.
     * @see #onOpen(Connection)
     */
    @Override
    public synchronized void onOpen(Connection connection) {
        _connection = connection;
        _isOpen = true;
    }

    /** Remove the given subscriber for this endpoint.
     * @param service  A parent WebSocketService of this endpoint.
     * @see #addSubscriber(WebSocketService)
     */
    public void removeSubscriber(WebSocketService service) {
        _subscribers.remove(service);
    }
    
    /** Send the given message.  If the connection is not open, return false.
     *  If the connection is not open, there are multiple potential options: 
     *  the sender could block, messages could be buffered, messages could be
     *  discarded, or some combination.  Currently, messages are discarded.
     *  Synchronized so that only one sender can write to the websocket at a 
     *  time.  Returns true if the message was successfully sent; false 
     *  otherwise.
     *  @param message The message to send.
     *  @return True if the message was successfully sent; false otherwise.
     */
    // TODO:  Could enhance by trying to open connection if closed. 
    // TODO:  Enhance this to handle waiting on connection, buffering, etc.
    // SENT
    // BUFFERED
    // WAITING (on connection)
    // FAILED
    public synchronized boolean sendMessage(String message) {
        if (_isOpen && _connection != null) {
            try {
                _connection.sendMessage(message);
            }catch(IOException e) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    // TODO:  Do something onError?

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The WebSocket connection */
    private Connection _connection;
    
    /** An exception that might be thrown when connecting.  */
    // TODO:  Do something with this exception?  
    Exception _connectionException;

    /** Flag indicating if the connection is open. */
    private boolean _isOpen;

    /** The parent services to be notified of incoming messages. */
    private HashSet<WebSocketService> _subscribers;
}
