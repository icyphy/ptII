/* An actor that detect collision.

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

package ptolemy.domains.wireless.lib.network;

import java.util.Enumeration;
import java.util.Hashtable;

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

//////////////////////////////////////////////////////////////////////////
//// CollisionDetector

/** 
This actor models a network component that detects whether there is 
collision for messages received. Each message has a duration time 
and a power density level. If the power density of a message is
above a threshold defined by the <i>powerDensityThreshold<i> parameter,
this actor caches it in a hash table with the index key to be the 
time that the message completes, and ask the director to fire
it again at that time to output the message. If If the power density 
of a message is below the threshold, it simply ignore the message.

A message may be corrupted if during the duration of its transmission
some other message is received and the signal to interference ratio 
is below the ratio threshold specified by the 
<i>signalToInterferenceRatio<i> parameter. In this case, it claims 
that a collision is detected and the message is marked as corrupted.
If a message is corrupted, it is send to the <i>corrupted<i> output port
when the duration time elapsed. If it is not corrupted, it is send 
to the <i>received<i> output port.

Every time a new message is received or a message transmission has been
completed (removed from the hash table), the total interference power 
density is recalculated. The actor then loop through the hash table to
check whether a message is corrupted or not according the the new
interference power density.
 
FIXME: add more documentation.


@author Yang Zhao, Xiaojun Liu, Edward Lee
@version $Id$
*/
public class CollisionDetector extends TypedAtomicActor {

    /** Construct a channel with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This relation will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container.
     *  @param name The name of the relation.
     *  @exception IllegalActionException If the container is incompatible
     *   with this relation.
     *  @exception NameDuplicationException If the name coincides with
     *   a relation already in the container.
     */
    public CollisionDetector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // Create and configure the ports.       
        input = new WirelessIOPort(this, "input", true, false);
        powerDensity = new TypedIOPort(this, "powerDensity", true, false);
        duration = new TypedIOPort(this, "duration", true, false);
        powerDensity.setTypeEquals(BaseType.DOUBLE);
        duration.setTypeEquals(BaseType.DOUBLE);
        
        received = new TypedIOPort(this, "received", false, true);
        received.setTypeSameAs(input);
        corrupted = new TypedIOPort(this, "corrupted", false, true);
        corrupted.setTypeSameAs(input);
        
        //configure parameters.
        signalToInterferenceRatio = new Parameter(this, "signalToInterferenceRatio(db)");
        signalToInterferenceRatio.setTypeEquals(BaseType.DOUBLE);
        signalToInterferenceRatio.setExpression("Infinity");
        powerDensityThreshold = new Parameter(this, "powerDensityThreshold");
        powerDensityThreshold.setTypeEquals(BaseType.DOUBLE);
        powerDensityThreshold.setExpression("1.0E-6");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The input port of messages.
     */
    public TypedIOPort input;
    
    /** The input port of powerDensity.
     */
    public TypedIOPort powerDensity;
    
    /** The time that a message transmission takes.
     */
    public TypedIOPort duration;
    
    /** Output port that sends out messages without collison.
     */
    public TypedIOPort received;
    
    /** Output port that sends out messages corrupted.
     */
    public TypedIOPort corrupted;
    
    /** The threshold for the signal to be recognized from interference. 
     *  It is specified as db.
     */
    public Parameter signalToInterferenceRatio;
    
