/* A node renderer for an abstract syntax tree.

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

package ptolemy.lang.tree;

import ptolemy.lang.TreeNode;

import java.awt.Component;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
A node renderer for an abstract syntax tree.  This is used by
ASTModel to create a browsable image of an abstract syntax tree.
Each node is rendered by its class name, as a string.

@see ASTModel
@author Edward A. Lee
@version $Id$
*/
public class ASTNodeRenderer extends DefaultTreeCellRenderer {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a label for the given value.  Currently, the label
     *  contains only the classname.
     *  @returns A component to put into the JTree.
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
             boolean selected, boolean expanded, boolean leaf, 
             int row, boolean hasFocus) {
	// FIXME: If nodes ever acquire a common class, fix this up.
        if (value instanceof List) {
            List list = (List)value;
            if (list.size() == 0) {
                // NOTE: It seems that JTree gets confused if the list
                // is an ArrayList.  It displays an empty field even though
                // the following line gets executed.
                return (new JLabel("Empty List"));
            }
            return (new JLabel("List"));
        } else if (value instanceof TreeNode) {
            TreeNode node = (TreeNode)value;
            Object description = node.accept(new DescriptionVisitor());
            return (new JLabel(description.toString()));
        } else {
            return new JLabel(value.getClass().getName());
        }
    }
}
