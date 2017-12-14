/* An actor that outputs a random sequence with a triangular distribution.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Triangular

/**
 Produce a random sequence with a triangular distribution.  On each iteration,
 a new random number is produced.  The output port is of type double.
 The values that are generated are independent and identically distributed
 with mode and bounds as defined by the parameters.  In addition, the
 seed can be specified as a parameter to control the sequence that is
 generated.

 @author Raymond A. Cardillo
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.actor.lib.Bernoulli
 @see ptolemy.actor.lib.DiscreteRandomSource
 @see ptolemy.actor.lib.Rician
 @see ptolemy.actor.lib.Uniform
 */
public class Triangular extends RandomSource {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Triangular(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output.setTypeEquals(BaseType.DOUBLE);

        mode = new PortParameter(this, "mode", new DoubleToken(0.5));
        mode.setTypeEquals(BaseType.DOUBLE);
        new SingletonParameter(mode.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        min = new PortParameter(this, "min", new DoubleToken(0.0));
        min.setTypeEquals(BaseType.DOUBLE);
        new SingletonParameter(min.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        max = new PortParameter(this, "max", new DoubleToken(1.0));
        max.setTypeEquals(BaseType.DOUBLE);
        new SingletonParameter(max.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The minimum value.
     *  This parameter contains a DoubleToken, initially with value 0.0.
     */
    public PortParameter min;

    /** The maximum value.
     *  This parameter contains a DoubleToken, initially with value 1.0.
     */
    public PortParameter max;

    /** The mode of the distribution (peak of triangle).
     *  This parameter contains a DoubleToken, initially with value 0.5.
     */
    public PortParameter mode;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send a random number with a triangular distribution to the output.
     *  This number is only changed in the prefire() method, so it will
     *  remain constant throughout an iteration.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        mode.update();
        min.update();
        max.update();
        super.fire();
        output.send(0, new DoubleToken(_current));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate a new random number.
     *  @exception IllegalActionException If parameter values are incorrect.
     */
    @Override
    protected void _generateRandomNumber() throws IllegalActionException {
        double minValue = ((DoubleToken) min.getToken()).doubleValue();
        double maxValue = ((DoubleToken) max.getToken()).doubleValue();
        double modeValue = ((DoubleToken) mode.getToken()).doubleValue();

        if (minValue > maxValue) {
            throw new IllegalActionException(this,
                    "Invalid bounds: min is greater than max.");
        }

        if (modeValue < minValue) {
            throw new IllegalActionException(this,
                    "Invalid bounds: mode is less than min.");
        }

        if (modeValue > maxValue) {
            throw new IllegalActionException(this,
                    "Invalid bounds: mode is greater than max.");
        }

        double rawNum = _random.nextDouble();
        double left = modeValue - minValue;
        double whole = maxValue - minValue;
        double right = maxValue - modeValue;

        if (rawNum <= left / whole) {
            _current = minValue + Math.sqrt(rawNum * whole * left);
        } else {
            _current = maxValue - Math.sqrt((1.0d - rawNum) * whole * right);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The random number for the current iteration.
    private double _current;
}
