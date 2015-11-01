/*  An icon for a Ptera event.

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

import ptolemy.domains.ptera.kernel.Event;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.modal.StateIcon;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.LabelFigure;
import diva.canvas.toolbox.RoundedRectangle;
import diva.gui.toolbox.FigureIcon;

/**
 An icon for a Ptera event.
 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class EventIcon extends StateIcon {

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
    public EventIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _yPadding = 8.0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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

        RoundedRectangle figure = new RoundedRectangle(0, 0, 20, 10,
                Color.white, 1.0f, 5.0, 5.0);
        figure.setFillPaint(_getFill());
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
        Event event = (Event) getContainer();
        try {
            if (event.isInitialEvent()) {
                return _INITIAL_COLOR;
            }
            if (event.isFinalEvent()) {
                return _FINAL_COLOR;
            }
            if (event.isEndingEvent()) {
                return _ENDING_COLOR;
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
        RoundedRectangle border = new RoundedRectangle(left, 0.0, width, height
                - 2.0 * _spacingValue, _getFill(), _getLineWidth(),
                _roundingValue, _roundingValue);
        if (_spacingValue == 0.0) {
            background = border;
        } else {
            background = new CompositeFigure(new RoundedRectangle(left
                    - _spacingValue, -_spacingValue, width + 2.0
                    * _spacingValue, height, null, _getLineWidth(),
                    _roundingValue + _spacingValue, _roundingValue
                    + _spacingValue));
            ((CompositeFigure) background).add(border);
        }
        figure.setBackgroundFigure(background);

        label.translateTo(background.getBounds().getCenterX(),
                backBounds.getMaxY() + stringBounds.getHeight() / 2.0 - 1.0
                - _spacingValue);
        figure.add(label);
    }

    private static final Font _ACTION_FONT = new Font("SansSerif", Font.PLAIN,
            10);

    private static final Color _FINAL_COLOR = new Color(255, 64, 64);

    private static final Color _INITIAL_COLOR = new Color(64, 255, 64);

    private static final Color _ENDING_COLOR = new Color(255, 64, 64);

    private static final double _MIN_HEIGHT = 20.0;
}
