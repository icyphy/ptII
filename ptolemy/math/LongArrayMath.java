/*
A library for mathematical operations on arrays of longs.

This file was automatically generated with a preprocessor, so that 
similar array operations are supported on ints, longs, floats, and doubles. 

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
import java.lang.Long;              /* Needed by javadoc */

//////////////////////////////////////////////////////////////////////////
//// LongArrayMath
/**
 * This class provides a library for mathematical operations on long arrays.
 * Unless explicity noted otherwise, all array arguments are assumed to be
 * non-null. If a null array is passed to a method, a NullPointerException
 * will be thrown in the method or called methods.
 * <p>
 * This file was automatically generated with a preprocessor, so that 
 * similar matrix operations are supported on ints, longs, floats, and doubles. 
 * <p> 
 * 
 * @author Albert Chen, William Wu, Edward A. Lee, Jeff Tsay
 */

public class LongArrayMath {

    // Protected constructor prevents construction of this class.
    protected LongArrayMath() {}

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new array that is the element-by-element sum of the two
     *  input arrays.
     *  If the lengths of both arrays are 0, return a new array of length 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final long[] add(final long[] array1,
     final long[] array2) {
        int length = _commonLength(array1, array2, "LongArrayMath.add");
        long[] retval = new long[length];
        for (int i = 0; i < length; i++) {
            retval[i] = array1[i] + array2[i];
        }
        return retval;
    }

