/** A class representing the precision of a FixPoint number.

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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 This class defines the precision of a signed or unsigned fixed point
 value. The precision of a fixed point value is represented as
 internally as three fields: the number of bits (<i>n</i>), the
 binary exponent (<i>e</i>), and a sign bit (<i>s</i>).

 The number of bits (<i>n</i>) defines the dynamic range of
 the format (2<sup>n</sup>).

 The exponent (<i>e</i>) determines the placement of the binary point
 for all values of this format. This binary point placement is "fixed"
 and is specified within the precision format rather than within individual
 Fixed point values.  The mantissa bits of all fixed point values
 using this this precision format are multiplied by 2<sup>m</sup>. Note that
 <i>m</i> can take on a positive,  negative, or zero value. This allows
 the binary point to be placed anywhere necessary and even beyond the bounds
 of the mantissa (i.e. to the right of the lsb or to the left of the msb).

 The sign value <i>s</i> is used to indicate whether the fixed point
 precision value is treated as signed (s = 1) or unsigned (s = 0). <p>

 <b>Signed</b> fixed point formats are represented in a 2's complement
 format. As a consequence, a single bit is used to represent the
 sign of the number. The value of a multi-bit signed number is: <p>

 Signed(B)   = 2^m x (- b_{n-1} x 2^{n-1} + \sum_{i=0}^{n-2} b_i x 2^i),

 where b_i is the value of bit i of the bit vector. <p>

 <b>Unsigned</b> fixed formats are represented as unsigned binary
 values. No bits are used to represent the sign and all values are
 positive. The actual value of a multi-bit unsigned fixed
 point number is:<p>

 Unsigned(B) = 2^m x (\sum_{i=0}^{n-1} b_i x 2^i) <p>

 <p>
 This class supports several different String formats for specifying and
 displaying the Precision. These String formats are supported by several
 classes that extend the {@link PrecisionFormat} class. Several such classes
 have been created. Any given precision string format can be represented in
 any of the valid precision string formats. The four supported Precision
 String formats are shown in the table below. A static singleton object of
 each of these formats is provided in this class. Each String Representation
 column of the table represent equivalent Precision formats:

 <table border = 1>
 <caption>Precision Formats.</caption>
 <tr><td><b>Format Name</b></td> <td><b>Format Spec</b></td>
 <td colspan = "6"><center><b>String Representation</b></center></td>
 </tr>
 <tr><td>{@link IntegerFractionPrecisionFormat}</td>
 <td> [integer bits].[fraction bits] </td>
 <td>3.2</td>
 <td>0.7</td>
 <td>-2.12</td>
 <td>12.-4</td>
 <td>32.0</td>
 <td>U1.7</td>
 </tr>
 <tr><td>{@link LengthIntegerPrecisionFormat}</td>
 <td>[total bits]/[integer bits]</td>
 <td>5/3</td>
 <td>7/0</td>
 <td>10/-2</td>
 <td>8/12</td>
 <td>32/32</td>
 <td>U8/1</td>
 </tr>
 <tr><td>{@link VHDLPrecisionFormat}</td>
 <td>[MSb position]:[LSb position]</td>
 <td>2:-2</td>
 <td>-1:-7</td>
 <td>-3:-12</td>
 <td>11:4</td>
 <td>31:0</td>
 <td>U0:-7</td>
 </tr>
 <tr><td>{@link LengthExponentPrecisionFormat}</td>
 <td>[total bits]e[binary point position]</td>
 <td>5e-2</td>
 <td>7e-7</td>
 <td>10e-12</td>
 <td>8e4</td>
 <td>32e0</td>
 <td>U8e-7</td>
 </tr>
 </table>

 <p>
 An instance of the class is immutable, meaning
 that its value is set in the constructor and cannot then be modified.

 @author Bart Kienhuis, Contributor: Mike Wirthlin
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (kienhuis)
 @Pt.AcceptedRating Red (kienhuis)
 @see FixPoint
 */
public class Precision implements Cloneable {

