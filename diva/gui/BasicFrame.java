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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;


/**
 * BasicFrame is a very simple application context that is used
 * to display tutorial examples.  It contains a menubar with a quit
 * entry and a location for the main content pane. This kind of frame
 * doesn't understand applications, and should therefore be used only
 * for the simplest examples and tutorials.
 *
 * @author John Reekie
 * @author Nick Zamora
 * @version $Id$
 */
public class BasicFrame extends ApplicationContext {
    /** The component displaying the content
     */
    private transient JComponent _component = null;

    /** Create an instance of this Frame with the given title
     * and no main component.
     */
    public BasicFrame(String title) {
        this(title, null);
    }

    /** Create an instance of this Frame with the given title
     * and no main component.
     */
    public BasicFrame(String title, boolean show_and_size_window) {
        this(title, null, show_and_size_window);
    }

    public BasicFrame(String title, JComponent component,
            boolean show_and_size_window) {
        super();

        setTitle(title);
        setJMenuBar(new JMenuBar());

        // Create the menus
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic('F');

        // NOT a default action.
        Action action = new AbstractAction("Exit") {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            };

        setExitAction(action);

        JMenuItem itemQuit = menuFile.add(action);
        itemQuit.setMnemonic('E');
        itemQuit.setToolTipText("Exit this application");

        getJMenuBar().add(menuFile);

        // Add the content pane and make the frame visible
        if (component != null) {
            setMainComponent(component);
        }

        if (show_and_size_window) {
            setSize(600, 400);
            setVisible(true);
        }
    }

    /** Create an instance of this Frame with the given title
     * and with the given main component. The component can be
     * null if desired.
     */
    public BasicFrame(String title, JComponent component) {
        super();

        setTitle(title);
        setJMenuBar(new JMenuBar());

        // Create the menus
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic('F');

        // NOT a default action.
        Action action = new AbstractAction("Exit") {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            };

        setExitAction(action);

        JMenuItem itemQuit = menuFile.add(action);
        itemQuit.setMnemonic('E');
        itemQuit.setToolTipText("Exit this application");

        getJMenuBar().add(menuFile);

        // Add the content pane and make the frame visible
        if (component != null) {
            setMainComponent(component);
        }

        setSize(600, 400);
        setVisible(true);
    }

    /** Set the main component. If there already is one,
     * it is removed first.
     */
    public void setMainComponent(JComponent component) {
        if (_component != null) {
            getContentPane().remove(_component);
        }

        if (component != null) {
            getContentPane().add("Center", component);
        }

        this._component = component;
    }
}
