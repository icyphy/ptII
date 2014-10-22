/*
 * Copyright (c) 2004-2014 by Michael Connor. All Rights Reserved.
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
package ptolemy.actor.gui.run;

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
import javax.swing.JTable;

import org.mlc.swing.layout.TransferableWrapper;

import com.jgoodies.forms.layout.CellConstraints;

///////////////////////////////////////////////////////////////////
//// LayoutTable

/**
 A table subclass to make it easier to handle dragging and
 dropping.
 http://www.hut.fi/~landerso/cccp/src/java/cccp/mappingtool/util/MTTable.java

 @author Unknown
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
@SuppressWarnings("serial")
class LayoutTable extends JTable implements DragSourceListener,
        DragGestureListener, DropTargetListener, Autoscroll {
    protected DragSource fDragSource = null;
    protected DropTarget fDropTarget = null;
    protected Component dragComponent = null;

    final static int AUTOSCROLL_INSET_SIZE = 20;
    final static int SCROLL_AMOUNT = 10;

    public LayoutTable(RunLayoutFrame granddaddy, PtolemyFormEditor daddy) {
        super();

        _formEditor = daddy;
        _frame = granddaddy;

        fDragSource = new DragSource();
        fDropTarget = new DropTarget(this, this);
        fDragSource.createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_MOVE, this);
    }

    /**
     * Drag and drop is kind of weird in that when you drag something, you often
     * start in a cell that has a component in it but by the time the drag
     * handling code gets the event the selection has moved into a cell that
     * does have a component in it and the drag fails.
     */
    @Override
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle,
            boolean extend) {
        super.changeSelection(rowIndex, columnIndex, toggle, extend);

        Component component = getSelectedControl();
        _formEditor.setFormComponent(component);
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
        try {
            java.awt.Point location = e.getLocation();
            int col = columnAtPoint(location);
            int row = rowAtPoint(location);

            Component existingComponent = getControlAt(col, row);
            if (col < 1 || row < 1 || existingComponent != null) {
                // Don't allow drop onto constraints for row or column,
                // nor onto a pre-existing component.
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
            e.acceptDrop(DnDConstants.ACTION_MOVE);
            Transferable transferable = e.getTransferable();
            Object dropObject = transferable.getTransferData(javaObject);

            if (dropObject instanceof String) {
                String componentName = (String) dropObject;
                // Ensure the component name doesn't collide.
                int suffix = 1;
                while (_formEditor._containerLayout
                        .getComponentByName(componentName) != null) {
                    componentName = (String) dropObject + "_" + suffix;
                    suffix++;
                }
                try {
                    // Parse the name to get a component.
                    Component component = _frame._pane
                            ._getComponent((String) dropObject);
                    CellConstraints componentConstraints = new CellConstraints(
                            col, row);
                    // If the name starts with "Subpanel:" then it's a subpanel.
                    boolean isContainer = ((String) dropObject)
                            .startsWith("Subpanel:");

                    // the best way to add this control is to setup the constraints
                    // in the map of name->constraints and then add it to the
                    // container.
                    // this layout manager will intercept it, find the constraints
                    // and then set it up properly in the table and assign the maps.
                    _formEditor._containerLayout.addCellConstraints(
                            componentName, componentConstraints);
                    _formEditor._container.add(component, componentName);
                    _formEditor.newComponents.add(component);

                    if (isContainer) {
                        _frame.addContainer(componentName,
                                (Container) component);
                    }
                    e.dropComplete(true);

                    _formEditor.updateLayout(component); // force preview relayout
                    _formEditor._updateLayouts(); // the key for updating with new panel
                    changeSelection(componentConstraints.gridY,
                            componentConstraints.gridX, false, false);
                    repaint();
                    return;
                } catch (Throwable t) {
                    // FIXME: Not this!
                    t.printStackTrace();
                    e.dropComplete(false);
                    return;
                }
            } else {
                // someone dropped something unexpected on us...
                e.dropComplete(false);
                return;
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The parent form editor. */
    private PtolemyFormEditor _formEditor;

    /** The enclosing layout frame. */
    private RunLayoutFrame _frame;
}
