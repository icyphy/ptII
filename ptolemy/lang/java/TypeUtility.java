/* Methods dealing with types.

Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.lang.java;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

/** Methods dealing with types.
<p>
Portions of this code were derived from sources developed under the
auspices of the Titanium project, under funding from the DARPA, DoE,
and Army Research Office.

@author Jeff Tsay
@version $Id$
 */
public class TypeUtility implements JavaStaticSemanticConstants {

    /** Public constructor allows inheritence of methods although this class has no
     *  instance members.
     */
    public TypeUtility() {}

    /** Return an array type with given element type and dimensions.
     *  If dims is 0, return the element type.
     */
    public static TypeNode makeArrayType(TypeNode elementType, int dims) {
        for(int i = 0; i < dims; i++) {
           elementType = new ArrayTypeNode(elementType);
        }
        return elementType;
    }

    /** Return the base type of the array type, which is not itself an array type. */
    public static TypeNode arrayBaseType(TypeNode type) {
        if (type instanceof ArrayTypeNode) {
           return arrayBaseType(((ArrayTypeNode) type).getBaseType());

        }
        return type;
    }

    /** Return the dimension of the array, which is the number of contiguous
     *  bracket pairs required after the base type.
     */
    public static int arrayDimension(TypeNode type) {
        if (type instanceof ArrayTypeNode) {
           return 1 + arrayDimension(((ArrayTypeNode) type).getBaseType());

        }
        return 0;
    }
}
