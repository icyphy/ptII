/* An interactor that sets tooltips on different figures.

 Copyright (c) 2000 The Regents of the University of California.
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

package ptolemy.vergil.toolbox;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.moml.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import diva.canvas.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import diva.gui.toolbox.*;
import diva.graph.model.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.SwingConstants;

//////////////////////////////////////////////////////////////////////////
//// ToolTipInteractor
/**

This interactor detects which figure the mouse is over and sets the tooltip
of the graph component to the name of the ptolemy object that the figure
represents

@author Steve Neuendorffer
@version $Id$
*/
public class ToolTipInteractor extends AbstractInteractor {
    
    public ToolTipInteractor() {
	setMotionEnabled(true);
	// Accept only raw non-presses
	setMouseFilter(new ToolTipMouseFilter());
    }
    
    /** Set the tooltip.
     */
    public void mouseEntered (LayerEvent layerEvent) {
	Figure figure = layerEvent.getFigureSource();
	Node node = (Node)figure.getUserObject();
	Icon icon = (Icon)node.getSemanticObject();
	Entity entity = (Entity)icon.getContainer();
	CanvasLayer layer = figure.getLayer();
	CanvasPane pane = layer.getCanvasPane();
	JCanvas canvas = pane.getCanvas();
	canvas.setToolTipText(entity.getName());
    }

    /** Delete the tooltip.
     */
    public void mouseExited (LayerEvent layerEvent) {
	Figure figure = layerEvent.getFigureSource();
	CanvasLayer layer = figure.getLayer();
	CanvasPane pane = layer.getCanvasPane();
	JCanvas canvas = pane.getCanvas();
	canvas.setToolTipText("");
    }

    public class ToolTipMouseFilter extends MouseFilter {
	public ToolTipMouseFilter() {
	    super(0);
	}

	public boolean accept (MouseEvent event) {
	    return true;
	}	
    }
}
