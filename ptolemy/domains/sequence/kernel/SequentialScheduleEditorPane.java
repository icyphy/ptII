/* A pane that displays and edits a sequential schedule. 

 Copyright (c) 2010 The Regents of the University of California.
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
package ptolemy.domains.sequence.kernel;

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

/**
 * A pane that displays and edits a sequential schedule.
 * Changes are not commited. Order is passed to the owning 
 * instance via getOrderedActors(). 
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ristau)
 * @Pt.AcceptedRating Red (ristau)
 */
public class SequentialScheduleEditorPane extends JPanel implements
        ListSelectionListener {

    public SequentialScheduleEditorPane(Schedule schedule) {
        super(new BorderLayout());
        listModel = new DefaultListModel();

        Iterator firings = schedule.actorIterator();
        while (firings.hasNext()) {
            Object actor = firings.next();
            listModel.addElement(actor);
        }

        list = new JList(listModel);
        list.setCellRenderer(new ActorCellRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane listScrollPane = new JScrollPane(list);

        upButton = new JButton("Move up");
        upButton
                .setToolTipText("Move the currently selected list item higher.");
        upButton.setActionCommand(upString);
        upButton.addActionListener(new UpDownListener());

        downButton = new JButton("Move down");
        downButton
                .setToolTipText("Move the currently selected list item lower.");
        downButton.setActionCommand(downString);
        downButton.addActionListener(new UpDownListener());

        JPanel upDownPanel = new JPanel(new GridLayout(2, 1));
        upDownPanel.add(upButton);
        upDownPanel.add(downButton);

        //Put everything together.
        add(upDownPanel, BorderLayout.EAST);
        add(listScrollPane, BorderLayout.CENTER);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the order of the actors as displayed in this Panel.*/
    public Vector<Actor> getOrderedActors() {
        Vector<Actor> result = new Vector<Actor>();
        int size = listModel.getSize();
        for (int i = 0; i < size; ++i) {
            result.add((Actor) listModel.get(i));
        }
        return result;
    }

    /** Listener method for list selection changes. */
    public void valueChanged(ListSelectionEvent e) {
        // do nothing
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // Listen for clicks on the up and down arrow buttons.
    class UpDownListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            //This method can be called only when
            //there's a valid selection,
            //so go ahead and move the list item.
            int moveMe = list.getSelectedIndex();

            if (e.getActionCommand().equals(upString)) {
                //UP ARROW BUTTON
                if (moveMe != 0) {
                    //not already at top
                    swap(moveMe, moveMe - 1);
                    list.setSelectedIndex(moveMe - 1);
                    list.ensureIndexIsVisible(moveMe - 1);
                }
            } else {
                //DOWN ARROW BUTTON
                if (moveMe != listModel.getSize() - 1) {
                    //not already at bottom
                    swap(moveMe, moveMe + 1);
                    list.setSelectedIndex(moveMe + 1);
                    list.ensureIndexIsVisible(moveMe + 1);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    //Swap two elements in the list.
    private void swap(int a, int b) {
        Object aObject = listModel.getElementAt(a);
        Object bObject = listModel.getElementAt(b);
        listModel.set(a, bObject);
        listModel.set(b, aObject);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private JButton downButton;
    private static final String downString = "Move down";
    private JList list;
    private DefaultListModel listModel;
    private JButton upButton;
    private static final String upString = "Move up";
}
