 /* The controller for relation node

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
//// RelationController
/**
 * A Controller for relation nodes.
 *
 * @author Steve Neuendorffer 
 * @version $Id$
 */
public class RelationController extends NodeController {
    public RelationController(GraphController controller) {
	super(controller);
	setNodeRenderer(new RelationRenderer());
	SelectionModel sm = controller.getSelectionModel();
	NodeInteractor interactor = new NodeInteractor(sm);
	setNodeInteractor(interactor);	
	new MenuCreator(interactor);
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
	    Node sourcenode = (Node) source.getUserObject();
	    NamedObj object = (NamedObj) sourcenode.getSemanticObject();
	    JPopupMenu menu = 
		new RelationContextMenu(object);
	    menu.show(getController().getGraphPane().getCanvas(),
		      e.getX(), e.getY());
	}
    }
    
    public static class RelationContextMenu extends BasicContextMenu {
	public RelationContextMenu(NamedObj target) {
	    super(target);
	}
    }
    
    public class RelationRenderer implements NodeRenderer {
	public Figure render(Node n) {
	    double h = 12.0;            
	    double w = 12.0;
	    
	    Polygon2D.Double polygon = new Polygon2D.Double();
	    polygon.moveTo(w/2, 0);
	    polygon.lineTo(0, h/2);
	    polygon.lineTo(-w/2, 0);
	    polygon.lineTo(0, -h/2);
	    polygon.closePath();
	    //	    Figure figure = new BasicRectangle(-4, -4, 8, 8, Color.black);
	    Figure figure = new BasicFigure(polygon, Color.black);
            figure.setUserObject(n);
	    n.setVisualObject(figure);
	    return figure;
	}
    }
}
