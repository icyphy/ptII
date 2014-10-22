/* An actor that outputs a random sequence with a uniform distribution.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
//// Uniform

/**
 Produce a random sequence with a uniform distribution.  On each iteration,
 a new random number is produced.  The output port is of type DoubleToken.
 The values that are generated are independent and identically distributed
 with bounds defined by the parameters.  In addition, the
 seed can be specified as a parameter to control the sequence that is
 generated.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (cxh)
 @see ptolemy.actor.lib.Bernoulli
 @see ptolemy.actor.lib.DiscreteRandomSource
 @see ptolemy.actor.lib.Rician
 @see ptolemy.actor.lib.Triangular
 */
public class Uniform extends RandomSource {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Uniform(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output.setTypeEquals(BaseType.DOUBLE);

        lowerBound = new PortParameter(this, "lowerBound", new DoubleToken(0.0));
        lowerBound.setTypeEquals(BaseType.DOUBLE);
        new SingletonParameter(lowerBound.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        upperBound = new PortParameter(this, "upperBound", new DoubleToken(1.0));
        upperBound.setTypeEquals(BaseType.DOUBLE);
        new SingletonParameter(upperBound.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The lower bound.
     *  This parameter contains a DoubleToken, initially with value 0.0.
     */
    public PortParameter lowerBound;

    /** The upper bound.
     *  This parameter contains a DoubleToken, initially with value 0.0.
     */
    public PortParameter upperBound;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send a random number with a uniform distribution to the output.
     *  This number is only changed in the prefire() method, so it will
     *  remain constant throughout an iteration.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        lowerBound.update();
        upperBound.update();
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
        double lowerValue = ((DoubleToken) lowerBound.getToken()).doubleValue();
        double upperValue = ((DoubleToken) upperBound.getToken()).doubleValue();

        if (lowerValue > upperValue) {
            throw new IllegalActionException(this,
                    "Invalid bounds: lowerBound is greater than upperBound.");
        }

        double rawNum = _random.nextDouble();
        _current = rawNum * (upperValue - lowerValue) + lowerValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The random number for the current iteration.
    private double _current;
}
