/* An action that is associated with a figure.

 Copyright (c) 2000-2003 The Regents of the University of California.
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

package ptolemy.vergil.toolbox;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenuItem;

import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import diva.canvas.CanvasComponent;
import diva.canvas.CanvasLayer;
import diva.canvas.CanvasPane;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.event.LayerEvent;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.gui.toolbox.JContextMenu;

//////////////////////////////////////////////////////////////////////////
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
*/
public class FigureAction extends AbstractAction {

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
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        Component parent = null;
        if (source instanceof LayerEvent) {
            _sourceType = CANVAS_TYPE;
            // Action activated using an ActionInteractor.
            LayerEvent event = (LayerEvent) source;
            CanvasLayer layer = event.getLayerSource();
            GraphPane pane = (GraphPane)layer.getCanvasPane();
            GraphController controller = pane.getGraphController();
            GraphModel model = controller.getGraphModel();

            Figure figure = (Figure) event.getFigureSource();
            // Set the target.
            if (figure == null) {
                _target = (NamedObj) model.getRoot();
            } else {
                Object object = figure.getUserObject();
                _target = (NamedObj) model.getSemanticObject(object);
            }

            // Set the position.
            _x = event.getX();
            _y = event.getY();

            // Set the parent.
            CanvasPane canvasPane = layer.getCanvasPane();
            parent = canvasPane.getCanvas();

        } else if (source instanceof JMenuItem) {
            // Action activated using a context menu.
            JMenuItem item = (JMenuItem) source;
            if (item.getParent() instanceof JContextMenu) {
                _sourceType = CONTEXTMENU_TYPE;
                JContextMenu menu = (JContextMenu)item.getParent();
                parent = menu.getInvoker();
                _target = (NamedObj) menu.getTarget();
                _x = item.getX();
                _y = item.getY();
            } else {
                // Not implicit location.. should there be?
                _sourceType = MENUBAR_TYPE;
            }
        } else if (source instanceof JButton) {
            // presumably we are in a toolbar...
            _sourceType = TOOLBAR_TYPE;
            _target = null;
            parent = ((Component)source).getParent();
        } else if (source instanceof JGraph) {
            // This is an absurdly convoluted way to get the info we need.
            // But there seems to be no other way.
            // This is an architectural flaw in vergil.
            GraphPane pane = (GraphPane)((JGraph)source).getGraphPane();
            FigureLayer layer = pane.getForegroundLayer();
            CanvasComponent currentFigure = layer.getCurrentFigure();
            GraphController controller = pane.getGraphController();
            GraphModel model = controller.getGraphModel();
            if (currentFigure != null) {
                _target = null;
                while (_target == null && currentFigure != null) {
                    Object object = currentFigure;
                    if (object instanceof Figure) {
                        object = ((Figure)currentFigure).getUserObject();
                    }
                    _target = (NamedObj) model.getSemanticObject(object);
                    currentFigure = currentFigure.getParent();
                }
                // NOTE: _target may end up null here!
                if (_target == null) {
                    throw new InternalErrorException(
                    "Internal error: Figure has no associated Ptolemy II object!");
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
            _frame = (Frame)parent;
        } else {
            _frame = null;
        }
    }

    // FIXME: The following methods should all be protected.

    /** Return the source type of this action, which is one of
     *  CANVAS_TYPE, CONTEXTMENU_TYPE, TOOLBAR_TYPE, MENUBAR_TYPE,
     *  HOTKEY_TYPE, or null if none was recognized.
     *  @return The source type of this action.
     */
    public SourceType getSourceType() {
        return _sourceType;
    }

    /** Return the frame responsible for triggering this action,
     *  or null if none could be found.  This can be used to set the
     *  owner of any dialogs triggered by this event.  This must
     *  be called after actionPerformed(), and is typically called
     *  inside the actionPerformed() method of a subclass.
     *  @return The frame that triggered this action.
     */
    public Frame getFrame() {
        return _frame;
    }

    /** Return the target Ptolemy II object for this action,
     *  or null if none could be found.  This is typically the object
     *  whose icon is the figure on which this action was invoked.
     *  This must be called after actionPerformed(), and is typically called
     *  inside the actionPerformed() method of a subclass.
     *  @return The frame that triggered this action.
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** When the action was fired from a canvas interactor.
     */
    public static SourceType CANVAS_TYPE = new SourceType("canvas");

    /** When the action was fired from a context menu.
     */
    public static SourceType CONTEXTMENU_TYPE = new SourceType("contextmenu");

    /** When the action was fired from a toolbar icon.
     */
    public static SourceType TOOLBAR_TYPE = new SourceType("toolbar");

    /** When the action was fired from a menubar.
     */
    public static SourceType MENUBAR_TYPE = new SourceType("menubar");

    /** When the action was fired from a hotkey.
     */
    public static SourceType HOTKEY_TYPE = new SourceType("hotkey");

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    public static class SourceType {
        private SourceType(String name) {
            _name = name;
        }

        public String getName() {
            return _name;
        }
        private String _name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Frame _frame = null;
    private SourceType _sourceType = null;
    private NamedObj _target = null;
    private int _x = 0;
    private int _y = 0;
}
