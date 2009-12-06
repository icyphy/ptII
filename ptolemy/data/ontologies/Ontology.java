/**
 * A composite entity containing concepts and their ordering relations.
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

import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Ontology

/**
 * A specification of an ontology, which is a set of concepts and an
 * ordering relation on the set that is constrained to have the structure
 * of a mathematical lattice. The structure is represented by interconnections
 * between concepts contained by this ontology.
 * 
 * @see ConceptLattice
 * @see Concept
 * @author Edward A. Lee, Ben Lickly, Dai Bui, Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (blickly)
 * @Pt.AcceptedRating Red (blickly)
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
        _attachText("_iconDescription", _ICON);
    }
    
    /** Create a new Ontology with no container or name.
     *  @param workspace The workspace into which to put it.
     *  @throws IllegalActionException If the base class throws it.
     */
    public Ontology(Workspace workspace)
            throws IllegalActionException {
        super(workspace);
        _attachText("_iconDescription", _ICON);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    ////                   parameters                                       ////
    

    ////////////////////////////////////////////////////////////////////////////
    ////                   public methods                                   ////

    /** Return the lattice represented by this ontology.
     *  @return The property lattice.
     *  @throws IllegalActionException If the structure is not a lattice.
     */
    public ConceptLattice getLatticeGraph() throws IllegalActionException {
        if (workspace().getVersion() != _graphVersion) {
            // Construct the graph.
            _graph = new ConceptLattice();
            List<Concept> concepts = entityList(Concept.class);
            for (Concept concept : concepts) {
                _graph.addNodeWeight(concept);
            }
            for (Concept concept : concepts) {
                List<ComponentPort> remotePorts = concept.abovePort.deepConnectedPortList();
                for (ComponentPort remotePort : remotePorts) {
                    _graph.addEdge(concept, remotePort.getContainer());
                }
            }
            // Check that it's a lattice.
            if (!_graph.isLattice()) {
                throw new IllegalActionException(this, "Not a lattice.");
            }
        }
        return _graph;
    }
    
    /** Create a new relation with the specified name, add it to the
     *  relation list, and return it.
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     *  @param name The name of the new relation.
     *  @return The new relation.
     *  @exception IllegalActionException If name argument is null.
     *  @exception NameDuplicationException If name collides with a name
     *   already in the container.
     */
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            ComponentRelation rel = new ConceptRelation(this, name);
            return rel;
        } finally {
            _workspace.doneWriting();
        }
    }
        
    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    /** The cached graph. */
    private ConceptLattice _graph;
    
    /** The workspace version at which the cached graph was valid. */
    private long _graphVersion = -1L;
    
    /** The icon description used for rendering. */
    private static final String _ICON = "<svg>"
            + "<line x1=\"0\" y1=\"-30\" x2=\"18\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:3\"/>"
            + "<line x1=\"0\" y1=\"-30\" x2=\"-18\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:3\"/>"
            + "<line x1=\"0\" y1=\"-30\" x2=\"0\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:3\"/>"
            + "<line x1=\"0\" y1=\"30\" x2=\"18\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:3\"/>"
            + "<line x1=\"0\" y1=\"30\" x2=\"-18\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:3\"/>"
            + "<line x1=\"0\" y1=\"30\" x2=\"0\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:3\"/>"
            + "<circle cx=\"0\" cy=\"-30\" r=\"6\" style=\"fill:blue\"/>"
            + "<circle cx=\"0\" cy=\"30\" r=\"6\" style=\"fill:red\"/>"
            + "<circle cx=\"18\" cy=\"0\" r=\"6\" style=\"fill:white\"/>"
            + "<circle cx=\"-18\" cy=\"0\" r=\"6\" style=\"fill:white\"/>"
            + "<circle cx=\"0\" cy=\"0\" r=\"6\" style=\"fill:white\"/>"
            + "<line x1=\"12\" y1=\"42\" x2=\"12\" y2=\"36\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"9\" y1=\"42\" x2=\"15\" y2=\"42\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>" + "</svg>";

}