    /** The power density threshold above which the signal can be 
     *  detected at the receiver. 
     */
    public Parameter powerDensityThreshold;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** If the specified attribute is <i>propagationSpeed</i>, then
     *  check that a positive number is given. Otherwise,
     *  defer to the base class.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == signalToInterferenceRatio) {
            double signalToInterferenceRatioDB = ((DoubleToken)
                    signalToInterferenceRatio.getToken()).doubleValue();
            if (signalToInterferenceRatioDB <= 0.0) {
                throw new IllegalActionException(this,
                "Invalid value for signalToInterferenceThreshold: " 
                + signalToInterferenceRatioDB);            
            } else {
                _signalToInterferenceRatio = 
                        Math.pow(10, signalToInterferenceRatioDB/10);
            }
            
        }else if (attribute == powerDensityThreshold) {
            _powerDensityThreshold = ((DoubleToken)
                    powerDensityThreshold.getToken()).doubleValue();
            if (_powerDensityThreshold <= 0.0) {
                throw new IllegalActionException(this,
                "Invalid value for signalToInterferenceThreshold: " 
                + _powerDensityThreshold);            
            }
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    /** If the current time matches one of the times that we have previously
     *  recorded as the reception time for a transmission, then deliver
     *  the token to the receiver.
     *  @exception IllegalActionException If the token cannot be converted
     *   or if the token argument is null and the destination receiver
     *   does not support clear.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        double currentTime = getDirector().getCurrentTime();
        if(input.hasToken(0) && powerDensity.hasToken(0)
                && duration.hasToken(0)) {
            double pwDensity = ((DoubleToken)
                    powerDensity.get(0)).doubleValue();
            if(pwDensity >= _powerDensityThreshold){
                //record the reception.        
                Reception reception = new Reception();
                reception.data = input.get(0);
                reception.powerDensity = pwDensity;
                reception.arrivalTime = currentTime;
                reception.duration = ((DoubleToken)
                        duration.get(0)).doubleValue();
                //check whether this message has collision.
                if (_totalPowerDensity == 0.0 || 
                       (_totalPowerDensity > 0.0 &&
                       reception.powerDensity/_totalPowerDensity
                        > _signalToInterferenceRatio)) {
                    reception.currupted = false;
                } else {
                    reception.currupted = true;
                }
            
                //update the total power density.
                _totalPowerDensity = _totalPowerDensity
                        + reception.powerDensity; 
            
                //loop through the hash table to mark whether a 
                //message is corrupted acording to the new total
                //power density.        
                Enumeration keys = _receptions.keys();
                while (keys.hasMoreElements()) {
                    Double key = (Double)keys.nextElement();
                    Reception r = (Reception)_receptions.get(key);
                    double snr = 
                        r.powerDensity/(_totalPowerDensity-r.powerDensity);
                    if (!r.currupted && 
                            snr <= _signalToInterferenceRatio) {
                        _receptions.remove(r);
                        r.currupted = true;
                        _receptions.put(key, r);
                    }
                
                }
            
                //put the new reception to the hash table.
                double time = currentTime + reception.duration;
                _receptions.put(new Double(time), reception);
            
                //schedule this actor to be fired at the end of 
                //the duration of the message.
                getDirector().fireAt(this, time);
            } else {
                // The signal it too weak to be detected, simply drop it.
                input.get(0);
                powerDensity.get(0);
                duration.get(0);
            }
        }
        if (_receptions != null) {
            // We may be getting fired because of an impending event.
            Double timeDouble = new Double(currentTime);
            Reception reception = (Reception)_receptions.get(timeDouble);
            if (reception != null) {
                // The time matches a pending reception.
                _receptions.remove(reception);
                //update the total power density.
                _totalPowerDensity = _totalPowerDensity - 
                        reception.powerDensity;
                if(!reception.currupted) {
                    received.send(0, reception.data);
                } else {
                    corrupted.send(0, reception.data);
                }
            }
        }
    }
    
    /** Initialize the private varialbles.
     *  @exception IllegalActionException If thrown by the base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _totalPowerDensity = 0.0;
        _receptions = new Hashtable();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private double _powerDensityThreshold;
    private double _signalToInterferenceRatio;
    private double _totalPowerDensity;
    // Record of scheduled receptions, indexed by time.
    private Hashtable _receptions;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class Reception {
        public Token data;
        public double duration;
        public double arrivalTime;
        public double powerDensity;
        public boolean currupted;
    }
}
