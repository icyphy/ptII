/* The graph controller for the vergil viewer

 Copyright (c) 1999-2001 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.ptolemy.kernel;

import java.util.List;

import diva.canvas.interactor.Interactor;
import diva.canvas.interactor.SelectionDragger;
import diva.canvas.interactor.SelectionInteractor;
import diva.graph.EdgeController;
import diva.graph.GraphPane;
import diva.graph.NodeController;

import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Location;
import ptolemy.moml.Vertex;

//////////////////////////////////////////////////////////////////////////
//// ViewerGraphController
/**
A graph controller for the Ptolemy II schematic viewer.
This controller contains a set of default node controllers for attributes,
entities, links, ports, and relations.  Those default controllers can
be overridden by attributes of type NodeControllerFactory.
The getNodeController() method determines which controller to return
for each node.
<p>
In addition, this controller provides graph-wide operations that allow
nodes to be moved and context menus to be created.  It does
not provide interaction for adding or removing nodes; those are provided
by a derived class.  Right-clicking on the background will
create a context-sensitive menu for the graph.

@author Steve Neuendorffer
@contributor Edward A. Lee
@version $Id$
*/
public class ViewerGraphController extends PtolemyGraphController {

    /** Create a new basic controller with default
     *  terminal and edge interactors and default context menus.
     */
    public ViewerGraphController() {
        _createControllers();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the edge controller appropriate for the given node,
     *  which in this case is the same link controller returned by
     *  getLinkController().
     *  @param edge The edge object.
     */
    public EdgeController getEdgeController(Object edge) {
        return _linkController;
    }

    /** Return the node controller appropriate for the given object.
     *  If the object is an instance of Vertex, then return the
     *  local relation controller.  If it is an instance of Location,
     *  then determine whether it is an Entity, Attribute, or Port,
     *  and return the appropriate default controller.
     *  If the argument is an instance of Port, then return the
     *  local port controller.
     *  @param object A Vertex, Location, or Port.
     */
    public NodeController getNodeController(Object object) {
        // Defer to the superclass if it can provide a controller.
        NodeController result = super.getNodeController(object);
        if (result != null) {
            // Add to the selection dragger.
            // NOTE: This should not be null, but in case it is,
            // it is better to just have the selection dragger not
            // work than to get a null pointer exception.
            if(_selectionDragger != null) {
                _selectionDragger.addSelectionInteractor(
                        (SelectionInteractor)result.getNodeInteractor());
            }
            return result;
        }

        // Superclass cannot provide a controller. Use defaults.
	if(object instanceof Vertex) {
            return _relationController;
        } else if(object instanceof Location) {
            Object semanticObject = getGraphModel().getSemanticObject(object);
            if (semanticObject instanceof Entity) {
                return _entityController;
            } else if (semanticObject instanceof Attribute) {
                return _attributeController;
            } else if (semanticObject instanceof Port) {
                return _portController;
            } else {
                throw new RuntimeException(
                "Unrecognized object: " + semanticObject);
            }
        } else if(object instanceof Port) {
            return _entityPortController;
        }
        throw new RuntimeException(
                "Node with unknown semantic object: " + object);
    }

    /** Set the configuration.  This is used by some of the controllers
     *  to open files or URLs.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);
        _attributeController.setConfiguration(configuration);
        _entityController.setConfiguration(configuration);
        _entityPortController.setConfiguration(configuration);
        _relationController.setConfiguration(configuration);
        _linkController.setConfiguration(configuration);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the controllers for nodes in this graph.
     *  In this base class, controllers with PARTIAL access are created.
     *  This is called by the constructor, so derived classes that
     *  override this must be careful not to reference local variables
     *  defined in the derived classes, because the derived classes
     *  will not have been fully constructed by the time this is called.
     */
    protected void _createControllers() {
        super._createControllers();
	_attributeController = new AttributeController(this,
                 AttributeController.PARTIAL);
	_entityController = new EntityController(this,
                 AttributeController.PARTIAL);
	_entityPortController = new EntityPortController(this,
                 AttributeController.PARTIAL);
	_relationController = new RelationController(this);
	_linkController = new LinkController(this);
    }

    /** Initialize all interaction on the graph pane. This method
     *  is called by the setGraphPane() method of the superclass.
     *  This initialization cannot be done in the constructor because
     *  the controller does not yet have a reference to its pane
     *  at that time.
     */
    protected void initializeInteraction() {
        GraphPane pane = getGraphPane();

        // Create and set up the selection dragger
        _selectionDragger = new SelectionDragger(pane);

        // NOTE: Do not call _initializeInteraction to do this because
        // that is overloaded in derived classes, and we are still
        // in a constructor call.
        _selectionDragger.addSelectionInteractor(
                (SelectionInteractor)_entityController.getNodeInteractor());
        _selectionDragger.addSelectionInteractor(
                (SelectionInteractor)_relationController.getNodeInteractor());
        _selectionDragger.addSelectionInteractor(
                (SelectionInteractor)_portController.getNodeInteractor());
        _selectionDragger.addSelectionInteractor(
                (SelectionInteractor)_attributeController.getNodeInteractor());

        // Link controller is not a PtolemyNodeController, so it has
        // to be done directly.
	_selectionDragger.addSelectionInteractor(
                (SelectionInteractor)_linkController.getEdgeInteractor());

        super.initializeInteraction();
    }

    /** Initialize interactions for the specified controller.  This
     *  method is called when a new controller is constructed. In this
     *  class, this method attaches a selection dragger to the controller
     *  if the controller.
     *  @param controller The controller for which to initialize interaction.
     */
    protected void _initializeInteraction(PtolemyNodeController controller) {
        super._initializeInteraction(controller);
        Interactor interactor = controller.getNodeInteractor();
        if (interactor instanceof SelectionInteractor) {
            _selectionDragger.addSelectionInteractor(
                    (SelectionInteractor)interactor);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected variables                ////

    /** The attribute controller. */
    protected PtolemyNodeController _attributeController;

    /** The entity controller. */
    protected PtolemyNodeController _entityController;

    /** The entity port controller. */
    protected PtolemyNodeController _entityPortController;

    /** The link controller. */
    protected LinkController _linkController;

    /** The relation controller. */
    protected PtolemyNodeController _relationController;

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // The selection interactor for drag-selecting nodes
    private SelectionDragger _selectionDragger;
}
