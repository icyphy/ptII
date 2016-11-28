/* An action that is associated with a figure.

 Copyright (c) 2000-2016 The Regents of the University of California.
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
package ptolemy.vergil.toolbox;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import diva.canvas.CanvasComponent;
import diva.canvas.CanvasLayer;
import diva.canvas.CanvasPane;
import diva.canvas.CanvasUtilities;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.BasicGrabHandle;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.gui.toolbox.JContextMenu;
import diva.util.UserObjectContainer;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// FigureAction

/**
 An action that is attached to a figure on a named object.
 Such an action is fired in one of several ways.
 The first way is through an ActionInteractor that is attached
 to the figure.  The second way is through a context menu that is created
 on the figure.  A third way is through a hotkey.
 Unfortunately, the source of the event is different in
 these cases.  This class makes it easier to write an action that is
 triggered by any mechanism. Such an action would be derived from this
 class, and would invoke super.actionPerformed() first in its own
 actionPerformed() method.

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
@SuppressWarnings("serial")
public class FigureAction extends AbstractAction {
    /** Construct an action that is attached to a figure on a named object.
     *  @param name The name of the object.
     */
    public FigureAction(String name) {
        super(name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Determine the target Ptolemy II object, the originating frame,
     *  and the X, Y position of the action, if possible.  After this
     *  is invoked, the other public methods can be used to access
     *  this data.
     *  @param e The event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        Component parent = null;

        if (source instanceof LayerEvent) {
            _sourceType = CANVAS_TYPE;

            // Action activated using an ActionInteractor.
            LayerEvent event = (LayerEvent) source;
            CanvasLayer layer = event.getLayerSource();
            GraphPane pane = (GraphPane) layer.getCanvasPane();
            GraphController controller = pane.getGraphController();
            GraphModel model = controller.getGraphModel();

            _figure = event.getFigureSource();

            // Set the target.
            if (_figure == null) {
                _target = (NamedObj) model.getRoot();
            } else {
                Object object = _figure.getUserObject();
                _target = (NamedObj) model.getSemanticObject(object);
            }

            // Set the position.
            _x = event.getX();
            _y = event.getY();

            // Set the parent.
            CanvasPane canvasPane = layer.getCanvasPane();
            parent = canvasPane.getCanvas();
        } else if (source instanceof JMenuItem) {
            // Action activated using a context menu or submenu.
            JMenuItem item = (JMenuItem) source;
            // Find the original context menu.
            Component contextMenu = item.getParent();
            if (!(contextMenu instanceof JContextMenu)) {
                // Probably a submenu.
                // FIXME: This only supports one level of submenus.
                if (contextMenu instanceof JPopupMenu) {
                    contextMenu = ((JPopupMenu) contextMenu).getInvoker();
                }
                if (contextMenu instanceof JMenu) {
                    contextMenu = contextMenu.getParent();
                }
            }
            if (contextMenu instanceof JContextMenu) {
                _sourceType = CONTEXTMENU_TYPE;
                JContextMenu menu = (JContextMenu) contextMenu;
                parent = menu.getInvoker();
                _target = (NamedObj) menu.getTarget();
                _x = item.getX();
                _y = item.getY();
            } else {
                // Not implicit location.. should there be?
                _sourceType = MENUBAR_TYPE;
            }
            /*
             } else if (source instanceof JMenuItem) {
             // Action activated using a context menu.
             JMenuItem item = (JMenuItem) source;

             if (item.getParent() instanceof JContextMenu) {
             _sourceType = CONTEXTMENU_TYPE;

             JContextMenu menu = (JContextMenu) item.getParent();
             parent = menu.getInvoker();
             _target = (NamedObj) menu.getTarget();
             _x = item.getX();
             _y = item.getY();
             } else {
             // Not implicit location.. should there be?
             _sourceType = MENUBAR_TYPE;
             }
             */
        } else if (source instanceof JButton) {
            // presumably we are in a toolbar...
            _sourceType = TOOLBAR_TYPE;
            _target = null;
            parent = ((Component) source).getParent();
        } else if (source instanceof JGraph) {
            // This is an absurdly convoluted way to get the info we need.
            // But there seems to be no other way.
            // This is an architectural flaw in vergil.
            GraphPane pane = ((JGraph) source).getGraphPane();
            FigureLayer layer = pane.getForegroundLayer();
            CanvasComponent currentFigure = layer.getCurrentFigure();
            GraphController controller = pane.getGraphController();
            GraphModel model = controller.getGraphModel();

            if (currentFigure != null) {
                _target = null;

                while (_target == null && currentFigure != null) {
                    Object object = currentFigure;

                    if (object instanceof Figure) {
                        object = ((Figure) currentFigure).getUserObject();
                    }

                    _target = (NamedObj) model.getSemanticObject(object);
                    currentFigure = currentFigure.getParent();
                }

                // NOTE: _target may end up null here!
                if (_target == null) {
                    // On 5/29/09, Edward wrote:
                    // "If you select a transition in an FSM, put the
                    // mouse on the blue box that is the grab handle,
                    // and hit Command-L (to look inside), you get an
                    // ugly exception on the command line.
                    // If you put the mouse on the transition but not
                    // on the blue box, you correctly get a message
                    // that there is no refinement."
                    //
                    // So, if the current figure is a BasicGrabHandle, we
                    // do not throw the exception
                    if (!(layer.getCurrentFigure() instanceof BasicGrabHandle)) {
                        throw new InternalErrorException(
                                "Internal error: FigureLayer \""
                                        + layer.getCurrentFigure()
                                        + "\" has no associated Ptolemy II object!");
                    }
                }
            } else {
                _target = (NamedObj) model.getRoot();
            }

            _sourceType = HOTKEY_TYPE;

            // FIXME: set _x and _y.  How to do this?
            _x = 0;
            _y = 0;

            // Set the parent.
            CanvasPane canvasPane = layer.getCanvasPane();
            parent = canvasPane.getCanvas();
        } else {
            _sourceType = null;
            _target = null;
            parent = null;
            _x = 0;
            _y = 0;
        }

        if (parent != null) {
            while (parent.getParent() != null) {
                parent = parent.getParent();
            }
        }

        if (parent instanceof Frame) {
            _frame = new WeakReference(parent);
        } else {
            _frame = null;
        }
    }

    /** Return the figure of this action.
     *  @return The figure of this action.
     */
    public Figure getFigure() {
        return _figure;
    }

    // FIXME: The following methods should all be protected.

    /** Return the frame responsible for triggering this action,
     *  or null if none could be found.  This can be used to set the
     *  owner of any dialogs triggered by this event.  This must
     *  be called after actionPerformed(), and is typically called
     *  inside the actionPerformed() method of a subclass.
     *  @return The frame that triggered this action.
     */
    public Frame getFrame() {
        return (Frame) _frame.get();
    }

    /** Return the source type of this action, which is one of
     *  CANVAS_TYPE, CONTEXTMENU_TYPE, TOOLBAR_TYPE, MENUBAR_TYPE,
     *  HOTKEY_TYPE, or null if none was recognized.
     *  @return The source type of this action.
     */
    public SourceType getSourceType() {
        return _sourceType;
    }

    /** Return the target Ptolemy II object for this action,
     *  or null if none could be found.  This is typically the object
     *  whose icon is the figure on which this action was invoked.
     *  This must be called after actionPerformed(), and is typically called
     *  inside the actionPerformed() method of a subclass.
     *  @return The object on which this action was invoked.
     */
    public NamedObj getTarget() {
        return _target;
    }

    /** Return the horizontal position of the action, or 0 if this
     *  is not relevant (e.g., the action was triggered by a toolbar button).
     *  This must be called after actionPerformed(), and is typically called
     *  inside the actionPerformed() method of a subclass.
     *  @return The x position of the action.
     */
    public int getX() {
        return _x;
    }

    /** Return the vertical position of the action, or 0 if this
     *  is not relevant (e.g., the action was triggered by a toolbar button).
     *  This must be called after actionPerformed(), and is typically called
     *  inside the actionPerformed() method of a subclass.
     *  @return The y position of the action.
     */
    public int getY() {
        return _y;
    }

    /** Determine a new location for a figure if another figure is
     *  already at that location.
     *  @param x The x value of the proposed location.
     *  @param y The y value of the proposed location.
     *  @param xOffset The x offset to be used if a figure is found.
     *  @param yOffset The y offset to be used if a figure is found.
     *  @param figureClass The Class of the figure to avoid.
     *  @param foregroundLayer The layer containing the figures.
     *  @param visibleRectangle The rectangle that describe the bounds
     *  of the visible pane.
     *  @return An array of two doubles (x and y) that represents either
     *  the original location or an offset location that does not obscure
     *  an object of class <i>figure</i>.
     */
    static public double[] offsetFigure(double x, double y, double xOffset,
            double yOffset, Class<?> figureClass, FigureLayer foregroundLayer,
            Rectangle2D visibleRectangle) {
        // Solve the problem of items from the toolbar overlapping.
        // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3002

        // This method is in this class so that we can handle
        // ports and relations.

        double[] point = new double[2];
        point[0] = x;
        point[1] = y;

        double originalX = x;
        double originalY = y;

        // See EditorDropTarget for similar code.

        double halo = foregroundLayer.getPickHalo();
        double width = halo * 2;

        // Used to handle cases where we get to the edge.
        int xMax = 0;
        int yMax = 0;

        // Set to true if we need to check for a Figure at x and y
        boolean checkFigure = false;
        do {
            // If we are looping again, we set checkFigure to false
            // until we later possibly find a Figure.
            checkFigure = false;

            // The rectangle in which we search for a Figure.
            Rectangle2D region = new Rectangle2D.Double(point[0] - halo,
                    point[1] - halo, width, width);

            // Iterate through figures within the region.
            Iterator<?> foregroundFigures = foregroundLayer.getFigures()
                    .getIntersectedFigures(region).figuresFromFront();
            Iterator<?> pickFigures = CanvasUtilities.pickIter(
                    foregroundFigures, region);

            while (pickFigures.hasNext() && !checkFigure) {
                CanvasComponent possibleFigure = (CanvasComponent) pickFigures
                        .next();
                if (possibleFigure == null) {
                    // Nothing to see here, move along - there is no Figure.
                    break;
                } else if (possibleFigure instanceof UserObjectContainer) {
                    // Work our way up the CanvasComponent parent tree
                    // See EditorDropTarget for similar code.
                    Object userObject = null;

                    while (possibleFigure instanceof UserObjectContainer
                            && userObject == null && !checkFigure) {
                        userObject = ((UserObjectContainer) possibleFigure)
                                .getUserObject();
                        if (userObject instanceof Location
                                && (figureClass.isInstance(userObject) || figureClass
                                        .isInstance(possibleFigure))) {
                            // We found a figure here, so we will
                            // loop again.
                            checkFigure = true;
                            point[0] += xOffset;
                            point[1] += yOffset;

                            // Check to make sure we are not outside the view
                            if (point[0] > visibleRectangle.getWidth()) {
                                point[0] = originalX;
                                point[1] = originalY - PASTE_OFFSET * 2
                                        * ++xMax;
                                if (point[1] < 0) {
                                    point[1] = originalY + PASTE_OFFSET * 2
                                            * ++xMax;
                                }
                            }

                            if (point[1] > visibleRectangle.getHeight()) {
                                point[0] = originalX - PASTE_OFFSET * 2
                                        * ++yMax;
                                if (point[0] < 0) {
                                    point[0] = originalX + PASTE_OFFSET * 2
                                            * ++xMax;
                                }
                                point[1] = originalY;

                            }

                            // Fail safe. Don't try forever, just give up.
                            if (point[0] < 0 || point[1] < 0
                                    || point[0] > visibleRectangle.getWidth()
                                    || point[1] > visibleRectangle.getHeight()) {
                                // Can't do anything here, so return.
                                point[0] = originalX + 0.5 * xOffset;
                                point[1] = originalY + 0.5 * yOffset;
                                return point;
                            }
                        }
                        possibleFigure = possibleFigure.getParent();
                    }
                }
            }
        } while (checkFigure);
        return point;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** When the action was fired from a canvas interactor.
     */
    public static final SourceType CANVAS_TYPE = new SourceType("canvas");

    /** When the action was fired from a context menu.
     */
    public static final SourceType CONTEXTMENU_TYPE = new SourceType(
            "contextmenu");

    /** When the action was fired from a hotkey.
     */
    public static final SourceType HOTKEY_TYPE = new SourceType("hotkey");

    /** When the action was fired from a menubar.
     */
    public static final SourceType MENUBAR_TYPE = new SourceType("menubar");

    /** Offset used when pasting objects. See also OffsetMoMLChangeRequest. */
    public static final int PASTE_OFFSET = 20;

    /** When the action was fired from a toolbar icon.
     */
    public static final SourceType TOOLBAR_TYPE = new SourceType("toolbar");

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The source of the action. */
    public static class SourceType {
        /** Construct a SourceType.
         *  @param name The name of the SourceType.
         */
        private SourceType(String name) {
            _name = name;
        }

        /** Get the name of the SourceType.
         *  @return the name.
         */
        public String getName() {
            return _name;
        }

        private String _name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Figure _figure = null;

    private WeakReference _frame = null;

    private SourceType _sourceType = null;

    private NamedObj _target = null;

    private int _x = 0;

    private int _y = 0;
}
