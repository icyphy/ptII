/* Base class for graph controllers for objects that can have icons.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.vergil.basic;

import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.Action;

import ptolemy.actor.IOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.actor.ExternalIOPortController;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.toolbox.EditIconAction;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.RemoveIconAction;
import ptolemy.vergil.toolbox.SnapConstraint;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.graph.GraphException;
import diva.graph.GraphPane;
import diva.graph.NodeRenderer;
import diva.gui.GUIUtilities;
import diva.gui.toolbox.FigureIcon;

///////////////////////////////////////////////////////////////////
//// WithIconGraphController

/**
 A base class for Ptolemy II graph controllers for objects that can have
 icons. This adds to the base class the context menu items "Edit Custom Icon"
 and "Remove Custom Icon".  This also adds a port controller.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public abstract class WithIconGraphController extends BasicGraphController {
    /** Create a new controller.
     */
    public WithIconGraphController() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get a location for a port that hasn't got a location yet.
     * @param pane The GraphPane.
     * @param frame The BasicGraphFrame.
     * @param _prototype The port.
     * @return The location.
     */
    static public double[] getNewPortLocation(GraphPane pane,
            BasicGraphFrame frame, IOPort _prototype) {
        Point2D center = frame.getCenter();

        // If we are zoomed in, then place the ports in the canvas
        // view, not way off yonder.
        //Rectangle2D visiblePart = frame.getVisibleRectangle();
        BasicGraphFrame basicGraphFrame = frame;
        Rectangle2D visiblePart = basicGraphFrame.getVisibleCanvasRectangle();

        double[] p;
        if (_prototype.isInput() && _prototype.isOutput()) {
            p = _offsetFigure(
                    center.getX(),
                    visiblePart.getY() + visiblePart.getHeight() - _PORT_OFFSET,
                    FigureAction.PASTE_OFFSET * 2, 0, pane, frame);
        } else if (_prototype.isInput()) {
            p = _offsetFigure(visiblePart.getX() + _PORT_OFFSET, center.getY(),
                    0, FigureAction.PASTE_OFFSET * 2, pane, frame);
        } else if (_prototype.isOutput()) {
            p = _offsetFigure(visiblePart.getX() + visiblePart.getWidth()
                    - _PORT_OFFSET, center.getY(), 0,
                    FigureAction.PASTE_OFFSET * 2, pane, frame);
        } else {
            p = _offsetFigure(center.getX(), center.getY(),
                    FigureAction.PASTE_OFFSET * 2,
                    FigureAction.PASTE_OFFSET * 2, pane, frame);
        }
        return p;
    }

    /** Set the configuration.  This is used by some of the controllers
     *  when opening files or URLs.
     *  @param configuration The configuration.
     */
    @Override
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);
        _portController.setConfiguration(configuration);
        _editIconAction.setConfiguration(configuration);
        _removeIconAction.setConfiguration(configuration);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the controllers for nodes in this graph.
     *  In this base class, a port controller with PARTIAL access is created.
     *  This is called by the constructor, so derived classes that
     *  override this must be careful not to reference local variables
     *  defined in the derived classes, because the derived classes
     *  will not have been fully constructed by the time this is called.
     */
    @Override
    protected void _createControllers() {
        super._createControllers();
        _portController = new ExternalIOPortController(this,
                AttributeController.PARTIAL);
    }

    // NOTE: The following method name does not have a leading underscore
    // because it is a diva method.

    /** Initialize all interaction on the graph pane. This method
     *  is called by the setGraphPane() method of the superclass.
     *  This initialization cannot be done in the constructor because
     *  the controller does not yet have a reference to its pane
     *  at that time.  Regrettably, the canvas is not yet associated
     *  with the GraphPane, so you can't do any initialization that
     *  involves the canvas.
     */
    @Override
    protected void initializeInteraction() {
        super.initializeInteraction();

        //GraphPane pane = getGraphPane();
        _menuFactory.addMenuItemFactory(new MenuActionFactory(_editIconAction));
        _menuFactory
        .addMenuItemFactory(new MenuActionFactory(_removeIconAction));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Offset a figure if another figure is already at that location.
     *  @param x The x value of the proposed location.
     *  @param y The y value of the proposed location.
     *  @param xOffset The x offset to be used if a figure is found.
     *  @param yOffset The x offset to be used if a figure is found.
     *  @param pane The GraphPane.
     *  @param frame The BasicGraphFrame.
     *  @return An array of two doubles (x and y) that represents either
     *  the original location or an offset location that does not obscure
     *  an object of class <i>figure</i>.
     */
    static private double[] _offsetFigure(double x, double y, double xOffset,
            double yOffset, GraphPane pane, BasicGraphFrame frame) {
        FigureLayer foregroundLayer = pane.getForegroundLayer();

        Rectangle2D visibleRectangle;
        if (frame != null) {
            visibleRectangle = frame.getVisibleRectangle();
        } else {
            visibleRectangle = pane.getCanvas().getVisibleSize();
        }
        double[] point = FigureAction.offsetFigure(x, y, xOffset, yOffset,
                diva.canvas.connector.TerminalFigure.class, foregroundLayer,
                visibleRectangle);
        return point;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The edit custom icon action. */
    protected static final EditIconAction _editIconAction = new EditIconAction();

    /** The port controller. */
    protected NamedObjController _portController;

    /** The remove custom icon action. */
    protected static final RemoveIconAction _removeIconAction = new RemoveIconAction();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Offset of ports from the visible border. */
    private static double _PORT_OFFSET = 20.0;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// NewPortAction

    /** An action to create a new port. */
    @SuppressWarnings("serial")
    public class NewPortAction extends FigureAction {
        /** Create a new port that has the same input, output, and
         *  multiport properties as the specified port.  If the specified
         *  port is null, then a new port that is neither an input, an
         *  output, nor a multiport will be created.
         *  @param prototype Prototype port.
         *  @param description The description used for menu entries and
         *   tooltips.
         *  @param mnemonicKey The KeyEvent field for the mnemonic key to
         *   use in the menu.
         */
        public NewPortAction(IOPort prototype, String description,
                int mnemonicKey) {
            // null as the fourth arg means get the figure from the
            // _portController
            this(prototype, description, mnemonicKey, null);
        }

        /** Create a new port that has the same input, output, and
         *  multiport properties as the specified port and has icons
         *  associated with being unselected, rollover, rollover
         *  selected, and selected.  If the specified port is null,
         *  then a new port that is neither an input, an output, nor a
         *  multiport will be created.
         *
         *  @param prototype Prototype port.
         *  @param description The description used for menu entries and
         *   tooltips.
         *  @param mnemonicKey The KeyEvent field for the mnemonic key to
         *   use in the menu.
         *  @param iconRoles A matrix of Strings, where each element
         *  consists of two Strings, the absolute URL of the icon
         *  and the key that represents the role of the icon.  The keys
         *  are usually static fields from this class, such as
         *  {@link diva.gui.GUIUtilities#LARGE_ICON},
         *  {@link diva.gui.GUIUtilities#ROLLOVER_ICON},
         *  {@link diva.gui.GUIUtilities#ROLLOVER_SELECTED_ICON} or
         *  {@link diva.gui.GUIUtilities#SELECTED_ICON}.
         *  If this parameter is null, then the icon comes from
         *  the calling getNodeRenderer() on the {@link #_portController}.
         *  @see diva.gui.GUIUtilities#addIcons(Action, String[][])
         */
        public NewPortAction(IOPort prototype, String description,
                int mnemonicKey, String[][] iconRoles) {
            super(description);
            _prototype = prototype;

            if (iconRoles != null) {
                GUIUtilities.addIcons(this, iconRoles);
            } else {
                // Creating the renderers this way is rather nasty..
                // Standard toolbar icons are 25x25 pixels.
                NodeRenderer renderer = _portController.getNodeRenderer();

                Object location = null;

                if (_prototype != null) {
                    location = _prototype.getAttribute("_location");
                }

                Figure figure = renderer.render(location);

                FigureIcon icon = new FigureIcon(figure, 25, 25, 1, true);
                putValue(GUIUtilities.LARGE_ICON, icon);
            }
            putValue("tooltip", description);
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(mnemonicKey));
        }

        /** Create a new port. */
        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            double x;
            double y;

            if (getSourceType() == TOOLBAR_TYPE
                    || getSourceType() == MENUBAR_TYPE) {
                // No location in the action, so put it in the middle.
                BasicGraphFrame frame = WithIconGraphController.this.getFrame();
                GraphPane pane = getGraphPane();

                if (frame != null) {
                    if (_prototype != null) {
                        // Put in the middle of the visible part.
                        double[] p = getNewPortLocation(pane, frame, _prototype);
                        x = p[0];
                        y = p[1];

                    } else {
                        // Put in the middle of the visible part.
                        Point2D center = frame.getCenter();

                        x = center.getX();
                        y = center.getY();
                    }
                } else {
                    // Put in the middle of the pane.
                    Point2D center = pane.getSize();
                    x = center.getX() / 2;
                    y = center.getY() / 2;
                }
            } else {
                // Transform
                AffineTransform current = getGraphPane().getTransformContext()
                        .getTransform();
                AffineTransform inverse;

                try {
                    inverse = current.createInverse();
                } catch (NoninvertibleTransformException ex) {
                    throw new RuntimeException(ex.toString());
                }

                Point2D point = new Point2D.Double(getX(), getY());

                inverse.transform(point, point);
                x = point.getX();
                y = point.getY();
            }

            AbstractBasicGraphModel graphModel = (AbstractBasicGraphModel) getGraphModel();
            final double[] point = SnapConstraint.constrainPoint(x, y);
            final NamedObj toplevel = graphModel.getPtolemyModel();

            if (!(toplevel instanceof Entity)) {
                throw new InternalErrorException(
                        "Cannot invoke NewPortAction on an object "
                                + "that is not an instance of Entity.");
            }
            
            String name = "port";
            if (_prototype != null) {
                if (_prototype.isInput() && !_prototype.isOutput()) {
                    name = "in";
                }
                if (!_prototype.isInput() && _prototype.isOutput()) {
                    name = "out";
                }
            }

            final String portName = toplevel.uniqueName(name);
            final String locationName = "_location";

            // Create the port.
            StringBuffer moml = new StringBuffer();
            moml.append("<port name=\"" + portName + "\">\n");
            moml.append("<property name=\"" + locationName
                    + "\" class=\"ptolemy.kernel.util.Location\"/>\n");

            if (_prototype != null) {
                if (_prototype.isInput()) {
                    moml.append("<property name=\"input\"/>");
                }

                if (_prototype.isOutput()) {
                    moml.append("<property name=\"output\"/>");
                }

                if (_prototype.isMultiport()) {
                    // Set the width of the multiport to -1 so that the width is inferred.
                    // See ptolemy/actor/lib/test/auto/VectorDisassemblerComposite.xml
                    moml.append("<property name=\"width\" class=\"ptolemy.data.expr.Parameter\" value=\"-1\"/>");
                    moml.append("<property name=\"multiport\"/>");
                }
            }

            moml.append("</port>");

            MoMLChangeRequest request = new MoMLChangeRequest(this, toplevel,
                    moml.toString()) {
                @Override
                protected void _execute() throws Exception {
                    super._execute();

                    // Set the location of the icon.
                    // Note that this really needs to be done after
                    // the change request has succeeded, which is why
                    // it is done here.  When the graph controller
                    // gets around to handling this, it will draw
                    // the icon at this location.
                    // NOTE: The cast is safe because it is checked
                    // above, and presumably a reasonable GUI would
                    // provide no mechanism for creating a port on
                    // something that is not an entity.
                    NamedObj newObject = ((Entity) toplevel).getPort(portName);
                    Location location = (Location) newObject
                            .getAttribute(locationName);
                    location.setLocation(point);
                }
            };

            request.setUndoable(true);
            toplevel.requestChange(request);

            try {
                request.waitForCompletion();
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new GraphException(ex);
            }
        }

        private IOPort _prototype;

        /** Offset a figure if another figure is already at that location.
         *  @param x The x value of the proposed location.
         *  @param y The y value of the proposed location.
         *  @param xOffset The x offset to be used if a figure is found.
         *  @param yOffset The x offset to be used if a figure is found.
         *  @return An array of two doubles (x and y) that represents either
         *  the original location or an offset location that does not obscure
         *  an object of class <i>figure</i>.
         */
        protected double[] _offsetFigure(double x, double y, double xOffset,
                double yOffset) {

            double[] point = WithIconGraphController._offsetFigure(x, y,
                    xOffset, yOffset, getGraphPane(),
                    WithIconGraphController.this.getFrame());
            return point;
        }
    }
}
