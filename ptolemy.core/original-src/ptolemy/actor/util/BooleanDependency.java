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
//// BooleanDependency

/**
 This dependency represents causal relationships that are either
 present or not. That is, given any two ports, either one depends
 causally on the other or not. See the paper "Causality Interfaces for
 Actor Networks" by Ye Zhou and Edward A. Lee, ACM Transactions on
 Embedded Computing Systems (TECS), April 2008, as available as <a
 href="http://www.eecs.berkeley.edu/Pubs/TechRpts/2006/EECS-2006-148.pdf">
 Technical Report No. UCB/EECS-2006-148</a>, November 16, 2006.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class BooleanDependency implements Dependency {

    /** Construct a dependency with the specified value.
     *  Note that the constructor is private. Use valueOf()
     *  to create instances.
     *  @param value The value.
     */
    protected BooleanDependency(boolean value) {
        _value = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return Dependency.LESS_THAN, EQUALS, or GREATER_THAN depending
     *  on whether the argument is less than, equal to, or greater than
     *  this dependency. Boolean dependencies are totally ordered, so
     *  this never returns Dependency.INCOMPARABLE. The order is that
     *  the OPLUS_IDENTITY (false) is less than OTIMES_IDENTITY (true).
     *  @param dependency The dependency to compare against.
     *  @return The result of comparison.
     */
    @Override
    public int compareTo(Dependency dependency) {
        if (equals(dependency)) {
            return Dependency.EQUALS;
        }
        if (_value) {
            return Dependency.LESS_THAN;
        }
        return Dependency.GREATER_THAN;
    }

    /** Return true if the value of this dependency equals that
     *  of the specified one, and the specified one is an instance
     *  of RealDependency.
     *  @param object The object to compare against.
     *  @return true if the values are equal
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
            return _value == ((BooleanDependency) object)._value;
        }
    }

    /** Return the same hashCode that that Java Boolean object would
     *  return had it the same value, which is
     *  the integer 1231 if this object represents true,
     *  and the integer 1237 if this object represents false.
     *  @return A hash code for this object.
     */
    @Override
    public int hashCode() {
        if (_value) {
            return 1231;
        }
        return 1237;
    }

    /** Return a dependency that results from parallel composition of
     *  this one and the specified one. This is a dependency whose
     *  value is the logical OR of the value of this dependency and
     *  specified one.
     *  @param d The dependency to add.
     *  @return A dependency whose value is the logical OR of the two dependency
     *   values.
     *  @exception ClassCastException if d is not a BooleanDependency.
     */
    @Override
    public Dependency oPlus(Dependency d) {
        // FIXME: Findbugs reports this as an Unchecked/unconfirmed cast
        if (((BooleanDependency) d)._value || _value) {
            return OTIMES_IDENTITY;
        }
        return OPLUS_IDENTITY;
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
     *  this one and the specified one. This is a dependency whose
     *  value is the logical AND of the value of this dependency and
     *  specified one.
     *  @param d The dependency to multiply.
     *  @return A dependency whose value is the logical AND of the value of
     *   this one and the specified one.
     *  @exception ClassCastException if d is not a BooleanDependency.
     */
    @Override
    public Dependency oTimes(Dependency d) {
        // FIXME: Findbugs reports this as an Unchecked/unconfirmed cast
        if (_value && ((BooleanDependency) d)._value) {
            return OTIMES_IDENTITY;
        }
        return OPLUS_IDENTITY;
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
     *  "BooleanDependency(value)", where value is "true"
     *  or "false".
     *  @return A string representation.
     */
    @Override
    public String toString() {
        if (_value) {
            return "BooleanDependency(true)";
        }
        return "BooleanDependency(false)";
    }

    /** Return an instance of BooleanDependency with the specified
     *  value. This is preferable to use over the constructor
     *  because there are only ever two possible values.
     *  @param value  The specified value.
     *  @return an instance of BooleanDependency, if value
     *  is true, then {@link #OTIMES_IDENTITY} is returned, if
     *  value is false, then {@link #OPLUS_IDENTITY}.
     */
    public static BooleanDependency valueOf(boolean value) {
        if (value) {
            return OTIMES_IDENTITY;
        }
        return OPLUS_IDENTITY;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // FIXME: FindBugs suggests that both these fields be final
    // "MS: Field isn't final but should be (MS_SHOULD_BE_FINAL)
    // A mutable static field could be changed by malicious code or by
    // accident from another package. The field could be made final to avoid
    // this vulnerability."

    /** The additive identity, which has value false and indicates
     *  that there is no dependency.
     */
    public static final BooleanDependency OPLUS_IDENTITY = new BooleanDependency(
            false);

    /** The multiplicative identity, which has value true and
     *  indicates that there is a dependency.
     */
    public static final BooleanDependency OTIMES_IDENTITY = new BooleanDependency(
            true);

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The value. */
    protected boolean _value;
}
