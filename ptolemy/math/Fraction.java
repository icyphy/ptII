/* A Fraction.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

// do we want to make this a token class?
// upside: consistent interface, might be useful for domains.
// downside: work

package ptolemy.math;


//////////////////////////////////////////////////////////////////////////
//// Fraction
/**
A class for representing fractions.  Fractions are immutable and
maintained in lowest terms, with a positive denominator.   Thus,
1/2 and 2/4 are interpreted as different names for the same number.
Any fraction with the value zero is expressed as 0/1 in lowest terms.
<p>This class only represents fractions with a determined value,
so fractions with a zero in the denominator are not allowed (including 0/0).

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 0.2
*/
public class Fraction extends Object {
    /** Create a new fraction with the value zero (0/1).
     */
    public Fraction() {
        _num = 0;
        _den = 1;
        _simplify();
    }

    /** Create a new fraction with the value i/1.
     */
    public Fraction(int i) {
        _num = i;
        _den = 1;
        _simplify();
    }

    /**
     * Create a new fraction in lowest terms
     * with the value Numerator/Denominator.
     * @exception ArithmeticException If the denominator is specified to be
     * zero.
     */
    public Fraction(int Numerator, int Denominator) {
        if (Denominator == 0)
            throw new ArithmeticException("Illegal Fraction: " +
                    "cannot have denominator of zero.");

        _num = Numerator;
        _den = Denominator;
        _simplify();
    }

    /** Create a new fraction with the same value as the given fraction.
     */
    public Fraction(Fraction f) {
        _num = f._num;
        _den = f._den;
    }

    /* It is arguable as to whether or not this is needed.  It may
     * reduce the number of object creations, and increase speed,
     * depending how often a zero fraction is needed.  This may become
     * useful when this class is made into a Token.
     */
    public static Fraction ZERO = new Fraction(0, 1);

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add this fraction to the given fraction.
     *  @return The answer as another fraction in lowest terms.
     */
    public Fraction add(Fraction b) {
        Fraction f = new Fraction(
                _num * b._den + _den * b._num, _den * b._den);
        return f;
    }

    /** Divide this fraction by the given fraction.
     *  @return The answer as another fraction in lowest terms.
     *  @exception ArithmeticException If the fraction in the divisor has
     *  a value of zero.
     */
    public Fraction divide(Fraction b) {
        if (b.equals(ZERO)) throw new ArithmeticException(
                "Division by zero!");
        Fraction f = new Fraction(_num * b._den, _den * b._num);
        return f;
    }

    /** Compare this fraction with the given object.
     *  @return True if the given object is a fraction
     *  and equal to this fraction.
     */
    public boolean equals(Object b) {
        // The Fractions are already in lowest terms, so we just compare the
        // numerator and denominator
        if (b instanceof Fraction)
            return ((_num == ((Fraction) b)._num) &&
                    (_den == ((Fraction) b)._den));
        else
            return false;
    }

    /** Return the denominator of this fraction.
     */
    public int getDenominator() {
        return _den;
    }

    /** Return the numerator of this fraction.
     */
    public int getNumerator() {
        return _num;
    }

    /** Find the multiplicative inverse of this fraction.
     *  @return The answer as another fraction in lowest terms
     *  @exception ArithmeticException If this fraction has a value of zero,
     *  in which case the multiplicative inverse cannot be represented.
     */
    public Fraction inverse() {
        if (equals(ZERO)) throw new ArithmeticException(
                "Inverse of zero is undefined!");
        Fraction f = new Fraction(_den, _num);
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
        Fraction f = new Fraction(_num * b._num, _den * b._den);
        return f;
    }

    /** Find the additive inverse of this fraction.
     *  @return The answer as another fraction in lowest terms
     */
    public Fraction negate() {
        Fraction f = new Fraction(-_num, _den);
        return f;
    }

    /** Subtract the given fraction from this fraction.
     *  @return The answer as another fraction in lowest terms
     */
    public Fraction subtract(Fraction b) {
        Fraction f = new Fraction(
                _num * b._den - _den * b._num, _den * b._den);
        return f;
    }

    /** Convert the fraction to a readable string.
     */
    public String toString() {
        StringBuffer s = new StringBuffer();
        _simplify();
        s.append(_num);
        s.append('/');
        s.append(_den);
        return s.toString();
    }

    /** Reduce the fraction to lowest terms by dividing the Numerator and
     *  Denominator by their Greatest Common Divisor.  In addition the
     *  fraction is put in standard form (denominator greater than zero).
     */
    protected void _simplify() {
        int factor = ExtendedMath.gcd(_num, _den);
        _num = _num / factor;
        _den = _den / factor;
        // Standardize the sign
        if (_den < 0) {
            _den = -_den;
            _num = -_num;
        }
    }

    private int _num;
    private int _den;
}
