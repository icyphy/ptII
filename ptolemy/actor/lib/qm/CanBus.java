/* This actor implements a Network Bus.

@Copyright (c) 2010-2011 The Regents of the University of California.
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

package ptolemy.actor.lib.qm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ptolemy.actor.IOPort;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/** This actor is an {@link QuantityManager} that simulates a CAN bus network
 *  When its {@link #sendToken(Receiver, Receiver, Token)} method is called,
 *  the delivery of the specified token to the specified receiver is delayed according to the CAN protocol.  
 *  <p> 
 *  The CAN bus is a serial communication protocol that supports real-time systems with high reliability.
 *  Its main features are: priority-based bus access and non destructive content-based arbitration.
 *  If two or more nodes attempt to transmit a message on the idle bus, the access conflicts are resolved 
 *  by performing a bitwise arbitration (non destructive) according to the content of the identifier (called here <i>CanId</i>).
 *  Our {@link QuantityManager} simulates such content-based arbitration.
 *  A node attempting to transmit a message when the bus is busy must try again when the bus will be free (in fact, there is a 
 *  queue with messages that did not win the bus during arbitration or arrived when the bus is busy).
 *  </p>
 *  <p>
 *  In order to perform such an arbitration, it is needed to add a parameter called <i>CanId</i> to every sending actor.
 *  <i>CanId</i> is a positive integer. The higher is CanId the lower is the priority.
 *  (note that in the reality the arbitration is done bit to bit. The higher is the <i>identifier</i> the higher is the priority)
 *  It is also possible (and even advisable) to use the {@link CanBusPriority} actor.
 *  It is just needed to put it after the port(s) we want to connect to the bus. 
 *  The {@link CanBusPriority} actor is specifically designed for use with the CanBus quantity manager.
 *  The <i>CanId</i> parameter is already added and is visible on Vergil (enhancing visibility).
 *  </p> 
 *  <p>
 *  Messages sent on the Bus are stored and delivered on due time.  
 *  Since the CAN protocol cover the second layer of the OSI model, messages sent on the bus are encapsulated 
 *  in frames according to the CAN protocol.
 *  We consider that messages sent by actors correspond to exactly one frame.
 *  Also, two formats of frames are provided by the CAN standard: the base frame and the extended frame.
 *  We can choose the standard according to which the simulation will be performed.
 *  </p>  
 *  <p>
 *  Of course, the bit rate of the bus is also a parameter that can be modified. 
 *  Typical bit rates for the CAN bus range from 125 Kbits/second to 1 Mbits/second.
 *  </p>
 *  <p>  
 *  Future work: implementing an application layer of the OSI model based on CAN, taking errors into account
 *  by delaying the deliveries of messages, finest management of time by dividing the time continuum in periods of 1/bitRate,
 *  bit stuffing...
 *  </p>
 *  
 *  For more information please refer to: <i>CAN bus simulator using a Quantity Manager</i>.
 *       
 *  @author D. Marciano, G. Lasnier, P. Derler
 *  @version $Id$
   @since Ptolemy II 0.2

   @Pt.ProposedRating Red (derler)
   @Pt.AcceptedRating Red (derler)
 */
public class CanBus extends MonitoredQuantityManager {

    /** Construct a CanBus with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CanBus(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        canFormatOfFrame = new Parameter(this, "canFormatOfFrame");
        canFormatOfFrame.setTypeEquals(BaseType.STRING);
        canFormatOfFrame.addChoice("\"Standard frame\"");
        canFormatOfFrame.addChoice("\"Extended frame\"");
        canFormatOfFrame.setExpression("\"Standard frame\"");
        _frameSize = 108;

        bitRate = new Parameter(this, "bitRate");
        bitRate.setDisplayName("bitRate (kbit/s)");
        bitRate.setExpression("125");
        bitRate.setTypeEquals(BaseType.DOUBLE);
        _bitRate = 125;
        
        canFramePolicy = new Parameter(this, "canFramePolicy");
        canFramePolicy.setTypeEquals(BaseType.STRING);
        canFramePolicy.addChoice("\"Send all frames\"");
        canFramePolicy.addChoice("\"Send only most recent frame\"");
        canFramePolicy.setExpression("\"Send all frames\"");
        _mostRecentFrame = false;

        _tokenTree = new TreeMap<Integer, LinkedList<Object[]>>();
        _multiCast = new HashMap<Integer, Integer>();

    }

    ///////////////////////////////////////////////////////////////////
    //                          public variables                     //

    /** The bit rate of the bus. This is a double with default value to 125 (Kbit/second).
     *  It is required to be positive.
     */
    public Parameter bitRate;

