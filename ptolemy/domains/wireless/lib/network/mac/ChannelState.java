/* An actor that maintains the channel state based on both the result of carrier sense
 * and the reservation (NAV).

 Copyright (c) 2004-2005 The Regents of the University of California.
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

 */
package ptolemy.domains.wireless.lib.network.mac;

import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ChannelState

/**
 This actor updates the channel state based on the information from PHY
 and NAV (Network Allocation Vector). To speed up simulation, slot events
 in 802.11 are not generated here.
 @author Yang Zhao
 @version ChannelState.java,v 1.21 2004/04/22 19:46:18 ellen_zh Exp
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (ellen_zh)
 @Pt.AcceptedRating Red (pjb2e)
 */
public class ChannelState extends MACActorBase {
    /** Construct an actor with the specified name and container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the empty string.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container.
     *  @param name The name of the actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ChannelState(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create and configure the ports.
        channelStatus = new TypedIOPort(this, "channelStatus", true, false);

        //set the port to be a south port if it hasn't been configured.
        /**StringAttribute cardinal = (StringAttribute)
         channelStatus.getAttribute("_cardinal");
         if (cardinal == null) {
         StringAttribute thisCardinal =
         new StringAttribute(channelStatus, "_cardinal");
         thisCardinal.setExpression("SOUTH");
         }*/
        fromFilterMpdu = new TypedIOPort(this, "fromFilterMpdu", true, false);
        fromValidateMpdu = new TypedIOPort(this, "fromValidateMpdu", true,
                false);
        toTransmission = new TypedIOPort(this, "toTransmission", false, true);

        channelStatus.setTypeEquals(BaseType.GENERAL);
        fromValidateMpdu.setTypeEquals(BaseType.GENERAL);
        fromFilterMpdu.setTypeEquals(BaseType.GENERAL);
        toTransmission.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The input port for channel status message from the
     *  physical layer.  This has undeclared type.
     */
    public TypedIOPort channelStatus;

    /** The input port for messages from ValidateMpdu process.
     *  Two types of messages: UseIfs and RtsTimeout
     */
    public TypedIOPort fromValidateMpdu;

    /** The input port for setNav message from FilterMpdu.
     */
    public TypedIOPort fromFilterMpdu;

    /** The output port that produces messages that
     *  indicate the channel status.
     */
    public TypedIOPort toTransmission;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ChannelState newObject = (ChannelState) super.clone(workspace);

