package ptolemy.vergil.ptolemy;

import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.CompositeEntity;
import ptolemy.moml.Locatable;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.VisualNotation;
import ptolemy.vergil.graph.EditorDropTarget;

import diva.canvas.CanvasUtilities;
import diva.canvas.Site;
import diva.canvas.Figure;
import diva.canvas.connector.FixedNormalSite;
import diva.canvas.connector.Terminal;
import diva.canvas.interactor.SelectionModel;

import diva.gui.View;
import diva.gui.AbstractView;
import diva.gui.Document;
import diva.gui.toolbox.FocusMouseListener;

import diva.graph.JGraph;

import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.GraphUtilities;
import diva.graph.MutableGraphModel;
import diva.graph.basic.BasicLayoutTarget;
import diva.graph.layout.LevelLayout;
import diva.graph.layout.LayoutTarget;
import diva.graph.toolbox.DeletionListener;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import java.awt.print.PageFormat;

import java.io.IOException;
import java.io.StringWriter;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.KeyStroke;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class PtolemyGraphView extends AbstractView 
    implements Printable, ClipboardOwner {
  
    public PtolemyGraphView(PtolemyDocument d) {
        super(d);
    }

    /** Get the currently selected objects from this document, if any,
     * and place them on the given clipboard. 
     */
    public void copy (Clipboard c) {
	GraphPane graphPane = _jgraph.getGraphPane();
	GraphController controller =
	    (GraphController)graphPane.getGraphController();
	SelectionModel model = controller.getSelectionModel();
	GraphModel graphModel = controller.getGraphModel();
	Object selection[] = model.getSelectionAsArray();
	HashSet objectSet = new HashSet();
	for(int i = 0; i < selection.length; i++) {
	    if(selection[i] instanceof Figure) {
		Object userObject = ((Figure)selection[i]).getUserObject();
		NamedObj object = (NamedObj)userObject;
		NamedObj actual = 
		    (NamedObj)graphModel.getSemanticObject(object);
		if(objectSet.contains(actual)) continue;
		objectSet.add(actual);
	    }
	}
	
	StringWriter buffer = new StringWriter();	   
	try {
	    buffer.write("<group>\n");
	    Iterator elements = objectSet.iterator();
	    while(elements.hasNext()) {
		NamedObj element = (NamedObj) elements.next();
		// first level to avoid obnoxiousness with 
		// toplevel translations.
		element.exportMoML(buffer, 1);
	    }
	    CompositeEntity container = (CompositeEntity)graphModel.getRoot();
	    buffer.write(container.exportLinks(1, objectSet));
	    buffer.write("</group>\n");
	 
	    // The code below does not use a PtolemyTransferable, 
	    // to work around
	    // a bug in the JDK that should be fixed as of jdk1.3.1.  The bug
	    // is that cut and paste through the system clipboard to native
	    // applications doesn't work unless you use string selection. 
	    c.setContents(new StringSelection(buffer.toString()), this);
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	}

    }
 
    /** Remove the currently selected objects from this document, if any,
     * and place them on the given clipboard.  If the document does not
     * support such an operation, then do nothing.
     */
    public void cut (Clipboard c) {
	// First copy everyrhing onto the clipboard.
	copy(c);

	/* FIXME
	JGraph jgraph = getView();
	GraphPane graphPane = _jgraph.getGraphPane();
	GraphController controller =
	    (GraphController)graphPane.getGraphController();
	GraphImpl impl = controller.getGraphImpl();
	SelectionModel model = controller.getSelectionModel();
	Object selection[] = model.getSelectionAsArray();
	*/
    }


    public JComponent getComponent() {
        if(_jgraph == null) {
	    VisualNotation notation = _getVisualNotation(((PtolemyDocument)getDocument()).getModel());
	    GraphPane pane = notation.createView(getDocument());
	    _jgraph = new JGraph(pane);
	    GraphController controller =
		_jgraph.getGraphPane().getGraphController();
	    
	    new EditorDropTarget(_jgraph);
	    
	    ActionListener deletionListener = new DeletionListener();
	    _jgraph.registerKeyboardAction(deletionListener, "Delete",
					  KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
					  JComponent.WHEN_IN_FOCUSED_WINDOW);
	    _jgraph.setRequestFocusEnabled(true);
	    _jgraph.addMouseListener(new FocusMouseListener());
	    _jgraph.setAlignmentX(1);
	    _jgraph.setAlignmentY(1);
	    _jgraph.setBackground(PtolemyModule.BACKGROUND_COLOR);
	  
	    // Ugh..  I hate setting the size like this.
	    _jgraph.setPreferredSize(new Dimension(600, 450));
	    _jgraph.setSize(600, 450);
	    
	    // Ugh, I hate having to add scrollbars manually.
	    _scrollPane = new JScrollPane(_jgraph);
	    _scrollPane.setVerticalScrollBarPolicy(_scrollPane.VERTICAL_SCROLLBAR_NEVER);
	    _scrollPane.setHorizontalScrollBarPolicy(_scrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	    return _scrollPane;
        }
        return _scrollPane;
    }

    /** Return the jgraph instance that makes up part of this view.
     */
    public JGraph getJGraph() {
	return _jgraph;
    }

    /** Return the document of this view, cast to a PtolemyDocument.
     */
    public PtolemyDocument getPtolemyDocument() {
	return (PtolemyDocument)getDocument();
    }

    public String getTitle() {
        return "Ptolemy Graph";
    }

    public String getShortTitle() {
        return "Ptolemy Graph";
    }

    /** Layout the graph view.
     */
    public void layout() {
	GraphController controller = 
	    _jgraph.getGraphPane().getGraphController();
        LayoutTarget target = new PtolemyLayoutTarget(controller);
        GraphModel model = controller.getGraphModel();
        PtolemyLayout layout = new PtolemyLayout(target);
	layout.setOrientation(LevelLayout.HORIZONTAL);
	layout.setRandomizedPlacement(false);
        // Perform the layout and repaint
	layout.layout(model.getRoot());
        _jgraph.repaint();
    }

    /** Do nothing.
     */
    public void lostOwnership(Clipboard clipboard, 
			      Transferable transferable) {
    }

    /** Clone the objects currently on the clipboard, if any,
     * and place them in the given document.  If the document does not
     * support such an operation, then do nothing.  This method is responsible
     * for copying the data.
     */
    public void paste (Clipboard c) {
	Transferable transferable = c.getContents(this);
	GraphPane graphPane = _jgraph.getGraphPane();
	GraphController controller =
	    (GraphController)graphPane.getGraphController();
	MutableGraphModel model = controller.getGraphModel();
	Workspace workspace = ((NamedObj) model.getRoot()).workspace();
	if(transferable == null) 
	    return;
	try {
	    String string = (String)
		transferable.getTransferData(DataFlavor.stringFlavor);
	    CompositeEntity toplevel = (CompositeEntity)model.getRoot();
	    MoMLParser parser = new MoMLParser(workspace);
	    parser.setContext(toplevel);
	    toplevel.requestChange(
                new MoMLChangeRequest(this, parser, string));
	} catch (UnsupportedFlavorException ex) {
	    System.out.println("Transferable object didn't " + 
			       "support stringFlavor: " +
			       ex.getMessage());
	} catch (IOException ex) {
	    System.out.println("IOException when pasting: " + 
			       ex.getMessage());
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException(ex.getMessage());
	} 
    }

    /** Print the document to a printer, represented by the specified graphics
     *  object.  This method assumes that a view exists of the this document
     *  in the application.
     *  @param graphics The context into which the page is drawn.
     *  @param format The size and orientation of the page being drawn.
     *  @param index The zero based index of the page to be drawn.
     *  @returns PAGE_EXISTS if the page is rendered successfully, or
     *   NO_SUCH_PAGE if pageIndex specifies a non-existent page.
     *  @exception PrinterException If the print job is terminated.
     */
    public int print(Graphics graphics, PageFormat format,
            int index) throws PrinterException {
	if(_jgraph != null) {
            return _jgraph.print(graphics, format, index);
        } else return NO_SUCH_PAGE;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private inner classes                 ////
    
    // A layout algorithm for laying out ptolemy graphs.  Since our edges
    // are undirected, this layout algorithm turns them into directed edges
    // aimed consistently. i.e. An edge should always be "out" of an
    // internal output port and always be "in" of an internal input port.
    // Conversely, an edge is "out" of an external input port, and "in" of
    // an external output port.  The copying operation also flattens
    // the graph, because the level layout algorithm doesn't understand
    // how to layout hierarchical nodes.
    private class PtolemyLayout extends LevelLayout {
	
	/**
	 * Construct a new levelizing layout with a vertical orientation.
	 */
	public PtolemyLayout(LayoutTarget target) {
	    super(target);
	}

	/**
	 * Copy the given graph and make the nodes/edges in the copied
	 * graph point to the nodes/edges in the original.
	 */ 
	protected Object copyComposite(Object origComposite) {
	    LayoutTarget target = getLayoutTarget();
	    GraphModel model = target.getGraphModel();
	    diva.graph.basic.BasicGraphModel local = getLocalGraphModel();
	    Object copyComposite = local.createComposite(null);
	    HashMap map = new HashMap();
	    
	    // Copy all the nodes for the graph.
	    for(Iterator i = model.nodes(origComposite); i.hasNext(); ) {
		Object origNode = i.next();
		if(target.isNodeVisible(origNode)) {
		    Rectangle2D r = target.getBounds(origNode);
		    LevelInfo inf = new LevelInfo();
		    inf.origNode = origNode;
		    inf.x = r.getX();
		    inf.y = r.getY();
		    inf.width = r.getWidth();
		    inf.height = r.getHeight();
		    Object copyNode = local.createNode(inf);
		    local.addNode(this, copyNode, copyComposite);
		    map.put(origNode, copyNode);
		}
	    }
	    
	    // Add all the edges.
	    Iterator i = GraphUtilities.localEdges(origComposite, model); 
	    while(i.hasNext()) {
		Object origEdge = i.next();
		Object origTail = model.getTail(origEdge);
		Object origHead = model.getHead(origEdge);
		if(origHead != null && origTail != null) {
		    Figure tailFigure = 
			(Figure)target.getVisualObject(origTail);
		    Figure headFigure = 
			(Figure)target.getVisualObject(origHead);
		    // Swap the head and the tail if it will improve the 
		    // layout, since LevelLayout only uses directed edges.
		    if(tailFigure instanceof Terminal) {
			Terminal terminal = (Terminal)tailFigure;
			Site site = terminal.getConnectSite();
			if(site instanceof FixedNormalSite) {
			    double normal = site.getNormal();
			    int direction = 
				CanvasUtilities.getDirection(normal);
			    if(direction == SwingUtilities.WEST) {
				Object temp = origTail;
				origTail = origHead;
				origHead = temp;
			    }
			}
		    } else if(headFigure instanceof Terminal) {
			Terminal terminal = (Terminal)headFigure;
			Site site = terminal.getConnectSite();
			if(site instanceof FixedNormalSite) {
			    double normal = site.getNormal();
			    int direction = 
				CanvasUtilities.getDirection(normal);
			    if(direction == SwingUtilities.EAST) {
				Object temp = origTail;
				origTail = origHead;
				origHead = temp;
			    }
			}
		    }

		    origTail =
			_getParentInGraph(model, origComposite, origTail);
		    origHead = 
			_getParentInGraph(model, origComposite, origHead);
		    Object copyTail = map.get(origTail);
		    Object copyHead = map.get(origHead);

		    if(copyHead != null && copyTail != null) {
                        Object copyEdge = local.createEdge(origEdge);
                        local.setEdgeTail(this, copyEdge, copyTail);
                        local.setEdgeHead(this, copyEdge, copyHead);
 		    }
		}
	    }
	    
	    return copyComposite;
	}

	// Unfortunately, the head and/or tail of the edge may not 
	// be directly contained in the graph.  In this case, we need to
	// figure out which of their parents IS in the graph 
	// and calculate the cost of that instead.
	private Object _getParentInGraph(GraphModel model, 
					 Object graph, Object node) {
	    while(node != null && !model.containsNode(graph, node)) {
		Object parent = model.getParent(node);
		if(model.isNode(parent)) {
		    node = parent;
		} else {
		    node = null;
		}
	    }
	    return node;
	}
    }

    // A layout target that translates locatable nodes.
    private class PtolemyLayoutTarget extends BasicLayoutTarget {
	/**
	 * Construce a new layout target that operates
	 * in the given pane.
	 */
	public PtolemyLayoutTarget(GraphController controller) {
	    super(controller);
	}
    
	/**
	 * Translate the figure associated with the given node in the
	 * target's view by the given delta.
	 */
	public void translate(Object node, double dx, double dy) {
	    super.translate(node, dx, dy);
	    if(node instanceof Locatable) {
		double location[] = ((Locatable)node).getLocation();
		if(location == null) {
		    location = new double[2];
		    Figure figure = getController().getFigure(node);
		    location[0] = figure.getBounds().getCenterX();
		    location[1] = figure.getBounds().getCenterY();
		} else {
		    location[0] += dx;
		    location[1] += dy;
		}
		((Locatable)node).setLocation(location);
 	    }
	}
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Return a visual notation that can create a view on this document.
    // In this class, we search the toplevel entity in the model for a
    // Ptolemy notation attribute and return the first one found.
    //
    private VisualNotation _getVisualNotation(CompositeEntity model) {
	List notationList = model.attributeList(VisualNotation.class);
	Iterator notations = notationList.iterator();
	VisualNotation notation = null;
        if(notations.hasNext()) {
	    notation = (VisualNotation) notations.next();
	} else {
	    notation = new PtolemyNotation();
	}
	return notation;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private JGraph _jgraph = null;
    private JScrollPane _scrollPane = null;
}
