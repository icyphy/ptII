/* The graph controller for the vergil viewer

 Copyright (c) 1999-2000 The Regents of the University of California.
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
//// ViewerGraphController
/**
A graph controller for the Ptolemy II schematic viewer.
This controller allows nodes to be moved and context menus to be created,
but does not provide interaction for adding or removing nodes.
Right-clicking on the background will
create a context-sensitive menu for the graph.

@author Steve Neuendorffer
@version $Id$
*/
public class ViewerGraphController extends CompositeGraphController {
    /**
     * Create a new basic controller with default
     * terminal and edge interactors.
     */
    public ViewerGraphController() {
	_entityController = new EntityController(this);
	_portController = new PortController(this);
	_relationController = new RelationController(this);
	_linkController = new LinkController(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Return the controller for entities
     */
    public EntityController getEntityController() {
	return _entityController;
    }

    /**
     * Return the controller for ports
     */
    public PortController getPortController() {
	return _portController;
    }

    /**
     * Return the controller for relations
     */
    public RelationController getRelationController() {
        return _relationController;
    }

    /**
     * Return the controller for links
     */
    public LinkController getLinkController() {
        return _linkController;
    }

    /**
     * Initialize all interaction on the graph pane. This method
     * is called by the setGraphPane() method of the superclass.
     * This initialization cannot be done in the constructor because
     * the controller does not yet have a reference to its pane
     * at that time.
     */
    protected void initializeInteraction() {
        GraphPane pane = getGraphPane();

        // Create and set up the selection dragger
	SelectionDragger _selectionDragger = new SelectionDragger(pane);
	_selectionDragger.addSelectionInteractor(
                (SelectionInteractor)_entityController.getNodeInteractor());
	_selectionDragger.addSelectionInteractor(
                (SelectionInteractor)_relationController.getNodeInteractor());
	_selectionDragger.addSelectionInteractor(
                (SelectionInteractor)_portController.getNodeInteractor());
	_selectionDragger.addSelectionInteractor(
                (SelectionInteractor)_linkController.getEdgeInteractor());

        // MenuCreator
        _menuCreator = new MenuCreator(new SchematicContextMenuFactory());
	pane.getBackgroundEventLayer().addInteractor(_menuCreator);

	pane.getBackgroundEventLayer().setConsuming(false);
    }

    /**
     * Return the node controller appropriate for the given node.
     */
    public NodeController getNodeController(Node node) {
        Object object = node.getSemanticObject();
        if(object instanceof Vertex) {
            return _relationController;
        } else if(object instanceof ptolemy.moml.Icon) {
            return _entityController;
	} else if(object instanceof Port) {
            return _portController;
        } else
            throw new RuntimeException(
                    "Node with unknown semantic object: " + object);
    }

    /**
     * Return the edge controller appropriate for the given node.
     */
    public EdgeController getEdgeController(Edge edge) {
        return _linkController;
    }

    /**
     * Set the controller for relations
     */
    public void setEntityController(EntityController controller) {
	_entityController = controller;
    }

    /**
     * Set the controller for ports
     */
    public void setPortController(PortController controller) {
        _portController = controller;
    }

    /**
     * Set the controller for relations
     */
    public void setRelationController(RelationController controller) {
        _relationController = controller;
    }

    /**
     * Set the controller for links
     */
    public void setLinkController(LinkController controller) {
        _linkController = controller;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        public variables                   ////
    // The graph that is being displayed.
    private Graph _graph;

    // The selection interactor for drag-selecting nodes
    private SelectionDragger _selectionDragger;

    
    // The interactor for creating context sensitive menus on the
    // graph itself.
    private MenuCreator _menuCreator;

    // The controllers
    private EntityController _entityController;
    private PortController _portController;
    private RelationController _relationController;
    private LinkController _linkController;

    ///////////////////////////////////////////////////////////////////
    ////                          inner classes                    ////
    public class SchematicContextMenuFactory extends PtolemyMenuFactory {
	public SchematicContextMenuFactory() {
	    super();
	    addMenuItemFactory(new EditParametersFactory());
	    addMenuItemFactory(new EditParameterStylesFactory());
	}	
	
	public NamedObj _getObjectFromFigure(Figure source) {
	    // FIXME
	    return null;
	}
    };
}
