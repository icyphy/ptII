/* A library for mathematical operations on arrays of doubles.

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
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// DoubleArrayMath
/**
 * This class provides library for mathematical operations on double arrays.
 * Unless explicity noted otherwise, all array arguments are assumed to be 
 * non-null.
 * <p>
 * @author Albert Chen, William Wu, Edward A. Lee, Jeff Tsay
 * @version @(#)DoubleArrayMath.java	1.0   09/25/98
 */

public class DoubleArrayMath {

    // Protected constructor prevents construction of this class.
    protected DoubleArrayMath() {}

    /////////////////////////////////////////////////////////////////////////
    ////                         Public methods                          ////


    /** Return a new array that is the sum of the two input arrays.
     *  @param array1 The first array of doubles.
     *  @param array2 The second array of doubles.
     *  @retval A new array of doubles.
     */
    public final static double[] add(double[] array1, double[] array2) {
        double[] retval = new double[array1.length];
        for (int i = 0; i < array1.length; i++) {
            retval[i] = array1[i] + array2[i];
        }
        return retval;
    }

    /** Return a new array that is the result of appending array2 to the end 
     *  of array1. 
     *  @param array1 The first array of doubles.
     *  @param array2 The second array of doubles, which is appended.
     *  @retval A new array of doubles.
     */
    public static final double[] append(double[] array1, double[] array2) {
        return append(array1, 0, array1.length, array2, 0, array2.length);
    }

    /** Return a new array that is the result of appending width2 elements 
     *  of array2, starting from the array1[idx2] to width1 elements of array1,
     *  starting from array1[idx1].
     *  @param array1 The first array of doubles.
     *  @param idx1 The starting index for array1.
     *  @param length1 The number of elements of array1 to use.
     *  @param array2 The second array of doubles, which is appended.
     *  @param idx2 The starting index for array2.
     *  @param length2 The number of elements of array2 to append.
     *  @retval A new array of doubles.
     */
    public final static double[] append(double[] array1, int idx1, int length1,
     double[] array2, int idx2, int length2) {
        double[] retval = new double[length1 + length2];
       
        System.arraycopy(array1, idx1, retval, 0, length1);
        System.arraycopy(array2, idx2, retval, length1, length2);
      
        return retval;
    }

    public final static double[] truncate(double[] array, int newLength) {
        return truncate(array,  newLength, 0);
    }

    /** Return a new array that is formed by newLength elements of
     *  array1, starting from array[startIdx].
     *  @param array An array of doubles.
     *  @param newLength The number of elements to copy.
     *  @param startIdx The starting index for array.
     */
    public final static double[] truncate(double[] array, int newLength, 
     int startIdx) {
        double[] retval = new double[newLength];

        System.arraycopy(array, startIdx, retval, 0, newLength);
   
        return retval;
    } 
    

    /** Return a new array that is the difference of the two input arrays,
     *  i.e. the first array minus the second array.
     *  @param array1 The first array of doubles.
     *  @param array2 The second array of doubles.
     *  @retval A new array of doubles.
     */
    public final static double[] subtract(double[] array1, double[] array2) {
        double[] retval = new double[array1.length];
        for (int i = 0; i < array1.length; i++) {
            retval[i] = array1[i] - array2[i];
        }
        return retval;
    }

    /** Return a new array that is the element by element multiplication of
     *  the two input arrays.
     *  @param array1 The first array of doubles.
     *  @param array2 The second array of doubles.
     *  @retval A new array of doubles.
     */
    public final static double[] multiply(double[] array1, double[] array2) {
        double[] retval = new double[array1.length];
        for (int i = 0; i < array1.length; i++) {
            retval[i] = array1[i] * array2[i];
        }
        return retval;
    }

    /** Return a new array that is the element by element division of
     *  the first array by the second array.
     *  @param array1 The first array of doubles.
     *  @param array2 The second array of doubles.
     *  @retval A new array of doubles.
     */
    public final static double[] divide(double[] array1, double[] array2) {
        double[] retval = new double[array1.length];
        for (int i = 0; i < array1.length; i++) {
            retval[i] = array1[i] / array2[i];
        }
        return retval;
    }

    /** Return a new array of doubles produced by scaling the input
     *  array by a constant.
     *  @param array An array of doubles.
     *  @param scalefactor A double.
     *  @retval A new array of doubles.
     */
    public final static double[] scale(double[] array, double scalefactor) {
        double[] retval = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            retval[i] = scalefactor * array[i];
        }
        return retval;
    }

    /** Return the dot product of the two arrays.
     *  @param array1 The first array of doubles.
     *  @param array2 The first array of doubles.
     *  @retval A double.
     */
    public final static double dotProduct(double[] array1, double[] array2) {
        double sum = 0.0;

        int limit = Math.min(array1.length, array2.length);

        for (int i = 0; i < array1.length; i++) {
            sum += array1[i] * array2[i];
        }
        return sum;
    }


    /*  FIXME: This should be moved to SignalProcessing I think. */
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
    public final static double[] convolve(double[] array1, double[] array2) {
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
     *  @param array An array of doubles.
     *  @param bottom The bottom limit.
     *  @param top The top limit.
     *  @return A new array with values in the range [bottom, top].
     */
    public final static double[] limit(double[] array, double bottom, 
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

    /** Return a new String in the format "{x[0], x[1], x[2], ... , x[n-1]}",
     *  where x[i] is the ith element of the array.
     *  @param array An array of doubles.
     *  @return A new String representing the contents of the array.
     */
    public final static String toString(double[] array) {
        int length = array.length;
        StringBuffer sb = new StringBuffer();
        sb.append('{');

        for (int i = 0; i < length; i++) {

            sb.append(Double.toString(array[i]));

            if (i < (length - 1)) {
               sb.append(',');
            }
        }

        sb.append('}');

        return new String(sb);
    }

    /** Return true iff all the absolute differences between corresponding 
     *  elements of array1 and array2 are all less than or equal to maxError.
     *  @param array1 An array of doubles.
     *  @param array2 An array of doubles.
     *  @param maxError A double.
     *  @return A boolean.
     */
    public final static boolean within(double[] array1, double[] array2, 
     double maxError) {
        int length = array1.length;
        
        for (int i = 0; i < length; i++) {
            if (Math.abs(array1[i] - array2[i]) > maxError) {
               return false;
            }
        }
        return true;
    } 
}
