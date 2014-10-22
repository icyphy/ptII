/* A drag interactor for locatable nodes

 Copyright (c) 1998-2014 The Regents of the University of California.
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

 */
package ptolemy.vergil.basic;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ptolemy.kernel.Entity;
import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.RelativeLocation;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLUndoEntry;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.toolbox.SnapConstraint;
import diva.canvas.Figure;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.SelectionModel;
import diva.graph.GraphPane;
import diva.graph.NodeDragInteractor;
import diva.util.UserObjectContainer;

///////////////////////////////////////////////////////////////////
//// LocatableNodeDragInteractor

/**
 An interaction role that drags nodes that have locatable objects
 as semantic objects.  When the node is dragged, this interactor
 updates the location in the locatable object with the new location of the
 figure.
 <p>
 The dragging of a selection is undoable, and is based on the difference
 between the point where the mouse was pressed and where the mouse was
 released. This information is used to create MoML to undo the move if
 requested.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class LocatableNodeDragInteractor extends NodeDragInteractor {
    /** Create a new interactor contained within the given controller.
     *  @param controller The controller.
     */
    public LocatableNodeDragInteractor(LocatableNodeController controller) {
        super(controller.getController());
        _controller = controller;

        // Create a snap constraint with the default snap resolution.
        _snapConstraint = new SnapConstraint();
        appendConstraint(_snapConstraint);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** When the mouse is pressed before dragging, store a copy of the
     *  pressed point location so that a relative move can be
     *  evaluated for undo purposes.
     *  @param  e  The press event.
     */
    @Override
    public void mousePressed(LayerEvent e) {
        super.mousePressed(e);
        _dragStart = _getConstrainedPoint(e);
    }

    /** When the mouse is released after dragging, mark the frame modified
     *  and update the panner, and generate an undo entry for the move.
     *  If no movement has occurred, then do nothing.
     *  @param e The release event.
     */
    @Override
    public void mouseReleased(LayerEvent e) {

        // We should factor out the common code in this method and in
        // transform().
        // Work out the transform the drag performed.
        double[] dragEnd = _getConstrainedPoint(e);
        double[] transform = new double[2];
        transform[0] = _dragStart[0] - dragEnd[0];
        transform[1] = _dragStart[1] - dragEnd[1];

        if (transform[0] == 0.0 && transform[1] == 0.0) {
            return;
        }

        BasicGraphController graphController = (BasicGraphController) _controller
                .getController();
        BasicGraphFrame frame = graphController.getFrame();

        SelectionModel model = graphController.getSelectionModel();
        AbstractBasicGraphModel graphModel = (AbstractBasicGraphModel) graphController
                .getGraphModel();
        Object[] selection = model.getSelectionAsArray();
        Object[] userObjects = new Object[selection.length];

        // First get the user objects from the selection.
        for (int i = 0; i < selection.length; i++) {
            userObjects[i] = ((Figure) selection[i]).getUserObject();
        }

        // First make a set of all the semantic objects as they may
        // appear more than once
        HashSet<NamedObj> namedObjSet = new HashSet<NamedObj>();

        for (Object element : selection) {
            if (element instanceof Figure) {
                Object userObject = ((Figure) element).getUserObject();

                if (graphModel.isEdge(userObject)
                        || graphModel.isNode(userObject)) {
                    NamedObj actual = (NamedObj) graphModel
                            .getSemanticObject(userObject);

                    if (actual != null) {
                        namedObjSet.add(actual);
                    } else {
                        // Special case, may need to handle by not going to
                        // MoML and which may not be undoable.
                        // FIXME: This is no way to handle it...
                        System.out
                                .println("Object with no semantic object , class: "
                                        + userObject.getClass().getName());
                    }
                }
            }
        }

        // Generate the MoML to carry out move.
        // Note that the location has already been set by the mouseMoved()
        // call, but we need to do this so that the undo is generated and
        // so that the change propagates.
        // The toplevel is the container being edited.
        final NamedObj toplevel = (NamedObj) graphModel.getRoot();
        // The object situated under the location where the mouse was released
        NamedObj dropTarget = null;

        StringBuffer moml = new StringBuffer();
        StringBuffer undoMoml = new StringBuffer();
        moml.append("<group>\n");
        undoMoml.append("<group>\n");

        for (NamedObj element : namedObjSet) {
            List<?> locationList = element.attributeList(Locatable.class);

            if (locationList.isEmpty()) {
                // Nothing to do as there was no previous location
                // attribute (applies to "unseen" relations)
                continue;
            }
            Locatable locatable = (Locatable) locationList.get(0);
            // Use the MoML element name in case the location is a vertex
            String locationElementName = ((NamedObj) locatable)
                    .getElementName();
            String locationName = locatable.getName();
            // The location class, which can change if a relative location is dragged.
            String locationClazz = locatable.getClass().getName();
            // The new relativeTo property of the relative location.
            String newRelativeTo = "";
            String newRelativeToElementName = "";
            // The old relativeTo property of the relative location.
            String oldRelativeTo = "";
            String oldRelativeToElementName = "";

            // If locatable is an instance of RelativeLocation,
            // then its getLocation() method returns the absolute
            // location, not the relative location.
            double[] newLocation = null;
            if (locatable instanceof RelativeLocation) {
                RelativeLocation relativeLocation = (RelativeLocation) locatable;
                newLocation = relativeLocation.getRelativeLocation();
                oldRelativeTo = relativeLocation.relativeTo.getExpression();
                oldRelativeToElementName = relativeLocation.relativeToElementName
                        .getExpression();
            } else {
                newLocation = locatable.getLocation();
            }

            // NOTE: we use the transform worked out for the drag to
            // set the original MoML location.
            // Should do this before we break or create the relative location link.
            double[] oldLocation = new double[2];
            oldLocation[0] = newLocation[0] + transform[0];
            oldLocation[1] = newLocation[1] + transform[1];

            // RelativeLocatables can be dropped onto an object to create a
            // link to that object. In this case the location attribute is
            // replaced by a RelativeLocation that holds a relative offset.
            boolean changeRelativeTo = false;
            if (element instanceof RelativeLocatable) {
                if (dropTarget == null) {
                    // Find the drop target if not yet done. Pass the selection
                    // as filter so that objects from the selection are not chosen.
                    dropTarget = _getObjectUnder(new Point2D.Double(dragEnd[0],
                            dragEnd[1]), selection);
                }
                // Check to see whether the target is an Entity, and if it is,
                // then make the position relative to that entity. Also,
                // Do not accept relative locatables as drop target!! This could lead
                // to a cycle in the references, and ultimately to a stack overflow
                // when trying to compute the positions!
                // FIXME: Could make this check weaker by checking for cycles.
                if (dropTarget instanceof Entity
                        && !(dropTarget instanceof RelativeLocatable)) {
                    // Set the new values for the relativeTo properties.
                    newRelativeTo = dropTarget.getName();
                    newRelativeToElementName = dropTarget.getElementName();
                    // Change the location class!
                    // FIXME: This doesn't work with object-oriented classes!!!
                    locationClazz = RelativeLocation.class.getName();
                    // Now the location value is relative, so take a fixed offset.
                    newLocation = new double[] {
                            RelativeLocation.INITIAL_OFFSET,
                            RelativeLocation.INITIAL_OFFSET };
                    changeRelativeTo = true;
                } else if (oldRelativeTo.length() > 0 /* && newLocation != null*/) {
                    // We have no drop target, so check the current distance to the
                    // relativeTo object. If it exceeds a threshold, break the reference.
                    double distance = Math.sqrt(newLocation[0] * newLocation[0]
                            + newLocation[1] * newLocation[1]);
                    if (distance > RelativeLocation.BREAK_THRESHOLD) {
                        // Set the relativeTo property to the empty string.
                        changeRelativeTo = true;
                        // Request the absolute location for correct new placement.
                        newLocation = locatable.getLocation();
                    }
                }
            }

            // Give default values in case the previous locations value
            // has not yet been set
            if (newLocation == null) {
                newLocation = new double[] { 0, 0 };
            }

            // Create the MoML, wrapping the new location attribute
            // in an element referring to the container
            String containingElementName = element.getElementName();
            String elementToMove = "<" + containingElementName + " name=\""
                    + element.getName() + "\" >\n";
            moml.append(elementToMove);
            undoMoml.append(elementToMove);

            moml.append("<" + locationElementName + " name=\"" + locationName
                    + "\" class=\"" + locationClazz + "\" value=\"["
                    + newLocation[0] + ", " + newLocation[1] + "]\" >\n");
            undoMoml.append("<" + locationElementName + " name=\""
                    + locationName + "\" value=\"[" + oldLocation[0] + ", "
                    + oldLocation[1] + "]\" >\n");

            if (changeRelativeTo) {
                // Case 1: We have dragged onto another object. Create a reference to
                // the drop target and store it as properties of the location.
                // Case 2: We have dragged the locatable away from its relativeTo
                // object. In this case delete the reference to break the link.
                moml.append("<property name=\"relativeTo\" value=\""
                        + newRelativeTo + "\"/>");
                moml.append("<property name=\"relativeToElementName\" value=\""
                        + newRelativeToElementName + "\"/>");
                // The old reference must be restored upon undo.
                undoMoml.append("<property name=\"relativeTo\" value=\""
                        + oldRelativeTo + "\"/>");
                undoMoml.append("<property name=\"relativeToElementName\" value=\""
                        + oldRelativeToElementName + "\"/>");
            }

            moml.append("</" + locationElementName + ">\n");
            undoMoml.append("</" + locationElementName + ">\n");
            moml.append("</" + containingElementName + ">\n");
            undoMoml.append("</" + containingElementName + ">\n");
        }

        moml.append("</group>\n");
        undoMoml.append("</group>\n");

        final String finalUndoMoML = undoMoml.toString();

        // Request the change.
        MoMLChangeRequest request = new MoMLChangeRequest(this, toplevel,
                moml.toString()) {
            @Override
            protected void _execute() throws Exception {
                super._execute();

                // Next create and register the undo entry;
                // The MoML by itself will not cause an undo
                // to register because the value is not changing.
                // Note that this must be done inside the change
                // request because write permission on the
                // workspace is required to push an entry
                // on the undo stack. If this is done outside
                // the change request, there is a race condition
                // on the undo, and a deadlock could result if
                // the model is running.
                MoMLUndoEntry newEntry = new MoMLUndoEntry(toplevel,
                        finalUndoMoML);
                UndoStackAttribute undoInfo = UndoStackAttribute
                        .getUndoInfo(toplevel);
                undoInfo.push(newEntry);
            }
        };

        toplevel.requestChange(request);

        if (frame != null) {
            // NOTE: Use changeExecuted rather than directly calling
            // setModified() so that the panner is also updated.
            frame.changeExecuted(null);
        }
    }

    /** Specify the snap resolution. The default snap resolution is 5.0.
     *  @param resolution The snap resolution.
     */
    public void setSnapResolution(double resolution) {
        _snapConstraint.setResolution(resolution);
    }

    /** Drag all selected nodes and move any attached edges.
     *  Update the locatable nodes with the current location.
     *  @param e The event triggering this translation.
     *  @param x The horizontal delta.
     *  @param y The vertical delta.
     */
    @Override
    public void translate(LayerEvent e, double x, double y) {
        // NOTE: To get snap to grid to work right, we have to do some work.
        // It is not sufficient to quantize the translation.  What we do is
        // find the location of the first locatable node in the selection,
        // and find a translation for it that will lead to an acceptable
        // quantized position.  Then we use that translation.  This does
        // not ensure that all nodes in the selection get to an acceptable
        // quantized point, but there is no way to do that without
        // changing their relative positions.
        // NOTE: We cannot use the location attribute of the target objects
        // The problem is that the location as set during a drag is a
        // queued mutation.  So the translation we get isn't right.
        Iterator<?> targets = targets();
        double[] originalUpperLeft = null;

        while (targets.hasNext()) {
            Figure figure = (Figure) targets.next();
            originalUpperLeft = new double[2];
            originalUpperLeft[0] = figure.getOrigin().getX();
            originalUpperLeft[1] = figure.getOrigin().getY();

            // Only snap the first figure in the set.
            break;
        }

        double[] snapTranslation;

        if (originalUpperLeft == null) {
            // No location found in the selection, so we just quantize
            // the translation.
            double[] oldTranslation = new double[2];
            oldTranslation[0] = x;
            oldTranslation[1] = y;
            snapTranslation = _snapConstraint.constrain(oldTranslation);
        } else {
            double[] newUpperLeft = new double[2];
            newUpperLeft[0] = originalUpperLeft[0] + x;
            newUpperLeft[1] = originalUpperLeft[1] + y;

            double[] snapLocation = _snapConstraint.constrain(newUpperLeft);
            snapTranslation = new double[2];
            snapTranslation[0] = snapLocation[0] - originalUpperLeft[0];
            snapTranslation[1] = snapLocation[1] - originalUpperLeft[1];
        }

        // NOTE: The following seems no longer necessary, since the
        // translation occurs as a consequence of setting the location
        // attribute. However, for reasons that I don't understand,
        // without this, drag doesn't work.  The new location ends
        // up identical to the old because of the snap, so no translation
        // occurs.  Oddly, the superclass call performs a translation
        // even if the snapTranslation is zero.  Beats me.  EAL 7/31/02.
        super.translate(e, snapTranslation[0], snapTranslation[1]);

        // Set the location attribute of each item that is translated.
        // NOTE: this works only because all the nodes that allow
        // dragging are location nodes.  If nodes can be dragged that
        // aren't locatable nodes, then they shouldn't be able to be
        // selected at the same time as a locatable node.
        try {
            targets = targets();

            while (targets.hasNext()) {
                Figure figure = (Figure) targets.next();
                Object node = figure.getUserObject();

                if (_controller.getController().getGraphModel().isNode(node)) {
                    // NOTE: This used to get the location and then set it,
                    // but since the returned value is the internal array,
                    // then setLocation() believed there was no change,
                    // so the change would not be persistent.
                    double[] location = new double[2];
                    location[0] = figure.getOrigin().getX();
                    location[1] = figure.getOrigin().getY();
                    _controller.setLocation(node, location);
                }
            }
        } catch (IllegalActionException ex) {
            MessageHandler.error("could not set location", ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Returns a constrained point from the given event
    private double[] _getConstrainedPoint(LayerEvent e) {
        Iterator<?> targets = targets();
        double[] result = new double[2];

        if (targets.hasNext()) {
            //Figure figure = (Figure) targets.next();

            // The transform context is always (0,0) so no use
            // NOTE: this is a bit of hack, needed to allow the undo of
            // the movement of vertexes by themselves
            result[0] = e.getLayerX();
            result[1] = e.getLayerY();
            return _snapConstraint.constrain(result);
        }

        /*
         * else {
         * AffineTransform transform
         * = figure.getTransformContext().getTransform();
         * result[0] = transform.getTranslateX();
         * result[1] = transform.getTranslateY();
         * }
         * Only snap the first figure in the set.
         * break;
         */
        return result;
    }

    /** Return the figure that is an icon of a NamedObj and is
     *  under the specified point, or null if there is none.
     *
     *  This code is copied from {@link EditorDropTargetListener#_getFigureUnder(Point2D)}
     *  and modified for the new context.
     *
     *  @param point The point in the graph pane.
     *  @param filteredFigures figures that are filtered from the object search
     *  @return The object under the specified point, or null if there
     *   is none or it is not a NamedObj.
     */
    private Figure _getFigureUnder(Point2D point, final Object[] filteredFigures) {
        GraphPane pane = getController().getGraphPane();

        return BasicGraphFrame.getFigureUnder(pane, point, filteredFigures);
    }

    /** Return the object under the specified point, or null if there
     *  is none.
     *
     *  This code is copied from {@link EditorDropTargetListener#_getObjectUnder(Point2D)}.
     *
     *  @param point The point in the graph pane.
     *  @param filteredFigures figures that are filtered from the object search
     *  @return The object under the specified point, or null if there
     *   is none or it is not a NamedObj.
     */
    private NamedObj _getObjectUnder(Point2D point, Object[] filteredFigures) {
        Figure figureUnderMouse = _getFigureUnder(point, filteredFigures);

        if (figureUnderMouse == null) {
            return null;
        }

        Object objectUnderMouse = ((UserObjectContainer) figureUnderMouse)
                .getUserObject();

        // Object might be a Location, in which case we want its container.
        if (objectUnderMouse instanceof Location) {
            return ((NamedObj) objectUnderMouse).getContainer();
        } else if (objectUnderMouse instanceof NamedObj) {
            return (NamedObj) objectUnderMouse;
        }

        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private LocatableNodeController _controller;

    // Used to undo a locatable node movement
    private double[] _dragStart;

    // Locally defined snap constraint.
    private SnapConstraint _snapConstraint;
}
