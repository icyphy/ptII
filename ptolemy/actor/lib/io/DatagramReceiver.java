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
import ptolemy.data.expr.Variable;
import ptolemy.domains.sdf.kernel.SDFIOPort;
import ptolemy.actor.lib.*;

import java.io.*;
import java.net.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// DatagramReceiver
/**
This actor receives datagram packets via a separate thread it creates.
Datagrams are open-loop internet communications.  Each contains data
plus a kind of return address.  Datagrams use the UDP protocol under
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

The data portion of the packet is brodcast as an array of bytes.  The
remote address and socket number are each broadcast as an int.
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
If true, it has the actor discard the queued packet in favor of the
new one.  If false, the new packet is queued behind the existing one.
In the latter case, both buffers are now full.  The thread then waits
for fire() to consume a queued packet before it stalls again awaiting
the next.  In all other cases (<i>overwrite</i> true or no queued
packets) the thread immidiately stalls to await the next packet.<p>

FIXME - Implement the <i>repeat</i> parameter as described below.  The
<i>repeat</i> parameter applies to the eager director case.  If
<i>repeat</i> is true, additional calls to fire() cause rebroadcast of
the most recent data and return-address information.  When fire() is
called before the first packet has arrived, default data are
broadcast.  If <i>repeat</i> is false, then the extra call to fire()
result in no extra broadcast.  FIXME-Can I just not broadcast?  Would
it be better to have prefire() return false instead?  <p>

This actor has a parameter <i>localSocketNumber</i> for the port
number assigned to the local datagram socket.  Initially, the local
socket number is set to -1 to indicate no socket at all (leave
socket=null).  FIXME - This -1 business is no good if the actor were
within a composite actor that was itself within the library.
Currently, this actor allocates the socket in preinitialize().  It
needs to do it before the model runs but after it leaves the library.
This may require a redifinition of when [pre]initialize() methods get
called.  <p>

Here's a very useful command for use when working with this actor.
Bash command netstat -an is very useful in seeing current port allocations!  
<p>

@author Winthrop Williams, Jorn, Xiojun, Edward Lee
(Based on TiltSensor actor written
   by Chamberlain Fong, Xiaojun Liu, Edward Lee)
@version $Id$ */
public class DatagramReceiver extends TypedAtomicActor {

