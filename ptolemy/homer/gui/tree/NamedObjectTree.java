/*
 TODO
 
 Copyright (c) 2011 The Regents of the University of California.
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
package ptolemy.homer.gui.tree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.tree.PTree;
import ptolemy.vergil.tree.PtolemyTreeCellRenderer;

//////////////////////////////////////////////////////////////////////////
//// NamedObjectTree

/**
 * TODO
 * @author Ishwinder Singh
 * @version $Id$ 
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (ishwinde)
 * @Pt.AcceptedRating Red (ishwinde)
 */
public class NamedObjectTree extends JPanel implements TreeSelectionListener {

    /** Create a new NamedObj tree for dragging and dropping into the scene.
     */
    public NamedObjectTree() {
        setBorder(new TitledBorder(null, "Named Objects", TitledBorder.LEADING,
                TitledBorder.TOP, null, null));
        setLayout(new BorderLayout());
        setSize(500, 200);
        setPreferredSize(new Dimension(250, 10));

        _search.setToolTipText("Search the tree.");
        _search.setFocusable(true);
        _search.requestFocus();
        _search.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                ((AttributeTreeModel) _tree.getModel()).applyFilter(_search
                        .getText());
            }
        });

        _tree = new PTree(new AttributeTreeModel(null));
        _tree.setRootVisible(false);
        _tree.setShowsRootHandles(true);
        _tree.setScrollsOnExpand(true);
        _tree.addTreeSelectionListener(this);
        _tree.setCellRenderer(new PtolemyTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree,
                    Object value, boolean sel, boolean expanded, boolean leaf,
                    int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded,
                        leaf, row, hasFocus);

                // If filter is applied & no children exist.
                if ((_search.getText() != null)
                        && (_search.getText().length() > 0)) {
                    if ((!((Nameable) value).getFullName().toLowerCase()
                            .contains(_search.getText().toLowerCase()) && (tree
                            .getModel().getChildCount(value) == 0))) {
                        // Disable the item and collapse.
                        setEnabled(false);
                        //tree.collapseRow(row);
                    } else {
                        // Has children or matches criteria, expand it.
                       // tree.expandRow(row);
                    }
                } else {
                    // Collapse all rows when not filtering.
                    //tree.collapseRow(row);
                }

                setText(((Nameable) value).getName());
                return this;
            }
        });

        _selection.setEditable(false);

        add(_search, BorderLayout.NORTH);
        add(new JScrollPane(_tree), BorderLayout.CENTER);
        add(_selection, BorderLayout.SOUTH);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /* TODO
     * (non-Javadoc)
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent e) {
        Object lastSelectedPathComponent = _tree.getLastSelectedPathComponent();
        if (lastSelectedPathComponent instanceof NamedObj) {
            _selection.setText("Current Selection: "
                    + ((NamedObj) lastSelectedPathComponent).getName());
        } else {
            _selection.setText("Current Selection:");
        }
    }

    /**
     * TODO
     * @param compositeEntity
     */
    public void setCompositeEntity(CompositeEntity compositeEntity) {
        _compositeEntity = compositeEntity;
        _tree.setModel(new AttributeTreeModel(_compositeEntity));
    }

    private void _expandAll(TreePath parent) {

        //        TreeNode node = (TreeNode) parent.getLastPathComponent();
        //        if (_tree.getModel().getChildCount(parent) >= 0) {
        //            for (Enumeration e = node.children(); e.hasMoreElements();) {
        //                _expandAll(parent.pathByAddingChild((TreeNode) e.nextElement()));
        //            }
        //        }
        //
        //        _tree.expandPath(parent);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private final JTextField _search = new JTextField();
    private final PTree _tree;
    private final JTextField _selection = new JTextField("Current Selection:");
    private CompositeEntity _compositeEntity;
}
