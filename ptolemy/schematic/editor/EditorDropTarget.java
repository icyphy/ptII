package ptolemy.schematic.editor;

import diva.graph.*;
import diva.graph.model.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import ptolemy.kernel.util.*;

/**
 * This class provides drag-and-drop support for the
 * ptolemy gui.  When this drop target receives a Transferable object 
 * containing a schematic entity object, it clones the entity, and 
 * adds it to the graph editor.
 *
 * @author Steve Neuendorffer
 * @version $Id$
 * @rating Red
 */
public class EditorDropTarget extends DropTarget {
    /**
     * The plain-text flavor that we will be using for our
     * basic drag-and-drop protocol.
     */
    static final DataFlavor TEXT_FLAVOR = DataFlavor.plainTextFlavor;
    static final DataFlavor STRING_FLAVOR = DataFlavor.stringFlavor;

    /**
     * Construct a new graph target to operate
     * on the given JGraph.
     */
    public EditorDropTarget(JGraph g) {
        setComponent(g);
        try {
            addDropTargetListener(new DTListener());
        }
        catch(java.util.TooManyListenersException wow) {
        }
    }

    /**
     * A drop target listener that comprehends
     * the different available keys.
     */
    private class DTListener implements DropTargetListener {
        /**
         * Accept the event if the data is a known key.
         */
        public void dragEnter(DropTargetDragEvent dtde) {
            if(dtde.isDataFlavorSupported(SchematicPalette.nodeFlavor)) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            }
            else {
                dtde.rejectDrag();
            }                
        }

        /**
         * Do nothing.
         */
        public void dragExit(DropTargetEvent dtde) {
        }

        /**
         * Accept the event if the data is a known key.
         */
        public void dragOver(DropTargetDragEvent dtde) {
            dragEnter(dtde); //for now
        }

        /**
         * Accept the event if the data is a known key;
         * clone the associated figure and place it in the
         * graph editor.
         */
        public void drop(DropTargetDropEvent dtde) {
            CompositeNode data = null;

            if(dtde.isDataFlavorSupported(SchematicPalette.nodeFlavor)) {
                try {
		    System.out.println(SchematicPalette.nodeFlavor);
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		    data = (CompositeNode)dtde.getTransferable().
			getTransferData(SchematicPalette.nodeFlavor);
                    System.out.println("Data is [" + data + "]");//DEBUG
                }
                catch(Exception e) {
                    System.out.println(e);//DEBUG
                    e.printStackTrace();
                }
            }
            else {
                dtde.rejectDrop();
            }
            
            if(data == null) {
                System.out.println("Drop failure"); //DEBUG
                dtde.dropComplete(false); //failure!
            }
            else {
                Point p = dtde.getLocation();
                System.out.println("Dropping at " + p); //DEBUG
                GraphController gc = 
		    ((JGraph)getComponent()).getGraphPane().getGraphController();
                NamedObj semanticObject = (NamedObj) data.getSemanticObject();
                GraphImpl impl = gc.getGraphImpl();
                CompositeNode newNode;

                try {
                    newNode = impl.createCompositeNode(semanticObject.clone());
                    NamedObj newObject = (NamedObj)newNode.getSemanticObject();
		    newObject.setName(semanticObject.getName() + 
				      ((EditorGraphController)gc).createUniqueID());
		    ((EditorGraphController) gc).addNode(newNode, p.x, p.y);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex.getMessage());
                }

		dtde.dropComplete(true); //success!
            }
        }

        /**
         * Accept the event if the data is a known key.
         */
        public void dropActionChanged(DropTargetDragEvent dtde) {
            dragEnter(dtde); //for now
        }
    }
}
