/* An actor that asynchronously reads datagram packets.

 Copyright (c) 2001-2014 The Regents of the University of California.
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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
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
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// DatagramReader

/**
 This actor reads datagram packets via a separate thread.  The thread
 responds to datagrams whenever they arrive, giving the actor the ability
 to read the datagrams asynchronously.


 Datagrams are connectionless, open-loop internet communications.  Each
 datagram packet contains data plus a return address.  The return
 address consists of an IP address and a socket number.  Datagrams use
 the UDP protocol under which no reply or confirmation is expected.
 This is in contrast to TCP which expects confirmations and attempts to
 deliver packets in order to the layer above TCP.  This can result in
 long delays in the delivery of information across the network.  Because
 UDP makes no such attempts, it never hangs and does not need to be timed out.

 <p>NOTE: This actor has been developed to work in the Discrete Event
 (DE) and Synchronous Data Flow (SDF) domains.  Use elsewhere with
 caution.

 <p>NOTE: This actor has problems, the tests do not reliably pass.
 For details, see <a href="https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=54"><code>https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=54</code></a>.

 <p> <p>The simplest scenario has the thread constantly stalled
 awaiting a packet.  When a packet arrives, the thread quickly queues
 it in one of the buffers of the actor, calls the
 getDirector().fireAtCurrentTime(), and then stalls again awaiting the
 next packet.  By stalling again and again, the thread keeps the actor
 aware at all times of incoming packets.  This is particularly
 important if packets come in more quickly than the model can process
 them.  Depending on the domain (e.g. DE) in which this actor is used,
 the director may respond to the fireAtCurrentTime() call of the thread by
 calling the fire() method of the actor.  In this case, fire() then
 broadcasts the data received, along with the return address and return
 socket number from which the datagram originated.

 <p>The data portion of the packet is broadcast at the <i>output</i> port.
 The type of the output is always an array of bytes.

 <p>The return address and socket number are broadcast as String and int
 respectively.  These tell where the received datagram originated from.

 <p>The behavior of the actor under less simple scenarios is governed by
 parameters of this actor.  Additional packet(s) can arrive while the
 director is getting around to calling fire().  Conversely, the
 director may make extra calls to fire(), even before any datagrams
 have come in.  I call these the eager packet and eager director
 scenarios respectively.

 <p>Background: There are two packet buffers.  The thread and the fire()
 method share these buffers and maintain consistency via synchronization
 on the object <i>_syncFireAndThread</i>.  This synchronization prevents
 conflicts when accessing the shared buffers and when accessing the
 count of queued packets.

 <p>The <i>overwrite</i> parameter applies to the eager packet
 scenario.  Setting this parameter to true is useful in cases where it
 is possible for data to come in too fast for the model to process.
 This setting alleviates data gluts without undue loss of data when the
 model is able to keep up.  When <i>overwrite</i> is set to true (the
 default), the actor discards the packet already received in
 favor of the new packet.  If false, the new packet is queued behind the
 existing one.  In the latter case, both buffers are now full.  The
 thread then waits for fire() to consume a queued packet before it
 stalls again awaiting the next.  In all other cases (<i>overwrite</i>
 true or no queued packets) the thread immediately stalls to await the
 next packet.

 <p>The <i>blockAwaitingDatagram</i> parameter applies to the eager
 director case.  This case comes up most often in SDF, where an actor
 is expected to block in fire until an output can be produced.  If
 true, a call to fire() will block unless or until a datagram has
 arrived.  If false, then fire() returns without waiting, using the
 <i>defaultOutput</i> parameter in place of real data.  The
 <i>returnAddress</i> and <i>returnSocketNumber</i> ports have default
 outputs as well, but they are not parameter-programmable.

 <p>
 <p>NOTE: This actor has a parameter <i>localSocketNumber</i> for the port
 number assigned to its local datagram socket.  Initially, the local
 socket number is set to 4004.  There is no particular reason for choosing
 this number, except that is noticeable in the code and in Vergil, thus
 encouraging you to change it to any desired value in the range 0..65535.
 Note that socket numbers 0..1023 are generally reserved and numbers 1024 and
 above are generally available.

 <p>Some commonly used port numbers (a.k.a. socket numbers) are shown below:
 <pre>
 Well-known Ports
 (Commonly Used Ports)
 7        (Echo)
 21        (FTP)
 23        (TELNET)
 25        (SMTP)
 53        (DNS)
 79        (finger)
 80        (HTTP)
 110        (POP3)
 119        (NNTP)
 161        (SNMP)
 162        (SNMP Trap)
 </pre>
 Reference:  http://192.168.1.1/Forward.htm
 (A webpage hosted from within the Linksys BEFSR41 Cable/DSL Router)

 <p>NOTE: This actor can also be configured to handle multicase datagram
 socket. A MulticastSocket is a DatagramSocket with additional capabilities
 to join groups of other multicast hosts on the internet. A multicast group
 is specified by a class D IP address and a standard UDP port number.
 When one member sends a packet to a multicast group, all recipients
 subscribing to that host and port receive the packet.
 Currently, The parameter <i>defaultReturnAddress</i> is overloaded to specify
 a multicast datagram IP address. When the return address is a multicast IP
 address, The parameter <i>localSocketNumber</i> is used to specify the
 UDP port number for the multicast group. A multicast IP address
 ranges from 224.0.0.0 to 239.255.255.255, inclusive. To send a packet to the
 group, the sender can be either a DatagramSocket or a MulticastSocket. The
 only difference is that MulticastSocket allows you to control the time-to-live
 of the datagram. Don't use 224.0.0.1 ~ 224.255.255.255 when the live time of
 is specified larger than 1.

 <p>FIXME: we might not want to overload the <i>defaultReturnAddress</i> and
 the <i>localSocketNumber</i> parameter...

 <p>Another useful tidbit is the command 'netstat'.  This works in a
 DOS prompt and also in the UNIX-like Bash shell.  In either shell,
 enter 'netstat -an'.  This command shows current port allocations!  Ports
 allocated to Ptolemy models are shown along with other port allocations.
 Other useful network commands include 'ping' and 'tracert'.
 Both TCP and UDP (datagram) ports are shown by netstat.
 FIXME: Find out whether a TCP port using a specific number blocks a
 UDP port from using that same number.

 @author Winthrop Williams, Joern Janneck, Xiaojun Liu, Edward Lee
 (Based on TiltSensor actor written
 by Chamberlain Fong, Xiaojun Liu, Edward Lee)
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (winthrop)
 @Pt.AcceptedRating Yellow (winthrop)
 */
