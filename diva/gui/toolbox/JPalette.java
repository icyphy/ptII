/*
  Copyright (c) 1998-2005 The Regents of the University of California
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
  PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
  CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
  ENHANCEMENTS, OR MODIFICATIONS.

  PT_COPYRIGHT_VERSION_2
  COPYRIGHTENDKEY
*/
package diva.gui.toolbox;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * A "palette" of components that can be dragged onto another
 * JComponent.  The components in the palette have String data
 * associated with them, and this is what the drop target sees
 * when a drop is initiated by the user.
 *
 * @author Michael Shilman
 * @version $Id$
 */
public class JPalette extends JPanel {
    /**
     * Construct a new palette instance with a vertical grid layout.
     */
    public JPalette() {
        // FIXME - need a real layout.
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // TESTING
        testLabel("foo");
        testLabel("bar");
        testLabel("baz");
    }

    /**
     * Add an icon to the palette, that is associated with the
     */
    public void addIcon(Icon i, String data) {
        JLabel l = new JLabel(i);
        makeDraggable(l, data);
        add(l);
    }

    /**
     * Make the given component draggable; the given data string will
     * be the "dragged" object that is associated with the component.
     * This data is made available via the StringSelection class,
     * meaning that it will support "plain text" and "java string"
     * data flavors.
     */
    public void makeDraggable(JComponent c, String data) {
        final String dataHandle = data;
        final DragSourceListener dsl = new DragSourceListener() {
                public void dragDropEnd(DragSourceDropEvent dsde) {
                }

                public void dragEnter(DragSourceDragEvent dsde) {
                    DragSourceContext context = dsde.getDragSourceContext();

                    //intersection of the users selected action, and the
                    //source and target actions
                    int myaction = dsde.getDropAction();

                    if ((myaction & DnDConstants.ACTION_COPY) != 0) {
                        context.setCursor(DragSource.DefaultCopyDrop);
                    } else {
                        context.setCursor(DragSource.DefaultCopyNoDrop);
                    }
                }

                public void dragExit(DragSourceEvent dse) {
                }

                public void dragOver(DragSourceDragEvent dsde) {
                }

                public void dropActionChanged(DragSourceDragEvent dsde) {
                }
            };

        final DragGestureListener dgl = new DragGestureListener() {
                public void dragGestureRecognized(DragGestureEvent e) {
                    try {
                        // check to see if action is OK ...
                        Transferable transferable = new StringSelection(dataHandle);

                        //initial cursor, transferable, dsource listener
                        e.startDrag(DragSource.DefaultCopyNoDrop, transferable,
                            dsl);
                    } catch (InvalidDnDOperationException idoe) {
                        System.err.println(idoe);
                    }
                }
            };

        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(c,
            DnDConstants.ACTION_COPY_OR_MOVE, dgl);
    }

    /**
     * Add a a label to the palette with the same string
     * as its associated data, for testing purposes.
     */
    public void testLabel(String s) {
        JLabel l = new JLabel(s);
        makeDraggable(l, s);
        add(l);
    }

    //public void removeIcon(Icon i) {...}
}
