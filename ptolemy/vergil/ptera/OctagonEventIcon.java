/*  An octagonal icon for a Ptera event.

 Copyright (c) 2008-2014 The Regents of the University of California.
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

package ptolemy.vergil.ptera;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;
import javax.swing.SwingConstants;

import ptolemy.data.ArrayToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.ptera.kernel.Event;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.icon.NameIcon;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.LabelFigure;
import diva.gui.toolbox.FigureIcon;
import diva.util.java2d.Polygon2D;

/**
 An octagonal icon for a Ptera event.
 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class OctagonEventIcon extends NameIcon {

    /** Create a new icon with the given name in the given container.
     *  The container is required to implement Settable, or an exception
     *  will be thrown.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If thrown by the parent
     *  class or while setting an attribute.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public OctagonEventIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _yPadding = 8.0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a background Figure.
     *  @return The figure.
     */
    @Override
    public Figure createBackgroundFigure() {
        Point2D size = _getBackgroundSize();
        double width = size.getX();
        double height = size.getY();
        double cornerWidth = Math.min(_cornerWidth,
                Math.min(height, width) / 2.0);

        Polygon2D polygon = _createOctagon(width, height, cornerWidth);
        Figure figure = new BasicFigure(polygon, _getFill(), _getLineWidth());

        if (_spacingValue > 0.0) {
            CompositeFigure compositeFigure = new CompositeFigure(figure);
            width += _spacingValue * 2.0;
            height += _spacingValue * 2.0;
            cornerWidth += _spacingValue * 1.8 / cornerWidth;
            polygon = _createOctagon(width, height, cornerWidth);
            Figure outerFigure = new BasicFigure(polygon, null, _getLineWidth());
            outerFigure.translate(-_spacingValue, -_spacingValue);
            compositeFigure.add(0, outerFigure);
            figure = compositeFigure;
        }

        return figure;
    }

    /** Create a Figure.
     *  @return The figure.
     */
    @Override
    public Figure createFigure() {
        CompositeFigure figure = (CompositeFigure) super.createFigure();

        String actions = null;
        String parameters = null;

        Event event = (Event) getContainer();
        if (event != null) {
            try {
                if (event.parameters.getParameterNames().size() > 0) {
                    parameters = event.parameters.stringValue();
                }
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(event.parameters, ex,
                        "Failed to get argument name list.");
            }
            String exp = event.actions.getExpression();
            if (exp != null && !exp.trim().equals("")) {
                actions = "{ " + exp + " }";
            }
        }

        if (parameters != null) {
            _addLabel(figure, parameters);
        }

        if (actions != null) {
            _addLabel(figure, actions);
        }

        return figure;
    }

    /** Create an icon.
     *  @return The icon.
     */
    @Override
    public Icon createIcon() {
        if (_iconCache != null) {
            return _iconCache;
        }

        double width = 30.0;
        double height = 15.0;
        double cornerWidth = 5.0;
        Polygon2D polygon = _createOctagon(width, height, cornerWidth);
        Figure figure = new BasicFigure(polygon, _getFill(), 1.0f);
        _iconCache = new FigureIcon(figure, 20, 15);
        return _iconCache;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    @Override
    protected Point2D _getBackgroundSize() {
        Point2D size = super._getBackgroundSize();
        if (size.getY() < _MIN_HEIGHT) {
            size = new Point2D.Double(size.getX(), _MIN_HEIGHT);
        }
        return size;
    }

    @Override
    protected Paint _getFill() {
        Parameter colorParameter;
        try {
            colorParameter = (Parameter) getAttribute("fill", Parameter.class);
            if (colorParameter != null) {
                ArrayToken array = (ArrayToken) colorParameter.getToken();
                if (array.length() == 4) {
                    Color color = new Color(
                            (float) ((ScalarToken) array.getElement(0))
                                    .doubleValue(),
                            (float) ((ScalarToken) array.getElement(1))
                                    .doubleValue(),
                            (float) ((ScalarToken) array.getElement(2))
                                    .doubleValue(),
                            (float) ((ScalarToken) array.getElement(3))
                                    .doubleValue());
                    return color;
                }
            }
        } catch (Throwable t) {
            // Ignore and return the default.
        }

        Event event = (Event) getContainer();
        try {
            if (event.isInitialEvent()) {
                return _INITIAL_COLOR;
            }
            if (event.isFinalEvent()) {
                return _FINAL_COLOR;
            }
        } catch (Throwable t) {
            // Ignore and return the default.
        }

        return super._getFill();
    }

    /** Return the line width to use in rendering the box.
     *  This returns 1.0f, unless the container is an instance of State
     *  and its <i>isInitialState</i> parameter is set to true.
     *  @return The line width to use in rendering the box.
     */
    @Override
    protected float _getLineWidth() {
        NamedObj container = getContainer();
        if (container instanceof Event) {
            try {
                if (((Event) container).isInitialEvent()) {
                    return 2.0f;
                }
            } catch (IllegalActionException e) {
                // Ignore and return the default.
            }
        }
        return 1.0f;
    }

    /** The width of a corner of a polygon. */
    protected double _cornerWidth = 5.0;

    private void _addLabel(CompositeFigure figure, String text) {
        LabelFigure label = new LabelFigure(text, _ACTION_FONT, 1.0,
                SwingConstants.CENTER);
        Rectangle2D stringBounds = label.getBounds();
        Figure background = figure.getBackgroundFigure();
        Rectangle2D backBounds = background.getBounds();

        double width = backBounds.getWidth() - _spacingValue * 2.0;
        double textWidth = stringBounds.getWidth() + 12.0;
        //double left = backBounds.getX();
        if (textWidth > width) {
            //left += (width - textWidth) / 2.0;
            width = textWidth;
        }
        double height = backBounds.getHeight() + stringBounds.getHeight()
                - _spacingValue * 2.0;

        background.setParent(null);

        double cornerWidth = Math.min(_cornerWidth,
                Math.min(height, width) / 2.0);
        Polygon2D polygon = _createOctagon(width, height, cornerWidth);
        background = new BasicFigure(polygon, _getFill(), _getLineWidth());

        if (_spacingValue > 0.0) {
            CompositeFigure compositeFigure = new CompositeFigure(background);
            width += _spacingValue * 2.0;
            height += _spacingValue * 2.0;
            cornerWidth += _spacingValue * 1.8 / cornerWidth;
            polygon = _createOctagon(width, height, cornerWidth);
            Figure outerFigure = new BasicFigure(polygon, null, _getLineWidth());
            outerFigure.translate(-_spacingValue, -_spacingValue);
            compositeFigure.add(0, outerFigure);
            background = compositeFigure;
        }
        background.translate((backBounds.getWidth() - width) / 2 - 3.0, 0.0);
        figure.setBackgroundFigure(background);

        label.translateTo(background.getBounds().getCenterX(),
                backBounds.getMaxY() + stringBounds.getHeight() / 2.0 - 1.0
                        - _spacingValue);
        figure.add(label);
    }

    private Polygon2D _createOctagon(double width, double height,
            double cornerWidth) {
        Polygon2D polygon = new Polygon2D.Double();
        polygon.moveTo(0.0, cornerWidth);
        polygon.lineTo(cornerWidth, 0.0);
        polygon.lineTo(width - cornerWidth, 0.0);
        polygon.lineTo(width, cornerWidth);
        polygon.lineTo(width, height - cornerWidth);
        polygon.lineTo(width - cornerWidth, height);
        polygon.lineTo(cornerWidth, height);
        polygon.lineTo(0.0, height - cornerWidth);
        polygon.closePath();
        return polygon;
    }

    private static final Font _ACTION_FONT = new Font("SansSerif", Font.PLAIN,
            10);

    private static final Color _FINAL_COLOR = new Color(255, 64, 64);

    private static final Color _INITIAL_COLOR = new Color(64, 255, 64);

    private static final double _MIN_HEIGHT = 20.0;
}
