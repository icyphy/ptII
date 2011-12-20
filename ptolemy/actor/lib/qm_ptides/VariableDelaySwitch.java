/* A Switch that delays  packets according to their priorities and length

@Copyright (c) 2011-2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
//as
package ptolemy.actor.lib.qm_ptides;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.TreeSet;
import java.util.Iterator;

import ptolemy.data.RecordToken;
import ptolemy.actor.Director;
import ptolemy.actor.Actor;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.actor.lib.qm.QuantityManagerListener.EventType;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.Port;

/** A {@link QuantityManager} actor that, when its
 *  {@link #sendToken(Receiver, Receiver, Token)} method is called, sends
 *  the token with the earliest timestamp to the corresponding output
 *  port. This quantity manager is used on
 *  FIXME: Fix the comments, it's cp from BasicSwitch.java right now
 *  
 ports by setting a parameter with an ObjectToken that refers
 *  to this QuantityManager at the port. Note that the name of this
 *  parameter is irrelevant.
 *
 *  <p>This quantity manager implements a simple switch. It has a parameter
 *  specifying the number of ports. On each port, an actor is connected.
 *  Note that these ports are not represented as ptolemy actor ports.
 *  This actor can send tokens to the switch and receive tokens from the
 *  switch. The mapping of ports to actors is done via parameters of this
 *  quantity manager.
 *
 *  <p>Internally, this switch has a buffer for every input, a buffer
 *  for the switch fabric and a buffer for every output. The delays
 *  introduced by the buffers are configured via parameters. Tokens are
 *  processed simultaneously on the buffers.
 *
 *  <p> This switch implements a very basic switch fabric consisting
 *  of a FIFO queue.
 *
 *  @author 
 *  @version 
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating 
 *  @Pt.AcceptedRating 
 */


import ptolemy.actor.lib.qm.BasicSwitch;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * 
 * A {@link QuantityManager} actor that applies packet-size and priority 
 * dependent delay to incoming packets. Intended to use with RecordTokens only.
 * Assumes record token includes the encapsulated packages with a label "packets"
 * and a TCP header with label "TCPlabel"
 * 
 * @author Ilge Akkaya
 * @version
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ilgea)
 * @Pt.AcceptedRating
 */
public class VariableDelaySwitch extends BasicSwitch {
    
    public VariableDelaySwitch(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        Director director = getDirector();

        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }
        
        
        // TODO Auto-generated constructor stub
        
        // ignore this input buffer delay, if
        inputBufferDelay.setExpression("0.2");
        inputBufferDelay.setTypeEquals(BaseType.DOUBLE);
        _inputBufferDelay = 0.2;
        
        // in bps
        channelBandwidth = new Parameter(this, "Channel Bandwidth (in bps)");
        channelBandwidth.setExpression("1000000");
        channelBandwidth.setTypeEquals(BaseType.INT);
        _channelBandwidth = 1000000; //1Mbps
        
        //set the unit packet size
        unitTokenSize = new Parameter(this, "Unit Packet Size(in bits)");
        unitTokenSize.setExpression("1000");
        unitTokenSize.setTypeEquals(BaseType.INT);
        _unitTokenSize = 1000;
        
        allowPDV = new Parameter(this,"Allow PDV");
        allowPDV.setExpression("true");
        allowPDV.setTypeEquals(BaseType.BOOLEAN);
        _allowPDV = true;
        
        allowPriority = new Parameter(this,"Allow Priority Routing");
        allowPriority.setExpression("true");
        allowPriority.setTypeEquals(BaseType.BOOLEAN);
        _allowPriority = true;
        
        
        
        
        //IPv4
        // TCP header = 20 bytes
        // IP header =20 bytes
        
        
        //        _switchFabricQueue = new TreeSet();
    }
    
//         public void attributeChanged(Attribute attribute)
//         throws IllegalActionException {
//         if (attribute == channelBandwidth) {
//             int value = ((IntToken) channelBandwidth.getToken())
//                     .intValue();
//             if (value <= 0) {
//                 throw new IllegalActionException(this,
//                         "Cannot have negative or zero channel bandwidth: " + value);
//             }
//             _channelBandwidth = value;
//         } else if (attribute == unitTokenSize) {
//             int value = ((IntToken) unitTokenSize.getToken())
//                     .intValue();
//             if (value <= 0) {
//                 throw new IllegalActionException(this,
//                         "Cannot have negative or zero packet size: " + value);
//             }
//             _unitTokenSize = value;
//         } else if( attribute == allowPDV){
//             boolean value = ((BooleanToken) allowPDV
//                     .getToken()).booleanValue();
//             _allowPDV = value;
//         } else if( attribute == allowPriority){
//             boolean value = ((BooleanToken) allowPriority
//                     .getToken()).booleanValue();
//             _allowPriority = value;
//         }
//         super.attributeChanged(attribute);
//     }
    
//     /** Move tokens from the input queue to the switch fabric, move tokens
//      *  from the switch fabric queue to the output queues and send tokens from the
//      *  output queues to the target receivers. When moving tokens between
//      *  queues the appropriate delays are considered.
//      *  @exception IllegalActionException If the token cannot be sent to
//      *  target receiver.
//      */
//     public void fire() throws IllegalActionException {
//         Time currentTime = getDirector().getModelTime();
//         // In a continuous domain this actor could be fired before any token has
//         // been received; _nextTimeFree could be null.
//         if (_nextFireTime != null && currentTime.compareTo(_nextFireTime) == 0) {

