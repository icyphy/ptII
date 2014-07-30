/*
 The tab scene onto which widgets can be dropped, resized, and
 arranged in order to suite the needs of the handheld consumer.

 Copyright (c) 2011-2014 The Regents of the University of California.
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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.AlignWithMoveDecorator;
import org.netbeans.api.visual.action.EditProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.ResizeProvider.ControlPoint;
import org.netbeans.api.visual.action.ResizeStrategy;
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
import org.netbeans.modules.visual.action.AlignWithResizeStrategyProvider;
import org.netbeans.modules.visual.action.SingleLayerAlignWithWidgetCollector;

import ptolemy.actor.injection.PortablePlaceable;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.homer.kernel.ContentPrototype;
import ptolemy.homer.kernel.HomerConstants;
import ptolemy.homer.kernel.HomerLocation;
import ptolemy.homer.kernel.HomerWidgetElement;
import ptolemy.homer.kernel.LayoutFileOperations;
import ptolemy.homer.kernel.LayoutFileOperations.SinkOrSource;
import ptolemy.homer.kernel.PositionableElement;
import ptolemy.homer.widgets.AttributeStyleWidget;
import ptolemy.homer.widgets.MinSizeInterface;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.toolbox.PtolemyTransferable;

///////////////////////////////////////////////////////////////////
//// TabScenePanel

/** The tab scene onto which widgets can be dropped, resized, and
 *  arranged in order to suite the needs of the handheld consumer.
 *
 *  @author Anar Huseynov
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
public class TabScenePanel implements ContentPrototype {

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The popup menu added to each widget loaded on the scene.
     */
    @SuppressWarnings("serial")
    private class NamedObjectPopupMenu extends JPopupMenu {

        /** Create a new context menu for the widget.
         *  @param element The triggering widget.
         */
        public NamedObjectPopupMenu(final PositionableElement element) {
            JMenuItem edit = new JMenuItem("Edit");
            edit.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    _showWidgetProperties(element);
                }
            });

            JMenuItem delete = new JMenuItem("Delete");
            delete.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    _mainFrame.removeVisualNamedObject(element);
                }
            });

            add(edit);
            add(delete);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new tab scene onto which widgets can be dropped.
     *  @param mainFrame The parent frame of the panel.
     */
    public TabScenePanel(HomerMainFrame mainFrame) {
        _mainFrame = mainFrame;
        _scene = new ObjectScene();
        _mainLayer = new LayerWidget(getContent());
        _interactionLayer = new LayerWidget(getContent());
        _scene.createView();
        _scene.setOpaque(true);
        _scene.setBackground(Color.BLACK);
        _scene.setLayout(LayoutFactory.createAbsoluteLayout());
        _scene.getActions().addAction(_hoverAction);

        getContent().addChild(_interactionLayer);
        getContent().addChild(_mainLayer);
        _mainLayer.setLayout(LayoutFactory.createAbsoluteLayout());

        final AlignWithMoveStrategyProvider alignWithMoveStrategyProvider = new AlignWithMoveStrategyProvider(
                new SingleLayerAlignWithWidgetCollector(_mainLayer, false),
                _interactionLayer, MOVE_ALIGN_DECORATOR, false);

        _moveAction = ActionFactory.createMoveAction(new MoveStrategy() {
            @Override
            public Point locationSuggested(Widget widget,
                    Point originalLocation, Point suggestedLocation) {
                _adjustLocation(widget, suggestedLocation);
                Point locationSuggested = alignWithMoveStrategyProvider
                        .locationSuggested(widget, originalLocation,
                                suggestedLocation);
                return locationSuggested;
            }
        }, alignWithMoveStrategyProvider);

        final AlignWithResizeStrategyProvider alignWithResizeStrategyProvider = new AlignWithResizeStrategyProvider(
                new SingleLayerAlignWithWidgetCollector(_mainLayer, false),
                _interactionLayer, MOVE_ALIGN_DECORATOR, false);

        _resizeAction = ActionFactory.createResizeAction(new ResizeStrategy() {

            @Override
            public Rectangle boundsSuggested(Widget widget,
                    Rectangle originalBounds, Rectangle suggestedBounds,
                    ControlPoint controlPoint) {
                _adjustBounds(widget, suggestedBounds);
                Rectangle boundsSuggested = alignWithResizeStrategyProvider
                        .boundsSuggested(widget, originalBounds,
                                suggestedBounds, controlPoint);
                return boundsSuggested;
            }
        }, alignWithResizeStrategyProvider);

        new DropTarget(_scene.getView(), new DropTargetAdapter() {

            /** Accept the event if the data is a known key.
             *  This is called while a drag operation is ongoing,
             *  when the mouse pointer enters the operable part of
             *  the drop site for the DropTarget registered with
             *  this listener.
             *  @param dropEvent The drop event.
             */
            @Override
            public void dragEnter(DropTargetDragEvent dropEvent) {
                try {
                    // Reject is data flavor is not supported.
                    if (dropEvent
                            .isDataFlavorSupported(PtolemyTransferable.namedObjFlavor)) {

                        List<?> dropObjects = (java.util.List) dropEvent
                                .getTransferable().getTransferData(
                                        PtolemyTransferable.namedObjFlavor);

                        // Reject if not PortablePlaceable or Settable.
                        Object transferable = dropObjects.get(0);
                        if (!(transferable instanceof PortablePlaceable)
                                && !(transferable instanceof Settable)) {
                            dropEvent.rejectDrag();
                            return;
                        }

                        // Reject if it is already in the contents.
                        if (_mainFrame.contains((NamedObj) transferable)) {
                            dropEvent.rejectDrag();
                            return;
                        }

                        // Reject if it's an entity, but not a sink.
                        if (transferable instanceof ComponentEntity) {
                            SinkOrSource isTransferableSinkOrSource = LayoutFileOperations
                                    .isSinkOrSource((ComponentEntity) transferable);
                            if (isTransferableSinkOrSource == SinkOrSource.NONE) {
                                dropEvent.rejectDrag();
                                return;
                            }
                        }

                        dropEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
                    } else if (dropEvent
                            .isDataFlavorSupported(NamedObjectTree.LABEL_FLAVOR)) {
                        dropEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
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
            @Override
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
                        dropEvent.acceptDrop(DnDConstants.ACTION_LINK);
                    } catch (UnsupportedFlavorException e) {
                        MessageHandler.error(
                                "Can't find a supported data flavor for drop in "
                                        + dropEvent, e);
                    } catch (IOException e) {
                        MessageHandler.error(
                                "Can't find a supported data flavor for drop in "
                                        + dropEvent, e);
                    } catch (IllegalActionException e) {
                        MessageHandler.error(
                                "Can't initialize widget for the selected object "
                                        + dropEvent, e);
                    } catch (NameDuplicationException e) {
                        MessageHandler.error(
                                "Can't initialize widget for the selected object "
                                        + dropEvent, e);
                    }
                } else if (dropEvent
                        .isDataFlavorSupported(NamedObjectTree.LABEL_FLAVOR)) {
                    try {
                        Object transferData = dropEvent.getTransferable()
                                .getTransferData(NamedObjectTree.LABEL_FLAVOR);
                        _mainFrame.addLabel(TabScenePanel.this,
                                transferData.toString(), null,
                                dropEvent.getLocation());
                        dropEvent.acceptDrop(DnDConstants.ACTION_LINK);
                    } catch (UnsupportedFlavorException e) {
                        MessageHandler.error(
                                "Can't initialize widget for the selected object "
                                        + dropEvent, e);
                    } catch (IOException e) {
                        MessageHandler.error(
                                "Can't initialize widget for the selected object "
                                        + dropEvent, e);
                    } catch (IllegalActionException e) {
                        MessageHandler.error(
                                "Can't initialize widget for the selected object "
                                        + dropEvent, e);
                    } catch (NameDuplicationException e) {
                        MessageHandler.error(
                                "Can't initialize widget for the selected object "
                                        + dropEvent, e);
                    }
                } else {
                    dropEvent.rejectDrop();
                }
            }
        });
        _scene.getView().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                _scene.setPreferredLocation(new Point(0, 0));
                _scene.setPreferredSize(_scene.getView().getSize());
                _mainLayer.setPreferredLocation(new Point(0, 0));
                _mainLayer.setPreferredSize(_scene.getView().getSize());
                _interactionLayer.setPreferredLocation(new Point(0, 0));
                _interactionLayer.setPreferredSize(_scene.getView().getSize());
                Rectangle bounds = new Rectangle(new Point(0, 0), _scene
                        .getView().getBounds().getSize());
                _scene.setMaximumBounds(bounds);
            }
        });
    }

    /** Add an element's representation to the scene.
     *  @param element The element to be added.
     *  @exception IllegalActionException If the appropriate element's representation
     *  cannot be loaded.
     */
    @Override
    public void add(final PositionableElement element)
            throws IllegalActionException {
        if (!(element instanceof HomerWidgetElement)) {
            throw new IllegalActionException(element.getElement(),
                    "No representation is available.");
        }

        final Widget widget = ((HomerWidgetElement) element).getWidget();
        widget.setBorder(DEFAULT_BORDER);
        HomerLocation homerLocation = element.getLocation();
        Insets insets = widget.getBorder().getInsets();
        Point location = new Point(homerLocation.getX() - insets.left,
                homerLocation.getY() - insets.top);

        widget.setPreferredLocation(location);
        if (homerLocation.getWidth() > 0 && homerLocation.getHeight() > 0) {
            widget.setPreferredSize(new Dimension(homerLocation.getWidth()
                    + insets.right, homerLocation.getHeight() + insets.bottom));
            widget.setPreferredBounds(new Rectangle(new Dimension(homerLocation
                    .getWidth() + insets.right, homerLocation.getHeight()
                    + insets.bottom)));
        }

        // Add widget resizing.
        widget.getActions().addAction(_resizeAction);

        // Add widget moving.
        widget.getActions().addAction(_hoverAction);

        // Add widget selection.
        widget.getActions().addAction(_scene.createSelectAction());

        // Add widget movement.
        widget.getActions().addAction(_moveAction);

        // Add widget double-click editing.
        widget.getActions().addAction(
                ActionFactory.createEditAction(new EditProvider() {
                    @Override
                    public void edit(Widget widget) {
                        _showWidgetProperties(element);
                    }
                }));

        // Add widget context menu.
        widget.getActions().addAction(
                ActionFactory.createPopupMenuAction(new PopupMenuProvider() {
                    @Override
                    public JPopupMenu getPopupMenu(Widget widget,
                            Point localLocation) {
                        return new NamedObjectPopupMenu(element);
                    }
                }));

        _mainLayer.addChild(widget);
        _scene.validate();
        _adjustLocation(widget, location);
        _scene.validate();
    }

    /** Get a reference to the current scene.
     *  @return The current scene.
     */
    @Override
    public ObjectScene getContent() {
        return _scene;
    }

    /** Get the name of the tab.
     *  @return The name of the tab.
     *  @see #setName(String)
     */
    public String getName() {
        return _name;
    }

    /** Get a new tab scene panel instance.
     *  @return A new TabScenePanel object.
     */
    @Override
    public ContentPrototype getNewInstance() {
        return new TabScenePanel(_mainFrame);
    }

    /** Return the tag of the tab.
     *  @return the tag of the tab.
     *  @see #setTag(String)
     */
    public String getTag() {
        return _tag;
    }

    /** Get the view associated with the scene.
     *  @return The view component of the scene.
     */
    public Component getView() {
        return getContent().getView();
    }

    /** Remove the widget from the scene.
     *  @param element The widget to be removed.
     */
    @Override
    public void remove(PositionableElement element) {
        _mainLayer.removeChild(((HomerWidgetElement) element).getWidget());
        _mainFrame.repaint();
    }

    /** Set the name of the tab.
     *  @param name The name of the tab.
     *  @see #getName()
     */
    public void setName(String name) {
        _name = name;
    }

    /** Set the tag of the tab.
     *  @param tag The tag to set.
     *  @see #getTag()
     */
    public void setTag(String tag) {
        _tag = tag;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Adjust the bounds of the widget to ensure it fits within the scene.
     *  @param widget The widget whose bounds are adjusted.
     *  @param bounds The bounds to adjust.
     */
    private void _adjustBounds(Widget widget, Rectangle bounds) {
        Insets insets = widget.getBorder().getInsets();
        if (widget instanceof MinSizeInterface) {
            Integer width = ((MinSizeInterface) widget).getMinWidth();
            Integer height = ((MinSizeInterface) widget).getMinHeight();
            if (width != null && bounds.getWidth() < width) {
                bounds.width = width + insets.right + insets.left;
            }
            if (height != null && bounds.getHeight() < height) {
                bounds.height = height + insets.top + insets.bottom;
            }
        }
        Point preferredLocation = widget.getPreferredLocation();
        if (bounds.x + preferredLocation.x + bounds.getWidth() - insets.right > _scene
                .getView().getWidth()) {
            bounds.width = _scene.getView().getWidth()
                    - (bounds.x + preferredLocation.x - insets.right);
        }
        if (bounds.y + preferredLocation.y + bounds.getHeight() + insets.bottom > _scene
                .getView().getHeight()) {
            bounds.height = _scene.getView().getHeight()
                    - (bounds.y + preferredLocation.y + insets.bottom);
        }

        if (bounds.x + preferredLocation.x + insets.left < 0) {
            int adjustment = bounds.x + preferredLocation.x + insets.left;
            bounds.x += -adjustment;
            bounds.width += adjustment;
        }
        if (bounds.y + preferredLocation.y + insets.top < 0) {
            int adjustment = bounds.y + preferredLocation.y + insets.top;
            bounds.y += -adjustment;
            bounds.height += adjustment;
        }
    }

    /** Adjust the screen location of the selected widget.
     *  @param widget The widget to be moved.
     *  @param location The target location.
     */
    private void _adjustLocation(Widget widget, Point location) {
        Rectangle clientArea = widget.getClientArea();
        // This only happens when the file is opened from the file
        if (clientArea == null) {
            return;
        }
        if (location.x + clientArea.getWidth() + clientArea.x > getView()
                .getPreferredSize().getWidth()) {
            location.x = getView().getWidth() - clientArea.width - clientArea.x;
        }
        if (location.y + clientArea.getHeight() + clientArea.y > getView()
                .getPreferredSize().getHeight()) {
            location.y = getView().getHeight() - clientArea.height
                    - clientArea.y;
        }
        if (location.x + clientArea.x < 0) {
            location.x = -clientArea.x;
        }
        if (location.y + clientArea.y < 0) {
            location.y = -clientArea.y;
        }
    }

    /** Display the widget properties window for modification.
     *  @param element The target widget whose properties should be displayed.
     */
    private void _showWidgetProperties(PositionableElement element) {
        Widget widget = ((HomerWidgetElement) element).getWidget();
        if (widget != null) {
            WidgetPropertiesFrame dialog = new WidgetPropertiesFrame(widget);
            if (dialog.showPrompt() == JOptionPane.OK_OPTION) {
                try {
                    Rectangle widgetBounds = dialog.getWidgetBounds();
                    widget.setPreferredBounds(widgetBounds);

                    NamedObj namedObj = ((HomerWidgetElement) element)
                            .getElement();
                    if (namedObj != null) {
                        Parameter enabled = (Parameter) namedObj
                                .getAttribute(HomerConstants.ENABLED_NODE);
                        if (enabled == null) {
                            new Parameter(namedObj,
                                    HomerConstants.ENABLED_NODE,
                                    new BooleanToken(dialog.getEnabled()));
                        } else {
                            enabled.setToken(new BooleanToken(dialog
                                    .getEnabled()));
                        }

                        Parameter required = (Parameter) namedObj
                                .getAttribute(HomerConstants.REQUIRED_NODE);
                        if (required == null) {
                            new Parameter(namedObj,
                                    HomerConstants.REQUIRED_NODE,
                                    new BooleanToken(dialog.getRequired()));
                        } else {
                            required.setToken(new BooleanToken(dialog
                                    .getRequired()));
                        }
                    }

                    if (HomerMainFrame.isLabelWidget(element.getElement())
                            && widget instanceof AttributeStyleWidget) {
                        AttributeStyleWidget attributeStyleWidget = (AttributeStyleWidget) widget;
                        ((Settable) element.getElement()).setExpression(dialog
                                .getLabel());
                        attributeStyleWidget.updateValue();
                    }

                    _adjustBounds(widget, widgetBounds);
                    _scene.validate();
                } catch (Exception ex) {
                    MessageHandler.error("Invalid size specifid", ex);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The default border to apply to all scenes.
     */
    private static final Border DEFAULT_BORDER = BorderFactory
            .createEmptyBorder(6);

    /** The default decorator for move alignment actions.
     */
    private static final AlignWithMoveDecorator MOVE_ALIGN_DECORATOR = new AlignWithMoveDecorator() {
        @Override
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
            .createResizeBorder(6, Color.WHITE, true);

    /** Default stroke for use in widget connection.
     */
    private static final BasicStroke STROKE = new BasicStroke(1.0f,
            BasicStroke.JOIN_BEVEL, BasicStroke.CAP_BUTT, 5.0f, new float[] {
            6.0f, 3.0f }, 0.0f);

    /** Hover action added to new widgets.
     */
    private final WidgetAction _hoverAction = ActionFactory
            .createHoverAction(new TwoStateHoverProvider() {
                @Override
                public void setHovering(Widget widget) {
                    if (!widget.getState().isSelected()) {
                        widget.setBorder(RESIZE_BORDER);
                    }
                }

                @Override
                public void unsetHovering(Widget widget) {
                    if (!widget.getState().isSelected()) {
                        widget.setBorder(DEFAULT_BORDER);
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

    /** The name of the tab.
     */
    private String _name;

    /**
     * The resize action added to all new widgets.
     */
    private WidgetAction _resizeAction;

    /** The object scene.
     */
    private final ObjectScene _scene;

    /** The tag of the tab.
     */
    private String _tag;
}
