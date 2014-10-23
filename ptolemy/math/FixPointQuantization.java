/** A class defining quantization to a FixPoint number.

 Copyright (c) 2002-2014 The Regents of the University of California.
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
package ptolemy.math;

import java.util.StringTokenizer;

/**
 The FixPointQuantization class defines the mapping of numeric values
 with unlimited precision to the finite precision supported by arithmetic
 using the FixPoint class.
 <p>
 It comprises a
 <ul>
 <li>
 <b>Precision</b>: to define the accuracy of the finite precision numbers.
 <li>
 <b>Overflow</b>: to define the treatment of out-of-range numbers.
 <li>
 <b>Rounding</b>: to define the loss of precision for in-range numbers.
 </ul>
 The active class functionality is provided by the quantize method, which
 is normally invoked from FixPoint.quantize to enforce quantization
 constraints upon the result of an unconstrained computation.
 <p>
 An instance of the class is immutable, meaning
 that its value is set in the constructor and cannot then be modified.

 @author Ed Willink
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (Ed.Willink)
 @Pt.AcceptedRating Red
 @see ptolemy.math.FixPoint
 @see ptolemy.math.Precision
 @see ptolemy.math.Overflow
 @see ptolemy.math.Rounding
 */
public class FixPointQuantization extends Quantization {
    /** Construct a FixPointQuantization object based on the provided
     *  string.  The string may consist of just <i>precision</i> or
     *  <i>precision, overflow</i> or
     *  <i>precision, overflow, rounding</i>, and may optionally be
     *  enclosed in parentheses.
     *
     *  <p> <i>precision</i> must be one of the Precision formats;
     *  <i>integer-bits.fraction-bits</i> or
     *  <i>total-bits/integer-bits</i>.
     *
     *  <p> <i>overflow</i> must be
     *  one of the Overflow strategies; e.g.  <i>saturate</i> or
     *  <i>modulo</i> or <i>trap</i>.   The default is <i>saturate</i>.
     *
     *  <p> <i>rounding</i> must be one
     *  of the Rounding strategies; e.g.  <i>up</i> or <i>half_even</i>
     *  or <i>unnecessary</i>.   The default is <i>nearest</i>.
     *
     *
     *  @param string The string representing the
     *  quantization specification.
     *  @exception IllegalArgumentException If the precision string
     *   supplied, overflow strategy or rounding strategy does not
     *   match one of the known formats.
     */
    public FixPointQuantization(String string) throws IllegalArgumentException {
        super(Overflow.SATURATE, Rounding.NEAREST);

        int strLen = string.length();

        if (strLen >= 2 && string.charAt(0) == '('
                && string.charAt(strLen - 1) == ')') {
            string = string.substring(1, strLen - 1);
        }

        StringTokenizer st = new StringTokenizer(string, ",");

        if (!st.hasMoreTokens()) {
            throw new IllegalArgumentException("A precision string"
                    + " consisting of two integers separated "
                    + " by a '/', or '.' token is required");
        }

        _precision = new Precision(st.nextToken());

        if (st.hasMoreTokens()) {
            _overflow = Overflow.getName(st.nextToken());
        }

        if (st.hasMoreTokens()) {
            _rounding = Rounding.getName(st.nextToken());
        }

        if (st.hasMoreTokens()) {
            throw new IllegalArgumentException("FixPointQuantization "
                    + "requires at most a precision overflow and rounding,");
        }
    }

    /** Construct a FixPointQuantization with specified precision, overflow
     *  and rounding.
     *
     *  @param precision The precision.
     *  @param overflow The overflow.
     *  @param rounding The rounding.
     */
    public FixPointQuantization(Precision precision, Overflow overflow,
            Rounding rounding) {
        super(overflow, rounding);
        _precision = precision;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the given object describes the same
     *  mapping to quantized values as this object.
     *
     *  @param object The FixPointQuantization object to use for equality
     *  checking.
     *  @return True if the quantizations are equal.
     */
    @Override
    public boolean equals(Object object) {
        if (super.equals(object) && object instanceof FixPointQuantization) {
            FixPointQuantization other = (FixPointQuantization) object;

            if (_precision.equals(other._precision)) {
                return true;
            }
        }

        return false;
    }

    /** Return the precision of the mantissa of the value.
     *  @return The precision.
     */
    @Override
    public Precision getMantissaPrecision() {
        return _precision;
    }

    /**
     * Return the precision.
     *
     * @see #setPrecision(Precision)
     * @return Precision object.
     */
    @Override
    public Precision getPrecision() {
        return _precision;
    }

    /** Return a hash code value for this FixPointQuantization. This
     *  method returns the bitwise xor of the hashcode of the
     *  superclass (Quantization) and the hashcode of the precision.
     *  @return A hash code value for this Quantization.
     */
    @Override
    public int hashCode() {
        return super.hashCode() >>> _precision.hashCode();
    }

    /** Return a FixPointQuantization with a changed overflow strategy.
     *  @param overflow The new overflow strategy.
     *  @return A new quantization.
     */
    public FixPointQuantization setOverflow(Overflow overflow) {
        return new FixPointQuantization(_precision, overflow, getRounding());
    }

    /** Return a FixPointQuantization with a changed precision.
     *
     *  @see #getPrecision()
     *  @param precision The new precision.
     *  @return A new quantization.
     */
    public FixPointQuantization setPrecision(Precision precision) {
        return new FixPointQuantization(precision, getOverflow(), getRounding());
    }

    /** Return a FixPointQuantization with a changed rounding strategy.
     *  @param rounding The new rounding.strategy.
     *  @return A new quantization.
     */
    public FixPointQuantization setRounding(Rounding rounding) {
        return new FixPointQuantization(_precision, getOverflow(), rounding);
    }

    /** Return a string representing this quantization. The string is
     *  expressed as "(<i>m.n, o, p</i>)", where <i>m</i>
     *  indicates the number of integer bits, <i>n</i> represents
     *  the number of fractional bits and <i>o</i> represents
     *  the overflow strategy and <i>r</i> represents
     *  the rounding strategy.
     *  @return A string representing this quantization.
     */
    @Override
    public String toString() {
        // TODO: check for null precision
        return "(" + _precision.getIntegerBitLength() + "."
        + _precision.getFractionBitLength() + ","
        + getOverflow().toString() + "," + getRounding().toString()
        + ")";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The precision. */
    protected Precision _precision = null;
}
