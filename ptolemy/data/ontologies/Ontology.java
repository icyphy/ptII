/**
 * A composite entity containing lattice properties and their ordering relations.
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
 * 
 */
package ptolemy.data.ontologies;

import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Ontology

/**
 * A specification of an ontology, which is a set of concepts and an
 * ordering relation on the set that is constrained to have the structure
 * of a mathematical lattice. Structure is represented by interconnections
 * between concepts contained by this ontology.
 * 
 * @see PropertyLattice
 * @see Concept
 * @author Thomas Mandl, Man-Kit Leung, Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class Ontology extends CompositeEntity {

    /** Create a new Ontology with the specified container and the specified
     *  name.
     *  @param container The container.
     *  @param name The name for the ontology.
     *  @throws NameDuplicationException If the container already contains an
     *   ontology with the specified name.
     *  @throws IllegalActionException If the base class throws it.
     */
    public Ontology(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    ////                   parameters                                       ////
    

    ////////////////////////////////////////////////////////////////////////////
    ////                   public methods                                   ////

    /** Return the lattice represented by this ontology.
     *  @return The property lattice.
     *  @throws IllegalActionException If the structure is not a lattice.
     */
    public PropertyLattice getLatticeGraph() throws IllegalActionException {
        if (workspace().getVersion() != _graphVersion) {
            // Construct the graph.
            _graph = new PropertyLattice();
            List<Concept> concepts = entityList(Concept.class);
            for (Concept concept : concepts) {
                _graph.addNodeWeight(concept);
            }
            for (Concept concept : concepts) {
                for (int i = 0; i < concept.abovePort.getWidth(); i++) {
                    List<IOPort> remotePorts = concept.abovePort.sinkPortList();
                    for (IOPort remotePort : remotePorts) {
                        _graph.addEdge(concept, remotePort.getContainer());
                    }
                }
            }
            // Check that it's a lattice.
            if (!_graph.isLattice()) {
                throw new IllegalActionException(this, "Not a lattice.");
            }
        }
        return _graph;
    }
        
    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    /** The cached graph. */
    private PropertyLattice _graph;
    
    /** The workspace version at which the cached graph was valid. */
    private long _graphVersion = -1L;
}
