/** A data structure that provides counterexample information when a graph is
    tested to see if it is a lattice.

 Copyright (c) 2011-2014 The Regents of the University of California.
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
package ptolemy.graph;

///////////////////////////////////////////////////////////////////
//// NonLatticeCounterExample

import java.util.ArrayList;
import java.util.List;

import ptolemy.graph.CPO.BoundType;

/**
A data structure that provides counterexample information when a graph is
tested to see if it is a lattice. If a graph is not a lattice, it could be
because a set of nodes have no least upper or greatest lower bound, or because
the graph has a cycle.

@author Charles Shelton
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (cshelton)
@Pt.AcceptedRating Red (cshelton)
 */
public class NonLatticeCounterExample {

    /** Construct a NonLatticeCounterExample object with the given example
     *  type.
     *  @param exampleType The given example type for this counterexample.
     */
    public NonLatticeCounterExample(ExampleType exampleType) {
        _exampleType = exampleType;
        _nodeList = new ArrayList();
    }

    /** Construct a NonLatticeCounterExample object with the given example
     *  type and list of nodes in the graph.
     *  @param exampleType The given example type for this counterexample.
     *  @param nodeList The list of node weights for this counterexample.
     */
    public NonLatticeCounterExample(ExampleType exampleType, List nodeList) {
        _exampleType = exampleType;
        _nodeList = new ArrayList(nodeList);
    }

    /** Construct a NonLatticeCounterExample object for a graph with a cycle.
     *  @param node The weight of one of the nodes in the graph that is on the
     *   cycle path.
     */
    public NonLatticeCounterExample(Object node) {
        _exampleType = GraphExampleType.GRAPHCYCLE;
        _nodeList = new ArrayList();
        _nodeList.add(node);
    }

    /** Construct a NonLatticeCounterExample object for a pair of nodes that
     *  have either no least upper or greatest lower bound.
     *  @param bound The bound type for this counter example.
     *  @param node1 The first node weight.
     *  @param node2 The second node weight.
     */
    public NonLatticeCounterExample(BoundType bound, Object node1, Object node2) {
        _exampleType = getExampleTypeFromBoundType(bound);
        _nodeList = new ArrayList();
        _nodeList.add(node1);
        _nodeList.add(node2);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the example type for this NonLatticeCounterExample.
     *  @return Either LEASTUPPER, GREATESTLOWER, or GRAPHCYCLE.
     */
    public ExampleType getExampleType() {
        return _exampleType;
    }

    /** Return the list of node weights in the graph associated with this
     *  counter example.
     *  @return The list of node weights.
     */
    public List getNodeList() {
        return _nodeList;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the correct example type given the CPO bound type (LEASTUPPER or
     *  GREATESTLOWER).
     *  @param boundType The given CPO bound type.
     *  @return The example type for the given bound type.
     */
    private ExampleType getExampleTypeFromBoundType(BoundType boundType) {
        switch (boundType) {
        case LEASTUPPER:
            return GraphExampleType.LEASTUPPER;
        case GREATESTLOWER:
            return GraphExampleType.GREATESTLOWER;
        default:
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The example type for this NonLatticeCounterExample. */
    private ExampleType _exampleType;

    /** The list of nodes associated with this NonLatticeCounterExample. */
    private List _nodeList;

    ///////////////////////////////////////////////////////////////////
    ////                         public inner classes              ////

    /** Marker interface for the counter example type. This allows
     *  us to create other enumerations of counter example types in
     *  subclasses. In particular, this is needed for the ontologies
     *  package classes ptolemy.data.ontologies.lattice.ProductLatticeOntology
     *  ProductLatticeOntology and
     *  ptolemy.data.ontologies.lattice.ProductLatticeCPO ProductLatticeCPO.
     *  When a product lattice is not a lattice, the reason is because one of
     *  its component lattices is not a lattice. This is only relevant for
     *  product lattice ontologies.
     */
    public interface ExampleType {
    }

    /** An enumeration type to represent the types of counterexamples
     *  that can be found when checking to see if a graph is a lattice.
     */
    public static enum GraphExampleType implements ExampleType {
        /** Represents a counterexample where some nodes have no greatest lower bound. */
        GREATESTLOWER,

        /** Represents a counterexample where some nodes have no least upper bound. */
        LEASTUPPER,

        /** Represents a counterexample where the graph has a cycle. */
        GRAPHCYCLE
    }
}
