/* An actor that sends its input as a UDP datagram packet.

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
//// DatagramSender
/**
This actor sends its input as a Datagram over the Ethernet using the UDP
protocol.  Before sending it, the data is encoded as a text string
representing the value being sent.  The address and port number to which the
datagram is sent is given by optional inputs.  Each optional input comes
with a parameter which gives its default value.  (This is cumbersome.
Is there a way to merge the concepts of Parameter and TypedIoPort?)
The data and the optional inputs may arrive at any time.  Optional
inputa arriving along with or before data control the data's destination.

Initially, the local port number is set to -1 to indicate no port at
all.  This prevents the actor still "in the barn" from having a port
number conflict with the actor that is being placed into the graph
editor's work area.

@author Winthrop Williams, Joern Janneck,  Xiaojun Liu, Edward A. Lee
(Based on TiltSensor actor writen by Chamberlain Fong, Xiaojun Liu, Edward Lee)
@version $Id$
*/


/** Construct a DatagramSender actor with given name in the given container.
 *  Set up ports and parameters and default values.  Two of the parameters
 *  are used to give default values for the remoteAddress and remotePort
 *  ports.
 *  @param container The container.
 *  @param name The name for this actor.
 *  @exception NameDuplicationException If the container already has an
 *   actor with this name.
 *  @exception IllegalActionException If the actor cannot be contained by
 *   this container
 */
public class DatagramSender extends TypedAtomicActor {

