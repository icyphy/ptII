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
protocol. The Datagram is sent to an address given by the actor's
input. Said address is a string, such as "128.32.239.10".  The data
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
public class DatagramSender extends TypedAtomicActor {

    public DatagramSender(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

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

	defaultRemoteAddress =
	        new StringAttribute(this, "defaultRemoteAddress");
	defaultRemoteAddress.setExpression("localhost");

	defaultRemotePort =
	        new Parameter(this, "defaultRemotePort");
        defaultRemotePort.setTypeEquals(BaseType.INT);
        defaultRemotePort.setExpression("-1");

	localPort = new Parameter(this, "localPort");
        localPort.setTypeEquals(BaseType.INT);
        localPort.setExpression("-1");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** This port inputs the data to be sent.
     */
    public TypedIOPort data;

    /** The default remote address to send datagrams.
     */
    public StringAttribute defaultRemoteAddress;

    /** The default remote (UDP) port to send datagrams.
     */
    public Parameter defaultRemotePort;

    /** The local port number for this actor's socket.
     */
    public Parameter localPort;

    /** This port inputs the remote address towards which to launch
     *  the packet.
     */
    public TypedIOPort remoteAddress;

    /** This port inputs the remote (UDP) port number.
     */
    public TypedIOPort remotePort;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** If the parameter changed is <i>localPort</i>, then if the model
     * is running (as evedenced by socket != null) then close socket
     * and reopen with new port number (even if same as old port
     * number).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == localPort) {
	    synchronized(this) {
		if ( _socket != null ) {

		    System.out.println("Current socket port is "
				       + _socket.getLocalPort());

		    int portNum =
			    ((IntToken)(localPort.getToken())).intValue();
		    System.out.println("Port number is " + portNum);
		    try {
			System.out.println("Try create socket for port "
					   + portNum);
			DatagramSocket newSocket = new DatagramSocket(portNum);
			System.out.println("A socket is created!!");
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

    /** Transmits the packed over the ethernet.
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
	    int portNum =
		    ((IntToken)remotePort.get(0)).intValue();
	    if (portNum < 0 || portNum > 65535) {
		// FIXME: port number is invalid, ignore for now
	    } else {
		_remotePortNum = portNum;
	    }
	}

	if (!data.hasToken(0)) {
	    return;
	}

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
        }
    }

    /** Preinitialize
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        int portNum = ((IntToken)(localPort.getToken())).intValue();
        if (portNum < 0 || portNum > 65535) {
            throw new IllegalActionException(this, "Local port number is "
                    + "invalid, must be between 0 and 65535.");
        }
        try {
            System.out.println("PI Try create socket for port " + portNum);
            _socket = new DatagramSocket(portNum);
            System.out.println("PI A socket is created!!");
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

	portNum =
		((IntToken)defaultRemotePort.getToken()).intValue();
	if (portNum < 0 || portNum > 65535) {
	    // FIXME: port number is invalid, ignore for now
	} else {
	    _remotePortNum = portNum;
	}
    }

    /** Wrap up
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

    // Intermediate variables used to [re]construct the packet for
    // transmission.

    private InetAddress _address;
    private int _remotePortNum;
    private DatagramSocket _socket;
}
