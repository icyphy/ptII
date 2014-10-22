/* A specialized port for Ptides platforms.

@Copyright (c) 2008-2014 The Regents of the University of California.
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

package ptolemy.domains.ptides.lib;

import java.util.HashMap;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.lib.hoc.MirrorPort;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptides.kernel.PtidesDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonConfigurableAttribute;

/** A specialized port for Ptides platform I/O implementing
 *  functionality for sensors, actuators and network receiver and
 *  transmitter ports.
 *
 *  When creating a port in vergil, a sensor or actuator port is
 *  created. To create a network port, the parameter <i>
 *  isNetworkPort</i> is changed. Upon changing the type of the port,
 *  the icon is changed as well.
 *
 *  PtidesPorts are configured by parameters where some parameters
 *  are the same for all types of ports and others are specific
 *  to the type. Every PtidesPort can be configured with a
 *  deviceDelay and a deviceDelayBound. The deviceDelay is the amount
 *  of platform time passing between the port receiving the event and
 *  the platform time the event is put on the event queue (for input
 *  ports) or the time the event is released to the environment (for
 *  output ports).  The deviceDelayBound is the maximum of this value
 *  and it is used in the safe-to-process analysis.
 *
 *  Actuators additionally have a parameter actuateAtEventTimestamp
 *  which is used to specify whether the actuator can output events
 *  before the event timestamp or only at the event timestamp.
 *
 *  A network transmitter has a parameter platformDelayBound
 *  which is configured to represent the maximum difference between
 *  platform time and the event timestamp if platform time is
 *  ahead of the event timestamp. This is used for the safe-to-process
 *  analysis.
 *
 *  A network receiver has a parameter sourcePlatformDelayBound
 *  which can be used to match the parameter platformDelayBound
 *  on the sending platform. Again, this parameter is used for
 *  the safe-to-process analysis.
 *
 *  Network receivers have an additional parameter networkDelayBound
 *  which describes the maximum physical time input events spent on
 *  the network after being sent by the another PtidesPlatform.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (derler)
 *  @Pt.AcceptedRating
 */
public class PtidesPort extends MirrorPort {

    /** Create a new PtidesPort with a given container and a name.
     * @param container The container of the port.
     * @param name The name of the port.
     * @exception IllegalActionException If parameters cannot be set.
     * @exception NameDuplicationException If name already exists.
     */
    public PtidesPort(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        deviceDelay = new Parameter(this, "deviceDelay");
        deviceDelay.setToken(new DoubleToken(0.0));
        deviceDelay.setTypeEquals(BaseType.DOUBLE);

        deviceDelayBound = new Parameter(this, "deviceDelayBound");
        deviceDelayBound.setExpression("0.0");
        deviceDelayBound.setTypeEquals(BaseType.DOUBLE);

        isNetworkPort = new Parameter(this, "isNetworkPort");
        isNetworkPort.setTypeEquals(BaseType.BOOLEAN);
        isNetworkPort.setExpression("false");

        actuateAtEventTimestamp = new Parameter(this, "actuateAtEventTimestamp");
        actuateAtEventTimestamp.setTypeEquals(BaseType.BOOLEAN);
        actuateAtEventTimestamp.setExpression("true");
        _actuateAtEventTimestamp = true;

        platformDelayBound = new Parameter(this, "platformDelayBound");
        platformDelayBound.setExpression("0.0");
        platformDelayBound.setTypeEquals(BaseType.DOUBLE);
        platformDelayBound.setVisibility(Settable.EXPERT);

        sourcePlatformDelayBound = new Parameter(this,
                "sourcePlatformDelayBound");
        sourcePlatformDelayBound.setExpression("0.0");
        sourcePlatformDelayBound.setTypeEquals(BaseType.DOUBLE);
        sourcePlatformDelayBound.setVisibility(Settable.EXPERT);

        networkDelayBound = new Parameter(this, "networkDelayBound");
        networkDelayBound.setExpression("0.0");
        networkDelayBound.setTypeEquals(BaseType.DOUBLE);

        _iconDescription = new SingletonConfigurableAttribute(this,
                "_iconDescription");
        _iconDescription.setPersistent(false);
        _setIconAndParameterVisibility();
    }

    /** Actuate at event timestamp parameter that defaults to the boolean value TRUE.
     *  If this parameter is set to FALSE, an actuator can produce outputs as soon
     *  as they are available.
     */
    public Parameter actuateAtEventTimestamp;

    /** Device delay parameter that defaults to the double value 0.0. */
    public Parameter deviceDelay;

    /** Device delay bound parameter that defaults to the double value 0.0. */
    public Parameter deviceDelayBound;

