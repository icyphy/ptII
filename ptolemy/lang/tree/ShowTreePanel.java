/* A panel that shows the names of AST nodes in a JTree.

Copyright (c) 1998-2000 The Regents of the University of California.
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
@AcceptedRating Red (eal@eecs.berkeley.edu)

*/

package ptolemy.lang.tree;

import ptolemy.lang.java.StaticResolution;
import ptolemy.lang.java.nodetypes.CompileUnitNode;

import java.awt.Dimension;
import javax.swing.JScrollPane;
import javax.swing.JTree;

//////////////////////////////////////////////////////////////////////////
//// ShowTreePanel
/** 
A panel that displays the abstract syntax tree of the specified
java files.

@author Edward Lee
@version $Id$
*/
public class ShowTreePanel extends JScrollPane {

    /** Construct a display of the abstract syntax trees of the specified
     *  Java file.
     *  @param filename The name of a Java source file.
     */
    public ShowTreePanel(String filename) {
        // Do static semantic analysis to resolve names...
        // Uncomment this to get more info:
        //    ApplicationUtility.enableTrace = debug;
        // Need to go all the way to the last pass (2) to resolve all names.
        CompileUnitNode ast = StaticResolution.load(filename, 2);
        ASTModel model = new ASTModel(ast);
        JTree tree = new JTree(model);
        tree.setPreferredSize(new Dimension(600,800));
        tree.setCellRenderer(new ASTNodeRenderer());
        tree.setScrollsOnExpand(true);

        setViewportView(tree);
    }
}
