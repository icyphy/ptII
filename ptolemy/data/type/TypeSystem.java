/** A class that solves constraints on Types.

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
import collections.*;

//////////////////////////////////////////////////////////////////////////
//// TypeSystem
/**
This class solves constraints on Type objects.  After executing
the resolveTypes method, the type of all the appropriate Typeable 
objects will be fully resolved unless a type conflict occured.

@author Steve Neuendorffer
$Id$

*/

public class TypeSystem
{
    /** Check types on all the connections and resolve undeclared types.
     *  This method is not write-synchronized on the workspace, so one
     *  of its calling methods should be (normally Manager.resolveTypes()).
     * 
     *  @return An enumeration of Typeable objects that failed type checking.
     *  @exception InternalErrorException If a constraint is given that
     *  does not fall on ArrayTypes.
     */
    public Enumeration resolveTypes(Enumeration constraints) {
	LinkedList allconstraints = new LinkedList();
        LinkedList unknownconstraints = new LinkedList();
	LinkedList expandedconstraints = new LinkedList();
        LinkedList dataconstraints = new LinkedList();
        LinkedList dimensionconstraints = new LinkedList();
	
	//Pull out the unknown constraints
	while(constraints.hasMoreElements()) {
            Inequality constraint = (Inequality) constraints.nextElement();
	    if(constraint.getLesserTerm() instanceof UnknownType) 
		unknownconstraints.insertLast(constraint);
	    else if(constraint.getGreatedTerm() instanceof UnknownType) 
		unknownconstraints.insertLast(constraint);
	    else
		allconstraints.insertLast(constraint);
	}

	// fix up the unkown constraints.
	while() {
	}

	constraints = allconstraints.elements();

	// now expand the constraints
        while(constraints.hasMoreElements()) {
            Inequality constraint = (Inequality) constraints.nextElement();
            Enumeration expconstraints = _expandConstraint(constraint);
            expandedconstraints.appendElements(expconstraints);
	}
            
        constraints = expandedconstraints.elements();
        while(constraints.hasMoreElements()) {
            Inequality constraint = (Inequality) constraints.nextElement();
            if(_validConstraint(constraint, DataType.class)) {
                dataconstraints.insertLast(constraint);
            }
            else
                if(_validConstraint(constraint, DimensionType.class)) { 
                    dimensionconstraints.insertLast(constraint);
            }
        }
        
        DataTypeResolver dataresolver = 
            new DataTypeResolver();
        Enumeration dataconflicts = 
            dataresolver.resolveTypes(dataconstraints.elements());
        
        DimensionTypeResolver dimensionresolver = 
            new DimensionTypeResolver();
        Enumeration dimensionconflicts = 
            dimensionresolver.resolveTypes(
                    dimensionconstraints.elements());
        
        LinkedList conflicts = new LinkedList();
        conflicts.appendElements(dataconflicts);
        conflicts.appendElements(dimensionconflicts);
                
        return conflicts.elements();
    }
    
    public Enumeration _expandConstraint(Inequality constraint) {
        Type lesser = (Type)constraint.getLesserTerm();
        return lesser.expandConstraint(constraint);
    }    

    public boolean _validConstraint(Inequality constraint, Class cclass) {
        InequalityTerm lesser = constraint.getLesserTerm();
        InequalityTerm greater = constraint.getGreaterTerm();
        if((cclass.isInstance(lesser)) && (cclass.isInstance(greater))) {
            return true;
        } else
            return false;
    }
}


