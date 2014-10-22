/* A library of signal processing operations.

 The algorithms for the FFT and DCT are based on the FFCT algorithm
 described in:

 Martin Vetterli and Henri J. Nussbaumer."Simple FFT and DCT Algorithms with
 Reduced Number of Operations". Signal Processing 6 (1984) 267-278.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

///////////////////////////////////////////////////////////////////
//// SignalProcessing

/**
 This class provides signal processing functions.

 The algorithms for the FFT and DCT are based on the FFCT algorithm
 described in:

 Martin Vetterli and Henri J. Nussbaumer."Simple FFT and DCT Algorithms with
 Reduced Number of Operations". Signal Processing 6 (1984) 267-278.

 @author Albert Chen, William Wu, Edward A. Lee, Jeff Tsay, Elaine Cheong
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (ctsay)
 @Pt.AcceptedRating Red (cxh)
 */
public class SignalProcessing {
    // The only constructor is private so that this class cannot
    // be instantiated.
    private SignalProcessing() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the first argument is close to the second (within
     *  EPSILON, where EPSILON is a static public variable of this class).
     */
    public static final boolean close(double first, double second) {
        double diff = first - second;
        return Math.abs(diff) < EPSILON;
    }

    /** Return a new array that is the convolution of the two argument arrays.
     *  The length of the new array is equal to the sum of the lengths of the
     *  two argument arrays minus one.  Note that convolution is the same
     *  as polynomial multiplication.  If the two argument arrays represent
     *  the coefficients of two polynomials, then the resulting array
     *  represents the coefficients of the product polynomial.
     *  @param array1 The first array.
     *  @param array2 The second array.
     *  @return A new array of doubles.
     */
    public static final double[] convolve(double[] array1, double[] array2) {
        double[] result;
        int resultSize = array1.length + array2.length - 1;

        if (resultSize < 0) {
            // If we attempt to convolve two zero length arrays, return
            // a zero length array.
            result = new double[0];
            return result;
        }

        result = new double[resultSize];

        // The result is assumed initialized to zero.
        for (int i = 0; i < array1.length; i++) {
            for (int j = 0; j < array2.length; j++) {
                result[i + j] += array1[i] * array2[j];
            }
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
     *  @return A new array of complex numbers.
     */
    public static final Complex[] convolve(Complex[] array1, Complex[] array2) {
        Complex[] result;
        int resultSize = array1.length + array2.length - 1;

        if (resultSize < 0) {
            // If we attempt to convolve two zero length arrays, return
            // a zero length array.
            result = new Complex[0];
            return result;
        }

        double[] reals = new double[resultSize];
        double[] imags = new double[resultSize];

        for (int i = 0; i < array1.length; i++) {
            for (int j = 0; j < array2.length; j++) {
                reals[i + j] += array1[i].real * array2[j].real
                        - array1[i].imag * array2[j].imag;
                imags[i + j] += array1[i].imag * array2[j].real
                        + array1[i].real * array2[j].imag;
            }
        }

        result = new Complex[resultSize];

        for (int i = 0; i < result.length; i++) {
            result[i] = new Complex(reals[i], imags[i]);
        }

        return result;
    }

    /** Return a new array of doubles that is the forward, normalized
     *  DCT of the input array of doubles.
     *  This method automatically computes the order of the transform
     *  based on the length of the input array. It is equivalent to
     *  DCT(x, order, DCT_TYPE_NORMALIZED), where 2^order is the smallest
     *  power of two greater than or equal to the length of the specified
     *  array.  The length of the result is 2^order.
     *  @param x An array of doubles.
     *  @return A new array of doubles, with length 2^order.
     */
    public static final double[] DCT(double[] x) {
        return DCT(x, order(x.length), DCT_TYPE_NORMALIZED);
    }

    /** Return a new array of doubles that is the forward, normalized
     *  DCT of the input array of doubles.
     *  This method is equivalent to
     *  DCT(x, order, DCT_TYPE_NORMALIZED).
     *  @param x An array of doubles.
     *  @param order Log base 2 of the size of the transform.
     *  @return A new array of doubles, with length 2^order.
     */
    public static final double[] DCT(double[] x, int order) {
        return DCT(x, order, DCT_TYPE_NORMALIZED);
    }

    /** Return a new array of doubles that is the forward DCT of the
     *  input array of doubles.
     *  See the DCT_TYPE_XXX constants for documentation of the
     *  exact formula, which depends on the type.
     *  @param x An array of doubles.
     *  @param order Log base 2 of the size of the transform.
     *  @param type The type of DCT, which is one of DCT_TYPE_NORMALIZED,
     *   DCT_TYPE_UNNORMALIZED, or DCT_TYPE_ORTHONORMAL.
     *  @see #DCT_TYPE_NORMALIZED
     *  @see #DCT_TYPE_UNNORMALIZED
     *  @see #DCT_TYPE_ORTHONORMAL
     *  @return A new array of doubles, with length 2^order.
     */
    public static final double[] DCT(double[] x, int order, int type) {
        _checkTransformArgs(x, order, _FORWARD_TRANSFORM);

        if (type >= DCT_TYPES) {
            throw new IllegalArgumentException(
                    "ptolemy.math.SignalProcessing.DCT(): Unrecognized DCT type");
        }

        int size = 1 << order;

        // Make sure the tables have enough entries for the DCT computation
        if (order > _FFCTGenLimit) {
            _FFCTTableGen(order);
        }

        double[] returnValue = _DCT(x, size, order);

        switch (type) {
        case DCT_TYPE_ORTHONORMAL:

            double factor = Math.sqrt(2.0 / size);
            returnValue = DoubleArrayMath.scale(returnValue, factor);

            // no break here
        case DCT_TYPE_NORMALIZED:
            returnValue[0] *= ExtendedMath.ONE_OVER_SQRT_2;
            break;
        }

        return returnValue;
    }

    /** Return the value of the argument
     *  in decibels, which is defined to be 20*log<sub>10</sub>(<em>z</em>),
     *  where <em>z</em> is the argument.
     *  Note that if the input represents power, which is proportional to a
     *  magnitude squared, then this should be divided
     *  by two to get 10*log<sub>10</sub>(<em>z</em>).
     *  @param value The value to convert to decibels.
     *  @deprecated Use toDecibels() instead.
     *  @see #toDecibels(double)
     */
    @Deprecated
    public static final double decibel(double value) {
        return toDecibels(value);
    }

    /** Return a new array the value of the argument array
     *  in decibels, using the previous decibel() method.
     *  You may wish to combine this with DoubleArrayMath.limit().
     *  @deprecated Use toDecibels() instead.
     */
    @Deprecated
    public static final double[] decibel(double[] values) {
        double[] result = new double[values.length];

        for (int i = values.length - 1; i >= 0; i--) {
            result[i] = toDecibels(values[i]);
        }

        return result;
    }

    /** Return a new array that is formed by taking every nth sample
     *  starting with the 0th sample, and discarding the rest.
     *  This method calls :
     *  downsample(x, n, 0)
     *  @param x An array of doubles.
     *  @param n An integer specifying the downsampling factor.
     *  @return A new array of doubles of length = floor(L / n), where
     *  L is the size of the input array.
     */
    public static final double[] downsample(double[] x, int n) {
        return downsample(x, n, 0);
    }

    /** Return a new array that is formed by taking every nth sample
     *  starting at startIndex, and discarding the samples in between.
     *  @param x An array of doubles.
     *  @param n An integer specifying the downsampling factor.
     *  @param startIndex An integer specifying the index of sample at
     *  which to start downsampling. This integer must be between 0 and
     *  L - 1, where L is the size of the input array.
     *  @return A new array of doubles of length =
     *  floor((L - startIndex) / n),
     *  where L is the size of the input array.
     */
    public static final double[] downsample(double[] x, int n, int startIndex) {
        if (x.length <= 0) {
            throw new IllegalArgumentException(
                    "ptolemy.math.SignalProcessing.downsample(): "
                            + "array length must be greater than 0.");
        }

        if (n <= 0) {
            throw new IllegalArgumentException(
                    "ptolemy.math.SignalProcessing.downsample(): "
                            + "downsampling factor must be greater than 0.");
        }

        if (startIndex < 0 || startIndex > x.length - 1) {
            throw new IllegalArgumentException(
                    "ptolemy.math.SignalProcessing.downsample(): "
                            + "startIndex must be between 0 and L - 1, where L is the "
                            + "size of the input array.");
        }

        int length = (x.length + 1 - startIndex) / n;
        double[] returnValue = new double[length];

        int destIndex;
        int srcIndex = startIndex;

        for (destIndex = 0; destIndex < length; destIndex++) {
            returnValue[destIndex] = x[srcIndex];
            srcIndex += n;
        }

        return returnValue;
    }

    /** Return a new array of complex numbers which is the FFT
     *  of an input array of complex numbers.  The order of the transform
     *  is the next power of two greater than the length of the argument.
     *  The input is zero-padded if it does not match this length.
     *  @param x An array of complex numbers.
     *  @return The FFT of the argument.
     */
    public static final Complex[] FFT(Complex[] x) {
        return FFTComplexOut(x, order(x.length));
    }

    /** Return a new array of complex numbers which is the FFT
     *  of an input array of complex numbers.
     *  The input is zero-padded if it does not match the length.
     *  @param x An array of complex numbers.
     *  @param order The log base 2 of the length of the FFT.
     *  @return The FFT of the argument.
     */
    public static final Complex[] FFT(Complex[] x, int order) {
        return FFTComplexOut(x, order);
    }

    /** Return a new array of Complex's which is the forward FFT
     *  of an input array of Complex's.
     *  This method automatically computes the order of the transform
     *  based on the length of the input array, and calls
     *  FFTComplexOut(x, order).
     *  @param x An array of Complex's.
     *  @return A new array of Complex's.
     */
    public static final Complex[] FFTComplexOut(Complex[] x) {
        return FFTComplexOut(x, order(x.length));
    }

    /** Return a new array of Complex's which is the forward FFT
     *  of an input array of Complex's.
     *  @param x An array of Complex's.
     *  @param order The base-2 logarithm of the size of the transform.
     *  @return A new array of Complex's.
     */
    public static final Complex[] FFTComplexOut(Complex[] x, int order) {
        x = _checkTransformArgs(x, order, _FORWARD_TRANSFORM);

        double[] realx = ComplexArrayMath.realParts(x);
        double[] realrealX = FFTRealOut(realx, order);
        double[] imagrealX = FFTImagOut(realx, order);

        double[] imagx = ComplexArrayMath.imagParts(x);
        double[] realimagX = FFTRealOut(imagx, order);
        double[] imagimagX = FFTImagOut(imagx, order);

        realrealX = DoubleArrayMath.subtract(realrealX, imagimagX);
        imagrealX = DoubleArrayMath.add(imagrealX, realimagX);

        return ComplexArrayMath.formComplexArray(realrealX, imagrealX);
    }

    /** Return a new array of Complex's which is the forward FFT
     *  of a real input array of doubles.
     *  This method automatically computes the order of the transform
     *  based on the length of the input array, and calls
     *  FFTComplexOut(x, order).
     *  @param x An array of doubles.
     *  @return A new array of Complex's.
     */
    public static final Complex[] FFTComplexOut(double[] x) {
        return FFTComplexOut(x, order(x.length));
    }

    /** Return a new array of Complex's which is the forward FFT
     *  of a real input array of doubles.
     *  This method is half as expensive as computing the FFT of a
     *  Complex array.
     *  @param x An array of doubles.
     *  @param order The base-2 logarithm of the size of the transform.
     *  @return A new array of Complex's.
     */
    public static final Complex[] FFTComplexOut(double[] x, int order) {
        // Argument checking is done inside FFTRealOut() and FFTImagOut()
        double[] realPart = FFTRealOut(x, order);
        double[] imagPart = FFTImagOut(x, order);

        return ComplexArrayMath.formComplexArray(realPart, imagPart);
    }

    /** Return a new array of doubles which is the imaginary part of the
     *  FFT of an input array of Complex's.
     *  This method automatically computes the order of the transform
     *  based on the length of the input array, and calls
     *  FFTImagOut(x, order).
     *  @param x An array of Complex's.
     *  @return A new array of doubles.
     */
    public static final double[] FFTImagOut(Complex[] x) {
        return FFTImagOut(x, order(x.length));
    }

    /** Return a new array of doubles which is the imaginary part of the
     *  FFT of an input array of Complex's.
     *  This method is half as expensive as computing both the real and
     *  imaginary parts of an FFT on a array of Complex's. It is especially
     *  useful when the output is known to be purely imaginary.
     *  @param x An array of Complex's.
     *  @param order The base-2 logarithm of the size of the transform.
     *  @return A new array of doubles.
     */
    public static final double[] FFTImagOut(Complex[] x, int order) {
        x = _checkTransformArgs(x, order, _FORWARD_TRANSFORM);

        double[] realx = ComplexArrayMath.realParts(x);
        double[] imagrealX = FFTImagOut(realx, order);

        double[] imagx = ComplexArrayMath.imagParts(x);
        double[] realimagX = FFTRealOut(imagx, order);

        return DoubleArrayMath.add(imagrealX, realimagX);
    }

    /** Return a new array of doubles that is the imaginary part of the FFT
     *  of the real input array of doubles.
     *  This method is half as expensive as computing both the real and
     *  imaginary parts of a FFT on a real array. It is especially useful when
     *  the output is known to be purely imaginary (input is odd).
     *  This method automatically computes the order of the transform
     *  based on the length of the input array, and calls:
     *  FFTImagOut(x, order)
     *  @param x An array of doubles.
     *  @return A new array of doubles.
     */
    public static final double[] FFTImagOut(double[] x) {
        return FFTImagOut(x, order(x.length));
    }

    /** Return a new array of doubles that is the imaginary part of the FFT
     *  of the real input array of doubles.
     *  This method is half as expensive as computing both the real and
     *  imaginary parts of a FFT on a real array. It is especially useful when
     *  the output is known to be purely imaginary (input is odd).
     *  @param x An array of doubles.
     *  @param order The base-2 logarithm of the size of the transform.
     *  @return A new array of doubles.
     */
    public static final double[] FFTImagOut(double[] x, int order) {
        x = _checkTransformArgs(x, order, _FORWARD_TRANSFORM);

        int size = 1 << order;
        int halfN = size >> 1;

        // Make sure the tables have enough entries for the DCT computation
        // at size N/4
        if (order - 2 > _FFCTGenLimit) {
            _FFCTTableGen(order - 2);
        }

        double[] imagPart = _sinDFT(x, size, order);

        double[] returnValue = new double[size];

        // Don't bother to look at the array for element 0
        // returnValue[0] = 0.0; // not necessary in Java
        for (int k = 1; k < halfN; k++) {
            returnValue[k] = -imagPart[k];
            returnValue[size - k] = imagPart[k];
        }

        // returnValue[halfN] = 0.0; // not necessary in Java
        return returnValue;
    }

    /** Return a new array of doubles which is the real part of the
     *  forward FFT of an input array of Complex's.
     *  This method automatically computes the order of the transform
     *  based on the length of the input array, and calls :
     *  FFTRealOut(x, order)
     *  @param x An array of Complex's.
     *  @return A new array of doubles.
     */
    public static final double[] FFTRealOut(Complex[] x) {
        return FFTRealOut(x, order(x.length));
    }

    /** Return a new array of doubles which is the real part of the
     *  forward FFT of an input array of Complex's.
     *  This method is half as expensive as computing both the real and
     *  imaginary parts of an FFT on a array of Complex's. It is especially
     *  useful when the output is known to be purely real.
     *  @param x An array of Complex's.
     *  @param order The base-2 logarithm of the size of the transform.
     *  @return A new array of doubles.
     */
    public static final double[] FFTRealOut(Complex[] x, int order) {
        x = _checkTransformArgs(x, order, _FORWARD_TRANSFORM);

        double[] realx = ComplexArrayMath.realParts(x);
        double[] realrealX = FFTRealOut(realx, order);

        double[] imagx = ComplexArrayMath.imagParts(x);
        double[] imagimagX = FFTImagOut(imagx, order);

        return DoubleArrayMath.subtract(realrealX, imagimagX);
    }

    /** Return a new array of doubles that is the real part of the FFT of
     *  the real input array of doubles.
     *  This method automatically computes the order of the transform
     *  based on the length of the input array, and calls :
     *  FFTRealOut(x, order).
     *  @param x An array of doubles.
     *  @return A new array of doubles.
     */
    public static final double[] FFTRealOut(double[] x) {
        return FFTRealOut(x, order(x.length));
    }

    /** Return a new array of doubles that is the real part of the FFT of
     *  the real input array of doubles.
     *  This method is half as expensive as computing both the real and
     *  imaginary parts of an FFT on a real array. It is especially useful
     *  when the output is known to be purely real (input is even).
     *  @param x An array of doubles.
     *  @param order The base-2 logarithm of the size of the transform
     *  @return A new array of doubles.
     */
    public static final double[] FFTRealOut(double[] x, int order) {
        x = _checkTransformArgs(x, order, _FORWARD_TRANSFORM);

        int size = 1 << order;
        int halfN = size >> 1;

        if (x.length < size) {
            x = DoubleArrayMath.resize(x, size);
        }

        // Make sure the tables have enough entries for the DCT computation
        // at size N/4
        if (order - 2 > _FFCTGenLimit) {
            _FFCTTableGen(order - 2);
        }

        double[] realPart = _cosDFT(x, size, order);
        double[] returnValue = new double[size];

        System.arraycopy(realPart, 0, returnValue, 0, halfN + 1);

        for (int k = halfN + 1; k < size; k++) {
            returnValue[k] = realPart[size - k];
        }

        return returnValue;
    }

    /** Return a new array of doubles that is the inverse, normalized
     *  DCT of the input array of doubles.
     *  This method automatically computes the order of the transform
     *  based on the length of the input array. It is equivalent to
     *  IDCT(x, order, DCT_TYPE_NORMALIZED), where 2^order is the
     *  next power of two larger than or equal to the length of the
     *  specified array.  The returned array has length 2^order.
     *  @param x An array of doubles.
     *  @return A new array of doubles with length 2^order.
     */
    public static final double[] IDCT(double[] x) {
        return IDCT(x, order(x.length), DCT_TYPE_NORMALIZED);
    }

    /** Return a new array of doubles that is the inverse, normalized
     *  DCT of the input array of doubles, using the specified order.
     *  The length of the DCT is 2^<i>order</i>.  This is equivalent to
     *  IDCT(x, order, DCT_TYPE_NORMALIZED).
     *  @param x An array of doubles.
     *  @return A new array of doubles with length 2^order.
     */
    public static final double[] IDCT(double[] x, int order) {
        return IDCT(x, order, DCT_TYPE_NORMALIZED);
    }

    /** Return a new array of doubles that is the inverse DCT of the
     *  input array of doubles.
     *  See the DCT_TYPE_XXX constants for documentation of the
     *  exact formula, which depends on the type.
     *  @param x An array of doubles.
     *  @param order The base-2 logarithm of the size of the transform.
     *  @param type The type of IDCT, which is one of DCT_TYPE_NORMALIZED,
     *   DCT_TYPE_UNNORMALIZED, or DCT_TYPE_ORTHONORMAL.
     *  @see #DCT_TYPE_NORMALIZED
     *  @see #DCT_TYPE_UNNORMALIZED
     *  @see #DCT_TYPE_ORTHONORMAL
     *  @return A new array of doubles with length 2^order.
     */
    public static final double[] IDCT(double[] x, int order, int type) {
        // check if order > 31
        if (type >= DCT_TYPES) {
            throw new IllegalArgumentException(
                    "ptolemy.math.SignalProcessing.IDCT() : Bad DCT type");
        }

        int size = 1 << order;
        int twoSize = 2 << order;

        // Generate scaleFactors if necessary
        if (_IDCTfactors[type][order] == null) {
            _IDCTfactors[type][order] = new Complex[twoSize];

            double oneOverTwoSize = 1.0 / twoSize;

            double factor = 1.0;
            double oneOverE0 = 2.0;

            switch (type) {
            case DCT_TYPE_NORMALIZED:
                factor = 2.0;
                oneOverE0 = ExtendedMath.SQRT_2;
                break;

            case DCT_TYPE_ORTHONORMAL:
                factor = Math.sqrt(twoSize); // == 2 * sqrt(N/2)
                oneOverE0 = ExtendedMath.SQRT_2;
                break;

            case DCT_TYPE_UNNORMALIZED:
                factor = 2.0;
                oneOverE0 = 1.0;
                break;
            }

            _IDCTfactors[type][order][0] = new Complex(oneOverE0 * factor, 0.0);

            for (int k = 1; k < twoSize; k++) {
                Complex c = new Complex(0, k * Math.PI * oneOverTwoSize);
                _IDCTfactors[type][order][k] = c.exp().scale(factor);
            }
        }

        Complex[] evenX = new Complex[twoSize];
        Complex[] myFactors = _IDCTfactors[type][order];

        // Convert to Complex, while multiplying by scaleFactors
        evenX[0] = myFactors[0].scale(x[0]);

        for (int k = 1; k < size; k++) {
            // Do zero-padding here
            if (k >= x.length) {
                evenX[k] = new Complex(0.0);
                evenX[twoSize - k] = new Complex(0.0);
            } else {
                evenX[k] = myFactors[k].scale(x[k]);
                evenX[twoSize - k] = myFactors[twoSize - k].scale(-x[k]);
            }
        }

        evenX[size] = new Complex(0.0, 0.0);

        double[] longOutput = IFFTRealOut(evenX, order + 1);

        // Truncate result
        return DoubleArrayMath.resize(longOutput, size);
    }

    /** Return a new array of complex numbers which is the inverse FFT
     *  of an input array of complex numbers.  The length of the result
     *  is the next power of two greater than the length of the argument.
     *  The input is zero-padded if it does not match this length.
     *  @param x An array of complex numbers.
     *  @return The inverse FFT of the argument.
     */
    public static final Complex[] IFFT(Complex[] x) {
        return IFFTComplexOut(x, order(x.length));
    }

    /** Return a new array of complex numbers which is the inverse FFT
     *  of an input array of complex numbers.
     *  The input is zero-padded if it does not match this length.
     *  @param x An array of complex numbers.
     *  @param order The log base 2 of the length of the FFT.
     *  @return The inverse FFT of the argument.
     */
    public static final Complex[] IFFT(Complex[] x, int order) {
        return IFFTComplexOut(x, order);
    }

    /** Return a new array of Complex's which is the inverse FFT
     *  of an input array of Complex's.
     *  This method automatically computes the order of the transform
     *  based on the length of the input array.
     *  @param x An array of Complex's.
     *  @return A new array of Complex's.
     */
    public static final Complex[] IFFTComplexOut(Complex[] x) {
        return IFFTComplexOut(x, order(x.length));
    }

    /** Return a new array of Complex's which is the forward FFT
     *  of an input array of Complex's.
     *  @param x An array of Complex's.
     *  @param order The base-2 logarithm of the size of the transform.
     *  @return A new array of Complex's.
     */
    public static final Complex[] IFFTComplexOut(Complex[] x, int order) {
        x = _checkTransformArgs(x, order, _INVERSE_TRANSFORM);

        Complex[] conjX = ComplexArrayMath.conjugate(x);
        Complex[] yConj = FFTComplexOut(conjX, order);
        Complex[] y = ComplexArrayMath.conjugate(yConj);

        // scale by 1/N
        double oneOverN = 1.0 / (1 << order);
        return ComplexArrayMath.scale(y, oneOverN);
    }

    /** Return a new array of doubles which is the real part of the inverse
     *  FFT of an input array of Complex's.
     *  This is less than half as expensive as computing both the real and
     *  imaginary parts. It is especially useful when it is known that the
     *  output is purely real.
     *  This method automatically computes the order of the transform
     *  based on the length of the input array, and calls :
     *  IFFTRealOut(x, order)
     *  @param x An array of Complex's.
     *  @return A new array of doubles.
     */
    public static final double[] IFFTRealOut(Complex[] x) {
        return IFFTRealOut(x, order(x.length));
    }

    /** Return a new array of doubles which is the real part of the inverse
     *  FFT of an input array of Complex's.
     *  This method is less than half as expensive as computing both the
     *  real and imaginary parts of an IFFT of an array of Complex's. It is
     *  especially useful when it is known that the output is purely real.
     *  @param x An array of Complex's.
     *  @return A new array of doubles.
     */
    public static final double[] IFFTRealOut(Complex[] x, int order) {
        x = _checkTransformArgs(x, order, _INVERSE_TRANSFORM);

        double[] realx = ComplexArrayMath.realParts(x);
        double[] realrealX = FFTRealOut(realx, order);

        double[] imagx = ComplexArrayMath.imagParts(x);
        double[] imagimagX = FFTImagOut(imagx, order);

        realrealX = DoubleArrayMath.add(realrealX, imagimagX);

        // scale by 1/N
        double oneOverN = 1.0 / (1 << order);
        return DoubleArrayMath.scale(realrealX, oneOverN);
    }

    /** Return a new array of doubles which is the real part of the inverse
     *  FFT of an input array of doubles.
     *  This method automatically computes the order of the transform
     *  based on the length of the input array, and calls :
     *  IFFTRealOut(x, order)
     *  @param x An array of doubles.
     *  @return A new array of doubles.
     */
    public static double[] IFFTRealOut(double[] x) {
        return IFFTRealOut(x, order(x.length));
    }

    /** Return a new array of doubles which is the real part of the inverse
     *  FFT of an input array of doubles. This method is less than half
     *  as expensive as computing the real part of an IFFT of an array of
     *  Complex's. It is especially useful when both the input and output
     *  are known to be purely real.
     *  @param x An array of doubles.
     *  @param order The base-2 logarithm of the size of the transform.
     *  @return A new array of doubles.
     */
    public static double[] IFFTRealOut(double[] x, int order) {
        double[] y = FFTRealOut(x, order);

        // scale by 1/N
        double oneOverN = 1.0 / (1 << order);
        return DoubleArrayMath.scale(y, oneOverN);
    }

    /** Return a new array that is filled with samples of a Bartlett
     *  window of a specified length. Throw an IllegalArgumentException
     *  if the length is less than 1 or the window type is unknown.
     *  @param length The length of the window to be generated.
     *  @return A new array of doubles.
     */
    public static final double[] generateBartlettWindow(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("ptolemy.math.SignalProcessing"
                    + ".generateBartlettWindow(): "
                    + " length of window should be greater than 0.");
        }

        int M = length - 1;
        int n;
        double[] window = new double[length];

        int halfM = M / 2;
        double twoOverM = 2.0 / M;

        for (n = 0; n <= halfM; n++) {
            window[n] = n * twoOverM;
        }

        for (n = halfM + 1; n < length; n++) {
            window[n] = 2.0 - n * twoOverM;
        }

        return window;
    }

    /** Return a new array that is filled with samples of a Blackman
     *  window of a specified length. Throw an IllegalArgumentException
     *  if the length is less than 1 or the window type is unknown.
     *  @param length The length of the window to be generated.
     *  @return A new array of doubles.
     */
    public static final double[] generateBlackmanWindow(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("ptolemy.math.SignalProcessing"
                    + ".generateBlackmanWindow(): "
                    + " length of window should be greater than 0.");
        }

        int M = length - 1;
        int n;
        double[] window = new double[length];

        double twoPiOverM = 2.0 * Math.PI / M;
        double fourPiOverM = 2.0 * twoPiOverM;

        for (n = 0; n < length; n++) {
            window[n] = 0.42 - 0.5 * Math.cos(twoPiOverM * n) + 0.08
                    * Math.cos(fourPiOverM * n);
        }

        return window;
    }

