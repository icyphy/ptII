/* An actor that receives Datagram packets via a separate thread.

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
//// DatagramReceiver
/**
This actor receives datagram packets via a separate thread it creates.
Datagrams are open-loop internet communications.  Each contains data
plus a kind of return address.  FIXME - return address output has not
been implemented.  Datagrams use the UDP protocol under
which no reply or confirmation is expected.  This is in contrast to
TCP which expects confirmations and attempts to receive packets in
order.  Because UDP makes no such attempts, it never hangs and does
not need to be timed out.  The simplest scenario has the thread
constantly stalled awaiting a packet.  When it receives one, it
quickly queues it, calls fireAt(), and then stalls again for the next
packet.  The director is then expected to respond to fireAt() by
calling the actor's fire() method.  Fire() broadcasts the data
received, plus the remote address and remote socket number from which
the datagram originated.  This information goes to connected actors in
the Ptolemy model in the manner prescribed by the director.  <p>

This actor has been developed to work in the Discrete Event (DE) and
Synchronous Data Flow (SDF) domains.  Use elsewhere with caution.

The data portion of the packet is brodcast as an array of bytes.  The
remote address and socket number are each broadcast as an int.
FIXME-Eventually do work below, but for now I have made actor
switchable with <i>decodeWithPtolemyParser</i> boolean parameter.
FIXME-Add auxilary actors to translate in and out of these raw
formats. Specifically, for the byte array: serialize, deserialize,
packInt, unpackInt, packDouble, unpackDouble, packString, unpackString
(or maybe one actor or actor pair which handles several data types),
packForParse, unpackUsingParse.  Specifically for the address:
IPIntToString, IPStringToInt (takes only "128.32.239.10" format or
"localhost" to ensure no lookup delay), URLLookupToString.  (Maybe
also something working from/to a "128.32.239.10:80" format.  Maybe a
long combining both IP address and remote socket number.)  Additional
planned work: Ability to handle "connect"ions (which speed datagram
communication by only having to pass security once).  A similar actor
for HTTP and a generic one which can receive any kind of packet,
perhaps over a range of local socket numbers.  A simplified form of
this actor without a separate thread for experimenting with using
model composition (e.g. PN enclosing DE) for providing necessary
concurrency.  <p>

The actor's behavior under less simple scenarios is governed by
parameters.  Additional packet(s) can arrive while the director is
getting around to calling fire().  Conversely, the director may make
extra calls to fire(), even before any datagrams have come in.  (It is
assumed, however, that for each call to the director's fireAt()
method, the director will make at least one call to the actor's fire()
method.)  <p>

There are two packet buffers.  The thread and the fire() method
contain synchronized sections.  This synchronization prevent conflicts
when accessing the shared buffers and when accessing the count of
queued packets.  <p>

The <i>overwrite</i>parameter applies to the eager packet scenario.
If true (the default), it has the actor discard the queued
packet in favor of the new one.  If false, the new packet is queued
behind the existing one.  In the latter case, both buffers are now
full.  The thread then waits for fire() to consume a queued packet
before it stalls again awaiting the next.  In all other cases
(<i>overwrite</i> true or no queued packets) the thread immidiately
stalls to await the next packet.<p>

FIXME - Implement the <i>repeat</i> parameter as described below.  The
<i>repeat</i> parameter applies to the eager director case.  If
<i>repeat</i> is true, additional calls to fire() cause rebroadcast of
the most recent data and return-address information.  When fire() is
called before the first packet has arrived, default data are
broadcast.  If <i>repeat</i> is false, then the the default data are
always used for additional calls to fire(), even a previous packet
is available to repeat.  Until implemented, <i>repeat</i> is false.<p>

The <i>blockAwaitingDatagram</i> parameter interects with the
<i>defaultOutput</i> and <i>repeat</i> above.  If true, the actor's
fire method blocks until a datagram has arrived.  If false, the
default/repeat behavior described above takes effect.

This actor has a parameter <i>localSocketNumber</i> for the port
number assigned to the local datagram socket.  Initially, the local
socket number is set to -1 to indicate no socket at all (leave
socket=null).  FIXME - This "-1" business is no good if the actor were
within a composite actor that was itself within the library.
Currently, this actor allocates the socket in preinitialize().  It
needs to do it before the model runs but after it leaves the library.
This may require a redifinition of when the [pre]initialize() methods
get called.  <p>

Here's a very useful command for use when working with this actor.
Bash command netstat -an is very useful in seeing current port allocations!
<p>

@author Winthrop Williams, Jorn, Xiojun, Edward Lee
(Based on TiltSensor actor written
   by Chamberlain Fong, Xiaojun Liu, Edward Lee)
@version $Id$
*/
public class DatagramReceiver extends TypedAtomicActor {

