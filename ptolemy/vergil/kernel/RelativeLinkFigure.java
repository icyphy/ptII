/*

Copyright (c) 2011-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA LIABLE TO ANY PARTY
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
package ptolemy.vergil.kernel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.RelativeLocation;
import ptolemy.vergil.basic.BasicGraphPane;
import diva.canvas.AbstractFigure;
import diva.canvas.CanvasComponent;
import diva.canvas.CanvasPane;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.graph.GraphController;

/**
 * A figure for drawing a link between a relative locatable and its referenced object.
 * The link is represented by a straight thin line.
 *
 * FIXME: Some artifacts are visible when the relative locatable object is dragged, because
 *        the clipping region seems not to be updated quickly enough.
 *
 * @author Miro Spoenemann
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (msp)
 * @Pt.AcceptedRating Red (msp)
 */
public class RelativeLinkFigure extends AbstractFigure {

    /**
     * Construct a figure to draw the link of a relative locatable object.
     *
     * @param location The location of the relative locatable object.
     */
    public RelativeLinkFigure(RelativeLocation location) {
        _relativeLocation = location;
        _line = new Line2D.Double();
        NamedObj relativeTo = _relativeLocation.getRelativeToNamedObj();
        if (relativeTo != null) {
            _updateLine(relativeTo);
        }
    }

    /** Get the outline shape of this figure. This implementation returns a line.
     *
     * @return A line, which may have length 0 if the related object has no valid reference.
     */
    @Override
    public Shape getShape() {
        return _line;
    }

    /** Paint the figure. This implementation paints a line if the related object has
     *  a valid reference, and it paints nothing if it hasn't. If the length of the
     *  line exceeds a specific threshold, it is drawn with a different color to
     *  highlight that the reference will eventually be broken.
     *
     * @param g The graphics context used for painting.
     */
    @Override
    public void paint(Graphics2D g) {
        NamedObj relativeTo = _relativeLocation.getRelativeToNamedObj();
        if (relativeTo != null) {
            _updateLine(relativeTo);
            double distance = Math.sqrt(_line.x2 * _line.x2 + _line.y2
                    * _line.y2);
            if (distance <= RelativeLocation.BREAK_THRESHOLD) {
                g.setColor(NORMAL_COLOR);
            } else {
                g.setColor(THRESHOLD_COLOR);
            }
            g.setStroke(STROKE);
            if (_transform != null) {
                g.draw(_transform.createTransformedShape(_line));
            } else {
                g.draw(_line);
            }
        } else {
            // The relative location does not have a valid reference, so reset
            // the cached location vector and draw nothing.
            _line.x2 = 0;
            _line.y2 = 0;
        }
    }

