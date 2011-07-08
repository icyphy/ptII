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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;

import ptolemy.homer.gui.UIDesignerFrame;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
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

    /**
     * TODO
     */
    public NamedObjectTree() {
        setLayout(new BorderLayout());
        _tree = new PTree(new AttributeTreeModel(null));
        _tree.setShowsRootHandles(true);
        _tree.setRootVisible(false);
        _tree.addTreeSelectionListener(this);
        _tree.setScrollsOnExpand(true);
        _tree.setCellRenderer(new PtolemyTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree,
                    Object value, boolean sel, boolean expanded, boolean leaf,
                    int row, boolean hasFocus) {
                // TODO Auto-generated method stub
                DefaultTreeCellRenderer component = (DefaultTreeCellRenderer) super
                        .getTreeCellRendererComponent(tree, value, sel,
                                expanded, leaf, row, hasFocus);
                if (value instanceof Settable && value instanceof Nameable) {
                    component.setText(((Nameable) value).getName());
                }
                return component;
            }
        });
        add(new JScrollPane(_tree), BorderLayout.CENTER);
        _currentSelectionField = new JTextField("Current Selection:");
        _currentSelectionField.setEditable(false);
        add(_currentSelectionField, BorderLayout.SOUTH);
        setSize(500, 200);
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
            _currentSelectionField.setText("Current Selection: "
                    + ((NamedObj) lastSelectedPathComponent).getName());
        } else {
            _currentSelectionField.setText("Current Selection:");
        }
    }

    /**
     * TODO
     * @param topLevelContainer
     */
    public void setCompositeEntity(CompositeEntity topLevelContainer) {
        _tree.setModel(new AttributeTreeModel(topLevelContainer));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private final PTree _tree;
    private final JTextField _currentSelectionField;
    private UIDesignerFrame _mainFrame;
}
