/* IOPort for NonStrictActors

 Copyright (c) 1997-2001 The Regents of the University of California.
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

@ProposedRating Red (pwhitake@eecs.berkeley.edu)
@AcceptedRating Red (pwhitake@eecs.berkeley.edu)

*/

package ptolemy.domains.sr.kernel;

import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// NonStrictIOPort
/**
This class extends TypedIOPort to allow the port to be nonstrict.  That is, 
the values of tokens on the channels of the port may be unknown, or it may 
not be known whether tokens are even present.

@author Paul Whitaker
@version $Id$
*/
public class NonStrictIOPort extends TypedIOPort {

    /** Construct a NonStrictIOPort with no container and no name that is
     *  neither an input nor an output.
     */
    public NonStrictIOPort() {
        super();
    }

   /** Construct a port in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the port.
     */
    public NonStrictIOPort(Workspace workspace) {
	super(workspace);
    }

    /** Construct a NonStrictIOPort with a containing actor and a name
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
    public NonStrictIOPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
    }

    /** Construct a NonStrictIOPort with a container and a name that is
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
    public NonStrictIOPort(ComponentEntity container, String name,
            boolean isinput, boolean isoutput)
            throws IllegalActionException, NameDuplicationException {
        this(container, name);
	setInput(isinput);
        setOutput(isoutput);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if all channels of this port have known state, that is, 
     *  the tokens on each channel are known or each channel is known not to 
     *  have any tokens.
     *  <p>
     *  Note that this does not report any tokens in inside receivers
     *  of an output port. Those are accessible only through
     *  getInsideReceivers().
     *
     *  @return True if it is known whether there is a token in each channel.
     *  @exception IllegalActionException If the receivers do not support
     *   this query, if there is no director, and hence no receivers,
     *   if the port is not an input port, or if the channel index is out
     *   of range.
     */
    public boolean isKnown() throws IllegalActionException {
        for (int j = 0; j < getWidth(); j++) {
            if (!isKnown(j)) return false;
        }
        return true;
    }

    /** Return true if the specified channel has known state, that is, the
     *  tokens on this channel are known or this channel is known not to 
     *  have any tokens.  If the channel index is out of range, then throw 
     *  an exception.
     *  <p>
     *  Note that this does not report any tokens in inside receivers
     *  of an output port. Those are accessible only through
     *  getInsideReceivers().
     *
     *  @param channelIndex The channel index.
     *  @return True if it is known whether there is a token in the channel.
     *  @exception IllegalActionException If the receivers do not support
     *   this query, if there is no director, and hence no receivers,
     *   if the port is not an input port, or if the channel index is out
     *   of range.
     */
    public boolean isKnown(int channelIndex) throws IllegalActionException {
        try {
            if (isInput()) {
                Receiver[][] receivers = getReceivers();
                if (receivers != null && receivers[channelIndex] != null) {
                    for (int j = 0; j < receivers[channelIndex].length; j++) {
                        // FIXME:
                        //if (receivers[channelIndex][j].isKnown()) return true;
                    }
                }
            }
            if (isOutput()) {
                Receiver[][] receivers = getRemoteReceivers();
                if (receivers != null && receivers[channelIndex] != null) {
                    for (int j = 0; j < receivers[channelIndex].length; j++) {
                        // FIXME:
                        //if (receivers[channelIndex][j].isKnown()) return true;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalActionException(this,
                    "isKnown: channel index is out of range.");
        }
        return false;
    }
}






