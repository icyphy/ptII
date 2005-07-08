/* The numeric rounding strategy classes.

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
//// Rounding

/**
 The Rounding class provides a type safe enumeration of strategies for
 handling loss of numeric resolution. Rounding is typically resolved when
 quantization constraints are applied to a computed result in order
 to satisfy the requirements of the type to which the result is to be
 assigned.
 <p>
 The rounding strategies are
 <ul>
 <li>
 <i>ceiling</i>: Round all inexact values towards plus infinity.
 <li>
 <i>floor</i> or <i>truncate</i>: Round all inexact values towards minus
 infinity.
 <li>
 <i>up</i>: Round all inexact values away from zero.
 <li>
 <i>down</i>: Round all inexact values towards zero.
 <li>
 <i>half_ceiling</i> or <i>nearest</i>: Round to the nearest value and
 the half-way value towards plus infinity.
 <li>
 <i>half_floor</i>: Round to the nearest value and the
 half-way value towards minus infinity.
 <li>
 <i>half_up</i>: Round to the nearest value and the half-way value away
 from zero.
 <li>
 <i>half_down</i>: Round to the nearest value and the half-way value towards
 zero.
 <li>
 <i>half_even</i> or <i>convergent</i>: Round to the nearest value and the
 half-way value to an even value.
 <li>
 <i>unnecessary</i>: Generate an exception for any inexact value.
 </ul>

 A specific strategy may be chosen dynamically by invoking forName() or
 getName() with one of the above strategy names. Alternatively a strategy
 may be selected by using one of the static singletons.
 <p>
 The <i>truncate</i> and <i>nearest</i> strategies should be preferred since they
 correspond to capabilities available on many processors. Other
 rounding strategies may require costly code on practical hardware.
 <p>
 The active class functionality is provided by the quantize method which is
 normally invoked from Quantization.quantize.

 @author Ed Willink
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (Ed.Willink)
 @Pt.AcceptedRating Red
 */
public abstract class Rounding implements Cloneable, Serializable {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return this, that is, return the reference to this object.
     *  @return This Rounding.
     */
    public Object clone() {
        return this;
    }

    /** Determine if the argument represents the same Rounding as this
     *  object.
     *  @param object Another object.
     *  @return True if the argument represents the same Rounding as
     *   this object; false otherwise.
     */
    public boolean equals(Object object) {
        // since Rounding is a type safe enumeration, can use == to
        // test equality.
        return this == object;
    }

    /** Return an instance of this class with the specified name.
     *  @return An instance of Rounding or null.
     */
    public static Rounding forName(String name) {
        return (Rounding) _nameToRounding.get(name);
    }

