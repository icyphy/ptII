/* A tree of ptolemy objects.

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

package ptolemy.vergil.tree;

import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.toolbox.PtolemyTransferable;

import java.awt.Component;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
This class provides a tree view of a ptolemy model, showing only the
entities of the model.  The class supports drag-and-drop.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class PTree extends JTree {

    /** Create a new tree that is rooted at the given entity.
     */
    public PTree(TreeModel model) {
        // The use of EntityTreeModel here is what restricts the
        // tree to displaying only entities.
        super(model);
        setCellRenderer(new PtolemyTreeCellRenderer());
        DragSource.getDefaultDragSource().
            createDefaultDragGestureRecognizer(
                    this, DnDConstants.ACTION_COPY_OR_MOVE,
                    new PTreeDragGestureListener());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // A Drag Gesture listener for the tree.
    private class PTreeDragGestureListener implements DragGestureListener {
        public void dragGestureRecognized(DragGestureEvent e) {
            final DragSourceListener dsl = new DragSourceListener() {
                    public void dragDropEnd(DragSourceDropEvent dsde) {}
                    public void dragEnter(DragSourceDragEvent dsde) {
                        DragSourceContext context = dsde.getDragSourceContext();
                        // Intersection of the users selected action,
                        // and the source and target actions
                        int myaction = dsde.getDropAction();
                        if ((myaction & DnDConstants.ACTION_COPY_OR_MOVE) != 0) {
                            context.setCursor(DragSource.DefaultCopyDrop);
                        } else {
                            context.setCursor(DragSource.DefaultCopyNoDrop);
                        }
                    }
                    public void dragExit(DragSourceEvent dse) {}
                    public void dragOver(DragSourceDragEvent dsde) {}
                    public void dropActionChanged(DragSourceDragEvent dsde) {}
                };

            Component source = e.getComponent();
            if (source instanceof JTree) {
                JTree tree = (JTree) source;
                Point sourcePoint = e.getDragOrigin();
                TreePath path =
                    tree.getPathForLocation(sourcePoint.x,
                            sourcePoint.y);
                // If we didn't select anything.. then don't drag.
                if (path == null) return;
                Object object = path.getLastPathComponent();
                if (object == null) return;
                if (object instanceof NamedObj) {
                    PtolemyTransferable transferable =
                        new PtolemyTransferable();
                    transferable.addObject((NamedObj)object);
                    //initial cursor, transferable, dsource listener
                    e.startDrag(DragSource.DefaultCopyNoDrop,
                            transferable, dsl);
                }
            }
        }
    }
}
