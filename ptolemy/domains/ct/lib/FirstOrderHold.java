/* An actor that takes a value and a derivative and does first order
   projection.

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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTWaveformGenerator;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// FirstOrderHold
/**
Convert discrete events at the input to a continuous-time signal at the
output by projecting the value with the derivative.

@author Jie Liu
@version $Id$
@since Ptolemy II 2.0
*/

public class FirstOrderHold extends Transformer
    implements CTWaveformGenerator {

    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The subsystem that this actor is lived in
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public FirstOrderHold(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        derivative = new TypedIOPort(this, "derivative", true, false);
        defaultValue = new Parameter(this, "defaultValue");
        defaultValue.setExpression("0.0");
        defaultDerivative = new Parameter(this, "defaultDerivative");
        defaultDerivative.setExpression("0.0");

        input.setTypeEquals(BaseType.DOUBLE);
        derivative.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
        new Parameter(input, "signalType",
                new StringToken("DISCRETE"));
        new Parameter(derivative, "signalType",
                new StringToken("DISCRETE"));
        new Parameter(output, "signalType",
                new StringToken("CONTINUOUS"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                       ////

    /** The input port that takes the derivative. The type is double.
     */
    public TypedIOPort derivative;

    /**Default input derivative before receiving ay inputs.
     * The default is an integer with value 0.
     * The type of the output is set to at least this type.
     */
    public Parameter defaultDerivative;

    /**Default output before any input has received.
     * The default is an integer with value 0.
     * The type of the output is set to at least this type.
     */
    public Parameter defaultValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume the input and derivative if they both present, and output
     *  the first order projection of the last input and its
     *  derivative.
     *  @exception IllegalActionException If the token cannot be sent.
     */
    public void fire() throws IllegalActionException{
        CTDirector director = (CTDirector)getDirector();
        if (director.isDiscretePhase()) {
            if ((input.hasToken(0) && (!derivative.hasToken(0))) ||
                    !input.hasToken(0) && derivative.hasToken(0)) {
                throw new IllegalActionException(this,
                        " No synchronized inputs.");
            }
            if (input.hasToken(0) && derivative.hasToken(0)) {
                _value = ((DoubleToken)input.get(0)).doubleValue();
                _derivative = ((DoubleToken)derivative.get(0)).doubleValue();
                _time = director.getCurrentTime();
                if (_debugging) _debug(getFullName(),
                        " get inputs: (" + _value,
                        ", " + _derivative + ").");
            }
        }
        output.send(0, new DoubleToken(_value + (director.getCurrentTime()
                - _time)*_derivative));
    }

    /** Initialize token. If there is no input, the initial token is
     *  a zero Double Token.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException{
        super.initialize();
        _value = ((DoubleToken)defaultValue.getToken()).doubleValue();
        _derivative =
            ((DoubleToken)defaultDerivative.getToken()).doubleValue();
        _time = getDirector().getCurrentTime();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Saved token.
    private double _value;

    private double _derivative;

    private double _time;
}
