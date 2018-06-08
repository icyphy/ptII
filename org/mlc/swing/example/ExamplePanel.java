/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2007-2009 The Regents of the University of California.
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
 */
package org.mlc.swing.example;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.mlc.swing.layout.LayoutFrame;

/** An example.
 * @author Michael Connor
 * @version $Id$
 * @since Ptolemy II 8.0
 */
@SuppressWarnings("serial")
public class ExamplePanel extends javax.swing.JPanel {
    JLabel nameLabel = new JLabel("Name");

    JTextField nameText = new JTextField();

    JTabbedPane tabbedPane = new JTabbedPane();

    JPanel firstTab = new JPanel();

    JPanel secondTab = new JPanel();

    JPanel thirdTab = new JPanel();

    /** Create the ExamplePanel. */
    public ExamplePanel() {
        super();
        org.mlc.swing.layout.LayoutConstraintsManager layoutConstraintsManager = new org.mlc.swing.layout.LayoutConstraintsManager();
        setBorder(com.jgoodies.forms.factories.Borders.DIALOG_BORDER);

        layoutConstraintsManager.setLayout("panel", this);
        layoutConstraintsManager.setLayout("firstTab", firstTab);
        layoutConstraintsManager.setLayout("secondTab", secondTab);
        layoutConstraintsManager.setLayout("thirdTab", thirdTab);

        LayoutFrame layoutFrame = new LayoutFrame(layoutConstraintsManager);
        layoutFrame.setVisible(true);

        this.add(tabbedPane, "tabbedPane");
        this.add(nameLabel, "nameLabel");
        this.add(nameText, "nameText");
        tabbedPane.add("First", firstTab);
        tabbedPane.add("Second", secondTab);
        tabbedPane.add("Third", thirdTab);
    }

    /** Run the CustomerPanel example.
     *  @param args Not used.
     */
    public static void main(String[] args) {
        ExamplePanel examplePanel = new ExamplePanel();

        JFrame frame = new JFrame("Example");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(examplePanel, BorderLayout.CENTER);
        frame.setSize(400, 500);
        frame.setVisible(true);
    }

}
