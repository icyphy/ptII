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

/**
 *
 */
package org.mlc.swing.layout;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceListener;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

/**
 * DndList class.
 *
 * @author eal
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class DndList extends JList implements DragSourceListener,
DragGestureListener {
    /**
     *
     */
    private final FormEditor editor;
    private static final long serialVersionUID = 1L;
    protected DragSource fDragSource = null;

    public DndList(FormEditor editor, ListModel listModel) {
        super(listModel);
        this.editor = editor;
        fDragSource = new DragSource();
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // it is very important that autoscrolls be set to false. i
        // spent a lot of time tracking down a bug where there was
        // very strange behavior involving selections and dragging with
        // this list. as far as i can tell, the autoscroller was
        // generating phantom events which i believe is what it is
        // designed to do. i don't have an explanation for how this
        // should be done but i can say if this line of code is not
        // present then bad things will happen.
        this.editor.setAutoscrolls(false);
        fDragSource.createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_MOVE, this);
    }

    @Override
    public void dragDropEnd(java.awt.dnd.DragSourceDropEvent dragSourceDropEvent) {
    }

    @Override
    public void dragEnter(java.awt.dnd.DragSourceDragEvent dragSourceDragEvent) {
    }

    @Override
    public void dragExit(java.awt.dnd.DragSourceEvent dragSourceEvent) {
    }

    @Override
    public void dragOver(java.awt.dnd.DragSourceDragEvent dragSourceDragEvent) {
    }

    @Override
    public void dropActionChanged(
            java.awt.dnd.DragSourceDragEvent dragSourceDragEvent) {
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent event) {
        int dragIndex = locationToIndex(event.getDragOrigin());
        if (dragIndex >= 0) {
            Object draggingComponent = this.getModel().getElementAt(dragIndex);
            event.startDrag(java.awt.dnd.DragSource.DefaultCopyDrop,
                    new TransferableWrapper(draggingComponent), this);
        }
    }

    @Override
    public String getToolTipText(MouseEvent evt) {
        // return a tooltip for the specific entry in the list
        // Get item index
        int index = locationToIndex(evt.getPoint());
        if (index == -1) {
            return "";
        }

        // Get item
        Object o = this.getModel().getElementAt(index);
        if (o instanceof ComponentDef) {
            ComponentDef thisItem = (ComponentDef) o;
            if (thisItem != null) {
                return thisItem.getDescription();
            }
        }
        return "";
    }

    public void drop(java.awt.dnd.DropTargetDropEvent e) {
    }
}
