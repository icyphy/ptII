/* An actor that maintains the channel state based on both the result of carrier sense
 * and the reservation (NAV).

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

import java.util.Random;

import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Backoff

/** 


@author Yang Zhao
@version $Id$
@since Ptolemy II 2.1
*/
public class Backoff extends MACActorBase {

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
    public Backoff(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create and configure the ports.       
        channelStatus = new TypedIOPort(this, "channelStatus", true, false);
        fromProtocol = new TypedIOPort(this, "fromProtocol", true, false);
        toProtocol = new TypedIOPort(this, "toProtocol", false, true);
        
        channelStatus.setTypeEquals(BaseType.GENERAL);
        fromProtocol.setTypeEquals(BaseType.GENERAL);
        toProtocol.setTypeEquals(BaseType.GENERAL); 
        
        seed = new Parameter(this, "seed", new LongToken(0));
        seed.setTypeEquals(BaseType.LONG);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The input port for channel status message.  
     *  This has undeclared type.
     */
    public TypedIOPort channelStatus;
    

    /** The input port for backoff message from the Protocol control block.
     */
    public TypedIOPort fromProtocol;
    

    /** The output port that produces messages that
     *  indicate the backoff is done.
     */
    public TypedIOPort toProtocol;
    
    /** The seed that controls the random number generation.
     *  A seed of zero is interpreted to mean that no seed is specified,
     *  which means that each execution of the model could result in
     *  distinct data. For the value 0, the seed is set to
     *  System.currentTimeMillis() + hashCode(), which means that
     *  with extremely high probability, two distinct actors will have
     *  distinct seeds.  However, current time may not have enough
     *  resolution to ensure that two subsequent executions of the
     *  same model have distinct seeds.
     *  
     *  This parameter contains a LongToken, initially with value 0.
     */
    public Parameter seed;
    
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
        Backoff newObject = (Backoff)super.clone(workspace);
        return newObject;
    }

    /** The main function
     *  @exception IllegalActionException If an error occurs reading
     *   or writing inputs or outputs.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        int ct = 0;
        Director director = getDirector();
        _currentTime = director.getCurrentTime();
	    int kind=whoTimeout();
        // if a timer is processed, should not consume the message token
        // kind = -1 means no timer event.
        if (kind == -1) { 
            if (fromProtocol.hasToken(0)) {
                _inputMessage = (RecordToken) fromProtocol.get(0);
            } else if (channelStatus.hasToken(0)) {
                _inputMessage = (RecordToken) channelStatus.get(0);
            } 
            if(_inputMessage != null) {
                _messageType = ((IntToken)
                        _inputMessage.get("kind")).intValue();

            } 
        }
                
        switch (_state) {
            case No_Backoff:
              switch (_messageType){
                  case Backoff:
                      _mBkIP = true;
                      _cnt = ((IntToken)_inputMessage.get("cnt")).intValue();
                      if (_cnt<0) {
                          int ccw = ((IntToken)_inputMessage.get("ccw")).intValue();
                          _slotCnt = _generateRandom(ccw);
                      } else
                          _slotCnt = _cnt;
                      if (_status == Idle)
                           _startBackoff();
                      else {
                          _cnt = 1; //backoff done will return with -1; another backoff will follow
                          _state = Channel_Busy;
                      }
                  break;
                  case Idle:
                      _status=Idle;
                  break;

                  case Busy:
                      _status=Busy;
                  break;
              }
            break;
            
            case Channel_Busy:
              switch(_messageType) {
                  // modify standard here
                  case Idle:
                    _startBackoff();
                      break;
                  // end modification
    
                  case Cancel:
                    _backoffDone(_slotCnt);
                  break;
              }
            break;

            case Channel_Idle:
              // modify standard here
              if (kind == BackoffTimeOut)
              {
                  if (_cnt == 0)
                      ct = -2;
                  else
                      ct = -1;
                  _backoffDone(ct);
              }
              switch(_messageType){
              // modify standard here
                  case Busy:
                      _slotCnt -= (int)((_currentTime - _backoffStartTime)*1e6/_aSlotTime);
                      cancelTimer(_BackoffTimer);
                      _state = Channel_Busy;
                      _status=Busy;
                  break;
                  // end modification

                  case Cancel:
                      _backoffDone(_slotCnt);
                  break;
              }
            break;
        }
        _inputMessage = null;
        _messageType = UNKNOWN;   
    }
    
    /** Initialize the private variables.
     *  @exception IllegalActionException If thrown by the base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        long sd = ((LongToken)(seed.getToken())).longValue();
        if (sd != (long)0) {
            _random.setSeed(sd);
        } else {
            _random.setSeed(System.currentTimeMillis() + hashCode());
        }
        _inputMessage = null;
        //_message = null;
        _messageType = UNKNOWN;
        _status = Busy;
        _state = No_Backoff;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                 ////
    private int _generateRandom(int ccw){
        double r = _random.nextDouble();
        return (int)Math.ceil(ccw * r);
    }
    
    private void _backoffDone(int cnt) throws IllegalActionException {
        Token[] value = {new IntToken(BkDone), new IntToken(_cnt)};
        toProtocol.send(0, new RecordToken(BackoffDoneMsgFields, value));
        _mBkIP=false;
        _state = No_Backoff;
    }

    private void _startBackoff() throws IllegalActionException {
        _backoffStartTime = _currentTime;
        _BackoffTimer = setTimer(BackoffTimeOut, _currentTime + _slotCnt*_aSlotTime*1e-6);
        _state = Channel_Idle;
        _status=Idle;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    //Define the states of the inside FSM.
    private static final int No_Backoff   = 0;
    private static final int Channel_Busy = 1;
    private static final int Channel_Idle = 2;

    private int _state=0;
    private int _slotCnt;
    private int _cnt; 
    private int _status; 
    private boolean _mBkIP;
    
    private Timer _BackoffTimer;
    // timer types
    private static final int BackoffTimeOut=0;
    
    private double _backoffStartTime = 0.0;

    private RecordToken _inputMessage;
    private int _messageType;
    private double _currentTime;
    
    protected Random _random = new Random();

}
