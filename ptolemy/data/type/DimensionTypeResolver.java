/** A class that solves constraints on DimensionTypes.

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
//// DimensionTypeResolver
/**
This class solves constraints on DimensionType objects.  After executing
the resolveTypes method, the dimension type of all the appropriate Typeable 
objects will be fully resolved unless a type conflict occured.

@author Steve Neuendorffer
$Id$

*/

public class DimensionTypeResolver 
{

    /** Check types on all the connections and resolve undeclared types.
     *  If the container is not an instance of TypedCompositeActor,
     *  do nothing.
     *  This method is write-synchronized on the workspace.
     *  @exception TypeConflictException If type conflict is detected.
     */
    public void resolveTypes() throws TypeConflictException {
        if ( !(_container instanceof TypedCompositeActor)) {
            return;
        }
	try {
	    workspace().getWriteAccess();
            
            _setState(RESOLVING_TYPES);
            
	    LinkedList conflicts = new LinkedList();
	    conflicts.appendElements(
                    ((TypedCompositeActor)_container).checkTypes());

            Enumeration constraints =
                ((TypedCompositeActor)_container).typeConstraints();

	    if (constraints.hasMoreElements()) {
                InequalitySolver solver = new InequalitySolver(
                        TypeLattice.lattice());
	        while (constraints.hasMoreElements()) {
                    Inequality ineq = (Inequality)constraints.nextElement();
                    solver.addInequality(ineq);
	        }

                // find the least solution (most specific types)
                boolean resolved = solver.solveLeast();
                if ( !resolved) {
		    Enumeration unsatisfied = solver.unsatisfiedInequalities();
		    while (unsatisfied.hasMoreElements()) {
		        Inequality ineq =
                            (Inequality)unsatisfied.nextElement();
		        InequalityTerm term =
					(InequalityTerm)ineq.getLesserTerm();
		        Object typeObj = term.getAssociatedObject();
		        if (typeObj != null) {
			    // typeObj is a Typeable
			    conflicts.insertLast(typeObj);
		        }
		        term = (InequalityTerm)ineq.getGreaterTerm();
		        typeObj = term.getAssociatedObject();
		        if (typeObj != null) {
			    // typeObj is a Typeable
			    conflicts.insertLast(typeObj);
		        }
		    }
                }

	        // check whether resolved types are acceptable.
                // They might be, for example, NaT.
	        Enumeration var = solver.variables();
	        while (var.hasMoreElements()) {
		    InequalityTerm term = (InequalityTerm)var.nextElement();
		    if ( !term.isTypeAcceptable()) {
		        conflicts.insertLast(term.getAssociatedObject());
		    }
	        }
	    }

	    if (conflicts.size() > 0) {
		throw new TypeConflictException(conflicts.elements(),
                        "Type conflicts occurred in " + _container.getFullName()
			+ " on the following Typeables:");
	    }
	} catch (IllegalActionException iae) {
	    // this should not happen.
	    throw new InternalErrorException(iae.getMessage());
	} finally {
	    workspace().doneWriting();
	}
    }

}

