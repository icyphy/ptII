/*

 Copyright (c) 2008 The Regents of the University of California.
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

package ptolemy.vergil.erg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;
import javax.swing.SwingConstants;

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.erg.kernel.Event;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.fsm.StateIcon;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.LabelFigure;
import diva.canvas.toolbox.RoundedRectangle;
import diva.gui.toolbox.FigureIcon;
import diva.util.java2d.Polygon2D;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class EventIcon extends StateIcon {

    public EventIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    public Figure createFigure() {
        CompositeFigure figure = (CompositeFigure) super.createFigure();

        String actions = null;
        String parameters = null;
        boolean fireOnInput = false;
        boolean monitor = false;

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

            fireOnInput = event.fireOnInput();
            monitor = !event.monitoredVariables.getExpression().trim().equals(
                    "");
        }

        if (parameters != null) {
            _addLabel(figure, parameters);
        }

        if (actions != null) {
            _addLabel(figure, actions);
        }

        Rectangle2D bounds = figure.getBounds();
        double y = bounds.getMinY() + 12.5;
        double x = bounds.getMinX();
        if (monitor) {
            _addTrianglarIcon(figure, fireOnInput ? x - 1.0 : x + 4.0, y,
                    Color.blue);
        }

        if (fireOnInput) {
            _addTrianglarIcon(figure, x + 4.0, y, Color.red);
        }

        return figure;
    }

    public Icon createIcon() {
        if (_iconCache != null) {
            return _iconCache;
        }

        RoundedRectangle figure = new RoundedRectangle(0, 0, 20, 10,
                Color.white, 1.0f, 5.0, 5.0);
        figure.setFillPaint(_getFill());
        _iconCache = new FigureIcon(figure, 20, 15);
        return _iconCache;
    }

    protected Paint _getFill() {
        Parameter colorParameter;
        try {
            colorParameter = (Parameter) (getAttribute("fill",
                    Parameter.class));
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
            boolean isInitial = ((BooleanToken) event.isInitialEvent.getToken())
                    .booleanValue();
            if (isInitial) {
                return _INITIAL_COLOR;
            }
            boolean isFinal = ((BooleanToken) event.isFinalEvent.getToken())
                    .booleanValue();
            if (isFinal) {
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
    protected float _getLineWidth() {
        NamedObj container = getContainer();
        if (container instanceof Event) {
            try {
                if (((BooleanToken) (((Event) container).isInitialEvent
                        .getToken())).booleanValue()) {
                    return 2.0f;
                }
            } catch (IllegalActionException e) {
                // Ignore and return the default.
            }
        }
        return 1.0f;
    }

    protected boolean _middleTrangles() {
        return false;
    }

    private void _addLabel(CompositeFigure figure, String text) {
        LabelFigure label = new LabelFigure(text, _ACTION_FONT, 1.0,
                SwingConstants.CENTER);
        Rectangle2D stringBounds = label.getBounds();
        Figure background = figure.getBackgroundFigure();
        Rectangle2D backBounds = background.getBounds();

        double width = backBounds.getWidth();
        double textWidth = stringBounds.getWidth() + 12.0;
        double left = backBounds.getX();
        if (textWidth > width) {
            left += (width - textWidth) / 2.0;
            width = textWidth;
        }
        double height = backBounds.getHeight() + stringBounds.getHeight();

        background.setParent(null);
        RoundedRectangle border = new RoundedRectangle(left, 0.0, width,
                height - 2.0 *_spacingValue, _getFill(), _getLineWidth(),
                _roundingValue, _roundingValue);
        if (_spacingValue == 0.0) {
            background = border;
        } else {
            background = new CompositeFigure(new RoundedRectangle(
                    left - _spacingValue, - _spacingValue,
                    width + 2.0 * _spacingValue, height,
                    null, _getLineWidth(), _roundingValue + _spacingValue,
                    _roundingValue + _spacingValue));
            ((CompositeFigure) background).add(border);
        }
        figure.setBackgroundFigure(background);

        label.translateTo(background.getBounds().getCenterX(),
                backBounds.getMaxY() + stringBounds.getHeight() / 2.0 - 1.0 -
                _spacingValue);
        figure.add(label);
    }

    private void _addTrianglarIcon(CompositeFigure figure, double x, double y,
            Paint fill) {
        Polygon2D.Double polygon = new Polygon2D.Double();
        polygon.moveTo(-5, 0);
        polygon.lineTo(-5, 5);
        polygon.lineTo(5, 0);
        polygon.lineTo(-5, -5);
        polygon.lineTo(-5, 0);
        polygon.closePath();
        polygon.translate(x, y);
        Figure inputIcon = new BasicFigure(polygon, fill, 1.5f);
        figure.add(inputIcon);
    }

    private static final Font _ACTION_FONT = new Font("SansSerif", Font.PLAIN,
            10);

    private static final Color _FINAL_COLOR = new Color(255, 64, 64);

    private static final Color _INITIAL_COLOR = new Color(64, 255, 64);
}
