/* An actor that detects collisions.

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

package ptolemy.domains.wireless.lib;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// CollisionDetector

/** 
This actor models a typical physical layer front end of a wireless
receiver. It models a receiver where messages have a non-zero duration
and messages can collide with one another, causing a failure to receive.
A message is provided to this actor at the time corresponding to the
start of its transmission. Along with the message (an arbitrary
token), the inputs must provide the duration of the message and its
power.  The message spans an interval of time starting when it is
provided to this actor and ending at that time plus the duration.
If another message overlaps with a given message and has sufficient
power, then the given message will be sent to the <i>collided</i>
output.  Otherwise it is sent to the <i>received</i> output. In both
cases, the message appears at the corresponding output at the time
it is received plus the duration (i.e. the time at which the message
has been completed).
<p>
The inputs are:
<ul>
<li> <i>message</i>: The message carried by each transmission.
<li> <i>power</i>: The power of the received signal at the
     location of this receiver.
<li> <i>duration</i>: The time duration of the transmission.
</ul>
The power and duration are typically delivered by the channel
in the "properties" field of the transmission. The power
is usually given as a power density (per unit area) so that
a receiver can multiply it by its antenna area to determine
the received power. It is in a linear scale, typically with
units such as watts per square meter.  The duration is a
non-negative double, and the message is an arbitrary token.
<p>
The outputs are:
<ul>
<li> <i>received</i>: The message received. This port produces
     an output only if the received power is sufficient
     and there are no collisions.  The output is produced at a
     time equal to the time this actor receives the message plus
     the value received on the <i>duration</i> input.
<li> <i>collided</i>: The message discarded. This port produces
     an output only if the received message collides with another
     message of sufficient power.  The output is produced at a
     time equal to the time this actor receives the message plus
     the value received on the <i>duration</i> input.  The value
     of the output is the message that cannot be received.
</ul>
<p>
This actor is typically used with a channel that delivers a properties
record token that contains <i>power</i> and <i>duration</i> fields.
These fields can be extracted by using a GetProperties actor followed
by a RecordDisassembler. The PowerLossChannel, for example.
In order for the type constraints to be satisfied, however, the
PowerLossChannel's <i>defaultProperties</i> parameter must be
augmented with a default value for the <i>duration</i>.
Each transmitter can override that default with its own message
duration and transmit power.
<p>
Any message whose power (as specified at the <i>power</i> input)
is less than the value of the <i>powerThreshold</i> parameter
is ignored. It will not cause collisions and is not produced
at the <i>collided</i> output.  The <i>powerThreshold</i> parameter
thus specifies the power level at which the receiver simply fails
to detect the signal.  It is given in a linear scale with the same
units as the <i>power</i> input.
<p>
Any message whose power exceeds <i>powerThreshold</i> has the
potential of being successfully received, of failing to be
received due to a collision, and of causing a collision.
A message is successfully received if throughout its duration,
its power exceeds the sum of all other message powers
by at least <i>SNRThresholdInDB</i> (which as the name
suggests, is given in decibels, rather than in a linear
scale, as is customary for power ratios).  Formally, let the
message power for the <i>i</i>-th message be
<i>p</i><sub><i>i</i></sub>(<i>t</i>) at time <i>t</i>.
Before the message is received and after its duration
expires, this power is zero.  The <i>i</i>-th message
is successfully received if
<quote>
<i>p</i><sub><i>i</i></sub>(<i>t</i>) >= <i>P</i>
<font face="Symbol">S</font><sub>(<i>j</i> != <i>i</i>)</sub>
<i>p</i><sub><i>j</i></sub>(<i>t</i>)
</quote>
for all <i>t</i> where <i>p</i><sub><i>i</i></sub>(<i>t</i>) > 0,
where
<quote>
<i>P</i> = 10^(<i>SNRThresholdInDB</i>/10)
</quote>
which is the signal to interference ration in a linear scale.
<p>
The way this actor works is that each input that has sufficient power
is recorded in a hash table indexed by the time at which its duration
expires. In addition, the director is requested to fire this actor at
that time.  Any time a message arrives, the actor checks for collisions,
and marks any message subjected to a collision by this arrival.
When the duration expires, the message is produced on one of the
two outputs depending on whether it is marked as having encountered
a collision, and it is removed from the hash table.
<p>
NOTE: This actor assumes that the duration of messages is short
relative to the rate at which the actors move. That is, the received
power (and whether a receiver is in range) is determined once, at the
time the message starts, and remains constant throughout the transmission.

@author Yang Zhao, Xiaojun Liu, Edward Lee
@version $Id$
@see PowerLossChannel
@see GetProperties
@see ptolemy.actor.lib.RecordDisassembler
*/
public class CollisionDetector extends TypedAtomicActor {

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
    public CollisionDetector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create and configure the ports.       
        message = new WirelessIOPort(this, "message", true, false);
        new Attribute(message, "_showName");
        
