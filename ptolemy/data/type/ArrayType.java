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
import ptolemy.kernel.util.InternalErrorException;
import java.util.Enumeration;
import collections.*;

//////////////////////////////////////////////////////////////////////////
//// ArrayType
/**
A class representing the type of a multidimensional array.   The type
of an array is represented as a composite type consisting of a Type object and
a DimensionType.

@author Steve Neuendorffer
$Id$

*/

public class ArrayType extends CompositeType 
{
    /** 
     * Create a new array type constant with the given characteristics.
     * @param type The type of data contained in the array.
     * @param dimension The size of the array.
     */
    public ArrayType(Type type, DimensionType dimension) {
        _dimension = dimension;
 	_type = type;
   }

   /** Return true if the given type is equal to this type.   In other words, 
     *  an object with this type can be expressed as an object of Type t with 
     *  no conversion.
     */
    public boolean equals(Object type) {
        if(!(type instanceof ArrayType)) return false;
        ArrayType dtype = (ArrayType) type;
        
        if(!_dimension.equals(dtype._dimension)) return false;
        return _type.equals(dtype._type);
    }

    /** Given a constraint on this Type, return an enumeration of constraints
     *  on other types, on which this type depends.
     *  If the constraint is not on array types, then throw an exception.
     */
    public Enumeration expandConstraint(Inequality constraint) {
        LinkedList list = new LinkedList();
        
        // Add a constraint on the types contained by the array type         
        ArrayType lesser = (ArrayType) constraint.getLesserTerm();
        ArrayType greater = (ArrayType) constraint.getGreaterTerm();
        Inequality newConstraint;
        // Create a constraint on the Type
        newConstraint = new Inequality(lesser._type, 
                greater._type);
        list.appendElements(lesser._type.expandConstraint(newConstraint));
        // Create a constriant on the Dimension
        newConstraint = new Inequality(lesser._dimension, 
                greater._dimension);
        list.appendElements(lesser._dimension.expandConstraint(newConstraint));
        return list.elements();
    }

    /** Return the dimension type of this
     *  array.
     */
    public DimensionType getDimension() {
        return _dimension;
    }

    /** Return the type that is associated with all the elements of this
     *  array.
     */
    public Type getType() {
        return _type;
    }

    /** Check whether the current type of this term is acceptable,
     *  and return true if it is.  Normally, a type is acceptable
     *  if it represents an instantiable object.
     *  @return True if the current type is acceptable.
     */
    public boolean isValueAcceptable() {
        return _type.isValueAcceptable() && _dimension.isValueAcceptable();
    }

    /** Return a string representing this type
     */
    public String toString() {
        String s = new String("ArrayType(");
        s += _type.toString() + "," +
            _dimension.toString();
        s += ")";
        return s;
    }

    /** The number of dimensions of an object of this type.
     *  1 = 1D array, 2 = 2D array, etc.
     */
    private DimensionType _dimension;
   
    /** The type of the objects contained in this array.
     */
    private Type _type;
}