    /** Construct a Precision object based on the provided string. The
     *  string can describe the precision in any of the
     *  syntaxes explained in the description of this class.
     *
     *  @param str The string representing the precision.
     *  @exception IllegalArgumentException If the precision string
     *   supplied does not match one of the known formats.
     */
    public Precision(String str) throws IllegalArgumentException {
        Precision p = null;

        if ((p = INTEGER_FRACTION.parseString(str)) != null) {
            _format = INTEGER_FRACTION;
        } else if ((p = LENGTH_INTEGER.parseString(str)) != null) {
            _format = LENGTH_INTEGER;
        } else if ((p = LENGTH_EXPONENT.parseString(str)) != null) {
            _format = LENGTH_EXPONENT;
        } else if ((p = VHDL.parseString(str)) != null) {
            _format = VHDL;
        }

        if (p != null) {
            _length = p._length;
            _exponent = p._exponent;
            _sign = p._sign;
            return;
        }

        throw new IllegalArgumentException("Unrecognized Precision String:"
                + str);
    }

    /** Construct a Precision object based on the length/integer
     *  bits format. This constructor will create
     *  a new <i>signed</i> Precision object with <i>length</i>
     *  bits and an exponent of <i>-fracBits</i>.
     *
     *  @param length The total number of bits.
     *  @param integerBits The number of integer bits.
     *  @exception IllegalArgumentException If the given values are
     *   negative or when the integer number of bits is larger than the
     *   total number of bits.
     */
    public Precision(int length, int integerBits)
            throws IllegalArgumentException {
        this(1, length, integerBits - length);
    }

