 /* The graph controller for the ptolemy schematic editor

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
//// EditorGraphController
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
public class EditorGraphController extends GraphController {
 
    /**
     * The graph that is being displayed.
     */
    private Graph _graph;

    /** The selection interactor for drag-selecting nodes
     */
    private SelectionDragger _selectionDragger;

    /** The interactor for creating new relations
     */
    private RelationCreator _relationCreator;

    /** The interactor for creating new vertecies connected
     *  to an existing relation
     */
    private ConnectedVertexCreator _connectedVertexCreator;

    /** The interactor for creating new terminals
     */
    private PortCreator _portCreator;

    /** The interactor for creating context sensitive menus.
     */
    private MenuCreator _menuCreator;

    /** The interactor that interactively creates edges
     */
    private LinkCreator _linkCreator;

    private PortController _portController;
    private RelationController _relationController;
    private EntityController _entityController;
    private LinkController _linkController;

    /** The filter for control operations
     */
    private MouseFilter _controlFilter = new MouseFilter (
            InputEvent.BUTTON1_MASK,
            InputEvent.CTRL_MASK);

    /** The filter for shift operations 
     */
    private MouseFilter _shiftFilter = new MouseFilter (
            InputEvent.BUTTON1_MASK,
            InputEvent.SHIFT_MASK);

    /**
     * Create a new basic controller with default 
     * terminal and edge interactors.
     */
    public EditorGraphController () {
	_portController = new PortController(this);
	_entityController = new EntityController(this);
	_relationController = new RelationController(this);
	_linkController = new LinkController(this);
    }

    /** Add an edge to this graph editor and render it
     * from the given tail node to the given head node.  This edge is 
     * anchored to it's head and tail node, and does not have a regular edge
     * interactor.
     
    public void addAnchoredEdge(Edge edge, Node head, Node tail, 
				double x, double y) {
        Figure hf = (Figure) head.getVisualObject();
        Figure tf = (Figure) tail.getVisualObject();
        FigureLayer layer = getGraphPane().getForegroundLayer();
        Site headSite, tailSite;

	tailSite = getConnectorTarget().getTailSite(tf, x, y);
	getGraphImpl().setEdgeTail(edge, tail);
	headSite = getConnectorTarget().getHeadSite(hf, x, y);
	getGraphImpl().setEdgeHead(edge, head);
        
        Connector ef = getEdgeRenderer().render(edge, tailSite, headSite);

        // Add to the view
        ef.setUserObject(edge);
        edge.setVisualObject(ef);
        layer.add(ef);

	ef.route();
    }
    */

    public NodeController getEntityController() {
	return _entityController;
    }

    /**
     * Initialize all interaction on the graph pane. This method
     * is called by the setGraphPane() method of the superclass.
     * This initialization cannot be done in the constructor because
     * the controller does not yet have a reference to its pane
     * at that time.
     */
    protected void initializeInteraction () {
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

        // Create a listener that creates new relations
        _relationCreator = new RelationCreator();
        _relationCreator.setMouseFilter(_shiftFilter);
        pane.getBackgroundEventLayer().addInteractor(_relationCreator);
        
        // Create a listener that creates new terminals
	_portCreator = new PortCreator();
        _portCreator.setMouseFilter(_controlFilter);
        pane.getBackgroundEventLayer().addInteractor(_portCreator);
        
        // Create the interactor that drags new edges.
	_linkCreator = new LinkCreator();
	_linkCreator.setMouseFilter(_controlFilter);
	((CompositeInteractor)_portController.getNodeInteractor()).addInteractor(_linkCreator);
        ((CompositeInteractor)_entityController.getPortController().getNodeInteractor()).addInteractor(_linkCreator);
	((CompositeInteractor)_relationController.getNodeInteractor()).addInteractor(_linkCreator);
	
        /*
        // Create the interactor that drags new edges.
	_connectedVertexCreator = new ConnectedVertexCreator();
        _connectedVertexCreator.setMouseFilter(_shiftFilter);
        getNodeInteractor().addInteractor(_connectedVertexCreator);
        */
        // MenuCreator 	
        _menuCreator = new MenuCreator();
	pane.getBackgroundEventLayer().addInteractor(_menuCreator);
	pane.getBackgroundEventLayer().setConsuming(true);
         
    }

    /**
     * Set the graph being viewed. If there is a graph already
     * and it contains data, delete the figures of that graph's
     * nodes and edges (but don't modify the graph itself).
     */
    /*    public void setGraph(Graph graph) {
        //        Schematic g = (Schematic) graph;
        Figure f;
        Node n;
        Edge e;
        Iterator i;
        FigureLayer layer = getGraphPane().getForegroundLayer();

        // Clear existing figures
        if (getGraph() != null && getGraph().getNodeCount() != 0) {
            for (i = getGraph().nodes(); i.hasNext(); ) {
                n = (Node) i.next();
                f = (Figure) n.getVisualObject();
                layer.remove(f);
                f.setUserObject(null);
                n.setVisualObject(null);
            }
            Enumeration relations = g.relations();
            while(relations.hasMoreElements()) {
                SchematicRelation relation = 
                    (SchematicRelation) relations.nextElement();
                Enumeration enum;
                for (enum = relation.terminals(); 
                     enum.hasMoreElements(); ) {
                    n = (Node) enum.nextElement();
                    f = (Figure) n.getVisualObject();
                    layer.remove(f);
                    f.setUserObject(null);
                    n.setVisualObject(null);
                }
                for (enum = relation.links(); enum.hasMoreElements(); ) {
                    e = (Edge) enum.nextElement();
                    f = (Figure) e.getVisualObject();
                    layer.remove(f);
                    f.setUserObject(null);
                    e.setVisualObject(null);
                }
            }
        }

        // Draw new entities
        for (i = g.nodes(); i.hasNext(); ) {
            drawNode((Node)i.next(), 100, 100);
        }
        
        Enumeration relations = g.relations();
        while(relations.hasMoreElements()) {
            SchematicRelation relation = 
                (SchematicRelation) relations.nextElement();
            Enumeration enum;
            for (enum = relation.terminals(); enum.hasMoreElements(); ) {
                drawNode((Node)enum.nextElement(), 100, 100);
            }

            for (enum = relation.links(); enum.hasMoreElements(); ) {
                drawEdge((Edge) enum.nextElement());
            }
        }

        // Set the grap
        _graph = g;
    }
    */
    
    public void setEntityController(NodeController controller) {
	_entityController = (EntityController)controller;
    }

    ///////////////////////////////////////////////////////////////
    //// PortCreator

    /** An inner class that places a terminal at the clicked-on point
     * on the screen, if control-clicked with mouse button 1. This
     * needs to be made more customizable.
     */
    protected class PortCreator extends AbstractInteractor {
        public void mousePressed(LayerEvent e) {
            System.out.println("Port Creator");
	    Graph graph = getGraph();
	    CompositeEntity toplevel = 
		(CompositeEntity)graph.getSemanticObject();
	    Port port;
	    if(toplevel == null) 
		port = new Port();
	    else { 
		try {
		    port = toplevel.newPort(createUniqueName("port"));
		}
		catch (Exception ex) {
		    ex.printStackTrace();
		    throw new RuntimeException(ex.getMessage());
		}
	    }
            Node n = getGraphImpl().createNode(port);
            _portController.addNode(n, e.getLayerX(), e.getLayerY());
        }
    }

    ///////////////////////////////////////////////////////////////
    //// RelationCreator

    /** An inner class that places a relation at the clicked-on point
     * on the screen, if shift-clicked with mouse button 1. This
     * needs to be made more customizable.
     */
    protected class RelationCreator extends AbstractInteractor {
        public void mousePressed(LayerEvent e) {
            System.out.println("Relation Creator");
	    Graph graph = getGraph();
	    CompositeEntity toplevel = 
		(CompositeEntity)graph.getSemanticObject();
	    Relation relation = null;
            Vertex vertex = null;
            try {                
                relation = 
                    toplevel.newRelation(createUniqueName("relation"));
                vertex = new Vertex(relation, 
                        createUniqueName("vertex"));
                int[] coords = new int[2];
                coords[0] = e.getX();
                coords[1] = e.getY();
                vertex.setLocation(coords);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex.getMessage());
            }
            Node n = getGraphImpl().createNode(vertex);
            _relationController.addNode(n, e.getLayerX(), e.getLayerY());
        }
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
            System.out.println(sourceObject.description());
            

	    FigureLayer layer = (FigureLayer) e.getLayerSource();
	    
	    // Create a new edge
	    CompositeEntity container = 
		(CompositeEntity)getGraph().getSemanticObject();
	    Edge edge = getGraphImpl().createEdge(null);	    
	    
	    // Add it to the editor
	    _linkController.addEdge(edge,
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



    /** An interactor that creates a new Vertex that is connected to a vertex
     *  in a relation
     */
    protected class ConnectedVertexCreator extends AbstractInteractor {
	public void mousePressed(LayerEvent e) {
	    FigureLayer layer = (FigureLayer) e.getLayerSource();
	    Figure source = e.getFigureSource();
	    Node sourcenode = (Node) source.getUserObject();
	    NamedObj sourceObject = (NamedObj) sourcenode.getSemanticObject();
	    
            if((sourceObject instanceof Vertex)) {
		System.out.println(sourceObject.description());
		
		Relation relation = (Relation)sourceObject.getContainer();
		Vertex vertex = null;
		try {
		    vertex = new Vertex(relation, 
                            createUniqueName("vertex"));
                    int[] coords = new int[2];
                    coords[0] = e.getX();
                    coords[1] = e.getY();
                    vertex.setLocation(coords);
		}
		catch (Exception ex) {
		    ex.printStackTrace();
		    throw new RuntimeException(ex.getMessage());
		}
		Node node = getGraphImpl().createNode(vertex);
		//addNode(node, e.getLayerX(), e.getLayerY());

		Edge edge = getGraphImpl().createEdge(null);
		//addEdge(edge,
		//	sourcenode,
		//ConnectorEvent.TAIL_END,
		//	e.getLayerX(),
			//e.getLayerY());
		
		// Add it to the selection so it gets a manipulator, and
		// make events go to the grab-handle under the mouse
		Figure nf = (Figure) node.getVisualObject();
		getSelectionModel().addSelection(nf);
		//		ConnectorManipulator cm = (ConnectorManipulator) ef.getParent();
		//GrabHandle gh = cm.getHeadHandle();
		layer.grabPointer(e, nf);
	    }
	}
    }
	
    /** An interactor that creates context-sensitive menus.
     */
    public class MenuCreator extends AbstractInteractor {
	public MenuCreator() {
	    setMouseFilter(new MouseFilter(3));
	}
	
	public void mousePressed(LayerEvent e) {
	    Figure source = e.getFigureSource();
	    if(source == null) {
		Graph graph = getGraph();
		CompositeEntity object = 
		    (CompositeEntity) graph.getSemanticObject();
		JPopupMenu menu = 
		    new SchematicContextMenu(object);
		menu.show(getGraphPane().getCanvas(), e.getX(), e.getY());
		e.consume();
	    }
	}
    }
        
    public class SchematicContextMenu extends BasicContextMenu {
	public SchematicContextMenu(CompositeEntity target) {
	    super(target);
	    
	    Action action;
	    action = new AbstractAction ("Get Director Parameters") {
		public void actionPerformed(ActionEvent e) {
		    // Create a dialog and attach the dialog values 
		    // to the parameters of the schematic's director
		    CompositeActor object = 
		    (CompositeActor) getValue("target");
		    Director director = object.getDirector();
		    JFrame frame =
		    new JFrame("Parameters for " + director.getName());
                    JPanel pane = (JPanel) frame.getContentPane();
		    Query query;
		    try {
			query = new ParameterEditor(director);
		    } catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex.getMessage());
		    }
			
		    pane.add(query);
                    frame.setVisible(true);
                    frame.pack();
                }
            };
            action.putValue("target", target);
            action.putValue("tooltip", "Get Director Parameters");
            JMenuItem item = add(action);
            item.setToolTipText("Get Director Parameters");
            action.putValue("menuItem", item);  

	    //FIXME
	    JLabel domain = new JLabel("Domain");
	    add(domain);
	    JLabel director = new JLabel("Director");
	    add(director);

        }
    }

    public String createUniqueName(String root) {
	String name = root + _uniqueID++;
	return name;
    }

    public int createUniqueID() {
	return _uniqueID++;
    }
    
    private int _uniqueID = 1;
}

