/* The graph controller for FSM models.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

// FIXME: Trim this.
import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.vergil.*;
import ptolemy.vergil.ptolemy.*;
import ptolemy.vergil.toolbox.*;
import ptolemy.gui.*;
import ptolemy.moml.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.graph.*;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import java.awt.geom.Rectangle2D;
import diva.util.Filter;
import java.awt.*;
import diva.util.java2d.Polygon2D;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;
import javax.swing.Action;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// FSMGraphController
/**
A Graph Controller for FSM models.  This controller allows nodes to be
dragged and dropped onto its graph. 
Arcs can be created by control-clicking and dragging on
a port.  Anything can be deleted by selecting it and pressing
the delete key on the keyboard.

@author Steve Neuendorffer
@version $Id$
 */
public class FSMGraphController extends FSMViewerController {
    /**
     * Create a new basic controller with default
     * terminal and edge interactors.
     */
    public FSMGraphController() {
	super();
    }

    // FIXME remove this methods.
    public Action getNewStateAction() {
	return _newStateAction;
    }

    /**
     * Initialize all interaction on the graph pane. This method
     * is called by the setGraphPane() method of the superclass.
     * This initialization cannot be done in the constructor because
     * the controller does not yet have a reference to its pane
     * at that time.
     */
    protected void initializeInteraction() {
        super.initializeInteraction();
        GraphPane pane = getGraphPane();

	// Create the interactor that drags new edges.
	_linkCreator = new LinkCreator();
	_linkCreator.setMouseFilter(_controlFilter);
	((CompositeInteractor)getEntityController().getNodeInteractor()).addInteractor(_linkCreator);

        // Create a listener that creates new states.
	_stateCreator = new StateCreator();
        _stateCreator.setMouseFilter(_controlFilter);
        pane.getBackgroundEventLayer().addInteractor(_stateCreator);

    }

    ///////////////////////////////////////////////////////////////
    //// LinkCreator

    /** An interactor that interactively drags edges from one terminal
     * to another.
     */
    protected class LinkCreator extends AbstractInteractor {
        public void mousePressed(LayerEvent e) {
            Figure source = e.getFigureSource();
	    NamedObj object = (NamedObj) source.getUserObject();

	    FigureLayer layer = (FigureLayer) e.getLayerSource();

	    // Create a new edge
	    CompositeEntity container = 
		(CompositeEntity)getGraphModel().getRoot();

	    Arc link;
	    try {
                link = new Arc();
            }
            catch (Exception ex) {
		MessageHandler.error("Create relation failed:", ex);
		return;
	    }
	    // Add it to the editor
	    getLinkController().addEdge(link,
                    object,
                    ConnectorEvent.TAIL_END,
                    e.getLayerX(),
                    e.getLayerY());

	    // Add it to the selection so it gets a manipulator, and
	    // make events go to the grab-handle under the mouse
	    Figure ef = getFigure(link);
	    getSelectionModel().addSelection(ef);
	    ConnectorManipulator cm =
		(ConnectorManipulator) ef.getParent();
	    GrabHandle gh = cm.getHeadHandle();
	    layer.grabPointer(e, gh);
	}
    }

    // An action to create a new relation.
    public class NewStateAction extends FigureAction {
	public NewStateAction() {
	    super("New State");
	    putValue("tooltip", "New State");
	    String dflt = "";
	    // Creating the renderers this way is rather nasty..
	    // Standard toolbar icons are 25x25 pixels.
	    //	    NodeRenderer renderer = new FSMStateController.StateRenderer();
	    //Figure figure = renderer.render(null);
	    
	    // FigureIcon icon = new FigureIcon(figure, 25, 25, 1, true);
	    //putValue(diva.gui.GUIUtilities.LARGE_ICON, icon);
	}

	public void actionPerformed(ActionEvent e) {
	    super.actionPerformed(e);
	    GraphPane pane = getGraphPane();
	    double x;
	    double y;
	    if(getSourceType() == TOOLBAR_TYPE ||
	       getSourceType() == MENUBAR_TYPE) {	
		// no location in the action, so make something up.
		// FIXME this is a lousy way to do this.
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
	    final CompositeEntity toplevel = graphModel.getToplevel();
	 		
	    final String stateName = toplevel.uniqueName("state");
	    final String iconName = "_icon";
	    // Create the state.
	    StringBuffer moml = new StringBuffer();
	    moml.append("<entity name=\"" + stateName + 
			"\" class=\"ptolemy.domains.fsm.kernel.State\">\n");
	    moml.append("<rendition name=\"" + iconName + 
	    		"\" class=\"ptolemy.vergil.toolbox.LibraryIcon\">\n");
	    moml.append("<configure>generic.state</configure>\n");
	    moml.append("</rendition>\n");
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
			
			// FIXME: Have to know whether this is an entity,
			// port, etc. For now, assuming it is an entity.
			NamedObj newObject =
			toplevel.getEntity(stateName);
			Icon icon = 
			(Icon) newObject.getAttribute(iconName);
						
			double point[] = new double[2];
			point[0] = ((int)finalX);
			point[1] = ((int)finalY);
			icon.setLocation(point);
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
    
    protected class StateCreator extends ActionInteractor {
	public StateCreator() {
	    super(_newStateAction);
	}
    }

    /** The interactor that interactively creates edges
     */
    private LinkCreator _linkCreator;

    /** The interactor that interactively creates states
     */
    private StateCreator _stateCreator;

    /** The action for creating states.
     */
    private NewStateAction _newStateAction = new NewStateAction();

    /** The filter for control operations
     */
    private MouseFilter _controlFilter = new MouseFilter(
            InputEvent.BUTTON1_MASK,
            InputEvent.CTRL_MASK);

    /** The filter for shift operations
     */
    private MouseFilter _shiftFilter = new MouseFilter(
            InputEvent.BUTTON1_MASK,
            InputEvent.SHIFT_MASK);
}





