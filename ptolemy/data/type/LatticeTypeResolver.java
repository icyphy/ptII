/** A class that solves type constraints expressed over a lattice.

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
import java.util.Iterator;
import collections.*;

//////////////////////////////////////////////////////////////////////////
//// LatticeTypeResolver
/**
This class solves type constraints on a lattice of objects.  After executing
the resolveTypes method, the inequalities will be fulfilled according to the
type lattice, or the Enumeration will contain references to the Typeable 
objects which failed to type check.

@author Steve Neuendorffer
$Id$

*/

public class LatticeTypeResolver {
    /** 
     * Construct a new type resolver for types on the given lattice.
     */
    public LatticeTypeResolver(CPO lattice) {
        _lattice = lattice;
    }

    /** Check types on all the connections and resolve undeclared types.
     *  This method is not write-synchronized on the workspace, so one
     *  of its calling methods should be (normally Manager.resolveTypes()).
     * 
     *  @return An enumeration of Typeable objects that failed type checking.
     *  @exception InternalErrorException If a constraint is given that
     *  does not fall on DimensionTypes.
     */
    public Enumeration resolveTypes(Enumeration constraints) {

        LinkedList conflicts = new LinkedList();

        if (constraints.hasMoreElements()) {
            InequalitySolver solver = new InequalitySolver(
                    _lattice);
            while (constraints.hasMoreElements()) {                
                Inequality ineq = (Inequality)constraints.nextElement();
                solver.addInequality(ineq);
            }                        

            // find the least solution (most specific types)
            boolean resolved = solver.solveLeast();
            
            if (!resolved) {
                Iterator unsatisfied = solver.unsatisfiedInequalities();
                while (unsatisfied.hasNext()) {
                    Inequality ineq =
                        (Inequality)unsatisfied.next();
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
        return conflicts.elements();
    }

    private CPO _lattice;
}

