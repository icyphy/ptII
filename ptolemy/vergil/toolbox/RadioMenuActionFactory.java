/* A radio menu item factory that creates actions for firing actions.

 Copyright (c) 2008-2014 The Regents of the University of California.
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

import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import ptolemy.kernel.util.NamedObj;
import diva.gui.toolbox.JContextMenu;

//////////////////////////////////////////////////////////////////////////
//// RadioMenuActionFactory

/**
 A factory that adds a given action or set of actions as radio selections
 to a context menu. All the actions created by an instance of this factory are
 in the same group. Selecting one item in a group causes the currently selected
 item in the same group unselected, if it is not the same as the newly selected
 one. If an array of actions is given to the constructor, then the actions will
 be put in a submenu with the specified label.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class RadioMenuActionFactory extends MenuActionFactory {

    /** Construct a factory that adds a given action as a radio selection to a
     *  given context menu.
     *  @param action The action to be associated with the context menu.
     */
    public RadioMenuActionFactory(Action action) {
        super(action);
    }

    /** Construct a factory that adds a given group of actions as radio
     *  selections to a given context menu in a submenu with the specified
     *  label.
     *  @param actions The actions to be in the submenu.
     *  @param label The label for the submenu.
     */
    public RadioMenuActionFactory(Action[] actions, String label) {
        super(actions, label);
    }

    /** Add an item to the given context menu that will configure the
     *  parameters on the given target.
     *  @param menu The context menu to add to.
     *  @param object The object that the menu item command will operate on.
     *  @return A menu item, or null to decline to provide a menu item.
     */
    @Override
    public JMenuItem create(JContextMenu menu, NamedObj object) {
        int selected = -1;
        if (_group != null) {
            Enumeration<AbstractButton> elements = _group.getElements();
            int i = 0;
            while (elements.hasMoreElements()) {
                AbstractButton button = elements.nextElement();
                if (button.isSelected()) {
                    selected = i;
                }
                i++;
            }
        }
        _group = new ButtonGroup();
        JMenuItem item = super.create(menu, object);

        Enumeration<AbstractButton> elements = _group.getElements();
        int i = 0;
        while (elements.hasMoreElements()) {
            AbstractButton button = elements.nextElement();
            if (i >= selected) {
                button.setSelected(true);
                break;
            }
            i++;
        }
        return item;
    }

    /** Add an action to the context menu.
     *  @param menu The context menu.
     *  @param action The action to be added to the context menu.
     *  @param tooltip The tooltip for the action.
     *  @return The added menu item.
     */
    @Override
    protected JMenuItem _add(JContextMenu menu, Action action, String tooltip) {
        String label = (String) action.getValue(Action.NAME);

        if (tooltip == null) {
            tooltip = (String) action.getValue("tooltip");
        }
        action.putValue("tooltip", tooltip);

        JRadioButtonMenuItem item = new JRadioButtonMenuItem(action);
        item.setText(label);
        item.setToolTipText(tooltip);
        action.putValue("menuItem", item);

        _group.add(item);
        menu.add(item);
        return item;
    }

    /** Add an action to the submenu.
     *  @param submenu The submenu.
     *  @param action The action to be added to the submenu.
     *  @return The added menu item.
     */
    @Override
    protected JMenuItem _add(JMenu submenu, Action action) {
        String label = (String) action.getValue(Action.NAME);

        String tooltip = (String) action.getValue("tooltip");
        action.putValue("tooltip", tooltip);

        JRadioButtonMenuItem item = new JRadioButtonMenuItem(action);
        item.setText(label);
        item.setToolTipText(tooltip);
        action.putValue("menuItem", item);

        _group.add(item);
        submenu.add(item);
        return item;
    }

    /** The group that contains all the actions for this factory. */
    private ButtonGroup _group;
}
