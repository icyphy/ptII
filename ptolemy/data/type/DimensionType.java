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
//// DimentionType
/**
A class representing the size of a multi-dimensional array as a type.

@author Steve Neuendorffer
$Id$

*/

public class DimensionType implements Type implements CPO
{
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
}

