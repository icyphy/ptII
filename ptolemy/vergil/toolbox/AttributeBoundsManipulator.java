/* A manipulator for resizable icons.

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

package ptolemy.vergil.toolbox;

import java.awt.geom.Rectangle2D;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import diva.canvas.CanvasUtilities;
import diva.canvas.Figure;
import diva.canvas.FigureDecorator;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.BoundsGeometry;
import diva.canvas.interactor.BoundsManipulator;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.GrabHandle;

//////////////////////////////////////////////////////////////////////////
//// AttributeBoundsManipulator
/**
This is a bounds manipulator supporting resizable icons.
It records the new size when the mouse is released, and supports
snap to grid.
*/
public class AttributeBoundsManipulator extends BoundsManipulator {

    /** Construct a new bounds manipulator.
     *  @param container The container of the icon to be manipulated.
     */
    public AttributeBoundsManipulator(NamedObj container) {
        super();
        _container = container;
        // To get resizing to snap to grid, use a custom resizer,
        // rather than the one provided by the base class.
        setHandleInteractor(new Resizer());
    }

    /////////////////////////////////////////////////////////////////////////
    ////                        public methods                           ////

    /** Make a persistent record of the new size by issuing a change request.
     *  @param e The mouse event.
     */
    public void mouseReleased(LayerEvent e) {
        Figure child = getChild();
        // FIXME: Diva has a bug where this method is called on the
        // prototype rather than the instance that has a child.
        // We work around this by getting access to the instance.
        if (child == null && _instanceDecorator != null) {
            child = _instanceDecorator.getChild();
        }
        if (child != null) {
            // NOTE: Calling getBounds() on the child itself yields an
            // inaccurate bounds, for some reason.
            // Weirdly, to get the size right, we need to use this.
            // But to get the location right, we need the other!
            Rectangle2D bounds = child.getShape().getBounds2D();
            Rectangle2D childBounds = child.getBounds();

            // Use a MoMLChangeRequest here so that the resize can be
            // undone and so that a repaint occurs.

            Attribute widthParameter = _container.getAttribute("width");
            Attribute heightParameter = _container.getAttribute("height");
            Attribute locationParameter = _container.getAttribute("_location");

            // Proceed only if the container has these parameters.
            if (widthParameter != null && heightParameter != null) {
                StringBuffer command =
                    new StringBuffer("<group><property name =\"width\" value=\"");
                // FIXME: Force to integer values only. Is this a good idea?
                command.append(Math.rint(bounds.getWidth()));
                command.append("\"/><property name =\"height\" value=\"");
                command.append(Math.rint(bounds.getHeight()));
                command.append("\"/>");

                // Location needs to change too if dragged left or down.
                if (locationParameter != null) {
                    // Weirdly, to get the location right, need to use
                    // these bounds.
                    double newX = childBounds.getX();
                    double newY = childBounds.getY();
                    // If the figure is centered, have to use the center
                    // instead.
                    try {
                        Attribute centered =
                            _container.getAttribute(
                                "centered",
                                Parameter.class);
                        if (centered != null) {
                            boolean isCentered =
                                ((BooleanToken) ((Parameter) centered)
                                    .getToken())
                                    .booleanValue();
                            if (isCentered) {
                                newX = childBounds.getCenterX();
                                newY = childBounds.getCenterY();
                            }
                        }
                    } catch (IllegalActionException ex) {
                        // Something went wrong. Use default.
                    }
                    command.append("<property name = \"_location\" value=\"");
                    // FIXME: Make locations only integral?
                    command.append(Math.rint(newX));
                    command.append(", ");
                    command.append(Math.rint(newY));
                    command.append("\"/>");
                }
                command.append("</group>");

                MoMLChangeRequest request =
                    new MoMLChangeRequest(this, _container, command.toString());
                _container.requestChange(request);
            }
        } else {
            throw new InternalErrorException("No child figure for the manipulator!");
        }
    }

    /** Create a new instance of this manipulator. The new
     *  instance will have the same grab handle, and interactor
     *  for grab-handles.  This is typically called on the prototype
     *  to yield a decorator that gets displayed while the object
     *  is selected.
     */
    public FigureDecorator newInstance(Figure f) {
        BoundsManipulator m = new AttributeBoundsManipulator(_container);
        m.setGrabHandleFactory(this.getGrabHandleFactory());
        m.setHandleInteractor(this.getHandleInteractor());
        m.setDragInteractor(getDragInteractor());

        // FIXME: There is a bug in Diva where mouseReleased()
        // is called on the prototype that is used to create this
        // new instance, not on the new instance.  So we make
        // a record of the new instance to get access to it in
        // mouseReleased().
        _instanceDecorator = m;

        return m;
    }

    // FIXME: Instance used to work around Diva bug.
    private FigureDecorator _instanceDecorator;

    // Container of the icon to be manipulated.
    private NamedObj _container;

    /////////////////////////////////////////////////////////////////////////
    ////                         inner classes                           ////

    /** An interactor class that changes the bounds of the child
     * figure and triggers a repaint.
     */
    private class Resizer extends DragInteractor {
        
        /** Create a new resizer.
         */
        public Resizer() {
            // NOTE: I don't know why the following is needed anymore,
            // but if it isn't here, dragging causes things to move random
            // amounts.  Beats me... EAL
            appendConstraint(new SnapConstraint());
        }

        /** Override the base class to notify the enclosing BoundsInteractor.
         *  @param e The mouse event.
         */
        public void mouseReleased(LayerEvent e) {
            super.mouseReleased(e);
            AttributeBoundsManipulator.this.mouseReleased(e);
        }

        /** Translate the grab-handle.
         */
        public void translate(LayerEvent e, double x, double y) {
            // Snap to grid.
            double[] snapped = SnapConstraint.constrainPoint(x, y);
            
            // Translate the grab-handle, resizing the geometry
            GrabHandle g = (GrabHandle) e.getFigureSource();
            g.translate(snapped[0], snapped[1]);

            // Transform the child.
            BoundsManipulator parent = (BoundsManipulator) g.getParent();
            BoundsGeometry geometry = parent.getGeometry();

            parent.getChild().transform(
                CanvasUtilities.computeTransform(
                    parent.getChild().getBounds(),
                    geometry.getBounds()));
        }
    }
}
