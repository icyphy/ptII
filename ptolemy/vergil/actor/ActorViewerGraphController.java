/* The graph controller for the vergil viewer

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.actor;

import diva.canvas.Figure;
import diva.canvas.interactor.Interactor;
import diva.canvas.interactor.SelectionDragger;
import diva.canvas.interactor.SelectionInteractor;
import diva.graph.EdgeController;
import diva.graph.GraphPane;
import diva.graph.NodeController;
import diva.gui.GUIUtilities;

import ptolemy.actor.Actor;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Vertex;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.basic.RunnableGraphController;
import ptolemy.vergil.kernel.AnimationRenderer;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.kernel.RelationController;

import javax.swing.JMenu;
import javax.swing.JToolBar;

//////////////////////////////////////////////////////////////////////////
//// ActorViewerGraphController
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
by a derived class.  If does provide toolbar buttons for executing
the model (or if this is not the top level, delegating to the top
level to execute). Right-clicking on the background will
create a context-sensitive menu for the graph.

@author Steve Neuendorffer, Contributor: Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class ActorViewerGraphController extends RunnableGraphController {

    /** Create a new basic controller with default
     *  terminal and edge interactors and default context menus.
     */
    public ActorViewerGraphController() {
        _createControllers();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add hot key for look inside.
     *  @param menu The menu to add to, which is ignored.
     *  @param toolbar The toolbar to add to, which is also ignored.
     */
    public void addToMenuAndToolbar(JMenu menu, JToolBar toolbar) {
        super.addToMenuAndToolbar(menu, toolbar);
        GUIUtilities.addHotKey(getFrame().getJGraph(),
                _entityController._lookInsideAction);
    }

    /** React to an event by highlighting the actor being iterated.
     *  This effectively animates the execution.
     *  @param event The debug event.
     */
    public void event(DebugEvent event) {
        if (event instanceof FiringEvent) {
            Actor actor = ((FiringEvent)event).getActor();
            if (actor instanceof NamedObj) {
                NamedObj objToHighlight = (NamedObj)actor;

                // If the object is not contained by the associated
                // composite, then find an object above it in the hierarchy
                // that is.
                AbstractBasicGraphModel graphModel =
                    (AbstractBasicGraphModel)getGraphModel();
                NamedObj toplevel = graphModel.getPtolemyModel();
                while (objToHighlight != null
                        && objToHighlight.getContainer() != toplevel) {
                    objToHighlight = (NamedObj)objToHighlight.getContainer();
                }
                if (objToHighlight == null) {
                    return;
                }
                Object location = objToHighlight.getAttribute("_location");
                if (location != null) {
                    Figure figure = getFigure(location);
                    if (figure != null) {
                        if (_animationRenderer == null) {
                            _animationRenderer = new AnimationRenderer();
                        }
                        FiringEvent.FiringEventType type
                            = ((FiringEvent)event).getType();
                        if (type == FiringEvent.BEFORE_ITERATE
                                || type == FiringEvent.BEFORE_FIRE) {
                            _animationRenderer.renderSelected(figure);
                            _animated = figure;
                            long animationDelay = getAnimationDelay();
                            if (animationDelay > 0) {
                                try {
                                    Thread.sleep(animationDelay);
                                } catch (InterruptedException ex) {}
                            }
                        } else if (type == FiringEvent.AFTER_ITERATE
                                || type == FiringEvent.AFTER_POSTFIRE) {
                            if (_animated != null) {
                                _animationRenderer.renderDeselected(_animated);
                            }
                        }
                    }
                }
            }
        }
    }

    /** Return the edge controller appropriate for the given node,
     *  which in this case is the same link controller returned by
     *  getLinkController().
     *  @param edge The edge object.
     */
    public EdgeController getEdgeController(Object edge) {
        return _linkController;
    }

    /** Return the value of the entity controller so that callers
     *  can add context menus by calling
     *  <pre>
     *  getEntityController.addMenuItemFactory(new XXXDialogFactory);
     *  </pre>
     *  @return that ActorController that is used to
     */
    public ActorController getEntityController() {
        // Used by jni.ThalesGraphFrame to add jni.ArgumentDialogFactory
        return _entityController;
    }

    /** Return the node controller appropriate for the given object.
     *  If the object is an instance of Vertex, then return the
     *  local relation controller.  If it implements Locatable,
     *  then determine whether it is an Entity, Attribute, or Port,
     *  and return the appropriate default controller.
     *  If the argument is an instance of Port, then return the
     *  local port controller.
     *  @param object A Vertex, Locatable, or Port.
     */
    public NodeController getNodeController(Object object) {
        // Defer to the superclass if it can provide a controller.
        NodeController result = super.getNodeController(object);
        if (result != null) {
            return result;
        }

        // Superclass cannot provide a controller. Use defaults.
        if (object instanceof Vertex) {
            return _relationController;
        } else if (object instanceof Locatable) {
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
        } else if (object instanceof Port) {
            return _entityPortController;
        }
        throw new RuntimeException(
                "Node with unknown semantic object: " + object);
    }

    /** Set the configuration.  The configuration is used when
     *  opening documentation files.
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
        _entityController = new ActorController(this,
                AttributeController.PARTIAL);
        _entityPortController = new IOPortController(this,
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
        _selectionDragger.addSelectionModel(getSelectionModel());

        super.initializeInteraction();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The attribute controller. */
    protected NamedObjController _attributeController;

    /** The entity controller. */
    protected ActorController _entityController;

    /** The entity port controller. */
    protected NamedObjController _entityPortController;

    /** The link controller. */
    protected LinkController _linkController;

    /** The relation controller. */
    protected NamedObjController _relationController;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The selection interactor for drag-selecting nodes
    private SelectionDragger _selectionDragger;
}
