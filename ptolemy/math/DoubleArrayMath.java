/*
A library for mathematical operations on arrays of doubles.

This file was automatically generated with a preprocessor, so that
similar array operations are supported on ints, longs, floats, and doubles.

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
@AcceptedRating Yellow (ctsay@eecs.berkeley.edu)
*/

package ptolemy.math;


//////////////////////////////////////////////////////////////////////////
//// DoubleArrayMath
/**
This class provides a library for mathematical operations on double arrays.
unless explicitly noted otherwise, all array arguments are assumed to be
non-null. If a null array is passed to a method, a NullPointerException
will be thrown in the method or called methods.
@author Albert Chen, William Wu, Edward A. Lee, Jeff Tsay
@version $Id$
@since Ptolemy II 0.3
*/
public class DoubleArrayMath {

    // Protected constructor prevents construction of this class.
    protected DoubleArrayMath() {}

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new array that is the formed by adding z to each element
     *  of the input array.
     */
    public static final double[] add(final double[] array, final double z) {
        int length = array.length;
        double[] returnValue = new double[length];
        for (int i = 0; i < length; i++) {
            returnValue[i] = array[i] + z;
        }
        return returnValue;
    }

    /** Return a new array that is the element-by-element sum of the two
     *  input arrays.
     *  If the lengths of both arrays are 0, return a new array of length 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final double[] add(final double[] array1,
            final double[] array2) {
        int length = _commonLength(array1, array2, "DoubleArrayMath.add");
        double[] returnValue = new double[length];
        for (int i = 0; i < length; i++) {
            returnValue[i] = array1[i] + array2[i];
        }
        return returnValue;
    }

    /** Return a new array that is the result of appending array2 to the end
     *  of array1. This method simply calls
     *  append(array1, 0, array1.length, array2, 0, array2.length)
     */
    public static final double[] append(final double[] array1,
            final double[] array2) {
        return append(array1, 0, array1.length, array2, 0, array2.length);
    }

    /** Return a new array that is the result of appending length2
     *  elements of array2, starting from the array2[idx2] to length1
     *  elements of array1, starting from array1[idx1].  Appending
     *  empty arrays is supported. In that case, the corresponding idx
     *  may be any number. Allow System.arraycopy() to throw array
     *  access exceptions if idx .. idx + length - 1 are not all valid
     *  array indices, for both of the arrays.
     *
     *  @param array1 The first array of doubles.
     *  @param idx1 The starting index for array1.
     *  @param length1 The number of elements of array1 to use.
     *  @param array2 The second array of doubles, which is appended.
     *  @param idx2 The starting index for array2.
     *  @param length2 The number of elements of array2 to append.
     *  @return A new array of doubles.
     */
    public static final double[] append(final double[] array1,
            final int idx1, final int length1,
            final double[] array2, final int idx2, final int length2) {
        double[] returnValue = new double[length1 + length2];

        if (length1 > 0) {
            System.arraycopy(array1, idx1, returnValue, 0, length1);
        }

        if (length2 > 0) {
            System.arraycopy(array2, idx2, returnValue, length1, length2);
        }

        return returnValue;
    }

    /** Return a new array that is formed by applying an instance of a
     *  DoubleBinaryOperation to each element in the input array
     *  and z, using the array elements as the left operands and z
     *  as the right operand in all cases. (op.operate(array[i], z)).
     *  If the length of the array is 0, return a new array of length 0.
     */
    public static final double[] applyBinaryOperation(
            DoubleBinaryOperation op, final double[] array, final double z) {
        int length = array.length;
        double[] returnValue = new double[length];
        for (int i = 0; i < length; i++) {
            returnValue[i] = op.operate(array[i], z);
        }
        return returnValue;
    }

