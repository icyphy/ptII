/* A model of a track in air traffic control systems.

 Copyright (c) 2015 The Regents of the University of California.
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
package ptolemy.domains.atc.lib;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.atc.kernel.ATCDirector;
import ptolemy.domains.atc.kernel.Rejecting;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** A model of a track in air traffic control systems.
 *  This track can have no more than one aircraft in transit.
 *  If there is one in transit, then it rejects all inputs.
 *  @author Marjan Sirjani and Edward A. Lee
 */
public class Track extends TypedAtomicActor implements Rejecting{

    /** Create a new Track actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public Track(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);
        delay = new Parameter(this, "delay");
        delay.setTypeEquals(BaseType.DOUBLE);
        delay.setExpression("1.0");
    }

    /** The input port.  The type is not specified. */
    public TypedIOPort input;

    /** The output port.  The type is not specified. */
    public TypedIOPort output;

    /** The delay.  The default is 1.0 */
    public Parameter delay;

    @Override
    public boolean reject(Token token, IOPort port) {
        return (_inTransit != null);
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        if (currentTime.equals(_transitExpires) && _inTransit != null) {
            try {
                output.send(0, _inTransit);
            } catch (NoRoomException ex) {
                // Token rejected by the destination.
                if (!(director instanceof ATCDirector)) {
                    throw new IllegalActionException(this, "Track must be used with an ATCDirector.");
                }
                double additionalDelay = ((ATCDirector)director).handleRejectionWithDelay(this);
                if (additionalDelay < 0.0) {
                    throw new IllegalActionException(this, "Unable to handle rejection.");
                }
                _transitExpires = _transitExpires.add(additionalDelay);
                director.fireAt(this, _transitExpires);
                // Shouldn't have any inputs because we have rejected them if we are occupied.
                return;
            }
            _inTransit = null;
        }
        // Handle any input that have been accepted.
        if (input.hasToken(0)) {
            _inTransit = input.get(0);
            _transitExpires = currentTime.add(
                    ((DoubleToken)delay.getToken()).doubleValue());
            director.fireAt(this, _transitExpires);
        }
    }

    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _inTransit = null;
    }

    private Token _inTransit;
    private Time _transitExpires;
}
