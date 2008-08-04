package ptolemy.vergil.gt;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingConstants;

import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.ZList;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.LabelFigure;
import diva.util.java2d.Polygon2D;
import ptolemy.actor.gt.controller.Test;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.icon.NameIcon;

public class TestIcon extends NameIcon {

    public TestIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

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
        Rectangle2D stringBounds = label.getBounds();

        // NOTE: Padding of 20. Quantize the height so that
        // snap to grid still works.
        width = Math.floor(stringBounds.getWidth()) + 40;
        height = Math.floor(stringBounds.getHeight()) + 25;

        Polygon2D polygon = new Polygon2D.Double(new double[] {
                0.0, height / 2.0, width / 2, 0.0, width, height / 2.0,
                width / 2.0, height
        });
        return new BasicFigure(polygon, _getFill(), _getLineWidth());
    }

    public Figure createFigure() {
        CompositeFigure result = (CompositeFigure) super.createFigure();
        Test test = (Test) getContainer();
        try {
            if (!((BooleanToken) test.matched.getToken()).booleanValue()) {
                ZList children = result.getChildren();
                LabelFigure label = null;
                for (int i = children.getFigureCount() - 1; i >= 0; i--) {
                    Figure figure = children.get(i);
                    if (figure instanceof LabelFigure) {
                        label = (LabelFigure) figure;
                        break;
                    }
                }

                if (label != null) {
                    label.translate(0.0, 2.0);
                    Rectangle2D bounds = label.getBounds();
                    double y = bounds.getMinY() - 2.5;
                    Line2D overline = new Line2D.Double(bounds.getMinX(), y,
                            bounds.getMaxX(), y);
                    result.add(new BasicFigure(overline, 1.5f));
                }
            }
        } catch (IllegalActionException e) {
            // Ignore.
        }

        return result;
    }

    protected Paint _getFill() {
        Parameter colorParameter;
        try {
            colorParameter = (Parameter) (getAttribute("_fill",
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
        } catch (Throwable e) {
            // Ignore and return the default.
        }
        return super._getFill();
    }
}
