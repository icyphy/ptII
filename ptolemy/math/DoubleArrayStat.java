/*
A library of statistical operations on arrays of doubles.

Copyright (c) 1998-1999 The Regents of the University of California.
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
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.math;

import java.lang.*;
import java.util.*;

///////////////////////////////////////////////////////////////
//// DoubleArrayStat
/**
 * This class provides a library for statistical operations on arrays of
 * doubles.
 * Unless explicity noted otherwise, all array arguments are assumed to be
 * non-null. If a null array is passed to a method, a NullPointerException
 * will be thrown in the method or called methods.
 * <p>
 * @author Jeff Tsay
 * @version $Id$
 */

public class DoubleArrayStat extends DoubleArrayMath {

    // Protected constructor prevents construction of this class.
    protected DoubleArrayStat() {}

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the sum of all of the elements in the array.
     *  Return 0.0 of the length of the array is 0.
     */
    public static final double sumOfElements(double[] array) {
        double sum = 0.0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum;
    }

    /** Return the product of all of the elements in the array.
     *  Return 1.0 if the length of the array is 0.
     */
    public static final double productOfElements(double[] array) {
        double product = 1.0;
        for (int i = 0; i < array.length; i++) {
            product *= array[i];
        }
        return product;
    }

    /** Return the sum of the squares of all of the elements in the array.
     *  Return 0.0 if the length of the array is 0.
     */
    public static final double sumOfSquares(double[] array) {
        double sum = 0.0;
        for (int i = 0; i < array.length; i++) {
            sum += (array[i] * array[i]);
        }
        return sum;
    }

    /** Return the arithmetic mean of the elements in the array.
     */
    public static final double mean(double[] array) {
        _nonZeroLength(array, "DoubleArrayStat.mean");
        return sumOfElements(array) / (double) array.length;
    }

    /** Return the geometric mean of the elements in the array. This
     *  is defined to be the Nth root of the product of the elements
     *  in the array, where N is the length of the array.
     *  This method is only useful for arrays of non-negative numbers. If
     *  the product of the elements in the array is negative, throw an
     *  IllegalArgumentException. However, the individual elements of the array
     *  are not verified to be non-negative.
     *  Return 1.0 if the length of the array is 0.
     *  @param array An array of doubles.
     *  @return A double.
     */
    public static final double geometricMean(double[] array) {
        return Math.pow(productOfElements(array), 1.0 / array.length);
    }

    /** Return the maximum value in the array.
     *  Throw an exception if the length of the array is 0.
     */
    public static final double max(double[] array) {

        int length = _nonZeroLength(array, "DoubleArrayStat.max");

        double maxElement = array[0];

        for (int i = 1; i < length; i++) {
            maxElement = Math.max(array[i], maxElement);
        }
        return maxElement;
    }

    /** Return the minimum value in the array.
     *  Throw an exception if the length of the array is 0.
     */
    public static final double min(double[] array) {

        int length = _nonZeroLength(array, "DoubleArrayStat.min");

        double minElement = array[0];

        for (int i = 1; i < length; i++) {
            minElement = Math.min(array[i], minElement);
        }
        return minElement;
    }

    /** Return the variance of the elements in the array.
     *  Simply return variance(array, false).
     */
    public static double variance(double[] array) {
        return variance(array, false);
    }