    /** Return a new array that is filled with samples of a Blackman Harris
     *  window of a specified length. Throw an IllegalArgumentException
     *  if the length is less than 1 or the window type is unknown.
     *  @param length The length of the window to be generated.
     *  @return A new array of doubles.
     */
    public static final double[] generateBlackmanHarrisWindow(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("ptolemy.math.SignalProcessing"
                    + ".generateBlackmanHarrisWindow(): "
                    + " length of window should be greater than 0.");
        }

        int M = length - 1;
        int n;
        double[] window = new double[length];

        double twoPiOverM = 2.0 * Math.PI / M;
        double fourPiOverM = 2.0 * twoPiOverM;
        double sixPiOverM = 3.0 * twoPiOverM;

        for (n = 0; n < length; n++) {
            window[n] = 0.35875 - 0.48829 * Math.cos(twoPiOverM * n) + 0.14128
                    * Math.cos(fourPiOverM * n) - 0.01168
                    * Math.cos(sixPiOverM * n);
        }

        return window;
    }

    /** Return an array with samples the Gaussian curve (the "bell curve").
     *  The returned array is symmetric.  E.g., to get a Gaussian curve
     *  that extends out to "four sigma," then the <i>extent</i> argument
     *  should be 4.0.
     *  @param standardDeviation The standard deviation.
     *  @param extent The multiple of the standard deviation out to
     *   which the curve is plotted.
     *  @param length The length of the returned array.
     *  @return An array that contains samples of the Gaussian curve.
     */
    public static final double[] generateGaussianCurve(
            double standardDeviation, double extent, int length) {
        GaussianSampleGenerator generator = new GaussianSampleGenerator(0.0,
                standardDeviation);
        return sampleWave(length, -extent * standardDeviation, 2.0 * extent
                * standardDeviation / length, generator);
    }

    /** Return a new array that is filled with samples of a Hamming
     *  window of a specified length. Throw an IllegalArgumentException
     *  if the length is less than 1 or the window type is unknown.
     *  @param length The length of the window to be generated.
     *  @return A new array of doubles.
     */
    public static final double[] generateHammingWindow(int length) {
        if (length < 1) {
            throw new IllegalArgumentException(
                    "ptolemy.math.SignalProcessing.generateHammingWindow(): "
                            + " length of window should be greater than 0.");
        }

        int M = length - 1;
        int n;
        double[] window = new double[length];

        double twoPiOverM = 2.0 * Math.PI / M;

        for (n = 0; n < length; n++) {
            window[n] = 0.54 - 0.46 * Math.cos(twoPiOverM * n);
        }

        return window;
    }

    /** Return a new array that is filled with samples of a Hanning
     *  window of a specified length. Throw an IllegalArgumentException
     *  if the length is less than 1 or the window type is unknown.
     *  @param length The length of the window to be generated.
     *  @return A new array of doubles.
     */
    public static final double[] generateHanningWindow(int length) {
        if (length < 1) {
            throw new IllegalArgumentException(
                    "ptolemy.math.SignalProcessing.generateHanningWindow(): "
                            + " length of window should be greater than 0.");
        }

        int M = length - 1;
        int n;
        double[] window = new double[length];

        double twoPiOverM = 2.0 * Math.PI / M;

        for (n = 0; n < length; n++) {
            window[n] = 0.5 - 0.5 * Math.cos(twoPiOverM * n);
        }

        return window;
    }

    /** Return an array with samples a polynomial curve.
     *  The first argument is an array giving the coefficients
     *  of the polynomial, starting with the constant term, followed
     *  by the linear term, followed by the quadratic term, etc.
     *  The remaining coefficients determine the points at which
     *  the polynomial curve is sampled. That is, they determine
     *  the values of the polynomial variable at which the polynomial
     *  is evaluated.
     *  @param polynomial An array with polynomial coefficients.
     *  @param start The point of the first sample.
     *  @param step The step size between samples.
     *  @param length The length of the returned array.
     *  @return An array that contains samples of a polynomial curve.
     */
    public static final double[] generatePolynomialCurve(double[] polynomial,
            double start, double step, int length) {
        PolynomialSampleGenerator generator = new PolynomialSampleGenerator(
                polynomial, 1);
        return sampleWave(length, start, step, generator);
    }

    /** Return an array containing a symmetric raised-cosine pulse.
     *  This pulse is widely used in communication systems, and is called
     *  a "raised cosine pulse" because the magnitude its Fourier transform
     *  has a shape that ranges from rectangular (if the excess bandwidth
     *  is zero) to a cosine curved that has been raised to be non-negative
     *  (for excess bandwidth of 1.0).  The elements of the returned array
     *  are samples of the function:
     *  <pre>
     *         sin(PI t/T)   cos(x PI t/T)
     *  h(t) = ----------- * -----------------
     *          PI t/T      1-(2 x t/T)<sup>2</sup>
     *  </pre>
     *  where x is the excess bandwidth and T is the number of samples
     *  from the center of the pulse to the first zero crossing.
     *  The samples are taken with a
     *  sampling interval of 1.0, and the returned array is symmetric.
     *  With an excessBandwidth of 0.0, this pulse is a sinc pulse.
     *  @param excessBandwidth The excess bandwidth.
     *  @param firstZeroCrossing The number of samples from the center of the
     *   pulse to the first zero crossing.
     *  @param length The length of the returned array.
     *  @return An array containing a symmetric raised-cosine pulse.
     */
    public static final double[] generateRaisedCosinePulse(
            double excessBandwidth, double firstZeroCrossing, int length) {
        RaisedCosineSampleGenerator generator = new RaisedCosineSampleGenerator(
                firstZeroCrossing, excessBandwidth);
        return sampleWave(length, -(length - 1) / 2.0, 1.0, generator);
    }

    /** Return a new array that is filled with samples of a rectangular
     *  window of a specified length. Throw an IllegalArgumentException
     *  if the length is less than 1 or the window type is unknown.
     *  @param length The length of the window to be generated.
     *  @return A new array of doubles.
     */
    public static final double[] generateRectangularWindow(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("ptolemy.math.SignalProcessing"
                    + ".generateRectangularWindow(): "
                    + " length of window should be greater than 0.");
        }

        int n;
        double[] window = new double[length];

        for (n = 0; n < length; n++) {
            window[n] = 1.0;
        }

        return window;
    }

    /** Return an array containing a symmetric raised-cosine pulse.
     *  This pulse is widely used in communication systems, and is called
     *  a "raised cosine pulse" because the magnitude its Fourier transform
     *  has a shape that ranges from rectangular (if the excess bandwidth
     *  is zero) to a cosine curved that has been raised to be non-negative
     *  (for excess bandwidth of 1.0).  The elements of the returned array
     *  are samples of the function:
     *  <pre>
     *           4 x(cos((1+x)PI t/T) + T sin((1-x)PI t/T)/(4x t/T))
     *  h(t) =  ---------------------------------------------------
     *                PI sqrt(T)(1-(4 x t/T)<sup>2</sup>)
     *  </pre>
     *  <p>
     *  where <i>x</i> is the the excess bandwidth.
     *  This pulse convolved with itself will, in principle, be equal
     *  to a raised cosine pulse.  However, because the pulse decays rather
     *  slowly for low excess bandwidth, this ideal is not
     *  closely approximated by short finite approximations of the pulse.
     *  The samples are taken with a
     *  sampling interval of 1.0, and the returned array is symmetric.
     *  With an excessBandwidth of 0.0, this pulse is a scaled sinc pulse.
     *  @param excessBandwidth The excess bandwidth.
     *  @param firstZeroCrossing The number of samples from the center of the
     *   pulse to the first zero crossing.
     *  @param length The length of the returned array.
     *  @return A new array containing a square-root raised-cosine pulse.
     */
    public static final double[] generateSqrtRaisedCosinePulse(
            double excessBandwidth, double firstZeroCrossing, int length) {
        RaisedCosineSampleGenerator generator = new RaisedCosineSampleGenerator(
                firstZeroCrossing, excessBandwidth);
        return sampleWave(length, -(length - 1) / 2.0, 1.0, generator);
    }

    /** Return a new array that is filled with samples of a window of a
     *  specified length and type. Throw an IllegalArgumentException
     *  if the length is less than 1 or the window type is unknown.
     *  @param length The length of the window to be generated.
     *  @param windowType The type of window to generate.
     *  @return A new array of doubles.
     */
    public static final double[] generateWindow(int length, int windowType) {
        if (length < 1) {
            throw new IllegalArgumentException(
                    "ptolemy.math.SignalProcessing.generateWindow(): "
                            + " length of window should be greater than 0.");
        }

        switch (windowType) {
        case WINDOW_TYPE_RECTANGULAR:
            return generateRectangularWindow(length);

        case WINDOW_TYPE_BARTLETT:
            return generateBartlettWindow(length);

        case WINDOW_TYPE_HANNING:
            return generateHanningWindow(length);

        case WINDOW_TYPE_HAMMING:
            return generateHammingWindow(length);

        case WINDOW_TYPE_BLACKMAN:
            return generateBlackmanWindow(length);

        case WINDOW_TYPE_BLACKMAN_HARRIS:
            return generateBlackmanHarrisWindow(length);

        default:
            throw new IllegalArgumentException(
                    "ptolemy.math.SignalProcessing.generateWindow(): "
                            + "Unknown window type (" + windowType + ").");
        }
    }

    /** Return the next power of two larger than the argument.
     *  @param x A positive real number.
     *  @exception IllegalArgumentException If the argument is less than
     *   or equal to zero.
     */
    public static final int nextPowerOfTwo(double x) {
        if (x <= 0.0) {
            throw new IllegalArgumentException(
                    "ptolemy.math.SignalProcessing.nextPowerOfTwo(): "
                            + "argument (" + x + ") is not a positive number.");
        }

        double m = Math.log(x) * _LOG2SCALE;
        int exp = (int) Math.ceil(m);
        return 1 << exp;
    }

    /** Return the "order" of a transform size, i.e. the base-2 logarithm
     *  of the size. The order will be rounded up to the nearest integer.
     *  If the size is zero or negative, throw an IllegalArgumentException.
     *  @param size The size of the transform.
     *  @return The order of the transform.
     */
    public static final int order(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("ptolemy.math.SignalProcessing:"
                    + " size of transform must be positive.");
        }

        double m = Math.log(size) * _LOG2SCALE;
        double exp = Math.ceil(m);
        return (int) exp;
    }

    /** Given an array of pole locations, an array of zero locations, and a
     *  gain term, return frequency response specified by these.
     *  This is calculated by walking around the unit circle and forming
     *  the product of the distances to the zeros, dividing by the product
     *  of the distances to the poles, and multiplying by the gain.
     *  The length of the returned array is <i>numSteps</i>.
     *
     *  @param poles An array of pole locations.
     *  @param zeros An array of zero locations.
     *  @param gain A complex gain.
     *  @param numSteps The number of samples in the returned
     *  frequency response.
     */
    public static final Complex[] poleZeroToFrequency(Complex[] poles,
            Complex[] zeros, Complex gain, int numSteps) {
        double step = 2 * Math.PI / numSteps;
        Complex[] freq = new Complex[numSteps];

        double angle = -Math.PI;

        for (int index = 0; index < freq.length; index++) {
            Complex polesContribution = Complex.ONE;
            Complex zerosContribution = Complex.ONE;
            Complex ejw = new Complex(Math.cos(angle), Math.sin(angle));

            if (poles.length > 0) {
                Complex[] diffPoles = ComplexArrayMath.subtract(poles, ejw);
                polesContribution = ComplexArrayMath.product(diffPoles);
            }

            if (zeros.length > 0) {
                Complex[] diffZeros = ComplexArrayMath.subtract(zeros, ejw);
                zerosContribution = ComplexArrayMath.product(diffZeros);
            }

            freq[index] = zerosContribution.divide(polesContribution);
            freq[index] = freq[index].multiply(gain);
            angle += step;
        }

        return freq;
    }

    /** Return a new array that is filled with samples of a waveform of a
     *  specified length. The waveform is sampled with starting at startTime,
     *  at a sampling period of interval.
     *  @param length The number of samples to generate.
     *  @param startTime The corresponding time for the first sample.
     *  @param interval The time between successive samples. This may
     *  be negative if the waveform is to be reversed, or zero if the
     *  array is to be filled with a constant.
     *  @param sampleGen A DoubleUnaryOperation.
     *  @return A new array of doubles.
     *  @see ptolemy.math.DoubleUnaryOperation
     */
    public static final double[] sampleWave(int length, double startTime,
            double interval, DoubleUnaryOperation sampleGen) {
        double time = startTime;

        double[] returnValue = new double[length];

        for (int t = 0; t < length; t++) {
            returnValue[t] = sampleGen.operate(time);
            time += interval;
        }

        return returnValue;
    }

    /** Return a sample of a sawtooth wave with the specified period and
     *  phase at the specified time.  The returned value ranges between
     *  -1.0 and 1.0.  The phase is given as a fraction of a cycle,
     *  typically ranging from 0.0 to 1.0.  If the phase is 0.0 or 1.0,
     *  the wave begins at zero with a rising slope.  If it is 0.5, it
     *  begins at the falling edge with value -1.0.
     *  If it is 0.25, it begins at +0.5.
     *
     *  Throw an exception if the period is less than or equal to 0.
     *
     *  @param period The period of the sawtooth wave.
     *  @param phase The phase of the sawtooth wave.
     *  @param time The time of the sample.
     *  @return A double in the range -1.0 to +1.0.
     */
    public static double sawtooth(double period, double phase, double time) {
        if (period <= 0) {
            throw new IllegalArgumentException(
                    "ptolemy.math.SignalProcessing.sawtooth(): "
                            + "period should be greater than 0.");
        }

        double point = 2 / period
                * Math.IEEEremainder(time + phase * period, period);

        // get rid of negative zero
        point = point == -0.0 ? 0.0 : point;

        // hole at +1.0
        return point == 1.0 ? -1.0 : point;
    }

    /** Return sin(x)/x, the so-called sinc function.
     *  If the argument is very close to zero, significant quantization
     *  errors may result (exactly 0.0 is OK, since this just returns 1.0).
     *
     *  @param x A number.
     *  @return The sinc function.
     */
    public static final double sinc(double x) {
        if (x == 0.0) {
            return 1.0;
        }

        return Math.sin(x) / x;
    }

    /** Return a sample of a square wave with the specified period and
     *  phase at the specified time.  The returned value is 1 or -1.
     *  A sample that falls on the rising edge of the square wave is
     *  assigned value +1.  A sample that falls on the falling edge is
     *  assigned value -1.  The phase is given as a fraction of a
     *  cycle, typically ranging from 0.0 to 1.0.  If the phase is 0.0
     *  or 1.0, the square wave begins at the start of the +1.0 phase.
     *  If it is 0.5, it begins at the start of the -1.0 phase. If it
     *  is 0.25, it begins halfway through the +1.0 portion of the
     *  wave.
     *
     *  Throw an exception if the period is less than or equal to 0.
     *
     *  @param period The period of the square wave.
     *  @param phase The phase of the square wave.
     *  @param time The time of the sample.
     *  @return +1.0 or -1.0.
     */
    public static double square(double period, double phase, double time) {
        if (period <= 0) {
            throw new IllegalArgumentException(
                    "ptolemy.math.SignalProcessing.square(): "
                            + "period should be greater than 0.");
        }

        double point = 2 / period
                * Math.IEEEremainder(time + phase * period, period);

        // hole at +1.0
        return point >= 0 && point < 1 ? 1.0 : -1.0;
    }

    /** Return a sample of a triangle wave with the specified period and
     *  phase at the specified time.  The returned value ranges between
     *  -1.0 and 1.0.  The phase is given as a fraction of a cycle,
     *  typically ranging from 0.0 to 1.0.  If the phase is 0.0 or 1.0,
     *  the wave begins at zero with a rising slope.  If it is 0.5, it
     *  begins at zero with a falling slope. If it is 0.25, it begins at +1.0.
     *
     *  Throw an exception if the period is less than or equal to 0.
     *
     *  @param period The period of the triangle wave.
     *  @param phase The phase of the triangle wave.
     *  @param time The time of the sample.
     *  @return A number in the range -1.0 to +1.0.
     */
    public static double triangle(double period, double phase, double time) {
        if (period <= 0) {
            throw new IllegalArgumentException(
                    "ptolemy.math.SignalProcessing.triangle(): "
                            + "period should be greater than 0.");
        }

        double point = Math.IEEEremainder(time + phase * period, period);

        if (-period / 2.0 <= point && point < -period / 4.0) {
            point = -(4.0 / period * point) - 2;
        } else if (-period / 4.0 <= point && point < period / 4.0) {
            point = 4.0 / period * point;
        } else { // if (T/4.0 <= point && point < T/2.0)
            point = -(4.0 / period * point) + 2;
        }

        return point;
    }

    /** Return the value of the argument
     *  in decibels, which is defined to be 20*log<sub>10</sub>(<em>z</em>),
     *  where <em>z</em> is the argument.
     *  Note that if the input represents power, which is proportional to a
     *  magnitude squared, then this should be divided
     *  by two to get 10*log<sub>10</sub>(<em>z</em>).
     *  @param value The value to convert to decibels.
     */
    public static final double toDecibels(double value) {
        return 20.0 * Math.log(value) * _LOG10SCALE;
    }

    /** Return a new array that is constructed from the specified
     *  array by unwrapping the angles.  That is, if
     *  the difference between successive values is greater than
     *  <em>PI</em> in magnitude, then the second value is modified by
     *  multiples of 2<em>PI</em> until the difference is less than
     *  or equal to <em>PI</em>.
     *  In addition, the first element is modified so that its
     *  difference from zero is less than or equal to <em>PI</em> in
     *  magnitude.  This method is used for generating more meaningful
     *  phase plots.
     *  @param angles An array of angles.
     *  @return A new array of phase-unwrapped angles.
     */
    public static final double[] unwrap(double[] angles) {
        double previous = 0.0;
        double[] result = new double[angles.length];

        for (int i = 0; i < angles.length; i++) {
            result[i] = angles[i];

            while (result[i] - previous < -Math.PI) {
                result[i] += 2 * Math.PI;
            }

            while (result[i] - previous > Math.PI) {
                result[i] -= 2 * Math.PI;
            }

            previous = result[i];
        }

        return result;
    }

    /** Return a new array that is the result of inserting (n-1) zeroes
     *  between each successive sample in the input array, resulting in an
     *  array of length n * L, where L is the length of the original array.
     *  Throw an exception for n &le; 0.
     *  @param x The input array of doubles.
     *  @param n An integer specifying the upsampling factor.
     *  @return A new array of doubles.
     *  @exception IllegalArgumentException If the second argument is not
     *   strictly positive.
     */
    public static final double[] upsample(double[] x, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException(
                    "ptolemy.math.SignalProcessing.upsample(): "
                            + "upsampling factor must be greater than or equal to 0.");
        }

        int length = x.length * n;
        double[] returnValue = new double[length];
        int srcIndex = 0;
        int destIndex;

        // Assume returnValue has been zeroed out
        for (destIndex = 0; destIndex < length; destIndex += n) {
            returnValue[destIndex] = x[srcIndex];
            srcIndex++;
        }

        return returnValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** A small number ( = 1.0e-9). This number is used by algorithms to
     *  detect whether a double is close to zero.
     */
    public static final double EPSILON = 1.0e-9;

    // Scale factor types for the DCT/IDCT
    // In the following formulas,
    //  e[0] = 1/sqrt(2)
    //  e[k] = 1 for k != 0

    /** To select the forward transform :
     *  <p>
     *               N - 1 <br>
     *   X[k] = e[k]  sum  x[n] * cos ((2n + 1)k * PI / 2N) <br>
     *               n = 0 <br>
     *  </p>
     *  <p>
     *  and the inverse transform :
     *              N - 1 <br>
     *   x(n) = (2/N) sum  e(k) X[k] * cos ((2n + 1)k * PI / 2N)<br>
     *              k = 0 <br>
     *  </p>
     *  use this DCT type.
     */
    public static final int DCT_TYPE_NORMALIZED = 0;

    /** To select the forward transform :
     *         N - 1
     *   X[k] = sum  x[n] * cos ((2n + 1)k * PI / 2N)
     *         n = 0
     *  and the inverse transform :
     *         N - 1
     *   x[n] = sum  X[k] * cos ((2n + 1)k * PI / 2N)
     *         k = 0
     *  use this DCT type.
     *  This is the definition of the DCT used by MPEG.
     */
    public static final int DCT_TYPE_UNNORMALIZED = 1;

    /** To select the forward transform :
     *                         N - 1
     *   X[k] = sqrt(2/N) e[k]  sum  x[n] * cos ((2n + 1)k * PI / 2N)
     *                         n = 0
     *  and the inverse transform :
     *                   N - 1
     *   x[n] = sqrt(2/N) sum  e[k] X[k] * cos ((2n + 1)k * PI / 2N)
     *                   k = 0
     *  use this DCT type.
     *  This is the definition of the DCT used in Matlab.
     */
    public static final int DCT_TYPE_ORTHONORMAL = 2;

    /** The number of DCT types supported. */
    public static final int DCT_TYPES = 3;

    // Window types for generateWindow
    // In all of the formulas below, M = length of window - 1

    /** To select the rectangular window,
     *  <p>
     *   w[n] = 1 for 0 &le; n &le; M
     *  </p>
     *  use this window type.
     */
    public static final int WINDOW_TYPE_RECTANGULAR = 0;

    /** To select the Bartlett (triangular) window,
     *  <p>
     *   w[n] = 2n/M      for 0 &le; n &le; M/2 <br>
     *   w[n] = 2 - 2n/M  for M/2 &lt; n &le; M
     *  </p>
     *  use this window type.
     */
    public static final int WINDOW_TYPE_BARTLETT = 1;

    /** To select the Hanning window,
     *  <p>
     *   w[n] = 0.5 - 0.5 cos(2 * PI * n / M)  <br>
     *   for 0 &le; n &le; M
     *  </p>
     *  use this window type.
     */
    public static final int WINDOW_TYPE_HANNING = 2;

    /** To select the Hamming window,
     *  <p>
     *   w[n] = 0.54 - 0.46 cos(2 * PI * n / M) <br>
     *   for 0 &le; n &le; M
     *  </p>
     *  use this window type.
     */
    public static final int WINDOW_TYPE_HAMMING = 3;

    /** To select the Blackman window,
     *  <p>
     *   w[n] = 0.42 - 0.5 cos(2 * PI * n /M)  + <br>
     *          0.08 cos (4 * PI * n / M) <br>
     *   for 0 &le; n &le; M
     *  </p>
     *  use this window type.
     */
    public static final int WINDOW_TYPE_BLACKMAN = 4;

    /** To select the 4-term Blackman-Harris window,
     *  <p>
     *   w[n] = 0.35875 - 0.48829 cos(2 * PI * n /M)  + <br>
     *          0.14128 cos (4 * PI * n / M) - 0.01168 cos(6 * PI * n / M) <br>
     *   for 0 &le; n &le; M
     *  </p>
     *  use this window type.
     */
    public static final int WINDOW_TYPE_BLACKMAN_HARRIS = 5;

    /** The number of window types that can be generated. */
    public static final int WINDOW_TYPES = 6;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** This class generates samples of a Gaussian function with the
     *  specified mean and standard deviation.
     *  The function computed is :
     *  <pre>
     *  h(t) = (1/(sqrt(2 * PI) * stdDev) *
     *         exp(-(t - mean)<sup>2</sup> / (2 * stdDev<sup>2</sup>))
     *  </pre>

     */
    public static class GaussianSampleGenerator implements DoubleUnaryOperation {
        /** Construct a GaussianSampleGenerator.
         *  @param standardDeviation The standard deviation of the
         *  Gaussian function.
         */
        public GaussianSampleGenerator(double mean, double standardDeviation) {
            _mean = mean;
            _oneOverTwoVariance = 1.0 / (2.0 * standardDeviation * standardDeviation);
            _factor = ONE_OVER_SQRT_TWO_PI / standardDeviation;
        }

        /** Return a sample of the Gaussian function, sampled at the
         *  specified time.
         */
        @Override
        public final double operate(double time) {
            double shiftedTime = time - _mean;
            return _factor
                    * Math.exp(-shiftedTime * shiftedTime * _oneOverTwoVariance);
        }

        private final double _mean;

        private final double _oneOverTwoVariance;

        private final double _factor;

        private static final double ONE_OVER_SQRT_TWO_PI = 1.0 / Math
                .sqrt(2 * Math.PI);
    }

    /** This class generates samples of a polynomial.
     *  The function computed is:
     *  <pre>
     *  h(t) = a<sub>0</sub> + a<sub>1</sub>t + a<sub>2</sub>t<sup>2</sup> + ...
     *         + a<sub>n-1</sub>t<sup>n-1</sup>
     *  </pre>
     *  or
     *  <pre>
     *  h(t) = a<sub>0</sub> + a<sub>1</sub>t<sup>-1</sup>
     *         + a<sub>2</sub>t<sup>-2</sup> + ...
     *         + a<sub>n-1</sub>t<sup>-(n-1)</sup>
     *  </pre>
     *  depending on the direction specified.
     */
    public static class PolynomialSampleGenerator implements
            DoubleUnaryOperation {
        /** Construct a PolynomialSampleGenerator. The input argument is
         *  an array of doubles
         *  a[0] = a<sub>0</sub> .. a[n-1] = a<sub>n-1</sub> used to compute
         *  the formula :
         *  h(t) = a<sub>0</sub> + a<sub>1</sub>t + ... + a<sub>n-1</sub>t<sup>n-1</sup>
         *  The array of doubles must be of length 1 or greater.
         *  The array of doubles is copied, so the user is free to modify
         *  it after construction.
         *  The exponents on t in the above equation will all be negated if
         *  the direction parameter is -1; otherwise the direction parameter
         *  should be 1.
         *  @param coefficients An array of double coefficients.
         *  @param direction 1 for positive exponents, -1 for negative
         *  exponents.
         */
        public PolynomialSampleGenerator(double[] coefficients, int direction) {
            if (direction != 1 && direction != -1) {
                throw new IllegalArgumentException(
                        "ptolemy.math.SignalProcessing.LineSampleGenerator: "
                                + "direction must be either 1 or -1");
            }

            _coeffLength = coefficients.length;

            // copy coefficient array
            _coefficients = DoubleArrayMath.resize(coefficients, _coeffLength);
            _direction = direction;
        }

        /** Return a sample of the line, sampled at the specified time.
         *  Note that at time = 0, with a negative direction, the sample
         *  will be positive or negative infinity.
         */
        @Override
        public final double operate(double time) {
            double sum = _coefficients[0];
            double tn = time;

            for (int i = 1; i < _coeffLength; i++) {
                if (_direction == 1) {
                    sum += _coefficients[i] * tn;
                } else {
                    sum += _coefficients[i] / tn;
                }

                tn *= time;
            }

            return sum;
        }

        private final double[] _coefficients;

        private final int _coeffLength;

        private final int _direction;
    }

    /** This class generates samples of a sawtooth wave with the specified
     *  period and phase. The returned values range between -1.0 and 1.0.
     */
    public static class SawtoothSampleGenerator implements DoubleUnaryOperation {
        /** Construct a SawtoothSampleGenerator with the given period and
         *  phase.  The phase is given as a fraction of a cycle,
         *  typically ranging from 0.0 to 1.0.  If the phase is 0.0 or 1.0,
         *  the wave begins at zero with a rising slope.  If it is 0.5, it
         *  begins at the falling edge with value -1.0.
         *  If it is 0.25, it begins at +0.5.
         */
        public SawtoothSampleGenerator(double period, double phase) {
            _period = period;
            _phase = phase;
        }

        /** Return a sample of the sawtooth wave, sampled at the
         *  specified time.
         */
        @Override
        public final double operate(double time) {
            return sawtooth(_period, _phase, time);
        }

        private final double _period;

        private final double _phase;
    }

    /** This class generates samples of a sinusoidal wave.
     *  The function computed is :
     *  <pre>
     *  h(t) = cos(frequency * t + phase)
     *  </pre>
     *  where the argument is taken to be in radians.
     *  To use this class to generate a sine wave, simply
     *  set the phase to -Math.PI*0.5 from the
     *  phase, since sin(t) = cos(t - PI/2).
     */
    public static class SinusoidSampleGenerator implements DoubleUnaryOperation {
        /**
         *  Construct a SinusoidSampleGenerator.
         *  @param frequency The frequency of the cosine wave, in radians per
         *  unit time.
         *  @param phase The phase shift, in radians.
         */
        public SinusoidSampleGenerator(double frequency, double phase) {
            _frequency = frequency;
            _phase = phase;
        }

        @Override
        public final double operate(double time) {
            return Math.cos(_frequency * time + _phase);
        }

        private final double _frequency;

        private final double _phase;
    }

    /** This class generates samples of a raised cosine pulse, or if the
     *  excess is zero, a modified sinc function.
     *  <p>
     *  The function that is computed is:
     *  <p>
     *  <pre>
     *         sin(PI t/T)   cos(excess PI t/T)
     *  h(t) = ----------- * -----------------
     *          PI t/T      1-(2 excess t/T)<sup>2</sup>
     *  </pre>
     *  <p>
     *  This is called a "raised cosine pulse" because in the frequency
     *  domain its shape is that of a raised cosine.
     *  <p>
     *  For some applications, you may wish to apply a window function to this
     *  impulse response, since it is rather abruptly terminated at the two \
     *  ends.
     *  <p>
     *  This implementation is ported from the Ptolemy 0.x implementation
     *  by Joe Buck, Brian Evans, and Edward A. Lee.
     *  Reference: <a href=http://www.amazon.com/exec/obidos/ASIN/0792393910/qid%3D910596335/002-4907626-8092437>E. A. Lee and D. G. Messerschmitt,
     *  <i>Digital Communication, Second Edition</i>,
     *  Kluwer Academic Publishers, Boston, 1994.</a>
     *
     */
    public static class RaisedCosineSampleGenerator implements
            DoubleUnaryOperation {
        /** Construct a RaisedCosineSampleGenerator.
         *  @param firstZeroCrossing The time of the first zero crossing,
         *  after time zero. This would be the symbol interval in a
         *  communications application of this pulse.
         *  @param excess The excess bandwidth (in the range 0.0 to 1.0), also
         *  called the rolloff factor.
         */
        public RaisedCosineSampleGenerator(double firstZeroCrossing,
                double excess) {
            _oneOverFZC = 1.0 / firstZeroCrossing;
            _excess = excess;
        }

        /**  Return a sample of the raised cosine pulse, sampled at the
         *  specified time.
         */
        @Override
        public final double operate(double time) {
            if (time == 0.0) {
                return 1.0;
            }

            double x = time * _oneOverFZC;
            double s = sinc(Math.PI * x);

            if (_excess == 0.0) {
                return s;
            }

            x *= _excess;

            double denominator = 1.0 - 4.0 * x * x;

            // If the denominator is close to zero, take it to be zero.
            if (close(denominator, 0.0)) {
                return s * ExtendedMath.PI_OVER_4;
            }

            return s * Math.cos(Math.PI * x) / denominator;
        }

        private final double _oneOverFZC;

        private final double _excess;
    }

    /** This class generates samples of a sinc wave with the specified
     *  first zero crossing.
     */
    public static class SincSampleGenerator implements DoubleUnaryOperation {
        @Override
        public final double operate(double time) {
            return sinc(time);
        }
    }

    /** This class generates samples of a square-root raised cosine pulse.
     *  The function computed is:
     *  <p>
     *  <pre>
     *           4 x(cos((1+x)PI t/T) + T sin((1-x)PI t/T)/(4x t/T))
     *  h(t) =  ---------------------------------------------------
     *                PI sqrt(T)(1-(4 x t/T)<sup>2</sup>)
     *  </pre>
     *  <p>
     *  where <i>x</i> is the the excess bandwidth.
     *  This pulse convolved with itself will, in principle, be equal
     *  to a raised cosine pulse.  However, because the pulse decays rather
     *  slowly for low excess bandwidth, this ideal is not
     *  closely approximated by short finite approximations of the pulse.
     *  <p>
     *  This implementation was ported from the Ptolemy 0.x implementation
     *  by Joe Buck, Brian Evans, and Edward A. Lee.
     *  Reference: E. A. Lee and D. G. Messerschmitt,
     *  <i>Digital Communication, Second Edition</i>,
     *  Kluwer Academic Publishers, Boston, 1994.
     *
     *  The implementation was then further optimized to cache computations
     *  that are independent of time.
     */
    public static class SqrtRaisedCosineSampleGenerator implements
            DoubleUnaryOperation {
        /** Construct a SqrtRaisedCosineSampleGenerator.
         *  @param firstZeroCrossing The time of the first zero crossing of
         *  the corresponding raised cosine pulse.
         *  @param excess The excess bandwidth of the corresponding raised
         *  cosine pulse (also called the rolloff factor).
         */
        public SqrtRaisedCosineSampleGenerator(double firstZeroCrossing,
                double excess) {
            _excess = excess;

            _oneOverFZC = 1.0 / firstZeroCrossing;
            _sqrtFZC = Math.sqrt(firstZeroCrossing);
            _squareFZC = firstZeroCrossing * firstZeroCrossing;

            _onePlus = (1.0 + _excess) * Math.PI * _oneOverFZC;
            _oneMinus = (1.0 - _excess) * Math.PI * _oneOverFZC;

            _fourExcess = 4.0 * _excess;
            _eightExcessPI = 8.0 * _excess * Math.PI;
            _sixteenExcessSquared = _fourExcess * _fourExcess;

            double fourExcessOverPI = _fourExcess / Math.PI;
            double oneOverSqrtFZC = 1.0 / _sqrtFZC;

            _sampleAtZero = (fourExcessOverPI + 1.0 - _excess) * oneOverSqrtFZC;
            _fourExcessOverPISqrtFZC = fourExcessOverPI * oneOverSqrtFZC;

            _fzcSqrtFZCOverEightExcessPI = firstZeroCrossing * _sqrtFZC
                    / _eightExcessPI;
            _fzcOverFourExcess = firstZeroCrossing / _fourExcess;

            _oneMinusFZCOverFourExcess = _oneMinus * _fzcOverFourExcess;
        }

        /*  Return a sample of the raised cosine pulse, sampled at the
         *  specified time.
         *  @param time The time at which to sample the pulse.
         *  @return A double.
         */
        @Override
        public final double operate(double time) {
            if (time == 0.0) {
                return _sampleAtZero;
            }

            double x = time * _oneOverFZC;

            if (_excess == 0.0) {
                return _sqrtFZC * Math.sin(Math.PI * x) / (Math.PI * time);
            }

            double oneMinusTime = _oneMinus * time;
            double onePlusTime = _onePlus * time;

            // Check to see whether we will get divide by zero.
            double denominator = time * time * _sixteenExcessSquared
                    - _squareFZC;

            if (close(denominator, 0.0)) {
                double oneOverTime = 1.0 / time;
                double oneOverTimeSquared = oneOverTime * oneOverTime;

                return _fzcSqrtFZCOverEightExcessPI
                        * oneOverTime
                        * (_onePlus * Math.sin(onePlusTime)
                                - _oneMinusFZCOverFourExcess * oneOverTime
                                * Math.cos(oneMinusTime) + _fzcOverFourExcess
                                * oneOverTimeSquared * Math.sin(oneMinusTime));
            }

            return _fourExcessOverPISqrtFZC
                    * (Math.cos(onePlusTime) + Math.sin(oneMinusTime)
                            / (x * _fourExcess))
                    / (1.0 - _sixteenExcessSquared * x * x);
        }

        private final double _oneOverFZC;

        private final double _sqrtFZC;

        private final double _squareFZC;

        private final double _onePlus;

        private final double _oneMinus;

        private final double _excess;

        private final double _fourExcess;

        private final double _eightExcessPI;

        private final double _sixteenExcessSquared;

        private final double _sampleAtZero;

        private final double _fourExcessOverPISqrtFZC;

        private final double _fzcSqrtFZCOverEightExcessPI;

        private final double _fzcOverFourExcess;

        private final double _oneMinusFZCOverFourExcess;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Check that the order of a transform is between 0 and 31, inclusive.
    // Throw an exception otherwise.
    private static void _checkTransformOrder(int order) {
        if (order < 0) {
            throw new IllegalArgumentException(
                    "ptolemy.math.SignalProcessing : order of transform "
                            + "must be non-negative.");
        } else if (order > 31) {
            throw new IllegalArgumentException(
                    "ptolemy.math.SignalProcessing : order of transform "
                            + "must be less than 32.");
        }
    }

    // Check the arguments for a transform on an array of doubles, using
    // _checkTransformInput() and _checkTransformOrder(). Return an
    // appropriately padded array on which to perform the transform.
    private static double[] _checkTransformArgs(double[] x, int order,
            boolean inverse) {
        _checkTransformOrder(order);

        int size = 1 << order;

        // Zero pad the array if necessary
        if (x.length < size) {
            x = inverse ? DoubleArrayMath.padMiddle(x, size) : DoubleArrayMath
                    .resize(x, size);
        }

        return x;
    }

    // Check the arguments for a transform on an array of Complex's, using
    // _checkTransformInput() and _checkTransformOrder(). Return an
    // appropriately padded array on which to perform the transform.
    private static Complex[] _checkTransformArgs(Complex[] x, int order,
            boolean inverse) {
        _checkTransformOrder(order);

        int size = 1 << order;

        // Zero pad the array if necessary
        if (x.length < size) {
            x = inverse == _INVERSE_TRANSFORM ? ComplexArrayMath.padMiddle(x,
                    size) : ComplexArrayMath.resize(x, size);
        }

        return x;
    }

    // Returns an array with half the size + 1 because of the symmetry
    // of the cosDFT function.
    private static double[] _cosDFT(double[] x, int size, int order) {
        switch (size) {
        // Base cases for lower orders
        case 0:
            return null; // should never be used

        case 1: {
            double[] returnValue = new double[1];
            returnValue[0] = x[0];
            return returnValue;
        }

        case 2: {
            double[] returnValue = new double[2];
            returnValue[0] = x[0] + x[1];
            returnValue[1] = x[0] - x[1];
            return returnValue;
        }

        // Optimized base case for higher orders
        case 4: {
            double[] returnValue = new double[3];
            returnValue[0] = x[0] + x[1] + x[2] + x[3];
            returnValue[1] = x[0] - x[2];
            returnValue[2] = x[0] - x[1] + x[2] - x[3];
            return returnValue;
        }
        }

        int halfN = size >> 1;
        int quarterN = size >> 2;

        double[] x1 = new double[halfN];

        for (int k = 0; k < halfN; k++) {
            x1[k] = x[k << 1];
        }

        double[] x2 = new double[quarterN];

        for (int k = 0; k < quarterN; k++) {
            int twoIp = (k << 1) + 1;
            x2[k] = x[twoIp] + x[size - twoIp];
        }

        double[] halfCosDFT = _cosDFT(x1, halfN, order - 1);
        double[] quarterDCT = _DCT(x2, quarterN, order - 2);

        double[] returnValue = new double[halfN + 1];

        for (int k = 0; k < quarterN; k++) {
            returnValue[k] = halfCosDFT[k] + quarterDCT[k];
        }

        returnValue[quarterN] = halfCosDFT[quarterN];

        for (int k = quarterN + 1; k <= halfN; k++) {
            int idx = halfN - k;
            returnValue[k] = halfCosDFT[idx] - quarterDCT[idx];
        }

        return returnValue;
    }

    // Returns an array with half the size because of the symmetry
    // of the sinDFT function.
    private static double[] _sinDFT(double[] x, int size, int order) {
        switch (size) {
        // Base cases for lower orders
        case 0:
        case 1:
        case 2:
            return null; // should never be used

            // Optimized base case for higher orders
        case 4: {
            double[] returnValue = new double[2];

            // returnValue[0] = 0.0; // not necessary for Java,
            // also not read
            returnValue[1] = x[1] - x[3];
            return returnValue;
        }
        }

        int halfN = size >> 1;
        int quarterN = size >> 2;

        double[] x1 = new double[halfN];

        for (int k = 0; k < halfN; k++) {
            x1[k] = x[k << 1];
        }

        double[] x3 = new double[quarterN];

        for (int k = 0; k < quarterN; k++) {
            int twoIp = (k << 1) + 1;
            x3[k] = (k & 1) == 1 ? x[size - twoIp] - x[twoIp] : x[twoIp]
                    - x[size - twoIp];
        }

        double[] halfSinDFT = _sinDFT(x1, halfN, order - 1);
        double[] quarterDCT = _DCT(x3, quarterN, order - 2);

        double[] returnValue = new double[halfN];

        // returnValue[0] = 0.0; // not necessary in Java
        for (int k = 1; k < quarterN; k++) {
            returnValue[k] = halfSinDFT[k] + quarterDCT[quarterN - k];
        }

        returnValue[quarterN] = quarterDCT[0];

        for (int k = quarterN + 1; k < halfN; k++) {
            returnValue[k] = quarterDCT[k - quarterN] - halfSinDFT[halfN - k];
        }

        return returnValue;
    }

    private static double[] _DCT(double[] x, int size, int order) {
        double[] returnValue;

        if (size == 1) {
            returnValue = new double[1];
            returnValue[0] = x[0];
            return returnValue;
        }

        if (size == 2) {
            returnValue = new double[2];
            returnValue[0] = x[0] + x[1];
            returnValue[1] = ExtendedMath.ONE_OVER_SQRT_2 * (x[0] - x[1]);
            return returnValue;
        }

        int halfN = size >> 1;

        double[] x4 = new double[size];

        for (int n = 0; n < halfN; n++) {
            int twoN = n << 1;

            // Do zero padding here
            if (twoN >= x.length) {
                x4[n] = 0.0;
            } else {
                x4[n] = x[twoN];
            }

            if (twoN + 1 >= x.length) {
                x4[size - n - 1] = 0.0;
            } else {
                x4[size - n - 1] = x[twoN + 1];
            }
        }

        double[] cosDFTarray = _cosDFT(x4, size, order);
        double[] sinDFTarray = _sinDFT(x4, size, order);

        double[] p1tab = _P1Table[order];
        double[] p2tab = _P2Table[order];
        double[] ctab = _CTable[order];

        returnValue = new double[size];

        returnValue[0] = cosDFTarray[0];

        for (int k = 1; k < halfN; k++) {
            double m1 = (cosDFTarray[k] + sinDFTarray[k]) * ctab[k];
            double m2 = sinDFTarray[k] * p1tab[k];
            double m3 = cosDFTarray[k] * p2tab[k];
            returnValue[k] = m1 - m2;
            returnValue[size - k] = m1 + m3;
        }

        returnValue[halfN] = ExtendedMath.ONE_OVER_SQRT_2 * cosDFTarray[halfN];

        return returnValue;
    }

    private synchronized static void _FFCTTableGen(int limit) {
        for (int i = _FFCTGenLimit; i <= limit; i++) {
            int N = 1 << i; // Watch out for this if i ever becomes > 31
            _P1Table[i] = new double[N];
            _P2Table[i] = new double[N];
            _CTable[i] = new double[N];

            double[] p1t = _P1Table[i];
            double[] p2t = _P2Table[i];
            double[] ct = _CTable[i];

            for (int k = 0; k < N; k++) {
                double arg = Math.PI * k / (2.0 * N);
                double c = Math.cos(arg);
                double s = Math.sin(arg);
                p1t[k] = c + s;
                p2t[k] = s - c;
                ct[k] = c;
            }
        }

        _FFCTGenLimit = Math.max(_FFCTGenLimit, limit);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                         ////
    // Tables needed for the FFCT algorithm. We assume no one will attempt
    // to do a transform of size greater than 2^31.
    private static final double[][] _P1Table = new double[32][];

    private static final double[][] _P2Table = new double[32][];

    private static final double[][] _CTable = new double[32][];

    private static int _FFCTGenLimit = 0;

    // Table of scaleFactors for the IDCT.
    private static final Complex[][][] _IDCTfactors = new Complex[DCT_TYPES][32][];

    // Various constants
    private static final double _LOG10SCALE = 1.0 / Math.log(10.0);

    private static final double _LOG2SCALE = 1.0 / Math.log(2.0);

    // Indicates a forward/inverse transform for checking arguments
    private static final boolean _FORWARD_TRANSFORM = false;

    private static final boolean _INVERSE_TRANSFORM = true;
}
