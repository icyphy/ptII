/* A pane that displays and edits a sequential schedule.

 Copyright (c) 2010-2014 The Regents of the University of California.
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
package ptolemy.domains.sequence.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ptolemy.actor.Actor;
import ptolemy.actor.sched.Schedule;

///////////////////////////////////////////////////////////////////
//// SequentialScheduleEditorPane

/** A pane that displays and edits a sequential schedule.
 * Changes are not committed. Order is passed to the owning
 * instance via getOrderedActors().
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ristau)
 * @Pt.AcceptedRating Red (ristau)
 */
@SuppressWarnings("serial")
public class SequentialScheduleEditorPane extends JPanel implements
ListSelectionListener {

    /** Construct a SequentialScheduleEditorPane displaying the given actors
     * in the order given by the vector containing the actors.
     *
     * @param actors The actors to be displayed in the pane.
     */
    public SequentialScheduleEditorPane(Vector<Actor> actors) {
        super(new BorderLayout());
        _init();
        Iterator it = actors.iterator();
        while (it.hasNext()) {
            Actor actor = (Actor) it.next();
            _listModel.addElement(actor);
        }
    }

    /** Construct a SequentialScheduleEditorPane displaying the actors
     * contained in the given schedule and in the order given by the schedule.
     *
     * @param schedule The schedule for the actors to be displayed in the pane.
     */
    public SequentialScheduleEditorPane(Schedule schedule) {
        super(new BorderLayout());
        _init();
        Iterator firings = schedule.actorIterator();
        while (firings.hasNext()) {
            Object actor = firings.next();
            _listModel.addElement(actor);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the order of the actors as displayed in this Panel.
     * @return The ordered actors.
     */
    public Vector<Actor> getOrderedActors() {
        Vector<Actor> result = new Vector<Actor>();
        int size = _listModel.getSize();
        for (int i = 0; i < size; ++i) {
            result.add((Actor) _listModel.get(i));
        }
        return result;
    }

    /** Listener method for _list selection changes. */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        // do nothing
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // Listen for clicks on the up and down arrow buttons.
    class UpDownListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            //This method can be called only when
            //there's a valid selection,
            //so go ahead and move the _list item.
            int moveMe = _list.getSelectedIndex();

            if (e.getActionCommand().equals(_upString)) {
                //UP ARROW BUTTON
                if (moveMe != 0) {
                    //not already at top
                    _swap(moveMe, moveMe - 1);
                    _list.setSelectedIndex(moveMe - 1);
                    _list.ensureIndexIsVisible(moveMe - 1);
                }
            } else {
                //DOWN ARROW BUTTON
                if (moveMe != _listModel.getSize() - 1) {
                    //not already at bottom
                    _swap(moveMe, moveMe + 1);
                    _list.setSelectedIndex(moveMe + 1);
                    _list.ensureIndexIsVisible(moveMe + 1);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _init() {
        _listModel = new DefaultListModel();

        _list = new JList(_listModel);
        _list.setCellRenderer(new ActorCellRenderer());
        _list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane listScrollPane = new JScrollPane(_list);

        _upButton = new JButton("Move up");
        _upButton
        .setToolTipText("Move the currently selected _list item higher.");
        _upButton.setActionCommand(_upString);
        _upButton.addActionListener(new UpDownListener());

        _downButton = new JButton("Move down");
        _downButton
        .setToolTipText("Move the currently selected _list item lower.");
        _downButton.setActionCommand(_downString);
        _downButton.addActionListener(new UpDownListener());

        JPanel upDownPanel = new JPanel(new GridLayout(2, 1));
        upDownPanel.add(_upButton);
        upDownPanel.add(_downButton);

        //Put everything together.
        add(upDownPanel, BorderLayout.EAST);
        add(listScrollPane, BorderLayout.CENTER);
    }

    //Swap two elements in the _list.
    private void _swap(int a, int b) {
        Object aObject = _listModel.getElementAt(a);
        Object bObject = _listModel.getElementAt(b);
        _listModel.set(a, bObject);
        _listModel.set(b, aObject);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private JButton _downButton;
    private static final String _downString = "Move down";
    private JList _list;
    private DefaultListModel _listModel;
    private JButton _upButton;
    private static final String _upString = "Move up";
}
