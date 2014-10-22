/* A Switch that delays  packets according to their priorities and length

@Copyright (c) 2011-2013 The Regents of the University of California.
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
package ptolemy.domains.ptides.lib.qm;

import ptolemy.actor.Actor;
import ptolemy.actor.CommunicationAspect;
import ptolemy.actor.CommunicationAspectListener.EventType;
import ptolemy.actor.Director;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.lib.aspect.BasicSwitch;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * A {@link CommunicationAspect} actor that applies packet-size and priority
 * dependent delay to incoming packets. Intended to use with RecordTokens only.
 * Assumes record token includes the encapsulated packages with a label "packets"
 * and a TCP header with label "TCPlabel"
 *
 * @author Ilge Akkaya
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ilgea)
 * @Pt.AcceptedRating
 */
public class VariableDelaySwitch extends BasicSwitch {

    /** Construct a VariableDelaySwitch.
     *  @param container the container.
     *  @param name the name.
     *  @exception IllegalActionException If there is a problem with
     *  construction.
     *  @exception NameDuplicationException If there is a name collision.
     */
    public VariableDelaySwitch(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        Director director = getDirector();

        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }

        // input, output and switch fabric delays are defined and set by parent class

        // in bps
        channelBandwidth = new Parameter(this, "Channel Bandwidth (in bps)");
        channelBandwidth.setExpression("1000000");
        channelBandwidth.setTypeEquals(BaseType.INT);
        _channelBandwidth = 1000000; //1Mbps

        //set the unit packet size
        unitTokenSize = new Parameter(this, "Unit Packet Size(in bits)");
        unitTokenSize.setExpression("1000");
        unitTokenSize.setTypeEquals(BaseType.INT);
        _unitTokenSize = 1000;

        allowPDV = new Parameter(this, "Allow PDV");
        allowPDV.setExpression("true");
        allowPDV.setTypeEquals(BaseType.BOOLEAN);
        _allowPDV = true;

        allowPriority = new Parameter(this, "Allow Priority Routing");
        allowPriority.setExpression("false");
        allowPriority.setTypeEquals(BaseType.BOOLEAN);
        _allowPriority = false;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** A Boolean to enable/disable packet-length dependent input delay.
     *  The initial default value is true.
     */
    public Parameter allowPDV;

    /** A Boolean to enable/disable priority dependent input delay.
     *  The initial default value is false.
     */
    public Parameter allowPriority;

    /** The channel bandwidth in bits/second.  The initial default
     *  value is the integer 1000000, signifying 1Mbps.
     */
    public Parameter channelBandwidth;

    /** The size of one data token in bits. The initial default value
     *  is an integer with the value 1000.
     */
    public Parameter unitTokenSize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Process the attributes.
     *  @exception IllegalActionException If value is inappropriate.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == channelBandwidth) {
            int value = ((IntToken) channelBandwidth.getToken()).intValue();
            if (value <= 0) {
                throw new IllegalActionException(this,
                        "Cannot have negative or zero channel bandwidth: "
                                + value);
            }
            _channelBandwidth = value;
        } else if (attribute == unitTokenSize) {
            int value = ((IntToken) unitTokenSize.getToken()).intValue();
            if (value <= 0) {
                throw new IllegalActionException(this,
                        "Cannot have negative or zero packet size: " + value);
            }
            _unitTokenSize = value;
        } else if (attribute == allowPDV) {
            boolean value = ((BooleanToken) allowPDV.getToken()).booleanValue();
            _allowPDV = value;
        } else if (attribute == allowPriority) {
            boolean value = ((BooleanToken) allowPriority.getToken())
                    .booleanValue();
            _allowPriority = value;
        }
        super.attributeChanged(attribute);
    }

    /** Initiate a send of the specified token to the specified
     *  receiver. This method will schedule a refiring of this actor
     *  if there is not one already scheduled. Additional input delays
     *  are calculated and added to the timing constraints here.
     *  @param source Sender of the token.
     *  @param receiver The sending receiver.
     *  @param token The token to send.
     *  @exception IllegalActionException If the refiring request fails.
     *
     */
    @Override
    public void sendToken(Receiver source, Receiver receiver, Token token)
            throws IllegalActionException {
        if (token == null) {
            throw new IllegalActionException(this, "The token was null, "
                    + "which is not supported.  The token may be null "
                    + "through a convoluted and unlikely call sequence, see "
                    + "Coverity Scan CID 1135586");
        }
        Time currentTime = getDirector().getModelTime();
        // FIXME add Continuous support.

        int inputPortID = _getPortID(receiver, true);

        Time lastTimeStamp = currentTime;
        if (_inputTokens.get(inputPortID).size() > 0) {
            lastTimeStamp = _inputTokens.get(inputPortID).last().timeStamp;
        }

        /* calculate and add input buffer delays */
        double _priorityDelay = 0.0;
        double _packetSizeDelay = 0.0;


        RecordToken TCPFrame = (RecordToken) token;
        // get payload
        RecordToken tokens = (RecordToken) TCPFrame.get("tokens");
        RecordToken TCPHeader = (RecordToken) TCPFrame.get("TCPlabel");

        if (tokens == null || TCPHeader == null) {
            throw new IllegalActionException(this, "Token structure must"
                    + "contain a tokens and a TCPHeader field");
        }

        /* priority is carried as a part of the options field
        of the TCP header */

        int numberOfTokens = tokens.length();
        double packetLength = numberOfTokens * _unitTokenSize + TCPHeaderSize;
        // get priority value

        if (true == _allowPDV) {

            if (packetLength > 0.0) {

                _packetSizeDelay = packetLength / _channelBandwidth;
            } else {
                _packetSizeDelay = 0.0;
            }
        } else {
            _packetSizeDelay = 0.0;
        }

        IntToken recordPriority = (IntToken) TCPHeader.get("options");
        if (true == _allowPriority && recordPriority != null) {
            int _priority = recordPriority.intValue();
            _priorityDelay = _priority / 1000.0;
        } else {
            //
            _priorityDelay = 0.0;
        }

        // in addition to the static _inputBufferDelay, packet-specific delays calculated and added
        _inputTokens.get(inputPortID).add(
                new TimedEvent(lastTimeStamp.add(_inputBufferDelay
                        + _priorityDelay + _packetSizeDelay), new Object[] {
                        receiver, token }));
        _tokenCount++;
        sendCommunicationEvent((Actor) source.getContainer().getContainer(), 0,
                _tokenCount, EventType.RECEIVED);
        _scheduleRefire();

        if (_debugging) {
            _debug("At time " + getDirector().getModelTime()
                    + ", initiating send to "
                    + receiver.getContainer().getFullName() + ": " + token);

        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The channel bandwidth that will be used to determine the delay (in bits/sec). */
    protected int _channelBandwidth;

    /** The unit token size in bits. */
    protected int _unitTokenSize;

    /** Aallow or disallow input buffer packet delay variation. */
    protected boolean _allowPDV;

    /** Allow or disallow priority switching. */
    protected boolean _allowPriority;

    /** The default header size for TCP. */
    protected static final int TCPHeaderSize = 160;
}
