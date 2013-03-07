/* The class responsible for display, in a tree, the model elements that
   can be added to the customized layout.

 Copyright (c) 2011-2013 The Regents of the University of California.
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
package ptolemy.homer.gui;

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

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Nameable;
import ptolemy.vergil.tree.PTree;
import ptolemy.vergil.tree.PtolemyTreeCellRenderer;

///////////////////////////////////////////////////////////////////
//// NamedObjectTree

/** A side panel panel containing tree of named objects, filtering text field and
 *  draggable label.
 *
 *  @author Ishwinder Singh
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (ishwinde)
 *  @Pt.AcceptedRating Red (ishwinde)
 */
public class NamedObjectTree extends JPanel {

    /** The data flavor of the label that is dropped onto the scene panel.
     */
    public static final DataFlavor LABEL_FLAVOR;

    /** Static block that defines the label flavor.
     */
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
                AttributeTreeModel treeModel = (AttributeTreeModel) _tree
                        .getModel();
                if ((_search.getText() != null)
                        && (_search.getText().length() > 0)) {
                    treeModel.applyFilter(_search.getText());

                    for (int i = 0; i < _tree.getRowCount(); i++) {
                        _tree.expandRow(i);
                    }
                } else {
                    for (int i = 0; i < _tree.getRowCount(); i++) {
                        treeModel.applyFilter("");
                        _tree.collapseRow(i);
                    }
                }
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

                // Set the text of the tree item.
                setText(((Nameable) value).getName());

                // If filter is applied & no children exist.
                if ((_search.getText() != null)
                        && (_search.getText().length() > 0)) {
                    if ((!((Nameable) value).getFullName().toLowerCase()
                            .contains(_search.getText().toLowerCase()))) {
                        setEnabled(false);
                    }
                }

                return this;
            }
        });

        add(_search, BorderLayout.NORTH);
        add(new JScrollPane(_tree), BorderLayout.CENTER);

        JButton button = new JButton("Label");
        button.setUI(new BasicButtonUI());
        button.setTransferHandler(new TransferHandler("text"));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JComponent c = (JComponent) e.getSource();
                TransferHandler handler = c.getTransferHandler();
                handler.exportAsDrag(c, e, TransferHandler.COPY);
            }
        });

        // Make it non-focusable.
        button.setFocusable(false);
        button.setBorder(BorderFactory.createEtchedBorder());
        button.setRolloverEnabled(true);
        add(button, BorderLayout.SOUTH);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the composite entity of the panel.
     *  This method creates new tree model for the tree.
     *  @param compositeEntity the composite entity to set.
     */
    public void setCompositeEntity(CompositeEntity compositeEntity) {
        _tree.setModel(new AttributeTreeModel(compositeEntity));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    //    /** Expand all tree nodes.
    //     *  @param parent The parent path to expand.
    //     */
    //    private void _expand(TreePath parent) {
    //        int childCount = _tree.getModel().getChildCount(parent);
    //        if (childCount == 0) {
    //            for (int i = 0; i < childCount; i++) {
    //                TreePath path = parent.pathByAddingChild(_tree.getModel()
    //                        .getChild(parent, i));
    //                _expand(path);
    //            }
    //        }
    //
    //        _tree.expandPath(parent);
    //    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The search filter text field.
     */
    private final JTextField _search = new JTextField();
    /**
     * The tree containing named objects.
     */
    private final PTree _tree;
}
