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
It contains a node controller for the ports that the entity contains, and when
it draws an entity, it defers to that controller to draw the ports.  The
figures for ports are automatically placed on the left and right side of the
figure for the entity.  Standard selection and movement interaction is
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
	setPortController(new EntityPortController(controller));

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
    }

    /** Draw the node and all the ports contained within the node.
     */
    public Figure drawNode(Object n) {
        Figure nf = super.drawNode(n);
	LinkedList inputs = new LinkedList();
	LinkedList outputs = new LinkedList();
	LinkedList inouts = new LinkedList();
	int inCount = 0;
	int outCount = 0;
	int inOutCount = 0;

	Iterator nodes = getController().getGraphModel().nodes(n);
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

	int nodeNumber = 0;

	_createPortFigures((Icon)n, inputs, inCount,
                SwingConstants.WEST);
	_createPortFigures((Icon)n, outputs, outCount,
                SwingConstants.EAST);
	_createPortFigures((Icon)n, inouts, inOutCount,
                SwingConstants.SOUTH);
        return nf;
    }

    /** Get the menu factory that will create context menus for this
     *  controller.
     */
    public MenuFactory getMenuFactory() {
        return _menuCreator.getMenuFactory();
    }

    /** Get the controller for the ports of this entity.
     */
    public NodeController getPortController() {
	return _portController;
    }

    /**
     * Remove all the ports in this entity, all the edges connected to those
     * ports and this node.
     */
    public void removeNode(Object node) {
	// This code sucks because we need a list iterator.
	List nodeList = new LinkedList();
	Iterator i = getController().getGraphModel().nodes(node);
	while(i.hasNext()) {
	    nodeList.add(i.next());
	}
	Object nodes[] = nodeList.toArray();
	for(int j = 0; j < nodes.length; j++) {
	    _portController.removeNode(nodes[j]);
	}
	super.removeNode(node);
    }

    /** Set the menu factory that will create menus for this Entity.
     */
    public void setMenuFactory(MenuFactory factory) {
        _menuCreator.setMenuFactory(factory);
    }

    /** Set the controller for the ports of this entity.
     */
    public void setPortController(NodeController controller) {
	_portController = (EntityPortController)controller;
    }

    /** Create figures for each node in the node list.  Place them on the
     * side of the composite given by direction.
     * Count must be the number of nodes in the list.
     */
    protected void _createPortFigures(Icon container,
            LinkedList nodeList, int count,
            int direction) {
	int nodeNumber = 0;
	Iterator nodes = nodeList.iterator();
	while(nodes.hasNext()) {
	    nodeNumber ++;
	    Port port = (Port) nodes.next();
	    _portController.drawNode(port, container, direction,
                    100.0*nodeNumber/(count+1));
	}
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
