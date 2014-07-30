/* A tree of ptolemy objects.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.vergil.tree;

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

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.EntityLibrary;
import ptolemy.vergil.toolbox.PtolemyTransferable;

/**
 This class provides a tree view of a ptolemy model.
 The class supports drag-and-drop.

 @author Steve Neuendorffer and Edward A. Lee, contributor: Sean Riddle
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
@SuppressWarnings("serial")
public class PTree extends JTree {
    /** Create a new tree that is rooted at the given entity.
     *  This constructor creates a tree that shows the expression
     *  value for any object that implements {@link ptolemy.kernel.util.Settable}.
     *  @param model The model that is the root of the tree.
     */
    public PTree(TreeModel model) {
        this(model, true);
    }

    /** Create a new tree that is rooted at the given entity.
     *  @param model The model that is the root of the tree.
     *  @param showSettableValues If true, show the expression value
     *   for any object that implements {@link ptolemy.kernel.util.Settable}.
     */
    public PTree(TreeModel model, boolean showSettableValues) {
        // The use of EntityTreeModel here is what restricts the
        // tree to displaying only entities.
        super(model);
        setCellRenderer(new PtolemyTreeCellRenderer(showSettableValues));
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
                this, DnDConstants.ACTION_COPY_OR_MOVE,
                new PTreeDragGestureListener());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Give a string representation of the node value.
     *  If the value is a Nameable, this returns its name.
     *  Otherwise, it returns value.toString();
     */
    @Override
    public String convertValueToText(Object value, boolean selected,
            boolean expanded, boolean leaf, int row, boolean hasFocus) {
        String result;
        if (value instanceof Nameable) {
            result = ((Nameable) value).getName();
        } else {
            result = value.toString();
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // A Drag Gesture listener for the tree.
    private static class PTreeDragGestureListener implements
    DragGestureListener {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.
        @Override
        public void dragGestureRecognized(DragGestureEvent e) {
            final DragSourceListener dsl = new DragSourceListener() {
                @Override
                public void dragDropEnd(DragSourceDropEvent dsde) {
                }

                @Override
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

                @Override
                public void dragExit(DragSourceEvent dse) {
                }

                @Override
                public void dragOver(DragSourceDragEvent dsde) {
                }

                @Override
                public void dropActionChanged(DragSourceDragEvent dsde) {
                }
            };

            Component source = e.getComponent();

            if (source instanceof JTree) {
                JTree tree = (JTree) source;
                Point sourcePoint = e.getDragOrigin();
                TreePath path = tree.getPathForLocation(sourcePoint.x,
                        sourcePoint.y);

                // If we didn't select anything.. then don't drag.
                if (path == null) {
                    return;
                }

                if (path.getLastPathComponent() instanceof EntityLibrary) {
                    //this prevents a user from dragging a folder.
                    return;
                }

                Object object = path.getLastPathComponent();

                if (object == null) {
                    return;
                }

                if (object instanceof NamedObj
                        && !_isPropertySet((NamedObj) object, "_notDraggable")) {
                    PtolemyTransferable transferable = new PtolemyTransferable();
                    transferable.addObject((NamedObj) object);

                    //initial cursor, transferable, dsource listener
                    e.startDrag(DragSource.DefaultCopyNoDrop, transferable, dsl);
                }
            }
        }
    }

    private static boolean _isPropertySet(NamedObj object, String name) {
        Attribute attribute = object.getAttribute(name);

        if (attribute == null) {
            return false;
        }

        if (attribute instanceof Parameter) {
            try {
                Token token = ((Parameter) attribute).getToken();

                if (token instanceof BooleanToken) {
                    if (!((BooleanToken) token).booleanValue()) {
                        return false;
                    }
                }
            } catch (IllegalActionException e) {
                // Ignore, using default of true.
            }
        }

        return true;
    }

}