    /** Flag that is true if the port is a network receiver or transmitter.
     *  The flag defaults to false.
     */
    public Parameter isNetworkPort;

    /** An assumed upper bound on the network delay, used if this is a network
     *  receiver port (<i>isNetworkPort</i> is true and the port is an input).
     *  If the actual network delay exceeds this assumed value, then an exception
     *  may result if actual received time of the input is too late to ensure that
     *  DE semantics is respected.
     *  This is a double that defaults to 0.0.
     */
    public Parameter networkDelayBound;

    /** An assumed upper bound on the difference between platform time and
     *  the event timestamp sent to a network transmitter port
     *  (<i>isNetworkPort</i> is true and the port is an output),
     *  where platform time is smaller than the event timestamp.
     *  If the platform time plus the platformDelayBound
     *  is smaller than the event timestamp, an exception is thrown.
     *  This parameter is used in cases where the execution time is larger than
     *  the logical time delays on the platform to indicate that the designer
     *  knows that the execution time is bigger than the time delay on the
     *  platform.
     *  This is a double that defaults to 0.0. In that case, an exception
     *  is thrown when platform time is smaller than the event timestamp.
     */
    public Parameter platformDelayBound;

    /** An assumed upper bound on the difference between platform time and
     *  the event timestamp received at a network receiver port
     *  (<i>isNetworkPort</i> is true and the port is an output),
     *  where platform time is smaller than the event timestamp.
     *  <p>This is used to indicate that the designer knows that the execution
     *  time on the sending platform is bigger than the time delay on the
     *  sending platform.</p>
     *  <p>Together with the networkDelayBound and the
     *  clockSychronizationErrorBound parameters, this parameter is
     *  used to determine whether an event is arriving at a network
     *  receiver too late.</p>
     *  <p>The value specified for the platformDelayBound on a network transmitter
     *  and the value specified for the sourcePlatformDelayBound on a network
     *  receiver should ideally match, however, this is not enforced.
     *  This is a double that defaults to 0.0.</p>
     */
    public Parameter sourcePlatformDelayBound;

