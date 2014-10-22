/** A data structure that provides counterexample information when a product
 *  lattice ontology CPO is tested to see if it is a lattice.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice;

///////////////////////////////////////////////////////////////////
//// NonProductLatticeCounterExample

import ptolemy.data.ontologies.Ontology;
import ptolemy.graph.NonLatticeCounterExample;

/**
A data structure that provides counterexample information when a product lattice
ontology CPO is tested to see if it is a lattice. If it is not a lattice, it is
because at least one of its component ontologies is not a lattice.

@author Charles Shelton
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (cshelton)
@Pt.AcceptedRating Red (cshelton)
 */
public class NonProductLatticeCounterExample extends NonLatticeCounterExample {

    /** Construct a NonProductLatticeCounterExample object that indicates that
     *  the specified sub ontology is not a lattice.
     *  @param subOntology The sub ontology of the product lattice ontology
     *   that is the reason that the product lattice ontology is not a lattice.
     */
    public NonProductLatticeCounterExample(Ontology subOntology) {
        super(ProductExampleType.SUBONTOLOGY);
        _subOntology = subOntology;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the sub ontology that is the reason the product lattice ontology
     *  is not a lattice.
     *  @return The sub ontology that is not a valid lattice.
     */
    public Ontology getSubOntology() {
        return _subOntology;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The sub ontology associated with this NonLatticeCounterExample
     *  that is not a valid lattice.
     */
    private Ontology _subOntology;

    ///////////////////////////////////////////////////////////////////
    ////                         public inner classes              ////

    /** An enumeration type to represent the types of counterexamples
     *  that can be found when checking to see if product lattice ontology is a
     *  lattice.
     */
    public static enum ProductExampleType implements ExampleType {
        /** Represents a counterexample where one of the sub ontologies of the
         *  product lattice ontology is not a lattice. */
        SUBONTOLOGY
    }
}
