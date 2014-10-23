/* A solver for product lattice-based ontologies.
 *
 * Copyright (c) 2007-2014 The Regents of the University of California. All
 * rights reserved.
 *
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
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
 */

package ptolemy.data.ontologies.lattice;

import java.util.ArrayList;
import java.util.List;

import ptolemy.data.StringToken;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.ontologies.OntologyAdapter;
import ptolemy.data.ontologies.OntologySolverModel;
import ptolemy.data.ontologies.OntologySolverUtilities;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// ProductLatticeOntologySolver

/** A solver for product lattice-based ontologies. This is a derived class
 *  of {@link LatticeOntologySolver} that specially handles adapters for
 *  ProductLatticeOntologies.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ProductLatticeOntologySolver extends LatticeOntologySolver {

    /** Constructor for the ProductLatticeOntologySolver.
     *  @param container The model that contains the OntologySolver
     *  @param name The name of the OntologySolver
     *  @exception IllegalActionException If there is any problem creating the
     *   OntologySolver object.
     *  @exception NameDuplicationException If there is already a component
     *   in the container with the same name
     */
    public ProductLatticeOntologySolver(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        solverStrategy.setVisibility(Settable.NONE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return all the LatticeOntologySolvers contained in the ProductLatticeOntologySolver's
     *  model. These are the solvers that are associated with each ontology that
     *  comprises the product lattice ontology for this solver.
     *  @return The list of LatticeOntologySolvers contained in the solver model, or null
     *   if the solver model is null.
     */
    public List<LatticeOntologySolver> getAllContainedOntologySolvers() {
        OntologySolverModel solverModel = (OntologySolverModel) getContainedModel();
        if (solverModel != null) {
            return solverModel.attributeList(LatticeOntologySolver.class);
        } else {
            return null;
        }
    }

    /** Return the product lattice ontology for this constraint solver.
     *  If this solver contains more than one product lattice ontology, then return the
     *  last one added. If it contains no product lattice ontologies, then return null.
     *  A product lattice ontology solver must contain at least one product lattice ontology.
     *  Ontologies that are not product lattice ontologies are ignored.
     *  @return The product lattice ontology for this constraint solver.
     *  @exception IllegalActionException Thrown if there is an error getting the
     *   ontology objects from the solver model.
     */
    @Override
    public ProductLatticeOntology getOntology() throws IllegalActionException {
        List<Ontology> ontologies = getAllContainedOntologies();
        List<ProductLatticeOntology> productLatticeOntologies = new ArrayList<ProductLatticeOntology>();
        for (Ontology ontology : ontologies) {
            if (ontology instanceof ProductLatticeOntology) {
                productLatticeOntologies.add((ProductLatticeOntology) ontology);
            }
        }

        if (!productLatticeOntologies.isEmpty()) {
            return productLatticeOntologies
                    .get(productLatticeOntologies.size() - 1);
        } else {
            return null;
        }
    }

    /** Return the LatticeOntologySolver associated with the given ontology that
     *  is contained in the ProductLatticeOntologySolver model, or null if there
     *  is none.
     *  @param containedOntology The ontology that is a subcomponent of the
     *   product lattice ontology.
     *  @return The LatticeOntologySolver that is associated with the given
     *   ontology.
     *  @exception IllegalActionException If there is an error getting the contained
     *   LatticeOntologySolvers.
     */
    public LatticeOntologySolver getRelatedSolver(Ontology containedOntology)
            throws IllegalActionException {
        if (containedOntology != null) {
            List<LatticeOntologySolver> containedSolvers = getAllContainedOntologySolvers();
            if (containedSolvers != null) {
                for (LatticeOntologySolver innerSolver : containedSolvers) {
                    Ontology innerOntology = innerSolver.getOntology();
                    if (innerOntology != null
                            && innerOntology.getName().equals(
                                    containedOntology.getName())) {
                        return innerSolver;
                    }
                }
            }
        }
        return null;
    }

    /** Initialize the solver:  Reset the solver (superclass) and then collect
     *  all of the initial constraints from the model. For a ProductLatticeOntologySolver,
     *  all the LatticeOntologySolvers for the ontologies that compose the ProductLatticeOntology
     *  must have their solver utilities object set to the same object as the
     *  ProductLatticeOntologySolver.
     *  @exception IllegalActionException If an exception occurs when
     *  collecting the constraints.
     */
    @Override
    public void initialize() throws IllegalActionException {
        //reset();
        OntologySolverUtilities productLatticeSolverUtilities = getOntologySolverUtilities();

        // Before calling initialize set all the sub ontology solvers to use the same shared utilities
        // object as the product lattice ontology solver.  This is necessary to keep track of
        // constraint relationships between Ptolemy expressions and the model elements that contain them.
        List<LatticeOntologySolver> containedSolvers = getAllContainedOntologySolvers();
        if (containedSolvers != null) {
            for (LatticeOntologySolver innerSolver : containedSolvers) {
                innerSolver
                .setOntologySolverUtilities(productLatticeSolverUtilities);
            }
        }

        super.initialize();
        /*
        reset();
        NamedObj toplevel = _toplevel();
        LatticeOntologyAdapter toplevelAdapter = (LatticeOntologyAdapter) getAdapter(toplevel);

        toplevelAdapter.reinitialize();
        if (containedSolvers != null) {
            for (LatticeOntologySolver innerSolver : containedSolvers) {
                ProductLatticeOntologyAdapter.addConstraintsFromTupleOntologyAdapter(
                        ((LatticeOntologyAdapter) innerSolver.getAdapter(toplevel)).constraintList(), innerSolver.getOntology(), toplevelAdapter);
            }
        }
        _initialConstraintList = toplevelAdapter.constraintList();
         */
    }

    /** Reset the solver. This removes the internal states of the
     *  solver (e.g.  previously recorded properties, statistics,
     *  etc.). Also resets the {@linkplain ConceptTermManager} to null
     *  and clears the trained constraints. For the ProductLatticeOntologySolver,
     *  also reset the solvers for the component ontologies in the solver model.
     */
    @Override
    public void reset() {
        super.reset();

        // After resetting the solver, reset each contained solver
        // and reinitialize their lattice ontology adapters.
        List<LatticeOntologySolver> containedSolvers = getAllContainedOntologySolvers();
        if (containedSolvers != null) {
            for (LatticeOntologySolver innerSolver : containedSolvers) {
                innerSolver.reset();
                NamedObj toplevel = _toplevel();
                try {
                    LatticeOntologyAdapter toplevelAdapter = (LatticeOntologyAdapter) innerSolver
                            .getAdapter(toplevel);
                    toplevelAdapter.reinitialize();
                    toplevelAdapter._addDefaultConstraints(innerSolver
                            ._getConstraintType());
                    toplevelAdapter._setConnectionConstraintType(innerSolver
                            ._getConstraintType());
                } catch (IllegalActionException e) {
                    throw new IllegalStateException("Could not reinitialize "
                            + "the adapters for the contained "
                            + "LatticeOntologySolvers.", e);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the LatticeOntologyAdapter for the specified
     *  component. This instantiates a new OntologyAdapter if it does
     *  not already exist for the specified component.  This returns
     *  specific LatticeOntologyAdapters for the LatticeOntologySolver. For
     *  the ProductLatticeOntologySolver it will create new adapters that are
     *  derived from the adapters of the individual LatticeOntologySolvers
     *  for the component ontologies of the product lattice ontology, unless
     *  the user has specified new adapters in the solver model.
     *
     *  @param component The specified component.
     *  @return The LatticeOntologyAdapter for the specified component.
     *  @exception IllegalActionException Thrown if the LatticeOntologyAdapter
     *   cannot be instantiated.
     */
    @Override
    protected OntologyAdapter _getAdapter(Object component)
            throws IllegalActionException {
        OntologyAdapter adapter = null;

        if (_adapterStore.containsKey(component)) {
            return _adapterStore.get(component);
        } else {
            // Next look for the adapter in the LatticeOntologySolver model.
            List<ActorConstraintsDefinitionAttribute> modelDefinedAdapters = ((OntologySolverModel) _model)
                    .attributeList(ActorConstraintsDefinitionAttribute.class);
            for (ActorConstraintsDefinitionAttribute adapterDefinitionAttribute : modelDefinedAdapters) {
                if (((StringToken) adapterDefinitionAttribute.actorClassName
                        .getToken()).stringValue().equals(
                                component.getClass().getName())) {
                    adapter = adapterDefinitionAttribute.createAdapter(
                            (ComponentEntity) component, this);
                    break;
                }
            }
        }

        if (adapter == null) {
            if (component instanceof CompositeEntity) {
                adapter = new ProductLatticeOntologyCompositeAdapter(this,
                        (CompositeEntity) component);
            } else if (component instanceof ASTPtRootNode) {
                adapter = new ProductLatticeOntologyASTNodeAdapter(this,
                        (ASTPtRootNode) component, false);
            } else {
                adapter = new ProductLatticeOntologyAdapter(this, component,
                        false);
            }
        }

        _adapterStore.put(component, adapter);
        return adapter;
    }
}
