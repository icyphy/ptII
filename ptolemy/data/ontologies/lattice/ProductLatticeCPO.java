/* A complete partial order for product lattice-based ontologies.
 * 
 * Copyright (c) 2007-2010 The Regents of the University of California. All
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

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.graph.CPO;

///////////////////////////////////////////////////////////////////
//// ProductLatticeCPO

/** A complete partial order for product lattice-based ontologies.
 *  Given a product lattice defined by a list of {@link ProductLatticeConcept}s,
 *  this class provides the implementation for all complete partial order
 *  operations on the product lattice.
 * 
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ProductLatticeCPO implements CPO {
    
    /** Create a new ProductLatticeCPO from the given list of
     *  ProductLatticeConcepts.
     *  @param conceptList The list of {@link ProductLatticeConcept}s that define
     *   the product lattice.
     */
    public ProductLatticeCPO(List<ProductLatticeConcept> conceptList) {
        _conceptList = conceptList;        
        List<Concept> tuple = _conceptList.get(0).getConceptTuple();
        
        _ontologyList = new ArrayList<Ontology>(tuple.size());
        for (Concept concept : tuple) {
            _ontologyList.add(concept.getOntology());
        }
        
        _findBottom();
        _findTop();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the bottom element of this CPO.
     *  The bottom element is the element in the CPO that is lower than
     *  all the other elements.
     *  @return An Object representing the bottom element, or
     *   <code>null</code> if the bottom does not exist.
     */
    public Object bottom() {
        return _bottomConcept;
    }

    /** Compare two concepts in the product lattice ontology. The arguments must be
     *  instances of {@link ProductLatticeConcept}, otherwise an exception will be thrown.
     *  This method returns one of ptolemy.graph.CPO.LOWER, ptolemy.graph.CPO.SAME,
     *  ptolemy.graph.CPO.HIGHER, ptolemy.graph.CPO.INCOMPARABLE, indicating the
     *  first argument is lower than, equal to, higher than, or incomparable with
     *  the second argument in the property hierarchy, respectively.
     *  @param e1 An instance of {@link ProductLatticeConcept}.
     *  @param e2 An instance of {@link ProductLatticeConcept}.
     *  @return One of CPO.LOWER, CPO.SAME, CPO.HIGHER, CPO.INCOMPARABLE.
     *  @exception IllegalArgumentException If one or both arguments are not
     *   instances of {@link ProductLatticeConcept}, the arguments are not from
     *   the same ontology, or either argument has an empty or null concept tuple list.
     */
    public int compare(Object e1, Object e2) {
        if (!(e1 instanceof ProductLatticeConcept) || !(e2 instanceof ProductLatticeConcept)) {
            throw new IllegalArgumentException("ProductLatticeCPO.compare: "
                    + "Arguments are not instances of ProductLatticeConcept: "
                    + " arg1 = " + e1 + ", arg2 = " + e2);
        }
        
        if (!(((ProductLatticeConcept) e1).getOntology().equals(((ProductLatticeConcept) e2).getOntology()))) {
            throw new IllegalArgumentException("Attempt to compare elements from two distinct ontologies: "
                    + " arg1 = " + e1 + ", arg2 = " + e2);
        }       
        
        List<Concept> leftArgTuple = ((ProductLatticeConcept) e1).getConceptTuple();
        List<Concept> rightArgTuple = ((ProductLatticeConcept) e2).getConceptTuple();
        
        if (leftArgTuple == null || leftArgTuple.isEmpty()) {
           throw new IllegalArgumentException("Attempt to compare ProductLatticeConcept " +
                        "elements where one does not have a valid " +
                        "concept tuple: arg1 = " + e1);
        }
        
        if (rightArgTuple == null || rightArgTuple.isEmpty()) {
            throw new IllegalArgumentException("Attempt to compare ProductLatticeConcept " +
                         "elements where one does not have a valid " +
                         "concept tuple: arg2 = " + e2);
         }
        
        if (leftArgTuple.size() != rightArgTuple.size()) {
            throw new IllegalArgumentException("Attemp to compare " +
                        "ProductLatticeConcept elements that do not have the same size " +
                        "concept tuple arrays even though they are in the same " +
                        "Ontology. This is an error."
                        + " arg1 = " + e1 + ", arg2 = " + e2);
        }
        
        int tupleSize = leftArgTuple.size();
        int numSame = 0;
        int numHigher = 0;
        int numLower = 0;

        // For each pair of concepts in the tuple
        // track which ones are higher, same, or lower.
        for (int i = 0; i < tupleSize; i++) {
            Ontology tupleOntology = leftArgTuple.get(i).getOntology();
            int comparison = tupleOntology.getGraph().compare(leftArgTuple.get(i), rightArgTuple.get(i));
            
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
    
    /** Compute the down-set of an element in this CPO.
     *  The down-set of an element is the subset consisting of
     *  all the elements lower than or the same as the specified element.
     *  @param e An Object representing an element in this CPO.
     *  @return An array of Objects representing the elements in the
     *   down-set of the specified element.
     *  @exception IllegalArgumentException If the specified Object is not
     *   an element in this CPO, or the resulting set is infinite.
     */
    public Object[] downSet(Object e) {
        throw new IllegalArgumentException("Method not implemented!");
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
    public Object greatestElement(Object[] subset) {
        throw new IllegalArgumentException("Method not implemented!");
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
    public Object greatestLowerBound(Object e1, Object e2) {
        ProductLatticeConcept glb = _bottomConcept;
        
        for (ProductLatticeConcept concept : _conceptList) {
            int e1Compare = compare(concept, e1);
            int e2Compare = compare(concept, e2);
            
            if (compare(concept, glb) == CPO.HIGHER &&
                    (e1Compare == CPO.LOWER || e1Compare == CPO.SAME) &&
                    (e2Compare == CPO.LOWER || e2Compare == CPO.SAME)) {
                glb = concept;
            }
        }
        
        return glb;
    }

    /** Compute the greatest lower bound (GLB) of a subset.
     *  The GLB of a subset is the greatest element in the CPO that
     *  is lower than or the same as all the elements in the
     *  subset.
     *  @param subset An array of Objects representing the subset.
     *  @return Nothing.
     *  @exception IllegalArgumentException Always thrown.
     */
    public Object greatestLowerBound(Object[] subset) {
        throw new IllegalArgumentException("Method not implemented!");
    }

    /** Test if this CPO is a lattice. A Product Lattice CPO is a lattice if
     *  all of its component ontologies are lattices.
     *  @return True if this CPO is a lattice;
     *   <code>false</code> otherwise.
     */
    public boolean isLattice() {
        for(Ontology ontology : _ontologyList) {
            if (!ontology.isLattice()) {
                return false;
            }
        }
        
        return true;
    }

    /** Compute the least element of a subset.
     *  The least element of a subset is an element in the
     *  subset that is lower than all the other element in the
     *  subset.
     *  @param subset An array of Objects representing the subset.
     *  @return Nothing.
     *  @exception IllegalArgumentException Always thrown.
     */
    public Object leastElement(Object[] subset) {
        throw new IllegalArgumentException("Method not implemented!");
    }

    /** Compute the least upper bound (LUB) of two elements.
     *  The LUB of two elements is the least element in the CPO
     *  that is greater than or the same as both of the two elements.
     *  @param e1 An Object representing an element in this CPO.
     *  @param e2 An Object representing an element in this CPO.
     *  @return Nothing.
     *  @exception IllegalArgumentException Always thrown.
     */
    public Object leastUpperBound(Object e1, Object e2) {
        ProductLatticeConcept lub = _topConcept;
        
        for (ProductLatticeConcept concept : _conceptList) {
            int e1Compare = compare(concept, e1);
            int e2Compare = compare(concept, e2);
            
            if (compare(concept, lub) == CPO.LOWER &&
                    (e1Compare == CPO.HIGHER || e1Compare == CPO.SAME) &&
                    (e2Compare == CPO.HIGHER || e2Compare == CPO.SAME)) {
                lub = concept;
            }
        }
        
        return lub;
    }

    /** Compute the least upper bound (LUB) of a subset.
     *  The LUB of a subset is the least element in the CPO that
     *  is greater than or the same as all the elements in the
     *  subset.
     *  @param subset An array of Objects representing the subset.
     *  @return Nothing.
     *  @exception IllegalArgumentException Always thrown.
     */
    public Object leastUpperBound(Object[] subset) {
        throw new IllegalArgumentException("Method not implemented!");
    }

    /** Return the top element of this CPO.
     *  The top element is the element in the CPO that is higher than
     *  all the other elements.
     *  @return An Object representing the top element, or
     *   <code>null</code> if the top does not exist.
     */
    public Object top() {
        return _topConcept;
    }

    /** Compute the up-set of an element in this CPO.
     *  The up-set of an element is the subset consisting of
     *  all the elements higher than or the same as the specified element.
     *  @param e An Object representing an element in this CPO.
     *  @return Nothing.
     *  @exception IllegalArgumentException Always thrown.
     */
    public Object[] upSet(Object e) {
        throw new IllegalArgumentException("Method not implemented!");
    }    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Set the bottom concept of the product lattice.
     */
    private void _findBottom() {
        _bottomConcept = _conceptList.get(0);
        
        for (ProductLatticeConcept concept : _conceptList) {
            if (compare(concept, _bottomConcept) == CPO.LOWER) {
                _bottomConcept = concept;
            }
        }
    }
    
    /** Set the top concept of the product lattice.
     */
    private void _findTop() {
        _topConcept = _conceptList.get(0);
        
        for (ProductLatticeConcept concept : _conceptList) {
            if (compare(concept, _topConcept) == CPO.HIGHER) {
                _topConcept = concept;
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The bottom concept of the product lattice. */
    private ProductLatticeConcept _bottomConcept;

    /** The list of {@link ProductLatticeConcept}s that define the product lattice. */
    private List<ProductLatticeConcept> _conceptList;
    
    /** The list of Ontologies for each element in the concept tuple of
     *  each ProductLatticeConcept in the product lattice.
     */
    private List<Ontology> _ontologyList;
    
    /** The bottom concept of the product lattice. */
    private ProductLatticeConcept _topConcept;
}
