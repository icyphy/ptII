
/* Controls the sequence of events involved in initiating a session.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@ProposedRating Red (czhong@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.lib.network.mac;
import java.util.LinkedList;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

////////////////////////////////////////////////////////////////////////=
//
//// TXCoordinationSta
/**
TXCoordination class is responsible for initiating a session. After a packet
arrives from the network layer, TXCoordination will generate RTS if the packet
is long enough. It will send RTS to the destination and wait for CTS. If CTS
is received within a specified interval, data will be sent after SIFS seconds.
If ACK is received within a given time after the data is sent, the session 
is complete. TXCoordination will go to backoff before handling the next packet
in the queue.If the network packet is not long enough, RTS/CTS will not be used
to reduce overhead. In either case, Carrier sense is only done for the first
message in the sequence, which is RTS, retransmitted data or data if RTS is not
used. TXCoordination gets the channel status by sending a backoff signal with
count 0 to the Backoff process in the Transmission block.If the channel turns
to be busy, TXCoordination will send another backoff signal with count -1 to
start the real backoff. If CTS or ACK is not received in time, the corrresponding
RTS or data will neeed to be retransmitted. TXCoordination will increase the
corresponding retry counters. The backoff window size is also exponentially
increased Retransmission will not start until another backoff is completed to
avoid congestions.



@author Charlie Zhong
@version $Id$
*/

public class TxCoordination extends MACActorBase {
   
    /** Construct an actor with the specified name and container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the empty =
string.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container.
     *  @param name The name of the actor.
     *  @exception IllegalActionException If the container is incompatib=
le
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public TxCoordination(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
       
     
        // create ports
        PduRequest =new TypedIOPort(this, "PduRequest", true, false);
        PduRequest.setTypeEquals(BaseType.GENERAL);

        TXTXConfirm = new TypedIOPort(this, "TXTXConfirm", true, false);
        TXTXConfirm.setTypeEquals(BaseType.GENERAL);

        BkDone = new TypedIOPort(this, "BkDone", true, false);
        BkDone.setTypeEquals(BaseType.GENERAL);

        GotAck = new TypedIOPort(this, "GotAck", true, false);
        GotAck.setTypeEquals(BaseType.GENERAL);

        fromPowerControl = new TypedIOPort(this, "fromPowerControl", true, false);
        fromPowerControl.setTypeEquals(BaseType.GENERAL);

        getBackoff = new TypedIOPort(this, "getBackoff", false, true);
        getBackoff.setTypeEquals(BaseType.GENERAL);

        TXTXRequest = new TypedIOPort(this, "TXTXRequest", false, true);
        TXTXRequest.setTypeEquals(BaseType.GENERAL);

}
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////


    /** Port receiving messages from the network layer
     */
    public TypedIOPort PduRequest;

    /** Port receiving confirmation from the Transmission block
     */
    public TypedIOPort TXTXConfirm;

    /** Port receiving backoff complete notification from the Backoff process
     */
    public TypedIOPort BkDone;

    /** Port receiving the notification of the receipt of either Ack or Cts
     *  from the RxCoordination process
     */
    public TypedIOPort GotAck;

    /** Port receiving messages from the PowerControl block
     */
    public TypedIOPort fromPowerControl;

    /** Port sending the backoff request to the Backoff process
     */
    public TypedIOPort getBackoff;

    /** Port sending the TX request to the Transmission block
     */
    public TypedIOPort TXTXRequest;

    

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////



