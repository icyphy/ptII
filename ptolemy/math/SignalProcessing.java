/* A library of signal processing operations.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.math;

import java.lang.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// SignalProcessing
/**
 * This class provides signal processing functions.
 *
 * @author Albert Chen, William Wu, Edward A. Lee, Jeff Tsay
 * @version $Id$
 */

public final class SignalProcessing {

    // The only constructor is private so that this class cannot
    // be instantiated.
    private SignalProcessing() {}

    /////////////////////////////////////////////////////////////////////////
    ////                         public methods                          ////

    /** Return true if the first argument is close to the second (within
     *  epsilon, where epsilon is a static public variable of this class).
     */
    public static boolean close(double first, double second) {
        double diff = first-second;
        return (diff < epsilon && diff > -epsilon);
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

    /** Return a new array of doubles that is the forward, normalized
     *  DCT of the input array of doubles.
     *  This method automatically computes the order of the transform
     *  based on the length of the input array.
     *  @param x An array of doubles.
     *  @retval A new array of doubles.
     */
    public static double[] DCT(double[] x) {
        return DCT(x, order(x.length), DCT_TYPE_NORMALIZED);
    }

    /** Return a new array of doubles that is the forward DCT of the 
     *  input array of doubles.
     *  See the DCT_TYPE_XXX constants for documentation of the
     *  exact formula, which depends on the type.
     *  @param x An array of doubles.
     *  @param order The base-2 logarithm of the size of the transform.
     *  @param type An integer specifying which type of DCT.
     *  @retval A new array of doubles.
     */
    public static double[] DCT(double[] x, int order, int type) {
        // check if order > 31

        if (type >= DCT_TYPES) {
          throw new IllegalArgumentException("ptolemy.math.SignalProcessing." +
           "DCT() : Bad DCT type");
        }

        int size = 1 << order;

        // Make sure the tables have enough entries for the DCT computation
        if (order >  _FFCTGenLimit)
           _FFCTTableGen(order);

        double[] retval =  _DCT(x, size, order);
       
        switch (type) {
   
          case DCT_TYPE_ORTHONORMAL:
          double factor = Math.sqrt(2.0 / size);
          retval = DoubleArrayMath.scale(retval, factor);
          // no break here

          case DCT_TYPE_NORMALIZED:
          retval[0] *= _ONEOVERROOT2;
          break;
        }
       
        return retval;
    }

    /** Return the value of the argument <em>z</em>
     *  in decibels, which is defined to be 20*log<sub>10</sub>(<em>z</em>).
     *  Note that if the input represents power, which is proportional to a
     *  magnitude squared, then this should be divided
     *  by two to get 10*log<sub>10</sub>(<em>z</em>).
     */
    public static double decibel(double value) {
        return 20*Math.log(value)/_LOG10SCALE;
    }

    /** Return a new array the value of the argument array
     *  in decibels, using the previous decibel() method.
     *  You may wish to combine this with DoubleArrayMath.limit()
     */
    public static double[] decibel(double[] values) {
        double[] result = new double[values.length];
        for (int i = values.length-1; i >= 0; i--) {
            result[i] = decibel(values[i]);
        }
        return result;
    }

    /** Return a new array of doubles that is the inverse, normalized
     *  DCT of the input array of doubles.
     *  This method automatically computes the order of the transform
     *  based on the length of the input array, and calls :
     *  IDCT(x, order, DCT_TYPE_NORMALIZED)
     *  @param x An array of doubles.
     *  @retval A new array of doubles.
     */
    public static double[] IDCT(double[] x) {
        return IDCT(x, order(x.length), DCT_TYPE_NORMALIZED);
    } 

    /** Return a new array of doubles that is the inverse DCT of the 
     *  input array of doubles.
     *  See the DCT_TYPE_XXX constants for documentation of the
     *  exact formula, which depends on the type.
     *  @param x An array of doubles.
     *  @param order The base-2 logarithm of the size of the transform.
     *  @param type An integer specifying which type of IDCT.
     *  @retval A new array of doubles.
     */
    public static double[] IDCT(double[] x, int order, int type) {
       // check if order > 31

       if (type >= DCT_TYPES) {
          throw new IllegalArgumentException("ptolemy.math.SignalProcessing." +
           "IDCT() : Bad DCT type");
       }

       int size    = 1 << order;
       int twoSize = 2 << order;      

       // Generate scalefactors if necessary
       if (_IDCTfactors[type][order] == null) {
          _IDCTfactors[type][order] = new Complex[twoSize];
          
          double oneOverTwoSize = 1.0 / (double) twoSize;

          double factor = 1.0;
          double oneOverE0 = 2.0;

          switch (type) {
            case DCT_TYPE_NORMALIZED:
            factor = 2.0;
            oneOverE0 = _ROOT2;
            break; 

            case DCT_TYPE_ORTHONORMAL:
            factor = Math.sqrt(16.0 * oneOverTwoSize); // == 2 * sqrt(2/N)
            oneOverE0 = _ROOT2;
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

       // Convert to Complex, while multiplying by scalefactors

       evenX[0] = myFactors[0].scale(x[0]);
       for (int k = 1; k < size; k++) {
           evenX[k] = myFactors[k].scale(x[k]);
           evenX[twoSize - k] = myFactors[twoSize - k].scale(-x[k]); 
       }
       evenX[size] = new Complex(0.0, 0.0);

       double[] longOutput = IFFTRealOut(evenX, order + 1);

       // Truncate result
       return DoubleArrayMath.resize(longOutput, size);
    }

    /** Return a new array of Complex's which is the inverse FFT
     *  of an input array of Complex's.
     *  This method automatically computes the order of the transform
     *  based on the length of the input array.
     *  @param x An array of Complex's.
     *  @retval A new array of Complex's.
     */
    public static Complex[] IFFTComplexOut(Complex[] x) {
        return IFFTComplexOut(x, order(x.length));
    }

    /** Return a new array of Complex's which is the forward FFT
     *  of an input array of Complex's.
     *  @param x An array of Complex's.
     *  @param order The base-2 logarithm of the size of the transform.
     *  @retval A new array of Complex's.
     */
    public static Complex[] IFFTComplexOut(Complex[] x, int order) {

        x = _checkTransformArgs(x, order);

        Complex[] conjX = ComplexArrayMath.conjugate(x);
        Complex[] yConj = FFTComplexOut(conjX, order);
        Complex[] y = ComplexArrayMath.conjugate(yConj);

        // scale by 1/N
        double oneOverN = 1.0 / (double) (1 << order);
        return ComplexArrayMath.scale(y, oneOverN);
    }

    /** Return a new array of double's which is the real part of the inverse 
     *  FFT of an input array of Complex's.
     *  This is less than half as expensive as computing both the real and
     *  imaginary parts. It is especially useful when it is known that the
     *  output is purely real.
     *  This method automatically computes the order of the transform
     *  based on the length of the input array, and calls :
     *  IFFTRealOut(x, order)
     *  @param x An array of Complex's.
     *  @retval A new array of doubles.
     */
    public static double[] IFFTRealOut(Complex[] x) {
        return IFFTRealOut(x, order(x.length));
    }

    /** Return a new array of double's which is the real part of the inverse 
     *  FFT of an input array of Complex's.
     *  This method is less than half as expensive as computing both the 
     *  real and imaginary parts of an IFFT of an array of Complex's. It is 
     *  especially useful when it is known that the output is purely real.
     *  @param x An array of Complex's.
     *  @retval A new array of double's.
     */
    public static double[] IFFTRealOut(Complex[] x, int order) {
        
        x = _checkTransformArgs(x, order);

        double[] realx = ComplexArrayMath.realParts(x);
        double[] realrealX = FFTRealOut(realx, order);

        double[] imagx = ComplexArrayMath.imagParts(x);
        double[] imagimagX = FFTImagOut(imagx, order);

        realrealX = DoubleArrayMath.add(realrealX, imagimagX); 

        // scale by 1/N
        double oneOverN = 1.0 / (double) (1 << order);
        return DoubleArrayMath.scale(realrealX, oneOverN);
    }

    /** Return a new array of double's which is the real part of the inverse 
     *  FFT of an input array of doubles. 
     *  This method automatically computes the order of the transform
     *  based on the length of the input array, and calls :
     *  IFFTRealOut(x, order)
     *  @param x An array of doubles.
     *  @retval A new array of doubles.
     */  
    public static double[] IFFTRealOut(double[] x) {
        return IFFTRealOut(x, order(x.length));
    }

    /** Return a new array of double's which is the real part of the inverse 
     *  FFT of an input array of doubles. This method is less than half
     *  as expensive as computing the real part of an IFFT of an array of
     *  Complex's. It is especially useful when both the input and output
     *  are known to be purely real.
     *  @param x An array of doubles.
     *  @param order The base-2 logarithm of the size of the transform.
     *  @retval A new array of doubles.
     */  
    public static double[] IFFTRealOut(double[] x, int order) {
        double[] y = FFTRealOut(x, order);

        // scale by 1/N
        double oneOverN = 1.0 / (double) (1 << order);
        return DoubleArrayMath.scale(y, oneOverN);
    }

    /** Return a new array of Complex's which is the forward FFT
     *  of an input array of Complex's.
     *  This method automatically computes the order of the transform
     *  based on the length of the input array, and calls
     *  FFTComplexOut(x, order).
     *  @param x An array of Complex's.
     *  @retval A new array of Complex's.
     */
    public static Complex[] FFTComplexOut(Complex[] x) {
        return FFTComplexOut(x, order(x.length));
    }

    /** Return a new array of Complex's which is the forward FFT
     *  of an input array of Complex's.
     *  @param x An array of Complex's.
     *  @param order The base-2 logarithm of the size of the transform.
     *  @retval A new array of Complex's.
     */
    public static Complex[] FFTComplexOut(Complex[] x, int order) {
        x = _checkTransformArgs(x, order);

        double[] realx = ComplexArrayMath.realParts(x);
        double[] realrealX = FFTRealOut(realx, order);
        double[] imagrealX = FFTImagOut(realx, order);

        double[] imagx = ComplexArrayMath.imagParts(x);
        double[] realimagX = FFTRealOut(imagx, order);
        double[] imagimagX = FFTImagOut(imagx, order);

        realrealX = DoubleArrayMath.subtract(realrealX, imagimagX);
        imagrealX = DoubleArrayMath.add(imagrealX, realimagX);

        return ComplexArrayMath.formArray(realrealX, imagrealX);
    }

    /** Return a new array of Complex's which is the forward FFT
     *  of a real input array of doubles.
     *  This method automatically computes the order of the transform
     *  based on the length of the input array, and calls
     *  FFTComplexOut(x, order).
     *  @param x An array of doubles.
     *  @retval A new array of Complex's.
     */
    public static Complex[] FFTComplexOut(double[] x) {
        return FFTComplexOut(x, order(x.length));
    }

    /** Return a new array of Complex's which is the forward FFT
     *  of a real input array of doubles.
     *  This method is half as expensive as computing the FFT of a
     *  Complex array.
     *  @param x An array of doubles.
     *  @param order The base-2 logarithm of the size of the transform.
     *  @retval A new array of Complex's.
     */
    public static Complex[] FFTComplexOut(double[] x, int order) {
        // Argmument checking is done inside FFTRealOut() and FFTImagOut()  

        double[] realPart = FFTRealOut(x, order);
        double[] imagPart = FFTImagOut(x, order);

        return ComplexArrayMath.formArray(realPart, imagPart);
    }

    /** Return a new array of doubles which is the imaginary part of the
     *  FFT of an input array of Complex's.
     *  This method automatically computes the order of the transform
     *  based on the length of the input array, and calls
     *  FFTImagOut(x, order).
     *  @param x An array of Complex's.
     *  @retval A new array of doubles.
     */
    public static double[] FFTImagOut(Complex[] x) {
        return FFTImagOut(x, order(x.length));
    }

    /** Return a new array of doubles which is the imaginary part of the
     *  FFT of an input array of Complex's.
     *  This method is half as expensive as computing both the real and
     *  imaginary parts of an FFT on a array of Complex's. It is especially 
     *  useful when the output is known to be purely imaginary.
     *  @param x An array of Complex's.
     *  @param order The base-2 logarithm of the size of the transform.
     *  @retval A new array of doubles.
     */
    public static double[] FFTImagOut(Complex[] x, int order) {
        x = _checkTransformArgs(x, order);

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
     *  @retval A new array of doubles.
     */
    public static double[] FFTImagOut(double[] x) {
        return FFTImagOut(x, order(x.length));
    }

    /** Return a new array of doubles that is the imaginary part of the FFT
     *  of the real input array of doubles.
     *  This method is half as expensive as computing both the real and
     *  imaginary parts of a FFT on a real array. It is especially useful when
     *  the output is known to be purely imaginary (input is odd).
     *  @param x An array of doubles.
     *  @param order The base-2 logarithm of the size of the transform.
     *  @retval A new array of doubles.
     */
    public static double[] FFTImagOut(double[] x, int order) {
        x = _checkTransformArgs(x, order);

        int size = 1 << order;
        int halfN = size >> 1;
        
        // Make sure the tables have enough entries for the DCT computation
        // at size N/4
        if ((order - 2) >  _FFCTGenLimit) {
           _FFCTTableGen(order - 2);
        }

        double[] imagPart = _sinDFT(x, size, order);

        double[] retval = new double[size];

        // Don't bother to look at the array for element 0
        // retval[0] = 0.0; // not necessary in Java

        for (int k = 1; k < halfN; k++) {
            retval[k] = -imagPart[k];
            retval[size - k] = imagPart[k];
        }

        // retval[halfN] = 0.0; // not necessary in Java

        return retval;
    }

    /** Return a new array of doubles which is the real part of the 
     *  forward FFT of an input array of Complex's.
     *  This method automatically computes the order of the transform
     *  based on the length of the input array, and calls :
     *  FFTRealOut(x, order)
     *  @param x An array of Complex's.
     *  @retval A new array of doubles.
     */
    public static double[] FFTRealOut(Complex[] x) {
        return FFTRealOut(x, order(x.length));
    }

    /** Return a new array of doubles which is the real part of the 
     *  forward FFT of an input array of Complex's.
     *  This method is half as expensive as computing both the real and
     *  imaginary parts of an FFT on a array of Complex's. It is especially 
     *  useful when the output is known to be purely real.
     *  @param x An array of Complex's.
     *  @param order The base-2 logarithm of the size of the transform.
     *  @retval A new array of doubles.
     */
    public static double[] FFTRealOut(Complex[] x, int order) {
        x = _checkTransformArgs(x, order);

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
     *  @retval A new array of doubles.
     */
    public static double[] FFTRealOut(double[] x) {
        return FFTRealOut(x, order(x.length));
    }

    /** Return a new array of doubles that is the real part of the FFT of
     *  the real input array of doubles.
     *  This method is half as expensive as computing both the real and
     *  imaginary parts of an FFT on a real array. It is especially useful 
     *  when the output is known to be purely real (input is even).
     *  @param x An array of doubles.
     *  @param order The base-2 logarithm of the size of the transform     
     *  @retval A new array of doubles.
     */
    public static double[] FFTRealOut(double[] x, int order) {
        x = _checkTransformArgs(x, order);
        
        int size = 1 << order;
        int halfN = size >> 1;
        
        if (x.length < size) {
           x = DoubleArrayMath.resize(x, size);
        }
        
        // Make sure the tables have enough entries for the DCT computation
        // at size N/4
        if ((order - 2) >  _FFCTGenLimit) {
           _FFCTTableGen(order - 2);
        }
     
        double[] realPart = _cosDFT(x, size, order);
        double[] retval = new double[size];

        System.arraycopy(realPart, 0, retval, 0, halfN + 1);

        for (int k = halfN + 1; k < size; k++) {
            retval[k] = realPart[size - k];
        }

        return retval;
    }
    
    /** Return the "order" of a transform size, i.e. the base-2 logarithm
     *  of the size. The order will be rounded up to the nearest integer.
     *  @param size The size of the transform.
     *  @retval The order of the transform.
     */
    public static int order(int size) {
       double m = Math.log(size)*_LOG2SCALE;
       double exp = Math.ceil(m);
       return (int) exp;
    }

    /** Given an array of pole locations, an array of zero locations, and a
     *  gain term, return frequency response specified by these.
     *  This is calculated by walking around the unit circle and forming
     *  the product of the distances to the zeros, dividing by the product
     *  of the distances to the poles, and multiplying by the gain.
     *  The length of the returned array is <i>numsteps</i>.
     *
     *  @param poles An array of pole locations.
     *  @param zeros An array of zero locations.
     *  @param gain A complex gain.
     *  @param numsteps The number of samples in the returned
     *  frequency response.
     */
    public static Complex[] poleZeroToFreq(Complex[] poles, Complex[] zeros,
            Complex gain, int numsteps){
        double step = 2*Math.PI/numsteps;
        Complex[] freq = new Complex[numsteps];

        double angle = -Math.PI;
        for (int index = 0; index < freq.length; index++){
            Complex polescontrib = new Complex(1);
            Complex zeroscontrib = new Complex(1);
            Complex ejw = new Complex(Math.cos(angle), Math.sin(angle));
            if (poles.length > 0) {
                Complex[] diffpoles = ComplexArrayMath.subtract(poles, ejw);
                polescontrib = ComplexArrayMath.product(diffpoles);
            }
            if (zeros.length > 0) {
                Complex[] diffzeros = ComplexArrayMath.subtract(zeros, ejw);
                zeroscontrib = ComplexArrayMath.product(diffzeros);
            }
            freq[index] = zeroscontrib.divide(polescontrib);
            freq[index] = freq[index].multiply(gain);
            angle += step;
        }
        return freq;
    }

    /** Return the next power of two larger than the argument.
     *  @param x A positive real number.
     *  @exception IllegalArgumentException If the argument is less than
     *   or equal to zero. This is a runtime exception, so it need not be
     *   declared by the caller.
     */
    public static int powerOfTwo(double x) {
        if (x <= 0.0) {
            throw new IllegalArgumentException("SignalProcessing.powerOfTwo: "
                    + "argument is not a positive number: " + x);
        }
        double m = Math.log(x)*_LOG2SCALE;
        int exp = (int)Math.ceil(m);
        return 1 << exp;
    }

    /** Return a sample of a raised cosine pulse, or if the third
     *  argument is zero, a sin(x)/x function.  The argument <em>t</em> is
     *  the time of the sample (the pulse is centered at zero). The
     *  argument <em>T</em> is the time of the first zero crossing.
     *  This would be the symbol interval in a communications application
     *  of this pulse. The argument <em>excess</em> is the excess
     *  bandwidth, which is normally in the range of 0.0 to 1.0, corresponding
     *  to 0% to 100% excess bandwidth.
     *  <p>
     *  The function that is computed is:
     *  <p>
     *  <pre>
     *         sin(PI t/T)   cos(excess PI t/T)
     *  h(n) = ----------- * -----------------
     *          PI t/T      1-(2 excess t/T)<sup>2</sup>
     *  </pre>
     *  <p>
     *  This is called a "raised cosine pulse" because in the frequency
     *  domain its shape is that of a raised cosine.
     *  <p>
     *  This implementation is ported from the Ptolemy 0.x implementation
     *  by Joe Buck, Brian Evans, and Edward A. Lee.
     *  Reference: <a href=http://www.amazon.com/exec/obidos/ASIN/0792393910/qid%3D910596335/002-4907626-8092437>E. A. Lee and D. G. Messerschmitt,
     *  <i>Digital Communication, Second Edition</i>,
     *  Kluwer Academic Publishers, Boston, 1994.</a>
     *
     *  @param t The sample time.
     *  @param T The time of the first zero crossing.
     *  @param excess The excess bandwidth (in the range 0.0 to 1.0).
     *  @return A sample of a raised cosine pulse.
     */
    public static double raisedCosine(double t, double T, double excess) {
        if (t == 0.0) return 1.0;
        double x = t/T;
        double s = Math.sin(Math.PI * x) / (Math.PI * x);
        if (excess == 0.0) return s;
        x *= excess;
        double denominator = 1.0 - 4 * x * x;
        // If the denominator is close to zero, take it to be zero.
        if (close(denominator, 0.0)) {
            return s * Math.PI/4.0;
        }
        return s * Math.cos (Math.PI * x) / denominator;
    }

    /** Return a new array containing a raised cosine pulse, computed using
     *  the raisedCosine() method. The first argument is the time of the first
     *  sample.  The second is the number of desired samples.
     *  The third argument is the sample period.
     *  The fourth arguments is the time is the time of the first zero crossing
     *  (after time zero).
     *  This would be the symbol interval in a communications application
     *  of this pulse. The fifth argument is the excess
     *  bandwidth, which is normally in the range of 0.0 to 1.0, corresponding
     *  to 0% to 100% excess bandwidth.  Note that the pulse is centered
     *  at time zero.
     *  <p>
     *  For some applications, you may wish to apply a window function to
     *  this impulse response, since it is rather abruptly terminated
     *  at the two ends.
     *
     *  @param start The time of the first sample.
     *  @param length The number of desired samples.
     *  @param period The sample period.
     *  @param T The time of the first zero crossing.
     *  @param excess The excess bandwidth (in the range 0.0 to 1.0).
     *  @return An array containing a raised cosine pulse.
     */
    public static double[] raisedCosinePulse(double start, int length,
            double period, double T, double excess) {
        double[] result = new double[length];
        for (int i = 0; i < length; i++) {
            result[i] = raisedCosine(start + i*period, T, excess);
        }
        return result;
    }

    /** Return a sample of a sawtooth wave with the specified period and
     *  phase at the specified time.  The returned value ranges between
     *  -1.0 and 1.0.  The phase is given as a fraction of a cycle,
     *  typically ranging from 0.0 to 1.0.  If the phase is 0.0 or 1.0,
     *  the wave begins at zero with a rising slope.  If it is 0.5, it
     *  begins at the falling edge with value -1.0.
     *  If it is 0.25, it begins at +0.5.
     *
     *  @param period The period of the sawtooth wave.
     *  @param phase The phase of the sawtooth wave.
     *  @param time The time of the sample.
     *  @return A double in the range -1.0 to +1.0.
     */
    public static double sawtooth(double period, double phase, double time) {
        double point = ((time/period)+phase+0.5)%1.0;
        return 2.0*point-1.0;
    }

    /** Return sin(x)/x, the so-called sinc function.
     *  If the argument is very close to zero, significant quantization
     *  errors may result (exactly 0.0 is OK, since this just returns 1.0).
     *
     *  @param x A number.
     *  @return The sinc function.
     */
    public static double sinc(double x) {
        if (x == 0.0) return 1.0;
        return Math.sin(x) / x;
    }

    /** Return a sample of a square wave with the specified period and
     *  phase at the specified time.  The returned value is 1 or -1.
     *  A sample that falls on the rising edge of the square wave is
     *  assigned value +1.  A sample that falls on the falling edge is
     *  assigned value -1.  The phase is given as a fraction of a cycle,
     *  typically ranging from 0.0 to 1.0.  If the phase is 0.0 or 1.0,
     *  the square wave begins at the start of the +1.0 phase.  If it is 0.5, it
     *  begins at the start of the -1.0 phase. If it is 0.25, it begins halfway
     *  through the +1.0 portion of the wave.
     *
     *  @param period The period of the square wave.
     *  @param phase The phase of the square wave.
     *  @param time The time of the sample.
     *  @return +1.0 or -1.0.
     */
    public static double square(double period, double phase, double time) {
        double point = (time+(phase*period))%period;
        return (point < period/2.0)?1.0:-1.0;
    }

    /** Return a sample of a square-root raised cosine pulse.
     *  The function computed is:
     *  <p>
     *  <pre>
     *         4 x(cos((1+x)PI t/T) + T sin((1-x)PI t/T)/(4n x/T))
     *  h(t) = ---------------------------------------------------
     *                       PI sqrt(T)(1-(4 x t/T)<sup>2</sup>)
     *  </pre>
     *  <p>
     *  where <i>x</i> is the the excess bandwidth.
     *  This pulse convolved with itself will, in principle, be equal
     *  to a raised cosine pulse.  However, because the pulse decays rather
     *  slowly for low excess bandwidth, this ideal is not
     *  closely approximated by short finite approximations of the pulse.
     *  <p>
     *  This implementation is ported from the Ptolemy 0.x implementation
     *  by Joe Buck, Brian Evans, and Edward A. Lee.
     *  Reference: E. A. Lee and D. G. Messerschmitt,
     *  <i>Digital Communication, Second Edition</i>,
     *  Kluwer Academic Publishers, Boston, 1994.
     *
     *  @param t The time of the sample.
     *  @param T The time of the first zero crossing of the corresponding
     *   raised cosine pulse.
     *  @param excess The excess bandwidth of the corresponding
     *   raised cosine pulse.
     */
    public static double sqrtRaisedCosine(double t, double T, double excess) {
        double sqrtT = Math.sqrt(T);
        if (t == 0) {
            return ((4*excess/Math.PI) + 1 - excess)/sqrtT;
        }

        double x = t/T;
        if (excess == 0.0) {
            return sqrtT*Math.sin(Math.PI * x)/(Math.PI * t);
        }

        double oneplus = (1.0 + excess)*Math.PI/T;
        double oneminus = (1.0 - excess)*Math.PI/T;
        // Check to see whether we will get divide by zero.
        double denominator = t*t*16*excess*excess - T*T;
        if (close(denominator, 0.0)) {
            return (T * sqrtT/(8 * excess * Math.PI * t)) *
                (oneplus * Math.sin(oneplus * t) -
                        (oneminus * T/(4 * excess * t)) *
                        Math.cos(oneminus * t) +
                        (T/(4 * excess * t * t)) * Math.sin(oneminus * t) );
        }
        return (4 * excess / (Math.PI*sqrtT)) *
            (Math.cos(oneplus * t) + Math.sin(oneminus * t)/(x * 4 * excess)) /
            (1.0 - 16 * excess * excess * x * x);
    }

    /** Return a new array containing a square-root raised cosine pulse,
     *  computed using the sqrtRaisedCosine() method. The first
     *  argument is the time of the first sample. The second is
     *  the number of desired samples.  The third argument is the sample period.
     *  The fourth arguments is the time is the time of the first zero crossing
     *  (after time zero) of the corresponding raised-cosine pulse (the square).
     *  This would be the symbol interval in a communications application
     *  of this pulse. The fifth argument is the excess
     *  bandwidth, which is normally in the range of 0.0 to 1.0, corresponding
     *  to 0% to 100% excess bandwidth.  Note that the pulse is centered
     *  at time zero.
     *  <p>
     *  For some applications, you may wish to apply a window function to
     *  this impulse response, since it is rather abruptly terminated
     *  at the two ends.
     *
     *  @param start The time of the starting sample.
     *  @param length The number of desired samples.
     *  @param period The sample period.
     *  @param T The time of the first zero crossing.
     *  @param excess The excess bandwidth (in the range 0.0 to 1.0).
     *  @return An array containing a raised cosine pulse.
     */
    public static double[] sqrtRaisedCosinePulse(double start, int length,
            double period, double T, double excess) {
        double[] result = new double[length];
        for (int i = 0; i < length; i++) {
            result[i] = sqrtRaisedCosine(start + i*period, T, excess);
        }
        return result;
    }

    /** Return a sample of a triangle wave with the specified period and
     *  phase at the specified time.  The returned value ranges between
     *  -1.0 and 1.0.  The phase is given as a fraction of a cycle,
     *  typically ranging from 0.0 to 1.0.  If the phase is 0.0 or 1.0,
     *  the wave begins at zero with a rising slope.  If it is 0.5, it
     *  begins at zero with a falling slope. If it is 0.25, it begins at +1.0.
     *
     *  @param period The period of the triangle wave.
     *  @param phase The phase of the triangle wave.
     *  @param time The time of the sample.
     *  @return A number in the range -1.0 to +1.0.
     */
    public static double triangle(double period, double phase, double time) {
        double point = ((time/period)+phase+0.25)%1.0;
        return (point < 0.5)?(4.0*point-1.0):(((1.0-point)*4.0)-1.0);
    }

    /** Modify the specified array to unwrap the angles.
     *  That is, if the difference between successive values is greater than
     *  <em>PI</em> in magnitude, then the second value is modified by
     *  multiples of 2<em>PI</em> until the difference is less than <em>PI</em>.
     *  In addition, the first element is modified so that its difference from
     *  zero is less than <em>PI</em> in magnitude.  This method is used
     *  for generating more meaningful phase plots.
     */
    public void unwrap(double[] angles) {
        double previous = 0.0;
        for (int i = angles.length-1; i >= 0; i--) {
            while (angles[i] - previous < -Math.PI) {
                angles[i] += 2*Math.PI;
            }
            while (angles[i] - previous > -Math.PI) {
                angles[i] -= 2*Math.PI;
            }
            previous = angles[i];
        }
    }

    /////////////////////////////////////////////////////////////////////////
    ////                         public variables                        ////

    /** A small number. This number is used by algorithms to
     *  detect whether a double is close to zero.
     */
    public static double epsilon = 1.0e-9;

    // Scalefactor types for the DCT/IDCT
    /* To select the forward transform :
     *              N - 1
     *  X(k) = e(k)  sum  x[n] * cos ((2n + 1)k * PI / 2N)
     *              n = 0
     * and the inverse transform :
     *             N - 1
     *  x(n) =(2/N) sum  e(k) X[k] * cos ((2n + 1)k * PI / 2N)
     *             k = 0
     * with
     *  e(0) = 1/sqrt(2); e(k) = 1 for k != 0
     * use the following DCT type.
     */
    public static final int DCT_TYPE_NORMALIZED   = 0;

    /* To select the forward transform :
     *        N - 1
     *  X(k) = sum  x[n] * cos ((2n + 1)k * PI / 2N)
     *        n = 0
     * and the inverse transform :
     *        N - 1
     *  x(n) = sum  X[k] * cos ((2n + 1)k * PI / 2N)
     *        k = 0
     * use the following DCT type.
     */
    public static final int DCT_TYPE_UNNORMALIZED = 1;

    /* To select the forward transform :
     *                        N - 1
     *  X(k) = sqrt(2/N) e(k)  sum  x[n] * cos ((2n + 1)k * PI / 2N)
     *                        n = 0
     * and the inverse transform :
     *                 N - 1
     *  x(n) =sqrt(2/N) sum  e(k) X[k] * cos ((2n + 1)k * PI / 2N)
     *                 k = 0
     * with
     *  e(0) = 1/sqrt(2); e(k) = 1 for k != 0
     * use the following DCT type.
     */
    public static final int DCT_TYPE_ORTHONORMAL  = 2;
    public static final int DCT_TYPES             = 3; 

    /////////////////////////////////////////////////////////////////////////
    ////                         private methods                         ////

    // Check the input array for a transform. Throw an exception if the 
    // array is null or of zero length.
    private static void _checkTransformInput(Complex[] x) {
        if (x == null) {
           throw new IllegalArgumentException(
            "ptolemy.math.SignalProcessing : null array passed to " +
            "transform method.");           
        }
  
        if (x.length <= 0) {
           throw new IllegalArgumentException(
            "ptolemy.math.SignalProcessing : empty array passed to " +
            "transform method.");
        }
    }

    // Check the input array for a transform. Throw an exception if the 
    // array is null or of zero length.
    private static void _checkTransformInput(double[] x) {
        if (x == null) {
           throw new IllegalArgumentException(
            "ptolemy.math.SignalProcessing : null array passed to " +
            "transform method.");           
        }
  
        if (x.length <= 0) {
           throw new IllegalArgumentException(
            "ptolemy.math.SignalProcessing : empty array passed to " +
            "transform method.");
        }
    }

    // Check that the order of a transform is between 1 and 31, inclusive.
    // Throw an exception otherwise.
    private static void _checkTransformOrder(int order) {
        if (order < 1) {
           throw new IllegalArgumentException( 
            "ptolemy.math.SignalProcessing : order of transform must be "+
            "positive.");
        } else if (order > 31) {
           throw new IllegalArgumentException(
            "ptolemy.math.SignalProcessing : order of transform must be "+
            "less than 32.");
        }
    }

    // Check the arguments for a transform on an array of doubles, using
    // _checkTransformInput() and _checkTransformOrder(). Return an 
    // appropriately padded array on which to perform the transform.    
    private static double[] _checkTransformArgs(double[] x, int order) {
        _checkTransformInput(x);
        _checkTransformOrder(order);

        int size = 1 << order;

        // Zero pad the array if necessary
        if (x.length < size) {
           x = DoubleArrayMath.resize(x, size);
        } 
        return x;
    }
   
    // Check the arguments for a transform on an array of Complex's, using
    // _checkTransformInput() and _checkTransformOrder(). Return an 
    // appropriately padded array on which to perform the transform.    
    private static Complex[] _checkTransformArgs(Complex[] x, int order) {
        _checkTransformInput(x);
        _checkTransformOrder(order);

        int size = 1 << order;

        // Zero pad the array if necessary
        if (x.length < size) {
           x = ComplexArrayMath.resize(x, size);
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
       
          case 1:
            {
             double[] retval = new double[1];
             retval[0] = x[0];
             return retval;
            }
  
          case 2:
            {
             double[] retval = new double[2];
             retval[0] = x[0] + x[1];
             retval[1] = x[0] - x[1];
             return retval;
            }

          // Optimized base case for higher orders 
          case 4:
            {                   
             double[] retval = new double[3];
             retval[0] = x[0] + x[1] + x[2] + x[3];
             retval[1] = x[0] - x[2];
             retval[2] = x[0] - x[1] + x[2] - x[3];
             return retval;
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

        double[] retval = new double[halfN + 1];
        for (int k = 0; k < quarterN; k++) {
            retval[k] = halfCosDFT[k] + quarterDCT[k];
        }

        retval[quarterN] = halfCosDFT[quarterN];

        for (int k = quarterN + 1; k <= halfN; k++) {
            int idx = halfN - k;
            retval[k] = halfCosDFT[idx] - quarterDCT[idx];
        }

        return retval;
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
          case 4:
            {                   
             double[] retval = new double[2];
             // retval[0] = 0.0; // not necessary for Java,
                                 // also not read

             retval[1] = x[1] - x[3];
             return retval;
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
            x3[k] = ((k & 1) == 1) ? (x[size - twoIp] - x[twoIp]) :
                    (x[twoIp] - x[size - twoIp]);
        }

        double[] halfSinDFT = _sinDFT(x1, halfN, order - 1);
        double[] quarterDCT = _DCT(x3, quarterN, order - 2);

        double[] retval = new double[halfN];

        // retval[0] = 0.0; // not necessary in Java

        for (int k = 1; k < quarterN; k++) {
            retval[k] = halfSinDFT[k] + quarterDCT[quarterN - k];
        }

        retval[quarterN] = quarterDCT[0];

        for (int k = quarterN + 1; k < halfN; k++) {
            retval[k] = quarterDCT[k - quarterN] - halfSinDFT[halfN - k];
        }

        return retval;
    }

    private static double[] _DCT(double[] x, int size, int order) {

        double[] retval;

        if (size == 2) {
           retval = new double[2];
           retval[0] = x[0] + x[1];
           retval[1] = _ONEOVERROOT2 * (x[0] - x[1]);
           return retval;
        }

        int halfN = size >> 1;

        double[] x4 = new double[size];

        for (int n = 0; n < halfN; n++) {
            int twoN = n << 1;
            x4[n] = x[twoN];
            x4[size - n - 1] = x[twoN + 1];
        }

        double[] cosDFTarray = _cosDFT(x4, size, order);
        double[] sinDFTarray = _sinDFT(x4, size, order);

        double[] p1tab = _P1Table[order];
        double[] p2tab = _P2Table[order];
        double[] ctab  = _CTable[order];

        retval = new double[size];

        retval[0] = cosDFTarray[0];

        for (int k = 1; k < halfN; k++) {
            double m1 = (cosDFTarray[k] + sinDFTarray[k]) * ctab[k];
            double m2 = sinDFTarray[k] * p1tab[k];
            double m3 = cosDFTarray[k] * p2tab[k];
            retval[k] = m1 - m2;
            retval[size - k] = m1 + m3;
        }

        retval[halfN] = _ONEOVERROOT2 * cosDFTarray[halfN];

        return retval;
    }

    private synchronized static void _FFCTTableGen(int limit) {

        for (int i = _FFCTGenLimit; i <= limit; i++) {
            int N = 1 << i; // Watch out for this if i ever becomes > 31
            _P1Table[i] = new double[N];
            _P2Table[i] = new double[N];
            _CTable[i]  = new double[N];

            double p1t[] = _P1Table[i];
            double p2t[] = _P2Table[i];
            double ct[]  = _CTable[i];

            for (int k = 0; k < N; k++) {
                double arg = Math.PI * k / (2.0 * N);
                double c = Math.cos(arg);
                double s = Math.sin(arg);
                p1t[k] = c + s;
                p2t[k] = s - c;
                ct[k]  = c;
            }
        }
        _FFCTGenLimit = Math.max(_FFCTGenLimit, limit);
    }

    /////////////////////////////////////////////////////////////////////////
    ////                         private members                         ////

    // Tables needed for the FFCT algorithm. We assume no one will attempt
    // to do a transform of size greater than 2^31.
    private static final double _P1Table[][] = new double[32][];
    private static final double _P2Table[][] = new double[32][];
    private static final double _CTable[][]  = new double[32][];
    private static int _FFCTGenLimit = 0;

    // Table of scalefactors for the IDCT.
    private static final Complex _IDCTfactors[][][] = 
     new Complex[DCT_TYPES][32][];
    
    // Various constants
    private static final double _LOG10SCALE = 1.0 / Math.log(10.0);
    private static final double _LOG2SCALE  = 1.0 / Math.log(2.0);

    private static final double _ROOT2 = Math.sqrt(2.0);
    private static final double _ONEOVERROOT2 = 1.0 / _ROOT2;
}


