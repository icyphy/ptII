/* The node controller for ports contained in entities.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

package ptolemy.vergil.actor;

import diva.canvas.CanvasUtilities;
import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.connector.FixedNormalSite;
import diva.canvas.connector.PerimeterSite;
import diva.canvas.connector.TerminalFigure;
import diva.canvas.interactor.CompositeInteractor;
import diva.canvas.interactor.Interactor;
import diva.canvas.toolbox.BasicFigure;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.NodeRenderer;
import diva.util.java2d.Polygon2D;
import diva.util.java2d.Polygon2D.Double;
import ptolemy.actor.IOPort;
import ptolemy.data.type.Type;
import ptolemy.data.type.Typeable;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.*;
import ptolemy.vergil.kernel.AttributeController;

import javax.swing.SwingUtilities;
import java.awt.Color;

//////////////////////////////////////////////////////////////////////////
//// IOPortController
/**
This class provides interaction with nodes that represent Ptolemy II
ports on an actor.  It provides a double click binding and context
menu entry to edit the parameters of the port ("Configure") and a
command to get documentation.
It can have one of two access levels, FULL or PARTIAL.
If the access level is FULL, the the context menu also
contains a command to rename the node.
Note that whether the port is an input or output or multiport cannot
be controlled via this interface.  The "Configure Ports" command of
the container should be invoked instead.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/
public class IOPortController extends AttributeController {

    /** Create a port controller associated with the specified graph
     *  controller.  The controller is given full access.
     *  @param controller The associated graph controller.
     */
    public IOPortController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create a port controller associated with the
     *  specified graph controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public IOPortController(GraphController controller, Access access) {
	super(controller, access);
	setNodeRenderer(new EntityPortRenderer());

	// Ports of entities do not use a selection interactor with
	// the same selection model as the rest of the first level figures.
	// If this were allowed, then the port would be able to be deleted.
	CompositeInteractor interactor = new CompositeInteractor();
 	setNodeInteractor(interactor);
	interactor.addInteractor(_menuCreator);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Render the ports of components as triangles.  Multiports are
     *  rendered hollow, while single ports are rendered filled.
     */
    public class EntityPortRenderer implements NodeRenderer {
	public Figure render(Object n) {
	    final Port port = (Port)n;

            // If the port has an attribute called "_hide", then
            // do not render it.
            if (port.getAttribute("_hide") != null) return null;

	    Polygon2D.Double polygon = new Polygon2D.Double();
	    polygon.moveTo(-4, 4);
	    polygon.lineTo(4, 0);
	    polygon.lineTo(-4, -4);
	    polygon.closePath();
            Color fill;
            if (port instanceof IOPort && ((IOPort)port).isMultiport()) {
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

            ActorGraphModel model =
                (ActorGraphModel)getController().getGraphModel();

	    // Wrap the figure in a TerminalFigure to set the direction that
	    // connectors exit the port.  Note that this direction is the
	    // same direction that is used to layout the port in the
	    // Entity Controller.
	    int direction;
	    if (!(port instanceof IOPort)) {
		direction = SwingUtilities.SOUTH;
	    } else if (((IOPort)port).isInput() && ((IOPort)port).isOutput()) {
		direction = SwingUtilities.SOUTH;
	    } else if (((IOPort)port).isInput()) {
		direction = SwingUtilities.WEST;
	    } else if (((IOPort)port).isOutput()) {
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
}
