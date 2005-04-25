/*
  A library for mathematical operations on arrays of doubles.

  This file was automatically generated with a preprocessor, so that
  similar array operations are supported on ints, longs, floats, and doubles.

  Copyright (c) 2004-2005 The Regents of the University of California.
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


//////////////////////////////////////////////////////////////////////////
//// FractionArrayMath

/**
   This class provides a library for mathematical operations on Fraction arrays.
   unless explicitly noted otherwise, all array arguments are assumed to be
   non-null. If a null array is passed to a method, a NullPointerException
   will be thrown in the method or called methods.
   @author Adam Cataldo
   @Pt.ProposedRating Red (acataldo)
*/
public class FractionArrayMath {
    // Protected constructor prevents construction of this class.
    protected FractionArrayMath() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new array that is the formed by adding z to each element
     *  of the input array.
     */
    public static final Fraction[] add(Fraction[] array, final Fraction z) {
        int length = array.length;
        Fraction[] returnValue = new Fraction[length];

        for (int i = 0; i < length; i++) {
            returnValue[i] = array[i].add(z);
        }

        return returnValue;
    }

    /** Return a new array that is the element-by-element sum of the two
     *  input arrays.
     *  If the lengths of both arrays are 0, return a new array of length 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final Fraction[] add(final Fraction[] array1,
            final Fraction[] array2) {
        int length = _commonLength(array1, array2, "FractionArrayMath.add");
        Fraction[] returnValue = new Fraction[length];

        for (int i = 0; i < length; i++) {
            returnValue[i] = array1[i].add(array2[i]);
        }

        return returnValue;
    }

    /** Return a new array that is a copy of the array argument.
     *  @param array An array of Fractions.
     *  @return A new array of Fractions.
     */
    public static final Fraction[] allocCopy(final Fraction[] array) {
        int elements = array.length;
        Fraction[] newArray = new Fraction[elements];
        System.arraycopy(array, 0, newArray, 0, elements);
        return newArray;
    }

