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
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.tree.TreePath;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Nameable;
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
public class NamedObjectTree extends JPanel {
    public static final DataFlavor LABEL_FLAVOR;
    static {
        try {
            LABEL_FLAVOR = new DataFlavor(
                    "application/x-java-jvm-local-objectref;class=java.lang.String");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

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

        add(_search, BorderLayout.NORTH);
        add(new JScrollPane(_tree), BorderLayout.CENTER);
        JButton button = new JButton("Label");
        button.setTransferHandler(new TransferHandler("text"));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JComponent c = (JComponent) e.getSource();
                TransferHandler handler = c.getTransferHandler();
                handler.exportAsDrag(c, e, TransferHandler.COPY);
            }
        });
        button.setUI(new BasicButtonUI());
        // Make it non-focusable.
        button.setFocusable(false);
        button.setBorder(BorderFactory.createEtchedBorder());
        button.setRolloverEnabled(true);
        add(button, BorderLayout.SOUTH);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
    private CompositeEntity _compositeEntity;
}
