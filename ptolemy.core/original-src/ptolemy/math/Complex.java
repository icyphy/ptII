/* A data type representing a complex number.

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

 isCloseTo(t), isCloseTo(t, e), EPSILON

 */
package ptolemy.math;

/** This class provides a complex data type and a library of functions that
 operate on and return complex numbers.  An instance of the class is
 immutable, meaning that its value is set in the constructor and
 cannot then be modified.  This is similar to the Java built-in classes
 like Double, Integer, etc.
 <p>
 Although this code is written from scratch, I looked at several designs
 and borrowed elements from each of them:
 <ul>
 <li> The ComplexSubset class in Ptolemy 0.x, written by Joe Buck,
 which borrowed design elements from the cfront and libg++
 Complex classes.
 <li> Version 1.0 of the Complex class by Alma services, dated Fri
 29-Aug-97, written by Sandy Anderson and Priyantha Jayanetti,
 and obtained from the <a href="http://www.netlib.org/">
 Netlib Repository</a>.
 <li> The Complex class in JNL, a Java Numerical Library, dated 1997,
 by Visual Numerics, Inc.  This was used for interface design only,
 to compare the choice of method names and method templates.
 <li> Matlab, which was used to validate the results in the test
 suite, and influenced some of the method name choices.
 </ul>
 @author Edward A. Lee, Jeff Tsay, Steve Neuendorffer, Adam Cataldo
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class Complex {
    /** Construct a Complex equal to zero.
     *  @deprecated Use Complex.ZERO instead.
     */
    @Deprecated
    public Complex() {
        this.real = 0.0;
        this.imag = 0.0;
    }

    /** Construct a Complex with a zero imaginary part.
     *  @param  real The real part.
     */
    public Complex(double real) {
        this.real = real;
        this.imag = 0.0;
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

    /** Return the magnitude or absolute value of the specified complex number.
     *  @param x The specified number.
     *  @return A non-negative number that is the absolute value of
     *  this complex number.
     */
    public static double abs(Complex x) {
        return x.magnitude();
    }

    /** Return the principal arc cosine of this complex number.  This
     *  is defined by:
     *  <pre>
     *   acos(z) = -i * log(z + i*sqrt(1 - z*z))
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @return A new complex number with value equal to the
     *  arc cosine of the given complex number.
     */
    public final Complex acos() {
        Complex c1 = new Complex(1.0 - real * real + imag * imag, -2.0 * real
                * imag);
        Complex c2 = c1.sqrt();
        Complex c3 = new Complex(real - c2.imag, imag + c2.real);
        Complex c4 = c3.log();
        return new Complex(c4.imag, -c4.real);
    }

    /** Return the principal arc cosine of the specified complex number.  This
     *  is defined by:
     *  <pre>
     *   acos(z) = -i * log(z + i*sqrt(1 - z*z))
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @param z A complex number.
     *  @return A new complex number with value equal to the
     *  arc cosine of the given complex number.
     */
    public static Complex acos(Complex z) {
        return z.acos();
    }

    /** Return the principal hyperbolic arc cosine of this
     *  complex number.  This is defined by:
     *  <pre>
     *   acosh(z) = log(z + sqrt(z*z - 1))
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @return A new complex number with value equal to the
     *  principal hyperbolic arc cosine of this complex number.
     */
    public final Complex acosh() {
        Complex c1 = new Complex(real * real - imag * imag - 1.0, 2.0 * real
                * imag);
        Complex c2 = c1.sqrt();
        Complex c3 = add(c2);
        return c3.log();
    }

    /** Return the principal hyperbolic arc cosine of the given
     *  complex number.  This is defined by:
     *  <pre>
     *   acosh(z) = log(z + sqrt(z*z - 1))
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @param z A complex number.
     *  @return A new complex number with value equal to the
     *  principal hyperbolic arc cosine of this complex number.
     */
    public static Complex acosh(Complex z) {
        return z.acosh();
    }

    /** Return the sum of this complex number and the argument <i>z</i>.
     *  @param z A complex number.
     *  @return A new complex number equal to the sume of the given complex
     *  number and the argument.
     */
    public final Complex add(Complex z) {
        return new Complex(real + z.real, imag + z.imag);
    }

    /** Return the angle or argument of this complex number.
     *  @return A double in the range -<em>pi </em> to <em>pi</em>.
     */
    public final double angle() {
        return Math.atan2(imag, real);
    }

    /** Return the angle or argument of this complex number.
     *  @param z A complex number.
     *  @return A double in the range -<em>pi </em> to <em>pi</em>.
     */
    public static double angle(Complex z) {
        return z.angle();
    }

    /** Return the principal arc sine of this complex number.
     *  This is defined by:
     *  <pre>
     *   asin(z) = -i * log(i*z + sqrt(1 - z*z))
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @return A new complex number equal to the principal arc sine
     *  of this complex number.
     */
    public final Complex asin() {
        Complex c1 = new Complex(1.0 - real * real + imag * imag, -2.0 * real
                * imag);
        Complex c2 = c1.sqrt();
        Complex c3 = new Complex(c2.real - imag, c2.imag + real);
        Complex c4 = c3.log();
        return new Complex(c4.imag, -c4.real);
    }

    /** Return the principal arc sine of the given complex number.
     *  This is defined by:
     *  <pre>
     *   asin(z) = -i * log(i*z + sqrt(1 - z*z))
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @param z A complex number.
     *  @return A new complex number equal to the principal arc sine
     *  of this complex number.
     */
    public static Complex asin(Complex z) {
        return z.asin();
    }

    /** Return the principal hyperbolic arc sine of this
     *  complex number.  This is defined by:
     *  <pre>
     *   asinh(z) = log(z + sqrt(z*z + 1))
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @return A new complex number with value equal to the principal
     *  hyperbolic arc sine of this complex number.
     */
    public final Complex asinh() {
        Complex c1 = new Complex(1.0 + real * real - imag * imag, 2.0 * real
                * imag);
        Complex c2 = c1.sqrt();
        Complex c3 = add(c2);
        return c3.log();
    }

    /** Return the principal hyperbolic arc sine of the given
     *  complex number.  This is defined by:
     *  <pre>
     *   asinh(z) = log(z + sqrt(z*z + 1))
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @param z A complex number.
     *  @return A new complex number with value equal to the principal
     *  hyperbolic arc sine of this complex number.
     */
    public static Complex asinh(Complex z) {
        return z.asinh();
    }

    /** Return the principal arc tangent of this complex
     *  number.  This is defined by:
     *  <pre>
     *  atan(z) = -i/2 * log((i-z)/(i+z))
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @return a new complex number with value equal to the principal arc
     *  tangent of this complex number.
     */
    public final Complex atan() {
        double denominator = real * real + (imag + 1.0) * (imag + 1.0);
        Complex c1 = new Complex((-real * real - imag * imag + 1.0)
                / denominator, 2.0 * real / denominator);
        Complex c2 = c1.log();
        return new Complex(c2.imag * 0.5, -c2.real * 0.5);
    }

    /** Return the principal arc tangent of the given complex
     *  number.  This is defined by:
     *  <pre>
     *  atan(z) = -i/2 * log((i-z)/(i+z))
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @param z A complex number.
     *  @return a new complex number with value equal to the principal arc
     *  tangent of this complex number.
     */
    public static Complex atan(Complex z) {
        return z.atan();
    }

    /** Return the principal hyperbolic arc tangent of
     *  this complex number.  This is defined by:
     *  <pre>
     *   atanh(z) = 1/2 * log((1+z)/(1-z))
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @return a new complex number with value equal to the principal
     *  hyperbolic arc tangent of this complex number.
     */
    public final Complex atanh() {
        double denominator = (1.0 - real) * (1.0 - real) + imag * imag;
        Complex c1 = new Complex((-real * real - imag * imag + 1.0)
                / denominator, 2.0 * imag / denominator);
        Complex c2 = c1.log();
        return new Complex(c2.real * 0.5, c2.imag * 0.5);
    }

    /** Return the principal hyperbolic arc tangent of
     *  the given complex number.  This is defined by:
     *  <pre>
     *   atanh(z) = 1/2 * log((1+z)/(1-z))
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @param z A complex number.
     *  @return a new complex number with value equal to the principal
     *  hyperbolic arc tangent of this complex number.
     */
    public static Complex atanh(Complex z) {
        return z.atanh();
    }

    /** Return the complex conjugate of this complex number.
     *  @return A new Complex with value equal to the complex conjugate of
     *  of this complex number.
     */
    public final Complex conjugate() {
        // Avoid negative zero.
        if (imag != 0.0) {
            return new Complex(real, -imag);
        }

        return new Complex(real, imag);
    }

    /** Return the complex conjugate of the specified complex number.
     *  @param z The specified complex number.
     *  @return A new Complex with value equal to the complex conjugate of
     *   of the specified complex number.
     */
    public static final Complex conjugate(Complex z) {
        return z.conjugate();
    }

    /** Return the complex conjugate of the specified real number, which is
     *  just the real number itself.  This method is provided for completeness
     *  in the expression language.
     *  @param z The specified real number.
     *  @return The number provided as an argument, but converted to complex.
     */
    public static final Complex conjugate(double z) {
        return new Complex(z, 0.0);
    }

    /** Return the cosine of this complex number.  This is defined by:
     *  <pre>
     *  cos(z) = (exp(i*z) + exp(-i*z))/2
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @return a new complex number with value equal to the cosine
     *  of this complex number.
     */
    public final Complex cos() {
        Complex c1 = new Complex(-imag, real);
        Complex c2 = c1.exp();
        Complex c3 = new Complex(imag, -real);
        Complex c4 = c2.add(c3.exp());
        return new Complex(c4.real * 0.5, c4.imag * 0.5);
    }

    /** Return the cosine of the given complex number.  This is defined by:
     *  <pre>
     *  cos(z) = (exp(i*z) + exp(-i*z))/2
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @param z A complex number.
     *  @return a new complex number with value equal to the cosine
     *  of this complex number.
     */
    public static Complex cos(Complex z) {
        return z.cos();
    }

    /** Return the hyperbolic cosine of this complex
     *  number.  This is defined by:
     *  <pre>
     *  cosh(z) = (exp(z) + exp(-z))/2
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @return A new complex number with value equal to the hyperbolic cosine
     *  of this complex number.
     */
    public final Complex cosh() {
        Complex c1 = exp();
        Complex c2 = new Complex(-real, -imag);
        Complex c3 = c1.add(c2.exp());
        return new Complex(c3.real * 0.5, c3.imag * 0.5);
    }

    /** Return the hyperbolic cosine of the given complex
     *  number.  This is defined by:
     *  <pre>
     *  cosh(z) = (exp(z) + exp(-z))/2
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @param z A complex number.
     *  @return A new complex number with value equal to the hyperbolic cosine
     *  of this complex number.
     */
    public static Complex cosh(Complex z) {
        return z.cosh();
    }

    /** Return the cotangent of this complex number.  This is simply:
     *  <pre>
     *  cot(z) = 1/tan(z)
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @return A new complex number with value equal to the cotangent
     *  of this complex number.
     */
    public final Complex cot() {
        Complex c1 = tan();
        return c1.reciprocal();
    }

    /** Return the cotangent of the given complex number.  This is simply:
     *  <pre>
     *  cot(z) = 1/tan(z)
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @param z A complex number.
     *  @return A new complex number with value equal to the cotangent
     *  of this complex number.
     */
    public static Complex cot(Complex z) {
        return z.cot();
    }

    /** Return the cosecant of this complex number.  This is simply:
     *  <pre>
     *  csc(z) = 1/sin(z)
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @return A new complex number with value equal to the cosecant
     *  of this complex number.
     */
    public Complex csc() {
        Complex c1 = sin();
        return c1.reciprocal();
    }

    /** Return the cosecant of the given complex number.  This is simply:
     *  <pre>
     *  csc(z) = 1/sin(z)
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @param z A complex number.
     *  @return A new complex number with value equal to the cosecant
     *  of this complex number.
     */
    public static Complex csc(Complex z) {
        return z.csc();
    }

    /** Divide this complex number by the argument, and return the result
     *  in a new Complex object.
     *
     *  @param divisor The denominator in the division.
     *  @return A new complex number equal to this complex number divided
     *  by the argument.
     */
    public final Complex divide(Complex divisor) {
        // This algorithm results from writing a/b as (ab*)/magSquared(b).
        double denominator = divisor.magnitudeSquared();
        return new Complex((real * divisor.real + imag * divisor.imag)
                / denominator, (imag * divisor.real - real * divisor.imag)
                / denominator);
    }

    /** Return true if the real and imaginary parts of this complex number
     *  are equal to those of the argument.
     *  @param z The argument to which this number is being compared.
     *  @return True if the real and imaginary parts are equal.
     */
    @Override
    public final boolean equals(Object z) {
        if (z instanceof Complex) {
            return ((Complex) z).real == real && ((Complex) z).imag == imag;
        }
        return false;
    }

    /** Return the exponential of this complex number,
     *  or <em>e<sup>z</sup></em>,
     *  where <code>z</code> is this complex number.
     *
     *  @return A new complex number with value equal to the exponential
     *  of this complex number.
     */
    public final Complex exp() {
        double magnitude = Math.exp(real);
        return polarToComplex(magnitude, imag);
    }

    /** Return the exponential of the specified complex number,
     *  or <em>e<sup>z</sup></em>,
     *  where <code>z</code> is the argument.
     *  @param z A complex exponent.
     *  @return A new complex number with value equal to the exponential
     *  of this complex number.
     */
    public static Complex exp(Complex z) {
        return z.exp();
    }

    /** Return a hash code value for this Complex. This method returns the
     *  bitwise xor of the hashcode of the real and imaginary parts.
     *  @return A hash code value for this Complex.
     */
    @Override
    public int hashCode() {
        // Use bitwise xor here so that if either real or imag is 0
        // we get better values.
        return Double.valueOf(real).hashCode() >>> Double.valueOf(imag)
                .hashCode();
    }

    /** Return the imaginary part of the specified complex number.
     *  @param z The complex number.
     *  @return The imaginary part of the argument.
     */
    public static double imag(Complex z) {
        return z.imag;
    }

    /** Return the imaginary part of the specified real number, which is 0.0.
     *  @param z The complex number.
     *  @return 0.0.
     */
    public static double imag(double z) {
        return 0.0;
    }

    /** Return true if the distance between this complex number and
     *         the argument is less than or equal to EPSILON.
     *  @param z The number to compare against.
     *  @return True if the distance to the argument is less
     *   than or equal to EPSILON.
     *  @see #EPSILON
     */
    public final boolean isCloseTo(Complex z) {
        return isCloseTo(z, EPSILON);
    }

    /** Return true if the distance between this complex number and
     *         the first argument is less than or equal to the second argument. If
     *  the distance argument is negative, return false.
     *  @param z The number to compare against.
     *  @param distance The distance criterion.
     *  @return True if the distance to the first argument is less
     *   than or equal to the second argument.
     */
    public final boolean isCloseTo(Complex z, double distance) {
        // NOTE: I couldn't find a way to make this as precise as double.
        // With this implementation, the following example yields the
        // wrong answer due to rounding errors:
        //    close (1.0i, 1.1i, 0.1)
        // (This is how to invoke this in the expression language.)
        if (distance < 0.0) {
            return false;
        }

        double differenceSquared = subtract(z).magnitudeSquared();
        double distanceSquared = distance * distance;

        if (differenceSquared > distanceSquared) {
            return false;
        } else {
            return true;
        }
    }

    /** Return true if either the real or imaginary part is infinite.
     *  This is determined by the isInfinite() method of the java.lang.Double
     *  class.
     *
     *  @return True if this is infinite.
     */
    public final boolean isInfinite() {
        return Double.isInfinite(real) || Double.isInfinite(imag);
    }

    /** Return true if either the real or imaginary part of the given
     *  complex number is infinite.  This is determined by the
     *  isInfinite() method of the java.lang.Double class.
     *
     *  @param z A complex number.
     *  @return True if this is infinite.
     */
    public static boolean isInfinite(Complex z) {
        return z.isInfinite();
    }

    /** Return true if either the real or imaginary part is NaN.  NaN means
     *  "not a number," per the IEEE floating point standard.
     *  This is determined by the isNaN() method of the java.lang.Double
     *  class.
     *
     *  @return True if this is NaN.
     */
    public final boolean isNaN() {
        return Double.isNaN(real) || Double.isNaN(imag);
    }

    /** Return true if either the real or imaginary part of the given
     *  Complex number is NaN.  NaN means "not a number," per the IEEE
     *  floating point standard.  This is determined by the isNaN()
     *  method of the java.lang.Double class.
     *
     *  @param z A complex number.
     *  @return True if this is NaN.
     */
    public static boolean isNaN(Complex z) {
        return z.isNaN();
    }

    /** Return the natural logarithm of this complex
     *  number.  The principal value is returned, which
     *  is
     *  <pre>
     *  log(z) = log(abs(z)) + i * angle(z)
     *  </pre>
     *  where <code>z</code> is this complex number, <code>abs(z)</code>
     *  is its magnitude, and <code>angle(z)</code> is its angle.
     *
     *  @return A new complex number with value equal to the natural logarithm
     *  of this complex number.
     */
    public final Complex log() {
        return new Complex(Math.log(magnitude()), angle());
    }

    /** Return the natural logarithm of the specified complex
     *  number.  The principal value is returned, which
     *  is
     *  <pre>
     *  log(z) = log(abs(z)) + i * angle(z)
     *  </pre>
     *  where <code>z</code> is this complex number, <code>abs(z)</code>
     *  is its magnitude, and <code>angle(z)</code> is its angle.
     *
     *  @param z A complex number.
     *  @return A new complex number with value equal to the natural logarithm
     *  of this complex number.
     */
    public static Complex log(Complex z) {
        return z.log();
    }

    /** Return the magnitude or absolute value of this complex number.
     *
     *  @return A non-negative number that is the absolute value of
     *  this complex number.
     */
    public final double magnitude() {
        return Math.sqrt(magnitudeSquared());
    }

    /** Return the magnitude or absolute value of the given complex number.
     *
     *  @param z A complex number.
     *  @return A non-negative number that is the absolute value of
     *  this complex number.
     */
    public static double magnitude(Complex z) {
        return z.magnitude();
    }

    /** Return the square of the magnitude of this complex number.
     *  This is provided for efficiency, since it is considerably easier
     *  to compute than the magnitude (which is the square root of this
     *  result).
     *
     *  @return A non-negative number which is the magnitude of this
     *  complex number.
     */
    public double magnitudeSquared() {
        return real * real + imag * imag;
    }

    /** Return the square of the magnitude of this complex number.
     *  This is provided for efficiency, since it is considerably easier
     *  to compute than the magnitude (which is the square root of this
     *  result).
     *
     *  @param z A complex number.
     *  @return A non-negative number which is the magnitude of this
     *  complex number.
     */
    public static double magnitudeSquared(Complex z) {
        return z.magnitudeSquared();
    }

    /** Return a new complex number that is formed by multiplying this
     *  complex number by the specified complex number.
     *
     *  @param w The specified complex number.
     *  @return A new complex number which is the product of this complex
     *  number and the specified complex number.
     *  @see Complex#scale
     */
    public Complex multiply(Complex w) {
        return new Complex(w.real * real - w.imag * imag, w.real * imag
                + w.imag * real);
    }

    /** Negate this complex number.
     *
     *  @return A new complex number that is formed by taking the
     *  negatives of both the real and imaginary parts of this complex
     *  number.
     */
    public final Complex negate() {
        // Avoid negative zero.
        double r = 0.0;
        double i = 0.0;

        if (real != 0.0) {
            r = -real;
        }

        if (imag != 0.0) {
            i = -imag;
        }

        return new Complex(r, i);
    }

    /** Return a new complex number with the specified magnitude and angle.
     *
     *  @param magnitude The magnitude.
     *  @param angle The angle.
     *  @return A new complex number with the specified magnitude and angle.
     */
    public static Complex polarToComplex(double magnitude, double angle) {
        if (magnitude < 0.0) {
            angle += Math.PI;
            magnitude = -magnitude;
        }

        if (magnitude == 0.0) {
            return Complex.ZERO;
        }

        return new Complex(magnitude * Math.cos(angle), magnitude
                * Math.sin(angle));
    }

    /** Return a new complex number with value <em>z <sup>y</sup></em>
     *  where <em>z</em> is this complex number and <em>y</em> is the
     *  argument, a double.
     *
     *  @param y The exponent, which is a double.
     *  @return A new complex number that with value <em>z <sup>y</sup></em>.
     */
    public Complex pow(double y) {
        // This formula follows from expanding the input form
        //     (rho e^(j theta))^(c + dj)
        // to something of the form ae^jb.
        double lnrho = Math.log(magnitude());
        double magnitude = Math.exp(lnrho * y);
        double angle = angle() * y;
        return polarToComplex(magnitude, angle);
    }

    /** Return a new complex number with value <em>z <sup>y</sup></em>
     *  where <em>z</em> is the first argument and <em>y</em> is the second
     *  argument.
     *  @param z The number to be raised to a power.
     *  @param y The exponent.
     *  @return A new complex number that with value <em>z <sup>y</sup></em>.
     */
    public static Complex pow(Complex z, double y) {
        return z.pow(y);
    }

    /** Return <em>z<sup>y</sup></em>
     *  where <em>z</em> is this complex number and <em>y</em> is the
     *  argument, a Complex.
     *
     *  @param y The exponent, which is a complex number.
     *  @return A new complex number equal to <em>z<sup>y</sup></em>.
     */
    public final Complex pow(Complex y) {
        // This formula follows from expanding the input form
        //     (rho e^(j theta))^(c + dj)
        // to something of the form ae^jb.
        double lnrho = Math.log(magnitude());
        double theta = angle();
        double magnitude = Math.exp(lnrho * y.real - theta * y.imag);
        double angle = lnrho * y.imag + theta * y.real;
        return polarToComplex(magnitude, angle);
    }

    /** Return a new complex number with value <em>z <sup>y</sup></em>
     *  where <em>z</em> is the first argument and <em>y</em> is the second
     *  argument.
     *  @param z The number to be raised to a power.
     *  @param y The exponent.
     *  @return A new complex number that with value <em>z <sup>y</sup></em>.
     */
    public static Complex pow(Complex z, Complex y) {
        return z.pow(y);
    }

    /** Return a new complex number with value <em>z <sup>y</sup></em>
     *  where <em>z</em> is the first argument and <em>y</em> is the second
     *  argument.
     *  @param z The number to be raised to a power.
     *  @param y The exponent.
     *  @return A new complex number that with value <em>z <sup>y</sup></em>.
     */
    public static Complex pow(double z, Complex y) {
        return new Complex(z, 0.0).pow(y);
    }

    /** Return the real part of the specified complex number.
     *  @param z The complex number.
     *  @return The real part of the argument.
     */
    public static double real(Complex z) {
        return z.real;
    }

    /** Return the real part of the specified real number, which is the
     *  real number itself.
     *  @param z The complex number.
     *  @return The argument.
     */
    public static double real(double z) {
        return z;
    }

    /** Return the reciprocal of this complex number.
     *  The result 1/a is given by (a*)/|a|^2.
     *
     *  @return A new complex number that is the reciprocal of this one.
     */
    public final Complex reciprocal() {
        double magSquared = magnitudeSquared();
        return new Complex(real / magSquared, -imag / magSquared);
    }

    /** Return the reciprocal of this complex number.
     *  The result 1/a is given by (a*)/|a|^2.
     *
     *  @param z A complex number.
     *  @return A new complex number that is the reciprocal of this one.
     */
    public static Complex reciprocal(Complex z) {
        return z.reciprocal();
    }

    /** Return the nth roots of this complex number in an array. There are
     *  n of them, computed by :
     *
     *  r<sup>1/n</sup>(cos((theta + 2kPI) / n + i sin((theta + 2kPI)/n)
     *  where k is the index of the returned array. If n is not greater than or
     *  equal to one, throw a IllegalArgumentException.
     *
     *  @param n An integer that must be greater than or equal to one.
     *  @return An array of Complex numbers, containing the n roots.
     */
    public final Complex[] roots(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("Complex.roots(): "
                    + "n must be greater than or equal to one.");
        }

        Complex[] returnValue = new Complex[n];

        double oneOverN = 1.0 / n;
        double twoPIOverN = 2.0 * Math.PI * oneOverN;
        double thetaOverN = angle() * oneOverN;
        double twoPIkOverN = 0.0;

        // r^(1/n) = (r^2)^(0.5 / n)
        double retMag = Math.pow(magnitudeSquared(), 0.5 * oneOverN);

        for (int k = 0; k < n; k++) {
            returnValue[k] = polarToComplex(retMag, thetaOverN + twoPIkOverN);
            twoPIkOverN += twoPIOverN;
        }

        return returnValue;
    }

    /** Return the nth roots of the given complex number in an
     *  array. There are n of them, computed by :
     *
     *  r<sup>1/n</sup>(cos((theta + 2kPI) / n + i sin((theta + 2kPI)/n)
     *  where k is the index of the returned array. If n is not greater than or
     *  equal to one, throw a IllegalArgumentException.
     *
     *  @param z A complex number.
     *  @param n An integer that must be greater than or equal to one.
     *  @return An array of Complex numbers, containing the n roots.
     */
    public static Complex[] roots(Complex z, int n) {
        return z.roots(n);
    }

    /** Return a new complex number with value equal to the product
     *  of this complex number and the real argument.
     *
     *  @param scalar A real number.
     *  @return A new complex number with value equal to the product
     *  of this complex number and the real argument.
     *  @see Complex#multiply
     */
    public final Complex scale(double scalar) {
        return new Complex(real * scalar, imag * scalar);
    }

    /** Return a new complex number with value equal to the secant
     *  of this complex number.  This is simply:
     *  <pre>
     *  sec(z) = 1/cos(z)
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @return A new complex number equal to the secant of this
     *  complex number.
     */
    public Complex sec() {
        Complex c1 = cos();
        return c1.reciprocal();
    }

    /** Return a new complex number with value equal to the secant
     *  of the given complex number.  This is simply:
     *  <pre>
     *  sec(z) = 1/cos(z)
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @param z A complex number.
     *  @return A new complex number equal to the secant of this
     *  complex number.
     */
    public static Complex sec(Complex z) {
        return z.sec();
    }

    /** Return a new complex number with value equal to the sine
     *  of this complex number.  This is defined by:
     *  <pre>
     *  sin(z) = (exp(i*z) - exp(-i*z))/(2*i)
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @return A new complex number equal to the sine of this complex number.
     */
    public final Complex sin() {
        Complex c1 = new Complex(-imag, real);
        Complex c2 = c1.exp();
        Complex c3 = new Complex(imag, -real);
        Complex c4 = c2.subtract(c3.exp());
        return new Complex(c4.imag * 0.5, -c4.real * 0.5);
    }

    /** Return a new complex number with value equal to the sine
     *  of the given complex number.  This is defined by:
     *  <pre>
     *  sin(z) = (exp(i*z) - exp(-i*z))/(2*i)
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @param z A complex number.
     *  @return A new complex number equal to the sine of this complex number.
     */
    public static Complex sin(Complex z) {
        return z.sin();
    }

    /** Return a new complex number with value equal to the hyperbolic sine
     *  of this complex number.  This is defined by:
     *  <pre>
     *  sinh(z) = (exp(z) - exp(-z))/2
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @return A new complex number equal to the hyperbolic sine
     *  of this complex number.
     */
    public final Complex sinh() {
        Complex c1 = exp();
        Complex c2 = new Complex(-real, -imag);
        Complex c3 = c1.subtract(c2.exp());
        return new Complex(c3.real * 0.5, c3.imag * 0.5);
    }

    /** Return a new complex number with value equal to the hyperbolic sine
     *  of this complex number.  This is defined by:
     *  <pre>
     *  sinh(z) = (exp(z) - exp(-z))/2
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @param z A complex number.
     *  @return A new complex number equal to the hyperbolic sine
     *  of this complex number.
     */
    public static Complex sinh(Complex z) {
        return z.sinh();
    }

    /** Return a new complex number with its value equal to the
     *  the square root of this complex number.
     *  The square root is defined to be:
     *  <pre>
     *  sqrt(z) = sqrt(mag(z))*(cos(angle(z)/2) + i * sin(angle(z)/2) )
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @return A new complex number equal to the square root of
     *  this complex number.
     */
    public final Complex sqrt() {
        double magnitude = Math.sqrt(magnitude());
        double angle = angle() * 0.5;
        return polarToComplex(magnitude, angle);
    }

    /** Return a new complex number with its value equal to the
     *  the square root of the specified complex number.
     *  The square root is defined to be:
     *  <pre>
     *  sqrt(z) = sqrt(mag(z))*(cos(angle(z)/2) + i * sin(angle(z)/2) )
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @param z A complex number.
     *  @return A new complex number equal to the square root of
     *  this complex number.
     */
    public static Complex sqrt(Complex z) {
        return z.sqrt();
    }

    /** Return a new complex number formed by subtracting the specified
     *  complex number from this complex number.
     *
     *  @param w The number that is being subtracted.
     *  @return A new complex number formed by subtracting the specified
     *  complex number from this complex number.
     */
    public final Complex subtract(Complex w) {
        return new Complex(real - w.real, imag - w.imag);
    }

    /** Return a new complex number with value equal to the tangent
     *  of this complex number.  This is defined by:
     *  <pre>
     *  tan(z) = sin(z)/cos(z)
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @return A new complex number equal to sin(z)/cos(z).
     */
    public final Complex tan() {
        Complex c1 = sin();
        return c1.divide(cos());
    }

    /** Return a new complex number with value equal to the tangent
     *  of the given complex number.  This is defined by:
     *  <pre>
     *  tan(z) = sin(z)/cos(z)
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @param z A complex number.
     *  @return A new complex number equal to sin(z)/cos(z).
     */
    public static Complex tan(Complex z) {
        return z.tan();
    }

    /** Return a new complex number with value equal to the hyperbolic tangent
     *  of this complex number.  This is defined by:
     *  <pre>
     *  tanh(z) = sinh(z)/cosh(z)
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @return A new complex number equal to sinh(z)/cosh(z).
     */
    public final Complex tanh() {
        Complex c1 = sinh();
        return c1.divide(cosh());
    }

    /** Return a new complex number with value equal to the hyperbolic tangent
     *  of the given complex number.  This is defined by:
     *  <pre>
     *  tanh(z) = sinh(z)/cosh(z)
     *  </pre>
     *  where <code>z</code> is this complex number.
     *
     *  @param z A complex number.
     *  @return A new complex number equal to sinh(z)/cosh(z).
     */
    public static Complex tanh(Complex z) {
        return z.tanh();
    }

    /** Return a string representation of this Complex.
     *
     * @return A string of the form "<em>x</em> + <em>y</em>i".
     */
    @Override
    public final String toString() {
        if (imag >= 0) {
            return Double.toString(real) + " + " + Double.toString(imag) + "i";
        } else {
            return Double.toString(real) + " - " + Double.toString(-imag) + "i";
        }
    }

    /** Return a string representation of the given Complex.
     *
     * @param value The given value.
     * @return A string of the form "<em>x</em> + <em>y</em>i".
     */
    public static String toString(Complex value) {
        if (value.imag >= 0) {
            return Double.toString(value.real) + " + "
                    + Double.toString(value.imag) + "i";
        } else {
            return Double.toString(value.real) + " - "
                    + Double.toString(-value.imag) + "i";
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

    /** A small number ( = 1.0e-9). This number is used by algorithms to
     *  detect whether a double is close to zero.  This value is
     *  public so that it can be changed on platforms with different
     *  precisions.
     *  This variable is not final so that users may set it as necessary.
     */
    public static/*final*/double EPSILON = 1.0e-9;

    /** A Complex number representing negative infinity, by which we mean
     *  that both the real and imaginary parts are equal to
     *  Double.NEGATIVE_INFINITY.
     */
    public static final Complex NEGATIVE_INFINITY = new Complex(
            Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

    /** A Complex number representing positive infinity, by which we mean
     *  that both the real and imaginary parts are equal to
     *  Double.POSITIVE_INFINITY.
     */
    public static final Complex POSITIVE_INFINITY = new Complex(
            Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

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
