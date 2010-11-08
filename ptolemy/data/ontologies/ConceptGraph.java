/*
 * The data structure for the graph of relations between concepts in an ontology.
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
 * 
 */
package ptolemy.data.ontologies;

import java.util.Collection;

import ptolemy.graph.CPO;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Node;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ConceptGraph

/** A data structure defining the relationships in an ontology. An ontology is a set of concepts
 *  and the relationships between them.  In a general ontology the graph describing the relationships
 *  between concepts need not be a directed acyclic graph (DAG).  But we restrict our implementation
 *  to a DAG because we currently deal only with ontologies than can be partially ordered. This is
 *  particularly important for an ontology whose graph is a lattice, where we can use the Reihof and
 *  Mogensen algorithm to do a scalable analysis
 *  and inference on a model to assign concepts from the ontology to each element in the model.
 *  This specialization is implemented as a {@linkplain ptolemy.data.ontologies.lattice.LatticeOntologySolver
 *  LatticeOntologySolver}, a subclass of {@linkplain OntologySolver}.
 * 
 * @author Thomas Mandl, Man-Kit Leung, Edward A. Lee, Ben Lickly, Dai Bui, Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 * @see ptolemy.graph.CPO
 */
public class ConceptGraph implements CPO {
    
    /** Create an empty concept graph with no concepts in it.
     */
    public ConceptGraph() {
        _dag = new DirectedAcyclicGraph();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add a relation between two Concepts as an edge to the graph.
     * 
     * @param weight1 The source concept
     * @param weight2 The sink concept
     * @param newEdgeWeight The ConceptRelation between the two concepts weight1
     *  and weight2.
     * @return The set of edges that were added; each element
     *  of this set is an instance of {@link Edge}.
     * @exception IllegalArgumentException If the newEdgeWeight argument is not an
     *  instance of {@link ConceptRelation}.
     */
    public Collection<Edge> addEdge(FiniteConcept weight1, FiniteConcept weight2,
            ConceptRelation newEdgeWeight) {
        return _dag.addEdge(weight1, weight2, newEdgeWeight);
    }

    /** Add a concept to this concept graph.
     *  @param weight The concept.
     *  @return The constructed node in the graph.
     *  @exception IllegalArgumentException If the argument is not
     *   an instance of {@link FiniteConcept}.
     */
    public Node addNodeWeight(FiniteConcept weight) {
        return _dag.addNodeWeight(weight);
    }

    /** Return the least element of this concept graph.
     *  @return The least element of this graph.
     */
    public Concept bottom() {
        return (Concept)_dag.bottom();
    }

    /** Compare two concepts in the ontology. The arguments must be
     *  instances of {@link FiniteConcept}, otherwise an exception will be thrown.
     *  This method returns one of ptolemy.graph.CPO.LOWER, ptolemy.graph.CPO.SAME,
     *  ptolemy.graph.CPO.HIGHER, ptolemy.graph.CPO.INCOMPARABLE, indicating the
     *  first argument is lower than, equal to, higher than, or incomparable with
     *  the second argument in the property hierarchy, respectively.
     *  @param e1 An instance of {@link FiniteConcept}.
     *  @param e2 An instance of {@link FiniteConcept}.
     *  @return One of CPO.LOWER, CPO.SAME, CPO.HIGHER, CPO.INCOMPARABLE.
     *  @exception IllegalArgumentException If one or both arguments are not
     *   instances of {@link FiniteConcept}.
     */
    public int compare(Object e1, Object e2) {
        if (!(e1 instanceof Concept) || !(e2 instanceof Concept)) {
            throw new IllegalArgumentException("ConceptGraph.compare: "
                    + "Arguments are not instances of Concept: "
                    + " arg1 = " + e1 + ", arg2 = " + e2);
        }

        if ((e1 instanceof FiniteConcept) && (e2 instanceof FiniteConcept)) {
            return _dag.compare(e1, e2);
        } else if (e1 instanceof InfiniteConcept) {
            try {
                return ((InfiniteConcept)e1).compare((Concept)e2);
            } catch (IllegalActionException e) {
                return CPO.INCOMPARABLE;
            }
        } else { // (e2 instanceof InfiniteConcept)
            try {
                int oppositeResult = ((InfiniteConcept)e2).compare((Concept)e1);
                return DirectedAcyclicGraph.reverseCompareCode(oppositeResult);
            } catch (IllegalActionException e) {
                return CPO.INCOMPARABLE;
            }
        }
    }

    /** Compute the down-set of an element in this CPO.
     *  The down-set of an element is the subset consisting of
     *  all the elements lower than or the same as the specified element.
     *  @param e An Object representing an element in this CPO.
     *  @return An array of Concepts of the down-set of the
     *   specified element.
     *  @exception IllegalArgumentException If the specified Object is not
     *   an element in this CPO, or the resulting set is infinite.
     */
    public Concept[] downSet(Object e) {
        // FIXME: What happens if the downSet should contain some InfiniteConcepts
        // that are lower in the lattice?
        if (e instanceof FiniteConcept) {
            Object[] set = _dag.downSet(e);
            Concept[] downSet = new Concept[set.length];

            for (int i = 0; i < set.length; i++) {
                downSet[i] = (Concept) set[i];
            }

            return downSet;
        } else {
            // FIXME: Need to implement downSet for InfiniteConcepts.
            throw new IllegalArgumentException("downSet method not implemented" +
            		" for Concept subclass " + e.getClass().getName() + ".");
        }
    }

    /** Compute the greatest element of a subset.
     *  The greatest element of a subset is an element in the
     *  subset that is higher than all the other elements in the
     *  subset.
     *  @param subset An array of Objects representing the subset.
     *  @return An Object representing the greatest element of the subset,
     *   or <code>null</code> if the greatest element does not exist.
     *  @exception IllegalArgumentException If at least one Object in the
     *   specified array is not an element of this CPO.
     */
    public Concept greatestElement(Object[] subset) {
        if (subset != null && subset.length > 0) {
            Concept greatest = (Concept) subset[0];
            for (Object concept : subset) {
                if (compare(concept, greatest) == CPO.HIGHER) {
                    greatest = (Concept) concept;
                }
            }
            return greatest;
        }
        return null;
    }

    /** Compute the greatest lower bound (GLB) of two elements.
     *  The GLB of two elements is the greatest element in the CPO
     *  that is lower than or the same as both of the two elements.
     *  @param e1 An Object representing an element in this CPO.
     *  @param e2 An Object representing an element in this CPO.
     *  @return An Object representing the GLB of the two specified
     *   elements, or <code>null</code> if the GLB does not exist.
     *  @exception IllegalArgumentException If at least one of the
     *   specified Objects is not an element of this CPO.
     */
    public Concept greatestLowerBound(Object e1, Object e2) {
        if (!(e1 instanceof FiniteConcept) || !(e2 instanceof FiniteConcept)) {
            throw new IllegalArgumentException("ConceptGraph.greatestLowerBound:"
                    + " Arguments are not instances of FiniteConcept: "
                    + " arg1 = " + e1 + ", arg2 = " + e2);
        }
        return (Concept)_dag.greatestLowerBound(e1, e2);
    }

    /** Compute the greatest lower bound (GLB) of a subset.
     *  The GLB of a subset is the greatest element in the CPO that
     *  is lower than or the same as all the elements in the
     *  subset.
     *  @param subset An array of Objects representing the subset.
     *  @return Nothing.
     *  @exception IllegalArgumentException Always thrown.
     */
    public Concept greatestLowerBound(Object[] subset) {
        if (subset != null && subset.length > 0) {
            Concept glb = (Concept) subset[0];
            for (Object concept : subset) {
                glb = greatestLowerBound(glb, concept);
            }
            return glb;
        } else {
            return null;
        }
    }

    /** Return weather this concept graph is a lattice.
     *  Should be true for all existing concept graphs.
     *  @return True, if the concept graph is a lattice.
     */
    public boolean isLattice() {
        return _dag.isLattice();
    }

    /** Compute the least element of a subset.
     *  The least element of a subset is an element in the
     *  subset that is lower than all the other element in the
     *  subset.
     *  @param subset An array of Objects representing the subset.
     *  @return Nothing.
     *  @exception IllegalArgumentException Always thrown.
     */
    public Concept leastElement(Object[] subset) {
        if (subset != null && subset.length > 0) {
            Concept least = (Concept) subset[0];
            for (Object concept : subset) {
                if (compare(concept, least) == CPO.LOWER) {
                    least = (Concept) concept;
                }
            }
            return least;
        }
        return null;
    }

    /** Compute the least upper bound (LUB) of two elements.
     *  The LUB of two elements is the least element in the CPO
     *  that is greater than or the same as both of the two elements.
     *  @param e1 An Object representing an element in this CPO.
     *  @param e2 An Object representing an element in this CPO.
     *  @return An Object representing the LUB of the two specified
     *   elements, or <code>null</code> if the LUB does not exist.
     *  @exception IllegalArgumentException If at least one of the
     *   specified Objects is not an element of this CPO.
     */
    public Concept leastUpperBound(Object e1, Object e2) {
        if (!(e1 instanceof Concept) || !(e2 instanceof Concept)) {
            throw new IllegalArgumentException("ConceptGraph.leastUpperBound:"
                    + " Arguments are not instances of Concept: "
                    + " arg1 = " + e1 + ", arg2 = " + e2);
        }
        if ((e1 instanceof FiniteConcept) && (e2 instanceof FiniteConcept)) {
            Concept lub = (Concept)_dag.leastUpperBound(e1, e2);
            
            // If the least upper bound is a representative for a set of flat
            // infinite concepts but is not either of the two inputs, then the
            // actual lub must be at least one level above it.
            if (lub instanceof FlatTokenRepresentativeConcept && !lub.equals(e1) && !lub.equals(e2)) {
                lub = leastUpperBound(((FiniteConcept) lub).getStrictDominators().toArray());
            }
            return lub;
        } else if (e1 instanceof InfiniteConcept) {
            return ((InfiniteConcept)e1).leastUpperBound((Concept)e2);
        } else { // (e2 instanceof InfiniteConcept)
            return ((InfiniteConcept)e2).leastUpperBound((Concept)e1);
        }
    }

    /** Compute the least upper bound (LUB) of a subset.
     *  The LUB of a subset is the least element in the CPO that
     *  is greater than or the same as all the elements in the
     *  subset.
     *  @param subset An array of Objects representing the subset.
     *  @return Nothing.
     *  @exception IllegalArgumentException Always thrown.
     */
    public Concept leastUpperBound(Object[] subset) {
        if (subset != null && subset.length > 0) {
            Concept lub = (Concept) subset[0];
            for (Object concept : subset) {
                lub = leastUpperBound(lub, concept);
            }
            return lub;
        } else {
            return null;
        }
    }

    /** Return the greatest element in this concept graph.
     *  @return The greatest element in this concept graph.
     */
    public Concept top() {
        return (Concept)_dag.top();
    }

    /** Compute the up-set of an element in this CPO.
     *  The up-set of an element is the subset consisting of
     *  all the elements higher than or the same as the specified element.
     *  @param e An Object representing an element in this CPO.
     *  @return An array of Concepts of the up-set of the
     *   specified element.
     *  @exception IllegalArgumentException Always thrown.
     */
    public Concept[] upSet(Object e) {
        // FIXME: What happens if the upSet should contain some InfiniteConcepts
        // that are higher in the lattice?
        if (e instanceof FiniteConcept) {
            Object[] set = _dag.upSet(e);
            Concept[] upSet = new Concept[set.length];

            for (int i = 0; i < set.length; i++) {
                upSet[i] = (Concept) set[i];
            }

            return upSet;
        } else {
            // FIXME: Need to implement upSet for InfiniteConcepts.
            throw new IllegalArgumentException("upSet method not implemented" +
                    " for Concept subclass " + e.getClass().getName() + ".");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    /** A directed acyclic graph representing the connectivity of the
     *  concepts in this concept graph.
     */
    private DirectedAcyclicGraph _dag;
}
