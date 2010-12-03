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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ptolemy.data.ScalarToken;
import ptolemy.data.expr.Variable;
import ptolemy.graph.GraphStateException;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
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
 *  @since Ptolemy II 8.0
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
        infiniteConceptExpressionOperationsClass = new StringAttribute(this,
                "infiniteConceptExpressionOperationsClass");
        _attachText("_iconDescription", _ICON);
    }

    /** Create a new Ontology with no container or name.
     *  @param workspace The workspace into which to put it.
     *  @exception IllegalActionException If the base class throws it.
     */
    public Ontology(Workspace workspace) throws IllegalActionException {
        super(workspace);
        try {
            infiniteConceptExpressionOperationsClass = new StringAttribute(
                    this, "infiniteConceptExpressionOperationsClass");
        } catch (NameDuplicationException nameDupEx) {
            throw new IllegalActionException(this, nameDupEx, "");
        }
        _attachText("_iconDescription", _ICON);
    }

    /** Name of the class that provides the expression operations for
     *  any infinite concepts contained in this ontology.
     */
    public StringAttribute infiniteConceptExpressionOperationsClass;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the
     *  infiniteConceptExpressionOperationsClass attribute changes, update
     *  the internal object reference for the expression operations for infinite
     *  concepts by instantiating a new object from the specified class.
     *  @param attribute The attribute that changed.
     *  @throws IllegalActionException If the class specified for the
     *   infiniteConceptExpressionOperationsClass is not valid or an object
     *   for that class cannot be instantiated.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == infiniteConceptExpressionOperationsClass) {
            String operationsClassName = infiniteConceptExpressionOperationsClass
                    .getValueAsString();
            if (operationsClassName != null && !operationsClassName.equals("")) {
                Class operationsClass = null;
                try {
                    operationsClass = Class.forName(operationsClassName);
                } catch (ClassNotFoundException ex) {
                    try {
                        operationsClassName = _addPackagePrefix(operationsClassName);
                        operationsClass = Class.forName(operationsClassName);
                    } catch (ClassNotFoundException ex2) {
                        throw new IllegalActionException(this, ex2,
                                "Expression operations for infinite concepts class "
                                        + "named " + operationsClassName
                                        + " not found.");
                    }
                }
                _createOperationsClassInstance(operationsClass);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return the concept in the ontology represented by the given string, or
     *  null if no such concept exists.
     *  @param conceptString The string of the concept to look for, which would be what
     *   is returned by the concept's toString() method.  This is not necessarily
     *   the Ptolemy {@link ptolemy.kernel.util.NamedObj NamedObj} name of the
     *   concept. For example, {@link InfiniteConcept InfininteConcepts}
     *   have automatically generated unique names that are not the same as
     *   what is returned by their toString() method.
     *  @return The concept that is represented by the given string, or null
     *   if no such concept exists.
     */
    public Concept getConceptByString(String conceptString) {
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
            for (Object concept : entityList(FlatTokenRepresentativeConcept.class)) {
                String repName = ((Concept) concept).getName();
                if (conceptString.startsWith(repName)) {
                    String expression = conceptString.substring(repName
                            .length() + 1);
                    try {
                        // Use a temporary Variable object to parse the 
                        // expression string that represents the token.
                        Variable var = new Variable(this, "temp");
                        var.setExpression(expression);
                        var.setContainer(null);

                        if (concept instanceof FlatScalarTokenRepresentativeConcept) {
                            return FlatScalarTokenInfiniteConcept
                                    .createFlatScalarTokenInfiniteConcept(
                                            this,
                                            (FlatScalarTokenRepresentativeConcept) concept,
                                            (ScalarToken) var.getToken());
                        } else {
                            return FlatTokenInfiniteConcept
                                    .createFlatTokenInfiniteConcept(
                                            this,
                                            (FlatTokenRepresentativeConcept) concept,
                                            var.getToken());
                        }
                    } catch (IllegalActionException ex) {
                        throw new IllegalArgumentException(
                                "Could not instantiate "
                                        + "a FlatTokenInfiniteConcept for "
                                        + conceptString + ".", ex);
                    } catch (NameDuplicationException nameDupEx) {
                        throw new IllegalArgumentException(
                                "Could not instantiate "
                                        + "a FlatTokenInfiniteConcept for "
                                        + conceptString + ".", nameDupEx);
                    }
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

    /** Return the expression operations for infinite concepts object
     *  contained in this ontology.
     *  @return The expression operations object.
     */
    public ExpressionOperationsForInfiniteConcepts getExpressionOperations() {
        return _operationsForInfiniteConcepts;
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

        for (FiniteConcept concept : _containedConcepts()) {
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
        _graph = _buildConceptGraph();

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

        List<FiniteConcept> ontologyConcepts = _containedConcepts();

        // This is the same check done in ptolemy.graph.DirectedAcyclicGraph.
        for (int i = 0; i < ontologyConcepts.size() - 1; i++) {
            for (int j = i + 1; j < ontologyConcepts.size(); j++) {
                Concept lub = (Concept) _graph.leastUpperBound(
                        ontologyConcepts.get(i), ontologyConcepts.get(j));

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
    ////                         protected methods                 ////

    /** Return the graph represented by this ontology.
     *  Graph is weighted by Concepts on the nodes and ConceptRelations on
     *  the edges.
     *  @return The concept graph.
     */
    protected ConceptGraph _buildConceptGraph() {
        if (workspace().getVersion() != _graphVersion) {
            // Construct the graph.
            _graph = new DAGConceptGraph();
            List<FiniteConcept> concepts = _containedConcepts();
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
                    assert (remotePorts.size() == 1) : "ConceptRelations can only connect two concepts";
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

    /** Return a string composed of the ontology java package prefix and the
     *  specified name of the class.
     *  @param operationsClassName The name of the expression operations for
     *   infinite concepts class provided by the user in a string attribute.
     *  @return The composed string.
     */
    private String _addPackagePrefix(String operationsClassName) {
        String ontologyClassName = getClassName();
        int ontologyPrefixLength = ontologyClassName.lastIndexOf(getName());
        String operationsClass = ontologyClassName.substring(0,
                ontologyPrefixLength).concat(operationsClassName);
        return operationsClass;
    }

    /** Return all the finite concepts contained in this ontology.
     *  @return A list containing all finite concepts in this ontology.
     */
    @SuppressWarnings("unchecked")
    private List<FiniteConcept> _containedConcepts() {
        return (List<FiniteConcept>) entityList(FiniteConcept.class);
    }

    /** Create an expression operations for infinite concepts object from
     *  the given class.
     * 
     *  @param operationsClass The specified class for the expression operations
     *   object. It must be a subclass of ExpressionOperationsForInfiniteConcepts.
     *  @throws IllegalActionException Thrown if the class is not a subclass
     *   of ExpressionOperationsForInfiniteConcepts, or an object cannot be
     *   instantiated from the given class.
     */
    // FIXME: This code is very similar to the method _createTempActorInstance()
    // in the class ActorConstraintsDefinitionAttribute.
    // Maybe we should pull this method out into a generic utility class that
    // can return an object instance when passed a class and an array of objects
    // for inputs to the constructor.
    private void _createOperationsClassInstance(Class operationsClass)
            throws IllegalActionException {

        if (!ExpressionOperationsForInfiniteConcepts.class
                .isAssignableFrom(operationsClass)) {
            throw new IllegalActionException(this, "The class "
                    + operationsClass.getName() + " is not a subclass of "
                    + "ExpressionOperationsForInfiniteConcepts.");
        }

        Constructor operationsConstructor = null;
        try {
            operationsConstructor = operationsClass
                    .getConstructor(new Class[] { Ontology.class });
        } catch (NoSuchMethodException ex) {
            throw new IllegalActionException(this, ex,
                    "Could not find the constructor"
                            + " method for the expression operations class "
                            + operationsClass + ".");
        }

        try {
            _operationsForInfiniteConcepts = (ExpressionOperationsForInfiniteConcepts) operationsConstructor
                    .newInstance(new Object[] { this });
        } catch (InvocationTargetException ex) {
            throw new IllegalActionException(this, ex,
                    "Exception thrown when trying to call"
                            + " the constructor for the operations class "
                            + operationsClass + ".");
        } catch (IllegalArgumentException ex) {
            throw new IllegalActionException(this, ex,
                    "Invalid argument passed to"
                            + " the constructor for the operations class "
                            + operationsClass + ".");
        } catch (InstantiationException ex) {
            throw new IllegalActionException(this, ex, "Unable to instantiate"
                    + " the operations class " + operationsClass + ".");
        } catch (IllegalAccessException ex) {
            throw new IllegalActionException(this, ex, "Do not have access "
                    + " the constructor for the operations class "
                    + operationsClass + " within this method.");
        }
    }

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

    /** The object that provides mathematical operations for infinite concepts
     *  within this ontology for constructing concept functions.
     */
    private ExpressionOperationsForInfiniteConcepts _operationsForInfiniteConcepts = null;
}
