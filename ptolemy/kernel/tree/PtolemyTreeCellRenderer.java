/* A tree cell renderer for ptolemy objects.

 Copyright (c) 2000 The Regents of the University of California.
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

package ptolemy.kernel.tree;

import ptolemy.kernel.util.Nameable;

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
A tree cell renderer for Ptolemy objects.  This renderer just adds the
objects name to the default rendition.
 
@see #PtolemyTreeModel
@author Steve Neuendorffer and Edward A. Lee
@version $Revision$
*/
public class PtolemyTreeCellRenderer extends DefaultTreeCellRenderer {

    /** Create a new rendition for the given object.  The rendition is
     *  the default provided by the base class with the text set to
     *  the name of the node (if it is an object implementing Nameable).
     */
    public Component getTreeCellRendererComponent(JTree tree,
	Object value, boolean selected, boolean expanded, boolean leaf, 
	int row, boolean hasFocus) {
	
	DefaultTreeCellRenderer component = (DefaultTreeCellRenderer)
	    super.getTreeCellRendererComponent(tree, value, 
		selected, expanded, leaf, row, hasFocus);	
	if(value instanceof Nameable) {
	    Nameable object = (Nameable) value;
	    // Fix the background colors because transparent 
	    // labels don't work quite right.
            // FIXME: is this still needed?
	    if(!selected) {
		component.setBackground(tree.getBackground());
		component.setOpaque(true);
	    } else {
		component.setOpaque(false);
	    }	    
	    component.setText(object.getName());
        }
	return component;
    }
}
