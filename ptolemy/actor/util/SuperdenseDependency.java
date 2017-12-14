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
//// SuperdenseDependency

/**
 This dependency represents causal relationships that have a real value
 and a superdense time index. If the real value is infinite, this
 represents the lack of a causal relationship.
 A finite positive real value represents a causal dependency with (typically)
 a time delay. A zero value represents no time delay, but whether there is
 an immediate causal relationship depends on the index. If the index is 0,
 then there is an immediate causal relationship. Otherwise, there is not.
 See the paper "Causality Interfaces for Actor Networks" by Ye Zhou and
 Edward A. Lee, ACM Transactions on Embedded Computing Systems (TECS),
 April 2008, as available as <a href="http://www.eecs.berkeley.edu/Pubs/TechRpts/2006/EECS-2006-148.pdf">
 Technical Report No. UCB/EECS-2006-148</a>,
 November 16, 2006.

 @author Edward A. Lee, Slobodan Matic, Jia Zou
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class SuperdenseDependency extends BooleanDependency {

    /** Construct a dependency with the specified value.
     *  The constructor is private. Use valueOf() to construct
     *  an instance.
     *  @param time The real part of the dependency.
     *  @param index The supersense index part of the dependency.
     */
    private SuperdenseDependency(double time, int index) {
        super(time != Double.POSITIVE_INFINITY);
        _time = time;
        _index = index;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return Dependency.LESS_THAN, EQUALS, or GREATER_THAN depending
     *  on whether the argument is less than, equal to, or greater than
     *  this dependency. Real dependencies are totally ordered, so
     *  this never returns Dependency.INCOMPARABLE. The order is the
     *  usual numerical ordering of doubles, with Double.POSITIVE_INFINITY
     *  on top.
     *  <p>
     *  In the case where both dependencies have _time value equal to
     *  Double.POSITIVE_INFINITY, these two dependencies are equal, even
     *  though its indices may differ. This conforms with valueOf() method.
     *  @see #valueOf(double, int)
     *  @param dependency The dependency to compare against.
     *  @return The result of comparison.
     *  @exception ClassCastException If the argument is not an instance
     *   of SuperdenseDependency.
     */
    @Override
    public int compareTo(Dependency dependency) {
        if (equals(dependency)) {
            return Dependency.EQUALS;
        }
        if (((SuperdenseDependency) dependency)._time == Double.POSITIVE_INFINITY
                && _time == Double.POSITIVE_INFINITY) {
            return Dependency.EQUALS;
        }
        if (_time < ((SuperdenseDependency) dependency)._time) {
            return Dependency.LESS_THAN;
        }
        if (_time == ((SuperdenseDependency) dependency)._time
                && _index < ((SuperdenseDependency) dependency)._index) {
            return Dependency.LESS_THAN;
        }
        return Dependency.GREATER_THAN;
    }

    /** Return true if the value of this dependency equals that
     *  of the specified one, and the specified one is an instance
     *  of RealDependency.
     *  <p>
     *  In the case where both dependencies have _time value equal to
     *  Double.POSITIVE_INFINITY, these two dependencies are equal, even
     *  though its indices may differ. This conforms with valueOf() method.
     *  @see #valueOf(double, int)
     *  @param object The object to compare against.
     *  @return true if this object is the same as the object argument.
     */
    @Override
    public boolean equals(Object object) {
        // See http://www.technofundo.com/tech/java/equalhash.html
        if (object == this) {
            return true;
        }
        if (object == null || object.getClass() != getClass()) {
            return false;
        } else {
            if (((SuperdenseDependency) object)._time == Double.POSITIVE_INFINITY
                    && _time == Double.POSITIVE_INFINITY) {
                return true;
            }
            return _time == ((SuperdenseDependency) object)._time
                    && _index == ((SuperdenseDependency) object)._index;
        }
    }

    /** Return the same hashCode that that Java Double object would
     *  return had it the same value as the real part of the value
     *  of this dependency.
     */
    @Override
    public int hashCode() {
        long v = Double.doubleToLongBits(_time);
        return (int) (v ^ v >>> 32);
    }

    /** Return the index value of this dependency.
     *  @return The index part of the dependency.
     */
    public int indexValue() {
        return _index;
    }

    /** Return a dependency that results from parallel composition of
     *  this one and the specified one.
     *  @param dependency The dependency to add.
     *  @return A dependency whose value is the minimum of the two dependency
     *   values.
     *  @exception ClassCastException if dependency is not a SuperdenseDependency.
     */
    @Override
    public Dependency oPlus(Dependency dependency) {
        // NOTE: Findbugs reports this as an Unchecked/unconfirmed cast
        if (((SuperdenseDependency) dependency)._time < _time
                || ((SuperdenseDependency) dependency)._time == _time
                        && ((SuperdenseDependency) dependency)._index < _index) {
            return dependency;
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
     *  this one and the specified one. The real part of the result
     *  is the sum of the real parts of this and the specified dependency.
     *  The index, however, will be nonzero only if the specified
     *  dependency has a real part equal to 0.0. In that case,
     *  the index of the result will be the sum of indices of
     *  this and the specified dependency.
     *  NOTE: This implementation of oTimes is not commutative.
     *  Fortunately, the theory does not require it to be.
     *  @param dependency The dependency to multiply.
     *  @return A dependency whose value is the sum of the value of
     *   this one and the specified one.
     *  @exception ClassCastException if dependency is not a RealDependency.
     */
    @Override
    public Dependency oTimes(Dependency dependency) {
        int index = 0;
        // NOTE: Findbugs reports this as an Unchecked/unconfirmed cast
        if (((SuperdenseDependency) dependency)._time == 0.0) {
            index = _index + ((SuperdenseDependency) dependency)._index;
        }
        return new SuperdenseDependency(
                _time + ((SuperdenseDependency) dependency)._time, index);
    }

    /** Return the dependency that when multiplied by any other
     *  dependency using oTimes() yields the other dependency.
     *  @return The multiplicative identity.
     */
    @Override
    public Dependency oTimesIdentity() {
        return OTIMES_IDENTITY;
    }

    /** Return the time value of this dependency.
     *  @return The real part of the dependency.
     */
    public double timeValue() {
        return _time;
    }

    /** Return a string representation in the form
     *  "SuperdenseDependency(_time, _index)".
     *  @return A string representation.
     */
    @Override
    public String toString() {
        return "SuperdenseDependency(" + _time + ", " + _index + ")";
    }

    /** Return an instance of SuperdenseDependency with the specified
     *  time and index value. This is preferable to use over the constructor
     *  because it uses the same instances for the most common
     *  values.
     *  @param time The time value.
     *  @param index The index value.
     *  @return an instance of RealDependency, if value
     *  is (0.0, 0) then {@link #OTIMES_IDENTITY} is returned, if
     *  value is (Double.POSITIVE_INFINITY, n) for any n,
     *  then {@link #OPLUS_IDENTITY}
     *  is returned.  Otherwise the SuperdenseDependency constructor
     *  is called.
     */
    public static SuperdenseDependency valueOf(double time, int index) {
        if (time == 0.0 && index == 0) {
            return OTIMES_IDENTITY;
        } else if (time == Double.POSITIVE_INFINITY) {
            return OPLUS_IDENTITY;
        }
        return new SuperdenseDependency(time, index);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // NOTE: FindBugs suggests that both these fields be final
    // "MS: Field isn't final but should be (MS_SHOULD_BE_FINAL)
    // A mutable static field could be changed by malicious code or by
    // accident from another package. The field could be made final to avoid
    // this vulnerability."

    /** The additive identity. */
    public static final SuperdenseDependency OPLUS_IDENTITY = new SuperdenseDependency(
            Double.POSITIVE_INFINITY, 0);

    /** The multiplicative identity. */
    public static final SuperdenseDependency OTIMES_IDENTITY = new SuperdenseDependency(
            0.0, 0);

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The superdense time index. */
    private int _index;

    /** The real (time) value. */
    private double _time;

}
