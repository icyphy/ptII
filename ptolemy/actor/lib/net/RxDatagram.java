/* An actor that trivially receives a Datagram packet.

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
import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// RxDatagram
/**
This actor creates a separate thread which stalls, awaiting reception
of a datagram packet.  That thread calls the director's fireAt method
each time it receives a packet.  The resulting firing broadcasts the
packet's 'from' address out into the Ptolemy model.  The data and
'from' port info. are discarded.

<p>This actor has a parameter 'port' for the local port number assigned
to its socket.

<p>The bash command netstat -an is very useful in seeing current port
allocations!

<p>Initially, the local port number is set to -1 to indicate no port at all.

@author Winthrop Williams, Joern Janneck, Xiaojun Liu, Edward A. Lee
(Based on TiltSensor actor writen by Chamberlain Fong, Xiaojun Liu, Edward Lee)
@version $Id$
*/
public class RxDatagram extends TypedAtomicActor {
    // FIXME: should this extend Sink?

    public RxDatagram(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        remoteAddress = new TypedIOPort(this, "remoteAddress");
        remoteAddress.setOutput(true);
        remoteAddress.setTypeEquals(BaseType.STRING);
        port = new Parameter(this, "port");
        port.setTypeEquals(BaseType.INT);
        port.setToken(new IntToken(-1));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** This port outputs the remote 'from' address the packet came from.
     */
    public TypedIOPort remoteAddress;

    /** The local port number for this actor's socket.
     */
    public Parameter port;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** If the parameter changed is <i>port</i>, then if the model
     * is running (as evedenced by socket != null) then interrupt
     * thread & close socket, and then and reopen with new port number
     * & restart thread (even if same as old port number).  Thread is
     * not reinstantiated, just restarted on the new socket.
     * FIXME: @param?  @exception?
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == port) {
            if ( socket != null ) {

                _listenerThread.interrupt();

                System.out.println("Current port number is " +
                        socket.getLocalPort());
                socket.close();

                int portNum = ((IntToken)(port.getToken())).intValue();
                System.out.println("New port number is " + portNum);
                try {
                    System.out.println("Try creating socket " + portNum);
                    socket = new DatagramSocket(portNum);
                    System.out.println("A socket is created!!");
                }
                catch (SocketException ex) {
                    /* ignore */
                    ex.printStackTrace();
                    throw new InternalErrorException("socket exception");
                }

                _listenerThread.start();

            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Broadcasts the return address of the packet received over the Ethernet.
     * FIXME: @exception?
     */
    public void fire() throws IllegalActionException {
        System.out.println("Actor is fired");

        InetAddress __address = ___packet.getAddress();
        // this line stalls when __ vs ___

        String _address = __address.getHostAddress();
        remoteAddress.broadcast(new StringToken(_address));
    }

    /** Preinitialize
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        int portNum = ((IntToken)(port.getToken())).intValue();
        if (portNum == -1) {
            System.out.println("Can't run with port = -1");
            throw new IllegalActionException("Cannot run w/ port = -1");
            // *** sysout works but IAE does nothing
            // *** (in presence of wrapup exception anyway)
        }
        try {
            System.out.println("PI Try to create socket for Wport " + portNum);
            socket = new DatagramSocket(portNum);
            System.out.println("PI socket created!!");
        }
        catch (SocketException ex) {
            /* ignore */
            ex.printStackTrace();
            throw new InternalErrorException("PI can't create socket");
        }

        _listenerThread = new ListenerThread(this);
        _listenerThread.start();
        System.out.println("PI thread created & started");
    }


    /** Wrap up
     * FIXME: what does it do?
     * FIXME: @exception?
     */
    public void wrapup() throws IllegalActionException {
        if (_listenerThread != null) {
            _listenerThread.interrupt();
            _listenerThread = null;
        } else {
            throw new IllegalActionException("listenerThread null "
                    + "at wrapup!?");
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

    // FIXME: private variables need comments

    // Variables used
    private DatagramPacket __packet;
    // FIXME: why two underscores?  Consider a different name
    private DatagramPacket ___packet;
    private DatagramSocket socket;
    private ListenerThread _listenerThread;


    // Variables maybe or maybe not used
    private byte _data;
    private int _length;
    private String _address;
    // FIXME: why two underscores?  Consider a different name
    private InetAddress __address;
    private int _port;

    private class ListenerThread extends Thread {
        /** Create a new thread to listen for packets at the socket
         * opened by the preinitialize method.
         */
        public ListenerThread(Actor _thisThreadsActor) {
            thisThreadsActor = _thisThreadsActor;
        }

        public void run() {
            byte _buf[] = {'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',
                    'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E'};
            int _offset = 0;
            while (true) {
                if (_offset == 0) {
                    _offset = 200;
                } else {
                    _offset = 0;
                }
                __packet = new DatagramPacket(_buf, _offset, 200);
                try {
                    System.out.println("attempt socket.receive");
                    socket.receive(__packet);
                    System.out.println("receive unblocked!");
                    ___packet = __packet;
                } catch (IOException ex) {
                    System.out.println("IOException in thread.");
                }

                /*
                } catch (Exception ex) {
                    getManager().notifyListenersOfException(ex);
                } finally {
                    System.out.println("Thread Terminating :)");
                }
                */

                try {
                    getDirector().fireAt(thisThreadsActor,
                            getDirector().getCurrentTime() + 0.0);
                } catch (IllegalActionException ex) {
                    System.out.println("Illegal Act. Ex. in thread!!");
                } catch (NullPointerException ex) {
                    System.out.println("Oh no! Null pointer exception!!");
                    // NPE had been due to not copyingin thisThreadsActor.
                }
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////

        private Actor thisThreadsActor;
    }
}
