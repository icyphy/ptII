/* Limits angles to the appropriate range.

 Copyright (c) 2003-2004 The Regents of the University of California.
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
@ProposedRating Red (acataldo@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.apps.softwalls;

import java.util.List;

import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// AngleProcessor
/**
Given an input angle, it computes the equivalent angle in the range
[minAngle, maxAngle).  Typically, maxAngle - minAngle = 2 * pi
(radians) or 360 (degrees) or 2 (normalized angle).

minAngle and maxAngle are parameters.

The inputAngle, minAngle, and maxAngle must be DoubleTokens.

@author Adam Cataldo
@version $Id$
@since Ptolemy II 2.0.1
*/
public class AngleProcessor extends TypedAtomicActor {
    /** Constructs an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public AngleProcessor(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {

        super(container, name);

        // Create and configure ports and parameters
        inputAngle = new TypedIOPort(this, "inputAngle", true, false);
        outputAngle = new TypedIOPort(this, "outputAngle", false, true);
        minAngle = new PortParameter(this,
                "minAngle", new DoubleToken(0.0));
        maxAngle = new PortParameter(this,
                "maxAngle", new DoubleToken(2 * Math.PI));
        inputAngle.setTypeEquals(BaseType.DOUBLE);
        outputAngle.setTypeEquals(BaseType.DOUBLE);
        minAngle.setTypeEquals(BaseType.DOUBLE);
        maxAngle.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input angle in radians. This port is of type Double
     */
    public TypedIOPort inputAngle;

    /** The output angle in radians. This port is of type Double
     */
    public TypedIOPort outputAngle;

    /** The minimum angle.  The initial value is a double of value 0.0.
     */
    public PortParameter minAngle;

    /** The maximum angle.  The initial value is a double of value 2 * PI.
     */
    public PortParameter maxAngle;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the minAngle and maxAngle values.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If an exception is thrown
     *  from reading an attribute.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == minAngle) {
            _minAngle =
                ((DoubleToken)(minAngle.getToken())).doubleValue();
        } else if (attribute == maxAngle) {
            _maxAngle =
                ((DoubleToken)(maxAngle.getToken())).doubleValue();
        }
    }

    /** Output the correct angle.
     *  @exception IllegalActionException If an exception is
     *  thrown from the input port, or if the minimum angle is
     *  greater than the maximum angle.
     */
    public void fire() throws IllegalActionException {
        double angle;
        double range;

        angle = ((DoubleToken)(inputAngle.get(0))).doubleValue();

        minAngle.update();
        maxAngle.update();
        _minAngle = ((DoubleToken)(minAngle.getToken())).doubleValue();
        _maxAngle = ((DoubleToken)(maxAngle.getToken())).doubleValue();

        if (_minAngle >= _maxAngle) {
            throw new IllegalActionException(getContainer(),
                    "minAngle >= maxAngle");
        }

        range = _maxAngle - _minAngle;

        while (angle < _minAngle) {
            angle = angle + range;
        }
        while (angle >= _maxAngle) {
            angle = angle - range;
        }

        outputAngle.send(0, new DoubleToken(angle));
    }

    /** Initialize the minAngle and maxAngle values.
     *  @exception IllegalActionException If an exception is thrown
     *  from reading the minAngle or maxAngle parameters.
     */
    public void initialize() throws IllegalActionException {
        _minAngle = ((DoubleToken)(minAngle.getToken())).doubleValue();
        _maxAngle = ((DoubleToken)(maxAngle.getToken())).doubleValue();
    }

    /** Return true of the inputAngle port has a token, otherwise return
     *  false.   
     *  @exception IllegalActionException If the hasToken() method
     *  throws exception.
     */
    public boolean prefire() throws IllegalActionException {
        return inputAngle.hasToken(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    private double _minAngle, _maxAngle;
}

