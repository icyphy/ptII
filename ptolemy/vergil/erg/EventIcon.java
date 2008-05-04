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
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;
import javax.swing.SwingConstants;

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

    /**
     * @param container
     * @param name
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public EventIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    public Figure createFigure() {
        CompositeFigure figure = (CompositeFigure) super.createFigure();

        String actions = null;
        String parameters = null;
        boolean fireOnInput = false;

        Event event = (Event) getContainer();
        if (event != null) {
            try { 
                if (event.parameters.getArgumentNameList().size() > 0) {
                    parameters = event.parameters.getValueAsString();
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
        }

        if (parameters != null) {
            _addLabel(figure, parameters);
        }

        if (actions != null) {
            _addLabel(figure, actions);
        }

        if (fireOnInput) {
            _addInputIcon(figure);
        }

        return figure;
    }

    public Icon createIcon() {
        if (_iconCache != null) {
            return _iconCache;
        }

        Figure figure = new RoundedRectangle(0, 0, 20, 10, Color.white, 1.0f,
                5.0, 5.0);
        _iconCache = new FigureIcon(figure, 20, 15);
        return _iconCache;
    }

    private void _addInputIcon(CompositeFigure figure) {
        Rectangle2D bounds = figure.getBounds();
        Polygon2D.Double polygon = new Polygon2D.Double();
        polygon.moveTo(-5, 0);
        polygon.lineTo(-5, 5);
        polygon.lineTo(5, 0);
        polygon.lineTo(-5, -5);
        polygon.lineTo(-5, 0);
        polygon.closePath();
        polygon.translate(bounds.getMinX() + 4, bounds.getMinY() + 12.5);
        Figure inputIcon = new BasicFigure(polygon, Color.red, 1.5f);
        figure.add(inputIcon);
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
        RoundedRectangle border = new RoundedRectangle(left, 0.0, width, height,
                _getFill(), _getLineWidth(), _roundingValue, _roundingValue);
        if (_spacingValue == 0.0) {
            background = border;
        } else {
            background = new CompositeFigure(new RoundedRectangle(
                    left - _spacingValue, - _spacingValue,
                    width + 2.0 * _spacingValue, height + 2.0 * _spacingValue,
                    null, _getLineWidth(), _roundingValue + _spacingValue,
                    _roundingValue + _spacingValue));
            ((CompositeFigure) background).add(border);
        }
        figure.setBackgroundFigure(background);

        label.translateTo(background.getBounds().getCenterX(),
                backBounds.getMaxY() + stringBounds.getHeight() / 2.0 - 1.0);
        figure.add(label);
    }

    private static final Font _ACTION_FONT =
        new Font("SansSerif", Font.PLAIN, 10);
}
