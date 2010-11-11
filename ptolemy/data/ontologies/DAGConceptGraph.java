/* A data structure representing DAGs of concepts in an ontology.
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

import ptolemy.graph.CPO;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// DAGConceptGraph

/** A data structure representing relationships of ontologies whose structure
 *  can be represented as a directed acyclic graph of concepts.
 *  This corresponds directly to the subset of ontologies that users can
 *  costruct in the Ontology Editor.
 *
 *  @author Thomas Mandl, Man-Kit Leung, Edward A. Lee, Ben Lickly, Dai Bui, Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (mankit)
 *  @Pt.AcceptedRating Red (mankit)
 *  @see ptolemy.data.ontologies.ConceptGraph
 */
public class DAGConceptGraph extends ConceptGraph {

    /** Create an empty concept graph with no concepts in it.
     */
    public DAGConceptGraph() {
        _dag = new DirectedAcyclicGraph();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a concept to this concept graph.
     *  @param concept The concept.
     *  @exception IllegalArgumentException If the concept we are trying to
     *   add is already contained in this concept graph.
     */
    public void addConcept(FiniteConcept concept) {
        if (!_dag.containsNodeWeight(concept)) {
            _dag.addNodeWeight(concept);
        } else {
            throw new IllegalArgumentException("Cannot add concept " + concept
                    + " as it is already contained in this concept graph.");
        }
    }

    /** Add a relation between two Concepts as an edge to the graph.
     *
     *  @param concept1 The source concept
     *  @param concept2 The sink concept
     *  @param conceptRelation The ConceptRelation between the two concepts
     *   concept1 and conceptt2.
     */
    public void addRelation(FiniteConcept concept1, FiniteConcept concept2,
            ConceptRelation conceptRelation) {
        _dag.addEdge(concept1, concept2, conceptRelation);
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

    /** Compute the down-set of an element in this concept graph.
     *  The down-set of an element is the subset consisting of
     *  all the elements lower than or the same as the specified element.
     *  @param e An Object representing an element in this concept graph.
     *  @return An array of Concepts of the down-set of the
     *   specified element.
     *  @exception IllegalArgumentException If the specified Object is not
     *   an element in this concept graph, or the resulting set is infinite.
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

    /** Compute the greatest lower bound (GLB) of two elements.
     *  The GLB of two elements is the greatest element in the concept graph
     *  that is lower than or the same as both of the two elements.
     *  @param e1 An Object representing an element in this concept graph.
     *  @param e2 An Object representing an element in this concept graph.
     *  @return A Concept representing the GLB of the two specified
     *   elements, or <code>null</code> if the GLB does not exist.
     *  @exception IllegalArgumentException If at least one of the
     *   specified Objects is not an element of this concept graph.
     */
    public Concept greatestLowerBound(Object e1, Object e2) {
        if (!(e1 instanceof FiniteConcept) || !(e2 instanceof FiniteConcept)) {
            throw new IllegalArgumentException("ConceptGraph.greatestLowerBound:"
                    + " Arguments are not instances of FiniteConcept: "
                    + " arg1 = " + e1 + ", arg2 = " + e2);
        }
        return (Concept)_dag.greatestLowerBound(e1, e2);
    }

    /** Return weather this concept graph is a lattice.
     *  Should be true for all existing concept graphs.
     *  @return True, if the concept graph is a lattice.
     */
    public boolean isLattice() {
        return _dag.isLattice();
    }

    /** Compute the least upper bound (LUB) of two elements.
     *  The LUB of two elements is the least element in the concept graph
     *  that is greater than or the same as both of the two elements.
     *  @param e1 An Object representing an element in this concept graph.
     *  @param e2 An Object representing an element in this concept graph.
     *  @return A Concept representing the LUB of the two specified
     *   elements, or <code>null</code> if the LUB does not exist.
     *  @exception IllegalArgumentException If at least one of the
     *   specified Objects is not an element of this concept graph.
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
                lub = leastUpperBound(((FiniteConcept) lub).getCoverSetAbove().toArray());
            }
            return lub;
        } else if (e1 instanceof InfiniteConcept) {
            return ((InfiniteConcept)e1).leastUpperBound((Concept)e2);
        } else { // (e2 instanceof InfiniteConcept)
            return ((InfiniteConcept)e2).leastUpperBound((Concept)e1);
        }
    }

    /** Return the greatest element in this concept graph.
     *  @return The greatest element in this concept graph.
     */
    public Concept top() {
        return (Concept)_dag.top();
    }

    /** Compute the up-set of an element in this concept graph.
     *  The up-set of an element is the subset consisting of
     *  all the elements higher than or the same as the specified element.
     *  @param e An Object representing an element in this concept graph.
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
