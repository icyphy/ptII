/* A port for use by actors specialized to the DE domain.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (lmuliadi@eecs.berkeley.edu)

*/

package ptolemy.domains.de.kernel;

import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// DEIOPort
/**
This port can be used by actors that are specialized to the discrete-event
(DE) domain. It supports annotations that inform the scheduler about delays
and about priorities for handling simultaneous inputs. It also provides
two additional methods, overloaded versions of broadcast() and send().
The overloaded versions have a second argument for the time delay,
allowing actors to send output data with a time delay (relative to current
time).
<p>
Actors in the DE domain are not required to use this port. If they use
the base class, TypedIOPort, then the data they send is sent with zero delay.

@authors Lukito Muliadi, Edward A. Lee
@version $Id$
*/
public class DEIOPort extends TypedIOPort {

    /** Construct a DEIOPort with no container and no name that is
     *  neither an input nor an output.
     */
    public DEIOPort() {
        super();
    }

    /** Construct a DEIOPort with the specified container and name
     *  that is neither an input nor an output.  The specified container
     *  must implement the Actor interface, or an exception will be thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public DEIOPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
    }

    /** Construct a DEIOPort with the specified container and name that is
     *  either an input, an output, or both, depending on the third
     *  and fourth arguments. The specified container must implement
     *  the Actor interface or an exception will be thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @param isinput True if this is to be an input port.
     *  @param isoutput True if this is to be an output port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public DEIOPort(ComponentEntity container, String name,
            boolean isinput, boolean isoutput)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, isinput, isoutput);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Assert that this port has higher priority than the port in the
     *  argument.  The scheduler uses this information to ensure that
     *  if this port and the argument are to receive simultaneous events,
     *  then the event at this port will trigger a firing first (or
     *  both events might be made visible in the same firing).
     *
     *  @param otherport Another input port
     *  @exception IllegalActionException If this port or the argument port
     *   is not an input.
     */
    public void before(IOPort otherport) throws IllegalActionException {
        if (!isInput() || !otherport.isInput()) {
            throw new IllegalActionException(this,
                    "Invalid before relationship.  " + 
                    "Must be input before input.");
        }
        _beforeList.insertLast(otherport);
    }

    /** Return an enumeration of the other input ports that have lower
     *  priority than this one, as asserted by the before() method.
     *  @return An enumeration of TypedIOPorts.
     */
    public Enumeration beforePorts() {
        return _beforeList.elements();
    }

    /** Broadcast a token to all receivers connected to this output
     *  port with the specified time delay.  The time stamp of
     *  of the token is equal to current time plus the specified delay.
     *
     *  @param token The token to send.
     *  @param delay The time stamp of the token being broadcast.
     *  @exception IllegalActionException If the port is not an output.
     */
    public void broadcast(Token token, double delay)
            throws IllegalActionException {
        _delay = delay;
        try {
            broadcast(token);
        } finally {
            _delay = 0.0;
        }
    }

    /** Override the base class to use the delay that may have been set
     *  by calling the overloaded version of send() that takes a delay
     *  argument, or the similarly overloaded version of broadcast().
     *  After setting the delay in all the receivers, the base class
     *  send() method is invoked.
     *
     *  @param channelindex The index of the channel, from 0 to width-1
     *  @param token The token to send
     *  @exception IllegalActionException If the port is not an output,
     *   or if the index is out of range, or if the token to be sent cannot
     *   be converted to the type of this port, or if the token is null.
     *  @exception NoRoomException If there is no room in the receiver.
     *   This should not occur in the DE domain.
     */
    public void send(int channelindex, Token token)
            throws IllegalActionException, NoRoomException {
        try {
            workspace().getReadAccess();
            Receiver[][] fr = getRemoteReceivers();
            if (fr == null) return;
            if (channelindex >= fr.length || channelindex < 0) {
                throw new IllegalActionException(this,
                "send: channel index is out of range.");
            }
            if (fr[channelindex] == null) return;
            for (int j = 0; j < fr[channelindex].length; j++) {
                try {
                    ((DEReceiver)fr[channelindex][j]).setDelay(_delay);
                } catch (ClassCastException e) {
                    throw new InvalidStateException("DEIOPort.send() " +
                    "expects to connect to receivers of type DEReceiver.");
                }
            }
            super.send(channelindex, token);
        } finally {
            workspace().doneReading();
        }
    }

    /** Send a token with the specified time delay to the receivers connected
     *  on the specified channel.  The time stamp of
     *  of the token is equal to current time plus the specified delay.
     *
     *  @param channelindex The index of the channel, from 0 to width-1.
     *  @param token The token to send.
     *  @param delay The time delay of the token being sent.
     *  @exception IllegalActionException If the port is not an output,
     *   or if the index is out of range.
     */
    public void send(int channelindex, Token token, double delay)
            throws IllegalActionException {
        _delay = delay;
        try {
            send(channelindex, token);
        } finally {
            _delay = 0.0;
        }
    }

    /** Add the specified port to the list of output ports that may
     *  have zero-delay outputs triggered by this input port.
     *  @param output The output port that may be triggered.
     *  @exception IllegalActionException If this port is not an input,
     *   or if the argument is not an output port.
     */
    public void triggers(IOPort output) throws IllegalActionException {
        if (!isInput() || !output.isOutput()) {
            throw new IllegalActionException(this,
                    "Invalid triggering relationship.  " +
                    "Must be input triggers output.");
        }
        _triggerList.insertLast(output);
    }

    /** Return an enumeration of the output ports that are triggered by
     *  this input port.  I.e., an event at this input port may cause
     *  an immediate (zero-delay) event at any of these output ports.
     */
    public Enumeration triggersPorts() {
        return _triggerList.elements();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // List of ports with lower priority than this one.
    // I.e., events at this port should be triggered before those at ports
    // in this list.
    private LinkedList _beforeList = new LinkedList();

    // List of ports triggered immediately by this input port.
    private LinkedList _triggerList = new LinkedList();

    // The delay to use in transfering tokens.
    // Be careful to set this back to zero after using it.
    private double _delay = 0.0;
}
