/* A library for mathematical operations on arrays.

Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.math;

import java.lang.*;
import java.util.*;
import java.lang.reflect.*;


//////////////////////////////////////////////////////////////////////////
//// ArrayMath
/**
 * This class provides library for mathematical operations on Complex
 * and double arrays.
 * <p>
 * The suffix "R" on method names means "Replace."  Any method with
 * that suffix modifies the array argument rather than constructing a new
 * array.
 *
 * @author Albert Chen, William Wu, Edward A. Lee
 * @version $Id$
 */

public final class ArrayMath {

    // Private constructor prevents construction of this class.
    private ArrayMath() {}

    /////////////////////////////////////////////////////////////////////////
    ////                         Public methods                          ////

    /** Return a new array that is constructed from the argument by
     *  adding the second argument to every element.
     *  @param array An array of complex numbers.
     *  @param z The complex number to add.
     *  @return A new array of complex numbers.
     */
    public static Complex[] add(Complex[] array, Complex z) {
        Complex[] result = new Complex[array.length];
        for (int i = array.length-1; i >= 0; i--) {
            result[i] = Complex.add(array[i], z);
        }
        return result;
    }

    /** Modify the array argument by
     *  adding argument to every element.
     *  @param array An array of complex numbers.
     *  @param z The complex number to add
     */
    public static void addR(Complex[] array, Complex z) {
        for (int i = array.length-1; i >= 0; i--) {
            array[i].add(z);
        }
    }

    /** Return a new array that is the complex-conjugate of the argument.
     *  @param array An array of complex numbers.
     *  @return A new array of complex numbers.
     */
    public static Complex[] conjugate(Complex[] array) {
        Complex[] result = new Complex[array.length];
        for (int i = array.length-1; i >= 0; i--) {
            result[i] = Complex.conjugate(array[i]);
        }
        return result;
    }

    /** Modify the argument array by replacing each element with its
     *  complex conjugate.
     *  @param array An array of complex numbers.
     */
    public static void conjugateR(Complex[] array) {
        for (int i = array.length-1; i >= 0; i--) {
            array[i].conjugate();
        }
    }

    /** Return a new array that is the convolution of two complex arrays.
     *  The length of the new array is equal to the sum of the lengths of the
     *  two argument arrays minus one.  Note that some authors define
     *  complex convolution slightly differently as the convolution of the
     *  first array with the <em>conjugate</em> of the second.  If you need
     *  to use that definition, then conjugate the second array before
     *  calling this method. Convolution defined as we do here is the
     *  same as polynomial multiplication.  If the two argument arrays
     *  represent the coefficients of two polynomials, then the resulting
     *  array represents the coefficients of the product polynomial.
     *  @param array1 The first array.
     *  @param array2 The second array.
     *  @return A new array.
     */
    public static Complex[] convolve(Complex[] array1, Complex[] array2) {
        Complex[] result = new Complex[array1.length+array2.length-1];
        for (int i = 0; i<result.length; i++) {
            result[i] = new Complex();
        }
        for (int i = 0; i<array1.length; i++) {
            for (int j = 0; j<array2.length; j++) {
                Complex c = result[i+j];
                c.add(c.multiply(array1[i], array2[j]));
            }
        }
        return result;
    }

    /** Return a new array that is the convolution of the two argument arrays.
     *  The length of the new array is equal to the sum of the lengths of the
     *  two argument arrays minus one.  Note that convolution is the same
     *  as polynomial multiplication.  If the two argument arrays represent
     *  the coefficients of two polynomials, then the resulting array
     *  represents the coefficients of the product polynomial.
     *  @param array1 The first array.
     *  @param array2 The second array.
     *  @return A new array.
     */
    public static double[] convolve(double[] array1, double[] array2) {
        double[] result = new double[array1.length+array2.length-1];
        // The result is assumed initialized to zero (in the Java spec).
        for (int i = 0; i<array1.length; i++) {
            for (int j = 0; j<array2.length; j++) {
                result[i+j] += array1[i]*array2[j];
            }
        }
        return result;
    }