public class DatagramReader extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DatagramReader(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // ports - Ordering here sets the order they show up in Vergil
        returnAddress = new TypedIOPort(this, "returnAddress");
        returnAddress.setTypeEquals(BaseType.STRING);
        returnAddress.setOutput(true);

        returnSocketNumber = new TypedIOPort(this, "returnSocketNumber");
        returnSocketNumber.setTypeEquals(BaseType.INT);
        returnSocketNumber.setOutput(true);

        output = new TypedIOPort(this, "output");
        output.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));
        output.setOutput(true);

        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setTypeEquals(BaseType.GENERAL);
        trigger.setMultiport(true);

        // parameters - Ordering here sets the order they show up in Vergil
        localSocketNumber = new Parameter(this, "localSocketNumber");
        localSocketNumber.setTypeEquals(BaseType.INT);
        localSocketNumber.setToken(new IntToken(4004));

        actorBufferLength = new Parameter(this, "actorBufferLength");
        actorBufferLength.setTypeEquals(BaseType.INT);
        actorBufferLength.setToken(new IntToken(440));

        platformBufferLength = new Parameter(this, "platformBufferLength");
        platformBufferLength.setTypeEquals(BaseType.INT);
        platformBufferLength.setToken(new IntToken(64));

        setPlatformBufferLength = new Parameter(this,
                "setPlatformBufferLength", new BooleanToken(false));
        setPlatformBufferLength.setTypeEquals(BaseType.BOOLEAN);

        overwrite = new Parameter(this, "overwrite", new BooleanToken(true));
        overwrite.setTypeEquals(BaseType.BOOLEAN);

        blockAwaitingDatagram = new Parameter(this, "blockAwaitingDatagram");
        blockAwaitingDatagram.setTypeEquals(BaseType.BOOLEAN);
        blockAwaitingDatagram.setExpression("true");

        defaultReturnAddress = new Parameter(this, "defaultReturnAddress");
        defaultReturnAddress.setTypeEquals(BaseType.STRING);
        defaultReturnAddress.setToken(new StringToken("localhost"));

        defaultReturnSocketNumber = new Parameter(this,
                "defaultReturnSocketNumber");
        defaultReturnSocketNumber.setTypeEquals(BaseType.INT);
        defaultReturnSocketNumber.setExpression("0");

        defaultOutput = new Parameter(this, "defaultOutput");
        defaultOutput.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));
        defaultOutput.setExpression("{0ub}");

        // Repeat has not been implemented.  However, I'd place it
        // here so that it would show up in Vergil below
        // <i>defaultOutput</i>.  It works in tandem with the above
        // two parameters in that, if blocking is true, that takes
        // precedence.  Otherwise, if no data is available for
        // repeating, the default takes precedence.  Repeat, then, is
        // third in line.  It would act (were it implemented) only
        // when blocking is false and repeatable data is available.
        //
        // One could, incidentally, construct an argument that repeat
        // is second and the default is a last resort.  However, one
        // could not construct an actor with repeat and without a
        // default.  Such an actor would have nothing to resort to if
        // repeatable data were unavailable.
        //
        //repeat = new Parameter(this, "repeat", new BooleanToken(false));
        //repeat.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    // Ports and parameters are in the same order hare as in the constructor.

    /** This port outputs the IP address portion of the received
     *  datagram packet.  The type of this output is String.  This is
     *  the IP address of the remote datagram socket which sent the
     *  packet to the socket of this actor.  Under IPv4, this string has
     *  the familiar form "128.32.1.1".  This output defaults (when no
     *  datagram has been received and blocking is false) to the IP
     *  address of the socket.
     */
    public TypedIOPort returnAddress;

    /** This port outputs the socket (a.k.a port) number portion of the
     *  received datagram packet.  The type of this output is int.
     *  This is the socket number of the remote datagram socket which
     *  sent the packet to this actor's socket.  This is an integer in
     *  the range 0 through 65535.  This output defaults (when no
     *  datagram has been received and blocking is false) to this
     *  actor's local socket number.
     */
    public TypedIOPort returnSocketNumber;

    /** This port outputs the data portion of the received datagram
     *  packet.  The type of <i>output</i> may depend on the datagram
     *  received, which may vary even during a single run of a model.
     *  <b>The user is encouraged to play with the configuration of
     *  this port</b> to best suit the need at hand.
     */
    public TypedIOPort output;

    /** The trigger input port reads and discards a token from each
     *  channel that has a token.  The type of this port has been set
     *  to GENERAL, permitting any token type to be accepted.  The
     *  hasToken() and get(int) methods are called on this input, but
     *  their contents are discarded.  The presence of a connection to
     *  this input serves a purpose by causing the director to
     *  schedule the firing of this actor at an appropriate place in
     *  the sequence of firings of actors.  This is particularly
     *  useful in the SDF domain.  Without a trigger input, the SDF
     *  scheduler would be unable to schedule a firing of this actor
     *  unless it can be scheduled as the first actor to be fired.
     *  Thus, without this input, configurations in SDF would be
     *  limited.  (@See ptolemy.actor.lib.Source for an archetypal
     *  trigger input.)  */
    public TypedIOPort trigger = null;

    /** This actor's local socket (a.k.a. port) number.  <b>This is a
     *  system resource allocated to this actor.</b> No other actor
     *  with the same local socket number may run at the same time.
     *  Currently, When the return address is a multicast IP address,
     *  this parameter is also used to specify the UDP port number
     *  for the multicast group.
     */
    public Parameter localSocketNumber;

    /** Length (in bytes) of each of the actor's two packet buffers for
     *  receiving a datagram.  This length does not include the bytes
     *  needed for storing the datagram's return address and other
     *  housekeeping information.  This buffer need only be big enough
     *  to hold the payload (a.k.a. data portion) of the datagram.
     */
    public Parameter actorBufferLength;

    /** Length (in bytes) of the buffer within java and/or the
     *  platform layers below java.  Java documents refers to all this
     *  collectively as the platform.  The size of this buffer is
     *  controlled via the getReceiveBufferSize() and
     *  setReceiveBufferSize() methods.  @see java.net.DatagramSocket.
     *  Caution #1 - The platform treats setReceiveBufferSize() as a
     *  suggestion only.  It supposedly reports the actual buffer size
     *  granted in subsequent calls to getReceiveBufferSize().
     *  However, my experiments with this showed it granting buffers
     *  as large as 2 gigabytes, with no apparent limit except the
     *  maximum representable integer value.  Thus, I suggest taking
     *  this with a grain of salt.  Caution #2 - the
     *  get/setReceiveBufferSize() calls block when called as long as
     *  another thread is in a receive() call on that same socket.
     *  This is undocumented in Java's documentation.  Also note that
     *  the setReceiveBufferSize() method is not available in early
     *  JDK's, which makes it important to have
     *  setPlatformBufferLength set to false when generating code.
     */
    public Parameter platformBufferLength;

    /** Determine whether the platformBufferLength parameter will be
     *  used to set the platform's receive buffer size.  This
     *  parameter must contain a boolean token, and has a default of
     *  false.
     */
    public Parameter setPlatformBufferLength;

    /** Whether to overwrite when inundated with datagrams or let
     *  them pile up.  Default is true.  If false, datagrams will
     *  queue up (mostly in the platform, some in the actor).  The
     *  datagram used at each invocation of fire will be the oldest in
     *  the queue.  On the other hand, if <i>overwrite</i> is true,
     *  then minimal queuing will occur and the most recent data will
     *  be used when fire() is called.  Older data will be discarded.  */
    public Parameter overwrite;

    /** Whether to block in fire().  If fire() is called before the
     *  datagram has arrived, the actor must either block awaiting the
     *  datagram or use its <i>defaultOutput</i>.  This blocking
     *  parameter controls which choice fire() will make.  This
     *  parameter is useful for SDF models, where it is generally set
     *  to true.  It has no effect in DE models unless the trigger
     *  input has been connected.  Type is Boolean.  Default value is
     *  true.
     */
    public Parameter blockAwaitingDatagram;

    /** The default for the <i>returnAddress</i> output.  This token is
     *  broadcast when the actor is fired, but no actual datagram
     *  is available to broadcast and <i>blockAwaitingDatagram</i> is
     *  false.  If blocking were true, the actor would simply stall in
     *  fire() until a datagram arrives.  Type is string.  Default value
     *  is "localhost".
     *  Currently, this parameter can be overloaded to specify a multicast
     *  datagram IP address. A multicast IP address ranges from
     *  224.0.0.0 to 239.255.255.255, inclusive.
     */
    public Parameter defaultReturnAddress;

    /** The default the <i>returnSocketNumber</i> output.  This token is
     *  broadcast when the actor is fired, but no actual datagram
     *  is available to broadcast and <i>blockAwaitingDatagram</i> is
     *  false.  If blocking were true, the actor would simply stall in
     *  fire() until a datagram arrives.  Type is integer.  Default
     *  value is 0.
     *  */
    public Parameter defaultReturnSocketNumber;

    /** The default for the <i>output</i> output.  This default token
     *  is broadcast when the actor is fired, but no actual datagram
     *  data is available to broadcast and
     *  <i>blockAwaitingDatagram</i> is false.  If blocking were true,
     *  the actor would simply stall in fire() until a datagram
     *  arrives.  Type is defined by the expression entered.  Default
     *  type & value is the integer 0.
     */
    public Parameter defaultOutput;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change of the given attribute.  Generally, this is
     *  method called between firings of an actor.  However, this
     *  actor contains a separate thread of which the director is not
     *  aware.  Thus, any time the model is running, calls to this
     *  method typically happen while the thread is running.
     *  Furthermore, the thread spends most of its time blocked in the
     *  DatagramSocket.receive() method or (if <i>overwrite</i> is
     *  false) waiting for fire() to notify it that space is available
     *  to receive another packet.  This method has been architected
     *  to, when possible, permit prompt changes (such as to the
     *  socket number being monitored for incoming datagrams), while
     *  at the same time maintaining consistency.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not
     *   allowed.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == defaultReturnAddress) {
            synchronized (_syncDefaultOutputs) {
                _defaultReturnAddress = ((StringToken) defaultReturnAddress
                        .getToken()).stringValue();

                //check whether is ip multicase datagram.
                //multicast IP ranges from 224.0.0.1 to 239.255.255.255(inclusive).
                //Note: don't use 224.0.0.1 ~ 224.255.255.255 when the live time
                //of the socket is specified larger than 1.
                if (_defaultReturnAddress.compareTo("224.0.0.1") >= 0
                        && _defaultReturnAddress.compareTo("239.255.255.255") <= 0) {
                    _multiCast = true;

                    try {
                        _address = InetAddress.getByName(_defaultReturnAddress);
                    } catch (UnknownHostException ex) {
                        throw new IllegalActionException(this, ex,
                                "The default remote "
                                        + "address specifies an unknown host");
                    }

                    if (_multicastSocket != null) {
                        try {
                            _multicastSocket.joinGroup(_address);
                        } catch (IOException exp) {
                            throw new IllegalActionException(
                                    "can't join the multicast group" + exp);
                        }
                    }
                } else {
                    //FIXME: what should it do when change from multicast to unicast...
                    _multiCast = false;

                    if (_multicastSocket != null) {
                        try {
                            _multicastSocket.leaveGroup(_address);
                        } catch (IOException exp) {
                            throw new IllegalActionException(this, exp,
                                    "Disconnecting from the multicast group "
                                            + "failed.");
                        }
                    }
                }
            }
        } else if (attribute == defaultReturnSocketNumber) {
            synchronized (_syncDefaultOutputs) {
                _defaultReturnSocketNumber = ((IntToken) defaultReturnSocketNumber
                        .getToken()).intValue();
            }
        } else if (attribute == defaultOutput) {
            synchronized (_syncDefaultOutputs) {
                _defaultOutputToken = output.getType().convert(
                        defaultOutput.getToken());
            }

            // In the case of <i>blockAwaitingDatagram</i> or <i>overwrite</i>,
            // notify potentially waiting fire() or thread respectively
            // that it might no longer need to wait.  Each will recheck its
            // conditions when notified, so extra notifications merely
            // waste a little CPU.
        } else if (attribute == overwrite) {
            _overwrite = ((BooleanToken) overwrite.getToken()).booleanValue();

            if (_overwrite) {
                synchronized (_syncFireAndThread) {
                    _syncFireAndThread.notifyAll();
                }
            }
        } else if (attribute == blockAwaitingDatagram) {
            _blockAwaitingDatagram = ((BooleanToken) blockAwaitingDatagram
                    .getToken()).booleanValue();

            if (!_blockAwaitingDatagram) {
                synchronized (_syncFireAndThread) {
                    _syncFireAndThread.notifyAll();
                }
            }

            // In the case of <i>localSocketNumber</i> it is necessary to
            // disturb the thread if it is in the call to
            // socket.receive().  This is rather disruptive, so I choose
            // to act on these attribute change requests only if the value
            // of this parameter has actually changed.  Furthermore, when
            // receive() is busy, wait 444mS (an arbitrary choice) to
            // see if receive will return on its own.  Close and
            // re-create the socket when either the time expires or
            // the receive() call returns, whichever comes first.
            //
            // Point of interest:  The behavior of a datagram socket
            // upon close() by one thread while another thread is
            // stalled in receive() on the same socket is given in
            // page 3 of  file:///D%7C/Depot/j2sdk-1_3_1-doc/
            // docs/guide/misc/threadPrimitiveDeprecation.html
            // "What if a thread doesn't respond to Thread.interrupt?"
        } else if (attribute == localSocketNumber) {
            // FIXME-- Try again w/o sync(this):
            // Unlike other attributes, concurrent calls here
            // (each with attribute == localSocketNumber have
            // caused an exception to be thrown.  (Or, was it that
            // socket so recently closed had not fully released
            // that socket number!)
            // Note: This synchronized block breaks codegen.
            synchronized (this) {
                if (_multiCast == true) {
                }

                if (_socket != null || _multicastSocket != null) {
                    // Verify presence & health of the thread.
                    if (_socketReadingThread == null) {
                        throw new IllegalActionException(this, "thread == null");
                    } else if (!_socketReadingThread.isAlive()) {
                        throw new IllegalActionException(this,
                                "thread is not Alive");
                    }

                    int newSocketNumber = ((IntToken) localSocketNumber
                            .getToken()).intValue();

                    if (_multicastSocket != null
                            && newSocketNumber != _multicastSocket
                                    .getLocalPort()) {
                        synchronized (_syncSocket) {
                            if (_inReceive) {
                                // Wait for receive to finish, if it
                                // does not take very long that is.
                                try {
                                    _syncSocket.wait(444);
                                } catch (InterruptedException ex) {
                                    throw new IllegalActionException(this, ex,
                                            "Interrupted while waiting");
                                }

                                // Either I've been notified that receive()
                                // has completed, or the timeout has occurred.
                                // It does not matter which.  Either way I am
                                // now ready to close and re-open the socket.
                            }

                            _multicastSocket.close();

                            try {
                                _multicastSocket = new MulticastSocket(
                                        newSocketNumber);
                            } catch (Exception ex) {
                                throw new InternalErrorException(this, ex,
                                        "Couldn't open new socket number "
                                                + newSocketNumber);
                            }

                            if (_address != null) {
                                try {
                                    _multicastSocket.joinGroup(_address);
                                } catch (IOException exp) {
                                    throw new IllegalActionException(
                                            "can't join the multicast group"
                                                    + exp);
                                }
                            }
                        }
                    } else if (_socket != null
                            && newSocketNumber != _socket.getLocalPort()) {
                        synchronized (_syncSocket) {
                            if (_inReceive) {
                                // Wait for receive to finish, if it
                                // does not take very long that is.
                                try {
                                    _syncSocket.wait(444);
                                } catch (InterruptedException ex) {
                                    throw new IllegalActionException(this, ex,
                                            "Interrupted while waiting");
                                }

                                // Either I've been notified that receive()
                                // has completed, or the timeout has occurred.
                                // It does not matter which.  Either way I am
                                // now ready to close and re-open the socket.
                            }

                            _socket.close();

                            try {
                                _socket = new DatagramSocket(newSocketNumber);
                            } catch (SocketException ex) {
                                throw new InternalErrorException(this, ex,
                                        "Couldn't open new socket number "
                                                + newSocketNumber);
                            }
                        }
                    }
                }
            }

            // In the case of <i>actorBufferLength</i>, simply cache
            // the parameter.  The thread used this value to set the
            // size of a buffer prior to the socket.receive() call.
            // The thread only resizes a buffer when it is about to
            // call receive on it and this parameter has changed from
            // the value last used for that specific buffer.
            // Synchronization ensures that the thread's test for a
            // change in this value and its use of the value access
            // the same thing.
        } else if (attribute == actorBufferLength) {
            synchronized (_syncBufferLength) {
                _actorBufferLength = ((IntToken) actorBufferLength.getToken())
                        .intValue();
            }

            // Just increment a flag here, so that before the next
            // receive() call the new buffer size will be set.
            // Setting buffer here did not work because the calls to
            // set the size and get the existing size both block if
            // the socket is being received on.  This flag is set to 1
            // in initialize().
        } else if (attribute == platformBufferLength && _socket != null) {
            synchronized (_syncBufferLength) {
                _ChangeRequestedToPlatformBufferLength++;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        DatagramReader newObject = (DatagramReader) super.clone(workspace);

        // Needed for actor oriented classes
        newObject._syncFireAndThread = new Object();
        newObject._syncDefaultOutputs = new Object();
        newObject._syncBufferLength = new Object();
        newObject._syncSocket = new Object();

        return newObject;
    }

    /** Broadcast a received datagram, or block awaiting one, or
     *  broadcast default values.  Broadcast a return address and a
     *  return socket number along with the net data payload contents
     *  of the datagram.  Conversion of the payload data into a token
     *  is handled in a variety of ways, according to the setting of
     *  the <i>encoding</i> parameter.  The return address and return
     *  socket number are always broadcast as a string and an integer
     *  respectively.
     *  @exception IllegalActionException If the data cannot be
     *  converted into a token of the same type as the configured type
     *  of the output port.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        // Consume trigger input(s), otherwise model can hang.
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                trigger.get(i);
            }
        }

        boolean useDefaultOutput;

        // The part dependent on the packet's contents must be synchronized
        // to ensure that the thread does mess with it while it is in use
        // here.
        synchronized (_syncFireAndThread) {
            int bytesAvailable = 0;
            byte[] dataBytes = new byte[0];

            // If requested, block awaiting a packet (useful in SDF).
            // Actually <u>while</u> requested, not if, in case the
            // attribute is changed while waiting.  Has no effect in
            // DE, unless the DE model uses this actor's
            // <i>trigger</i> input, since fire() is only called by
            // the director in response to this actor's thread calling
            // the director's fireAtCurrentTime() method.  In the
            // latter case, there is always a packet waiting because
            // otherwise fireAtCurrentTime() would not have been
            // called.
            while (_blockAwaitingDatagram && _packetsAlreadyAwaitingFire == 0) {
                try {
                    _fireIsWaiting = true;
                    _syncFireAndThread.wait();
                    _fireIsWaiting = false;
                } catch (InterruptedException ex) {
                    throw new InternalErrorException(this, ex,
                            "!!fire()'s wait interrupted!!");

                    // This finally block breaks Jode.
                } finally {
                    if (_stopFire) {
                        _stopFire = false;

                        if (_debugging) {
                            _debug("return due to stopFire");
                        }

                        return;
                    }
                }
            }

            if (_packetsAlreadyAwaitingFire != 0) {
                bytesAvailable = _broadcastPacket.getLength();

                if (bytesAvailable > 0) {
                    useDefaultOutput = false;
                } else {
                    useDefaultOutput = true;
                }

                dataBytes = _broadcastPacket.getData(); //The buffer, not copy.
                _returnAddress = _broadcastPacket.getAddress().getHostAddress();
                _returnSocketNumber = _broadcastPacket.getPort();
                _packetsAlreadyAwaitingFire--;
            } else {
                useDefaultOutput = true;
            }

            if (!useDefaultOutput) {
                Token[] dataTokens = new Token[bytesAvailable];

                for (int j = 0; j < bytesAvailable; j++) {
                    dataTokens[j] = new UnsignedByteToken(dataBytes[j]);
                }

                _outputToken = new ArrayToken(BaseType.UNSIGNED_BYTE,
                        dataTokens);
            }

            _syncFireAndThread.notifyAll();
        } // sync

        if (!useDefaultOutput) {
            returnAddress.broadcast(new StringToken(_returnAddress));
            returnSocketNumber.broadcast(new IntToken(_returnSocketNumber));
            output.broadcast(_outputToken);
        } else {
            // No fresh data, so use the default output.
            // (If repeat parameter were implemented,
            //  would test it here and, if true, would
            //  repeat the most recent output instead
            //  of outputting the default, provided
            //  there is a previous output to repeat.)
            // _defaultOutputToken == null when user has entered blank
            // parameter.  Take this as a directive to not broadcast a
            // token in this case.
            if (_defaultOutputToken == null) {
                if (_debugging) {
                    _debug("DO NOT Broadcast ANY output (blank default)");
                }
            } else {
                if (_debugging) {
                    _debug("Broadcast default outputs");
                }

                synchronized (_syncDefaultOutputs) {
                    // Ensure that any change to the default output parameters
                    // occurs atomically with respect to its use here.
                    returnAddress.broadcast(new StringToken(
                            _defaultReturnAddress));
                    returnSocketNumber.broadcast(new IntToken(
                            _defaultReturnSocketNumber));
                    output.broadcast(_defaultOutputToken);
                }
            }
        }
    }

    /** Initialize this actor, including the creation of an evaluation
     *  variable for the Ptolemy parser, a DatagramSocket for
     *  receiving datagrams, and a SocketReadingThread for blocking in
     *  the DatagramSocket.receive() method call.  This method is used
     *  as a bookend with wrapup() being the other end.  Resources
     *  created/allocated here are released in wrapup().
     *  @exception IllegalActionException If the
     *  <i>localSocketNumber</i> parameter has a value outside 0..65535
     * or a socket could not be created.  */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // Reset private variables
        _packetsAlreadyAwaitingFire = 0;
        _defaultReturnAddress = ((StringToken) defaultReturnAddress.getToken())
                .stringValue();

        //check whether is ip multicase datagram.
        if (_defaultReturnAddress.compareTo("224.0.0.1") >= 0
                && _defaultReturnAddress.compareTo("239.255.255.255") <= 0) {
            _multiCast = true;
        } else {
            _multiCast = false;
        }

        int portNumber = ((IntToken) localSocketNumber.getToken()).intValue();

        if (portNumber < 0 || portNumber > 65535) {
            throw new IllegalActionException(this, localSocketNumber
                    + " is outside the required 0..65535 range");
        }

        if (_debugging) {
            _debug(this + "portNumber = " + portNumber);
        }

        if (_multiCast == true) {
            // Allocate a new multicast socket.
            try {
                if (_debugging) {
                    _debug("Trying to create a new multicast socket on port "
                            + portNumber);
                }

                _multicastSocket = new MulticastSocket(portNumber);

                if (_debugging) {
                    _debug("Multicast Socket created successfully!");
                }
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex,
                        "Failed to create a new multicast socket on port "
                                + portNumber);
            }

            String address = ((StringToken) defaultReturnAddress.getToken())
                    .stringValue();

            try {
                _address = InetAddress.getByName(address);
            } catch (UnknownHostException ex) {
                throw new IllegalActionException(this, ex,
                        "The default remote "
                                + "address specifies an unknown host");
            }

            try {
                _multicastSocket.joinGroup(_address);
            } catch (IOException exp) {
                throw new IllegalActionException(
                        "can't join the multicast group" + exp);
            }
        } else {
            // Allocate a new socket.
            try {
                if (_debugging) {
                    _debug("Trying to create a new socket on port "
                            + portNumber);
                }

                _socket = new DatagramSocket(portNumber);

                if (_debugging) {
                    _debug("Socket created successfully!");
                }
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex,
                        "Failed to create a new socket on port " + portNumber);
            }
        }

        // Set flag so that thread will [Set and] get platform's buffer length.
        _ChangeRequestedToPlatformBufferLength = 1;

        // Allocate & start a thread to read from the socket.
        _socketReadingThread = new SocketReadingThread();
        _socketReadingThread.start();
    }

    /** Override the setContainer() method to call wrapup() if the
     *  actor is deleted while the model is running.  Wrapup() then
     *  releases resources acquired by initialize(), to wit, the
     *  evaluation variable, the DatagramSocket, and the
     *  SocketReadingThread.
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        // FIXME: Is this necessary?
        if (container != getContainer()) {
            wrapup();
        }

        super.setContainer(container);
    }

    /** Stop the fire() method, but only if it is blocked.  The actor
     *  returns from fire(), with its state the same as before fire()
     *  was called.  Thus, when the manager is ready, it may call
     *  fire() again and the actor will start again in a consistent
     *  state.  The fire() method uses a wait() call to block.  If
     *  fire() not currently blocked, it is permitted to continue.
     *  This could be before or after the wait() call.  If it has
     *  passed the wait() statement, it will complete.  However, if it
     *  has not yet reached the wait() statement, it will block
     *  anyway.  Thus, when pausing or stopping execution, it will on
     *  rare occasion be necessary to press 'pause' or 'stop' a second
     *  time.
     *
     *  I tried to clean this up, so that one, and exactly one,
     *  stopFire() call would suffice.  These experiments and some
     *  thought about them are discussed below.  However, the bottom
     *  line is that any approach which stopped fire() in each case
     *  where it ought to be stopped, also stopped it in some
     *  additional cases where it ought not be stopped.
     *
     *  This is a very interesting predicament!  One could say the
     *  problem in an incompatibility of system-level types.  The type
     *  provided to the actor collectively by the Manager,
     *  CompositeActor, and Director is a call-return type of
     *  interface.  That is, stopFire() is called, and it is expected
     *  to return.  However, what may be needed is an interrupt type
     *  of interface.  One difference is that an interrupt comes with
     *  a flag that can be tested via the interrupted() and
     *  isInterrupted() methods.  Such a flag would solve the
     *  immediate problem with stopFire().  However, I wonder how we
     *  could be habitually applying the theory of system-level types
     *  and/or interface automata theory here and elsewhere?  Could we
     *  be building clearer designs from the get go?  Could the
     *  designs self modify or type-check themselves to maintain
     *  consistency as changes are made?  I am intrigued by the
     *  possibilities!
     *
     *  I did a bit of nosing around and discovered how stopFire()
     *  gets called.  The Manager initiates the call on the
     *  CompositeActor.  It then calls stopFire() on the Director.
     *  The Director fans out the call to every actor below it.
     *
     *  The director/manager insists on calling fire() after every
     *  call of prefire().  Even when it has issued a stopFire()
     *  during prefire(), it persists, executing the very fire() it is
     *  trying to stop!  This behavior makes it impossible for the
     *  actor to cover all the cases where it ought to stop.  See
     *  detailed discussion below.  Fortunately, multiple presses of
     *  the 'pause' and 'stop' buttons result in multiple stopFire()
     *  calls.  By the time a user can click the mouse a second time,
     *  fire() will have blocked if it is going to do so.  It can then
     *  be stopped as expected.
     *
     *  FIXME: There exists a circumstance where stopFire() could fail
     *  to stop the fire() method, and the fire() method could block
     *  indefinitely.  This occurs if stopFire() is called after
     *  fire() is called (or after the director commits itself to
     *  calling fire()) but before fire() enters the synchronized
     *  section in which it blocks with a wait() call.  In theory,
     *  fire() should avoid this by testing a flag before waiting.
     *  Such a flag could be set by stopFire().  However, there is no
     *  way for the actor to tell whether the flag was set during (or
     *  just before) this firing (in which case it ought to be
     *  obeyed), or if it was set during the completion of the last
     *  firing or when the actor was not being fired at all (in which
     *  case it ought to be ignored).  What is needed is a flag which
     *  is set when stopFire() is called, and cleared before the
     *  director commits to calling fire().
     *
     *  Perhaps prefire() could serve to clear the flag!  This assumes
     *  that, upon resuming execution, prefire() is repeated before
     *  fire() is reentered.-[Tested; assumption holds.]  It also
     *  assumes that superfluous calls to stopFire() do not occur
     *  during prefire() or between prefire() and fire().-[Tested;
     *  assumption holds with only known superfluous call, the one
     *  when the user presses 'Go'] Additionally, this assumes that
     *  intentional calls to stopFire(), if they occur before or
     *  during the [pre]initialize call, are backed up by a test which
     *  prevents the director from calling fire() if it has already
     *  called stopFire() with the intention of stopping that same
     *  fire().-[Tested; assumption does not hold.  I caused
     *  stopFire() to be called during prefire().  The director
     *  went ahead and called fire() anyway!]
     *
     *  If the director/manager need to be fixed anyway, perhaps the
     *  stopFire paradigm ought to be rearchitected to incorporate a
     *  flag.  (Does it already and I just don't know about it?)
     */
    @Override
    public void stopFire() {
        super.stopFire();
        synchronized (_syncFireAndThread) {
            if (_fireIsWaiting) {
                // stopFire() gets called a lot.  Including each time
                // the program is started.  This caveat has proven
                // necessary to avoid disrupting one of the first
                // few firings.
                _stopFire = true;
                _syncFireAndThread.notifyAll();
            }
        }
    }

    /** Request that execution of the current iteration stop as soon
     *  as possible. Wake up the manager thread if it is blocking on
     *  fire() of this actor.
     */
    @Override
    public void stop() {
        super.stop();

        synchronized (_syncFireAndThread) {
            if (_fireIsWaiting) {
                _syncFireAndThread.notifyAll();
            }
        }
    }

    /** Release resources acquired in the initialize() method,
     *  specifically the evaluation variable, the DatagramSocket, and
     *  the SocketReadingThread.  This method also gets called from
     *  this actor's setContainer() method.  This insures that when
     *  the actor is removed from a running simulation, locked
     *  resources are released.  Since the thread blocks in receive()
     *  on the DatagramSocket, and since this method does not respond
     *  to an interrupt() call on its thread, the close() method is
     *  used on the DatagramSocket to break the thread out of the
     *  receive() call.  Because attributeChanged() also (temporarily)
     *  closes the DatagramSocket, wrapup() additionally makes the
     *  DatagramSocket null.  This signals the thread to terminate.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        // FIXME - Look into whether I ought to make it null first
        // and then interrupt it (having made a copy of the pointer
        // to it).  Examples all do it the latter way.  Why?
        if (_socketReadingThread != null) {
            _socketReadingThread.interrupt();
            _socketReadingThread = null;
        } else {
            if (_debugging) {
                _debug("socketReadingThread null at wrapup!?");
            }
        }

        if (_socket != null) {
            _socket.close();
            _socket = null;
        } else if (_multicastSocket != null) {
            _multicastSocket.close();
            _multicastSocket = null;
        } else {
            if (_debugging) {
                _debug("Socket null at wrapup!?");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Synchronization objects.  Used only for synchronization.
    private Object _syncFireAndThread = new Object();

    private Object _syncDefaultOutputs = new Object();

    private Object _syncBufferLength = new Object();

    private Object _syncSocket = new Object();

    // Cached copies of parameters:
    private int _actorBufferLength;

    private boolean _overwrite;

    private boolean _blockAwaitingDatagram;

    //whether is multicast datagram or not.
    private boolean _multiCast = false;

    // Cashed copies of default outputs:
    private String _defaultReturnAddress;

    private int _defaultReturnSocketNumber;

    private Token _defaultOutputToken;

    // Packet buffer info.  Allocated lengths need to be kept track of
    // separately because the .getLength() method returns the length
    // of the contained data, not the (typically longer) length
    // allocated to the data buffer.  FIXME - could I do
    // .getData().Length() to get the byte array's length?
    private DatagramPacket _receivePacket = null;

    private DatagramPacket _broadcastPacket = null;

    private int _receiveAllocated = 0;

    private int _broadcastAllocated = 0;

    // Misc.
    private int _packetsAlreadyAwaitingFire = 0;

    private int _ChangeRequestedToPlatformBufferLength;

    // System resources allocated: DatagramSocket and Thread to read it.
    private DatagramSocket _socket;

    private MulticastSocket _multicastSocket;

    private SocketReadingThread _socketReadingThread;

    // Most recent non-default values output from the
    // <i>returnAddress</i>, <i>returnSocketNumber</i>, and
    // <i>output</i> ports respectively.  Someday useful if
    // a <i>repeat</i> parameter is implemented.  Otherwise,
    // could have been local variables to the fire() method.
    private String _returnAddress;

    private int _returnSocketNumber;

    private Token _outputToken;

    //the remote address the packege from.
    private InetAddress _address = null;

    // Flag used when changing the port number (a.k.a. socket number) of
    // this actor's datagram socket.  Serves both to remind thread to
    // loop back when it has been disrupted by AttributeChanged() and
    // to tell AttributeChanged() how to behave to minimize disruption
    // of the thread.
    private boolean _inReceive = false;

    // Flags used for stopFire() capability.
    private boolean _fireIsWaiting = false;

    private boolean _stopFire = false;

    ///////////////////////////////////////////////////////////////////
    ////                        private inner class                ////
    private class SocketReadingThread extends Thread {
        /** Constructor.  Create a new thread to listen for packets
         *  at the socket opened by the actor's [pre]initialize method.
         */
        public SocketReadingThread() {
        }

        /** Run.  Run the thread.  This begins running when .start()
         *  is called on the thread.
         */
        @Override
        public void run() {
            while (true) {
                // [Set and] get platform's buffer length.
                synchronized (_syncBufferLength) {
                    if (_ChangeRequestedToPlatformBufferLength > 0) {
                        _ChangeRequestedToPlatformBufferLength = -1;

                        try {
                            // [Set].  If the setPlatformBufferLength
                            // parameter is true, then suggest the
                            // platform set its buffer size to the
                            // platformBufferLength parameter value
                            // (in bytes).
                            if (((BooleanToken) setPlatformBufferLength
                                    .getToken()).booleanValue()) {
                                if (_multiCast) {
                                    _multicastSocket
                                            .setReceiveBufferSize(((IntToken) platformBufferLength
                                                    .getToken()).intValue());
                                } else {
                                    _socket.setReceiveBufferSize(((IntToken) platformBufferLength
                                            .getToken()).intValue());
                                }
                            }

                            // Get.
                            // See what actual buffer size was allocated.
                            // Copy this into the platformBufferSize
                            // parameter.  This allows a user to see
                            // what buffer size settings are in place
                            // in the underlying platform.
                            // NOTE: The call below to setToken() results
                            // in AttributeChanged() being called again.
                            // This is why the flag is set to -1 above.
                            // Doing so avoids an infinite sequence of calls.
                            //                            platformBufferLength.setToken(new IntToken(
                            //                                     _socket.getReceiveBufferSize()));
                        } catch (SocketException ex) {
                            System.out.println("Socket Ex." + ex.toString());

                            // throw new IllegalActionException(this,
                            // ex.toString());
                        } catch (IllegalActionException ex) {
                            System.out.println("getToken or setToken failed"
                                    + "on platformBufferSize" + ex.toString());
                        }
                    }
                }

                // Allocate or resize the packet buffers.
                synchronized (_syncBufferLength) {
                    if (_receivePacket == null
                            || _receiveAllocated != _actorBufferLength) {
                        _receivePacket = new DatagramPacket(
                                new byte[_actorBufferLength],
                                _actorBufferLength);
                        _receiveAllocated = _actorBufferLength;
                    }
                }

                if (_broadcastPacket == null) {
                    // If merely wrong length, fix it next time around
                    // when the buffers have swapped and it is called
                    // <i>_receivePacket</i>!  This code is here for
                    // when the thread is run for the first time.
                    // FIXME Maybe it is not necessary at all!
                    synchronized (_syncBufferLength) {
                        _broadcastPacket = new DatagramPacket(
                                new byte[_actorBufferLength],
                                _actorBufferLength);
                        _broadcastAllocated = _actorBufferLength;
                    }
                }

                // Attempt to receive a datagram packet!
                synchronized (_syncSocket) {
                    _inReceive = true;
                }

                while (_inReceive) {
                    // Fluff the buffer back up to its allocated size.
                    // Otherwise, it forgets how big it is and can
                    // receive a datagram no bigger than the last one.
                    _receivePacket.setLength(_actorBufferLength);

                    if (_multiCast) {
                        try {
                            // NOTE: The following call may block.
                            _multicastSocket.receive(_receivePacket);

                            // A packet was successfully received!
                            synchronized (_syncSocket) {
                                _inReceive = false;
                                _syncSocket.notifyAll();
                            }
                        } catch (IOException ex) {
                            // _inReceive is still true!  Will retry
                            // receive().  Don't retry, however, until
                            // attributeChanged() is done changing the
                            // <i>localSocketNumber</i>
                            //synchronized (_syncSocket) {
                            //   System.out.println("foo");
                            //}
                        } catch (NullPointerException ex) {
                            if (_debugging) {
                                _debug("--!!--" + (_socket == null));
                            }

                            return;

                            // -> --!!--true
                            //System.out.println(ex.toString());
                            // -> java.lang.NullPointerException
                            //throw new RuntimeException("-null ptr-");
                            // -> java.lang.RuntimeException: -null ptr-
                            //     at ptolemy.actor.lib.net.DatagramReceiver$ListenerThread.run(DatagramReceiver.java:935)
                        }
                    } else {
                        try {
                            // NOTE: The following call may block.
                            _socket.receive(_receivePacket);

                            // A packet was successfully received!
                            synchronized (_syncSocket) {
                                _inReceive = false;
                                _syncSocket.notifyAll();
                            }
                        } catch (IOException ex) {
                            // _inReceive is still true!  Will retry
                            // receive().  Don't retry, however, until
                            // attributeChanged() is done changing the
                            // <i>localSocketNumber</i>
                            //synchronized (_syncSocket) {
                            //   System.out.println("foo");
                            //}
                        } catch (NullPointerException ex) {
                            if (_debugging) {
                                _debug("--!!--" + (_socket == null));
                            }

                            return;

                            // -> --!!--true
                            //System.out.println(ex.toString());
                            // -> java.lang.NullPointerException
                            //throw new RuntimeException("-null ptr-");
                            // -> java.lang.RuntimeException: -null ptr-
                            //     at ptolemy.actor.lib.net.DatagramReceiver$ListenerThread.run(DatagramReceiver.java:935)
                        }
                    }
                }

                // Transfer received the datagram to fire() for broadcast.
                // There are 2 datagram packet buffers.
                // If no data is already in the actor awaiting
                // fire, then swap the buffers, increment the
                // awaiting count, and either notify fire() if it
                // is waiting, or call the director's
                // fireAtCurrentTime() method to schedule a
                // firing if fire() is not waiting.
                // Else, data is already waiting.  I this case
                // look to the overwrite parameter for guidance.
                // If <i>overwrite</i> is true, then go ahead and
                // swap anyway but do not increment the count and
                // do not call fireAtCurrentTime() again.
                // In either of the two cases above, receive()
                // gets called again promptly.  However, if data
                // is waiting AND <i>overwrite</i> is false, then
                // instead wait() here in the synchronized section
                // to block awaiting a call to fire() and entry of
                // its synched section.  When it exits its synched
                // section, then complete this synched section
                // which swaps buffers, increments the count, and
                // calls fireAtCurrentTime().  Then go back around
                // finally to the socket.receive() call.
                // Sync to avoid executing concurrently with actor's fire().
                boolean fireAtWillBeCalled;

                synchronized (_syncFireAndThread) {
                    // Wait, if need be, for buffer space or overwrite
                    // permission
                    while (_packetsAlreadyAwaitingFire != 0 && !_overwrite) {
                        try {
                            _syncFireAndThread.wait();
                        } catch (InterruptedException ex) {
                            // Don't use exception chaining so the
                            // code generator will be able to generate
                            // code for jdk1.1 compatible devices.
                            throw new RuntimeException("-interrupted-");
                        }
                    }

                    // Swap the memory areas which the packets reference.
                    // Keep the correct allocation info. with each.
                    DatagramPacket tmp = _broadcastPacket;
                    _broadcastPacket = _receivePacket;
                    _receivePacket = tmp;

                    int tmpLength = _broadcastAllocated;
                    _broadcastAllocated = _receiveAllocated;
                    _receiveAllocated = tmpLength;

                    // Before leaving synchronization, choose a course
                    // of action regarding whether or not to call
                    // fireAtCurrentTime().  This is simply a way to
                    // put the call outside this sync block.
                    // <i>_packetsAlreadyAwaitingFire</i> is incremented
                    // too, unless data was overwritten.
                    if (_packetsAlreadyAwaitingFire != 0) {
                        // Swap just overwrote the packet.  Fire()
                        // will now take new the one instead without
                        // any additional prompting.
                        fireAtWillBeCalled = false;
                    } else if (_fireIsWaiting) {
                        // Fire() can only be waiting when packets
                        // already waiting is zero.  Again, no need to
                        // call fireAtCurrentTime().  Simply notify
                        // fire() that it may stop waiting for data.
                        fireAtWillBeCalled = false;
                        _syncFireAndThread.notifyAll();
                        _packetsAlreadyAwaitingFire++;
                    } else {
                        fireAtWillBeCalled = true;
                        _packetsAlreadyAwaitingFire++;
                    }
                }

                if (fireAtWillBeCalled) {
                    try {
                        getDirector().fireAtCurrentTime(DatagramReader.this);
                    } catch (IllegalActionException ex) {
                        throw new RuntimeException("fireAtCurrentTime() "
                                + "threw an exception", ex);
                    }
                }
            }
        }
    }
}
