/* The default adapter class for ptolemy.actor.lib.MultiplyDivide.

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice.adapters.defaultAdapters.actor.lib;

import java.util.List;

import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.ConceptFunctionInequalityTerm;
import ptolemy.data.ontologies.lattice.DivideConceptFunctionDefinition;
import ptolemy.data.ontologies.lattice.LatticeOntologyAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.data.ontologies.lattice.MultiplyConceptFunctionDefinition;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// MultiplyDivide

/** The default adapter class for ptolemy.actor.lib.MultiplyDivide.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class MultiplyDivide extends LatticeOntologyAdapter {

    /**
     * Construct a MultiplyDivide adapter for the flatUnitSystem lattice.
     * @param solver The given solver.
     * @param actor The given MultiplyDivide actor.
     * @exception IllegalActionException If the adapter cannot be initialized.
     */
    public MultiplyDivide(LatticeOntologySolver solver,
            ptolemy.actor.lib.MultiplyDivide actor)
            throws IllegalActionException {
        super(solver, actor, false);
    }

    /** Return the list of constraints for the MultiplyDivide actor.
     *  @return The list of constraints for this adapter.
     *  @throws IllegalActionException If there is an error creating
     *   the constraint list.
     */
    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.MultiplyDivide actor = (ptolemy.actor.lib.MultiplyDivide) getComponent();

        if (actor.multiply.getWidth() > 1 || actor.divide.getWidth() > 1) {
            throw new IllegalActionException(actor, "The property analysis "
                    + "currently supports only binary division (e.g. 1 "
                    + "connection to the multiply port and 1 connection "
                    + "to the divide port.");
        }

        ConceptFunction multiplyFunction = null;
        MultiplyConceptFunctionDefinition multiplyDefinition = (MultiplyConceptFunctionDefinition) (_solver
                .getContainedModel())
                .getAttribute(LatticeOntologySolver.MULTIPLY_FUNCTION_NAME);
        if (multiplyDefinition != null) {
            multiplyFunction = multiplyDefinition.createConceptFunction();
        }

        ConceptFunction divideFunction = null;
        DivideConceptFunctionDefinition divideDefinition = (DivideConceptFunctionDefinition) (_solver
                .getContainedModel())
                .getAttribute(LatticeOntologySolver.DIVIDE_FUNCTION_NAME);
        if (divideDefinition != null) {
            divideFunction = divideDefinition.createConceptFunction();
        }

        // If the multiplyFunction is not defined in the ontology solver model
        // then by default do not set up any constraints for the multiply
        // input.
        if (multiplyFunction != null) {
            setAtLeast(actor.multiply, new ConceptFunctionInequalityTerm(
                    multiplyFunction, new InequalityTerm[] {
                            getPropertyTerm(actor.output),
                            getPropertyTerm(actor.divide) }));
        }

        if (divideFunction == null) {
            // Use these default constraints if the divideFunction is not
            // defined in the ontology solver model.
            setAtLeast(actor.output, actor.multiply);
            setAtLeast(actor.output, actor.divide);
        } else {
            setAtLeast(actor.output, new ConceptFunctionInequalityTerm(
                    divideFunction, new InequalityTerm[] {
                            getPropertyTerm(actor.multiply),
                            getPropertyTerm(actor.divide) }));
            setAtLeast(actor.divide, new ConceptFunctionInequalityTerm(
                    divideFunction, new InequalityTerm[] {
                            getPropertyTerm(actor.multiply),
                            getPropertyTerm(actor.output) }));
        }

        return super.constraintList();
    }
}
