/* A complete partial order for product lattice-based ontologies.
 *
 * Copyright (c) 2007-2013 The Regents of the University of California. All
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptGraph;
import ptolemy.data.ontologies.InfiniteConcept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.graph.CPO;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ProductLatticeCPO

/** A complete partial order for product lattice-based ontologies.
 *  Given a product lattice defined by a list of {@link ProductLatticeConcept}s,
 *  this class provides the implementation for all complete partial order
 *  operations on the product lattice. Note that this complete partial order
 *  implementation is not derived from a graph of the concepts, but rather by
 *  doing comparison operations that depend on the structure of the individual
 *  lattices that comprise the product lattice.  For example, take a product
 *  lattice P that is composed of two lattices L1 and L2.  Each lattice element
 *  concept in P is a tuple of the form &lt;C(L1), C(L2)&gt;.  To decide the
 *  relationship between two concepts in p1 and p2 in P, it is determined by
 *  the relationships of the individual concepts in their tuples. So:
 *  p1 &ge; p2 iff C1(L1) &ge; C2(L1) and C1(L2) &ge; C2(L2)
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ProductLatticeCPO extends ConceptGraph {

    /** Create a new ProductLatticeCPO from the given list of
     *  ProductLatticeConcepts.
     *  @param productOntology The product lattice ontology for which this CPO
     *   is a complete partial order.
     */
    public ProductLatticeCPO(ProductLatticeOntology productOntology) {
        _productOntology = productOntology;
        try {
            _ontologyList = _productOntology.getLatticeOntologies();
        } catch (IllegalActionException ex) {
            throw new IllegalArgumentException(
                    "Invalid product lattice ontology; "
                            + "could not get the list of tuple ontologies for the product "
                            + "lattice ontology.", ex);
        }

        _findBottom();
        _findTop();
        _cachedGLBs = new HashMap<List<Concept>, Concept>();
        _cachedLUBs = new HashMap<List<Concept>, Concept>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the bottom element of this CPO.
     *  The bottom element is the element in the CPO that is lower than
     *  all the other elements.
     *  @return An Object representing the bottom element, or
     *   <code>null</code> if the bottom does not exist.
     */
    @Override
    public Concept bottom() {
        return _bottomConcept;
    }

    /** Compare two concepts in the product lattice ontology. The arguments must be
     *  instances of {@link ProductLatticeConcept}, otherwise an exception will be thrown.
     *  This method returns one of ptolemy.graph.CPO.LOWER, ptolemy.graph.CPO.SAME,
     *  ptolemy.graph.CPO.HIGHER, ptolemy.graph.CPO.INCOMPARABLE, indicating the
     *  first argument is lower than, equal to, higher than, or incomparable with
     *  the second argument in the product lattice hierarchy, respectively.
     *  @param e1 An instance of {@link ProductLatticeConcept}.
     *  @param e2 An instance of {@link ProductLatticeConcept}.
     *  @return One of CPO.LOWER, CPO.SAME, CPO.HIGHER, CPO.INCOMPARABLE.
     *  @exception IllegalArgumentException If one or both arguments are not
     *   instances of {@link ProductLatticeConcept}, the arguments are not from
     *   the same ontology, or either argument has an empty or null concept tuple list.
     */
    @Override
    public int compare(Object e1, Object e2) {
        if (e1 == null || e2 == null) {
            return CPO.INCOMPARABLE;
        }

        if (e1 instanceof InfiniteConcept) {
            try {
                return ((InfiniteConcept) e1).compare((Concept) e2);
            } catch (IllegalActionException e) {
                return CPO.INCOMPARABLE;
            }
        } else if (e2 instanceof InfiniteConcept) {
            try {
                int oppositeResult = ((InfiniteConcept) e2)
                        .compare((Concept) e1);
                return DirectedAcyclicGraph.reverseCompareCode(oppositeResult);
            } catch (IllegalActionException e) {
                return CPO.INCOMPARABLE;
            }
        }

        _validateInputArguments(e1, e2);
        List<Concept> leftArgTuple = ((ProductLatticeConcept) e1)
                .getConceptTuple();
        List<Concept> rightArgTuple = ((ProductLatticeConcept) e2)
                .getConceptTuple();
        int tupleSize = leftArgTuple.size();
        int numSame = 0;
        int numHigher = 0;
        int numLower = 0;

        // For each pair of concepts in the tuple
        // track which ones are higher, same, or lower.
        for (int i = 0; i < tupleSize; i++) {
            Ontology tupleOntology = leftArgTuple.get(i).getOntology();
            int comparison = tupleOntology.getConceptGraph().compare(
                    leftArgTuple.get(i), rightArgTuple.get(i));

            if (comparison == CPO.HIGHER) {
                numHigher++;
            } else if (comparison == CPO.SAME) {
                numSame++;
            } else if (comparison == CPO.LOWER) {
                numLower++;
            }
        }

        // If all concepts in the tuple are the same, the product lattice concepts
        // are the same.
        if (numSame == tupleSize) {
            return CPO.SAME;

            // If all concepts in the tuple are higher or the same, the product lattice concept
            // is higher.
        } else if (numHigher == tupleSize || numHigher + numSame == tupleSize) {
            return CPO.HIGHER;

            // If all concepts in the tuple are lower or the same, the product lattice concept
            // is lower.
        } else if (numLower == tupleSize || numLower + numSame == tupleSize) {
            return CPO.LOWER;

            // Otherwise the product lattice concepts are incomparable.
        } else {
            return CPO.INCOMPARABLE;
        }
    }

    /** Compute the down-set of an element in this concept graph.
     *
     *  This is implemented by deferring to the downSet functions of the
     *  component graphs and then enumerating all the product results.
     *
     *  @param e An Object representing a ProductLatticeConcept in this
     *   concept graph.
     *  @return An array of ProductLatticeConcepts of the down-set of
     *   the given argument concept.
     *  @exception IllegalArgumentException If the passed object is not a
     *   ProductLatticeConcept or does not belong to this CPO.
     */
    @Override
    public ProductLatticeConcept[] downSet(Object e) {
        // FIXME: Modify _validateInputArguments to check single argument more
        // gracefully.  (Right now the error messages will be a little weird.)
        _validateInputArguments(e, e);
        ProductLatticeConcept productConcept = (ProductLatticeConcept) e;

        // Get individual down set from each component ontology.
        List<List<Concept>> downSets = new ArrayList<List<Concept>>();
        for (Concept c : productConcept.getConceptTuple()) {
            ConceptGraph cg = c.getOntology().getConceptGraph();
            List<Concept> downSet = Arrays.asList(cg.downSet(c));
            downSets.add(downSet);
        }

        // Take the product of each of those resulting component concepts.
        List<List<Concept>> productLatticeConcepts = new ArrayList<List<Concept>>();
        productLatticeConcepts.add(new ArrayList<Concept>());
        for (List<Concept> concepts : downSets) {
            List<List<Concept>> oldLayer = productLatticeConcepts;
            productLatticeConcepts = new ArrayList<List<Concept>>();
            for (Concept c : concepts) {
                for (List<Concept> intermediateResult : oldLayer) {
                    List<Concept> newLayer = new ArrayList<Concept>(
                            intermediateResult);
                    newLayer.add(c);
                    productLatticeConcepts.add(newLayer);
                }
            }
        }

        // Change the lists of lists into a ProductLatticeConcept[] to return.
        List<ProductLatticeConcept> result = new ArrayList<ProductLatticeConcept>();
        for (List<Concept> pc : productLatticeConcepts) {
            try {
                result.add(_productOntology
                        .getProductLatticeConceptFromTuple(pc));
            } catch (IllegalActionException ex) {
                throw new IllegalArgumentException("ProductLatticeCPO: "
                        + "Argument's ontologies do not match this CPO: "
                        + " arg = " + e + ", CPO = " + this, ex);
            }
        }
        return result.toArray(new ProductLatticeConcept[result.size()]);
    }

    /** Compute the greatest lower bound (GLB) of two elements.
     *  The GLB of two elements is the greatest element in the CPO
     *  that is lower than or the same as both of the two elements.
     *  @param e1 An Object representing an element in this CPO.
     *  @param e2 An Object representing an element in this CPO.
     *  @return An Object representing the GLB of the two specified
     *   elements, or <code>null</code> if the GLB does not exist.
     *  @exception IllegalArgumentException Thrown if the product lattice concept
     *   greatest lower bound cannot be created from the component greatest lower
     *   bound concepts.
     */
    @Override
    public Concept greatestLowerBound(Object e1, Object e2) {
        List<Concept> inputs = new ArrayList<Concept>();
        inputs.add((Concept) e1);
        inputs.add((Concept) e2);

        Concept glb = _cachedGLBs.get(inputs);
        if (glb == null) {
            _validateInputArguments(e1, e2);
            List<Concept> leftArgTuple = ((ProductLatticeConcept) e1)
                    .getConceptTuple();
            List<Concept> rightArgTuple = ((ProductLatticeConcept) e2)
                    .getConceptTuple();
            int tupleSize = leftArgTuple.size();

            List<Concept> glbTuple = new ArrayList<Concept>(tupleSize);
            for (int i = 0; i < tupleSize; i++) {
                Ontology tupleOntology = leftArgTuple.get(i).getOntology();
                Concept ithGLB = tupleOntology.getConceptGraph()
                        .leastUpperBound(leftArgTuple.get(i),
                                rightArgTuple.get(i));
                glbTuple.add(ithGLB);
            }
            try {
                glb = _productOntology
                        .getProductLatticeConceptFromTuple(glbTuple);
                _cachedGLBs.put(inputs, glb);
            } catch (IllegalActionException ex) {
                throw new IllegalArgumentException(
                        "Could not create the product "
                                + "lattice concept greatest lower bound from the "
                                + "component greates lower bound concepts.", ex);
            }
        }
        return glb;
    }

    /** Return the reason why this CPO is not a lattice, or null if it is.
     *  A Product Lattice CPO is a lattice if
     *  all of its component ontologies are lattices.
     *  @return A string representing which subontology is responsible for
     *   this not being a lattice;
     *   <code>null</code> otherwise.
     */
    @Override
    public NonProductLatticeCounterExample nonLatticeReason() {
        for (Ontology ontology : _ontologyList) {
            if (!ontology.isLattice()) {
                return new NonProductLatticeCounterExample(ontology);
            }
        }
        return null;
    }

    /** Compute the least upper bound (LUB) of two elements.
     *  The LUB of two elements is the least element in the CPO
     *  that is greater than or the same as both of the two elements.
     *  @param e1 An Object representing an element in this CPO.
     *  @param e2 An Object representing an element in this CPO.
     *  @return Nothing.
     *  @exception IllegalArgumentException Thrown if the product lattice concept
     *   least upper bound cannot be created from the component least upper bound
     *   concepts.
     */
    @Override
    public Concept leastUpperBound(Object e1, Object e2) {
        List<Concept> inputs = new ArrayList<Concept>();
        inputs.add((Concept) e1);
        inputs.add((Concept) e2);

        Concept lub = _cachedLUBs.get(inputs);
        if (lub == null) {
            if (e1 instanceof InfiniteConcept) {
                return ((InfiniteConcept) e1).leastUpperBound((Concept) e2);
            } else if (e2 instanceof InfiniteConcept) {
                return ((InfiniteConcept) e2).leastUpperBound((Concept) e1);
            }

            _validateInputArguments(e1, e2);
            List<Concept> leftArgTuple = ((ProductLatticeConcept) e1)
                    .getConceptTuple();
            List<Concept> rightArgTuple = ((ProductLatticeConcept) e2)
                    .getConceptTuple();
            int tupleSize = leftArgTuple.size();

            List<Concept> lubTuple = new ArrayList<Concept>(tupleSize);
            for (int i = 0; i < tupleSize; i++) {
                Ontology tupleOntology = leftArgTuple.get(i).getOntology();
                Concept ithLUB = tupleOntology.getConceptGraph()
                        .leastUpperBound(leftArgTuple.get(i),
                                rightArgTuple.get(i));
                lubTuple.add(ithLUB);
            }
            try {
                lub = _productOntology
                        .getProductLatticeConceptFromTuple(lubTuple);
                _cachedLUBs.put(inputs, lub);
            } catch (IllegalActionException ex) {
                throw new IllegalArgumentException(
                        "Could not create the product "
                                + "lattice concept least upper bound from the "
                                + "component least upper bound concepts.", ex);
            }
        }
        return lub;
    }

    /** Return the top element of this CPO.
     *  The top element is the element in the CPO that is higher than
     *  all the other elements.
     *  @return An Object representing the top element, or
     *   <code>null</code> if the top does not exist.
     */
    @Override
    public Concept top() {
        return _topConcept;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Set the bottom concept of the product lattice.
     */
    private void _findBottom() {
        StringBuffer conceptNameBuffer = new StringBuffer();
        for (Ontology ontology : _ontologyList) {
            conceptNameBuffer.append(ontology.getConceptGraph().bottom()
                    .getName());
        }
        String productLatticeConceptName = conceptNameBuffer.toString();
        _bottomConcept = (ProductLatticeConcept) _productOntology
                .getEntity(productLatticeConceptName);
    }

    /** Set the top concept of the product lattice.
     */
    private void _findTop() {
        StringBuffer conceptNameBuffer = new StringBuffer();
        for (Ontology ontology : _ontologyList) {
            conceptNameBuffer
                    .append(ontology.getConceptGraph().top().getName());
        }
        String productLatticeConceptName = conceptNameBuffer.toString();
        _topConcept = (ProductLatticeConcept) _productOntology
                .getEntity(productLatticeConceptName);
    }

    /** Validate that the input arguments are valid ProductLatticeConcepts in the same
     *  ontology before trying to compare them or find the greatest lower or least upper
     *  bound.  Throw an IllegalArgumentException if either argument is invalid.
     *  @param e1 The first ProductLatticeConcept argument.
     *  @param e2 The second ProductLatticeConcept argument.
     *  @exception IllegalArgumentException Thrown if either argument is invalid.
     */
    private void _validateInputArguments(Object e1, Object e2) {
        if (!(e1 instanceof ProductLatticeConcept)
                || !(e2 instanceof ProductLatticeConcept)) {
            throw new IllegalArgumentException("ProductLatticeCPO: "
                    + "Arguments are not instances of ProductLatticeConcept: "
                    + " arg1 = " + e1 + ", arg2 = " + e2);
        }

        if (!((ProductLatticeConcept) e1).getOntology().equals(
                ((ProductLatticeConcept) e2).getOntology())) {
            throw new IllegalArgumentException(
                    "Attempt to compare elements from two distinct ontologies: "
                            + " arg1 = " + e1 + ", arg2 = " + e2);
        }

        List<Concept> leftArgTuple = ((ProductLatticeConcept) e1)
                .getConceptTuple();
        List<Concept> rightArgTuple = ((ProductLatticeConcept) e2)
                .getConceptTuple();

        if (leftArgTuple == null || leftArgTuple.isEmpty()) {
            throw new IllegalArgumentException(
                    "Attempt to compare ProductLatticeConcept "
                            + "elements where one does not have a valid "
                            + "concept tuple: arg1 = " + e1);
        }

        if (rightArgTuple == null || rightArgTuple.isEmpty()) {
            throw new IllegalArgumentException(
                    "Attempt to compare ProductLatticeConcept "
                            + "elements where one does not have a valid "
                            + "concept tuple: arg2 = " + e2);
        }

        if (leftArgTuple.size() != rightArgTuple.size()) {
            throw new IllegalArgumentException(
                    "Attempt to compare "
                            + "ProductLatticeConcept elements that do not have the same size "
                            + "concept tuple arrays even though they are in the same "
                            + "Ontology. This is an error." + " arg1 = " + e1
                            + ", arg2 = " + e2);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The bottom concept of the product lattice. */
    private ProductLatticeConcept _bottomConcept;

    /** The map of cached greatest lower bounds that have already been calculated by the CPO. */
    private Map<List<Concept>, Concept> _cachedGLBs;

    /** The map of cached least upper bounds that have already been calculated by the CPO. */
    private Map<List<Concept>, Concept> _cachedLUBs;

    /** The list of Ontologies for each element in the concept tuple of
     *  each ProductLatticeConcept in the product lattice.
     */
    private List<Ontology> _ontologyList;

    /** The product lattice ontology for which this is a complete partial order. */
    private ProductLatticeOntology _productOntology;

    /** The bottom concept of the product lattice. */
    private ProductLatticeConcept _topConcept;
}
