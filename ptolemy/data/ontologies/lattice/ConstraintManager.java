/** A class that collects and manages all the inequality constraints for a LatticeOntologySolver.

 Copyright (c) 1997-2011 The Regents of the University of California.
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


 */
package ptolemy.data.ontologies.lattice;

import java.util.List;

import ptolemy.data.StringToken;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.MultiHashMap;

/**
 * A class that collects and manages all the inequality constraints for
 * an OntologySolver.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class ConstraintManager {

    /**
     * Constructs a ConstraintManager object.
     *
     * @param solver The lattice-based ontology solver using
     * this constraint manager
     */
    public ConstraintManager(LatticeOntologySolver solver) {
        _solver = solver;
    }

    /** Sets the list of inequality constraints to be managed by the
     *  ConstraintManager.
     *  @param constraints The list of inequality constraints.
     */
    public void setConstraints(List<Inequality> constraints) {
        for (Inequality constraint : constraints) {
            InequalityTerm greaterTerm = constraint.getGreaterTerm();
            InequalityTerm lesserTerm = constraint.getLesserTerm();
            _greaterTermMap.put(greaterTerm, lesserTerm);
            _lesserTermMap.put(lesserTerm, greaterTerm);
        }
    }

    /**
     * Return the list of constraining terms for the given object.
     * @param object The given object.
     * @return The list of constraining terms for the given object.
     */
    public List<ptolemy.graph.InequalityTerm> getConstrainingTerms(Object object) {
        boolean least;
        try {
            least = ((StringToken) _solver.solvingFixedPoint.getToken())
                    .stringValue().equals("least");
        } catch (IllegalActionException e) {
            least = true;
        }

        if (least) {
            return (List<ptolemy.graph.InequalityTerm>) _greaterTermMap
                    .get(_solver.getConceptTerm(object));
        } else {
            return (List<ptolemy.graph.InequalityTerm>) _lesserTermMap
                    .get(_solver.getConceptTerm(object));
        }
    }

    /** The lattice-based ontology solver that uses this manager. */
    private LatticeOntologySolver _solver;

    /** The multi-map of the greater terms (key) to the lesser terms (values). */
    private MultiHashMap _greaterTermMap = new MultiHashMap();

    /** The multi-map of the lesser terms (key) to the greater terms (values). */
    private MultiHashMap _lesserTermMap = new MultiHashMap();
}