    public DatagramReceiver(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        localSocketNumber = new Parameter(this, "localSocketNumber");
        localSocketNumber.setTypeEquals(BaseType.INT);
        localSocketNumber.setToken(new IntToken(-1));
        overwrite = new Parameter(this, "overwrite", new BooleanToken(true));
        overwrite.setTypeEquals(BaseType.BOOLEAN);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** This port outputs the data in the packet.
     */
    public TypedIOPort output;
    
    /** The local port number for this actor's socket.
     */
    public Parameter localSocketNumber;
    
    /** Boolean directive in case datagrams pile up.
     */
    public Parameter overwrite;
    
    /** Parameter directing whether to use the Ptolemy parser
     * to interpret the datagram contents or whether to copy
     * the raw data to an output data type (or use 'serialize'?)
     */
    //public Parameter ...;
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    
    /** React to the change of the given acttribute.
     *  If the parameter changed is <i>localSocketNumber</i> 
     *  and the model is running
     *  (i.e. socket != null), then replace the
     *  current socket with a socket on the new number.  This
     *  involves interrupting the thread reading from the socket, 
     *  creating a new
     *  socket, and restarting the thread.  This is done even if the
     *  port number has not actually changed.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // Cache the overwrite parameter so the reading thread can use it.
        //FIXME - Check that writing to _overwrite is atomic.
        //Otherwise add synchronized(_broadcastPacket){...}
        if (attribute == overwrite) {
            _overwrite = ((BooleanToken)(overwrite.getToken())).booleanValue();
        } else if (attribute == localSocketNumber) {
            // Sync handles concurrent calls by director.
            // FIXME Why does the director do that?
            synchronized(this) {
                if ( socket != null ) {
                    
                    _listenerThread.interrupt();
                    
                    if(_debugging) _debug("Current port number is " +
                                socket.getLocalPort());
                    socket.close();

                    int portNum = ((IntToken)
                                (localSocketNumber.getToken())).intValue();
                    if(_debugging) _debug("New port number is " + portNum);
                    try {
                        if(_debugging) _debug("Try creating socket " 
                                + portNum);
                        socket = new DatagramSocket(portNum);
                        if(_debugging) _debug("A socket is created!!");
                    }
                    catch (SocketException ex) {
                        throw new InternalErrorException(
                                KernelException.stackTraceToString(ex));
                    }
                    
                    _listenerThread.start();
                    
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    /** Fire this actor.  Parse a received datagram and convert it
     *  into a token that has the same type as the output port.  
     *  Broadcast the converted token on the output port.
     *  @exception IllegalActionException If the data cannot be 
     *  converted into a token.
     */
    public void fire() throws IllegalActionException {
        if(_debugging) _debug("Actor is fired");
        
        // this line stalls when __ vs ___
        //InetAddress __address = ___packet.getAddress();
        //String _address = __address.getHostAddress();
        
        // NOTE: Avoid executing concurrently with thread's run()'s sync block.
        synchronized(_broadcastPacket) {
            // Get the data out of the packet as a string of data's length
            _length = _broadcastPacket.getLength();
            _dataStr = new String(_broadcastPacket.getData(), 0, _length);
            packetsAlreadyAwaitingFire--;
        }
        
        // Parse this data string to a Ptolemy II data object
        _evalVar.setExpression(_dataStr);
        output.broadcast(_evalVar.getToken());
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

        _overwrite = ((BooleanToken)(overwrite.getToken())).booleanValue();

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
            if(_debugging) _debug("Can't run with port = -1");
            throw new IllegalActionException(this, "Cannot run w/ port = -1");
        }

        // Allocate a new socket.
        try {
            if(_debugging) {
                _debug("Trying to create a new socket on port " + portNumber);
            }
            socket = new DatagramSocket(portNumber);
            if(_debugging) {
                _debug("Socket created successfully!");
            }
        }
        catch (SocketException ex) {
            throw new IllegalActionException(this,
                    "Failed to create a new socket:" + ex);
        }
        
        // Allocate a thread to read from the socket.
        _listenerThread = new ListenerThread();
        _listenerThread.start();
        if(_debugging) _debug("Socket-reading thread created & started.");
    }
    
    
    /** Wrapup execution of this actor.  Interrupt the thread that was
     *  created to read from the socket and close the socket.
     *  @exception IllegalActionException If the thread or the socket
     *  was not created.
     */
    public void wrapup() throws IllegalActionException {
        if (_listenerThread != null) {
            _listenerThread.interrupt();
            _listenerThread = null;
        } else {
            System.out.println("listenerThread null at wrapup!?");
            //throw new IllegalActionException(
            //        "listenerThread null at wrapup!?");
        }
        
        if (socket != null) {
            socket.close();
            socket = null;
        } else {
            System.out.println("Socket null at wrapup!?");
            //throw new IllegalActionException("Socket null at wrapup!?");
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // FIXME: these need indivdual comments
    private DatagramPacket _receivePacket =
             new DatagramPacket(new byte[440], 0, 440);
    private DatagramPacket _broadcastPacket =
             new DatagramPacket(new byte[440], 0 ,440);
    private int packetsAlreadyAwaitingFire = 0;
    private boolean _overwrite;
    private DatagramSocket socket;
    private ListenerThread _listenerThread;
    private Variable _evalVar;
    private String _dataStr;
    private int _length;
    
    ///////////////////////////////////////////////////////////////////
    ////                        private inner class                ////
    
    private class ListenerThread extends Thread {
        /** Create a new thread to listen for packets at the socket
         * opened by the preinitialize method.
         */
        public ListenerThread() {
        }
        
        public void run() {
            while (true) {
                if (this.interrupted()) {
                    return;
                }
                try {
                    if(_debugging) _debug("attempt socket.receive");
                    // NOTE: The following call may block.
                    socket.receive(_receivePacket);
                    if(_debugging) _debug("receive unblocked!");
                } catch (IOException ex) {
                    if(_debugging) _debug("receive IOException in thread.");
                    return;
                }
                
                // NOTE: Avoid executing concurrently with actor's fire().
                // 'DatagramReceiver.this' replaced with '_broadcastPacket'
                synchronized(_broadcastPacket) {
                    
                    // There are 2 datagram packet buffers.
                    
                    // If no data is already in the actor awaiting fire,
                    // then swap the buffers, increment the awaiting count,
                    // and call fireAt() ( or fireAtCurrentTime() ).
                    
                    // Else, data is already waiting.  I this case look
                    // to the overwrite parameter for guidance.
                    // If overwrite it true, then go ahead and swap anyway
                    // but do not increment the count and do not call 
                    // fireAt() again.
                    
                    // If data is waiting AND overwrite is false, then
                    // don't prepare to call socket.receive again.
                    // Instead, wait() in the synchronized section to block
                    // awaiting a call to fire and entry of its synched
                    // section.  When it exits its synched section, then
                    // complete this synched section which swaps buffers,
                    // increments the count, and calls fireAt().  Then
                    // go back around finally to the socket.receive call.
                    
                    if(packetsAlreadyAwaitingFire != 0 && !_overwrite) {
                        try {
                            wait();
                        } catch (InterruptedException ex) {
                            //I'm here because I've been asked to not 
                            //lose packets.  So, if interrupted in here
                            //I'd like to be able to still wait.
                            //FIXME - how to still wait while socket 
                            //number attribute is being changed?
                            System.out.println("InterrExcept in wait()");
                        }
                    }
                    
                    // Swap the memory areas the packet pointers ref.
                    DatagramPacket tmp = _broadcastPacket;
                    _broadcastPacket = _receivePacket;
                    _receivePacket = tmp;
                    
                    if(packetsAlreadyAwaitingFire == 0) {
                        // Increment count & call fireAt()
                        packetsAlreadyAwaitingFire++;
                        try {
                            getDirector().fireAt(DatagramReceiver.this,
                                    getDirector().getCurrentTime());
                        } catch (IllegalActionException ex) {
                            if(_debugging) _debug("IAE 0!!");
                        }
                    }
                    
                } // Close the Synchronized block
            } // Close the While
        }
    }
}




