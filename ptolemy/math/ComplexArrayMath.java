/* A library for mathematical operations on arrays of Complex's.

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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.math;

import java.lang.*;
import java.lang.Double;		/* Needed by javadoc */
import java.util.*;

public final class ComplexArrayMath {

    // Private constructor prevents construction of this class.
    private ComplexArrayMath() {}

    /** Return a new array of Complex's using two arrays for the real and
     *  imaginary parts.
     *  @param realPart An array of doubles, used for the real parts.
     *  @param imagPart An array of doubles, used for the imaginary parts.
     *  @retval A new array of Complex's.
     */
    public static final Complex[] formArray(double[] realPart, 
     double[] imagPart) {
       int size = Math.min(realPart.length, imagPart.length);

       Complex[] retval = new Complex[size];

       for (int i = 0; i < size; i++) {
           retval[i] = new Complex(realPart[i], imagPart[i]);
       }

       return retval;
    }

    /** Return a new array of Complex's with the values in realArray for
     *  the real parts, and 0 for the imaginary parts.
     *  @param realArray An array of doubles, used for the real parts.
     *  @retval A new array of Complex's.
     */ 
    public static final Complex[] realArrayToComplexArray(double[] realArray)
    {
       int size = realArray.length;

       Complex[] retval = new Complex[size];

       for (int i = 0; i < size; i++) {
           retval[i] = new Complex(realArray[i], 0);
       }
       return retval;
    }

    /** Return a new array of Complex's with the values in imagArray for
     *  the imaginary parts, and 0 for the real parts.
     *  @param imagArray An array of doubles, used for the imag parts.
     *  @retval A new array of Complex's.
     */  
    public static final Complex[] imagArrayToComplexArray(double[] imagArray) 
    { 
       int size = imagArray.length;

       Complex[] retval = new Complex[size];

       for (int i = 0; i < size; i++) {
           retval[i] = new Complex(0, imagArray[i]);
       }
       return retval;
    }

    /** Return a new array of doubles with the real parts of the array of
     *  Complex's.
     *  @param Complex[] An array of Complex's.
     *  @retval A new array of doubles.
     */
    public static final double[] realParts(Complex[] x) {
        int size = x.length;

        double[] retval = new double[size];

        for (int i = 0; i < size; i++) {
            retval[i] = x[i].real;
        }

        return retval;
    }

    /** Return a new array of doubles with the imaginary parts of the array of
     *  Complex's.
     *  @param Complex[] An array of Complex's.
     *  @retval A new array of doubles.
     */
    public static final double[] imagParts(Complex[] x) {
        int size = x.length;

        double[] retval = new double[size];

        for (int i = 0; i < size; i++) {
            retval[i] = x[i].imag;
        }

        return retval;
    }

    /** Return a new array that is constructed from the argument by
     *  adding the second argument to every element.
     *  @param array An array of complex numbers.
     *  @param z The complex number to add.
     *  @return A new array of complex numbers.
     */
    public static final Complex[] add(Complex[] array, Complex z) {
        Complex[] result = new Complex[array.length];
        for (int i = array.length-1; i >= 0; i--) {
            result[i] = array[i].add(z);
        }
        return result;
    }

