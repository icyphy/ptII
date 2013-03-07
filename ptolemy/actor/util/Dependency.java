/* Interface representing a dependency between ports.

 Copyright (c) 2003-2013 The Regents of the University of California.
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
//// Dependency

/**
 This interface provides a dependency for causality interfaces as described
 in the paper "Causality Interfaces for Actor Networks" by Ye Zhou and
 Edward A. Lee, ACM Transactions on Embedded Computing Systems (TECS),
 April 2008, as available as <a href="http://www.eecs.berkeley.edu/Pubs/TechRpts/2006/EECS-2006-148.pdf">
 Technical Report No. UCB/EECS-2006-148</a>,
 November 16, 2006.
 <p>
 A dependency represents a causality relationship between two ports.
 Implementations of this interface are required to satisfy certain
 algebraic properties, but as long as these are satisfied, there is
 considerable freedom.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public interface Dependency extends Comparable<Dependency> {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a dependency that results from parallel composition of
     *  this one and the specified one. This is required to be
     *  associative and commutative. That is, d1.oPlus(d2).oPlus(d3)
     *  should be equal to d1.oPlus(d2.oPlus(d3)), and
     *  d1.oPlus(d2) should be equal to d2.oPlus(d1).
     *  In addition, any implementation should be idempotent,
     *  meaning that d.oPlus(d) is equal to d.
     *  @param d The dependency to add.
     *  @return A dependency whose value is the logical OR of the two dependency
     *   values.
     *  @exception ClassCastException if d is not a BooleanDependency.
     */
    public Dependency oPlus(Dependency d);

    /** Return the dependency that when added to any other
     *  dependency using oPlus() yields the other dependency.
     *  @return The additive identity.
     */
    public Dependency oPlusIdentity();

    /** Return a dependency that results from serial composition of
     *  this one and the specified one. This is required to be
     *  associative, but not necessarily commutative.
     *  That is, d1.oTimes(d2).oTimes(d3)
     *  should be equal to d1.oTimes(d2.oTimes(d3)).
     *  Moreover, it should be distributive over oPlus.
     *  That is, d1.oTimes(d2.oPlus(d3)) should be equal to
     *  (d1.oTimes(d2)).oPlus(d1.oTimes(d3)).
     *  @param d The dependency to multiply.
     *  @return A dependency whose value is the logical AND of the value of
     *   this one and the specified one.
     *  @exception ClassCastException if d is not a BooleanDependency.
     */
    public Dependency oTimes(Dependency d);

    /** Return the dependency that when multiplied by any other
     *  dependency using oTimes() yields the other dependency.
     *  @return The multiplicative identity.
     */
    public Dependency oTimesIdentity();

    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////

    /** Return value of compareTo() if this is equal to the argument. */
    public static int EQUALS = 0;

    /** Return value of compareTo() if this is greater than the argument. */
    public static int GREATER_THAN = 1;

    /** Return value of compareTo() if this is incomparable to the argument. */
    public static int INCOMPARABLE = 2;

    /** Return value of compareTo() if this is less than the argument. */
    public static int LESS_THAN = -1;
}
