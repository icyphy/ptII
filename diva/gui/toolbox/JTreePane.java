/*
 Copyright (c) 1998-2001 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
 */
package diva.gui.toolbox;

import java.awt.Dimension;
import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import diva.gui.BasicFrame;

/**
 * In the case of a small number of panes, a JTabbedPane or a JTreePane
 * is a good way of organizing them.  However, in the case of a large
 * number of frames, both of these metaphors break down.  This class is
 * meant to organize a number of frames as elements of a tree.  Selecting an
 * element in the tree makes the pane visible.  The pane and the tree
 * both share this panel, with the tree on top and the pane on the bottom.
 * The two are separated by a movable divider.
 * Unlike tabbed pane and shade pane, this class cannot easily be indexed
 * by numbers.  Hence when interacting with objects in pane, most methods
 * take a title instead of a name.  In general, only unique titles will work
 * right.  This could be expanded to allow access by the treeModel's indexing
 * by child/number methods.
 *
 * @see javax.swing.JTabbedPane
 * @see diva.gui.toolbox.JShadePane
 * @author Steve Neuendorffer
 * @version $Id$
 */
public class JTreePane extends JSplitPane {
    /**
     * The selected title.
     */
    private String _selectedTitle;

    /**
     * The panel that is used when the component gets set to null.
     */
    private JPanel _defaultPanel;

    /**
     * The Tree.
     */
    private JTree _tree;

    /**
     * The ScrollPane around the tree.
     */
    private JScrollPane _scrollPane;

    /**
     * The preferred size of the scrollpane.  This is required because
     * the splitpane doesn't remember it's size if we change the component
     * that is in it, unless we give it a preferred size.
     */
    private Dimension _scrollPaneSize = new Dimension(200, 200);

    public JTreePane() {
        this("TreePane");
    }

