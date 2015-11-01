/* A test icon for a Ptera model.

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
import java.awt.geom.Rectangle2D;

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
import diva.canvas.ZList;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.LabelFigure;
import diva.util.java2d.Polygon2D;

///////////////////////////////////////////////////////////////////
//// TestIcon

/**
 A test icon for a Ptera model.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TestIcon extends NameIcon {

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
    public TestIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a background Figure.
     *  @return The figure.
     */
    @Override
    public Figure createBackgroundFigure() {
        String name = "No Name";
        NamedObj container = getContainer();
        if (container != null) {
            name = container.getDisplayName();
        }

        double width = 60;
        double height = 30;

        // Measure width of the text.  Unfortunately, this
        // requires generating a label figure that we will not use.
        LabelFigure label = new LabelFigure(name, _labelFont, 1.0,
                SwingConstants.CENTER);
        CompositeFigure figure = new CompositeFigure(label);
        Event event = (Event) getContainer();
        String parameters = null;
        String actions = null;
        if (event != null) {
            try {
                if (event.parameters.getParameterNames().size() > 0) {
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
        }
        if (parameters != null) {
            label = _addLabel(figure, label, parameters);
        }
        if (actions != null) {
            _addLabel(figure, label, actions);
        }

        Rectangle2D stringBounds = figure.getBounds();

        // NOTE: Padding of 20. Quantize the height so that
        // snap to grid still works.
        width = Math.floor(stringBounds.getWidth()) + 40;
        height = Math.floor(stringBounds.getHeight()) + 24;

        Polygon2D polygon = new Polygon2D.Double(new double[] { 0.0,
                height / 2.0, width / 2, 0.0, width, height / 2.0, width / 2.0,
                height });
        return new BasicFigure(polygon, _getFill(), _getLineWidth());
    }

    /** Create a Figure.
     *  @return The figure.
     */
    @Override
    public Figure createFigure() {
        CompositeFigure figure = (CompositeFigure) super.createFigure();
        LabelFigure label = null;

        ZList children = figure.getChildren();
        for (int i = children.getFigureCount() - 1; i >= 0; i--) {
            Figure childFigure = children.get(i);
            if (childFigure instanceof LabelFigure) {
                label = (LabelFigure) childFigure;
                break;
            }
        }

        if (label == null) {
            return figure;
        } else if (label != null) {
            Rectangle2D bounds = figure.getBounds();
            label.translateTo(bounds.getCenterX(), bounds.getMinY()
                    + label.getBounds().getHeight() / 2.0 + 15.0);
        }

        String actions = null;
        String parameters = null;
        Event event = (Event) getContainer();

        if (event != null) {
            try {
                if (event.parameters.getParameterNames().size() > 0) {
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
        }

        if (parameters != null) {
            label = _addLabel(figure, label, parameters);
        }

        if (actions != null) {
            _addLabel(figure, label, actions);
        }

        return figure;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

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
        } catch (Throwable e) {
            // Ignore and return the default.
        }
        return super._getFill();
    }

    private LabelFigure _addLabel(CompositeFigure figure, Figure previous,
            String text) {
        Rectangle2D bounds = previous.getBounds();
        LabelFigure label = new LabelFigure(text, _ACTION_FONT, 1.0,
                SwingConstants.CENTER);
        Rectangle2D newBounds = label.getBounds();
        label.translateTo(bounds.getCenterX(),
                bounds.getMaxY() + newBounds.getHeight() / 2.0 + 7.0);
        figure.add(label);
        return label;
    }

    private static final Font _ACTION_FONT = new Font("SansSerif", Font.PLAIN,
            10);
}
