/*
 Copyright (c) 1998-2014 The Regents of the University of California
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

import java.awt.event.InputEvent;

import javax.swing.JPopupMenu;

import diva.canvas.Figure;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.AbstractInteractor;

/**
 * This interactor creates a menu when it is activated.  By default, this
 * interactor is associated with the right mouse button.  This class is
 * commonly used to create context sensitive menus for figures in a canvas.
 *
 *
 * @author Stephen Neuendorffer
 * @version $Id$
 */
public class MenuCreator extends AbstractInteractor {
    /** The menu factory.
     */
    MenuFactory _factory;

    /** Return the menu factory.
     */
    public MenuFactory getMenuFactory() {
        return _factory;
    }

    /**
     * Construct a new interactor with a right button mouse filter.
     * Set the menu factory to the given factory.
     */
    public MenuCreator(MenuFactory factory) {
        setMenuFactory(factory);
        setMouseFilter(new MouseFilter(InputEvent.BUTTON3_MASK));
    }

    /**
     * When a mouse press happens, ask the factory to create a menu and show
     * it on the screen.  Consume the mouse event.  If the factory is set to
     * null, then ignore the event and do not consume it.
     */
    @Override
    public void mousePressed(LayerEvent e) {
        _doEvent(e);
    }

    /**
     * When a mouse press happens, ask the factory to create a menu and show
     * it on the screen.  Consume the mouse event.  If the factory is set to
     * null, then ignore the event and do not consume it.
     */
    @Override
    public void mouseReleased(LayerEvent e) {
        _doEvent(e);
    }

    /** Set the menu factory.
     */
    public void setMenuFactory(MenuFactory factory) {
        _factory = factory;
    }

    /** Process a mousePressed or mouseReleased event.
     */
    private void _doEvent(LayerEvent e) {
        if (_factory != null && e.isPopupTrigger()) {
            Figure source = e.getFigureSource();
            JPopupMenu menu = _factory.create(source);

            if (menu == null) {
                return;
            }

            menu.show(e.getComponent(), e.getX(), e.getY());
            e.consume();
        }
    }
}
