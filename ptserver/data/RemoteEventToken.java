/* Token to indicate to the client that an exception has occurred in
   the running simulation.  The hessian protocol propagates exceptions
   that happened as a result synchronous calls, but the MQTT protocol
   and broker combination do not.  Should an exception occur during the
   simulation, an ExceptionToken will be sent.

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

package ptserver.data;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

import ptolemy.data.Token;

///////////////////////////////////////////////////////////////////
//// RemoteEventToken

/** Encapsulates an informational token raised by the proxy model infrastructure.
 *  @author Justin Killian
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (jkillian)
 *  @Pt.AcceptedRating Red (jkillian)
 */
public class RemoteEventToken extends Token {

    /** Type of notification message being sent to the user.
     */
    public enum EventType {
        /** An exception was raised.
         */
        EXCEPTION,

        /** The server is shutting down.
         */
        SERVER_SHUTDOWN
    }

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Initialize the token using its superclass.
     */
    public RemoteEventToken() {
        super();
    }

    /** Initialize the token with a name and event type.
     *  @param eventType The type of event to alert the user of.
     *  @param message The accompanying message to explain the event.
     */
    public RemoteEventToken(EventType eventType, String message) {
        this();

        _message = message;
        _eventType = eventType;
    }

    /** Initialize the token when an exception has occurred.
     *  @param message The accompanying message to explain the event.
     *  @param exception Exception thrown by the active simulation.
     */
    public RemoteEventToken(String message, Throwable exception) {
        this();
        _eventType = EventType.EXCEPTION;

        // Print the stack trace to the writer.
        StringWriter writer = new StringWriter();
        if (message != null) {
            writer.write("Message: " + message + "\n");
        }
        exception.printStackTrace(new PrintWriter(new BufferedWriter(writer)));
        // Save the message and stack trace.
        _message = writer.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the type of notification sent to the user.
     *  @return The type of the notification.
     */
    public EventType getEventType() {
        return _eventType;
    }

    /** Get the informational message associated with the token.
     *  @return The informational message provided to the user.
     */
    public String getMessage() {
        return _message;
    }

    @Override
    public String toString() {
        return "Event type: " + _eventType + "\n" + _message;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The type of event the token is associated with.
     */
    private EventType _eventType;

    /** The message explaining the event.
     */
    private String _message;
}
