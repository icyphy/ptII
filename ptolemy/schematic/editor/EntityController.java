/* The graph controller for the ptolemy schematic editor entities

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.schematic.editor;

import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.schematic.util.*;
import ptolemy.schematic.xml.*;
import ptolemy.gui.*;
import ptolemy.moml.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.graph.*; 
import diva.graph.model.*;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import java.awt.geom.Rectangle2D;
import diva.util.Filter;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// EntityController
/**
 * A Graph Controller for the Ptolemy II schematic editor.  
 * Terminal creation: Ctrl-button 1
 * Edge creation: Ctrl-Button 1 Drag
 * Entity creation: Shift-Button 1
 * Edges can connect to Terminals, but not entities.
 *
 * @author Steve Neuendorffer 
 * @version $Id$
 */

public class EntityController extends LocatableNodeController {
    public EntityController(GraphController controller) {
	super(controller);
	setNodeRenderer(new EntityRenderer());
	setPortController(new EntityPortController(controller));
	
	SelectionModel sm = controller.getSelectionModel();
        SelectionInteractor interactor = 
            (SelectionInteractor) getNodeInteractor();
	interactor.setSelectionModel(sm);

	_menuCreator = new MenuCreator(new EntityContextMenuFactory());
	interactor.addInteractor(_menuCreator);
    }
    
    /** Create a new node with the given semantic object and
     *  add it to the graph.  Draw the node at the given location.
     */
    public Node addNode(Object semanticObject, double x, double y) {
        GraphController controller = getController();
        Node n = controller.getGraphImpl().createCompositeNode(
                semanticObject);

        // Add to the graph
        controller.getGraphImpl().addNode(n, controller.getGraph());
        Figure nf = drawNode(n);
	CanvasUtilities.translateTo(nf, x, y);        
        return n;             
    }
	
    /** Draw the node and all the ports contained within the node.
     */
    public Figure drawNode(Node n) {
        Figure nf = super.drawNode(n);
	LinkedList inputs = new LinkedList();
	LinkedList outputs = new LinkedList();
	LinkedList inouts = new LinkedList();
	int inCount = 0;
	int outCount = 0;
	int inOutCount = 0;
                                                  
	Iterator nodes = ((CompositeNode) n).nodes();
	while(nodes.hasNext()) {
	    Node portNode = (Node) nodes.next();
	    Port port = (Port) portNode.getSemanticObject();
	    if(!(port instanceof IOPort)) {
		inOutCount++;
		inouts.addLast(portNode);
	    } else {
		IOPort ioport = (IOPort) port;
		if(ioport.isInput() && ioport.isOutput()) {
		    inOutCount++;
		    inouts.addLast(portNode);
		} else if(ioport.isInput()) {
		    inCount++;
		    inputs.addLast(portNode);
		} else if(ioport.isOutput()) {
		    outCount++;
		    outputs.addLast(portNode);
		}
	    }
	}

	int nodeNumber = 0;
        
	_createPortFigures((CompositeNode)n, inputs, inCount, 
			   SwingConstants.WEST);
	_createPortFigures((CompositeNode)n, outputs, outCount, 
			   SwingConstants.EAST);
	_createPortFigures((CompositeNode)n, inouts, inOutCount,
			   SwingConstants.SOUTH);
        return nf;
    }

    /** Get the controller for the ports of this entity.
     */
    public NodeController getPortController() {
	return _portController;
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
    protected void _createPortFigures(CompositeNode node,
				   LinkedList nodeList, int count, 
				   int direction) {        
	int nodeNumber = 0;
	Iterator nodes = nodeList.iterator();            
	while(nodes.hasNext()) {	    
	    nodeNumber ++;
	    Node portNode = (Node) nodes.next();
	    _portController.drawNode(portNode, node, direction, 
	    		    100.0*nodeNumber/(count+1));
	}
    }  
    
    /**
     * The factory for creating context menus on entities.
     */
    public class EntityContextMenuFactory extends MenuFactory {
	public JPopupMenu create(Figure source) {
	    Node sourcenode = (Node) source.getUserObject();
	    Icon icon = (Icon)sourcenode.getSemanticObject();
	    NamedObj object = (NamedObj) icon.getContainer();
	    return new Menu(object);
	}    
	
	public class Menu extends BasicContextMenu {
	    public Menu(NamedObj target) {
		super(target);
	    }
	}
    }
	
    public class EntityRenderer implements NodeRenderer {
	public Figure render(Node n) {
	    Figure figure;
	    EditorIcon icon = (EditorIcon)n.getSemanticObject();
	    return icon.createFigure();
	}
    }

    EntityPortController _portController;
    MenuCreator _menuCreator;
}




