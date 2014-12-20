/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2014 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
package org.ptolemy.machineLearning;

import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.math.DoubleMatrixMath;

/**
 * Algorithms class.
 *
 * @author ilgea
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Algorithms {

    /** Return the probability mass function P(x=k) ~ Poisson(mean)
     * value at k, for the Poisson distribution with parameter mean
     *  @param k The value at which the probability mass function will be computed
     *  @param mean The mean (lambda) of the poisson distribution
     *  @return The PMF value at k.
     * @exception IllegalActionException
     *  @exception Exception If k < 0
     */
    public static double poissonPmf(int k, double mean)
            throws IllegalActionException {
        // p(x=k) = (exp(-mean)*mean^k)/(k!)
        // log p  = -mean + k*log(mean) - log(k!)
        // compute the log and then take exponential, to avoid numerical errors
        if (k < 0) {
            throw new IllegalActionException(
                    "Poisson distribution is only defined over nonnegative integer values!");
        }
        double logFactorial = 0.0;
        for (int i = 1; i <= k; i++) {
            logFactorial += Math.log(i);
        }
        double logTerm = -mean + k * Math.log(mean) - logFactorial;
        return Math.exp(logTerm);
    }

    /** Return the probability mass function P(x=k) ~ Poisson(mean)
     * value at k, for the poisson distribution with parameter mean
     *  @param k The value at which the probability mass function will be computed
     *  @param mean The mean (lambda) of the poisson distribution
     *  @return The PMF value at k.
     * @exception IllegalActionException
     *  @exception Exception If k < 0
     */
    public static double poissonPmf(int k, double mean, double logFactorial)
            throws IllegalActionException {
        // p(x=k) = (exp(-mean)*mean^k)/(k!)
        // log p  = -mean + k*log(mean) - log(k!)
        // compute the log and then take exponential, to avoid numerical errors
        if (k < 0) {
            throw new IllegalActionException(
                    "Poisson distribution is only defined over nonnegative integer values!");
        }
        double logTerm = -mean + k * Math.log(mean) - logFactorial;
        return Math.exp(logTerm);
    }

    /**
     * Do a binary interval search for the key in array A. The bin index in which
     * key is found is returned.
     * @param A The search array
     * @param key Key to be searched
     * @return the found key index.
     */
    public static int _binaryIntervalSearch(double[] A, double key) {
        return _binaryIntervalSearch(A, key, 0, A.length - 1);
    }

    /**
     * Do a binary interval search for the key in array A. The bin index in which
     * key is found is returned.
     * @param A A The search array
     * @param key Key to be searched
     * @param imin minimum array index to look for key
     * @param imax maximum array index to look for key
     * @return the found key index, or value of KEY_NOT_FOUND for not found.
     */
    public static int _binaryIntervalSearch(double[] A, double key, int imin,
            int imax) {
        if (imax < imin) {
            return -1;
        } else {
            int imid = imin + ((imax - imin) / 2);
            if (imid >= A.length - 1) {
                return KEY_NOT_FOUND;
            } else if (A[imid] <= key && A[imid + 1] > key) {
                return imid;
            } else if (A[imid] > key) {
                return _binaryIntervalSearch(A, key, imin, imid - 1);
            } else if (A[imid] < key) {
                return _binaryIntervalSearch(A, key, imid + 1, imax);
            } else {
                return imid;
            }
        }
    }

    /**
     * Compute the Gaussian pdf value with the given mean and covariance parameters
     * at data point y
     * @param y observation point
     * @param mu mean array
     * @param sigma covariance matrix
     * @return value of the probability distribution 
     */
    public static double mvnpdf(double[] y, double[] mu, double[][] sigma) { 
        double[] xt = new double[y.length];
        Token[] xmat = new Token[y.length]; 
        for (int i = 0; i < y.length; i ++) {
            xt[i] = y[i] - mu[i];
            xmat[i] = new DoubleToken(xt[i]); 
        }
         
        int k = y.length;     
        try {
            MatrixToken X = MatrixToken.arrayToMatrix(xmat, y.length, 1); 
            DoubleMatrixToken inverseCovariance = new DoubleMatrixToken( DoubleMatrixMath.inverse(sigma));
            MatrixToken Xtranspose = MatrixToken.arrayToMatrix(xmat, 1, y.length); 
            Token exponent = Xtranspose.multiply(inverseCovariance);
            exponent = exponent.multiply(X); 
            double value = ((DoubleMatrixToken) exponent)
                    .getElementAt(0, 0);
            double result = 1.0 /Math.sqrt(Math.pow(2 * Math.PI,k) * DoubleMatrixMath.determinant(sigma))
                    * Math.exp(-0.5 * value); 

            return result;
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        } 
    }

    private static final int KEY_NOT_FOUND = -1;
}
