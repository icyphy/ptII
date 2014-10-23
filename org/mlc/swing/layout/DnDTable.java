/*
 * Copyright (c) 2004-2007 by Michael Connor. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of FormLayoutBuilder or Michael Connor nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mlc.swing.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTable;

import com.jgoodies.forms.layout.CellConstraints;

/**
 * I'm creating a table subclass to make it easier to handle dragging and
 * dropping
 * http://www.hut.fi/~landerso/cccp/src/java/cccp/mappingtool/util/MTTable.java
 *
 * @author Michael Connor
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
class DnDTable extends JTable implements DragSourceListener,
DragGestureListener, DropTargetListener, Autoscroll {
    protected DragSource fDragSource = null;
    protected DropTarget fDropTarget = null;
    protected Component dragComponent = null;

    final static int AUTOSCROLL_INSET_SIZE = 20;
    final static int SCROLL_AMOUNT = 10;

    FormEditor parent;
    MultiContainerFrame superparent;

    public DnDTable(MultiContainerFrame granddaddy, FormEditor daddy) {
        super();

        parent = daddy;
        superparent = granddaddy;

        fDragSource = new DragSource();
        fDropTarget = new DropTarget(this, this);
        fDragSource.createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_MOVE, this);
    }

    /**
     * Drag and drop is kind of weird in that when you drag something, you often
     * start in a cell that has a component in it but by the time the drag
     * handling code get's the event the selection has moved into a cell that
     * does have a component in it and the drag fails.
     */
    @Override
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle,
            boolean extend) {
        super.changeSelection(rowIndex, columnIndex, toggle, extend);

        Component component = getSelectedControl();
        parent.setFormComponent(component);

        if (component != null) {
            // let's update the list control so when they select
            // something in the grid it becomes selected in the
            // list and the property values susequently change.
            parent.componentList.setSelectedValue(component, true);
        } else {
            ;
        }
    }

    /**
     * Implements autoscrolling.
     */
    @Override
    public Insets getAutoscrollInsets() {
        Rectangle visible = getVisibleRect();
        Dimension size = getSize();
        int top = 0, left = 0, bottom = 0, right = 0;
        if (visible.y > 0) {
            top = visible.y + AUTOSCROLL_INSET_SIZE;
        }
        if (visible.x > 0) {
            left = visible.x + AUTOSCROLL_INSET_SIZE;
        }
        if (visible.y + visible.height < size.height) {
            bottom = size.height - visible.y - visible.height
                    + AUTOSCROLL_INSET_SIZE;
        }
        if (visible.x + visible.width < size.width) {
            right = size.width - visible.x - visible.width
                    + AUTOSCROLL_INSET_SIZE;
        }
        return new Insets(top, left, bottom, right);
    }

    /**
     * Implements autoscrolling.
     */
    @Override
    public void autoscroll(Point cursorLocn) {
        Rectangle visible = getVisibleRect();
        int x = 0, y = 0, width = 0, height = 0;
        // Scroll left.
        if (cursorLocn.x < visible.x + AUTOSCROLL_INSET_SIZE) {
            x = -SCROLL_AMOUNT;
            width = SCROLL_AMOUNT;
        }
        // Scroll right.
        else if (cursorLocn.x > visible.x + visible.width
                - AUTOSCROLL_INSET_SIZE) {
            x = visible.width + SCROLL_AMOUNT;
            width = SCROLL_AMOUNT;
        }
        // Scroll up.
        if (cursorLocn.y < visible.y + AUTOSCROLL_INSET_SIZE) {
            y = -SCROLL_AMOUNT;
            height = SCROLL_AMOUNT;
        }
        // Scroll down.
        else if (cursorLocn.y > visible.y + visible.height
                - AUTOSCROLL_INSET_SIZE) {
            y = visible.height + SCROLL_AMOUNT;
            height = SCROLL_AMOUNT;
        }
        ((JComponent) getParent()).scrollRectToVisible(new Rectangle(x, y,
                width, height));
    }

    @Override
    public void dragEnter(DragSourceDragEvent event) {
    }

    @Override
    public void dragOver(DragSourceDragEvent event) {
        DragSourceContext context = event.getDragSourceContext();
        java.awt.Point location = event.getLocation();
        Point org = this.getLocationOnScreen();
        Point relLoc = new Point(location.x - org.x, location.y - org.y);

        int col = columnAtPoint(relLoc);
        int row = rowAtPoint(relLoc);
        Component component = getControlAt(col, row);

        if (col < 1 || row < 1 || component != null) {
            context.setCursor(DragSource.DefaultMoveNoDrop);
        } else {
            context.setCursor(DragSource.DefaultMoveDrop);
        }
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent event) {
    }

    @Override
    public void dragExit(DragSourceEvent event) {
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent event) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent event) {
    }

    @Override
    public void dragExit(DropTargetEvent event) {
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent event) {
        Point p = event.getDragOrigin();
        int row = rowAtPoint(p);
        int col = columnAtPoint(p);
        Component component = getControlAt(col, row);
        if (component != null) {
            event.startDrag(java.awt.dnd.DragSource.DefaultMoveDrop,
                    new TransferableWrapper(component), this);
        }
    }

    @Override
    public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
        try {
            if (dropTargetDragEvent.isDataFlavorSupported(new DataFlavor(
                    DataFlavor.javaJVMLocalObjectMimeType))) {
                dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_MOVE);
            } else {
                dropTargetDragEvent.rejectDrag();
            }
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }

    @Override
    public void dragOver(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent) {
        //DropTargetContext context = dropTargetDragEvent.getDropTargetContext();

        try {
            //java.awt.Point location = dropTargetDragEvent.getLocation();
            //int col = columnAtPoint(location);
            //int row = rowAtPoint(location);

            if (dropTargetDragEvent.isDataFlavorSupported(new DataFlavor(
                    DataFlavor.javaJVMLocalObjectMimeType))) {
                dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_MOVE);
            } else {
                dropTargetDragEvent.rejectDrag();
            }
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }

    @Override
    public void drop(java.awt.dnd.DropTargetDropEvent e) {
        CellConstraints componentConstraints = null;
        Component component = null;

        try {

            java.awt.Point location = e.getLocation();
            int col = columnAtPoint(location);
            int row = rowAtPoint(location);
            Component existComp = getControlAt(col, row);

            if (col < 1 || row < 1 || existComp != null) {
                // don't allow drop onto constraints row/col
                e.rejectDrop();
                e.getDropTargetContext().dropComplete(true);
                return;
            }

            DataFlavor javaObject = new DataFlavor(
                    DataFlavor.javaJVMLocalObjectMimeType);
            if (!e.isDataFlavorSupported(javaObject)) {
                // flavor unsupported
                e.rejectDrop();
                return;
            }

            {
                e.acceptDrop(DnDConstants.ACTION_MOVE);
                Transferable transferable = e.getTransferable();
                Object dropObject = transferable.getTransferData(javaObject);

                if (dropObject instanceof ComponentDef) {
                    ComponentDef componentDef = (ComponentDef) dropObject;

                    NewComponentDialog dlg = NewComponentDialog.doDialog(
                            (JFrame) superparent, componentDef);
                    if (dlg.succeeded()) {
                        String componentName = dlg.getComponentName();
                        componentDef = dlg.componentDef;

                        // insure the component name doesn't collide
                        int suffix = 1;
                        while (parent.containerLayout
                                .getComponentByName(componentName) != null) {
                            componentName = dlg.getComponentName() + "_"
                                    + suffix;
                            suffix++;
                        }
                        componentDef.name = componentName; // store new name for later edit

                        try {
                            component = dlg.getInstance();
                            componentConstraints = new CellConstraints(col, row);

                            // Earlier attempts to use beaninfo wasn't working for JPanel,
                            // and did identify the JGoodies buttonbar as a container (which
                            // it is, but not for our purposes).
                            boolean isContainer = componentDef.isContainer;

                            // the best way to add this control is to setup the constraints
                            // in the map of name->constraints and then add it to the
                            // container.
                            // this layout manager will intercept it, find the constraints
                            // and then set it up properly in the table and assign the maps.
                            parent.containerLayout.addComponent(componentName,
                                    componentDef, componentConstraints);
                            parent.container.add(component, componentName);
                            parent.newComponents.add(component);

                            if (isContainer) {
                                superparent.addContainer(componentName,
                                        (Container) component);
                            }

                            e.dropComplete(true);
                            parent.updateList();

                            parent.updateLayout(component); // force preview relayout
                            parent.updateLayouts(); // the key for updating with new panel
                            changeSelection(componentConstraints.gridY,
                                    componentConstraints.gridX, false, false);
                            repaint();
                            return;

                        } catch (Throwable t) {
                            t.printStackTrace();
                            //              JOptionPane
                            //                  .showMessageDialog(parent,
                            //                      "Unable to create new instance of "
                            //                          + componentClass.getName() + "(" + t.getMessage()
                            //                          + ")", "Error", JOptionPane.ERROR_MESSAGE);
                            e.dropComplete(false);
                            return;
                        }
                    }
                }

                else if (dropObject instanceof Component) {
                    component = (Component) dropObject;
                    componentConstraints = parent
                            .getComponentConstraints(component);

                    if (col > 0 && row > 0) {
                        componentConstraints.gridX = col;
                        componentConstraints.gridY = row;
                        componentConstraints.gridWidth = Math.min(
                                componentConstraints.gridWidth,
                                parent.containerLayout.getColumnCount()
                                - componentConstraints.gridX + 1);
                        componentConstraints.gridHeight = Math.min(
                                componentConstraints.gridHeight,
                                parent.containerLayout.getRowCount()
                                - componentConstraints.gridY + 1);

                        if (!component.isVisible()) {
                            component.setVisible(true);
                        }
                        parent.topComponent = component; // make sure this sorts to the top...

                        e.dropComplete(true);

                        // we either moved a component or added it to the
                        // container for the first time so either way we
                        // need to update the layout...
                        parent.updateLayout(component);
                        changeSelection(componentConstraints.gridY,
                                componentConstraints.gridX, false, false);
                        // we repaint the table because it has changed. i would think that
                        // firing a table update event would be more appropriate but this seems
                        // to work just fine.
                        repaint();
                        // repaint the list because we change the component text from bold
                        // to plain once it's been added.
                        parent.updateList();

                        return;
                    }
                } else {
                    // someone dropped something unexpected on us...
                    e.dropComplete(false);
                    return;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            // flavor exception or ClassNotFoundException
            e.rejectDrop();
            return;
        }
    }

    public Component getControlAt(int col, int row) {
        Component component = row == 0 || col == 0 ? null
                : (Component) getModel().getValueAt(row, col);
        return component;
    }

    public Component getSelectedControl() {
        int col = getSelectedColumn();
        int row = getSelectedRow();
        return getControlAt(col, row);
    }
}