    public DatagramSender(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Ports
        remoteAddress = new TypedIOPort(this, "remoteAddress");
        remoteAddress.setInput(true);
        remoteAddress.setMultiport(true);
        remoteAddress.setTypeEquals(BaseType.STRING);

        remotePort = new TypedIOPort(this, "remotePort");
        remotePort.setInput(true);
        remotePort.setMultiport(true);
        remotePort.setTypeEquals(BaseType.INT);

        data = new TypedIOPort(this, "data");
        data.setInput(true);
        data.setTypeEquals(BaseType.GENERAL);

        // Parameters that are default values for ports
        defaultRemoteAddress =
                new StringAttribute(this, "defaultRemoteAddress");
        defaultRemoteAddress.setExpression("localhost");

        defaultRemotePort =
                new Parameter(this, "defaultRemotePort");
        defaultRemotePort.setTypeEquals(BaseType.INT);
        defaultRemotePort.setExpression("-1");

        // Pure parameter
        localPort = new Parameter(this, "localPort");
        localPort.setTypeEquals(BaseType.INT);
        localPort.setExpression("-1");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The local port-number for this actor's socket.
     *  Integer in the range 0..65535.
     */
    public Parameter localPort;

    /** Data to be sent.  Data will be encoded as a text string, much as
     *  if it were printed.  This text string is what is sent.  Thus any
     *  datatype, including arrays etc. is handled.  Ptolemy parser must
     *  be used to deconstruct on the other side.
     */
    public TypedIOPort data;

    /** The default remote address to which to send datagrams.
     *  This is a string.  It will get looked up to find the IP address.
     *  (Legal forms of this string include "128.32.239.10" and "localhost".)
     */
    // FIXME Why is this not simply a 'Parameter' like localPort?
    public StringAttribute defaultRemoteAddress;

    /** The remote address towards which to launch the packet.
     */
    public TypedIOPort remoteAddress;

    /** The default remote (UDP (are there other kinds?) ) port to
     *  which to send datagrams.  This is an integer in 0..65535.
     */
    public Parameter defaultRemotePort;

    /** The remote (UDP (Can TCP reuse a UDP number?) ) port number.
     */
    public TypedIOPort remotePort;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** If the parameter changed is <i>localPort</i>, then if the model
     *  is running (as evedenced by socket != null) then close socket
     *  and reopen with new port number (even if same as old port
     *  number).
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If cannot create socket.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == localPort) {
            synchronized(this) {
                if (_socket != null) {
                    if (_debugging) _debug("Current socket port is "
                                       + _socket.getLocalPort());

                    int portNum =
                        ((IntToken)(localPort.getToken())).intValue();
                    if (_debugging) _debug("Port number is " + portNum);
                    try {
                        if (_debugging) _debug("Try create socket for port "
                                           + portNum);
                        DatagramSocket newSocket = new DatagramSocket(portNum);
                        if (_debugging) _debug("A socket is created!!");
                        _socket.close();
                        _socket = newSocket;
                    }
                    catch (SocketException ex) {
                        throw new IllegalActionException(this,
                                "Cannot create socket on the given "
                                + "local port: " + ex.getMessage());
                    }
                }
	    }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Does up to three things, in this order: Set new remote address value,
     *  Set new remote port value, transmit data as a UDP packet over the
     *  ethernet.  The first two can, of course, affect where the datagram
     *  goes.
     */
    public void fire() throws IllegalActionException {
        if (remoteAddress.getWidth() > 0 && remoteAddress.hasToken(0)) {
            String address =
                ((StringToken)(remoteAddress.get(0))).stringValue();
            try {
                _address = InetAddress.getByName(address);
            }
            catch (UnknownHostException ex) {
                throw new IllegalActionException(this, "The input remote "
                        + "address specifies an unknown host: "
                        + ex.getMessage());
            }
        }

        if (remotePort.getWidth() > 0 && remotePort.hasToken(0)) {
            // Valid port numbers are 0..65535 so keep only lower 16 bits.
            _remotePortNum = 65535 & ((IntToken)remotePort.get(0)).intValue();
        }

        if (data.hasToken(0)) {
            byte[] dataBytes = data.get(0).toString().getBytes();
            DatagramPacket packet =
                    new DatagramPacket(dataBytes, dataBytes.length,
                    _address, _remotePortNum);

            try {
                _socket.send(packet);
            }
            catch (IOException ex) {
                // ignore, UDP does not guarantee success
                //throw new InternalErrorException("socket.send failed");
                //FIXME  I don't believe that!  I think send guarantees that
                // it will send!!  Just not that anyone is listening.
                //    (yet when I ran it with 'throw...' uncommented
                //     then it threw it right away!? )
                // Would TCP stall here awaiting reply??  I doubt it!
            }
        }

    }

    /** Preinitialize allocates the socket and makes use of default
     *  parameters for the remote address and socket to which datagrams
     *  will be sent.  InetAddress.getByName does the address lookup,
     *  and can fail (see below).  The remote port number need only
     *  be in the 0 .. 65535 range.  However, the local port number
     *  must be in range and must allow new DatagramSocket(portNum) to
     *  successfully create a socket with that port number.
     *  @exception IllegalActionException If port number is beyond 16 bits,
     *   the socket cannot be created with the given port number,
     *   translation of remote address fails to make IP address from
     *   address string, or the romote port number is beyond 16 bits.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        int portNum = ((IntToken)(localPort.getToken())).intValue();
        if (portNum < 0 || portNum > 65535) {
            throw new IllegalActionException(this, "Local port number is "
                    + "invalid, must be between 0 and 65535.");
        }
        try {
            if (_debugging) _debug("PI Try create socket for port " + portNum);
            _socket = new DatagramSocket(portNum);
            if (_debugging) _debug("PI A socket is created!!");
        }
        catch (SocketException ex) {
            throw new IllegalActionException(this, "Cannot create socket on "
                    + "the specified local port: " + ex.getMessage());
        }

        String address =
                defaultRemoteAddress.getExpression();
        try {
            _address = InetAddress.getByName(address);
        }
        catch (UnknownHostException ex) {
            throw new IllegalActionException(this, "The default remote "
                    + "address specifies an unknown host: "
                    + ex.getMessage());
        }

        int remotePortNum =
                ((IntToken)defaultRemotePort.getToken()).intValue();
        if (remotePortNum < 0 || remotePortNum > 65535) {
            throw new IllegalActionException(this, "Default remote port number"
                    + " is invalid, must be between 0 and 65535.");
        } else {
            _remotePortNum = remotePortNum;
        }
    }

    /** Wrap up.  Free the socket, allowing the port number to be reused.
     *  @exception IllegalActionException If the socket was already null.
     */
    public void wrapup() throws IllegalActionException {
        synchronized(this) {
            if (_socket != null) {
                _socket.close();
                _socket = null;
            } else {
                throw new IllegalActionException("Socket was already null "
                        +  "at wrapup!?");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Remote Address and port number for construction of packet.
    private InetAddress _address;
    private int _remotePortNum;

    // The socked from which to transmit datagram packets.
    private DatagramSocket _socket;
}









