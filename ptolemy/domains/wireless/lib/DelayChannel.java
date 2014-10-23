/* A channel with a specified propagation speed.

 Copyright (c) 2003-2014 The Regents of the University of California.
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

 */
package ptolemy.domains.wireless.lib;

import java.util.HashMap;

import ptolemy.actor.Director;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.domains.wireless.kernel.WirelessReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// DelayChannel

/**
 Model of a wireless channel with a specified propagation speed and
 loss probability. The propagation speed introduces delay that depends
 on the distance between the transmitter and receiver at the time
 that the transmitter initiates the communication. This channel model
 assumes that the duration of the message is (effectively) zero, so
 that neither the transmitter nor the receiver move during the
 transmission. It also assumes that the receiver does not move during
 propagation. I.e., it assumes that the propagation of the message is
 much faster than the node movement.
 <p>
 A speed equal to Infinity (the default) results in no
 propagation delay. If the loss probability is greater than zero then on each
 call to the transmit() method, for each receiver in range,
 with the specified probability, the transmission to that
 receiver will not occur.  Whether a transmission occurs to a particular
 receiver is independent of whether it occurs to any other receiver.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class DelayChannel extends ErasureChannel {
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
    public DelayChannel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        propagationSpeed = new Parameter(this, "propagationSpeed");
        propagationSpeed.setTypeEquals(BaseType.DOUBLE);
        propagationSpeed.setExpression("Infinity");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The propagation speed. This determines the delay between
     *  transmission and reception.  This is a double that defaults
     *  to Infinity, which means that there is no delay.
     */
    public Parameter propagationSpeed;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>propagationSpeed</i>, then
     *  check that a positive number is given. Otherwise,
     *  defer to the base class.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == propagationSpeed) {
            double speed = ((DoubleToken) propagationSpeed.getToken())
                    .doubleValue();

            if (speed <= 0.0) {
                throw new IllegalActionException(this,
                        "Invalid value for propagationSpeed: " + speed);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        DelayChannel newObject = (DelayChannel) super.clone(workspace);
        newObject._receptions = null;
        return newObject;
    }

    /** If the current time matches one of the times that we have previously
     *  recorded as the reception time for a transmission, then deliver
     *  the token to the receiver.
     *  @exception IllegalActionException If the _transmitTo() method of the
     *  base class throws it, i.e. the token attribute of the reception
     *  cannot be converted or the token attribute is null and the receiver
     *  attribute of the receptions does not support clear.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (_receptions != null) {
            // We may be getting fired because of an impending event.
            Time currentTime = getDirector().getModelTime();
            Double timeDouble = Double.valueOf(currentTime.getDoubleValue());
            Reception reception = (Reception) _receptions.get(timeDouble);

            if (reception != null) {
                // The time matches a pending reception.
                _receptions.remove(timeDouble);

                // Use the superclass, not this class, or we just delay again.
                super._transmitTo(reception.token, reception.sender,
                        reception.receiver, reception.properties);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Transmit the specified token to the specified receiver with the
     *  specified properties.  If <i>propagationSpeed</i> is less than
     *  Infinity, then this results in a call to fireAt() of
     *  the director for each receiver that is in range. The token is
     *  not actually transmitted to the receiver until the corresponding
     *  invocation of fire() occurs. The time delay is equal to
     *  distance/<i>propagationSpeed</i>.  See the class comments
     *  for the assumptions that make this correct.
     *  <p>
     *  If the <i>lossProbability</i> is zero, (the default) then
     *  the specified receiver will receive the token if it has room.
     *  If <i>lossProbability</i> is greater than zero, the token will
     *  be lost with the specified probability, independently
     *  for each receiver in range.
     *  Note that in this base class, a port is in range if it refers to
     *  this channel by name and is at the right place in the hierarchy.
     *  This base class makes no use of the properties argument.
     *  But derived classes may limit the range or otherwise change
     *  transmission properties using this argument.
     *  @param token The token to transmit, or null to clear
     *   the specified receiver.
     *  @param sender The sending port.
     *  @param receiver The receiver to which to transmit.
     *  @param properties The transmit properties (ignored in this base class).
     *  @exception IllegalActionException If the token cannot be converted
     *   or if the token argument is null and the destination receiver
     *   does not support clear.
     */
    @Override
    protected void _transmitTo(Token token, WirelessIOPort sender,
            WirelessReceiver receiver, RecordToken properties)
                    throws IllegalActionException {
        double speed = ((DoubleToken) propagationSpeed.getToken())
                .doubleValue();

        if (speed == Double.POSITIVE_INFINITY) {
            super._transmitTo(token, sender, receiver, properties);
        } else {
            Director director = getDirector();

            // FIXME: This isn't right because the receiver
            // may have moved during propagation.  Maybe
            // register a ValueListener to the _location attributes
            // of the receiver actors, and continually recalculate
            // the correct arrival time for the message each time the
            // receiver location changes.  Even so, this will be
            // an approximation, and needs to be fully characterized.
            // Also, the receiver needs to be in range at the
            // conclusion of the propagation, whereas this method is
            // called only if the receiver is in range at the
            // initiation of the transmission.
            WirelessIOPort destination = (WirelessIOPort) receiver
                    .getContainer();
            double distance = _distanceBetween(sender, destination);
            Time time = director.getModelTime().add(distance / speed);

            if (_receptions == null) {
                _receptions = new HashMap();
            }

            Double timeDouble = Double.valueOf(time.getDoubleValue());
            Reception reception = new Reception();
            reception.token = token;
            reception.sender = sender;
            reception.receiver = receiver;
            reception.properties = properties;
            _receptions.put(timeDouble, reception);

            _fireAt(time);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Record of scheduled receptions, indexed by time.
    private HashMap _receptions;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Data structure for storing reception data.
     */
    private static class Reception {

        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        public Token token;

        public WirelessIOPort sender;

        public WirelessReceiver receiver;

        public RecordToken properties;
    }
}
