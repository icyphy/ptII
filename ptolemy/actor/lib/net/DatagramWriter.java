/* An actor that sends its <i>data</i> input as a UDP datagram packet.

Copyright (c) 2001-2005 The Regents of the University of California.
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
package ptolemy.actor.lib.net;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


//////////////////////////////////////////////////////////////////////////
//// DatagramWriter

/**
   This actor sends its input as a Datagram over the network using the
   UDP protocol.  Before being sent, the data is optionally encoded as a
   text string representing the value being sent.  When this option is
   selected, any Ptolemy data type may be represented.
   See the <i>encoding</i> parameter.

   <p>The address and socket number towards which the datagram is sent
   are given by the optional inputs <i>remoteAddress</i> and
   <i>remoteSocketNumber</i>.  However, unless/until token(s) arrive at
   these inputs, default values, given by <i>defaultRemoteAddress</i> and
   <i>defaultRemoteSocketNumber</i> are used.  Note that some IP
   addresses are special broadcast addresses.  An address such as
   "128.32.239.255" broadcasts to any IP addresses on the "128.23.239.X"
   subnet.  This only works on your own subnet.

   <p>Each instance of this actor needs to allocate a local socket from
   which to transmit datagrams.  Initially, the local socket number is
   set to 4003, just to pick a number.  The socket is not allocated
   until the model is run.

   @author Winthrop Williams, Joern Janneck, Xiaojun Liu, Edward A. Lee
   (Based on TiltSensor actor written by
   Chamberlain Fong, Xiaojun Liu, Edward Lee)
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Yellow (winthrop)
   @Pt.AcceptedRating Yellow (winthrop)
*/
public class DatagramWriter extends TypedAtomicActor {
    /** Construct a DatagramWriter actor with given name in the given
     *  container.  Set up ports, parameters and default values.  Two
     *  of the parameters, <i>defaultRemoteAddress</i> and
     *  <i>defaultRemoteSocketNumber</i>, give default values for when
     *  the <i>remoteAddress</i> and <i>remoteSocketNumber</i> ports
     *  have not yet received tokens.  I wonder if the port and
     *  parameter concepts could be combined in some way to factor out
     *  this functionality.  Perhaps every port could have the
     *  capability of having a default value and a setting as to
     *  whether this default takes effect every time the actor is
     *  fired and a token is absent of whether the previous input is
     *  repeated once an has been present.
     *  @param container The container.
     *  @param name The name for this actor.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     *  @exception IllegalActionException If the actor cannot be contained by
     *   this container.
     */
    public DatagramWriter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Ports
        remoteAddress = new TypedIOPort(this, "remoteAddress");
        remoteAddress.setInput(true);
        remoteAddress.setMultiport(true);
        remoteAddress.setTypeEquals(BaseType.STRING);

        remoteSocketNumber = new TypedIOPort(this, "remoteSocketNumber");
        remoteSocketNumber.setInput(true);
        remoteSocketNumber.setMultiport(true);
        remoteSocketNumber.setTypeEquals(BaseType.INT);

        data = new TypedIOPort(this, "data");
        data.setInput(true);
        data.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        // Parameters that are default values for ports
        //defaultRemoteAddress =
        //        new StringAttribute(this, "defaultRemoteAddress");
        //defaultRemoteAddress.setExpression("localhost");
        // Above way was set w/o quotes vs constant which is set with them.
        // This has been confusing, so I've switched to the approach below.
        defaultRemoteAddress = new Parameter(this, "defaultRemoteAddress");
        defaultRemoteAddress.setTypeEquals(BaseType.STRING);
        defaultRemoteAddress.setToken(new StringToken("localhost"));

        defaultRemoteSocketNumber = new Parameter(this,
                "defaultRemoteSocketNumber");
        defaultRemoteSocketNumber.setTypeEquals(BaseType.INT);
        defaultRemoteSocketNumber.setExpression("4004"); //setExpression works

        // Pure parameters
        localSocketNumber = new Parameter(this, "localSocketNumber");
        localSocketNumber.setTypeEquals(BaseType.INT);
        localSocketNumber.setToken(new IntToken(4003)); //setToken works too

        // Added for SDF usability.  Empty Token() is output, just a trigger.
        triggerOutput = new TypedIOPort(this, "triggerOutput");
        triggerOutput.setTypeEquals(BaseType.GENERAL);

