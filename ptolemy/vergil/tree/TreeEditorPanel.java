/* A panel that shows the elements of a Ptolemy II model in a JTree.

Copyright (c) 1998-2003 The Regents of the University of California.
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

package ptolemy.vergil.tree;

import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// TreeEditorPanel
/**
A panel that displays a Ptolemy II model in a JTree.

@author Edward Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class TreeEditorPanel extends JScrollPane {

    /** Construct a display of the Ptolemy II model given by the
     *  specified MoML file.
     *  @param filename The name of a MoML file.
     *  @exception Exception If the parser cannot parse the file.
     */
    public TreeEditorPanel(String filename) throws Exception {
        this(filename, null);
    }

    /** Construct a display of the Ptolemy II model given by the
     *  specified MoML file.
     *  @param filename The name of a MoML file.
     *  @param cellRenderer The renderer for nodes of the tree, or null
     *   to use the default.
     *  @exception Exception If the parser cannot parse the file.
     */
    public TreeEditorPanel(String filename, TreeCellRenderer cellRenderer)
            throws Exception {
        _parser = new MoMLParser();
        // FIXME: This should use the Configuration.
        _toplevel = _parser.parseFile(filename);
        if (_toplevel instanceof CompositeEntity) {
            FullTreeModel model
                = new FullTreeModel((CompositeEntity)_toplevel);
            JTree tree = new JTree(model);
            tree.setPreferredSize(new Dimension(600,800));
            if (cellRenderer == null) {
                cellRenderer = new PtolemyTreeCellRenderer();
            }
            tree.setCellRenderer(cellRenderer);
            tree.setScrollsOnExpand(true);

            setViewportView(tree);
        } else {
            throw new IllegalActionException(
                    "Cannot display a tree unless "
                    + "the top level is a CompositeEntity.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The parser being used for this model. */
    private MoMLParser _parser;

    /** The top-level entity of the model. */
    private NamedObj _toplevel;
}