    public DatagramReceiver(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // ports - Ordering here sets the order they show up in vergil
        output = new TypedIOPort(this, "output");
        output.setTypeEquals(BaseType.GENERAL);//Goes with 777
        output.setOutput(true);

        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setTypeEquals(BaseType.GENERAL);
        trigger.setMultiport(true);

        // parameters - Ordering here sets the order they show up in vergil
        localSocketNumber = new Parameter(this, "localSocketNumber");
        localSocketNumber.setTypeEquals(BaseType.INT);
        localSocketNumber.setToken(new IntToken(-1));

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
        // here so that it would show up in vergil below
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

        /*
	decodeWithPtolemyParser =
                new Parameter(this, "decodeWithPtolemyParser");
	decodeWithPtolemyParser.setTypeEquals(BaseType.BOOLEAN);
        decodeWithPtolemyParser.setExpression("true"); // Goes with 777
	*/
	//
	// This is a 'ChoiceStyle' i.e. drop-menu-choose parameter
        encoding = new StringAttribute(this, "encoding");
        encoding.setExpression("for_Ptolemy_parser");
	// Values of _encoding and _decodeWithPtolemyParser get set
	// automatically by the call to setExpression(), which in turn
	// calls this actor's attributeChanged() method.
        // Values of _decodeToIntegerArray and _decodedBytesPerInteger
        // don't matter with Parser==true, but they get handled too in
        // attributeChanged().

   }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The trigger input port.  The type of this port has been set to
     *  GENERAL, permitting any token type to be accepted.  The
     *  .hasToken() and .get() methods are never called on this input.
     *  It serves as a trigger via the director, which is compelled to
     *  fire() this actor when a trigger input is available.  Without
     *  this input, configurations in SDF would be limited.  (See
     *  actor/lib/Source.java for archetype of a trigger input.  
     */
    public TypedIOPort trigger = null;

    /** The default output.  This default token is broadcast when the
     * actor is fired, but no actual datagram data is available to
     * broadcast and <i>blockAwaitingDatagram</i> is false.  If
     * blocking were true, the actor would simply stall in fire()
     * until a datagram arrives. 
     */
    public Parameter defaultOutput;

    /** Whether to decode with Ptolemy's parser upon reception.  Boolean.
     * Default value is true.  <p>
   FIXME: Text below:
     * When decodeWithPtolemyParser is true, fire() applies
     * <i>toString().getBytes()</i> to the <i>data</i> token prior to
     * transmission.  This allows reconstruction of any data type upon
     * reception.  When this parameter is false, the data must be an
     * array of integers.  In this case, the data transmitted is the
     * series of bytes generated by taking the least significant 8
     * bits from each integer.  */
    //public Parameter decodeWithPtolemyParser;
    //
    // This is a 'ChoiceStyle' i.e. drop-menu-choose parameter
    /** Encoding to expect of received datagrams.  This is a
     *  string-valued attribute that defaults to "forPtolemyParser".  
     */
    public StringAttribute encoding;


    /** Whether to block in fire().  If fire() is called before the
     *  datagram has arrived, the actor must either block awaiting the
     *  datagram or use its <i>defaultOutput</i>.  This blocking
     *  parameter controls which choice fire() will make.  Useful for
     *  SDF.  Has no effect in DE unless trigger input is connected.
     *  Trigger is normally unconnected in DE.  Boolean.  Default
     *  value is true.  
     */
    public Parameter blockAwaitingDatagram;

    /** This port outputs the data portion of the received datagram
     *  packet.  The type of <i>output</i> may depend on the datagram
     *  received, which may vary even during a single run of a model.
     *  <b>The user is encouraged to play with the configuration of
     *  this port</b> to best suit the need at hand.  Note that the
     *  configured type of this port changes automatically to
     *  'general' or '{int}' when <i>_decodeWithPtolemyParser</i> is
     *  changed to true or false respectively.
     */
    public TypedIOPort output;

    /** This actor's local socket (a.k.a. port) number.  <b>This is a
     *  system resource allocated to this actor.</b> No other actor
     *  with the same local socket number may run at the same time.  
     */
    public Parameter localSocketNumber;

    /** Boolean directive in case datagrams pile up.  Default is true.
     *  If false, datagrams will queue up (mostly in the platform,
     *  some in the actor).  The datagram used at each invoaction of
     *  fire will be the oldest in the queue.  On the other hand, if
     *  <i>overwrite</i> is true, then minimal queuing will occur and
     *  the most recent data will be used when fire() is called.
     *  Older data will be discarded.  
     */
    public Parameter overwrite;

    /** Length (in bytes) of each of the two packet buffers for
     * receiving a datagram.  This length does not include the bytes
     * needed for storing the datagram's return address and other
     * houskeeping information.  This buffer need only be big enough
     * to hold the payload or net contents of the datagram.  There is
     * also a buffer somewhere in the Java Virtual Machine or in the
     * underlying firmware or platform.  The size of this buffer is
     * not controlled by this actor, but it could be.  Its length is
     * accessable via the getReceiveBufferSize and
     * setReceiveBufferSize methods of java.net.DatagramSocket.
     * Caution - The set is only a suggestion.  Must call get to see
     * what you actually got.  */
    public Parameter bufferLength;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** React to a change of the given acttribute.  Generally, this is
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
            if (false) System.out.println("AtCh" 
                    + attribute.toString().substring(28));
        }

