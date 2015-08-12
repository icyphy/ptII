/* An actor that converts a FixToken into another FixToken with possibly
 different precision.

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
package ptolemy.actor.lib.conversions;

import ptolemy.data.FixToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.FixType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.FixPoint;
import ptolemy.math.FixPointQuantization;
import ptolemy.math.Overflow;
import ptolemy.math.Precision;
import ptolemy.math.Rounding;

///////////////////////////////////////////////////////////////////
//// FixToFix

/**
 This actor converts a FixToken into another FixToken with a specified
 precision. Note that this conversion may be lossy, in that the output
 may be an approximation of the input. The approximation can be
 constructed using a variety of rounding and overflow strategies,
 <p>
 The precision of the output is given by the <i>precision</i> parameter,
 which is an integer matrix of the form [<i>m</i>, <i>n</i>], where
 the total number of bits in the output is <i>m</i>, of which
 <i>n</i> are integer bits. The default precision is [16, 2], which means
 that an output has 16 bits, of which 2 bits represent the
 integer part.
 <p>
 The rounding strategy is defined by the <i>rounding</i> parameter and
 defaults to <i>nearest</i> (or <i>half_floor</i>), selecting the nearest
 representable value. The floor value nearer to minus infinity is used
 for values half way between representable values. Other strategies
 such as <i>truncate</i> are described under ptolemy.math.Rounding.
 <p>
 The overflow strategy is defined by the <i>overflow</i> parameter and
 defaults to <i>saturate</i> (or <i>clip</i>). Out of range values are
 saturated to the nearest representable value. Other strategies
 such as <i>modulo</i> are described under ptolemy.math.Overflow.

 @author Bart Kienhuis, Edward A. Lee, Ed Willink
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (pwhitake)
 @Pt.AcceptedRating Green (pwhitake)
 @see ptolemy.data.FixToken
 @see ptolemy.math.Overflow
 @see ptolemy.math.Precision
 @see ptolemy.math.Rounding
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
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input.setTypeAtMost(BaseType.UNSIZED_FIX);
        output.setTypeEquals(BaseType.UNSIZED_FIX);

        precision = new Parameter(this, "precision");
        precision.setTypeEquals(BaseType.INT_MATRIX);
        precision.setExpression("[16, 2]");

        rounding = new StringAttribute(this, "rounding");
        rounding.setExpression("nearest");

        overflow = new StringAttribute(this, "overflow");
        overflow.setExpression("saturate");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The precision of the output fix-point number, represented by an
     integer matrix. */
    public Parameter precision;

    /** The rounding strategy used, such as "nearest" or "truncate". */
    public StringAttribute rounding;

    /** The overflow strategy used to convert a double into a fix point,
     such as "saturate" or "to_zero". */
    public StringAttribute overflow;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set locally cached variables.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the parameter value is invalid.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == precision) {
            IntMatrixToken token = (IntMatrixToken) precision.getToken();

            if (token.getRowCount() != 1 || token.getColumnCount() != 2) {
                throw new IllegalActionException(this,
                        "Invalid precision (not a 1 by 2 matrix).");
            }

            Precision precision = new Precision(token.getElementAt(0, 0),
                    token.getElementAt(0, 1));
            _quantization = _quantization.setPrecision(precision);
            if (_quantization.getOverflow() == Overflow.GROW) {
                output.setTypeEquals(BaseType.UNSIZED_FIX);
            } else {
                output.setTypeEquals(new FixType(_quantization.getPrecision()));
            }
        } else if (attribute == rounding) {
            Rounding r = Rounding.getName(rounding.getExpression());
            _quantization = _quantization.setRounding(r);
        } else if (attribute == overflow) {
            Overflow o = Overflow.forName(overflow.getExpression());
            _quantization = _quantization.setOverflow(o);
            if (_quantization.getOverflow() == Overflow.GROW) {
                output.setTypeEquals(BaseType.UNSIZED_FIX);
            } else {
                output.setTypeEquals(new FixType(_quantization.getPrecision()));
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the value public variable in the new
     *  object to equal the cloned parameter in that new object.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        FixToFix newObject = (FixToFix) super.clone(workspace);

        // Set the type constraint.
        newObject.input.setTypeAtMost(BaseType.UNSIZED_FIX);

        // The non-primitive fields of the clone must refer to objects
        // distinct from the objects of the same name in the class.
        // If this is not done, then there may be problems with actor
        // oriented classes.
        newObject._quantization = new FixPointQuantization(
                newObject._quantization.getPrecision(),
                newObject._quantization.getOverflow(),
                newObject._quantization.getRounding());

        return newObject;
    }

    /** Read at most one token from the input and convert it to a fixed-point
     *  value with the precision given by the <i>precision</i> parameter,
     *  overflow strategy given by the <i>overflow</i> parameter,
     *  and rounding strategy given by the <i>rounding</i> parameter.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        FixToken in = (FixToken) input.get(0);
        FixPoint fixValue = in.fixValue().quantize(_quantization);
        FixToken result = new FixToken(fixValue);
        output.send(0, result);
    }

    /** Return false if the input port has no token, otherwise return
     *  what the superclass returns (presumably true).
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (!input.hasToken(0)) {
            return false;
        }

        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The quantization of the output.
    private FixPointQuantization _quantization = new FixPointQuantization(
            new Precision(0, 0), Overflow.SATURATE, Rounding.NEAREST);
}
