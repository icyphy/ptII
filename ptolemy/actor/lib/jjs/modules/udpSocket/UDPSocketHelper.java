/* Embedding of a Datagram (UDP) socket.

   Copyright (c) 2014 The Regents of the University of California.
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

package ptolemy.actor.lib.jjs.modules.udpSocket;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.datagram.DatagramPacket;
import org.vertx.java.core.datagram.DatagramSocket;
import org.vertx.java.core.datagram.InternetProtocolFamily;

import ptolemy.actor.lib.jjs.modules.VertxHelperBase;


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
    ////                     public methods                        ////
    
    /** Close the UDP socket with a given port number.
     */
    public void bind(int port) {
        _socket.listen("0.0.0.0", port, new AsyncResultHandler<DatagramSocket>() {
            public void handle(AsyncResult<DatagramSocket> asyncResult) {
                if (asyncResult.succeeded()) {
                   _socket.dataHandler(new Handler<DatagramPacket>() {
                        public void handle(DatagramPacket packet) {
                            // Do something with the packet
                            _currentObj.callMember("notifyIncoming", packet.data().toString());
                        }
                    });
                } else {
                    System.out.println("Listen failed" + asyncResult.cause());
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
     */
    public static UDPSocketHelper createSocket(ScriptObjectMirror currentObj)
    {
        return new UDPSocketHelper(currentObj);
    }

    /** Send a UDP message.
     */
    public void send(byte[] buf, int offset, int length, int port, String hostname) {
        Buffer buffer = new Buffer("content");
        // Send a Buffer
        _socket.send(buffer, "10.0.0.1", 1234, new AsyncResultHandler<DatagramSocket>() {
         public void handle(AsyncResult<DatagramSocket> asyncResult) {
             System.out.println("Send succeeded? " + asyncResult.succeeded());
         }
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private constructors                  ////
    private UDPSocketHelper(ScriptObjectMirror currentObj)
    {
        _currentObj = currentObj;
        _socket = _vertx.createDatagramSocket(InternetProtocolFamily.IPv4);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** The current instance of the Vert.x UDP socket. */
    private DatagramSocket _socket;
    
    /** The current instance of the JavaScript module. */
    private ScriptObjectMirror _currentObj;

}