    /** Return a new array that is a copy of the argument except that the
     *  elements are limited to lie within the specified range.
     *  If any value is infinite or NaN (not a number),
     *  then it is replaced by either the top or the bottom, depending on
     *  its sign.  To leave either the bottom or the top unconstrained,
     *  specify Double.MIN_VALUE or Double.MAX_VALUE.
     *  @param array An array of numbers.
     *  @param bottom The bottom limit.
     *  @param top The top limit.
     *  @return A new array with values in the range [bottom, top].
     */
    public static double[] limit(double[] array, double bottom, double top) {
        double[] result = new double[array.length];
        for (int i = array.length-1; i >= 0; i--) {
            if (array[i] > top ||
                    array[i] == Double.NaN ||
                    array[i] == Double.POSITIVE_INFINITY) {
                result[i] = top;
            } else if (array[i] < bottom ||
                    array[i] == -Double.NaN ||
                    array[i] == Double.NEGATIVE_INFINITY) {
                result[i] = bottom;
            } else {
                result[i] = array[i];
            }
        }
        return result;
    }

    /** Modify the argument array by limiting its values to lie within
     *  the specified range.  If any value is infinite or NaN (not a number),
     *  then it is replaced by either the top or the bottom, depending on
     *  its sign.  To leave either the bottom or the top unconstrained,
     *  specify Double.MIN_VALUE or Double.MAX_VALUE.
     *  @param array An array of numbers.
     *  @param bottom The bottom limit.
     *  @param top The top limit.
     */
    public static void limitR(double[] array, double bottom, double top) {
        for (int i = array.length-1; i >= 0; i--) {
            if (array[i] > top ||
                    array[i] == Double.NaN ||
                    array[i] == Double.POSITIVE_INFINITY) {
                array[i] = top;
            }
            if (array[i] < bottom ||
                    array[i] == -Double.NaN ||
                    array[i] == Double.NEGATIVE_INFINITY) {
                array[i] = bottom;
            }
        }
    }

    /** Given the roots of a polynomial, return a polynomial that has
     *  has such roots.  If the roots are
     *  [<em>r</em><sub>0</sub>, ..., <em>r</em><sub>N-1</sub>],
     *  then the polynomial is given by
     *  [<em>a</em><sub>0</sub>, ..., <em>a</em><sub>N</sub>], where
     *  <p>
     *  <em>a</em><sub>0</sub> +
     *  <em>a</em><sub>1</sub><em>z</em><sup>-1</sup> + ... +
     *  <em>a</em><sub>N</sub><em>z</em><sup>-N</sup> =
     *  (1 - <em>r</em><sub>0</sub><em>z</em><sup>-1</sup>)
     *  (1 - <em>r</em><sub>1</sub><em>z</em><sup>-1</sup>) ...
     *  (1 - <em>r</em><sub>N-1</sub><em>z</em><sup>-1</sup>).
     *  <p>
     *  The returned polynomial will always be monic, meaning that
     *  <em>a</em><sub>0</sub> = 0.
     *
     *  @param roots An array of roots of a polynomial.
     *  @return A new array representing a monic polynomial with the given
     *   roots.
     */
    public static Complex[] polynomial(Complex[] roots) {
        if (roots.length <= 1) {
            Complex[] result = new Complex[1];
            result[0] = new Complex(1);
            return result;
        }
        Complex[] result = new Complex[2];
        result[0] = new Complex(1);

        if (roots.length >= 1) {
            result[1] = roots[0].negate(roots[0]);
            if (roots.length > 1) {
                for (int i = 1; i < roots.length; i++) {
                    Complex[] factor =
                    {new Complex(1), roots[i].negate(roots[i])};
                    result = convolve(result, factor);
                }
            }
        }
        return result;
    }

    /** Return the product of the elements in the array.
     *  @param array A complex array.
     *  @return A new complex number.
     */
    public static Complex product(Complex[] array) {
        Complex value = new Complex(1.0);
        for (int i = 0; i < array.length; i++) {
            value.multiply(array[i]);
        }
        return value;
    }

    /** Return a new array that is constructed from the argument by
     *  subtracting the second argument from every element.
     *  @param array An array of complex numbers.
     *  @param z The complex number to subtract.
     *  @return A new array of complex numbers.
     */
    public static Complex[] subtract(Complex[] array, Complex z) {
        Complex[] result = new Complex[array.length];
        for (int i = array.length-1; i >= 0; i--) {
            result[i] = Complex.subtract(array[i], z);
        }
        return result;
    }

    /** Modify the array argument by
     *  subtracting the second argument from every element.
     *  @param array An array of complex numbers.
     *  @param z The complex number to subtract.
     */
    public static void subtractR(Complex[] array, Complex z) {
        for (int i = array.length-1; i >= 0; i--) {
            array[i].subtract(z);
        }
    }
}