        /*
         String[] labels = {"type", "content"};
         Type[] types = {BaseType.INT, BaseType.GENERAL};
         RecordType recordType = new RecordType(labels, types);

         newObject.channelStatus.setTypeAtMost(recordType);
         newObject.fromFilterMpdu.setTypeAtMost(recordType);
         newObject.fromValidateMpdu.setTypeAtMost(recordType);
         //no need to set type constraint for toTransmission since
         //it has an absolute constraint.
         *
         */
        return newObject;
    }

    /** The main function
     *  @exception IllegalActionException If an error occurs reading
     *   or writing inputs or outputs.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        Director director = getDirector();
        _currentTime = director.getModelTime();

        int kind = whoTimeout();

        // if a timer is processed, should not consume the message token
        // kind = UNKNOWN means no timer event.
        if (kind == UNKNOWN) {
            //THE SDL specification has don-deterministic
            //transition.However the order does not matter here,
            // so we will pick a particular order.
            // UseIfs messages are handled in all states
            if (fromValidateMpdu.hasToken(0)) {
                _inputMessage = (RecordToken) fromValidateMpdu.get(0);
                _messageType = ((IntToken) _inputMessage.get("kind"))
                        .intValue();

                if (_messageType == UseDifs || _messageType == UseEifs) {
                    if (_messageType == UseDifs) {
                        _dIfs = _dDIfs - _aRxTxTurnaroundTime;
                    } else if (_messageType == UseEifs) {
                        _dIfs = _dEIfs - _aRxTxTurnaroundTime;
                    }

                    if (_debugging) {
                        _debug("the msg token received is : "
                                + _inputMessage.toString());
                    }

                    DoubleToken t = (DoubleToken) _inputMessage.get("tRxEnd");
                    Time tRxEnd = new Time(director, t.doubleValue());

                    if (_IfsTimer != null) {
                        cancelTimer(_IfsTimer);
                    }

                    _IfsTimer = setTimer(IfsTimeOut, tRxEnd.add(_dIfs * 1e-6));
                }
            } else if (channelStatus.hasToken(0)) {
                _inputMessage = (RecordToken) channelStatus.get(0);
            } else if (fromFilterMpdu.hasToken(0)) {
                _inputMessage = (RecordToken) fromFilterMpdu.get(0);
            }

            if (_inputMessage != null) {
                _messageType = ((IntToken) _inputMessage.get("kind"))
                        .intValue();
            }
        }

        switch (_state) {
        case Cs_noNav:

            //_getMsgType();
            switch (_messageType) {
            case Idle:

                // if channel becomes idle,set timer and goes to Wait_Ifs state
                if (_IfsTimer != null) {
                    cancelTimer(_IfsTimer);
                }

                _IfsTimer = setTimer(IfsTimeOut, _currentTime.add(_dIfs * 1e-6));
                _state = Wait_Ifs;
                break;

            case SetNav:

                // if a reservation is needed, make it and goes to Cs_Nav state
                if (_setNav()) {
                    _state = Cs_Nav;
                }

                break;
            }

            break;

        case Wait_Ifs:

            if (kind == IfsTimeOut) {
                // if channel remains idle for the whole IFS duration,
                // let the Transmission block know.
                _changeStatus(Idle);
                _state = noCs_noNav;
            } else {
                // if we have processed IfsTimer, we will not consume this token
                //_getMsgType();
                switch (_messageType) {
                case Busy:
                    _state = Cs_noNav;
                    break;

                case SetNav:

                    if (_setNav()) {
                        //From OMNET: original standard
                        //FSM_Goto(fsm,Cs_Nav);
                        // modify standard here
                        _state = noCs_Nav;
                    }

                    break;
                }
            }

            break;

        case noCs_noNav:

            //_getMsgType();
            switch (_messageType) {
            case Busy:
                _changeStatus(Busy);
                _state = Cs_noNav;
                break;

            case SetNav:

                if (_setNav()) {
                    _changeStatus(Busy);
                    _state = noCs_Nav;
                }

                break;
            }

            break;

        case Cs_Nav:

            if (kind == NavTimeOut) {
                _state = Cs_noNav;
            } else {
                //_getMsgType();
                if (_messageType == Idle) {
                    _state = noCs_Nav;
                } else {
                    // RtsTimeout token will be consumed if NavTimeout
                    // is processed, but we ignore it anyway in the
                    // to be transitioned to: noCs_Nav or Wait_Ifs.
                    _updateNav();
                }
            }

            break;

        case noCs_Nav:

            // if the reservation is over, goes to Wait_Ifs state.
            if (kind == NavTimeOut) {
                if (_IfsTimer != null) {
                    cancelTimer(_IfsTimer);
                }

                _IfsTimer = setTimer(IfsTimeOut, _currentTime.add(_dIfs * 1e-6));
                _state = Wait_Ifs;
            } else {
                //_getMsgType();
                if (_messageType == Busy) {
                    _state = Cs_Nav;
                } else {
                    _updateNav();
                }
            }

            break;
        }

        _inputMessage = null;
        _messageType = UNKNOWN;
        kind = -1;
    }

    /** Initialize the private variables.
     *  @exception IllegalActionException If thrown by the base class.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _dDIfs = _aSifsTime + 2 * _aSlotTime;
        _dEIfs = _aSifsTime + _sAckCtsLng / _mBrate + _aPreambleLength
                + _aPlcpHeaderLength + _dDIfs;
        _dIfs = _dEIfs;
        _state = 0;
        _curSrc = nosrc;
        _inputMessage = null;

        //_message = null;
        _messageType = UNKNOWN;
        _IfsTimer = null;
        _NavTimer = null;

        NamedObj macComposite = getContainer().getContainer();

        if (macComposite.getAttribute("tNavEnd") != null) {
            _tNavEnd = macComposite.getAttribute("tNavEnd");
        } else {
            _tNavEnd = null;
            throw new IllegalActionException("the MAC compositor "
                    + "dosen't contain a parameter named tNavEnd");
        }

        // First assume channel is busy until PHY sends an idle event
        _changeStatus(Busy);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void _updateNav() throws IllegalActionException {
        switch (_messageType) {
        case SetNav:

            // new NAV
            Time tNew = new Time(getDirector(),
                    ((DoubleToken) _inputMessage.get("tRef")).doubleValue()
                    + ((IntToken) _inputMessage.get("dNav")).intValue()
                    * 1e-6);

            // if the new NAV is larger than the existing one, use it instead
            if (tNew.compareTo(_NavTimer.expirationTime) > 0) {
                _NavTimer.expirationTime = tNew;
                _setAttribute(_tNavEnd, new DoubleToken(tNew.getDoubleValue()));
                _curSrc = ((IntToken) _inputMessage.get("src")).intValue();
            }

            break;

        case RtsTimeout:

            if (_curSrc != Rts) {
                break;
            }

        case ClearNav:

            // force the state transition to the corresponding noNav states
            _NavTimer.expirationTime = _currentTime;
            _setAttribute(_tNavEnd,
                    new DoubleToken(_currentTime.getDoubleValue()));
            _curSrc = nosrc;
            break;
        }
    }

    private boolean _setNav() throws IllegalActionException {
        Time expirationTime = new Time(getDirector(),
                ((DoubleToken) _inputMessage.get("tRef")).doubleValue()
                + ((IntToken) _inputMessage.get("dNav")).intValue()
                * 1e-6);
        _setAttribute(_tNavEnd,
                new DoubleToken(expirationTime.getDoubleValue()));

        if (expirationTime.compareTo(_currentTime) > 0) {
            if (_NavTimer != null) {
                cancelTimer(_NavTimer);
            }

            _NavTimer = setTimer(NavTimeOut, expirationTime);
            return true;
        } else {
            return false;
        }
    }

    private void _changeStatus(int kind) throws IllegalActionException {
        // send idle/busy event to the Transmission block
        Token[] value = { new IntToken(kind) };
        RecordToken t = new RecordToken(CSMsgFields, value);
        toTransmission.send(0, t);
    }

    /**   private void _getMsgType() throws IllegalActionException {

     if (channelStatus.hasToken(0)) {
     _inputMessage = (RecordToken) channelStatus.get(0);
     } else if (fromFilterMpdu.hasToken(0)) {
     _inputMessage = (RecordToken) fromFilterMpdu.get(0);
     }
     if (_inputMessage != null) {
     _messageType = ((IntToken)
     _inputMessage.get("kind")).intValue();
     }
     }
     */

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    //Define the states of the inside FSM.
    private static final int Cs_noNav = 0;

    private static final int Cs_Nav = 1;

    private static final int Wait_Ifs = 2;

    private static final int noCs_Nav = 3;

    private static final int noCs_noNav = 4;

    private int _state = 0;

    //the distributed inter frame space.
    private int _dDIfs;

    //the extended inter frame space.
    private int _dEIfs;

    //the applying inter frame space.
    private int _dIfs;

    private int _curSrc;

    private Timer _IfsTimer;

    private Timer _NavTimer;

    // timer types
    private static final int IfsTimeOut = 0;

    private static final int NavTimeOut = 1;

    private RecordToken _inputMessage;

    private int _messageType;

    private Time _currentTime;
}