    public void fire() throws IllegalActionException {
  super.fire();
  int kind=whoTimeout();	// check if a timer times out and which
  double currentTime =getDirector().getCurrentTime();

  boolean isNetData=false;

  if (PduRequest.hasToken(0)) 
      {
	    isNetData=true;
          RecordToken msg= (RecordToken)PduRequest.get(0);
	 
          if (_txQueue.size() >= QueueSize)
            if (_debugging) {
                _debug("Queue is full.");}
          else
            _txQueue.add(msg);
      }

  switch(_currentState)
      {
      case TxC_Idle:
          if (isNetData && !mBkIP)
	      _handleData(); 
	  else if (BkDone.hasToken(0))
 	      _checkQueue();	     
	  break;

      case Wait_Rts_Backoff:
          if (BkDone.hasToken(0))
	      {
		  RecordToken BkDoneMsg= (RecordToken)BkDone.get(0);
		  if (((IntToken)BkDoneMsg.get("cnt")).intValue()== -2)
		      {
		          TXTXRequest.send(0,_rtsdu);
		          _currentState=Wait_Rts_Sent;
		      }
		  else // channel is busy, need to backoff
		      {
			    _backoff(_ccw,-1);
	                // modified standard here
	                _cont=true;
	                _currentState=TxC_Backoff;
		      }
	      }
	  break;

      case Wait_Rts_Sent:
	  if (TXTXConfirm.hasToken(0))
	      {
		  _Trsp=setTimer(Timeout,currentTime+_CTSTimeout*1e-6);
		  _currentState=Wait_Cts;
	      }
	  break;


      case Wait_Cts:
	  if (kind==Timeout)
	      {
		  if (_ccw!=_aCWmax)
		      _ccw=2*_ccw+1;

	          // backoff before retry
	          _backoff(_ccw,-1);
	   
	          // need to reset it!!!
	          _slrc++;

	          if (_slrc==_dot11LongRetryLimit)
		      {
			     _ccw=_aCWmin;
   	                 // modified standard here
	                 _slrc=0;
	                 _cont=false;
		      }

	          else
                      _cont=true;
		  _currentState=TxC_Backoff;
	      }
	  else if (GotAck.hasToken(0))
	      {
		  RecordToken GotCtsMsg=(RecordToken)GotAck.get(0);
              if ( ((IntToken)GotCtsMsg.get("kind")).intValue()==GotCts )
              {
		     cancelTimer(_Trsp);
		     double endRx=((DoubleToken)GotCtsMsg.get("endRx")).doubleValue();
	           _ssrc=0;
		     setTimer(SifsTimeout, endRx+_dSifsDly*1e-6); 
	           int durId=_aSifsTime+_aPreambleLength+_aPlcpHeaderLength+_sAckCtsLng/_mBrate;
                 _setDurIdField(_tpdu,durId);	    
	           _currentState=Wait_Cts_Sifs;
              }	
	      }
	  break;

      case Wait_Cts_Sifs:
	  if (kind==SifsTimeout)
	      _sendTxRequest();
	  break;

      case Wait_Mpdu_Backoff:
          if (BkDone.hasToken(0))
	      {
		  RecordToken BkDoneMsg= (RecordToken)BkDone.get(0);
		  if (((IntToken)BkDoneMsg.get("cnt")).intValue()== -2)
		      {
                          _sendTxRequest();
		      }
		  else
		      {
                          _backoff(_ccw,-1);
	                    // modified standard here
                          _cont=true;
                          _currentState=TxC_Backoff;
		      }
	      }
	  break;

      case Wait_Pdu_Sent:
          if (TXTXConfirm.hasToken(0))
	      {
	             // no need to wait for ACK for broadcast
                  int Addr1=((IntToken)_tpdu.get("Addr1")).intValue();
                  if (Addr1==mac_broadcast_addr)
		      {
                          _ssrc=0;
                          _slrc=0;
                          _ccw=_aCWmin;
                          _cont=false;
                          _backoff(_ccw,-1);
                          _currentState=TxC_Backoff;
		      }
		  else
		      {
			  _Trsp=setTimer(Timeout,currentTime+_CTSTimeout*1e-6);
			  _currentState=Wait_Ack;
		      }
	      }
	  break;

      case Wait_Ack:
          if (kind==Timeout)
	      {
                if (_ccw!=_aCWmax)
                    _ccw=2*_ccw+1;
                // backoff before retry
                _backoff(_ccw,-1);
	          _ssrc++;
	          //set retryBit=1;
                _setRetryField(_tpdu,1);
                if (_ssrc==_dot11ShortRetryLimit)
		      {
                        _ccw=_aCWmin;
	                  // modified standard here
	                  _ssrc=0;
                        _cont=false;
		      }
		    else
	                 _cont=true;
                _currentState=TxC_Backoff;
	      }
          else if (GotAck.hasToken(0))
	      {
                _ssrc=0;
	          _slrc=0;
	          _ccw=_aCWmin;
	          _cont=false;
	          _backoff(_ccw,-1);
                _currentState=TxC_Backoff;
            }
          break;

      case TxC_Backoff:
          if (BkDone.hasToken(0))
	      if (_cont)
		  {
	              _cont=false;
	              _sendFrag();
		  }
	      else
	          _checkQueue();
	  break;
      }
}

