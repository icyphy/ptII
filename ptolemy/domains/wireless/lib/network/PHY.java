/* Implements a physical layer (PHY)

Copyright (c) 2004-2005 The Regents of the University of California.
All rights reserved.
Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above=

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
*/
package ptolemy.domains.wireless.lib.network;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.Iterator;


////////////////////////////////////////////////////////////////////////=
//
//// PHY

/**
   The PHY class implements a physical layer which does the following:
   1) collision detection;
   2) carrier sense;
   3) send TxStartConfirm to the MAC when TxStart is received; send TxEnd to
   the MAC when transmission is completed; send RxStart to the MAC when
   reception starts; send RxData and RxEnd to the MAC when reception ends.

   Things that can be added:
   1) not send channel status in the transmit state;
   2) The first received signal is above the sensitivity, so the PHY decides
   to receive it. If the second signal is much stronger, the PHY can
   abort the reception of the 1st one and receive the 2nd one instead.

   @author Charlie Zhong
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (czhong)
   @Pt.AcceptedRating Red (reviewmoderator)
*/
public class PHY extends NetworkActorBase {
    /** Construct an actor with the specified name and container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container.
     *  @param name The name of the actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public PHY(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Configure PHY layer parameters.
        aPreambleLength = new Parameter(this, "aPreambleLength");
        aPreambleLength.setTypeEquals(BaseType.INT);
        aPreambleLength.setExpression("20");

        aPlcpHeaderLength = new Parameter(this, "aPlcpHeaderLength");
        aPlcpHeaderLength.setTypeEquals(BaseType.INT);
        aPlcpHeaderLength.setExpression("4");

        // Configure parameters.
        SNRThresholdInDB = new Parameter(this, "SNRThresholdInDB");
        SNRThresholdInDB.setTypeEquals(BaseType.DOUBLE);
        SNRThresholdInDB.setExpression("-20");

        sensitivity = new Parameter(this, "sensitivity");
        sensitivity.setTypeEquals(BaseType.DOUBLE);
        sensitivity.setExpression("0.0");

        // create ports
        fromMAC = new TypedIOPort(this, "fromMAC", true, false);
        fromMAC.setTypeEquals(BaseType.GENERAL);

        fromChannel = new TypedIOPort(this, "fromChannel", true, false);
        fromChannel.setTypeEquals(BaseType.GENERAL);

        toMAC = new TypedIOPort(this, "toMAC", false, true);
        toMAC.setTypeEquals(BaseType.GENERAL);

        channelStatus = new TypedIOPort(this, "channelStatus", false, true);
        channelStatus.setTypeEquals(BaseType.GENERAL);

        PHYConfirm = new TypedIOPort(this, "PHYConfirm", false, true);
        PHYConfirm.setTypeEquals(BaseType.GENERAL);

        toChannel = new TypedIOPort(this, "toChannel", false, true);
        toChannel.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    // all time are in the unit of microseconds

    /** The size of the Preamble header in a frame. The Preamble
     *  header contains synchronization information and timing
     *  information for the frame.
     */
    public Parameter aPreambleLength;

    /** The size of the PLCP header in a frame. The PLCP header
     *  contains logical information for the physical layer to
     *  decode the frame.
     */
    public Parameter aPlcpHeaderLength;

    /** The threshold for the signal to be recognized from interference.
     *  It is specified in decibels (10 * log<sub>10</sub>(<i>r</i>),
     *  where <i>r</i> is the power ratio.  This is a double that
     *  defaults to Infinity, which indicates that all overlapping
     *  messages are lost to collisions.
     */
    public Parameter SNRThresholdInDB;

    /** The power threshold above which the signal can be
     *  detected at the receiver. Any message with a received power
     *  below this number is ignored.  This has type double
     *  and defaults to 0.0, which indicates that all messages
     *  (with nonzero power) will be received.
     */
    public Parameter sensitivity;

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Port receiving messages from the MAC
     */
    public TypedIOPort fromMAC;

    /** Port receiving messages from the channel
     */
    public TypedIOPort fromChannel;

    /** Port sending messages to the MAC
     */
    public TypedIOPort toMAC;

    /** Port sending channel status to the MAC
     */
    public TypedIOPort channelStatus;

    /** Port sending transmit confirmation to the MAC
     */
    public TypedIOPort PHYConfirm;

