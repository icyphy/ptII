/* A Fraction.

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
//// Fraction

/**
 A class for representing fractions.  Fractions are immutable and
 maintained in lowest terms, with a positive denominator.   Thus,
 1/2 and 2/4 are interpreted as different names for the same number.
 Any fraction with the value zero is expressed as 0/1 in lowest terms.
 <p>This class only represents fractions with a determined value,
 so fractions with a zero in the denominator are not allowed (including 0/0).

 @author Stephen Neuendorffer, Adam Cataldo
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class Fraction extends Object {
    /** Create a new fraction with the value zero (0/1).
     */
    public Fraction() {
        _numerator = 0;
        _denominator = 1;
        _simplify();
    }

    /** Create a new fraction with the value i/1.
     *  @param i The numerator.
     */
    public Fraction(int i) {
        _numerator = i;
        _denominator = 1;
        _simplify();
    }

    /**
     * Create a new fraction in lowest terms
     * with the value Numerator/Denominator.
     * @param numerator The numerator.
     * @param denominator The denominator.
     * @exception ArithmeticException If the denominator is specified to be
     * zero.
     */
    public Fraction(int numerator, int denominator) {
        if (denominator == 0) {
            throw new ArithmeticException("Illegal Fraction: "
                    + "cannot have denominator of zero.");
        }

        _numerator = numerator;
        _denominator = denominator;
        _simplify();
    }

    /** Create a new fraction with the same value as the given fraction.
     *  @param f The given Fraction.
     */
    public Fraction(Fraction f) {
        _numerator = f._numerator;
        _denominator = f._denominator;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add this fraction to the given fraction.
     *  @param b The given Fraction.
     *  @return The answer as another fraction in lowest terms.
     */
    public Fraction add(Fraction b) {
        Fraction f = new Fraction(_numerator * b._denominator + _denominator
                * b._numerator, _denominator * b._denominator);
        return f;
    }

    /** Divide this fraction by the given fraction.
     *  @param b The given Fraction.
     *  @return The answer as another fraction in lowest terms.
     *  @exception ArithmeticException If the fraction in the divisor has
     *  a value of zero.
     */
    public Fraction divide(Fraction b) {
        if (b.equals(ZERO)) {
            throw new ArithmeticException("Division by zero!");
        }

        Fraction f = new Fraction(_numerator * b._denominator, _denominator
                * b._numerator);
        return f;
    }

    /** Compare this fraction with the given object.
     *  @return True if the given object is a fraction
     *  and equal to this fraction.
     */
    @Override
    public boolean equals(Object b) {
        // The Fractions are already in lowest terms, so we just compare the
        // numerator and denominator
        if (b instanceof Fraction) {
            return _numerator == ((Fraction) b)._numerator
                    && _denominator == ((Fraction) b)._denominator;
        }
        return false;
    }

    /** Return the denominator of this fraction.
     */
    public int getDenominator() {
        return _denominator;
    }

    /** Return the numerator of this fraction.
     */
    public int getNumerator() {
        return _numerator;
    }

    /** Test if this Fraction is greater than the input.
     *
     * @param testInput The input to compare against.
     * @return True if this Fraction is greater than the input.
     */
    public boolean greaterThan(Fraction testInput) {
        int gcd = ExtendedMath.gcd(testInput.getDenominator(),
                this.getDenominator());
        int thisScaled = this.multiply(new Fraction(gcd, 1)).getNumerator();
        int inputScaled = testInput.multiply(new Fraction(gcd, 1))
                .getNumerator();

        if (thisScaled > inputScaled) {
            return true;
        }
        return false;
    }

    /** Return a hash code value for this Fraction. This method returns the
     *  bitwise and of the hashcode of the denominator and the numerator.
     *  @return A hash code value for this Coordinate
     */
    @Override
    public int hashCode() {
        return Integer.valueOf(_denominator).hashCode()
                & Integer.valueOf(_numerator).hashCode();
    }

    /** Find the multiplicative inverse of this fraction.
     *  @return The answer as another fraction in lowest terms
     *  @exception ArithmeticException If this fraction has a value of zero,
     *  in which case the multiplicative inverse cannot be represented.
     */
    public Fraction inverse() {
        if (equals(ZERO)) {
            throw new ArithmeticException("Inverse of zero is undefined!");
        }

        Fraction f = new Fraction(_denominator, _numerator);
        return f;
    }

    /** Finds the least common multiple(LCM) of two integers.
     *  If one of the numbers is negative, then the LCM is negative.
     *  If both of the numbers are negative, then the LCM is positive.
     *  the LCM is least in terms of absolute value.
     */

    //FIXME: should this go someplace better?
    public static int lcm(int u, int v) {
        int gcd = ExtendedMath.gcd(u, v);
        int result = u * v / gcd;
        return result;
    }

    /** Multiply this fraction by the given fraction.
     *  @return The answer as another fraction in lowest terms.
     */
    public Fraction multiply(Fraction b) {
        Fraction f = new Fraction(_numerator * b._numerator, _denominator
                * b._denominator);
        return f;
    }

    /** Find the additive inverse of this fraction.
     *  @return The answer as another fraction in lowest terms
     */
    public Fraction negate() {
        Fraction f = new Fraction(-_numerator, _denominator);
        return f;
    }

    /** Subtract the given fraction from this fraction.
     *  @return The answer as another fraction in lowest terms
     */
    public Fraction subtract(Fraction b) {
        Fraction f = new Fraction(_numerator * b._denominator - _denominator
                * b._numerator, _denominator * b._denominator);
        return f;
    }

    /** Convert the fraction to a double.
     *  @return The double value.
     */
    public double toDouble() {
        double numerator;
        double denominator;
        numerator = _numerator;
        denominator = _denominator;
        return numerator / denominator;
    }

    /** Convert the fraction to a float.
     *  @return The float value.
     */
    public float toFloat() {
        float numerator;
        float denominator;
        numerator = _numerator;
        denominator = _denominator;
        return numerator / denominator;
    }

    /** Convert the fraction to a readable string.
     */
    @Override
    public String toString() {
        _simplify();
        return _numerator + "/" + _denominator;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The value of zero as a Fraction.
     * It is arguable as to whether or not this is needed.  It may
     * reduce the number of object creations, and increase speed,
     * depending how often a zero fraction is needed.  This may become
     * useful when this class is made into a Token.
     */
    public static final Fraction ZERO = new Fraction(0, 1);

    /** Reduce the fraction to lowest terms by dividing the Numerator and
     *  Denominator by their Greatest Common Divisor.  In addition the
     *  fraction is put in standard form (denominator greater than zero).
     */
    protected void _simplify() {
        int factor = ExtendedMath.gcd(_numerator, _denominator);
        _numerator = _numerator / factor;
        _denominator = _denominator / factor;

        // Standardize the sign
        if (_denominator < 0) {
            _denominator = -_denominator;
            _numerator = -_numerator;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _numerator;

    private int _denominator;
}
