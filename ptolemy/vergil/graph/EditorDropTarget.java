/* A drop target for the ptolemy editor.

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

import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.JGraph;
import diva.gui.Application;

// FIXME: Replace these with per-class imports.
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
import ptolemy.kernel.*;
import ptolemy.gui.MessageHandler;
import ptolemy.moml.Icon;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.toolbox.EditorIcon;
import ptolemy.vergil.toolbox.PtolemyTransferable;

//////////////////////////////////////////////////////////////////////////
//// EditorDropTarget
/**
This class provides drag-and-drop support. When this drop target
receives a Transferable object containing a ptolemy entity, it creates
a new instance of the entity, and adds it to the given graph.

@author Steve Neuendorffer
@contributor Michael Shilman
@version $Id$
*/
public class EditorDropTarget extends DropTarget {

    /** Construct a new graph target to operate on the given JGraph.
     *  FIXME: @param tags.
     */
    public EditorDropTarget(JGraph g) {
        setComponent(g);
        try {
            addDropTargetListener(new DTListener());
        } catch(java.util.TooManyListenersException wow) {}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * A drop target listener that comprehends
     * the different available keys.
     */
    private class DTListener implements DropTargetListener {
        /**
         * Accept the event if the data is a known key.
         */
        public void dragEnter(DropTargetDragEvent dtde) {
            if(dtde.isDataFlavorSupported(PtolemyTransferable.namedObjFlavor)) {
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
	    Iterator iterator = null;
            if(dtde.isDataFlavorSupported(PtolemyTransferable.namedObjFlavor)) {
		try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		    iterator = (Iterator)dtde.getTransferable().
			getTransferData(PtolemyTransferable.namedObjFlavor);
		} catch(Exception e) {
		    MessageHandler.error(
                        "Couldn't find a supported data flavor in " + dtde, e);
		}
            } else {
                dtde.rejectDrop();
            }

            if(iterator == null) {
		// Nothing to drop!
		return;
            }

	    final Point point = dtde.getLocation();
	    final GraphController controller =
		((JGraph)getComponent()).getGraphPane().getGraphController();
	    GraphModel model = controller.getGraphModel();
	    final CompositeEntity container = (CompositeEntity)model.getRoot();
	    while(iterator.hasNext()) {
		NamedObj data = (NamedObj) iterator.next();
                // FIXME: Might consider giving a simpler name and then
                // displaying the classname in the icon.
                final String name = container.uniqueName(data.getName());
                String moml = data.exportMoML(name);

                ChangeRequest request = new MoMLChangeRequest(this,
                        container, moml) {
                    protected void _execute() throws Exception {
			super._execute();
			// Set the location of the icon.
			// Note that this really needs to be done after
			// the change request has succeeded, which is why
			// it is done here.  When the graph controller
			// gets around to handling this, it will draw 
			// the icon at this location.
			
			// FIXME: Have to know whether this is an entity,
			// port, etc. For now, assuming it is an entity.
			NamedObj newObject = container.getEntity(name);
			Icon icon = (Icon) newObject.getAttribute("_icon");
			// If there is no icon, then manufacture one.
			if(icon == null) {
			    icon = new EditorIcon(newObject, "_icon");
			}
			
			double location[] = new double[2];
			location[0] = ((int)point.x);
			location[1] = ((int)point.y);
			icon.setLocation(location);
		    }
                };

                container.requestChange(request);
	    }
	    dtde.dropComplete(true); //success!
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
    public static final DataFlavor TEXT_FLAVOR = DataFlavor.plainTextFlavor;
    public static final DataFlavor STRING_FLAVOR = DataFlavor.stringFlavor;
}
