/* The node controller for entities (and icons)

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

package ptolemy.vergil.graph;

import ptolemy.actor.*;
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
import diva.graph.basic.*;
import diva.graph.layout.*;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import java.awt.geom.Rectangle2D;
import diva.util.Filter;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.*;
import java.net.URL;
import javax.swing.JMenuItem;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// EntityController
/**
This class provides interaction with nodes that represent Ptolemy II entities.
(Or, more specifically, with the icon that is contained in an entity.)
A layout algorithm is applied so that the figures for ports are 
automatically placed on the sides of the figure for the entity.  
Standard selection and movement interaction is
provided.  In addition, right clicking on the entity will create a context
menu for the entity.

@author Steve Neuendorffer
@version $Id$
*/
public class EntityController extends LocatableNodeController {

    /** Create an entity controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public EntityController(GraphController controller) {
	super(controller);
	setNodeRenderer(new EntityRenderer());

	SelectionModel sm = controller.getSelectionModel();
        NodeInteractor interactor =
            (NodeInteractor) getNodeInteractor();
	interactor.setSelectionModel(sm);

	VergilApplication application = VergilApplication.getInstance();
	Action action = application.getAction("Look Inside");
	ActionInteractor actionInteractor = new ActionInteractor(action);
	actionInteractor.setConsuming(false);
	actionInteractor.setMouseFilter(new MouseFilter(1, 0, 0, 2));
	interactor.addInteractor(actionInteractor);

	// FIXME this is a horrible dance so that the actioninteractor gets
	// the events before the drag interactor.
	interactor.setDragInteractor(interactor.getDragInteractor());

        // Initialize the menu creator. 
	_menuCreator = 
	    new MenuCreator(new EntityContextMenuFactory(controller));
	interactor.addInteractor(_menuCreator);

	// The filter for the layout algorithm of the ports within this
	// entity.
	Filter portFilter = new Filter() {
	    public boolean accept(Object o) {
		if(o instanceof Port) {
		    return true;
		} else {
		    return false;
		}
	    }
	};

	// Anytime we add a port to an entity, we want to layout all the
	// ports within that entity.
	GlobalLayout layout = new EntityLayout();
	controller.addGraphViewListener(new IncrementalLayoutListener(
	     new IncrLayoutAdapter(layout), portFilter));
    }

    /** This layout algorithm is responsible for laying out the ports
     * within an entity.
     */    
    public class EntityLayout extends AbstractGlobalLayout {
	public EntityLayout() {
	    super(new BasicLayoutTarget(getController()));
	}
	
	public void layout(Object node) {
	    GraphModel model = getController().getGraphModel();
	    Iterator nodes = model.nodes(node);
	    LinkedList inputs = new LinkedList();
	    LinkedList outputs = new LinkedList();
	    LinkedList inouts = new LinkedList();
	    int inCount = 0;
	    int outCount = 0;
	    int inOutCount = 0;
	    
	    while(nodes.hasNext()) {
		Port port = (Port) nodes.next();
		if(!(port instanceof IOPort)) {
		    inOutCount++;
		    inouts.addLast(port);
		} else {
		    IOPort ioport = (IOPort) port;
		    if(ioport.isInput() && ioport.isOutput()) {
			inOutCount++;
			inouts.addLast(port);
		    } else if(ioport.isInput()) {
			inCount++;
			inputs.addLast(port);
		    } else if(ioport.isOutput()) {
			outCount++;
			outputs.addLast(port);
		    }
		}
	    }
	    CompositeFigure figure = 
		(CompositeFigure)getLayoutTarget().getVisualObject(node);
	    
	    _placePortFigures(figure, inputs, inCount,
			      SwingConstants.WEST);
	    _placePortFigures(figure, outputs, outCount,
			      SwingConstants.EAST);
	    _placePortFigures(figure, inouts, inOutCount,
			      SwingConstants.SOUTH);
	    
	}
	
	private void _placePortFigures(CompositeFigure figure, List portList, 
					int count, int direction) {
	    Iterator ports = portList.iterator();
	    int number = 0;
	    while(ports.hasNext()) {
		number ++;
		Object port = ports.next();
		Figure portFigure = getController().getFigure(port);
		// If there is no figure, then ignore this port.  This may
		// happen if the port hasn't been rendered yet.
		if(portFigure == null) continue;
		Rectangle2D portBounds = 
		    portFigure.getBounds();
		BoundsSite site = 
		    new BoundsSite(figure.getBackgroundFigure(), 0, 
				   direction, 
				   100.0 * number / (count+1));
		CanvasUtilities.translateTo(portFigure, 
					    site.getX(), site.getY());
	    }		    
	}	    
    }
 
    /** Get the menu factory that will create context menus for this
     *  controller.
     */
    public MenuFactory getMenuFactory() {
        return _menuCreator.getMenuFactory();
    }

    /** Set the menu factory that will create menus for this Entity.
     */
    public void setMenuFactory(MenuFactory factory) {
        _menuCreator.setMenuFactory(factory);
    }

    /**
     * The factory for creating context menus on entities.
     */
    public static class EntityContextMenuFactory extends PtolemyMenuFactory {
	public EntityContextMenuFactory(GraphController controller) {
	    super(controller);
	    addMenuItemFactory(new EditParametersFactory());
	    addMenuItemFactory(new EditParameterStylesFactory());
	    addMenuItemFactory(new MenuActionFactory(VergilApplication.getInstance().getAction("Look Inside")));
	    addMenuItemFactory(new MenuActionFactory(VergilApplication.getInstance().getAction("Edit Icon")));
	}
    }
    
    public static class EntityRenderer implements NodeRenderer {
	public Figure render(Object n) {
	    Figure figure;
	    EditorIcon icon = (EditorIcon)n;
	    figure = icon.createFigure();
            NamedObj object = (NamedObj) icon.getContainer();
            figure.setToolTipText(object.getName());
	    return figure;
	}
    }

    private EntityPortController _portController;
    private MenuCreator _menuCreator;
}