    /** Return the variance of the elements in the array.
     *  The variance is computed as follows :
     *  <p>
     *  <pre>
     *  variance = (sum(X<sup>2</sup>) - (sum(X) / N)<sup>2</sup>) / N
     *  </pre>
     *  <p>
     *  The sample variance is computed as follows :
     *  <p>
     *  <pre>
     *  variance<sub>sample</sub> =
     *       (sum(X<sup>2</sup>) - (sum(X) / N)<sup>2</sup>) / (N - 1)
     *  </pre>
     *  <p>
     *
     *  Throw an exception if the array is of length 0, or if the
     *  sample variance is taken on an array of length less than 2.
     */
    public static double variance(double[] array, boolean sample) {

        int length = _nonZeroLength(array, "DoubleArrayStat.variance");

        if (sample && (array.length < 2)) {
            throw new IllegalArgumentException(
                    "ptolemy.math.DoubleArrayStat.variance() : " +
                    "sample variance and standard deviation of an array " +
                    "of length less than 2 are not defined.");
        }

        double ex2 = 0.0;
        double sum = 0.0;
        for (int i = 0; i < length; i++) {
            ex2 += array[i] * array[i];
            sum += array[i];
        }

        double norm = sample ? (length - 1) : length;
        double sumSquaredOverLength = sum * sum / length;
        return (ex2 - sumSquaredOverLength) / norm;
    }

    /** Return the standard deviation of the elements in the array.
     *  Simply return standardDeviation(array, false).
     */
    public static double standardDeviation(double[] array) {
        return Math.sqrt(variance(array, false));
    }

    /** Return the standard deviation of the elements in the array.
     *  The standard deviation is computed as follows :
     *  <p>
     *  <pre>
     *  stdDev = sqrt(variance)
     *  </pre>
     *  <p>
     *  The sample standard deviation is computed as follows
     *  <p>
     *  <pre>
     *  stdDev = sqrt(variance<sub>sample</sub>)
     *  </pre>
     *  <p>
     *  Throw an exception if the array is of length 0, or if the
     *  sample standard deviation is taken on an array of length less than 2.
     *  @param array An array of doubles.
     *  @param sample True if the sample standard deviation is desired.
     *  @return A double.
     */
    public static double standardDeviation(double[] array, boolean sample) {
        return Math.sqrt(variance(array, sample));
    }

    /** Return the cross-correlation of two arrays at a certain lag value,
     *  defined by :
     *  Rxy[d] = sum of i = 0 to N - 1 of x[i] * y[i + d]
     *  @param x The first array of doubles.
     *  @param y The second array of doubles.
     *  @param N An integer indicating the  number of samples to sum over.
     *  This must be non-negative, but large numbers are ok because this
     *  routine will not overrun reading of the arrays.
     *  @param lag An integer indicating the lag value (may be negative).
     *  @return A double, Rxy[lag].
     */
    public static double crossCorrelationAt(double[] x, double[] y,
            int N, int lag) {

        // Find the most efficient and correct place to start the summation
        int start = Math.max(0, -lag);

        // Find the most efficient and correct place to end the summation
        int limit = Math.min(x.length, N);

        limit = Math.min(limit, y.length - lag);

        double sum = 0.0;

        for (int i = start; i < limit; i++) {
            sum += x[i] * y[i + lag];
        }

        return sum;
    }

    /** Return the auto-correlation of an array at a certain lag value,
     *  summing over a certain number of samples
     *  defined by :
     *  Rxx[d] = sum of i = 0 to N - 1 of x[i] * x[i + d]
     *  N must be non-negative, but large numbers are ok because this
     *  routine will not overrun reading of the arrays.
     *  @param x An array of doubles.
     *  @param N An integer indicating the  number of samples to sum over.
     *  @param lag An integer indicating the lag value (may be negative).
     *  @return A double, Rxx[lag].
     */
    public static double autoCorrelationAt(double[] x, int N, int lag) {

        // Find the most efficient and correct place to start the summation
        int start = Math.max(0, -lag);

        // Find the most efficient and correct place to end the summation
        int limit = Math.min(x.length, N);

        limit = Math.min(limit, x.length - lag);

        double sum = 0.0;

        for (int i = start; i < limit; i++) {
            sum += x[i] * x[i + lag];
        }

        return sum;
    }

