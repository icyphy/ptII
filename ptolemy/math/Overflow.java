/* The overflow strategy classes.

Copyright (c) 2002-2005 The Regents of the University of California.
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

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


//////////////////////////////////////////////////////////////////////////
//// Overflow

/**
   The Overflow class provides a type safe enumeration of strategies for
   handling numeric overflows. Overflows are typically resolved when
   quantization constraints are applied to a computed result in order to
   satisfy the requirements of the type to which the result is to be assigned.
   <p>
   The overflow strategies are
   <ul>
   <li>
   <i>grow</i>: Out of range values provoke precision growth.
   <li>
   <i>modulo</i> or <i>wrap</i>: Out of range values are wrapped around.
   <li>
   <i>to_zero</i>: Out of range values are set to zero.
   <li>
   <i>saturate</i> or <i>clip</i>: Out of range values are saturated to the
   nearest extreme value.
   <li>
   <i>trap</i> or <i>throw</i>: Out of range values throw an exception.
   </ul>
   <p>
   A specific strategy may be chosen dynamically by invoking forName() or
   getName() with one of the above strategy names. Alternatively a strategy
   may be selected by using one of the static singletons.
   <p>
   The active class functionality is provided by the quantize method which is
   normally invoked from Quantization.quantize.
   <p>
   Division by zero can trigger the use of the plusInfinity or minusInfinity
   methods, which return null, except in the case of the <i>to_zero</i>
   and <i>saturate</i> strategies for which infinity is well-defined.

   @author Ed Willink
   @version $Id$
   @since Ptolemy II 2.1
   @Pt.ProposedRating Red (Ed.Willink)
   @Pt.AcceptedRating Red
   @see FixPoint
   @see Quantization
   @see Rounding
*/
public abstract class Overflow implements Cloneable, Serializable {
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
     *  @return An instance of Overflow or null.
     */
    public static Overflow forName(String name) {
        return (Overflow) _nameToOverflow.get(name);
    }

    /** Return an instance of this class with the specified name,
     *  or null if none exists.
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

    /** Test if the argument type is compatible with this type. The method
     *  returns true if this type is UNKNOWN, since any type is a
     *  substitution instance of it. If this type is not UNKNOWN, this
     *  method returns true if the argument type is less than or equal to
     *  this type in the type lattice, and false otherwise.
     *  @param type An instance of Overflow.
     *  @return True if the argument is compatible with this type.
     */

    //    public boolean isCompatible(Overflow type) {
    //        if (this == UNKNOWN) {
    //            return true;
    //        }
    //        int typeInfo = OverflowLattice.compare(this, type);
    //        return (typeInfo == CPO.SAME || typeInfo == CPO.HIGHER);
    //    }

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

    /** Return the integerValue after applying the overflow constraints of
     *  quant.
     *  @param integerValue The unbounded integer value.
     *  @param quant The quantization constraints.
     *  @return The bounded integer value.
     */
    abstract public BigInteger quantize(BigInteger integerValue,
        Quantization quant);

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

    /** The general overflow strategy at the top of the overflow lattice */
    public static class General extends Overflow {
        private General() {
            super("general");
        }

        public BigInteger quantize(BigInteger integerValue, Quantization quant) {
            return integerValue;
        }
    }

    public static final General GENERAL = new General();

    /** The grow overflow strategy */
    public static class Grow extends Overflow {
        private Grow() {
            super("grow");
        }

        public BigInteger quantize(BigInteger integerValue, Quantization quant) {
            return integerValue;
        }
    }

    public static final Grow GROW = new Grow();

    /** The modulo overflow strategy */
    public static class Modulo extends Overflow {
        private Modulo() {
            super("modulo");
            _addOverflow(this, "wrap");
        }

        public BigInteger quantize(BigInteger integerValue, Quantization quant) {
            BigInteger minValue = quant.getMinimumUnscaledValue();
            BigInteger maxValue = quant.getMaximumUnscaledValue();

            if ((0 <= integerValue.compareTo(minValue))
                            && (integerValue.compareTo(maxValue) <= 0)) {
                return integerValue;
            }

            integerValue = integerValue.subtract(minValue);

            BigInteger modValue = quant.getModuloUnscaledValue();
            integerValue = integerValue.remainder(modValue);

            if (integerValue.signum() < 0) {
                integerValue = integerValue.add(modValue);
            }

            integerValue = integerValue.add(minValue);
            return integerValue;
        }
    }

    public static final Modulo MODULO = new Modulo();
    public static final Modulo WRAP = MODULO;

    /** The saturate overflows strategy */
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

        public BigInteger quantize(BigInteger integerValue, Quantization quant) {
            BigInteger minValue = quant.getMinimumUnscaledValue();

            if (integerValue.compareTo(minValue) < 0) {
                return minValue;
            }

            BigInteger maxValue = quant.getMaximumUnscaledValue();

            if (integerValue.compareTo(maxValue) > 0) {
                return maxValue;
            }

            return integerValue;
        }
    }

    public static final Saturate SATURATE = new Saturate();
    public static final Saturate CLIP = SATURATE;

    /** The overflow to zero strategy */
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

        public BigInteger quantize(BigInteger integerValue, Quantization quant) {
            BigInteger minValue = quant.getMinimumUnscaledValue();

            if (integerValue.compareTo(minValue) < 0) {
                return BigInteger.ZERO;
            }

            BigInteger maxValue = quant.getMaximumUnscaledValue();

            if (integerValue.compareTo(maxValue) > 0) {
                return BigInteger.ZERO;
            }

            return integerValue;
        }
    }

    public static final ToZero TO_ZERO = new ToZero();

    /** The trap overflows strategy */
    public static class Trap extends Overflow {
        private Trap() {
            super("trap");
            _addOverflow(this, "throw");
        }

        public BigInteger quantize(BigInteger integerValue, Quantization quant) {
            BigInteger minValue = quant.getMinimumUnscaledValue();

            if (integerValue.compareTo(minValue) < 0) {
                throw new ArithmeticException(
                    "Minimum overflow threshold exceeded.");
            }

            BigInteger maxValue = quant.getMaximumUnscaledValue();

            if (integerValue.compareTo(maxValue) > 0) {
                throw new ArithmeticException(
                    "Maximum overflow threshold exceeded.");
            }

            return integerValue;
        }
    }

    public static final Trap TRAP = new Trap();
    public static final Trap THROW = TRAP;

    /** The unknown overflow strategy at the bottom of the overflow
     * lattice */
    public static class Unknown extends Overflow {
        private Unknown() {
            super("unknown");
        }

        public BigInteger quantize(BigInteger integerValue, Quantization quant) {
            return integerValue;
        }
    }

    public static final Unknown UNKNOWN = new Unknown();

    ///////////////////////////////////////////////////////////////////
    ////                     protected constructor                 ////
    // The constructor is protected to make a type safe enumeration.
    protected Overflow(String name) {
        _name = name;
        _addOverflow(this, name);
    }

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
    private static Map _nameToOverflow;
}
