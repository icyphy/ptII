/* The edge controller for links between ports and relations.

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
import ptolemy.actor.gui.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.gui.*;
import ptolemy.moml.*;
import ptolemy.vergil.*;
import ptolemy.vergil.toolbox.*;
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
This class provides interaction techniques for edges that are to be connected
between ports and relations.  Standard interaction techniques for an
undirected edge are allowed.

@author Steve Neuendorffer
@version $Id$
*/
public class LinkController extends EdgeController {
    public LinkController(GraphController controller) {
	super(controller);
	// Create and set up the target for connectors
	// This is wierd...  we want 2 targets, one for head and port,
	// one for tail and vertex.
	ConnectorTarget ct = new LinkTarget();
	setConnectorTarget(ct);
	setEdgeRenderer(new LinkRenderer());

	// Create and set up the manipulator for connectors
	EdgeInteractor interactor = (EdgeInteractor)getEdgeInteractor();
	BasicSelectionRenderer selectionRenderer = (BasicSelectionRenderer)
	    interactor.getSelectionRenderer();
	ConnectorManipulator manipulator = (ConnectorManipulator)
	    selectionRenderer.getDecorator();
	manipulator.setConnectorTarget(ct);
		
	_menuCreator = new MenuCreator(new LinkContextMenuFactory());
	interactor.addInteractor(_menuCreator);
    }

    public class LinkTarget extends PerimeterTarget {
        public boolean acceptHead(Connector c, Figure f) {
            Object object = f.getUserObject();
            if(object instanceof Node) {
                Node node = (Node) object;
                object = node.getSemanticObject();
                if(object instanceof Port) return super.acceptHead(c, f);
                if(object instanceof Vertex) return super.acceptHead(c, f);
            }
            return false;
        }

        public boolean acceptTail(Connector c, Figure f) {
            Object object = f.getUserObject();
            if(object instanceof Node) {
                Node node = (Node) object;
                object = node.getSemanticObject();
                if(object instanceof Port) return super.acceptTail(c, f);
                if(object instanceof Vertex) return super.acceptTail(c, f);
            }
            return false;
        }

        public Site getHeadSite(Figure f, double x, double y) {
            if(f instanceof Terminal) {
		Site site = ((Terminal)f).getConnectSite();
		return site;
            } else {
                return super.getHeadSite(f, x, y);
            }
        }
    }

    /**
     * The factory for creating context menus on relations
     */
    public static class LinkContextMenuFactory 
	extends RelationController.RelationContextMenuFactory {
	
	public NamedObj _getObjectFromFigure(Figure source) {
	    Edge edge = (Edge) source.getUserObject();
	    Relation relation = (Relation)edge.getSemanticObject();
	    if(relation == null) {
		Node node = edge.getHead();
		Object object = node.getSemanticObject();
		if(!(object instanceof Vertex)) {
		    node = edge.getTail();
		    object = node.getSemanticObject();
		}
		if(object != null && object instanceof Vertex) {
		    Vertex vertex = (Vertex) object;
		    relation = (Relation)vertex.getContainer();
		}
	    }
	    return relation;
	}
    }

    public class LinkRenderer implements EdgeRenderer {
	/**
         * Render a visual representation of the given edge.
         */
        public Connector render(Edge edge, Site tailSite, Site headSite) {
            AbstractConnector c = new ManhattanConnector(tailSite, headSite);
            c.setLineWidth((float)2.0);
            c.setUserObject(edge);
            return c;
        }
    }
    private MenuCreator _menuCreator;
}
