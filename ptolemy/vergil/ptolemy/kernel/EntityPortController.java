/* The node controller for ports contained in entities.

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
import ptolemy.data.type.Typeable;
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
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
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
public class EntityPortController extends BasicNodeController {
    public EntityPortController(GraphController controller) {
	super(controller);
	setNodeRenderer(new EntityPortRenderer());
	//SelectionModel sm = controller.getSelectionModel();
	// Ports of entities do not use a selection interactor with
	// the same selection model as
	// the rest of the first level figures.
	// If this is allowed, then the port can be deleted.
	CompositeInteractor interactor = new CompositeInteractor();
 	setNodeInteractor(interactor);
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

    /** Render the ports of components as triangles.  Multiports are
     *  rendered hollow, while single ports are rendered filled.
     */
    public class EntityPortRenderer implements NodeRenderer {
	public Figure render(Object n) {
	    final Port port = (Port)n;
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
	    Figure figure = new BasicFigure(polygon, fill, (float)1.5) {
                // Override this because we want to show the type.
                // It doesn't work to set it once because the type
                // has not been resolved, and anyway, it may change.
                public String getToolTipText() {
                    String tipText = port.getName();
                    if (port instanceof Typeable) {
                        try {
                            tipText = tipText + ", type:"
                                     + ((Typeable)port).getType();
                    } catch (IllegalActionException ex) {}
                }
                return tipText;
                }
            };
            // Have to do this also, or the awt doesn't display any
            // tooltip at all.
            figure.setToolTipText(port.getName());

            PtolemyGraphModel model =
                (PtolemyGraphModel)getController().getGraphModel();

	    // Wrap the figure in a TerminalFigure to set the direction that
	    // connectors exit the port.  Note that this direction is the
	    // same direction that is used to layout the port in the
	    // Entity Controller.
	    int direction;
	    if(!(port instanceof IOPort)) {
		direction = SwingUtilities.SOUTH;
	    } else if(((IOPort)port).isInput() && ((IOPort)port).isOutput()) {
		direction = SwingUtilities.SOUTH;
	    } else if(((IOPort)port).isInput()) {
		direction = SwingUtilities.WEST;
	    } else if(((IOPort)port).isOutput()) {
		direction = SwingUtilities.EAST;
	    } else {
		// should never happen
		direction = SwingUtilities.SOUTH;
	    }
	    double normal = CanvasUtilities.getNormal(direction);
	    Site tsite = new PerimeterSite(figure, 0);
	    tsite.setNormal(normal);
	    tsite = new FixedNormalSite(tsite);
	    figure = new TerminalFigure(figure, tsite);
	    return figure;
	}
    }

    // The interactor for creating context menus.
    private MenuCreator _menuCreator;
}
