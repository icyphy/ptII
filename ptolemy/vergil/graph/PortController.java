/* The graph controller for the ptolemy schematic editor ports

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

public class PortController extends NodeController {
    public PortController(GraphController controller) {
	super(controller);
	setNodeRenderer(new PortRenderer());
	SelectionModel sm = controller.getSelectionModel();
	NodeInteractor interactor = new NodeInteractor(controller, sm);
	setNodeInteractor(interactor);
	_menuCreator = new MenuCreator(
	    new EntityPortController.PortContextMenuFactory(controller));
	interactor.addInteractor(_menuCreator);
    }

    public static class PortRenderer implements NodeRenderer {
	public Figure render(Object n) {
	    Port port = (Port)n;
	    Polygon2D.Double polygon = new Polygon2D.Double();
	    polygon.moveTo(-6, 6);
	    polygon.lineTo(0, 6);
	    polygon.lineTo(8, 0);
	    polygon.lineTo(0, -6);
	    polygon.lineTo(-6, -6);
	    polygon.closePath();
	    Figure figure = new BasicFigure(polygon, Color.black);

	    // Wrap the figure in a TerminalFigure to set the direction that
	    // connectors exit the port.  Note that this direction is the
	    // OPPOSITE direction that is used to layout the port in the
	    // Entity Controller.
	    int direction;
	    if(!(port instanceof IOPort)) {
		direction = SwingUtilities.NORTH;
	    } else if(((IOPort)port).isInput() && ((IOPort)port).isOutput()) {
		direction = SwingUtilities.NORTH;
	    } else if(((IOPort)port).isInput()) {
		direction = SwingUtilities.EAST;
	    } else if(((IOPort)port).isOutput()) {
		direction = SwingUtilities.WEST;
	    } else {
		// should never happen
		direction = SwingUtilities.NORTH;
	    }
	    double normal = CanvasUtilities.getNormal(direction);	    
	    Site tsite = new PerimeterSite(figure, 0);
	    tsite.setNormal(normal);
	    tsite = new FixedNormalSite(tsite);
	    figure = new TerminalFigure(figure, tsite);
	    return figure;
	}
    }
    private MenuCreator _menuCreator;
}


