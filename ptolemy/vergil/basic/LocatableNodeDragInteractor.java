/* A drag interactor for locatable nodes

 Copyright (c) 1998-2003 The Regents of the University of California.
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

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLUndoEntry;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.toolbox.SnapConstraint;
import diva.canvas.Figure;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.SelectionModel;
import diva.graph.NodeDragInteractor;

//////////////////////////////////////////////////////////////////////////
//// LocatableNodeDragInteractor
/**
An interaction role that drags nodes that have locatable objects
as semantic objects.  When the node is dragged, this interactor
updates the location in the locatable object with the new location of the
figure.
<p>
The dragging of a selection is undoable, and is based on the difference
between the point where the mouse was pressed and where the mouse was
released. This informatio is used to create MoML to undo the move if
requested.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.0
 */
public class LocatableNodeDragInteractor extends NodeDragInteractor {

    /** Create a new interactor contained within the given controller.
     */
    public LocatableNodeDragInteractor(LocatableNodeController controller) {
        super(controller.getController());
        _controller = controller;

        Point2D origin = new Point(0,0);
        // NOTE: The quadrant constraint is not needed anymore with
        // new zoom capability.
        // appendConstraint(new QuadrantConstraint(
        //        origin, SwingConstants.SOUTH_EAST));
        // NOTE: I don't know why the following is needed anymore,
        // but if it isn't here, dragging causes things to move random
        // amounts.  Beats me... EAL
        appendConstraint(new SnapConstraint());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** When the mouse is pressed before dragging, store a copy of the
     *  pressed point location so that a relative move can be
     *  evaluated for undo purposes.
     *  @param  e  The press event.
     */
    public void mousePressed(LayerEvent e) {
        super.mousePressed(e);
        _dragStart = _getConstrainedPoint(e);
    }


    /** When the mouse is released after dragging, mark the frame modified
     *  and update the panner, and generate an undo entry for the move.
     *  If no movement has occured, then do nothing.
     *  @param e The release event.
     */
    public void mouseReleased(LayerEvent e) {
        // We should factor out the common code in this method and in
        // transform().

        // Work out the transform the drag performed.
        double[] dragEnd = _getConstrainedPoint(e);
        double[] transform = new double[2];
        transform[0] = _dragStart[0] - dragEnd[0];
        transform[1] = _dragStart[1] - dragEnd[1];
        
        if (transform[0] == 0.0 && transform[1] == 0.0) return;

        // Note that this now goes through the MoML parser so it is
        // undoable.
        
        BasicGraphController graphController
                = (BasicGraphController)_controller.getController();
        BasicGraphFrame frame
                = graphController.getFrame();

        SelectionModel model
                = graphController.getSelectionModel();
        AbstractBasicGraphModel graphModel
                = (AbstractBasicGraphModel)graphController.getGraphModel();
        Object selection[]
                = model.getSelectionAsArray();
        Object userObjects[]
                = new Object[selection.length];
        // First get the user objects from the selection.
        for (int i = 0; i < selection.length; i++) {
            userObjects[i] = ((Figure)selection[i]).getUserObject();
        }

        // First make a set of all the semantic objects as they may
        // appear more than once
        HashSet namedObjSet = new HashSet();
        for (int i = 0; i < selection.length; i++) {
            if (selection[i] instanceof Figure) {
                Object userObject = ((Figure)selection[i]).getUserObject();
                if (graphModel.isEdge(userObject) ||
                        graphModel.isNode(userObject)) {
                    NamedObj actual
                            = (NamedObj)graphModel.getSemanticObject(userObject);
                    if (actual != null) {
                        namedObjSet.add(actual);
                    } else {
                        // Special case, may need to handle by not going to
                        // MoML and which may not be undoable.
                        // FIXME: This is no way to handle it...
                        System.out.println(
                                "Object with no semantic object , class: "
                                + userObject.getClass().getName());
                    }
                }
            }
        }

        // Generate the MoML to carry out undo.
        StringBuffer moml = new StringBuffer();
        moml.append("<group>\n");
        Iterator elements = namedObjSet.iterator();
        while (elements.hasNext()) {
            NamedObj element = (NamedObj)elements.next();
            List locationList = element.attributeList(Location.class);
            if (locationList.isEmpty()) {
                // Nothing to do as there was no previous location
                // attribute (applies to "unseen" relations)
                continue;
            }
            // Set the new location attribute.
            Location newLoc = (Location)locationList.get(0);
            // Give default values in case the previous locations value
            // has not yet been set
            double[] newLocation = new double[]{0, 0};
            if (newLoc.getLocation() != null) {
                newLocation = newLoc.getLocation();
            }
            // NOTE: we use the trasform worked out for the drag to
            // set the original MoML location
            double[] oldLocation = new double[2];
            oldLocation[0] = newLocation[0] + transform[0];
            oldLocation[1] = newLocation[1] + transform[1];
            // Create the MoML, wrapping the new location attribute
            // in an element refering to the container
            String containingElementName = element.getMoMLInfo().elementName;
            moml.append("<" + containingElementName + " name=\"" +
                    element.getName() + "\" >\n");
            // NOTE: use the moml info element name here in case the
            // location is a vertex
            moml.append("<" + newLoc.getMoMLInfo().elementName + " name=\"" +
                    newLoc.getName() + "\" value=\"" + oldLocation[0] + ", " +
                    oldLocation[1] + "\" />\n");
            moml.append("</" + containingElementName + ">\n");
        }
        moml.append("</group>\n");

        // Next create and register the undo entry;
        NamedObj toplevel = (NamedObj)graphModel.getRoot();
        MoMLUndoEntry newEntry = new MoMLUndoEntry(toplevel, moml.toString());
        UndoStackAttribute undoInfo = UndoStackAttribute.getUndoInfo(toplevel);
        undoInfo.push(newEntry);
        
        if (frame != null) {
            // NOTE: Use changeExecuted rather than directly calling
            // setModified() so that the panner is also updated.
            frame.changeExecuted(null);
        }
        
        // Finally to notify to get other views to update.
        // FIXME: Hack: Surely there is a better way to do this
        // than to create an empty change request!
        toplevel.requestChange(new MoMLChangeRequest(
                this, toplevel, "<group/>"));
    }

    /** Drag all selected nodes and move any attached edges.
     *  Update the locatable nodes with the current location.
     *  @param e The event triggering this translation.
     *  @param x The horizontal delta.
     *  @param y The vertical delta.
     */
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

        Iterator targets = targets();
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
            snapTranslation = SnapConstraint.constrainPoint(oldTranslation);
        } else {
            double[] newUpperLeft = new double[2];
            newUpperLeft[0] = originalUpperLeft[0] + x;
            newUpperLeft[1] = originalUpperLeft[1] + y;
            double[] snapLocation = SnapConstraint.constrainPoint(newUpperLeft);
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
                    if (_controller.hasLocation(node)) {
                        double[] location = _controller.getLocation(node);
                        location[0] += snapTranslation[0];
                        location[1] += snapTranslation[1];
                        _controller.setLocation(node, location);

                    } else {
                        double[] location = new double[2];
                        location[0] = figure.getOrigin().getX();
                        location[1] = figure.getOrigin().getY();
                        _controller.setLocation(node, location);
                    }
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
        Iterator targets = targets();
        double[] result = new double[2];
        if (targets.hasNext()) {
            Figure figure = (Figure)targets.next();
            // The transform context is always (0,0) so no use
            // NOTE: this is a bit of hack, needed to allow the undo of
            // the movement of vertexes by themselves
            result[0] = e.getLayerX();
            result[1] = e.getLayerY();
            return SnapConstraint.constrainPoint(result);
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private LocatableNodeController _controller;

    // Used to undo a locatable node movement
    private double[] _dragStart;
}
