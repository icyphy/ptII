/* The graph controller for vergil.

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

package ptolemy.vergil.ptolemy.kernel;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import javax.swing.Action;
import javax.swing.KeyStroke;
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
import diva.graph.NodeRenderer;
import diva.gui.toolbox.FigureIcon;
import diva.gui.toolbox.MenuCreator;

import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.moml.Location;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.Vertex;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.SnapConstraint;

//////////////////////////////////////////////////////////////////////////
//// EditorGraphController
/**
A Graph Controller for the Ptolemy II schematic editor.  In addition to the
interaction allowed in the viewer, this controller allows nodes to be
dragged and dropped onto its graph.  Relations can be created by
control-clicking on the background.  Links can be created by control-clicking
and dragging on a port or a relation.  In addition links can be created by
clicking and dragging on the ports that are inside an entity.
Anything can be deleted by selecting it and pressing
the delete key on the keyboard.

@author Steve Neuendorffer
@contributor Edward A. Lee
@version $Id$
 */
public class EditorGraphController extends ViewerGraphController {

    /** Create a new basic controller with default
     *  terminal and edge interactors.
     */
    public EditorGraphController() {
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
        // Add an item that adds new ports.
	diva.gui.GUIUtilities.addMenuItem(menu, _newPortAction);
       	diva.gui.GUIUtilities.addToolBarButton(toolbar, _newPortAction);

        // Add an item that adds new relations.
	diva.gui.GUIUtilities.addMenuItem(menu, _newRelationAction);
	diva.gui.GUIUtilities.addToolBarButton(toolbar, _newRelationAction);
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
	_entityController = new EntityController(this,
                 AttributeController.FULL);
	_entityPortController = new EntityPortController(this,
                 AttributeController.FULL);
	_portController = new PortController(this,
                 AttributeController.FULL);
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
        super.initializeInteraction();
        GraphPane pane = getGraphPane();

        // Add a menu command to configure the ports.
	_menuFactory.addMenuItemFactory(new PortDialogFactory());

        // Create a listener that creates new relations
	_relationCreator = new RelationCreator();
        _relationCreator.setMouseFilter(_controlFilter);
        pane.getBackgroundEventLayer().addInteractor(_relationCreator);

        // Create the interactor that drags new edges.
	_linkCreator = new LinkCreator();
	_linkCreator.setMouseFilter(_controlFilter);
	((CompositeInteractor)_portController.getNodeInteractor())
                .addInteractor(_linkCreator);
        ((CompositeInteractor)_entityPortController.getNodeInteractor())
                .addInteractor(_linkCreator);
	((CompositeInteractor)_relationController.getNodeInteractor())
                .addInteractor(_linkCreator);

	LinkCreator linkCreator2 = new LinkCreator();
	linkCreator2.setMouseFilter(
                new MouseFilter(InputEvent.BUTTON1_MASK,0));
	((CompositeInteractor)_entityPortController.getNodeInteractor())
                .addInteractor(linkCreator2);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The filter for control operations. */
    private MouseFilter _controlFilter = new MouseFilter(
            InputEvent.BUTTON1_MASK,
            InputEvent.CTRL_MASK);

    /** The interactor that interactively creates edges. */
    private LinkCreator _linkCreator;

    /** Action for creating a new port. */
    private Action _newPortAction = new NewPortAction();

    /** Action for creating a new relation. */
    private Action _newRelationAction = new NewRelationAction();

    /** The interactor for creating new relations. */
    private RelationCreator _relationCreator;

    /** The filter for shift operations. */
    private MouseFilter _shiftFilter = new MouseFilter(
            InputEvent.BUTTON1_MASK,
            InputEvent.SHIFT_MASK);

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////
    //// LinkCreator

    /** This class is an interactor that interactively drags edges from
     *  one terminal to another, creating a link to connect them.
     */
    protected class LinkCreator extends AbstractInteractor {

        /** Create a new edge when the mouse is pressed. */
        public void mousePressed(LayerEvent event) {
	    Figure source = event.getFigureSource();
            NamedObj sourceObject = (NamedObj) source.getUserObject();

	    // Create the new edge.
	    Link link = new Link();
	    // Set the tail, going through the model so the link is added
	    // to the list of links.
            PtolemyGraphModel model = (PtolemyGraphModel)getGraphModel();
            model.getLinkModel().setTail(link, sourceObject);

            try {
		// add it to the foreground layer.
		FigureLayer layer =
		    getGraphPane().getForegroundLayer();
		Site headSite, tailSite;

		// Temporary sites.  One of these will get blown away later.
		headSite = new AutonomousSite(layer,
                        event.getLayerX(),
                        event.getLayerY());
		tailSite = new AutonomousSite(layer,
                        event.getLayerX(),
                        event.getLayerY());
		// Render the edge.
                Connector c = getEdgeController(link)
                        .render(link, layer, tailSite, headSite);
		// get the actual attach site.
		tailSite =
		    getEdgeController(link).getConnectorTarget()
                            .getTailSite(c, source,
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
		getSelectionModel().addSelection(c);
                ConnectorManipulator cm =
                    (ConnectorManipulator) c.getParent();
                GrabHandle gh = cm.getHeadHandle();
                layer.grabPointer(event, gh);
            } catch (Exception ex) {
                MessageHandler.error("Drag connection failed:", ex);
            }
	}
    }

    ///////////////////////////////////////////////////////////////
    //// NewPortAction

    // An action to create a new port.
    public class NewPortAction extends FigureAction {
	public NewPortAction() {
	    super("New External Port");
	    String dflt = "";
	    // Creating the renderers this way is rather nasty..
	    // Standard toolbar icons are 25x25 pixels.
	    NodeRenderer renderer = _portController.getNodeRenderer();
	    Figure figure = renderer.render(null);

	    FigureIcon icon = new FigureIcon(figure, 25, 25, 1, true);
	    putValue(diva.gui.GUIUtilities.LARGE_ICON, icon);
	    putValue("tooltip", "Create a New External Port");
	    putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_E,
                            java.awt.Event.CTRL_MASK));
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_E));
	}

	public void actionPerformed(ActionEvent e) {
	    super.actionPerformed(e);
	    GraphPane pane = getGraphPane();
	    double x;
	    double y;
	    if(getSourceType() == TOOLBAR_TYPE ||
                    getSourceType() == MENUBAR_TYPE) {
		// no location in the action, so make something up.
		Point2D point = pane.getSize();
		x = point.getX()/2;
		y = point.getY()/2;
	    } else {
		x = getX();
		y = getY();
	    }

	    PtolemyGraphModel graphModel = (PtolemyGraphModel)getGraphModel();
            // FIXME: This is not sufficient for some reason to get proper
            // snap to grid.
            final double[] point = SnapConstraint.constrainPoint(x, y);
	    final CompositeEntity toplevel = graphModel.getPtolemyModel();
	    final String portName = toplevel.uniqueName("port");
	    final String locationName = "location1";
	    // Create the port.
	    StringBuffer moml = new StringBuffer();
	    moml.append("<port name=\"" + portName + "\">\n");
	    moml.append("<property name=\"" + locationName +
                    "\" class=\"ptolemy.moml.Location\"/>\n");
	    moml.append("</port>");

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
                    NamedObj newObject = toplevel.getPort(portName);
                    Location location =
			     (Location) newObject.getAttribute(locationName);
                    location.setLocation(point);
                }
            };
	    toplevel.requestChange(request);
	    try {
		request.waitForCompletion();
	    } catch (Exception ex) {
		ex.printStackTrace();
		throw new GraphException(ex);
	    }
	}
    }

    ///////////////////////////////////////////////////////////////
    //// NewRelationAction

    // An action to create a new relation.
    public class NewRelationAction extends FigureAction {
	public NewRelationAction() {
	    super("New Relation");
	    String dflt = "";
	    // Creating the renderers this way is rather nasty..
	    // Standard toolbar icons are 25x25 pixels.
	    NodeRenderer renderer = _relationController.getNodeRenderer();
	    Figure figure = renderer.render(null);

	    FigureIcon icon = new FigureIcon(figure, 25, 25, 1, true);
	    putValue(diva.gui.GUIUtilities.LARGE_ICON, icon);
	    putValue("tooltip", "Control-click to create a new relation");
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_R));
	}

	public void actionPerformed(ActionEvent e) {
	    super.actionPerformed(e);
	    GraphPane pane = getGraphPane();
	    double x;
	    double y;
	    if(getSourceType() == TOOLBAR_TYPE ||
                    getSourceType() == MENUBAR_TYPE) {
		// No location in the action, so put it in the middle.
		Point2D point = pane.getSize();
		x = point.getX()/2;
		y = point.getY()/2;
	    } else {
		x = getX();
		y = getY();
	    }

	    PtolemyGraphModel graphModel = (PtolemyGraphModel)getGraphModel();
            final double[] point = SnapConstraint.constrainPoint(x, y);
	    final CompositeEntity toplevel = graphModel.getPtolemyModel();

	    final String relationName = toplevel.uniqueName("relation");
	    final String vertexName = "vertex1";
	    // Create the relation.
	    StringBuffer moml = new StringBuffer();
	    moml.append("<relation name=\"" + relationName + "\">\n");
	    moml.append("<vertex name=\"" + vertexName + "\"/>\n");
	    moml.append("</relation>");

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
                    NamedObj newObject = toplevel.getRelation(relationName);
                    Vertex vertex = (Vertex) newObject.getAttribute(vertexName);
                    vertex.setLocation(point);
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
    //// RelationCreator

    /** An interactor for creating relations upon control clicking.
     */
    protected class RelationCreator extends ActionInteractor {
	public RelationCreator() {
	    super();
            setAction(_newRelationAction);
	}
    }
}
