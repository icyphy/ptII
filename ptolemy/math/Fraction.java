/* A Fraction.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
*/



// do we want to make this a token class?
// upside: consistant interface, might be useful for domains.
// downside: work

package ptolemy.math;

import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// Fraction
/**
A Fraction class.  Yes, it works just the way you'd expect.

@author Stephen Neuendorffer
@version $Id$
*/
public class Fraction {

    /** Create a new fraction.   Default Value = 0
     */
    public Fraction() {
        _num = 0;
        _den = 1;
    }


    /** Create a new fraction.   Default Value = i;
     */
    public Fraction(int i) {
        _num = i;
        _den = 1;
    }

    /** Create a new Fraction.   Default Value = Numerator/Denominator;
     */
    public Fraction(int Numerator, int Denominator) {
                if(Denominator == 0)
                    throw new ArithmeticException("Illegal Fraction: " +
                            "cannot Divide by zero");

        _num = Numerator;
        _den = Denominator;
    }

    /** Create a new Fraction.   Default value = Fraction;
     */
    public Fraction(Fraction f) {
        _num = f._num;
        _den = f._den;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Set the Numerator of an existing Fraction
     */
    public void setNumerator(int Numerator) {
        _num = Numerator;
    }


    /** Set the Denominator of an existing Fraction
     */
    public void setDenominator(int Denominator) {
                if(Denominator == 0)
                    throw new ArithmeticException("Illegal Fraction: " +
                            "cannot Divide by zero");
                _den = Denominator;
    }


    /** Reduce the fraction to lowest terms by dividing the Numerator and
        Denominator by their Greatest Common Divisor */
    public void simplify() {
        int factor = gcd(_num, _den);
        _num = _num / factor;
        _den = _den / factor;
        // Standardize the sign
        if(_den < 0) {
            _den = -_den;
            _num = -_num;
        }
    }


    /** Multiply two Fractions.
     *  @return The answer as another Fraction
     */
    public static Fraction multiply(Fraction a, Fraction b) {
        Fraction f = new Fraction(a._num * b._num, a._den * b._den);
        return f;
    }


    /** Divide two Fractions.
     *  @return The answer as another Fraction
     */
    public static Fraction divide(Fraction a, Fraction b) {
        Fraction f = new Fraction(a._num * b._den, a._den * b._num);
        if(f._den == 0)
            throw new ArithmeticException("Illegal Fraction: " +
                    "cannot Divide by zero");
        return f;
    }


    /** Add two Fractions.
     *  @return The answer as another Fraction
     */
    public static Fraction add(Fraction a, Fraction b) {
        Fraction f = new Fraction(
                a._num * b._den + a._den * b._num, a._den * b._den);
        return f;
    }


    /** Subtract two Fractions.
     *  @return The answer as another Fraction
     */
    public static Fraction subtract(Fraction a, Fraction b) {
       Fraction f = new Fraction(
               a._num * b._den - a._den * b._num, a._den * b._den);
        return f;
    }


    /** Negate a Fraction.
     *  @return The answer as another Fraction
     */
    public static Fraction negate(Fraction a) {
        Fraction f = new Fraction(-a._num, a._den);
        return f;
    }


    /** Invert a Fractions.
     *  @return The answer as another Fraction
     */
    public static Fraction inverse(Fraction a) {
        Fraction f = new Fraction(a._den, a._num);
        return f;
    }


    /** Do two Fractions have the same value?   The Fractions are compared in
     *  lowest terms, standard form.
     */
    public static boolean equals(Fraction ai, Fraction bi) {
        Fraction a = new Fraction(ai);
        Fraction b = new Fraction(bi);
        a.simplify();
        b.simplify();
        return ((a._num == b._num) && (a._den == b._den));
    }


    /** Convert the fraction to a readable string
     */
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(_num);
        s.append('/');
        s.append(_den);
        return s.toString();
    }


    /** Return the Numerator
     */
    public int getNumerator() {
        return _num;
    }


    /** Return the Denominator
     */
    public int getDenominator() {
        return _den;
    }

    public static Fraction ZERO = new Fraction(0, 1);

    /** Implement Euclid's method for finding the Greatest Common Divisor of
     *  two numbers
     */
    // These should be moved to the Math Package

    public static int gcd(int u, int v) {
        int t;
        while(u > 0) {
            if(u < v) { t = u; u = v; v = t; }
            u = u - v;
        }
        return v;
    }

    /** Finds the least common multiple of two integers
     */
    public static int lcm(int u, int v) {
        int gcd = gcd(u, v);
        int result = u * v / gcd;
        return result;
    }

    protected int _num;
    protected int _den;


}
