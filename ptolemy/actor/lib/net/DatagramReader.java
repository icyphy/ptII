/* An actor that asynchronously reads datagram packets.

 Copyright (c) 2001-2002 The Regents of the University of California.
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

@ProposedRating red (winthrop@robotics.eecs.berkeley.edu)
@AcceptedRating red (winthrop@robotics.eecs.berkeley.edu)
*/

package ptolemy.actor.lib.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
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

<p>
<p>The simplest scenario has the thread constantly stalled awaiting a
packet.  When a packet arrives, the thread quickly queues it in one of
the actor's buffers, calls the getDirector().fireAtCurrentTime(), and then
stalls again awaiting the next packet.  By stalling again and again,
the thread keeps the actor aware at all times of incoming packets.
This is particularly important if packets come in more quickly than
the model can process them.  Depending on the domain (e.g. DE) in
which this actor is used, the director may respond to the
thread's fireAtCurrentTime()
call by calling the actor's fire() method.
In this case, fire() then broadcasts the data
received, along with the return address and return socket number from
which the datagram originated.

<p>The data portion of the packet is broadcast at the <i>output</i> port.
The <i>encoding</i> parameter determines how the bytes received are
formed into a token.  The 3 values currently available for the
<i>encoding</i> parameter are "for_Ptolemy_parser",
"raw_integers_little_endian", and "raw_low_bytes_of_integers".

<p>If <i>encoding</i> equals "for_Ptolemy_parser", then the bytes
received are first made into a string using the platform's default
character encoding.  Then this string is parsed by the Ptolemy parser.
The parser is capable of throwing an exception if the data has been
garbled or truncated or is incorrectly formed for the parser.  A
variety of output data types are possible, and may vary from packet 
to packet.  This is because this setting permits the Ptolemy parser 
to determine the type as it parses. 

<p>If <i>encoding</i> equals "raw_integers_little_endian", then the bytes
received are packed into integers, 4 bytes to each integer.  As the
name implies, the low byte of each integer receives a byte whose
index in the received byte array is a multiple of 4.  The most significant
byte of this same integer receives a byte whose address in the received
array was 3 more than the low byte.  If the number of bytes received
was not a multiple 4, then the excess is discarded.  The type of the
output in this case is an array of integers.

<p>If <i>encoding</i> equals "raw_low_bytes_of_integers", everything is
the same as above except that only one byte is placed into each integer.
No attention is placed on what goes into the other 24 bits, so they may
be 1s or 0s.  Since only one byte is put into each integer, no excess
bytes are discarded.  This is the robust setting, capable of handling
any datagram that may arrive.  As above, The type of the
output is an array of integers.

<p>The return address and socket number are broadcast as String and int 
respectively.  These tell where the received datagram originated from.

<p>FIXME: Factor out the encoding capability so all actors can use it
and so the I/O actors can share it rather than each having to separately
implement it.  Ideally, there would be more encoding options than
provided here.  Actors to factor out encoding functionality might be 
named Encode and Decode, or Pack and Unpack, or simply Repack.  These
will be sort of like the conversion actors, but they will be bit-preserving
rather than value-preserving (as are most of the existing converters).

<p>
<p>The actor's behavior under less simple scenarios is governed by
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
   7	(Echo)
  21	(FTP)
  23	(TELNET)
  25	(SMTP)
  53	(DNS)
  79	(finger)
  80	(HTTP)
 110	(POP3)
 119	(NNTP)
 161	(SNMP)
 162	(SNMP Trap)
</pre>
Reference:  http://192.168.1.1/Forward.htm
(A webpage hosted from within the Linksys BEFSR41 Cable/DSL Router)

<p>Another useful tidbit is the command 'netstat'.  This works in a 
DOS prompt and also in the UNIX-like Bash shell.  In either shell, 
enter 'netstat -an'.  This command shows current port allocations!  Ports 
allocated to Ptolemy models are shown along with other port allocations.
Both TCP and UDP (datagram) ports are shown by netstat.
FIXME: Find out whether a TCP port using a specific number blocks a 
UDP port from using that same number.

@author Winthrop Williams, Joern Janneck, Xiaojun Liu, Edward Lee
(Based on TiltSensor actor written
   by Chamberlain Fong, Xiaojun Liu, Edward Lee)
