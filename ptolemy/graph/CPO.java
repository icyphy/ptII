/** An interface defining the operations on complete partial order (CPO).

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
An interface defining the operations on complete partial order (CPO).
The definitions of these operations can be found in "Introduction to
Lattices and Order",  Cambridge University Press, 1990, by B.A. Davey
and H.A. Priestley.

Each element in the CPO is represented by an Object.
For infinite CPOs, the result of some of the operations may be an
infinite set. In this case, the class implementing this interface
can throw an Exception.

@author Yuhong Xiong
$Id$
*/

public interface CPO
{
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the bottom element of this CPO.
     *  @return an Object representing the bottom element, or
     *   <code>null</code> if the bottom does not exist.
     */
    public Object bottom();

    /** Compare two elements in this CPO.
     *  @param e1 an Object representing a CPO element.
     *  @param e2 an Object representing a CPO element.
     *  @return one of <code>CPO.STRICT_LESS, CPO.EQUAL,
     *   CPO.STRICT_GREATER, CPO.INCOMPARABLE</code>.
     *  @exception IllegalArgumentException at least one of the 
     *   specified Objects is not an element of this CPO.
     */
    public int compare(Object e1, Object e2);

    /** Compute the down-set of an element in this CPO.
     *  @param e an Object representing an element in this CPO.
     *  @return an array of of Objects representing the elements in the
     *   down-set of the specified element.
     *  @exception IllegalArgumentException the specified Object is not
     *   an element in this CPO.
     */
    public Object[] downSet(Object e);    

    /** Compute the greatest lower bound (GLB) of two elements.
     *  @param e1 an Object representing an element in this CPO.
     *  @param e2 an Object representing an element in this CPO.
     *  @return an Object representing the GLB of the two specified
     *   elements, or <code>null</code> if the GLB does not exist.
     *  @exception IllegalArgumentException at least one of the 
     *   specified Objects is not an element of this CPO.
     */
    public Object greatestLowerBound(Object e1, Object e2);
    
    /** Compute the greatest lower bound (GLB) of a subset.
     *  @param subset an array of Objects representing the subset.
     *  @return an Object representing the GLB of the subset, or
     *   <code>null</code> if the GLB does not exist.
     *  @exception IllegalArgumentException at least one Object
     *   in the specified array is not an element of this CPO.
     */
    public Object greatestLowerBound(Object[] subset);

    /** Compute the greatest element of a subset.
     *  @param subset an array of Objects representing the subset.
     *  @return an Object representing the greatest element of the subset,
     *   or <code>null</code> if the greatest element does not exist.
     *  @exception IllegalArgumentException at least one Object in the
     *   specified array is not an element of this CPO.
     */
    public Object greatestElement(Object[] subset);

    /** Compute the least element of a subset.
     *  @param subset an array of Objects representing the subset.
     *  @return an Object representing the least element of the subset,
     *   or <code>null</code> if the least element does not exist.
     *  @exception IllegalArgumentException at least one Object in the
     *   specified array is not an element of this CPO.
     */
    public Object leastElement(Object[] subset);
    
    /** Compute the least upper bound (LUB) of two elements.
     *  @param e1 an Object representing an element in this CPO.
     *  @param e2 an Object representing element in this CPO.
     *  @return an Object representing the LUB of the two specified
     *   elements, or <code>null</code> if the LUB does not exist.
     *  @exception IllegalArgumentException at least one of the
     *   specified Objects is not an element of this CPO.
     */
    public Object leastUpperBound(Object e1, Object e2);
    
    /** Compute the least upper bound (LUB) of a subset.
     *  @param subset an array of Objects representing the subset.
     *  @return an Object representing the LUB of the subset, or
     *   <code>null</code> if the LUB does not exist.
     *  @exception IllegalArgumentException at least one Object
     *   in the specified array is not an element of this CPO.
     */
    public Object leastUpperBound(Object[] subset);
 
    /** Return the top element of this CPO.
     *  @return an Object representing the top element, or null if
     *   the top does not exist.
     */
    public Object top();

    /** Compute the up-set of an element in this CPO.
     *  @param e an Object representing an element in this CPO.
     *  @return an array of Objects representing the elements in the
     *   up-set of the specified element.
     *  @exception IllegalArgumentException the specified Object is not
     *   an element of this CPO.
     */
    public Object[] upSet(Object e);
 
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** One of the return values of <code>compare</code>, indicating
     *  that the first element is strictly less than the second.
     *  @see #compare
     */
    public static final int STRICT_LESS = -1;
    
    /** One of the return values of <code>compare</code>, indicating
     *  that the two elements are equal.
     *  @see #compare
     */
    public static final int EQUAL = 0;
 
    /** One of the return values of <code>compare</code>, indicating
     *  that the first element is strictly greater than the second.
     *  @see #compare
     */
    public static final int STRICT_GREATER = 1;
    
    /** One of the return values of <code>compare</code>, indicating
     *  that the two elements are incomparable.
     *  @see #compare
     */
    public static final int INCOMPARABLE = 2;
}