        // 'INT' works too in place of 'GENERAL'.
        triggerOutput.setOutput(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The triggerOutput port.  The type of this port is GENERAL,
     *  forcing input ports connected here to also be of type GENERAL,
     *  (as trigger inputs typically are).  This port always transmits
     *  a Token with nothing in it.  This gives the designer a way to
     *  control the order in which other actors fire with respect to
     *  the firing of this actor in the SDF domain.
     */
    public TypedIOPort triggerOutput;

    /** The local socket number for this actor's socket.  Integer in
     *  the range 0..65535.  Default is 4003.
     */
    public Parameter localSocketNumber;

    /** Data to be sent.  Data can be encoded different ways,
     *  depending on the setting of <i>encoding</i>.
     */
    public TypedIOPort data;

    /** The default remote address to which to send datagrams.
     *  This is a string.  It will get looked up to find the IP address.
     *  (Legal forms of this string include "128.32.239.10" and "localhost".)
     */
    public Parameter defaultRemoteAddress;

    /** The remote address towards which to launch the packet.
     *  This is a multiport.  This permits it to be left unconnected in DE.
     *  If multiple addresses are delivered simultaneously to this port,
     *  the one arriving via the highest numbered channel is used.
     *  Type is string.
     */
    public TypedIOPort remoteAddress;

    /** The default remote UDP socket to which to launch the packet.
     *  This is an integer in 0..65535.  NOTE: TCP sockets
     *  get their own distinct, non-interfering, set of 2^16 numbers! */
    public Parameter defaultRemoteSocketNumber;

    /** The remote socket number towards which to launch the packet.
     *  This is a multiport.  This permits it to be left unconnected in DE.
     *  If multiple addresses are delivered simultaneously to this port,
     *  the one arriving via the highest numbered channel is used.
     *  Type is integer.
     */
    public TypedIOPort remoteSocketNumber;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to reinitialize the state if the
     *  <i>localSocketNumber</i>, <i>defaultRemoteAddress</i> or
     *  <i>defaultRemoteSocketNumber</i> parameter has changed.
     *
     *  <p>If the parameter changed is <i>localSocketNumber</i>, then
     *  if the model is running (as evidenced by socket != null) then
     *  close socket and reopen with new socket number (even if it is
     *  the same as the old socket number).  Do not close the socket
     *  until a new one has been successfully opened.  If
     *  <i>defaultRemoteAddress</i> or
     *  <i>defaultRemoteSocketNumber</i> is changed, simply update
     *  these parameters, checking, in the case of the address, that
     *  it passes lookup anc conversion to an IP address.  If the
     *  <i>encoding</i> parameter is changed, set the private encoding
     *  settings to the new values.
     *
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the socket cannot be created.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == localSocketNumber) {
            synchronized (this) {
                if (_socket != null) {
                    if (_debugging) {
                        _debug("Current socket port is "
                                + _socket.getLocalPort());
                    }

                    _localSocketNumber = ((IntToken) (localSocketNumber
                                                  .getToken())).intValue();

                    if (_debugging) {
                        _debug("Socket number is " + _localSocketNumber);
                    }

                    try {
                        if (_debugging) {
                            _debug("Try create socket for port "
                                    + _localSocketNumber);
                        }

                        DatagramSocket newSocket = new DatagramSocket(_localSocketNumber);

                        if (_debugging) {
                            _debug("A socket is created!!");
                        }

                        _socket.close();
                        _socket = newSocket;
                    } catch (SocketException ex) {
                        throw new IllegalActionException(this, ex,
                                "Cannot create socket on the given "
                                + "local socket number.");
                    }
                }
            }
        } else if (attribute == defaultRemoteAddress) {
            String address = ((StringToken) defaultRemoteAddress.getToken())
                .stringValue();

            try {
                _address = InetAddress.getByName(address);
            } catch (UnknownHostException ex) {
                throw new IllegalActionException(this, ex,
                        "The default remote " + "address specifies an unknown host");
            }
        } else if (attribute == defaultRemoteSocketNumber) {
            _remoteSocketNumber = ((IntToken) defaultRemoteSocketNumber
                    .getToken()).intValue();
            _remoteSocketNumber &= 65535; // Truncate to 16 bits.
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Does up to three things, in this order: Set new remote address value,
     *  Set new remote socket number, transmit data as a UDP packet over the
     *  ethernet.  The first two can, of course, affect where the datagram
     *  goes.  Any remote address/socket values supplied are saved and
     *  become the defaults for next time.
     */
    public void fire() throws IllegalActionException {
        String address = null;

        for (int jj = 0; jj < remoteAddress.getWidth(); jj++) {
            if (remoteAddress.hasToken(jj)) {
                address = ((StringToken) (remoteAddress.get(jj))).stringValue();
            }
        }

        if (address != null) {
            try {
                _address = InetAddress.getByName(address);
            } catch (UnknownHostException ex) {
                throw new IllegalActionException(this, ex,
                        "The input remote " + "address specifies an unknown host");
            }
        }

        for (int jj = 0; jj < remoteSocketNumber.getWidth(); jj++) {
            if (remoteSocketNumber.hasToken(jj)) {
                // Valid socket numbers are 0..65535,
                // so keep only lower 16 bits.
                _remoteSocketNumber = 65535
                    & ((IntToken) remoteSocketNumber.get(jj))
                    .intValue();
            }
        }

        if (data.hasToken(0)) {
            ArrayToken dataArrayToken = (ArrayToken) data.get(0);

            byte[] dataBytes = new byte[dataArrayToken.length()];

            for (int j = 0; j < dataArrayToken.length(); j++) {
                UnsignedByteToken token = (UnsignedByteToken) dataArrayToken
                    .getElement(j);
                dataBytes[j] = (byte) token.byteValue();
            }

            DatagramPacket packet = new DatagramPacket(dataBytes,
                    dataBytes.length, _address, _remoteSocketNumber);

            try {
                _socket.send(packet);
            } catch (IOException ex) {
                // ignore, UDP does not guarantee success
                //throw new InternalErrorException("socket.send failed");
                //FIXME  I don't believe that!  I think send guarantees that
                // it will send!!  Just not that anyone is listening.
                //    (yet when I ran it with 'throw...' uncommented
                //     then it threw it right away!? )
                // Would TCP stall here awaiting reply??  I doubt it!
            }

            triggerOutput.broadcast(new Token());
        }
    }

    /** Initialize allocates the socket and makes use of default
     *  parameters for the remote address and socket to which datagrams
     *  will be sent.
     *  @exception IllegalActionException If
     *   the default remote socket number is outside the range 0..65535,
     *   the local socket number is outside the range 0..65535,
     *   the local socket cannot be created with the given
     *  socket number (such as because that number is already in use),
     *   or translation of remote address fails to make an IP
     *  address from the given address string (i.e. InetAddress.getByName()
     *  fails in the address lookup attempt.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _localSocketNumber = ((IntToken) (localSocketNumber.getToken()))
            .intValue();

        if ((_localSocketNumber < 0) || (_localSocketNumber > 65535)) {
            throw new IllegalActionException(this,
                    "Local socket number " + _localSocketNumber
                    + " must be between 0 and 65535.");
        }

        try {
            if (_debugging) {
                _debug("PI Try create socket number " + _localSocketNumber);
            }

            _socket = new DatagramSocket(_localSocketNumber);

            if (_debugging) {
                _debug("PI A socket is created!!");
            }
        } catch (SocketException ex) {
            throw new IllegalActionException(this, ex,
                    "Cannot create socket on "
                    + "the specified local socket number");
        }

        String address = ((StringToken) defaultRemoteAddress.getToken())
            .stringValue();

        try {
            _address = InetAddress.getByName(address);
        } catch (UnknownHostException ex) {
            throw new IllegalActionException(this, ex,
                    "The default remote " + "address specifies an unknown host");
        }

        _remoteSocketNumber = ((IntToken) defaultRemoteSocketNumber.getToken())
            .intValue();

        if ((_remoteSocketNumber < 0) || (_remoteSocketNumber > 65535)) {
            _remoteSocketNumber &= 65535; // Truncate to 16 bits.
            throw new IllegalActionException(this,
                    "defaultRemoteSocketNumber" + _remoteSocketNumber
                    + " is out of range, must be between 0 and 65535.");
        }
    }

    /** Override the setContainer() method to call wrapup() if
     *  container is not equal to the result of getContainer().  If
     *  this method did not override super.setContainer(), then when
     *  the actor is deleted while the model is running, wrapup()
     *  would never get called.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (container != getContainer()) {
            wrapup();
        }

        super.setContainer(container);
    }

    /** Wrap up.  Free the socket, allowing the socket number to be reused.
     *  @exception IllegalActionException If the socket was already null.
     */
    public void wrapup() throws IllegalActionException {
        synchronized (this) {
            if (_socket != null) {
                _socket.close();
                _socket = null;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Remote Address and socket number for construction of packet.
    private InetAddress _address;
    private int _remoteSocketNumber;

    // The socket (& socket number) from which to transmit datagram packets.
    private DatagramSocket _socket;
    private int _localSocketNumber;
}
