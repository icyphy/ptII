/* A tree of ptolemy objects.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

package ptolemy.vergil.toolbox;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.gui.util.*;
import diva.canvas.*;
import diva.gui.toolbox.*;
import java.awt.Component;
import java.awt.Point;
import java.awt.dnd.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
A Design tree views a ptolemy model as a tree.  By default, only
the entities in the model are displayed.  The entities in the tree
are also made to be dragged and dropped.
 
@author Steve Neuendorffer
@version $Id$
*/
public class JDesignTree extends JTree {

    /** 
     * Create a new JTree that is rooted on the given entity.
     */
    public JDesignTree(CompositeEntity entity) {
        super(new PtolemyTreeModel(entity));
	setCellRenderer(new PtolemyTreeCellRenderer());
	DragSource.getDefaultDragSource().
	    createDefaultDragGestureRecognizer(
		this, DnDConstants.ACTION_COPY_OR_MOVE,
		new DesignTreeDragGestureListener());
    }

    // A Drag Gesture listener for the tree.  Drag each item as an
    // instance of Ptoel
    private class DesignTreeDragGestureListener implements DragGestureListener
    {
	public void dragGestureRecognized(DragGestureEvent e) {
	    final DragSourceListener dsl = new DragSourceListener() {
		public void dragDropEnd(DragSourceDropEvent dsde) {}
		public void dragEnter(DragSourceDragEvent dsde) {
		    DragSourceContext context = dsde.getDragSourceContext();
		    //intersection of the users selected action, and the
		    //source and target actions
		    int myaction = dsde.getDropAction();
		    if( (myaction & DnDConstants.ACTION_COPY_OR_MOVE) != 0) {
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
	    if(source instanceof JTree) {
		JTree tree = (JTree) source;
		Point sourcePoint = e.getDragOrigin();
		TreePath path = 
		    tree.getPathForLocation(sourcePoint.x, 
					    sourcePoint.y);
		// If we didn't select anything.. then don't drag.
		if(path == null) return;
		Object object = path.getLastPathComponent();
		if(object == null) return;
		if(object instanceof NamedObj) {
		    PtolemyTransferable transferable =
			new PtolemyTransferable((NamedObj)object);
		    //initial cursor, transferable, dsource listener
		    e.startDrag(DragSource.DefaultCopyNoDrop,
				transferable, dsl);

		}	  
	    }
	}
    };
}
