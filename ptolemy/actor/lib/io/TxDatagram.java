/* An actor that transmits a trivial UDP Datagram packet.

 Copyright (c) 2001 The Regents of the University of California.
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

@ProposedRating red (winthrop@eecs.berkeley.edu)
@AcceptedRating red (winthrop@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.io;

import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.SDFIOPort;
import ptolemy.actor.lib.*;

import java.io.*;
import java.net.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// TxDatagram
/**

This actor transmits a Datagram over the Ethernet using the UDP
protocol.  The Datagram is sent to an address given by the actor's
input.  Said address is a string, such as "128.32.239.10".  The data
contained in the Datagram is the single byte 'A'.  Although this
actor's port number is selectable (parameter port), The actor always
launches its datagram towards port 4096 of the remote address.

Initially, the local port number is set to -1 to indicate no port at
all.  This prevents the actor still "in the barn" from having a port
number conflict with the actor that is being places into the graph
editor's work area.

@author Winthrop Williams, Joern Janneck,  Xiaojun Liu, Edward A. Lee
(Based on TiltSensor actor writen by Chamberlain Fong, Xiaojun Liu, Edward Lee)
@version $Id$
*/
public class TxDatagram extends TypedAtomicActor {

    public TxDatagram(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        remoteAddress = new TypedIOPort(this, "remoteAddress");
        remoteAddress.setInput(true);
        port = new Parameter(this, "port");
        port.setTypeEquals(BaseType.INT);
        port.setToken(new IntToken(-1));
        //attributeChanged(port);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** This port inputs the remote address towards which to launch
     *  the packet.
     */

    public TypedIOPort remoteAddress;

    /** The local port number for this actor's socket.
     */
    public Parameter port;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** If the parameter changed is <i>port</i>, then if the model
     * is running (as evedenced by socket != null) then close socket
     * and reopen with new port number (even if same as old port
     * number).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == port) {
            if ( socket != null ) {

                System.out.println("Current socket port is "
                        + socket.getLocalPort());
                socket.close();

                int portNum = ((IntToken)(port.getToken())).intValue();
                System.out.println("Port number is " + portNum);
                try {
                    System.out.println("Try create socket for port "
                            + portNum);
                    socket = new DatagramSocket(portNum);
                    System.out.println("A socket is created!!");
                }
                catch (SocketException ex) {
                    /* ignore */
                    ex.printStackTrace();
                    throw new InternalErrorException("cannot create socket");
                }

            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Transmits the packed over the ethernet.
     */
    public void fire() throws IllegalActionException {

        String _address =
            ((StringToken)(remoteAddress.get(0))).stringValue();
        try {
            __address = InetAddress.getByName(_address);
        }
        catch (UnknownHostException ex) {
            /* Ignore also */
            throw new InternalErrorException("InetAddress.getByName failed");
        }
        int _port = 4096;
        byte _data[] = {'A'};
        int _length = 1;
        DatagramPacket __packet =
            new DatagramPacket(_data, _length, __address, _port);

        try {
            socket.send(__packet);
        }
        catch (IOException ex) {
            /* Ignore for now */
            throw new InternalErrorException("socket.send failed");
        }
    }

    /** Preinitialize
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        int portNum = ((IntToken)(port.getToken())).intValue();
        if (portNum == -1) {
            throw new IllegalActionException("Model cannot be run with "
                    + "port == -1");
        }
        try {
            System.out.println("PI Try create socket for port " + portNum);
            socket = new DatagramSocket(portNum);
            System.out.println("PI A socket is created!!");
        }
        catch (SocketException ex) {
            /* ignore */
            ex.printStackTrace();
            throw new InternalErrorException("PI cannot create socket");
        }
    }


    /** Wrap up
     */
    public void wrapup() throws IllegalActionException {
        if (socket != null) {
            socket.close();
            socket = null;
        } else {
            throw new IllegalActionException("Socket was already null "
                    +  "at wrapup!?");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Intermediate variables used to [re]construct the packet for
    // transmission.

    private byte _data;
    private int _length;
    private String _address;
    private InetAddress __address;
    private int _port;
    private DatagramPacket __packet;
    private DatagramSocket socket;
}