    /** Return a new array that is formed by applying an instance of a
     *  DoubleBinaryOperation to each element in the input array,
     *  using z as the left operand in all cases and the array elements
     *  as the right operands (op.operate(z, array[i])).
     *  If the length of the array is 0, return a new array of length 0.
     */
    public static final double[] applyBinaryOperation(
            DoubleBinaryOperation op, final double z, final double[] array) {
        int length = array.length;
        double[] returnValue = new double[length];
        for (int i = 0; i < length; i++) {
            returnValue[i] = op.operate(array[i], z);
        }
        return returnValue;
    }

    /** Return a new array that is formed by applying an instance of a
     *  DoubleBinaryOperation to the two arrays, element by element,
     *  using the elements of the first array as the left operands and the
     *  elements of the second array as the right operands.
     *  (op.operate(array[i], array2[i])).
     *  If the lengths of both arrays are 0, return a new array of length 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final double[] applyBinaryOperation(
            DoubleBinaryOperation op,
            final double[] array1, final double[] array2) {
        int length = _commonLength(array1, array2,
                "DoubleArrayMath.applyBinaryOperation");
        double[] returnValue = new double[length];
        for (int i = 0; i < length; i++) {
            returnValue[i] = op.operate(array1[i], array2[i]);
        }
        return returnValue;
    }

    /** Return a new array that is formed by applying an instance of a
     *  DoubleUnaryOperation to each element in the input array
     *  (op.operate(array[i])).
     *  If the length of the array is 0, return a new array of length 0.
     */
    public static final double[] applyUnaryOperation(
            final DoubleUnaryOperation op, final double[] array) {
        int length = array.length;
        double[] returnValue = new double[length];
        for (int i = 0; i < length; i++) {
            returnValue[i] = op.operate(array[i]);
        }
        return returnValue;
    }



    // no need for an element-by-element division, use divide(array,
    // 1.0 / z) instead


    /** Return a new array that is the element-by-element division of
     *  the first array by the second array (array1[i] / array2[i]).
     *  If the lengths of both arrays are 0, return a new array of length 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     *  @param array1 The first array of doubles.
     *  @param array2 The second array of doubles.
     *  @return A new array of doubles.
     */
    public static final double[] divideElements(final double[] array1,
            final double[] array2) {
        int length = _commonLength(array1, array2, "DoubleArrayMath.divideElements");
        double[] returnValue = new double[length];
        for (int i = 0; i < length; i++) {
            returnValue[i] = array1[i] / array2[i];
        }
        return returnValue;
    }

