 /* The controller for link edges

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
import ptolemy.actor.gui.*;
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
import diva.util.java2d.Polygon2D;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// LinkController
/**
 * A Controller for link edges.
 *
 * @author Steve Neuendorffer 
 * @version $Id$
 */
public class LinkController extends EdgeController {
    public LinkController(GraphController controller) {
	super(controller);
	// Create and set up the target for connectors
	// This is wierd...  we want 2 targets, one for head and port, 
	// one for tail and vertex.
	ConnectorTarget ct = new LinkTarget();
	
	setEdgeRenderer(new LinkRenderer());
	
	// Create and set up the manipulator for connectors
	EdgeInteractor interactor = (EdgeInteractor)getEdgeInteractor();
	BasicSelectionRenderer selectionRenderer = (BasicSelectionRenderer)
	    interactor.getSelectionRenderer();
	ConnectorManipulator manipulator = (ConnectorManipulator) 
	    selectionRenderer.getDecorator();
	manipulator.setConnectorTarget(ct);
	//	    manipulator.addConnectorListener(new EdgeDropper());
	//getEdgeInteractor().setPrototypeDecorator(manipulator);
	
	//    MouseFilter handleFilter = new MouseFilter(1, 0, 0);
	//manipulator.setHandleFilter(handleFilter);
	
	// FIXME links should have context menus as well
	//	    EdgeInteractor interactor = 
	//(EdgeInteractor)getEdgeInteractor();
	//new MenuCreator(interactor);
    }
    
    // FIXME this should be PerimeterTarget, but it doesn't support non-
    // rectangular shapes yet.
    public class LinkTarget extends CenterTarget {
        public boolean accept (Figure f) {
            Object object = f.getUserObject();
            if(object instanceof Node) {
                Node node = (Node) object;
                object = node.getSemanticObject();
                if(object instanceof Port) return true;
                if(object instanceof Vertex) return true;
            }
            return false;
        }
        
        public Site getHeadSite (Figure f, double x, double y) {
            if(f instanceof StraightTerminal) {
		return ((Terminal)f).getConnectSite();
            } else {
                return super.getHeadSite(f, x, y);
            }
        }        
        ConnectorTarget _vertexTarget;
    }
             
    /** An interactor that creates context-sensitive menus.
     */
    protected class MenuCreator extends AbstractInteractor {
	public MenuCreator(CompositeInteractor interactor) {
	    interactor.addInteractor(this);
	    setMouseFilter(new MouseFilter(3));
	}
	
	public void mousePressed(LayerEvent e) {
	    Figure source = e.getFigureSource();
	    Edge sourcenode = (Edge) source.getUserObject();
	    NamedObj object = (NamedObj) sourcenode.getSemanticObject();
	    JPopupMenu menu = 
		new RelationController.RelationContextMenu(object);
	    menu.show(getController().getGraphPane().getCanvas(),
		      e.getX(), e.getY());
	}
    }

    public class LinkRenderer implements EdgeRenderer {
	/**
         * Render a visual representation of the given edge.
         */
        public Connector render(Edge edge, Site tailSite, Site headSite) {
            StraightConnector c = new StraightConnector(tailSite, headSite);
            c.setUserObject(edge);
            //            Arrowhead arrow = new Arrowhead(
            //        headSite.getX(), headSite.getY(), headSite.getNormal());
            //c.setHeadEnd(arrow);
            // Add to the view and model
            c.setUserObject(edge);
            edge.setVisualObject(c);
            return c;
        }
    }
}
