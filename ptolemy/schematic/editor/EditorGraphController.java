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

    /** The interactor for creating new terminals
     */
    private RelationCreator _relationCreator;

     /** The interactor for creating new terminals
     */
    private TerminalCreator _terminalCreator;

    /** The interactor for creating context sensitive menus.
     */
    private MenuCreator _menuCreator;

    /** The interactor that interactively creates edges
     */
    private EdgeCreator _edgeCreator;

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

    /** The filter for context sensitive menus
     */
    private MouseFilter _menuFilter = new MouseFilter (3);

    /**
     * Create a new basic controller with default 
     * terminal and edge interactors.
     */
    public EditorGraphController () {
        // The interactors attached to terminals and edges
        SelectionModel sm = getSelectionModel();
        NodeInteractor ni = new NodeInteractor(this, sm);
	//ni.setSelectionManipulator(new BoundsManipulator());
        EdgeInteractor ei = new EdgeInteractor(this, sm);
        setNodeInteractor(ni);
        setEdgeInteractor(ei);

        NodeRenderer nr = new EditorNodeRenderer();
        setNodeRenderer(nr);

        // Create and set up the target for connectors
        ConnectorTarget ct = new PerimeterTarget() {
	    public boolean accept (Figure f) {
    //		System.out.println(f.getUserObject().toString());
                return (f.getUserObject() instanceof Node);
		                // FIXME Used needs something like: ||
		// (f instanceof FigureWrapper &&
                //             ((FigureWrapper)f).getChild().instanceof Node);
            }
	};
        setConnectorTarget(ct);

        // Create and set up the manipulator for connectors
        ConnectorManipulator manipulator = new ConnectorManipulator();
        manipulator.setSnapHalo(4.0);
        manipulator.setConnectorTarget(ct);
        manipulator.addConnectorListener(new EdgeDropper());
        ei.setPrototypeDecorator(manipulator);

        // The mouse filter needs to accept regular click or control click
        MouseFilter handleFilter = new MouseFilter(1, 0, 0);
        manipulator.setHandleFilter(handleFilter);
    }

    /** Add an entity to this graph editor and render it
     * at the given location.
     */
    //    public void addEntity(SchematicEntity entity, double x, double y) {
    //	addNode(entity, x, y);
	
	/*
	Enumeration terminals = entity.terminals();
	while(terminals.hasMoreElements()) {
	    SchematicTerminal terminal = 
		(SchematicTerminal) terminals.nextElement();
	    drawNode(terminal, x + terminal.getX(), y + terminal.getY());
	} 
	*/       
    //    }

    /** Add an edge to this graph editor and render it
     * from the given tail node to an autonomous site at the
     * given location. The "end" flag is either HEAD_END
     * or TAIL_END, from diva.canvas.connector.ConnectorEvent.
     */
    public void addEdge(Edge edge, Node node, int end, double x, double y) {
        Figure nf = (Figure) node.getVisualObject();
        FigureLayer layer = getGraphPane().getForegroundLayer();
        Site headSite, tailSite;

        if (end == ConnectorEvent.TAIL_END) {
            tailSite = getConnectorTarget().getTailSite(nf, x, y);
            headSite = new AutonomousSite(layer, x, y);
            getGraphImpl().setEdgeTail(edge, node);
        } else {
            tailSite = new AutonomousSite(layer, x, y);
            headSite = getConnectorTarget().getHeadSite(nf, x, y);
            getGraphImpl().setEdgeHead(edge, node);
        }

        Connector ef = getEdgeRenderer().render(edge, tailSite, headSite);
        ef.setInteractor(getEdgeInteractor());

        // Add to the view
        ef.setUserObject(edge);
        edge.setVisualObject(ef);
        layer.add(ef);

	ef.route();
        // Add to the graph
        // FIXME
        // SchematicGraphImpl impl = (SchematicGraphImpl) getGraphImpl();
        // impl.addEdge(edge, getGraph());
    }

    /** Add a node to this graph editor and render it
     * at the given location.
     */
    public void addNode(Node node, double x, double y) {
        // Create a figure for it
        drawNode(node, x, y);

        // Add to the graph
        getGraphImpl().addNode(node, getGraph());
    }
    
    /** Draw an edge.
     */
    public void drawEdge(Edge edge) {
        Node tail = edge.getTail();
        Node head = edge.getHead();
        FigureLayer layer = getGraphPane().getForegroundLayer();
        Figure tf = (Figure) tail.getVisualObject();
        Figure hf = (Figure) head.getVisualObject();

        // Get a tail site
        Rectangle2D bounds = tf.getBounds();
        Site tailSite = getConnectorTarget().getTailSite(tf,
                bounds.getCenterX(), bounds.getCenterY());

        // Get a head site
        bounds = hf.getBounds();
        Site headSite = getConnectorTarget().getHeadSite(hf,
                bounds.getCenterX(), bounds.getCenterY());

        // Create the figure
        Connector ef = getEdgeRenderer().render(edge, tailSite, headSite);
        edge.setVisualObject(ef);
        ef.setUserObject(edge);
        ef.setInteractor(getEdgeInteractor());
        layer.add(ef);
        ef.route();
    }

    /** Draw a node at the given location.
     */
    public void drawNode(Node n, double x, double y) {
	// Create a figure for it
        Figure nf = getNodeRenderer().render(n);
        nf.setInteractor(getNodeInteractor());
	getGraphPane().getForegroundLayer().add(nf);
	
        if(n instanceof CompositeNode) {
	    Iterator nodes = ((CompositeNode) n).nodes();
	    while(nodes.hasNext()) {
		Node node = (Node) nodes.next();
		Figure nodeFigure = getNodeRenderer().render(node);
		nodeFigure.setInteractor(getNodeInteractor());

                // Assume that CompositeNode -> CompositeFigure
		((CompositeFigure)nf).add(nodeFigure);

                //FIXME Where to put the damned things?
                //		CanvasUtilities.translateTo(nodeFigure, 
		//			  terminal.getX(), 
		//			  terminal.getY());

		nodeFigure.setUserObject(node);
		node.setVisualObject(nodeFigure);
	    }
        }
	
        CanvasUtilities.translateTo(nf, x, y);
 
        // Add to the view and model
        nf.setUserObject(n);
        n.setVisualObject(nf);
    }

    /**
     * Return the graph being viewed.
     */
    //    public Graph getGraph() {
    //    return _graph;
    //}

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
        _selectionDragger.addSelectionInteractor(getEdgeInteractor());
        _selectionDragger.addSelectionInteractor(getNodeInteractor());

        // Create a listener that creates new relations
        _relationCreator = new RelationCreator();
        _relationCreator.setMouseFilter(_shiftFilter);
        pane.getBackgroundEventLayer().addInteractor(_relationCreator);

        // Create a listener that creates new terminals
        _terminalCreator = new TerminalCreator();
        _terminalCreator.setMouseFilter(_controlFilter);
        pane.getBackgroundEventLayer().addInteractor(_terminalCreator);

        // Create the interactor that drags new edges.
	_edgeCreator = new EdgeCreator();
        _edgeCreator.setMouseFilter(_controlFilter);
        getNodeInteractor().addInteractor(_edgeCreator);

        // MenuCreator 
        _menuCreator = new MenuCreator();
        _menuCreator.setMouseFilter(_menuFilter);
        // For menus on objects
        getNodeInteractor().addInteractor(_menuCreator);   
        // For menus on the schematic.
        pane.getBackgroundEventLayer().addInteractor(_menuCreator);
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

        // Set the graph
        _graph = g;
    }
    */

    ///////////////////////////////////////////////////////////////
    //// TerminalCreator

    /** An inner class that places a terminal at the clicked-on point
     * on the screen, if control-clicked with mouse button 1. This
     * needs to be made more customizable.
     */
    protected class TerminalCreator extends AbstractInteractor {
        public void mousePressed(LayerEvent e) {
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
            addNode(n, e.getLayerX(), e.getLayerY());
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
	    Graph graph = getGraph();
	    CompositeEntity toplevel = 
		(CompositeEntity)graph.getSemanticObject();
	    Relation relation = null;
            VertexAttribute vertex = null;
            try {                
                relation = 
                    toplevel.newRelation(createUniqueName("relation"));
                vertex = new VertexAttribute(relation, 
                        createUniqueName("vertex"), 
                        e.getX(), e.getY());
            }
            catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex.getMessage());
            }
            Node n = getGraphImpl().createNode(vertex);
            addNode(n, e.getLayerX(), e.getLayerY());
        }
    }

    ///////////////////////////////////////////////////////////////
    //// EdgeDropper

    /** An inner class that handles interactive changes to connectivity.
     */
    protected class EdgeDropper extends ConnectorAdapter {
        /**
         * Called when a connector end is dropped--attach or
         * detach the edge as appropriate.
         */
        public void connectorDropped(ConnectorEvent evt) {
            Connector c = evt.getConnector();
            Figure f = evt.getTarget();
            Edge e = (Edge)c.getUserObject();
            Node n = (f == null) ? null : (Node)f.getUserObject();
	    GraphImpl impl = getGraphImpl();
            switch (evt.getEnd()) {
            case ConnectorEvent.HEAD_END:
                impl.setEdgeHead(e, n);
                break;
            case ConnectorEvent.TAIL_END:
                impl.setEdgeTail(e, n);
                break;
            default:
                throw new IllegalStateException(
                        "Cannot handle both ends of an edge being dragged.");
            }
        }
    }

    ///////////////////////////////////////////////////////////////
    //// EdgeCreator

    /** An interactor that interactively drags edges from one terminal
     * to another.
     */
    protected class EdgeCreator extends AbstractInteractor {
        public void mousePressed(LayerEvent e) {
            Figure source = e.getFigureSource();
	    Node sourcenode = (Node) source.getUserObject();
	    NamedObj sourceObject = (NamedObj) sourcenode.getSemanticObject();

            if(!(sourceObject instanceof VertexAttribute)) return;
	    System.out.println(sourceObject.description());

            FigureLayer layer = (FigureLayer) e.getLayerSource();
            
            // Create a new edge
            CompositeEntity container = 
                (CompositeEntity)getGraph().getSemanticObject();
            /*            LinkAttribute link;
            try {
                link = new LinkAttribute(container, 
                        createUniqueName("Relation"));
            }
            catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex.getMessage());
                }*/
            Edge edge = getGraphImpl().createEdge(null);	    
            
            // Add it to the editor
            addEdge(edge,
                    sourcenode,
                    ConnectorEvent.TAIL_END,
                    e.getLayerX(),
                    e.getLayerY());

            // Add it to the selection so it gets a manipulator, and
            // make events go to the grab-handle under the mouse
            Figure ef = (Figure) edge.getVisualObject();
            getSelectionModel().addSelection(ef);
            ConnectorManipulator cm = (ConnectorManipulator) ef.getParent();
            GrabHandle gh = cm.getHeadHandle();
            layer.grabPointer(e, gh);
        }
    }
    
    /** An interactor that creates context-sensitive menus.
     */
    protected class MenuCreator extends AbstractInteractor {
        public void mousePressed(LayerEvent e) {
            Figure source = e.getFigureSource();
            if(source == null) {
                Graph graph = getGraph();
                NamedObj object = (NamedObj) graph.getSemanticObject();
                JPopupMenu menu = 
                    new ObjectContextMenu(object);
                menu.show(getGraphPane().getCanvas(), e.getX(), e.getY());
            }
            else {
                Node sourcenode = (Node) source.getUserObject();
                NamedObj object = (NamedObj) sourcenode.getSemanticObject();
                JPopupMenu menu = 
                    new ObjectContextMenu(object);
                menu.show(getGraphPane().getCanvas(), e.getX(), e.getY());
            }
        }
    }
    
    /**
     * This is a base class for popup menus used to manipulate various
     * PTMLObjects within the editor.  It contains an entry for parameter
     * editing that opens a dialog box in a new frame for 
     * editing the parameters
     * of an object.  
     */
    public class ObjectContextMenu extends JPopupMenu {
        protected NamedObj _target;
        
        public ObjectContextMenu(NamedObj target) {
            super(target.getName());
            _target = target;
            
            Action action;
            action = new AbstractAction ("Get Parameters") {
                public void actionPerformed(ActionEvent e) {
                    // Create a dialog and attach the dialog values 
                    // to the parameters of the object                    
                    NamedObj object = (NamedObj) getValue("target");
                    System.out.println(object);
                    JFrame frame = new JFrame("Parameters for " + object.getName());
                    JPanel pane = (JPanel) frame.getContentPane();
                    Query query;
                    try {
                        query = new ParameterQuery(object);
                    } catch (IllegalActionException ex) {
                        ex.printStackTrace();
                        throw new RuntimeException(ex.getMessage());
                    }

                    pane.add(query);
                    frame.setVisible(true);
                    frame.pack();
                }
            };
            action.putValue("target", target);
            action.putValue("tooltip", "Get Parameters");
            JMenuItem item = add(action);
            item.setToolTipText("Get Parameters");
            action.putValue("menuItem", item);
        }
    }

    /*    public class SchematicContextMenu extends ObjectContextMenu {
        public SchematicContextMenu(Schematic target) {
            super(target);

            Action action;
            action = new AbstractAction ("Get Director Parameters") {
                public void actionPerformed(ActionEvent e) {
                    // Create a dialog and attach the dialog values 
                    // to the parameters of the schematic's director
                    Schematic object = (Schematic) getValue("target");
                    SchematicDirector director = object.getDirector();
                    JFrame frame =
                        new JFrame("Parameters for " + director.getName());
                    JPanel pane = (JPanel) frame.getContentPane();
                    
                    Query query = new ParameterQuery(director);
                    
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
        }
    }
    */
    
    public String createUniqueName(String root) {
	String name = root + _uniqueID++;
	return name;
    }

    public int createUniqueID() {
	return _uniqueID++;
    }
    
    private int _uniqueID = 1;
}

