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

import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// DataPump

/** 
This actor works in the Transmission block in IEEE 802.11 Mac. For every 
TxRequest from the Protocol_Control block, this actor sends a PhyTxStart
request to the physical layer. Upon receiving the PhyTxStart confirmation,
it sends data to the physical layer. After the data has been sent, it sends
PhyTxEnd request to the physical layer and sends TxConfirm to the source of
the original TxRequest after receiving PhyTxEnd confirmation from the 
physcal layer.
<p>
Both TxCoordination and RxCoordination in the Protocol_Control can send 
TxRequest and require TxConfirm. This actor uses a pair of input and output
ports to tell the source of the TxRequest and the destination of the 
TxConfirm. 

@author Yang Zhao, Charlie Zhong and Xiaojun Liu
@version $Id$
@since Ptolemy II 2.1
*/
public class DataPump extends MACActorBase {

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
    public DataPump(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create and configure the ports.       
        fromProtocolTx = new TypedIOPort(this, "fromProtocolTx", true, false);
        fromProtocolRx = new TypedIOPort(this, "fromProtocolRx", true, false);
        fromCs = new TypedIOPort(this, "fromCs", true, false);
        fromPhysical = new TypedIOPort(this, "fromPhysical", true, false);
        toProtocolTx = new TypedIOPort(this, "toProtocolTx", false, true);
        toProtocolRx = new TypedIOPort(this, "toProtocolRx", false, true);
        forwardCs = new TypedIOPort(this, "forwardCs", false, true);
        toPhysical = new TypedIOPort(this, "toPhysical", false, true);
        
        fromProtocolTx.setTypeEquals(BaseType.GENERAL);
        fromProtocolRx.setTypeEquals(BaseType.GENERAL);
        fromCs.setTypeEquals(BaseType.GENERAL);
        fromPhysical.setTypeEquals(BaseType.GENERAL);
        toProtocolTx.setTypeEquals(BaseType.GENERAL); 
        toProtocolRx.setTypeEquals(BaseType.GENERAL); 
        forwardCs.setTypeEquals(BaseType.GENERAL); 
        toPhysical.setTypeEquals(BaseType.GENERAL);     
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The input port for transmission request from the Protocol_Control
     *  Tx_Coordination block.
     */
    public TypedIOPort fromProtocolTx;
    
    /** The input port for transmission request from the Protocol_Control
     *  Rx_Coordination block.
     */
    public TypedIOPort fromProtocolRx;
    

    /** The input port for the channel status massage from the 
     *  reception block.
     */
    public TypedIOPort fromCs;
    
    /** The input port for transmission conformation from the physical
     *  layer, including transmission start comfirmation, transmiting
     *  data comfirmation and transmission end comfirmation. 
     */
    public TypedIOPort fromPhysical;
    
    /** The output port for transmission confirmation to the Protocol_Control
     *  Tx_Coordination block.
     */
    public TypedIOPort toProtocolTx;
    
    /** The output port for transmission confirmation to the Protocol_Control
     *  Rx_Coordination block.
     */
    public TypedIOPort toProtocolRx;
    
    /** The output port that send transmission request to the physical
     *  layer, including transmission start request, transmiting
     *  data request and transmission end request. 
     */
    public TypedIOPort toPhysical;
    
    /** The output port sending the the channel status to the
     *  Backoff block.
     */
    public TypedIOPort forwardCs;
    
    
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
        DataPump newObject = (DataPump)super.clone(workspace);
        return newObject;
    }

