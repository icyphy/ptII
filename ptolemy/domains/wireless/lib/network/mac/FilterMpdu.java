/* An actor that filters out duplicate MPDU packets. It also reads the 
 *duration field of a RTS message and let the ChannelState process to
 * make reservation. In addition, if a data packet is received, it informs
 * the ProtocolControl block that an Ack is needed.
 
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

import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// FilterMPDU
/** 
Filter the received MPDU (MAC Protocol Data Unit) packets.
The code is based on a OMNET model created by Charlie Zhong.

@author Xiaojun Liu
@version $Id$
*/
public class FilterMpdu extends MACActorBase {

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
    public FilterMpdu(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        fromValidateMpdu = new TypedIOPort(this, "fromValidateMPDU", true, false);

        fromValidateMpdu.setTypeEquals(BaseType.GENERAL);
        
        toChannelState = new TypedIOPort(this, "toChannelState", false, true);
        toChannelState.setTypeEquals(BaseType.GENERAL);
        toChannelState.setMultiport(true);
        
        toProtocolControl = new TypedIOPort(this, "toProtocolControl", false, true);
        toProtocolControl.setTypeEquals(BaseType.GENERAL);
    }

    /////////////////////////////////////////////////////////////////   
    ////                         parameters                      ////
    
    /** Port receiving packets to be filtered.
     */
    TypedIOPort fromValidateMpdu;
    
    /** Send NAV (Network Allocation Vector) to ChannelState component.
     */
    TypedIOPort toChannelState;
    
    /** Send data to ProtocolControl component.
     */
    TypedIOPort toProtocolControl;
    
    
    /////////////////////////////////////////////////////////////////   
    ////                         public methods                  ////
    
    /** Process input packets.
     *  @exception IllegalActionException If an error occurs reading
     *   or writing inputs or outputs.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        
        if (!fromValidateMpdu.hasToken(0)) return;
        
        int dAck = 0;
        
        RecordToken msg = (RecordToken)fromValidateMpdu.get(0);
        int msgKind = ((IntToken)msg.get("kind")).intValue();

        if (msgKind == RxMpdu) {
            if (_debugging) _debug("FILTER: Got RxMpdu");
            RecordToken pdu = (RecordToken)msg.get("pdu");
            if (intFieldValue(pdu, "moreFrag") ==1)
                dAck = intFieldValue(pdu, "durId");
            int dNav = intFieldValue(pdu, "durId");
            int src =misc;
            // code for broadcast
            if (intFieldValue(pdu, "Addr1") == mac_broadcast_addr) {
                RecordToken msgout = new RecordToken(
                        RxIndicateMessageFields,
                        new Token[] {
				new IntToken(RxIndicate),
				//TODO: how to implement this?
                                //msgout->pdu=pdu->copyEncapMsg();
                                pdu,
                                msg.get("endRx"),
                                msg.get("rxRate")
				});
                // send RxIndicate message to the ProtocolControl block             
                toProtocolControl.send(0, msgout);
                if (_debugging) _debug("FILTER: Sent RxIndicate");
            } else if (intFieldValue(pdu, "Addr1") == getID()) {
                boolean dup = false;
                if (intFieldValue(pdu, "retryBit") == 1) 
                    dup = _searchTupleCache(pdu);
                if (intFieldValue(pdu, "retryBit") == 0 || !dup) {
                    RecordToken msgout = new RecordToken(
                            RxIndicateMessageFields,
                            new Token[] {
                                    new IntToken(RxIndicate),
                                    //TODO: how to implement this?
                                    //msgout->pdu=pdu->copyEncapMsg();
                                    pdu,
                                    msg.get("endRx"),
                                    msg.get("rxRate")
                                    });
                    // only if it is not a duplicate packet, will it be forwarded                
                    toProtocolControl.send(0, msgout);
                    if (_debugging) _debug("FILTER: Sent RxIndicate");
                }

                if (intFieldValue(pdu, "Type") == DataType) {
                    RecordToken msgout = new RecordToken(
                            NeedAckMessageFields,
                            new Token[] {
                                    new IntToken(NeedAck),
                                    pdu.get("Addr2"),
                                    msg.get("endRx"),
                                    msg.get("rxRate"),
                                    new IntToken(dAck)});
                    // if it is a data packet, an Ack is needed
                    toProtocolControl.send(0, msgout);
                    if (_debugging) _debug("FILTER: Sent NeedAck");
		    // add this packet to the TupleCache
                    _updateTupleCache(pdu);
                }
            } 
	    // if this packet is not for me
	    else {
                if (intFieldValue(pdu, "Type") ==ControlType &&
                        intFieldValue(pdu, "Subtype") == Rts)
                    src = Rts;
                
                if (intFieldValue(pdu, "durId") <= 32767) {
                    RecordToken msgout = new RecordToken(
                            SetNavMessageFields,
                            new Token[] {
                                    new IntToken(SetNav),
                                    msg.get("endRx"),
                                    new IntToken(dNav),
                                    new IntToken(src)});
                    //TODO: send(msgout, toChannelstateGateId+msgin->channel);
         	    // ask the ChannelState process to make reservation
                    toChannelState.send(0, msgout);
                }
            } // end of RTS
        } // end of RxMpdu
    } // end of fire()

    /** Initialize this actor.
     *  @exception IllegalActionException If thrown by the superclass.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _tupleCache = new LinkedList();
    }

    private int intFieldValue(RecordToken token, String label) {
        IntToken t = (IntToken)token.get(label);
        return t.intValue();
    }
    
    private boolean _searchTupleCache(RecordToken pdu) {
        boolean result = false;
        int addr = intFieldValue(pdu, "Addr2");
        int seqNum = intFieldValue(pdu, "SeqNum");
        int fragNum = intFieldValue(pdu, "FragNum");
        Iterator tuples = _tupleCache.iterator();
        while (tuples.hasNext()) {
            int[] tuple = (int[])tuples.next();
            if (addr == tuple[0] && seqNum == tuple[1] && fragNum == tuple[2])
                return true;
        }
        return false;
    }
    
    private void _updateTupleCache(RecordToken pdu) {
        int addr = intFieldValue(pdu, "Addr2");
        int seqNum = intFieldValue(pdu, "SeqNum");
        int fragNum = intFieldValue(pdu, "FragNum");
        Iterator tuples = _tupleCache.iterator();
        while (tuples.hasNext()) {
            int[] tuple = (int[])tuples.next();
	    // if both Addr2 and SeqNum match, use this entry
	    // but overwite its FragNum
            if (addr == tuple[0] && seqNum == tuple[1])
	    {
		tuple[2]=fragNum;
                return;
	    }
        }
	// only if no entry is found, will we add a new one
        int[] tuple = new int[] {
                intFieldValue(pdu, "Addr2"),
                intFieldValue(pdu, "SeqNum"),
                intFieldValue(pdu, "FragNum")};
        if (_tupleCache.size() == _TUPLE_CACHE_SIZE)
            _tupleCache.removeLast();
        _tupleCache.addFirst(tuple);
    }
    
    private LinkedList _tupleCache;
    private static final int _TUPLE_CACHE_SIZE = 32;


}
