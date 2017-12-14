/* The numeric rounding strategy classes.

 Copyright (c) 2002-2014 The Regents of the University of California.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

///////////////////////////////////////////////////////////////////
//// Rounding

/**
 The Rounding class provides a type safe enumeration of strategies for
 handling loss of numeric resolution when rounding. Rounding is typically
 resolved when quantization constraints are applied to a computed result
 in order to satisfy the requirements of the type to which the result is to be
 assigned.
 <p>
 Rounding is an abstract class for all rounding strategies.
 The primary method of this class is {@link #round(BigDecimal) round}. This
 method will round a BigDecimal value to the appropriate integer
 and return a BigInteger object.</p>
 <p>
 {@link BigDecimal} objects are rounded by calling the
 {@link BigDecimal#setScale(int)} method with the appropriate
 rounding mode. Several static methods are provided in this class
 to support this functionality for each of the supported rounding
 modes. In addition, several static singleton rounding strategies
 are provided by this class to implement one of the supported
 routing modes.  Each of these rounding strategies are modeled
 after the rounding strategies provided by {@link BigDecimal}
 and include:</p>

 <ul>

 <li> <i><b>up</b></i> <br>
 Rounding mode to round away from zero.
 Always increments the digit prior to a non-zero discarded fraction.
 Note that this rounding mode never decreases the magnitude of
 the calculated value.
 This rounding mode is supported by the static
 {@link #roundUp(BigDecimal) roundUp} method and the Rounding
 singletons {@link #GENERAL}, {@link #UNKNOWN} and {@link #UP}.</li>

 <li> <i><b>down</b></i> <br>
 Rounding mode to round towards zero.
 Never increments the digit prior to a discarded fraction
 (i.e., truncates). Note that this rounding mode never increases
 the magnitude of the calculated value.
 This rounding mode is supported by the static
 {@link #roundDown(BigDecimal) roundDown} method and the Rounding
 singleton {@link #DOWN}.</li>

 <li> <i><b>floor</b></i> <br>
 Rounding mode to round towards negative infinity.
 If decimal is positive, behave as <b>round down</b>;
 if decimal is negative, behave as <b>round up</b>.
 This rounding mode is supported by the static
 {@link #roundFloor(BigDecimal) roundFloor} method and the Rounding
 singleton {@link #FLOOR}.</li>

 <li> <i><b>ceiling</b></i> <br>
 Rounding mode to round towards positive infinity.
 If decimal is positive, behave as <b>round up</b>;
 if decimal is negative, behave as <b>round down</b>.
 This rounding mode is supported by the static
 {@link #roundCeiling(BigDecimal) roundCeiling} method and the Rounding
 singleton {@link #CEILING}.</li>

 <li> <i><b>half up</b></i> <br>
 Rounding mode to round towards "nearest neighbor" unless
 both neighbors are equidistant, in which case round up.
 Behaves as for <b>round up</b> if the discarded fraction is &ge; .5;
 otherwise, behaves as for <b>round down</b>. Note that this is the
 rounding mode that most of us were taught in grade school.
 Rounding mode to round towards zero.
 This rounding mode is supported by the static
 {@link #roundHalfUp(BigDecimal) roundHalfUp} method and the Rounding
 singleton {@link #HALF_UP}.</li>

 <li> <i><b>half down</b></i> <br>
 Rounding mode to round towards "nearest neighbor" unless
 both neighbors are equidistant, in which case round down.
 Behaves as for <b>round up</b> if the discarded fraction is &gt; .5;
 otherwise, behaves as for <b>ROUND_DOWN</b>.
 This rounding mode is supported by the static
 {@link #roundHalfDown(BigDecimal) roundHalfDown} method and the Rounding
 singleton {@link #HALF_DOWN}.</li>

 <li> <i><b>half even</b></i> <br>
 Rounding mode to round towards the "nearest neighbor" unless
 both neighbors are equidistant, in which case, round towards
 the even neighbor. Behaves as for <b>round half up</b>
 if the digit to the left of the discarded fraction is odd;
 behaves as for <b>round half down</b> if it's even.
 Note that this is the rounding
 mode that minimizes cumulative error when applied repeatedly
 over a sequence of calculations.
 This rounding mode is supported by the static
 {@link #roundHalfEven(BigDecimal) roundHalfEven} method and the Rounding
 singletons {@link #HALF_EVEN} and {@link #CONVERGENT}.</li>

 <li> <i><b>half floor</b></i> <br>
 Rounding mode to round towards "nearest neighbor" unless
 both neighbors are equidistant, in which case round
 "ceiling". Behaves as <b>round half down</b>
 if the decimal is positive and as <b>round half up</b>
 if the decimal is negative. Note that there is no half floor rounding
 mode supported for BigDecimal values.
 This rounding mode is supported by the static
 {@link #roundHalfFloor(BigDecimal) roundHalfFloor} method and
 the Rounding singleton {@link #HALF_FLOOR}.</li>

 <li> <i><b>half ceiling</b></i> <br>
 Rounding mode to round towards "nearest neighbor" unless
 both neighbors are equidistant, in which case round
 "ceiling". Behaves as <b>round half up</b> if the decimal
 is positive and as <b>round half down</b>
 if the decimal is negative.
 Note that there is no half ceiling rounding mode
 supported for BigDecimal values.
 This rounding mode is supported by the static
 {@link #roundHalfFloor(BigDecimal) roundHalfCeiling} method and
 the Rounding singleton {@link #HALF_CEILING}.</li>

 </ul>


 <p>A specific strategy may be chosen dynamically by invoking forName() or
 getName() with one of the above strategy names. Alternatively a strategy
 may be selected by using one of the static singletons.</p>
 <p>
 The <i>truncate</i> and <i>nearest</i> strategies should be
 preferred since they
 correspond to capabilities available on many processors. Other
 rounding strategies may require costly code on practical hardware.</p>
 <p>
 The active class functionality is provided by the quantize method which is
 normally invoked from Quantization.quantize.</p>

 @author Ed Willink, Contributor: Mike Wirthlin
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (Ed.Willink)
 @Pt.AcceptedRating Red
 */
