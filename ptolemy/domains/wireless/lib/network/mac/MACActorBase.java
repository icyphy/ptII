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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
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
to a actor here (I hope...). 
@author Yang Zhao
@version $ $
*/

public class MACActorBase extends TypedAtomicActor {

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
        
        // Configure parameters.
        SIfsTime = new Parameter(this, "SIfsTime(us)");
        SIfsTime.setTypeEquals(BaseType.INT);
        SIfsTime.setExpression("28000");
        
        preambleLength = new Parameter(this, "preambleLength");
        preambleLength.setTypeEquals(BaseType.INT);
        preambleLength.setExpression("96");
        
        PLCPHeaderLength = new Parameter(this, "PLCPHeaderLength");
        PLCPHeaderLength.setTypeEquals(BaseType.INT);
        PLCPHeaderLength.setExpression("0");
        
        RxTxTurnaroundTime = new Parameter(this, "RxTxTurnaroundTime(ms)");
        RxTxTurnaroundTime.setTypeEquals(BaseType.INT);
        RxTxTurnaroundTime.setExpression("0");
        
        sAckCtsLng = new Parameter(this, "sAckCtsLng");
        sAckCtsLng.setTypeEquals(BaseType.INT);
        sAckCtsLng.setExpression("0");
        
        mBrate = new Parameter(this, "mBrate");
        mBrate.setTypeEquals(BaseType.INT);
        mBrate.setExpression("0");
        
        slotTime = new Parameter(this, "slotTime(us)");
        slotTime.setTypeEquals(BaseType.INT);
        slotTime.setExpression("0");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /** The paremeter for SIfs(Short Inter Frame Space). This time
     *  is used to separate transmissions belong to a single diolog.
     *  This Value is fixed per PHY and is calculated base on
     *  the time that takes for the transmitting part to switch back
     *  to receive mode. It is 28ms for 802.11 FH PHY.    
     */
    public Parameter SIfsTime;
    
    /** The size of the Preamble header in a frame. The Preamble
     *  header contains synchronization information and timing 
     *  information for the frame.    
     */
    public Parameter preambleLength;
    
    /** The size of the PLCP header in a frame. The PLCP header 
     *  contains logical information for the physical layer to 
     *  decode the frame.    
     */
    public Parameter PLCPHeaderLength;
    
    /** FIXME:?  
     */
    public Parameter RxTxTurnaroundTime;
    
    /** FIXME:?  
     */
    public Parameter sAckCtsLng;
    
    /** FIXME:?  
     */
    public Parameter mBrate;
    
    /** FIXME:?  
     */
    public Parameter slotTime;
    
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
        if (attribute == SIfsTime) {
            temp = ((IntToken)
                    SIfsTime.getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "SIfsTime is required to be positive. "
                + "Attempt to set it to: " 
                + temp);            
            } else {
                _SIfsTime = temp;
            }
            
        } else if (attribute == preambleLength) {
            temp = ((IntToken)
                    preambleLength.getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "preamble Length is required to be nonnegative. "
                + "Attempt to set it to: " 
                + temp);            
            } else {
                _preambleLength = temp;
            }
        } else if (attribute == PLCPHeaderLength) {
            temp = ((IntToken)
                    PLCPHeaderLength.getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "PLCPHeader Length is required to be nonnegative. "
                + "Attempt to set it to: " 
                + temp);            
            } else {
                _PLCPHeaderLength = temp;
            }
        } else if (attribute == RxTxTurnaroundTime) {
            temp = ((IntToken)
                    RxTxTurnaroundTime.getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "RxTxTurnaroundTime is required to be nonnegative. "
                + "Attempt to set it to: " 
                + temp);            
            } else {
                _RxTxTurnaroundTime = temp;
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
        } else if (attribute == slotTime) {
            temp = ((IntToken)
                    slotTime.getToken()).intValue();
            if (temp < 0) {
                throw new IllegalActionException(this,
                "slotTime is required to be nonnegative. "
                + "Attempt to set it to: " 
                + temp);            
            } else {
                _slotTime = temp;
            }     
        } else {
            super.attributeChanged(attribute);
        }
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables                 ////
    //the local varibles for the parameters of this actor.
    protected int _SIfsTime;
    protected int _preambleLength;
    protected int _PLCPHeaderLength;
    protected int _RxTxTurnaroundTime;
    protected int _sAckCtsLng;
    protected int _mBrate;
    protected int _slotTime;
    
    protected static final int NeedAck         = 1;
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
