/** A concept in a finite ontology.
 *
 * Copyright (c) 2007-2013 The Regents of the University of California. All
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

import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Flowable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// FiniteConcept

/** A concept in a finite ontology.
 *  The fact that this concept is part of a finite ontology allows
 *  us to do things like draw it in a GUI, do graph traversal algorithms
 *  with a (finite) adjacency matrix, and other similar useful conveniences.
 *
 *  @author Edward A. Lee, Ben Lickly, Dai Bui, Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 10.0
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
    public FiniteConcept(CompositeEntity ontology, String name)
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
    ////                         public methods                    ////

    /** Return the finite concepts that cover this one.
     *  If this concept is x, then we mean here the set of concepts y such
     *  that x &le; y and x &le; z &lt; y implies that z = x.
     *  @return A set of concepts that cover this one.
     */
    public Set<FiniteConcept> getCoverSetAbove() {
        return _getConnectedConcepts(abovePort);
    }

    /** Return the finite concepts that are covered by this one.
     *  If this concept is x, then we mean here the set of concepts y such
     *  that y &le; x and y &lt; z &le; x implies that z = x.
     *  @return A set of concepts that are covered by this one.
     */
    public Set<FiniteConcept> getCoverSetBelow() {
        return _getConnectedConcepts(belowPort);
    }

    /** Return the below port specified in the constructor.
     *  @return The below port specified in the constructor.
     */
    @Override
    public ComponentPort getIncomingPort() {
        return belowPort;
    }

    /** Return the above port specified in the constructor.
     *  @return The above port specified in the constructor.
     */
    @Override
    public ComponentPort getOutgoingPort() {
        return abovePort;
    }

    /** Return the string representation of this concept, its name.
     *
     * @return The name of this concept.
     */
    @Override
    public String toString() {
        return getName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the set of deeply connected concepts connected through
     *  the given port.
     *  @param direction The port in the direction of interest.
     *  @return The set of concepts connected through the given port.
     */
    private Set<FiniteConcept> _getConnectedConcepts(ComponentPort direction) {
        Set<FiniteConcept> cover = new HashSet<FiniteConcept>();
        @SuppressWarnings("unchecked")
        List<ComponentPort> ports = direction.deepConnectedPortList();
        for (ComponentPort port : ports) {
            cover.add((FiniteConcept) port.getContainer());
        }
        return cover;
    }
}