    /** Port sending messages to the channel
     */
    public TypedIOPort toChannel;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public void fire() throws IllegalActionException {
        super.fire();

        int oldnum = _numBusyTimers;
        int kind = whoTimeout2(); // check if a timer times out and which
        Time currentTime = getDirector().getModelTime();

        if ((oldnum > 0) && (_numBusyTimers == 0)) {
            // update channel status
            RecordToken ChannelStatusMsg = new RecordToken(SignalMsgFields,
                    new Token[] {
                        new IntToken(Idle)
                    });
            channelStatus.send(0, ChannelStatusMsg);
        }

        double power = 0;
        double duration = -1;
        RecordToken msg;

        switch (_currentState) {
        case PHY_Idle:

            if (fromChannel.hasToken(0)) {
                _data = (RecordToken) fromChannel.get(0);

                // the input port may not be a WirelessIOPort, but the port it is
                // connected to is
                Iterator connectedPorts = fromChannel.sourcePortList().iterator();

                while (connectedPorts.hasNext()) {
                    IOPort port = (IOPort) connectedPorts.next();

                    if (port.isInput() && port instanceof WirelessIOPort) {
                        // Found the port.
                        RecordToken properties = (RecordToken) ((WirelessIOPort) port)
                            .getProperties(0);
                        power = ((DoubleToken) properties.get("power"))
                            .doubleValue();
                        duration = ((DoubleToken) properties.get("duration"))
                            .doubleValue();
                        break;
                    }
                }

                // let us be a little picky about receiving a message
                if ((power > _sensitivity)
                        && ((_interference == 0.0)
                                || ((power / _interference) > _SNRThresholdInDB))) {
                    if (_debugging) {
                        Token dbg = new DoubleToken(power / _interference);
                        _debug(getFullName() + "Receiving a message."
                                + dbg.toString());
                    }

                    // The PHY will receive this message
                    setTimer2(RxDone, currentTime.add(duration), power);

                    // update channel status
                    _numBusyTimers++;

                    if (_numBusyTimers == 1) {
                        RecordToken ChannelStatusMsg = new RecordToken(SignalMsgFields,
                                new Token[] {
                                    new IntToken(Busy)
                                });
                        channelStatus.send(0, ChannelStatusMsg);
                    }

                    // send RxStart to the MAC
                    Token[] RxStartValues = {
                        new IntToken(RxStart),
                        _data.get("rate")
                    };
                    RecordToken RxStartMsg = new RecordToken(RxStartMsgFields,
                            RxStartValues);
                    toMAC.send(0, RxStartMsg);

                    // remember the power of the received message
                    _receivedPower = power;
                    _currentState = Receive;
                } else {
                    // this is also an interference
                    // add every conversation in the network to this giant table
                    setTimer2(InterferenceDone, currentTime.add(duration), power);

                    // update interference
                    _interference = _interference + power;
                }
            } else if (fromMAC.hasToken(0)) {
                msg = (RecordToken) fromMAC.get(0);

                if (((IntToken) msg.get("kind")).intValue() == TxStart) {
                    _startTransmission(msg);
                }
            }

            break;

        case Receive:

            if (kind == RxDone) // MUST check the timer first
                {
                    // send RxData to the MAC
                    Token[] RxDataValues = {
                        new IntToken(RxData),
                        _data.get("data")
                    };
                    RecordToken RxDataMsg = new RecordToken(RxDataMsgFields,
                            RxDataValues);
                    toMAC.send(0, RxDataMsg);

                    // send RxEnd to the MAC
                    Token[] RxEndValues = {
                        new IntToken(RxEnd),
                        new IntToken(_rxStatus)
                    };
                    RecordToken RxEndMsg = new RecordToken(RxEndMsgFields,
                            RxEndValues);
                    toMAC.send(0, RxEndMsg);

                    _currentState = PHY_Idle;
                } else if (fromChannel.hasToken(0)) { // This message is an interference
                    _handleInterference();

                    // check collision
                    if ((_receivedPower / _interference) <= _SNRThresholdInDB) {
                        _rxStatus = Error;
                    }
                } else if (fromMAC.hasToken(0)) {
                    msg = (RecordToken) fromMAC.get(0);

                    if (((IntToken) msg.get("kind")).intValue() == TxStart) {
                        _startTransmission(msg);

                        // abort the current reception and send RxEnd to the MAC
                        Token[] RxEndValues = {
                            new IntToken(RxEnd),
                            new IntToken(Error)
                        }; // set the status to Error
                        RecordToken RxEndMsg = new RecordToken(RxEndMsgFields,
                                RxEndValues);
                        toMAC.send(0, RxEndMsg);
                    }
                }

            break;

        case Transmit:

            if (kind == TxDone) // MUST check the timer first
                {
                    // send TxEnd to the MAC
                    RecordToken TxEndMsg = new RecordToken(SignalMsgFields,
                            new Token[] {
                                new IntToken(TxEnd)
                            });
                    PHYConfirm.send(0, TxEndMsg);

                    _currentState = PHY_Idle;
                } else if (fromChannel.hasToken(0)) { // This message is an interference
                    _handleInterference();
                } else if (fromMAC.hasToken(0)) {
                    msg = (RecordToken) fromMAC.get(0);

                    if (((IntToken) msg.get("kind")).intValue() == TxData) {
                        // send the data to the channel
                        Token[] ChMsgValues = {
                            new IntToken(_txRate),
                            msg.get("pdu")
                        };
                        RecordToken ChMsg = new RecordToken(ChMsgFields, ChMsgValues);
                        toChannel.send(0, ChMsg);

                        // update the parameter: duration
                        _duration = (Variable) getContainer().getContainer()
                            .getAttribute("duration");
                        _duration.setToken(new DoubleToken(_txDuration));
                        ;

                        setTimer2(TxDone, currentTime.add(_txDuration), 0.0);
                    }
                }

            break;
        }
    }

