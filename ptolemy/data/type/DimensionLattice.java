/** A class representing the type of a multi-dimensional array.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data.type;

import ptolemy.graph.*;
import ptolemy.kernel.util.IllegalActionException;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DimensionLattice
/**
A class representing the order of dimension types.

@author Steve Neuendorffer
$Id$

*/

public final class DimensionLattice implements CPO
{
    /**
     *  Override the default public constructor.
     */
    private DimensionLattice() {
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the bottom element of this CPO.
     *  The bottom element is the element in the CPO that is lower than
     *  all the other elements.
     *  @return Dimension.BOTTOM
     */
    public Object bottom() {
	return Dimension.BOTTOM;
    }

    /** Compare two elements in this CPO.
     *  @param e1 an Object representing a CPO element.
     *  @param e2 an Object representing a CPO element.
     *  @return one of <code>CPO.LOWER, CPO.SAME,
     *   CPO.HIGHER, CPO.INCOMPARABLE</code>.
     *  @exception IllegalArgumentException If either of the
     *   specified Objects is not an element of this CPO.
     */
    public int compare(Object e1, Object e2) {
	Dimension t1 = _castType(e1);
	Dimension t2 = _castType(e2);
        
        if(t1.equals(t2)) return CPO.SAME;
        if(t1.equals(Dimension.TOP)) return CPO.HIGHER;
        if(t1.equals(Dimension.BOTTOM)) return CPO.LOWER;
        if(t2.equals(Dimension.TOP)) return CPO.LOWER;
        if(t2.equals(Dimension.BOTTOM)) return CPO.HIGHER;
        return CPO.INCOMPARABLE;
    }

    /** Compute the down-set of an element in this CPO.
     *  The down-set of an element is the subset consisting of
     *  all the elements lower than or the same as the specified element.
     *  @param e an Object representing an element in this CPO.
     *  @return an array of Objects representing the elements in the
     *   down-set of the specified element.
     *  @exception IllegalArgumentException If the specified Object is not
     *   an element in this CPO, or the set is infinite.
     */
    public Object[] downSet(Object e1) {
        Dimension t1 = _castType(e1);

        if(t1.equals(Dimension.TOP)) 
            throw new IllegalArgumentException(
                "The object has an infinite downset.");
        Object[] obj;
        if(t1.equals(Dimension.BOTTOM)) {
            obj = new Object[1];
            obj[0] = Dimension.BOTTOM;
        } else {
            obj = new Object[2];
            obj[0] = Dimension.BOTTOM;
            obj[1] = t1;
        }
        return obj;
    }

    /** 
     * Return the singleton instance of this class;
     */
    public static DimensionLattice getInstance() {
        return _instance;
    }
            
    /** Compute the greatest lower bound (GLB) of two elements.
     *  The GLB of two elements is the greatest element in the CPO
     *  that is lower than or the same as both of the two elements.
     *  @param e1 an Object representing an element in this CPO.
     *  @param e2 an Object representing an element in this CPO.
     *  @return an Object representing the GLB of the two specified
     *   elements, or <code>null</code> if the GLB does not exist.
     *  @exception IllegalArgumentException If at least one of the
     *   specified Objects is not an element of this CPO.
     */
    public Object greatestLowerBound(Object e1, Object e2) {
        Dimension t1 = _castType(e1);
	Dimension t2 = _castType(e2);

	if(t1.equals(Dimension.TOP)) return t2;
	if(t2.equals(Dimension.TOP)) return t1;
	return Dimension.BOTTOM;
    }


    /** Compute the greatest lower bound (GLB) of a subset.
     *  The GLB of a subset is the greatest element in the CPO that
     *  is lower than or the same as all the elements in the
     *  subset.
     *  @param subset an array of Objects representing the subset.
     *  @return an Object representing the GLB of the subset, or
     *   <code>null</code> if the GLB does not exist.
     *  @exception IllegalArgumentException If at least one Object
     *   in the specified array is not an element of this CPO.
     */
    public Object greatestLowerBound(Object[] subset) {
	Dimension glb = Dimension.TOP;
	int i;
	for(i = 0; i < subset.length; i++) {
	    glb = _castType(greatestLowerBound(glb, subset[i]));
	    if(glb.equals(Dimension.BOTTOM))
		return glb;
	}
	return glb;
    }
    

    /** Compute the greatest element of a subset.
     *  The greatest element of a subset is an element in the
     *  subset that is higher than all the other element in the
     *  subset.
     *  @param subset an array of Objects representing the subset.
     *  @return an Object representing the greatest element of the subset,
     *   or <code>null</code> if the greatest element does not exist.
     *  @exception IllegalArgumentException If at least one object in the
     *   specified array is not an element of this CPO.
     */
    public Object greatestElement(Object[] subset) {
        Dimension lub = _castType(leastUpperBound(subset));
        int i;
        for(i = 0; i < subset.length; i++) {
            if(lub.equals(_castType(subset[i]))) return lub;
        }
        return null;
    }

    /** Test if this CPO is a lattice.
     *  A lattice is a CPO where the LUB and GLB of any pair of elements
     *  exist.
     *  @return true.
     */
    public boolean isLattice() {
        return true;
    }

    /** Compute the least element of a subset.
     *  The least element of a subset is an element in the
     *  subset that is lower than all the other element in the
     *  subset.
     *  @param subset an array of Objects representing the subset.
     *  @return an Object representing the least element of the subset,
     *   or <code>null</code> if the least element does not exist.
     *  @exception IllegalArgumentException If at least one object in the
     *   specified array is not an element of this CPO.
     */
    public Object leastElement(Object[] subset) {
        Dimension glb = _castType(greatestLowerBound(subset));
        int i;
        for(i = 0; i < subset.length; i++) {
            if(glb.equals(_castType(subset[i]))) return glb;
        }
        return null;
    }

    /** Compute the least upper bound (LUB) of two elements.
     *  The LUB of two elements is the least element in the CPO
     *  that is greater than or the same as both of the two elements.
     *  @param e1 an Object representing an element in this CPO.
     *  @param e2 an Object representing an element in this CPO.
     *  @return an Object representing the LUB of the two specified
     *   elements, or <code>null</code> if the LUB does not exist.
     *  @exception IllegalArgumentException If at least one of the
     *   specified Objects is not an element of this CPO.
     */
    public Object leastUpperBound(Object e1, Object e2) {
        Dimension t1 = _castType(e1);
	Dimension t2 = _castType(e2);

	if(t1.equals(Dimension.BOTTOM)) return t2;
	if(t2.equals(Dimension.BOTTOM)) return t1;
	return Dimension.TOP;
    }

    /** Compute the least upper bound (LUB) of a subset.
     *  The LUB of a subset is the least element in the CPO that
     *  is greater than or the same as all the elements in the
     *  subset.
     *  @param subset an array of Objects representing the subset.
     *  @return an Object representing the LUB of the subset, or
     *   <code>null</code> if the LUB does not exist.
     *  @exception IllegalArgumentException If at least one object
     *   in the specified array is not an element of this CPO.
     */
    public Object leastUpperBound(Object[] subset) {
	Dimension lub = Dimension.BOTTOM;
	int i;
	for(i = 0; i < subset.length; i++) {
	    lub = _castType(leastUpperBound(lub, subset[i]));
	    if(lub.equals(Dimension.TOP))
		return lub;
	}
	return lub;
    }

    /** Return the top element of this CPO.
     *  The top element is the element in the CPO that is higher than
     *  all the other elements.
     *  @return Dimension.TOP
     */
    public Object top() {
        return Dimension.TOP;
    }

    /** Compute the up-set of an element in this CPO.
     *  The up-set of an element is the subset consisting of
     *  all the elements higher than or the same as the specified element.
     *  @param e an Object representing an element in this CPO.
     *  @return an array of Objects representing the elements in the
     *   up-set of the specified element.
     *  @exception IllegalArgumentException If the specified Object is not
     *   an element of this CPO, or the set is infinite.
     */
    public Object[] upSet(Object e1) {
        Dimension t1 = _castType(e1);

        if(t1.equals(Dimension.BOTTOM)) 
            throw new IllegalArgumentException(
                "The object has an infinite upset.");
        Object[] obj;
        if(t1.equals(Dimension.TOP)) {
            obj = new Object[1];
            obj[0] = Dimension.TOP;
        } else {
            obj = new Object[2];
            obj[0] = Dimension.TOP;
            obj[1] = t1;
        }
        return obj;
    }

    /** 
     *  Cast the given object to a Dimension.
     *  Throw an exception appropriate when this lattice is given an object
     *  that is not a Dimension.
     */
    private Dimension _castType(Object o) {
        if(o instanceof Dimension) 
            return (Dimension) o;
        else
            throw new IllegalArgumentException("Object must be an " +
                    "instance of Dimension");
    }

    private static final DimensionLattice _instance = 
    new DimensionLattice();
}

