/* A timed input/output port used in the DDE domain.

 Copyright (c) 1997-2000 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.dde.kernel;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// DDEIOPort
/**
A DDEIOPort is a timed input/output port used in the DDE domain.
DDEIOPorts are used to send tokens between actor, and in so
doing, associate time with the tokens as they are placed in
DDEReceivers.
<P>
DDEIOPorts are not necessary to facilitate communication between
actors executing in a DDE model; standard TypedIOPorts are sufficient
for most communication. DDEIOPorts become useful when the time stamp
to be associated with an outgoing token is greater than the current
time of the sending actor.
<P>
The designers of models that incorporate DDEIOPorts should be careful
to make sure that output time stamp ordering information is correct.
Since the output time stamp of a token being sent through a DDEIOPort
can be greater then the sending actor's current time, it is possible
on a subsequent token production to create an outgoing token with
a time stamp that is greater than the current time but less then the
previously produced time stamp. In such cases, an
IllegalArgumentException will be thrown.


@author John S. Davis II
@version $Id$
@see ptolemy.domains.dde.kernel.DDEReceiver
*/

public class DDEIOPort extends TypedIOPort {

    /** Construct a DDEIOPort with no container and an empty
     *  string as a name. The constructed port will be neither
     *  an input nor an output.
     */
    public DDEIOPort() {
        super();
    }

    /** Construct a DDEIOPort with a containing actor and the
     *  specified name that is neither an input nor an output.
     *  The specified container must implement the Actor interface,
     *  or an exception will be thrown.
     * @param container The container actor.
     * @param name The name of the port.
     * @exception IllegalActionException If the port is not of an
     *  acceptable class for the container, or if the container
     *  does not implement the Actor interface.
     * @exception NameDuplicationException If the name coincides
     *  with a port already in the container.
     */
    public DDEIOPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a DDEIOPort with a container and the specified
     *  name that is either an input, an output, or both, depending
     *  on the third and fourth arguments. The specified container
     *  must implement the Actor interface or an exception will be
     *  thrown.
     * @param container The container actor.
     * @param name The name of the port.
     * @param isinput True if this is to be an input port.
     * @param isoutput True if this is to be an output port.
     * @exception IllegalActionException If the port is not of an
     *  acceptable class for the container, or if the container does
     *  not implement the Actor interface.
     * @exception NameDuplicationException If the name coincides with
     *  a port already in the container.
     */
    public DDEIOPort(ComponentEntity container, String name,
            boolean isinput, boolean isoutput)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, isinput, isoutput);
        if( !container.isAtomic() ) {
            throw new IllegalActionException(container, this,
                    "A DDEIOPort can not be contained by a " +
                    "composite actor.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send a token to all connected receivers by calling send on
     *  all of the remote receivers connected to this port. If there
     *  are no connected receivers, then nothing is sent. Associate
     *  a time stamp with the token that is equivalent to the specified
     *  <I>sendTime</I> parameter. Throw an IllegalActionException if
     *  the port is not an output. Throw a NoRoomException if one of
     *  the channels throws it.
     * @param token The token to send
     * @param sendTime The output time of the token being sent.
     * @exception IllegalActionException If the port is not an output
     *  or the delay is negative.
     * @exception NoRoomException If a send to one of the channels
     *  throws it.
     */
    public void broadcast(Token token, double sendTime)
            throws IllegalActionException, NoRoomException {
        try {
            workspace().getReadAccess();

            Receiver fr[][] = getRemoteReceivers();
            if(fr == null) {
                return;
            }
            for (int j = 0; j < fr.length; j++) {
                send(j, token, sendTime);
            }
        } finally {
            workspace().doneReading();
        }
    }

    /** Send the specified token to all receivers connected to the
     *  specified channel. The first receiver gets the actual token,
     *  while subsequent receivers get a clone. If there are no
     *  receivers, then do nothing. Associate a time stamp with the
     *  token that is equivalent to the specified <I>sendTime</I>
     *  parameter. Throw an IllegalActionException if the port is
     *  not an output. Throw a NoRoomException if one of the channels
     *  throws it.
     * @param chIndex The index of the channel, between (inclusive)
     *  0 to width-1.
     * @param token The token to send.
     * @param sendTime The output time of the token being sent.
     * @exception NoRoomException If there is no room in the receiver.
     * @exception IllegalActionException If the port is not an output,
     *  if the index is out of range.
     */
    public void send(int chIndex, Token token, double sendTime)
            throws IllegalActionException, NoRoomException {
	double currentTime = 0.0;
	Thread thread = Thread.currentThread();
	DDEThread ddeThread = null;
	if( thread instanceof DDEThread ) {
	    ddeThread = (DDEThread)thread;
	    currentTime = ddeThread.getTimeKeeper().getCurrentTime();
	}
        if( sendTime < currentTime &&
		sendTime != PrioritizedTimedQueue.IGNORE &&
		sendTime != PrioritizedTimedQueue.INACTIVE ) {
            throw new IllegalActionException( this, "Time values in "
                    + "the past are not allowed.");
	}

        if( thread instanceof DDEThread ) {
            ddeThread = (DDEThread)thread;
            TimeKeeper tKeeper = ddeThread.getTimeKeeper();
            tKeeper.setOutputTime(sendTime);
        }

        super.send( chIndex, token );
    }

    /** Constrain DDEIOPorts to only be contained by non-atomic
     *  entities.
     * @exception IllegalActionException If the container argument
     *  is not atomic.
     * @exception NameDuplicationException If the name of this
     *  port is not unique with respect to other objects
     *  contained by this port's container.
     */
    public void setContainer(ComponentEntity container)
            throws IllegalActionException, NameDuplicationException {
        if( !container.isAtomic() ) {
            throw new IllegalActionException(container, this,
                    "A DDEIOPort can not be contained by a " +
                    "composite actor.");
        }
        super.setContainer(container);
    }
}
