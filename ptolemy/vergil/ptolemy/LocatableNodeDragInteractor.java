/* A drag interactor for locatable nodes

 Copyright (c) 1998-2001 The Regents of the University of California.
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

package ptolemy.vergil.ptolemy;

import ptolemy.moml.*;

import diva.graph.*;
import diva.canvas.Figure;
import diva.canvas.TransformContext;
import diva.canvas.connector.Connector;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.QuadrantConstraint;
import diva.canvas.interactor.SelectionModel;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import javax.swing.SwingConstants;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.gui.MessageHandler;
import ptolemy.vergil.toolbox.SnapConstraint;

//////////////////////////////////////////////////////////////////////////
//// LocatableNodeDragInteractor
/**
An interaction role that drags nodes that have locatable objects
as semantic objects.  When the node is dragged, this interactor
updates the location in the locatable object with the new location of the
figure.

@author Steve Neuendorffer
@version $Id$
 */
public class LocatableNodeDragInteractor extends NodeDragInteractor {

    /** Create a new interactor contained within the given controller.
     */
    public LocatableNodeDragInteractor(LocatableNodeController controller) {
	super(controller.getController());
	_controller = controller;

        Point2D origin = new Point(0,0);
        appendConstraint(new QuadrantConstraint(
                origin, SwingConstants.SOUTH_EAST));
        // NOTE: I don't know why the following is needed anymore,
        // but if it isn't here, dragging causes things to move random
        // amounts.  Beats me... EAL
        appendConstraint(new SnapConstraint());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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

        // NOTE: Unfortunately, vergil treats the location information
        // as representing the center of the figure.  This means that the
        // alignment of the figure is very sensitive to the label on the
        // top of the figure, since that will change where the center is.
        // It isn't the right thing to do to align the center of the figure
        // to the grid.  So we align to the zero point of the figure,
        // which is a little tough to get...

        Iterator targets = targets();
        double[] originalUpperLeft = null;
        while (targets.hasNext()) {
            Figure figure = (Figure) targets.next();
            originalUpperLeft = new double[2];
            AffineTransform transform
                    = figure.getTransformContext().getTransform();
            originalUpperLeft[0] = transform.getTranslateX();
            originalUpperLeft[1] = transform.getTranslateY();
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
                if(_controller.getController().getGraphModel().isNode(node)) {
                    if(_controller.hasLocation(node)) {
                        double[] location = _controller.getLocation(node);
                        location[0] += snapTranslation[0];
                        location[1] += snapTranslation[1];
                        _controller.setLocation(node, location);

                    } else {
                        double[] location = new double[2];
                        location[0] = figure.getBounds().getCenterX();
                        location[1] = figure.getBounds().getCenterY();
                        _controller.setLocation(node, location);
                    }
                }
            }
        } catch (IllegalActionException ex) {
            MessageHandler.error("could not set location", ex);
        }
    }

    private LocatableNodeController _controller;
}
