/* A graph model for graphically manipulating ontology solver models.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.vergil.ontologies;

import java.util.Iterator;

import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.NamedObjNodeModel;
import diva.graph.GraphEvent;
import diva.graph.modular.EdgeModel;
import diva.graph.modular.NodeModel;
import diva.util.NullIterator;

///////////////////////////////////////////////////////////////////
//// OntologySolverGraphModel

/** A graph model for graphically manipulating ontology solver models.
 *
 *  @author Charles Shelton, Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class OntologySolverGraphModel extends AbstractBasicGraphModel {

    /** Construct a new graph model whose root is the given composite entity.
     *  This should always be an
     *  {@link ptolemy.data.ontologies.OntologySolverModel OntologySolverModel}
     *  composite entity.
     *  @param composite The top-level composite entity for the model.
     */
    public OntologySolverGraphModel(CompositeEntity composite) {
        super(composite);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Disconnect an edge object. In an ontology solver model there are no
     *  edges in the graph, so this method does nothing.
     *  @param eventSource The source of the event that will be dispatched,
     *   e.g. the view that made this call.
     *  @param edge The edge to be disconnected.
     */
    @Override
    public void disconnectEdge(Object eventSource, Object edge) {
    }

    /** Get the ontology model which maps all ontology nodes in the graph to the
     *  {@link Ontology} elements in then ontology solver model.
     *  @return The concept model.
     */
    public OntologyModel getOntologyModel() {
        return _ontologyModel;
    }

    /** Return a MoML String that will delete the given edge from the
     *  Ptolemy model. Since there are no edges in an ontology solver model,
     *  this method always returns the empty string.
     *  @param edge The edge.
     *  @return The empty string.
     */
    @Override
    public String getDeleteEdgeMoML(Object edge) {
        return "";
    }

    /** Return a MoML String that will delete the given node from the
     *  Ptolemy model.
     *  @param node The node.
     *  @return A valid MoML string.
     */
    @Override
    public String getDeleteNodeMoML(Object node) {
        if (!(getNodeModel(node) instanceof NamedObjNodeModel)) {
            return "";
        }

        NamedObjNodeModel model = (NamedObjNodeModel) getNodeModel(node);
        return model.getDeleteNodeMoML(node);
    }

    /** Return the edge controller appropriate for the given edge. In the
     *  ontology solver editor, there are no edges in the model, so this
     *  method returns null.
     *  @param edge The given edge in the ontology solver model editor. No
     *   edges exist in the ontology solver model.
     *  @return null.
     */
    @Override
    public EdgeModel getEdgeModel(Object edge) {
        return null;
    }

    /** Return the node model for the given object.  If the object is not
     *  a node, then return null. The nodes in an ontology solver model should
     *  be either {@link Ontology} entities or Ptolemy attributes that contain
     *  concept functions or actor constraint definitions.
     *  @param node An object which is assumed to be in this graph model.
     *  @return The node model for the specified node, or null if there
     *   is none.
     */
    @Override
    public NodeModel getNodeModel(Object node) {
        if (node instanceof Locatable) {
            Object container = ((Locatable) node).getContainer();

            if (container instanceof Ontology) {
                return _ontologyModel;
            }
        }

        return super.getNodeModel(node);
    }

    /** Delete a node from its parent graph and notify
     *  graph listeners with a NODE_REMOVED event.
     *  @param eventSource The source of the event that will be dispatched,
     *   e.g. the view that made this call.
     *  @param node The node to be removed.
     */
    @Override
    public void removeNode(Object eventSource, Object node) {
        if (!(getNodeModel(node) instanceof NamedObjNodeModel)) {
            return;
        }

        NamedObjNodeModel model = (NamedObjNodeModel) getNodeModel(node);
        model.removeNode(eventSource, node);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The model of ontologies in the ontology solver model. */
    private OntologyModel _ontologyModel = new OntologyModel();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The model for an icon that represents ontologies in the ontology solver
     *  model.
     */
    public class OntologyModel extends NamedObjNodeModel {

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Return a MoML String that will delete the given node from the
         *  ontology solver model. This assumes that the context is the
         *  container of the ontology.
         *  @param node The node to be deleted.
         *  @return A valid MoML string.
         */
        @Override
        public String getDeleteNodeMoML(Object node) {
            NamedObj deleteObj = ((Locatable) node).getContainer();
            NamedObj container = deleteObj.getContainer();
            return "<deleteEntity name=\"" + deleteObj.getName(container)
                    + "\"/>\n";
        }

        /** Return the graph parent of the given node.
         *  @param node The node, which is assumed to be a location.
         *  @return The container of the icon's container, which should
         *   be the root of this graph model.
         */
        @Override
        public Object getParent(Object node) {
            return ((Locatable) node).getContainer().getContainer();
        }

        /** Return an iterator over the edges coming into the given ontology
         *  node. Since there are no edges in an ontology solver model, this
         *  method always returns an iterator over an empty list.
         *  @param node The node, which is assumed to be a location attribute
         *   of an ontology in the ontology solver model.
         *  @return An iterator over an empty list.
         */
        @Override
        public Iterator inEdges(Object node) {
            return new NullIterator();
        }

        /** Return an iterator over the edges coming out of the given ontology
         *  node. Since there are no edges in an ontology solver model, this
         *  method always returns an iterator over an empty list.
         *  @param node The node, which is assumed to be a location attribute
         *   of an ontology in the ontology solver model.
         *  @return An iterator over an empty list.
         */
        @Override
        public Iterator outEdges(Object node) {
            return new NullIterator();
        }

        /** Remove the given node from the model.  The node is assumed
         *  to be a Locatable belonging to an entity.
         *  @param eventSource The source of the event directing the removal of
         *   the node.
         *  @param node The node to be removed.
         */
        @Override
        public void removeNode(final Object eventSource, Object node) {
            NamedObj deleteObj = ((Locatable) node).getContainer();

            String elementName = null;

            if (deleteObj instanceof Ontology) {
                // Object is an entity.
                elementName = "deleteEntity";
            } else {
                throw new InternalErrorException(
                        "Attempt to remove a node that is not an Ontology. "
                                + "node = " + node);
            }

            String moml = "<" + elementName + " name=\"" + deleteObj.getName()
                    + "\"/>\n";

            // Make the request in the context of the container.
            NamedObj container = deleteObj.getContainer();
            MoMLChangeRequest request = new MoMLChangeRequest(
                    OntologySolverGraphModel.this, container, moml);
            request.addChangeListener(new ChangeListener() {
                @Override
                public void changeFailed(ChangeRequest change,
                        Exception exception) {
                    // If we fail, then issue structureChanged.
                    dispatchGraphEvent(new GraphEvent(eventSource,
                            GraphEvent.STRUCTURE_CHANGED, getRoot()));
                }

                @Override
                public void changeExecuted(ChangeRequest change) {
                    // If we succeed, then issue structureChanged, since
                    // this is likely connected to something.
                    dispatchGraphEvent(new GraphEvent(eventSource,
                            GraphEvent.STRUCTURE_CHANGED, getRoot()));
                }
            });
            request.setUndoable(true);
            container.requestChange(request);
        }
    }
}