    /** Construct a Precision object based on the sign, length, and
     *  exponent format.
     *
     *  @param sign The presence of a sign bit (1 = signed, 0 = unsigned).
     *  @param length The total number of bits.
     *  @param exponent The bit location of the exponent.
     *  @exception IllegalArgumentException If the Precision arguments
     *  are inconsistent.
     */
    public Precision(int sign, int length, int exponent)
            throws IllegalArgumentException {
        if (sign != 0 && sign != 1) {
            throw new IllegalArgumentException("Incorrect definition of "
                    + "Precision. Sign must be 0 or 1");
        }

        if (length < 0) {
            throw new IllegalArgumentException("Incorrect definition of "
                    + "Precision. Do not use negative total length ");
        }

        _sign = sign;
        _length = length;
        _exponent = exponent;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return this, that is, return the reference to this object.
     *  @return This Precision.
     */
    @Override
    public Object clone() {
        // FIXME: Note that we do not call super.clone() here.  Is that right?
        return this;
    }

    /** Return true if the indicated object is an instance of Precision
     *  and the precision format matches exactly.
     *  @param object Object to test for equality
     *  @return True if the precisions are equal.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof Precision) {
            Precision other = (Precision) object;

            if (other._length == _length && other._exponent == _exponent
                    && other._sign == _sign) {
                return true;
            }
        }

        return false;
    }

    /** Return the maximum obtainable value in this precision.
     *  This value is obtained by multiplying the result of
     *  {@link #getMaximumUnscaledValue()} by 2<sup>exponent</sup>.
     *
     *  @return The maximum value obtainable for this precision.
     */
    public BigDecimal findMaximum() {
        BigDecimal bd = new BigDecimal(getMaximumUnscaledValue());
        return shiftBigDecimal(bd, _exponent);
    }

    /** Return the minimum obtainable value for this precision.
     *  This value is obtained by multiplying the result of
     *  {@link #getMinimumUnscaledValue()} by 2<sup>exponent</sup>.
     *
     *  @return The minimum value obtainable for the given precision.
     */
    public BigDecimal findMinimum() {
        if (!isSigned()) {
            return new BigDecimal(0);
        }
        BigDecimal bd = new BigDecimal(getMinimumUnscaledValue());
        return shiftBigDecimal(bd, _exponent);
    }

    /** Return the incremental value between discrete values under
     * the given Precision format. This is calculated as 2^exponent.
     *
     * @return Incremental (epsilon) value.
     */
    public BigDecimal getEpsilon() {
        return new BigDecimal(Math.pow(2, _exponent));
    }

    /** Return the location of the binary exponent.
     *  @return the location of the fixed binary exponent.
     */
    public int getExponent() {
        return _exponent;
    }

    /** Return the number of bits representing the fractional part.
     *  The is computed as -exponent.
     *  @return The length of the fractional part.
     */
    public int getFractionBitLength() {
        return -_exponent;
    }

    /** Return the number of bits representing the integer part.
     *  This is computed as length + exponent.
     *  @return the length of the integer part.
     */
    public int getIntegerBitLength() {
        return _length + _exponent;
    }

    /**
     * Return the bit position of the least significant bit of the
     * given fixed point precision. This value is the same as
     * the exponent value of this format. Bit 0 refers to
     * the bit just to the left of the binary point.
     *
     * @return Least significant bit position.
     */
    public int getLeastSignificantBitPosition() {
        return _exponent;
    }

    /** Return the maximum integer value <i>before</i> scaling so that
     *  quantization levels are represented by adjacent integers.
     *
     *  For signed values, this number is 2^(n-1) - 1
     *  For unsigned values, this number is 2^(n) - 1
     *  @return The maximum inclusive value.
     */
    public BigInteger getMaximumUnscaledValue() {
        // Create a positive "bit vector" that is the length
        // of the this precision and has all ones.
        BigInteger val = BigInteger.ZERO.setBit(_length - _sign);
        val = val.subtract(BigInteger.ONE);
        return val;
    }

    /** Return the minimum integer value <i>before</i> scaling so
     * that quantization levels are represented by adjacent
     * integers.
     *
     *  For signed values, this number is -(2^(n-1))
     *  For unsigned values, this number is 0.
     *
     *  @return The minimum unscaled value value obtainable
     *  for the given precision.
     */
    public BigInteger getMinimumUnscaledValue() {
        if (isSigned()) {
            return BigInteger.ZERO.setBit(_length - 1).negate();
        }

        return BigInteger.ZERO;
    }

    /**
     * Return the bit position of the most significant bit of the
     * given fixed point precision. Bit 0 refers to
     * the bit just to the left of the binary point.
     *
     * @return Least significant bit position.
     */
    public int getMostSignificantBitPosition() {
        return _exponent + _length - 1;
    }

    /**
     * Return the bit position of the most significant
     * data bit of the given fixed point precision. If this
     * Precision is signed, this will return
     * getMostSignificantBitPosition - 1. If this Precision
     * is unsigned, this will return getMostSignificantBitPosition.
     *
     * @return Least significant bit position.
     */
    public int getMostSignificantDataBitPosition() {
        return _exponent + _length - 1 - _sign;
    }

    /** Return the total number of bits.
     *  @return the total number of bits.
     */
    public int getNumberOfBits() {
        return _length;
    }

    /** Return the total number of discrete values possible.
     *  @return the total number of levels.
     */
    public BigInteger getNumberOfLevels() {
        int numBits = getNumberOfBits();
        return BigInteger.ZERO.setBit(numBits);
        //return Math.pow(2.0, _length);
    }

    /** Return the sign (0 = unsigned, 1 = signed).
     *  @return the sign status.
     */
    public int getSign() {
        return _sign;
    }

    /** Return a hash code value for this Precision. This method returns the
     *  bitwise and of the length, exponent and sign.
     *  @return A hash code value for this Precision.
     */
    @Override
    public int hashCode() {
        return Integer.valueOf(_length).hashCode() >>> Integer.valueOf(
                _exponent).hashCode() >>> Integer.valueOf(_sign).hashCode();
    }

    /**
     * Determine if the fixed point format is signed or not.
     *
     * @return true if format is signed
     */
    public boolean isSigned() {
        return _sign == 1;
    }

    /** Return the precision that is the maximum of the two supplied
     *  precisions in both the length and exponent. This
     *  method is used to align instances of FixPoint onto a single
     *  precision representation. If either Precision object is
     *  signed, the new Precision object will be signed.
     *
     *  @param precisionA A precision
     *  @param precisionB Another precision
     *  @return A precision at least as precise as the two arguments.
     */
    public static Precision union(Precision precisionA, Precision precisionB) {

        int minExponent = precisionA._exponent < precisionB._exponent ? precisionA._exponent
                : precisionB._exponent;

        int aDataMSB = precisionA.getMostSignificantDataBitPosition();
        int bDataMSB = precisionB.getMostSignificantDataBitPosition();

        int maxDataMSB = aDataMSB > bDataMSB ? aDataMSB : bDataMSB;
        int newLength = maxDataMSB - minExponent + 1;

        int newSign = precisionA._sign == 1 || precisionB._sign == 1 ? 1 : 0;
        newLength += newSign;

        return new Precision(newSign, newLength, minExponent);
    }

    /** Shift the BigDecimal value either right or left by
     *  a power of 2 value. This method will perform a
     *  multiplication by 2^<sup>shiftval</sup> on the BigDecimal
     *  value when shiftval is positive and will perform a
     *  divide by 2^<sup>-shiftval</sup> on the BigDecimal value
     *  when shiftval is negative.
     *
     *
     * @param val BigDecimal value to shift.
     * @param shiftval Amount of "power of 2" shifting to perform
     * on the BigDecimal value
     * @return shifted BigDecimal value.
     */
    public static BigDecimal shiftBigDecimal(BigDecimal val, int shiftval) {

        // No shifting takes place. Return the BigDecimal value
        if (shiftval == 0) {
            return val;
        }

        // Left shifting is going to occur. Multiply value by
        // 2^shiftval
        if (shiftval > 0) {
            return val.multiply(_getTwoRaisedTo(shiftval));
        }

        // Right shifting is going to occur. Divide value by
        // 2^(-shiftval) since shiftval is negative.

        // When dividing BigDecimal values, we need to know the maximum
        // number of decimal places will be added to the right
        // as a result of the divide
        // to preserve the precision of the result. Since we are
        // dividing by a number of the form, 2^n, the division
        // is essentially a divide by 2 for n times. Every
        // divide by two will add at most one new
        // least significant decimal digit to the right of the decimal
        // point. To preserve all of the precision of the divide,
        // set the BigDecimal "scale" to n + val.scale() where
        // n is the power of 2.
        int scaleShift = -shiftval + val.scale();

        BigDecimal result = val.divide(_getTwoRaisedTo(-shiftval), scaleShift,
                BigDecimal.ROUND_HALF_EVEN);
        return result;
    }

    /** Return a string representing this precision. The string is
     *  expressed as "(<i>m.n</i>)", where <i>m</i>
     *  indicates the number of integer bits and <i>n</i> represents
     *  the number of fractional bits.
     *  @return A string representing this precision.
     */
    @Override
    public String toString() {
        return _format.printPrecisionFormat(this);
    }

    /**
     * Return a string representation of this format in one of several
     * styles.
     * @param format The String format represented desired for printing.
     * @return String representation of the object using the given
     * PrecisionFormat
     */
    public String toString(PrecisionFormat format) {
        return format.printPrecisionFormat(this);
    }

    /** Defines a String format for specifying a Precision object.
     * This abstract class defines a method for parsing the string
     * format and a method for creating a valid String representation
     * of a Precision object. The printing and parsing methods should
     * be consistent (i.e. the parsing method should successfully parse
     * the result of the printing method).
     */
    public static abstract class PrecisionFormat {

        /**
         * Parse the given String argument using the rules of the specific
         * PrecisionFormat that is defined. This method will return a
         * valid Precision object from the String. If the String parsing
         * does not produce a valid match, this method will return a null.
         * If the String match is successful but there is a problem in
         * the interpretation of the match, this method will throw
         * a IllegalArgumentException.
         *
         * @param str String to parse
         * @return A Precision object. Returns a null if the String does
         * not match the particular string format.
         * @exception IllegalArgumentException If there is a problem
         * parsing the String (i.e. an illegal string).
         */
        public abstract Precision parseString(String str)
                throws IllegalArgumentException;

        /** Creates a valid String representation of the Precision object
         * based on the rules of the given string format. The format of this
         * String should be consistent with the format used in the
         * parseString method.
         *
         * @param p Precision object to represent as a String.
         * @return String representing the Precision object
         */
        public abstract String printPrecisionFormat(Precision p);

        /** Parse a String as an integer. This method calls the
         * Integer.parseInt method but throws the IllegalArgumentException
         * instead of the NumberFormatException when there is a problem.
         *
         * @param str The integer string to parse
         * @return The integer value of the string.
         * @exception IllegalArgumentException If the parsing of the
         * Integer value fails (i.e. NumberFormatException).
         */
        public static int parseInteger(String str)
                throws IllegalArgumentException {
            int value = 0;

            try {
                value = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid integer number:"
                        + str + " " + e);
            }

            return value;
        }

        /** Parse the 'U' or 'S' sign specifier.
         *
         * @param str String to parse for a sign indicator.
         * @return Return a 0 for unsigned and a 1 for signed.
         * @exception IllegalArgumentException If the
         * string does not match the 'U' or 'S' characters.
         */
        public static int parseSignString(String str)
                throws IllegalArgumentException {
            if (str.equalsIgnoreCase("U")) {
                return 0;
            } else if (str.equalsIgnoreCase("S")) {
                return 1;
            }

            throw new IllegalArgumentException("Invalid signed format string:'"
                    + str + "'. Expecting 'U' or 'S'");
        }

        /** The following static String objects are used for convenience
         *  when performing regular expression matches on Precision
         *  Strings. The following explanation will help explain the
         *  meaning of some of these cryptic strings:
         *
         *  "\\"
         *    This corresponds to a single "\" in the regular expression
         *    matcher. A single "\" must be represetned within Java as an
         *    escape sequence (i.e. "\\"). A single "\" will not compile as
         *    the \" corresponds to the string """ (i.e. a single quotation
         *    mark).
         *
         *  GROUP
         *    Any variable with the GROUP in its name corresponds to a
         *    regular expression with the "capturing group" parenthesis
         *    around the regular expression. This "capturing" is used
         *    to obtain the matching string associated with the
         *    regular expression.
         *
         */

        /** Regular expression definition for a comma "," or a
         *  forward slash "/". */
        public final static String COMMA_OR_FORWARDSLASH = "[,/]";

        /** Regular expression definition for an optional left parenthesis. **/
        public final static String OPTIONAL_L_PARAN = "\\(?";

        /** Regular expression definition for an optional left parenthesis
         *  or left bracket. **/
        public final static String OPTIONAL_L_PARANBRACKET = "[\\(\\[]?";

        /** Regular expression definition for an optional right parenthesis. **/
        public final static String OPTIONAL_R_PARAN = "\\)?";

        /** Regular expression definition for an optional right parenthesis
         *  or left bracket. **/
        public final static String OPTIONAL_R_PARANBRACKET = "[\\)\\]]?";

        /** Regular expression definition for a Period ".". */
        public final static String PERIOD = "\\.";

        /** Regular expression for a grouped signed integer
         * (positive or negative). **/
        public final static String SIGNED_INTEGER_GROUP = "(-?\\d+)";

        /** Regular expression for an optional 'S' or 'U' "group". */
        public final static String OPTIONAL_U_OR_S_GROUP = "([USus])?";

        /** Regular expression for a grouped unsigned integer. **/
        public final static String UNSIGNED_INTEGER_GROUP = "(\\d+)";

        /** Regular expression for optional white space. **/
        public final static String OPTIONAL_WHITE_SPACE = "\\s*";
    }

