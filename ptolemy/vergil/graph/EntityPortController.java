/* The node controller for ports contained in entities.

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
import diva.util.java2d.Polygon2D;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;
import javax.swing.JMenuItem;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// EntityPortController
/**
A controller for ports of entities.  Left clicking selects the port, but
dragging is not allowed (since the ports should remain attached to their
entity).  Right clicking on the port will create a context menu for the port.

@author Steve Neuendorffer
@version $Id$
*/
public class EntityPortController extends NodeController {
    public EntityPortController(GraphController controller) {
	super(controller);
	setNodeRenderer(new EntityPortRenderer());
	//SelectionModel sm = controller.getSelectionModel();
	// Ports of entities do not use the same selection model as
	// the rest of the first level figures.
	// If this is allowed, then the port can be deleted.
	CompositeInteractor interactor = new CompositeInteractor();
 	setNodeInteractor(interactor);
	_menuCreator = new MenuCreator(new PortContextMenuFactory(controller));
	interactor.addInteractor(_menuCreator);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Given a node, add it to the given parent.
     */
    public void drawNode(Port port, Icon parent, int direction,
            double fraction) {

	GraphModel model = getController().getGraphModel();

        // Create a figure for it
	//System.out.println("adding port");
	Figure figure = getNodeRenderer().render(port);
	double normal = CanvasUtilities.getNormal(direction);
	
	Site tsite = new PerimeterSite(figure, 0);
	tsite.setNormal(normal);
	tsite = new FixedNormalSite(tsite);
	figure = new TerminalFigure(figure, tsite);

        figure.setInteractor(getNodeInteractor());
        figure.setUserObject(port);
        getController().setFigure(port, figure);
        CompositeFigure parentFigure =
	    (CompositeFigure)getController().getFigure(parent);
	BoundsSite site =
	    new BoundsSite(parentFigure.getBackgroundFigure(), 0,
                    direction, fraction);

	figure.translate(site.getX() -
                parentFigure.getBackgroundFigure().getBounds().getX(),
                site.getY() -
                parentFigure.getBackgroundFigure().getBounds().getY());

	parentFigure.add(figure);
    }

    public static class EntityPortRenderer implements NodeRenderer {
	public Figure render(Object n) {
	    Port port = (Port)n;
	    Polygon2D.Double polygon = new Polygon2D.Double();
	    polygon.moveTo(-4, 4);
	    polygon.lineTo(4, 0);
	    polygon.lineTo(-4, -4);
	    polygon.closePath();
            Color fill;
            if(port instanceof IOPort && ((IOPort)port).isMultiport()) {
                fill = Color.white;
            } else {
                fill = Color.black;
            }
	    Figure figure = new BasicFigure(polygon, fill, (float)1.5);
	    figure.setToolTipText(port.getName());
	    return figure;
	}
    }

    /**
     * The factory for creating context menus on entities.
     */
    public static class PortContextMenuFactory extends PtolemyMenuFactory {
	public PortContextMenuFactory(GraphController controller) {
	    super(controller);
	    addMenuItemFactory(new EditParametersFactory());
	    addMenuItemFactory(new EditParameterStylesFactory());
	    addMenuItemFactory(new PortDescriptionFactory());
	}

	public class PortDescriptionFactory extends MenuItemFactory {
	    /**
	     * Add an item to the given context menu that will configure the
	     * parameters on the given target.
	     */
	    public JMenuItem create(JContextMenu menu, NamedObj target) {
		target = _getItemTargetFromMenuTarget(target);
		if(target instanceof IOPort) {
		    IOPort port = (IOPort)target;
		    String string = "";
		    int count = 0;
		    if(port.isInput()) {
			string += "Input";
			count++;
		    }
		    if(port.isOutput()) {
			if(count > 0) {
			    string += ", ";
			}
			string += "Output";
			count++;
		    }
		    if(port.isMultiport()) {
			if(count > 0) {
			    string += ", ";
			}
			string += "Multiport";
			count++;
		    }
		    if(count > 0) {
			return menu.add(new JMenuItem("   " + string));
		    }
		}
		return null;
	    }
	    
	    /**
	     * Get the name of the items that will be created. 
	     * This is provided so
	     * that factory can be overriden slightly with the name changed.
	     */
	    protected String _getName() {
		return null;
	    }     
	}
    }

    private MenuCreator _menuCreator;
}