    /** Return an instance of this class with the specified name,
     *  or null if none exists.
     *  @return An instance of Rounding.
     *  @exception IllegalArgumentException If the string does not
     *   match one of the known strategies.
     */
    public static Rounding getName(String name) throws IllegalArgumentException {
        Rounding rounding = (Rounding) _nameToRounding.get(name);

        if (rounding != null) {
            return rounding;
        }

        throw new IllegalArgumentException("Unknown rounding strategy \""
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
     *  @param type An instance of Rounding.
     *  @return True if the argument is compatible with this type.
     */

    //    public boolean isCompatible(Rounding type) {
    //        if (this == UNKNOWN) {
    //            return true;
    //        }
    //        int typeInfo = RoundingLattice.compare(this, type);
    //        return (typeInfo == CPO.SAME || typeInfo == CPO.HIGHER);
    //    }
    /** Return the value of intPart after adjustment for loss of fracPart.
     *  @return The rounded value.
     */
    public BigInteger quantize(BigInteger intPart, double fracPart) {
        if (fracPart < 0) {
            intPart = intPart.subtract(BigInteger.ONE);
            fracPart += 1.0;
        }

        if (!_roundUp(intPart, fracPart)) {
            return intPart;
        }

        return intPart.add(BigInteger.ONE);
    }

    /** Return the string representation of this rounding.
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

    /** The general rounding strategy at the top of the rounding lattice */
    public static class General extends Rounding {
        private General() {
            super("general");
        }

        protected boolean _roundUp(BigInteger intPart, double fracPart) {
            return fracPart >= 0.5;
        }
    }

    public static final General GENERAL = new General();

    /** The round towards plus infinity rounding strategy */
    public static class RoundCeiling extends Rounding {
        private RoundCeiling() {
            super("ceiling");
        }

        protected boolean _roundUp(BigInteger intPart, double fracPart) {
            return fracPart > 0.0;
        }
    }

    public static final RoundCeiling CEILING = new RoundCeiling();

    /** The round towards zero rounding strategy */
    public static class RoundDown extends Rounding {
        private RoundDown() {
            super("down");
        }

        protected boolean _roundUp(BigInteger intPart, double fracPart) {
            if (intPart.signum() >= 0) {
                return false;
            } else {
                return fracPart > 0.0;
            }
        }
    }

    public static final RoundDown DOWN = new RoundDown();

    /** The round towards minus infinity (truncate) rounding strategy */
    public static class RoundFloor extends Rounding {
        private RoundFloor() {
            super("floor");
            _addRounding(this, "truncate");
        }

        protected boolean _roundUp(BigInteger intPart, double fracPart) {
            return false;
        }
    }

    public static final RoundFloor FLOOR = new RoundFloor();

    public static final RoundFloor TRUNCATE = FLOOR;

    /** The round to nearest and halves towards plus infinity (nearest)
     *  rounding strategy */
    public static class RoundHalfCeiling extends Rounding {
        private RoundHalfCeiling() {
            super("half_ceiling");
            _addRounding(this, "nearest");
            _addRounding(this, "round"); // For compatibility
        }

        protected boolean _roundUp(BigInteger intPart, double fracPart) {
            return fracPart >= 0.5;
        }
    }

    public static final RoundHalfCeiling HALF_CEILING = new RoundHalfCeiling();

    public static final RoundHalfCeiling NEAREST = HALF_CEILING;

    /** The round to nearest and halves towards zero rounding strategy */
    public static class RoundHalfDown extends Rounding {
        private RoundHalfDown() {
            super("half_down");
        }

        protected boolean _roundUp(BigInteger intPart, double fracPart) {
            if (fracPart != 0.5) {
                return fracPart > 0.5;
            } else if (intPart.signum() >= 0) {
                return false;
            } else {
                return fracPart > 0.0;
            }
        }
    }

    public static final RoundHalfDown HALF_DOWN = new RoundHalfDown();

    /** The round to nearest and halves towards the even value (convergent)
     * rounding strategy */
    public static class RoundHalfEven extends Rounding {
        private RoundHalfEven() {
            super("half_even");
            _addRounding(this, "convergent");
        }

        protected boolean _roundUp(BigInteger intPart, double fracPart) {
            if (fracPart != 0.5) {
                return fracPart > 0.5;
            }

            return intPart.testBit(0);
        }
    }

    public static final RoundHalfEven HALF_EVEN = new RoundHalfEven();

    public static final RoundHalfEven CONVERGENT = HALF_EVEN;

    /** The round to nearest and halves towards minus infinity
     * rounding strategy */
    public static class RoundHalfFloor extends Rounding {
        private RoundHalfFloor() {
            super("half_floor");
        }

        protected boolean _roundUp(BigInteger intPart, double fracPart) {
            return fracPart > 0.5;
        }
    }

    public static final RoundHalfFloor HALF_FLOOR = new RoundHalfFloor();

    /** The round to nearest and halves away from zero rounding strategy */
    public static class RoundHalfUp extends Rounding {
        private RoundHalfUp() {
            super("half_up");
        }

        protected boolean _roundUp(BigInteger intPart, double fracPart) {
            if (fracPart != 0.5) {
                return fracPart > 0.5;
            } else if (intPart.signum() >= 0) {
                return fracPart > 0.0;
            } else {
                return false;
            }
        }
    }

    public static final RoundHalfUp HALF_UP = new RoundHalfUp();

    /** The no rounding necessary rounding strategy */
    public static class RoundUnnecessary extends Rounding {
        private RoundUnnecessary() {
            super("unnecessary");
        }

        protected boolean _roundUp(BigInteger intPart, double fracPart) {
            if (fracPart != 0.0) {
                throw new ArithmeticException("Rounding necessary.");
            }

            return false;
        }
    }

    public static final RoundUnnecessary UNNECESSARY = new RoundUnnecessary();

    /** The round away from zero rounding strategy */
    public static class RoundUp extends Rounding {
        private RoundUp() {
            super("up");
        }

        protected boolean _roundUp(BigInteger intPart, double fracPart) {
            if (intPart.signum() >= 0) {
                return fracPart > 0.0;
            } else {
                return false;
            }
        }
    }

    public static final RoundUp UP = new RoundUp();

    /** The unknown rounding strategy at the bottom of the rounding
     * lattice */
    public static class Unknown extends Rounding {
        private Unknown() {
            super("unknown");
        }

        protected boolean _roundUp(BigInteger intPart, double fracPart) {
            return fracPart >= 0.5;
        }
    }

    public static final Unknown UNKNOWN = new Unknown();

    ///////////////////////////////////////////////////////////////////
    ////                     protected constructor                 ////
    // The constructor is protected to make a type safe enumeration.
    protected Rounding(String name) {
        _name = name;
        _addRounding(this, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if intPart should be incremented to represent
     *  the rounded value of intPart + fracPart, which is positive.
     */
    protected abstract boolean _roundUp(BigInteger intPart, double fracPart);

    ///////////////////////////////////////////////////////////////////
    ////                    package private method                 ////
    // Add entries in this class to index the given name to
    // the given rounding type.
    static void _addRounding(Rounding type, String name) {
        // Because the private variables are below the public variables
        // that call this initializer,
        // it doesn't work to initialize this statically.
        if (_nameToRounding == null) {
            _nameToRounding = new HashMap();
        }

        _nameToRounding.put(name, type);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private String _name;

    // A map from rounding type name to the rounding type for all rounding
    //  types.
    private static Map _nameToRounding;
}
