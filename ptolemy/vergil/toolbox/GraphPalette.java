/* A palette of enitities for the ptolemy schematic editor

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

package ptolemy.vergil.toolbox;

import ptolemy.kernel.*;
import ptolemy.vergil.graph.*;
import java.io.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.*;
import diva.graph.*;
import diva.graph.model.*;
import diva.graph.layout.*;
import diva.canvas.*;
import diva.canvas.interactor.*;
import diva.canvas.event.*;
import diva.gui.*;
import diva.gui.toolbox.*;

//////////////////////////////////////////////////////////////////////////
//// GraphPalette
/**
A palette of entities for the ptolemy schematic editor.  When nodes are
dragged in this palette, instead of moving the entity, a swing drag and
drop action is started, which can drop the entity on another canvas
with an appropriate drop target.  Drop targets will most likely
support the GraphPalette.nodeFlavor data flavor in order to get a
reference to the dropped node.

@author Steve Neuendorffer
@contributor Michael Shilman
@version $Id$
*/
public class GraphPalette extends JGraph {
    /**
     * Construct a new GraphPalette with a default empty graph
     */
    public GraphPalette() {
        super();
	_controller = new PaletteController(this);
	GraphPane pane = new GraphPane(_controller, new BasicGraphImpl());
	setGraphPane(pane);
	makeDraggable(this);
    }

    public void addNode(Object object, double x, double y) {
	_controller._entityController.addNode(object, x, y);
    }

    public void addEntity(Entity e, double x, double y) {
        Node node = new BasicCompositeNode();
	node.setSemanticObject(e);
        _controller._entityController.addNode(node, x, y);
    }

    public Node getDraggedNode() {
        return _draggedNode;
    }