    /** Defines a Precision string format using the INTEGER.FRACTION
     * precision format. The INTEGER value specifies the number of
     * integer bits and the FRACTION value specifies the number of
     * fractional bits. Negative values are allowed for either INTEGER
     * or FRACTION (but not both). <p>
     *
     * This format supports the specification of either signed or
     * unsigned values. The character 'U' must precede the
     * integer.fraction format to specify an unsigned value. An 'S' character
     * may be applied to specify a signed number. If no 'U' or 'S'
     * signed specification is provided, the precision will default
     * to a signed value. <p>
     *
     * The exponent in this format is set to -fraction.
     * <p>
     * Parenthesis or brackets are optional around this specification.
     *
     * Examples:
     * <ul>
     *   <li> (3.5)
     *   <li> (U2.1)
     *   <li> [S2.1]
     *   <li> [S6.-2]
     *   <li> S-2.4
     * </ul>
     *
     * When printed, this format will always use parenthesis and
     * only use the signed format specification for unsigned numbers
     * (i.e. 'U' used for unsigned numbers and NO 'S' for signed
     * numbers).
     *
     */
    public static class IntegerFractionPrecisionFormat extends PrecisionFormat {

        /** Regular expression for IntegerFractionPrecisionFormat.
         *  Example: (S3.2) */
        protected final static String _regex = OPTIONAL_WHITE_SPACE
                + OPTIONAL_L_PARANBRACKET + OPTIONAL_WHITE_SPACE
                + OPTIONAL_U_OR_S_GROUP + OPTIONAL_WHITE_SPACE
                + SIGNED_INTEGER_GROUP + PERIOD + SIGNED_INTEGER_GROUP
                + OPTIONAL_WHITE_SPACE + OPTIONAL_R_PARANBRACKET
                + OPTIONAL_WHITE_SPACE;

