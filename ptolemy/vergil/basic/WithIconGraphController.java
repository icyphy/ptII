/* Base class for graph controllers for objects that can have icons.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

package ptolemy.vergil.basic;

import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

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
import diva.graph.GraphException;
import diva.graph.GraphPane;
import diva.graph.NodeRenderer;
import diva.gui.GUIUtilities;
import diva.gui.toolbox.FigureIcon;


//////////////////////////////////////////////////////////////////////////
//// WithIconGraphController
/**
A base class for Ptolemy II graph controllers for objects that can have
icons. This adds to the base class the context menu items "Edit Custom Icon"
and "Remove Custom Icon".  This also adds a port controller.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public abstract class WithIconGraphController extends BasicGraphController {
    
    /** Create a new controller.
     */
    public WithIconGraphController() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the configuration.  This is used by some of the controllers
     *  when opening files or URLs.
     *  @param configuration The configuration.
     */
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
    protected void initializeInteraction() {
        super.initializeInteraction();
        GraphPane pane = getGraphPane();
        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(_editIconAction));
        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(_removeIconAction));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
            
    /** The edit custom icon action. */
    protected static EditIconAction _editIconAction
            = new EditIconAction();

    /** The port controller. */
    protected NamedObjController _portController;

    /** The remove custom icon action. */
    protected static RemoveIconAction _removeIconAction
            = new RemoveIconAction();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Offset of ports from the visible border. */
    private static double _PORT_OFFSET = 20.0;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// NewPortAction

    /** An action to create a new port. */
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
        public NewPortAction(
                IOPort prototype, String description, int mnemonicKey) {
            super(description);
            _prototype = prototype;
            String dflt = "";
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

            putValue("tooltip", description);
            putValue(GUIUtilities.MNEMONIC_KEY,
                    new Integer(mnemonicKey));
        }

        /** Create a new port. */
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);
            double x;
            double y;
            if (getSourceType() == TOOLBAR_TYPE ||
                    getSourceType() == MENUBAR_TYPE) {
                // No location in the action, so put it in the middle.
                BasicGraphFrame frame = WithIconGraphController.this.getFrame();
                if (frame != null) {
                    // Put in the middle of the visible part.
                    Point2D center = frame.getCenter();
                    if (_prototype != null) {
                        Rectangle2D visiblePart = frame.getVisibleRectangle();
                        if (_prototype.isInput() && _prototype.isOutput()) {
                            x = center.getX();
                            y = visiblePart.getY()
                                + visiblePart.getHeight() - _PORT_OFFSET;
                        } else if (_prototype.isInput()) {
                            x = visiblePart.getX() + _PORT_OFFSET;
                            y = center.getY();
                        } else if (_prototype.isOutput()) {
                            x = visiblePart.getX()
                                + visiblePart.getWidth() - _PORT_OFFSET;
                            y = center.getY();
                        } else {
                            x = center.getX();
                            y = center.getY();
                        }
                    } else {
                        x = center.getX();
                        y = center.getY();
                    }
                } else {
                    // Put in the middle of the pane.
                    GraphPane pane = getGraphPane();
                    Point2D center = pane.getSize();
                    x = center.getX()/2;
                    y = center.getY()/2;
                }
            } else {
                // Transform
                AffineTransform current =
                    getGraphPane().getTransformContext().getTransform();
                AffineTransform inverse;
                try {
                    inverse = current.createInverse();
                }
                catch(NoninvertibleTransformException ex) {
                    throw new RuntimeException(ex.toString());
                }
                Point2D point = new Point2D.Double(getX(), getY());

                inverse.transform(point, point);
                x = point.getX();
                y = point.getY();
            }

            AbstractBasicGraphModel graphModel =
                (AbstractBasicGraphModel)getGraphModel();
            final double[] point = SnapConstraint.constrainPoint(x, y);
            final NamedObj toplevel = graphModel.getPtolemyModel();
            if (!(toplevel instanceof Entity)) {
                throw new InternalErrorException(
                "Cannot invoke NewPortAction on an object " +
                "that is not an instance of Entity.");
            }
            NamedObj container =
                MoMLChangeRequest.getDeferredToParent(toplevel);
            if (container == null) {
                container = toplevel;
            }

            final NamedObj context = container;
            final String portName = toplevel.uniqueName("port");
            final String locationName = "_location";
            // Create the port.
            StringBuffer moml = new StringBuffer();
            if (container != toplevel) {
                moml.append("<entity name=\"" +
                        toplevel.getName(container) + "\">\n");
            }
            moml.append("<port name=\"" + portName + "\">\n");
            moml.append("<property name=\"" + locationName +
                    "\" class=\"ptolemy.kernel.util.Location\"/>\n");
            if (_prototype != null) {
                if (_prototype.isInput()) {
                    moml.append("<property name=\"input\"/>");
                }
                if (_prototype.isOutput()) {
                    moml.append("<property name=\"output\"/>");
                }
                if (_prototype.isMultiport()) {
                    moml.append("<property name=\"multiport\"/>");
                }
            }
            moml.append("</port>");
            if (container != toplevel) {
                moml.append("</entity>");
            }

            MoMLChangeRequest request =
                new MoMLChangeRequest(this, container, moml.toString()) {
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
                            NamedObj newObject =
                                ((Entity)toplevel).getPort(portName);
                            Location location =
                                (Location) newObject.getAttribute(locationName);
                            location.setLocation(point);
                        }
                    };
            request.setUndoable(true);
            container.requestChange(request);
            try {
                request.waitForCompletion();
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new GraphException(ex);
            }
        }

        private IOPort _prototype;
    }
}