    /** Initialize the private variables.
     *  @exception IllegalActionException If thrown by the base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _currentState = PHY_Idle;
        _interference = 0.0;
        _numBusyTimers = 0;

        // initialize the channel status in the MAC
        RecordToken ChannelStatusMsg = new RecordToken(SignalMsgFields,
                new Token[] {
                    new IntToken(Idle)
                });
        channelStatus.send(0, ChannelStatusMsg);
    }

    /** If the specified attribute is changed,
     *  check that a positive number is given. Otherwise,
     *  defer to the base class.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        int temp = 0;

        if (attribute == aPreambleLength) {
            temp = ((IntToken) aPreambleLength.getToken()).intValue();

            if (temp < 0) {
                throw new IllegalActionException(this,
                        "preamble Length is required to be nonnegative. "
                        + "Attempt to set it to: " + temp);
            } else {
                _aPreambleLength = temp;
            }
        } else if (attribute == aPlcpHeaderLength) {
            temp = ((IntToken) aPlcpHeaderLength.getToken()).intValue();

            if (temp < 0) {
                throw new IllegalActionException(this,
                        "PLCPHeader Length is required to be nonnegative. "
                        + "Attempt to set it to: " + temp);
            } else {
                _aPlcpHeaderLength = temp;
            }
        } else if (attribute == SNRThresholdInDB) {
            double SNRThresholdInDBValue = ((DoubleToken) SNRThresholdInDB
                    .getToken()).doubleValue();

            // Convert to linear scale.
            _SNRThresholdInDB = Math.pow(10, SNRThresholdInDBValue / 10);
        } else if (attribute == sensitivity) {
            _sensitivity = ((DoubleToken) sensitivity.getToken()).doubleValue();

            if (_sensitivity < 0.0) {
                throw new IllegalActionException(this,
                        "sensitivity is required to be nonnegative. "
                        + "Attempt to set it to: " + _sensitivity);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    protected ExtendedTimer setTimer2(int kind, Time expirationTime,
            double power) throws IllegalActionException {
        ExtendedTimer timer = new ExtendedTimer();
        timer.kind = kind;
        timer.expirationTime = expirationTime;
        timer.power = power;

        // put all timers of this object into a queue
        _timersSet.add(timer);
        getDirector().fireAt(this, expirationTime);
        return timer;
    }

    /** Remove the timer that matches with the <i>timerToCancel<i> argument
     *  from the timers set. If no match is found, do nothing.
     */
    protected void cancelTimer2(ExtendedTimer timerToCancel)
            throws IllegalActionException {
        Iterator timers = _timersSet.iterator();

        // iterate through the queue to find the timer to be canceled
        while (timers.hasNext()) {
            ExtendedTimer timer = (ExtendedTimer) timers.next();

            if (timer == timerToCancel) {
                _timersSet.remove(timer);
                break;
            }
        }
    }