    /** Initialize the private variables.
     *  @exception IllegalActionException If thrown by the base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
	_dSifsDly=_aSifsTime-_aRxTxTurnaroundTime;
	_ssrc=0;
	_slrc=0;
	_ccw=_aCWmin;
	_seqNum=0;
	_CTSTimeout=_aSifsTime+_aPreambleLength+_aPlcpHeaderLength+_aSlotTime+
	_sAckCtsLng/_mBrate;
	// randomize node's time to go to TxC_Idle
	_backoff(_ccw,-1);
}

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                 ////


    private RecordToken _createPacket(int subtype,int duration,int RA, int TA)
      throws IllegalActionException {
	Token[] DataPacketValues={
	    new IntToken(0),
	    new IntToken(ControlType),
	    new IntToken(subtype),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(123),
	    new IntToken(duration),
	    new IntToken(RA),
	    new IntToken(TA),
	    new IntToken(160)};
	RecordToken pkt=new RecordToken(RtsPacket, DataPacketValues);
	return(pkt);
}

    private RecordToken _createDataPacket(RecordToken msg, int dest_addr)
      throws IllegalActionException {
	Token[] DataPacketValues={
	    new IntToken(0),
	    new IntToken(DataType),
	    new IntToken(Data),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(123),
	    new IntToken(_aSifsTime+_aPreambleLength+_aPlcpHeaderLength+_sAckCtsLng/_mBrate),
	    new IntToken(dest_addr),
	    new IntToken(getID()),
	    new IntToken(0),
	    new IntToken(_seqNum-_seqNum/4096*4096),
	    new IntToken(0),
	    new IntToken(0),
          msg,
	    new IntToken(34*8+((IntToken)msg.get("Length")).intValue())};
	_seqNum++;
	RecordToken pkt=new RecordToken(DataPacket, DataPacketValues);
	return(pkt);
}


    private RecordToken _setRetryField(RecordToken msg, int retryBit)
      throws IllegalActionException {
	Token[] DataPacketValues={
	    new IntToken(0),
	    new IntToken(DataType),
	    new IntToken(Data),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(retryBit),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(123),
	    msg.get("durId"),
	    msg.get("Addr1"),
	    msg.get("Addr2"),
	    new IntToken(0),
	    msg.get("SeqNum"),
	    new IntToken(0),
	    new IntToken(0),
          msg.get("payload"),
	    msg.get("Length")};

	RecordToken pkt=new RecordToken(DataPacket, DataPacketValues);
	return(pkt);
}


    private RecordToken _setDurIdField(RecordToken msg, int durId)
      throws IllegalActionException {
	Token[] DataPacketValues={
	    new IntToken(0),
	    new IntToken(DataType),
	    new IntToken(Data),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(0),
	    msg.get("retryBit"),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(0),
	    new IntToken(123),
	    new IntToken(durId),
	    msg.get("Addr1"),
	    msg.get("Addr2"),
	    new IntToken(0),
	    msg.get("SeqNum"),
	    new IntToken(0),
	    new IntToken(0),
          msg.get("payload"),
	    msg.get("Length")};

	RecordToken pkt=new RecordToken(DataPacket, DataPacketValues);
	return(pkt);
}

    private void _backoff(int ccw, int cnt) throws IllegalActionException {
	Token[] getBackoffMsgValues={
	    new IntToken(Backoff),
	    new IntToken(ccw),
	    new IntToken(cnt)};
      RecordToken event=new RecordToken(getBackoffMsgFields,getBackoffMsgValues);
	getBackoff.send(0, event);
}

    private void _sendTxRequest() throws IllegalActionException {
	Token[] TxRequestMsgValues={
	    new IntToken(TxRequest),
	    new IntToken(_mBrate*(int)1e6),
          _tpdu};

        RecordToken copyTpdu=new RecordToken(TxRequestMsgFields,TxRequestMsgValues);
        TXTXRequest.send(0, copyTpdu);
        _currentState=Wait_Pdu_Sent;
}

    private void _sendFrag() throws IllegalActionException {
        // just to see if channel is idle
        _backoff(0,0);
	  // no RTS is needed for broadcast
        int length=((IntToken)_tpdu.get("Length")).intValue();
        int retryBit=((IntToken)_tpdu.get("retryBit")).intValue();
        int Addr1=((IntToken)_tpdu.get("Addr1")).intValue();
        if (length > _dotllRTSThreshold && retryBit==0 && Addr1!=mac_broadcast_addr)
	    _currentState=Wait_Rts_Backoff;
	  else
	    _currentState=Wait_Mpdu_Backoff;
}

    private void _handleData() throws IllegalActionException {
	  int dest_addr;
        RecordToken msg=(RecordToken)_txQueue.removeFirst();
        int msg_kind=((IntToken)msg.get("kind")).intValue();
        switch(msg_kind)
        {
        case netw_interest_msg:
        case netw_data_msg:
          dest_addr = ((IntToken)msg.get("toMACAddr")).intValue(); ;
          break;
        default: // everything else is broadcast
          dest_addr = mac_broadcast_addr;
        }
        _tpdu=_createDataPacket(msg,dest_addr);
        int length=((IntToken)_tpdu.get("Length")).intValue();
        int Addr1=((IntToken)_tpdu.get("Addr1")).intValue();

        int durId=3*(_aSifsTime+_aPreambleLength+_aPlcpHeaderLength)+(length+
            2*_sAckCtsLng)/_mBrate;
	  // no RTS is needed for broadcast
        if (length<= _dotllRTSThreshold || Addr1==mac_broadcast_addr)     
              if (_debugging) {
                _debug("RTS is not sent.");}
	  else
	    {
              RecordToken pdu=_createPacket(Rts,durId,
                dest_addr, getID());
              Token[] TxRequestMsgValues={
                  new IntToken(TxRequest),
                  pdu,
                  new IntToken(_mBrate*(int)1e6)};
              RecordToken _rtsdu=new RecordToken(TxRequestMsgFields,TxRequestMsgValues);
	    }
        _sendFrag();
}

    private void _checkQueue() throws IllegalActionException {
	if (_txQueue.size() > 0)
	  _handleData();
	else
	  _currentState=TxC_Idle;
}
	
     
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

 
    private int _dSifsDly, _ccw, _slrc, _ssrc, _CTSTimeout, _seqNum;
    private boolean _cont;

    private RecordToken _pdu, _rtsdu, _tpdu;   
    private LinkedList _txQueue;
    private Timer _Trsp;

    // define states in FSM
    private static final int TxC_Idle=0;
    private static final int Wait_Mpdu_Backoff=1;
    private static final int Wait_Pdu_Sent=2;
    private static final int Wait_Ack=3;
    private static final int TxC_Backoff=4;
    private static final int Wait_Rts_Backoff=5;
    private static final int Wait_Rts_Sent=6;
    private static final int Wait_Cts=7;
    private static final int Wait_Cts_Sifs=8;

    private int _currentState=0;

    private static final int Timeout=1;
    private static final int SifsTimeout=2;
    private static final int QueueSize=100;

}
