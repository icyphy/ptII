/* A drop target for the ptolemy editor.

 Copyright (c) 1999-2001 The Regents of the University of California.
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
@contributor Michael Shilman and Edward A. Lee
@version $Id$
*/
public class EditorDropTarget extends DropTarget {

    /** Construct a new graph target to operate on the given JGraph.
     *  @param graph The diva graph panel.
     */
    public EditorDropTarget(JGraph graph) {
        setComponent(graph);
        try {
            addDropTargetListener(new DTListener());
        } catch(java.util.TooManyListenersException wow) {}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public memebers                   ////

    /** The plain-text flavor that we will be using for our
     *  basic drag-and-drop protocol.
     */
    public static final DataFlavor TEXT_FLAVOR = DataFlavor.plainTextFlavor;

    /** The plain-text flavor that we will be using for our
     *  basic drag-and-drop protocol.
     */
    public static final DataFlavor STRING_FLAVOR = DataFlavor.stringFlavor;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The drop target listener used with the diva graph object.
     */
    private class DTListener implements DropTargetListener {

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /** Accept the event if the data is a known key.
         *  @param dtde The drop event.
         */
        public void dragEnter(DropTargetDragEvent dtde) {
            if(dtde.isDataFlavorSupported(PtolemyTransferable.namedObjFlavor)) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            }
            else {
                dtde.rejectDrag();
            }
        }

        /** Do nothing.
         *  @param dtde The drop event.
         */
        public void dragExit(DropTargetEvent dtde) {
        }

        /** Accept the event if the data is a known key.
         *  @param dtde The drop event.
         */
        public void dragOver(DropTargetDragEvent dtde) {
            dragEnter(dtde); //for now
        }

        /** If the data is recognized as a Ptolemy II object, then use the
         *  MoML description of the object to create a new instance of the
         *  object at the drop location.
         *  @param dtde The drop event.
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
                NamedObj dropObj = (NamedObj) iterator.next();
                // FIXME: Might consider giving a simpler name and then
                // displaying the classname in the icon.
                final String name = container.uniqueName(dropObj.getName());
                String moml = dropObj.exportMoML(name);

                // NOTE: Have to know whether this is an entity,
                // port, etc. This seems awkward.
                ChangeRequest request = null;
                if (dropObj instanceof ComponentEntity) {

                    // Dropped object is an entity.
                    request = new MoMLChangeRequest(this, container, moml) {
                        protected void _execute() throws Exception {
                            super._execute();
                            NamedObj newObject = container.getEntity(name);
                            _setLocation(newObject, point);
                        }
                    };

                } else if (dropObj instanceof Port) {

                    // Dropped object is a port.
                    request = new MoMLChangeRequest(
                            this, container, moml) {
                        protected void _execute() throws Exception {
                            super._execute();
                            NamedObj newObject = container.getPort(name);
                            _setLocation(newObject, point);
                        }
                    };

                } else if (dropObj instanceof Relation) {

                    // Dropped object is a relation.
                    request = new MoMLChangeRequest(
                            this, container, moml) {
                        protected void _execute() throws Exception {
                            super._execute();
                            NamedObj newObject = container.getRelation(name);
                            _setLocation(newObject, point);
                        }
                    };

                } else if (dropObj instanceof Attribute) {

                    // Dropped object is an attribute.
                    request = new MoMLChangeRequest(
                            this, container, moml) {
                        protected void _execute() throws Exception {
                            super._execute();
                            NamedObj newObject = container.getAttribute(name);
                            _setLocation(newObject, point);
                        }
                    };
                }

                // NOTE: If the drop object is not recognized, nothing
                // happens.  Is this the right behavior?
                if (request != null) {
                    container.requestChange(request);
                }
	    }
	    dtde.dropComplete(true); //success!
	}

        /** Accept the event if the data is a known key.
         *  @param dtde The drop event.
         */
        public void dropActionChanged(DropTargetDragEvent dtde) {
            dragEnter(dtde); //for now
        }

        ///////////////////////////////////////////////////////////////
        ////                     private methods                   ////

        // Create an icon if necessary for the specified object, and
        // set its location to the specified point.
        // Note that this needs to be done after the change request
        // that creates the object has succeeded.			
        private void _setLocation(NamedObj newObject, Point point)
               throws Exception {
            if (newObject == null) {
                throw new InternalErrorException(
                    "Dropped object not found after change completed!");
            }
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
    }
}
