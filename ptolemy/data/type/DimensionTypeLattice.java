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

import ptolemy.graph.InequalityTerm;
import ptolemy.graph.Inequality;	/* Needed for javadoc */ 
import ptolemy.kernel.util.IllegalActionException;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DimentionTypeLattice
/**
A class representing the order of dimension types.

@author Steve Neuendorffer
$Id$

*/

public class DimensionTypeLattice implements CPO
{
     ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the bottom element of this CPO.
     *  The bottom element is the element in the CPO that is lower than
     *  all the other elements.
     *  @return an Object representing the bottom element, or
     *   <code>null</code> if the bottom does not exist.
     */
    public Object bottom() {
	return DimensionType.BOTTOM;
    }

    /** Compare two elements in this CPO.
     *  @param e1 an Object representing a CPO element.
     *  @param e2 an Object representing a CPO element.
     *  @return one of <code>CPO.LOWER, CPO.SAME,
     *   CPO.HIGHER, CPO.INCOMPARABLE</code>.
     *  @exception IllegalArgumentException If at least one of the
     *   specified Objects is not an element of this CPO.
     */
    public int compare(Object e1, Object e2);

    /** Compute the down-set of an element in this CPO.
     *  The down-set of an element is the subset consisting of
     *  all the elements lower than or the same as the specified element.
     *  @param e an Object representing an element in this CPO.
     *  @return an array of Objects representing the elements in the
     *   down-set of the specified element.
     *  @exception IllegalArgumentException If the specified Object is not
     *   an element in this CPO.
     */
    public Object[] downSet(Object e);

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
	if(!(e1 instanceof DimensionType))
	    throw new IllegalActionException(e1, "Object must be an " +
					     "instance of DimensionType");	       DimensionType t1 = (DimensionType) e1;

	if(!(e2 instanceof DimensionType))
	    throw new IllegalActionException(e2, "Object must be an " +
					     "instance of DimensionType");
	DimensionType t2 = (DimensionType) e2;

	if(t1 isEqualTo top()) return t2;
	if(t2 isEqualTo top()) return t1;
	return bottom();
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
	DimensionType glb = top();
	int i;
	for(i = 0; i < subset.length; i++) {
	    glb = greatestLowerBound(glb, subset[i]);
	    if(glb.isEqualTo(bottom()))
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
     *  @exception IllegalArgumentException If at least one Object in the
     *   specified array is not an element of this CPO.
     */
    public Object greatestElement(Object[] subset);

    /** Test if this CPO is a lattice.
     *  A lattice is a CPO where the LUB and GLB of any pair of elements
     *  exist.
     *  @return <code>true</code> if this CPO is a lattice;
     *   <code>false</code> otherwise.
     */
    public boolean isLattice();

    /** Compute the least element of a subset.
     *  The least element of a subset is an element in the
     *  subset that is lower than all the other element in the
     *  subset.
     *  @param subset an array of Objects representing the subset.
     *  @return an Object representing the least element of the subset,
     *   or <code>null</code> if the least element does not exist.
     *  @exception IllegalArgumentException If at least one Object in the
     *   specified array is not an element of this CPO.
     */
    public Object leastElement(Object[] subset);

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
    public Object leastUpperBound(Object e1, Object e2);

    /** Compute the least upper bound (LUB) of a subset.
     *  The LUB of a subset is the least element in the CPO that
     *  is greater than or the same as all the elements in the
     *  subset.
     *  @param subset an array of Objects representing the subset.
     *  @return an Object representing the LUB of the subset, or
     *   <code>null</code> if the LUB does not exist.
     *  @exception IllegalArgumentException If at least one Object
     *   in the specified array is not an element of this CPO.
     */
    public Object leastUpperBound(Object[] subset);

    /** Return the top element of this CPO.
     *  The top element is the element in the CPO that is higher than
     *  all the other elements.
     *  @return an Object representing the top element, or
     *   <code>null</code> if the top does not exist.
     */
    public Object top();

    /** Compute the up-set of an element in this CPO.
     *  The up-set of an element is the subset consisting of
     *  all the elements higher than or the same as the specified element.
     *  @param e an Object representing an element in this CPO.
     *  @return an array of Objects representing the elements in the
     *   up-set of the specified element.
     *  @exception IllegalArgumentException If the specified Object is not
     *   an element of this CPO.
     */
    public Object[] upSet(Object e);

   /** Return true if the given type is equal to this type.   In other words, 
     *  an object with this type can be expressed as an object of Type t with 
     *  no conversion.
     */
    public boolean isEqualTo(Type t) {
        if(_numDimensions != t._numDimensions) return false;
        int i;
        for(i = 0; i < _numDimensions; i++) {
            if(_dimensions[i] != t._dimensions[i]) return false;
        }
        return true;
    }


    /** Return true if this type can be converted into an object of the given
     *  type, with some possible conversion.   If the two types are equal,
     *  then this method will return true.
     */
    public boolean isConvertibleTo(Type t) {
        return isEqualTo(t);
    }

    /** Return true if the given type can be converted into an object of this
     *  type, with some possible conversion.   If the two types are equal, 
     *  then this method will return true.
     */
    public boolean isConvertibleFrom(Type t) {
        return isEqualTo(t);
    }

    /** Return true if the given type can be instantiated as a token.
     */
    public boolean isInstantiable() {
        if (_numDimensions <= 2) return true;
        return false;
    }

    /** The number of dimensions of an object of this type.
     *  0 = scalar, 1 = 1D array, 2 = 2D array, etc.
     */
    private int _numDimensions;
   
    /** The dimensions of an object of this type.
     *  The array has a length given by _numDimensions
     */
    private int _dimensions[];


    private static final _instance = new DimensionTypeLattice();
}

