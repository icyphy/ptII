/* A graph model for basic ptolemy models.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.basic;

import java.util.List;

import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.kernel.AttributeNodeModel;
import ptolemy.vergil.kernel.CompositeEntityModel;
import diva.graph.GraphEvent;
import diva.graph.modular.CompositeModel;
import diva.graph.modular.ModularGraphModel;
import diva.graph.modular.NodeModel;

//////////////////////////////////////////////////////////////////////////
//// AbstractBasicGraphModel
/**
This base class provides some common services for visual notations for
Ptolemy II models. It assumes that the semantic object of a particular
graph object is fixed, and provides facilities for making changes to the
model via a change request. It supports visible attributes.
<p>
This class uses a change listener to detect changes to the Ptolemy model
that do not originate from this class.  These changes are propagated
as structure changed graph events to all graphListeners registered with this
model.  This mechanism allows a graph visualization of a ptolemy model to
remain synchronized with the state of a mutating model.

@author Steve Neuendorffer, Contributor: Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public abstract class AbstractBasicGraphModel extends ModularGraphModel {

    /** Create a graph model for the specified Ptolemy II model.
     *  @param composite The Ptolemy II model.
     */
    public AbstractBasicGraphModel(CompositeEntity composite) {
        super(composite);
        _composite = composite;
        _graphChangeListener = new GraphChangeListener();
        composite.addChangeListener(_graphChangeListener);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Disconnect an edge from its two endpoints and notify graph
     *  listeners with an EDGE_HEAD_CHANGED and an EDGE_TAIL_CHANGED
     *  event whose source is the given source.
     *  @param eventSource The source of the event that will be dispatched,
     *   e.g. the view that made this call.
     *  @exception GraphException If the operation fails.
     */
    public abstract void disconnectEdge(Object eventSource, Object edge);

    /** Return a MoML String that will delete the given edge from the
     *  Ptolemy model.
     *  @return A valid MoML string.
     */
    public abstract String getDeleteEdgeMoML(Object edge);

    /** Return a MoML String that will delete the given node from the
     *  Ptolemy model.
     *  @return A valid MoML string.
     */
    public abstract String getDeleteNodeMoML(Object edge);

    /** Return the model for the given composite object.
     *  In this base class, return an instance of CompositeEntityModel
     *  if the object is the root object of this graph model.
     *  Otherwise return null.
     *  @param composite A composite object.
     *  @return An instance of CompositeEntityModel if the object is the root
     *   object of this graph model.  Otherwise return null.
     */
    public CompositeModel getCompositeModel(Object composite) {
        if (composite.equals(_composite)) {
            return _compositeModel;
        } else {
            return null;
        }
    }

    /** Return the node model for the given object.  If the object is an
     *  attribute, then return an attribute model. Otherwise, return null.
     *  @param node An object which is assumed to be in this graph model.
     *  @return An instance of the inner class AttributeNodeModel if the object
     *   is an instance of Locatable whose container is an instance
     *   of Attribute, and otherwise, null.
     */
    public NodeModel getNodeModel(Object node) {
        if (node instanceof Locatable
                && ((Locatable)node).getContainer() instanceof Attribute) {
            return _attributeModel;
        }
        return null;
    }

    /** Return the property of the object associated with
     *  the given property name. In this implementation
     *  properties are stored in variables of the graph object (which is
     *  always a Ptolemy NamedObj). If no variable with the given name
     *  exists in the object, then return null.  Otherwise retrieve the
     *  token from the variable.  If the token is an instance of ObjectToken,
     *  then get the value from the token and return it.  Otherwise, return
     *  the result of calling toString on the token.
     *  @param object The graph object, which is assumed to be an instance of
     *   NamedObj.
     *  @param propertyName The name of the new property.
     */
    public Object getProperty(Object object, String propertyName) {
        try {
            NamedObj namedObject = (NamedObj)object;
            Attribute a = namedObject.getAttribute(propertyName);
            Token t = ((Variable)a).getToken();
            if (t instanceof ObjectToken) {
                return ((ObjectToken)t).getValue();
            } else {
                return t.toString();
            }
        } catch (Exception ex) {
            return null;
        }
    }

    /** Return the Ptolemy II model associated with this graph model.
     *  @return The Ptolemy II model.
     */
    public CompositeEntity getPtolemyModel() {
        return _composite;
    }

    /** Return the semantic object corresponding to the given node, edge,
     *  or composite.  A "semantic object" is an object associated with
     *  a node in the graph.  In this base class, if the argument is an
     *  instance of Port, then return the port.  If the argument is an
     *  instance of Locatable, then return the container of the Locatable.
     *  @param element A graph element.
     *  @return The semantic object associated with this element, or null
     *   if the object is not recognized.
     */
    public Object getSemanticObject(Object element) {
        if (element instanceof Port) {
            return element;
        } else if (element instanceof Locatable) {
            return ((Locatable)element).getContainer();
        }
        return null;
    }

    /** Delete a node from its parent graph and notify
     *  graph listeners with a NODE_REMOVED event.
     *  @param eventSource The source of the event that will be dispatched,
     *   e.g. the view that made this call.
     *  @exception GraphException if the operation fails.
     */
    public abstract void removeNode(Object eventSource, Object node);

    /** Set the property of the given graph object associated with
     *  the given property name to the given value.  In this implementation
     *  properties are stored in variables of the graph object (which is
     *  always a Ptolemy NamedObj).  If no variable with the given name exists
     *  in the graph object, then create a new variable contained
     *  by the graph object with the given name.
     *  If the value is a string, then set the expression of the variable
     *  to that string. Otherwise create a new object token contained the
     *  value and place that in the variable instead.
     *  The operation is performed in a ptolemy change request.
     *  @param object The graph object.
     *  @param propertyName The property name.
     *  @param value The new value of the property.
     */
    public void setProperty(final Object object,
            final String propertyName,
            final Object value) {
        throw new UnsupportedOperationException("hack");
    }

    /** Set the semantic object corresponding to the given node, edge,
     *  or composite.  The semantic objects in this graph model are
     *  fixed, so this method throws an UnsupportedOperationException.
     *  @param object The graph object that represents a node or an edge.
     *  @param semantic The semantic object to associate with the given
     *   graph object.
     */
    public void setSemanticObject(Object object, Object semantic) {
        throw new UnsupportedOperationException("Ptolemy Graph Model does" +
                " not allow semantic objects" +
                " to be changed");
    }

    /** Remove any listeners we have created. The frame displaying this
     *  graph model should call this function when the frame is closed.
     */
    public void removeListeners() {
        _composite.removeChangeListener(_graphChangeListener);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the context for which a change request concerning the given
     *  object should be made.  This is the first container
     *  above the object in the hierarchy that defers its
     *  MoML definition, or the immediate parent if there is none.
     *  @param object The object to change.
     *  @return The context for a change request.
     */
    protected static NamedObj _getChangeRequestParent(NamedObj object) {
        NamedObj container = MoMLChangeRequest.getDeferredToParent(object);
        if (container == null) {
            container = (NamedObj)object.getContainer();
        }
        if (container == null) {
            return object;
        }
        return container;
    }

    /** Return the location attribute contained in the given object, or
     *  a new location contained in the given object if there was no location.
     *  @param object The object for which a location is needed.
     *  @return The location of the object, or a new location if none.
     */
    protected Locatable _getLocation(NamedObj object) {
        List locations = object.attributeList(Locatable.class);
        if (locations.size() > 0) {
            return (Locatable)locations.get(0);
        } else {
            try {
                Locatable location = new Location(object, "_location");
                return location;
            }
            catch (Exception e) {
                throw new InternalErrorException("Failed to create " +
                        "location, even though one does not exist:" +
                        e.getMessage());
            }
        }
    }

    /** Update the graph model.  This is called whenever a change request is
     *  executed.  Subclasses will override this to update internal data
     *  structures that may be cached.
     *  @return True if the graph model changes (always true in this
     *   base class).
     */
    protected boolean _update() {
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The node model for a visible attribute.
    private AttributeNodeModel _attributeModel = new AttributeNodeModel();

    // The root of this graph model, as a CompositeEntity.
    private CompositeEntity _composite;

    // The model for composite entities.
    private CompositeEntityModel _compositeModel = new CompositeEntityModel();

    // Our change listener on _composite.
    private GraphChangeListener _graphChangeListener;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Mutations may happen to the ptolemy model without the knowledge of
     *  this model.  This change listener listens for those changes,
     *  and when they occur, issues a GraphEvent so that any views of
     *  this graph model can update themselves.  Note that although
     *  the graph model uses mutations to make changes to the ptolemy
     *  model, those graph events are not handled here. Instead, they
     *  are handled in the base class since they can be easily
     *  propagated at a finer level of granularity than is possible here.
     */
    public class GraphChangeListener implements ChangeListener {

        /** Notify the listener that a change has been successfully executed.
         *  If the originator of this change is not this graph model, then
         *  issue a graph event to indicate that the structure of the graph
         *  has changed.
         *  @param change The change that has been executed.
         */
        public void changeExecuted(ChangeRequest change) {
            // Ignore anything that comes from this graph model.
            // The other methods take care of issuing the graph event in
            // that case.
            // NOTE: Unfortunately, when you perform look inside, you
            // get a new graph model, and that graph model is modified
            // (for example, by adding icons). This means that the
            // original graph model will be notified of changes,
            // rather spuriously.  We tried having this ignore
            // any change whose source was an instance of GraphModel,
            // but this breaks MVC. If you have two views open
            // on the graph, then the second view will not be notified
            // of changes.
            // Note that a change listener is registered with the top-level
            // model, as it probably has to be, since a change to a model
            // can have repercusions anywhere in the model.
            if (change != null && change.getSource() == this) {
                return;
            }

            // update the graph model.
            if (_update()) {
                // Notify any graph listeners
                // that the graph might have
                // completely changed.
                dispatchGraphEvent(new GraphEvent(
                        AbstractBasicGraphModel.this,
                        GraphEvent.STRUCTURE_CHANGED, getRoot()));
            }
        }

        /** Notify the listener that the change has failed with the
         *  specified exception.
         *  @param change The change that has failed.
         *  @param exception The exception that was thrown.
         */
        public void changeFailed(ChangeRequest change, Exception exception) {
            // Report it if it has not been reported.
            if (change == null) {
                MessageHandler.error("Change failed", exception);
            } else if (!change.isErrorReported()) {
                change.setErrorReported(true);
                MessageHandler.error("Change failed", exception);
            }
            // update the graph model.
            if (_update()) {
                dispatchGraphEvent(new GraphEvent(
                        AbstractBasicGraphModel.this,
                        GraphEvent.STRUCTURE_CHANGED,
                        getRoot()));
            }
        }
    }
}
