/* An actor that outputs a specified math function of the input.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

// NOTE: If you update the list of functions, then you will want
// to update the list in actor/lib/math.xml.

//////////////////////////////////////////////////////////////////////////
//// MathFunction
/**
Produce an output token on each firing with a value that is
equal to the specified math function of the input.
The input and output types are DoubleToken.  The functions
are exactly those in the java.lang.Math class.  They are:
<ul>
<li> <b>exp</b>: The exponential function.
If the argument is NaN, then the result is NaN.
<li> <b>log</b>: The natural logarithm function.
If the argument is NaN, then the result is NaN.
<li> <b>square</b>: The square function
If the argument is NaN, then the result is NaN.
<li> <b>sqrt</b>: The square root function.
If the argument is NaN, then the result is NaN.
<li> <b>remainder</b>: The remainder after division.
If the second operand is zero, then the result is NaN.
</ul>
<p>
NOTE: Some functions like exp, log, square, and sqrt act on a single 
operand only.  Other functions like remainder act on two operands.
The actor acquires a second input when the function is changed to
remainder, and loses the input when the function is changed back.

@author C. Fong
@version $Id$
@see AbsoluteValue
@see Scale
@see Round
@see TrigFunction
*/

public class MathFunction extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MathFunction(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // parameters
        function = new StringAttribute(this, "function");
        function.setExpression("exp");
        _function = EXP;
        
        // ports
        firstOperand = new TypedIOPort(this, "firstOperand", true, false);
	    output = new TypedIOPort(this, "output", false, true);
        firstOperand.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The function to compute.  This is a string-valued attribute
     *  that defaults to "exp".
     */
    public StringAttribute function;

    /** The port for the first operand.
     */
    public TypedIOPort firstOperand = null;

    /** The port for the second operand, if it is needed.
     */
    public TypedIOPort secondOperand = null;

    /** Output port.  The type is inferred from the connections.
     */
    public TypedIOPort output = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

            
    /** Override the base class to determine which function is being
     *  specified.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized.
     */
    public void attributeChanged(Attribute attribute)
            throws  IllegalActionException {
        try {
            if (attribute == function) {
                String spec = function.getExpression();
                if (spec.equals("exp")) {
                    _function = EXP;
                    if (secondOperand != null) {
                        secondOperand.setContainer(null);
                    }
                } else if (spec.equals("log")) {
                    _function = LOG;
                    if (secondOperand != null) {
                        secondOperand.setContainer(null);
                    }
                } else if (spec.equals("square")) {
                    _function = SQUARE;
                    if (secondOperand != null) {
                        secondOperand.setContainer(null);
                    }
                } else if (spec.equals("sqrt")) {
                    _function = SQRT;
                    if (secondOperand != null) {
                        secondOperand.setContainer(null);
                    }
                } else if (spec.equals("remainder")) {
                    _function = REMAINDER;
                    _createSecondPort();
                } else {
                    throw new IllegalActionException(this,
                        "Unrecognized math function: " + spec);
                }
            } else {
                super.attributeChanged(attribute);
            }
        } catch (NameDuplicationException e) {
            throw new InternalErrorException("Unexpected name duplication.");
        }
    }

    /** Compute the specified math function of the input.
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (firstOperand.hasToken(0)) {
            double in1 = ((DoubleToken) firstOperand.get(0)).doubleValue();
            double in2 = 0;
            if (_function == REMAINDER) {
                if (secondOperand.hasToken(0)) {
                    in2 = ((DoubleToken) secondOperand.get(0)).doubleValue();
                }
            } 
            output.send(0, new DoubleToken(_doFunction(in1,in2)));
        }
    }

    /** Invoke a specified number of iterations of this actor. Each
     *  iteration computes the math function specified by the
     *  <i>function</i> parameter on a single token. An invocation
     *  of this method therefore applies the function to <i>count</i>
     *  successive input tokens.
     *  <p>
     *  This method should be called instead of the usual prefire(),
     *  fire(), postfire() methods when this actor is used in a
     *  domain that supports vectorized actors.  This leads to more
     *  efficient execution.
     *  @param count The number of iterations to perform.
     *  @return COMPLETED if the actor was successfully iterated the
     *   specified number of times. Otherwise, return NOT_READY, and do
     *   not consume any input tokens.
     *  @exception IllegalActionException If iterating cannot be
     *  performed.
     */
    public int iterate(int count) throws IllegalActionException {
	// Check whether we need to reallocate the output token array.
	    if (count > _resultArray.length) {
	        _resultArray = new DoubleToken[count];
	    }
        
        if (firstOperand.hasToken(0,count)) {
            Token[] inArray1 = firstOperand.get(0,count);
            
            
            if (_function == REMAINDER) {
                if (secondOperand.hasToken(0,count)) {
                    Token[] inArray2 = secondOperand.get(0,count);
                    for (int i = 0; i < count; i++) {
                        double input1 =
                                ((DoubleToken)(inArray1[i])).doubleValue();
                        double input2 = 
                                ((DoubleToken)(inArray2[i])).doubleValue();
                        _resultArray[i] = 
                                new DoubleToken(_doFunction(input1,input2));
                    }
                    output.send(0, _resultArray, count);
                    return COMPLETED;
                } else {
                    return NOT_READY;
                }
            } else {
                for(int i = 0; i < count ; i++) {
                    double input1 = ((DoubleToken)(inArray1[i])).doubleValue();
                    _resultArray[i] = new DoubleToken(_doFunction(input1,0));
                }
                output.send(0, _resultArray, count);
                return COMPLETED;
            }
        } else {
            return NOT_READY;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

     
    /** Create the second port needed by remainder function
     */
    private void _createSecondPort() 
        throws NameDuplicationException, IllegalActionException {
        
        if (secondOperand == null) {
            secondOperand = new TypedIOPort(this, "secondOperand", true, false);
        } else if (secondOperand.getContainer() == null) {
            secondOperand = new TypedIOPort(this, "secondOperand", true, false);
        }        
        secondOperand.setTypeEquals(BaseType.DOUBLE);
    }

    
    /** Calculate the function on the given argument.
     *  @param in The input value.
     *  @return The result of applying the function.
     */
    private double _doFunction(double in1, double in2) {
        double result;
        switch(_function) {
        case EXP:
            result = Math.exp(in1);
            break;
        case LOG:
            result = Math.log(in1);
            break;
        case SQUARE:
            result = in1 * in1;
            break;
        case SQRT:
            result = Math.sqrt(in1);
            break;
        case REMAINDER:
            result = in1 % in2;
            break;
        default:
            throw new InternalErrorException(
                    "Invalid value for _function private variable. "
                    + "MathFunction actor (" + getFullName()
                    + ")");
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private DoubleToken[] _resultArray = new DoubleToken[1];

    // An indicator for the function to compute.
    private int _function;

    // Constants used for more efficient execution.
    private static final int EXP = 0;
    private static final int LOG = 1;
    private static final int SQUARE = 2;
    private static final int SQRT = 3;
    private static final int REMAINDER = 4;
}
            
