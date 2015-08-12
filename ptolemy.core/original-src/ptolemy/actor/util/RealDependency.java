/* Interface representing a dependency between ports.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.actor.util;

///////////////////////////////////////////////////////////////////
//// RealDependency

/**
 This dependency represents causal relationships that have a real value
 or are infinite. Infinity represents the lack of a causal relationship.
 A finite positive real value represents a causal dependency with (typically)
 a time delay. A zero value represents an immediate causal relationship.
 See the paper "Causality Interfaces for Actor Networks" by Ye Zhou and
 Edward A. Lee, ACM Transactions on Embedded Computing Systems (TECS),
 April 2008, as available as <a href="http://www.eecs.berkeley.edu/Pubs/TechRpts/2006/EECS-2006-148.pdf">
 Technical Report No. UCB/EECS-2006-148</a>,
 November 16, 2006.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class RealDependency implements Dependency {

    /** Construct a dependency with the specified value.
     *  The constructor is private. Use valueOf() to construct
     *  an instance.
     *  @param value The value.
     */
    private RealDependency(double value) {
        _value = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return Dependency.LESS_THAN, EQUALS, or GREATER_THAN depending
     *  on whether the argument is less than, equal to, or greater than
     *  this dependency. Real dependencies are totally ordered, so
     *  this never returns Dependency.INCOMPARABLE. The order is the
     *  usual numerical ordering of doubles, with Double.POSITIVE_INFINITY
     *  on top.
     *  @param dependency The dependency to compare against.
     *  @return The result of comparison.
     *  @exception ClassCastException If the argument is not an instance
     *   of RealDependency.
     */
    @Override
    public int compareTo(Dependency dependency) {
        if (equals(dependency)) {
            return Dependency.EQUALS;
        }
        if (_value < ((RealDependency) dependency)._value) {
            return Dependency.LESS_THAN;
        }
        return Dependency.GREATER_THAN;
    }

    /** Return true if the value of this dependency equals that
     *  of the specified one, and the specified one is an instance
     *  of RealDependency.
     *  @param object The object to compare against.
     *  @return true if this object is the same as the object argument.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof RealDependency) {
            return _value == ((RealDependency) object)._value;
        }
        return false;
    }

    /** Return the same hashCode that that Java Double object would
     *  return had it the same value.
     */
    @Override
    public int hashCode() {
        long v = Double.doubleToLongBits(_value);
        return (int) (v ^ v >>> 32);
    }

    /** Return a dependency that results from parallel composition of
     *  this one and the specified one.
     *  @param d The dependency to add.
     *  @return A dependency whose value is the minimum of the two dependency
     *   values.
     *  @exception ClassCastException if d is not a RealDependency.
     */
    @Override
    public Dependency oPlus(Dependency d) {
        // FIXME: Findbugs reports this as an Unchecked/unconfirmed cast
        if (((RealDependency) d)._value < _value) {
            return d;
        }
        return this;
    }

    /** Return the dependency that when added to any other
     *  dependency using oPlus() yields the other dependency.
     *  @return The additive identity.
     */
    @Override
    public Dependency oPlusIdentity() {
        return OPLUS_IDENTITY;
    }

    /** Return a dependency that results from serial composition of
     *  this one and the specified one.
     *  @param d The dependency to multiply.
     *  @return A dependency whose value is the sum of the value of
     *   this one and the specified one.
     *  @exception ClassCastException if d is not a RealDependency.
     */
    @Override
    public Dependency oTimes(Dependency d) {
        // FIXME: Findbugs reports this as an Unchecked/unconfirmed cast
        return new RealDependency(_value + ((RealDependency) d)._value);
    }

    /** Return the dependency that when multiplied by any other
     *  dependency using oTimes() yields the other dependency.
     *  @return The multiplicative identity.
     */
    @Override
    public Dependency oTimesIdentity() {
        return OTIMES_IDENTITY;
    }

    /** Return a string representation in the form
     *  "RealDependency(value)".
     *  @return A string representation.
     */
    @Override
    public String toString() {
        return "RealDependency(_value)";
    }

    /** Return the double value of the dependency.
     * @return The value of the dependency.
     */
    public double value() {
        return _value;
    }

    /** Return an instance of RealDependency with the specified
     *  value. This is preferable to use over the constructor
     *  because it uses the same instances for the most common
     *  values.
     *  @param value The value used to determine the RealDependency
     *  to be returned.
     *  @return an instance of RealDependency, if value
     *  is 0.0, then {@link #OTIMES_IDENTITY} is returned, if
     *  value is Double.POSITIVE_INFINITY, then {@link #OPLUS_IDENTITY}
     *  is returned.  Otherwise the RealDependency constructor
     *  is called.
     */
    public static RealDependency valueOf(double value) {
        if (value == 0.0) {
            return OTIMES_IDENTITY;
        } else if (value == Double.POSITIVE_INFINITY) {
            return OPLUS_IDENTITY;
        }
        return new RealDependency(value);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // FIXME: FindBugs suggests that both these fields be final
    // "MS: Field isn't final but should be (MS_SHOULD_BE_FINAL)
    // A mutable static field could be changed by malicious code or by
    // accident from another package. The field could be made final to avoid
    // this vulnerability."

    /** The additive identity. */
    public static final RealDependency OPLUS_IDENTITY = new RealDependency(
            Double.POSITIVE_INFINITY);

    /** The multiplicative identity. */
    public static final RealDependency OTIMES_IDENTITY = new RealDependency(0.0);

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The value. */
    private double _value;

}
