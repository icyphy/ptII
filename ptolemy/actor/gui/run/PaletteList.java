/* A palette of objects that can be dropped into a customizable run control panel.

 Copyright (c) 2007-2014 The Regents of the University of California.
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

 This class is based on DndList by Michael Connor, which
 bears the following copyright:

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

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceListener;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import org.mlc.swing.layout.ComponentDef;
import org.mlc.swing.layout.TransferableWrapper;

///////////////////////////////////////////////////////////////////
//// PaletteList

/**
A customized version of the DndList class by
Michael Connor (mlconnor&#064;yahoo.com). The only reason for
customization is to require a PtolemyFormEditor constructor argument.

@author Michael Connor and Edward A. Lee
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (cxh)
 */
public class PaletteList extends JList implements DragSourceListener,
        DragGestureListener {

    /** Construct a PaletteList.
     * @param editor The form editor
     * @param listModel the list model
     */
    public PaletteList(PtolemyFormEditor editor, ListModel listModel) {
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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** In this derived class, do nothing.
     *  @param dragSourceDropEvent Ignored.
     */
    @Override
    public void dragDropEnd(java.awt.dnd.DragSourceDropEvent dragSourceDropEvent) {
    }

    /** In this derived class, do nothing.
     *  @param dragSourceDragEvent Ignored.
     */
    @Override
    public void dragEnter(java.awt.dnd.DragSourceDragEvent dragSourceDragEvent) {
    }

    /** In this derived class, do nothing.
     *  @param dragSourceEvent Ignored.
     */
    @Override
    public void dragExit(java.awt.dnd.DragSourceEvent dragSourceEvent) {
    }

    /** Start the drag.
     *  @param event The event that starts the drag if it has an index.
     */
    @Override
    public void dragGestureRecognized(DragGestureEvent event) {
        int dragIndex = locationToIndex(event.getDragOrigin());
        if (dragIndex >= 0) {
            Object draggingComponent = this.getModel().getElementAt(dragIndex);
            event.startDrag(java.awt.dnd.DragSource.DefaultCopyDrop,
                    new TransferableWrapper(draggingComponent), this);
        }
    }

    /** In this derived class, do nothing.
     *  @param dragSourceDragEvent Ignored.
     */
    @Override
    public void dragOver(java.awt.dnd.DragSourceDragEvent dragSourceDragEvent) {
    }

    /** In this derived class, do nothing.
     *  @param e Ignored.
     */
    public void drop(java.awt.dnd.DropTargetDropEvent e) {
    }

    /** In this derived class, do nothing.
     *  @param dragSourceDragEvent Ignored.
     */
    @Override
    public void dropActionChanged(
            java.awt.dnd.DragSourceDragEvent dragSourceDragEvent) {
    }

    /** Return the tool tip text for an event.
     *  @param evt The event
     *  @return The tool tip text, if any.
     */
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The source of the drag. */
    protected DragSource fDragSource = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private final PtolemyFormEditor editor;

    private static final long serialVersionUID = 1L;
}
