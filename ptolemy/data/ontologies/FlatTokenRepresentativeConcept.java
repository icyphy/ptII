/* A concept in a finite ontology that represents a flat set of infinite
 * concepts that map to a set of arbitrary Ptolemy tokens.
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

import java.util.HashSet;
import java.util.Set;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// FlatTokenRepresentativeConcept

/** A concept in a finite ontology that represents a flat set of infinite
 *  concepts that map to a set of arbitrary Ptolemy tokens.
 * 
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class FlatTokenRepresentativeConcept extends FiniteConcept {

    /** Create a new concept with the specified name and the specified
     *  ontology.
     *  
     *  @param ontology The specified ontology where this concept resides.
     *  @param name The specified name for the concept.
     *  @exception NameDuplicationException If the ontology already contains a
     *   concept with the specified name.
     *  @exception IllegalActionException If the base class throws it.
     */
    public FlatTokenRepresentativeConcept(Ontology ontology, String name)
            throws NameDuplicationException, IllegalActionException {
        super(ontology, name);
        
        _instantiatedInfiniteConcepts = new HashSet<FlatTokenInfiniteConcept>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** Add an instantiated infinite concept that is represented by this
     *  concept to its set of instantiated infinite concepts.
     *  @param concept The FlatTokenInfiniteConcept to be added.
     */
    public void addInfiniteConcept(FlatTokenInfiniteConcept concept) {
        // Only add the concept if its representative matches.
        if (concept.getRepresentative().equals(this)) {
            _instantiatedInfiniteConcepts.add(concept);
        }
    }
    
    /** Return the set of instantiated infinite concepts that are represented
     *  by this concept.
     *  @return The set of instantiated infinite concepts that are represented
     *   by this concept.
     */
    public Set<FlatTokenInfiniteConcept> getInstantiatedInfiniteConcepts() {
        return _instantiatedInfiniteConcepts;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////
    
    /** Indicates whether or not the interval is closed on its left endpoint. */
    private Set<FlatTokenInfiniteConcept> _instantiatedInfiniteConcepts;
}
