/* The graph controller for FSM models.

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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.Site;
import diva.canvas.connector.AutonomousSite;
import diva.canvas.connector.Connector;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.AbstractInteractor;
import diva.canvas.interactor.ActionInteractor;
import diva.canvas.interactor.CompositeInteractor;
import diva.canvas.interactor.GrabHandle;
import diva.graph.GraphException;
import diva.graph.GraphPane;

import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Location;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.ptolemy.kernel.AttributeController;
import ptolemy.vergil.ptolemy.kernel.PortController;
import ptolemy.vergil.toolbox.FigureAction;

//////////////////////////////////////////////////////////////////////////
//// FSMGraphController
/**
A Graph Controller for FSM models.  This controller allows states to be
dragged and dropped onto its graph. Arcs can be created by
control-clicking and dragging from one state to another.

@author Steve Neuendorffer
@contributor Edward A. Lee
@version $Id$
 */
public class FSMGraphController extends FSMViewerController {

    /** Create a new basic controller with default
     *  terminal and edge interactors.
     */
    public FSMGraphController() {
	super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add commands to the specified menu and toolbar, as appropriate
     *  for this controller.  In this class, commands are added to create
     *  ports and relations.
     *  @param menu The menu to add to, or null if none.
     *  @param toolbar The toolbar to add to, or null if none.
     */
    public void addToMenuAndToolbar(JMenu menu, JToolBar toolbar) {
        super.addToMenuAndToolbar(menu, toolbar);
        // Add an item that adds new states.
	diva.gui.GUIUtilities.addMenuItem(menu, _newStateAction);
        // To get a new-state item on the toolbar, uncomment this:
        // diva.gui.GUIUtilities.addToolBarButton(toolbar, _newStateAction);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the controllers for nodes in this graph.
     *  In this class, controllers with FULL access are created.
     *  This is called by the constructor, so derived classes that
     *  override this must be careful not to reference local variables
     *  defined in the derived classes, because the derived classes
     *  will not have been fully constructed by the time this is called.
     */
    protected void _createControllers() {
	_attributeController = new AttributeController(this,
                 AttributeController.FULL);
	_portController = new PortController(this,
                 AttributeController.FULL);
	_stateController = new FSMStateController(this,
                 AttributeController.FULL);
	_transitionController = new FSMTransitionController(this);
    }

    /** Initialize interaction on the graph pane. This method
     *  is called by the setGraphPane() method of the superclass.
     *  This initialization cannot be done in the constructor because
     *  the controller does not yet have a reference to its pane
     *  at that time.
     */
    protected void initializeInteraction() {
        super.initializeInteraction();
        GraphPane pane = getGraphPane();

	// Create the interactor that drags new edges.
	_linkCreator = new LinkCreator();
	_linkCreator.setMouseFilter(_controlFilter);
	((CompositeInteractor)_stateController
                .getNodeInteractor()).addInteractor(_linkCreator);

        // Create a listener that creates new states.
	_stateCreator = new StateCreator();
        _stateCreator.setMouseFilter(_controlFilter);
        pane.getBackgroundEventLayer().addInteractor(_stateCreator);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The interactor that interactively creates edges. */
    private LinkCreator _linkCreator;

    /** The interactor that interactively creates states. */
    private StateCreator _stateCreator;

    /** The action for creating states. */
    private NewStateAction _newStateAction = new NewStateAction();

    /** The filter for control operations. */
    private MouseFilter _controlFilter = new MouseFilter(
            InputEvent.BUTTON1_MASK,
            InputEvent.CTRL_MASK);

    /** The filter for shift operations. */
    private MouseFilter _shiftFilter = new MouseFilter(
            InputEvent.BUTTON1_MASK,
            InputEvent.SHIFT_MASK);

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////
    //// LinkCreator

    /** An interactor that interactively drags edges from one terminal
     *  to another.
     */
    protected class LinkCreator extends AbstractInteractor {

        /** Initiate creation of an arc. */
        public void mousePressed(LayerEvent event) {
            Figure source = event.getFigureSource();
	    NamedObj sourceObject = (NamedObj) source.getUserObject();

	    Arc link = new Arc();

	    // Set the tail, going through the model so the link is added
	    // to the list of links.
            FSMGraphModel model = (FSMGraphModel)getGraphModel();
	    model.getArcModel().setTail(link, sourceObject);

            try {
		// add it to the foreground layer.
		FigureLayer layer =
		    getGraphPane().getForegroundLayer();
		Site headSite, tailSite;

		// Temporary sites.  One of these will get removed later.
		headSite = new AutonomousSite(layer,
                        event.getLayerX(),
                        event.getLayerY());
		tailSite = new AutonomousSite(layer,
                        event.getLayerX(),
                        event.getLayerY());
		// Render the edge.
		Connector c = getEdgeController(link).render(
                        link, layer, tailSite, headSite);
		// get the actual attach site.
		tailSite = getEdgeController(link)
                        .getConnectorTarget().getTailSite(c, source,
                        event.getLayerX(),
                        event.getLayerY());
		if(tailSite == null) {
		    throw new RuntimeException("Invalid connector target: " +
                            "no valid site found for tail of new connector.");
		}

		// And reattach the connector.
		c.setTailSite(tailSite);

		// Add it to the selection so it gets a manipulator, and
		// make events go to the grab-handle under the mouse
		Figure ef = getFigure(link);
		getSelectionModel().addSelection(ef);
		ConnectorManipulator cm =
		        (ConnectorManipulator) ef.getParent();
		GrabHandle gh = cm.getHeadHandle();
		layer.grabPointer(event, gh);
            } catch (Exception ex) {
                MessageHandler.error("Drag connection failed:", ex);
            }
	}
    }

    ///////////////////////////////////////////////////////////////
    //// NewStateAction

    /** An action to create a new state. */
    public class NewStateAction extends FigureAction {

        /** Construct a new action. */
	public NewStateAction() {
	    super("New State");
	    putValue("tooltip", "New State");
	    String dflt = "";
            // If we want a new-state item in the toolbar, uncomment this:
            /*
	    NodeRenderer renderer = new FSMStateController.StateRenderer();
	    Figure figure = renderer.render(null);
	    // Standard toolbar icons are 25x25 pixels.
	    FigureIcon icon = new FigureIcon(figure, 25, 25, 1, true);
	    putValue(diva.gui.GUIUtilities.LARGE_ICON, icon);
            */
	    putValue("tooltip", "Control-click to create a new state.");
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_S));
	}

        /** Execute the action. */
	public void actionPerformed(ActionEvent e) {
	    super.actionPerformed(e);
	    GraphPane pane = getGraphPane();
	    double x;
	    double y;
	    if(getSourceType() == TOOLBAR_TYPE ||
                    getSourceType() == MENUBAR_TYPE) {
		// No location in the action, so make something up.
		// NOTE: is there a better way to do this?
		Point2D point = pane.getSize();
		x = point.getX()/2;
		y = point.getY()/2;
	    } else {
		x = getX();
		y = getY();
	    }

	    FSMGraphModel graphModel =
		(FSMGraphModel)getGraphModel();
	    final double finalX = x;
	    final double finalY = y;
	    final CompositeEntity toplevel = graphModel.getPtolemyModel();

	    final String stateName = toplevel.uniqueName("state");
	    // Create the state.
	    StringBuffer moml = new StringBuffer();
	    final String locationName = "location1";
            moml.append("<entity name=\"" + stateName +
                    "\" class=\"ptolemy.domains.fsm.kernel.State\">\n");
	    moml.append("<property name=\"" + locationName +
                    "\" class=\"ptolemy.moml.Location\"/>\n");
	    moml.append("</entity>\n");


	    ChangeRequest request =
		new MoMLChangeRequest(this, toplevel, moml.toString()) {
                protected void _execute() throws Exception {
                    super._execute();
                    // Set the location of the icon.
                    // Note that this really needs to be done after
                    // the change request has succeeded, which is why
                    // it is done here.  When the graph controller
                    // gets around to handling this, it will draw
                    // the icon at this location.

                    // NOTE: Have to know whether this is an entity,
                    // port, etc. Since this is a state, it is safe
                    // to assume it is an entity.
                    NamedObj newObject =
			toplevel.getEntity(stateName);
                    Location location =
			(Location) newObject.getAttribute(locationName);

                    double point[] = new double[2];
                    point[0] = ((int)finalX);
                    point[1] = ((int)finalY);
                    location.setLocation(point);
                }
            };
	    toplevel.requestChange(request);
	    try {
		request.waitForCompletion();
	    } catch (Exception ex) {
		throw new GraphException(ex);
	    }
	}
    }

    ///////////////////////////////////////////////////////////////
    //// State Creator

    /** An interactor for the new state action.*/
    protected class StateCreator extends ActionInteractor {
	public StateCreator() {
	    super();
            setAction(_newStateAction);
	}
    }
}