    /** Get the timer with expiration time that matches the current time.
     *  Remove the timer from the timers set and return the <i>kind<i>
     *  parameter of the timer to the caller method. If there are multiple
     *  timers with expiration time matching the current time, return the
     *  first one from the iterator list.
     *  @return return the i>kind<i> parameter of the timeout timer.
     *  @exception IllegalActionException If thrown by
     *  getDirector().getCurrentTime().
     */
    protected int whoTimeout2() throws IllegalActionException {
        // find the 1st timer expired
        Iterator timers = _timersSet.iterator();

        while (timers.hasNext()) {
            ExtendedTimer timer = (ExtendedTimer) timers.next();

            if (timer.expirationTime == getDirector().getModelTime()) {
                // update interference
                if (timer.kind == InterferenceDone) {
                    _interference = _interference - timer.power;

                    // Quantization errors may make this negative. Do not allow.
                    if (_interference < 0.0) {
                        _interference = 0.0;
                    }
                }

                if (((timer.kind == InterferenceDone) || (timer.kind == RxDone))
                        && (timer.power > _sensitivity)) {
                    _numBusyTimers--;
                }

                // remove it from the set no matter that
                // it will be processed or ignored
                timers.remove();
                return timer.kind;
            }
        }

        return UNKNOWN;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void _handleInterference() throws IllegalActionException {
        double power = 0.5;
        double duration = -1;
        ;
        fromChannel.get(0); // consume the token

        Iterator connectedPorts = fromChannel.sourcePortList().iterator();

        while (connectedPorts.hasNext()) {
            IOPort port = (IOPort) connectedPorts.next();

            if (port.isInput() && port instanceof WirelessIOPort) {
                // Found the port.
                RecordToken properties = (RecordToken) ((WirelessIOPort) port)
                    .getProperties(0);
                power = ((DoubleToken) properties.get("power")).doubleValue();
                duration = ((DoubleToken) properties.get("duration"))
                    .doubleValue();
                break;
            }
        }

        Time currentTime = getDirector().getModelTime();

        // add every conversation in the network to this giant table
        setTimer2(InterferenceDone, currentTime.add(duration), power);

        // update interference
        _interference = _interference + power;

        // update channel status
        if (power > _sensitivity) {
            _numBusyTimers++;
        }

        if (_numBusyTimers == 1) {
            Token[] value = {
                new IntToken(Busy)
            };
            RecordToken ChannelStatusMsg = new RecordToken(SignalMsgFields,
                    value);
            channelStatus.send(0, ChannelStatusMsg);
        }
    }

    private void _startTransmission(RecordToken msg)
            throws IllegalActionException {
        _txRate = ((IntToken) msg.get("rate")).intValue();

        int length = ((IntToken) msg.get("length")).intValue();

        // compute the duration of this packet ( with the PHY overhead added)
        _txDuration = ((double) length / _txRate)
            + ((_aPreambleLength + _aPlcpHeaderLength) * 1e-6);

        // send TxStartConfirm to the MAC
        RecordToken TxStartConfirmMsg = new RecordToken(SignalMsgFields,
                new Token[] {
                    new IntToken(TxStartConfirm)
                });
        PHYConfirm.send(0, TxStartConfirmMsg);
        _currentState = Transmit;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // extend the default timer to link the additional info (e.g. power) to a timer
    protected class ExtendedTimer {
        public int kind;
        public Time expirationTime;
        public double power;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    //the local varibles for the parameters of this actor.
    protected int _aPreambleLength;
    protected int _aPlcpHeaderLength;
    protected double _sensitivity;
    protected double _SNRThresholdInDB;

    // message formats
    protected static final String[] RxStartMsgFields = {
        "kind",
        "rxRate"
    };
    protected static final String[] RxEndMsgFields = {
        "kind",
        "status"
    };
    protected static final String[] RxDataMsgFields = {
        "kind",
        "pdu"
    };
    protected static final String[] SignalMsgFields = {
        "kind"
    };
    protected static final String[] ChMsgFields = {
        "rate",
        "data"
    };

    // time that a packet uses the channel
    protected Variable _duration = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private RecordToken _data;
    private ExtendedTimer _chMsg;
    private double _txDuration;
    private double _receivedPower;
    private int _rxStatus;
    private int _txRate;
    private double _interference;
    private int _numBusyTimers;

    // define states in FSM
    private static final int PHY_Idle = 0; // not use Idle as state name
    private static final int Receive = 1;
    private static final int Transmit = 2;
    private int _currentState = PHY_Idle;

    // timer types
    private static final int RxDone = 1;
    private static final int InterferenceDone = 2;
    private static final int TxDone = 3;
}
