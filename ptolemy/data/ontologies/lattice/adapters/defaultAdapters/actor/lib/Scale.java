/* The default adapter class for ptolemy.actor.lib.Scale.

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
package ptolemy.data.ontologies.lattice.adapters.defaultAdapters.actor.lib;

import java.util.List;

import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.ConceptFunctionDefinitionAttribute;
import ptolemy.data.ontologies.ConceptFunctionInequalityTerm;
import ptolemy.data.ontologies.lattice.LatticeOntologyAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver.ConstraintType;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Scale

/** The default adapter class for ptolemy.actor.lib.Scale.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class Scale extends LatticeOntologyAdapter {

    /** Construct a default lattice ontology adapter for the Scale actor.
     *  @param solver The given solver.
     *  @param actor The given Scale actor.
     *  @exception IllegalActionException If the adapter cannot be initialized.
     */
    public Scale(LatticeOntologySolver solver, ptolemy.actor.lib.Scale actor)
            throws IllegalActionException {
        super(solver, actor, false);

        _multiplyDefinition = (ConceptFunctionDefinitionAttribute) _solver
                .getContainedModel().getAttribute(
                        LatticeOntologySolver.MULTIPLY_FUNCTION_NAME);

        // If a definition for a multiplication concept function cannot be
        // found, just use the default constraints.
        if (_multiplyDefinition == null) {
            _useDefaultConstraints = true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the list of constraints for the Scale actor.
     *  @return The list of constraints for this adapter.
     *  @exception IllegalActionException If there is an error creating
     *   the constraint list.
     */
    @Override
    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.Scale actor = (ptolemy.actor.lib.Scale) getComponent();

        ConceptFunction multiplyFunction = null;
        if (_multiplyDefinition != null) {
            multiplyFunction = _multiplyDefinition.createConceptFunction();
        }

        if (multiplyFunction != null) {
            if (interconnectConstraintType == ConstraintType.EQUALS
                    || interconnectConstraintType == ConstraintType.SINK_GE_SOURCE) {
                // The output of the Scale actor is the product of the input and the factor parameter
                // So use the MultiplyConceptFunction for the output property.
                setAtLeast(actor.output, new ConceptFunctionInequalityTerm(
                        multiplyFunction, new InequalityTerm[] {
                                getPropertyTerm(actor.input),
                                getPropertyTerm(actor.factor) }));
            }
        }

        // Add back in default constraints for the output to input relationship.
        if (!_useDefaultConstraints
                && (interconnectConstraintType == ConstraintType.EQUALS || interconnectConstraintType == ConstraintType.SOURCE_GE_SINK)) {
            setAtLeast(actor.input, actor.output);
            setAtLeast(actor.factor, actor.output);
        }

        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the list of propertyable attributes for the actor referred to
     *  by this adapter.
     *  @return The list of propertyable attributes.
     */
    @Override
    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = super._getPropertyableAttributes();

        // The factor parameter for the Scale actor must be added to the list of
        // propertyable attributes in order for its property to be resolved.
        result.add(((ptolemy.actor.lib.Scale) getComponent()).factor);

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The multiplication concept function definition found in the solver model. */
    private ConceptFunctionDefinitionAttribute _multiplyDefinition;
}
