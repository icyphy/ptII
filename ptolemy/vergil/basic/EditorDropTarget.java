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

import java.awt.Color;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Singleton;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.kernel.AnimationRenderer;
import ptolemy.vergil.toolbox.PtolemyTransferable;
import ptolemy.vergil.toolbox.SnapConstraint;
import diva.canvas.CanvasComponent;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.util.UserObjectContainer;

//////////////////////////////////////////////////////////////////////////
//// EditorDropTarget
/**
This class provides drag-and-drop support. When this drop target
receives a transferable object containing a ptolemy entity, it creates
a new instance of the object, and adds it to the given graph.
If the drop location falls on top of an icon associated with an
instance of NamedObj, then the object is deposited inside that
instance (so the instance becomes its container). Otherwise,
the object is deposited inside the model associated with the
target graph. In either case, if the target container implements
the DropListener interface, then it is informed of the drop by
calling its dropped() method.

@author Steve Neuendorffer and Edward A. Lee, Contributor: Michael Shilman
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
    ////                         inner classes                     ////

    /** The drop target listener used with the diva graph object.
     */
    private class DTListener implements DropTargetListener {

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /** Accept the event if the data is a known key.
         *  This is called while a drag operation is ongoing,
         *  when the mouse pointer enters the operable part of
         *  the drop site for the DropTarget registered with
         *  this listener.
         *  @param dtde The drop event.
         */
        public void dragEnter(DropTargetDragEvent dtde) {
            if (dtde.isDataFlavorSupported(PtolemyTransferable.namedObjFlavor)) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            } else {
                dtde.rejectDrag();
            }
        }

        /** Remove any highlighting that might be active.
         *  This is called while a drag operation is ongoing, when the mouse
         *  pointer has exited the operable part of the drop site for the
         *  DropTarget registered with this listener. 
         *  @param dtde The drop event.
         */
        public void dragExit(DropTargetEvent dtde) {
            if (_highlighted != null) {
                _highlighter.renderDeselected(_highlightedFigure);
                _highlighted = null;
                _highlightedFigure = null;
            }
        }

        /** If the location of the event is over an icon for
         *  an instance of NamedObj, then highlight that icon.
         *  This is called when a drag operation is ongoing,
         *  while the mouse pointer is still over the operable
         *  part of the drop site for the DropTarget registered
         *  with this listener.
         *  @param dtde The drop event.
         */
        public void dragOver(DropTargetDragEvent dtde) {
            // See whether there is a container under the point.
            Point2D originalPoint = SnapConstraint.constrainPoint(
                    dtde.getLocation());
            NamedObj over = _getObjectUnder(originalPoint);
            if (over != _highlighted) {
                if (_highlighted != null) {
                    _highlighter.renderDeselected(_highlightedFigure);
                    _highlighted = null;
                    _highlightedFigure = null;
                }
                if (over != null) {
                    if (_highlighter == null) {
                        _highlighter = new AnimationRenderer(Color.white);
                    }
                    _highlighted = over;
                    _highlightedFigure = _getFigureUnder(originalPoint);
                    _highlighter.renderSelected(_highlightedFigure);
                }
            }
            // Used to do this... Any reason for it?
            // dragEnter(dtde);
        }

        /** If the transferrable object is recognized as a Ptolemy II object,
         *  then use the MoML description of the object to create a new
         *  instance of the object at the drop location. If the drop
         *  location is on top of an icon representing an instance of
         *  NamedObj, then make that instance the container of the new
         *  object. Otherwise, make the model associated with the graph
         *  the container.
         *  This is called when the drag operation has terminated with a
         *  drop on the operable part of the drop site for the DropTarget
         *  registered with this listener. 
         *  @param dtde The drop event.
         */
        public void drop(DropTargetDropEvent dtde) {
            
            // Unhighlight the target. Do this first in case
            // errors occur... Don't want to leave highlighting.
            if (_highlighted != null) {
                _highlighter.renderDeselected(_highlightedFigure);
                _highlighted = null;
                _highlightedFigure = null;
            }
            
            // See whether there is a container under the point.
            Point2D originalPoint = SnapConstraint.constrainPoint(
                    dtde.getLocation());
            NamedObj container = _getObjectUnder(originalPoint);
            
            GraphPane pane = ((JGraph)getComponent()).getGraphPane();

            if (container == null) {
                // Find the default container for the dropped object
                GraphController controller = pane.getGraphController();
                GraphModel model = controller.getGraphModel();
                container = (NamedObj)model.getRoot();
            }
            
            // Find the context for the MoML change request.
            NamedObj context = MoMLChangeRequest.getDeferredToParent(container);
            if (context == null) {
                context = container;
            }
            
            // Find the location for the dropped objects.
            // Account for the scaling in the pane.
            final Point2D transformedPoint = new Point2D.Double();
            pane.getTransformContext().getInverseTransform().transform(
                    originalPoint, transformedPoint);
            
            // Get an iterator over objects to drop.
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

            // Create the MoML change request to instantiate the new objects.
            // Count MoML change requests so that they can be undone all together.
            int count = 0;
            while (iterator.hasNext()) {
                count++;
                final NamedObj dropObj = (NamedObj) iterator.next();
                final String name;
                if (dropObj instanceof Singleton) {
                    name = dropObj.getName();
                } else {
                    name = container.uniqueName(dropObj.getName());
                }

                // Create the MoML command.
                StringBuffer moml = new StringBuffer();
                moml.append("<group>");
                if (container != context) {
                    moml.append("<");
                    moml.append(container.getMoMLInfo().elementName);
                    moml.append(" name=\""
                            + container.getName(context) + "\">\n");
                }
                /* Removed by EAL (9/28/03), which means the MoML import
                 * element is no longer supported.
                // If the dropObj defers to something else, then we
                // have to check the parent of the object
                // for import attributes, and then we have to
                // generate import statements.  Note that everything
                // imported by the parent will be imported now by
                // the object into which this is dropped.
                if (dropObj.getMoMLInfo().deferTo != null) {
                    CompositeEntity sourceContainer =
                        (CompositeEntity)dropObj.getContainer();
                    if (sourceContainer != null) {
                        Iterator imports = sourceContainer.attributeList(
                                ImportAttribute.class).iterator();
                        while (imports.hasNext()) {
                            // Does this code ever get called?
                            // There is no code in the tree that instantiates
                            // an ImportAttribute (8/02).
                            moml.append(((ImportAttribute)imports.next())
                                    .exportMoML());
                        }
                    }
                }
                */
                moml.append(dropObj.exportMoML(name));
                if (container != context) {
                    moml.append("</");
                    moml.append(container.getMoMLInfo().elementName);
                    moml.append(">");
                }
                moml.append("</group>");

                // NOTE: Have to know whether this is an entity,
                // port, etc. This seems awkward. The following
                // code needs refactoring, as there are four very
                // similar copies.
                MoMLChangeRequest request = null;
                final NamedObj finalContainer = container;
                
                if (dropObj instanceof ComponentEntity) {

                    // Dropped object is an entity.
                    request = new MoMLChangeRequest(
                        this, context, moml.toString()) {
                        protected void _execute() throws Exception {
                            // Errors will be reported to listeners as a consequence of
                            // throwing an exception here, so don't also report them
                            // via the error handler.
                            setReportErrorsToHandler(false);
                            try {
                                super._execute();
                            } catch (Throwable ex) {
                                throw new IllegalActionException(finalContainer, ex,
                                "Cannot drop " + dropObj.getName());
                            }
                            // If this succeeded, then the container is surely a
                            // a CompositeEntity.
                            if (finalContainer instanceof CompositeEntity) {
                                NamedObj newObject
                                        = ((CompositeEntity)finalContainer)
                                        .getEntity(name);
                                if (newObject != null) {
                                    _setLocation(name, newObject, transformedPoint);
                                }
                            }
                        }
                    };

                } else if (dropObj instanceof Port) {

                    // Dropped object is a port.
                    request = new MoMLChangeRequest(
                        this, context, moml.toString()) {
                        protected void _execute() throws Exception {
                            // Errors will be reported to listeners as a consequence of
                            // throwing an exception here, so don't also report them
                            // via the error handler.
                            setReportErrorsToHandler(false);
                            try {
                                super._execute();
                            } catch (Throwable ex) {
                                throw new IllegalActionException(finalContainer, ex,
                                "Cannot drop " + dropObj.getName());
                            }
                            // If this succeeded, then the container is surely an
                            // instance of Entity.
                            if (finalContainer instanceof Entity) {
                                NamedObj newObject
                                        = ((Entity)finalContainer)
                                        .getPort(name);
                                if (newObject != null) {
                                    _setLocation(name, newObject, transformedPoint);
                                }
                            }
                        }
                    };

                } else if (dropObj instanceof Relation) {

                    // Dropped object is a relation.
                    request = new MoMLChangeRequest(
                        this, context, moml.toString()) {
                        protected void _execute() throws Exception {
                            // Errors will be reported to listeners as a consequence of
                            // throwing an exception here, so don't also report them
                            // via the error handler.
                            setReportErrorsToHandler(false);
                            try {
                                super._execute();
                            } catch (Throwable ex) {
                                throw new IllegalActionException(finalContainer, ex,
                                "Cannot drop " + dropObj.getName());
                            }
                            // If this succeeded, then the container is surely
                            // an instance of CompositeEntity.
                            if (finalContainer instanceof CompositeEntity) {
                                NamedObj newObject
                                        = ((CompositeEntity)finalContainer)
                                        .getRelation(name);
                                if (newObject != null) {
                                    _setLocation(name, newObject, transformedPoint);
                                }
                            }
                        }
                    };

                } else if (dropObj instanceof Attribute) {

                    // Dropped object is an attribute.
                    request = new MoMLChangeRequest(
                        this, context, moml.toString()) {
                        protected void _execute() throws Exception {
                            // Errors will be reported to listeners as a consequence of
                            // throwing an exception here, so don't also report them
                            // via the error handler.
                            setReportErrorsToHandler(false);
                            try {
                                super._execute();
                            } catch (Throwable ex) {
                                throw new IllegalActionException(finalContainer, ex,
                                "Cannot drop " + dropObj.getName());
                            }
                            NamedObj newObject = finalContainer.getAttribute(name);
                            if (newObject != null) {
                                _setLocation(name, newObject, transformedPoint);
                            }
                        }
                    };
                }

                // NOTE: If the drop object is not recognized, nothing
                // happens.  Is this the right behavior?
                if (request != null) {
                    request.setUndoable(true);
                    if (count > 1) {
                        request.setMergeWithPreviousUndo(true);
                    }
                    container.requestChange(request);
                }
            }
            dtde.dropComplete(true); //success!
        }

        /** Accept the event if the data is a known key.
         *  This is called if the user has modified the current drop gesture. 
         *  @param dtde The drop event.
         */
        public void dropActionChanged(DropTargetDragEvent dtde) {
            // Used to do this... Not needed?
            // dragEnter(dtde);
        }

        ///////////////////////////////////////////////////////////////
        ////                     private methods                   ////

        /** Return the figure that is an icon of a NamedObj and is
         *  under the specified point, or null if there is none.
         *  @param point The point in the graph pane.
         *  @return The object under the specified point, or null if there
         *   is none or it is not a NamedObj.
         */
        private Figure _getFigureUnder(Point2D point) {
            GraphPane pane = ((JGraph)getComponent()).getGraphPane();
                                           
            // Account for the scaling in the pane.
            Point2D transformedPoint = new Point2D.Double();
            pane.getTransformContext().getInverseTransform().transform(
                    point, transformedPoint);
                       
            FigureLayer layer = pane.getForegroundLayer();
           
            // Find the figure under the point.
            // NOTE: Unfortunately, FigureLayer.getCurrentFigure() doesn't
            // work with a drop target (I guess it hasn't seen the mouse events),
            // so we have to use a lower level mechanism.
            double halo = layer.getPickHalo();
            double width = halo * 2;
            Rectangle2D region = new Rectangle2D.Double (
                    transformedPoint.getX() - halo,
                    transformedPoint.getY() - halo,
                    width, width);
            CanvasComponent figureUnderMouse = layer.pick(region);

            // Find a user object belonging to the figure under the mouse
            // or to any figure containing it (it may be a composite figure).
            Object objectUnderMouse = null;
            while (figureUnderMouse instanceof UserObjectContainer
                    && objectUnderMouse == null) {
                objectUnderMouse = ((UserObjectContainer)figureUnderMouse).getUserObject();
                if (objectUnderMouse instanceof NamedObj) {
                    if (figureUnderMouse instanceof Figure) {
                        return (Figure)figureUnderMouse;
                    }
                }
                figureUnderMouse = figureUnderMouse.getParent();
            }
            return null;
        }
        
        /** Return the object under the specified point, or null if there
         *  is none.
         *  @param point The point in the graph pane.
         *  @return The object under the specified point, or null if there
         *   is none or it is not a NamedObj.
         */
        private NamedObj _getObjectUnder(Point2D point) {
            Figure figureUnderMouse = _getFigureUnder(point);
            if (figureUnderMouse == null) {
                return null;
            }
            Object objectUnderMouse = ((UserObjectContainer)figureUnderMouse).getUserObject();
            // Object might be a Location, in which case we want its container.
            if (objectUnderMouse instanceof Location) {
                return (NamedObj)((NamedObj)objectUnderMouse).getContainer();
            } else if (objectUnderMouse instanceof NamedObj) {
                return (NamedObj)objectUnderMouse;
            }
            return null;
        }
        
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
        
        ///////////////////////////////////////////////////////////////
        ////                     private variables                 ////

        // Currently highlighted drop target.
        private NamedObj _highlighted = null;
        
        // Currently highlighted figure.
        private Figure _highlightedFigure = null;
        
        // The renderer used for highlighting.
        private AnimationRenderer _highlighter = null;
    }
}
