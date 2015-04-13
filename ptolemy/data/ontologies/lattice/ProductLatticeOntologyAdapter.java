/** A product lattice-based ontology adapter whose constraints are derived from
 *  the component ontology solvers.
 *
 * Copyright (c) 2007-2014 The Regents of the University of California. All
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
 */

package ptolemy.data.ontologies.lattice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.ConceptFunctionInequalityTerm;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.ontologies.RecordConcept;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ProductLatticeOntologyAdapter

/** A product lattice-based ontology adapter whose constraints are derived from
 *  the component ontology solvers.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ProductLatticeOntologyAdapter extends LatticeOntologyAdapter {

    /** Construct the product lattice ontology adapter associated with the given
     *  component and product lattice ontology solver. The constructed adapter
     *  implicitly uses the default constraints set by the solver.
     *  @param solver The specified product lattice-based ontology solver.
     *  @param component The associated component.
     *  @exception IllegalActionException Thrown if the adapter cannot be
     *   initialized.
     */
    public ProductLatticeOntologyAdapter(ProductLatticeOntologySolver solver,
            Object component) throws IllegalActionException {
        this(solver, component, true);
    }

    /** Construct the product lattice ontology adapter for the given component and
     *  product lattice ontology solver.
     *  @param solver The specified product lattice-based ontology solver.
     *  @param component The given component.
     *  @param useDefaultConstraints Indicate whether this adapter uses the
     *   default actor constraints.
     *  @exception IllegalActionException Thrown if the adapter cannot be
     *   initialized.
     */
    public ProductLatticeOntologyAdapter(ProductLatticeOntologySolver solver,
            Object component, boolean useDefaultConstraints)
                    throws IllegalActionException {
        super(solver, component, useDefaultConstraints);

        _tupleAdapters = getTupleAdapters(solver, component);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the constraints of this component. The constraints is a list of
     *  inequalities. The constraints for the product lattice ontology solver
     *  are generated from the component ontology constraint lists.
     *  @return The constraints of this component.
     *  @exception IllegalActionException Thrown if there is a problem creating
     *   the constraints.
     */
    @Override
    public List<Inequality> constraintList() throws IllegalActionException {
        for (LatticeOntologyAdapter adapter : _tupleAdapters) {
            if (adapter != null) {
                Ontology adapterOntology = adapter.getSolver().getOntology();
                adapter._addDefaultConstraints(adapter.getSolver()
                        ._getConstraintType());
                addConstraintsFromTupleOntologyAdapter(
                        adapter.constraintList(), adapterOntology, this);
            }
        }
        return super.constraintList();
    }

    /** Return a list of property-able objects contained by
     *  the component. This is the union of all property-able objects
     *  for each LatticeOntologyAdapter for each component ontology.
     *  @return The list of property-able named object.
     */
    @Override
    public List<Object> getPropertyables() {
        Set<Object> propertyableSet = new HashSet<Object>();

        for (LatticeOntologyAdapter adapter : _tupleAdapters) {
            if (adapter != null) {
                propertyableSet.addAll(adapter.getPropertyables());
            }
        }
        return new ArrayList<Object>(propertyableSet);
    }

    /** Create constraints for the ProductLatticeOntologyAdapter that are
     *  derived from the given component LatticeOntologyAdapter.
     *  @param constraints The list of constraints from the original LatticeOntologyAdapter.
     *  @param sourceOntology The ontology over which the original constraints
     *   from the LatticeOntologyAdapter are defined.
     *  @param productLatticeOntologyAdapter The ProductLatticeOntologyAdapter
     *   for which we create new constraints.
     *  @exception IllegalActionException Thrown if there is an error creating
     *   the new constraints.
     */
    public static void addConstraintsFromTupleOntologyAdapter(
            List<Inequality> constraints, Ontology sourceOntology,
            LatticeOntologyAdapter productLatticeOntologyAdapter)
                    throws IllegalActionException {
        ProductLatticeOntology productOntology = ((ProductLatticeOntologySolver) productLatticeOntologyAdapter
                .getSolver()).getOntology();

        for (Inequality constraint : constraints) {
            Object greater = constraint.getGreaterTerm().getAssociatedObject();
            Object lesser = constraint.getLesserTerm().getAssociatedObject();

            // The lesser element has no associated object because it is already a concept value.
            if (lesser == null) {

                // Get the derived product lattice concept for this concept value.
                lesser = getDerivedConceptForProductLattice(
                        (Concept) constraint.getLesserTerm().getValue(),
                        productOntology);
                if (lesser != null) {
                    productLatticeOntologyAdapter.setAtLeast(greater, lesser);
                }

                // The lesser element is a concept function so it needs a wrapper for the product lattice ontology solver.
            } else if (lesser instanceof ConceptFunction) {
                String wrapperName = "wrapper_"
                        + ((ConceptFunction) lesser).getName();
                ProductLatticeWrapperConceptFunction wrapperFunction = new ProductLatticeWrapperConceptFunction(
                        wrapperName, productOntology, sourceOntology,
                        (ConceptFunction) lesser);

                // Get the dependent terms for the concept function inequality term and make them new terms
                // for the product lattice ontology.
                InequalityTerm[] dependentTerms = ((ConceptFunctionInequalityTerm) constraint
                        .getLesserTerm()).getDependentTerms();
                for (int i = 0; i < dependentTerms.length; i++) {
                    Object termObject = dependentTerms[i].getAssociatedObject();

                    if (termObject != null) {
                        dependentTerms[i] = productLatticeOntologyAdapter
                                .getSolver().getConceptTerm(termObject);
                    } else {
                        Concept concept = (Concept) dependentTerms[i]
                                .getValue();
                        Concept newTermConcept = getDerivedConceptForProductLattice(
                                concept, productOntology);
                        dependentTerms[i] = newTermConcept;
                    }
                }

                productLatticeOntologyAdapter.setAtLeast(greater,
                        new ConceptFunctionInequalityTerm(wrapperFunction,
                                dependentTerms));

                // Otherwise the lesser element is just another object in the model
                // that needs a derived concept function to transform its concept
                // value in the original lattice into a derived product lattice
                // concept value for the product lattice ontology solver.
            } else {
                // FindBugs: Useless object stored in variable domainOntologyList 
                //List<Ontology> domainOntologyList = new ArrayList<Ontology>(1);
                //domainOntologyList.add(sourceOntology);
                ProductLatticeDerivedConceptFunction derivedFunction = new ProductLatticeDerivedConceptFunction(
                        "derivedFunction", productOntology, sourceOntology);

                productLatticeOntologyAdapter
                .setAtLeast(
                        greater,
                        new ConceptFunctionInequalityTerm(
                                derivedFunction,
                                new InequalityTerm[] { productLatticeOntologyAdapter
                                        .getSolver().getConceptTerm(
                                                lesser) }));
            }
        }
    }

    /** Return the component concept value for the specified ontology from the
     *  given product lattice concept. The product lattice concept input
     *  argument could be a {@link RecordConcept} of ProductLatticeConcepts,
     *  which requires special handling beyond what is done in
     *  {@link ProductLatticeConcept#getComponentConceptValue(Ontology)}.
     *
     * @param productLatticeConcept The ProductLatticeConcept or RecordConcept
     *  of ProductLatticeConcepts from which to get
     * @param componentOntology The component ontology of the product lattice
     *  ontology from which the return concept should be taken.
     * @return The component concept value from the specified component
     *  ontology contained in the product lattice concept.
     * @exception IllegalActionException Thrown if there is a problem retrieving
     *  the component concept, or if productLatticeConcept is not an
     *  instance of ProductLatticeConcept or RecordConcept.
     */
    public static Concept getComponentConceptFromProductLatticeConcept(
            Concept productLatticeConcept, Ontology componentOntology)
                    throws IllegalActionException {

        if (productLatticeConcept instanceof RecordConcept) {
            RecordConcept originalOntologyRecordConcept = RecordConcept
                    .createRecordConcept(componentOntology);
            for (String field : ((RecordConcept) productLatticeConcept)
                    .keySet()) {
                originalOntologyRecordConcept
                .putConcept(
                        field,
                        ((ProductLatticeConcept) ((RecordConcept) productLatticeConcept)
                                .getConcept(field))
                                .getComponentConceptValue(componentOntology));
            }
            return originalOntologyRecordConcept;
        } else if (productLatticeConcept instanceof ProductLatticeConcept) {
            return ((ProductLatticeConcept) productLatticeConcept)
                    .getComponentConceptValue(componentOntology);
        } else {
            throw new IllegalActionException("The productLatticeConcept input "
                    + "must be an instance of either ProductLatticeConcept "
                    + "or RecordConcept.");
        }
    }

    /** Get the derived concept for a product lattice ontology from the given
     *  concept that is an element in one of the ontologies that comprises
     *  the product ontology. The returned concept will be a product lattice concept
     *  whose tuple will have every entry as bottom except for the input concept.
     *
     *  @param concept The given concept that must be from an ontology that comprises
     *   the given product lattice ontology.
     *  @param productOntology The given product lattice ontology.
     *  @return The derived product lattice concept.
     *  @exception IllegalActionException Thrown if the concept's ontology is not
     *   part of the given product lattice ontology.
     */
    public static Concept getDerivedConceptForProductLattice(Concept concept,
            ProductLatticeOntology productOntology)
                    throws IllegalActionException {
        if (concept instanceof RecordConcept) {
            RecordConcept productLatticeRecordConcept = RecordConcept
                    .createRecordConcept(productOntology);
            for (String field : ((RecordConcept) concept).keySet()) {
                productLatticeRecordConcept.putConcept(
                        field,
                        getDerivedConceptForProductLattice(
                                ((RecordConcept) concept).getConcept(field),
                                productOntology));
            }

            return productLatticeRecordConcept;
        } else {
            List<Ontology> tupleOntologies = productOntology
                    .getLatticeOntologies();
            Ontology sourceOntology = concept.getOntology();
            boolean foundOntology = false;

            if (tupleOntologies != null) {
                List<Concept> conceptTuple = new ArrayList<Concept>(
                        tupleOntologies.size());
                for (Ontology ontology : tupleOntologies) {

                    // FIXME: A single ontology could have multiple instances but we don't
                    // have a defined equals() method for ontologies, so this hack of
                    // comparing their Ptolemy class names is used.
                    if (sourceOntology.getName().equals(ontology.getName())) {
                        conceptTuple.add(ontology.getConceptByString(concept
                                .toString()));
                        foundOntology = true;
                    } else {
                        conceptTuple.add(ontology.getConceptGraph().bottom());
                    }
                }

                if (!foundOntology) {
                    throw new IllegalActionException(
                            "The concept "
                                    + concept.getName()
                                    + " belongs to an ontology "
                                    + sourceOntology.getName()
                                    + " that is not a component of the given product lattice ontology "
                                    + productOntology.getName() + ".");
                }
                ProductLatticeConcept value = productOntology
                        .getProductLatticeConceptFromTuple(conceptTuple);
                return value;
            } else {
                return null;
            }
        }
    }

    /** Return the adapters for each tuple ontology that comprises the
     *  product lattice ontology for this solver and model component.
     *  @param solver The ProductLatticeOntologySolver for this adapter.
     *  @param component The model component object for this adapter.
     *  @return The list of LatticeOntologyAdapters for the tuple ontology solvers.
     *  @exception IllegalActionException Thrown if there is an error in initializing
     *   the tuple ontology adapters.
     */
    public static List<LatticeOntologyAdapter> getTupleAdapters(
            ProductLatticeOntologySolver solver, Object component)
                    throws IllegalActionException {
        List<LatticeOntologyAdapter> tupleAdapters = new ArrayList<LatticeOntologyAdapter>();
        ProductLatticeOntology productOntology = solver.getOntology();
        if (productOntology == null) {
            throw new IllegalActionException(solver, "Can not apply a "
                    + ProductLatticeOntologySolver.class.getSimpleName()
                    + " that does not contain a "
                    + ProductLatticeOntology.class.getSimpleName() + ".");
        }
        List<Ontology> tupleOntologies = productOntology.getLatticeOntologies();
        List<LatticeOntologySolver> containedSolvers = solver
                .getAllContainedOntologySolvers();

        if (tupleOntologies != null && containedSolvers != null) {
            for (Ontology ontology : tupleOntologies) {
                for (LatticeOntologySolver innerSolver : containedSolvers) {

                    // FIXME: A single ontology could have multiple instances but we don't
                    // have a defined equals() method for ontologies, so this hack of
                    // comparing their Ptolemy class names is used.
                    if (innerSolver.getOntology().getName()
                            .equals(ontology.getName())) {
                        LatticeOntologyAdapter adapter = (LatticeOntologyAdapter) innerSolver
                                .getAdapter(component);
                        tupleAdapters.add(adapter);
                        break;
                    }
                }
            }
        }
        return tupleAdapters;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The list of adapters for the model component for each ontology that
     *  comprises the product lattice ontology.
     */
    List<LatticeOntologyAdapter> _tupleAdapters;
}