        // Cache parameters into private variables:

	// This is a 'ChoiceStyle' i.e. drop-menu-choose parameter.
        // See also ../sources.xml for other half of this mechanism.
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
		// I have acted on this parameter only if the requested
		// new value of the parameter differs from what is already
		// in use.  This caveat improves usibility by avoiding
		// unnecessary undoing of the user's "configure ports"
		// setting of the <i>output</i> by the code below.
		//
		// Set type of <i>output</i> 
		if(_decodeWithPtolemyParser) {
		    // Set <i>output</i> to GENERAL so that output can 
		    // handle whatever type comes out of the parser.
		    //if (true) System.out.println("->GENERAL");
		    output.setTypeEquals(BaseType.GENERAL);
		} else if (_decodeToIntegerArray) {
		    // Set <i>output</i> to {INT} to exactly match the 
		    // form in which the raw data will be output.
		    // (To wit, as an array of integers, each holding 
		    // one 8-bit byte of data and 24 unused bits.)
		    if (true) System.out.println("->ArrayType(INT)");
		    output.setTypeEquals(new ArrayType(BaseType.INT));
		    // Aha!  Found this in data/type/ArrayType.java by
		    // double grepping ptolemy/data/*/*.java!
		} else {
		    // No other cases implemented yet.
		}
	    } // Close if (_encoding != encoding)
	
        // In the case of <i>defaultOutput</i>, synchronize in ensure
        // atomic copy from the parameter to the the private variable.

        } else if (attribute == defaultOutput) {
             synchronized(_syncDefaultOutput) {
                _defaultOutputToken = defaultOutput.getToken();
                // This private variable is named _defaultOutputToken 
                // instead of _defaultOutput because it is being 
                // kept in token form (allowing any type).
                // Typically, private copies of parameters are in
                // value form.  Thus the atypical name to clue the 
                // reader in to the atypical content.
            }


        // In the case of <i>blockAwaitingDatagram</i> or <i>overwrite</i>, 
        // notify potentially waiting fire() or thread respectively
        // that it might nolonger need to wait.  Each will recheck its
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


        // In the case of <i>localSocketNumber</i> or <i>bufferLength</i>,
        // it is necessary to disturb the thread if it is in the call to
        // socket.recieve().  This is rather disruptive, so I choose to   
        // act on these attribute change requests only if the value of the  
        // parameter in question has actually changed.
        // 
        // For compatability with Java version 1.3, which does not
        // offer an interruptable datagram socket, the socket's
        // close() method has been employed to interrupt the receive()
        // call.  (Actually, the socket does offer a time-out feature.
        // Thus, in principal, it could offer an interrupt feature as
        // well.  They are identical operations.  Unless the socket
        // begins to prepare for the time-out as the hour approaches,
        // it must react either way to an unexpected interruption.
        // Version 1.4 has not (yet) generalized the time-out feature
        // to the full event-out flexibility it is capable of.)  
        //
        // While I'm at it, another flaw of the DatagramSocket in Java
        // is the dual inability of, first, being unable to measure
        // the length of a packet prior making a buffer to hold it
        // and, second, being unable to pass a circular buffer.  The
        // latter augmentation would be achieveable by having an
        // additional form of the receive method which would take two
        // buffers instead of one.  The 'second buffer' could be the
        // wrap-around portion of a larger circular buffer.
        //
        // 
 
            /*    Junkyard:
              _listenerThread.interrupt();

            */
            // Actually, closing the socket is by itself supposed 
            // to be sufficient to interrupt the thread is it is 
            // in socket.receive.  See page 3 of 
            // file:///D%7C/Depot/j2sdk-1_3_1-doc/
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
                   if(newSktNum != _socket.getLocalPort()) {
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

        } else if (attribute == bufferLength) {
            synchronized(_syncBufferLength) {
                _bufferLength = ((IntToken)
                       (bufferLength.getToken())).intValue();
            }

        } else {
            super.attributeChanged(attribute);
        }

        if (attribute != _evalVar) {
            if (false) System.out.println(this + "attributeChanged() done");
            if (false) System.out.println("---" 
                    + attribute.toString().substring(28));
        }

    }

    /** Fire this actor.  Optionally parse a received datagram into a
     *  token of arbitrary type.  Otherwise, assemble an integer array
     *  token by placing one byte of the datagram into each integer in
     *  the array.  Broadcast the converted token on the output port.
     *  @exception IllegalActionException If the data cannot be
     *  converted into a token of the same type as the configured type
     *  of the output port.
     */
    public void fire() throws IllegalActionException {

        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                trigger.get(i);
            }
        }

        //NOTE: Avoid executing concurrently with thread's run()'s sync block.
        //Synchronize to avoid executing concurrently with thread's run()'s sync block.

        boolean useDefaultOutput;
        synchronized(_syncFireAndThread) {
            int bytesAvailable = 0; // Compiler requires this to be initialized.
            byte[] dataBytes = new byte[0];  // Compiler requires init.

            // If requested, block awaiting a packet (useful in SDF).
            // Actually <u>while</u> requested, not if, in case the 
            // attribute is changed while waiting.
            // Has no effect in DE, since fire() is only called by
            // the director in responce to this actor's thread
            // calling the director's fireAtCurrentTime() method.
            // In the latter case, there is always a packet waiting.
            // An exception to this is if the trigger input is
            // connected.  Then, even in DE, this takes effect.
            while (_blockAwaitingDatagram && 
                    _packetsAlreadyAwaitingFire == 0) {
                try {
                    _fireIsWaiting = true;
                    _syncFireAndThread.wait();
                    _fireIsWaiting = false;
                } catch (InterruptedException ex) {
                    System.out.println(this + "!!fire()'s wait interrupted!!");
                    throw new RuntimeException("!-!");
                }
                if (_stopFire) {
                    _stopFire = false;
                    System.out.println("return due to stopFire");
                    return;
                }
            }

            if (_packetsAlreadyAwaitingFire != 0) {
                useDefaultOutput = false;
                bytesAvailable = _broadcastPacket.getLength();
                dataBytes = _broadcastPacket.getData();//The buffer, not copy.
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
                        if (false) System.out.println(
                                "Broadcast blank 'new Token()'");
                        _outputToken = new Token();
                        // This simplest of tokens is of type 'general'.
                        // It prits on the display actor as 'present'.
                    } else {
                        if (false) System.out.println(
                                "Broadcast non-null parsed token");
                    }
                } else if (_decodeToIntegerArray) {
		    int xs = bytesAvailable%_decodedBytesPerInteger;
		    if (xs != 0) {
			System.out.println(xs + " bytes will be discarded");
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
                        if (false) System.out.println(
                                "Broadcast non-zero length {int}");
                        _outputToken = new ArrayToken(dataIntTokens);
                    } else {
                        // Special case of zero length array:
                        // FIXME - test yuhong's work
                        if (false) System.out.println(
                                "Broadcast zero length {int}");
                        _outputToken = new ArrayToken(BaseType.INT);
                        // WORKS! (in 20Jan tree).  Comment out for 14Dec tree.
                    }
		} else {
		    // No other cases implemented yet.
                }
	    }
            _syncFireAndThread.notifyAll();
        } // sync

        if (!useDefaultOutput) {
            output.broadcast(_outputToken);
        } else {
            // No fresh data, so use the default output.
            // (If repeat parameter were implemented, 
            //  would test it here and, if true, would 
            //  repeat the most recent output instead 
            //  of outputting the default, provided 
            //  there is a previous output to repeat.)

            // Ensure that any change to the default output parameter 
            // occurs atomically with respect to its use here.
            if (false) System.out.println(
                    "Broadcast default output");
            synchronized (_syncDefaultOutput) {
                output.broadcast(_defaultOutputToken);
            }
        }

    }

    /** Preinitialize this actor.  Create a new datagram socket and
     *  initialize the thread that reads from the socket.  The thread
     *  will stay alive until the socket is closed.
     *  @exception IllegalActionException If the
     *  <i>localSocketNumber</i> parameter has a value of -1, or a
     *  socket could not be created.
     *  @exception NameDuplicationException Not throw in this base class.
     */
    public void preinitialize() throws IllegalActionException /* ,
            NameDuplicationException */ {
        super.preinitialize();

        if (false) System.out.println(this + "preinitialize has begun");

        // This is a key fix.  Programs in DE (such as 1plusFxC.xml)
        // Used to only run the first time after opening the XML!
        _packetsAlreadyAwaitingFire = 0;

        // REDUNDENT since AtCh gets called for everything
        //_overwrite = ((BooleanToken)(overwrite.getToken())).booleanValue();
        //_blockAwaitingDatagram = ((BooleanToken)
        //        (blockAwaitingDatagram.getToken())).booleanValue();

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

        // If the port number is -1, then the actor is assumed to be in the
        // library.   If the actor is in the library then we do not want
        // to open the socket or start a new thread.
        int portNumber = ((IntToken)(localSocketNumber.getToken())).intValue();
        if (portNumber == -1) {
            if(false&&_debugging) _debug("Can't run with port = -1");
            throw new IllegalActionException(this, "Cannot run w/ port = -1");
        }
        if (false) System.out.println(this + "portNumber = " + portNumber);
        // Allocate a new socket.
        try {
            if(false&&_debugging) {
                _debug("Trying to create a new socket on port " + portNumber);
            }
            _socket = new DatagramSocket(portNumber);
            if(false&&_debugging) {
                _debug("Socket created successfully!");
            }
        }
        catch (SocketException ex) {
            throw new IllegalActionException(this,
                    "Failed to create a new socket:" + ex);
        }

        // Set up the buffers.
        _bufferLength = ((IntToken)(bufferLength.getToken())).intValue();
        _receivePacket = new DatagramPacket(
                new byte[_bufferLength], 0, _bufferLength);
        _broadcastPacket = new DatagramPacket(
                new byte[_bufferLength], 0, _bufferLength);

        // Allocate & start a thread to read from the socket.
        _listenerThread = new ListenerThread();
        _listenerThread.start();
        if(false) System.out.println("Thread created & started.");

        if (true) System.out.println("preinitialize ends");
        if (true) System.out.println("------------------");
        
    }

    /** Stop fire of this actor, interrupting if blocked.
     */
    public void stopFire() {
        if (true) System.out.println("stopFire() is called");
        synchronized (_syncFireAndThread) {
            if (_fireIsWaiting) {
                // stopFire() gets called alot.  Including each time 
                // the program is started.  This caveat has proven 
                // necessary to avoid disrupting one of the first 
                // few firings.
		_stopFire = true;
                _syncFireAndThread.notifyAll();
                System.out.println("stopFire() notified fire()");
            } else {
                System.out.println("stopFire() did not notify fire()");
            }
        }
    }

    /** Wrapup execution of this actor.  Interrupt the thread that was
     *  created to read from the socket and close the socket.
     *  @exception IllegalActionException If the thread or the socket
     *  was not created.
     */
    public void wrapup() throws IllegalActionException {

        if (true) System.out.println("WRAPUP IS CALLED");

        //System.err.println("wrapup() has been called in " + this);
        //e.printStackTrace();
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
            if (true) System.out.println("listenerThread null at wrapup!?");
            //throw new IllegalActionException(
            //        "listenerThread null at wrapup!?");
        }

        if (_socket != null) {
            _socket.close();
            _socket = null;
        } else {
            if (true) System.out.println("Socket null at wrapup!?");
            //throw new IllegalActionException("Socket null at wrapup!?");
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // FIXME: these need indivdual comments

    // Synchronization objects.  Used only for synchronization.
    private Object _syncFireAndThread = new Object();
    private Object _syncDefaultOutput = new Object();
    private Object _syncBufferLength = new Object();
    private Object _syncSocket = new Object();

    // Cashed copies of parameters:
    private int _bufferLength;
    private boolean _overwrite;
    private boolean _blockAwaitingDatagram;
    private Token _defaultOutputToken;

    private String _encoding = new String("");
    private boolean _decodeWithPtolemyParser;
    private boolean _decodeToIntegerArray;
    private int _decodedBytesPerInteger;

    // Packet buffer info.  Allocated lengths need to be kept track of
    // separately because the .getLength() method returns the length
    // of the contained data, not the (typically longer) length
    // allocated to the data buffer.  FIXME - could I do
    // .getData().Length() to get the byte array's lentgth?
    private DatagramPacket _receivePacket = null;
    private DatagramPacket _broadcastPacket = null;
    private int _receiveAllocated = 0;
    private int _broadcastAllocated = 0;

    // Misc.
    private boolean _fireIsWaiting = false;
    private int _packetsAlreadyAwaitingFire = 0;
    private DatagramSocket _socket;
    private ListenerThread _listenerThread;
    private int _listenerReturnSite;
    private Variable _evalVar;
    private Token _outputToken;
    private boolean _inReceive = false;
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
                        System.out.println("--!!--" + (_socket == null));
                        System.out.println(ex.toString());
                        throw new RuntimeException("-null ptr-");
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

                if(fireAtWillBeCalled) {
                    try {
                        getDirector().fireAtCurrentTime(
                                DatagramReceiver.this);
                    } catch (IllegalActionException ex) {

                        System.out.println(this 
                                + "!!thread catch fireAtCurrentTime!!");
                        throw new RuntimeException("-fireAt* catch-");
                    }
                }

            } // Close while(true)
        } // Close run()

    } // Close private class ListenerThread

} // Close the whole java file