        @Override
        public Precision parseString(String str)
                throws IllegalArgumentException {
            int sign = 1;
            int intLength = 0;
            int fracLength = 0;
            Matcher matcher;

            matcher = Pattern.compile(_regex).matcher(str);

            if (matcher.matches()) {
                String signString = matcher.group(1);

                if (signString != null) {
                    sign = parseSignString(signString);
                }

                intLength = parseInteger(matcher.group(2));
                fracLength = parseInteger(matcher.group(3));

                int length = fracLength + intLength;
                int exponent = -fracLength;

                if (length < 1) {
                    throw new IllegalArgumentException("Precision format "
                            + " must be at least 1 bit:" + str);
                }

                return new Precision(sign, length, exponent);
            }

            return null;
        }

        @Override
        public String printPrecisionFormat(Precision p) {
            String sign = p.isSigned() ? "" : "U";
            return "(" + sign + p.getIntegerBitLength() + "."
            + p.getFractionBitLength() + ")";
        }
    }

    /** Defines a Precision string format using the LENGTH/INTEGER
     * precision format. The LENGTH value specifies the length
     * of the format in bits. The INTEGER field specifies the
     * number of integer bits (i.e. bits to the left of the
     * binary point). Negative values are allowed for the INTEGER
     * field. <p>
     *
     * The exponent in this format is set to -(length - integer);
     * <p>
     *
     * This format supports the specification of either signed or
     * unsigned values. The character 'U' must precede the
     * LENGTH/INTEGER format to specify an unsigned value. An 'S' character
     * may be applied to specify a signed number. If no 'U' or 'S'
     * signed specification is provided, the precision will default
     * to a signed value. <p>
     *
     * Parenthesis or brackets are optional around this specification.
     * A comma (,) may be used in place of the slash ('/').
     *
     * Examples:
     * <ul>
     *   <li> (3/2)
     *   <li> (U5/0)
     *   <li> [8,-2]
     *   <li> [S6,6]
     *   <li> S8/4
     * </ul>
     *
     * When printed, this format will always use parenthesis, always
     * use the slash '/', and
     * only use the signed format specification for unsigned numbers
     * (i.e. 'U' used for unsigned numbers and NO 'S' for signed
     * numbers).
     *
     */
    public static class LengthIntegerPrecisionFormat extends PrecisionFormat {
        /** Regular expression for IntegerFractionPrecisionFormat.
         *   Example (S3,2) or (S3/2) */
        protected final static String _regex = OPTIONAL_WHITE_SPACE
                + OPTIONAL_L_PARANBRACKET + OPTIONAL_WHITE_SPACE
                + OPTIONAL_U_OR_S_GROUP + OPTIONAL_WHITE_SPACE
                + UNSIGNED_INTEGER_GROUP + COMMA_OR_FORWARDSLASH
                + OPTIONAL_WHITE_SPACE + SIGNED_INTEGER_GROUP
                + OPTIONAL_WHITE_SPACE + OPTIONAL_R_PARANBRACKET
                + OPTIONAL_WHITE_SPACE;

