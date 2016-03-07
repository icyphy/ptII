/* Embedding of a Datagram (UDP) socket.

@Copyright (c) 2015-2016 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.actor.lib.jjs.modules.udpSocket;

import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.datagram.DatagramSocket;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.VertxHelperBase;

///////////////////////////////////////////////////////////////////
////UDPSocketHelper

/**
   A helper class for the udpSocket module in JavaScript.
   See the documentation of that module for instructions.

   @author Hokeun Kim
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */

public class UDPSocketHelper extends VertxHelperBase {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Close the UDP socket with a given port number.
     *  @param port The port number.
     */
    public void bind(int port) {
        _socket.listen(port, "0.0.0.0",
                new AsyncResultHandler<DatagramSocket>() {
                    public void handle(AsyncResult<DatagramSocket> asyncResult) {
                        if (asyncResult.succeeded()) {
                            _socket.handler(new Handler<DatagramPacket>() {
                                public void handle(DatagramPacket packet) {
                                    // Do something with the packet
                                    _currentObj.callMember("notifyIncoming",
                                            packet.data().toString());
                                }
                            });
                        } else {
                            System.out.println("Listen failed"
                                    + asyncResult.cause());
                        }
                    }
                });
    }

    /** Close the UDP socket.
     */
    public void close() {
        _socket.close();
    }

    /** Create the UDP socket.
     *  @param scriptObjectMirror The JavaScript instance invoking the shell.
     *  @return The UDP socket helper.
     */
    public static UDPSocketHelper createSocket(
            ScriptObjectMirror scriptObjectMirror) {
        return new UDPSocketHelper(scriptObjectMirror);
    }

    /** Send a UDP message.
     *  @param data An array of bytes to be sent.  Currently Ignored.
     *  @param offset The offset. Current Ignored.
     *  @param length The length of the message. Currently Ignored.
     *  @param port The port.  Currently Ignored.
     *  @param hostname The hostname.  Currently Ignored.
     */
    public void send(byte[] data, int offset, int length, int port,
            String hostname) {
        // FIXME: Why are we not using data here?
        Buffer buffer = Buffer.buffer("content");
        // Send a Buffer
        _socket.send(buffer, 1234, "10.0.0.1",
                new Handler<AsyncResult<DatagramSocket>>() {
                    public void handle(AsyncResult<DatagramSocket> asyncResult) {
                        System.out.println("Send succeeded? "
                                + asyncResult.succeeded());
                    }
                });
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private constructors                  ////
    private UDPSocketHelper(ScriptObjectMirror currentObj) {
        // Need to call super(currentObject) here and avoid
        // "UDPSocketHelper.java:110: error: constructor VertxHelperBase in class VertxHelperBase cannot be applied to given types;"
        super(currentObj);
        _currentObj = currentObj;
        _socket = _vertx.createDatagramSocket();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** The current instance of the Vert.x UDP socket. */
    private DatagramSocket _socket;

    /** The current instance of the JavaScript module. */
    private ScriptObjectMirror _currentObj;

}
