/* A class that creates a product lattice-based ontology adapter from
 * a model-based actor constraints definition attribute.
 *
 * Copyright (c) 2010-2014 The Regents of the University of California. All
 * rights reserved.
 *
 * Permission is hereby granted, without written agreement and
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
 *
 */
package ptolemy.data.ontologies.lattice;

import static ptolemy.data.ontologies.lattice.ActorConstraintsDefinitionAttribute.getActorElementName;
import static ptolemy.data.ontologies.lattice.ActorConstraintsDefinitionAttribute.isActorElementAPort;
import static ptolemy.data.ontologies.lattice.ActorConstraintsDefinitionAttribute.isActorElementAnAttribute;
import static ptolemy.data.ontologies.lattice.ActorConstraintsDefinitionAttribute.isActorElementIgnored;
import static ptolemy.data.ontologies.lattice.ActorConstraintsDefinitionAttribute.isActorElementUnconstrained;

import java.util.ArrayList;
import java.util.List;

import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.ontologies.Ontology;
import ptolemy.graph.Inequality;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// ActorProductLatticeConstraintsDefinitionAdapter.java

/** A class that creates a product lattice-based ontology adapter from
 *   a model-based actor constraints definition attribute.
 *
 *  @see ActorConstraintsDefinitionAttribute
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Green (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ActorProductLatticeConstraintsDefinitionAdapter extends
        ActorConstraintsDefinitionAdapter {

    /** Construct the lattice ontology adapter for the given component
     *  and property lattice.
     *  @param solver The specified lattice-based ontology solver.
     *  @param component The given component.
     *  @param constraintExpressions The list of constraint
     *   expressions for each port or component in the actor.
     *  @exception IllegalActionException Thrown if the adapter cannot be
     *   initialized.
     */
    public ActorProductLatticeConstraintsDefinitionAdapter(
            ProductLatticeOntologySolver solver, ComponentEntity component,
            List<StringParameter> constraintExpressions)
            throws IllegalActionException {
        // Don't use default constraints for user-defined actor constraints.
        super(solver, component, constraintExpressions);

        _tupleAdapters = ProductLatticeOntologyAdapter.getTupleAdapters(solver,
                component);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** String representing that the actor port or attribute should inherit
     *  its constraints from the tuple ontology solvers for the product
     *  lattice ontology solver.
     */
    public static final String INHERIT = "INHERIT";

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the actor element is set to inherit its constraints from
     *  the tuple ontologies that comprise the product lattice ontology,
     *  false otherwise.
     *  @param actorElementConstraintExpression The constraint expression
     *   for the actor element.
     *  @return true if the actor element is set to inherit its constraints from
     *   the tuple ontologies that comprise the product lattice ontology,
     *   false otherwise.
     *  @exception IllegalActionException If the constrain expression parameter is null.
     */
    public static boolean areActorElementConstraintsInherited(
            StringParameter actorElementConstraintExpression)
            throws IllegalActionException {
        if (actorElementConstraintExpression == null) {
            throw new IllegalActionException(
                    "The constraint expression for the actor"
                            + " element cannot be null.");
        }
        return actorElementConstraintExpression.getExpression().trim()
                .equals(INHERIT);
    }

    /** Return the constraints of this component. The constraints are
     *  generated from the expressions passed in from an
     *  ActorConstraintsDefinitionAttribute that allows the user to
     *  define actor constraints in the OntologySolver model.
     *  @return The list of constraints for this component.
     *  @exception IllegalActionException If there is a problem
     *   parsing the constraint expression strings to create the actor
     *   constraints.
     */
    @Override
    public List<Inequality> constraintList() throws IllegalActionException {
        for (StringParameter constraintExpression : _constraintTermExpressions) {
            String objName = getActorElementName(constraintExpression);
            NamedObj actorElement = null;

            if (isActorElementAPort(constraintExpression)) {
                actorElement = ((ComponentEntity) getComponent())
                        .getPort(objName);
            } else if (isActorElementAnAttribute(constraintExpression)) {
                actorElement = ((ComponentEntity) getComponent())
                        .getAttribute(objName);
            }

            // if the actor element's constraints are inherited, get them from
            // the tuple adapters.
            if (areActorElementConstraintsInherited(constraintExpression)) {
                for (LatticeOntologyAdapter adapter : _tupleAdapters) {
                    if (adapter != null) {
                        Ontology adapterOntology = adapter.getSolver()
                                .getOntology();
                        adapter._addDefaultConstraints(adapter.getSolver()
                                ._getConstraintType());
                        List<Inequality> actorElementConstraints = _getActorElementConstraints(
                                actorElement, adapter.constraintList());
                        ProductLatticeOntologyAdapter
                                .addConstraintsFromTupleOntologyAdapter(
                                        actorElementConstraints,
                                        adapterOntology, this);
                    }
                }
            } else if (!isActorElementIgnored(constraintExpression)
                    && !isActorElementUnconstrained(constraintExpression)) {
                _setConstraints(actorElement,
                        ((StringToken) constraintExpression.getToken())
                                .stringValue());
            }
        }

        _constrainAttributes();
        _addSubAdapterConstraints();
        return _union(_ownConstraints, _subAdapterConstraints);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the list of constraints that apply only to the given actor
     *  element. These are the inequality constraints in which the greater
     *  term refers to the given actor element.
     *  @param actorElement The given actor element for which we want to collect
     *   constraints.
     *  @param adapterConstraintsList The full list of constraints from the
     *   lattice ontology adapter from which the actor element constraints are taken.
     *  @return The list of constraints that only apply to the given actor element.
     */
    private List<Inequality> _getActorElementConstraints(NamedObj actorElement,
            List<Inequality> adapterConstraintsList) {
        List<Inequality> elementConstraints = new ArrayList<Inequality>(
                adapterConstraintsList);

        for (Inequality constraint : adapterConstraintsList) {
            if (!actorElement.equals(constraint.getGreaterTerm()
                    .getAssociatedObject())) {
                elementConstraints.remove(constraint);
            }
        }

        return elementConstraints;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The list of adapters for the model component for each ontology that
     *  comprises the product lattice ontology.
     */
    List<LatticeOntologyAdapter> _tupleAdapters;
}
