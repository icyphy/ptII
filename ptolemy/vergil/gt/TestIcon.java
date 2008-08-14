package ptolemy.vergil.gt;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingConstants;

import ptolemy.data.ArrayToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.erg.kernel.Event;
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

        Polygon2D polygon = new Polygon2D.Double(new double[] {
                0.0, height / 2.0, width / 2, 0.0, width, height / 2.0,
                width / 2.0, height
        });
        return new BasicFigure(polygon, _getFill(), _getLineWidth());
    }

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
            label.translateTo(bounds.getCenterX(), bounds.getMinY() +
                    label.getBounds().getHeight() / 2.0 + 15.0);
        }

        String actions = null;
        String parameters = null;
        Event event = (Event) getContainer();
        boolean fireOnInput = false;
        boolean monitor = false;
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
            fireOnInput = event.fireOnInput();
            monitor = !event.monitoredVariables.getExpression().trim().equals(
                    "");
        }

        if (parameters != null) {
            label = _addLabel(figure, label, parameters);
        }

        if (actions != null) {
            _addLabel(figure, label, actions);
        }

        Rectangle2D bounds = figure.getBounds();
        double y = bounds.getCenterY();
        double x = bounds.getMinX();
        if (monitor) {
            _addTrianglarIcon(figure, fireOnInput ? x + 2.0 : x + 7.0, y,
                    Color.blue);
        }

        if (fireOnInput) {
            _addTrianglarIcon(figure, x + 7.0, y, Color.red);
        }

        return figure;
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
}
