/* The graph controller for the ptolemy schematic editor ports

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.vergil.actor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;
import javax.swing.SwingConstants;

import ptolemy.actor.IOPort;
import ptolemy.actor.PubSubPort;
import ptolemy.actor.PublisherPort;
import ptolemy.actor.SubscriberPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.type.Typeable;
import ptolemy.kernel.InstantiableNamedObj;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.basic.BasicGraphController;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.WithIconGraphController;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.toolbox.EditIconAction;
import ptolemy.vergil.toolbox.RemoveIconAction;
import ptolemy.vergil.toolbox.SnapConstraint;
import diva.canvas.CanvasUtilities;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.connector.FixedNormalSite;
import diva.canvas.connector.PerimeterSite;
import diva.canvas.connector.TerminalFigure;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.LabelFigure;
import diva.graph.GraphController;
import diva.graph.GraphPane;
import diva.util.java2d.Polygon2D;

///////////////////////////////////////////////////////////////////
//// ExternalIOPortController

/**
 This class provides interaction with nodes that represent Ptolemy II
 ports inside a composite.  It provides a double click binding and context
 menu entry to edit the parameters of the port ("Configure") and a
 command to get documentation.
 It can have one of two access levels, FULL or PARTIAL.
 If the access level is FULL, the the context menu also
 contains a command to rename the node.
 Note that whether the port is an input or output or multiport cannot
 be controlled via this interface.  The "Configure Ports" command of
 the container should be invoked instead.

 @author Steve Neuendorffer and Edward A. Lee, Elaine Cheong
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class ExternalIOPortController extends AttributeController {
    /** Create a port controller associated with the specified graph
     *  controller.  The controller is given full access.
     *  @param controller The associated graph controller.
     */
    public ExternalIOPortController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create a port controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public ExternalIOPortController(GraphController controller, Access access) {
        super(controller, access);
        setNodeRenderer(new PortRenderer());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** Prototype input port. */
    public static final IOPort _GENERIC_INPUT = new IOPort();

    /** Prototype output port. */
    public static final IOPort _GENERIC_OUTPUT = new IOPort();

    /** Prototype inout port. */
    public static final IOPort _GENERIC_INOUT = new IOPort();

    /** Prototype input multiport. */
    public static final IOPort _GENERIC_INPUT_MULTIPORT = new IOPort();

    /** Prototype output multiport. */
    public static final IOPort _GENERIC_OUTPUT_MULTIPORT = new IOPort();

    /** Prototype inout multiport. */
    public static final IOPort _GENERIC_INOUT_MULTIPORT = new IOPort();

    /** Polygon coordinates for input output port. */
    public static Integer[] IOPORT_COORDINATES = new Integer[] { 0, 4, 0, 9, 6,
        4, 12, 4, 12, -4, 6, -4, 0, -9, 0, -4, -8, -4 };

    /** Polygon coordinates for input port. */
    public static Integer[] IPORT_COORDINATES = new Integer[] { 0, 4, 0, 9, 12,
        0, 0, -9, 0, -4, -8, -4 };

    /** Polygon coordinates for output port. */
    public static Integer[] OPORT_COORDINATES = new Integer[] { -8, 9, -2, 4,
        12, 4, 12, -4, -2, -4, -8, -9 };

    /** Polygon coordinates for input output multiport. */
    public static Integer[] MULTI_IPORT_COORDINATES = new Integer[] { -5, 4,
        -5, 9, 1, 4, 1, 9, 7, 4, 12, 0, 7, -4, 1, -9, 1, -4, -5, -9, -5,
        -4, -8, -4 };

    /** Polygon coordinates for output multiport. */
    public static Integer[] MULTI_OPORT_COORDINATES = new Integer[] { -8, 4,
        -8, 9, -2, 4, -2, 9, 4, 4, 12, 4, 12, -4, 4, -4, -2, -9, -2, -4,
        -8, -9 };

    /** Polygon coordinates for input multiport. */
    public static Integer[] MULTI_IOPORT_COORDINATES = new Integer[] { -4, 4,
        -4, 9, 2, 4, 2, 9, 8, 4, 12, 4, 12, -4, 8, -4, 2, -9, 2, -4, -4,
        -9, -4, -4, -8, -4 };

    // Static initializer.
    static {
        try {
            _GENERIC_INPUT.setInput(true);
            _GENERIC_OUTPUT.setOutput(true);
            _GENERIC_INOUT.setInput(true);
            _GENERIC_INOUT.setOutput(true);
            _GENERIC_INPUT_MULTIPORT.setInput(true);
            _GENERIC_OUTPUT_MULTIPORT.setOutput(true);
            _GENERIC_INOUT_MULTIPORT.setInput(true);
            _GENERIC_INOUT_MULTIPORT.setOutput(true);
            _GENERIC_INPUT_MULTIPORT.setMultiport(true);
            _GENERIC_OUTPUT_MULTIPORT.setMultiport(true);
            _GENERIC_INOUT_MULTIPORT.setMultiport(true);

            // Need location attributes for these ports in order to
            // be able to render them.
            new Location(_GENERIC_INPUT, "_location");
            new Location(_GENERIC_OUTPUT, "_location");
            new Location(_GENERIC_INOUT, "_location");
            new Location(_GENERIC_INPUT_MULTIPORT, "_location");
            new Location(_GENERIC_OUTPUT_MULTIPORT, "_location");
            new Location(_GENERIC_INOUT_MULTIPORT, "_location");
        } catch (KernelException ex) {
            // Should not occur.
            throw new InternalErrorException(null, ex, null);
        }
    }

    /** Move the node's figure to the location specified in the node's
     *  semantic object, if that object is an instance of Locatable.
     *  If the semantic object is not a location, then do nothing.
     *  If the figure associated with the semantic object is an instance
     *  of TerminalFigure, then modify the location to ensure that the
     *  connect site snaps to grid.
     *  @param node The object to locate.
     */
    @Override
    public void locateFigure(Object node) {
        Figure nf = getController().getFigure(node);

        try {
            if (hasLocation(node)) {
                double[] location = getLocation(node);
                if (node instanceof Location) {
                    NamedObj port = ((Location) node).getContainer();

                    // In case the location is (0,0) we try to come up with a
                    // better one.
                    if (port instanceof IOPort && location[0] == 0.0
                            && location[1] == 0.0) {
                        BasicGraphController controller = (BasicGraphController) getController();
                        BasicGraphFrame frame = controller.getFrame();

                        // We have a bootstrapping problem. In case the window
                        // is just being opened, the rendering happens before the
                        // creation of the the JGraph, and we don't know the actual
                        // size of the window (hence the magic numbers below in case
                        // frame.getJGraph() == null.
                        if (frame.getJGraph() != null) {
                            GraphPane pane = controller.getGraphPane();
                            location = WithIconGraphController
                                    .getNewPortLocation(pane, frame,
                                            (IOPort) port);
                        } else {
                            IOPort ioPort = (IOPort) port;
                            if (ioPort.isInput() && ioPort.isOutput()) {
                                double[] newLocation = _inoutputPortLocations
                                        .get(ioPort);
                                if (newLocation != null) {
                                    location = newLocation;
                                } else {
                                    // Put at the bottom
                                    location[0] = 300.0 + _inoutputPortLocations
                                            .size() * 40;
                                    location[1] = 380.0;
                                    _inoutputPortLocations
                                    .put(ioPort, location);
                                }
                            } else if (ioPort.isInput()) {
                                double[] newLocation = _inputPortLocations
                                        .get(ioPort);
                                if (newLocation != null) {
                                    location = newLocation;
                                } else {
                                    // Put at the left side
                                    location[0] = 20.0;
                                    location[1] = 200.0 + _inputPortLocations
                                            .size() * 40;
                                    _inputPortLocations.put(ioPort, location);
                                }
                            } else if (ioPort.isOutput()) {
                                double[] newLocation = _outputPortLocations
                                        .get(ioPort);
                                if (newLocation != null) {
                                    location = newLocation;
                                } else {
                                    // Put at the right side
                                    location[0] = 580.0;
                                    location[1] = 200.0 + _outputPortLocations
                                            .size() * 40;
                                    _outputPortLocations.put(ioPort, location);
                                }
                            } else {
                                double[] newLocation = _otherPortLocations
                                        .get(ioPort);
                                if (newLocation != null) {
                                    location = newLocation;
                                } else {
                                    // Put in the middle
                                    location[0] = 300.0;
                                    location[1] = 200.0 + _otherPortLocations
                                            .size() * 40;
                                    _otherPortLocations.put(ioPort, location);
                                }
                            }

                        }
                        location = SnapConstraint.constrainPoint(location[0],
                                location[1]);
                    }
                }
                CanvasUtilities.translateTo(nf, location[0], location[1]);
            }
        } catch (Throwable throwable) {
            // FIXME: Ignore if there is no valid location.  This
            // happens occasionally due to a race condition in the
            // Bouncer demo.  Occasionally, the repaint thread will
            // attempt to locate the bouncing icon before the location
            // parameter has been evaluated, causing an exception to
            // be thrown.  Basically the lazy parameter evaluation
            // mechanism causes rerendering in Diva to be rentrant,
            // which it shouldn't be.  Unfortunately, I have no idea
            // how to fix it... SN 5/5/2003
        }
    }

    /** Set the configuration.  This is used in derived classes to
     *  to open files (such as documentation).  The configuration is
     *  is important because it keeps track of which files are already
     *  open and ensures that there is only one editor operating on the
     *  file at any one time.
     *  @param configuration The configuration.
     */
    @Override
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);

        if (_configuration != null) {
            // Create an Appearance submenu.
            _createAppearanceSubmenu();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to return true if the specified node
     *  contains an attribute named "_hideInside".  This ensures that
     *  ports can be hidden on the outside while still being visible
     *  on the outside.
     */
    @Override
    protected boolean _hide(java.lang.Object node) {
        if (node instanceof Locatable) {
            if (((Locatable) node).getContainer().getAttribute("_hideInside") != null) {
                return true;
            }
        }

        if (node instanceof NamedObj) {
            if (((NamedObj) node).getAttribute("_hideInside") != null) {
                return true;
            }
        }

        return false;
    }

    /** Given a port, return a reasonable tooltip message for that port.
     *  @param port The port.
     *  @return The name, type, and whether it's a multiport.
     */
    protected String _portTooltip(final Port port) {
        String tipText = port.getName();

        if (port instanceof IOPort) {
            IOPort ioport = (IOPort) port;

            if (ioport.isInput()) {
                tipText += ", Input";
            }

            if (ioport.isOutput()) {
                tipText += ", Output";
            }

            if (ioport.isMultiport()) {
                tipText += ", Multiport";
            }

            try {
                tipText = tipText + ", type:" + ((Typeable) port).getType();
            } catch (ClassCastException ex) {
                // Do nothing.
            } catch (IllegalActionException ex) {
                // Do nothing.
            }
        }

        return tipText;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The action that handles edit custom icon. */
    protected EditIconAction _editIconAction = new EditIconAction();

    /** The font used to label a port. */
    protected static Font _labelFont = new Font("SansSerif", Font.PLAIN, 12);

    /** The action that handles removing a custom icon. */
    protected RemoveIconAction _removeIconAction = new RemoveIconAction();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Create an Appearance submenu.
     */
    private void _createAppearanceSubmenu() {
        _editIconAction.setConfiguration(_configuration);
        _removeIconAction.setConfiguration(_configuration);
        Action[] actions = { _editIconAction, _removeIconAction };
        _appearanceMenuActionFactory.addActions(actions, "Appearance");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Render the external ports of a graph as a 5-sided tab thingy.
     *  Multiports are rendered hollow,
     *  while single ports are rendered filled.
     *  Publisher and subscriber ports are rendered specially.
     */
    public class PortRenderer extends IconRenderer {

        /** Render a port.  If the argument implements Locatable,
         *  then render the port that is the container of that locatable.
         *  If the argument is an instance of _GENERIC_INPUT,
         *  _GENERIC_OUTPUT, or _GENERIC_INOUT, then render an input,
         *  output, or inout port with no name.  If the argument is null,
         *  then render a port that is neither an input nor an output.
         *  @param n An instance of Locatable or one of the objects
         *   _GENERIC_INPUT, _GENERIC_OUTPUT, or _GENERIC_INOUT.
         *  @return The figure that is rendered.
         */
        @Override
        public Figure render(Object n) {
            Figure figure = null;
            Locatable location = (Locatable) n;
            if (location == null) {
                Polygon2D.Double polygon = new Polygon2D.Double();
                polygon.moveTo(0, 0);
                polygon.lineTo(0, 10);
                polygon.lineTo(12, 0);
                polygon.lineTo(0, -10);
                polygon.closePath();

                figure = new BasicFigure(polygon, Color.black);
                figure.setToolTipText("Unknown port");
                return figure;
            }

            final Port port = (Port) location.getContainer();
            List iconList = port.attributeList(EditorIcon.class);

            // Check to see whether there is an icon that has been created,
            // but not inserted.
            if (iconList.size() > 0) {
                EditorIcon icon = (EditorIcon) iconList
                        .get(iconList.size() - 1);
                figure = icon.createFigure();
            } else {
                Attribute iconDescription = port
                        .getAttribute("_iconDescription");
                if (iconDescription != null) {
                    figure = super.render(n);
                }
            }

            // Wrap the figure in a TerminalFigure to set the direction that
            // connectors exit the port.  Note that this direction is the
            // OPPOSITE direction that is used to layout the port in the
            // Entity Controller.
            int direction;
            Shape shape;
            Polygon2D.Double polygon = new Polygon2D.Double();
            Color fill = Color.black;
            if (figure == null) {
                if (!(port instanceof IOPort)) {
                    polygon.moveTo(-6, 6);
                    polygon.lineTo(0, 6);
                    polygon.lineTo(8, 0);
                    polygon.lineTo(0, -6);
                    polygon.lineTo(-6, -6);
                } else {
                    IOPort ioport = (IOPort) port;
                    polygon.moveTo(-8, 4);
                    if (ioport.isMultiport()) {
                        if (ioport instanceof ParameterPort) {
                            // It would be better to couple these to the
                            // parameters by position, but this is impossible
                            // in diva, so we assign a special color.
                            // FIXME: are Multiport ParameterPorts possible?
                            // FIXME: Should this be lightGrey?
                            fill = Color.darkGray;
                        } else if (ioport instanceof PublisherPort
                                || ioport instanceof SubscriberPort) {
                            fill = Color.CYAN;
                        } else {
                            fill = Color.white;
                        }
                        if (ioport.isOutput() && ioport.isInput()) {
                            polygon = _createPolygon(MULTI_IOPORT_COORDINATES,
                                    polygon);
                        } else if (ioport.isOutput()) {
                            polygon = _createPolygon(MULTI_OPORT_COORDINATES,
                                    polygon);
                        } else if (ioport.isInput()) {
                            polygon = _createPolygon(MULTI_IPORT_COORDINATES,
                                    polygon);
                        } else {
                            polygon = null;
                        }
                    } else {
                        if (ioport instanceof ParameterPort) {
                            // It would be better to couple these to the
                            // parameters by position, but this is impossible
                            // in diva, so we assign a special color.
                            // FIXME: what about multiports para
                            fill = Color.lightGray;
                        } else if (ioport instanceof PublisherPort
                                || ioport instanceof SubscriberPort) {
                            fill = Color.CYAN;
                        } else {
                            fill = Color.black;
                        }
                        if (ioport.isOutput() && ioport.isInput()) {
                            polygon = _createPolygon(IOPORT_COORDINATES,
                                    polygon);
                        } else if (ioport.isOutput()) {
                            polygon = _createPolygon(OPORT_COORDINATES, polygon);
                        } else if (ioport.isInput()) {
                            polygon = _createPolygon(IPORT_COORDINATES, polygon);
                        } else {
                            polygon = null;
                        }
                    }
                }

                if (polygon == null) {
                    Ellipse2D.Double ellipse = new Ellipse2D.Double(0.0, 0.0,
                            16.0, 16.0);
                    shape = ellipse;
                } else {
                    polygon.closePath();
                    shape = polygon;
                }
                if (port instanceof ParameterPort) {
                    figure = new BasicFigure(shape, new Color(0, 0, 0, 0),
                            (float) 0.0);
                } else {
                    figure = new BasicFigure(shape, fill, (float) 1.5);
                }
            }

            if (!(port instanceof IOPort)) {
                direction = SwingConstants.NORTH;
            } else {
                IOPort ioport = (IOPort) port;

                if (ioport.isInput() && ioport.isOutput()) {
                    direction = SwingConstants.NORTH;
                } else if (ioport.isInput()) {
                    direction = SwingConstants.EAST;
                } else if (ioport.isOutput()) {
                    direction = SwingConstants.WEST;
                } else {
                    // should never happen
                    direction = SwingConstants.NORTH;
                }
            }

            double normal = CanvasUtilities.getNormal(direction);
            String name = port.getDisplayName();
            Rectangle2D backBounds = figure.getBounds();
            figure = new CompositeFigure(figure) {
                // Override this because we want to show the type.
                // It doesn't work to set it once because the type
                // has not been resolved, and anyway, it may
                // change. NOTE: This is copied from above.
                @Override
                public String getToolTipText() {
                    return _portTooltip(port);
                }
            };

            if (name != null && !name.equals("")
                    && !(port instanceof ParameterPort)) {
                // Do not create a label if there is a custom icon.
                List<EditorIcon> icons = port.attributeList(EditorIcon.class);
                if (icons.size() == 0) {
                    LabelFigure label = new LabelFigure(name, _labelFont, 1.0,
                            SwingConstants.SOUTH_WEST);

                    // Shift the label slightly right so it doesn't
                    // collide with ports.
                    label.translateTo(backBounds.getX(), backBounds.getY());
                    ((CompositeFigure) figure).add(label);
                }
            }

            if (port instanceof IOPort) {
                // Create a diagonal connector for multiports, if necessary.
                IOPort ioPort = (IOPort) port;

                if (ioPort.isMultiport()) {
                    int numberOfLinks = ioPort.insideRelationList().size();

                    if (numberOfLinks > 1) {
                        // The diagonal is necessary.
                        // Line depends on the orientation.
                        double startX, startY, endX, endY;
                        Rectangle2D bounds = figure.getShape().getBounds2D();
                        double x = bounds.getX();
                        double y = bounds.getY();
                        double width = bounds.getWidth();
                        double height = bounds.getHeight();
                        int extent = numberOfLinks - 1;

                        if (direction == SwingConstants.EAST) {
                            startX = x + width;
                            startY = y + height / 2;
                            endX = startX
                                    + extent
                                    * IOPortController.MULTIPORT_CONNECTION_SPACING;
                            endY = startY
                                    + extent
                                    * IOPortController.MULTIPORT_CONNECTION_SPACING;
                        } else if (direction == SwingConstants.WEST) {
                            startX = x;
                            startY = y + height / 2;
                            endX = startX
                                    - extent
                                    * IOPortController.MULTIPORT_CONNECTION_SPACING;
                            endY = startY
                                    - extent
                                    * IOPortController.MULTIPORT_CONNECTION_SPACING;
                        } else if (direction == SwingConstants.NORTH) {
                            startX = x + width / 2;
                            startY = y;
                            endX = startX
                                    - extent
                                    * IOPortController.MULTIPORT_CONNECTION_SPACING;
                            endY = startY
                                    - extent
                                    * IOPortController.MULTIPORT_CONNECTION_SPACING;
                        } else {
                            // Coverity: Logically dead code:
                            // direction can only be EAST, WEST or
                            // NORTH.

                            // However, if we don't have this else
                            // clause, then the compiler indicates
                            // that startX, startY, endX and endY are
                            // not initialized.  One fix would be to
                            // set them when we declare them, but then
                            // set them again in the clauses above.

                            startX = x + width / 2;
                            startY = y + height;
                            endX = startX
                                    + extent
                                    * IOPortController.MULTIPORT_CONNECTION_SPACING;
                            endY = startY
                                    + extent
                                    * IOPortController.MULTIPORT_CONNECTION_SPACING;
                        }

                        Line2D line = new Line2D.Double(startX, startY, endX,
                                endY);
                        Figure lineFigure = new BasicFigure(line, fill,
                                (float) 2.0);
                        ((CompositeFigure) figure).add(lineFigure);
                    }
                }

                _createPubSubLabels(ioPort, (CompositeFigure) figure);

                figure = new PortTerminal(ioPort, figure, normal, true);
            } else {
                Site tsite = new PerimeterSite(figure, 0);
                tsite.setNormal(normal);
                tsite = new FixedNormalSite(tsite);
                figure = new TerminalFigure(figure, tsite) {
                    // Override this because the tooltip may
                    // change over time.  I.e., the port may
                    // change from being an input or output, etc.
                    @Override
                    public String getToolTipText() {
                        return _portTooltip(port);
                    }
                };
            }

            // Have to do this as well or awt will not render a tooltip.
            figure.setToolTipText(port.getName());
            AttributeController.renderHighlight(port, figure);

            return figure;
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private Polygon2D.Double _createPolygon(Integer[] coordinates,
            Polygon2D.Double polygon) {
        for (int i = 0; i < coordinates.length; i = i + 2) {
            polygon.lineTo(coordinates[i], coordinates[i + 1]);
        }
        return polygon;
    }

    /** Create a label showing the channel and initial values
     *  for PubSubPort. If the port argument is not an instance
     *  of PubSubPort, then do nothing.
     *  @param port The port.
     *  @param figure The composite figure to add the label to.
     */
    private void _createPubSubLabels(IOPort port, CompositeFigure figure) {
        if (!(port instanceof PubSubPort)) {
            return;
        }
        try {
            String channel = null;
            if (((InstantiableNamedObj) port.getContainer())
                    .isWithinClassDefinition()) {
                // If the port is in a class definition, do not expand it, it might contain $foo.$bar.
                channel = "Channel: "
                        + ((PubSubPort) port).channel.getExpression();
            } else {
                channel = "Channel: "
                        + ((PubSubPort) port).channel.stringValue();
            }
            // The anchor argument below is (sadly) ignored.
            Figure label = new LabelFigure(channel, _labelFont, 0.0,
                    SwingConstants.SOUTH_EAST, _pubSubLabelColor);
            double labelHeight = label.getBounds().getHeight();
            Rectangle2D bounds = figure.getShape().getBounds2D();
            label.translate(-8.0, bounds.getMaxY() + labelHeight + 4);
            figure.add(label);

            String initialTokens = ((PubSubPort) port).initialTokens
                    .getExpression();
            if (!initialTokens.trim().equals("")) {
                initialTokens = "Initial tokens: " + initialTokens;
                label = new LabelFigure(initialTokens, _labelFont, 0.0,
                        SwingConstants.SOUTH_EAST, _pubSubLabelColor);
                label.translate(-8.0, bounds.getMaxY() + 2 * labelHeight + 8);
                figure.add(label);
            }
        } catch (Exception e) {
            // Ignore and display question marks.
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Color for publish and subscribe labels. */
    private static Color _pubSubLabelColor = new Color(0.0f, 0.4f, 0.4f, 1.0f);

    // The following maps are to keep track of the ports that already needed
    // to be located (since they had location 0,0).
    private HashMap<Object, double[]> _inputPortLocations = new HashMap<Object, double[]>();
    private HashMap<Object, double[]> _outputPortLocations = new HashMap<Object, double[]>();
    private HashMap<Object, double[]> _inoutputPortLocations = new HashMap<Object, double[]>();
    private HashMap<Object, double[]> _otherPortLocations = new HashMap<Object, double[]>();
}
