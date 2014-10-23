/* The ontology adapter class for ptolemy.domains.ontologies.lib.UnitsConverter.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice.adapters.defaultAdapters.domains.ontologies.lib;

import java.util.List;

import ptolemy.data.ontologies.lattice.LatticeOntologyAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.data.ontologies.lattice.unit.UnitConcept;
import ptolemy.graph.Inequality;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// UnitsConverter

/** The ontology adapter class for ptolemy.domains.ontologies.lib.UnitsConverter.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class UnitsConverter extends LatticeOntologyAdapter {

    /** Construct a lattice ontology adapter for the UnitsConverter actor. If the
     *  solver is the unit ontology solver for the UnitsConverter actor, then
     *  set special constraints for it.
     *  @param solver The given solver.
     *  @param actor The given UnitsConverter actor.
     *  @exception IllegalActionException If the adapter cannot be initialized.
     */
    public UnitsConverter(LatticeOntologySolver solver,
            ptolemy.domains.ontologies.lib.UnitsConverter actor)
                    throws IllegalActionException {
        super(solver, actor, false);

        if (!_solver.equals(actor.getUnitOntologySolver())) {
            _useDefaultConstraints = true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the list of constraints for the UnitsConverter actor.
     *  @return The list of constraints for this adapter.
     *  @exception IllegalActionException If there is an error creating
     *   the constraint list.
     */
    @Override
    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.domains.ontologies.lib.UnitsConverter actor = (ptolemy.domains.ontologies.lib.UnitsConverter) getComponent();

        // Check to make sure the unit system solver matches the solver specified
        // in the UnitsConverter actor.
        if (_solver.equals(actor.getUnitOntologySolver())) {

            // The UnitsConverter actor specifies which units ontology concepts
            // should be applied to its input and output ports.
            UnitConcept inputConcept = actor.getUnitConcept(true);
            UnitConcept outputConcept = actor.getUnitConcept(false);

            if (inputConcept != null) {
                setAtLeast(actor.input, inputConcept);
            }
            if (outputConcept != null) {
                setAtLeast(actor.output, outputConcept);
            }
        }

        return super.constraintList();
    }
}
