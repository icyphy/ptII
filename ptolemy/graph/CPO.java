/** An interface for Complete partial order (CPO).

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

*/

package ptolemy.graph;
import java.util.*;


//////////////////////////////////////////////////////////////////////////
//// CPO
/**
An interface for Complete Partial Order(CPO). 
Please see "Introduction to Lattices and Order" by Davey and Priestley
for definition of operations.

@author Yuhong Xiong
@version $Id$
*/

public interface CPO
{
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Returns the bottom element of this CPO.
     *  @return an object representing the bottom element.
     */
    public Object bottom();

    /** Compares two elements in this CPO.
     *  @param e1 an object representing a CPO element.
     *  @param e2 another object representing a CPO element.
     *  @return one of <code>CPO.STRICT_LESS, CPO.EQUAL,
     *   CPO.STRICT_GREATER, CPO.INCOMPARABLE</code>.
     *  @exception IllegalArgumentException at least one element
     *   doesn't belong to this CPO.
     */
    public int compare(Object e1, Object e2);

    /** Computes the down-set of an element in this CPO.
     *  @param e an Object representing an element in this CPO.
     *  @return an array of of objects representing the elements in the
     *   down-set of the input.
     *  @exception IllegalArgumentException the specified object doesn't
     *   belong to this CPO.
     */
     // FIXME: down-set may be infinite for infinite CPOs.  move this to
     // FiniteCPO class?
    public Object[] downSet(Object e);    

    /** Computes the greatest lower bound (GLB) of two elements.
     *  @param e1 an object representing an element in this CPO.
     *  @param e2 another object representing an element in this CPO.
     *  @return An object representing the GLB of the two specified
     *   elements.  <code>null</code> if the GLB doesn't exist.
     *  @exception IllegalArgumentException at least one element
     *   doesn't belong to this CPO.
     */
    public Object glb(Object e1, Object e2);
    
    /** Computes the greatest lower bound (GLB) of a subset.
     *  @param subset an array of objects representing the subset.
     *  @return An object representing the GLB of the subset.
     *  <code>null</code> if the GLB doesn't exist.
     *  @exception IllegalArgumentException at least one element
     *  in the subset doesn't belong to this CPO.
     */
    public Object glb(Object[] subset);

    /** Computes the greatest element of a subset.
     *  @param subset an array of objects representing the subset.
     *  @return an object representing the greatest element of the subset.
     *   <code>null</code> if the greatest element doesn't exist.
     *  @exception IllegalArgumentException at least one element in the
     *   subset doesn't belong to this CPO.
     */
    public Object greatestElement(Object[] subset);

    /** Computes the least element of a subset.
     *  @param subset an array of objects representing the subset.
     *  @return an object representing the least element of the subset.
     *   <code>null</code> if the least element doesn't exist.
     *  @exception IllegalArgumentException at least one element in the
     *   subset doesn't belong to this CPO.
     */
    public Object leastElement(Object[] subset);
    
    /** Computes the least upper bound (LUB) of two elements.
     *  @param e1 an object representing an element in this CPO.
     *  @param e2 another object representing element in this CPO.
     *  @return an object representing the LUB of the two specified
     *   elements.  <code>null</code> if the LUB doesn't exist.
     *  @exception IllegalArgumentException at least one element
     *   doesn't belong to this CPO.
     */
    public Object lub(Object e1, Object e2);
    
    /** Computes the least upper bound (LUB) of a subset.
     *  @param subset an array of objects representing the subset.
     *  @return An object representing the LUB of the subset.
     *  <code>null</code> if the LUB doesn't exist.
     *  @exception IllegalArgumentException at least one element
     *   in the subset doesn't belong to this CPO.
     */
    public Object lub(Object[] subset);
 
    /** Returns the top element of this CPO.
     *  @return an object representing the top element.
     */
    public Object top();

    /** Computes the up-set of an element in this CPO.
     *  @param e an Object representing an element in this CPO.
     *  @return an array of of objects representing the elements in the
     *   up-set of the input.
     *  @exception IllegalArgumentException the specified object doesn't
     *   belong to this CPO.
     */
     // FIXME: up-set may be infinite for infinite CPOs.  move this to
     // FiniteCPO class?
    public Object[] upSet(Object e);
 
    ////////////////////////////////////////////////////////////////////////
    ////                        public variables                        ////

    /** Comparison result: the first element is strictly less than
     *  the second.
     *  @see #compare
     */
    public static final int STRICT_LESS = -1;
    
    /** Comparison result: the two element are equal.
     *  @see #compare
     */
    public static final int EQUAL = 0;
    
    /** Comparison result: the first element is strictly greater than
     *  the second.
     *  @see #compare
     */
    public static final int STRICT_GREATER = 1;
    
    /** Comparison result: the two elements are incomparable.
     *  @see #compare
     */
    public static final int INCOMPARABLE = 2;
}

