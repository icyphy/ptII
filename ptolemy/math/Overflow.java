/* The overflow strategy classes.

 Copyright (c) 2002-2013 The Regents of the University of California.
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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

///////////////////////////////////////////////////////////////////
//// Overflow

/**
 The Overflow class provides a type safe enumeration of strategies for
 handling numeric overflows for FixPoint data types.
 Overflows are typically resolved after performing a FixPoint
 arithmetic operation or transformation.
 <p>
 The active class functionality is provided by the
 {@link #quantize(BigInteger, Precision) quantize} method. This method
 evaluates an unscaled {@link BigInteger BigInteger} value and a
 Precision constraint and creates a FixPoint object with a new
 unscaled value and precision that conforms to the corresponding
 overflow strategy. Depending on the overflow strategy used,
 the unscaled integer value or the precision may be changed as
 part of the overflow process.
 <p>
 The overflow strategies supported are as follows:
 <ul>

 <li> <i><b>grow</b></i> <br>
 If the integer value does not fall within the dynamic range
 of the Precision constraint, increase the Precision to the
 minimum dynamic range needed to include the integerValue.
 Note that the actual integer unscaled value will not change
 during this overflow strategy.
 This rounding mode is supported by the static
 {@link #quantizeGrow(BigInteger, Precision) quantizeGrow} method
 and the Overflow singletons {@link #GROW} and {@link #GENERAL}.

 <li> <i><b>minimize</b></i> <br>
 Represent the unscaled integer value with the minimum
 number of data bits. The sign and exponent of the
 new precision constraint is determined by the Precision
 parameter. The use of this overflow strategy may increase
 or decrease the Precision of the value.
 Note that the actual integer unscaled value will not change
 during this overflow strategy.
 This rounding mode is supported by the static
 {@link #quantizeMinimum(BigInteger, Precision) quantizeMinimize}
 method and the Overflow singleton {@link #MINIMIZE} .

 <li> <i><b>modulo</b></i> <br>
 This overflow strategy will perform a twos complement modulo
 operation any integer values that are outside of the
 dynamic range of the Precision constraint.
 Note that the actual precision will not change
 during this overflow strategy.
 This rounding mode is supported by the static
 {@link #quantizeModulo(BigInteger, Precision) quantizeModulo}
 method and the Overflow singletons {@link #MODULO} and {@link #WRAP}.

 <li> <i><b>saturate</b></i> <br>
 If the integer value falls outside of the Precision dynamic
 range, this overflow strategy will saturate result.
 If the value is below the minimum of the range, the
 minimum value of the range is used. If the value is above
 the maximum of the range, the maximum value of the range
 is used.
 Note that the precision will not change during this overflow strategy.
 This rounding mode is supported by the static
 {@link #quantizeSaturate(BigInteger, Precision) quantizeSaturate}
 method and the Overflow singletons {@link #SATURATE} and
 {@link #CLIP}.

 <li> <i><b>to_zero</b></i> <br>
 If the integer value falls outside of the Precision dynamic
 range, this overflow strategy will set the integer value to zero.
 Note that the actual precision will not change
 during this overflow strategy.
 This rounding mode is supported by the static
 {@link #quantizeToZero(BigInteger, Precision) quantizeToZero}
 method and the Overflow singleton {@link #TO_ZERO}.

 <li> <i><b>trap</b></i> <br>
 If the integer value falls outside of the Precision dynamic
 range, a {@link java.lang.ArithmeticException} is generated.
 This rounding mode is supported by the
 singleton {@link #TRAP}.
 </ul>

 <p>
 A specific strategy may be chosen dynamically by invoking
 {@link #forName forName} or {@link #getName getName}
 with one of the above strategy names. Alternatively a strategy
 may be selected by using one of the static singletons.
 <p>
 Division by zero can trigger the use of the plusInfinity or minusInfinity
 methods, which return null, except in the case of the <i>to_zero</i>
 and <i>saturate</i> strategies for which infinity is well-defined.

 @author Ed Willink
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (Ed.Willink)
 @Pt.AcceptedRating Red
 @see ptolemy.math.FixPoint
 @see ptolemy.math.Quantization
 @see ptolemy.math.Rounding
 */