    /** The format of frame. This is a string with default value to "Standard frame".
     *  It is required to be either "Standard frame" or "Extended frame".
     */
    public Parameter canFormatOfFrame;
    
    /**
     * The selected policy for the frame queue behavior. This is a string with 
     * the default value "Send All Frames".
     */
    public Parameter canFramePolicy;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>bitRate</i>, then ensure that the value
     *  is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the service time is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == bitRate) {
            double value = ((DoubleToken) bitRate.getToken()).doubleValue();
            if (value <= 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative or zero bitRate: " + value);
            }
            _bitRate = value;
        } else if (attribute == canFormatOfFrame) {
            String value = ((StringToken) canFormatOfFrame.getToken())
                    .stringValue();
            if (value.equalsIgnoreCase("Standard frame")) {
                _frameSize = 108;
            } else if (value.equalsIgnoreCase("Extended frame")) {
                _frameSize = 128;
            }

        } else if (attribute == canFramePolicy) {
            String value = ((StringToken) canFramePolicy.getToken())
                    .stringValue();
            if (value.equalsIgnoreCase("Send all frames")) {
                _mostRecentFrame = false;                
            } else if (value.equalsIgnoreCase("Send only most recent frame")) {
                _mostRecentFrame = true;             
            }
        }
        super.attributeChanged(attribute);
    }

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same ports as the original, but
     *  no connections and no container.  A container must be set before
     *  much can be done with this actor.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new CanBus.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        CanBus newObject = (CanBus) super.clone(workspace);

        newObject._tokenTree = new TreeMap<Integer, LinkedList<Object[]>>();
        newObject._multiCast = new HashMap<Integer, Integer>();
        newObject._frameSize = _frameSize;
        newObject._nextTokenSize = _nextTokenSize;
        newObject._nextTokenFiringTime = null;
        newObject._startingTime = null;
        newObject._channelUsed = _channelUsed;
        newObject._bitRate = _bitRate;
        newObject._mostRecentFrame = _mostRecentFrame;
        return newObject;

    }

    /** Fire the actor. 
     *  Typically, the fire() method performs the computation associated
     *  with an actor. 
     *  Here, it delivers (if required) the intended token to the intended receiver(s).  
     *
     *  @exception IllegalActionException If firing is not permitted.
     */
    public void fire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();

        // 'If' statement that allows to construct the 'multiCast' Map
        if (!_multiCast.containsKey(_channelUsed)) {
            HashSet<Receiver> receiverSet = new HashSet<Receiver>();
            ListIterator<Object[]> li = _tokenTree.get(_channelUsed)
                    .listIterator();
            while (li.hasNext()) {
                receiverSet.add((Receiver) ((li.next())[0]));
            }
            _multiCast.put(_channelUsed, receiverSet.size());
        }
        
        // "Most recent frame" case, we sort the list of frames of the bus owner
        // by testing if the frame is visible at the current firing time.
        if (_mostRecentFrame) {
        	LinkedList<Object []> listToSort = _tokenTree.get(_channelUsed);
        	
        	for (int i = 0; i < listToSort.size(); i++) {
        		for (int mostRecent = i +1 ; mostRecent < listToSort.size(); mostRecent++) {
        			
        			// If a message will be delivered at time t we consider that
        			// the CanBus will be occupied at time t. So , all messages 
        			// arriving at time t must be re-emitted => "> 0" condition.
        		    if (listToSort.get(i)[0] == listToSort.get(mostRecent)[0] 
        		    	  && (this.getDirector().getModelTime())
                          	.compareTo(((Time) listToSort.get(mostRecent)[2])
                          			.add(nextTokenTransmissionTime())) > 0) {        		    	
        	        	listToSort.remove(i);
        	        	i--;
        			}
        		}
        	}
		}

        // delivers (if required) the intended token to the intended receiver
        if (_nextTokenFiringTime != null && _nextTokenFiringTime == currentTime) {
            for (int i = 0; i < _multiCast.get(_channelUsed); i++) {
                Object[] o = _tokenTree.get(_channelUsed).poll();
                _sendToReceiver((Receiver) o[0], (Token) o[1]);
            }
            _scheduleRefire();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an intermediate receiver that wraps a given receiver.
     *  @param receiver The receiver that is being wrapped.
     *  @return A new intermediate receiver.
     */
    public IntermediateReceiver createIntermediateReceiver(Receiver receiver) {
        IntermediateReceiver intermediateReceiver = new IntermediateReceiver(
                this, receiver);
        return intermediateReceiver;
    }

    /** Create a receiver to mediate a communication via the specified receiver. This
     *  receiver is linked to a specific port of the quantity manager.
     *  @param receiver Receiver whose communication is to be mediated.
     *  @param port Port of the quantity manager.
     *  @return A new receiver.
     *  @exception IllegalActionException If the receiver cannot be created.
     */
    public Receiver getReceiver(Receiver receiver, IOPort port)
            throws IllegalActionException {
        return createIntermediateReceiver(receiver);
    }

    /** Initialize the actor.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        _tokenTree.clear();
        _multiCast.clear();
        _nextTokenSize = 0;
        _nextTokenFiringTime = null;
        _startingTime = null;
        _channelUsed = 0;
    }

    /** Method that computes the identifier ('CanId') of the message that has 
     * the highest priority. 
     * 
     * @return The identifier ('CanId') of the message that has the highest priority.
     */
    public int nextCanId() {
        Set es = _tokenTree.entrySet();
        Iterator<Map.Entry<Integer, LinkedList<Object[]>>> it = es.iterator();
        Integer result = it.next().getKey();

        while (_tokenTree.get(result).isEmpty() && it.hasNext()) {
            result = it.next().getKey();
        }
        if (_tokenTree.get(result).isEmpty()) {
            result = -1;
        }
        return result;
    }

    /** Return the next token to be sent according to the CAN protocol.
     *  
     * @return The next token to be sent according to the CAN protocol.
     */
    public Token nextToken() {
        return (Token) (_tokenTree.get(nextCanId()).element())[1];
    }

    /** Method that compute the size of the next token that need to be sent according to the CAN protocol
     *  This method uses the serialization API to compute the size of the token that need to be sent
     *  Actually, this method is never called in the current version of the code
     *  If you want to use a variable size for objects sent through the network, you need to uncomment the line
     *  dedicated to this functionality in the {@link #nextTokenSize()} method.
     * @return The size of the next token to be sent.
     */
    public int nextTokenSize() {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ObjectOutputStream oOut;
        try {
            oOut = new ObjectOutputStream(bOut);
            oOut.writeObject(nextToken());
            oOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        _nextTokenSize = bOut.toByteArray().length;
        return _nextTokenSize;
    }

    /** Compute the transmission time of the next token through the network
     *  In the current version of this {@link QuantityManager} the size of a token (message) is fixed
     *  In order to have a variable token size please uncomment the dedicated line in this method.
     * @return Transmission time for the next token to be sent through the network.
     */
    public double nextTokenTransmissionTime() {
        //  Variable frame size
    	//  return nextTokenSize()/(_bitRate*1000);
    	
        return _frameSize / (_bitRate * 1000);
    }

    /** Method that print in a human readable way the content of {@link #_tokenTree}.
     * 
     */
    public void printTokenTree() {
        Set<Map.Entry<Integer, LinkedList<Object[]>>> es = _tokenTree
                .entrySet();
        Iterator<Map.Entry<Integer, LinkedList<Object[]>>> it = es.iterator();
        Map.Entry<Integer, LinkedList<Object[]>> entry;

        while (it.hasNext()) {
            entry = it.next();
            if (_debugging) {
            	_debug("Key: " + entry.getKey().toString()); 
            
            	if (!entry.getValue().isEmpty()) { 
            		for (int i=0; i < entry.getValue().size(); i++) {
            			_debug("Receiver: " 
            					+ ((Receiver) entry.getValue().get(i)[0]).toString() 
            					+ " Token: "
            					+ ((Token) entry.getValue().get(i)[1]).toString()
            					+ " Time: "+((Time) entry.getValue().get(i)[2])
            						.getDoubleValue());
            		}
            	}
            }
        }
    }

    /**
     * Reset the quantity manager.
     */
    public void reset() {

    }

    /** Initiate a send of the specified token to the specified
     *  receiver. This method will schedule a refiring of this actor 
     *  according to the requirements of the CAN protocol.
     *  
     *  @param source Sender of the token.
     *  @param receiver The receiver to send to.
     *  @param token The token to send.
     *  @exception IllegalActionException If the refiring request fails.
     */
    public void sendToken(Receiver source, Receiver receiver, Token token)
            throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();       			

        // 'CanId' parameter
        Parameter priority = (Parameter) ((NamedObj) ((IntermediateReceiver) source).source)
                .getAttribute("CanId");

        // 'CanId' value
        int id = ((IntToken) priority.getToken()).intValue();

        if (!_tokenTree.containsKey(id)) {
            _tokenTree.put(id, new LinkedList<Object[]>());
        }

        // Storage of the token until it's delivered to the specified receiver at the scheduled time
        if (nextCanId() == -1) {
            Time visibleAt = this.getDirector().getModelTime(); 
            
            _channelUsed = id;
            
            ((LinkedList<Object[]>) _tokenTree.get(id)).add(new Object[] {
                    receiver, token, visibleAt });

            _nextTokenFiringTime = currentTime.add(nextTokenTransmissionTime());
            _fireAt(_nextTokenFiringTime);
            _startingTime = currentTime;
            
        } else {
        	Time visibleAt = this.getDirector().getModelTime();
        	
        	((LinkedList<Object[]>) _tokenTree.get(id)).add(new Object[] {
    				receiver, token, visibleAt});
    		if (currentTime.equals(_startingTime)) {
    			_channelUsed = nextCanId();
    		}
        }
    }

    /** Schedule a refiring of the actor.
     *  @exception IllegalActionException Thrown if the actor cannot be rescheduled.
     */
    protected void _scheduleRefire() throws IllegalActionException {

        if (nextCanId() != -1) {
            Time currentTime = getDirector().getModelTime();
            _nextTokenFiringTime = currentTime.add(nextTokenTransmissionTime());
            _channelUsed = nextCanId();
            _fireAt(_nextTokenFiringTime);
        }
    }

    /** Data structure in which all tokens received and valuable information will be stocked
     * The <i>Integer</i> key corresponds to the <i>CanId</i> parameter (identifier of the message that sets the priority).
     * Object[0] corresponds to the receiver to which the token is intended to.
     * Object[1] corresponds to the token that needs to be sent.
     */
    private TreeMap<Integer, LinkedList<Object[]>> _tokenTree;

    /** Data structure that will store information concerning multicast
     *  The <i>Integer</i> key corresponds to the <i>CanId</i>
     *  The <i>Integer</i> value corresponds to the number of receivers connected to the bus (i.e. processed by the
     *  quantity manager) and to the transmitting actor (which correspond to a multicast)
     *  This information is important, since multicasted messages on the CAN bus are received at the same time.
     */
    private HashMap<Integer, Integer> _multiCast;

    /**
     * Fixed size of a frame.
     */
    private int _frameSize;

    /**
     * Size of the next token due to be sent.
     */
    private int _nextTokenSize;

    /**
     * Scheduled date of delivery.
     */
    private Time _nextTokenFiringTime;

    /**
     * Variable used in the case of a collision.
     */
    private Time _startingTime;

    /**
     * Represents the identifier of the message that is being transmitted on the Bus.
     */
    private int _channelUsed;

    /**
     * Value of the bit rate of the bus.
     */
    private double _bitRate;
    
    /**
     * Value of the frame sending policy.
     */
    private boolean _mostRecentFrame;

}
