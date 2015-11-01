/* An abstract menu factory that creates context menus using item factories

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.vergil.tree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.TreePath;

import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.toolbox.MenuItemFactory;
import diva.gui.toolbox.JContextMenu;

///////////////////////////////////////////////////////////////////
//// PTreeMenuCreator

/**
 A mouse listener that creates context menus for a PTree using menu
 item factories.  When asked to create a context menu, This class
 determines the ptolemy object associated with the point in the tree
 that was clicked on.  Then it passes the ptolemy object to each menu
 item factory that it contains to add the menu items.  Lastly, it pops
 up the resulting menu.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class PTreeMenuCreator extends MouseAdapter {
    /** Create a new menu factory that contains no menu item factories.
     */
    public PTreeMenuCreator() {
        _factoryList = new LinkedList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a menu item factory to this creator.
     *  @param factory The menu item factory to add.
     */
    public void addMenuItemFactory(MenuItemFactory factory) {
        _factoryList.add(factory);
    }

    /** Create an instance of the menu associated with this factory.
     *  @param e The mouse event.  If the mouse event is
     *  a popup event, then menu is created and shown.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        _doEvent(e);
    }

    /** Create an instance of the menu associated with this factory.
     *  @param e The mouse event.  If the mouse event is
     *  a popup event, then menu is created and shown.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        _doEvent(e);
    }

    /** Return the list of menu item factories.
     * @return An unmodifiable list.
     */
    public List menuItemFactoryList() {
        return Collections.unmodifiableList(_factoryList);
    }

    /** Remove the given menu item factory from the factory list.
     *  @param factory The factory to be removed.
     */
    public void removeMenuItemFactory(MenuItemFactory factory) {
        _factoryList.remove(factory);
    }

    /** Remove all MenuItemFactories from the factory list.
     */
    public void clear() {
        _factoryList.clear();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Process a mousePressed or mouseReleased event.
     */
    private void _doEvent(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }

        PTree tree = (PTree) e.getComponent();
        TreePath treePath = tree.getPathForLocation(e.getX(), e.getY());
        if (treePath == null) {
            // On the Mac under Java 1.5.0_22, treePath can be null.
            // 1) Start vergil
            // 2) Open Actors -> Sources -> GenericSources
            // 3) Drag a Const into the canvas
            // 4) Place the mouse over the Const actor and quickly
            // use two finger to signal a right click.

            // An exception appears:
            // Exception in thread "AWT-EventQueue-0" java.lang.NullPointerException
            //         at ptolemy.vergil.tree.PTreeMenuCreator._doEvent(PTreeMenuCreator.java:119)
            //         at ptolemy.vergil.tree.PTreeMenuCreator.mousePressed(PTreeMenuCreator.java:82)
            //         at java.awt.AWTEventMulticaster.mousePressed(AWTEventMulticaster.java:222)
            //         at java.awt.Component.processMouseEvent(Component.java:5599)
            //         at javax.swing.JComponent.processMouseEvent(JComponent.java:3129)
            //         at java.awt.Component.processEvent(Component.java:5367)
            //         at java.awt.Container.processEvent(Container.java:2010)
            //         at java.awt.Component.dispatchEventImpl(Component.java:4068)
            //         at java.awt.Container.dispatchEventImpl(Container.java:2068)
            //         at java.awt.Component.dispatchEvent(Component.java:3903)
            //         at java.awt.LightweightDispatcher.retargetMouseEvent(Container.java:4256)
            //         at java.awt.LightweightDispatcher.processMouseEvent(Container.java:3933)
            //         at java.awt.LightweightDispatcher.dispatchEvent(Container.java:3866)
            //         at java.awt.Container.dispatchEventImpl(Container.java:2054)
            //         at java.awt.Window.dispatchEventImpl(Window.java:1801)
            //         at java.awt.Component.dispatchEvent(Component.java:3903)
            //         at java.awt.EventQueue.dispatchEvent(EventQueue.java:463)
            //         at java.awt.EventDispatchThread.pumpOneEventForHierarchy(EventDispatchThread.java:269)
            //         at java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:190)
            //         at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:184)
            //         at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:176)
            //         at java.awt.EventDispatchThread.run(EventDispatchThread.java:110)

            // I think the problem here is that the actor pane has the focus when
            // the mouse click occurs.
            return;
        }
        Object object = treePath.getLastPathComponent();

        if (object instanceof NamedObj) {
            NamedObj namedObj = (NamedObj) object;
            JContextMenu menu = new JContextMenu(namedObj,
                    namedObj.getFullName());
            Iterator i = menuItemFactoryList().iterator();

            while (i.hasNext()) {
                MenuItemFactory factory = (MenuItemFactory) i.next();
                factory.create(menu, namedObj);
            }

            menu.show(tree, e.getX(), e.getY());
            e.consume();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private members                       ////

    /** The menu item factories.
     */
    private List _factoryList;
}
