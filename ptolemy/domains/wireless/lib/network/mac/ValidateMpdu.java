
/* Validates a received MPDU (MAC Protocol Data Unit).

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
//// ValidateMpdu
/**
ValidateMpdu class checks the status field of the RxEnd message. If it indicates 
no error, the received data is forwarded to FilterMpdu process. Otherwise,
the data is dropped and ChannelState process will be notified of using EIFS
as IFS (interframe space). In the case of correctly received data, this class
also check its data type. If it is a RTS packet, a timer is set. The duration 
is set to be the time needed to receive a CTS, plus some guard time. If no CTS
is received before this timer expires, a RTSTimeout signal is sent to ChannelState
process, which will clear the channel reservation made earlier by the above
RTS packet.

@author Charlie Zhong, Xiaojun Liu and Yang Zhao
@version $Id$
*/

public class ValidateMpdu extends MACActorBase {
   
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
    public ValidateMpdu(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
       
     
        // create ports
        fromPHYLayer =new TypedIOPort(this, "fromPHYLayer", true, false);
        fromPHYLayer.setTypeEquals(BaseType.GENERAL);

        toChannelState =new TypedIOPort(this, "toChannelState", false,true);
        toChannelState.setTypeEquals(BaseType.GENERAL);

        toFilterMpdu =new TypedIOPort(this, "toFilterMpdu", false,true);
        toFilterMpdu.setTypeEquals(BaseType.GENERAL);
       
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////


    /** Port receiving messages from the PHY
     */
    public TypedIOPort fromPHYLayer;

    /** Port sending messages to the ChannelState process
     */
    public TypedIOPort toChannelState;

    /** Port sending received data packets to the FilterMpdu process
     */
    public TypedIOPort toFilterMpdu;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////



    public void fire() throws IllegalActionException {
        super.fire();
	int UseIfs;
        // perform the actions/computation done in the handleMessage()
        // method
	int kind=whoTimeout();	// check if a timer times out and which
        double currentTime =getDirector().getCurrentTime();
	switch(_currentState)
	    {
	    case Rx_Idle:
		if (kind==RtsTimeout)
		    { // send RtsTimeout message to ChannelState process
			Token[] values ={
			  new IntToken(RtsTimeout)};			       
			RecordToken msgout =new RecordToken(RtsTimeoutMsgFields, values);
			toChannelState.send(0, msgout);
		    } 

		else if  (fromPHYLayer.hasToken(0)) 
		    {
			RecordToken msg= (RecordToken)fromPHYLayer.get(0);
			if (((IntToken)msg.get("kind")).intValue()==RxStart)
			    {
				_rxRate=((IntToken)msg.get("rxRate")).intValue();
				// cancel the RTS timer
				cancelTimer(_timer);
				_currentState=Rx_Frame;
			    }
		    }
		break;

	    case Rx_Frame:
		if (fromPHYLayer.hasToken(0) )
		    {
		    RecordToken msg= (RecordToken)fromPHYLayer.get(0);
		    switch(((IntToken)msg.get("kind")).intValue())
			{
			case RxEnd:
			    _endRx=currentTime-_D1*1e-6;
			    if ( ((IntToken)msg.get("status")).intValue()==NoError)
				{
				// if the received message is RTS, set RtsTimeout timer
				if (((IntToken)_pdu.get("Type")).intValue()==ControlType 
				    && ((IntToken)_pdu.get("Subtype")).intValue()==Rts)
				    {
					_dRts=2*_aSifsTime+2*_aSlotTime+_sAckCtsLng/_rxRate+
					    _aPreambleLength+_aPlcpHeaderLength;
					_timer=setTimer(RtsTimeout, currentTime +_dRts*1e-6);
				    }
				// working with record tokens to represent messages
				Token[] RxMpduvalues ={
				    new IntToken(RxMpdu),
				    _pdu,
				    new DoubleToken(_endRx),
				    new IntToken(_rxRate)};
				RecordToken msgout =new RecordToken(RxMpduMsgFields, RxMpduvalues);
				// forward the packet to FilterMpdu process 
				toFilterMpdu.send(0, msgout);
				// use DIFS as IFS for normal packets
				UseIfs=UseDifs;
				}
			    else
				{
				// use EIFS as IFS if a packet is corrupted 
				UseIfs=UseEifs;
				}

			    // send UseIfs message to ChannelState process
			    Token[] Ifsvalues ={
				    new IntToken(UseIfs),
				    new DoubleToken(_endRx)};
			    RecordToken msgout =new RecordToken(UseIfsMsgFields, Ifsvalues);
			    toChannelState.send(0, msgout);

			    // go back to Rx_Idle state
			    _currentState=Rx_Idle;
			    break;

			case RxData:
			    // store the packet and process it after RxEnd is received
			    _pdu=msg;
			    break;
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
        _D1=_aRxRfDelay+_aRxPlcpDelay;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

 
    private int _dRts,_rxRate;
    private double _endRx;
    private RecordToken _pdu; 
    private Timer _timer;  
    private int _D1;

    // define states in FSM
    private static final int Rx_Idle=0;
    private static final int Rx_Frame=1;

    private int _currentState=0;


}

