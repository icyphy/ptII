/*
 * The base class of a property lattice.
 * 
 * Copyright (c) 2007-2009 The Regents of the University of California. All
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

import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.graph.Node;

//////////////////////////////////////////////////////////////////////////////
//// ConceptLattice

/** A data structure defining an ontology. An ontology is partially
 *  ordered set of concepts.
 * 
 * @author Thomas Mandl, Man-Kit Leung, Edward A. Lee, Ben Lickly, Dai Bui, Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 * @see ptolemy.graph.CPO
 */
public class ConceptGraph extends DirectedAcyclicGraph {
    
    /** Construct a lattice associated with the specified ontology.
     *  @param ontology The associated ontology.
     */
    public ConceptGraph(Ontology ontology) {
        _ontology = ontology;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a concept to this lattice.
     *  @param weight The concept.
     *  @return The constructed graph node in the lattice.
     *  @throws IllegalArgumentException If the argument is not
     *   an instance of {@link Concept}.
     */
    public Node addNodeWeight(Object weight) {
        if (!(weight instanceof Concept)) {
            throw new IllegalArgumentException("Attempt to add a non-Concept to an Ontology.");
        }
        return super.addNodeWeight(weight);
    }

    /**
     * Add structured properties. The parameter flag is a bit-wise OR union of
     * the structured properties desired to add. The class provides a set of
     * symbolic constants for these structured property types. For example,
     * invoke addStructuredProperties(RECORD) to add the RecordProperty. This
     * method should be called after all base elements have been added. The user
     * is responsible for ensuring a lattice structure before calling this
     * method. The lattice structure should be preserved after calling this
     * method, so there is no need to do another check.
     * @param structuredPropertiesToAdd The bit-wise OR union of the structured
     * properties desired to add.
     * @throws IllegalActionException
     */
    /* FIXME: To support structured concepts:
    public void addStructuredProperties(int structuredPropertiesToAdd)
            throws IllegalActionException {

        if ((structuredPropertiesToAdd & RECORD) != 0) {
            Property record = new RecordProperty(this, new String[0],
                    new LatticeProperty[0]).getRepresentative();

            if (!isLattice()) {
                throw new IllegalActionException(
                        "This ontology needs to be a lattice "
                                + "before adding structured types.");
            }

            Object bottom = bottom();
            Object top = top();

            addNodeWeight(record);
            addEdge(bottom, record);
            addEdge(record, top);
        }

        if ((structuredPropertiesToAdd & ARRAY) != 0) {
            // FIXME: add Array structure.
        }
    }
    */

    /** Compare two concepts in the ontology. The arguments must be
     *  instances of {@link Concept}, otherwise an exception will be thrown.
     *  This method returns one of ptolemy.graph.CPO.LOWER, ptolemy.graph.CPO.SAME,
     *  ptolemy.graph.CPO.HIGHER, ptolemy.graph.CPO.INCOMPARABLE, indicating the
     *  first argument is lower than, equal to, higher than, or incomparable with
     *  the second argument in the property hierarchy, respectively.
     *  @param concept1 An instance of {@link Concept}.
     *  @param concept2 an instance of {@link Concept}.
     *  @return One of CPO.LOWER, CPO.SAME, CPO.HIGHER, CPO.INCOMPARABLE.
     *  @exception IllegalArgumentException If one or both arguments are not
     *   instances of {@link Concept}.
     */
    public int compare(Object concept1, Object concept2) {
        if (!(concept1 instanceof Concept) || !(concept2 instanceof Concept)) {
            throw new IllegalArgumentException("ConceptGraph.compare: "
                    + "Arguments are not instances of Concept: "
                    + " concept1 = " + concept1 + ", concept2 = " + concept2);
        }

        Concept t1Rep = _toRepresentative((Concept) concept1);
        Concept t2Rep = _toRepresentative((Concept) concept2);

        /* FIXME: Support structured concepts using something like this:
        if (t1Rep.equals(t2Rep) && t1Rep instanceof StructuredProperty) {
            return ((StructuredProperty) t1)
                    ._compare((StructuredProperty) t2);
        }
        */

        return super.compare(t1Rep, t2Rep);
    }

    /** Return the ontology associated with this graph.
     *  @return The ontology specified in the constructor.
     */
    public Ontology getOntology() {
        return _ontology;
    }

    ///////////////////////////////////////////////////////////////////
    ////                          public fields                    ////

    /** Public symbolic constant value for the RecordProperty. */
    // FIXME: For structured concepts.
    // public final int RECORD = 0x1;

    /** Public symbolic constant value for the ArrayProperty. */
    // FIXME: For structured concepts:
    // public final int ARRAY = 0x2;

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    /** If the argument is a structured concept, return its representative;
     *  otherwise, return the argument.
     *  @return The representive for the specified concept.
     */
    private Concept _toRepresentative(Concept p) {
        /* FIXME: Support structured concepts using something like this:
        if (p instanceof StructuredProperty) {
            return ((StructuredProperty) p).getRepresentative();
        } else {
            return p;
        }
        */
        return p;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The associated ontology. */
    private Ontology _ontology;
}
