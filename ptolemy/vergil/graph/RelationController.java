/* The node controller for relations (and verteces)

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
import ptolemy.vergil.*;
import ptolemy.vergil.toolbox.*;
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
This class provides interaction with nodes that represent Ptolemy II relations.
(Or, more specifically, for the verteces that are contained in a relation.)
Standard selection and movement interaction is
provided.  In addition, right clicking on the entity will create a context
menu for the entity.

@author Steve Neuendorffer
@version $Id$
*/
public class RelationController extends LocatableNodeController {

    public RelationController(GraphController controller) {
	super(controller);
	setNodeRenderer(new RelationRenderer());
	SelectionModel sm = controller.getSelectionModel();
	SelectionInteractor interactor =
            (SelectionInteractor) getNodeInteractor();
	interactor.setSelectionModel(sm);
	_menuCreator = new MenuCreator(new RelationContextMenuFactory());
	interactor.addInteractor(_menuCreator);
    }

    /**
     * The factory for creating context menus on relations.
     */
    public static class RelationContextMenuFactory extends PtolemyMenuFactory {
	public RelationContextMenuFactory() {
	    super();
	    addMenuItemFactory(new EditParametersFactory());
	    addMenuItemFactory(new EditParameterStylesFactory());
	}

	public NamedObj _getObjectFromFigure(Figure source) {
	    Vertex vertex = (Vertex)super._getObjectFromFigure(source);
	    return(NamedObj) vertex.getContainer();
	}
    }

    /** 
     * The renderer for relation node.  This class creates a Figure that 
     * looks like a black diamond.
     */
    public static class RelationRenderer implements NodeRenderer {
	public Figure render(Node n) {
	    double h = 12.0;
	    double w = 12.0;

	    Polygon2D.Double polygon = new Polygon2D.Double();
	    polygon.moveTo(w/2, 0);
	    polygon.lineTo(0, h/2);
	    polygon.lineTo(-w/2, 0);
	    polygon.lineTo(0, -h/2);
	    polygon.closePath();
	    Figure figure = new BasicFigure(polygon, Color.black);
	    return figure;
	}
    }

    // The interactor that creates menus.
    private MenuCreator _menuCreator;
}
