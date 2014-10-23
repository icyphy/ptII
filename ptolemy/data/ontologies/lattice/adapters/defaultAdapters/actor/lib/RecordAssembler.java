/* An adapter class for ptolemy.actor.lib.RecordAssembler.
 *
 * Copyright (c) 2006-2014 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 *
 */
package ptolemy.data.ontologies.lattice.adapters.defaultAdapters.actor.lib;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import ptolemy.data.ontologies.ConceptFromRecordField;
import ptolemy.data.ontologies.ConceptFunctionInequalityTerm;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.ontologies.RecordFromIndividualConcepts;
import ptolemy.data.ontologies.lattice.LatticeOntologyAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver.ConstraintType;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// RecordAssembler

/** An adapter class for ptolemy.actor.lib.RecordAssembler.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (mankit)
 *  @Pt.AcceptedRating Red (mankit)
 */
public class RecordAssembler extends LatticeOntologyAdapter {

    /** Construct a RecordAssembler adapter for the staticDynamic lattice. This
     *  set a permanent constraint for the output port to be STATIC, but does not
     *  use the default actor constraints.
     *  @param solver The given solver.
     *  @param actor The given RecordAssembler actor
     *  @exception IllegalActionException If the adapter cannot be created.
     */
    public RecordAssembler(LatticeOntologySolver solver,
            ptolemy.actor.lib.RecordAssembler actor)
                    throws IllegalActionException {
        super(solver, actor, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the list of constraints for the RecordAssembler actor.
     *  @return The list of constraints for this adapter.
     *  @exception IllegalActionException If there is an error creating
     *   the constraint list.
     */
    @Override
    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.RecordAssembler actor = (ptolemy.actor.lib.RecordAssembler) getComponent();
        Ontology ontology = _solver.getOntology();
        SortedSet<String> fieldLabels = new TreeSet<String>();

        for (Object port : actor.inputPortList()) {
            fieldLabels.add(((Port) port).getName());
        }

        // The InequalityTerm array must order the terms from the input ports
        // in the exact same order as the SortedSet fieldLabels set of
        // label names.  This is necessary to ensure that the RecordFromIndividualConcepts
        // concept function is correctly evaluated.
        InequalityTerm[] inputPortTerms = new InequalityTerm[fieldLabels.size()];
        int counter = 0;
        for (String field : fieldLabels) {
            inputPortTerms[counter++] = getPropertyTerm(actor.getPort(field));
        }

        if (interconnectConstraintType == ConstraintType.EQUALS
                || interconnectConstraintType == ConstraintType.SINK_GE_SOURCE) {
            setAtLeast(actor.output, new ConceptFunctionInequalityTerm(
                    new RecordFromIndividualConcepts("recordConcept",
                            fieldLabels, ontology), inputPortTerms));
        }

        if (interconnectConstraintType == ConstraintType.EQUALS
                || interconnectConstraintType == ConstraintType.SOURCE_GE_SINK) {
            for (Object port : actor.inputPortList()) {
                setAtLeast(port, new ConceptFunctionInequalityTerm(
                        new ConceptFromRecordField("conceptFromRecord",
                                ((Port) port).getName(), ontology),
                                new InequalityTerm[] { getPropertyTerm(actor.output) }));
            }
        }

        return super.constraintList();
    }
}