    /** Return a new array that is the result of appending array2 to the end
     *  of array1. This method simply calls
     *  append(array1, 0, array1.length, array2, 0, array2.length)
     */
    public static final Fraction[] append(final Fraction[] array1,
            final Fraction[] array2) {
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
     *  @param array1 The first array of Fractions.
     *  @param idx1 The starting index for array1.
     *  @param length1 The number of elements of array1 to use.
     *  @param array2 The second array of Fractions, which is appended.
     *  @param idx2 The starting index for array2.
     *  @param length2 The number of elements of array2 to append.
     *  @return A new array of doubles.
     */
    public static final Fraction[] append(final Fraction[] array1,
            final int idx1, final int length1, final Fraction[] array2,
            final int idx2, final int length2) {
        Fraction[] returnValue = new Fraction[length1 + length2];

        if (length1 > 0) {
            System.arraycopy(array1, idx1, returnValue, 0, length1);
        }

        if (length2 > 0) {
            System.arraycopy(array2, idx2, returnValue, length1, length2);
        }

        return returnValue;
    }

    /** Return a new array that is the element-by-element division of
     *  the first array by the second array.
     *  @param num The numerator array of Fractions.
     *  @param den The denominator array of Fractions.
     *  @return A new array of Fractions.
     */
    public static final Fraction[] divide(Fraction[] num, Fraction[] den) {
        int length = _commonLength(num, den, "divide");
        Fraction[] returnValue = new Fraction[length];

        for (int i = 0; i < length; i++) {
            returnValue[i] = num[i].divide(den[i]);
        }

        return returnValue;
    }

    /** Return the dot product of the two arrays.
     *  If the lengths of the array are both 0, return 0/1.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final Fraction dotProduct(final Fraction[] array1,
            final Fraction[] array2) {
        int length = _commonLength(array1, array2,
                "FractionArrayMath.dotProduct");

        Fraction sum = new Fraction(0, 1);

        for (int i = 0; i < length; i++) {
            sum = sum.add(array1[i].multiply(array2[i]));
        }

        return sum;
    }

    /** Returns true if the two input arrays have all elements
     *  equal.
     *
     * @param array1 The first input array.
     * @param array2 The second input array.
     * @return True if array1 == array2.
     */
    public static final boolean equals(final Fraction[] array1,
            final Fraction[] array2) {
        boolean output = true;

        if (array1.length != array2.length) {
            output = false;
        } else {
            for (int i = 0; i < array1.length; i++) {
                output = output && array1[i].equals(array2[i]);
            }
        }

        return output;
    }

    /** Return a new array that is the element-by-element multiplication of
     *  the two input arrays.
     *  If the lengths of both arrays are 0, return a new array of length 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final Fraction[] multiply(final Fraction[] array1,
            final Fraction[] array2) {
        int length = _commonLength(array1, array2, "FractionArrayMath.multiply");
        Fraction[] returnValue = new Fraction[length];

        for (int i = 0; i < length; i++) {
            returnValue[i] = array1[i].multiply(array2[i]);
        }

        return returnValue;
    }

    /** Return a new array that is constructed from the argument by
     *  multiplying each element in the array by the second argument, which is
     *  a Fraction.
     *  If the sizes of the array is 0, return a new array of size 0.
     *  @param array An array of Fractions.
     *  @param factor A Fraction.
     *  @return A new array of Fractions.
     */
    public static final Fraction[] multiply(Fraction[] array, Fraction factor) {
        int length = array.length;
        Fraction[] returnValue = new Fraction[length];

        for (int i = 0; i < length; i++) {
            returnValue[i] = array[i].multiply(factor);
        }

        return returnValue;
    }

    /** Return a new array that is the formed by the additive inverse of each
     *  element of the input array (-array[i]).
     */
    public static final Fraction[] negative(final Fraction[] array) {
        int length = array.length;
        Fraction[] returnValue = new Fraction[length];

        for (int i = 0; i < length; i++) {
            returnValue[i] = array[i].negate();
        }

        return returnValue;
    }

    /** Return a new array that is the element-by-element difference of the
     *  two input arrays, i.e. the first array minus the second array
     *  (array1[i] - array2[i]).
     *  If the lengths of both arrays are 0, return a new array of length 0.
     */
    public static final Fraction[] subtract(final Fraction[] array1,
            final Fraction[] array2) {
        int length = _commonLength(array1, array2, "FractionArrayMath.subtract");
        Fraction[] returnValue = new Fraction[length];

        for (int i = 0; i < length; i++) {
            returnValue[i] = array1[i].subtract(array2[i]);
        }

        return returnValue;
    }

    /** Return the sum of the elements in the array.
     *  Return 0/1 if the length of the array is 0.
     */
    public static final Fraction sum(Fraction[] array) {
        Fraction sum = new Fraction(0, 1);

        for (int i = 0; i < array.length; i++) {
            sum = sum.add(array[i]);
        }

        return sum;
    }

    /** Return a new array that is formed by converting the Fractions in
     *  the argument array to doubles.  If the length of the argument
     *  array is 0, return a new array of length 0.
     *  @param array An array of Fractions.
     *  @return A new array of doubles.
     */
    public static final double[] toDoubleArray(final Fraction[] array) {
        int length = array.length;
        double[] returnValue = new double[length];

        for (int i = 0; i < length; i++) {
            returnValue[i] = array[i].toDouble();
        }

        return returnValue;
    }

    /** Return a new String representing the array, formatted as
     *  in Java array initializers.
     */
    public static final String toString(final Fraction[] array) {
        return toString(array, ", ", "{", "}");
    }

    /** Return a new String representing the array, formatted as
     *  specified by the ArrayStringFormat argument.
     *  To get a String in the Ptolemy expression language format,
     *  call this method with ArrayStringFormat.exprASFormat as the
     *  format argument.
     */
    public static final String toString(final Fraction[] array,
            String elementDelimiter, String vectorBegin, String vectorEnd) {
        int length = array.length;
        StringBuffer sb = new StringBuffer();

        sb.append(vectorBegin);

        for (int i = 0; i < length; i++) {
            sb.append(array[i].toString());

            if (i < (length - 1)) {
                sb.append(elementDelimiter);
            }
        }

        sb.append(vectorEnd);

        return new String(sb);
    }

    /** Throw an exception if the two arrays are not of the same length,
     *  or if either array is null. An exception is NOT thrown if both
     *  arrays are of length 0. If no exception is thrown, return the common
     *  length of the arrays.
     *  @param array1 The first array of Fractions.
     *  @param array2 The second array of Fractions.
     *  @param methodName A String representing the method name of the caller,
     *  without parentheses.
     *  @return The common length of both arrays.
     */
    protected static final int _commonLength(final Fraction[] array1,
            final Fraction[] array2, String methodName) {
        if (array1 == null) {
            throw new IllegalArgumentException("ptolemy.math." + methodName
                    + "() : first input array is null.");
        }

        if (array2 == null) {
            throw new IllegalArgumentException("ptolemy.math." + methodName
                    + "() : second input array is null.");
        }

        if (array1.length != array2.length) {
            throw new IllegalArgumentException("ptolemy.math." + methodName
                    + "() : input arrays must have the same length, "
                    + "but the first array has length " + array1.length
                    + " and the second array has length " + array2.length + ".");
        }

        return array1.length;
    }
}
