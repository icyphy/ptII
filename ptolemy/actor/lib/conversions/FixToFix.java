/* An actor that converts a FixToken into another FixToken with possibly
   different precision.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Green (pwhitake@eecs.berkeley.edu)
@AcceptedRating Green (pwhitake@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.conversions;

import ptolemy.data.FixToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.math.Precision;
import ptolemy.math.Quantizer;

//////////////////////////////////////////////////////////////////////////
//// FixToFix
/**
This actor converts a FixToken into another FixToken with a specified
precision. Note that this conversion may be lossy, in that the output
may be an approximation of the input.  The approximation can be
constructed using rounding or truncation, depending on the value
of the <i>quantization</i> parameter. If <i>quantization</i> is
"round" (the default), then the output will be the
FixToken of the specified precision that is nearest to the input value.
If <i>quantization</i> is "truncate", then the output will be the
FixToken of the specified precision that is nearest to the
input value, but no greater than the input value in magnitude.
<p>
The precision of the output is given by the <i>precision</i> parameter,
which is an integer matrix of the form [<i>m</i>, <i>n</i>], where
the total number of bits in the output is <i>m</i>, of which
<i>n</i> are integer bits. The default precision is [16, 2], which means
that an output has 16 bits, of which 2 bits represent the
integer part.
<p>
The input may be out of range for the output precision.  If so, then by
default, the output will be saturated, meaning that its value will be
either the maximum or the minimum that is representable in the specified
precision, depending on the sign of the input.  However, if the
<i>overflow</i> parameter is changed from "saturate" to
"overflow_to_zero", then the output is set to zero whenever overflow
occurs.

@author Bart Kienhuis and Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
@see ptolemy.math.Quantizer
@see ptolemy.data.FixToken
@see ptolemy.math.Precision
*/

public class FixToFix extends Converter {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FixToFix(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setTypeEquals(BaseType.FIX);
	output.setTypeEquals(BaseType.FIX);

	precision = new Parameter(this, "precision");
        precision.setTypeEquals(BaseType.INT_MATRIX);
        precision.setExpression("[16, 2]");

	quantization = new StringAttribute(this, "quantization");
        quantization.setExpression("round");

	overflow = new StringAttribute(this, "overflow");
        overflow.setExpression("saturate");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The precision of the output fix-point number, represented by an
        integer matrix. */
    public Parameter precision;

    /** The quantization strategy used, either "round" or "truncate". */
    public StringAttribute quantization;

    /** The overflow strategy used to convert a double into a fix point,
        either "saturate" or "overflow_to_zero". */
    public StringAttribute overflow;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set locally cached variables.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the parameter value is invalid.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == precision) {
            IntMatrixToken token = (IntMatrixToken)precision.getToken();
            if (token.getRowCount() != 1 || token.getColumnCount() != 2) {
                throw new IllegalActionException(this,
                        "Invalid precision (not a 1 by 2 matrix).");
            }
            _precision = new Precision(token.getElementAt(0, 0),
                    token.getElementAt(0, 1));
        } else if (attribute == quantization) {
            String spec = quantization.getExpression();
            if (spec.equals("truncate")) {
                _quantization = TRUNCATE;
            } else if (spec.equals("round")) {
                _quantization = ROUND;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized quantization: " + spec);
            }
        } else if (attribute == overflow) {
            String spec = overflow.getExpression();
            if (spec.equals("saturate")) {
                _overflow = SATURATE;
            } else if (spec.equals("overflow_to_zero")) {
                _overflow = OVERFLOW_TO_ZERO;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized overflow strategy: " + spec);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read at most one token from the input and convert it to a fixed-point
     *  value with the precision given by the <i>precision</i> parameter
     *  and overflow strategy given by the <i>overflow</i> parameter.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        FixToken result = null;
        FixToken in = (FixToken)input.get(0);
        switch(_quantization) {
        case 1:
            result = new FixToken(
                    Quantizer.truncate(in.fixValue(),
                            _precision, _overflow));
            break;
        default:
            result = new FixToken(
                    Quantizer.round(in.fixValue(), _precision, _overflow));
        }
        output.send(0, result);
    }

    /** Return false if the input port has no token, otherwise return
     *  what the superclass returns (presumably true).
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        if (!input.hasToken(0)) {
            return false;
        }
        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The precision of the output.
    private Precision _precision = null;

    // The quantization strategy, encoded as an int.
    private int _quantization = ROUND;

    // The overflow strategy, encoded as an int.
    private int _overflow = SATURATE;

    private static final int ROUND = 0;
    private static final int TRUNCATE = 1;

    private static final int SATURATE = 0;
    private static final int OVERFLOW_TO_ZERO = 1;

}
