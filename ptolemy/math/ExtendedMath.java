/* A library of additional mathematical operations beyond those provided
   by the Java Math class.

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

@ProposedRating Yellow (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.math;


//////////////////////////////////////////////////////////////////////////
//// ExtendedMath
/**
ExtendedMath is a library of additional mathematical operations
beyond those provided by the Java Math class.

@author Albert Chen, William Wu, Edward A. Lee, Jeff Tsay
@version $Id$
@since Ptolemy II 0.2
*/
public class ExtendedMath {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the inverse hyperbolic cosine of the argument.
     *  The argument is required to be greater than one, or an
     *  IllegalArgumentException is thrown (this is a runtime
     *  exception, so it need not be declared).
     *  The returned value is positive.
     */
    public static final double acosh(final double x) {
        // FIXME: Is the range of the argument correct?
        if (x < 1) {
            throw new IllegalArgumentException("ExtendedMath.acosh: Argument "
                    + "is required to be greater than 1.  Got " + x);
        }
        return Math.log( x+Math.sqrt(x*x-1) );
    }

    /** Return the inverse hyperbolic sine of the argument.
     */
    public static final double asinh(final double x) {
        double result;
        if (x < 0) {
            result = -Math.log( -x+Math.sqrt(x*x+1) );
        } else {
            result = Math.log( x+Math.sqrt(x*x+1) );
        }
        return result;
    }

    /** Return the hyperbolic cosine of the argument.
     */
    public static final double cosh(final double x) {
        return (Math.exp(x) + Math.exp(-x))/2;
    }

    /** Implement Euclid's method for finding the Greatest Common Divisor
     * (GCD) of
     *  two numbers.  If the numbers are negative, then we compute the
     *  GCD of their absolute values.
     */
    public static int gcd(int u, int v) {
        int t;
        if (u < 0) u = -u;
        if (v < 0) v = -v;
        while (u > 0) {
            if (u < v)
                { t = u; u = v; v = t; }
            else
                { u = u % v; }
        }
        return v;
    }

    /** Return the base-10 logarithm of the argument. */
    public static final double log10(final double x) {
        return Math.log(x) * _ONEOVERLN10;
    }

    /** Return the base-2 logarithm of the argument. */
    public static final double log2(final double x) {
        return Math.log(x) * _ONEOVERLN2;
    }

    /** Compute the remainder after dividing the first argument by the
     *  second argument as prescribed by the IEEE 754 standard.  This
     *  is implemented by the java.lang.Math class method IEEERemainder.
     *  The documentation for that class says:
     *
     *  <p> "The remainder value is mathematically equal to f1 - f2
     *  &times; <i>n</i>, where <i>n</i> is the mathematical integer
     *  closest to the exact mathematical value of the quotient f1/f2,
     *  and if two mathematical integers are equally close to f1/f2,
     *  then <i>n</i> is the integer that is even. If the remainder is
     *  zero, its sign is the same as the sign of the first
     *  argument. Special cases:
     *
     *  <ul>
     *  <li> If either argument is NaN, or the first argument is
     *  infinite, or the second argument is positive zero or negative
     *  zero, then the result is NaN.
     *  <li> If the first argument is finite
     *  and the second argument is infinite, then the result is the
     *  same as the first argument.
     *  </ul>
     */
    public static double remainder(double f1, double f2) {
        return Math.IEEEremainder(f1, f2);
    }

    /** Round to the nearest integer.  If the argument is NaN, then
     *  the return value is 0. If the argument is out of range, then
     *  the return value is Integer.MAX_VALUE or Integer.MIN_VALUE,
     *  depending on the sign of the argument.
     *  @param x The number to round.
     *  @return The nearest integer.
     */
    public static final int roundToInt(final double x) {
        long returnValue = Math.round(x);
        if (returnValue >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (returnValue <= Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int)returnValue;
    }

    /** If the argument is less than zero, return -1, otherwise
     *  return 1.
     */
    public static final int sgn(final double x) {
        if (x < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    /** Return the hyperbolic sine of the argument.
     */
    public static final double sinh(final double x) {
        return (Math.exp(x) - Math.exp(-x))/2;
    }

    /** Return the hyperbolic tangent of the argument.
     */
    public static final double tanh(final double x) {
        return (sinh(x)/cosh(x));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** sqrt(2) */
    public static final double SQRT_2 = Math.sqrt(2.0);

    /** 1 / sqrt(2) */
    public static final double ONE_OVER_SQRT_2 = 1.0 / SQRT_2;

    /** PI / 2 */
    public static final double PI_OVER_2 = Math.PI * 0.5;

    /** PI / 4 */
    public static final double PI_OVER_4 = Math.PI * 0.25;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static final double _ONEOVERLN2  = 1.0 / Math.log(2.0);
    private static final double _ONEOVERLN10 = 1.0 / Math.log(10.0);
}
