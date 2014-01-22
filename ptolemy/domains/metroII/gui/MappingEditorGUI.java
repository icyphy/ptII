/* An editor to configure mapping for MetroIIDirector.

 Copyright (c) 2006-2013 The Regents of the University of California.
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
package ptolemy.domains.metroII.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.domains.metroII.kernel.MappingEditor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// MappingEditorGUI

/**
 * This is an attribute that creates an editor for configuring mapping for
 * MetroIIDirector. This UI will be invoked when you double click on the code
 * generator.
 * 
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 */
public class MappingEditorGUI extends PtolemyFrame {

    /**
     * Construct a frame for the MappingEditor.
     * 
     * @param tableau
     *            The tableau responsible for this frame.
     * @exception IllegalActionException
     *                If the model rejects the configuration attribute.
     * @exception NameDuplicationException
     *                If a name collision occurs.
     */
    public MappingEditorGUI(final NamedObj container, Tableau tableau)
            throws IllegalActionException, NameDuplicationException {
        super(container, tableau);

        File mappingFile = ((MappingEditor) container).getMappingFile();
        this.setTitle(mappingFile.getAbsolutePath());

        JPanel buttonPanel = new JPanel();
        JButton commitButton = new JButton("Save");
        commitButton.setToolTipText("Save constraints");
        commitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    ((MappingEditor) container).saveMapping(textArea.getText());
                    // JFrame frame = (JButton) e.getSource();
                    // frame.dispose();
                    dispose();
                } catch (IllegalActionException e1) {
                    MessageHandler.error(e1.getMessage());
                }
            }
        });
        buttonPanel.add(commitButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText("Cancel inputs");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        buttonPanel.add(cancelButton);

        textArea = new JTextArea(((MappingEditor) container).readMapping(), 30,
                80);
        textArea.setEditable(true);
        textArea.setBorder(BorderFactory.createEtchedBorder());

        JScrollPane textPane = new JScrollPane(textArea);

        JPanel middle = new JPanel();
        middle.add(textPane);
        middle.add(buttonPanel);
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));

        getContentPane().add(middle, BorderLayout.CENTER);

    }

    JTextArea textArea;

}