        @Override
        public Precision parseString(String str)
                throws IllegalArgumentException {
            int sign = 1;
            int intLength = 0;
            int length = 0;
            Matcher matcher;

            matcher = Pattern.compile(_regex).matcher(str);

            if (matcher.matches()) {
                String signString = matcher.group(1);

                if (signString != null) {
                    sign = parseSignString(signString);
                }

                length = parseInteger(matcher.group(2));
                intLength = parseInteger(matcher.group(3));

                int exponent = -(length - intLength);

                if (length < 1) {
                    throw new IllegalArgumentException("Precision format "
                            + "must be at least 1 bit:" + str);
                }

                return new Precision(sign, length, exponent);
            }

            return null;
        }

        @Override
        public String printPrecisionFormat(Precision p) {
            String sign = p.isSigned() ? "" : "U";
            return "(" + sign + p.getNumberOfBits() + "/"
            + p.getIntegerBitLength() + ")";
        }
    }

    /** Precision format for use with the Expression Language. */
    public static class ExpressionLanguagePrecisionFormat extends
    LengthIntegerPrecisionFormat {
        /** Regular expression for ExpressionLanguagePrecisionFormat.
         *  For example: (3,2).
         */
        @Override
        public String printPrecisionFormat(Precision p) {
            return "(" + p.getNumberOfBits() + "," + p.getIntegerBitLength()
                    + ")";
        }
    }

