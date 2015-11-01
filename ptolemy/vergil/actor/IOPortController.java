/* The node controller for ports contained in entities.

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
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.SwingConstants;

import ptolemy.actor.CommunicationAspect;
import ptolemy.actor.IOPort;
import ptolemy.actor.PubSubPort;
import ptolemy.actor.PublisherPort;
import ptolemy.actor.SubscriberPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.Typeable;
import ptolemy.kernel.InstantiableNamedObj;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.vergil.kernel.AttributeController;
import diva.canvas.CanvasUtilities;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.connector.PerimeterSite;
import diva.canvas.connector.TerminalFigure;
import diva.canvas.interactor.CompositeInteractor;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.LabelFigure;
import diva.graph.GraphController;
import diva.graph.NodeRenderer;
import diva.util.java2d.Polygon2D;
import diva.util.java2d.ShapeUtilities;

///////////////////////////////////////////////////////////////////
//// IOPortController

/**
 This class provides interaction with nodes that represent Ptolemy II
 ports on an actor.  It provides a double click binding and context
 menu entry to edit the parameters of the port ("Configure") and a
 command to get documentation.
 It can have one of two access levels, FULL or PARTIAL.
 If the access level is FULL, the the context menu also
 contains a command to rename the node.
 Note that whether the port is an input or output or multiport cannot
 be controlled via this interface.  The "Configure Ports" command of
 the container should be invoked instead.

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class IOPortController extends AttributeController {
    /** Create a port controller associated with the specified graph
     *  controller.  The controller is given full access.
     *  @param controller The associated graph controller.
     */
    public IOPortController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create a port controller associated with the
     *  specified graph controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public IOPortController(GraphController controller, Access access) {
        super(controller, access);

        // Override this default value to ensure that ports are
        // not decorated to indicate that they are derived.
        // Instead, the entire actor will be decorated.
        _decoratable = false;

        setNodeRenderer(new EntityPortRenderer());

        //        // "Listen to Actor"
        //        _menuFactory.addMenuItemFactory(new MenuActionFactory(
        //                new ListenToPortAction()));

        // Ports of entities do not use a selection interactor with
        // the same selection model as the rest of the first level figures.
        // If this were allowed, then the port would be able to be deleted.
        CompositeInteractor interactor = new CompositeInteractor();
        setNodeInteractor(interactor);
        interactor.addInteractor(_menuCreator);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The spacing between individual connections to a multiport. */
    public static final double MULTIPORT_CONNECTION_SPACING = 5.0;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return one of {-270, -180, -90, 0, 90, 180, 270} specifying
     *  the orientation of a port. This depends on whether the port
     *  is an input, output, or both, whether the port has a parameter
     *  named "_cardinal" that specifies a cardinality, and whether the
     *  containing actor has a parameter named "_rotatePorts" that
     *  specifies a rotation of the ports.  In addition, if the
     *  containing actor has a parameter named "_flipPortsHorizontal"
     *  or "_flipPortsVertical" with value true, then any ports that end up on the left
     *  or right (top or bottom) will be reversed.
     *  @param port The port.
     *  @return One of {-270, -180, -90, 0, 90, 180, 270}.
     */
    public static int getCardinality(Port port) {
        // Determine whether the port has an attribute that specifies
        // which side of the icon it should be on, and whether the
        // actor has an attribute that rotates the ports. If both
        // are present, the port attribute takes precedence.

        boolean isInput = false;
        boolean isOutput = false;
        boolean isInputOutput = false;

        // Figure out what type of port we're dealing with.
        // If ports are not IOPorts, then draw then as ports with
        // no direction.
        if (port instanceof IOPort) {
            isInput = ((IOPort) port).isInput();
            isOutput = ((IOPort) port).isOutput();
            isInputOutput = isInput && isOutput;
        }

        StringAttribute cardinal = null;
        int portRotation = 0;
        try {
            cardinal = (StringAttribute) port.getAttribute("_cardinal",
                    StringAttribute.class);
            NamedObj container = port.getContainer();
            if (container != null) {
                Parameter rotationParameter = (Parameter) container
                        .getAttribute("_rotatePorts", Parameter.class);
                if (rotationParameter != null) {
                    Token rotationValue = rotationParameter.getToken();
                    if (rotationValue instanceof IntToken) {
                        portRotation = ((IntToken) rotationValue).intValue();
                    }
                }
            }
        } catch (IllegalActionException ex) {
            // Ignore and use defaults.
        }
        if (cardinal == null) {
            // Port cardinality is not specified in the port.
            if (isInputOutput) {
                portRotation += -90;
            } else if (isOutput) {
                portRotation += 180;
            }
        } else if (cardinal.getExpression().equalsIgnoreCase("NORTH")) {
            portRotation = 90;
        } else if (cardinal.getExpression().equalsIgnoreCase("SOUTH")) {
            portRotation = -90;
        } else if (cardinal.getExpression().equalsIgnoreCase("EAST")) {
            portRotation = 180;
        } else if (cardinal.getExpression().equalsIgnoreCase("WEST")) {
            portRotation = 0;
        } else { // this shouldn't happen either
            portRotation += -90;
        }

        // Ensure that the port rotation is one of
        // {-270, -180, -90, 0, 90, 180, 270}.
        portRotation = 90 * (portRotation / 90 % 4);

        // Finally, check for horizontal or vertical flipping.
        try {
            NamedObj container = port.getContainer();
            if (container != null) {
                Parameter flipHorizontalParameter = (Parameter) container
                        .getAttribute("_flipPortsHorizontal", Parameter.class);
                if (flipHorizontalParameter != null) {
                    Token rotationValue = flipHorizontalParameter.getToken();
                    if (rotationValue instanceof BooleanToken
                            && ((BooleanToken) rotationValue).booleanValue()) {
                        if (portRotation == 0 || portRotation == -180) {
                            portRotation += 180;
                        } else if (portRotation == 180) {
                            portRotation = 0;
                        }
                    }
                }
                Parameter flipVerticalParameter = (Parameter) container
                        .getAttribute("_flipPortsVertical", Parameter.class);
                if (flipVerticalParameter != null) {
                    Token rotationValue = flipVerticalParameter.getToken();
                    if (rotationValue instanceof BooleanToken
                            && ((BooleanToken) rotationValue).booleanValue()) {
                        if (portRotation == -270 || portRotation == -90) {
                            portRotation += 180;
                        } else if (portRotation == 90 || portRotation == 270) {
                            portRotation -= 180;
                        }
                    }
                }
            }
        } catch (IllegalActionException ex) {
            // Ignore and use defaults.
        }

        return portRotation;
    }

    /** Return the direction associated with the specified angle,
     *  which is assumed to be one of {-270, -180, -90, 0, 90, 180, 270}.
     *  @param portRotation The angle
     *  @return One of SwingUtilities.NORTH, SwingUtilities.EAST,
     *  SwingUtilities.SOUTH, or SwingUtilities.WEST.
     */
    public static int getDirection(int portRotation) {
        int direction;
        if (portRotation == 90 || portRotation == -270) {
            direction = SwingConstants.NORTH;
        } else if (portRotation == 180 || portRotation == -180) {
            direction = SwingConstants.EAST;
        } else if (portRotation == 270 || portRotation == -90) {
            direction = SwingConstants.SOUTH;
        } else {
            direction = SwingConstants.WEST;
        }
        return direction;
    }

    /** Return one of {-270, -180, -90, 0, 90, 180, 270} specifying
     *  the orientation of a port. This depends on whether the port
     *  is an input, output, or both, whether the port has a parameter
     *  named "_cardinal" that specifies a cardinality, and whether the
     *  containing actor has a parameter named "_rotatePorts" that
     *  specifies a rotation of the ports.  In addition, if the
     *  containing actor has a parameter named "_flipPortsHorizontal"
     *  or "_flipPortsVertical" with value true, then any ports that end up on the left
     *  or right (top or bottom) will be reversed.
     *  @deprecated Use public getCardinality() method instead
     *  @param port The port.
     *  @return One of {-270, -180, -90, 0, 90, 180, 270}.
     *  @see ptolemy.vergil.actor.IOPortController#getCardinality(Port)
     */
    @Deprecated
    protected static int _getCardinality(Port port) {
        return getCardinality(port);
    }

    /**
     * Get the class label of the component.
     * @return the class label of the component.
     */
    @Override
    protected String _getComponentType() {
        return "Port";
    }

    /** Return the direction associated with the specified angle,
     *  which is assumed to be one of {-270, -180, -90, 0, 90, 180, 270}.
     *  @deprecated Use public getDirection() method instead
     *  @param portRotation The angle
     *  @return One of SwingUtilities.NORTH, SwingUtilities.EAST,
     *  SwingUtilities.SOUTH, or SwingUtilities.WEST.
     *  @see ptolemy.vergil.actor.IOPortController#getDirection(int)
     */
    @Deprecated
    protected static int _getDirection(int portRotation) {
        return getDirection(portRotation);
    }

    /** The text used in the MoveAction.TO_FIRST action menu choice.
     */
    @Override
    protected String _moveToFirstDescription() {
        return "Move to First";
    }

    /** The text used in the MoveAction.TO_LAST action menu choice.
     */
    @Override
    protected String _moveToLastDescription() {
        return "Move to Last";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Color for publish and subscribe labels. */
    private static Color _pubSubLabelColor = new Color(0.0f, 0.4f, 0.4f, 1.0f);

    /** Font for port labels. */
    private static Font _pubSubLabelFont = new Font("SansSerif", Font.PLAIN, 10);

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /**
     * Render the ports of components as triangles. Multiports are rendered
     * hollow, while single ports are rendered filled in black. ParameterPort
     * is filled in grey. PublisherPort and SubscriberPort are filled with
     * cyan.
     */
    public class EntityPortRenderer implements NodeRenderer {

        /**  Render a visual representation of the given node. If the
         * StringAttribute _color of the node is set then use that color to
         * render the node. If the StringAttribute _explanation of the node is
         * set then use it to set the tooltip.
         * @see diva.graph.NodeRenderer#render(java.lang.Object)
         */
        @Override
        public Figure render(Object n) {
            final Port port = (Port) n;

            // If the port has an attribute called "_hide", then
            // do not render it.
            if (_isPropertySet(port, "_hide")) {
                return null;
            }

            boolean isInput = false;
            boolean isOutput = false;
            boolean isInputOutput = false;

            // Figure out what type of port we're dealing with.
            // If ports are not IOPorts, then draw then as ports with
            // no direction.
            if (port instanceof IOPort) {
                isInput = ((IOPort) port).isInput();
                isOutput = ((IOPort) port).isOutput();
                isInputOutput = isInput && isOutput;
            }

            // The shape that the port will have. These are all
            // created as oriented to the West, i.e. the left side of
            // the actor.
            Shape shape;

            // NOTE: The preferences mechanism may set this.
            Token portSize = PtolemyPreferences.preferenceValue(port,
                    "_portSize");

            // Default size is 4.0.
            double size = 4.0;
            if (portSize instanceof DoubleToken) {
                size = ((DoubleToken) portSize).doubleValue();
            }
            double halfSize = size / 2.0;
            double doubleSize = size * 2.0;

            if (size == 0.0) {
                // Use an empty ellipse as the shape.
                shape = new Ellipse2D.Double();
            } else {
                if (isInputOutput) {
                    Polygon2D.Double polygon = new Polygon2D.Double();
                    polygon.moveTo(0, -size);
                    polygon.lineTo(-size, -size);
                    polygon.lineTo(-halfSize, 0);
                    polygon.lineTo(-size, size);
                    polygon.lineTo(size, size);
                    polygon.lineTo(halfSize, 0);
                    polygon.lineTo(size, -size);
                    polygon.lineTo(0, -size);
                    polygon.closePath();
                    shape = polygon;
                } else if (isInput) {
                    Polygon2D.Double polygon = new Polygon2D.Double();
                    polygon.moveTo(-size, 0);
                    polygon.lineTo(-size, size);
                    polygon.lineTo(size, 0);
                    polygon.lineTo(-size, -size);
                    polygon.lineTo(-size, 0);
                    polygon.closePath();
                    shape = polygon;
                } else if (isOutput) {
                    Polygon2D.Double polygon = new Polygon2D.Double();
                    polygon.moveTo(size, 0);
                    polygon.lineTo(size, -size);
                    polygon.lineTo(-size, 0);
                    polygon.lineTo(size, size);
                    polygon.lineTo(size, 0);
                    polygon.closePath();
                    shape = polygon;
                } else {
                    shape = new Ellipse2D.Double(-size, -size, doubleSize,
                            doubleSize);
                }
            }
            boolean isPubSubPort = port instanceof PubSubPort;
            Color fill;
            if (port instanceof ParameterPort) {
                fill = Color.lightGray;
            } else if (isPubSubPort) {
                fill = Color.CYAN;
            } else if (port instanceof IOPort && ((IOPort) port).isMultiport()) {
                fill = Color.white;
            } else {
                fill = Color.black;
            }

            // Handle communication aspects.
            try {
                if (port instanceof IOPort) {
                    List<CommunicationAspect> communicationAspects = ((IOPort) port)
                            .getCommunicationAspects();
                    if (communicationAspects != null
                            && communicationAspects.size() > 0) {
                        Object object = null;
                        if (((IOPort) port).isOutput()) {
                            object = communicationAspects.get(0);
                        } else {
                            object = communicationAspects
                                    .get(communicationAspects.size() - 1);
                        }
                        ColorAttribute color = null;
                        if (object != null) {
                            color = (ColorAttribute) ((NamedObj) object)
                                    .getAttribute(CommunicationAspect.decoratorHighlightColorName);
                        }
                        if (color != null) {
                            fill = color.asColor();
                        } else {
                            fill = Color.BLACK;
                        }

                        StringAttribute info = (StringAttribute) port
                                .getAttribute("_showInfo");
                        if (info == null) {
                            info = new StringAttribute(port, "_showInfo");
                        }

                        StringBuffer aspectsStringBuffer = new StringBuffer();
                        for (int j = 0; j < communicationAspects.size(); j++) {
                            NamedObj namedObj = (NamedObj) communicationAspects
                                    .get(j);
                            if (namedObj != null) {
                                if (aspectsStringBuffer.length() > 0) {
                                    aspectsStringBuffer.append(", ");
                                }
                                aspectsStringBuffer.append(namedObj.getName());
                            }
                        }
                        info.setExpression("Aspects: "
                                + aspectsStringBuffer.toString());
                    } else {
                        // No CommunicationAspect in use anymore, clean up _showInfo
                        // string.

                        // Use Attribute here instead of StringAttribute since
                        // the attribute could be e.g. a StringParameter, which
                        // is an Attribute but not a StringAttribute
                        Attribute info = port.getAttribute("_showInfo");
                        if (info != null && info instanceof StringAttribute) {
                            String infoString = ((StringAttribute) info)
                                    .getValueAsString();
                            if (infoString.contains("Aspects:")) {
                                int start = infoString.indexOf("Aspects: ");
                                int end = infoString.length();
                                String aspectInfo = infoString.substring(start,
                                        end);
                                infoString = infoString.replace(aspectInfo, "");
                                infoString = infoString.trim();
                                ((StringAttribute) info)
                                .setExpression(infoString);
                            }
                        }
                    }
                }
            } catch (IllegalActionException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (NameDuplicationException e) {
                // Ignore. This exception should be thrown because before adding the
                // new ColorAttribute any previous attribute with the same name is
                // removed.
                e.printStackTrace();
            }

            ColorAttribute colorAttribute;
            try {
                colorAttribute = (ColorAttribute) port.getAttribute("_color",
                        ColorAttribute.class);
                if (colorAttribute != null) {
                    Color color = colorAttribute.asColor();
                    fill = color;
                }
            } catch (IllegalActionException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            //            StringAttribute _colorAttr = (StringAttribute) (port
            //                    .getAttribute("_color"));
            //
            //            if (_colorAttr != null) {
            //                String _color = _colorAttr.getExpression();
            //                fill = SVGUtilities.getColor(_color);
            //            }

            //ActorGraphModel model = (ActorGraphModel) getController()
            //        .getGraphModel();

            int portRotation = getCardinality(port);
            int direction = getDirection(portRotation);

            // Transform the port shape so it is facing the right way.
            double rotation = portRotation;
            AffineTransform transform = AffineTransform.getRotateInstance(Math
                    .toRadians(rotation));
            shape = ShapeUtilities.transformModify(shape, transform);

            // Create a figure with a tooltip.
            Figure figure = new BasicFigure(shape, fill, (float) 1.5) {
                // Override this because we want to show the type.
                // It doesn't work to set it once because the type
                // has not been resolved, and anyway, it may
                // change. NOTE: This is copied below.
                @Override
                public String getToolTipText() {
                    String tipText = port.getName();
                    String displayName = port.getDisplayName();
                    if (!tipText.equals(displayName)) {
                        tipText = displayName + " (" + tipText + ")";
                    }
                    StringAttribute _explAttr = (StringAttribute) port
                            .getAttribute("_explanation");

                    if (_explAttr != null) {
                        tipText = _explAttr.getExpression();
                    } else if (port instanceof Typeable) {
                        try {
                            tipText = tipText + ", type:"
                                    + ((Typeable) port).getType();
                        } catch (IllegalActionException ex) {
                            // System.out.println("Tooltip failed: " + ex);
                        }
                    }

                    return tipText;
                }
            };
            // Have to do this also, or the AWT doesn't display any
            // tooltip at all.
            String tipText = port.getName();
            String displayName = port.getDisplayName();
            if (!tipText.equals(displayName)) {
                tipText = displayName + " (" + tipText + ")";
            }
            figure.setToolTipText(tipText);

            double normal = CanvasUtilities.getNormal(direction);

            if (port instanceof IOPort) {
                // Create a diagonal connector for multiports, if necessary.
                IOPort ioPort = (IOPort) port;

                if (ioPort.isMultiport()) {
                    int numberOfLinks = ioPort.linkedRelationList().size();

                    if (numberOfLinks > 1) {
                        // The diagonal is necessary.
                        CompositeFigure compositeFigure = new CompositeFigure(
                                figure) {
                            // Override this because we want to show the type.
                            // It doesn't work to set it once because the type
                            // has not been resolved, and anyway, it may
                            // change. NOTE: This is copied from above.
                            @Override
                            public String getToolTipText() {
                                String tipText = port.getName();
                                String displayName = port.getDisplayName();
                                if (!tipText.equals(displayName)) {
                                    tipText = displayName + " (" + tipText
                                            + ")";
                                }
                                StringAttribute _explAttr = (StringAttribute) port
                                        .getAttribute("_explanation");

                                if (_explAttr != null) {
                                    tipText = _explAttr.getExpression();
                                } else if (port instanceof Typeable) {
                                    try {
                                        tipText = tipText + ", type:"
                                                + ((Typeable) port).getType();
                                    } catch (IllegalActionException ex) {
                                        // System.out.println("Tooltip failed: " + ex);
                                    }
                                }
                                return tipText;
                            }
                        };

                        // Line depends on the orientation.
                        double startX;

                        // Line depends on the orientation.
                        double startY;

                        // Line depends on the orientation.
                        double endX;

                        // Line depends on the orientation.
                        double endY;
                        Rectangle2D bounds = figure.getShape().getBounds2D();
                        double x = bounds.getX();
                        double y = bounds.getY();
                        double width = bounds.getWidth();
                        double height = bounds.getHeight();
                        int extent = numberOfLinks - 1;

                        if (direction == SwingConstants.EAST) {
                            startX = x + width;
                            startY = y + height / 2;
                            endX = startX + extent
                                    * MULTIPORT_CONNECTION_SPACING;
                            endY = startY + extent
                                    * MULTIPORT_CONNECTION_SPACING;
                        } else if (direction == SwingConstants.WEST) {
                            startX = x;
                            startY = y + height / 2;
                            endX = startX - extent
                                    * MULTIPORT_CONNECTION_SPACING;
                            endY = startY - extent
                                    * MULTIPORT_CONNECTION_SPACING;
                        } else if (direction == SwingConstants.NORTH) {
                            startX = x + width / 2;
                            startY = y;
                            endX = startX - extent
                                    * MULTIPORT_CONNECTION_SPACING;
                            endY = startY - extent
                                    * MULTIPORT_CONNECTION_SPACING;
                        } else {
                            startX = x + width / 2;
                            startY = y + height;
                            endX = startX + extent
                                    * MULTIPORT_CONNECTION_SPACING;
                            endY = startY + extent
                                    * MULTIPORT_CONNECTION_SPACING;
                        }

                        Line2D line = new Line2D.Double(startX, startY, endX,
                                endY);
                        Figure lineFigure = new BasicFigure(line, fill,
                                (float) 2.0);
                        compositeFigure.add(lineFigure);
                        figure = compositeFigure;
                    }
                }
                if (port instanceof PublisherPort) {
                    CompositeFigure compositeFigure = new CompositeFigure(
                            figure);
                    Path2D path = new Path2D.Double();
                    path.moveTo(0.5 * size, -size);
                    path.lineTo(2.5 * size, 0);
                    path.lineTo(0.5 * size, size);

                    path.moveTo(2 * size, -size);
                    path.lineTo(4 * size, 0);
                    path.lineTo(2 * size, size);

                    Figure pathFigure = new BasicFigure(path);
                    compositeFigure.add(pathFigure);

                    String channel = "???";
                    try {
                        if (((InstantiableNamedObj) port.getContainer())
                                .isWithinClassDefinition()) {
                            // If the port is in a class definition, do not expand it, it might contain $foo.$bar.
                            channel = ((PublisherPort) port).channel
                                    .getExpression();
                        } else {
                            channel = ((PublisherPort) port).channel
                                    .stringValue();
                        }
                        // Also display the number of initial tokens in parens.
                        Token initialTokens = ((PublisherPort) port).initialTokens
                                .getToken();
                        if (initialTokens instanceof ArrayToken) {
                            int number = ((ArrayToken) initialTokens).length();
                            channel = channel + " (" + number + ")";
                        }
                    } catch (IllegalActionException e) {
                        // Ignore and display question marks.
                        e.printStackTrace();
                    }
                    // The anchor argument below is (sadly) ignored.
                    Figure label = new LabelFigure(channel, _pubSubLabelFont,
                            0.0, SwingConstants.SOUTH_EAST, _pubSubLabelColor);
                    double labelHeight = label.getBounds().getHeight();
                    label.translate(5 * size, 0.5 * labelHeight);
                    compositeFigure.add(label);

                    figure = compositeFigure;
                } else if (port instanceof SubscriberPort) {
                    CompositeFigure compositeFigure = new CompositeFigure(
                            figure);
                    Path2D path = new Path2D.Double();
                    path.moveTo(-2 * size, -size);
                    path.lineTo(-2 * size, size);

                    path.moveTo(-3 * size, -size);
                    path.lineTo(-3 * size, size);

                    path.moveTo(-4 * size, -size);
                    path.lineTo(-4 * size, size);

                    Figure pathFigure = new BasicFigure(path);
                    compositeFigure.add(pathFigure);

                    String channel = "???";
                    try {
                        if (((InstantiableNamedObj) port.getContainer())
                                .isWithinClassDefinition()) {
                            // If the port is in a class definition, do not expand it, it might contain $foo.$bar.
                            channel = ((SubscriberPort) port).channel
                                    .getExpression();
                        } else {
                            channel = ((SubscriberPort) port).channel
                                    .stringValue();
                        }
                        // Also display the number of initial tokens in parens.
                        Token initialTokens = ((SubscriberPort) port).initialTokens
                                .getToken();
                        if (initialTokens instanceof ArrayToken) {
                            int number = ((ArrayToken) initialTokens).length();
                            channel = channel + " (" + number + ")";
                        }
                    } catch (IllegalActionException e) {
                        // Ignore and display question marks.
                        e.printStackTrace();
                    }
                    // The anchor argument below is (sadly) ignored.
                    Figure label = new LabelFigure(channel, _pubSubLabelFont,
                            0.0, SwingConstants.SOUTH_EAST, _pubSubLabelColor);
                    double labelHeight = label.getBounds().getHeight();
                    double labelWidth = label.getBounds().getWidth();
                    label.translate(-labelWidth - 5 * size, 0.5 * labelHeight);
                    compositeFigure.add(label);

                    figure = compositeFigure;
                }
                figure = _decoratePortFigure(n, figure);
                // Wrap the figure in a TerminalFigure to set the direction that
                // connectors exit the port. Note that this direction is the
                // same direction that is used to layout the port in the
                // Entity Controller.
                figure = new PortTerminal(ioPort, figure, normal, false);
            } else {
                figure = _decoratePortFigure(n, figure);
                Site tsite = new PerimeterSite(figure, 0);
                tsite.setNormal(normal);
                figure = new TerminalFigure(figure, tsite);
            }

            // New way to specify a highlight color.
            AttributeController.renderHighlight(port, figure);

            return figure;
        }

        /** Decorate the figure according to the properties of the node. This
         *  method does nothing, but subclasses may override this method to
         *  decorate the port's figure (e.g., add highlighting color).
         *
         *  @param node The node.
         *  @param figure The port's figure before decoration.
         *  @return The port's figure after decoration (which may or may not be
         *   the figure given in the parameter).
         */
        protected Figure _decoratePortFigure(Object node, Figure figure) {
            return figure;
        }
    }
}