    /** Return a new array that is the element-by-element absolute value of the input array.
     *  If the length of the array is 0, return a new array of length 0.
     */
    public static final long[] abs(final long[] array) {
        long[] retval = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            retval[i] = Math.abs(array[i]);
        }
        return retval;
    }

    /** Return a new array that is the result of appending array2 to the end
     *  of array1. This method simply calls
     *  append(array1, 0, array1.length, array2, 0, array2.length)
     */
    public static final long[] append(final long[] array1,
     final long[] array2) {
        return append(array1, 0, array1.length, array2, 0, array2.length);
    }

    /** Return a new array that is the result of appending length2 elements
     *  of array2, starting from the array1[idx2] to length1 elements of array1,
     *  starting from array1[idx1].
     *  Appending empty arrays is supported. In that case, the corresponding
     *  idx may be any number. Allow System.arraycopy() to throw array access
     *  exceptions if idx .. idx + length - 1 are not all valid array indices,
     *  for both of the arrays.
     *  @param array1 The first array of longs.
     *  @param idx1 The starting index for array1.
     *  @param length1 The number of elements of array1 to use.
     *  @param array2 The second array of longs, which is appended.
     *  @param idx2 The starting index for array2.
     *  @param length2 The number of elements of array2 to append.
     *  @return A new array of longs.
     */
    public static final long[] append(final long[] array1,
     final int idx1, final int length1, final long[] array2, final int idx2,
     final int length2) {
        long[] retval = new long[length1 + length2];

        if (length1 > 0) {
            System.arraycopy(array1, idx1, retval, 0, length1);
        }

        if (length2 > 0) {
            System.arraycopy(array2, idx2, retval, length1, length2);
        }

        return retval;
    }

    /** Return a new array that is the element-by-element division of
     *  the first array by the second array.
     *  If the lengths of both arrays are 0, return a new array of length 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     *  @param array1 The first array of longs.
     *  @param array2 The second array of longs.
     *  @return A new array of longs.
     */
    public static final long[] divide(final long[] array1,
     final long[] array2) {
        int length = _commonLength(array1, array2, "LongArrayMath.divide");
        long[] retval = new long[length];
        for (int i = 0; i < length; i++) {
            retval[i] = array1[i] / array2[i];
        }
        return retval;
    }

    /** Return the dot product of the two arrays.
     *  If the lengths of the array are both 0, return 0L.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final long dotProduct(final long[] array1,
     final long[] array2) {
        int length = _commonLength(array1, array2,
                                   "LongArrayMath.dotProduct");

        long sum = 0L;

        for (int i = 0; i < length; i++) {
            sum += array1[i] * array2[i];
        }
        return sum;
    }

    /** Return a new array that is a copy of the argument except that the
     *  elements are limited to lie within the specified range.

     *  If any value is MAX_VALUE or MIN_VALUE,
     *  then it is replaced by either the top or the bottom, depending on
     *  its sign.  To leave either the bottom or the top unconstrained,
     *  specify Long.MIN_VALUE or Long.MAX_VALUE.

     *  If the length of the array is 0, return a new array of length 0.
     *  @param array An array of longs.
     *  @param bottom The bottom limit.
     *  @param top The top limit.
     *  @return A new array with values in the range [bottom, top].
     */
    public static final long[] limit(final long[] array,
     final long bottom, final long top) {
        long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            if ((array[i] > top) ||  

                (array[i] == Long.MAX_VALUE)) {

                result[i] = top;
            } else if ((array[i] < bottom) ||

                (array[i] == Long.MIN_VALUE)) {

                result[i] = bottom;
                
            } else {
                result[i] = array[i];
            }
        }
        return result;
    }

    /** Return a new array that is the element-by-element multiplication of
     *  the two input arrays.
     *  If the lengths of both arrays are 0, return a new array of length 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final long[] multiply(final long[] array1,
     final long[] array2) {
        int length = _commonLength(array1, array2, "LongArrayMath.multiply");
        long[] retval = new long[length];
        for (int i = 0; i < length; i++) {
            retval[i] = array1[i] * array2[i];
        }
        return retval;
    }

    /** Return a new array of longs that is formed by padding the
     *  middle of the array with 0's. If either the length of the
     *  input array is odd, the sample with index ceil(L/2) will be
     *  repeated in the output array, where L is the length of the input array.
     *  If the length of the input and output arrays are equal, return
     *  a copy of the input array.
     *  This method is useful for preparing data for an IFFT.
     *  @param array An array of longs.
     *  @param newLength The desired length of the returned array.
     *  @return A new array of longs.
     */
    public static final long[] padMiddle(final long[] array,
     final int newLength) {
        int length = array.length;

        int entriesNeeded = newLength - length;

        if (entriesNeeded < 0) {
           throw new IllegalArgumentException("ptolemy.math." +
            "LongArrayMath.padMiddle() : newLength must be >= length of " +
            "array.");
        } else if (entriesNeeded == 0) {
           return resize(array, newLength); // allocates a new array
        }

        double halfLength   = length * 0.5;
        int halfLengthFloor = (int) Math.floor(halfLength);
        int halfLengthCeil  = (int) Math.ceil(halfLength);
        long[] retval = new long[newLength];

        System.arraycopy(array, 0, retval, 0, halfLengthCeil);

        System.arraycopy(array,  halfLengthFloor, retval,
         newLength - halfLengthCeil, halfLengthCeil);


        return retval;
    }


    /** Return a new array of length newLength that is formed by
     *  either truncating or padding the input array.
     *  This method simply calls :
     *  resize(array, newLength, 0)
     *  @param array An array of longs.
     *  @param newLength The desired length of the output array.
     *  @return A new array of longs of length newLength.
     */
    public static final long[] resize(final long[] array,
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
     *  zero's, implicitly by Java (padding).
     *  @param array An array of longs.
     *  @param newLength The desired length of the output array.
     *  @param startIdx The starting index for the input array.
     *  @return A new array of longs of length newLength.
     */
    public static final long[] resize(long[] array,
     final int newLength, final int startIdx) {

        long[] retval = new long[newLength];
        int copySize = Math.min(newLength, array.length - startIdx);
        if ((startIdx >= array.length) && (copySize > 0)) {
            throw new IllegalArgumentException(
                    "ptolemy.math.LongArrayMath.resize() : " +
                    "input array size is less than the start index");
        }

        if (copySize > 0) {
            System.arraycopy(array, startIdx, retval, 0, copySize);
        }

        return retval;
    }

    /** Return a new array of longs produced by scaling the input
     *  array elements by scalefactor.
     *  If the length of the array is 0, return a new array of length 0.
     */
    public static final long[] scale(long[] array, long scalefactor) {
        long[] retval = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            retval[i] = scalefactor * array[i];
        }
        return retval;
    }

    /** Return a new array that is constructed from the argument by
     *  arithmetically shifting the elements in the array by the second argument.
     *  If the second argument is positive, the elements are shifted left by
     *  the second argument. If the second argument is negative, the elements
     *  are shifted right (arithmetically, with the >>> operator) by the absolute 
     *  value of the second argument. If the second argument is 0, no operation is 
     *  performed (the array is just copied).
     *  @param matrix A first array of longs.
     *  @param shiftAmount The amount to shift by, positive for left shift, 
     *  negative for right shift.
     *  @return A new array of longs.
     */
    public static final long[] shiftArithmetic(long[] array, int shiftAmount) {
        long[] result = new long[array.length];
        
        if (shiftAmount >= 0) {        
           for (int i = 0; i < array.length; i++) {
               result[i] = array[i] << shiftAmount;
           }
        } else if (shiftAmount < 0) {
           for (int i = 0; i < array.length; i++) {
               result[i] = array[i] >>> -shiftAmount;
           }
        }
        
        return result;
    }

    /** Return a new array that is constructed from the argument by
     *  logically shifting the elements in the array by the second argument.
     *  If the second argument is positive, the elements are shifted left by
     *  the second argument. If the second argument is negative, the elements
     *  are shifted right (logically, with the >>> operator) by the absolute 
     *  value of the second argument. If the second argument is 0, no operation is 
     *  performed (the array is just copied).
     *  @param matrix A first array of longs.
     *  @param shiftAmount The amount to shift by, positive for left shift, 
     *  negative for right shift.
     *  @return A new array of longs.
     */
    public static final long[] shiftLogical(long[] array, int shiftAmount) {
        long[] result = new long[array.length];
        
        if (shiftAmount >= 0) {        
           for (int i = 0; i < array.length; i++) {
               result[i] = array[i] << shiftAmount;
           }
        } else if (shiftAmount < 0) {
           for (int i = 0; i < array.length; i++) {
               result[i] = array[i] >> -shiftAmount;
           }
        }
        
        return result;
    }
    

    /** Return a new array that is the element-by-element difference of the
     *  two input arrays, i.e. the first array minus the second array.
     *  If the lengths of both arrays are 0, return a new array of length 0.
     *  @param array1 The first array of longs.
     *  @param array2 The second array of longs.
     *  @return A new array of longs.
     */
    public static final long[] subtract(final long[] array1,
     final long[] array2) {
        int length = _commonLength(array1, array2, "LongArrayMath.subtract");
        long[] retval = new long[length];

        for (int i = 0; i < length; i++) {
            retval[i] = array1[i] - array2[i];
        }
        return retval;
    }
    
    /** Return a new array that is formed by converting the longs in
     *  the argument array to doubles.
     *  If the length of the argument array is 0,
     *  return a new array of length 0.
     *  @param array An array of long.
     *  @return A new array of doubles.
     */
    public static final double[] toDoubleArray(final long[] array) {
        int length = array.length;
        double[] retval = new double[length];

        for (int i = 0; i < length; i++) {
            retval[i] = (double) array[i];
        }
        return retval;
    }
    

    /** Return a new array that is formed by converting the longs in
     *  the argument array to doubles.
     *  If the length of the argument array is 0,
     *  return a new array of length 0.
     *  @param array An array of long.
     *  @return A new array of doubles.
     */
    public static final float[] toFloatArray(final long[] array) {
        int length = array.length;
        float[] retval = new float[length];

        for (int i = 0; i < length; i++) {
            retval[i] = (float) array[i];
        }
        return retval;
    }
    


    /** Return a new array that is formed by converting the longs in
     *  the argument array to integers.
     *  If the length of the argument array is 0,
     *  return a new array of length 0.
     *  @param array An array of long.
     *  @return A new array of integers.
     */
    public static final int[] toIntegerArray(final long[] array) {
        int length = array.length;
        int[] retval = new int[length];

        for (int i = 0; i < length; i++) {
            retval[i] = (int) array[i];
        }
        return retval;
    }
    


    /** Return a new String representing the array, formatted as
     *  in Java array initializers.
     */
    public static final String toString(final long[] array) {
        return toString(array, ArrayStringFormat.javaASFormat);
    }

    /** Return a new String representing the array, formatted as
     *  specified by the ArrayStringFormat argument.
     *  To get a String in the Ptolemy expression language format,
     *  call this method with ArrayStringFormat.exprASFormat as the
     *  format argument.
     */
    public static final String toString(final long[] array,
            ArrayStringFormat format) {
        int length = array.length;
        StringBuffer sb = new StringBuffer();

        sb.append(format.vectorBeginString());

        for (int i = 0; i < length; i++) {

            sb.append(format.longString(array[i]));

            if (i < (length - 1)) {
                sb.append(format.elementDelimiterString());
            }
        }

        sb.append(format.vectorEndString());

        return new String(sb);
    }

    /** Return true if all the absolute differences between corresponding
     *  elements of array1 and array2 are all less than or equal to maxError.
     *  Otherwise return false.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final boolean within(final long[] array1,
     final long[] array2, long maxError) {
        int length = _commonLength(array1, array2, "LongArrayMath.within");

        for (int i = 0; i < length; i++) {
            if (Math.abs(array1[i] - array2[i]) > maxError) {
                return false;
            }
        }
        return true;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Throw an exception if the array is null or length 0.
     *  Otherwise return the length of the array.
     *  @param array An array of longs.
     *  @param methodName A String representing the method name of the caller,
     *  without parentheses.
     *  @return The length of the array.
     */
    public static final int _nonZeroLength(final long[] array,
            String methodName) {
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

    /** Throw an exception if the two arrays are not of the same length,
     *  or if either array is null. An exception is NOT thrown if both
     *  arrays are of length 0. If no exception is thrown, return the common
     *  length of the arrays.
     *  @param array The first array of longs.
     *  @param array The second array of longs.
     *  @param methodName A String representing the method name of the caller,
     *  without parentheses.
     *  @return The common length of both arrays.
     */
    public static final int _commonLength(final long[] array1,
     final long[] array2,
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
