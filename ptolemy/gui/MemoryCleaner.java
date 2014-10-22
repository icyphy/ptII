/* Methods that clean up possible memory leaks caused by listeners.

 Copyright (c) 2011-2014 The Regents of the University of California.
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
package ptolemy.gui;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

/**
 * Methods that clean up possible memory leaks caused by listeners.
 * Experimental methods will be marked so in their javadoc
 * comments until they are proven to be effective.
 *
 * The static methods of this class can be used to remove listeners
 * when disposing of Windows.  Swing, for various reasons, does not
 * know when a window has been disposed and thus never garbage
 * collects the listeners.  These listeners each have a reference
 * to the window which means the window (and everything it references)
 * never gets garbage collected.
 *
 * @author Aaron Schultz
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class MemoryCleaner {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Remove ActionListeners from a JMenuBar.
     *
     * @param menubar The menubar from which the ActionListeners
     * are to be removed.
     * @return The number of listeners removed.
     */
    public static int removeActionListeners(JMenuBar menubar) {
        int listenersRemoved = 0;
        if (menubar != null) {
            int count = menubar.getMenuCount();
            if (_isDebugging) {
                System.out.println("menu count: " + count);
            }

            for (int m = 0; m < count; m++) {
                JMenu menu = menubar.getMenu(m);

                int removed = removeActionListeners(menu);
                listenersRemoved += removed;
            }
        }
        return listenersRemoved;
    }

    /**
     * Remove ActionListeners from an AbstractButton (such as a JMenuItem.
     * If the AbstractButton is a JMenu, the ActionListeners of all the
     * child items get removed too.
     *
     * @param button The button from which the ActionListeners
     * are to be removed.
     * @return The number of listeners removed.
     */
    public static int removeActionListeners(AbstractButton button) {
        int listenersRemoved = 0;
        if (button != null) {
            ActionListener[] listeners = button.getActionListeners();
            if (listeners != null) {
                int count = listeners.length;
                for (ActionListener listener : listeners) {
                    button.removeActionListener(listener);
                }
                int countAfter = button.getActionListeners().length;
                listenersRemoved += count - countAfter;
                //System.out.println("Removed "+listenersRemoved+" for "+button.getText());
                if (countAfter != 0) {
                    //System.out.println("cOUNTaFTERrEMOVE: "+countAfter);
                }
            }

            if (button instanceof JMenu) {
                JMenu jmenu = (JMenu) button;
                int totalMenuItems = jmenu.getMenuComponentCount();
                for (int n = 0; n < totalMenuItems; n++) {
                    Component component = jmenu.getMenuComponent(n);
                    if (component instanceof AbstractButton) {
                        AbstractButton b = (AbstractButton) component;
                        listenersRemoved += removeActionListeners(b);
                    }
                }
            }
        }
        return listenersRemoved;
    }

    /**
     * Remove WindowListeners from a Window.
     * Experimental.
     *
     * @param window  The Window from which WindowListeners
     * are to be removed.
     * @return The number of listeners removed.
     */
    public static int removeWindowListeners(Window window) {
        int listenersRemoved = 0;
        if (window != null) {
            WindowListener[] listeners = window.getWindowListeners();
            int count = listeners.length;
            for (WindowListener listener : listeners) {
                window.removeWindowListener(listener);
            }
            int countAfter = window.getWindowListeners().length;
            listenersRemoved = count - countAfter;
        }
        return listenersRemoved;
    }

    /**
     * Remove ActionListeners from a JToolBar.
     *
     * @param toolbar The tool bar from which the ActionListeners
     * are to be removed.
     * @return The number of listeners removed.
     */
    public static int removeActionListeners(JToolBar toolbar) {
        int listenersRemoved = 0;
        if (toolbar != null) {
            int count = toolbar.getComponentCount();
            if (_isDebugging) {
                System.out.println("component count: " + count);
            }

            for (int m = 0; m < count; m++) {
                Component c = toolbar.getComponentAtIndex(m);
                if (c instanceof JButton) {
                    JButton b = (JButton) c;
                    b.setAction(null);
                    listenersRemoved++;
                } else {
                    if (_isDebugging) {
                        System.out.println((c == null ? "null" : c.getClass()
                                .getName()));
                    }
                }
            }
        }
        return listenersRemoved;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static boolean _isDebugging = false;
}