public abstract class Rounding implements Cloneable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return this, that is, return the reference to this object.
     *  @return This Rounding.
     */
    @Override
    public Object clone() {
        return this;
    }

    /** Determine if the argument represents the same Rounding as this
     *  object.
     *  @param object Another object.
     *  @return True if the argument represents the same Rounding as
     *   this object; false otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // since Rounding is a type safe enumeration, can use == to
        // test equality.
        return this == object;
    }

    /** Return an instance of this class with the specified name.
     *  @param name Rounding mode name.
     *  @return An instance of Rounding or null if the given name
     *  does not match a valid rounding mode.
     */
    public static Rounding forName(String name) {
        return (Rounding) _nameToRounding.get(name);
    }

    /** Return an instance of this class with the specified name,
     *  or null if none exists.
     *
     *  @param name The name of the Rounding strategy to find.
     *  @return An instance of Rounding.
     *  @exception IllegalArgumentException If the string does not
     *   match one of the known strategies.
     */
    public static Rounding getName(String name)
            throws IllegalArgumentException {
        Rounding rounding = (Rounding) _nameToRounding.get(name);

        if (rounding != null) {
            return rounding;
        }

        throw new IllegalArgumentException(
                "Unknown rounding strategy \"" + name + "\".");
    }

    /** Return a hash code value for this object.    */
    @Override
    public int hashCode() {
        return _name.hashCode();
    }

    /**
     * Return an iterator for the names of all overflow types.
     * @return An iterator for the names of all overflow types.
     */
    public static Iterator nameIterator() {
        return _nameToRounding.keySet().iterator();
    }

    /**
     * Round the BigDecimal value using the appropriate rounding
     * strategy. The result is a BigInteger value rounded
     * appropriately. Each class that extends Rounding will provide
     * a mode specific round function.
     *
     * @param decimal value to be rounded
     * @return The rounded BigInteger.
     */
    public abstract BigInteger round(BigDecimal decimal);

    /** Rounding mode to round towards positive infinity.
     * If decimal is positive, behave as {@link #roundUp};
     * if decimal is negative, behave as {@link #roundDown}.
     *
     * @see BigDecimal#ROUND_CEILING
     *
     * @param decimal The BigDecimal value to round.
     * @return Rounded BigDecimal value
     * **/
    public static BigDecimal roundCeiling(BigDecimal decimal) {
        return decimal.setScale(0, BigDecimal.ROUND_CEILING);
    }

    /** Rounding mode to round towards zero.
     * Never increments the digit prior to a discarded fraction
     * (i.e., truncates). Note that this rounding mode never increases
     * the magnitude of the calculated value.
     *
     * @see BigDecimal#ROUND_DOWN
     *
     * @param decimal The BigDecimal value to round.
     * @return Rounded BigDecimal value
     * **/
    public static BigDecimal roundDown(BigDecimal decimal) {
        return decimal.setScale(0, BigDecimal.ROUND_DOWN);
    }

    /** Rounding mode to round towards negative infinity.
     * If decimal is positive, behave as {@link #roundDown};
     * if decimal is negative, behave as {@link #roundUp}.
     *
     * @see BigDecimal#ROUND_FLOOR
     *
     * @param decimal The BigDecimal value to round.
     * @return Rounded BigDecimal value
     * **/
    public static BigDecimal roundFloor(BigDecimal decimal) {
        return decimal.setScale(0, BigDecimal.ROUND_FLOOR);
    }

    /** Rounding mode to round towards "nearest neighbor" unless
     * both neighbors are equidistant, in which case round
     * "ceiling".
     * Behaves as HALF_ROUND_UP if the decimal is positive and
     * as HALF_ROUND_DOWN if the decimal is negative.
     * <p>
     *
     * Note that there is no half ceiling rounding mode
     * supported for BigDecimal values. This method uses a
     * combination of the {@link BigDecimal#ROUND_HALF_UP} and
     * {@link BigDecimal#ROUND_HALF_DOWN} to perform this
     * new rounding mode.
     *
     * @param decimal The BigDecimal value to round.
     * @return Rounded BigDecimal value
     * **/
    public static BigDecimal roundHalfCeiling(BigDecimal decimal) {
        if (decimal.signum() == -1) {
            return roundHalfDown(decimal);
        }
        return roundHalfUp(decimal);
    }

    /** Rounding mode to round towards "nearest neighbor" unless
     * both neighbors are equidistant, in which case round down.
     * Behaves as for ROUND_UP if the discarded fraction is &gt; .5;
     * otherwise, behaves as for ROUND_DOWN.
     *
     * @see BigDecimal#ROUND_HALF_UP
     *
     * @param decimal The BigDecimal value to round.
     * @return Rounded BigDecimal value
     * **/
    public static BigDecimal roundHalfDown(BigDecimal decimal) {
        return decimal.setScale(0, BigDecimal.ROUND_HALF_DOWN);
    }

    /** Rounding mode to round towards the "nearest neighbor" unless
     * both neighbors are equidistant, in which case, round towards
     * the even neighbor. Behaves as for ROUND_HALF_UP if the digit
     * to the left of the discarded fraction is odd; behaves as for
     * ROUND_HALF_DOWN if it's even. Note that this is the rounding
     * mode that minimizes cumulative error when applied repeatedly
     * over a sequence of calculations.
     *
     * @see BigDecimal#ROUND_HALF_EVEN
     *
     * @param decimal The BigDecimal value to round.
     * @return Rounded BigDecimal value
     * **/
    public static BigDecimal roundHalfEven(BigDecimal decimal) {
        return decimal.setScale(0, BigDecimal.ROUND_HALF_EVEN);
    }

    /** Rounding mode to round towards "nearest neighbor" unless
     * both neighbors are equidistant, in which case round
     * "ceiling".
     * Behaves as HALF_ROUND_DOWN if the decimal is positive and
     * as HALF_ROUND_UP if the decimal is negative.
     * <p>
     *
     * Note that there is no half floor rounding mode
     * supported for BigDecimal values. This method uses a
     * combination of the {@link BigDecimal#ROUND_HALF_UP} and
     * {@link BigDecimal#ROUND_HALF_DOWN} to perform this
     * new rounding mode.
     *
     * @param decimal The BigDecimal value to round.
     * @return Rounded BigDecimal value
     * **/
    public static BigDecimal roundHalfFloor(BigDecimal decimal) {
        if (decimal.signum() == -1) {
            return roundHalfUp(decimal);
        }
        return roundHalfDown(decimal);
    }

    /** Rounding mode to round towards "nearest neighbor" unless
     * both neighbors are equidistant, in which case round up.
     * Behaves as for ROUND_UP if the discarded fraction is &ge; .5;
     * otherwise, behaves as for ROUND_DOWN. Note that this is the
     * rounding mode that most of us were taught in grade school.
     * Rounding mode to round towards zero.
     *
     * @see BigDecimal#ROUND_HALF_UP
     *
     * @param decimal The BigDecimal value to round.
     * @return Rounded BigDecimal value
     * **/
    public static BigDecimal roundHalfUp(BigDecimal decimal) {
        return decimal.setScale(0, BigDecimal.ROUND_HALF_UP);
    }

    /** Rounding mode to round away from zero.
     * Always increments the digit prior to a non-zero discarded fraction.
     * Note that this rounding mode never decreases the magnitude of
     * the calculated value.
     *
     * @see BigDecimal#ROUND_UP
     *
     * @param decimal The BigDecimal value to round.
     * @return Rounded BigDecimal value
     * **/
    public static BigDecimal roundUp(BigDecimal decimal) {
        return decimal.setScale(0, BigDecimal.ROUND_UP);
    }

    /** Return the string representation of this rounding.
     *  @return A String.
     */
    @Override
    public String toString() {
        return _name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    // NOTE: It may seem strange that these inner classes are built this
    // way instead of as anonymous classes...  This code was copied from
    // ptolemy.data.type.BaseType where an explanation that was valid
    // for that usage may be found.

    /** Singleton implementing ceiling rounding strategy. */
    public static final RoundCeiling CEILING = new RoundCeiling();

    /** Singleton implementing floor rounding strategy. */
    public static final RoundFloor FLOOR = new RoundFloor();

    /** Singleton implementing truncate rounding strategy. */
    public static final RoundFloor TRUNCATE = FLOOR;

    /** Singleton implementing down rounding strategy. */
    public static final RoundDown DOWN = new RoundDown();

    /** Singleton implementing up rounding strategy. */
    public static final RoundUp UP = new RoundUp();

    /** Singleton implementing half down rounding strategy. */
    public static final RoundHalfDown HALF_DOWN = new RoundHalfDown();

    /** Singleton implementing half up rounding strategy. */
    public static final RoundHalfUp HALF_UP = new RoundHalfUp();

    /** Singleton implementing half even rounding strategy. */
    public static final RoundHalfEven HALF_EVEN = new RoundHalfEven();

    /** Singleton implementing convergent rounding strategy. */
    public static final RoundHalfEven CONVERGENT = HALF_EVEN;

    /** Singleton implementing half ceiling rounding strategy. */
    public static final RoundHalfCeiling HALF_CEILING = new RoundHalfCeiling();

    /** Singleton implementing nearest rounding strategy. */
    public static final RoundHalfCeiling NEAREST = HALF_CEILING;

    /** Singleton implementing half floor rounding strategy. */
    public static final RoundHalfFloor HALF_FLOOR = new RoundHalfFloor();

    // The following singleton classes are listed below (and out of
    // alphabetic order) because they depend on the construction of
    // other singleton objects before they can be defined.

    /** Singleton implementing general rounding strategy. */
    public static final Rounding GENERAL = UP;

    /** Singleton implementing unknown rounding strategy. */
    public static final Rounding UNKNOWN = UP;

    /** Singleton implementing unnecessary rounding strategy. */
    public static final Rounding UNNECESSARY = HALF_UP;

    ///////////////////////////////////////////////////////////////////
    ////                         public inner classes              ////

    /** Rounding class implementing the round ceiling strategy. */
    public static class RoundCeiling extends Rounding {
        private RoundCeiling() {
            super("ceiling");
        }

        @Override
        public BigInteger round(BigDecimal dec) {
            return roundCeiling(dec).toBigInteger();
        }
    }

    /** Rounding class implementing the round down strategy. */
    public static class RoundDown extends Rounding {
        private RoundDown() {
            super("down");
        }

        @Override
        public BigInteger round(BigDecimal dec) {
            return roundDown(dec).toBigInteger();
        }
    }

    /** Rounding class implementing the round floor strategy. */
    public static class RoundFloor extends Rounding {
        private RoundFloor() {
            super("floor");
            _addRounding(this, "truncate");
        }

        @Override
        public BigInteger round(BigDecimal dec) {
            return roundFloor(dec).toBigInteger();
        }
    }

    /** Rounding class implementing the round half ceiling strategy. */
    public static class RoundHalfCeiling extends Rounding {
        private RoundHalfCeiling() {
            super("half_ceiling");
            _addRounding(this, "nearest");
            _addRounding(this, "round"); // For compatibility
        }

        @Override
        public BigInteger round(BigDecimal dec) {
            return roundHalfCeiling(dec).toBigInteger();
        }
    }

    /** Rounding class implementing the round half down strategy. */
    public static class RoundHalfDown extends Rounding {
        private RoundHalfDown() {
            super("half_down");
        }

        @Override
        public BigInteger round(BigDecimal dec) {
            return roundHalfDown(dec).toBigInteger();
        }
    }

    /** Rounding class implementing the round half even strategy. */
    public static class RoundHalfEven extends Rounding {
        private RoundHalfEven() {
            super("half_even");
            _addRounding(this, "convergent");
        }

        @Override
        public BigInteger round(BigDecimal dec) {
            return roundHalfEven(dec).toBigInteger();
        }
    }

    /** Rounding class implementing the round half floor strategy. */
    public static class RoundHalfFloor extends Rounding {
        private RoundHalfFloor() {
            super("half_floor");
        }

        @Override
        public BigInteger round(BigDecimal dec) {
            return roundHalfFloor(dec).toBigInteger();
        }
    }

    /** Rounding class implementing the round half up strategy. */
    public static class RoundHalfUp extends Rounding {
        private RoundHalfUp() {
            super("half_up");
            _addRounding(this, "unnecessary");
        }

        @Override
        public BigInteger round(BigDecimal dec) {
            return roundHalfUp(dec).toBigInteger();
        }
    }

    /** Rounding class implementing the round up strategy. */
    public static class RoundUp extends Rounding {
        private RoundUp() {
            super("up");
            _addRounding(this, "general");
            _addRounding(this, "unknown");
        }

        @Override
        public BigInteger round(BigDecimal dec) {
            return roundUp(dec).toBigInteger();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected constructor                 ////

    /** Construct a Rounding object with the given String name.
     *  This name is used for finding a Rounding object at a later
     *  time (@see #forName(String)). This constructor
     *  is protected to make a type safe enumeration.
     *
     * @param name The String name to give this Rounding strategy.
     *
     */
    protected Rounding(String name) {
        _name = name;
        _addRounding(this, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

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

    /** The name of the rounding mode. */
    private String _name;

    /** A map from rounding type name to the rounding type for all rounding
     *  types.
     */
    private static Map _nameToRounding;

}