    /**
     * Construct an empty instance of the Tree pane.
     */
    public JTreePane(String name) {
        super(JSplitPane.VERTICAL_SPLIT);
        _selectedTitle = "";
        _defaultPanel = new JPanel();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                new Entry(this, null, name, _defaultPanel));
        DefaultTreeModel model = new DefaultTreeModel(node);
        _tree = new JTree(model);
        TreeNode nodePath[] = node.getPath();
        TreePath path = new TreePath(nodePath);
        _tree.expandPath(path);
        _tree.getSelectionModel().setSelectionMode
            (TreeSelectionModel.SINGLE_TREE_SELECTION);
        _tree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)
                        _tree.getLastSelectedPathComponent();

                    if (treeNode == null) return;

                    Entry entry = (Entry)treeNode.getUserObject();
                    setSelectedTitle(entry._title);
                    refresh();
                }
            });

        _scrollPane = new JScrollPane(_tree);
        setTopComponent(_scrollPane);
        setBottomComponent(_defaultPanel);

        setContinuousLayout(true);
        _scrollPane.setPreferredSize(_scrollPaneSize);
        refresh();
        //setSplitPosition(.5);
    }

    /**
     * Adds a component represented by a title button with no icon.
     * Cover method for insertEntry().
     */
    public void addEntry (String parent, String title, JComponent entry) {
        addEntry(parent, title, null, entry);
    }

    /**
     * Adds a component represented by a title and/or icon, either
     * of which can be null. Cover method for insertEntry().
     */
    public void addEntry (String parent, String title, Icon icon,
            JComponent entry) {
        insertEntry(parent, title, icon, entry, null);
    }

    /**
     * Adds a component and tooltip represented by a title and/or icon,
     * either of which can be null. Cover method for insertEntry().
     */
    public void addEntry (String parent, String title, Icon icon,
            JComponent entry, String tip) {
        insertEntry(parent, title, icon, entry, tip);
    }

    /**
     * Return the selectedString.
     */
    public String getSelectedTitle () {
        return _selectedTitle;
    }

    /**
     * Returns the tab icon at the given index.
     *
     * @see #setIconAt
     */
    public Icon getIconAt (String title) {
        return (_findEntry(title))._icon;
    }

    /**
     * Returns the tab disabled icon at the given index.
     *
     * @see #setDisabledIconAt
     */
    public Icon getDisabledIconAt (String title) {
        return (_findEntry(title))._icon;
    }

    /**
     * Returns the tree object.
     */
    public JTree getTree () {
        return _tree;
    }

    /**
     * Returns the split pane object.
     */
    public JSplitPane getSplitPane () {
        return this;
    }

    /**
     * Returns whether or not the tab at the given index is
     * currently enabled.
     *
     * @see #setEnabledAt
     */
    public boolean isEnabledAt (String title) {
        return (_findEntry(title))._enabled;
    }

    /**
     * Returns the component at the given index.
     */
    public JComponent getComponentAt (String title) {
        return (_findEntry(title))._component;
    }

    /**
     * Insert a entry with the given title/icon/component/tip
     * at the specified index.  Either the icon can
     * be null, but the title must be specified.
     */
    public void insertEntry (String parent, String title, Icon icon,
            JComponent component, String tip) {

        /*
          Icon disabledIcon = null;
          if (icon != null && icon instanceof ImageIcon) {
          disabledIcon = new ImageIcon(
          GrayFilter.createDisabledImage(
          ((ImageIcon)icon).getImage()));
          }
        */

        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode();
        newNode.setUserObject(new Entry(this, icon, title, component));
        DefaultMutableTreeNode parentNode = _findNode(parent);
        if (parentNode == null) {
            System.out.println("parent == null");
            throw new RuntimeException("Parent not found!");
        }
        DefaultTreeModel model = (DefaultTreeModel)_tree.getModel();
        model.insertNodeInto(newNode, parentNode, 0);
        refresh();
    }

    /**
     * Refresh the layout.
     */
    protected void refresh () {
        _scrollPane.getSize(_scrollPaneSize);
        _scrollPane.setPreferredSize(_scrollPaneSize);
        System.out.println("title = " + _selectedTitle + " size = " + _scrollPaneSize);
        Entry entry = _findEntry(_selectedTitle);
        if (entry == null || entry._component == null) {
            setBottomComponent(_defaultPanel);
        } else {
            setBottomComponent(entry._component);
            if (entry._component != null) {
                entry._component.validate();
            }
        }
        validate();
        repaint();
    }


    /**
     * Remove all of the entrys.
     */
    public void removeAll () {
        DefaultTreeModel model = (DefaultTreeModel)_tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
        root.removeAllChildren();
        refresh();
    }

    /**
     * Remove the entry at the given index.
     */
    public void removeEntry (String title) {
        Entry entry = _findEntry(title);

        refresh();

    }

    /**
     * Set the disabled icon for the button at the given index.
     */
    public void setDisabledIconAt (String title, Icon icon) {
        Entry p = _findEntry(title);
        //        p._button.setDisabledIcon(icon);
    }

    /**
     * Set the enabledness of the entry at the given index.
     */
    public void setEnabledAt (String title, boolean enabled) {
        Entry p = _findEntry(title);
        p._enabled = enabled;
    }

    /**
     * Set the disabled icon for the button at the given index.
     */
    public void setIconAt (String title, Icon icon) {
        Entry p = _findEntry(title);
        p._icon = icon;
    }

    /**
     * Sets the selected index for this entry pane and
     * causes the pane to refresh its display to show
     * the selected pane's component.
     *
     * @see SingleSelectionModel#setSelectedIndex
     * @beaninfo
     *   preferred: true
     * description: The entrypane's selected tab index.
     */
    public void setSelectedTitle (String title) {
        if (title != _selectedTitle) {  //(index >= 0) &&
            _selectedTitle = title;
            System.out.println("SELECTING: " + _selectedTitle);
            DefaultMutableTreeNode node = _findNode(title);
            TreeNode nodePath[] = node.getPath();
            TreePath path = new TreePath(nodePath);
            _tree.setSelectionPath(path);
            refresh();
        }
    }

    /**
     * Set the title string at the given index.
     */
    public void setTitleAt (String title, String newTitle) {
        Entry p = _findEntry(title);
        p._title = newTitle;
    }

    /** Return the entry with the given title, or null if no Entry is found
     * with the given title.
     */
    private Entry _findEntry(String title) {
        DefaultMutableTreeNode node = _findNode(title);
        if (node == null)
            return null;
        else
            return (Entry)(_findNode(title).getUserObject());
    }

    /** Return the node with the given title. If the title is null, then
     *  return the root node.  If the node is not found, then return null.
     */
    private DefaultMutableTreeNode _findNode(String title) {
        DefaultTreeModel model = (DefaultTreeModel)_tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        if (title == null)
            return root;
        Enumeration nodes = root.preorderEnumeration();
        while (nodes.hasMoreElements()) {
            DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) nodes.nextElement();
            Entry entry = (Entry)node.getUserObject();
            if ((entry != null)&&(entry._title.equals(title)))
                return node;
        }
        return null;
    }

    /**
     * Holds all the info about each entry.
     */
    private class Entry extends Object {

        JTreePane _parent;
        Icon _icon;
        String _title;
        JComponent _component;
        boolean _enabled;

        Entry(JTreePane parent, Icon icon, String title, JComponent component) {
            _icon = icon;
            _title = title;
            _parent = parent;
            _component = component;
            _enabled = true;
        }

        public String toString() {
            return _title;
        }
    }

    public static void main(String argv[]) {
        final JTreePane sp = new JTreePane();
        sp.addEntry(null, "Foo", new JLabel("foo's component"));
        sp.addEntry(null, "Bar", new JLabel("bar's component"));
        sp.addEntry(null, "Baz", new JLabel("baz's component"));
        sp.addEntry(null, "Moo", new JLabel("moo's component"));

        JFrame f = new BasicFrame("Entry test");
        f.getContentPane().add("Center", sp);

        f.setSize(600,400);
        f.setVisible(true);
    }
}