//             // move tokens from input queue to switch fabric
//             double _priorityDelay = 0.0;
//             double _packetSizeDelay = 0.0;
            
            
//             TimedEvent event;
//             for (int i = 0; i < _numberOfPorts; i++) {
//                 if (_inputTokens.get(i).size() > 0) {
//                     event = _inputTokens.get(i).first();
//                     if (event.timeStamp.compareTo(currentTime) == 0) {
//                         Time lastTimeStamp = currentTime;
//                         if (_switchFabricQueue.size() > 0) {
//                             lastTimeStamp = _switchFabricQueue.last().timeStamp;
//                         }
//                         // TIMING MODIFICATIONS //
//                         Object[] _tokenContent = (Object[])event.contents;
                        
//                         RecordToken TCPFrame = (RecordToken)_tokenContent[1];
                        
//                         // actual content
//                         RecordToken tokens = (RecordToken)TCPFrame.get("tokens");
                        
//                         RecordToken TCPHeader = (RecordToken)TCPFrame.get("TCPlabel");
                        
//                         if( tokens == null || TCPHeader == null){
//                             throw new IllegalActionException(this, "Token structure must"
//                                     + "contain a tokens and a TCPHeader field");
//                         }
                        
//                         /* priority is carried as a part of the options field
//                         of the TCP header */
//                         IntToken recordPriority = ((IntToken)TCPHeader.get("options"));
//                         //IntToken value = (IntToken)record.get("priority");
//                         // get priority value
//                         // subtract the 'priority' token from the record.
//                         int numberOfTokens = tokens.length();
                        
//                         double packetLength = numberOfTokens*_unitTokenSize + TCPHeaderSize;
//                         // get priority value
//                         if(recordPriority == null){
//                             // disallow priority
//                             _allowPriority = false;
//                         }
//                         // apply priority related delay
//                         if( true == _allowPDV)
//                         {
                            
//                                 if ( packetLength > 0.0){
                                    
//                                     _packetSizeDelay = packetLength/_channelBandwidth;
//                                 }
//                                 else
//                                 {
//                                     _packetSizeDelay = 0.0;
//                                 }
//                         } 
//                         else{
//                                 _packetSizeDelay = 0;
//                         }
                        
//                        if(true == _allowPriority){
//                            int _priority = recordPriority.intValue();
//                            _priorityDelay = _priority/1000.0;
//                            //_priorityDelay = ((DoubleToken)recordPriority.divide(new DoubleToken(10.0))).doubleValue();
//                        }
//                        else{
//                           //
//                        }
//                         _switchFabricQueue.add(new TimedEvent(lastTimeStamp
//                                 .add(_switchFabricDelay+_priorityDelay+_packetSizeDelay), event.contents));
//                         _inputTokens.get(i).remove(event);
//                     }
//                 }
//             }

//             // move tokens from switch fabric to output queue

//             if (_switchFabricQueue.size() > 0) {
//                 event = _switchFabricQueue.first();
//                 if (event.timeStamp.compareTo(currentTime) == 0) {
//                     Object[] output = (Object[]) event.contents;
//                     Receiver receiver = (Receiver) output[0];

//                     Actor actor;
//                     if (receiver instanceof IntermediateReceiver) {
//                         actor = (Actor) ((IntermediateReceiver) receiver).quantityManager;
//                     } else {
//                         actor = (Actor) receiver.getContainer().getContainer();
//                     }
//                     int actorPort = _actorPorts.get(actor);
//                     Time lastTimeStamp = currentTime;
//                     if (_outputTokens.get(actorPort).size() > 0) {
//                         lastTimeStamp = _outputTokens.get(actorPort).last().timeStamp;
//                     }
//                     _outputTokens.get(actorPort).add(
//                             new TimedEvent(lastTimeStamp
//                                     .add(_outputBufferDelay), event.contents));
//                     _switchFabricQueue.remove(event);
//                 }
//             }

//             // send tokens to target receiver

//             for (int i = 0; i < _numberOfPorts; i++) {
//                 if (_outputTokens.get(i).size() > 0) {
//                     event = _outputTokens.get(i).first();
//                     if (event.timeStamp.compareTo(currentTime) == 0) {
//                         Object[] output = (Object[]) event.contents;
//                         Receiver receiver = (Receiver) output[0];
//                         Token token = (Token) output[1];
//                         _sendToReceiver(receiver, token);
//                         _outputTokens.get(i).remove(event);
//                     }
//                 }
//             }

//             if (_debugging) {
//                 _debug("At time " + currentTime + ", completing send");
//             }
//         }
//     }
   

    
   // private TreeSet<TimedEvent> _switchFabricQueue;
    
    //channel bandwidth that will be used to determine the delay (in bits/sec)
    
    protected int _channelBandwidth;
    //unit token size in bits
    protected int _unitTokenSize;
    //allow or disallow input buffer packet delay variation
    protected boolean _allowPDV;
    //allow or disallow priority switching
    protected boolean _allowPriority;
    protected static final int TCPHeaderSize = 160;

    // user-defined parameters to govern input buffer delay adjustments
    public Parameter channelBandwidth;
    public Parameter unitTokenSize;
    public Parameter allowPDV;
    public Parameter allowPriority;
}
    
                
                
                
            
        
            
        
