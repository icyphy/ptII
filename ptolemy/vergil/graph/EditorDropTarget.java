/* A drop target for the ptolemy editor.

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

package ptolemy.vergil.graph;

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
import ptolemy.moml.Icon;
import ptolemy.vergil.toolbox.*;

//////////////////////////////////////////////////////////////////////////
//// EditorDropTarget
/**
This class provides drag-and-drop support.
When this drop target receives a Transferable object
containing a ptolemy entity, it clones the entity, and
adds it to the given graph.

@author Steve Neuendorffer
@contributor Michael Shilman
@version $Id$
*/
public class EditorDropTarget extends DropTarget {
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
            if(dtde.isDataFlavorSupported(GraphPalette.nodeFlavor)) {
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

            if(dtde.isDataFlavorSupported(GraphPalette.nodeFlavor)) {
                try {
		    // System.out.println(GraphPalette.nodeFlavor);
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		    data = (CompositeNode)dtde.getTransferable().
			getTransferData(GraphPalette.nodeFlavor);
		    // System.out.println("Data is [" + data + "]");//DEBUG
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
                GraphController gc =
		    ((JGraph)getComponent()).getGraphPane().getGraphController();
		Icon sourceIcon = (Icon) data.getSemanticObject();
                NamedObj sourceEntity = (NamedObj) sourceIcon.getContainer();
                CompositeNode newNode;
                Graph graph;

                try {
                    // Figure out where this is going, so we can clone into
                    // the right workspace.
                    graph = gc.getGraph();
                    NamedObj container = (NamedObj) graph.getSemanticObject();
                    // Create the new node
                    NamedObj entity = (NamedObj) sourceEntity.clone(
                            container.workspace());
		    Icon icon = (Icon) entity.getAttribute("_icon");
                    // FIXME it would be nice if this was not editor specific.
                    entity.setName(container.uniqueName(
                            sourceEntity.getName()));
		    ((EditorGraphController) gc).getEntityController()
                         .addNode(icon, p.x, p.y);
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

    /**
     * The plain-text flavor that we will be using for our
     * basic drag-and-drop protocol.
     */
    static final DataFlavor TEXT_FLAVOR = DataFlavor.plainTextFlavor;
    static final DataFlavor STRING_FLAVOR = DataFlavor.stringFlavor;
}