    /** Defines a Precision string format using the LENGTHeEXPONENT
     * precision format. The LENGTH value specifies the length
     * of the format in bits and the EXPONENT specifies the
     * location of the exponent. Negative values are allowed for the
     * EXPONENT field. <p>
     *
     * This format supports the specification of either signed or
     * unsigned values. The character 'U' must precede the
     * LENGTH/INTEGER format to specify an unsigned value. An 'S' character
     * may be applied to specify a signed number. If no 'U' or 'S'
     * signed specification is provided, the precision will default
     * to a signed value. <p>
     *
     * Parenthesis or brackets are optional around this specification.
     *
     * Examples:
     * <ul>
     *   <li> (3e2)
     *   <li> (U5e0)
     *   <li> [8e-2]
     *   <li> [S6e6]
     *   <li> S8e-4
     * </ul>
     *
     * When printed, this format will always use parenthesis and
     * only use the signed format specification for unsigned numbers
     * (i.e. 'U' used for unsigned numbers and NO 'S' for signed
     * numbers).
     *
     */
    public static class LengthExponentPrecisionFormat extends PrecisionFormat {
        /** Regular expression for IntegerFractionPrecisionFormat.
         *   Example (S3e2) */
        protected final String _regex = OPTIONAL_WHITE_SPACE
                + OPTIONAL_L_PARANBRACKET + OPTIONAL_WHITE_SPACE
                + OPTIONAL_U_OR_S_GROUP + OPTIONAL_WHITE_SPACE
                + UNSIGNED_INTEGER_GROUP + "e" + SIGNED_INTEGER_GROUP
                + OPTIONAL_WHITE_SPACE + OPTIONAL_R_PARANBRACKET
                + OPTIONAL_WHITE_SPACE;

        @Override
        public Precision parseString(String str)
                throws IllegalArgumentException {
            int sign = 1;
            int exponent = 0;
            int length = 0;
            Matcher matcher;

            matcher = Pattern.compile(_regex).matcher(str);

            if (matcher.matches()) {
                String signString = matcher.group(1);

                if (signString != null) {
                    sign = parseSignString(signString);
                }

                length = parseInteger(matcher.group(2));
                exponent = parseInteger(matcher.group(3));

                if (length < 1) {
                    throw new IllegalArgumentException("Precision format "
                            + "must be at least 1 bit:" + str);
                }

                return new Precision(sign, length, exponent);
            }

            return null;
        }

        @Override
        public String printPrecisionFormat(Precision p) {
            String sign = p.isSigned() ? "" : "U";
            return "(" + sign + p.getNumberOfBits() + "e" + p.getExponent()
                    + ")";
        }
    }

    /** Defines a Precision string format using the VHDL MSB:LSB
     * precision format. The MSB value specifies the location
     * of the most significant bit and LSB specifies the location
     * of the least significant bit.
     * Negative values are allowed for both MSB and LSB so long as
     * the MSB is greater than the LSB. <p>
     *
     * This format supports the specification of either signed or
     * unsigned values. The character 'U' must precede the
     * MSB:LSB format to specify an unsigned value. An 'S' character
     * may be applied to specify a signed number. If no 'U' or 'S'
     * signed specification is provided, the precision will default
     * to a signed value. <p>
     *
     * Parenthesis or brackets are optional around this specification.
     *
     * Examples:
     * <ul>
     *   <li> (2:3)
     *   <li> (4:-3)
     *   <li> [7:0]
     *   <li> [-3:-8]
     *   <li> 4:3
     * </ul>
     *
     * When printed, this format will always use parenthesis and
     * only use the signed format specification for unsigned numbers
     * (i.e. 'U' used for unsigned numbers and NO 'S' for signed
     * numbers).
     *
     */
    public static class VHDLPrecisionFormat extends PrecisionFormat {
        /** Regular expression for IntegerFractionPrecisionFormat.
         *   Example ([US]{digit}:{digit}) */
        protected final String _regex = OPTIONAL_WHITE_SPACE + OPTIONAL_L_PARAN
                + OPTIONAL_WHITE_SPACE + OPTIONAL_U_OR_S_GROUP
                + OPTIONAL_WHITE_SPACE + SIGNED_INTEGER_GROUP
                + OPTIONAL_WHITE_SPACE + ":" + OPTIONAL_WHITE_SPACE
                + SIGNED_INTEGER_GROUP + OPTIONAL_WHITE_SPACE
                + OPTIONAL_R_PARAN + OPTIONAL_WHITE_SPACE;