    /** Return true if actuation should happen at event timestamp and false if
     *  actuation can happen sooner.
     *  @return whether actuation should be done at the event timestamp.
     */
    public boolean actuateAtEventTimestamp() {
        return _actuateAtEventTimestamp;
    }

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  This overrides
     *  the base class so that if the attribute is an instance of
     *  TypeAttribute, then it sets the type of the port.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == isNetworkPort) {
            _isNetworkPort = ((BooleanToken) isNetworkPort.getToken())
                    .booleanValue();
            _setIconAndParameterVisibility();
        }
        if (attribute == actuateAtEventTimestamp) {
            _actuateAtEventTimestamp = ((BooleanToken) actuateAtEventTimestamp
                    .getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return the timestamp and sourceTimestamp for a specific token.
     *  @param t The token.
     *  @return The timestamp.
     */
    public Object[] getTimestampForToken(Token t) {
        Object[] times = _transmittedTokenTimestamps.get(t);
        _transmittedTokenCnt.put(t, _transmittedTokenCnt.get(t).intValue() - 1);
        if (_transmittedTokenCnt.get(t).intValue() == 0) {
            _transmittedTokenTimestamps.remove(t);
            _transmittedTokenCnt.remove(t);
        }
        return times;
    }

    /** Get the microstep of the event that contained the token.
     * @param t The token.
     * @return The microstep.
     */
    public int getMicrostepForToken(Token t) {
        Integer microstep = _transmittedTokenMicrosteps.get(t);
        _transmittedTokenMicrosteps.remove(t);
        return microstep;
    }

    /** Check whether port is output and network port parameter is not set.
     *  @return True if port is an actuator port.
     */
    public boolean isActuatorPort() {
        return isOutput() && !_isNetworkPort;
    }

    /** Check whether port is input and network port parameter is not set.
     * @return True if is sensor port.
     */
    public boolean isSensorPort() {
        return isInput() && !_isNetworkPort;
    }

    /** Check whether port is input and network port parameter is set.
     * @return True if is network receiver port.
     */
    public boolean isNetworkReceiverPort() {
        return isInput() && _isNetworkPort;
    }

    /** Check whether port is output and network port parameter is set.
     *  @return True if port is an network transmitter port.
     */
    public boolean isNetworkTransmitterPort() {
        return isOutput() && _isNetworkPort;
    }

    /** Make port input port and update visual representation.
     */
    @Override
    public void setInput(boolean isInput) throws IllegalActionException {
        super.setInput(isInput);

        _setIconAndParameterVisibility();
    }

    /** Make port output port and update visual representation.
     */
    @Override
    public void setOutput(boolean isInput) throws IllegalActionException {
        super.setOutput(isInput);

        _setIconAndParameterVisibility();

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
        Time timestamp = ((CompositeActor) getContainer()).getDirector()
                .getModelTime();
        int microstep = ((PtidesDirector) ((CompositeActor) getContainer())
                .getDirector()).getIndex();
        Time sourceTimestamp = ((PtidesDirector) ((CompositeActor) getContainer())
                .getDirector()).getCurrentSourceTimestamp();
        if (sourceTimestamp == null) {
            sourceTimestamp = timestamp;
        }
        if (_transmittedTokenTimestamps == null) {
            _transmittedTokenTimestamps = new HashMap();
            _transmittedTokenMicrosteps = new HashMap();
            _transmittedTokenCnt = new HashMap();
        }
        if (_transmittedTokenTimestamps.get(token) == null) {
            _transmittedTokenCnt.put(token, 0);
        }
        _transmittedTokenTimestamps.put(token, new Object[] { timestamp,
                sourceTimestamp });

        _transmittedTokenMicrosteps.put(token, microstep);

        _transmittedTokenCnt.put(token, _transmittedTokenCnt.get(token)
                .intValue() + 1);
        super.send(channelIndex, token);
    }

    /** Change visibility of parameters depending on the type of
     *  port.
     *  @exception IllegalActionException Thrown if icon cannot be changed.
     */
    private void _setIconAndParameterVisibility() throws IllegalActionException {
        try {
            if (isSensorPort()) {
                actuateAtEventTimestamp.setVisibility(Settable.NONE);
                networkDelayBound.setVisibility(Settable.NONE);
                platformDelayBound.setVisibility(Settable.NONE);
                sourcePlatformDelayBound.setVisibility(Settable.NONE);
                _iconDescription
                        .configure(
                                null,
                                null,
                                "<svg>\n"
                                        + "<polygon points=\"-8, 8, 8, 8, 8, 4, 12, 0, 8, -4, 8, -8, -8, -8\" "
                                        + "style=\"fill:black\"/>\n"
                                        + "</svg>\n");
            } else if (isActuatorPort()) {
                actuateAtEventTimestamp.setVisibility(Settable.FULL);
                networkDelayBound.setVisibility(Settable.NONE);
                platformDelayBound.setVisibility(Settable.NONE);
                sourcePlatformDelayBound.setVisibility(Settable.NONE);
                _iconDescription
                        .configure(
                                null,
                                null,
                                "<svg>\n"
                                        + "<polygon points=\"-8, 8, 8, 8, 8, -8, -8, -8, -8, -4, -12, 0, -8, 4\" "
                                        + "style=\"fill:black\"/>\n"
                                        + "</svg>\n");
            } else if (isNetworkReceiverPort()) {
                actuateAtEventTimestamp.setVisibility(Settable.NONE);
                networkDelayBound.setVisibility(Settable.FULL);
                platformDelayBound.setVisibility(Settable.NONE);
                sourcePlatformDelayBound.setVisibility(Settable.EXPERT);
                _iconDescription
                        .configure(
                                null,
                                null,
                                "<svg>\n"
                                        + "<polygon points=\"-8, 8, 8, 8, 8, 4, 12, 4, 12, -4, 8, -4, 8, -8, -8, -8\" "
                                        + "style=\"fill:black\"/>\n"
                                        + "</svg>\n");
            } else if (isNetworkTransmitterPort()) {
                actuateAtEventTimestamp.setVisibility(Settable.NONE);
                networkDelayBound.setVisibility(Settable.NONE);
                platformDelayBound.setVisibility(Settable.EXPERT);
                sourcePlatformDelayBound.setVisibility(Settable.NONE);
                _iconDescription
                        .configure(
                                null,
                                null,
                                "<svg>\n"
                                        + "<polygon points=\"-8, 8, 8, 8, 8, -8, -8, -8, -8, -4, -12, -4, -12, 4, -8, 4\" "
                                        + "style=\"fill:black\"/>\n"
                                        + "</svg>\n");
            }
        } catch (Exception e) {
            throw new IllegalActionException(this, e.getMessage());
        }
    }

    private boolean _actuateAtEventTimestamp;

    private SingletonConfigurableAttribute _iconDescription;

    private boolean _isNetworkPort;

    private HashMap<Token, Object[]> _transmittedTokenTimestamps;

    private HashMap<Token, Integer> _transmittedTokenMicrosteps;

    private HashMap<Token, Integer> _transmittedTokenCnt;

}
