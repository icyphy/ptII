/* The graph controller for the ptolemy schematic editor ports

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
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.vergil.ptolemy.LocatableNodeController;
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
import diva.util.java2d.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.awt.geom.*;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// PortController
/**
A Controller for Ptolemy ports that are contained within the composite.
Left clicking selects the port, which can then be dragged.
Right clicking on the port will create a context menu for the port.

@author Steve Neuendorffer
@version $Id$
*/

public class PortController extends LocatableNodeController {
    public PortController(GraphController controller) {
	super(controller);
	setNodeRenderer(new PortRenderer());
	SelectionModel sm = controller.getSelectionModel();
	SelectionInteractor interactor =
            (SelectionInteractor) getNodeInteractor();
	interactor.setSelectionModel(sm);
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

    /** Render the external ports of a graph as a 5-sided tab thingy.
     *  Multiports are rendered hollow,
     *  while single ports are rendered filled.
     */
    public class PortRenderer implements NodeRenderer {
	public Figure render(Object n) {
	    Polygon2D.Double polygon = new Polygon2D.Double();

	    Figure figure;
	    // Wrap the figure in a TerminalFigure to set the direction that
	    // connectors exit the port.  Note that this direction is the
	    // OPPOSITE direction that is used to layout the port in the
	    // Entity Controller.
	    int direction;
	    Location location = (Location)n;
	    if(location != null) {
		Port port = (Port)location.getContainer();

		Color fill;
		if(port instanceof IOPort) {
		    IOPort ioport = (IOPort)port;
		    polygon.moveTo(0, 5);
		    polygon.lineTo(0, 10);
		    if(ioport.isOutput()) {
			polygon.lineTo(6, 5);
			polygon.lineTo(14, 5);
			polygon.lineTo(14, -5);
			polygon.lineTo(6, -5);
		    } else {
			polygon.lineTo(12, 0);
		    }
		    polygon.lineTo(0, -10);
		    if(ioport.isInput()) {
			polygon.lineTo(0, -5);
			polygon.lineTo(-6, -5);
			polygon.lineTo(-6, 5);
		    }

		    polygon.closePath();

		    if(ioport.isMultiport()) {
			fill = Color.white;
		    } else {
			fill = Color.black;
		    }
		} else {
		    polygon.moveTo(-6, 6);
		    polygon.lineTo(0, 6);
		    polygon.lineTo(8, 0);
		    polygon.lineTo(0, -6);
		    polygon.lineTo(-6, -6);
		    polygon.closePath();
		    fill = Color.black;
		}
		figure = new BasicFigure(polygon, fill, (float)1.5);
                PtolemyGraphModel model =
                    (PtolemyGraphModel)getController().getGraphModel();
                figure.setToolTipText(port.getName());

		if(!(port instanceof IOPort)) {
		    direction = SwingUtilities.NORTH;
		} else {
		    IOPort ioport = (IOPort)port;

		    if(ioport.isInput() &&
                            ioport.isOutput()) {
                        direction = SwingUtilities.NORTH;
		    } else if(ioport.isInput()) {
			direction = SwingUtilities.EAST;
		    } else if(ioport.isOutput()) {
			direction = SwingUtilities.WEST;
		    } else {
			// should never happen
			direction = SwingUtilities.NORTH;
		    }
		}
		double normal = CanvasUtilities.getNormal(direction);
		Site tsite = new PerimeterSite(figure, 0);
		tsite.setNormal(normal);
		tsite = new FixedNormalSite(tsite);
                figure = new NameWrapper(figure, port.getName());
		figure = new TerminalFigure(figure, tsite);
	    } else {
		polygon.moveTo(0, 0);
		polygon.lineTo(0, 10);
		polygon.lineTo(12, 0);
		polygon.lineTo(0, -10);
		polygon.closePath();

		figure = new BasicFigure(polygon, Color.black);
	    }
	    return figure;
	}
    }

    private class NameWrapper extends AbstractFigure {
        /** The child
         */
        private Figure _child = null;
        
        /** The label
         */
        private LabelFigure _label = null;
        
        /** The label anchor
         */
        private int _anchor = SwingConstants.SOUTH_WEST;
        
        /** Construct a new figure with the given child figure and
         * the given string.
         */
        public NameWrapper (Figure f, String label) {
            _child = f;
            f.setParent(this);
            
            _label = new LabelFigure(label);
            _label.setPadding(0);
            _label.setAnchor(_anchor);
            Rectangle2D bounds = _child.getBounds();
            _label.translateTo(bounds.getX(), bounds.getY());
        }
        
        /** Get the bounds of this figure.
         */
        public Rectangle2D getBounds () {
            Rectangle2D bounds = _child.getBounds();
            Rectangle2D.union(bounds, _label.getBounds(), bounds);
            return bounds;
        }
        
        /** Get the child figure
         */
        public Figure getChild () {
            return _child;
        }
        
        /** Get the label. This can be used to adjust the label
         * appearance, anchor, and so on.
         */
        public LabelFigure getLabel () {
            return _label;
        }
        
        /** Get the shape of this figure. This is the shape
         * of the child figure only -- the label is not included
         * in the shape.
         */
        public Shape getShape () {
            return _child.getShape();
        }
        
        /** We are hit if either the child or the figure is hit.
         */
        public boolean hit (Rectangle2D r) {
            return _child.hit(r);
        }
        
        /** Paint this figure
         */
        public void paint (Graphics2D g) {
            if (_child != null && isVisible()) {
                _child.paint(g);
                _label.paint(g);
            }
        }
        
        /** Transform the figure with the supplied transform.
         */
        public void transform (AffineTransform at) {
            repaint();
            _child.transform(at);
            Rectangle2D bounds = _child.getBounds();
            _label.translateTo(bounds.getX(), bounds.getY());
            repaint();
        }
    }

    private MenuCreator _menuCreator;
}


