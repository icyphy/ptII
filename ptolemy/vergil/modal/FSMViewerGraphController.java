/* The graph controller for the vergil viewer for finite state machines.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.vergil.modal;

import ptolemy.actor.gui.Configuration;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.StateEvent;
import ptolemy.domains.modal.modal.ModalController;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.Locatable;
import ptolemy.vergil.basic.LocatableNodeController;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.basic.RunnableGraphController;
import ptolemy.vergil.kernel.AnimationRenderer;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.modal.modal.ModalTransitionController;
import diva.canvas.Figure;
import diva.canvas.interactor.SelectionDragger;
import diva.graph.EdgeController;
import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.graph.NodeController;

///////////////////////////////////////////////////////////////////
//// FSMViewerGraphController

/**
 A graph controller for the Ptolemy II finite-state machine viewer.
 This controller allows states to be moved and context menus to be accessed,
 but does not provide interaction for adding or removing states or
 transitions.

 @author Steve Neuendorffer, Contributor: Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class FSMViewerGraphController extends RunnableGraphController {

    /** Create a new controller with default port, state, and transition
     *  controllers.
     */
    public FSMViewerGraphController() {
        _createControllers();

        // The following is a fallback controller used when encountering
        // instances of Locatable that have no container. Do not create
        // this in _createControllers() because that is overridden by
        // derived classes.
        _locatableController = new LocatableNodeController(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to an event by highlighting the new state.
     *  @param event The debug event.
     */
    @Override
    public void event(DebugEvent event) {
        if (event instanceof StateEvent) {
            State state = ((StateEvent) event).getState();

            if (state != null) {
                Object location = state.getAttribute("_location");

                if (location != null) {
                    Figure figure = getFigure(location);

                    if (figure != null) {
                        if (_animationRenderer == null) {
                            _animationRenderer = new AnimationRenderer();
                        }

                        if (_animated != figure) {
                            // Deselect previous one.
                            if (_animated != null) {
                                _animationRenderer.renderDeselected(_animated);
                            }

                            _animationRenderer.renderSelected(figure);
                            _animated = figure;

                            long animationDelay = getAnimationDelay();

                            if (animationDelay > 0) {
                                try {
                                    Thread.sleep(animationDelay);
                                } catch (InterruptedException ex) {
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** Return the node controller appropriate for the given node.
     */
    @Override
    public NodeController getNodeController(Object object) {
        // Defer to the superclass if it can provide a controller.
        NodeController result = super.getNodeController(object);

        if (result != null) {
            return result;
        }

        // Superclass cannot provide a controller. Use defaults.
        if (object instanceof Locatable) {
            Object semanticObject = getGraphModel().getSemanticObject(object);

            if (semanticObject instanceof Entity) {
                return _stateController;
            } else if (semanticObject instanceof Attribute) {
                return _attributeController;
            } else if (semanticObject instanceof Port) {
                return _portController;
            } else {
                return _locatableController;
            }
        }

        throw new RuntimeException("Node with unknown semantic object: "
                + object);
    }

    /** Return the edge controller appropriate for the given node.
     */
    @Override
    public EdgeController getEdgeController(Object edge) {
        if (((FSMGraphModel) getGraphModel()).getPtolemyModel() instanceof ModalController) {
            return _modalTransitionController;
        } else {
            return _transitionController;
        }
    }

    /** Set the configuration.  This is may be used by derived controllers
     *  to open files or URLs.
     *  @param configuration The configuration.
     */
    @Override
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);
        _attributeController.setConfiguration(configuration);
        _stateController.setConfiguration(configuration);
        _transitionController.setConfiguration(configuration);
        _modalTransitionController.setConfiguration(configuration);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add hot keys to the actions in the given JGraph.
     *
     *  @param jgraph The JGraph to which hot keys are to be added.
     */
    @Override
    protected void _addHotKeys(JGraph jgraph) {
        super._addHotKeys(jgraph);

        // NOTE: The transition controller's LookInsideAction is designed
        // to handle both State and Transition.  We can't associate more
        // than one with the hot key, so that one handles both.
        _transitionController.addHotKeys(jgraph);

        _attributeController.addHotKeys(jgraph);
    }

    /** Create the controllers for nodes in this graph.
     *  In this base class, controllers with PARTIAL access are created.
     *  This is called by the constructor, so derived classes that
     *  override this must be careful not to reference local variables
     *  defined in the derived classes, because the derived classes
     *  will not have been fully constructed by the time this is called.
     */
    @Override
    protected void _createControllers() {
        super._createControllers();
        _attributeController = new AttributeController(this,
                AttributeController.PARTIAL);
        _stateController = new StateController(this,
                AttributeController.PARTIAL);
        _modalTransitionController = new ModalTransitionController(this);
        _transitionController = new TransitionController(this);
    }

    /** Initialize all interaction on the graph pane. This method
     *  is called by the setGraphPane() method of the superclass.
     *  This initialization cannot be done in the constructor because
     *  the controller does not yet have a reference to its pane
     *  at that time.
     */
    @Override
    protected void initializeInteraction() {
        // NOTE: This method name does not have a leading underscore
        // because it is a diva method.
        GraphPane pane = getGraphPane();

        // Create and set up the selection dragger
        _selectionDragger = new SelectionDragger(pane);
        _selectionDragger.addSelectionModel(getSelectionModel());

        // If the selectionDragger is consuming, then popup menus don't
        // disappear properly.
        _selectionDragger.setConsuming(false);

        super.initializeInteraction();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The attribute controller. */
    protected NamedObjController _attributeController;

    /** The state controller. */
    protected StateController _stateController;

    /** The transition controller. */
    protected TransitionController _transitionController;

    /** The modal transition controller. */
    protected TransitionController _modalTransitionController;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The default controller, used only for instances of Locatable
     *  that have no container.
     */
    private LocatableNodeController _locatableController;

    // The selection interactor for drag-selecting nodes
    private SelectionDragger _selectionDragger;

}
