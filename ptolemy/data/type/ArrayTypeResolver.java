/** A class that solves constraints on ArrayTypes.

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
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// ArrayTypeResolver
/**
This class solves constraints on ArrayType objects.  After executing
the resolveTypes method, the dimension type of all the appropriate Typeable 
objects will be fully resolved unless a type conflict occured.

@author Steve Neuendorffer
$Id$

*/

public class ArrayTypeResolver implements TypeResolver
{

    /** Check types on all the connections and resolve undeclared types.
     *  This method is not write-synchronized on the workspace, so one
     *  of its calling methods should be (normally Manager.resolveTypes()).
     *  Constraints that are not constraints on ArrayTypes are ignored.
     * 
     *  @return An enumeration of Typeables for which no solution was
     *  found for the array type constraints.
     */
    public Enumeration resolveTypes(Enumeration constraints) {
	LinkedList arrayconstraints = new LinkedList();
    
	while(constraints.hasMoreElements()) {
            Inequality constraint = (Inequality) constraints.nextElement();
            InequalityTerm lesser = constraint.getLesserTerm();
            InequalityTerm greater = constraint.getGreaterTerm();

            if((lesser instanceof ArrayType) && 
                    (greater instanceof ArrayType)) {
                arrayconstraints.insertLast(constraint);
            }
        }

        LatticeTypeResolver arrayresolver = 
            new LatticeTypeResolver(ArrayType.getTypeLattice());
        Enumeration arrayconflicts = 
            arrayresolver.resolveTypes(arrayconstraints.elements());

        return arrayconflicts;
    }
}