    /** Set the given affine transformation for this figure.
     *
     * @param at an affine transformation
     */
    @Override
    public void transform(AffineTransform at) {
        _transform = at;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Update the line to the specified object.
     *  @param relativeTo The destination object.
     */
    private void _updateLine(NamedObj relativeTo) {
        // The line goes from this object (number 1) to
        // the remote object (number 2). The positions for
        // these objects are as follows.
        boolean success = false;
        if (getParent() instanceof CompositeFigure) {
            CompositeFigure parentFigure = (CompositeFigure) getParent();
            // NOTE: Calling getBounds() on the figure itself yields an
            // inaccurate bounds, for some reason.
            // Weirdly, to get the size right, we need to use the shape.
            // But to get the location right, we need the other!
            Rectangle2D bounds1 = parentFigure.getBackgroundFigure()
                    .getBounds();
            Point2D origin1 = parentFigure.getBackgroundFigure().getOrigin();

            double xOffset1 = origin1.getX();
            // FIXME: Diva is a complete mystery. Offset doesn't work here, but works below.
            xOffset1 = 0.0;
            double left1 = bounds1.getX() - xOffset1;
            double center1 = bounds1.getX() + bounds1.getWidth() * 0.5
                    - xOffset1;
            double right1 = bounds1.getX() + bounds1.getWidth() - xOffset1;

            double yOffset1 = origin1.getY();
            // FIXME: Diva is a complete mystery. Offset doesn't work here, but works below.
            yOffset1 = 0.0;
            double top1 = bounds1.getY() - yOffset1;
            double middle1 = bounds1.getY() + bounds1.getHeight() * 0.5
                    - yOffset1;
            double bottom1 = bounds1.getY() + bounds1.getHeight() - yOffset1;

            // Now find the destination.
            // Unfortunately, this is rather hard to do.
            Locatable location = null;
            try {
                location = (Locatable) relativeTo.getAttribute("_location",
                        Locatable.class);
            } catch (IllegalActionException e1) {
                // Ignore and handle as if location is null.
            }
            CanvasComponent parent = getParent();
            FigureLayer enclosingFigureLayer = null;
            while (parent != null) {
                if (parent instanceof FigureLayer) {
                    enclosingFigureLayer = (FigureLayer) parent;
                    break;
                }
                parent = parent.getParent();
            }
            if (location != null && enclosingFigureLayer != null) {
                CanvasPane pane = enclosingFigureLayer.getCanvasPane();
                if (pane instanceof BasicGraphPane) {
                    GraphController controller = ((BasicGraphPane) pane)
                            .getGraphController();
                    Figure figure = controller.getFigure(location);
                    if (figure instanceof CompositeFigure) {
                        figure = ((CompositeFigure) figure)
                                .getBackgroundFigure();
                    }
                    double[] offset = _relativeLocation.getRelativeLocation();
                    // NOTE: Calling getBounds() on the figure itself yields an
                    // inaccurate bounds, for some reason.
                    // Weirdly, to get the size right, we need to use the shape.
                    // But to get the location right, we need the other!
                    Rectangle2D bounds2 = figure.getShape().getBounds2D();

                    Point2D origin2 = figure.getOrigin();
                    double xOffset2 = origin2.getX();
                    double left2 = -offset[0] + bounds2.getX() - xOffset2;
                    double center2 = -offset[0] + bounds2.getX()
                            + bounds2.getWidth() * 0.5 - xOffset2;
                    double right2 = -offset[0] + bounds2.getX()
                            + bounds2.getWidth() - xOffset2;

                    double yOffset2 = origin2.getY();
                    // FIXME: Diva is a complete mystery. Offset isn't right. Fudge it.
                    yOffset2 += 11;
                    double top2 = -offset[1] + bounds2.getY() - yOffset2;
                    double middle2 = -offset[1] + bounds2.getY()
                            + bounds2.getHeight() * 0.5 - yOffset2;
                    double bottom2 = -offset[1] + bounds2.getY()
                            + bounds2.getHeight() - yOffset2;

                    // We have all the information we need for optimal placement.
                    success = true;

                    // There are five possible x positions.
                    if (left1 > right2) {
                        _line.x1 = left1;
                        _line.x2 = right2;
                    } else if (center1 > right2) {
                        _line.x1 = center1;
                        _line.x2 = right2;
                    } else if (center1 > left2) {
                        _line.x1 = center1;
                        _line.x2 = center2;
                    } else if (right1 > left2) {
                        _line.x1 = center1;
                        _line.x2 = left2;
                    } else {
                        _line.x1 = right1;
                        _line.x2 = left2;
                    }
                    // There are five possible y positions.
                    if (top1 > bottom2) {
                        _line.y1 = top1;
                        _line.y2 = bottom2;
                    } else if (middle1 > bottom2) {
                        _line.y1 = middle1;
                        _line.y2 = bottom2;
                    } else if (middle1 > top2) {
                        _line.y1 = middle1;
                        _line.y2 = middle2;
                    } else if (bottom1 > top2) {
                        _line.y1 = middle1;
                        _line.y2 = top2;
                    } else {
                        _line.y1 = bottom1;
                        _line.y2 = top2;
                    }
                }
            }
        }
        if (!success) {
            // Fallback connection.
            _line.x1 = 0;
            _line.y1 = 0;
            double[] offset = _relativeLocation.getRelativeLocation();
            _line.x2 = -offset[0];
            _line.y2 = -offset[1];
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The relative location represented by this figure. */
    private RelativeLocation _relativeLocation;

    /** The line used for drawing. */
    private Line2D.Double _line;

    /** The current affine transformation. */
    private AffineTransform _transform;

    /** The stroke used for drawing the line. */
    private static final Stroke STROKE = new BasicStroke(1.0f,
            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] {
            2.0f, 2.0f }, 0.0f);

    /** The normal color of the line. */
    private static final Color NORMAL_COLOR = new Color(180, 180, 0);

    /** The color used when the line is longer that a specific threshold. */
    private static final Color THRESHOLD_COLOR = new Color(250, 50, 0);

}
