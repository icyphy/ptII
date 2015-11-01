/* A menu item factory that creates actions for firing actions

 Copyright (c) 2000-2014 The Regents of the University of California.
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
package ptolemy.vergil.toolbox;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ptolemy.kernel.util.NamedObj;
import diva.gui.toolbox.JContextMenu;

///////////////////////////////////////////////////////////////////
//// MenuActionFactory

/**
 A factory that adds a given action or set of actions
 to a context menu. If an array of actions is given to
 the constructor, then the actions will be put in a submenu
 with the specified label.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class MenuActionFactory implements MenuItemFactory {
    /** Construct a factory that adds a given action to a given context menu.
     *  @param action The action to be associated with the context menu.
     */
    public MenuActionFactory(Action action) {
        _action = action;
    }

    /** Construct a factory that adds a given group of actions
     *  to a given context menu in a submenu with the specified label.
     *  @param actions The actions to be in the submenu.
     *  @param label The label for the submenu.
     */
    public MenuActionFactory(Action[] actions, String label) {
        _actions = actions;
        _label = label;
    }

    /** Add an action to the pre-existing group of actions.
     *  @param action The action to add.
     */
    public void addAction(Action action) {
        addAction(action, null);
    }

    /** Add an action to the pre-existing group of actions.
     *  If this was constructed with the single argument, then this
     *  converts the menu action into a submenu with the specified label.
     *  @param action The action to add.
     *  @param label The label to give to the menu group.
     */
    public void addAction(Action action, String label) {
        if (_action != null) {
            // Previously, there was only one action.
            // Convert to a subaction.
            _actions = new Action[2];
            _actions[0] = _action;
            _actions[1] = action;
            _action = null;
        } else {
            // Create a new actions array.
            Action[] newActions = new Action[_actions.length + 1];
            System.arraycopy(_actions, 0, newActions, 0, _actions.length);
            newActions[_actions.length] = action;
            _actions = newActions;
        }
        if (label != null) {
            _label = label;
        }
    }

    /** Add a set of action to the pre-existing group of actions.
     *  If this was constructed with the single argument, then this
     *  converts the menu action into a submenu with the specified label.
     *  @param actions The actions to add.
     *  @param label The label to give to the menu group if it
     *   previously not a menu group.
     */
    public void addActions(Action[] actions, String label) {
        int start = 0;
        if (_action != null) {
            // Previously, there was only one action.
            // Convert to a subaction.
            _actions = new Action[actions.length + 1];
            _actions[0] = _action;
            start = 1;
            _action = null;
        } else {
            Action[] newActions = new Action[_actions.length + actions.length];
            System.arraycopy(_actions, 0, newActions, 0, _actions.length);
            start = _actions.length;
            _actions = newActions;
        }
        System.arraycopy(actions, 0, _actions, start, actions.length);
        _label = label;
    }

    /** Add a menu item listener to the list of menu item listeners.
     *
     *  @param listener The menu item listener.
     */
    public void addMenuItemListener(MenuItemListener listener) {
        if (_menuItemListeners == null) {
            _menuItemListeners = new LinkedList();
        }
        _menuItemListeners.add(new WeakReference(listener));
    }

    /** Add an item to the given context menu that will configure the
     *  parameters on the given target.
     *  @param menu The context menu to add to.
     *  @param object The object that the menu item command will operate on.
     *  @return A menu item, or null to decline to provide a menu item.
     */
    @Override
    public JMenuItem create(JContextMenu menu, NamedObj object) {
        JMenuItem menuItem;
        if (_action != null) {
            // Single action as a simple menu entry.
            menuItem = _add(menu, _action,
                    (String) _action.getValue(Action.NAME));
        } else {
            // Requested a submenu with a group of actions.
            final JMenu submenu = new JMenu(_label);
            menu.add(submenu, _label);
            for (Action _action2 : _actions) {
                _add(submenu, _action2);
            }
            menuItem = submenu;
        }
        if (_menuItemListeners != null) {
            Iterator listeners = _menuItemListeners.iterator();
            while (listeners.hasNext()) {
                WeakReference reference = (WeakReference) listeners.next();
                Object contents = reference.get();
                if (contents instanceof MenuItemListener) {
                    ((MenuItemListener) contents).menuItemCreated(menu, object,
                            menuItem);
                }
            }
        }
        return menuItem;
    }

    /** Substitute the old action with the new action, if the old action is
     *  added to this factory.
     *  @param oldAction The old action.
     *  @param newAction The new action.
     *  @return true if the old action is found and is substituted; false if the
     *  old action is not added to this factory.
     */
    public boolean substitute(Action oldAction, Action newAction) {
        if (_action != null) {
            if (_action == oldAction) {
                _action = newAction;
                return true;
            } else {
                return false;
            }
        } else {
            for (int i = 0; i < _actions.length; i++) {
                if (_actions[i] == oldAction) {
                    _actions[i] = newAction;
                    return true;
                }
            }
            return false;
        }
    }

    /** Add an action to the context menu.
     *  @param menu The context menu.
     *  @param action The action to be added to the context menu.
     *  @param tooltip The tooltip for the action.
     *  @return The added menu item.
     */
    protected JMenuItem _add(JContextMenu menu, Action action, String tooltip) {
        return menu.add(action, tooltip);
    }

    /** Add an action to the submenu.
     *  @param submenu The submenu.
     *  @param action The action to be added to the submenu.
     *  @return The added menu item.
     */
    protected JMenuItem _add(JMenu submenu, Action action) {
        return submenu.add(action);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The action that will be added to the context menu. */
    private Action _action;

    /** The group of actions that will be added in a submenu. */
    private Action[] _actions;

    /** The submenu label, if one was given. */
    private String _label;

    /** the list of menu item listeners. */
    private List _menuItemListeners;
}
