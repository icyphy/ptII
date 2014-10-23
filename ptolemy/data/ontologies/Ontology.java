/**
 * A composite entity containing concepts and their ordering relations.
 *
 * Copyright (c) 2007-2014 The Regents of the University of California. All
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

import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Ontology

/** A specification of an ontology, which is a set of concepts and a
 *  partial ordering relation.
 *  The structure is represented by interconnections
 *  between concepts contained by this ontology.
 *
 *  @see ConceptGraph
 *  @see Concept
 *  @author Edward A. Lee, Ben Lickly, Dai Bui, Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
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

    /** Return the concept in the ontology represented by the given string, or
     *  null if no such concept exists.
     *  @param conceptString The string of the concept to look for, which would be what
     *   is returned by the concept's toString() method.  This is not necessarily
     *   the Ptolemy {@link ptolemy.kernel.util.NamedObj NamedObj} name of the
     *   concept. For example, {@link InfiniteConcept InfiniteConcepts}
     *   have automatically generated unique names that are not the same as
     *   what is returned by their toString() method.
     *  @return The concept that is represented by the given string, or null
     *   if no such concept exists.
     *  @exception IllegalActionException Thrown if there is an error getting the
     *   concept.
     */
    public Concept getConceptByString(String conceptString)
            throws IllegalActionException {
        // If the conceptString is wrapped by quotes, strip them off before
        // trying to find the concept that matches the conceptString.
        if (conceptString.startsWith("\"") && conceptString.endsWith("\"")) {
            conceptString = conceptString.substring(1,
                    conceptString.length() - 1);
        }

        Concept result = _findConceptByString(conceptString);

        // If the concept is not found, check to see if it is an infinite
        // concept that is represented by a concept in this ontology.
        if (result == null) {
            for (InfiniteConceptRepresentative infiniteRepresentative : entityList(InfiniteConceptRepresentative.class)) {
                if (infiniteRepresentative
                        .containsThisInfiniteConceptString(conceptString)) {
                    return infiniteRepresentative
                            .getInfiniteConceptByString(conceptString);
                }
            }
        }

        return result;
    }

    /** Return the graph represented by this ontology.
     *  Graph is weighted by Concepts on the nodes and ConceptRelations on
     *  the edges. Currently we only have ontologies that are lattices, but
     *  in general, an ontology can represent more general relationships
     *  that might have a graph structure that is not a lattice.  So we
     *  provide the getGraph() method in the base class for any future
     *  ontology subclasses that are not lattices.
     *  @return The concept graph.
     */
    public ConceptGraph getConceptGraph() {
        return _buildConceptGraph();
    }

    /** Return a set of finite concepts which are unacceptable solutions in all situations.
     *  Here these concepts are undesirable for any user of this ontology, for example,
     *  "Top" may indicate a conflict for all models using this ontology.
     *  Ontologies may not contain duplicate concepts, so the collection of
     *  unacceptable finite concepts is always a set.
     *  @return The set of unacceptable finite concepts in this ontology.
     */
    public Set<FiniteConcept> getUnacceptableConcepts() {

        HashSet<FiniteConcept> unacceptableConcepts = new HashSet<FiniteConcept>();

        for (FiniteConcept concept : entityList(FiniteConcept.class)) {
            if (!concept.isValueAcceptable()) {
                unacceptableConcepts.add(concept);
            }
        }

        return unacceptableConcepts;

    }

    /** Return true if the ontology graph is a lattice, false otherwise.
     *  @return True if the graph is a lattice, false otherwise.
     */
    public boolean isLattice() {
        return getConceptGraph().isLattice();
    }

    /** Create a new relation with the specified name, add it to the
     *  relation list, and return it.
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     *  This overrides {@link CompositeEntity#newRelation} of CompositeEntity.
     *  @param name The name of the new relation.
     *  @return The new relation.
     *  @exception IllegalActionException If name argument is null.
     *  @exception NameDuplicationException If name collides with a name
     *   already in the container.
     */
    @Override
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
    ////                         protected methods                 ////

    /** Return the graph represented by this ontology.
     *  Graph is weighted by FiniteConcepts on the nodes and ConceptRelations on
     *  the edges.
     *  @return The concept graph.
     */
    protected ConceptGraph _buildConceptGraph() {
        if (workspace().getVersion() != _graphVersion) {
            // Construct the graph.
            _graph = new DAGConceptGraph();
            List<FiniteConcept> concepts = entityList(FiniteConcept.class);
            for (FiniteConcept concept : concepts) {
                ((DAGConceptGraph) _graph).addConcept(concept);
            }
            for (FiniteConcept concept : concepts) {
                @SuppressWarnings("unchecked")
                List<ConceptRelation> relationLinks = concept.abovePort
                .linkedRelationList();
                for (ConceptRelation link : relationLinks) {
                    @SuppressWarnings("unchecked")
                    List<ComponentPort> remotePorts = link
                    .linkedPortList(concept.abovePort);
                    assert remotePorts.size() == 1 : "ConceptRelations can only connect two concepts";
                    for (ComponentPort remotePort : remotePorts) {
                        ((DAGConceptGraph) _graph)
                        .addRelation(concept,
                                (FiniteConcept) remotePort
                                .getContainer(), link);
                    }
                }
            }

            // Set the graph version after creating the new graph
            _graphVersion = workspace().getVersion();
        }
        return _graph;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The cached graph. */
    protected ConceptGraph _graph;

    /** The workspace version at which the cached graph was valid. */
    protected long _graphVersion = -1L;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Find the concept in the ontology whose toString() method returns the
     *  given string.
     *  @param conceptString The string that represents the concept to be found.
     *  @return The concept whose toString() method matches the input string,
     *   or null if it is not found.
     */
    private Concept _findConceptByString(String conceptString) {
        for (Object concept : entityList(Concept.class)) {
            if (((Concept) concept).toString().equals(conceptString)) {
                return (Concept) concept;
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

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
