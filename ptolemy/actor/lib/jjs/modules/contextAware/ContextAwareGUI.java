/* A GUI for the Context Aware accessor. */

// Copyright (c) 2015-2016 The Regents of the University of California.
// All rights reserved.

// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.

// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.

// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.

package ptolemy.actor.lib.jjs.modules.contextAware;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

/** A GUI for the Context Aware accessor.
 * @author Anne H. Ngu (angu@txstate.edu)
@version $Id$
@since Ptolemy II 11.0
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ContextAwareGUI {

    /** Construct the gui for a context-aware accessor.
     *  @param list A of list values to present.
     */
    public ContextAwareGUI(String[] list) {
        System.out.println(list.length);
        JFrame frame = new JFrame("title");
        JPanel panel = new JPanel();
        textFields = new ArrayList<JTextField>();
        labels = new ArrayList<JLabel>();
        for (int i = 0; i <= list.length; i++) {
            labels.add(new JLabel());
            textFields.add(new JTextField(10));
            labels.get(i).setVisible(false);
            textFields.get(i).setVisible(false);
        }
        searchButton = new JButton("Search");
        listModel = new DefaultListModel<String>();
        servicesList = new JList<String>(list);
        _groupLayout = new GroupLayout(panel);
        _groupLayout.setHorizontalGroup(_groupLayout
                .createParallelGroup(Alignment.LEADING)
                .addGroup(_groupLayout.createSequentialGroup()
                        .addComponent(servicesList, GroupLayout.PREFERRED_SIZE,
                                110, GroupLayout.PREFERRED_SIZE)
                        .addGroup(_groupLayout
                                .createParallelGroup(Alignment.LEADING)
                                .addGroup(_groupLayout.createSequentialGroup()
                                        .addGap(207).addComponent(searchButton,
                                                GroupLayout.PREFERRED_SIZE, 89,
                                                GroupLayout.PREFERRED_SIZE))
                                .addGroup(_groupLayout.createSequentialGroup()
                                        .addGap(18)
                                        .addGroup(_groupLayout
                                                .createParallelGroup(
                                                        Alignment.LEADING)
                                                .addGroup(_groupLayout
                                                        .createSequentialGroup()
                                                        .addComponent(
                                                                labels.get(5))
                                                        .addPreferredGap(
                                                                ComponentPlacement.RELATED)
                                                        .addComponent(textFields
                                                                .get(5)))
                                                .addGroup(_groupLayout
                                                        .createSequentialGroup()
                                                        .addComponent(
                                                                labels.get(4))
                                                        .addPreferredGap(
                                                                ComponentPlacement.RELATED)
                                                        .addComponent(textFields
                                                                .get(4)))
                                                .addGroup(_groupLayout
                                                        .createSequentialGroup()
                                                        .addComponent(
                                                                labels.get(3))
                                                        .addPreferredGap(
                                                                ComponentPlacement.RELATED)
                                                        .addComponent(textFields
                                                                .get(3)))
                                                .addGroup(_groupLayout
                                                        .createSequentialGroup()
                                                        .addComponent(
                                                                labels.get(2))
                                                        .addPreferredGap(
                                                                ComponentPlacement.RELATED)
                                                        .addComponent(textFields
                                                                .get(2)))
                                                .addGroup(_groupLayout
                                                        .createSequentialGroup()
                                                        .addComponent(
                                                                labels.get(1))
                                                        .addPreferredGap(
                                                                ComponentPlacement.RELATED)
                                                        .addComponent(textFields
                                                                .get(1)))
                                                .addGroup(_groupLayout
                                                        .createSequentialGroup()
                                                        .addComponent(
                                                                labels.get(0))
                                                        .addPreferredGap(
                                                                ComponentPlacement.RELATED)
                                                        .addComponent(
                                                                textFields
                                                                        .get(0),
                                                                GroupLayout.DEFAULT_SIZE,
                                                                217,
                                                                Short.MAX_VALUE)))
                                        .addGap(9)))
                        .addGap(122)));
        _groupLayout.setVerticalGroup(_groupLayout
                .createParallelGroup(Alignment.LEADING)
                .addComponent(servicesList, GroupLayout.DEFAULT_SIZE, 294,
                        Short.MAX_VALUE)
                .addGroup(Alignment.TRAILING,
                        _groupLayout.createSequentialGroup().addGap(24)
                                .addGroup(_groupLayout
                                        .createParallelGroup(Alignment.BASELINE)
                                        .addComponent(textFields.get(0),
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(labels.get(0)))
                                .addGap(5)
                                .addGroup(_groupLayout
                                        .createParallelGroup(Alignment.BASELINE)
                                        .addComponent(labels.get(1),
                                                GroupLayout.PREFERRED_SIZE, 20,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(textFields.get(1),
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                                .addGap(5)
                                .addGroup(_groupLayout
                                        .createParallelGroup(Alignment.BASELINE)
                                        .addComponent(labels.get(2),
                                                GroupLayout.PREFERRED_SIZE, 20,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(textFields.get(2),
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                                .addGap(5)
                                .addGroup(_groupLayout
                                        .createParallelGroup(Alignment.BASELINE)
                                        .addComponent(labels.get(3),
                                                GroupLayout.PREFERRED_SIZE, 20,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(textFields.get(3),
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                                .addGap(5)
                                .addGroup(_groupLayout
                                        .createParallelGroup(Alignment.BASELINE)
                                        .addComponent(textFields.get(4),
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(labels.get(4)))
                                .addGap(5)
                                .addGroup(_groupLayout
                                        .createParallelGroup(Alignment.BASELINE)
                                        .addComponent(textFields.get(5),
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(labels.get(5)))
                                .addPreferredGap(ComponentPlacement.RELATED,
                                        111, Short.MAX_VALUE)
                                .addComponent(searchButton).addContainerGap()));

        _groupLayout.setHonorsVisibility(true);
        panel.setLayout(_groupLayout);

        frame.setSize(300, 200);
        panel.add(searchButton); // add button to panel
        frame.setContentPane(panel); // add panel to frame
        frame.setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The search button. */
    public JButton searchButton;

    /** The list of text fields. */
    public List<JTextField> textFields;

    /** The list of labels. */
    public List<JLabel> labels;

    /** The list model. */
    public DefaultListModel<String> listModel;

    /** List of services. */
    public JList<String> servicesList;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private GroupLayout _groupLayout;
}
