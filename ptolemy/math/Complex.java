/* A complex data type.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.math;

import  java.io.Serializable;

/** This class provides a complex data type and a library of functions that
 *  operate on and return complex data.  Methods that modify an instance of
 *  the complex number (vs. creating a new one) are also generally available
 *  as static methods that return a new one.  Some of the
 *  more esoteric methods are available only as static methods.
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
 *  @author Edward A. Lee
 *  @version $Id$
 */
public final class Complex implements Cloneable, Serializable {

    /** Construct a Complex equal to zero.
     */
    public Complex () {
        this(0.0, 0.0);
    }

    /** Construct a Complex with a zero imaginary part.
     *  @param  real The real part.
     */
    public Complex (double real) {
        this(real, 0.0);
    }

    /** Construct a Complex with the specified real and imaginary parts.
     *  @param real The real part.
     *  @param imag The imaginary part.
     */
    public Complex (double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                          public methods                        ////

    /** Return the magnitude or absolute value of this complex number.
     *  This is the square root of the norm.
     *  @return A non-negative number.
     */
    public double abs () {
        return Math.sqrt(norm());
    }

    /** Add the specified complex number to this complex number.
     *  @param w A complex number.
     */
    public void add (Complex w) {
        real += w.real;
        imag += w.imag;
    }

    /** Return a new complex number with value equal to the sum
     *  of the two complex arguments.
     *  @param z A complex number.
     *  @param w A complex number.
     *  @return A new complex number.
     */
    public static Complex add (Complex z, Complex w) {
        Complex result = new Complex(z.real, z.imag);
        result.add(w);
        return result;
    }

    /** Return a new complex number with value equal to the principal arc cosine
     *  of the complex argument.  This is defined by:
     *  <pre>
     *   acos(z) = -i * log(z + i*sqrt(1 - z*z))
     *  </pre>
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public static Complex acos (Complex z) {
        Complex c1 = new Complex(z.real, z.imag);
        c1.multiply(c1);
        Complex c2 = new Complex(1.0, 0.0);
        c2.subtract(c1);
        c2.sqrt();
        c2.multiply(new Complex(0.0, 1.0));
        c2.add(z);
        Complex result = log(c2);
        result.multiply(new Complex(0.0, -1.0));
        return result;
    }

    /** Return a new complex number with value equal to the principal
     *  hyperbolic arc cosine of the complex argument.  This is defined by:
     *  <pre>
     *   asin(z) = log(z + sqrt(z*z - 1))
     *  </pre>
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public static Complex acosh (Complex z) {
        Complex c1 = new Complex(z.real, z.imag);
        c1.multiply(c1);
        c1.subtract(new Complex(1.0, 0.0));
        c1.sqrt();
        c1.add(z);
        Complex result = log(c1);
        return result;
    }

    /** Return the angle or argument of this complex number.
     *  @return A number in the range -<em>pi</em> to <em>pi</em>.
     */
    public double angle () {
        return  Math.atan2(imag, real);
    }

    /** Return a new complex number with value equal to the principal arc sine
     *  of the complex argument.  This is defined by:
     *  <pre>
     *   asin(z) = -i * log(i*z + sqrt(1 - z*z))
     *  </pre>
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public static Complex asin (Complex z) {
        Complex c1 = new Complex(z.real, z.imag);
        c1.multiply(c1);
        Complex c2 = new Complex(1.0, 0.0);
        c2.subtract(c1);
        c2.sqrt();
        c2.add(new Complex(-z.imag, z.real));
        Complex result = log(c2);
        result.multiply(new Complex(0.0, -1.0));
        return result;
    }

    /** Return a new complex number with value equal to the principal
     *  hyperbolic arc sine of the complex argument.  This is defined by:
     *  <pre>
     *   asin(z) = log(z + sqrt(z*z + 1))
     *  </pre>
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public static Complex asinh (Complex z) {
        Complex c1 = new Complex(z.real, z.imag);
        c1.multiply(c1);
        c1.add(new Complex(1.0, 0.0));
        c1.sqrt();
        c1.add(z);
        Complex result = log(c1);
        return result;
    }

    /** Return a new complex number with value equal to the principal arc
     *  tangent of the complex argument.  This is defined by:
     *  <pre>
     *  atan(z) = -i/2 * log((i-z)/(i+z))
     *  </pre>
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public static Complex atan (Complex z) {
        Complex c1 = new Complex(0.0, 1.0);
        c1.subtract(z);
        Complex c2 = new Complex(0.0, 1.0);
        c2.add(z);
        c1.divide(c2);
        Complex result = log(c1);
        result.multiply(new Complex(0.0, -0.5));
        return result;
    }

    /** Return a new complex number with value equal to the principal
     *  hyperbolic arc tangent of the complex argument.  This is defined by:
     *  <pre>
     *   asin(z) = 1/2 * log((1+z)/(1-z))
     *  </pre>
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public static Complex atanh (Complex z) {
        Complex c1 = new Complex(1.0, 0.0);
        c1.add(z);
        Complex c2 = new Complex(1.0, 0.0);
        c2.subtract(z);
        c1.divide(c2);
        Complex result = log(c1);
        result.multiply(new Complex(0.5, 0.0));
        return result;
    }

    /** Return a new complex number with value equal to the cotangent
     *  of the complex argument.  This is simply:
     *  <pre>
     *  cot(z) = 1/tan(z)
     *  </pre>
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public static Complex cot (Complex z) {
        Complex result = tan(z);
        result.reciprocal();
        return result;
    }

    /** Return a new complex number with value equal to the cosecant
     *  of the complex argument.  This is simply:
     *  <pre>
     *  csc(z) = 1/sin(z)
     *  </pre>
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public static Complex csc (Complex z) {
        Complex result = sin(z);
        result.reciprocal();
        return result;
    }

    /** Conjugate this complex number.
     */
    public void conjugate () {
        // Avoid negative zero.
        if (imag != 0.0) imag = -imag;
    }

    /** Return a new Complex with value equal to the complex-conjugate of
     *  of the argument.
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public static Complex conjugate (Complex z) {
        // Avoid negative zero.
        if (z.imag != 0.0) return  new Complex(z.real, -z.imag);
        else return new Complex(z.real, z.imag);
    }

    /** Return a new complex number with value equal to the cosine
     *  of the complex argument.  This is defined by:
     *  <pre>
     *  cos(z) = (exp(i*z) + exp(-i*z))/2
     *  </pre>
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public static Complex cos (Complex z) {
        Complex c1 = new Complex(-z.imag, z.real);
        Complex result = exp(c1);
        Complex c2 = new Complex(z.imag, -z.real);
        result.add(exp(c2));
        result.scale(0.5);
        return result;
    }

    /** Return a new complex number with value equal to the hyperbolic cosine
     *  of the complex argument.  This is defined by:
     *  <pre>
     *  cosh(z) = (exp(z) + exp(-z))/2
     *  </pre>
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public static Complex cosh (Complex z) {
        Complex c1 = new Complex(z.real, z.imag);
        Complex result = exp(c1);
        Complex c2 = new Complex(-z.real, -z.imag);
        result.add(exp(c2));
        Complex denom = new Complex(2.0, 0.0);
        result.divide(denom);
        return result;
    }

    /** Divide this complex number by the argument, replacing its
     *  value with the result.
     *  @param divisor The denominator in the division.
     */
    public void divide (Complex divisor) {
        // This algorithm results from writing a/b as (ab*)/norm(b).
        double norm = divisor.norm();
        double re = (real*divisor.real + imag*divisor.imag)/norm;
        double im = (divisor.real*imag - real*divisor.imag)/norm;
        real = re;
        imag = im;
    }

    /** Divide the first argument by the second, and return the result
     *  in a new Complex object.
     *  @param dividend The numerator in the division.
     *  @param divisor The denominator in the division.
     *  @return A new complex number.
     */
    public static Complex divide (Complex dividend, Complex divisor) {
        Complex result = new Complex(dividend.real, dividend.imag);
        result.divide(divisor);
        return result;
    }

    /** Return a new complex number with value equal to the exponential
     *  of the argument, or <em>e<sup>z</sup></em>.
     *  @param z A complex exponent.
     *  @return A new complex number.
     */
    public static Complex exp (Complex z) {
        double magnitude =  Math.exp(z.real);
        return polarToComplex(magnitude, z.imag);
    }

    /** Return true if either the real or imaginary part is infinite.
     *  This is determined by the isInfinite method of the java.lang.Double
     *  class.
     *  @return True if this is infinite.
     */
    public boolean isInfinite () {
        return  ( Double.isInfinite(real) || Double.isInfinite(imag) );
    }

    /** Return true if either the real or imaginary part is NaN.  NaN means
     *  not a number, per the IEEE floating point standard.
     *  This is determined by the isNaN method of the java.lang.Double
     *  class.
     *  @return True if this is NaN.
     */
    public boolean isNaN () {
        return  ( Double.isNaN(real) || Double.isNaN(imag) );
    }

    /** Return a new Complex with value equal to the natural logarithm
     *  of the complex argument.  The principal value is returned, which
     *  is
     *  <pre>
     *  log(z) = log(abs(z)) + i * angle(z)
     *  </pre>
     *  @param z A complex number
     *  @return A new complex number.
     */
    public static Complex log (Complex z) {
        return  new Complex( Math.log(z.abs()), z.angle() );
    }

    /** Multiply this complex number by the specified complex number.
     *  @param w A complex number.
     *  @see scale
     */
    public void multiply (Complex w) {
        double re = w.real*real - w.imag*imag;
        imag = w.real*imag + w.imag*real;
        real = re;
    }

    /** Return a new complex number with value equal to the product
     *  of the two complex arguments.
     *  @param z A complex number.
     *  @param w A complex number.
     *  @return A new complex number.
     *  @see scale
     */
    public static Complex multiply (Complex z, Complex w) {
        Complex result = new Complex(z.real, z.imag);
        result.multiply(w);
        return result;
    }

    /** Negate both the real and imaginary parts of this complex number.
     */
    public void negate () {
        // Avoid negative zero.
        if (real != 0.0) real = -real;
        if (imag != 0.0) imag = -imag;
    }

    /** Return a new Complex with value equal to the negative of the argument.
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public static Complex negate (Complex z) {
        // Avoid negative zero.
        double re = z.real;
        double im = z.imag;
        if (re != 0.0) re = -re;
        if (im != 0.0) im = -im;
        return  new Complex(re, im);
    }

    /** Return the square of the magnitude of this complex number.
     *  @return A non-negative number.
     */
    public double norm () {
        return (real*real) + (imag*imag);
    }

    /** Return a new Complex with the specified magnitude and angle.
     *  @param magnitude The magnitude.
     *  @param angle The angle.
     *  @return A new complex number.
     */
    public static Complex polarToComplex (double magnitude, double angle) {
        if (magnitude < 0.0) {
            angle +=  Math.PI;
            magnitude = -magnitude;
        }
        if (magnitude == 0.0) return new Complex(0.0, 0.0);
        return  new Complex(magnitude * Math.cos(angle),
                magnitude * Math.sin(angle));
    }

    /** Return <em>x<sup>y</sup></em>.
     *  @param x A complex number.
     *  @param y A complex number.
     *  @return A complex number.
     */
    public static Complex pow (Complex x, Complex y) {
        // This formula follows from expanding the input form
        //     (rho e^(j theta))^(c + dj)
        // to something of the form ae^jb.

        double lnrho =  Math.log(x.abs());
        double theta =  x.angle();
        double magnitude = Math.exp((lnrho*y.real) - (theta*y.imag));
        double angle =  (lnrho*y.imag) + (theta*y.real);
        return polarToComplex(magnitude, angle);
    }

    /** Replace the value of this complex number with its reciprocal.
     */
    public void reciprocal () {
        // This algorithm results from writing 1/a as (a*)/norm(a).
        double norm = norm();
        real = real/norm;
        imag = -imag/norm;
    }

    /** Return a new Complex object that is the reciprocal of the argument.
     *  @param z A complex number
     *  @return A new complex number, 1/z.
     */
    public static Complex reciprocal (Complex z) {
        Complex result = new Complex(z.real, z.imag);
        result.reciprocal();
        return result;
    }

    /** Multiply this complex number by the specified real scalar.
     *  @param scalar A real number.
     *  @see multiply
     */
    public void scale (double scalar) {
        real = scalar*real;
        imag = scalar*imag;
    }

    /** Return a new complex number with value equal to the product
     *  of the complex and real arguments.
     *  @param z A complex number.
     *  @param scalar A real number.
     *  @return A new complex number.
     *  @see multiply
     */
    public static Complex scale (Complex z, double scalar) {
        Complex result = new Complex(z.real, z.imag);
        result.scale(scalar);
        return result;
    }

    /** Return a new complex number with value equal to the secant
     *  of the complex argument.  This is simply:
     *  <pre>
     *  sec(z) = 1/cos(z)
     *  </pre>
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public static Complex sec (Complex z) {
        Complex result = cos(z);
        result.reciprocal();
        return result;
    }

    /** Return a new complex number with value equal to the sine
     *  of the complex argument.  This is defined by:
     *  <pre>
     *  sin(z) = (exp(i*z) - exp(-i*z))/(2*i)
     *  </pre>
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public static Complex sin (Complex z) {
        Complex c1 = new Complex(-z.imag, z.real);
        Complex result = exp(c1);
        Complex c2 = new Complex(z.imag, -z.real);
        result.subtract(exp(c2));
        Complex denom = new Complex(0.0, 2.0);
        result.divide(denom);
        return result;
    }

    /** Return a new complex number with value equal to the hyperbolic sine
     *  of the complex argument.  This is defined by:
     *  <pre>
     *  sinh(z) = (exp(z) - exp(-z))/2
     *  </pre>
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public static Complex sinh (Complex z) {
        Complex c1 = new Complex(z.real, z.imag);
        Complex result = exp(c1);
        Complex c2 = new Complex(-z.real, -z.imag);
        result.subtract(exp(c2));
        Complex denom = new Complex(2.0, 0.0);
        result.divide(denom);
        return result;
    }

    /** Replace the value of this complex number with its square root.
     *  The square root is defined to be:
     *  <pre>
     *  sqrt(z) = sqrt(abs(z))*(cos(angle(z)/2) + i * sin(angle(z)/2) )
     *  </pre>
     */
    public void sqrt () {
        double magnitude = Math.sqrt(abs());
        double angle = angle()/2;
        Complex c = polarToComplex(magnitude, angle);
        real = c.real;
        imag = c.imag;
    }

    /** Return a new complex number with its value equal to the
     *  the square root of the argument.
     *  The square root is defined to be:
     *  <pre>
     *  sqrt(z) = sqrt(abs(z))*(cos(angle(z)/2) + i * sin(angle(z)/2) )
     *  </pre>
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public static Complex sqrt (Complex z) {
        double magnitude = Math.sqrt(z.abs());
        double angle = z.angle()/2;
        return polarToComplex(magnitude, angle);
    }

    /** Subtract the specified complex number from this complex number.
     *  @param w A complex number.
     */
    public void subtract (Complex w) {
        real -= w.real;
        imag -= w.imag;
    }

    /** Return a new complex number with value equal to the first
     *  complex argument minus the second.
     *  @param z A complex number.
     *  @param w A complex number.
     *  @return A new complex number.
     */
    public static Complex subtract (Complex z, Complex w) {
        Complex result = new Complex(z.real, z.imag);
        result.subtract(w);
        return result;
    }

    /** Return a new complex number with value equal to the tangent
     *  of the complex argument.  This is defined by:
     *  <pre>
     *  tan(z) = sin(z)/cos(z)
     *  </pre>
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public static Complex tan (Complex z) {
        Complex result = sin(z);
        result.divide(cos(z));
        return result;
    }

    /** Return a new complex number with value equal to the hyberbolic tangent
     *  of the complex argument.  This is defined by:
     *  <pre>
     *  tan(z) = sinh(z)/cosh(z)
     *  </pre>
     *  @param z A complex number.
     *  @return A new complex number.
     */
    public static Complex tanh (Complex z) {
        Complex result = sinh(z);
        result.divide(cosh(z));
        return result;
    }

    /** Return a string representation of this Complex.
     * @return A string of the form "<em>x</em> + <em>y</em>i".
     */
    public String toString () {
        if (imag >= 0) {
            return "" + real + " + " + imag + "i";
        } else {
            return "" + real + " - " + (-imag) + "i";
        }
    }

    /////////////////////////////////////////////////////////////////////////
    ////                       public variables                          ////

    /** The real part. */
    public double real;
    /** The imaginary part. */
    public double imag;
}
