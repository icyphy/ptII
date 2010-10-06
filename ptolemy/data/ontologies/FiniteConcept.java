/** A concept in a finite ontology.
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
import java.util.List;
import java.util.Set;

import ptolemy.graph.CPO;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.util.Flowable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// FiniteConcept

/** A concept in a finite ontology.
 *  The fact that the concept is part of a finite ontology allows
 *  us to do things like draw every concept in a GUI, do graph
 *  traversal algorithms with an adjacency matrix that is finite,
 *  and similar useful conveniences.
 * 
 *  @author Edward A. Lee, Ben Lickly, Dai Bui, Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class FiniteConcept extends Concept implements Flowable {

    /** Create a new concept with the specified name and the specified
     *  ontology.
     *  
     *  @param ontology The specified ontology where this concept resides.
     *  @param name The specified name for the concept.
     *  @exception NameDuplicationException If the ontology already contains a
     *   concept with the specified name.
     *  @exception IllegalActionException If the base class throws it.
     */
    public FiniteConcept(Ontology ontology, String name)
            throws NameDuplicationException, IllegalActionException {
        super(ontology, name);

        belowPort = new ComponentPort(this, "belowPort");
        abovePort = new ComponentPort(this, "abovePort");
    }

    ///////////////////////////////////////////////////////////////////
    ////                   parameters and ports                    ////

    /** The port linked to concepts above this one in the lattice. */
    public ComponentPort abovePort;

    /** The port linked to concepts below this one in the lattice. */
    public ComponentPort belowPort;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    
    /** Compare of this concept with the given concept.
     *  Returns an int value that corresponds to the ordering between
     *  the elements as given in the CPO interface.
     * 
     *  @param rhs The concept with which we are comparing.
     *  @return CPO.HIGHER if this concept is above the given concept,
     *          CPO.LOWER if this concept is below the given concept,
     *          CPO.SAME if both concepts are the same,
     *      and CPO.INCOMPARABLE if concepts are incomparable.
     *  @exception IllegalActionException If the specified concept
     *          does not have the same ontology as this one.
     */
    public int compare(Concept concept) throws IllegalActionException {
        if (concept == null || !(concept.getOntology().equals(getOntology()))) {
            throw new IllegalActionException(this,
                    "Attempt to compare elements from two distinct ontologies");
        }
        return getOntology().getGraph().compare(this, concept);
    }

    /** Return the ontology that contains this concept.
     *
     *  @return The containing ontology.
     */
    public Ontology getOntology() {
        /* if (!(getContainer() instanceof Ontology)) {
            throw new IllegalActionException(this,
                    "Concept is not contained by an Ontology.");
        } */
        return (Ontology)getContainer();
    }

    /** Return the outgoing port.
     *  @return The outgoing port.
     */
    public ComponentPort getIncomingPort() {
        return belowPort;
    }

    /** Return the outgoing port.
     *  @return The outgoing port.
     */
    public ComponentPort getOutgoingPort() {
        return abovePort;
    }

    /** Return the finite concepts that are directly above this one.
     *  @return A set of concepts that strictly dominate this one.
     */
    public Set<FiniteConcept> getStrictDominators() {
        Set<FiniteConcept> dominators = new HashSet<FiniteConcept>();
        List<ComponentPort> ports = abovePort.deepConnectedPortList();
        for (ComponentPort port : ports) {
            dominators.add((FiniteConcept) port.getContainer());
        }
        return dominators;
    }

    /** Return the finite concepts that are directly below this one.
     *  @return A set of concepts that are strictly dominated by this one.
     */
    public Set<FiniteConcept> getStrictPostdominators() {
        Set<FiniteConcept> postdominators = new HashSet<FiniteConcept>();
        List<ComponentPort> ports = belowPort.deepConnectedPortList();
        for (ComponentPort port : ports) {
            postdominators.add((FiniteConcept) port.getContainer());
        }
        return postdominators;
    }

    /**
     * Return the string that represents this concept, its name.
     * 
     * @return The string name that represents this concept.
     */
    public String toString() {
        return _name;
    }
}
