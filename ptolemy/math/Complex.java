/* A data type representing a complex number.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.math;

import  java.io.Serializable;

/** This class provides a complex data type and a library of functions that
 *  operate on and return complex numbers.  An instance of the class is
 *  immutable, meaning that its value is set in the constructor and
 *  cannot then be modified.  This is similar to the Java built-in classes
 *  like Double, Integer, etc.
 *  <p>
 *  Although this code is written from scratch, I looked at several designs
 *  and borrowed elements from each of them:
 *  <ul>
 *  <li> The ComplexSubset class in Ptolemy 0.x, written by Joe Buck,
 *       which borrowed design elements from the cfront and libg++
 *       Complex classes.
 *  <li> Version 1.0 of the Complex class by Alma services, dated Fri
 *       29-Aug-97, written by Sandy Anderson and Priyantha Jayanetti,
 *       and obtained from the <a href="http://www.netlib.org/">
 *       Netlib Repository</a>.
 *  <li> The Complex class in JNL, a Java Numerical Library, dated 1997,
 *       by Visual Numerics, Inc.  This was used for interface design only,
 *       to compare the choice of method names and method templates.
 *  <li> Matlab, which was used to validate the results in the test
 *       suite, and influenced some of the method name choices.
 *  </ul>
 *
 *  @author Edward A. Lee, Jeff Tsay
 *  @version $Id$
 */
public class Complex implements Cloneable, Serializable {

    /** Construct a Complex equal to zero.
     *  @deprecated Use Complex.ZERO instead.
     */
    public Complex() {
        this(0.0, 0.0);
    }

    /** Construct a Complex with a zero imaginary part.
     *  @param  real The real part.
     */
    public Complex(double real) {
        this(real, 0.0);
    }

