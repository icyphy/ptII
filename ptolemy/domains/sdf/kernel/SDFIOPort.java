/* IOPort for SDF

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

@ProposedRating Red
@AcceptedRating Red

*/

package ptolemy.domains.sdf.kernel;

import ptolemy.actor.*;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;

import java.util.Enumeration;
import java.util.Hashtable;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// SDFIOPort
/**
This class extends IOPort to allow the transmission of arrays of tokens.
This is better then sending the tokens individually, because it amortizes
the overhead in send and get over a large number of tokens.

@authors Stephen Neuendorffer
@version $Id$
*/
public final class SDFIOPort extends IOPort {

    /** Construct an SDFIOPort with no container and no name that is
     *  neither an input nor an output.
     */
    public SDFIOPort() {
        super();
    }

    /** Construct an SDFIOPort with a containing actor and a name
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
    public SDFIOPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
    }

    /** Construct an SDFIOPort with a container and a name that is
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
    public SDFIOPort(ComponentEntity container, String name,
            boolean isinput, boolean isoutput)
            throws IllegalActionException, NameDuplicationException {
        this(container, name);
        setInput(isinput);
        setOutput(isoutput);
    }

    /** Get an array of tokens from the specified channel.
     *  This call is similar to IOPort.get(int), except that it returns
     *  an array of tokens.  This is faster because the overhead associated
     *  with get() is amortized over all the tokens in the array.
     *
     *  @param channelindex The channel index.
     *  @param tokens An array to fill with Tokens from the port.
     *  @exception NoTokenException If there are not enough tokens.
     *  @exception IllegalActionException If there is no director, and hence
     *   no receivers have been created, if the port is not an input port, or
     *   if the channel index is out of range.
     *  @exception IllegalActionException if the array is null.
     */
    public void getArray(int channelindex, Token tokens[])
            throws NoTokenException, IllegalActionException {
        Receiver[][] localRec;
        if(tokens == null) throw new IllegalActionException(
                "SDFIOPort: getArray: array must be not null");
        try {
            workspace().getReadAccess();
            if (!isInput()) {
                throw new IllegalActionException(this,
                        "get: Tokens can only be retrieved from " +
                        "an input port.");
            }
            if (channelindex >= getWidth() || channelindex < 0) {
                throw new IllegalActionException(this,
                        "get: channel index is out of range.");
            }
            // Note that the getReceivers() method might throw an
            // IllegalActionException if there's no director.
            localRec = getReceivers();
            if (localRec[channelindex] == null) {
                throw new NoTokenException(this,
                        "get: no receiver at index: " + channelindex + ".");
            }
        } finally {
            workspace().doneReading();
        }
        if((localRec[channelindex].length > 1)|| 
                !(localRec[channelindex][0] instanceof SDFReceiver)) {
            for (int i = 0; i < tokens.length; i++) {
                Token tt = null;
                for (int j = 0; j < localRec[channelindex].length; j++) {
                    Token ttt = localRec[channelindex][j].get();
                    if (tt == null) tt = ttt;
                }
                if (tt == null) {
                    throw new NoTokenException(this,
                            "get: No token to return.");
                }
                tokens[i] = tt;
            }
        }
        else {
            ((SDFReceiver) localRec[channelindex][0]).get(tokens);
        }    
    }

    /** Send an array of tokens to all receivers connected to the
     *  specified channel.  Operation is similar to IOPort.send()
     *
     *  @param channelindex The index of the channel, from 0 to width-1
     *  @param tokens The tokens to send
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException If the port is not an output or if
     *   the index is out of range.
     *  @exception IllegalActionException If the array is null
     */
    public void sendArray(int channelindex, Token tokens[])
            throws IllegalActionException, NoRoomException {
        Receiver[][] farRec;
        if(tokens == null) throw new IllegalActionException(
                "SDFIOPort: sendArray: array must be not null");
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
            if (farRec == null || farRec[channelindex] == null) return;
        } finally {
            workspace().doneReading();
        }
        for (int j = 0; j < farRec[channelindex].length; j++) {
            if(farRec[channelindex][j] instanceof SDFReceiver)
                ((SDFReceiver) farRec[channelindex][j]).put(tokens);
            else
                for (int i = 0; i < tokens.length; i++) {
                    farRec[channelindex][j].put(tokens[i]);
                }
        }
    }
}
