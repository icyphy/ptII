/* A drop target for the ptolemy editor.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

package ptolemy.vergil.basic;

import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.JGraph;

import ptolemy.gui.MessageHandler;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.ImportAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.toolbox.PtolemyTransferable;
import ptolemy.vergil.toolbox.SnapConstraint;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.geom.Point2D;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// EditorDropTarget
/**
This class provides drag-and-drop support. When this drop target
receives a Transferable object containing a ptolemy entity, it creates
a new instance of the entity, and adds it to the given graph.

@author Steve Neuendorffer, Contributor: Michael Shilman and Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
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
    ////                         public members                   ////

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
            if (dtde.isDataFlavorSupported(PtolemyTransferable.namedObjFlavor)) {
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
            if (dtde.isDataFlavorSupported(
                    PtolemyTransferable.namedObjFlavor)) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    iterator = (Iterator)dtde.getTransferable().getTransferData(
                            PtolemyTransferable.namedObjFlavor);
                } catch(Exception e) {
                    MessageHandler.error(
                            "Can't find a supported data flavor for drop in "
                            + dtde,
                            e);
                    return;
                }
            } else {
                dtde.rejectDrop();
            }

            if (iterator == null) {
                // Nothing to drop!
                return;
            }

            Point2D originalPoint = SnapConstraint.constrainPoint(
                    dtde.getLocation());
            GraphPane pane = ((JGraph)getComponent()).getGraphPane();
            final Point2D point = new Point2D.Double();
            // Account for the scaling in the pane.
            try {
                pane.getTransformContext().getInverseTransform().transform(
                        originalPoint, point);
            } catch (Exception ex) {
                MessageHandler.error("Failed to get transform context.", ex);
                return;
            }

            final GraphController controller = pane.getGraphController();
            GraphModel model = controller.getGraphModel();
            final CompositeEntity toplevel = (CompositeEntity)model.getRoot();
            NamedObj container =
                MoMLChangeRequest.getDeferredToParent(toplevel);
            if (container == null) {
                container = toplevel;
            }

            while (iterator.hasNext()) {
                NamedObj dropObj = (NamedObj) iterator.next();
                final String name = toplevel.uniqueName(dropObj.getName());

                // Create the MoML command.
                StringBuffer moml = new StringBuffer();
                // If the dropObj defers to something else, then we
                // have to check the parent of the object
                // for import attributes, and then we have to
                // generate import statements.  Note that everything
                // imported by the parent will be imported now by
                // the object into which this is dropped.
                moml.append("<group>");
                if (container != toplevel) {
                    moml.append("<entity name=\""
                            + toplevel.getName(container) + "\">\n");
                }
                if (dropObj.getMoMLInfo().deferTo != null) {
                    CompositeEntity sourceContainer =
                        (CompositeEntity)dropObj.getContainer();
                    if (sourceContainer != null) {
                        Iterator imports = sourceContainer.attributeList(
                                ImportAttribute.class).iterator();
                        while (imports.hasNext()) {
                            // FIXME: does this code ever get called?
                            // There is no code in the tree that instantiates
                            // an ImportAttribute (8/02)
                            moml.append(((ImportAttribute)imports.next())
                                    .exportMoML());
                        }
                    }
                }
                moml.append(dropObj.exportMoML(name));
                if (container != toplevel) {
                    moml.append("</entity>");
                }
                moml.append("</group>");

                // NOTE: Have to know whether this is an entity,
                // port, etc. This seems awkward.
                MoMLChangeRequest request = null;
                if (dropObj instanceof ComponentEntity) {

                    // Dropped object is an entity.
                    // FIXME: Should use the parser from the PtolemyEffigy,
                    // so that undo will work.
                    request = new MoMLChangeRequest(
                            this, container, moml.toString()) {
                            protected void _execute() throws Exception {
                                super._execute();
                                NamedObj newObject = toplevel.getEntity(name);
                                _setLocation(name, newObject, point);
                            }
                        };

                } else if (dropObj instanceof Port) {

                    // Dropped object is a port.
                    request = new MoMLChangeRequest(
                            this, container, moml.toString()) {
                            protected void _execute() throws Exception {
                                super._execute();
                                NamedObj newObject = toplevel.getPort(name);
                                _setLocation(name, newObject, point);
                            }
                        };

                } else if (dropObj instanceof Relation) {

                    // Dropped object is a relation.
                    request = new MoMLChangeRequest(
                            this, container, moml.toString()) {
                            protected void _execute() throws Exception {
                                super._execute();
                                NamedObj newObject = toplevel.getRelation(name);
                                _setLocation(name, newObject, point);
                            }
                        };

                } else if (dropObj instanceof Attribute) {

                    // Dropped object is an attribute.
                    request = new MoMLChangeRequest(
                            this, container, moml.toString()) {
                            protected void _execute() throws Exception {
                                super._execute();
                                NamedObj newObject = toplevel.getAttribute(name);
                                _setLocation(name, newObject, point);
                            }
                        };
                }

                // NOTE: If the drop object is not recognized, nothing
                // happens.  Is this the right behavior?
                if (request != null) {
                    request.setUndoable(true);
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

        // Create a Location attribute if necessary for the specified object,
        // and set the location to the specified point.
        // Note that this needs to be done after the change request
        // that creates the object has succeeded.
        private void _setLocation(String name,
                NamedObj newObject, Point2D point)
                throws Exception {
            if (newObject == null) {
                throw new InternalErrorException("Dropped object '"
                        + name
                        + "' not found after "
                        + "change completed!");
            }
            // Constrain point to snap to grid.
            Point2D newPoint = SnapConstraint.constrainPoint(point);

            Locatable location = (Locatable)newObject.getAttribute("_location");
            // If there is no location, then manufacture one.
            if (location == null) {
                location = new Location(newObject, "_location");
            }

            double coords[] = new double[2];
            coords[0] = ((int)newPoint.getX());
            coords[1] = ((int)newPoint.getY());
            location.setLocation(coords);
        }
    }
}
