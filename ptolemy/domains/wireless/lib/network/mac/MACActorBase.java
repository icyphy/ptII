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

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.wireless.lib.network.NetworkActorBase;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// MACActorBase

/** 
This is a base class designed for the MAC actors. Since these actors
largely share a set of prameters, we would like to declare them here
to avoid repeatly do the same amount of work in every MAC actor.
FIXME: Currently, the paremeters in this actor are extracted from
the ChannelState componnet...

This actor also defines a set of constant that are widely used in 
the OMNET c++ classes.

Based on this, it is relativly easy to translate a OMNET class
to a actor here. 
@author Yang Zhao
@version $Id$
@since Ptolemy II 2.1
*/

public class MACActorBase extends NetworkActorBase {

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
    public MACActorBase(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        // Configure MAC layer parameters.
        aSifsTime = new Parameter(this, "aSifsTime");
        aSifsTime.setTypeEquals(BaseType.INT);
        aSifsTime.setExpression("16");
        
        aPreambleLength = new Parameter(this, "aPreambleLength");
        aPreambleLength.setTypeEquals(BaseType.INT);
        aPreambleLength.setExpression("20");
        
        aPlcpHeaderLength = new Parameter(this, "aPlcpHeaderLength");
        aPlcpHeaderLength.setTypeEquals(BaseType.INT);
        aPlcpHeaderLength.setExpression("4");
        
        aRxTxTurnaroundTime = new Parameter(this, "aRxTxTurnaroundTime");
        aRxTxTurnaroundTime.setTypeEquals(BaseType.INT);
        aRxTxTurnaroundTime.setExpression("0");
        
        sAckCtsLng = new Parameter(this, "sAckCtsLng");
        sAckCtsLng.setTypeEquals(BaseType.INT);
        sAckCtsLng.setExpression("112");
        
        mBrate = new Parameter(this, "mBrate");
        mBrate.setTypeEquals(BaseType.INT);
        mBrate.setExpression("1");
        
        aSlotTime = new Parameter(this, "aSlotTime");
        aSlotTime.setTypeEquals(BaseType.INT);
        aSlotTime.setExpression("9");

	    aRxRfDelay = new Parameter(this, "aRxRfDelay");
	    aRxRfDelay.setTypeEquals(BaseType.INT);
	    aRxRfDelay.setExpression("1");

	    aRxPlcpDelay = new Parameter(this, "aRxPlcpDelay");
	    aRxPlcpDelay.setTypeEquals(BaseType.INT);
	    aRxPlcpDelay.setExpression("1");

	    aCWmin = new Parameter(this, " aCWmin");
	    aCWmin.setTypeEquals(BaseType.INT);
	    aCWmin.setExpression("15");

	    aCWmax= new Parameter(this, "aCWmax");
	    aCWmax.setTypeEquals(BaseType.INT);
	    aCWmax.setExpression("1023");

	    dot11ShortRetryLimit= new Parameter(this, "dot11ShortRetryLimit");
	    dot11ShortRetryLimit.setTypeEquals(BaseType.INT);
	    dot11ShortRetryLimit.setExpression("6");

	    dot11LongRetryLimit= new Parameter(this, "dot11LongRetryLimit");
	    dot11LongRetryLimit.setTypeEquals(BaseType.INT);
	    dot11LongRetryLimit.setExpression("6");

	    dotllRTSThreshold= new Parameter(this, "dotllRTSThreshold");
	    dotllRTSThreshold.setTypeEquals(BaseType.INT);
	    dotllRTSThreshold.setExpression("1");


    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    // all time are in the unit of microseconds
    
    /** The paremeter for SIFS (Short Inter Frame Space). This time
     *  is used to separate transmissions belong to a single diolog.
     *  This Value is fixed per PHY and is calculated base on
     *  the time that takes for the transmitting part to switch back
     *  to receive mode.    
     */
    public Parameter aSifsTime;
    
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
    
    /** Time needed for a transceiver to go from receiving to transmitting  
     */
    public Parameter aRxTxTurnaroundTime;
    
    /** The size of a CTS/Ack message
     */
    public Parameter sAckCtsLng;
    
    /** The radio data rate in the unit of Mbps
     */
    public Parameter mBrate;
    
    /**  The time unit for the backoff time
     */
    public Parameter aSlotTime;
   
    /** The nominal time between the end of a symbol at the air interface to
     *  the issurance of a PMD-DATA.indicate to the PLCP.
     */
    public Parameter aRxRfDelay;

    /** The nominal time that the PLCP uses to deliver a bit from the PMD
     *  receive path to the MAC.
     */
    public Parameter aRxPlcpDelay;

    /** The minimum size of the contention window, in units of aSlotTime.
     */
    public Parameter aCWmin;

    /** The maximum size of the contention window, in units of aSlotTime.
     */
    public Parameter aCWmax;

    /** The maximum number of retransmissions for MPDUs
     */
    public Parameter dot11ShortRetryLimit;

    /** The maximum number of retransmissions for RTS
     */
    public Parameter dot11LongRetryLimit;

    /** The threshold for the length of a MPDU, below which a RTS is not
     *  necessary.
     */
    public Parameter dotllRTSThreshold;
 
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
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
        if (attribute == aSifsTime) {
            temp = ((IntToken)
                    aSifsTime.getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "aSifsTime is required to be positive. "
                + "Attempt to set it to: " 
                + temp);            
            } else {
                _aSifsTime = temp;
            }
            
        } else if (attribute == aPreambleLength) {
            temp = ((IntToken)
                    aPreambleLength.getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "preamble Length is required to be nonnegative. "
                + "Attempt to set it to: " 
                + temp);            
            } else {
                _aPreambleLength = temp;
            }
        } else if (attribute == aPlcpHeaderLength) {
            temp = ((IntToken)
                    aPlcpHeaderLength.getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "PLCPHeader Length is required to be nonnegative. "
                + "Attempt to set it to: " 
                + temp);            
            } else {
                _aPlcpHeaderLength = temp;
            }
        } else if (attribute == aRxTxTurnaroundTime) {
            temp = ((IntToken)
                    aRxTxTurnaroundTime.getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "aRxTxTurnaroundTime is required to be nonnegative. "
                + "Attempt to set it to: " 
                + temp);            
            } else {
                _aRxTxTurnaroundTime = temp;
            }
        } else if (attribute == sAckCtsLng) {
            temp = ((IntToken)
                    sAckCtsLng.getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "sAckCtsLng is required to be nonnegative. "
                + "Attempt to set it to: " 
                + temp);            
            } else {
                _sAckCtsLng = temp;
            }
        } else if (attribute == mBrate) {
            temp = ((IntToken)
                    mBrate.getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "mBrate is required to be nonnegative. "
                + "Attempt to set it to: " 
                + temp);            
            } else {
                _mBrate = temp;
            } 
        } else if (attribute == aSlotTime) {
            temp = ((IntToken)
                    aSlotTime.getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "aSlotTime is required to be nonnegative. "
                + "Attempt to set it to: " 
                + temp);            
            } else {
                _aSlotTime = temp;
            } 
        } else if (attribute == aRxRfDelay) {
            temp = ((IntToken)
                    aRxRfDelay.getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "aRxRfDelay is required to be nonnegative. "
                + "Attempt to set it to: "
                + temp);
            } else {
                _aRxRfDelay = temp;
            }    
        } else if (attribute == aRxPlcpDelay) {
            temp = ((IntToken)
                    aRxPlcpDelay.getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "aRxPlcpDelay is required to be nonnegative. "
                + "Attempt to set it to: "
                + temp);
            } else {
                _aRxPlcpDelay = temp;
            }    
        } else if (attribute == aCWmin) {
            temp = ((IntToken)
                   aCWmin .getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "aCWmin is required to be nonnegative. "
                + "Attempt to set it to: "
                + temp);
            } else {
                _aCWmin = temp;
            }    
        } else if (attribute == aCWmax) {
            temp = ((IntToken)
                    aCWmax.getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "aCWmax is required to be nonnegative. "
                + "Attempt to set it to: "
                + temp);
            } else {
                _aCWmax = temp;
            }    
        } else if (attribute == dot11ShortRetryLimit) {
            temp = ((IntToken)
                    dot11ShortRetryLimit.getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "dot11ShortRetryLimit is required to be nonnegative. "
                + "Attempt to set it to: "
                + temp);
            } else {
                _dot11ShortRetryLimit = temp;
            }    
        } else if (attribute == dot11LongRetryLimit) {
            temp = ((IntToken)
                    dot11LongRetryLimit.getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "dot11LongRetryLimit is required to be nonnegative. "
                + "Attempt to set it to: "
                + temp);
            } else {
                _dot11LongRetryLimit = temp;
            }    
        } else if (attribute == dotllRTSThreshold) {
            temp = ((IntToken)
                    dotllRTSThreshold.getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "dotllRTSThreshold is required to be nonnegative. "
                + "Attempt to set it to: "
                + temp);
            } else {
                _dotllRTSThreshold = temp;
            }        
        } else {
            super.attributeChanged(attribute);
        }
    }

    public static final int MAC_BROADCAST_ADDR = -1;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables                 ////
    //the local varibles for the parameters of this actor.
    protected int _aSifsTime;
    protected int _aPreambleLength;
    protected int _aPlcpHeaderLength;
    protected int _aRxTxTurnaroundTime;
    protected int _sAckCtsLng;
    protected int _mBrate;
    protected int _aSlotTime;
    protected int _aRxRfDelay;
    protected int _aRxPlcpDelay;
    protected int _aCWmin;
    protected int _aCWmax;
    protected int _dot11ShortRetryLimit;
    protected int _dot11LongRetryLimit;
    protected int _dotllRTSThreshold;


    // message formats
    protected static final String[] RxStartMsgFields ={"kind", "rxRate"};
    protected static final String[] TxStartMsgFields = {"kind", "length", "rate"};
    protected static final String[] RtsTimeoutMsgFields ={"kind"};
    protected static final String[] RxMpduMsgFields ={"kind", "pdu","endRx","rxRate"};
    protected static final String[] UseIfsMsgFields ={"kind", "tRxEnd"};
    protected static final String[] RxEndMsgFields ={"kind", "status"};
    protected static final String[] RxIndicateMessageFields =
            {"kind", "pdu", "endRx", "rxRate"};
    protected static final String[] NeedAckMessageFields =
            {"kind", "ackto", "endRx", "rxRate", "dAck"};
    protected static final String[] SetNavMessageFields = 
            {"kind", "tRef", "dNav", "src"};
  
    protected static final String[] CSMsgFields={"kind"};
    
    // the value for the pdu field is a record with fields as DataPacket's fields.
    protected static final String[] TxRequestMsgFields = {"kind", "pdu", "rate", "channel"};
    protected static final String[] TxConfirmMsgFields = {"kind"}; 
    
    protected static final String[] DataPacketFields = {"kind", "name", "protocolVer", "Type", "Subtype",
            "toDs", "frDs", "moreFrag", "retryBit", "pwrMgt", "moreData", "wepBit", 
            "orderBit", "FCS", "durId", "Addr1", "Addr2", "Addr3", "SeqNum", "FragNum", "Addr4"};
            
    protected static final String[] BackoffDoneMsgFields = {"kind", "cnt"};
    
    

  
    // message types  
    protected static final int  NeedAck        = 1;
    protected static final int  RxIndicate     = 2;
    protected static final int  TxConfirm      = 3;
    protected static final int  PduRequest     = 4;
    protected static final int  BkDone         = 5;
    protected static final int  Backoff        = 6;
    protected static final int  Slot           = 7;
    protected static final int  Idle           = 8;
    protected static final int  Busy           = 9;
    protected static final int  Cancel         = 10;
    protected static final int  TxRequest      = 21;
    protected static final int  UseEifs        = 22;
    protected static final int  UseDifs        = 23;
    protected static final int  SetNav         = 24;
    protected static final int  ClearNav       = 25;
    protected static final int  RtsTimeout     = 26;
    protected static final int  nosrc          = 27;
    protected static final int  misc           = 28;
    protected static final int  RxMpdu         = 29;
    protected static final int  RxStart        = 30;
    protected static final int  RxEnd          = 31;
    protected static final int  RxData         = 32;
    protected static final int  TxStart        = 36;
    protected static final int  TxStartConfirm = 37;
    protected static final int  TxData         = 38;
    protected static final int  Timeout        = 39;
    protected static final int  Gilbert        = 40;
    protected static final int  Turnaround     = 41;
    protected static final int  Rxdelay        = 42;
    protected static final int  startRsp       = 43;
    protected static final int  PCresponse     = 44;
    protected static final int  PCrequest      = 45;
    protected static final int  PCcheck        = 46;
    protected static final int  PCremove       = 47;
    protected static final int  PCremove2      = 48;
    protected static final int  PCmax          = 48;
    protected static final int  PCmin          = 44;
    protected static final int  TxEnd          = 11;
    protected static final int  Ack            = 13;
    protected static final int  Cts            = 12;
    protected static final int  Data           = 0;
    protected static final int  Rts            = 11;
    protected static final int  ControlType    = 1;
    protected static final int  DataType       = 2;
    protected static final int  GotAck         = 1;
    protected static final int  GotCts         = 2;
    protected static final int  ControlCh      = 0;
    protected static final int  DataCh         = 1;
    protected static final int  NoError        = 0;
    protected static final int  Error          = 1;
    protected static final int  UNKNOWN        = -1;
}
