/* An actor that sends its <i>data</i> input as a UDP datagram packet.

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

package ptolemy.actor.lib.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
//// DatagramSender
/**
This actor sends its input as a Datagram over the network using the
UDP protocol.  Before being sent, the data is optionally encoded as a
text string representing the value being sent.  Any Ptolemy data type
may be represented.  See the <i>encodeForPtolemyParser</i> parameter.

The address and socket number towards which the datagram is sent are
given by optional inputs <i>remoteAddress</i> and <i>remoteSocketNumber</i>.
Each optional input has an associated parameter giving its default value.
The default values are used unless/until replaced by a token arriving at
that optional input.

Each instance of this actor needs to allocate a local socket from
which to transmit datagrams.  Initially, the local socket number is
set to -1 to cause no socket to be allocated at all.  This prevents
the library's instance of this actor from having a socket number
conflict with the actor that is being placed into the graph editor's
work area.  FIXME - Try allocating the socket in initialize() instead
of preinitialize() to better avoid this conflict.

@author Winthrop Williams, Joern Janneck, Xiaojun Liu, Edward A. Lee
(Based on TiltSensor actor writen by Chamberlain Fong, Xiaojun Liu, Edward Lee)
@version $Id$ */


/** Construct a DatagramSender actor with given name in the given
 *  container.  Set up ports, parameters and default values.  Two of
 *  the parameters are used to give default values for the
 *  <i>remoteAddress</i> and <i>remoteSocketNumber</i> ports.
 *  @param container The container.
 *  @param name The name for this actor.
 *  @exception NameDuplicationException If the container already has an
 *   actor with this name.
 *  @exception IllegalActionException If the actor cannot be contained by
 *   this container */
public class DatagramSender extends TypedAtomicActor {

    public DatagramSender(CompositeEntity container, String name)
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
        data.setTypeEquals(BaseType.GENERAL);

        // Parameters that are default values for ports
        defaultRemoteAddress =
                new StringAttribute(this, "defaultRemoteAddress");
        defaultRemoteAddress.setExpression("localhost");

        defaultRemoteSocketNumber =
                new Parameter(this, "defaultRemoteSocketNumber");
        defaultRemoteSocketNumber.setTypeEquals(BaseType.INT);
        defaultRemoteSocketNumber.setExpression("-1");

        // Pure parameters
        localSocketNumber = new Parameter(this, "localSocketNumber");
        localSocketNumber.setTypeEquals(BaseType.INT);
        localSocketNumber.setExpression("-1");

	/** When encodeForPtolemyParser is true, fire() applies
         *  <i>toString().getBytes()</i> to the <i>data</i> token
         *  prior to transmission.  This allows reconstruction of any
         *  data type upon reception.  When this parameter is false,
         *  the data must be an array of integers.  In this case, the
         *  data transmitted is the series of bytes generated by
         *  taking the least significant 8 bits from each integer.
         */
	encodeForPtolemyParser = new Parameter(this, "encodeForPtolemyParser");
	encodeForPtolemyParser.setTypeEquals(BaseType.BOOLEAN);
	encodeForPtolemyParser.setExpression("true");