    /** Construct a Complex with the specified real and imaginary parts.
     *  @param real The real part.
     *  @param imag The imaginary part.
     */
    public Complex(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    // NOTE: There is no need for a constructor that takes a Complex
    // argument because instances of this class are immutable.  There
    // is never a need to make another instance with the same value.

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new complex number with value equal to the sum
     *  of this complex number and the argument.
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public final Complex add(Complex z) {
        return new Complex(real + z.real, imag + z.imag);
    }

    /** Return a new complex number with value equal to the principal arc cosine
     *  of this complex number.  This is defined by:
     *  <pre>
     *   acos(z) = -i * log(z + i*sqrt(1 - z*z))
     *  </pre>
     *  where <code>z</code> is this complex number.
     *  @return A new complex number.
     */
    public final Complex acos() {
        Complex c1 = new Complex(1.0-real*real+imag*imag, -2.0*real*imag);
        Complex c2 = c1.sqrt();
        Complex c3 = new Complex(real - c2.imag, imag + c2.real);
        Complex c4 = c3.log();
        return new Complex(c4.imag, -c4.real);
    }

    /** Return a new complex number with value equal to the principal
     *  hyperbolic arc cosine of this complex number.  This is defined by:
     *  <pre>
     *   acosh(z) = log(z + sqrt(z*z - 1))
     *  </pre>
     *  where <code>z</code> is this complex number.
     */
    public final Complex acosh() {
        Complex c1 = new Complex(real*real-imag*imag-1.0, 2.0*real*imag);
        Complex c2 = c1.sqrt();
        Complex c3 = add(c2);
        return c3.log();
    }

    /** Return the angle or argument of this complex number.
     *  @return A number in the range -<em>pi < /em> to <em>pi</em>.
     */
    public final double angle() {
        return  Math.atan2(imag, real);
    }

    /** Return a new complex number with value equal to the principal arc sine
     *  of this complex number.  This is defined by:
     *  <pre>
     *   asin(z) = -i * log(i*z + sqrt(1 - z*z))
     *  </pre>
     *  where <code>z</code> is this complex number.
     *  @return A new complex number.
     */
    public final Complex asin() {
        Complex c1 = new Complex(1.0-real*real+imag*imag, -2.0*real*imag);
        Complex c2 = c1.sqrt();
        Complex c3 = new Complex(c2.real-imag, c2.imag+real);
        Complex c4 = c3.log();
        return new Complex(c4.imag, -c4.real);
    }

    /** Return a new complex number with value equal to the principal
     *  hyperbolic arc sine of this complex number.  This is defined by:
     *  <pre>
     *   asinh(z) = log(z + sqrt(z*z + 1))
     *  </pre>
     *  where <code>z</code> is this complex number.
     */
    public final Complex asinh() {
        Complex c1 = new Complex(1.0+real*real-imag*imag, 2.0*real*imag);
        Complex c2 = c1.sqrt();
        Complex c3 = add(c2);
        return c3.log();
    }

    /** Return a new complex number with value equal to the principal arc
     *  tangent of this complex number.  This is defined by:
     *  <pre>
     *  atan(z) = -i/2 * log((i-z)/(i+z))
     *  </pre>
     *  where <code>z</code> is this complex number.
     */
    public final Complex atan() {
        double denom = real*real+(imag+1.0)*(imag+1.0);
        Complex c1 = new Complex((-real*real-imag*imag+1.0)/denom,
                2.0*real/denom);
        Complex c2 = c1.log();
        return new Complex(c2.imag*0.5, -c2.real*0.5);
    }

    /** Return a new complex number with value equal to the principal
     *  hyperbolic arc tangent of this complex number.  This is defined by:
     *  <pre>
     *   atanh(z) = 1/2 * log((1+z)/(1-z))
     *  </pre>
     *  where <code>z</code> is this complex number.
     */
    public final Complex atanh() {
        double denom = (1.0-real)*(1.0-real)+imag*imag;
        Complex c1 = new Complex((-real*real-imag*imag+1.0)/denom,
                2.0*imag/denom);
        Complex c2 = c1.log();
        return new Complex(c2.real*0.5, c2.imag*0.5);
    }

    /** Return a new Complex with value equal to the complex conjugate of
     *  of this complex number.
     */
    public final Complex conjugate() {
        // Avoid negative zero.
        if (imag != 0.0) return new Complex(real, -imag);
        
        return new Complex(real, imag);
    }

    /** Return a new complex number with value equal to the cosine
     *  of this complex number.  This is defined by:
     *  <pre>
     *  cos(z) = (exp(i*z) + exp(-i*z))/2
     *  </pre>
     *  where <code>z</code> is this complex number.
     */
    public final Complex cos() {
        Complex c1 = new Complex(-imag, real);
        Complex c2 = c1.exp();
        Complex c3 = new Complex(imag, -real);
        Complex c4 = c2.add(c3.exp());
        return new Complex(c4.real*0.5, c4.imag*0.5);
    }

    /** Return a new complex number with value equal to the hyperbolic cosine
     *  of this complex number.  This is defined by:
     *  <pre>
     *  cosh(z) = (exp(z) + exp(-z))/2
     *  </pre>
     *  where <code>z</code> is this complex number.
     *  @return A new complex number.
     */
    public final Complex cosh() {
        Complex c1 = exp();
        Complex c2 = new Complex(-real, -imag);
        Complex c3 = c1.add(c2.exp());
        return new Complex(c3.real*0.5, c3.imag*0.5);
    }

    /** Return a new complex number with value equal to the cotangent
     *  of this complex number.  This is simply:
     *  <pre>
     *  cot(z) = 1/tan(z)
     *  </pre>
     *  where <code>z</code> is this complex number.
     *  @return A new complex number.
     */
    public final Complex cot() {
        Complex c1 = tan();
        return c1.reciprocal();
    }

    /** Return a new complex number with value equal to the cosecant
     *  of this complex number.  This is simply:
     *  <pre>
     *  csc(z) = 1/sin(z)
     *  </pre>
     *  where <code>z</code> is this complex number.
     *  @return A new complex number.
     */
    public Complex csc() {
        Complex c1 = sin();
        return c1.reciprocal();
    }

    /** Divide this complex number by the argument, and return the result
     *  in a new Complex object.
     *  @param divisor The denominator in the division.
     *  @return A new complex number.
     */
    public final Complex divide(Complex divisor) {
        // This algorithm results from writing a/b as (ab*)/magSquared(b).
        double denom = divisor.magnitudeSquared();
        return new Complex((real*divisor.real+imag*divisor.imag)/denom,
                (imag*divisor.real-real*divisor.imag)/denom);
    }

    /** Return true if the real and imaginary parts of this complex number
     *  are equal to those of the argument.
     *  @return True if the real and imaginary parts are equal.
     */
    public final boolean equals(Complex z) {
        return (z.real == real && z.imag == imag);
    }

    /** Return a new complex number with value equal to the exponential
     *  of this complex number, or <em>e<sup>z</sup></em>,
     *  where <code>z</code> is this complex number.
     *  @param z A complex exponent.
     *  @return A new complex number.
     */
    public final Complex exp() {
        double magnitude =  Math.exp(real);
        return polarToComplex(magnitude, imag);
    }

    /** Return true if either the real or imaginary part is infinite.
     *  This is determined by the isInfinite() method of the java.lang.Double
     *  class.
     *  @return True if this is infinite.
     */
    public final boolean isInfinite() {
        return  ( Double.isInfinite(real) || Double.isInfinite(imag) );
    }

    /** Return true if either the real or imaginary part is NaN.  NaN means
     *  "not a number," per the IEEE floating point standard.
     *  This is determined by the isNaN() method of the java.lang.Double
     *  class.
     *  @return True if this is NaN.
     */
    public final boolean isNaN() {
        return  ( Double.isNaN(real) || Double.isNaN(imag) );
    }

    /** Return a new complex number with value equal to the natural logarithm
     *  of this complex number.  The principal value is returned, which
     *  is
     *  <pre>
     *  log(z) = log(mag(z)) + i * angle(z)
     *  </pre>
     *  where <code>z</code> is this complex number.
     *  @return A new complex number.
     */
    public final Complex log() {
        return  new Complex( Math.log(magnitude()), angle() );
    }

    /** Return the magnitude or absolute value of this complex number.
     *  @return A non-negative number.
     */
    public final double magnitude() {
        return Math.sqrt(magnitudeSquared());
    }

    /** Return the square of the magnitude of this complex number.
     *  This is provided for efficiency, since it is considerably easier
     *  to compute than the magnitude (which is the square root of this
     *  result).
     *  @return A non-negative number.
     */
    public double magnitudeSquared() {
        return (real*real) + (imag*imag);
    }

    /** Return a new complex number that is formed by multiplying this
     *  complex number by the specified complex number.
     *  @param w A complex number.
     *  @return A new complex number.
     *  @see Complex#scale
     */
    public Complex multiply(Complex w) {
        return new Complex(w.real*real-w.imag*imag, w.real*imag+w.imag*real);
    }

    /** Return a new complex number that is formed by the negatives of both
     *  the real and imaginary parts of this complex number.
     */
    public final Complex negate() {
        // Avoid negative zero.
        double r = 0.0;
        double i = 0.0;
        if (real != 0.0) r = -real;
        if (imag != 0.0) i = -imag;
        return new Complex(r, i);
    }

    /** Return a new complex number with the specified magnitude and angle.
     *  @param magnitude The magnitude.
     *  @param angle The angle.
     *  @return A new complex number.
     */
    public static final Complex polarToComplex(double magnitude,
            double angle) {
        if (magnitude < 0.0) {
            angle +=  Math.PI;
            magnitude = -magnitude;
        }
        if (magnitude == 0.0) return Complex.ZERO;
        return  new Complex(magnitude * Math.cos(angle),
                magnitude * Math.sin(angle));
    }

    /** Return a new complex number with value <em>z <sup>y</sup></em>
     *  where <em>z</em> is this complex number and <em>y</em> is the
     *  argument, a double.
     *  @param y A double.
     *  @return A new complex number.
     */
    public Complex pow(double y) {
        // This formula follows from expanding the input form
        //     (rho e^(j theta))^(c + dj)
        // to something of the form ae^jb.

        double lnrho =  Math.log(magnitude());
        double magnitude = Math.exp(lnrho * y);
        double angle =  angle() * y;
        return polarToComplex(magnitude, angle);
    }

    /** Return a new complex number with value <em>z<sup>y</sup></em>
     *  where <em>z</em> is this complex number and <em>y</em> is the
     *  argument, a Complex.
     *  @param y A complex number.
     *  @return A new complex number.
     */
    public final Complex pow(Complex y) {
        // This formula follows from expanding the input form
        //     (rho e^(j theta))^(c + dj)
        // to something of the form ae^jb.

        double lnrho =  Math.log(magnitude());
        double theta =  angle();
        double magnitude = Math.exp((lnrho*y.real) - (theta*y.imag));
        double angle =  (lnrho*y.imag) + (theta*y.real);
        return polarToComplex(magnitude, angle);
    }

    /** Return a new complex number that is the reciprocal of this one.
     *  @return A new complex number.
     */
    public final Complex reciprocal() {
        // This algorithm results from writing 1/a as (a*)/|a|^2.
        double magSquared = magnitudeSquared();
        return new Complex(real/magSquared, -imag/magSquared);
    }

    /** Return the nth roots of this complex number in an array. There are
     *  n of them, computed by :
     *  <p>
     *  r<sup>1/n</sup>(cos((theta + 2kPI) / n + i sin((theta + 2kPI)/n)
     *  </p>
     *  where k is the index of the returned array. If n is not greater than or
     *  equal to one, throw a IllegalArgumentException.
     *  @param n An integer that must be greater than or equal to one.
     *  @return An array of Complex numbers, of length n.
     */
    public final Complex[] roots(int n) {
        if (n < 1) {
           throw new IllegalArgumentException("Complex.roots() : n must be greater " +
            "than or equal to one.");
        }
        
        Complex[] retval = new Complex[n];
        
        double oneOverN = 1.0 / (double) n;
        double twoPIOverN = 2.0 * Math.PI * oneOverN;
        double thetaOverN = angle() * oneOverN;
        double twoPIkOverN = 0.0;
        
        // r^(1/n) = (r^2)^(0.5 / n)
        double retMag = Math.pow(magnitudeSquared(), 0.5 * oneOverN);
        
        for (int k = 0; k < n; k++) {
            retval[k] = polarToComplex(retMag, thetaOverN + twoPIkOverN);
            twoPIkOverN += twoPIOverN;
        }
        
        return retval;
    }                        

    /** Return a new complex number with value equal to the product
     *  of this complex number and the real argument.
     *  @param scalar A real number.
     *  @return A new complex number.
     *  @see Complex#multiply
     */
    public final Complex scale(double scalar) {
        return new Complex(real*scalar, imag*scalar);
    }

    /** Return a new complex number with value equal to the secant
     *  of this complex number.  This is simply:
     *  <pre>
     *  sec(z) = 1/cos(z)
     *  </pre>
     *  where <code>z</code> is this complex number.
     *  @return A new complex number.
     */
    public Complex sec() {
        Complex c1 = cos();
        return c1.reciprocal();
    }

    /** Return a new complex number with value equal to the sine
     *  of this complex number.  This is defined by:
     *  <pre>
     *  sin(z) = (exp(i*z) - exp(-i*z))/(2*i)
     *  </pre>
     *  where <code>z</code> is this complex number.
     *  @return A new complex number.
     */
    public final Complex sin() {
        Complex c1 = new Complex(-imag, real);
        Complex c2 = c1.exp();
        Complex c3 = new Complex(imag, -real);
        Complex c4 = c2.subtract(c3.exp());
        return new Complex(c4.imag*0.5, -c4.real*0.5);
    }

    /** Return a new complex number with value equal to the hyperbolic sine
     *  of this complex number.  This is defined by:
     *  <pre>
     *  sinh(z) = (exp(z) - exp(-z))/2
     *  </pre>
     *  where <code>z</code> is this complex number.
     *  @return A new complex number.
     */
    public final Complex sinh() {
        Complex c1 = exp();
        Complex c2 = new Complex(-real, -imag);
        Complex c3 = c1.subtract(c2.exp());
        return new Complex(c3.real*0.5, c3.imag*0.5);
    }

    /** Return a new complex number with its value equal to the
     *  the square root of this complex number.
     *  The square root is defined to be:
     *  <pre>
     *  sqrt(z) = sqrt(mag(z))*(cos(angle(z)/2) + i * sin(angle(z)/2) )
     *  </pre>
     *  where <code>z</code> is this complex number.
     */
    public final Complex sqrt() {
        double magnitude = Math.sqrt(magnitude());
        double angle = angle()*0.5;
        return polarToComplex(magnitude, angle);
    }

    /** Return a new complex number formed by subtracting the specified
     *  complex number from this complex number.
     */
    public final Complex subtract(Complex w) {
        return new Complex(real-w.real, imag-w.imag);
    }

    /** Return a new complex number with value equal to the tangent
     *  of this complex number.  This is defined by:
     *  <pre>
     *  tan(z) = sin(z)/cos(z)
     *  </pre>
     *  where <code>z</code> is this complex number.
     */
    public final Complex tan() {
        Complex c1 = sin();
        return c1.divide(cos());
    }

    /** Return a new complex number with value equal to the hyperbolic tangent
     *  of this complex number.  This is defined by:
     *  <pre>
     *  tanh(z) = sinh(z)/cosh(z)
     *  </pre>
     *  where <code>z</code> is this complex number.
     */
    public final Complex tanh() {
        Complex c1 = sinh();
        return c1.divide(cosh());
    }

    /** Return a string representation of this Complex.
     * @return A string of the form "<em>x</em> + <em>y</em>i".
     */
    public final String toString() {
        if (imag >= 0) {
            return Double.toString(real) + " + " + Double.toString(imag) + "i";
        } else {
            return Double.toString(real) + " - " + Double.toString(-imag) + "i";
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The real part. This is a "blank final," which means that it
     *  can only be set in the constructor.
     */
    public final double real;

    /** The imaginary part. This is a "blank final," which means that it
     *  can only be set in the constructor.
     */
    public final double imag;

    /** A Complex number representing zero. Reference this to save
     *  memory usage and construction overhead.
     */
    public static final Complex ZERO = new Complex(0.0, 0.0);

    /** A Complex number representing one. Reference this to save
     *  memory usage and construction overhead.
     */
    public static final Complex ONE = new Complex(1.0, 0.0);

    /** A Complex number representing <i>i</i>. Reference this to save
     *  memory usage and construction overhead.
     */
    public static final Complex I = new Complex(0.0, 1.0);
}
