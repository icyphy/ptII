/* An actor that receives a Datagram packet.

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
This actor creates a separate thread which stalls, awaiting reception of a
datagram packet.  That thread calls the director's fireAt (or eventually
fireAtCurrentTime) method each time it receives a packet, unless it has
to discard one to make room.  In that case, the new packet and
the packet(s) already queued within the actor may be reordered, but no
additional fireAt call is made because the number of packets awaiting
broadcast has not changed.
<p>
The fire() method assumes data is present to be broadcast.  When it copies
the data from the packet buffer in preparation for broadcast, it
decrements the count of queued packets.
<p>
The part of the fire method that copies data from the packet buffer or
parses it in place (if that's what the Ptolemy parser does) is
synchronized so that it cannot execute concurrently with the thread's code
for reordering the buffers and choosing whether and how to reorder them
(since such choice may depend on the number of queued packets).

There are two buffers for packet reception.

When fired, the actor broadcasts the packet's data but discards the 'from'
address and port information

This actor has a parameter 'port' for the local port number assigned to its
socket.

Bash command netstat -an is very useful in seeing current port allocations!

Initially, the local port number is set to -1 to indicate no port at all.

@author Winthrop Williams, Jorn, Xiojun, Edward Lee
(Based on TiltSensor actor written
   by Chamberlain Fong, Xiaojun Liu, Edward Lee)
@version $Id$
*/
public class DatagramReceiver extends TypedAtomicActor {

    public DatagramReceiver(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        localPort = new Parameter(this, "localPort");
        localPort.setTypeEquals(BaseType.INT);
        localPort.setToken(new IntToken(-1));
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
    public Parameter localPort;

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
     *  If the parameter changed is <i>localPort</i> and the model is running
     *  (i.e. socket != null), then replace the
     *  current socket with a socket on the new port number.  This
     *  involves pausing the thread reading from the socket, creating a new
     *  socket and restarting the thread.  This is done even if the
     *  port number has not actually changed.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // Cache the overwrite parameter so the reading thread can use it.
        if (attribute == overwrite) {
            _overwrite = ((BooleanToken)(overwrite.getToken())).booleanValue();
        } else if (attribute == localPort) {
            if ( socket != null ) {

                _listenerThread.interrupt();

                if(_debugging) _debug("Current port number is " +
                        socket.getLocalPort());
                socket.close();

                int portNum = ((IntToken)(localPort.getToken())).intValue();
                if(_debugging) _debug("New port number is " + portNum);
                try {
                    if(_debugging) _debug("Try creating socket " + portNum);
                    socket = new DatagramSocket(portNum);
                    if(_debugging) _debug("A socket is created!!");
                }
                catch (SocketException ex) {
                    throw new InternalErrorException(KernelException
                            .stackTraceToString(ex));
                }

                _listenerThread.start();

            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Fire this actor.  Parse a received datagram and convert it
     *  into a token that has the same type as the output port.  Broadcast the
     *  converted token on the output port.
     *  @exception IllegalActionException If the data cannot be converted into
     *  a token.
     */
    public void fire() throws IllegalActionException {
        if(_debugging) _debug("Actor is fired");

        // this line stalls when __ vs ___
        //InetAddress __address = ___packet.getAddress();
        //String _address = __address.getHostAddress();

        // NOTE: Avoid executing concurrently with thread's run()'s sync block.
        synchronized(this) {
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
     *  @exception IllegalActionException If the <i>localPort</i> parameter
     *  has a value of -1, or a socket could not be created.
     *  @exception NameDuplicationException Not throw in this base class.
     */
    public void preinitialize() throws IllegalActionException,
            NameDuplicationException {
        super.preinitialize();

        _overwrite = ((BooleanToken)(localPort.getToken())).booleanValue();

        Variable var = (Variable)getAttribute("_evalVar");
        if (var == null) {
            var = new Variable(this, "_evalVar");
        }
        _evalVar = var;

        // If the port number is -1, then the actor is assumed to be in the
        // library.   If the actor is in the library then we do not want
        // to open the socket or start a new thread.
        int portNumber = ((IntToken)(localPort.getToken())).intValue();
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
        _listenerThread = new ListenerThread(this);
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
            throw new IllegalActionException(
                    "listenerThread null at wrapup!?");
        }

        if (socket != null) {
            socket.close();
            socket = null;
        } else {
            throw new IllegalActionException("Socket null at wrapup!?");
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
                synchronized(DatagramReceiver.this) {

                    // There are 2 datagram packet buffers.

                    // If no data is already in the actor awaiting fire,
                    // then swap the buffers, increment the awaiting count,
                    // and call fireAt() ( or fireAtCurrentTime() ).

                    // Else, data is already waiting.  I this case look
                    // to the overwrite parameter for guidance.
                    // If overwrite it true, then go ahead and swap anyway
                    // but do not increment the count or call fireAt()
                    // again.

                    // If data is waiting AND overwrite is false, then
                    // don't prepare to call socket.receive again.
                    // Instead, exit the synchronized section, then block
                    // awaiting a call to fire and entry of its synched
                    // section.  When it exits its synched section, then
                    // enter a synched section here which swaps buffers,
                    // increments the count, and calls fireAt().  Then
                    // go back around finally to the socket.receive call.

                    if(packetsAlreadyAwaitingFire == 0 || _overwrite) {
                        // Swap the memory areas the packet pointers ref.
                        DatagramPacket tmp = _broadcastPacket;
                        _broadcastPacket = _receivePacket;
                        _receivePacket = tmp;
                    }

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

                    // FIXME Need not be separate if, could be else of || above.
                    if(packetsAlreadyAwaitingFire != 0 && !_overwrite) {
                        // FIXME I don't know how to await next exit by
                        // fire() from its synchronized section.  So I'll
                        // just overwrite the most recent packet by doing
                        // nothing and just calling socket.receive again.
                        if(_debugging) _debug("Overwriting latest packet.");
                    }
                }
            }
        }
    }
}