	// Added for SDF usability.  No real output, just a trigger.
  	triggerOutput = new TypedIOPort(this, "triggerOutput");
	 // Had had ', true, false' before ');' above.
        triggerOutput.setTypeEquals(BaseType.GENERAL);
	 // 'INT' workes too in place of 'GENERAL'.
        triggerOutput.setOutput(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The triggerOutput port.  The type of this port is GENERAL,
     *  forcing input ports connected here to also be of type GENERAL,
     *  (as trigger inputs typically are).  This port always transmits
     *  a Token with nothing in it.  This is used to trigger the next
     *  actor in SDF.
     */
    public TypedIOPort triggerOutput;

    /** Whether to encode for parsing upon reception.
     *  Boolean.  Default value is true.
     */
    public Parameter encodeForPtolemyParser;

    /** The local socket number for this actor's socket.  Integer in
     *  the range 0..65535.  Default is -1 and must be changed before
     *  using the actor.  */
    public Parameter localSocketNumber;

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
    // FIXME Why is this not simply a 'Parameter' like localSocketNumber?
    public StringAttribute defaultRemoteAddress;

    /** The remote address towards which to launch the packet.
     */
    public TypedIOPort remoteAddress;

    /** The default remote (UDP (are there other kinds?) ) socket to
     *  which to send datagrams.  This is an integer in 0..65535.
     */
    public Parameter defaultRemoteSocketNumber;

    /** The remote (UDP (Can TCP reuse a UDP number?) ) socket number.
     */
    public TypedIOPort remoteSocketNumber;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** If the parameter changed is <i>localSocketNumber</i>, then if
     *  the model is running (as evedenced by socket != null) then
     *  close socket and reopen with new socket number (even if same
     *  as old socket number).
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If cannot create socket.  */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == localSocketNumber) {
            synchronized(this) {
                if (_socket != null) {
                    if (_debugging) _debug("Current socket port is "
                                       + _socket.getLocalPort());

                    _localSocketNumber =
                        ((IntToken)(localSocketNumber.getToken())).intValue();
                    if (_debugging) _debug("Socket number is "
		            + _localSocketNumber);
                    try {
                        if (_debugging) _debug("Try create socket for port "
                                + _localSocketNumber);
                        DatagramSocket newSocket =
                                new DatagramSocket(_localSocketNumber);
                        if (_debugging) _debug("A socket is created!!");
                        _socket.close();
                        _socket = newSocket;
                    }
                    catch (SocketException ex) {
                        throw new IllegalActionException(this,
                                "Cannot create socket on the given "
                                + "local socket number: " + ex.getMessage());
                    }
                }
	    }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Does up to three things, in this order: Set new remote address value,
     *  Set new remote socket number, transmit data as a UDP packet over the
     *  ethernet.  The first two can, of course, affect where the datagram
     *  goes.
     */
    public void fire() throws IllegalActionException {

	//System.out.println(this + " fire() method beginsXXX");

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

        if (remoteSocketNumber.getWidth() > 0 &&
                remoteSocketNumber.hasToken(0)) {
            // Valid socket numbers are 0..65535 so keep only lower 16 bits.
            _remoteSocketNumber = 65535 &
                    ((IntToken)remoteSocketNumber.get(0)).intValue();
        }

        if (data.hasToken(0)) {
	    boolean forParser = ((BooleanToken)(
                    encodeForPtolemyParser.getToken())).booleanValue();
	    byte[] dataBytes = { (byte)1, (byte)2, (byte)3 };
	    if (forParser) {
		dataBytes = data.get(0).toString().getBytes();
	    } else {
		//An exception, thrown as this line calls *Token, is
		//likely because the actor's input is not an integer array.
		//Please configure the <i>data</i> port as {int}.
		ArrayToken dataIntArrayToken = (ArrayToken) data.get(0);
		dataBytes = new byte[dataIntArrayToken.length()];
                for (int j = 0; j < dataIntArrayToken.length(); j++) {
                    IntToken dataIntOneToken =
                            (IntToken)dataIntArrayToken.getElement(j);
                    dataBytes[j] = (byte)dataIntOneToken.intValue();
                }
 	    }
	    DatagramPacket packet = new
                    DatagramPacket(dataBytes, dataBytes.length,
                    _address, _remoteSocketNumber);
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
	    triggerOutput.broadcast(new Token());
	    //triggerOutput.broadcast(new IntToken(7)); // Works w/ 'INT'
        }

	//System.out.println(this + " fire() method endsXXX");

    }

    /** Preinitialize allocates the socket and makes use of default
     *  parameters for the remote address and socket to which datagrams
     *  will be sent.  InetAddress.getByName does the address lookup,
     *  and can fail (see below).  The remote socket number need only
     *  be in the 0 .. 65535 range.  However, the local socket number
     *  must be in range and must allow new DatagramSocket() to
     *  successfully create a socket at that number.
     *  @exception IllegalActionException If socket number is beyond 16 bits,
     *   the socket cannot be created with the given socket number,
     *   translation of remote address fails to make IP address from
     *   address string, or the remote socket number is beyond 16 bits.
     */
    public void preinitialize() throws IllegalActionException {

	//System.out.println("preinitialize() called in " + this);

        super.preinitialize();
        _localSocketNumber =
                ((IntToken)(localSocketNumber.getToken())).intValue();
        if (_localSocketNumber < 0 || _localSocketNumber > 65535) {
            throw new IllegalActionException(this, "Local socket number is "
                    + "invalid, must be between 0 and 65535.");
        }
        try {
            if (_debugging) _debug("PI Try create socket number "
                    + _localSocketNumber);
            _socket = new DatagramSocket(_localSocketNumber);
            if (_debugging) _debug("PI A socket is created!!");
        }
        catch (SocketException ex) {
            throw new IllegalActionException(this, "Cannot create socket on "
                    + "the specified local socket number: " + ex.getMessage());
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

        _remoteSocketNumber =
                ((IntToken)defaultRemoteSocketNumber.getToken()).intValue();
        if (_remoteSocketNumber < 0 || _remoteSocketNumber > 65535) {
            //System.out.println(this + " defaultRemoteSocketNumber is "
	    //        + _remoteSocketNumber + " .  Must be in 0..65535.");
	    _remoteSocketNumber &= 65535; // Truncate to 16 bits.
	    throw new IllegalActionException(this, "defaultRemoteSocketNumber"
                    + " is out of range, must be between 0 and 65535.");
	}
    }

    /** Wrap up.  Free the socket, allowing the socket number to be reused.
     *  @exception IllegalActionException If the socket was already null.
     */
    public void wrapup() throws IllegalActionException {
        synchronized(this) {
            if (_socket != null) {
                _socket.close();
                _socket = null;
            } else {
		//System.out.println("Socket was already null in " + this);
                throw new IllegalActionException("Socket was already null "
	                +  "at wrapup!?");
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









