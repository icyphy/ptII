/* The graph controller for the ptolemy schematic editor ports

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
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// EntityPortController
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

public class EntityPortController extends NodeController {
    public EntityPortController(GraphController controller) {
	super(controller);
	setNodeRenderer(new EntityPortRenderer());
    }
    

    public class EntityPortRenderer implements NodeRenderer {
	public Figure render(Node n) {
	    StraightTerminal figure = new StraightTerminal();
	    //figure.setEnd(new Blob());
	    //Figure figure = new BasicRectangle(-2, -2, 4, 4, Color.black);
	    figure.setUserObject(n);
	    n.setVisualObject(figure);
	    return figure;
	}
    }    

    /** Given a node, add it to the given parent.  
     */
    public void addNode(Node node, CompositeNode parentNode, int direction, 
			double fraction) {
        // Create a figure for it
	//System.out.println("adding port");
	StraightTerminal nf = (StraightTerminal)getNodeRenderer().render(node);
        nf.setInteractor(getNodeInteractor());
	CompositeFigure parentFigure = 
	    (CompositeFigure)parentNode.getVisualObject();
	BoundsSite site = 
	    new BoundsSite(parentFigure, 0, direction, fraction);
        nf.setAttachSite(site);
	parentFigure.add(nf);
        
        // Add to the graph
	//getController().getGraphImpl().addNode(node, parentNode);
    }
}
