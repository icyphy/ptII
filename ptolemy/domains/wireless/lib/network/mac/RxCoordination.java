
/* Generates acknowledgements, routes data frames to the network layer
   and indicates receipt of Ack and Cts to TxCoordination process.

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

import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

////////////////////////////////////////////////////////////////////////=
//
//// RxCoordination
/**
RxCoordination class is responsible for the sequence of events in response 
to a session setup request. When a RTS packet is received, RxCoordination 
sends a CTS back after SIFS seconds if NAV is zero (i.e. channel is available);
when a data packet is received, a Ack is sent back after SIFS seconds and 
the data packet received will be forwarded to the network layer. RxCoordination 
also notifies TxCoordination process of the receipt of either CTS or Ack.

@author Charlie Zhong
@version $Id$
*/

public class RxCoordination extends MACActorBase {
   
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
    public RxCoordination(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
       
     
        // create ports
        fromReception =new TypedIOPort(this, "fromReception", true, false);
        fromReception.setTypeEquals(BaseType.GENERAL);

        RXTXConfirm =new TypedIOPort(this, "RXTXConfirm", true, false);
        RXTXConfirm.setTypeEquals(BaseType.GENERAL);

        SinkRequest =new TypedIOPort(this, "SinkRequest", false,true);
        SinkRequest.setTypeEquals(BaseType.GENERAL);

        toPowerControl =new TypedIOPort(this, "toPowerControl", false,true);
        toPowerControl.setTypeEquals(BaseType.GENERAL);

        GotAck =new TypedIOPort(this, "GotAck", false,true);
        GotAck.setTypeEquals(BaseType.GENERAL);

        RXTXRequest =new TypedIOPort(this, "RXTXRequest", false,true);
        RXTXRequest.setTypeEquals(BaseType.GENERAL);
       
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////


    /** Port receiving messages from the Reception block
     */
    public TypedIOPort fromReception;

    /** Port receiving confirmation on RXTxRequest from the Reception block
     */
    public TypedIOPort RXTXConfirm;

    /** Port sending messages to the network layer
     */
    public TypedIOPort SinkRequest;

    /** Port sending messages to the PowerControl block
     */
    public TypedIOPort toPowerControl;

    /** Port notifying the TxCoordination process of the receipt of Ack or Cts
     */
    public TypedIOPort GotAck;

    /** Port sending messages to the Reception block
     */
    public TypedIOPort RXTXRequest;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////



    public void fire() throws IllegalActionException {
        super.fire();
	    int dAck, ackto;
          double endRx;
          RecordToken pdu;

        // perform the actions/computation done in the handleMessage()
        // method 
	    switch(_currentState)
	    {
	        case RxC_Idle:
		    if (fromReception.hasToken(0)) {
			RecordToken msg= (RecordToken)fromReception.get(0);
			switch(((IntToken)msg.get("kind")).intValue())
			{
			    case NeedAck:
                           if (_debugging) {
                               _debug("Got NeedAck msg");
                           }
                                
                           dAck= ((IntToken)msg.get("dAck")).intValue();
                           ackto= ((IntToken)msg.get("ackto")).intValue();
                           endRx= ((DoubleToken)msg.get("endRx")).doubleValue();

			         if (dAck>0)
                               dAck=dAck-_dRsp;
                           // create Ack
                           _rspdu=_createPacket(Ack,dAck,ackto);
                           // schedule SIFS timer
                           setTimer(SifsTimeout, endRx+_dSifsDly*1e-6);
				   _currentState=Wait_Sifs;
                       break;

	                 case RxIndicate:
                            pdu= (RecordToken)msg.get("pdu");
                            endRx= ((DoubleToken)msg.get("endRx")).doubleValue();
                            int Type=((IntToken)pdu.get("Type")).intValue();
                            int Subtype=((IntToken)pdu.get("Subtype")).intValue();
                            int durId=((IntToken)pdu.get("durId")).intValue();
                            int Addr2=((IntToken)pdu.get("Addr2")).intValue();
                            _rate=((IntToken)pdu.get("rxRate")).intValue();

	                      switch(Type)
	                      {
	                          case ControlType:
	                               switch(Subtype)
	                               {
		                             case Ack:
                                              Token[] GotAckvalues={
                                                      new IntToken(GotAckMsg),
                                                      new DoubleToken(endRx)};
                                              RecordToken GotAckmsg = 
                                              new RecordToken(GotCtsMsgFields, GotAckvalues);
				                      // send the message to the TxCoordination process
				                      GotAck.send(0, GotAckmsg);
                                         break;

		                             case Cts:
                                              Token[] GotCtsvalues={
                                                      new IntToken(GotCts),
                                                      new DoubleToken(endRx)};
                                              RecordToken GotCtsmsg = 
                                              new RecordToken(GotCtsMsgFields, GotCtsvalues);
				                      // send the message to the TxCoordination process
				                      GotAck.send(0, GotCtsmsg);
                                         break;

	                                   case Rts:
                                              double currentTime =getDirector().getCurrentTime();
	          		                      if (tNavEnd <= currentTime)
		                                  {
                                                 // generate Cts
	 	                                     _rspdu=_createPacket(Cts,durId-_dRsp,Addr2);
		                                     setTimer(SifsTimeout, endRx+_dSifsDly*1e-6);
		                                     _currentState=Wait_Sifs;
		                                  }
		                             break;

                                     }
                                break;

                                case DataType:
                                     if (Subtype==Data)
                                     {
                                        RecordToken payload=(RecordToken)pdu.get("payload");
                                        int payload_kind=((IntToken)payload.get("kind")).intValue();
                                        // except power control messages, all others are sent
                                        // to the network layer
                                        if (payload_kind >= PCmin && payload_kind <= PCmax)
                                           toPowerControl.send(0, payload);
                                        else
                                           SinkRequest.send(0, payload);
                                     }
                                break;
                             }
                       break;
                  }}
              break;
            
              case Wait_Sifs:
	             int kind=whoTimeout();	// check if a timer times out and which
                   if (kind==SifsTimeout)
                   {
                       Token[] TXRequestvalues={
                           new IntToken(TxRequest),
                           _rspdu,
                           new IntToken(_rate)};
                       RecordToken txrq = new RecordToken(TxRequestMsgFields, TXRequestvalues);
			     // send the message to the Transmission block
			     RXTXRequest.send(0, txrq);
                       _currentState=Wait_TxDone;
                   }
              break;

              case Wait_TxDone:
                   if (RXTXConfirm.hasToken(0))
                   {
                       _currentState=RxC_Idle;
                   }
              break; 
    }
}

    /** Initialize the private variables.
     *  @exception IllegalActionException If thrown by the base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _dSifsDly=_aSifsTime-_aRxTxTurnaroundTime;
        _dRsp=_aSifsTime+_aPreambleLength+_aPlcpHeaderLength+_sAckCtsLng/_mBrate;
        _currentState = RxC_Idle;
    }

    private RecordToken _createPacket(int subtype,int duration,int RA)
      throws IllegalActionException {
        Token[] AckPacketValues={
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
           new IntToken(14*8)};
        RecordToken pkt=new RecordToken(AckPacket, AckPacketValues);
        return pkt;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _dSifsDly, _dRsp, _rate;
    private RecordToken _rspdu; 
    private static final int SifsTimeout=1;

    // define states in FSM
    private static final int RxC_Idle=0;
    private static final int Wait_Sifs=1;
    private static final int Wait_TxDone=2;

    private int _currentState=RxC_Idle;
}

