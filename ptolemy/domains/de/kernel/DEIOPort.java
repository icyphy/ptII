/* A Discrete Event domain port that supports sending both a token and a time
   stamp.

 Copyright (c) 1997-1998 The Regents of the University of California.
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
import java.util.*;
import collections.*;

//////////////////////////////////////////////////////////////////////////
//// DEIOPort
/**
Extension of the IOPort class to be used in Discrete Event domain. It
overloads two methods, broadcast() and send(). The overloaded versions have
another input parameter for the time stamp.

@authors Lukito Muliadi
@version $Id$
*/
public class DEIOPort extends IOPort {

    /** Construct a DEIOPort with no container and no name that is
     *  neither an input nor an output.
     */
    public DEIOPort() {
        super();
    }

    /** Construct a DEIOPort with a containing actor and a name
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

    /** Construct a DEIOPort with a container and a name that is
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

    /** Overload the broadcast() method to support sending both a token
     *  and a time stamp.
     *
     *  @param token The token to send
     *  @param time The time stamp of the token being broadcasted.
     *  @exception CloneNotSupportedException If there is more than one
     *   destination and the token cannot be cloned.
     *  @exception IllegalActionException If the port is not an output.
     */
    public void broadcast(Token token, double time)
	    throws CloneNotSupportedException, IllegalActionException {
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
            boolean first = true;

            for (int j = 0; j < fr.length; j++) {
                if (first) {
                    send(j, token, time);
                    first = false;
                } else {
                    send(j, ((Token)(token.clone())), time);
                }
            }
        } finally {
            workspace().doneReading();
        }
    }

    /** Overload the send() method to support sending both a token and
     *  a time stamp.
     *
     *  @param channelindex The index of the channel, from 0 to width-1
     *  @param token The token to send
     *  @param time The time stamp of the token being sent.
     *  @exception CloneNotSupportedException If the token cannot be cloned
     *   and there is more than one destination.
     *  @exception IllegalActionException If the port is not an output,
     *   or if the index is out of range.
     */
    public void send(int channelindex, Token token, double time)
            throws CloneNotSupportedException, IllegalActionException {
        try {
            workspace().getReadAccess();
            if (!isOutput()) {
                throw new IllegalActionException(this,
                        "send: Tokens can only be sent from an output port.");
            }
            if (channelindex >= getWidth() || channelindex < 0) {
                throw new IllegalActionException(this,
                        "send: channel index is out of range.");
            }
            Receiver[][] fr = getRemoteReceivers();
            if (fr == null || fr[channelindex] == null) return;
            boolean first = true;
            for (int j = 0; j < fr[channelindex].length; j++) {
                if (first) {
                    // FIXME: need to catch ?
                    try {
                        ((DEReceiver)fr[channelindex][j]).put(token, time);
                    } catch (ClassCastException e) {
                        throw new InvalidStateException("DEIOPort.send() is" +
                                " expected to have receivers of type "+
                                "DEReceiver (1)");
                    }
                    first = false;
                } else {
                    // FIXME: need to catch ?
                    try {
                        ((DEReceiver)fr[channelindex][j]).put((Token)(token.clone()), time);
                    } catch (ClassCastException e) {
                        throw new InvalidStateException("DEIOPort.send() is" +
                                " expected to have receivers of type "+
                                "DEReceiver (2)");
                    }
                }
            }
        } finally {
            workspace().doneReading();
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // FIXME: public ?
    public DEIOPort beforePort = null;
    // use insertLast() and take() methods to access it.
    public LinkedList triggerList = new LinkedList();


}