    /**
     * Make the given component draggable; the given data string will
     * be the "dragged" object that is associated with the component.
     * This data is made available via the NodeTransferable class.
     */
    public void makeDraggable(JComponent c) {
        final EditorDragGestureListener dgl = new EditorDragGestureListener();
	dgl.setPalette(this);

        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
                c, DnDConstants.ACTION_COPY_OR_MOVE,
		(DragGestureListener) dgl);
    }

    public void setDraggedNode(Node node) {
	_draggedNode = node;
    }

    public void validate() {
	super.validate();
	int delay = 100;
	if(_timer == null) {
	    Timer timer = new Timer(delay, new LayoutListener());
	    timer.setRepeats(false);
	    timer.start();
	}
    }

    /////////////////////////////////////////////////////////////
    //                    Inner Classes                        //

    public class LayoutListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
	    LayoutTarget target = new BasicLayoutTarget(_controller);
            Graph graph = _controller.getGraph();
            GlobalLayout layout = new GridAnnealingLayout();
            layout.layout(target, graph);
            repaint();
	    _timer = null;
        }
    }

    public class PaletteController extends GraphController {
	public PaletteController(GraphPalette palette) {
	    _portController = new PortController(this);
	    _entityController = new EntityController(this);
	    DragInteractor di = new NodeDnDInteractor(palette);
	    _entityController.setNodeInteractor(di);
	    di.setConsuming(false);
	}

        public void clearEdge(Edge edge) {
            throw new GraphException("PaletteController does not allow edges");
        }

        public void clearNode(Node node) {
            _entityController.clearNode(node);
        }

        public Figure drawEdge(Edge edge) {
            throw new GraphException("PaletteController does not allow edges");
        }

        public Figure drawNode(Node node) {
            return _entityController.drawNode(node);
        }

        public void removeEdge(Edge edge) {
            throw new GraphException("PaletteController does not allow edges");
        }

        public void removeNode(Node node) {
            _entityController.removeNode(node);
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
      	}

	public class NodeDnDInteractor extends DragInteractor {
	    /** The Palette that this interactor is a part of.
	     */
	    private GraphPalette _palette;

	    /** Create a new NodeDragInteractor and give it a pointer
	     * to its controller to it can find other useful objects
	     */
	    public NodeDnDInteractor(GraphPalette palette) {
                _palette = palette;
	    }

            /** Respond to a mouse press in the palette by setting the
             *  palette's dragged node.
             */
            public void mousePressed(LayerEvent layerEvent) {
                Figure draggedFigure = layerEvent.getFigureSource();
                _palette.setDraggedNode((Node)draggedFigure.getUserObject());
            }

            /** Respond to a mouse press in the palette by resetting the
             * palette's dragged node.
             */
            public void mouseReleased(LayerEvent layerEvent) {
                _palette.setDraggedNode(null);
            }

            /** Respond to a translate request.  Don't actually do anything,
             *  since the palette uses swing drag and drop instead of
             *  figure translation.
             */
            public void translate(LayerEvent e, double x, double y) {
                // This drag interactor doesn't actually drag.
            }
	}

	private EntityController _entityController;
	private PortController _portController;
    }

    /** A transferable object that contains a local JVM reference to a
     *  a node.
     */
    public class NodeTransferable implements Transferable {
	public NodeTransferable(Node node) {
	    _node = node;
	}

	public synchronized DataFlavor[] getTransferDataFlavors() {
	    return _flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
	    int i;
	    for(i = 0; i < _flavors.length; i++)
		if(_flavors[i].equals(flavor)) return true;
	    return false;
	}

	public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
	    if (flavor.equals(DataFlavor.plainTextFlavor)) {
		return new ByteArrayInputStream(_node.toString().
                        getBytes("Unicode"));
	    } else if(flavor.equals(GraphPalette.nodeFlavor)) {
		return _node;
	    } else if(flavor.equals(DataFlavor.stringFlavor)) {
		return _node.toString();
	    }
	    throw new UnsupportedFlavorException(flavor);
	}

	public final DataFlavor[] _flavors = {
	    DataFlavor.plainTextFlavor,
	    DataFlavor.stringFlavor,
	    GraphPalette.nodeFlavor,
	};
	private Node _node;
    }

    public class EditorDragGestureListener implements DragGestureListener
    {
	public void dragGestureRecognized(DragGestureEvent e) {
	    final DragSourceListener dsl = new DragSourceListener() {
		public void dragDropEnd(DragSourceDropEvent dsde) {}
		public void dragEnter(DragSourceDragEvent dsde) {
		    DragSourceContext context = dsde.getDragSourceContext();
		    //intersection of the users selected action, and the
		    //source and target actions
		    int myaction = dsde.getDropAction();
		    if( (myaction & DnDConstants.ACTION_COPY_OR_MOVE) != 0) {
			context.setCursor(DragSource.DefaultCopyDrop);
		    } else {
			context.setCursor(DragSource.DefaultCopyNoDrop);
		    }
		}
		public void dragExit(DragSourceEvent dse) {}
		public void dragOver(DragSourceDragEvent dsde) {}
		public void dropActionChanged(DragSourceDragEvent dsde) {}
	    };

	    try {
		if(_palette.getDraggedNode() == null) return;
		// check to see if action is OK ...
		NodeTransferable transferable =
		    new NodeTransferable(_palette.getDraggedNode());

                Node node = _palette.getDraggedNode();

		//initial cursor, transferable, dsource listener
		e.startDrag(DragSource.DefaultCopyNoDrop,
                        transferable, dsl);

                // reset the dragged node, so we can't drag again.
                _palette.setDraggedNode(null);
	    } catch (InvalidDnDOperationException idoe) {
		System.err.println( idoe );
                idoe.printStackTrace();
	    }
	}
	public void setPalette(GraphPalette palette) {
	    _palette = palette;
	}
	public GraphPalette getPalette() {
	    return _palette;
	}
	private GraphPalette _palette;
    };


    ///////////////////////////////////////////////////////////////
    //                      Data Members                         //
    public static final DataFlavor nodeFlavor =
    new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
            "diva.graph.Node", "divanode");

    private Node _draggedNode;
    private PaletteController _controller;
    private Timer _timer;


}


