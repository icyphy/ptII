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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import diva.gui.BasicFrame;

/**
 * A Visio-style tab box that uses a "window shade" metaphor instead
 * of a tabbed folder metaphor.  This class implements virtually the
 * same interface as JTabbedPane, but uses the naming convention
 * "shade" instead of "tab". <p>
 *
 * @see javax.swing.JTabbedPane
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class JShadePane extends JPanel {
    /**
     * The selected index.
     */
    private int _selectedIndex;

    /**
     * The shades in this pane.
     */
    private ArrayList _shades;

    /**
     * Construct an empty instance of the shade pane.
     */
    public JShadePane() {
        _shades = new ArrayList();
        _selectedIndex = -1;
    }

    /**
     * Adds a component represented by a title button with no icon.
     * Cover method for insertShade().
     */
    public void addShade (String title, Component shade) {
        addShade(title, null, shade);
    }

    /**
     * Adds a component represented by a title and/or icon, either
     * of which can be null. Cover method for insertShade().
     */
    public void addShade (String title, Icon icon, Component shade) {
        insertShade(title, icon, shade, null, getShadeCount());
    }

    /**
     * Adds a component and tooltip represented by a title and/or icon,
     * either of which can be null. Cover method for insertShade().
     */
    public void addShade (String title, Icon icon, Component shade, String tip) {
        insertShade(title, icon, shade, tip, getShadeCount());
    }

    /**
     * Return the index of the selected shade, or -1 if there is none.
     */
    public int getSelectedIndex () {
        return _selectedIndex;
    }

    /**
     * Return the number of shades in this pane.
     */
    public int getShadeCount () {
        return _shades.size();
    }


    /**
     * Returns the tab title at the given index.
     *
     * @see #setTitleAt
     */
    public String getTitleAt (int index) {
        return ((Shade)_shades.get(index))._button.getText();
    }

    /**
     * Returns the tab icon at the given index.
     *
     * @see #setIconAt
     */
    public Icon getIconAt (int index) {
        return ((Shade)_shades.get(index))._button.getIcon();
    }

    /**
     * Returns the tab disabled icon at the given index.
     *
     * @see #setDisabledIconAt
     */
    public Icon getDisabledIconAt (int index) {
        return ((Shade)_shades.get(index))._button.getDisabledIcon();
    }

    /**
     * Returns the tab background color at the given index.
     *
     * @see #setBackgroundAt
     */
    public Color getBackgroundAt (int index) {
        return ((Shade)_shades.get(index)).getBackground();
    }

    /**
     * Returns the tab foreground color at the given index.
     *
     * @see #setForegroundAt
     */
    public Color getForegroundAt (int index) {
        return ((Shade)_shades.get(index)).getForeground();
    }

    /**
     * Returns whether or not the tab at the given index is
     * currently enabled.
     *
     * @see #setEnabledAt
     */
    public boolean isEnabledAt (int index) {
        return ((Shade)_shades.get(index)).isEnabled();
    }

    /**
     * Returns the component at the given index.
     */
    public Component getComponentAt (int index) {
        return ((Shade)_shades.get(index))._component;
    }

    /**
     * Returns the index of the shade with the given title.
     */
    public int indexOfShade (String title) {
        for (int i = 0; i < getShadeCount(); i++) {
            if (getTitleAt(i).equals(title == null? "" : title)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Insert a shade with the given title/icon/component/tip
     * at the specified index.  Either the title or the icon can
     * be null, but not both.
     */
    public void insertShade (String title, Icon icon,
            Component component, String tip, int index) {

        Icon disabledIcon = null;
        if (icon != null && icon instanceof ImageIcon) {
            disabledIcon = new ImageIcon(
                    GrayFilter.createDisabledImage(
                            ((ImageIcon)icon).getImage()));
        }

        JButton button = new JButton(title, icon);
        final String titleHandle = title;
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int i = indexOfShade(titleHandle);
                    setSelectedIndex(i);
                }
            });
        button.setToolTipText(tip);

        //Add this shade to the data structure and also
        //create its visual representation.
        _shades.add(index, new Shade(this, button, component));

        if (_shades.size() == 1) {
            _selectedIndex = 0;
        }
        refresh();
    }

    /**
     * Refresh the layout.
     */
    protected void refresh () {
        super.removeAll();
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        for (int i = 0; i <= _selectedIndex; i++) {
            Shade p = (Shade)_shades.get(i);
            gbl.setConstraints(p._button, gbc);
            add(p._button);
        }

        if (_selectedIndex >= 0) {
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            Shade p = (Shade)_shades.get(_selectedIndex);
            gbl.setConstraints(p._component, gbc);
            add(p._component);
        }

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;

        for (int i = _selectedIndex+1; i < getShadeCount(); i++) {
            Shade p = (Shade)_shades.get(i);
            gbl.setConstraints(p._button, gbc);
            add(p._button);
        }

        validate();
        repaint();
    }


    /**
     * Remove all of the shades.
     */
    public void removeAll () {
        _shades.clear();
        setSelectedIndex(-1); //calls refresh
    }

    /**
     * Remove the shade at the given index.
     */
    public void removeShadeAt (int index) {
        _shades.remove(index);

        // If we are removing the currently selected tab AND
        // it happens to be the last tab in the bunch, then
        // select the previous tab
        int shadeCount = getShadeCount();
        if (getSelectedIndex() >= shadeCount) {
            setSelectedIndex(shadeCount - 1); //calls refresh()
        }
        else {
            refresh();
        }
    }

    /**
     * Set the background color at the given index.
     */
    public void setBackgroundAt (int index, Color background) {
        Shade p = (Shade)(_shades.get(index));
        p._button.setBackground(background);
    }

    /**
     * Set the disabled icon for the button at the given index.
     */
    public void setDisabledIconAt (int index, Icon icon) {
        Shade p = (Shade)(_shades.get(index));
        p._button.setDisabledIcon(icon);
    }

    /**
     * Set the enabledness of the shade at the given index.
     */
    public void setEnabledAt (int index, boolean enabled) {
        Shade p = (Shade)(_shades.get(index));
        p._button.setEnabled(enabled);
        //FIXME - what is the right behavior if this
        //        is the current selection?
    }


    /**
     * Set the foreground color at the given index.
     */
    public void setForegroundAt (int index, Color foreground) {
        Shade p = (Shade)(_shades.get(index));
        p._button.setForeground(foreground);
        //FIXME - what is the right behavior if this
        //        is the current selection?
    }


    /**
     * Set the disabled icon for the button at the given index.
     */
    public void setIconAt (int index, Icon icon) {
        Shade p = (Shade)(_shades.get(index));
        p._button.setIcon(icon);
    }

    /**
     * Sets the selected index for this shade pane and
     * causes the pane to refresh its display to show
     * the selected pane's component.
     */
    public void setSelectedIndex (int index) {
        if (index != _selectedIndex) {  //(index >= 0) &&
            System.out.println("SELECTING: " + _selectedIndex);
            _selectedIndex = index;
        }
        refresh(); //FIXME - shouldn't need to do this every time?
    }

    /**
     * Set the title string at the given index.
     */
    public void setTitleAt (int index, String title) {
        Shade p = (Shade)(_shades.get(index));
        p._button.setText(title);
    }

    /**
     * Borrowed from JTabbedPane.  Holds all the info about each shade.
     */
    private class Shade extends AccessibleContext
        implements Serializable, Accessible, AccessibleComponent {

        JShadePane _parent;
        JButton _button;
        Component _component;

        Shade(JShadePane parent, JButton button, Component component) {

            setAccessibleParent(_parent);
            _button = button;
            _parent = parent;
            _component = component;

            if (_component instanceof Accessible) {
                AccessibleContext ac;
                ac = ((Accessible) _component).getAccessibleContext();
                if (ac != null) {
                    ac.setAccessibleParent(this);
                }
            }
        }

        ///////////////////////////////////////////////////////
        // Accessibility support
        ///////////////////////////////////////////////////////

        public AccessibleContext getAccessibleContext() {
            return this;
        }

        public String getAccessibleName() {
            return _button.getText();
        }

        public String getAccessibleDescription() {
            return _button.getToolTipText();
        }

        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PAGE_TAB;
        }

        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states;
            states = _parent.getAccessibleContext().getAccessibleStateSet();
            states.add(AccessibleState.SELECTABLE);
            int i = _parent.indexOfShade(_button.getText());
            if (i == _parent.getSelectedIndex()) {
                states.add(AccessibleState.SELECTED);
            }
            return states;
        }

        public int getAccessibleIndexInParent() {
            return _parent.indexOfShade(_button.getText());
        }

        public int getAccessibleChildrenCount() {
            return (_component instanceof Accessible) ? 1 : 0;
        }

        public Accessible getAccessibleChild(int i) {
            return (_component instanceof Accessible) ? (Accessible)_component : null;
        }

        public Locale getLocale() {
            return _parent.getLocale();
        }

        public AccessibleComponent getAccessibleComponent() {
            return this;
        }

        public void addFocusListener(FocusListener l) {
            throw new UnsupportedOperationException("FIXME");
        }


        public Color getBackground() {
            return _button.getBackground();
        }

        public void setBackground(Color c) {
            _button.setBackground(c);
        }

        public Color getForeground() {
            return _button.getForeground();
        }

        public void setForeground(Color c) {
            _button.setForeground(c);
        }

        public Cursor getCursor() {
            return _parent.getCursor();
        }

        public void setCursor(Cursor c) {
            _parent.setCursor(c);
        }

        public Font getFont() {
            return _parent.getFont();
        }

        public void setFont(Font f) {
            _parent.setFont(f);
        }

        public FontMetrics getFontMetrics(Font f) {
            return _parent.getFontMetrics(f);
        }

        public boolean isEnabled() {
            return _button.isEnabled();
        }

        public void setEnabled(boolean b) {
            _button.setEnabled(b);
        }

        public boolean isVisible() {
            return _parent.isVisible();
        }

        public void setVisible(boolean b) {
            _parent.setVisible(b);
        }

        public boolean isShowing() {
            return _parent.isShowing();
        }

        public boolean contains(Point p) {
            Rectangle r = getBounds();
            return r.contains(p);
        }

        public Point getLocationOnScreen() {
            Point parentLocation = _parent.getLocationOnScreen();
            Point componentLocation = getLocation();
            componentLocation.translate(parentLocation.x, parentLocation.y);
            return componentLocation;
        }

        public Point getLocation() {
            Rectangle r = getBounds();
            return new Point(r.x, r.y);
        }

        public void setLocation(Point p) {
            throw new UnsupportedOperationException("FIXME");
        }

        public Rectangle getBounds() {
            return _button.getBounds();
        }

        public void setBounds(Rectangle r) {
            throw new UnsupportedOperationException("FIXME");
        }

        public Dimension getSize() {
            Rectangle r = getBounds();
            return new Dimension(r.width, r.height);
        }

        public void setSize(Dimension d) {
            throw new UnsupportedOperationException("FIXME");
        }

        public Accessible getAccessibleAt(Point p) {
            return (_component instanceof Accessible) ? (Accessible)_component : null;
        }

        public boolean isFocusTraversable() {
            return false;
        }

        public void requestFocus() {
            throw new UnsupportedOperationException("FIXME");
        }

        public void removeFocusListener(FocusListener l) {
            throw new UnsupportedOperationException("FIXME");
        }
    }

    public static void main(String argv[]) {
        final JShadePane sp = new JShadePane();
        sp.addShade("Foo", new JLabel("foo's component"));
        sp.addShade("Bar", new JLabel("bar's component"));
        sp.addShade("Baz", new JLabel("baz's component"));
        sp.addShade("Moo", new JLabel("moo's component"));

        JFrame f = new BasicFrame("Shade test");
        f.getContentPane().add("Center", sp);


        JPanel btns = new JPanel();
        f.getContentPane().add("South", btns);

        JButton b1 = new JButton("rm sel");
        b1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int i = sp.getSelectedIndex();
                    if (i >= 0) {
                        sp.removeShadeAt(i);
                    }
                }
            });
        btns.add(b1);

        JButton b2 = new JButton("rm sel-1");
        b2.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int i = sp.getSelectedIndex()-1;
                    if (i >= 0) {
                        sp.removeShadeAt(i);
                    }
                }
            });
        btns.add(b2);

        JButton b3 = new JButton("rm sel+1");
        b3.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int i = sp.getSelectedIndex() + 1;
                    if (i >= 1 && i < sp.getShadeCount()) {
                        sp.removeShadeAt(i);
                    }
                }
            });
        btns.add(b3);

        JButton b4 = new JButton("rm all");
        b4.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    sp.removeAll();
                }
            });
        btns.add(b4);

        JButton b5 = new JButton("toggle sel");
        b5.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int i = sp.getSelectedIndex();
                    if (i >= 0) {
                        boolean en = sp.isEnabledAt(i);
                        sp.setEnabledAt(i, !en);
                    }
                }
            });
        btns.add(b5);

        JButton b6 = new JButton("toggle sel-1");
        b6.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int i = sp.getSelectedIndex()-1;
                    if (i >= 0) {
                        boolean en = sp.isEnabledAt(i);
                        sp.setEnabledAt(i, !en);
                    }
                }
            });
        btns.add(b6);

        f.setSize(600,400);
        f.setVisible(true);
    }
}


