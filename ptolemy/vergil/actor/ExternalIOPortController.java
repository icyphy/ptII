/* The graph controller for the ptolemy schematic editor ports

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.actor;

import diva.canvas.AbstractFigure;
import diva.canvas.CanvasUtilities;
import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.connector.FixedNormalSite;
import diva.canvas.connector.PerimeterSite;
import diva.canvas.connector.TerminalFigure;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.LabelFigure;
import diva.graph.GraphController;
import diva.graph.NodeRenderer;
import diva.util.java2d.Polygon2D;

import ptolemy.actor.IOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.type.Typeable;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.kernel.AttributeController;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
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

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
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

    // Prototype input port.
    public static IOPort _GENERIC_INPUT = new IOPort();

    // Prototype output port.
    public static IOPort _GENERIC_OUTPUT = new IOPort();

    // Prototype inout port.
    public static IOPort _GENERIC_INOUT = new IOPort();

    // Prototype input multiport.
    public static IOPort _GENERIC_INPUT_MULTIPORT = new IOPort();

    // Prototype output multiport.
    public static IOPort _GENERIC_OUTPUT_MULTIPORT = new IOPort();

    // Prototype inout multiport.
    public static IOPort _GENERIC_INOUT_MULTIPORT = new IOPort();

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

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to return true if the specified node contains an
     *  attribute named "_hideInside".  This ensures that ports can be hidden
     *  on the outside while still being visible on the outside.
     */
    protected boolean _hide(java.lang.Object node) {
        if (node instanceof Locatable) {
            if (((NamedObj)((Locatable)node)
                    .getContainer()).getAttribute("_hideInside") != null) {
                return true;
            }
        }
        if (node instanceof NamedObj) {
            if (((NamedObj)node).getAttribute("_hideInside") != null) {
                return true;
            }
        }
        return false;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private static Font _labelFont = new Font("SansSerif", Font.PLAIN, 12);

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Render the external ports of a graph as a 5-sided tab thingy.
     *  Multiports are rendered hollow,
     *  while single ports are rendered filled.
     */
    public class PortRenderer implements NodeRenderer {

        /** Render a port.  If the argument implements Locatable,
         *  then render the port that is the container of that locatable.
         *  If the argument is an instance of _GENERIC_INPUT,
         *  _GENERIC_OUTPUT, or _GENERIC_INOUT, then render an input,
         *  output, or inout port with no name.  If the argument is null,
         *  then render a port that is neither an input nor an output.
         *  @param n An instance of Locatable or one of the objects
         *   _GENERIC_INPUT, _GENERIC_OUTPUT, or _GENERIC_INOUT.
         */
        public Figure render(Object n) {
            Polygon2D.Double polygon = new Polygon2D.Double();

            Figure figure;
            // Wrap the figure in a TerminalFigure to set the direction that
            // connectors exit the port.  Note that this direction is the
            // OPPOSITE direction that is used to layout the port in the
            // Entity Controller.
            int direction;
            Locatable location = (Location)n;
            if (location != null) {
                final Port port = (Port)location.getContainer();

                Color fill;
                if (port instanceof IOPort) {
                    IOPort ioport = (IOPort)port;
                    polygon.moveTo(0, 5);
                    polygon.lineTo(0, 10);
                    if (ioport.isOutput()) {
                        polygon.lineTo(6, 5);
                        polygon.lineTo(14, 5);
                        polygon.lineTo(14, -5);
                        polygon.lineTo(6, -5);
                    } else {
                        polygon.lineTo(12, 0);
                    }
                    polygon.lineTo(0, -10);
                    if (ioport.isInput()) {
                        polygon.lineTo(0, -5);
                        polygon.lineTo(-6, -5);
                        polygon.lineTo(-6, 5);
                    }

                    polygon.closePath();

                    if (ioport instanceof ParameterPort) {
                        // It would be better to couple these to the
                        // parameters by position, but this is impossible
                        // in diva, so we assign a special color.
                        fill = Color.lightGray;
                    } else if (ioport.isMultiport()) {
                        fill = Color.white;
                    } else {
                        fill = Color.black;
                    }
                } else {
                    polygon.moveTo(-6, 6);
                    polygon.lineTo(0, 6);
                    polygon.lineTo(8, 0);
                    polygon.lineTo(0, -6);
                    polygon.lineTo(-6, -6);
                    polygon.closePath();
                    fill = Color.black;
                }
                figure = new BasicFigure(polygon, fill, (float)1.5);

                if (!(port instanceof IOPort)) {
                    direction = SwingUtilities.NORTH;
                } else {
                    IOPort ioport = (IOPort)port;

                    if (ioport.isInput() &&
                            ioport.isOutput()) {
                        direction = SwingUtilities.NORTH;
                    } else if (ioport.isInput()) {
                        direction = SwingUtilities.EAST;
                    } else if (ioport.isOutput()) {
                        direction = SwingUtilities.WEST;
                    } else {
                        // should never happen
                        direction = SwingUtilities.NORTH;
                    }
                }
                double normal = CanvasUtilities.getNormal(direction);
                Site tsite = new PerimeterSite(figure, 0);
                tsite.setNormal(normal);
                tsite = new FixedNormalSite(tsite);
                String name = port.getName();
                if (name != null && !name.equals("")
                        && !(port instanceof ParameterPort)) {
                    figure = new NameWrapper(figure, port.getName());
                }
                figure = new TerminalFigure(figure, tsite)  {
                        // Override this because the tooltip may
                        // change over time.  I.e., the port may
                        // change from being an input or output, etc.
                        public String getToolTipText() {
                            String tipText = port.getName();
                            if (port instanceof IOPort) {
                                IOPort ioport = (IOPort)port;
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
                                    tipText = tipText + ", type:"
                                        + ((Typeable)port).getType();
                                } catch (IllegalActionException ex) {}
                            }
                            return tipText;
                        }
                    };
                // Have to do this as well or awt will not render a tooltip.
                figure.setToolTipText(port.getName());
            } else {
                polygon.moveTo(0, 0);
                polygon.lineTo(0, 10);
                polygon.lineTo(12, 0);
                polygon.lineTo(0, -10);
                polygon.closePath();

                figure = new BasicFigure(polygon, Color.black);
                figure.setToolTipText("Unknown port");
            }
            return figure;
        }
    }

    // Class for the name of the port.
    private class NameWrapper extends AbstractFigure {
        /** The child
         */
        private Figure _child = null;

        /** The label
         */
        private LabelFigure _label = null;

        /** The label anchor
         */
        private int _anchor = SwingConstants.SOUTH_WEST;

        /** Construct a new figure with the given child figure and
         * the given string.
         */
        public NameWrapper (Figure f, String label) {
            _child = f;
            f.setParent(this);

            _label = new LabelFigure(label, _labelFont);

            _label.setPadding(0);
            _label.setAnchor(_anchor);
            Rectangle2D bounds = _child.getBounds();
            _label.translateTo(bounds.getX(), bounds.getY());
        }

        /** Get the bounds of this figure.
         */
        public Rectangle2D getBounds () {
            Rectangle2D bounds = _child.getBounds();
            Rectangle2D.union(bounds, _label.getBounds(), bounds);
            return bounds;
        }

        /** Get the child figure
         */
        public Figure getChild () {
            return _child;
        }

        /** Get the label. This can be used to adjust the label
         * appearance, anchor, and so on.
         */
        public LabelFigure getLabel () {
            return _label;
        }

        /** Return the origin of the child figure in the enclosing
         *  transform context.
         *  @return The origin of the background figure.
         */
        public Point2D getOrigin () {
            if ( _child != null ) {
                return _child.getOrigin();
            } else {
                return super.getOrigin();
            }
        }

        /** Get the shape of this figure. This is the shape
         * of the child figure only -- the label is not included
         * in the shape.
         */
        public Shape getShape () {
            return _child.getShape();
        }

        /** We are hit if either the child or the figure is hit.
         */
        public boolean hit (Rectangle2D r) {
            return _child.hit(r);
        }

        /** Paint this figure
         */
        public void paint (Graphics2D g) {
            if (_child != null && isVisible()) {
                _child.paint(g);
                _label.paint(g);
            }
        }

        /** Transform the figure with the supplied transform.
         */
        public void transform (AffineTransform at) {
            repaint();
            _child.transform(at);
            Rectangle2D bounds = _child.getBounds();
            _label.translateTo(bounds.getX(), bounds.getY());
            repaint();
        }
    }
}


