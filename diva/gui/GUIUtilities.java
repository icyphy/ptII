/*
  Copyright (c) 1998-2005 The Regents of the University of California
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
package diva.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;


/**
 * A collection of utilities for the GUI.
 *
 * @author John Reekie
 * @author Steve Neuendorffer
 * @version $Id$
 */
public class GUIUtilities {
    /** JDK1.2 doesn't have this string defined in javax.swing.Action.
     *  This is the value that JDK1.3 uses.
     */
    public static final String ACCELERATOR_KEY = "AcceleratorKey";

    /** JDK1.2 doesn't have this string defined in javax.swing.Action.
     *  This is the value that JDK1.3 uses.
     */
    public static final String MNEMONIC_KEY = "MnemonicKey";

    /** This key is used in an action to specify an icon used in toolbars.
     */
    public static final String LARGE_ICON = "LargeIcon";

    /** Add a quick keystroke on the given pane for the given action.
     *  The keystroke that is added is given in the ACCELERATOR_KEY
     *  property that has been set in the action.  If the ACCELERATOR_KEY
     *  property has not been set, then do not add a hotkey.
     */
    public static void addHotKey(JComponent pane, Action action) {
        addHotKey(pane, action, null);
    }

    /** Add a quick keystroke on the given pane for the given action.
     *  If the given keystroke is null, then use the ACCELERATOR_KEY
     *  property that has been set in the action.  If the given keystroke
     *  is null, Otherwise, set the
     *  ACCELERATOR_KEY property to the given key stroke.
     */
    public static void addHotKey(JComponent pane, Action action, KeyStroke key) {
        String name = (String) action.getValue(Action.NAME);

        if (key == null) {
            key = (KeyStroke) action.getValue(ACCELERATOR_KEY);
        } else {
            action.putValue(ACCELERATOR_KEY, key);
        }

        if (key != null) {
            pane.registerKeyboardAction(action, name, key,
                    JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
    }

    /** Add an action to a menu and return the menu item created.  If
     * the tool tip is null, use the "tooltip" property already in the
     * action, otherwise add the property to the action. (The mnemonic
     * isn't added.)  The new menu item is added to the action as the
     * "menuItem" property.  The menu item's text is set using the
     * action's name, concatenated with a description of a keyboard
     * accelerator, if one has been set previously on the action.
     * The item will be enabled by default.
     */
    public static JMenuItem addMenuItem(JMenu menu, Action action) {
        String label = (String) action.getValue(Action.NAME);
        int mnemonic = 0;
        Integer i = (Integer) action.getValue(MNEMONIC_KEY);

        if (i != null) {
            mnemonic = i.intValue();
        }

        return addMenuItem(menu, label, action, mnemonic, null, true);
    }

    /** Add an action to a menu and return the menu item created.  If
     * the tool tip is null, use the "tooltip" property already in the
     * action, otherwise add the property to the action. (The mnemonic
     * isn't added.)  The new menu item is added to the action as the
     * "menuItem" property.  The menu item's text is set using the
     * action's name, concatenated with a description of a keyboard
     * accelerator, if one has been set previously on the action.
     * The item will be enabled by default.
     */
    public static JMenuItem addMenuItem(JMenu menu, Action action,
            int mnemonic, String tooltip) {
        String label = (String) action.getValue(Action.NAME);
        return addMenuItem(menu, label.toString(), action, mnemonic, tooltip,
                true);
    }

    /** Add an action to a menu and return the menu item created.  If
     * the tool tip is null, use the "tooltip" property already in the
     * action, otherwise add the property to the action. (The mnemonic
     * isn't added.)  The new menu item is added to the action as the
     * "menuItem" property.  The menu item's text is set to be "label",
     * and is disabled or enabled according to "isEnabled."
     */
    public static JMenuItem addMenuItem(JMenu menu, String label,
            Action action, int mnemonic, String tooltip, boolean isEnabled) {
        if (tooltip == null) {
            tooltip = (String) action.getValue("tooltip");
        } else {
            action.putValue("tooltip", tooltip);
        }

        action.putValue("tooltip", tooltip);

        JMenuItem item = menu.add(action);
        item.setText(label);
        item.setEnabled(isEnabled);
        item.setMnemonic(mnemonic);
        item.setToolTipText(tooltip);

        KeyStroke key = (KeyStroke) action.getValue(ACCELERATOR_KEY);
        item.setAccelerator(key);
        action.putValue("menuItem", item);
        return item;
    }

    /** Add the action to the given toolbar.
     *  If the LARGE_ICON property is specified in the Action, then use it
     *  for the button.  We use this instead of SMALL_ICON, because
     *  SMALL_ICON shows up when the action is added to a menu, and in
     *  most cases we don't actually want an icon there.
     *  If no icon is specified, then the button will just
     *  have the name of the action.  If the "tooltip" property is specified
     *  in the action, then create a tooltip for the button with the string.
     *  The new button is added to the action as the "toolButton" property.
     *  The button is enabled by default.
     */
    public static JButton addToolBarButton(JToolBar toolbar, Action action) {
        Icon icon = (Icon) action.getValue(LARGE_ICON);
        String label = null;

        if (icon == null) {
            label = (String) action.getValue(Action.NAME);
        }

        return addToolBarButton(toolbar, action, null, icon, label, true);
    }

    /** Add an action to the toolbar.  If the tool tip is null, use
     * the "tooltip" property already in the action, otherwise add the
     * property to the action. The new button is added to the action
     * as the "toolButton" property.  The button represented by an icon
     * (no text) and is enabled by default.
     */
    public static JButton addToolBarButton(JToolBar toolbar, Action action,
            String tooltip, Icon icon) {
        return addToolBarButton(toolbar, action, tooltip, icon, null, true);
    }

    /** Add an action to the toolbar.  If the tool tip is null, use
     * the "tooltip" property already in the action, otherwise add the
     * property to the action. The new button is added to the action
     * as the "toolButton" property.  The button represented by an icon
     * (no text) and is enabled by default.
     */
    public static JButton addToolBarButton(JToolBar toolbar, Action action,
            String tooltip, Icon icon, boolean isEnabled) {
        return addToolBarButton(toolbar, action, tooltip, icon, null, isEnabled);
    }

    /** Add an action to the toolbar.  If the tool tip is null, use
     * the "tooltip" property already in the action, otherwise add the
     * property to the action. The new button is added to the action
     * as the "toolButton" property.  The button represented by text
     * (no icon) and is enabled by default.
     */
    public static JButton addToolBarButton(JToolBar toolbar, Action action,
            String tooltip, String lbl) {
        return addToolBarButton(toolbar, action, tooltip, null, lbl, true);
    }

    /** Add an action to the toolbar.  If either an icon or a text string
     * are specified (non-null), they are added.  The button is enabled by
     * default.
     */
    public static JButton addToolBarButton(JToolBar toolbar, Action action,
            String tooltip, Icon icon, String lbl) {
        return addToolBarButton(toolbar, action, tooltip, icon, lbl, true);
    }

    /** Add an action to the toolbar.  If the tool tip is null, use
     * the "tooltip" property already in the action, otherwise add the
     * property to the action.  The new button is added to the action
     * as the "toolButton" property.  If either an icon or a text string
     * are specified (non-null), they are added.
     */
    public static JButton addToolBarButton(JToolBar toolbar, Action action,
            String tooltip, Icon icon, String lbl, boolean isEnabled) {
        if (tooltip == null) {
            tooltip = (String) action.getValue("tooltip");
        } else {
            action.putValue("tooltip", tooltip);
        }

        JButton button = toolbar.add(action);
        button.setToolTipText(tooltip);
        button.setText(null);
        button.setRequestFocusEnabled(false);

        if (icon != null) {
            button.setIcon(icon);
        }

        if (lbl != null) {
            button.setText(lbl);
        }

        button.setMargin(new Insets(0, 0, 0, 0));

        //        button.setBorderPainted(false);
        button.setBorderPainted(true);
        button.setEnabled(isEnabled);
        action.putValue("toolBarButton", button);
        return button;
    }

    /**
     * Return a string that contains the original string, limited to the
     * given number of characters.  If the string is truncated, ellipses
     * will be appended to the end of the string
     */
    public static String ellipsis(String string, int length) {
        if (string.length() > length) {
            return string.substring(0, length - 3) + "...";
        }

        return string;
    }

    /** Get the extension of a file. Return a null string is there
     * is no extension.
     */
    public static String getFileExtension(File file) {
        String str = file.getName();
        int i = str.lastIndexOf('.');

        if (i > 0) {
            return str.substring(i + 1);
        } else {
            return "";
        }
    }

    /** Return a good string representation of the given keystroke, since
     *  the toString method returns more garbage than we want to see in a
     *  user interface.
     */
    public static String keyStrokeToString(KeyStroke key) {
        int modifiers = key.getModifiers();
        StringBuffer buffer = new StringBuffer();

        if ((modifiers & Event.SHIFT_MASK) == Event.SHIFT_MASK) {
            buffer.append("(Shift-");
            buffer.append(KeyEvent.getKeyText(key.getKeyCode()));
            buffer.append(")");
        }

        if ((modifiers & Event.CTRL_MASK) == Event.CTRL_MASK) {
            buffer.append("(Ctrl-");
            buffer.append(KeyEvent.getKeyText(key.getKeyCode()));
            buffer.append(")");
        }

        if ((modifiers & Event.META_MASK) == Event.META_MASK) {
            buffer.append("(Meta-");
            buffer.append(KeyEvent.getKeyText(key.getKeyCode()));
            buffer.append(")");
        }

        if ((modifiers & Event.ALT_MASK) == Event.ALT_MASK) {
            buffer.append("(Alt-");
            buffer.append(KeyEvent.getKeyText(key.getKeyCode()));
            buffer.append(")");
        }

        if (modifiers == 0) {
            buffer.append("(");
            buffer.append(KeyEvent.getKeyText(key.getKeyCode()));
            buffer.append(")");
        }

        return buffer.toString();
    }

    /** Display an exception in a nice user-oriented way.  Instead of
     *  displaying the whole stack trace, just display the exception message
     *  and a button for displaying the whole stack trace.
     */
    public static void showException(Component parent, Exception e, String info) {
        Object[] message = new Object[1];
        String string;

        if (info != null) {
            string = info + "\n" + e.getMessage();
        } else {
            string = e.getMessage();
        }

        message[0] = ellipsis(string, 400);

        Object[] options = {
            "Dismiss",
            "Display Stack Trace"
        };

        // Show the MODAL dialog
        int selected = JOptionPane.showOptionDialog(parent, message,
                "Exception Caught", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE, null, options, options[0]);

        if (selected == 1) {
            showStackTrace(parent, e, info);
        }
    }

    /** Display a stack trace dialog. Eventually, the dialog should
     * be able to email us a bug report.
     */
    public static void showStackTrace(Component parent, Exception e) {
        showStackTrace(parent, e, null);
    }

    /** Display a stack trace dialog. Eventually, the dialog should
     * be able to email us a bug report. The "info" argument is a
     * string printed at the top of the dialog instead of the Exception
     * message.
     */
    public static void showStackTrace(Component parent, Exception e, String info) {
        // Show the stack trace in a scrollable text area.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        JTextArea text = new JTextArea(sw.toString(), 60, 80);
        JScrollPane stext = new JScrollPane(text);
        stext.setPreferredSize(new Dimension(400, 200));
        text.setCaretPosition(0);
        text.setEditable(false);

        // We want to stack the text area with another message
        Object[] message = new Object[2];
        String string;

        if (info != null) {
            string = info + "\n" + e.getMessage();
        } else {
            string = e.getMessage();
        }

        message[0] = ellipsis(string, 400);
        message[1] = stext;

        // Show the MODAL dialog
        JOptionPane.showMessageDialog(parent, message, "Exception Caught",
                JOptionPane.WARNING_MESSAGE);
    }
}