        @Override
        public Precision parseString(String str) {
            int sign = 1;
            int msb = 0;
            int lsb = 0;
            Matcher matcher;

            matcher = Pattern.compile(_regex).matcher(str);

            if (matcher.matches()) {
                String signString = matcher.group(1);

                if (signString != null) {
                    sign = parseSignString(signString);
                }

                msb = parseInteger(matcher.group(2));
                lsb = parseInteger(matcher.group(3));

                if (msb <= lsb) {
                    throw new IllegalArgumentException("MSb of VHDL "
                            + "format must be greater than LSb:" + str);
                }

                int length = msb - lsb + 1;
                int exponent = lsb;
                return new Precision(sign, length, exponent);
            }

            return null;
        }

        @Override
        public String printPrecisionFormat(Precision p) {
            String sign = p.isSigned() ? "" : "U";
            return "(" + sign + p.getMostSignificantBitPosition() + ":"
            + p.getLeastSignificantBitPosition() + ")";
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * static IntegerFractionPrecisionFormat object.
     */
    public final static PrecisionFormat INTEGER_FRACTION = new IntegerFractionPrecisionFormat();

    /**
     * static LengthExponentPrecisionFormat object.
     */
    public final static PrecisionFormat LENGTH_EXPONENT = new LengthExponentPrecisionFormat();

    /**
     * static LengthIntegerPrecisionFormat object.
     */
    public final static PrecisionFormat LENGTH_INTEGER = new LengthIntegerPrecisionFormat();

    /**
     * static ExpressionLanguagePrecisionFormat object.
     */
    public final static PrecisionFormat EXPRESSION_LANGUAGE = new ExpressionLanguagePrecisionFormat();

    /**
     * static VHDLPrecisionFormat object.
     */
    public final static PrecisionFormat VHDL = new VHDLPrecisionFormat();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get the BigDecimal which is the 2^exponent. If the value is
     *  already calculated, return this cached value, else calculate
     *  the value.
     *
     *  @param number the exponent.
     *  @return the BigDecimal representing 2^exponent.
     */
    private static BigDecimal _getTwoRaisedTo(int number) {
        // Since this is a private method we know that number is positive.
        if (number < _twoRaisedTo.length) {
            return _twoRaisedTo[number];
        } else {
            return new BigDecimal(BigInteger.ZERO.setBit(number));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The total number of bits. */
    private int _length = 0;

    /** The exponent for all values of this type. */
    private int _exponent = 0;

    /** The presence of a sign bit (0 = unsigned, 1 = signed) */
    private int _sign = 0;

    /** The precision format used for parsing/printing precision information.
     */
    private PrecisionFormat _format = INTEGER_FRACTION;

    ///////////////////////////////////////////////////////////////////
    ////                         static variables                  ////

    /** The size of the pre-computed _twoRaisedTo powers of two array.
     *   65 entries are used to cache all powers of 2 from 0 to 64.
     **/
    private static final int TWORAISEDTOSIZE = 64 + 1;

    /** Calculate the table containing 2^x, with 0 <= x < TWORAISEDTOSIZE.
     *   Purpose is to speed up calculations involving calculating
     *   2^x. The table is calculated using BigDecimal, since this
     *   make the transformation from string of bits to a double
     *   easier.
     **/
    private static BigDecimal[] _twoRaisedTo = new BigDecimal[TWORAISEDTOSIZE];

    ///////////////////////////////////////////////////////////////////
    ////                      static initializers                  ////
    static {
        BigDecimal powerOf2 = BigDecimal.valueOf(1);

        for (int i = 0; i < _twoRaisedTo.length; i++) {
            _twoRaisedTo[i] = powerOf2;
            powerOf2 = powerOf2.add(powerOf2);
        }
    }

}
