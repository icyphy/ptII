/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2008-2009 The Regents of the University of California.
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
*/
/**
 *
 */
package ptolemy.data.properties.lattice;

import java.util.List;

import ptolemy.data.properties.lattice.PropertyConstraintHelper.Inequality;
import ptolemy.data.properties.util.MultiHashMap;
import ptolemy.data.properties.util.MultiMap;
import ptolemy.graph.InequalityTerm;

/**
 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0.4
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */

public class ConstraintManager {

    public ConstraintManager(PropertyConstraintSolver solver) {
        _solver = solver;
    }

    /**
     *
     * @param constraints
     */
    public void setConstraints(List<Inequality> constraints) {
        for (Inequality constraint : constraints) {
            if (constraint.isBase()) {
                InequalityTerm greaterTerm = constraint.getGreaterTerm();
                InequalityTerm lesserTerm = constraint.getLesserTerm();

                _greaterTermMap.put(greaterTerm, lesserTerm);
                _lesserTermMap.put(lesserTerm, greaterTerm);
            }
        }
    }

    /**
     * Return the list of constrainting terms for the given object.
     * @param object The given object.
     * @return The list of constrainting terms for the given object.
     */
    public List<PropertyTerm> getConstraintingTerms(Object object) {
        boolean least = _solver.solvingFixedPoint.getExpression().equals("least");

        if (least) {
            return (List<PropertyTerm>) _greaterTermMap.get(_solver.getPropertyTerm(object));
        } else {
            return (List<PropertyTerm>) _lesserTermMap.get(_solver.getPropertyTerm(object));
        }
    }


    /** The property constraint solver that uses this manager. */
    private PropertyConstraintSolver _solver;

    /** The multi-map of the greater terms (key) to the lesser terms (values). */
    private MultiMap _greaterTermMap = new MultiHashMap();

    /** The multi-map of the lesser terms (key) to the greater terms (values). */
    private MultiMap _lesserTermMap = new MultiHashMap();
}