public abstract class Overflow implements Cloneable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return this, that is, return the reference to this object.
     *  @return This Overflow.
     */
    public Object clone() {
        return this;
    }

    /** Determine if the argument represents the same Overflow as this
     *  object.
     *  @param object Another object.
     *  @return True if the argument represents the same Overflow as
     *   this object; false otherwise.
     */
    public boolean equals(Object object) {
        // since Overflow is a type safe enumeration, can use == to
        // test equality.
        return this == object;
    }

    /** Return an instance of this class with the specified name.
     *
     *  @param name The name of the Overflow strategy to find.
     *  @return An instance of Overflow or null.
     */
    public static Overflow forName(String name) {
        return (Overflow) _nameToOverflow.get(name);
    }

    /** Return an instance of this class with the specified name,
     *  or null if none exists.
     *
     *  @param name The name of the Overflow strategy to find.
     *  @return An instance of Overflow.
     *  @exception IllegalArgumentException If the string does not
     *   match one of the known strategies.
     */
    public static Overflow getName(String name) throws IllegalArgumentException {
        Overflow overflow = (Overflow) _nameToOverflow.get(name);

        if (overflow != null) {
            return overflow;
        }

        throw new IllegalArgumentException("Unknown overflow strategy \""
                + name + "\".");
    }

    /** Return a hash code value for this object.
     */
    public int hashCode() {
        return _name.hashCode();
    }

    /**
     * Determines whether the given BigInteger unscaled value is considered
     * an "underflow" or an "overflow" under the given Precision constraint.
     * This will occur if the value is less than the minimum unscaled value of
     * the given Precision constraint or more than the maximum unscaled value
     * of the given Precision constraint.
     *
     * @param bigInt The value to test for underflow.
     * @param precision The Precision constraint to use for the test.
     * @return true if the value is considered an "underflow" or "overflow,
     * false otherwise.
     */
    public static boolean isOutOfRange(BigInteger bigInt, Precision precision) {
        if (bigInt.compareTo(precision.getMaximumUnscaledValue()) > 0
                || bigInt.compareTo(precision.getMinimumUnscaledValue()) < 0) {
            return true;
        }
        return false;
    }

    /**
     * Determines whether the given BigInteger unscaled value is considered
     * an "overflow" under the given Precision constraint. This will occur
     * if the value is larger than the maximum unscaled value of
     * the given Precision constraint.
     * (@see Precision#getMaximumUnscaledValue())
     *
     * @param value The value to test for overflow.
     * @param precision The Precision constraint to use for the test.
     * @return true if the value is considered an "overflow", false if
     * it is not an "overflow".
     */
    public static boolean isOverflow(BigInteger value, Precision precision) {
        if (value.compareTo(precision.getMaximumUnscaledValue()) > 0) {
            return true;
        }
        return false;
    }

    /**
     * Determines whether the given BigInteger unscaled value is considered
     * an "underflow" under the given Precision constraint. This will occur
     * if the value is less than the minimum unscaled value of
     * the given Precision constraint.
     * (@see Precision#getMinimumUnscaledValue())
     *
     * @param bigInt The value to test for underflow.
     * @param precision The Precision constraint to use for the test.
     * @return true if the value is considered an "underflow", false if
     * it is not an "underflow".
     */
    public static boolean isUnderflow(BigInteger bigInt, Precision precision) {
        if (bigInt.compareTo(precision.getMinimumUnscaledValue()) < 0) {
            return true;
        }
        return false;
    }

    /**
     * Return an iterator for the names of all overflow types.
     * @return An iterator for the names of all overflow types.
     */
    public static Iterator nameIterator() {
        return _nameToOverflow.keySet().iterator();
    }

    /** Return the value of minus infinity, or null if unrepresentable.
     *  <p>
     *  The saturation value is returned for the <i>saturate</i> and
     *  <i>to_zero</i> strategies for which infinity is quantizable.
     *  Null is returned for other strategies.
     *
     *  @param quant The quantization specification.
     *  @return The value if defined, null if not..
     */
    public BigInteger minusInfinity(Quantization quant) {
        return null;
    }

    /** Return the value of plus infinity, or null if unrepresentable.
     *  <p>
     *  The saturation value is returned for the <i>saturate</i> and
     *  <i>to_zero</i> strategies for which infinity is quantizable.
     *  Null is returned for other strategies.
     *  @param quant The quantization specification.
     *  @return The value if defined, null if not.
     */
    public BigInteger plusInfinity(Quantization quant) {
        return null;
    }

    /** Return a new FixPoint object based on the given BigInteger
     *  value and Precision constraint. This method will return
     *  a valid FixPoint object that conforms to the given
     *  overflow strategy implemented by the extending class.
     *
     *  @param integerValue The unbounded integer value.
     *  @param precision The Precision constraint of the quantization.
     *  @return A valid FixPoint value that conforms to the overflow
     *  strategy.
     */
    abstract public FixPoint quantize(BigInteger integerValue,
            Precision precision);

    /**
     * Quantize a FixPoint value using a "grow" overflow strategy.
     * If the Precision format does not provide sufficient dynamic range
     * for the value, increase the Precision to the minimum dynamic range
     * needed to include the integerValue. Note that the actual
     * integer unscaled value will not change during this overflow
     * strategy.
     *
     * @param integerValue unscaled integer value to check for overflow
     * @param precision the precision constraint used for the overflow check
     * @return Valid FixPoint data object
     */
    public static FixPoint quantizeGrow(BigInteger integerValue,
            Precision precision) {
        if (isOutOfRange(integerValue, precision)) {
            return quantizeMinimum(integerValue, precision);
        }
        return new FixPoint(integerValue, precision);
    }

    /**
     * Generates a new FixPoint data value based on the unscaled
     * value bigInt using as few bits as possible. The sign and
     * exponent of the precision are obtained from the Precision
     * parameter.
     *
     * @param bigInt Unscaled value to use for the FixPoint result
     * @param p Used to obtain the sign and exponent of the new FixPoint value.
     * @return FixPoint value with as few bits as necessary.
     */
    public static FixPoint quantizeMinimum(BigInteger bigInt, Precision p) {
        int sign = p.isSigned() ? 1 : 0;
        int int_bits = bigInt.bitLength();
        if (int_bits == 0) {
            int_bits++;
        }
        int new_bits = int_bits + sign;

        Precision newPrecision = new Precision(sign, new_bits, p.getExponent());
        return new FixPoint(bigInt, newPrecision);

    }

    /**
     * Quantize a FixPoint value using a "modulo" overflow strategy.
     * If the unscaled integer value is outside of the dynamic range
     * provided by the Precision constraint, modify the integer
     * value to fit within the dynamic range using a
     * modulo operation. Note that the Precision of the FixPoint
     * value remains the same.
     *
     * @param integerValue unscaled integer value to check for overflow
     * @param precision the precision constraint used for the overflow check
     * @return Valid FixPoint data object
     */
    public static FixPoint quantizeModulo(BigInteger integerValue,
            Precision precision) {
        if (!isOutOfRange(integerValue, precision)) {
            return new FixPoint(integerValue, precision);
        }

        BigInteger moduloInteger = null;

        if (!precision.isSigned()) {
            // If the precision is unsigned, the BigInteger will
            // be positive and the "modulo" value is the
            // remainder of "integerValue divided by # of levels".
            moduloInteger = integerValue.remainder(precision
                    .getNumberOfLevels());
        } else {
            // If the precision is signed, we need to perform a
            // twos complement modulo.

            BigInteger minValue = precision.getMinimumUnscaledValue();
            moduloInteger = integerValue.subtract(minValue);

            BigInteger modValue = precision.getNumberOfLevels();
            moduloInteger = moduloInteger.remainder(modValue);

            if (integerValue.signum() < 0) {
                moduloInteger = moduloInteger.add(modValue);
            }
            moduloInteger = moduloInteger.add(minValue);
        }

        return new FixPoint(moduloInteger, precision);
    }

    /**
     * Quantize a FixPoint value using a "saturate" overflow strategy.
     * If the unscaled integer value is outside of the dynamic range
     * provided by the Precision constraint, modify the integer
     * value to fit within the dynamic range by saturating the
     * value to the maximum or minimum value supported by the
     * Precision constraint. Note that the Precision of the FixPoint
     * value remains the same.
     *
     * @param integerValue unscaled integer value to check for overflow
     * @param precision the precision constraint used for the overflow check
     * @return Valid FixPoint data object
     */
    public static FixPoint quantizeSaturate(BigInteger integerValue,
            Precision precision) {
        if (isUnderflow(integerValue, precision)) {
            return new FixPoint(precision.getMinimumUnscaledValue(), precision);
        }
        if (isOverflow(integerValue, precision)) {
            return new FixPoint(precision.getMaximumUnscaledValue(), precision);
        }
        return new FixPoint(integerValue, precision);
    }

    /**
     * Quantize a FixPoint value using a "to Zero" overflow strategy.
     * If the unscaled integer value is outside of the dynamic range
     * provided by the Precision constraint, set the integer
     * value to zero. Note that the Precision of the FixPoint
     * value remains the same.
     *
     * @param integerValue unscaled integer value to check for overflow
     * @param precision the precision constraint used for the overflow check
     * @return Valid FixPoint data object
     */
    public static FixPoint quantizeToZero(BigInteger integerValue,
            Precision precision) {
        if (isOutOfRange(integerValue, precision)) {
            return new FixPoint(BigInteger.ZERO, precision);
        }
        return new FixPoint(integerValue, precision);
    }

    /** Return the string representation of this overflow.
     *  @return A String.
     */
    public String toString() {
        return _name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    // NOTE: It may seem strange that these inner classes are built this
    // way instead of as anonymous classes...  This code was copied from
    // ptolemy.data.type.BaseType where an explanation that was valid
    // for that usage may be found.

    /** Singleton implementing Grow overflow strategy. */
    public static final Grow GROW = new Grow();

    /** Singleton implementing Modulo overflow strategy. */
    public static final Modulo MODULO = new Modulo();

    /** Singleton implementing Minimize overflow strategy. */
    public static final Minimize MINIMIZE = new Minimize();

    /** Singleton implementing Trap overflow strategy. */
    public static final Trap TRAP = new Trap();

    /** Singleton implementing Saturate overflow strategy. */
    public static final Saturate SATURATE = new Saturate();

    /** Singleton implementing to zero overflow strategy. */
    public static final ToZero TO_ZERO = new ToZero();

    // The following singleton classes are listed below (and out of
    // alphabetic order) because they depend on the construction of
    // other singleton objects before they can be defined.

    /** Singleton implementing Saturate overflow strategy. */
    public static final Saturate CLIP = SATURATE;

    /** Singleton implementing Grow overflow strategy. */
    public static final Grow GENERAL = GROW;

    /** Singleton implementing Modulo overflow strategy. */
    public static final Modulo WRAP = MODULO;

    /** Singleton implementing Trap overflow strategy. */
    public static final Trap THROW = TRAP;

    /** The grow overflow strategy. Allow growth of value. */
    public static class Grow extends Overflow {
        private Grow() {
            super("grow");
        }

        public FixPoint quantize(BigInteger integerValue, Precision precision) {
            return quantizeGrow(integerValue, precision);
        }

    }

    /** The minimize overflow strategy. */
    public static class Minimize extends Overflow {
        private Minimize() {
            super("minimize");
            _addOverflow(this, "shrink");
        }

        public FixPoint quantize(BigInteger integerValue, Precision precision) {
            return quantizeMinimum(integerValue, precision);
        }

    }

    /** The modulo overflow strategy. */
    public static class Modulo extends Overflow {
        private Modulo() {
            super("modulo");
            _addOverflow(this, "wrap");
        }

        public FixPoint quantize(BigInteger integerValue, Precision precision) {
            return quantizeModulo(integerValue, precision);
        }

    }

    /** The saturate overflows strategy. */
    public static class Saturate extends Overflow {
        private Saturate() {
            super("saturate");
            _addOverflow(this, "clip");
        }

        public BigInteger minusInfinity(Quantization quant) {
            return quant.getMinimumUnscaledValue();
        }

        public BigInteger plusInfinity(Quantization quant) {
            return quant.getMaximumUnscaledValue();
        }

        public FixPoint quantize(BigInteger integerValue, Precision precision) {
            return quantizeSaturate(integerValue, precision);
        }
    }

    /** The overflow to zero strategy. */
    public static class ToZero extends Overflow {
        private ToZero() {
            super("to_zero");
            _addOverflow(this, "overflow_to_zero"); // For compatibility.
        }

        public BigInteger minusInfinity(Quantization quant) {
            return BigInteger.ZERO;
        }

        public BigInteger plusInfinity(Quantization quant) {
            return BigInteger.ZERO;
        }

        public FixPoint quantize(BigInteger integerValue, Precision precision) {
            return quantizeToZero(integerValue, precision);
        }
    }

    /** The trap overflows strategy. */
    public static class Trap extends Overflow {
        private Trap() {
            super("trap");
            _addOverflow(this, "throw");
        }

        public FixPoint quantize(BigInteger integerValue, Precision precision) {
            if (isUnderflow(integerValue, precision)) {
                throw new ArithmeticException("Minimum overflow threshold of "
                        + precision.getMinimumUnscaledValue()
                        + " exceeded with value " + integerValue);
            }
            if (isOverflow(integerValue, precision)) {
                throw new ArithmeticException("Maximum overflow threshold of "
                        + precision.getMaximumUnscaledValue()
                        + " exceeded with value " + integerValue);
            }
            return new FixPoint(integerValue, precision);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected constructor                 ////

    /** Construct an Overflow object with the given String name.
     *  This name is used for finding an Overflow object at a later
     *  time (@see #forName(String)). This constructor
     *  is protected to make a type safe enumeration.
     *
     * @param name The String name to give this Overflow strategy.
     *
     */
    protected Overflow(String name) {
        _name = name;
        _addOverflow(this, name);
    }

    // The constructor is protected to make a type safe enumeration.

    ///////////////////////////////////////////////////////////////////
    ////                    package private method                 ////
    // Add entries in this class to index the given name to
    // the given overflow type.
    static void _addOverflow(Overflow type, String name) {
        // Because the private variables are below the public variables
        // that call this initializer,
        // it doesn't work to initialize this statically.
        if (_nameToOverflow == null) {
            _nameToOverflow = new HashMap();
        }

        _nameToOverflow.put(name, type);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private String _name;

    // A map from overflow type name to the overflow type for all
    //  overflow types.
    private static volatile Map _nameToOverflow;

}