@version $Id$
@since Ptolemy II 2.0 */
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
	// Type setting here of <i>output</i> is in concert with
	// setting later in the constructor of <i>encoding</i>.
        output.setTypeEquals(BaseType.GENERAL);
        output.setOutput(true);

        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setTypeEquals(BaseType.GENERAL);
        trigger.setMultiport(true);

        // parameters - Ordering here sets the order they show up in Vergil
        localSocketNumber = new Parameter(this, "localSocketNumber");
        localSocketNumber.setTypeEquals(BaseType.INT);
        localSocketNumber.setToken(new IntToken(4004));

        bufferLength = new Parameter(this, "bufferLength");
        bufferLength.setTypeEquals(BaseType.INT);
        bufferLength.setToken(new IntToken(440));

        overwrite = new Parameter(this, "overwrite", new BooleanToken(true));
        overwrite.setTypeEquals(BaseType.BOOLEAN);

        blockAwaitingDatagram = new Parameter(this, "blockAwaitingDatagram");
        blockAwaitingDatagram.setTypeEquals(BaseType.BOOLEAN);
        blockAwaitingDatagram.setExpression("true");

        defaultOutput = new Parameter(this, "defaultOutput");
        defaultOutput.setTypeEquals(BaseType.GENERAL);
        defaultOutput.setExpression("0");

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

	// <i>encoding</i> is a 'ChoiceStyle' i.e. drop-menu-choose parameter.

        encoding = new StringAttribute(this, "encoding");
	// Type setting here of <i>encoding</i> is in concert with
	// setting earlier in this constructor of <i>output</i>.
        encoding.setExpression("for_Ptolemy_parser");
	// The above setExpression() call causes a call to the
	// attributeChanged() method here in this same actor!
	// The actor uses this to set the related cached values 
	// <i>_decodeWithPtolemyParser</i> etc. to be congruent 
	// with the setting of <i>encoding</i>.

   }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The trigger input port.  The type of this port has been set to
     *  GENERAL, permitting any token type to be accepted.  The
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
     *  trigger input.)
     */
    public TypedIOPort trigger = null;

    /** The default output.  This default token is broadcast when the
     *  actor is fired, but no actual datagram data is available to
     *  broadcast and <i>blockAwaitingDatagram</i> is false.  If
     *  blocking were true, the actor would simply stall in fire()
     *  until a datagram arrives.  Default is the integer 0.
     */
    public Parameter defaultOutput;

    /** Encoding to expect of received datagrams.  This is a
     *  string-valued attribute that defaults to "for_Ptolemy_parser".
     *  This is a ChoiceStyle (i.e. drop-menu-select) parameter.
     *  @see ptolemy.actor.gui.ChoiceStyle
     *  The three options currently implemented are: "for_Ptolemy_parser",
     *  "raw_low_bytes_of_integers", and "raw_integers_little_endian".
     *  The first option allows reconstruction of any data type upon
     *  reception.  The "for_Ptolemy_parser" setting is designed to be
     *  used in partnership with a similarly configured DatagramSender.
     *  The other two options are for receiving
     *  general data in raw form.  These formats are also convenient for
     *  receiving arrays of bytes and integers respectively.  The former
     *  are not received explicitly as bytes since the Byte type is
     *  still under development in Ptolemy.  Conversion in this actor
     *  between bytes and integers simply ignores the 24 high order bits 
     *  of the integer.  For example, 511, 255, and -1 are treated as 
     *  the same value under the "raw_low_bytes_of_integers" setting.
     */
    public StringAttribute encoding;


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

    /** This port outputs the IP address portion of the received
     *  datagram packet.  The type of this output is String.  This is
     *  the IP address of the remote datagram socket which sent the
     *  packet to this actor's socket.  Under IPv4, this string has
     *  the familiar form "128.32.1.1".  This output defaults (when no
     *  datagram has been received and blocking is false) to the IP
     *  address of this actor's socket.
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

    /** This actor's local socket (a.k.a. port) number.  <b>This is a
     *  system resource allocated to this actor.</b> No other actor
     *  with the same local socket number may run at the same time.
     */
    public Parameter localSocketNumber;

    /** Boolean directive in case datagrams pile up.  Default is true.
     *  If false, datagrams will queue up (mostly in the platform,
     *  some in the actor).  The datagram used at each invocation of
     *  fire will be the oldest in the queue.  On the other hand, if
     *  <i>overwrite</i> is true, then minimal queuing will occur and
     *  the most recent data will be used when fire() is called.
     *  Older data will be discarded.
     */
    public Parameter overwrite;

    /** Length (in bytes) of each of the two packet buffers for
     *  receiving a datagram.  This length does not include the bytes
     *  needed for storing the datagram's return address and other
     *  housekeeping information.  This buffer need only be big enough
     *  to hold the payload or net contents of the datagram.  There is
     *  also a buffer somewhere in the Java Virtual Machine or in the
     *  underlying firmware or platform.  The size of this buffer is
     *  not controlled by this actor, but it could be.  Its length is
     *  accessible via the getReceiveBufferSize() and
     *  setReceiveBufferSize() methods of @see java.net.DatagramSocket.
     *  Caution - The set*() is only a suggestion.  Must call get*()
     *  to see what you actually got.
     */
    public Parameter bufferLength;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change of the given attribute.  Generally, this is
     *  called between firings of an actor.  However, this actor
     *  contains a separate thread of which the director is not aware.
     *  Any time the model is running, calls to this method typically
     *  happen while the thread is running.  Furthermore, the thread
     *  spends most of its time blocked in the DatagramSocket.receive()
     *  method or (if <i>overwrite</i> is false) waiting for fire() to
     *  notify it that space is available to receive another packet.
     *  Thus, some of the cases below will be given special handling.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        // Conditionally track calls to this method.
        // Don't track _evalVar because it gets called every time
        // a datagram is parsed with the Ptolemy parser.
        if (attribute != _evalVar) {
            if (_debugging) _debug("AtCh"
                    + attribute.toString().substring(28));
        }

        // Cache parameters into private variables:

	// This is a 'ChoiceStyle' i.e. drop-menu-choose parameter.
        // See also ../io.xml for other half of this mechanism.
        if (attribute == encoding) {
	    //System.out.println("---" + encoding.getExpression() + "---");
	    //System.out.println("--" + _encoding + "--");
	    //System.out.println("-" + _encoding.equals("abc") + "-");
            if (!_encoding.equals(encoding.getExpression())) {
		_encoding = encoding.getExpression();
		if (_encoding.equals("for_Ptolemy_parser")) {
		    _decodeWithPtolemyParser = true;
		    _decodeToIntegerArray = false;
		} else if (_encoding.equals("raw_low_bytes_of_integers")) {
		    _decodeWithPtolemyParser = false;
		    _decodeToIntegerArray = true;
		    _decodedBytesPerInteger = 1;
		} else if (_encoding.equals("raw_integers_little_endian")) {
		    _decodeWithPtolemyParser = false;
		    _decodeToIntegerArray = true;
		    _decodedBytesPerInteger = 4;
		} else {
		    throw new IllegalActionException(this,
		            "Unrecognized encoding choice " + _encoding);
		}

		// This code is falsed out (like commented out).  To have
		// it really work, that is set the type of the <i>output</i>
		// port if and only if the <i>encoding</i> parameter is
		// actually changed, some things would need to be fixed.
		// The "configure" dialog currently implements "cancel" 
		// via a reapplication of stored values, thus reverting 
		// the change.  However, my actor would have already 
		// overwritten and lost user settings in such a process.  
		// Having fixed this, the next step would be to use 
		// MoMLChangeRequest(originator, context, "<....>") in 
		// place of setTypeEquals() whose changes do not stick. 
		// Finally, to make this safe during the constructor 
		// call, maybe an _inConstructor variable would serve 
		// to avoid overwriting user settings.
		if (false) {


		    // I have acted on this parameter only if the requested
		    // new value of the parameter differs from what is already
		    // in use.  This caveat improves usability by avoiding
		    // unnecessary undoing of the user's "configure ports"
		    // setting of the <i>output</i> by the code below.
		    //
		    // Set type of <i>output</i>
		    if (_decodeWithPtolemyParser) {
			// Set <i>output</i> to GENERAL so that output can
			// handle whatever type comes out of the parser.
			//if (true) System.out.println("->GENERAL");
			output.setTypeEquals(BaseType.GENERAL);
		    } else if (_decodeToIntegerArray) {
			// Set <i>output</i> to {INT} to exactly match the
			// form in which the raw data will be output.
			// (To wit, as an array of integers, each holding
			// one 8-bit byte of data and 24 unused bits.)
			//if (true) System.out.println("->ArrayType(INT)");
			output.setTypeEquals(new ArrayType(BaseType.INT));
			// Aha!  Found this in data/type/ArrayType.java by
			// double grepping ptolemy/data/*/*.java!
		    } else {
			// No other cases implemented yet.
		    }

		}

	    } // Close if (_encoding != encoding)

        // In the case of <i>defaultOutput</i>, synchronize in ensure
        // atomic copy from the parameter to the the private variable.

        } else if (attribute == defaultOutput) {
             synchronized(_syncDefaultOutput) {
                _defaultOutputToken = defaultOutput.getToken();
		// _defaultOutputToken==null when eser enters blank parameter!
		if (false) System.out.println(_defaultOutputToken==null);
                // This private variable is named _defaultOutputToken
                // instead of _defaultOutput because it is being
                // kept in token form (allowing any type).
                // Typically, private copies of parameters are in
                // value form.  Thus the atypical name to clue the
                // reader in to the atypical content.
            }


        // In the case of <i>blockAwaitingDatagram</i> or <i>overwrite</i>,
        // notify potentially waiting fire() or thread respectively
        // that it might no longer need to wait.  Each will recheck its
        // conditions when notified, so extra notifications merely
        // waste a little CPU.

        } else if (attribute == overwrite) {
            _overwrite = ((BooleanToken)
                    (overwrite.getToken())).booleanValue();
            if (_overwrite) {
                synchronized(_syncFireAndThread) {
                    _syncFireAndThread.notifyAll();
                }
            }

        } else if (attribute == blockAwaitingDatagram) {
            _blockAwaitingDatagram = ((BooleanToken)
                    (blockAwaitingDatagram.getToken())).booleanValue();
            if (!_blockAwaitingDatagram) {
                synchronized(_syncFireAndThread) {
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
           synchronized(this) {
               if (_socket != null) {
                   // Verify presence & health of the thread.
                   if (_listenerThread == null) {
                       throw new IllegalActionException(this, "thread==null");
                   } else if (!_listenerThread.isAlive()){
                       throw new IllegalActionException(this, "thread !Alive");
                   }
                   int newSktNum = ((IntToken)
                           (localSocketNumber.getToken())).intValue();
                   if (newSktNum != _socket.getLocalPort()) {
                       synchronized(_syncSocket) {
                           if (_inReceive) {
                               // Wait for receive to finish, if it
                               // does not take very long that is.
                               try {
                                   _syncSocket.wait((long)444);
                               } catch (InterruptedException ex) {
                                   System.out.println("---!!!---");
                                   throw new IllegalActionException(this,
                                           "Interrupted while waiting");
                               }
                               // Either I've been notified that receive()
                               // has completed, or the timeout has occurred.
                               // It does not matter which.  Either way I am
                               // now ready to close and re-open the socket.
                           }
                           _socket.close();
                           try {
                               _socket = new DatagramSocket(newSktNum);
                           }
                           catch (SocketException ex) {
                               System.out.println("couldn't open new socket");
                               throw new InternalErrorException(
                                       KernelException.stackTraceToString(ex));
                           }
                       } // Sync(_syncSocket)
                   }
               }
           } // Sync(this)

        // In the case of <i>bufferLength</i>, simply cache the parameter.
        // The thread used this value to set the size of a buffer prior 
        // to the socket.receive() call.  The thread only resizes a buffer 
        // when it is about to call receive on it and this parameter has 
        // changed from the value last used for that specific buffer.
        // Synchronization ensures that the thread's test for a change in
        // this value and its use of the value access the same thing.

        } else if (attribute == bufferLength) {
            synchronized(_syncBufferLength) {
                _bufferLength = ((IntToken)
                       (bufferLength.getToken())).intValue();
            }

        } else {
            super.attributeChanged(attribute);
        }

        if (attribute != _evalVar) {
            if (_debugging) _debug(this + "attributeChanged() done");
            if (_debugging) _debug("---"
                    + attribute.toString().substring(28));
        }

    }

    /** Fire this actor.  Optionally parse a received datagram into a
     *  token of arbitrary type.  Otherwise, assemble an integer array
     *  token by placing 1 or 4 bytes of the datagram into each integer in
     *  the array.  Broadcast the converted token on the output port.
     *  @exception IllegalActionException If the data cannot be
     *  converted into a token of the same type as the configured type
     *  of the output port.
     */
    public void fire() throws IllegalActionException {

	/*
	  // This section was for experimenting with stopFire()
	System.out.println("   fire() is called   ");
	synchronized(this) {
	    try {
		wait(1000);
	    } catch (InterruptedException ex) {
		System.out.println("interrupted exception!");
	    }
	}
	System.out.println("   fire() will sync   ");
	*/

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
        synchronized(_syncFireAndThread) {
            int bytesAvailable = 0; // Compiler requires it to be initialized.
            byte[] dataBytes = new byte[0];  // Compiler requires init.

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
	    while (_blockAwaitingDatagram &&
                    _packetsAlreadyAwaitingFire == 0) {
                try {
                    _fireIsWaiting = true;
                    _syncFireAndThread.wait();
                    _fireIsWaiting = false;
                } catch (InterruptedException ex) {
                    System.out.println(this + "!!fire()'s wait interrupted!!");
                    throw new RuntimeException("!-!");
                } finally {
		    if (_stopFire) {
			_stopFire = false;
			if (_debugging) _debug("return due to stopFire");
			return;
		    }
                }
            }

            if (_packetsAlreadyAwaitingFire != 0) {
                useDefaultOutput = false;
                bytesAvailable = _broadcastPacket.getLength();
                dataBytes = _broadcastPacket.getData();//The buffer, not copy.
                _returnAddress = _broadcastPacket
                        .getAddress().getHostAddress();
		_returnSocketNumber = _broadcastPacket.getPort();
		_packetsAlreadyAwaitingFire--;
            } else {
                useDefaultOutput = true;
            }

            if (!useDefaultOutput) {
                if (_decodeWithPtolemyParser) {
                    // Make the data into a string.
                    String dataStr = new String(dataBytes, 0, bytesAvailable);
                    // Parse this data string to a Ptolemy II data object
                    _evalVar.setExpression(dataStr);
                    // Stack dump calling getToken() in line below
                    // may indicate type of data received does not
                    // match type of <i>output</i> port.
                    // Please configure port to match type being sent.
                    _outputToken = _evalVar.getToken();
                    if (_outputToken == null) {
                        // FIXME - Consider doing this only in the
                        // case where bytesAvailable == 0 vs all cases
                        // which lead to _outputToken == null.
                        if (_debugging) _debug(
                                "Broadcast blank 'new Token()'");
                        _outputToken = new Token();
                        // This simplest of tokens is of type 'general'.
                        // It prints on the display actor as 'present'.
                    } else {
                        if (_debugging) _debug(
                                "Broadcast non-null parsed token");
                    }
                } else if (_decodeToIntegerArray) {
		    int xs = bytesAvailable%_decodedBytesPerInteger;
		    if (xs != 0) {
			if (_debugging) _debug(xs+" bytes will be discarded");
		    }
                    if (bytesAvailable/_decodedBytesPerInteger > 0) {
			// Make an array of tokens.
                        Token[] dataIntTokens = new Token[
			        bytesAvailable/_decodedBytesPerInteger];
                        // Fill each token with N bytes, low byte first.
			if (_decodedBytesPerInteger/*(N)*/ == 1) {
			    for (int j = 0; j < bytesAvailable; j++) {
				dataIntTokens[j] = new IntToken(dataBytes[j]);
			    }
			} else if (_decodedBytesPerInteger/*(N)*/ == 4) {
			    for (int j = 0; j < bytesAvailable/4; j++) {
				dataIntTokens[j] = new IntToken(
				        dataBytes[4*j] +
                                        256 * dataBytes[4*j+1] +
					65536 * dataBytes[4*j+2] +
					16777216 * dataBytes[4*j+3]);
			    }
			} else {
			    // No other cases of (N) implemented.
			}
                        // Assemble these into an array-token of tokens
                        if (_debugging) _debug(
                                "Broadcast non-zero length {int}");
                        _outputToken = new ArrayToken(dataIntTokens);
                    } else {
                        // Special case of zero length array:
                        // FIXME - test Yuhong's work
                        if (_debugging) _debug(
                                "Broadcast zero length {int}");
                        _outputToken = new ArrayToken(BaseType.INT);
                        // WORKS! (in 20Jan tree).  Comment out for 14Dec tree.
                    }
		} else {
		    // No other cases implemented yet.
		    if (true) System.out.println(
			    "Broadcast token not being set");
 		    throw new IllegalActionException(this,
		            "Unrecognized encoding selection " + _encoding);
                }
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

	    // _defaultOutputToken==null when eser enters blank parameter!
	    // Thus this cool and useful test works!
	    if (_defaultOutputToken == null) {
		if (_debugging) _debug(
                        "DO NOT Broadcast ANY output (blank default)");
	    } else {
		if (_debugging) _debug(
                        "Broadcast default outputs");
		// Use this actor's IP address and socket number as the
		// default return address and socket number values.
		returnAddress.broadcast(new StringToken("localhost"));
		returnSocketNumber.broadcast(
                        new IntToken(_socket.getLocalPort()));
		synchronized(_syncDefaultOutput) {
		    // Ensure that any change to the default output parameter
		    // occurs atomically with respect to its use here.
		    output.broadcast(_defaultOutputToken);
		}
	    }
        }

    }

    /** Prefire.
     *  NOT IN USE.  Was for experimenting with stopFire().
     */
    /*
    public boolean prefire() {
        System.out.println(" - Prefire in called - ");
	synchronized(this) {
	    try {
		wait(1000);
	    } catch (InterruptedException ex) {
		System.out.println("interrupted exception!");
	    }
	}
	System.out.println(" - Prefire completes - ");
	return true;
    }
    */


    /** Preinitialize this actor.  This method is used as a bookend
     *  with wrapup() being the other end.  Resources are allocated
     *  here and released in wrapup().  Create a new datagram socket
     *  and initialize the thread that reads from that socket.  Also
     *  create an evaluation variable <i>_evalVar</i>for the Ptolemy
     *  parser.  The thread will stay alive even if the socket is
     *  closed.  However, it terminates when the socket becomes null.
     *  Only wrapup() makes the socket null.
     *  @exception IllegalActionException If the
     *  <i>localSocketNumber</i> parameter has a value outside 0..65535
     *  or a socket could not be created.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        if (_debugging) _debug(this + "preinitialize has begun");

        // This is a key fix!  Programs in DE (such as 1plusFxC.xml)
        // Used to only run the first time after opening the XML!
        _packetsAlreadyAwaitingFire = 0;

        Variable var = (Variable)getAttribute("_evalVar");
        if (var == null) {
            try {
                var = new Variable(this, "_evalVar");
            } catch (NameDuplicationException ex) {
                throw new IllegalActionException(
                        "Name '_evalVar' is already in use");
            }
        }
        _evalVar = var;

        int portNumber = ((IntToken)(localSocketNumber.getToken())).intValue();
        if (portNumber < 0 || portNumber > 65535) {
            throw new IllegalActionException(this, localSocketNumber
                    + " is outside the required 0..65535 range");
        }
        if (_debugging) _debug(this + "portNumber = " + portNumber);
        // Allocate a new socket.
        try {
            if (false&&_debugging) {
                _debug("Trying to create a new socket on port " + portNumber);
            }
            _socket = new DatagramSocket(portNumber);
            if (false&&_debugging) {
                _debug("Socket created successfully!");
            }
        }
        catch (SocketException ex) {
	    // This is unmistakably clear, actor's individual name
	    // and says cannot bind because already in use!
            throw new IllegalActionException(this,
                    " Failed to create a new socket: " + ex);
        }

        // Allocate & start a thread to read from the socket.
        _listenerThread = new ListenerThread();
        _listenerThread.start();
        if (_debugging) _debug("Thread created & started.");

        if (_debugging) _debug("preinitialize ends");
        if (_debugging) _debug("------------------");

    }

    /** Override the setContainer method to ensure that if the actor
     *  is deleted while the model is running, then any resources it 
     *  has locked are released.
     */
    public void setContainer(CompositeEntity container) 
            throws IllegalActionException, NameDuplicationException {
	if (container != getContainer()) {
            wrapup();
	}
	super.setContainer(container);
    }

    /** Stop fire.  Stop the fire() method, but only if it is blocked.
     *  The actor returns from fire() with its state the same as
     *  before fire() was called.  Thus, when the (director? manager?)
     *  is ready, it may call fire() again and the actor will pick up
     *  where it left off.  If fire() not blocked, it is permitted to
     *  complete.  NOTE: When pausing or stopping execution, it will
     *  on rare occasion be necessary to press 'pause' or 'stop' a
     *  second time.  This is because the director/manager insists in
     *  calling fire() after every call of prefire().  Even when it
     *  has issued a stopFire() during prefire(), it persists,
     *  executing the very fire() it is trying to stop!  This behavior
     *  makes it impossible for the actor to cover all the cases where
     *  it ought to stop.  See detailed discussion below.
     *  Fortunately, multiple presses of the 'pause' and 'stop'
     *  buttons result in multiple stopFire() calls.  By the time a
     *  user can click the mouse a second time, fire() will have blocked
     *  if it is going to.  It can then be stopped as expected.

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

     *  Perhaps prefire() could serve to clear the flag!  This assumes
     *  that, upon resuming execution, prefire() is repeated before
     *  fire() is reentered.-[Tested; assumption holds.]  It also
     *  assumes that superfluous calls to stopFire() do not occur
     *  during prefire() or between prefire() and fire().-{Tested;
     *  assumption holds with only known superfluous call, the one
     *  when the user presses 'Go'] Additionally, this assumes that
     *  intentional calls to stopFire(), if they occur before or
     *  during the preinitialize call, are backed up by a test which
     *  prevents the director from calling fire() if it has already
     *  called stopFire() with the intention of stopping that same
     *  fire().-[Tested; assumption does not hold.  I caused
     *  stopFire() to be called during prefire().  The director
     *  went ahead and called fire() anyway!]

     *  If the director/manager need to be fixed anyway, perhaps the
     *  stopFire paradigm ought to be rearchitected to incorporate a
     *  flag.  (Does it already and I just don't know about it?)

     */
    public void stopFire() {
        if (_debugging) _debug("stopFire() is called");
        synchronized(_syncFireAndThread) {
            if (_fireIsWaiting) {
                // stopFire() gets called a lot.  Including each time 
                // the program is started.  This caveat has proven 
                // necessary to avoid disrupting one of the first 
                // few firings.
		_stopFire = true;
                _syncFireAndThread.notifyAll();
                if (_debugging) _debug("stopFire() has notified fire()");
            } else {
                if (_debugging) _debug("stopFire() did not notify fire()");
            }
        }
    }

    /** Wrapup execution of this actor.  Interrupt the thread that was
     *  created to read from the socket and close the socket.
     *  @exception IllegalActionException If the thread or the socket
     *  was not created.
     */
    public void wrapup() throws IllegalActionException {

        if (_debugging) _debug("WRAPUP IS CALLED");

        //System.err.println("wrapup() has been called in " + this);
        //e.printStackTrace();  
	      // FIXME cxh's java checker recommends
	      // KernelException.stackTraceToString(ex)
	      // instead of printStackTrace() above.  Try it.
        //throw new RuntimeException("Manager: " + e.getMessage());

        //throw new IllegalActionException(this,
        //"has had its wrapup() method called.");

        // FIXME - Look into whether I ought to make it null first
        // and then interrupt it (having made a copy of the pointer
        // to it).  Examples all do it the latter way.  Why?
        if (_listenerThread != null) {
            _listenerThread.interrupt();
            _listenerThread = null;
        } else {
            if (_debugging) _debug("listenerThread null at wrapup!?");
            //throw new IllegalActionException(
            //        "listenerThread null at wrapup!?");
        }

        if (_socket != null) {
            _socket.close();
            _socket = null;
        } else {
            if (_debugging) _debug("Socket null at wrapup!?");
            //throw new IllegalActionException("Socket null at wrapup!?");
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Synchronization objects.  Used only for synchronization.
    private Object _syncFireAndThread = new Object();
    private Object _syncDefaultOutput = new Object();
    private Object _syncBufferLength = new Object();
    private Object _syncSocket = new Object();

    // Cached copies of parameters:
    private int _bufferLength;
    private boolean _overwrite;
    private boolean _blockAwaitingDatagram;
    private Token _defaultOutputToken;

    // Cached copy of <i>encoding</i> parameter and its drogues:
    private String _encoding = new String("");
    private boolean _decodeWithPtolemyParser;
    private boolean _decodeToIntegerArray;
    private int _decodedBytesPerInteger;

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

    // System resources allocated: DatagramSocket and Thread to read it.
    private DatagramSocket _socket;
    private ListenerThread _listenerThread;

    // Evaluation variable for the Ptolemy parser.
    private Variable _evalVar;

    // Most recent non-default values output from the
    // <i>returnAddress</i>, <i>returnSocketNumber</i>, and
    // <i>output</i> ports respectively.  Someday useful if
    // a <i>repeat</i> parameter is implemented.  Otherwise,
    // could have been local variables to the fire() method.
    private String _returnAddress;
    private int _returnSocketNumber;
    private Token _outputToken;

    // Used when changing the port number (a.k.a. socket number) of
    // this actor's datagram socket.  Serves both to remind thread to
    // loop back when it has been disrupted by AttributeChanged() and
    // to tell AttributeChanged() how to behave to minimize disruption
    // of the thread.
    private boolean _inReceive = false;

    // Used for stopFire() capability.
    private boolean _fireIsWaiting = false;
    private boolean _stopFire = false;

    ///////////////////////////////////////////////////////////////////
    ////                        private inner class                ////

    private class ListenerThread extends Thread {

        /** Constructor.  Create a new thread to listen for packets
         *  at the socket opened by the actor's preinitialize method.
         */
        public ListenerThread() {
        }

        /** Run.  Run the thread.  This begins running when .start()
         *  is called on the thread.
         */
        public void run() {
            while (true) {

                // Allocate or resize the packet buffers.
                synchronized(_syncBufferLength) {
                    if (_receivePacket == null ||
                            _receiveAllocated != _bufferLength) {
                        _receivePacket = new DatagramPacket(
                                new byte[_bufferLength], 0, _bufferLength);
                        _receiveAllocated = _bufferLength;
                    }
                }
                if (_broadcastPacket == null) {
                    // If merely wrong length, fix it next time around
                    // when the buffers have swapped and it is called
                    // <i>_receivePacket</i>!  This code is here for
                    // when the thread is run for the first time.
                    // FIXME Maybe it is not necessary at all!
                    synchronized(_syncBufferLength) {
                        _broadcastPacket = new DatagramPacket(
                                new byte[_bufferLength], 0, _bufferLength);
                        _broadcastAllocated = _bufferLength;
                    }
                }

                // Attempt to receive a datagram packet!
                synchronized(_syncSocket) {
                    _inReceive = true;
                }
                while (_inReceive) {
                    // Fluff the buffer back up to its allocated size.
                    // Otherwise, it forgets how big it is and can
                    // receive a datagram no bigger than the last one.
                    _receivePacket.setLength(_bufferLength);
                    try {
                        // NOTE: The following call may block.
                        _socket.receive(_receivePacket);
                        // A packet was successfully received!
                        synchronized(_syncSocket) {
                            _inReceive = false;
                            _syncSocket.notifyAll();
                        }
                    } catch (IOException ex) {
                        // _inReceive is still true!  Will retry
                        // receive().  Don't retry, however, until
                        // attributeChanged() is done changing the
                        // <i>localSocketNumber</i>
                        synchronized(_syncSocket) {
                        }
                    } catch (NullPointerException ex) {
                        if (_debugging) _debug("--!!--" + (_socket == null));
			return;
			// -> --!!--true
                        //System.out.println(ex.toString());
                        // -> java.lang.NullPointerException
                        //throw new RuntimeException("-null ptr-");
			// -> java.lang.RuntimeException: -null ptr-
			//     at ptolemy.actor.lib.net.DatagramReceiver$ListenerThread.run(DatagramReceiver.java:935)
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
                synchronized(_syncFireAndThread) {

                    // Wait, if need be, for buffer space or overwrite
                    // permission
                    while (_packetsAlreadyAwaitingFire != 0 && !_overwrite) {
                        try {
                            _syncFireAndThread.wait();
                        } catch (InterruptedException ex) {
                            System.out.println("-!- interrupted");
                            System.out.println(ex.toString());
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

                } // Close sync block

                if (fireAtWillBeCalled) {
                    try {
                        getDirector().fireAtCurrentTime(
                                DatagramReader.this);
                    } catch (IllegalActionException ex) {

                        System.out.println(this
                                + "!!thread catch fireAtCurrentTime!!");
                        throw new RuntimeException("-fireAt* catch-");
                    }
                }

            } // Close while (true)
        } // Close run()

    } // Close private class ListenerThread

} // Close the whole java file