        power = new TypedIOPort(this, "power", true, false);
        power.setTypeEquals(BaseType.DOUBLE);
        new Attribute(power, "_showName");

        duration = new TypedIOPort(this, "duration", true, false);
        duration.setTypeEquals(BaseType.DOUBLE);
        new Attribute(duration, "_showName");
        
        received = new TypedIOPort(this, "received", false, true);
        received.setTypeSameAs(message);
        new Attribute(received, "_showName");
        
        collided = new TypedIOPort(this, "collided", false, true);
        collided.setTypeSameAs(message);
        new Attribute(collided, "_showName");
        
        // Configure parameters.
        SNRThresholdInDB = new Parameter(this, "SNRThresholdInDB");
        SNRThresholdInDB.setTypeEquals(BaseType.DOUBLE);
        SNRThresholdInDB.setExpression("Infinity");
        
        powerThreshold = new Parameter(this, "powerThreshold");
        powerThreshold.setTypeEquals(BaseType.DOUBLE);
        powerThreshold.setExpression("0.0");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The input port for message.  This has undeclared type.
     */
    public TypedIOPort message;
    
    /** The input port for power. This has type double, and is typically
     *  a power density, in units such as watts per square meter.
     */
    public TypedIOPort power;
    
    /** The time that a message transmission takes.
     */
    public TypedIOPort duration;
    
    /** The output port that produces messages that do not
     *  encounter a collison. This has the same type as the message input.
     */
    public TypedIOPort received;
    
    /** The output port that produces messages that cannot be
     *  received because of a collision. This has the same type as the
     *  message input.
     */
    public TypedIOPort collided;
    
    /** The threshold for the signal to be recognized from interference. 
     *  It is specified in decibels (10 * log<sub>10</sub>(<i>r</i>),
     *  where <i>r</i> is the power ratio.  This is a double that
     *  defaults to Infinity, which indicates that all overlapping
     *  messages are lost to collisions.
     */
    public Parameter SNRThresholdInDB;
    
