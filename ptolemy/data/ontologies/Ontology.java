/**
 * A composite entity containing concepts and their ordering relations.
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
 * 
 */
package ptolemy.data.ontologies;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ptolemy.graph.GraphStateException;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Ontology

/**
 * A specification of an ontology, which is a set of concepts and a
 * partial ordering relation.
 * The structure is represented by interconnections
 * between concepts contained by this ontology.
 * 
 * @see ConceptGraph
 * @see FiniteConcept
 * @author Edward A. Lee, Ben Lickly, Dai Bui, Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (blickly)
 * @Pt.AcceptedRating Red (blickly)
 */
public class Ontology extends CompositeEntity {

    /** Create a new Ontology with the specified container and the specified
     *  name.
     *  @param container The container.
     *  @param name The name for the ontology.
     *  @exception NameDuplicationException If the container already contains an
     *   ontology with the specified name.
     *  @exception IllegalActionException If the base class throws it.
     */
    public Ontology(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _attachText("_iconDescription", _ICON);
    }

    /** Create a new Ontology with no container or name.
     *  @param workspace The workspace into which to put it.
     *  @exception IllegalActionException If the base class throws it.
     */
    public Ontology(Workspace workspace) throws IllegalActionException {
        super(workspace);
        _attachText("_iconDescription", _ICON);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the graph represented by this ontology.
     *  Graph is weighted by Concepts on the nodes and ConceptRelations on
     *  the edges.
     *  @return The concept graph.
     */
    public ConceptGraph getGraph() {
        if (workspace().getVersion() != _graphVersion) {
            // Construct the graph.
            _graph = new ConceptGraph();
            List<FiniteConcept> concepts = entityList(FiniteConcept.class);
            for (Concept concept : concepts) {
                _graph.addNodeWeight(concept);
            }
            for (FiniteConcept concept : concepts) {
                List<ConceptRelation> relationLinks = concept.abovePort
                        .linkedRelationList();
                for (ConceptRelation link : relationLinks) {
                    List<ComponentPort> remotePorts = link
                            .linkedPortList(concept.abovePort);
                    assert (remotePorts.size() == 1) : "ConceptRelations can only connect two concepts";
                    for (ComponentPort remotePort : remotePorts) {
                        _graph
                                .addEdge(concept, remotePort.getContainer(),
                                        link);
                    }
                }
            }

            // Set the graph version after creating the new graph
            _graphVersion = workspace().getVersion();
        }
        return _graph;
    }
    
    /** Return a set of all concepts which are unacceptable solutions in all situations.
     *  Here these concepts are undesirable for any user of this ontology, for example,
     *  "Top" may indicate a conflict for all models using this ontology.
     *  Ontologies may not contain duplicate concepts, so the collection of
     *  unacceptable concepts is always a set 
     * @return  The set of all unacceptable concepts in this ontology.  
     */
    public Set getUnacceptableConcepts() {
        
        HashSet unacceptableConcepts = new HashSet();
        
        List<FiniteConcept> concepts = entityList(FiniteConcept.class);
        
        for (Concept concept : concepts) {
            if (!concept.isValueAcceptable()) {
                unacceptableConcepts.add(concept);
            }
        }
        
        return unacceptableConcepts;
        
    }

    /** Return true if the ontology graph is a lattice.
     *  @return True if the graph is a lattice.
     */
    public boolean isLattice() {
        _graph = getGraph();

        // 01/04/2010 Charles Shelton - Debug information for isLattice() function:
        // - Catch the exception from the directed acyclic graph _validate() method
        //      that checks for a cycle in the graph.
        // - Added additional debug messages to provide both positive and negative
        //      feedback about the lattice.

        try {
            if (_graph.top() == null) {
                _debug("This is not a lattice. Cannot find a unique top element.");
                return false;
            } else {
                Concept top = (Concept) _graph.top();
                _debug("Top is: " + top.toString());
            }
        } catch (GraphStateException e) {
            _debug("This is not a lattice. Proposed graph has a cycle and is not a lattice.");
            return false;
        }

        if (_graph.bottom() == null) {
            _debug("This is not a lattice. Cannot find a unique bottom element.");
            return false;
        } else {
            Concept bottom = (Concept) _graph.bottom();
            _debug("Bottom is: " + bottom.toString());
        }

        List<FiniteConcept> ontologyConcepts = entityList(FiniteConcept.class);

        // This is the same check done in ptolemy.graph.DirectedAcyclicGraph.
        for (int i = 0; i < ontologyConcepts.size() - 1; i++) {
            for (int j = i + 1; j < ontologyConcepts.size(); j++) {
                Concept lub = (Concept) _graph.leastUpperBound(ontologyConcepts
                        .get(i), ontologyConcepts.get(j));

                if (lub == null) {
                    // FIXME: add highlight color?
                    // The offending nodes.
                    _debug("This is not a lattice. \""
                            + ontologyConcepts.get(i).getName()
                            + "\" and \""
                            + ontologyConcepts.get(j).getName()
                            + "\""
                            + " does not have a unique least upper bound (LUB).");
                    return false;
                } else {
                    _debug("LUB(" + ontologyConcepts.get(i).getName() + ", "
                            + ontologyConcepts.get(j).getName() + "): "
                            + lub.toString());
                }
            }
        }

        _debug("This is a correctly formed lattice.");
        return true;
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
    ////                         private variables                 ////

    /** The cached graph. */
    private ConceptGraph _graph;

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
