/* The graph controller for the vergil viewer for finite state machines.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

package ptolemy.vergil.ptolemy.fsm;

import diva.canvas.interactor.SelectionDragger;
import diva.canvas.interactor.SelectionInteractor;
import diva.graph.EdgeController;
import diva.graph.GraphPane;
import diva.graph.NodeController;

import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.moml.Location;
import ptolemy.vergil.ptolemy.kernel.PortController;
import ptolemy.vergil.ptolemy.kernel.PtolemyGraphController;

//////////////////////////////////////////////////////////////////////////
//// FSMViewerController
/**
A graph controller for the Ptolemy II finite-state machine viewer.
This controller allows states to be moved and context menus to be accessed,
but does not provide interaction for adding or removing states or
transitions.

@author Steve Neuendorffer
@contributor Edward A. Lee
@version $Id$
*/
public class FSMViewerController extends PtolemyGraphController {

    /** Create a new controller with default port, state, and transition
     *  controllers.
     */
    public FSMViewerController() {
        _portController = new PortController(this);
	_stateController = new FSMStateController(this);
	_transitionController = new FSMTransitionController(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the node controller appropriate for the given node.
     */
    public NodeController getNodeController(Object object) {
	if(object instanceof Location &&
                getGraphModel().getSemanticObject(object)
                instanceof Entity) {
            return _stateController;
	} else if(object instanceof Location &&
                getGraphModel().getSemanticObject(object)
                instanceof Port) {
            return _portController;
	} else
            throw new RuntimeException(
                    "Node with unknown semantic object: " + object);
    }

    /**
     * Return the edge controller appropriate for the given node.
     */
    public EdgeController getEdgeController(Object edge) {
        return _transitionController;
    }

    /** Set the configuration.  This is may be used by derived controllers
     *  to open files or URLs.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);
        _portController.setConfiguration(configuration);
        _stateController.setConfiguration(configuration);
        _transitionController.setConfiguration(configuration);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Initialize all interaction on the graph pane. This method
     *  is called by the setGraphPane() method of the superclass.
     *  This initialization cannot be done in the constructor because
     *  the controller does not yet have a reference to its pane
     *  at that time.
     */
    protected void initializeInteraction() {
        GraphPane pane = getGraphPane();

        // Create and set up the selection dragger
	SelectionDragger _selectionDragger = new SelectionDragger(pane);
        _selectionDragger.addSelectionInteractor(
                (SelectionInteractor)_portController.getNodeInteractor());
	_selectionDragger.addSelectionInteractor(
                (SelectionInteractor)_stateController.getNodeInteractor());
	_selectionDragger.addSelectionInteractor(
                (SelectionInteractor)_transitionController.getEdgeInteractor());

        super.initializeInteraction();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The port controller. */
    protected PortController _portController;

    /** The state controller. */
    protected FSMStateController _stateController;

    /** The transition controller. */
    protected FSMTransitionController _transitionController;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The selection interactor for drag-selecting nodes
    private SelectionDragger _selectionDragger;
}
