/* An actor that converts a DoubleToken to a FixToken.

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

import ptolemy.data.DoubleToken;
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
//// DoubleToFix
/**
This actor converts a DoubleToken to a FixToken with a specified
precision. Note that this conversion is lossy, in that the output
is an approximation of the input.  The approximation can be
constructed using rounding or truncation, depending on the value
of the <i>quantization</i> parameter. If <i>quantization</i> is
"round" (the default), then the output will be the
FixToken of the specified precision
that is nearest to the input value.  If <i>quantization</i> is
"truncate", then the output will be the FixToken of the specified precision
that is nearest to the input value, but no greater than the input
value in magnitude.
<p>
The precision of the output is given by the <i>precision</i> parameter,
which is an integer matrix of the form [<i>m</i>, <i>n</i>], where
the total number of bits in the output is <i>m</i>, of which
<i>n</i> are integer bits. The default precision is [16, 2], which means
that an output has 16 bits, of which 2 bits represent the
integer part.

@author Bart Kienhuis, Edward A. Lee
@version $Id$
@since Ptolemy II 0.4
@see ptolemy.math.Quantizer
@see ptolemy.data.FixToken
@see ptolemy.math.Precision
*/
public class DoubleToFix extends Converter {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DoubleToFix(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);
	output.setTypeEquals(BaseType.FIX);

	precision = new Parameter(this, "precision");
        precision.setTypeEquals(BaseType.INT_MATRIX);
        precision.setExpression("[16, 2]");

	quantization = new StringAttribute(this, "quantization");
        quantization.setExpression("round");
    }

    // FIXME: This actor needs an overflow parameter. However,
    // the Quantizer class, for some bizarre reason, doesn't support this.

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The precision of the output fixed-point number, which is represented
        by a 2-element integer matrix. */
    public Parameter precision;

    /** The quantization strategy used, either "round" or "truncate". */
    public StringAttribute quantization;

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
            String strategy = quantization.getExpression();
            if (strategy.equals("truncate")) {
                _quantization = TRUNCATE;
            } else if (strategy.equals("round")) {
                _quantization = ROUND;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized quantization: " + strategy);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read at most one token from the input and output the converted
     *  fixed-point value with the precision and quantization given by the
     *  corresponding parameters on the output port.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        DoubleToken in = (DoubleToken)input.get(0);
        FixToken result = null;
        switch( _quantization ) {
        case 1:
            result = new FixToken(
                    Quantizer.truncate(in.doubleValue(), _precision));
            break;
        default:
            result = new FixToken(
                    Quantizer.round(in.doubleValue(), _precision));
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

    private static final int ROUND = 0;
    private static final int TRUNCATE = 1;
}

