/* An actor that provides the channel status.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (pjb2e@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.lib.network.mac;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ChannelState

/** 
This actor update the channel status based on the information from PHY
and NAV (Natwork Allocation Vector).


@author Yang Zhao
@version $ $
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
        //set the port to be a south port if it hasn't been configured
        StringAttribute cardinal = (StringAttribute)
                channelStatus.getAttribute("_cardinal");
        if (cardinal == null) {
            StringAttribute thisCardinal =  
                    new StringAttribute(channelStatus, "_cardinal");
            thisCardinal.setExpression("SOUTH");
        }
        
        updateNav = new TypedIOPort(this, "updateNav", true, false);
        
        IfsControl = new TypedIOPort(this, "IfsControl", true, false);
        
        fromControl = new TypedIOPort(this, "fromControl", true, false);
        
        toTransmission = new TypedIOPort(this, "toTransmission", false, true);
        //Don't infer the type from input.
        toTransmission.setTypeEquals(BaseType.UNKNOWN);     
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The input port for channel status message from the 
     *  physical layer.  This has undeclared type.
     */
    public TypedIOPort channelStatus;
    
    /** The input port for update NAV message. 
     */
    public TypedIOPort updateNav;
    
    /** The input port for setting up inter frame time..
     */
    public TypedIOPort IfsControl;
    
    /** The input port for NAV change message from the
     *  protocol control block.
     */
    public TypedIOPort fromControl;
    
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
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        ChannelState newObject = (ChannelState)super.clone(workspace);
        return newObject;
    }

    /** FIXME: add doc here.
     *  @exception IllegalActionException If an error occurs reading
     *   or writing inputs or outputs.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        Director director = getDirector();
        double currentTime = director.getCurrentTime();
        
        //FIXME: THE SDL specification seems has don-deterministic
        //transition. What should it do if multi inputs have token?
        if (IfsControl.hasToken(0)) {
            _inputMessage = (RecordToken)IfsControl.get(0);
            _messageType = ((IntToken)_inputMessage.
                    get("type")).intValue();
            _message = (RecordToken)_inputMessage.get("message");
            if (_messageType == (UseDifs)) {
                _dIfs = _dDIfs - _RxTxTurnaroundTime;
            } else if (_messageType == UseEifs){
                _dIfs = _dEIfs - _RxTxTurnaroundTime;
            }
            int tRxEnd = ((IntToken) _message.get("tRxEnd"))
                    .intValue();
            _IfsTimer = currentTime + tRxEnd + _dIfs*1e-6;
            director.fireAt(this, _IfsTimer);

        } else if (channelStatus.hasToken(0)) {
            _inputMessage = (RecordToken) channelStatus.get(0);
        } else if (updateNav.hasToken(0)){
            _inputMessage = (RecordToken) updateNav.get(0);
        } else if (fromControl.hasToken(0)){
            _inputMessage = (RecordToken) fromControl.get(0);
        } else if( currentTime == _IfsTimer) {
            _IfsTimeOut = true;
            _IfsTimer = CancelTimer;
        } else if( currentTime == _NavTimer) {
            _NavTimeOut = true;
            _NavTimer = CancelTimer;
        } else if( currentTime == _slotTimer) {
            _slotTimeOut = true;
            _slotTimer = CancelTimer;
        }
        if(_inputMessage != null) {
            _messageType = ((IntToken)
                    _inputMessage.get("type")).intValue();
            _message = (RecordToken)_inputMessage.get("message");
        } 
        
        switch (_state) {
            case Cs_noNav:
                switch(_messageType) {
                    case Idle:
                    _IfsTimer = currentTime + _dIfs*1e-6;
                    director.fireAt(this, _IfsTimer); 
                    _state = Wait_Ifs;
                    break;
                    
                    case SetNav:
                        if(_setNav()) {
                            _state = Cs_Nav;
                        }
                    break;     
                }
            break;
            
            case Wait_Ifs:
                if (_IfsTimeOut) {
                    if (_slotTimer > -1) {
                        //set it to a negtive value to cancel a timer.
                        _slotTimer = CancelTimer;
                    }
                    //From OMNET: modify standard here
                    //scheduleAt(simTime()+dSlot*1e-6, Tslot).
                    _state = noCs_noNav;
                    RecordToken t = _createRecordToken(Idle, null);
                    toTransmission.send(0, t);
                    
                } else { 
                    switch(_messageType) {
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
                if (_slotTimeOut){
                    toTransmission.send(0, _createRecordToken(Slot, null));
                    //From OMNET: modify standard here
                    //scheduleAt(simTime()+dSlot*1e-6, Tslot);
                 } else {
                    switch(_messageType){
                        case Busy:
                            _state = Cs_noNav;
                            toTransmission.send(0, _createRecordToken(Busy, null));
                        break;

                        case SetNav:
                           if (_setNav()){
                               toTransmission.send(0, _createRecordToken(Busy, null));
                               _state = noCs_Nav;
                           }
                        break;
                    }
                }
            break;

            case Cs_Nav:
                if (_NavTimeOut) {
                    _state = Cs_noNav;
                } else {
                    if (_messageType == Idle) 
                        _state = noCs_Nav;
                    else
                        _updateNav();
                }
            break;

            case noCs_Nav:
                  if (_NavTimeOut) {
                      _IfsTimer = currentTime + _dIfs*1e-6;
                      _state = Wait_Ifs;
                  } else {
                      if (_messageType == Busy)
                          _state = Cs_Nav;
                      else
                          _updateNav();
                      }
            break;
        }
        _inputMessage = null;
        _messageType = UNKNOWN;
        _message = null;         
    }
    
    /** Initialize the private variables.
     *  @exception IllegalActionException If thrown by the base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _dDIfs = _SIfsTime+2*_slotTime;
        _dEIfs= _SIfsTime+_sAckCtsLng/_mBrate+
                _preambleLength+_PLCPHeaderLength+_dDIfs;
        _dIfs=_dEIfs;
        _state = 0;
        _curSrc = nosrc;
        _inputMessage = null;
        _message = null;
        _messageType = UNKNOWN;
        _IfsTimer = _NavTimer = _slotTimer = CancelTimer;
        _IfsTimeOut = _NavTimeOut = _slotTimeOut = false;
        
        //FIXME:Why I need to output a "Busy" state at the beginning?
        //Seems it won't change anything in the down stream components...
        toTransmission.send(0, _createRecordToken(Busy, null));
    }
    
    private void _updateNav() throws IllegalActionException {
        double tNew;
        
        switch(_messageType) {
            case SetNav:
            
                tNew = ((IntToken)_message.get("tRef")).intValue()
                       +((IntToken)_message.get("dNav")).intValue()*1e-6;
                if (tNew > _NavTimer) {
                    _NavTimer = tNew;
                    //curSrc=((SetNavMsg *)msg)->src;
                    _curSrc=((IntToken)_message.get("src")).intValue();
                    Director director = getDirector();
                    director.fireAt(this, tNew);
                }
             break;

             case RtsTimeout:
                 if (_curSrc!=Rts)
             break;

             case ClearNav:
                Director director = getDirector();
                double currentTime = director.getCurrentTime();
                _NavTimer = currentTime;
                _curSrc=nosrc;
                director.fireAt(this, _NavTimer);
             break;
      
         }
    }

    private boolean _setNav() throws IllegalActionException {
        Director director = getDirector();
        double currentTime = director.getCurrentTime();
        _NavTimer =  ((IntToken)_message.get("tRef")).intValue()
                   +((IntToken)_message.get("dNav")).intValue()*1e-6;
        if(_NavTimer >= currentTime) {
            director.fireAt(this, _NavTimer);
            return true;
        } else {
            return false;
        }
    }
    
    private RecordToken _createRecordToken(int type, RecordToken message) 
            throws IllegalActionException {
        String[] labels = {"type", "message"};
        RecordToken newMessage = message;
        if(message == null ) {
            String[] subLabels = {"ChannelStatus"};
            Token[] subValue = {new IntToken(type)};
            newMessage = new RecordToken(subLabels, subValue);
        } 
        
        Token[] value = {new IntToken(type),
                         newMessage
        };
        return new RecordToken(labels, value);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    //Define the states of the inside FSM.
    private static final int Cs_noNav   = 0;
    private static final int Cs_Nav     = 1;
    private static final int Wait_Ifs   = 2;
    private static final int noCs_Nav   = 3;
    private static final int noCs_noNav = 4;
    
    private int _state;
    
    //the distributed inter frame space.
    private int _dDIfs;
    //the extended inter frame space.
    private int _dEIfs;
    //the applying inter frame space.
    private int _dIfs;
    
    private int _curSrc;
    
    private double _IfsTimer;
    private boolean _IfsTimeOut;
    private double _NavTimer;
    private boolean _NavTimeOut;
    private double _slotTimer;
    private boolean _slotTimeOut;
    
    private static int CancelTimer = -1;
    private RecordToken _inputMessage;
    private int _messageType;
    private RecordToken _message;
    
    private List _receptions = new LinkedList();
}
