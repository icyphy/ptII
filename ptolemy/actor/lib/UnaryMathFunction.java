/* An actor that computes a specified math function of the input.

 Copyright (c) 2004-2014 The Regents of the University of California.
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

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

// NOTE: If you update the list of functions, then you will want
// to update the list in actor/lib/math.xml.
///////////////////////////////////////////////////////////////////
//// UnaryMathFunction

/**
 Produce an output token on each firing with a value that is
 equal to the specified math function of the input.
 The input and output types are DoubleToken.  The functions
 are a subset of those in the java.lang.Math class.  They are:
 <ul>
 <li> <b>exp</b>: The exponential function.
 This is the default function for this actor
 If the argument is NaN, then the result is NaN.
 <li> <b>log</b>: The natural logarithm function.
 If the argument is NaN, then the result is NaN.
 <li> <b>sign</b>: If the argument is greater than 0, return 1.0, if
 it is less than 0, return -1.0, otherwise return 0.0.
 <li> <b>square</b>: The square function
 If the argument is NaN, then the result is NaN.
 <li> <b>sqrt</b>: The square root function.
 If the argument is NaN, then the result is NaN.
 </ul>
 <p>

 @author C. Fong, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 @see AbsoluteValue
 @see Remainder
 @see Scale
 @see TrigFunction
 */
public class UnaryMathFunction extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public UnaryMathFunction(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Parameters
        function = new StringParameter(this, "function");
        function.setExpression("exp");
        function.addChoice("exp");
        function.addChoice("log");
        function.addChoice("sign");
        function.addChoice("square");
        function.addChoice("sqrt");
        _function = _EXP;

        // Ports
        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);

        // Create a simple rectangle icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-15\" " + "width=\"60\" height=\"30\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The function to compute.  This is a string-valued attribute
     *  that defaults to "exp".
     */
    public StringParameter function;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to determine which function is being
     *  specified.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == function) {
            String functionName = function.stringValue();

            if (functionName.equals("exp")) {
                _function = _EXP;
            } else if (functionName.equals("log")) {
                _function = _LOG;
            } else if (functionName.equals("sign")) {
                _function = _SIGN;
            } else if (functionName.equals("square")) {
                _function = _SQUARE;
            } else if (functionName.equals("sqrt")) {
                _function = _SQRT;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized math function: " + functionName);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Consume at most one input token from each input channel, and
     *  compute the specified math function of the input.
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            double inputValue = ((DoubleToken) input.get(0)).doubleValue();
            output.send(0, new DoubleToken(_doFunction(inputValue)));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Calculate the function on the given argument.
     *  @param input The input value.
     *  @return The result of applying the function.
     */
    private double _doFunction(double input) {
        double result;

        switch (_function) {
        case _EXP:
            result = Math.exp(input);
            break;

        case _LOG:
            result = Math.log(input);
            break;

        case _SIGN:

            if (input > 0) {
                result = 1.0;
            } else if (input < 0) {
                result = -1.0;
            } else {
                result = 0.0;
            }

            break;

        case _SQUARE:
            result = input * input;
            break;

        case _SQRT:
            result = Math.sqrt(input);
            break;

        default:
            throw new InternalErrorException(
                    "Invalid value for _function private variable. "
                            + "MathFunction actor (" + getFullName() + ")"
                            + " on function type " + _function);
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // An indicator for the function to compute.
    private int _function;

    // Constants used for more efficient execution.
    private static final int _EXP = 0;

    private static final int _LOG = 1;

    private static final int _SIGN = 2;

    private static final int _SQUARE = 3;

    private static final int _SQRT = 4;
}