    /** Return a new array that is the element-by-element division of
     *  the first array by the given value.
     *  @param array The array of double numbers.
     *  @param num The double scalar.
     *  @return A new array of double numbers.
     */
    public static final double[] divide(double[] array, double num) {
        double[] returnValue = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            returnValue[i] = array[i]/num;
        }
        return returnValue;
    }

    /** Return the dot product of the two arrays.
     *  If the lengths of the array are both 0, return 0.0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final double dotProduct(final double[] array1,
            final double[] array2) {
        int length = _commonLength(array1, array2,
                "DoubleArrayMath.dotProduct");

        double sum = 0.0;

        for (int i = 0; i < length; i++) {
            sum += array1[i] * array2[i];
        }
        return sum;
    }

    /** Return the L2-norm of the array, that is, the square root of
     *  the sum of the squares of the elements.
     */
    public static final double l2norm(final double[] array) {
        return (double) Math.sqrt(sumOfSquares(array));
    }


    /** Return a new array that is a copy of the argument except that
     *  the elements are limited to lie within the specified range.
     *  If any value is infinite or NaN (not a number), then it is
     *  replaced by either the top or the bottom, depending on its
     *  sign.  To leave either the bottom or the top unconstrained,
     *  specify Double.NEGATIVE_INFINITY or Double.POSITIVE_INFINITY.
     *  <p>If the length of the array is 0, return a new array of length 0.
     *
     *  @param array An array of doubles.
     *  @param bottom The bottom limit.
     *  @param top The top limit.
     *  @return A new array with values in the range [bottom, top].
     */
    public static final double[] limit(final double[] array,
            final double bottom, final double top) {
        double[] returnValue = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            if ((array[i] > top) ||
                    (array[i] == Double.NaN) ||
                    (array[i] == Double.POSITIVE_INFINITY)) {


                returnValue[i] = top;
            } else if ((array[i] < bottom) ||
                    (array[i] == -Double.NaN) ||
                    (array[i] == Double.NEGATIVE_INFINITY)) {


                returnValue[i] = bottom;

            } else {
                returnValue[i] = array[i];
            }
        }
        return returnValue;
    }



    /** Return a new array that is the element-by-element multiplication of
     *  the two input arrays.
     *  If the lengths of both arrays are 0, return a new array of length 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final double[] multiply(final double[] array1,
            final double[] array2) {
        int length = _commonLength(array1, array2, "DoubleArrayMath.multiply");
        double[] returnValue = new double[length];
        for (int i = 0; i < length; i++) {
            returnValue[i] = array1[i] * array2[i];
        }
        return returnValue;
    }

    /** Return a new array that is constructed from the argument by
     *  multiplying each element in the array by the second argument, which is
     *  a double.
     *  If the sizes of the array is 0, return a new array of size 0.
     *  @param array An array of doubles.
     *  @param factor A double.
     *  @return A new array of doubles.
     */
    public static final double[] multiply(double[] array, double factor) {
        int length = array.length;
        double[] returnValue = new double[length];

        for (int i = 0; i < length; i++) {
            returnValue[i] = array[i]*factor;
        }

        return returnValue;
    }


    /** Return a new array that is the formed by the additive inverse of each
     *  element of the input array (-array[i]).
     */
    public static final double[] negative(final double[] array) {
        int length = array.length;
        double[] returnValue = new double[length];
        for (int i = 0; i < length; i++) {
            returnValue[i] = -array[i];
        }
        return returnValue;
    }

    /** Return a new array that is formed by scaling the array so that
     *  it has a L2-norm of 1.
     */
    public static final double[] normalize(final double[] array) {
        return scale(array, 1.0 / l2norm(array));
    }

    /** Return a new array of doubles that is formed by padding the
     *  middle of the array with 0's. If either the length of the
     *  input array is odd, the sample with index ceil(L/2) will be
     *  repeated in the output array, where L is the length of the input array.
     *  If the length of the input and output arrays are equal, return
     *  a copy of the input array.
     *  This method is useful for preparing data for an IFFT.
     *  @param array An array of doubles.
     *  @param newLength The desired length of the returned array.
     *  @return A new array of doubles.
     */
    public static final double[] padMiddle(final double[] array,
            final int newLength) {
        int length = array.length;

        int entriesNeeded = newLength - length;

        if (entriesNeeded < 0) {
            throw new IllegalArgumentException("ptolemy.math."
                    + "DoubleArrayMath.padMiddle() : newLength must be "
                    + ">= length of array.");
        } else if (entriesNeeded == 0) {
            return resize(array, newLength); // allocates a new array
        }

        double halfLength   = length * 0.5;
        int halfLengthFloor = (int) Math.floor(halfLength);
        int halfLengthCeil  = (int) Math.ceil(halfLength);
        double[] returnValue = new double[newLength];

        System.arraycopy(array, 0, returnValue, 0, halfLengthCeil);

        System.arraycopy(array,  halfLengthFloor, returnValue,
                newLength - halfLengthCeil, halfLengthCeil);


        return returnValue;
    }

    /** Return a new array of length newLength that is formed by
     *  either truncating or padding the input array.
     *  This method simply calls :
     *  resize(array, newLength, 0)
     *  @param array An array of doubles.
     *  @param newLength The desired length of the output array.
     *  @return A new array of doubles of length newLength.
     */
    public static final double[] resize(final double[] array,
            final int newLength) {
        return resize(array,  newLength, 0);
    }

    /** Return a new array of length newLength that is formed by
     *  either truncating or padding the input array.
     *  Elements from the input array are copied to the output array,
     *  starting from array[startIdx] until one of the following conditions
     *  is met :
     *  <ol>
     *  <li> The input array has no more elements to copy.
     *  <li> The output array has been completely filled.
     *  </ol>
     *  startIdx must index a valid entry in array unless the input array
     *  is of zero length or the output array is of zero length.
     *  If case 1) is met, the remainder of the output array is filled with
     *  zero's, implicitly by Java (padding).
     *  @param array An array of doubles.
     *  @param newLength The desired length of the output array.
     *  @param startIdx The starting index for the input array.
     *  @return A new array of doubles of length newLength.
     */
    public static final double[] resize(double[] array,
            final int newLength, final int startIdx) {

        double[] returnValue = new double[newLength];
        int copySize = Math.min(newLength, array.length - startIdx);
        if ((startIdx >= array.length) && (copySize > 0)) {
            throw new IllegalArgumentException(
                    "resize():  the start index '" + startIdx
                    + "' is greather than equal to the array length '"
                    + array.length + "' and the number of items to be copied '"
                    + copySize + "' is greater than zero.");
        }

        if (copySize > 0) {
            System.arraycopy(array, startIdx, returnValue, 0, copySize);
        }

        return returnValue;
    }

    /** Return a new array of doubles produced by scaling the input
     *  array elements by scaleFactor.
     *  If the length of the array is 0, return a new array of length 0.
     */
    public static final double[] scale(double[] array, double scaleFactor) {
        double[] returnValue = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            returnValue[i] = scaleFactor * array[i];
        }
        return returnValue;
    }


    /** Return a new array that is the element-by-element difference of the
     *  two input arrays, i.e. the first array minus the second array
     *  (array1[i] - array2[i]).
     *  If the lengths of both arrays are 0, return a new array of length 0.
     */
    public static final double[] subtract(final double[] array1,
            final double[] array2) {
        int length = _commonLength(array1, array2, "DoubleArrayMath.subtract");
        double[] returnValue = new double[length];

        for (int i = 0; i < length; i++) {
            returnValue[i] = array1[i] - array2[i];
        }
        return returnValue;
    }

    /** Return the sum of the elements in the array.
     *  Return 0.0 if the length of the array is 0.
     */
    public static final double sum(double[] array) {
        double sum = 0.0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum;
    }

    /** Return the sum of the squares of all of the elements in the array.
     *  This is equivalent to the square of the L2-norm of the array.
     *  Return 0.0 if the length of the array is 0.
     */
    public static final double sumOfSquares(double[] array) {
        double sum = 0.0;
        for (int i = 0; i < array.length; i++) {
            sum += (array[i] * array[i]);
        }
        return sum;
    }

    /** Return a new array that is formed by converting the doubles in
     *  the argument array to complex numbers. Each complex number has
     *  real part equal to the value in the argument matrix and a zero
     *  imaginary part.
     *
     *  @param array An array of double.
     *  @return A new array of complex numbers.
     */
    public static final Complex[] toComplexArray(final double[] array) {
        int length = array.length;
        Complex[] returnValue = new Complex[length];

        for (int i = 0; i < length; i++) {
            returnValue[i] = new Complex(array[i], 0.0);
        }
        return returnValue;

    }



    /** Return a new array that is formed by converting the doubles in
     *  the argument array to doubles.  If the length of the argument
     *  array is 0, return a new array of length 0.
     *  @param array An array of double.
     *  @return A new array of doubles.
     */
    public static final float[] toFloatArray(final double[] array) {
        int length = array.length;
        float[] returnValue = new float[length];

        for (int i = 0; i < length; i++) {
            returnValue[i] = (float) array[i];
        }
        return returnValue;
    }


    /** Return a new array that is formed by converting the doubles in
     *  the argument array to integers.  If the length of the argument
     *  array is 0, return a new array of length 0.
     *  @param array An array of double.
     *  @return A new array of integers.
     */
    public static final int[] toIntegerArray(final double[] array) {
        int length = array.length;
        int[] returnValue = new int[length];

        for (int i = 0; i < length; i++) {
            returnValue[i] = (int) array[i];
        }
        return returnValue;
    }


    /** Return a new array that is formed by converting the doubles in
     *  the argument array to longs.  If the length of the argument
     *  array is 0, return a new array of length 0.
     *
     *  @param array An array of double.
     *  @return A new array of longs.
     */
    public static final long[] toLongArray(final double[] array) {
        int length = array.length;
        long[] returnValue = new long[length];

        for (int i = 0; i < length; i++) {
            returnValue[i] = (long) array[i];
        }
        return returnValue;
    }


    /** Return a new String representing the array, formatted as
     *  in Java array initializers.
     */
    public static final String toString(final double[] array) {
        return toString(array, ", ", "{", "}");
    }

    /** Return a new String representing the array, formatted as
     *  specified by the ArrayStringFormat argument.
     *  To get a String in the Ptolemy expression language format,
     *  call this method with ArrayStringFormat.exprASFormat as the
     *  format argument.
     */
    public static final String toString(final double[] array,
            String elementDelimiter, String vectorBegin, String vectorEnd) {
        int length = array.length;
        StringBuffer sb = new StringBuffer();

        sb.append(vectorBegin);

        for (int i = 0; i < length; i++) {

            sb.append(Double.toString(array[i]));

            if (i < (length - 1)) {
                sb.append(elementDelimiter);
            }
        }

        sb.append(vectorEnd);

        return new String(sb);
    }

    /** Return true if all the distances between corresponding elements
     *  <i>array1</i> and <i>array2</i> are all less than or equal to
     *  the corresponding elements in <i>maxError</i>. If both arrays
     *  are empty, return true. If <i>maxError</i> is negative, return false.
     *
     *  @param array1 The first array.
     *  @param array2 The second array.
     *  @param maxError The threshold for the magnitude of the difference.
     *  @return True if all the distances between corresponding elements
     *  <i>array1</i> and <i>array2</i> are all less than or equal to
     *  the corresponding elements in <i>maxError</i>.
     *  @exception IllegalArgumentException If the arrays are not of the same
     *   length.
     */
    public static final boolean within(final double[] array1,
            final double[] array2, double maxError) {
        int length = _commonLength(array1, array2, "DoubleArrayMath.within");

        for (int i = 0; i < length; i++) {
            if (array1[i] > array2[i] + maxError ||
                    array1[i] < array2[i] - maxError) {
                return false;
            }
        }
        return true;
    }


    /** Return true if all the distances between corresponding elements
     *  <i>array1</i> and <i>array2</i> are all less than or equal to
     *  the corresponding elements in <i>maxError</i>. If both arrays
     *  are empty, return true. If any element of <i>maxError</i> is negative,
     *  return false.
     *
     *  @param array1 The first array.
     *  @param array2 The second array.
     *  @param maxError The array of thresholds for the magnitudes of
     *   the difference.
     *  @return True if all the distances between corresponding elements
     *  <i>array1</i> and <i>array2</i> are all less than or equal to
     *  the corresponding elements in <i>maxError</i>.
     *  @exception IllegalArgumentException If the arrays are not of the same
     *   length.
     */
    public static final boolean within(final double[] array1,
            final double[] array2, double[] maxError) {
        int length = _commonLength(array1, array2, "DoubleArrayMath.within");

        for (int i = 0; i < length; i++) {
            if (array1[i] > array2[i] + maxError[i] ||
                    array1[i] < array2[i] - maxError[i]) {
                return false;
            }
        }
        return true;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Throw an exception if the two arrays are not of the same length,
     *  or if either array is null. An exception is NOT thrown if both
     *  arrays are of length 0. If no exception is thrown, return the common
     *  length of the arrays.
     *  @param array1 The first array of doubles.
     *  @param array2 The second array of doubles.
     *  @param methodName A String representing the method name of the caller,
     *  without parentheses.
     *  @return The common length of both arrays.
     */
    protected static final int _commonLength(final double[] array1,
            final double[] array2,
            String methodName) {
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
                    " and the second array has length " +
                    array2.length + ".");
        }

        return array1.length;
    }
}
