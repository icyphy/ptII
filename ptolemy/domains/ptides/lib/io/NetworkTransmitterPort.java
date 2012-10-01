/* Network Transmitter port.

@Copyright (c) 2008-2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

*/



package ptolemy.domains.ptides.lib.io;

import java.util.HashMap;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.util.Time;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 *  This port provides a specialized TypedIOPort for network transmitters
 *  used in Ptides. This port just specializes parameters.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (derler)
 *  @Pt.AcceptedRating
 */
public class NetworkTransmitterPort extends PtidesPort {


    /** Create a new NetworkTransmitterPort with a given container and a name.
     * @param container The container of the port.
     * @param name The name of the port.
     * @exception IllegalActionException If parameters cannot be set.
     * @exception NameDuplicationException If name already exists.
     */
    public NetworkTransmitterPort(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);

        this.setOutput(true);

        platformDelayBound = new Parameter(this, "platformDelayBound");
        platformDelayBound.setExpression("0.0");
        platformDelayBound.setTypeEquals(BaseType.DOUBLE);

    }


    /** Return the timestamp for a specific token.
     *  @param t The token.
     *  @return The timestamp.
     */
    public Time getTimeStampForToken(Token t) {
        Time time = _transmittedTokens.get(t);
        _transmittedTokenCnt.put(t, _transmittedTokenCnt.get(t).intValue() - 1);
        if (_transmittedTokenCnt.get(t).intValue() == 0) {
            _transmittedTokens.remove(t);
            _transmittedTokenCnt.remove(t);
        }
        return time;
    }

    /** Save token and remember timestamp of the token. Then call send of
     *  super class.
     *  @param channelIndex The index of the channel, from 0 to width-1.
     *  @param token The token to send, or null to send no token.
     *  @exception IllegalActionException If the token to be sent cannot
     *   be converted to the type of this port, or if the token is null.
     *  @exception NoRoomException If there is no room in the receiver.
     */
    @Override
    public void send(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {
        Time timestamp = ((CompositeActor)getContainer()).getDirector().getModelTime();
        if (_transmittedTokens == null) {
            _transmittedTokens = new HashMap();
            _transmittedTokenCnt = new HashMap();
        }
        if (_transmittedTokens.get(token) == null) {
            _transmittedTokenCnt.put(token, 0);
        }
        _transmittedTokens.put(token, timestamp);
        _transmittedTokenCnt.put(token, _transmittedTokenCnt.get(token).intValue() + 1);
        super.send(channelIndex, token);
    }

    /** Platform delay bound parameter that defaults to the double value 0.0. */
    public Parameter platformDelayBound;


    private HashMap<Token, Time> _transmittedTokens;
    private HashMap<Token, Integer> _transmittedTokenCnt;


}
