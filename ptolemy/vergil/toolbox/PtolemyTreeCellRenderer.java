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

package ptolemy.vergil.toolbox;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import diva.canvas.*;
import diva.gui.toolbox.*;
import java.awt.Component;
import java.awt.Point;
import java.awt.dnd.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
A tree cell renderer for Ptolemy objects.  
 
@see #PtolemyTreeModel
@author Steve Neuendorffer
@version $Revision$
*/
public class PtolemyTreeCellRenderer extends DefaultTreeCellRenderer {

    /**
     * Create a new renderer for the given object.  Set the text of the 
     * returned component to be the name of the object and set the icon
     *    
     */
    public Component getTreeCellRendererComponent(JTree tree,
	Object value, boolean selected, boolean expanded, boolean leaf, 
	int row, boolean hasFocus) {
	
	DefaultTreeCellRenderer component = (DefaultTreeCellRenderer)
	    super.getTreeCellRendererComponent(tree, value, 
		selected, expanded, leaf, row, hasFocus);	
	if(value instanceof NamedObj) {
	    NamedObj object = (NamedObj) value;
	    component.setText(object.getName());
	   
            // Now we get to create its icon:
	    NamedObj iconObject = object.getAttribute("_icon");
	    if(iconObject != null && 
	       iconObject instanceof ptolemy.moml.Icon) {
		ptolemy.vergil.toolbox.EditorIcon icon = 
		    (ptolemy.vergil.toolbox.EditorIcon) iconObject;

                // Wow.. this is a confusing line of code.. :)
                component.setIcon(icon.createIcon());
            }
        }
	return component;
    }
}
