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
//// Dimension
/**
A class representing the size of a multi-dimensional array as a type.

@author Steve Neuendorffer
$Id$

*/

public class Dimension implements TypeValue
{
    /** 
     * Create a new array type constant with the given characteristics.
     * @param dimensions The number of dimensions of the new type.
     * @param size An array of the size in each dimension of the new type.
     */
    public Dimension(int dimensions, int size[]) {
        _dimensions = dimensions;
        _size = size;
   }

    /** Return true if the given type is equal to this type.   In other words, 
     *  an object with this type can be expressed as an object of Type t with 
     *  no conversion.
     */
    public boolean equals(Object type) {
        if(!(type instanceof Dimension)) return false;
        Dimension dtype = (Dimension) type;
        if(_dimensions != dtype._dimensions) return false;
        int i;
        for(i = 0; i < _dimensions; i++) {
            if(_size[i] != dtype._size[i]) return false;
        }
        return true;
    }

    /** Check whether the current type of this term is acceptable,
     *  and return true if it is.  Normally, a type is acceptable
     *  if it represents an instantiable object.
     *  @return True if the current type is acceptable.
     */
    public boolean isInstantiable() {
        if(this.equals(TOP)) return false;
        if(this.equals(BOTTOM)) return false;
        if(_dimensions != 2) return false;
        return true;
    }

    /** Return a string representing this type
     */
    public String toString() {
        String s = new String("Dimension(");
        s += _dimensions + ", {";
        int i;
        for(i = 0; i < _dimensions; i++) {
            s += _size[i] + ", ";
        }
        
        s += ")";
        return s;
    }

    /** Return the lattice associated with this type
     */
    public static CPO getTypeLattice() {
        return DimensionLattice.getInstance();
    }

   /** 
     * An unspecified dimension type.
     */
    public static final Dimension BOTTOM = new Dimension(-1, null);
    
    /**
     * A general dimension type.
     */
    public static final Dimension TOP = new Dimension(-2, null);

    /** The number of dimensions of an object of this type.
     *  1 = 1D array, 2 = 2D array, etc.
     */
    private int _dimensions;
   
    /** The dimensions of an object of this type.
     *  The array has a length given by _dimensions
     */
    private int _size[];
}



