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
import java.awt.LayoutManager;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.mlc.swing.layout.LayoutFrame;

/**
 * @author Michael Connor
@version $Id$
@since Ptolemy II 8.0
 */
@SuppressWarnings("serial")
public class CustomerPanel extends javax.swing.JPanel {
    javax.swing.JComboBox suffixCombo = new javax.swing.JComboBox();

    javax.swing.JLabel lastNameLabel = new javax.swing.JLabel("Last Name");

    javax.swing.JTextField firstNameText = new javax.swing.JTextField();

    javax.swing.JTextField lastNameText = new javax.swing.JTextField();

    javax.swing.JTextField faxText = new javax.swing.JTextField();

    java.awt.Component contactSeparator = com.jgoodies.forms.factories.DefaultComponentFactory
            .getInstance().createSeparator("Contact Info");

    javax.swing.JLabel surnameLabel = new javax.swing.JLabel("Surname");

    javax.swing.JButton okButton = new javax.swing.JButton("OK");

    javax.swing.JButton cancelButton = new javax.swing.JButton("Cancel");

    java.awt.Component buttonBar = com.jgoodies.forms.factories.ButtonBarFactory
            .buildRightAlignedBar(new JButton[] { okButton, cancelButton });

    javax.swing.JLabel workPhoneLabel = new javax.swing.JLabel("Work Phone");

    javax.swing.JTextField workPhoneText = new javax.swing.JTextField();

    javax.swing.JTextField emailText = new javax.swing.JTextField();

    javax.swing.JTextField homePhoneText = new javax.swing.JTextField();

    javax.swing.JLabel faxLabel = new javax.swing.JLabel("Fax Number");

    java.awt.Component ordersSeparator = com.jgoodies.forms.factories.DefaultComponentFactory
            .getInstance().createSeparator("Orders");

    javax.swing.JComboBox surnameCombo = new javax.swing.JComboBox();

    java.awt.Component nameSeparator = com.jgoodies.forms.factories.DefaultComponentFactory
            .getInstance().createSeparator("Name");

    javax.swing.JLabel suffixLabel = new javax.swing.JLabel("Suffix");

    javax.swing.JLabel emailLabel = new javax.swing.JLabel("E-mail");

    javax.swing.JLabel homePhoneLabel = new javax.swing.JLabel("Home Phone");

    javax.swing.JLabel firstNameLabel = new javax.swing.JLabel("First Name");

    javax.swing.JTable orderTableControl = new javax.swing.JTable();

    javax.swing.JScrollPane orderTable = new javax.swing.JScrollPane(
            orderTableControl);

    public CustomerPanel() {
        super();
        java.io.InputStream is = this.getClass().getResourceAsStream(
                "customerLayout.xml");
        if (is == null) {
            System.err.println("Could not find constraints customerLayout.xml");
            return;
        }
        org.mlc.swing.layout.LayoutConstraintsManager layoutConstraintsManager = org.mlc.swing.layout.LayoutConstraintsManager
                .getLayoutConstraintsManager(is);
        setBorder(com.jgoodies.forms.factories.Borders.DIALOG_BORDER);
        LayoutManager layout = layoutConstraintsManager.createLayout("panel",
                this);
        this.setLayout(layout);

        this.add(nameSeparator, "nameSeparator");
        this.add(surnameLabel, "surnameLabel");
        this.add(surnameCombo, "surnameCombo");
        this.add(firstNameLabel, "firstNameLabel");
        this.add(firstNameText, "firstNameText");
        this.add(lastNameLabel, "lastNameLabel");
        this.add(lastNameText, "lastNameText");
        this.add(suffixLabel, "suffixLabel");
        this.add(suffixCombo, "suffixCombo");
        this.add(contactSeparator, "contactSeparator");
        this.add(workPhoneLabel, "workPhoneLabel");
        this.add(workPhoneText, "workPhoneText");
        this.add(homePhoneLabel, "homePhoneLabel");
        this.add(homePhoneText, "homePhoneText");
        this.add(faxLabel, "faxLabel");
        this.add(faxText, "faxText");
        this.add(emailLabel, "emailLabel");
        this.add(emailText, "emailText");
        this.add(ordersSeparator, "ordersSeparator");
        this.add(orderTable, "orderTable");
        this.add(buttonBar, "buttonBar");

        LayoutFrame frame = new LayoutFrame(layoutConstraintsManager);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        UIDefaults defaults = UIManager.getDefaults();
        defaults.put("Label.font", new javax.swing.plaf.FontUIResource(
                new java.awt.Font("Arial", java.awt.Font.PLAIN, 12)));
        defaults.put("ComboBox.background",
                new javax.swing.plaf.ColorUIResource(255, 255, 255));

        CustomerPanel customerPanel = new CustomerPanel();

        JFrame frame = new JFrame("Edit Customer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(customerPanel, BorderLayout.CENTER);
        frame.setSize(600, 700);
        frame.setVisible(true);
    }

}
