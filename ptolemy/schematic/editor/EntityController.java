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
import javax.swing.*;
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

public class EntityController extends NodeController {
    public EntityController(GraphController controller) {
	super(controller);
	setNodeRenderer(new EntityRenderer());
	setPortController(new EntityPortController(controller));
    }
    
    public NodeController getPortController() {
	return _portController;
    }

    public void setPortController(NodeController controller) {
	_portController = (EntityPortController)controller;
    }

    public class EntityRenderer implements NodeRenderer {
	public Figure render(Node n) {
	    Figure figure;
	    NamedObj object = (NamedObj)n.getSemanticObject();
	    BasicCompositeNode node = (BasicCompositeNode) n;
	    Entity entity = (Entity)object;
            EditorIcon icon = (EditorIcon)entity.getAttribute("_icon");
            //           Figure background = new BasicRectangle(-10, -10, 20, 20, Color.red);
            //icon.createFigure();
	    Figure background = icon.createFigure(); 
	    figure = new CompositeFigure(background);
	    figure.setUserObject(n);
	    n.setVisualObject(figure); 
	    return figure;
	}
    }

    public void addNode(Node n, double x, double y) {
	
        Figure nf = getNodeRenderer().render(n);
        nf.setInteractor(getNodeInteractor());
        getController().getGraphPane().getForegroundLayer().add(nf);
        // Add to the graph
        getController().getGraphImpl().addNode(n, getController().getGraph());

	//super.addNode(n, 0, 0);	
	//System.out.println("adding node");
	
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
	//System.out.println("incount = "+ inCount);
	//System.out.println("outcount = "+ outCount);
	//System.out.println("inoutcount = "+ inOutCount);
	int nodeNumber = 0;
        
	_createPortFigures((CompositeNode)n, inputs, inCount, SwingConstants.WEST);
	_createPortFigures((CompositeNode)n, outputs, outCount, SwingConstants.EAST);
	_createPortFigures((CompositeNode)n, inouts, inOutCount, SwingConstants.SOUTH);
	// Hmm..  this translate has to come LAST.  How odd.
	CanvasUtilities.translateTo(nf, x, y);
    }
	
    public void _createPortFigures(CompositeNode node,
				   LinkedList nodeList, int count, 
				   int direction) {        
	int nodeNumber = 0;
	Iterator nodes = nodeList.iterator();            
	while(nodes.hasNext()) {	    
	    nodeNumber ++;
	    Node portNode = (Node) nodes.next();
	    _portController.addNode(portNode, node, direction, 
	    		    100.0*nodeNumber/(count+1));
	}
    }  

    EntityPortController _portController;
}