    /** Return a new array that is the complex-conjugate of the argument.
     *  @param array An array of complex numbers.
     *  @return A new array of complex numbers.
     */
    public static final Complex[] conjugate(Complex[] array) {
        Complex[] result = new Complex[array.length];
        for (int i = array.length-1; i >= 0; i--) {
            result[i] = array[i].conjugate();
        }
        return result;
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
    public static final Complex[] convolve(Complex[] array1, Complex[] array2)
    {
        Complex[] result;
        int resultsize = array1.length+array2.length-1;
        if (resultsize < 0) {
            // If we attempt to convolve two zero length arrays, return
            // a zero length array.
            result = new Complex[0];
            return result;
        }

        double[] reals = new double[resultsize];
        double[] imags = new double[resultsize];
        for (int i = 0; i<array1.length; i++) {
            for (int j = 0; j<array2.length; j++) {
                reals[i+j] += array1[i].real*array2[j].real
                        - array1[i].imag*array2[j].imag;
                imags[i+j] += array1[i].imag*array2[j].real
                        + array1[i].real*array2[j].imag;
            }
        }

        result = new Complex[resultsize];
        for (int i = 0; i<result.length; i++) {
            result[i] = new Complex(reals[i], imags[i]);
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
     *  @deprecated
     */
    public static double[] convolve(double[] array1, double[] array2) 
    {
        double[] result;
        int resultsize = array1.length+array2.length-1;

        if (resultsize < 0) {
            // If we attempt to convolve two zero length arrays, return
            // a zero length array.
            result = new double[0];
            return result;
        }

        result = new double[resultsize];

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
     *  @see java.lang.Double
     */
    public static final double[] limit(double[] array, double bottom, 
     double top) {
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

    /** Return a new array containing the magnitudes of the elements
     *  of the specified array of Complex's.
     *  @param array An array of Complex's.
     *  @return An array of doubles.
     */
    public static final double[] mag(Complex[] array) {
        double[] mags = new double[array.length];
        for (int i = array.length-1; i >= 0; i--) {
            mags[i] = array[i].mag();
        }
        return mags;
    }

    /** Return a new array containing the angles of the elements of the
     *  specified complex array.
     *  @param array A array of Complex's.
     *  @return An array of angles in the range of <em>-pi</em> to <em>pi</em>.
     */
    public static final double[] phase(Complex[] array) {
        double[] angles = new double[array.length];
        for (int i = array.length-1; i >= 0; i--) {
            angles[i] = array[i].angle();
        }
        return angles;
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
     *  <em>a</em><sub>0</sub> = 1.
     *
     *  @param roots An array of roots of a polynomial.
     *  @return A new array representing a monic polynomial with the given
     *   roots.
     */
    public static final Complex[] polynomial(Complex[] roots) {
        if (roots.length <= 1) {
            Complex[] result = new Complex[1];
            result[0] = new Complex(1);
            return result;
        }
        Complex[] result = new Complex[2];
        result[0] = new Complex(1);

        if (roots.length >= 1) {
            result[1] = roots[0].negate();
            if (roots.length > 1) {
                for (int i = 1; i < roots.length; i++) {
                    Complex[] factor =
                    {new Complex(1), roots[i].negate()};
                    result = convolve(result, factor);
                }
            }
        }
        return result;
    }

    /** Return the product of the elements in the array.
     *  If there are no elements in the array, return a Complex number 
     *  with value zero.
     *  @param array An array of Complex's.
     *  @return A new complex number.
     */
    public static final Complex product(Complex[] array) {
        if (array.length == 0) return new Complex();
        double real = 1.0;
        double imag = 0.0;
        for (int i = 0; i < array.length; i++) {
            double tmp = real*array[i].real - imag*array[i].imag;
            imag = real*array[i].imag + imag*array[i].real;
            real = tmp;
        }
        return new Complex(real, imag);
    }

    /** Return true iff all the elements in the array are purely imaginary.
     *  @param array An array of Complex's
     *  @return A boolean.
     */
    public static final boolean pureImag(Complex[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].real != 0.0) {
               return false;
            }
        }
        return true;
    }

    /** Return true iff all the elements in the array are purely real.
     *  @param array An array of Complex's
     *  @return A boolean.
     */
    public static final boolean pureReal(Complex[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].imag != 0.0) {
               return false;
            }
        }
        return true;
    }

    /** Return a new array of length newLength that is formed by
     *  either truncating or padding the input array. 
     *  This method simply calls :
     *  resize(array, newLength, 0)
     *  @param array An array of Complex's.
     *  @param newLength The desired size of the output array.
     */
    public final static Complex[] resize(Complex[] array, int newLength) {
        return resize(array,  newLength, 0);
    }

    /** Return a new array of length newLength that is formed by
     *  either truncating or padding the input array. 
     *  Elements from the input array are copied to the output array,
     *  starting from array[startIdx] until one of the following conditions 
     *  is met :
     *  1) The input array has no more elements to copy.
     *  2) The output array has been completely filled.
     *  startIdx must index a valid entry in array unless the input array
     *  is of zero length or the output array is of zero length.
     *  If case 1) is met, the remainder of the output array is filled with
     *  new Complex's with value 0.
     *  Copying here means shallow copying, i.e. pointers to Complex objects
     *  are copied instead of allocation of new copies. This works because
     *  Complex objects are immutable.
     *  @param array An array of Complex's.
     *  @param newLength The desired size of the output array.
     *  @param startIdx The starting index for the input array.
     */
    public final static Complex[] resize(Complex[] array, int newLength, 
     int startIdx) {
        
        Complex[] retval = new Complex[newLength];
        int copySize = Math.min(newLength, array.length - startIdx);

        if ((startIdx >= array.length) && (copySize >= 0)) {
           throw new IllegalArgumentException(
            "resize() :  input array size is less than the start index");
        }
        
        if (copySize > 0) {
           System.arraycopy(array, startIdx, retval, 0, copySize);
        }
    
        for (int i = copySize; i < newLength; i++) {
            retval[i] = new Complex(0.0, 0.0);
        }
                         
        return retval;
    }  

                
    /** Return a new array that is constructed from the argument by
     *  scaling each element in the array by the second argument.
     *  @param array An array of Complex's.
     *  @param factor A double.
     *  @return A new array of Complex's.
     */
    public static final Complex[] scale(Complex[] array, double factor) {
        int len = array.length;
        Complex[] retval = new Complex[len];

        for (int i = 0; i < len; i++) {
            retval[i] = array[i].scale(factor);
        }

        return retval;
    }

    /** Return a new array that is constructed from the argument by
     *  subtracting the second argument from every element.
     *  @param array An array of Complex's.
     *  @param z The complex number to subtract.
     *  @return A new array of Complex's.
     */
    public static final Complex[] subtract(Complex[] array, Complex z) {
        Complex[] result = new Complex[array.length];
        for (int i = array.length-1; i >= 0; i--) {
            result[i] = array[i].subtract(z);
        }
        return result;
    }

    /** Return a new String in the format "{x[0], x[1], x[2], ... , x[n-1]}",
     *  where x[i] is the ith element of the array.
     *  @param array An array of Complex's.
     *  @return A new String representing the contents of the array.
     */
    public static String toString(Complex[] array) {
        int length = array.length;
        StringBuffer sb = new StringBuffer();
        sb.append('{');

        for (int i = 0; i < length; i++) {

            sb.append(array[i].toString());

            if (i < (length - 1)) {
               sb.append(',');
            }
        }

        sb.append('}');

        return new String(sb);
    }

    /** Return true iff all the absolute differences between corresponding 
     *  elements of array1 and array2, for both the real and imaginary parts, 
     *  are all less than or equal to maxError.
     *  @param array1 An array of doubles.
     *  @param array2 An array of doubles.
     *  @param maxError A double.
     *  @return A boolean.
     */
    public static final boolean within(Complex[] array1, Complex[] array2, 
     double maxError) {
        int length = array1.length;
        
        for (int i = 0; i < length; i++) {
            if ((Math.abs(array1[i].real - array2[i].real) > maxError) ||
                (Math.abs(array1[i].imag - array2[i].imag) > maxError)) {
               return false;
            }

        }
        return true;
    } 
}

