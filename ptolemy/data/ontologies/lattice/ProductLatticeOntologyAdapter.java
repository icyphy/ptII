/** A product lattice-based ontology adapter whose constraints are derived from
 *  the component ontology solvers.
 * 
 * Copyright (c) 2007-2010 The Regents of the University of California. All
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

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.ConceptFunctionInequalityTerm;
import ptolemy.data.ontologies.Ontology;
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
 *  @since Ptolemy II 8.1
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
    public ProductLatticeOntologyAdapter(ProductLatticeOntologySolver solver, Object component)
            throws IllegalActionException {
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
        
        _initializeAdapter(solver, component);
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
    public List<Inequality> constraintList() throws IllegalActionException {
        if (!_useDefaultConstraints) {
            for (LatticeOntologyAdapter adapter : _tupleAdapters) {
                if (adapter != null) {        
                    adapter._addDefaultConstraints(adapter.getSolver()._getConstraintType());
                    addConstraintsFromTupleOntologyAdapter(adapter, this);
                }
            }
        }
        return super.constraintList();
    }
    
    /** Return a list of property-able objects contained by
     *  the component. This is the union of all property-able objects
     *  for each LatticeOntologyAdapter for each component ontology.
     *  @return The list of property-able named object.
     */
    public List<Object> getPropertyables() {
        HashSet<Object> propertyableSet = new HashSet<Object>();
        
        for (LatticeOntologyAdapter adapter : _tupleAdapters) {
            if (adapter != null) {
                propertyableSet.addAll(adapter.getPropertyables());
            }
        }        
        return new ArrayList<Object>(propertyableSet);
    }
    
    /** Create constraints for the ProductLatticeOntologyAdapter that are
     *  derived from the given component LatticeOntologyAdapter.
     *  @param sourceAdapter The LatticeOntologyAdapter that contains the
     *   source constraints from which to derive constraints for the
     *   ProductLatticeOntologyAdapter
     *  @param productLatticeOntologyAdapter The ProductLatticeOntologyAdapter
     *   for which we create new constraints.
     *  @throws IllegalActionException Thrown if there is an error creating
     *   the new constraints.
     */
    public static void addConstraintsFromTupleOntologyAdapter(LatticeOntologyAdapter sourceAdapter,
            LatticeOntologyAdapter productLatticeOntologyAdapter)
                throws IllegalActionException {
        List<Inequality> constraints = sourceAdapter.constraintList();
        ProductLatticeOntology productOntology =
            ((ProductLatticeOntologySolver) productLatticeOntologyAdapter.getSolver()).getOntology();
        Ontology sourceOntology = sourceAdapter.getSolver().getOntology();
        
        for (Inequality constraint : constraints) {
            Object greater = constraint.getGreaterTerm().getAssociatedObject();
            Object lesser = constraint.getLesserTerm().getAssociatedObject();
            
            // The lesser element has no associated object because it is already a concept value.
            if (lesser == null) {
                
                // Get the derived product lattice concept for this concept value.
                lesser = getDerivedConceptForProductLattice((Concept) constraint.getLesserTerm().getValue(), productOntology);
                if (lesser != null) {
                    productLatticeOntologyAdapter.setAtLeast(greater, lesser);
                }
                
            // The lesser element is a concept function so it needs a wrapper for the product lattice ontology solver.
            } else if (lesser instanceof ConceptFunction) {
                String wrapperName = "wrapper_" + ((ConceptFunction) lesser).getName();
                ProductLatticeWrapperConceptFunction wrapperFunction =
                    new ProductLatticeWrapperConceptFunction(wrapperName, productOntology, sourceOntology, (ConceptFunction) lesser);
                
                // Get the dependent terms for the concept function inequality term and make them new terms
                // for the product lattice ontology.
                InequalityTerm[] dependentTerms = ((ConceptFunctionInequalityTerm) constraint.getLesserTerm()).getDependentTerms();
                for (int i = 0; i < dependentTerms.length; i++) {
                    Object termObject = dependentTerms[i].getAssociatedObject();
                    
                    if (termObject != null) {
                        dependentTerms[i] = productLatticeOntologyAdapter.getSolver().getConceptTerm(termObject);
                    } else {
                        Concept concept = (Concept) dependentTerms[i].getValue();
                        ProductLatticeConcept newTermConcept = getDerivedConceptForProductLattice(concept, productOntology);
                        dependentTerms[i] = newTermConcept;
                    }
                }
                
                productLatticeOntologyAdapter.setAtLeast(greater,
                        new ConceptFunctionInequalityTerm(wrapperFunction, dependentTerms));      
                
            // Otherwise the lesser element is just another object in the model
            // that needs a derived concept function to transform its concept
            // value in the original latice into a derived product lattice
            // concept value for the product lattice ontology solver.
            } else {                
                List<Ontology> domainOntologyList = new ArrayList<Ontology>(1);
                domainOntologyList.add(sourceOntology);                
                ProductLatticeDerivedConceptFunction derivedFunction =
                    new ProductLatticeDerivedConceptFunction("derivedFunction", productOntology, sourceOntology);

                productLatticeOntologyAdapter.setAtLeast(greater,
                        new ConceptFunctionInequalityTerm(derivedFunction,
                                new InequalityTerm[]{ productLatticeOntologyAdapter.getSolver().getConceptTerm(lesser) }));
            }           
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
     *  @return The deived product lattice concept.
     *  @throws IllegalActionException Thrown if the concept's ontology is not
     *   part of the given product lattice ontology.
     */
    public static ProductLatticeConcept getDerivedConceptForProductLattice(Concept concept, ProductLatticeOntology productOntology)
            throws IllegalActionException {
        List<Ontology> tupleOntologies = productOntology.getLatticeOntologies();
        Ontology sourceOntology = concept.getOntology();
        boolean foundOntology = false;
        
        if (tupleOntologies != null) {            
            String productLatticeConceptName = new String("");

            for (Ontology ontology : tupleOntologies) {
                if (sourceOntology.getName().equals(ontology.getName())) {
                    productLatticeConceptName += concept.getName();
                    foundOntology = true;
                } else {
                    productLatticeConceptName += ((Concept) ontology.getCompletePartialOrder().bottom()).getName();
                }
            }
            
            if (!foundOntology) {
                throw new IllegalActionException("The concept " + concept.getName() +
                        " belongs to an ontology " + sourceOntology.getName() +
                        " that is not a component of the given product lattice ontology " + productOntology.getName() + ".");
            }

            ProductLatticeConcept value =
                (ProductLatticeConcept) productOntology.getEntity(productLatticeConceptName);            
            if (value == null) {
                throw new IllegalActionException("Could not create the derived concept for the " +
                        productOntology.getName() + " product lattice ontology for the concept " +
                        concept.getName() + " from the " + sourceOntology.getName() + " ontology.");
            }
            return value;  
        } else {
            return null;
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Initialize the adapters for each tuple ontology that comprises the
     *  product lattice ontology for this solver and model component.
     *  @param solver The ProductLatticeOntologySolver for this adapter.
     *  @param component The model component object for this adapter.
     *  @throws IllegalActionException Thrown if there is an error in initializing
     *   the tuple ontology adapters.
     */
    private void _initializeAdapter(ProductLatticeOntologySolver solver, Object component) throws IllegalActionException {
        _tupleAdapters = new ArrayList<LatticeOntologyAdapter>();
        List<Ontology> tupleOntologies = ((ProductLatticeOntology) solver.getOntology()).getLatticeOntologies();        
        List<LatticeOntologySolver> containedSolvers = solver.getAllContainedOntologySolvers();
        
        if (tupleOntologies != null && containedSolvers != null) {
            for (Ontology ontology : tupleOntologies) {
                for (LatticeOntologySolver innerSolver : containedSolvers) {
                    if (innerSolver.getOntology().getName().equals(ontology.getName())) {
                        LatticeOntologyAdapter adapter = (LatticeOntologyAdapter) innerSolver.getAdapter(component);
                        _tupleAdapters.add(adapter);
                        break;
                    }
                }
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The list of adapters for the model component for each ontology that
     *  comprises the product lattice ontology.
     */
    List<LatticeOntologyAdapter> _tupleAdapters;
}