    /** Return a new array that is the cross-correlation of the two
     *  argument arrays, starting and ending at user-specified lag values.
     *  The output array will have length (endlag - startlag + 1).  The first
     *  element of the output will have the cross-correlation at a lag of
     *  startlag. The last element of the output will have the
     *  cross-correlation at a lag of endlag.
     *  @param x The first array of doubles.
     *  @param y The second array of doubles.
     *  @param startLag An int indicating at which lag to start (may be
     *  negative).
     *  @param endLag An int indicating at which lag to end.
     *  @return A new array of doubles.
     */
    public static final double[] crossCorrelation(double[] x, double[] y, int N,
            int startLag, int endLag) {
        int outputLength = endLag - startLag + 1;
        double[] retval = new double[outputLength];

        for (int lag = startLag; lag < endLag; lag++) {

            // Find the most efficient and correct place to start the summation
            int start = Math.max(0, -lag);

            // Find the most efficient and correct place to end the summation
            int limit = Math.min(x.length, N);

            limit = Math.min(limit, y.length - lag);

            double sum = 0.0;

            for (int i = start; i < limit; i++) {
                sum += x[i] * y[i + lag];
            }

            retval[lag - startLag] = sum;
        }

        return retval;
    }

    /** Return a new array that is the auto-correlation of the
     *  argument array, starting and ending at user-specified lag values.
     *  The output array will have length (endlag - startlag + 1).  The first
     *  element of the output will have the auto-correlation at a lag of
     *  startlag. The last element of the output will have the
     *  auto-correlation at a lag of endlag.
     *  @param x An array of doubles.
     *  @param startLag An int indicating at which lag to start (may be
     *  negative).
     *  @param endLag An int indicating at which lag to end.
     *  @return A new array of doubles.
     */
    public static final double[] autoCorrelation(double[] x, int N,
            int startLag, int endLag) {
        int outputLength = endLag - startLag + 1;
        double[] retval = new double[outputLength];

        for (int lag = startLag; lag < endLag; lag++) {

            // Find the most efficient and correct place to start the summation
            int start = Math.max(0, -lag);

            // Find the most efficient and correct place to end the summation
            int limit = Math.min(x.length, N);

            limit = Math.min(limit, x.length - lag);

            double sum = 0.0;

            for (int i = start; i < limit; i++) {
                sum += x[i] * x[i + lag];
            }

            retval[lag - startLag] = sum;
        }

        return retval;
    }

    /** Given an array of probabilities, treated as a probability mass
     *  function (pmf), calculate the entropy (in bits). The pmf is
     *  a discrete function that gives the probability of each element.
     *  The sum of the elements in the pmf should be 1, and each element
     *  should be between 0 and 1.
     *  This method does not check to see if the pmf is valid, except for
     *  checking that each entry is non-negative.
     *  The function computed is :
     *  <p>
     *   H(p) = - sum (p[x] * log<sup>2</sup>(p[x]))
     *  </p>
     *  The entropy is always non-negative.
     *  Throw an IllegalArgumentException if the length of the array is 0,
     *  or a negative probability is encountered.
     */
    public static final double entropy(double[] p) {

        int length = _nonZeroLength(p, "DoubleArrayStat.entropy");

        double h = 0.0;

        for (int i = 0; i < length; i++) {
            if (p[i] < 0.0) {
                throw new IllegalArgumentException(
                        "ptolemy.math.DoubleArrayStat.entropy() : " +
                        "Negative probability encountered.");
            } else if (p[i] == 0.0) {
                // do nothing
            } else {
                h -= p[i] * ExtendedMath.log2(p[i]);
            }
        }
        return h;
    }