    /** The main function
     *  @exception IllegalActionException If an error occurs reading
     *   or writing inputs or outputs.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        
        Director director = getDirector();
        if (fromProtocolTx.hasToken(0)) {
            _inputMessage = (RecordToken) fromProtocolTx.get(0);
            _source = FromProtocolTx;
        } else if (fromProtocolRx.hasToken(0)) {
            _inputMessage = (RecordToken) fromProtocolRx.get(0);
            _source = FromProtocolRx;
        } else if (fromCs.hasToken(0)) {
            _inputMessage = (RecordToken) fromCs.get(0);
        } else if (fromPhysical.hasToken(0)) {
            _inputMessage = (RecordToken) fromPhysical.get(0);
        }
        if(_inputMessage != null) {
            _messageType = ((IntToken)
                    _inputMessage.get("kind")).intValue();
            switch (_state) {
                case Tx_Idle:
    		        //_getMsgType();
                    switch(_messageType) {
                        case TxRequest:
                            //Note: in OMNET++, the phy layer are strangely put
                            // in the channel module. The "channel" field are used
                            // for specifying a channel. We don't need it here. Yang
                            //channel = _inputMessage.get("channel").intValue();
                            
                            
                            _pdu = (RecordToken)(_inputMessage.get("pdu")); 
                            _toBackoff(Busy);
                            int length = ((IntToken)_pdu.get("length")).intValue();
                            int rate = ((IntToken)_inputMessage.get("rate")).intValue();
                            Token[] value = {new IntToken(TxStart),
                                             new IntToken(length),
                                             new IntToken(rate)};
                            toPhysical.send(0, new RecordToken(TxStartMsgFields, value));
                            _state = Wait_TxStart;
                            
                        break;
    
                        case Idle:
                        case Slot:
                        case Busy:
                          _toBackoff(_messageType);
                        break;
                    }
                break;
                    
                case Wait_TxStart:
                    if (_messageType == TxStartConfirm) {
                        int pduType = ((IntToken) _pdu.get("Type")).intValue();
                        int pduSubtype = ((IntToken) _pdu.get("Subtype")).intValue();
                        String pduName = " ";
                        switch(pduType) {
                            case ControlType:
                              switch(pduSubtype)
                              {
                              case Ack:
                                pduName = "ACK";
                              break;
        
                              case Cts:
                                pduName ="CTS";
                              break;
        
                              case Rts:
                                pduName = "RTS";
                              break;
                              }
                            break;
        
                            case DataType:
                              if (pduSubtype==Data)
                                pduName = "Data";
                            break;
                        }
                       String[] labels = {"kind", "name"};
                       Token[] values = {new IntToken(TxData), new StringToken( pduName)};  
                       
                       RecordToken mergeToPdu = new RecordToken(labels, values); 
                       RecordToken newPdu = RecordToken.merge(mergeToPdu, _pdu);
                       toPhysical.send(0, newPdu);
                       _state = Wait_TxEnd;
                    
                    }
                break;
    
                case Wait_TxEnd:
                  if (_messageType == TxEnd) {
                      Token[] value = {new IntToken(TxConfirm)};
                      RecordToken confirm = new RecordToken(TxConfirmMsgFields, value);
                      if (_source == FromProtocolTx)
                          toProtocolTx.send(0, confirm);
                      else
                          toProtocolRx.send(0, confirm);
                      _state = Tx_Idle;
                  }
                break;
            }
            _inputMessage = null;
            _messageType = UNKNOWN;
        }
    }
    
    /** Initialize the private variables.
     *  @exception IllegalActionException If thrown by the base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _state = 0;
        _source = 0;
        _inputMessage = null;
        //_message = null;
        _messageType = UNKNOWN;
        _pdu = null;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                 ////

    private void _toBackoff(int kind) throws IllegalActionException {
        // send idle/busy event to the backoff block
        Token[] value = {new IntToken(kind)};
        RecordToken t = new RecordToken(CSMsgFields, value);
        forwardCs.send(0, t);
    }

 /**   private void _getMsgType() throws IllegalActionException {

	    if (channelStatus.hasToken(0)) {
            _inputMessage = (RecordToken) channelStatus.get(0);
        } else if (fromFilterMpdu.hasToken(0)) {
            _inputMessage = (RecordToken) fromFilterMpdu.get(0);
	    }
        if(_inputMessage != null) {
            _messageType = ((IntToken)
            _inputMessage.get("kind")).intValue();
        } 
    }
*/    

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    //Define the states of the inside FSM.
    private static final int Tx_Idle    = 0;
    private static final int Wait_TxStart = 1;
    private static final int Wait_TxEnd = 2;

    
    private int _state=0;
    private int _source = 0;
    private static final int FromProtocolTx = 0; 
    private static final int FromProtocolRx = 1; 
    private RecordToken _pdu;

    private RecordToken _inputMessage;
    private int _messageType;
    private double _currentTime;

}
