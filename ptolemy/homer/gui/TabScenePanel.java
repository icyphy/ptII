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

import javax.swing.JMenuItem;
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
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.action.AlignWithMoveStrategyProvider;
import org.netbeans.modules.visual.action.SingleLayerAlignWithWidgetCollector;

import ptolemy.actor.gui.PortablePlaceable;
import ptolemy.homer.kernel.ContentPrototype;
import ptolemy.homer.kernel.PositionableElement;
import ptolemy.homer.widgets.NamedObjectWidgetInterface;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.toolbox.PtolemyTransferable;

//////////////////////////////////////////////////////////////////////////
//// TabScenePanel

/**
 *
 * @author Anar Huseynov
 * @version $Id$ 
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class TabScenePanel implements ContentPrototype {

    /**
     * TODO
     */
    public TabScenePanel(UIDesignerFrame mainFrame) {
        _mainFrame = mainFrame;
        _scene = new ObjectScene();
        _mainLayer = new LayerWidget(getScene());
        _interactionLayer = new LayerWidget(getScene());
        _scene.createView();
        getScene().addChild(_interactionLayer);
        getScene().addChild(_mainLayer);
        _resizeAction = ActionFactory.createAlignWithResizeAction(_mainLayer,
                _interactionLayer, null, false);
        //        _moveAction = ActionFactory.createAlignWithMoveAction(_mainLayer,
        //                _interactionLayer, null, false);
        SingleLayerAlignWithWidgetCollector collector = new SingleLayerAlignWithWidgetCollector(
                _mainLayer, false);
        final AlignWithMoveStrategyProvider alignWithMoveStrategyProvider = new AlignWithMoveStrategyProvider(
                collector, _interactionLayer,
                ALIGN_WITH_MOVE_DECORATOR_DEFAULT, false);
        _moveAction = ActionFactory.createMoveAction(new MoveStrategy() {

            public Point locationSuggested(Widget widget,
                    Point originalLocation, Point suggestedLocation) {
                adjustLocation(widget, suggestedLocation);
                Point locationSuggested = alignWithMoveStrategyProvider
                        .locationSuggested(widget, originalLocation,
                                suggestedLocation);
                return locationSuggested;
            }
        }, alignWithMoveStrategyProvider);
        _hoverProvider = new TwoStateHoverProvider() {

            public void unsetHovering(Widget widget) {
                widget.setBorder(DEFAULT_BORDER);
            }

            public void setHovering(Widget widget) {
                widget.setBorder(RESIZE_BORDER);
            }
        };
        _hoverAction = ActionFactory.createHoverAction(_hoverProvider);
        _clickListener = ActionFactory.createEditAction(new EditProvider() {

            public void edit(Widget widget) {
                new WidgetPropertiesFrame(widget).setVisible(true);
            }

        });
        _scene.getActions().addAction(_hoverAction);
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

                        Object transfererable = dropObjects.get(0);

                        if (transfererable instanceof PortablePlaceable
                                || transfererable instanceof Settable) {
                            dropEvent
                                    .acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
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
        _popupMenuAction = ActionFactory
                .createPopupMenuAction(new PopupMenuProvider() {

                    public JPopupMenu getPopupMenu(Widget widget,
                            Point localLocation) {
                        return new NamedObjectPopupMenu(
                                (NamedObjectWidgetInterface) widget);
                    }

                });
    }

    private class NamedObjectPopupMenu extends JPopupMenu {
        public NamedObjectPopupMenu(NamedObjectWidgetInterface widget) {
            _widget = widget;
            JMenuItem delete = new JMenuItem("Delete");
            delete.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    _mainFrame.removeNamedObject(_widget.getNamedObject());
                }
            });
            JMenuItem edit = new JMenuItem("Edit");
            edit.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                }
            });
            add(delete);
            add(edit);
        }

        private final NamedObjectWidgetInterface _widget;
    }

    /**
     * TODO
     * @param widget
     * @param location
     * @throws NameDuplicationException 
     * @throws IllegalActionException 
     */
    public void addWidget(Widget widget, Point location)
            throws IllegalActionException, NameDuplicationException {
        widget.setPreferredLocation(location);
        widget.getActions().addAction(_resizeAction);
        widget.getActions().addAction(_moveAction);
        widget.getActions().addAction(_clickListener);
        widget.getActions().addAction(_hoverAction);
        widget.getActions().addAction(_scene.createSelectAction());
        widget.getActions().addAction(_popupMenuAction);
        widget.setBorder(DEFAULT_BORDER);
        _mainLayer.addChild(widget);
        _scene.validate();
        adjustLocation(widget, location);
        widget.setPreferredLocation(location);
        _scene.validate();
    }

    public void removeWidget(Widget widget) {
        _mainLayer.removeChild(widget);
    }

    private void adjustLocation(Widget widget, Point location) {
        if (location.getX() + widget.getBounds().getWidth() > getContent()
                .getWidth()) {
            location.setLocation(getContent().getWidth()
                    - widget.getBounds().getWidth(), location.getY());
        }
        if (location.getY() + widget.getBounds().getHeight() > getContent()
                .getHeight()) {
            location.setLocation(location.getX(), getContent().getHeight()
                    - widget.getBounds().getHeight());
        }
        if (location.getX() < 0) {
            location.setLocation(0, location.getY());
        }
        if (location.getY() < 0) {
            location.setLocation(location.getX(), 0);
        }
    }

    /**
     * @return
     */
    public Component getContent() {
        return getScene().getView();
    }

    /**
     * @return the _scene
     */
    public ObjectScene getScene() {
        return _scene;
    }

    private static final Border RESIZE_BORDER = BorderFactory
            .createResizeBorder(6, Color.BLACK, true);
    private static final Border DEFAULT_BORDER = BorderFactory
            .createEmptyBorder(6);
    private final UIDesignerFrame _mainFrame;
    private final LayerWidget _mainLayer;
    private final LayerWidget _interactionLayer;
    private final ObjectScene _scene;
    private final WidgetAction _moveAction;
    private final WidgetAction _resizeAction;
    private final WidgetAction _clickListener;
    private final TwoStateHoverProvider _hoverProvider;
    private WidgetAction _popupMenuAction;
    private WidgetAction _hoverAction;
    private static final BasicStroke STROKE = new BasicStroke(1.0f,
            BasicStroke.JOIN_BEVEL, BasicStroke.CAP_BUTT, 5.0f, new float[] {
                    6.0f, 3.0f }, 0.0f);
    private static final AlignWithMoveDecorator ALIGN_WITH_MOVE_DECORATOR_DEFAULT = new AlignWithMoveDecorator() {
        public ConnectionWidget createLineWidget(Scene scene) {
            ConnectionWidget widget = new ConnectionWidget(scene);
            widget.setStroke(STROKE);
            widget.setForeground(Color.BLUE);
            return widget;
        }
    };
    public void add(PositionableElement element) throws IllegalActionException {
        try {
            _mainFrame.addVisualNamedObject(TabScenePanel.this,
                    element.getElement(), element.getLocation());
        } catch (NameDuplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ContentPrototype getNewInstance() {
        return new TabScenePanel(_mainFrame);
    }

}
