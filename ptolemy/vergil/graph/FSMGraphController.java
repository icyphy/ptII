/* The graph controller for FSM models.

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
import ptolemy.vergil.toolbox.BasicContextMenu;
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
//// FSMGraphController
/**
A Graph Controller for FSM models.  This controller allows nodes to be
dragged and dropped onto its graph. 
Arcs can be created by control-clicking and dragging on
a port.  Anything can be deleted by selecting it and pressing
the delete key on the keyboard.

@author Steve Neuendorffer
@version $Id$
 */
public class FSMGraphController extends ViewerGraphController {
    /**
     * Create a new basic controller with default
     * terminal and edge interactors.
     */
    public FSMGraphController() {
	super();
    }

    /**
     * Initialize all interaction on the graph pane. This method
     * is called by the setGraphPane() method of the superclass.
     * This initialization cannot be done in the constructor because
     * the controller does not yet have a reference to its pane
     * at that time.
     */
    protected void initializeInteraction() {
        super.initializeInteraction();
        GraphPane pane = getGraphPane();

	// Create the interactor that drags new edges.
	_linkCreator = new LinkCreator();
	_linkCreator.setMouseFilter(_controlFilter);
	((CompositeInteractor)getPortController().getNodeInteractor()).addInteractor(_linkCreator);
        ((CompositeInteractor)getEntityController().getPortController().getNodeInteractor()).addInteractor(_linkCreator);
	((CompositeInteractor)getRelationController().getNodeInteractor()).addInteractor(_linkCreator);

	//LinkCreator linkCreator2 = new LinkCreator();
	//linkCreator2.setMouseFilter(
        //   new MouseFilter(InputEvent.BUTTON1_MASK,0));
	//((CompositeInteractor)getEntityController().getPortController().getNodeInteractor()).addInteractor(_linkCreator);
    }

    ///////////////////////////////////////////////////////////////
    //// LinkCreator

    /** An interactor that interactively drags edges from one terminal
     * to another.
     */
    protected class LinkCreator extends AbstractInteractor {
        public void mousePressed(LayerEvent e) {
            Figure source = e.getFigureSource();
	    Node sourcenode = (Node) source.getUserObject();
	    NamedObj sourceObject = (NamedObj) sourcenode.getSemanticObject();

	    FigureLayer layer = (FigureLayer) e.getLayerSource();

	    // Create a new edge
	    CompositeEntity container =
		(CompositeEntity)getGraph().getSemanticObject();

	    // Add it to the editor
	    Edge edge = getLinkController().addEdge(null,
                    sourcenode,
                    ConnectorEvent.TAIL_END,
                    e.getLayerX(),
                    e.getLayerY());

	    // Add it to the selection so it gets a manipulator, and
	    // make events go to the grab-handle under the mouse
	    Figure ef = (Figure) edge.getVisualObject();
	    getSelectionModel().addSelection(ef);
	    ConnectorManipulator cm =
		(ConnectorManipulator) ef.getParent();
	    GrabHandle gh = cm.getHeadHandle();
	    layer.grabPointer(e, gh);
	}
    }

    /** The interactor for creating context sensitive menus.
     */
    private MenuCreator _menuCreator;

    /** The interactor that interactively creates edges
     */
    private LinkCreator _linkCreator;

    /** The filter for control operations
     */
    private MouseFilter _controlFilter = new MouseFilter(
            InputEvent.BUTTON1_MASK,
            InputEvent.CTRL_MASK);

    /** The filter for shift operations
     */
    private MouseFilter _shiftFilter = new MouseFilter(
            InputEvent.BUTTON1_MASK,
            InputEvent.SHIFT_MASK);
}