    /** The power threshold above which the signal can be 
     *  detected at the receiver. Any message with a received power
     *  below this number is ignored.  This has type double
     *  and defaults to 0.0, which indicates that no message
     *  (with nonzero power) is ignored.
     */
    public Parameter powerThreshold;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** If the specified attribute is <i>SNRThresholdInDB</i>,
     *  or <i>powerThreshold<i> then
     *  check that a positive number is given. Otherwise,
     *  defer to the base class.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == SNRThresholdInDB) {
            double SNRThresholdInDBValue = ((DoubleToken)
                    SNRThresholdInDB.getToken()).doubleValue();
            if (SNRThresholdInDBValue <= 0.0) {
                throw new IllegalActionException(this,
                "SNRThresholdInDB is required to be positive. "
                + "Attempt to set it to: " 
                + SNRThresholdInDBValue);            
            } else {
                // Convert to linear scale.
                _SNRThresholdInDB = 
                        Math.pow(10, SNRThresholdInDBValue/10);
            }
            
        } else if (attribute == powerThreshold) {
            _powerThreshold = ((DoubleToken)
                    powerThreshold.getToken()).doubleValue();
            if (_powerThreshold < 0.0) {
                throw new IllegalActionException(this,
                "powerThreshold is required to be nonnegative. "
                + "Attempt to set it to: " 
                + _powerThreshold);            
            }
        } else {
            super.attributeChanged(attribute);
        }
    }
    
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
        CollisionDetector newObject = (CollisionDetector)super.clone(workspace);

        newObject.received.setTypeSameAs(newObject.message);
        newObject.collided.setTypeSameAs(newObject.message);

        return newObject;
    }

    /** If a new message is available at the inputs, record it in the
     *  hashtable indexed with the time that the message shall be completed,
     *  and loop through the hashtable to check whether there is collision.
     *  If the current time matches one of the times that we have previously
     *  recorded as the completion time for a transmission, then output the 
     *  received message to the <i>received<i> output port if it is not
     *  lost to a collision; otherwise, output it to the <i>collided<i>
     *  output port.
     *  @exception IllegalActionException If an error occurs reading
     *   or writing inputs or outputs.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        double currentTime = getDirector().getCurrentTime();
        if (_debugging) {
            _debug("---------------------------------");
            _debug("Current time is: " + currentTime);
        }
        if(message.hasToken(0) && power.hasToken(0)
                && duration.hasToken(0)) {
            double powerValue = ((DoubleToken)
                    power.get(0)).doubleValue();
            if (_debugging) {
                _debug("Received message with power: " + powerValue);
            }
            if (powerValue < _powerThreshold) {
                // The signal it too weak to be detected, simply drop it.
                message.get(0);
                duration.get(0);
                if (_debugging) {
                    _debug("Message power is below threshold. Ignoring.");
                }
            } else {
                // Record the reception.        
                Reception reception = new Reception();
                reception.data = message.get(0);
                reception.power = powerValue;
                reception.arrivalTime = currentTime;
                reception.collided = false;
                reception.duration = ((DoubleToken)
                        duration.get(0)).doubleValue();
                if (_debugging) {
                    _debug("Message is above threshold and has duration: " + reception.duration);
                }
            
                // Update the total power density.
                _totalPower = _totalPower + reception.power; 
                        
                // Put the new reception into the list of prior receptions.
                double time = currentTime + reception.duration;
                reception.expiration = time;
                
                _receptions.add(reception);
            
                // Schedule this actor to be fired at the end of 
                // the duration of the message.
                getDirector().fireAt(this, time);
            }
        }
        // Loop through the prior receptions (and the new one)
        // to mark whether a message is collided acording to the new total
        // power density. Also, any prior receptions that are now
        // expiring are sent to one of the two outputs.  
        Iterator priorReceptions = _receptions.listIterator();
        while (priorReceptions.hasNext()) {
            Reception priorReception = (Reception)priorReceptions.next();
            if (_debugging) {
                _debug("Checking reception with arrival time: "
                + priorReception.arrivalTime);
            }
            // If the reception is now expiring, send it to one of the two
            // output ports.
            if (priorReception.expiration == currentTime) {
                if (_debugging) {
                    _debug("Current time matches expiration " +
                        "time of a prior message that arrived at: "
                        + priorReception.arrivalTime);
                }
                // The time matches a pending reception.
                priorReceptions.remove();
                
                // Update the total power.
                _totalPower = _totalPower - priorReception.power;
                
                // Quantization errors may take this negative. Do not allow.
                if (_totalPower < 0.0) _totalPower = 0.0;
                
                if(!priorReception.collided) {
                    received.send(0, priorReception.data);
                    if (_debugging) {
                        _debug("Message has been received: " + priorReception.data);
                    }
                } else {
                    collided.send(0, priorReception.data);
                    if (_debugging) {
                        _debug("Message has been lost: " + priorReception.data);
                    }
                }
                continue;
            }
            
            // Check the snr to see whether to mark this prior reception
            // collided.
            double powerWithoutThisOne = _totalPower - priorReception.power;
            // Quantization errors may make this negative.
            if (powerWithoutThisOne < 0.0) {
                powerWithoutThisOne = 0.0;
            }
            double snr = priorReception.power / powerWithoutThisOne;

            if (!priorReception.collided && snr <= _SNRThresholdInDB) {
                priorReception.collided = true;
                if (_debugging) {
                    _debug("Message now has a collision. SNR is: "
                    + snr
                    + ". Total power is: "
                    + _totalPower);
                }
            }
        }            
    }
    
    /** Initialize the private variables.
     *  @exception IllegalActionException If thrown by the base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _totalPower = 0.0;
        _receptions.clear();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private double _powerThreshold;
    private double _SNRThresholdInDB;
    private double _totalPower;
    // Record messages that have been received but
    // haven't completed transmission.
    private List _receptions = new LinkedList();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Data structure for storing reception data.
     */
    private class Reception {
        // The message token.
        public Token data;
        public double duration;
        public double arrivalTime;
        public double expiration;
        public double power;
        public boolean collided;
    }
}
