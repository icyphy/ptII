/* The edge controller for links between ports and relations.

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
	SelectionModel sm = controller.getSelectionModel();
	SelectionInteractor interactor =
            (SelectionInteractor) getEdgeInteractor();
	interactor.setSelectionModel(sm);

        // Create and set up the manipulator for connectors
        ConnectorManipulator manipulator = new ConnectorManipulator();
        manipulator.setSnapHalo(4.0);
        manipulator.addConnectorListener(new LinkDropper());
        interactor.setPrototypeDecorator(manipulator);

        // The mouse filter needs to accept regular click or control click
        MouseFilter handleFilter = new MouseFilter(1, 0, 0);
        manipulator.setHandleFilter(handleFilter);

	ConnectorTarget ct = new LinkTarget();
	setConnectorTarget(ct);
	setEdgeRenderer(new LinkRenderer());

	_menuCreator = new MenuCreator(null);
	interactor.addInteractor(_menuCreator);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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

    ///////////////////////////////////////////////////////////////////
    ////                         public classes                    ////

    public class LinkTarget extends PerimeterTarget {
        public boolean acceptHead(Connector c, Figure f) {
            Object object = f.getUserObject();
	    if(object instanceof Port) return super.acceptHead(c, f);
	    if(object instanceof Vertex) return super.acceptHead(c, f);
	    if(object instanceof Location &&
                    ((Location)object).getContainer() instanceof Port)
		return super.acceptHead(c, f);
	    return false;
        }

        public boolean acceptTail(Connector c, Figure f) {
            Object object = f.getUserObject();
	    if(object instanceof Port) return super.acceptTail(c, f);
	    if(object instanceof Vertex) return super.acceptTail(c, f);
	    if(object instanceof Location &&
                    ((Location)object).getContainer() instanceof Port)
		return super.acceptHead(c, f);
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
	// Tail sites are the same as head sites.
    }

    public static class LinkRenderer implements EdgeRenderer {
	/**
         * Render a visual representation of the given edge.
         */
        public Connector render(Object edge, Site tailSite, Site headSite) {
            AbstractConnector c = new ManhattanConnector(tailSite, headSite);
            //AbstractConnector c = new StraightConnector(tailSite, headSite);
            c.setLineWidth((float)2.0);
            c.setUserObject(edge);

	    Link link = (Link)edge;
	    Relation relation = link.getRelation();
	    if(relation != null) {
		c.setToolTipText(relation.getName());
	    }
            return c;
        }
    }

    /** An inner class that handles interactive changes to connectivity.
     */
    protected class LinkDropper extends ConnectorAdapter {
        /**
         * Called when a connector end is dropped--attach or
         * detach the edge as appropriate.
         */
        public void connectorDropped(ConnectorEvent evt) {
            Connector c = evt.getConnector();
            Figure f = evt.getTarget();
            Object edge = c.getUserObject();
            Object node = (f == null) ? null : f.getUserObject();
            PtolemyGraphModel model =
		(PtolemyGraphModel) getController().getGraphModel();
	    switch (evt.getEnd()) {
	    case ConnectorEvent.HEAD_END:
		model.getLinkModel().setHead(edge, node);
		break;
	    case ConnectorEvent.TAIL_END:
		model.getLinkModel().setTail(edge, node);
		break;
	    default:
		throw new IllegalStateException(
                        "Cannot handle both ends of an edge being dragged.");
	    }
        }
    }

    private MenuCreator _menuCreator;
}
