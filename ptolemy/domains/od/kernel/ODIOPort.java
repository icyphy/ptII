/* A timed input/output port used in the OD domain.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.domains.od.kernel;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// ODIOPort
/** 
An ODIOPort is a timed input/output port used in the OD domain. ODIOPorts
are used to send tokens between ODActors, and in so doing, time is 
associated with the tokens as they are placed in ODReceivers. The 
association of time with a token involves the use of an Event which is
then stored in a ODReceiver.

BEGIN FIXME 
       There are some critical semantic differences between actors that
       use ODIOPorts and polymorphic ports which use regular IOPorts.
       The key difference is that IOPort.send() sets the timestamp of the
       token to the rcvrTime of the receiving actor's receiver. ODIOPort.send()
       sets the timestamp of the token to the minimum rcvrTime of the
       sending actor. 
       
       What I should do is set the timestamp of the token to the current
       time of the sending actor.
END FIXME 

In addition to the specification of time values, each ODIOPort has a 
(integer) priority. If an ODIOPort is an input, its priority is used 
relative to the priorities of other input ODIOPorts for a given ODActor 
to determine how receivers are selected when there are pending events 
with simultaneous times. The receivers of ODIOPorts with higher priorities 
are selected first in situations involving simultaneous event times.


@author John S. Davis II
@version @(#)ODIOPort.java	1.5	11/16/98
@see ptolemy.domains.od.kernel.Event
*/

public class ODIOPort extends IOPort {

    /** Construct an ODIOPort with no container and no name that is
     *  neither an input nor an output.
     */
    public ODIOPort() {
        super();
    }
    
    /** Construct an ODIOPort with a containing actor and a name
     *  that is neither an input nor an output.  The specified container
     *  must implement the Actor interface, or an exception will be thrown.
     *
     * @param container The container actor.
     * @param name The name of the port.
     * @exception IllegalActionException If the port is not of an acceptable
     *  class for the container, or if the container does not implement the
     *  Actor interface.
     * @exception NameDuplicationException If the name coincides with
     *  a port already in the container.
     */
    public ODIOPort(ComponentEntity container, String name) 
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
    
    /** Construct an ODIOPort with a container and a name that is
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
    public ODIOPort(ComponentEntity container, String name,
            boolean isinput, boolean isoutput) 
            throws IllegalActionException, NameDuplicationException {
        super(container, name, isinput, isoutput);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send a token to all connected receivers. If there are no 
     *  connected receivers, then nothing is sent. The time stamp 
     *  associated with this token will be the current time of the 
     *  actor which contains this port.
     *
     * @param token The token to send
     * @exception IllegalActionException If the port is not an output.
     * @exception NoRoomException If a send to one of the channels throws
     *  it.
     */
    public void broadcast(Token token) 
            throws IllegalActionException, NoRoomException {
        broadcast( token, ((ODActor)getContainer()).getCurrentTime() );
    }
            
    /** Send a token to all connected receivers. If there are no connected
     *  receivers, then nothing is sent. The time stamp associated with 
     *  this token will be the current time of the actor which contains 
     *  this port plus the specified delay.
     *
     * @param token The token to send
     * @param delay The delay from the containing actors current time.
     * @exception IllegalActionException If the port is not an output or
     *  the delay is negative.
     * @exception NoRoomException If a send to one of the channels throws
     *  it.
     */
    public void broadcast(Token token, double delay) 
            throws IllegalActionException, NoRoomException {
        if( delay < -1.0 ) {
            throw new IllegalActionException( this, "Negative delay "
                    + "values are not allowed.");
        }
        try {
            workspace().getReadAccess();
            if (!isOutput()) {
                throw new IllegalActionException(this,
                        "broadcast: Tokens can only be sent from an " +
                        "output port.");
            }
            Receiver fr[][] = getRemoteReceivers();
            if(fr == null) {
                return;
            }

            for (int j = 0; j < fr.length; j++) {
                double currentTime = ((ODActor)getContainer()).getCurrentTime();
                send(j, token, currentTime + delay);
            }
        } finally {
            workspace().doneReading();
        }
    }
   
    /** Return the priority associated with this port.
     * @return int The priority of this port.
     */
    public int getPriority() {
        return _priority;
    }
    
    /** Set the priority associated with this port. If this is an input
     *  port, the priority will be passed to the contained receiver's of 
     *  this port and will be used to determine how receivers with 
     *  simultaneous events are dealt with. Greater priorities get 
     *  preference over lower priorities. Priority is measured with 
     *  respect to the priority of other input ports associated with the 
     *  containing actor of this port. 
     * @param int The priority of this port.
     */
    public void setPriority(int priority) {
        _priority = priority;
    }
    
    /** Send the specified token to all receivers connected to the
     *  specified channel.  The first receiver gets the actual token,
     *  while subsequent ones get a clone.  If there are no receivers,
     *  then do nothing. The current time of the containing actor of 
     *  this port will be associated with the token.
     * 
     * @param channel The index of the channel, from 0 to width-1.
     * @param token The token to send.
     * @exception NoRoomException If there is no room in the receiver.
     * @exception IllegalActionException If the port is not an output or if
     *  the index is out of range.
     */
    public void send(int channel, Token token)
            throws InvalidStateException, IllegalActionException  {
        // send( channel, token );
        send( channel, token, ((ODActor)getContainer()).getCurrentTime() );
    }
            
    /** Send the specified token to all receivers connected to the
     *  specified channel. The first receiver gets the actual token,
     *  while subsequent ones get a clone.  If there are no receivers,
     *  then do nothing. The current time of the containing actor of 
     *  this port plus the specified delay will be associated with the token.
     * 
     * @param channel The index of the channel, from 0 to width-1.
     * @param token The token to send.
     * @param delay The delay from the containing actors current time.
     * @exception NoRoomException If there is no room in the receiver.
     * @exception IllegalActionException If the port is not an output, if
     *  the index is out of range.
     */
    public void send(int channelindex, Token token, double delay)
            throws InvalidStateException, IllegalActionException  {
        if( delay < -1.0 ) {
            throw new IllegalActionException( this, "Negative delay "
                    + "values are not allowed.");
	}
        Receiver[][] farRec;
        try {
            workspace().getReadAccess();
            if (!isOutput()) {
                throw new IllegalActionException(this,
                        "send: Tokens can only be sent from an "+
                        "output port.");
            }
            if (channelindex >= getWidth() || channelindex < 0) {
                throw new IllegalActionException(this,
                        "send: channel index is out of range.");
            }
            // Note that the getRemoteReceivers() method doesn't throw
            // any non-runtime exception.
            farRec = getRemoteReceivers();
            if (farRec == null || farRec[channelindex] == null) {
                return;
            }
            
            /*
            // System.out.println("\nAbout to call ODReceiver.put() within " +
                    "ODIOPort.send() for " + getName() ); 
            */
            for (int j = 0; j < farRec[channelindex].length; j++) {
                double currentTime = ((ODActor)getContainer()).getCurrentTime(); 
                ((ODReceiver)farRec[channelindex][j]).put(
                        // FIXME 
                        // token, currentTime + delay);
                        token, delay);
            }
        } finally {
            workspace().doneReading();
	}

    }


    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////
    
    // The higher the integer, the higher the priority.
    private int _priority = 0;

}




















