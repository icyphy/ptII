/*
A library for mathematical operations on arrays of complex numbers.

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

@ProposedRating Yellow (ctsay@eecs.berkeley.edu)
@AcceptedRating Yellow (ctsay@eecs.berkeley.edu)
*/

package ptolemy.math;

import java.lang.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// ComplexArrayMath
/**
 * This class a provides a library for mathematical operations on arrays of
 * complex numbers, in particular arrays of instances of class
 * ptolemy.math.Complex.
 * Unless explicity noted otherwise, all array arguments are assumed to be
 * non-null. If a null array is passed to a method, a NullPointerException
 * will be thrown in the method or called methods.
 * <p>
 * @author Albert Chen, William Wu, Edward A. Lee, Jeff Tsay
 * @version $Id$
 */
public class ComplexArrayMath {

    // Protected constructor prevents construction of this class.
    protected ComplexArrayMath() {}

    /** Return a new array that is constructed from the argument by
     *  adding the complex number z to every element.
     *  @return A new array of complex numbers.
     */
    public static final Complex[] add(Complex[] array, Complex z) {
        Complex[] result = new Complex[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].add(z);
        }
        return result;
    }

    /** Return a new array that is the element-by-element sum of the two
     *  input arrays.
     *  If the sizes of both arrays are 0, return a new array of size 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final Complex[] add(Complex[] array1, Complex[] array2) {
        int length = _commonLength(array1, array2, "ComplexArrayMath.add");
        Complex[] retval = new Complex[length];
        for (int i = 0; i < length; i++) {
            retval[i] = array1[i].add(array2[i]);
        }
        return retval;
    }

    /** Return a new array that is formed by applying an instance of a 
     *  ComplexBinaryOperation to each element in the input array,     
     *  using z as the left operand in all cases and the array elements
     *  as the right operands (op.operate(z, array[i])).
     *  If the length of the array is 0, return a new array of length 0.          
     */
    public static final Complex[] applyBinaryOperation(
     ComplexBinaryOperation op, final Complex z, final Complex[] array) {
        int length = array.length;
        Complex[] retval = new Complex[length];
        for (int i = 0; i < length; i++) {
            retval[i] = op.operate(array[i], z);
        }
        return retval;
    }
            
    /** Return a new array that is formed by applying an instance of a 
     *  ComplexBinaryOperation to the two arrays, element by element,
     *  using the elements of the first array as the left operands and the 
     *  elements of the second array as the right operands.
     *  (op.operate(array[i], array2[i])).
     *  If the lengths of both arrays are 0, return a new array of length 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final Complex[] applyBinaryOperation(
     ComplexBinaryOperation op, final Complex[] array1, final Complex[] array2) {
        int length = _commonLength(array1, array2, 
         "ComplexArrayMath.applyBinaryOperation");     
        Complex[] retval = new Complex[length];
        for (int i = 0; i < length; i++) {
            retval[i] = op.operate(array1[i], array2[i]);
        }
        return retval;
    }

    /** Return a new array that is formed by applying an instance of a 
     *  ComplexUnaryOperation to each element in the input array 
     *  (op.operate(array[i])).
     *  If the length of the array is 0, return a new array of length 0.          
     */
    public static final Complex[] applyUnaryOperation(
     final ComplexUnaryOperation op, final Complex[] array) {
        int length = array.length;
        Complex[] retval = new Complex[length];
        for (int i = 0; i < length; i++) {
            retval[i] = op.operate(array[i]);
        }
        return retval;
    }

    /** Return a new array that is the result of appending array2 to the end
     *  of array1. This method simply calls
     *  append(array1, 0, array1.length, array2, 0, array2.length)
     */
    public static final Complex[] append(Complex [] array1, Complex[] array2) {
        return append(array1, 0, array1.length, array2, 0, array2.length);
    }

    /** Return a new array that is the result of appending length2 elements
     *  of array2, starting from the array1[idx2] to length1 elements of
     *  array1, starting from array1[idx1].
     *  Appending empty arrays is supported. In that case, the corresponding
     *  idx may be any number. Allow System.arraycopy() to throw array access
     *  exceptions if idx .. idx + length - 1 are not all valid array indices,
     *  for both of the arrays.
     *  @param array1 The first array of Complex.
     *  @param idx1 The starting index for array1.
     *  @param length1 The number of elements of array1 to use.
     *  @param array2 The second array of Complex, which is appended.
     *  @param idx2 The starting index for array2.
     *  @param length2 The number of elements of array2 to append.
     *  @return A new array of Complex.
     */
    public static final Complex[] append(Complex[] array1, int idx1,
            int length1, Complex[] array2, int idx2, int length2) {
        Complex[] retval = new Complex[length1 + length2];

        if (length1 > 0) {
            System.arraycopy(array1, idx1, retval, 0, length1);
        }

        if (length2 > 0) {
            System.arraycopy(array2, idx2, retval, length1, length2);
        }

        return retval;
    }

    /** Return true if all the magnitudes of the differences between
     *  corresponding elements of array1 and array2, are all less than or
     *  equal to maxMagnitudeDifference. Otherwise, return false.
     *  Throw an exception if the arrays are not of the same length. If both
     *  arrays are empty, return true.
     *  This is computationally more expensive than arePartsWithin().
     */
    public static final boolean areMagnitudesWithin(Complex[] array1,
            Complex[] array2, double maxMagnitudeDifference) {
        int length = _commonLength(array1, array2,
                "ComplexArrayMath.areMagnitudesWithin");

        for (int i = 0; i < length; i++) {
            if (array1[i].subtract(array2[i]).magnitude() >
                    maxMagnitudeDifference) {
                return false;
            }
        }
        return true;
    }

    /** Return true if all the absolute differences between corresponding
     *  elements of array1 and array2, for both the real and imaginary parts,
     *  are all less than or equal to maxError. Otherwise, return false.
     *  Throw an IllegalArgument exception if the arrays are not of the same
     *  length. If both arrays are empty, return true.
     *  This is computationally less expensive than isSquaredErrorWithin().
     */
    public static final boolean arePartsWithin(Complex[] array1,
            Complex[] array2, double maxError) {
        int length = _commonLength(array1, array2,
                "ComplexArrayMath.arePartsWithin");

        for (int i = 0; i < length; i++) {
            if ((Math.abs(array1[i].real - array2[i].real) > maxError) ||
                    (Math.abs(array1[i].imag - array2[i].imag) > maxError)) {
                return false;
            }

        }
        return true;
    }

    /** Return a new array of complex numbers that is formed by taking the
     *  complex-conjugate of each element in the argument array.
     *  If the argument has length 0, return a new array of complex numbers,
     *  with length 0.
     */
    public static final Complex[] conjugate(Complex[] array) {
        Complex[] result = new Complex[array.length];
        for (int i = array.length-1; i >= 0; i--) {
            result[i] = array[i].conjugate();
        }
        return result;
    }

    /** Return a new array that is the element-by-element division of
     *  the first array by the second array.
     *  If the sizes of both arrays are 0, return a new array of size 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     *  @param array1 The first array of complex numbers.
     *  @param array2 The second array of complex numbers.
     *  @return A new array of complex numbers.
     */
    public static final Complex[] divide(Complex[] array1, Complex[] array2) {
        int length = _commonLength(array1, array2, "ComplexArrayMath.divide");
        Complex[] retval = new Complex[length];
        for (int i = 0; i < length; i++) {
            retval[i] = array1[i].divide(array2[i]);
        }
        return retval;
    }
    
    /** Return a Complex number that is the dot product of the two argument
     *  arrays. The dot product is computed by the sum of the 
     *  element-by-element products of the first argument and the complex 
     *  conjugate of the second argument.
     *  If the sizes of both arrays is 0, return Complex.ZERO.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.     
     */
    public static final Complex dotProduct(final Complex[] array1, 
        final Complex[] array2) {
        int length = _commonLength(array1, array2, "ComplexArrayMath.dotProduct");
        Complex retval = Complex.ZERO;
    
        for (int i = 0; i < length; i++) {
            retval = retval.add(array1[i].multiply(array2[i].conjugate()));
        }
        return retval;
    }
        

    /** Return a new array of doubles with the imaginary parts of the array of
     *  complex numbers.
     */
    public static final double[] imagParts(final Complex[] x) {
        int size = x.length;

        double[] retval = new double[size];

        for (int i = 0; i < size; i++) {
            retval[i] = x[i].imag;
        }

        return retval;
    }

    /** Return a new array of Complex numbers using two arrays for the real and
     *  imaginary parts. If realPart is null, treated realPart as if it were an
     *  array of zeros, constructing an array of Complex numbers that are
     *  purely imaginary. If imagPart is null, treated imagPart as if it were an
     *  array of zeros, constructing an array of Complex numbers that are
     *  purely real.
     *  If both arrays are of length 0, or one array is null and the other
     *  array is of length 0, return a new array of complex numbers with length 0.
     *  If both arrays are null, allow a NullPointerException to be thrown by
     *  the array access code.
     *  @param realPart An array of doubles, used for the real parts.
     *  @param imagPart An array of doubles, used for the imaginary parts.
     *  @return A new array of complex numbers.
     */
    public static final Complex[] formComplexArray(final double[] realPart,
            final double[] imagPart) {
        Complex[] retval;    
        int size;
                  
        if ((realPart != null) && (imagPart != null)) {
           size = DoubleArrayMath._commonLength(realPart, imagPart,
                       "ComplexArrayMath.formComplexArray");
           retval = new Complex[size];

           for (int i = 0; i < size; i++) {
               retval[i] = new Complex(realPart[i], imagPart[i]);
           }                   
        } else if (realPart == null) {
           // NullPointerException will be thrown here if both arrays are null.
           size = imagPart.length; 
           
           retval = new Complex[size];
           
           for (int i = 0; i < size; i++) {
               retval[i] = new Complex(0.0, imagPart[i]);
           }                   
        } else { // imagPart == null
           size = realPart.length;
           
           retval = new Complex[size];

           for (int i = 0; i < size; i++) {
               retval[i] = new Complex(realPart[i], 0.0);
           }                   
        }

        return retval;
    }

    /** Return a new array of doubles containing the magnitudes of the elements
     *  of the specified array of complex numbers.
     */
    public static final double[] magnitude(Complex[] array) {
        double[] mags = new double[array.length];
        for (int i = array.length-1; i >= 0; i--) {
            mags[i] = array[i].magnitude();
        }
        return mags;
    }

    /** Return a new array that is the element-by-element multiplication of
     *  the two input arrays.
     *  If the sizes of both arrays are 0, return a new array of size 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final Complex[] multiply(Complex[] array1,
            Complex[] array2) {
        int length = _commonLength(array1, array2,
                "ComplexArrayMath.multiply");
        Complex[] retval = new Complex[length];
        for (int i = 0; i < length; i++) {
            retval[i] = array1[i].multiply(array2[i]);
        }
        return retval;
    }

    /** Return a new array that is constructed from the argument by
     *  multiplying each element in the array by the second argument, which is
     *  a complex number.
     *  If the sizes of the array is 0, return a new array of size 0.
     *  @param array An array of complex numbers.
     *  @param factor A Complex.
     *  @return A new array of complex numbers.
     */
    public static final Complex[] multiply(Complex[] array, Complex factor) {
        int length = array.length;
        Complex[] retval = new Complex[length];

        for (int i = 0; i < length; i++) {
            retval[i] = array[i].multiply(factor);
        }

        return retval;
    }

    /** Return a double that is the L2-norm of the array.If the
     *  length of the array is zero, return 0.0.
     */
    public static final double l2norm(Complex[] array) {
        return Math.sqrt(l2normSquared(array));
    }

    /** Return a double that is the sum of the squared magnitudes of the elements of the
     *  array. This is equal to the square of the L2-norm of the array. If the
     *  length of the array is zero, return 0.0.
     */
    public static final double l2normSquared(Complex[] array) {
        int length = array.length;
        
        if (length <= 0) return 0.0;
        
        double retval = 0.0;

        for (int i = 0; i < length; i++) {
            retval += array[i].magnitudeSquared();
        }

        return retval;
    }           

    /** Return a new array of Complex numbers that is formed by padding the
     *  middle of the array with 0's. If either the length of the
     *  input array is odd, the sample with index ceil(L/2) will be
     *  repeated in the output array, where L is the length of the input array.
     *  If the length of the input and output arrays are equal, return
     *  a copy of the input array.
     *  This method is useful for preparing data for an IFFT.
     *  @param array An array of complex numbers.
     *  @param newLength The desired length of the returned array.
     *  @return A new array of complex numbers.
     */
    public static final Complex[] padMiddle(final Complex[] array,
     final int newLength) {
        int length = array.length;

        int entriesNeeded = newLength - length;

        if (entriesNeeded < 0) {
           throw new IllegalArgumentException("ptolemy.math." +
            "ComplexArrayMath.padMiddle() : newLength must be >= length of " +
            "array.");
        } else if (entriesNeeded == 0) {
           return resize(array, newLength); // allocates a new array
        }

        double halfLength   = ((double) length) * 0.5;
        int halfLengthFloor = (int) Math.floor(halfLength);
        int halfLengthCeil  = (int) Math.ceil(halfLength);
        Complex[] retval = new Complex[newLength];

        System.arraycopy(array, 0, retval, 0, halfLengthCeil);

        System.arraycopy(array,  halfLengthFloor, retval,
         newLength - halfLengthCeil, halfLengthCeil);

        for (int i = halfLengthCeil; i < newLength - halfLengthCeil; i++) {
            retval[i] = Complex.ZERO;
        }

        return retval;
    }

    /** Return a new array containing the angles of the elements of the
     *  specified complex array.
     *  @param array A array of complex numbers.
     *  @return An array of angles in the range of
     *  <em>-pi</em> to <em>pi</em>.
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
    public static final Complex[] polynomial(final Complex[] roots) {
        if (roots.length <= 1) {
            return new Complex[] { Complex.ONE };
        }
        Complex[] result = new Complex[2];
        result[0] = Complex.ONE;

        if (roots.length >= 1) {
            result[1] = roots[0].negate();
            if (roots.length > 1) {
                for (int i = 1; i < roots.length; i++) {
                    Complex[] factor =
                    {new Complex(1), roots[i].negate()};
                    result = SignalProcessing.convolve(result, factor);
                }
            }
        }
        return result;
    }

    /** Return a new array of complex numbers that is formed by raising each
     *  element to the specified exponent, a double.
     *  If the size of the array is 0, return a new array of size 0.
     */
    public static final Complex[] pow(final Complex[] array, 
        final double exponent) {
        int length = array.length;
        Complex[] retval = new Complex[length];

        for (int i = 0; i < length; i++) {
            retval[i] = array[i].pow(exponent);
        }
        return retval;
    }

    /** Return the product of the elements in the array.
     *  If there are no elements in the array, return a Complex number
     *  with value zero.
     *  @param array An array of complex numbers.
     *  @return A new complex number.
     */
    public static final Complex product(final Complex[] array) {
        if (array.length == 0) return Complex.ZERO;
        double real = 1.0;
        double imag = 0.0;
        for (int i = 0; i < array.length; i++) {
            double tmp = real*array[i].real - imag*array[i].imag;
            imag = real*array[i].imag + imag*array[i].real;
            real = tmp;
        }
        return new Complex(real, imag);
    }

    /** Return a new array of doubles with the real parts of the array of
     *  complex numbers.
     */
    public static final double[] realParts(final Complex[] x) {
        int size = x.length;

        double[] retval = new double[size];

        for (int i = 0; i < size; i++) {
            retval[i] = x[i].real;
        }

        return retval;
    }

    /** Return a new array of length newLength that is formed by
     *  either truncating or padding the input array.
     *  This method simply calls :
     *  resize(array, newLength, 0)
     *  @param array An array of complex numbers.
     *  @param newLength The desired size of the output array.
     */
    public static final Complex[] resize(final Complex[] array, 
        final int newLength) {
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
     *  new complex numbers with value 0.
     *  Copying here means shallow copying, i.e. pointers to Complex objects
     *  are copied instead of allocation of new copies. This works because
     *  Complex objects are immutable.
     *  @param array An array of complex numbers.
     *  @param newLength The desired size of the output array.
     *  @param startIdx The starting index for the input array.
     */
    public static final Complex[] resize(final Complex[] array, 
            final int newLength, final int startIdx) {

        Complex[] retval = new Complex[newLength];
        int copySize = Math.min(newLength, array.length - startIdx);

        if ((startIdx >= array.length) && (copySize >= 0)) {
            throw new IllegalArgumentException(
                    "resize() :  input array size is less than " +
                    "the start index");
        }

        if (copySize > 0) {
            System.arraycopy(array, startIdx, retval, 0, copySize);
        }

        for (int i = copySize; i < newLength; i++) {
            retval[i] = Complex.ZERO;
        }

        return retval;
    }


    /** Return a new array that is constructed from the argument by
     *  scaling each element in the array by factor, which
     *  is a double. If the array argument is of length 0, return a new array
     *  of length 0.
     */
    public static final Complex[] scale(final Complex[] array, final double factor) {
        int len = array.length;
        Complex[] retval = new Complex[len];

        for (int i = 0; i < len; i++) {
            retval[i] = array[i].scale(factor);
        }

        return retval;
    }

    /** Return a new array that is constructed by subtracting the complex
     *  number z from every element in the first array. If the array argument
     *  is of length 0, return a new array of length 0.
     */
    public static final Complex[] subtract(final Complex[] array, final Complex z) {
        Complex[] result = new Complex[array.length];
        for (int i = array.length-1; i >= 0; i--) {
            result[i] = array[i].subtract(z);
        }
        return result;
    }

    /** Return a new array that is the element-by-element
     *  subtraction of the second array from the first array.
     *  If the sizes of both arrays are 0, return a new array of size 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     *  @param array1 An array of complex numbers from which to subtract.
     *  @param array2 An array of complex numbers to subtract.
     *  @return A new array of complex numbers.
     */
    public static final Complex[] subtract(Complex[] array1,
            final Complex[] array2) {
        int length = _commonLength(array1, array2,
                "ComplexArrayMath.subtract");
        Complex[] result = new Complex[length];
        for (int i = 0; i < length; i++) {
            result[i] = array1[i].subtract(array2[i]);
        }
        return result;
    }

    /** Return a new String representing the array, formatted as
     *  in Java array initializers.
     */
    public static final String toString(Complex[] array) {
        return toString(array, ArrayStringFormat.javaASFormat);
    }

    /** Return a new String representing the array, formatted as
     *  specified by the ArrayStringFormat argument.
     *  To get a String in the Ptolemy expression language format,
     *  call this method with ArrayStringFormat.exprASFormat as the
     *  format argument.
     */
    public static final String toString(final Complex[] array,
            ArrayStringFormat format) {
        int length = array.length;
        StringBuffer sb = new StringBuffer();

        sb.append(format.vectorBeginString());

        for (int i = 0; i < length; i++) {

            sb.append(format.complexString(array[i]));

            if (i < (length - 1)) {
                sb.append(format.elementDelimiterString());
            }
        }

        sb.append(format.vectorEndString());

        return sb.toString()
    }

    ///////////////////////////////////////////////////////////////////
    //    protected methods

    // Throw an exception if the array is null or length 0.
    // Otherwise return the length of the array.
    protected static final int _nonZeroLength(final Complex[] array,
            final String methodName) {
        if (array == null) {
            throw new IllegalArgumentException("ptolemy.math." + methodName +
                    "() : input array is null.");
        }

        if (array.length <= 0) {
            throw new IllegalArgumentException("ptolemy.math." + methodName +
                    "() : input array has length 0.");
        }

        return array.length;
    }

    // Throw an exception if the two arrays are not of the same length,
    // or if either array is null. An exception is NOT thrown if both
    // arrays are of size 0. If no exception is thrown, return the common
    // length of the arrays.
    protected static final int _commonLength(final Complex[] array1, 
            final Complex[] array2, final String methodName) {
        if (array1 == null) {
            throw new IllegalArgumentException("ptolemy.math." + methodName +
                    "() : first input array is null.");
        }

        if (array2 == null) {
            throw new IllegalArgumentException("ptolemy.math." + methodName +
                    "() : second input array is null.");
        }

        if (array1.length != array2.length) {
            throw new IllegalArgumentException("ptolemy.math." + methodName +
                    "() : input arrays must have the same length, " +
                    "but the first array has length " + array1.length +
                    " and the second array has length " + array2.length + '.');
        }

        return array1.length;
    }
}
