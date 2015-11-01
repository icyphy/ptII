/* A manipulator for resizable icons.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.vergil.toolbox;

import java.awt.geom.Rectangle2D;

import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Locatable;
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

///////////////////////////////////////////////////////////////////
//// AttributeBoundsManipulator

/**
 This is a bounds manipulator supporting resizable icons.
 It records the new size when the mouse is released, and supports
 snap to grid.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
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
        _resizer = new Resizer();
        setHandleInteractor(_resizer);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Make a persistent record of the new size by issuing a change request.
     *  @param e The mouse event.
     */
    @Override
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
            // inaccurate bounds, for some reason. Use getShape().
            Rectangle2D bounds = child.getShape().getBounds2D();

            double resolution = _resizer.getSnapResolution();
            // Check to see whether the size has changed by more than the
            // snap resolution.
            if (_boundsOnMousePressed != null
                    && Math.abs(bounds.getWidth()
                            - _boundsOnMousePressed.getWidth()) < resolution
                            && Math.abs(bounds.getHeight()
                                    - _boundsOnMousePressed.getHeight()) < resolution) {
                // Change is not big enough. Return.
                return;
            }

            // Use a MoMLChangeRequest here so that the resize can be
            // undone and so that a repaint occurs.
            Attribute widthParameter = _container.getAttribute("width");
            Attribute heightParameter = _container.getAttribute("height");
            Attribute locationParameter = _container.getAttribute("_location");

            // Proceed only if the container has these parameters.
            if (widthParameter instanceof Parameter
                    && heightParameter instanceof Parameter) {
                // Snap the new width and height to the grid (not the parameter values!).
                // The reason is that it is the new width and height, not the parameter
                // values, are what is visible on the screen.
                double[] snappedWidthHeight = _resizer.constrain(
                        bounds.getWidth(), bounds.getHeight());

                // The new width and height should be proportional to the original
                // ones. This is because the width and height parameters of the
                // attribute are not necessarily the same as the bounds of the
                // figure. An extreme example of this is the ArcAttribute,
                // where the width and height parameters specify the width
                // and height of the base ellipse used to draw the arc.
                // Provide default values in case something goes wrong.
                double newWidth = snappedWidthHeight[0];
                double newHeight = snappedWidthHeight[1];

                try {
                    Token previousWidth = ((Parameter) widthParameter)
                            .getToken();
                    if (previousWidth instanceof DoubleToken
                            && _boundsOnMousePressed != null) {
                        newWidth = snappedWidthHeight[0]
                                / _boundsOnMousePressed.getWidth()
                                * ((DoubleToken) previousWidth).doubleValue();
                    }
                } catch (IllegalActionException e1) {
                    // This should not occur.
                    e1.printStackTrace();
                }

                try {
                    Token previousHeight = ((Parameter) heightParameter)
                            .getToken();
                    if (previousHeight instanceof DoubleToken
                            && _boundsOnMousePressed != null) {
                        newHeight = snappedWidthHeight[1]
                                / _boundsOnMousePressed.getHeight()
                                * ((DoubleToken) previousHeight).doubleValue();
                    }
                } catch (IllegalActionException e1) {
                    // This should not occur.
                    e1.printStackTrace();
                }

                // Create the MoML command to change the width and height.
                StringBuffer command = new StringBuffer(
                        "<group><property name =\"width\" value=\"");
                command.append(newWidth);
                command.append("\"/><property name =\"height\" value=\"");
                command.append(newHeight);
                command.append("\"/>");

                // Location may be the upper left corner. Hence,
                // location needs to change too if dragged left or up.
                if (locationParameter instanceof Locatable) {

                    double[] previousLocation = ((Locatable) locationParameter)
                            .getLocation();

                    // Use these defaults if for some reason _boundsOnMousePressed == null
                    // (which should not occur).
                    Rectangle2D childBounds = child.getBounds();
                    double newX = childBounds.getX();
                    double newY = childBounds.getY();

                    if (_boundsOnMousePressed != null) {
                        // Snap the new X and Y to the grid (not the new location!).
                        // The reason is that it is the new X and Y, not the location,
                        // the is visible on the screen. The location could be the
                        // center of the object, or off center anywhere.
                        double[] snappedXY = _resizer.constrain(bounds.getX(),
                                bounds.getY());

                        // If the previous location does not match X and Y of
                        // _boundsOnMousePressed, then the figure location is not
                        // the upper left corner. In this case, we need to scale
                        // displacement according to the following formulas
                        // (this is a tricky geometry problem!).
                        newX = snappedXY[0]
                                + snappedWidthHeight[0]
                                        / _boundsOnMousePressed.getWidth()
                                        * (previousLocation[0] - _boundsOnMousePressed
                                                .getX());
                        newY = snappedXY[1]
                                + snappedWidthHeight[1]
                                        / _boundsOnMousePressed.getHeight()
                                        * (previousLocation[1] - _boundsOnMousePressed
                                                .getY());
                    } else {
                        // This is legacy code. Should never be invoked.
                        // If the figure is centered, have to use the center
                        // instead.
                        try {
                            Attribute centered = _container.getAttribute(
                                    "centered", Parameter.class);

                            if (centered != null) {
                                boolean isCentered = ((BooleanToken) ((Parameter) centered)
                                        .getToken()).booleanValue();

                                if (isCentered) {
                                    newX = childBounds.getCenterX();
                                    newY = childBounds.getCenterY();
                                }
                            }
                        } catch (IllegalActionException ex) {
                            // Something went wrong. Use default.
                        }
                    }

                    command.append("<property name = \"_location\" value=\"");

                    command.append(newX);
                    command.append(", ");
                    command.append(newY);
                    command.append("\"/>");
                }

                command.append("</group>");

                MoMLChangeRequest request = new MoMLChangeRequest(this,
                        _container, command.toString());
                _container.requestChange(request);
            }
        } else {
            throw new InternalErrorException(
                    "No child figure for the manipulator!");
        }
    }

    /** Make a record of the size before resizing.
     *  @param e The mouse event.
     */
    @Override
    public void mousePressed(LayerEvent e) {
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
            _boundsOnMousePressed = child.getShape().getBounds2D();
        } else {
            // Make sure we don't use some previous bogus value.
            _boundsOnMousePressed = null;
        }
    }

    /** Create a new instance of this manipulator. The new
     *  instance will have the same grab handle, and interactor
     *  for grab-handles.  This is typically called on the prototype
     *  to yield a decorator that gets displayed while the object
     *  is selected.
     */
    @Override
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

    /** Set the snap resolution.
     *  @param resolution The snap resolution.
     */
    public void setSnapResolution(double resolution) {
        _resizer.setSnapResolution(resolution);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // Bounds of the child figure upon the mouse being pressed.
    private Rectangle2D _boundsOnMousePressed;

    // FIXME: Instance used to work around Diva bug.
    private FigureDecorator _instanceDecorator;

    // Container of the icon to be manipulated.
    private NamedObj _container;

    // The local instance of the resizer.
    private Resizer _resizer;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An interactor class that changes the bounds of the child
     * figure and triggers a repaint.
     */
    private class Resizer extends DragInteractor {
        /** Create a new resizer.
         */
        public Resizer() {
            _snapConstraint = new SnapConstraint();
            appendConstraint(_snapConstraint);
        }

        /** Modify the specified point to snap to grid using the local
         *  resolution.
         *  @param x The x dimension of the point to modify.
         *  @param y The y dimension of the point to modify.
         *  @return The constrained point.
         */
        public double[] constrain(double x, double y) {
            return _snapConstraint.constrain(x, y);
        }

        /** Get the snap resolution.
         *  @return The snap resolution.
         */
        public double getSnapResolution() {
            return _snapConstraint.getResolution();
        }

        /** Override the base class to notify the enclosing BoundsInteractor.
         *  @param e The mouse event.
         */
        @Override
        public void mousePressed(LayerEvent e) {
            super.mousePressed(e);
            AttributeBoundsManipulator.this.mousePressed(e);
        }

        /** Override the base class to notify the enclosing BoundsInteractor.
         *  @param e The mouse event.
         */
        @Override
        public void mouseReleased(LayerEvent e) {
            super.mouseReleased(e);
            AttributeBoundsManipulator.this.mouseReleased(e);
        }

        /** Set the snap resolution.
         *  @param resolution The snap resolution.
         */
        public void setSnapResolution(double resolution) {
            _snapConstraint.setResolution(resolution);
        }

        /** Translate the grab-handle.
         */
        @Override
        public void translate(LayerEvent e, double x, double y) {
            // Snap to grid.
            double[] snapped = _snapConstraint.constrain(x, y);

            // Translate the grab-handle, resizing the geometry
            GrabHandle g = (GrabHandle) e.getFigureSource();
            g.translate(snapped[0], snapped[1]);

            // Transform the child.
            BoundsManipulator parent = (BoundsManipulator) g.getParent();
            BoundsGeometry geometry = parent.getGeometry();

            parent.getChild().transform(
                    CanvasUtilities.computeTransform(parent.getChild()
                            .getBounds(), geometry.getBounds()));
        }

        private SnapConstraint _snapConstraint;
    }
}