    /** Given two array's of probabilities, calculate the relative entropy
     *  aka Kullback Leibler distance, D(p || q), (in bits) between the
     *  two probability mass functions. The result will be POSITIVE_INFINITY if
     *  q has a zero probability for a symbol for which p has a non-zero
     *  probability.
     *  The function computed is :
     *  <p>
     *   D(p||q) = - sum (p[x] * log<sup>2</sup>(p[x]/q[x]))
     *  </p>
     *  Throw an IllegalArgumentException if either array has length 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     *  @param p An array of doubles representing the first pmf, p.
     *  @param q An array of doubles representing the second pmf, q.
     *  @return A double representing the relative entropy of the
     *  random variable.
     */
    public static final double relativeEntropy(double[] p, double[] q) {

        _nonZeroLength(p, "DoubleArrayStat.relativeEntropy");
        int length = _commonLength(p, q, "DoubleArrayStat.relativeEntropy");

        double d = 0.0;

        for (int i = 0; i < length; i++) {
            if ((p[i] < 0.0) || (q[i] < 0.0)) {
                throw new IllegalArgumentException(
                        "ptolemy.math.DoubleArrayStat.relativeEntropy() : " +
                        "Negative probability encountered.");
            } else if (p[i] == 0.0) {
                // do nothing
            } else if (q[i] == 0.0) {
                return Double.POSITIVE_INFINITY;
            } else {
                d += p[i] * ExtendedMath.log2(p[i] / q[i]);
            }
        }
        return d;
    }

    /** Return a new array of uniformly distributed doubles ranging from
     *  a to b. The number of elements to allocate is given by N.
     *  @param a A double indicating the lower bound.
     *  @param b A double indicating the upper bound.
     *  @param N An int indicating how many elements to generate.
     *  @return A new array of doubles.
     */
    public static double[] randomUniform(double a, double b, int N) {
        double range = b - a;
        double[] retval = new double[N];
        Random r = new Random();
        for (int i = 0; i < N; i++) {
            retval[i] = r.nextDouble() * range + a;
        }
        return retval;
    }

    /** Return a new array of Gaussian distributed doubles with a given
     *  mean and standard deviation. The number of elements to allocate
     *  is given by N.
     *  This algorithm is from [1].
     */
    public static final double[] randomGaussian(double mean,
            double standardDeviation, int N) {
        double t;
        double x, v1, v2, r;
        Random random = new Random();
        double[] retval = new double[N];

        for (int i = 0; i < N; i += 2) {
            do {
                v1 = 2.0 * random.nextDouble() - 1.0;
                v2 = 2.0 * random.nextDouble() - 1.0;
                r = v1 * v1 + v2 * v2;
            } while ((r >= 1.0) && (r == 0.0));
            // prevent division by zero later in very rare cases

            r = Math.sqrt((-2.0 * Math.log(r)) / r);
            retval[i] = mean + v1 * r * standardDeviation;

            if ((i + 1) < N) {
                retval[i + 1] = mean + v2 * r * standardDeviation;
            }
        }

        return retval;
    }

    /** Return a new array of Bernoulli random variables with a given
     *  probability of success p. On success, the random variable has
     *  value 1.0; on failure the random variable has value 0.0.
     *  The number of elements to allocate is given by N.
     */
    public static final double[] randomBernoulli(double p, int N) {
        Random random = new Random();
        double[] retval = new double[N];

        for (int i = 0; i < N; i++) {
            retval[i] = (random.nextDouble() < p) ? 1.0 : 0.0;
        }
        return retval;
    }

    /** Return a new array of exponentially distributed doubles with parameter
     *  lambda. The number of elements to allocate is given by N.
     *  Note lambda may not be 0!
     */
    public static final double[] randomExponential(double lambda, int N) {
        Random random = new Random();
        double[] retval = new double[N];

        for (int i = 0; i < N; i++) {
            double r;
            do {
                r = random.nextDouble();
            } while (r != 0.0);

            retval[i] = -Math.log(r) / lambda;
        }
        return retval;
    }

    /** Return a new array of Poisson random variables (as doubles) with
     *  a given mean. The number of elements to allocate is given by N.
     *  This algorithm is from [1].
     */
    public static final double[] randomPoisson(double mean, int N) {
        Random random = new Random();
        double[] retval = new double[N];

        for (int i = 0; i < N; i++) {
            double j;
            double u, p, f;

            j = 0.0;
            f = p = Math.exp(-mean);
            u = random.nextDouble();

            while (f <= u) {
                p *= (mean / (j + 1.0));
                f += p;
                j += 1.0;
            }

            retval[i] = j;
        }
        return retval;
    }
}
