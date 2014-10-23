/* An actor that talks to the Physical layer to get permission of
 sending data.

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

import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
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
 physical layer.
 <p>
 Both TxCoordination and RxCoordination in the Protocol_Control can send
 TxRequest and require TxConfirm. This actor uses a pair of input and output
 ports to tell the source of the TxRequest and the destination of the
 TxConfirm.

 @author Yang Zhao, Charlie Zhong and Xiaojun Liu
 @version DataPump.java,v 1.18 2004/04/22 19:46:18 ellen_zh Exp
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (ellen_zh)
 @Pt.AcceptedRating Red (pjb2e)
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
        TXTXRequest = new TypedIOPort(this, "TXTXRequest", true, false);
        RXTXRequest = new TypedIOPort(this, "RXTXRequest", true, false);
        fromReception = new TypedIOPort(this, "fromReception", true, false);
        PHYLayerConfirm = new TypedIOPort(this, "PHYLayerConfirm", true, false);
        TXTXConfirm = new TypedIOPort(this, "TXTXConfirm", false, true);
        RXTXConfirm = new TypedIOPort(this, "RXTXConfirm", false, true);
        toBackoff = new TypedIOPort(this, "toBackoff", false, true);
        toPHYLayer = new TypedIOPort(this, "toPHYLayer", false, true);

        TXTXRequest.setTypeEquals(BaseType.GENERAL);
        RXTXRequest.setTypeEquals(BaseType.GENERAL);
        fromReception.setTypeEquals(BaseType.GENERAL);
        PHYLayerConfirm.setTypeEquals(BaseType.GENERAL);
        TXTXConfirm.setTypeEquals(BaseType.GENERAL);
        RXTXConfirm.setTypeEquals(BaseType.GENERAL);
        toBackoff.setTypeEquals(BaseType.GENERAL);
        toPHYLayer.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The input port for transmission request from the Protocol_Control
     *  Tx_Coordination block.
     */
    public TypedIOPort TXTXRequest;

    /** The input port for transmission request from the Protocol_Control
     *  Rx_Coordination block.
     */
    public TypedIOPort RXTXRequest;

    /** The input port for the channel status massage from the
     *  reception block.
     */
    public TypedIOPort fromReception;

    /** The input port for transmission conformation from the physical
     *  layer, including transmission start confirmation, transmitting
     *  data confirmation and transmission end confirmation.
     */
    public TypedIOPort PHYLayerConfirm;

    /** The output port for transmission confirmation to the Protocol_Control
     *  Tx_Coordination block.
     */
    public TypedIOPort TXTXConfirm;

    /** The output port for transmission confirmation to the Protocol_Control
     *  Rx_Coordination block.
     */
    public TypedIOPort RXTXConfirm;

    /** The output port that send transmission request to the physical
     *  layer, including transmission start request, transmitting
     *  data request and transmission end request.
     */
    public TypedIOPort toPHYLayer;

    /** The output port sending the the channel status to the
     *  Backoff block.
     */
    public TypedIOPort toBackoff;

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
        DataPump newObject = (DataPump) super.clone(workspace);
        return newObject;
    }

    /** Override the base class to declare that the <i>TXTXConfirm</i>
     *  and <i>RXTXConfirm</i> output ports do not
     *  depend on the <i>TXTXRequest</i> and <i>RXTXRequest</i>
     *  in a firing.
     *  @exception IllegalActionException If the causality interface
     *  cannot be computed.
     */
    @Override
    public void declareDelayDependency() throws IllegalActionException {
        // Declare that output does not immediately depend on the input,
        // though there is no lower bound on the time delay.
        _declareDelayDependency(TXTXRequest, TXTXConfirm, 0.0);
        _declareDelayDependency(RXTXRequest, RXTXConfirm, 0.0);
        _declareDelayDependency(TXTXRequest, RXTXConfirm, 0.0);
        _declareDelayDependency(RXTXRequest, TXTXConfirm, 0.0);
    }

    /** The main function
     *  @exception IllegalActionException If an error occurs reading
     *   or writing inputs or outputs.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (TXTXRequest.hasToken(0)) {
            _inputMessage = (RecordToken) TXTXRequest.get(0);
            _source = FromProtocolTx;
        } else if (RXTXRequest.hasToken(0)) {
            _inputMessage = (RecordToken) RXTXRequest.get(0);
            _source = FromProtocolRx;
        } else if (fromReception.hasToken(0)) {
            _inputMessage = (RecordToken) fromReception.get(0);
        } else if (PHYLayerConfirm.hasToken(0)) {
            _inputMessage = (RecordToken) PHYLayerConfirm.get(0);
        }

        if (_inputMessage != null) {
            _messageType = ((IntToken) _inputMessage.get("kind")).intValue();

            switch (_state) {
            case Tx_Idle:

                //_getMsgType();
                switch (_messageType) {
                case TxRequest:

                    //Note: in OMNET++, the phy layer are strangely put
                    // in the channel module. The "channel" field are used
                    // for specifying a channel. We don't need it here. Yang
                    //channel = _inputMessage.get("channel").intValue();
                    _pdu = (RecordToken) _inputMessage.get("pdu");
                    _toBackoff(Busy);

                    int length = ((IntToken) _pdu.get("Length")).intValue();
                    int rate = ((IntToken) _inputMessage.get("rate"))
                            .intValue();
                    Token[] value = { new IntToken(TxStart),
                            new IntToken(length), new IntToken(rate) };
                    toPHYLayer
                    .send(0, new RecordToken(TxStartMsgFields, value));
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
                    Token[] values = { new IntToken(TxData), _pdu };
                    RecordToken newPdu = new RecordToken(TxDataMsgFields,
                            values);
                    toPHYLayer.send(0, newPdu);
                    _state = Wait_TxEnd;
                }

                break;

            case Wait_TxEnd:

                if (_messageType == TxEnd) {
                    Token[] value = { new IntToken(TxConfirm) };
                    RecordToken confirm = new RecordToken(TxConfirmMsgFields,
                            value);

                    if (_source == FromProtocolTx) {
                        TXTXConfirm.send(0, confirm);
                    } else {
                        RXTXConfirm.send(0, confirm);
                    }

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
    @Override
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
    ////                         private methods                   ////
    private void _toBackoff(int kind) throws IllegalActionException {
        // send idle/busy event to the backoff block
        Token[] value = { new IntToken(kind) };
        RecordToken t = new RecordToken(CSMsgFields, value);
        toBackoff.send(0, t);
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
    private static final int Tx_Idle = 0;

    private static final int Wait_TxStart = 1;

    private static final int Wait_TxEnd = 2;

    private int _state = 0;

    private int _source = 0;

    private static final int FromProtocolTx = 0;

    private static final int FromProtocolRx = 1;

    private RecordToken _pdu;

    private RecordToken _inputMessage;

    private int _messageType;
}
