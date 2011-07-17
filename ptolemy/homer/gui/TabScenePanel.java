/*
 TODO
 
 Copyright (c) 2011 The Regents of the University of California.
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
package ptolemy.homer.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.AlignWithMoveDecorator;
import org.netbeans.api.visual.action.EditProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.TwoStateHoverProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.action.AlignWithMoveStrategyProvider;
import org.netbeans.modules.visual.action.SingleLayerAlignWithWidgetCollector;

import ptolemy.actor.gui.PortablePlaceable;
import ptolemy.homer.kernel.ContentPrototype;
import ptolemy.homer.kernel.HomerWidgetElement;
import ptolemy.homer.kernel.PositionableElement;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.toolbox.PtolemyTransferable;

//////////////////////////////////////////////////////////////////////////
//// TabScenePanel

/** The tab scene onto which widgets can be dropped, resized, and
 *  arranged in order to suite the needs of the handheld consumer.
 *
 *  @author Anar Huseynov
 *  @version $Id$ 
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
public class TabScenePanel implements ContentPrototype {

    /** Create a new tab scene onto which widgets can be dropped.
     *  @param mainFrame The parent frame of the panel.
     */
    public TabScenePanel(HomerMainFrame mainFrame) {
        _mainFrame = mainFrame;
        _scene = new ObjectScene();
        _mainLayer = new LayerWidget(getContent());
        _interactionLayer = new LayerWidget(getContent());
        _scene.createView();
        _scene.setLayout(LayoutFactory.createOverlayLayout());
        _scene.getActions().addAction(_hoverAction);

        getContent().addChild(_interactionLayer);
        getContent().addChild(_mainLayer);

        final AlignWithMoveStrategyProvider alignWithMoveStrategyProvider = new AlignWithMoveStrategyProvider(
                new SingleLayerAlignWithWidgetCollector(_mainLayer, false),
                _interactionLayer, MOVE_ALIGN_DECORATOR, false);

        _moveAction = ActionFactory.createMoveAction(new MoveStrategy() {
            public Point locationSuggested(Widget widget,
                    Point originalLocation, Point suggestedLocation) {
                _adjustLocation(widget, suggestedLocation);

                Point locationSuggested = alignWithMoveStrategyProvider
                        .locationSuggested(widget, originalLocation,
                                suggestedLocation);

                return locationSuggested;
            }
        }, alignWithMoveStrategyProvider);

        new DropTarget(_scene.getView(), new DropTargetAdapter() {

            /** Accept the event if the data is a known key.
             *  This is called while a drag operation is ongoing,
             *  when the mouse pointer enters the operable part of
             *  the drop site for the DropTarget registered with
             *  this listener.
             *  @param dropEvent The drop event.
             */
            public void dragEnter(DropTargetDragEvent dropEvent) {
                try {
                    if (dropEvent
                            .isDataFlavorSupported(PtolemyTransferable.namedObjFlavor)) {
                        List<?> dropObjects = (java.util.List) dropEvent
                                .getTransferable().getTransferData(
                                        PtolemyTransferable.namedObjFlavor);

                        Object transferable = dropObjects.get(0);
                        if ((transferable instanceof PortablePlaceable)
                                || (transferable instanceof Settable)) {
                            if (_mainFrame.contains((NamedObj) transferable)) {
                                dropEvent.rejectDrag();
                            } else {
                                dropEvent
                                        .acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
                            }
                        } else {
                            dropEvent.rejectDrag();
                        }
                    } else {
                        dropEvent.rejectDrag();
                    }
                } catch (UnsupportedFlavorException e) {
                    MessageHandler.error(
                            "Can't find a supported data flavor for drop in "
                                    + dropEvent, e);
                    return;
                } catch (IOException e) {
                    MessageHandler.error(
                            "Can't find a supported data flavor for drop in "
                                    + dropEvent, e);
                    return;
                }
            }

            /** Perform the drop of the item onto the scene and 
             *  load the appropriate graphical widget.
             */
            public void drop(DropTargetDropEvent dropEvent) {

                if (dropEvent
                        .isDataFlavorSupported(PtolemyTransferable.namedObjFlavor)) {
                    try {
                        List<?> dropObjects = (java.util.List) dropEvent
                                .getTransferable().getTransferData(
                                        PtolemyTransferable.namedObjFlavor);
                        _mainFrame.addVisualNamedObject(TabScenePanel.this,
                                (NamedObj) dropObjects.get(0), null,
                                dropEvent.getLocation());
                    } catch (UnsupportedFlavorException e) {
                        MessageHandler.error(
                                "Can't find a supported data flavor for drop in "
                                        + dropEvent, e);
                        return;
                    } catch (IOException e) {
                        MessageHandler.error(
                                "Can't find a supported data flavor for drop in "
                                        + dropEvent, e);
                        return;
                    } catch (IllegalActionException e) {
                        MessageHandler.error(
                                "Can't initialize widget for the selected object "
                                        + dropEvent, e);
                        return;
                    } catch (NameDuplicationException e) {
                        MessageHandler.error(
                                "Can't initialize widget for the selected object "
                                        + dropEvent, e);
                        return;
                    }

                    dropEvent.acceptDrop(DnDConstants.ACTION_LINK);
                } else {
                    dropEvent.rejectDrop();
                }
            }
        });
    }

    /** Add an element's representation to the scene
     *  @param element The element to be added.
     *  @exception IllegalActionException If the appropriate element's representation
     *  cannot be loaded.
     */
    public void add(final PositionableElement element) throws IllegalActionException {
        if (!(element instanceof HomerWidgetElement)) {
            throw new IllegalActionException(element.getElement(),
                    "No representation is available.");
        }

        Widget widget = ((HomerWidgetElement) element).getWidget();
        Point location = new Point(element.getLocation().getX(), element
                .getLocation().getY());

        widget.setPreferredLocation(location);

        // Add widget resizing.
        widget.getActions().addAction(
                ActionFactory.createAlignWithResizeAction(_mainLayer,
                        _interactionLayer, null, false));

        // Add widget moving.
        widget.getActions().addAction(_hoverAction);

        // Add widget selection.
        widget.getActions().addAction(_scene.createSelectAction());

        // Add widget movement.
        widget.getActions().addAction(_moveAction);

        // Add widget double-click editing.
        widget.getActions().addAction(
                ActionFactory.createEditAction(new EditProvider() {
                    public void edit(Widget widget) {
                        _showWidgetProperties(element);
                    }
                }));

        // Add widget context menu.
        widget.getActions().addAction(
                ActionFactory.createPopupMenuAction(new PopupMenuProvider() {
                    public JPopupMenu getPopupMenu(Widget widget,
                            Point localLocation) {
                        return new NamedObjectPopupMenu(element);
                    }
                }));

        widget.setBorder(DEFAULT_BORDER);

        _mainLayer.addChild(widget);
        _scene.validate();
        _adjustLocation(widget, location);
        _scene.validate();
    }

    /** Get the view associated with the scene.
     *  @return The view component of the scene.
     */
    public Component getView() {
        return getContent().getView();
    }

    /** Get a new tab scene panel instance.
     *  @return A new TabScenePanel object.
     */
    public ContentPrototype getNewInstance() {
        return new TabScenePanel(_mainFrame);
    }

    public String getTag() {
        return _tag;
    }

    public void setTag(String tag) {
        _tag = tag;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    /** Get a reference to the current scene.
     *  @return The current scene.
     */
    public ObjectScene getContent() {
        return _scene;
    }

    /** Remove the widget from the scene.
     *  @param widget The widget to be removed.
     */
    public void remove(PositionableElement element) {
        _mainLayer.removeChild(((HomerWidgetElement) element).getWidget());
        _mainFrame.repaint();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Adjust the screen location of the selected widget.
     *  @param widget The widget to be moved.
     *  @param location The target location.
     */
    private void _adjustLocation(Widget widget, Point location) {
        if (location.getX() + widget.getBounds().getWidth() > getView()
                .getWidth()) {
            location.setLocation(getView().getWidth()
                    - widget.getBounds().getWidth(), location.getY());
        }
        if (location.getY() + widget.getBounds().getHeight() > getView()
                .getHeight()) {
            location.setLocation(location.getX(), getView().getHeight()
                    - widget.getBounds().getHeight());
        }
        if (location.getX() < 0) {
            location.setLocation(0, location.getY());
        }
        if (location.getY() < 0) {
            location.setLocation(location.getX(), 0);
        }
    }

    /** Display the widget properties window for modification.
     *  @param widget The target widget whose properties should be displayed.
     */
    private void _showWidgetProperties(PositionableElement element) {
        Widget widget = ((HomerWidgetElement) element).getWidget();
        WidgetPropertiesFrame dialog = new WidgetPropertiesFrame(widget);
        if (dialog.showPrompt() == JOptionPane.OK_OPTION) {
            try {
                widget.setPreferredBounds(dialog.getBounds());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(_mainFrame, new JLabel(ex
                        .getClass().getName()), "Invalid Size Specified",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The default border to apply to all scenes.
     */
    private static final Border DEFAULT_BORDER = BorderFactory
            .createEmptyBorder(6);

    private String _tag;
    private String _name;

    /** The default decorator for move alignment actions.
     */
    private static final AlignWithMoveDecorator MOVE_ALIGN_DECORATOR = new AlignWithMoveDecorator() {
        public ConnectionWidget createLineWidget(Scene scene) {
            ConnectionWidget widget = new ConnectionWidget(scene);
            widget.setStroke(STROKE);
            widget.setForeground(Color.BLUE);
            return widget;
        }
    };

    /** The default border for resize actions.
     */
    private static final Border RESIZE_BORDER = BorderFactory
            .createResizeBorder(6, Color.BLACK, true);

    /** Default stroke for use in widget connection.
     */
    private static final BasicStroke STROKE = new BasicStroke(1.0f,
            BasicStroke.JOIN_BEVEL, BasicStroke.CAP_BUTT, 5.0f, new float[] {
                    6.0f, 3.0f }, 0.0f);

    /** Hover action added to new widgets.
     */
    private final WidgetAction _hoverAction = ActionFactory
            .createHoverAction(new TwoStateHoverProvider() {
                public void unsetHovering(Widget widget) {
                    if (!widget.getState().isSelected()) {
                        widget.setBorder(DEFAULT_BORDER);
                    }
                }

                public void setHovering(Widget widget) {
                    if (!widget.getState().isSelected()) {
                        widget.setBorder(RESIZE_BORDER);
                    }
                }
            });

    /** Layer that user interacts with when dropping widgets.
     */
    private final LayerWidget _interactionLayer;

    /** Reference to the parent container frame.
     */
    private final HomerMainFrame _mainFrame;

    /** The main widget layer.
     */
    private final LayerWidget _mainLayer;

    /** The move action added to all new widgets.
     */
    private final WidgetAction _moveAction;

    /** The object scene.
     */
    private final ObjectScene _scene;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    /** The popup menu added to each widget loaded on the scene.
     */
    private class NamedObjectPopupMenu extends JPopupMenu {

        /** Create a new context menu for the widget.
         *  @param widget The triggering widget.
         */
        public NamedObjectPopupMenu(final PositionableElement element) {
            JMenuItem edit = new JMenuItem("Edit");
            edit.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    _showWidgetProperties(element);
                }
            });

            JMenuItem delete = new JMenuItem("Delete");
            delete.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    _mainFrame.removeVisualNamedObject(element);
                }
            });

            add(edit);
            add(delete);
        }
    }
}
