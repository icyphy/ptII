/* An editor to configure and run the JNI interface

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package jni.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import jni.JNIUtilities;
import ptolemy.actor.gui.Configurer;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.gui.JTextAreaExec;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// JNICodeGeneratorGUI

/**
 An attribute that creates an editor for that builds support code for
 the Java Native Interface actors.
 It customizes the user interface for "configuring" the code
 generator. This UI will be invoked when you double click on the
 code generator.

 @author Christopher Brooks, based on CodeGeneratorGUI by Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Red (eal)
 @deprecated This code is old, hard to use and unmaintained.  See
  {@link ptolemy.actor.lib.jni.EmbeddedCActor} for a more recent implementation.
 */
public class JNICodeGeneratorGUI extends PtolemyFrame {

    /** Construct a frame to control code generation for the specified
     *  Ptolemy II model.  After constructing this, it is necessary to
     *  call setVisible(true) to make the frame appear.  This is
     *  typically accomplished by calling show() on enclosing tableau.
     *
     *  @param codeGenerator The codeGenerator to put in this frame,
     *  or null if none.
     *  @param tableau The tableau responsible for this frame.
     *  @exception IllegalActionException If the model rejects the
     *   configuration attribute.
     *  @exception NameDuplicationException If a name collision occurs.
     */
    public JNICodeGeneratorGUI(final JNICodeGenerator codeGenerator,
            Tableau tableau) throws IllegalActionException,
            NameDuplicationException {
        super(codeGenerator, tableau);

        setTitle(codeGenerator.getName());

        if ((getEffigy() == null) || (getEffigy().uri == null)
                || (getEffigy().uri.getURI() == null)) {
            throw new InternalErrorException("Cannot get an effigy!");
        }

        // Caveats panel.
        JPanel caveatsPanel = new JPanel();
        caveatsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        caveatsPanel.setLayout(new BoxLayout(caveatsPanel, BoxLayout.X_AXIS));

        JTextArea messageArea = new JTextArea(
                "NOTE: This is a highly preliminary "
                        + "JNI facility, with many "
                        + "limitations.  It is best viewed as "
                        + "a concept demonstration."
                        + "\nPress the Generate button to create ports "
                        + "and generate code.", 2, 10);
        messageArea.setEditable(false);
        messageArea.setBorder(BorderFactory.createEtchedBorder());
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        caveatsPanel.add(messageArea);

        JButton moreInfoButton = new JButton("More Info");
        moreInfoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ArgumentConfigurerDialog.help(getConfiguration());
            }
        });
        caveatsPanel.add(moreInfoButton);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        caveatsPanel.setMaximumSize(new Dimension(500, 100));
        left.add(caveatsPanel);

        // Panel for push buttons.
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4));

        JButton goButton = new JButton("Generate");
        goButton.setToolTipText("Create ports, Generate code");
        buttonPanel.add(goButton);

        JButton stopButton = new JButton("Cancel");
        stopButton.setToolTipText("Terminate executing processes");
        buttonPanel.add(stopButton);

        JButton clearButton = new JButton("Clear");
        clearButton.setToolTipText("Clear Log");
        buttonPanel.add(clearButton);

        buttonPanel.setMaximumSize(new Dimension(500, 50));
        left.add(buttonPanel);

        Configurer configurer = new Configurer(codeGenerator);
        JPanel controlPanel = new JPanel();
        controlPanel.add(configurer);

        JScrollPane scrollPane = new JScrollPane(controlPanel);

        left.add(scrollPane, BorderLayout.CENTER);

        // Create a JTextAreaExec without Start and Cancel buttons.
        final JTextAreaExec exec = new JTextAreaExec("Code Generator Commands",
                false);

        // If we execute any commands, print the output in the text area.
        JNICodeGenerator.setExecuteCommands(exec);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                left, exec);
        splitPane.setOneTouchExpandable(true);

        // Adjust the divider so that the control panel does not
        // have a horizontal scrollbar.
        Dimension preferred = left.getPreferredSize();
        splitPane.setDividerLocation(preferred.width + 20);

        getContentPane().add(splitPane, BorderLayout.CENTER);

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                exec.cancel();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                exec.clear();
            }
        });

        goButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    exec.updateStatusBar("// Starting " + codeGenerator
                            + " code generatio for "
                            + getModel().getContainer());

                    JNIUtilities.generateJNI((CompositeEntity) (getModel()
                            .getContainer()));
                    //                     StringBuffer code = new StringBuffer();
                    //                     codeGenerator.generateCode(code);
                    //                     String codeFileNameWritten = codeGenerator
                    //                             .getCodeFileName();

                    //                     if (codeFileNameWritten != null) {
                    //                         Configuration configuration = getConfiguration();

                    //                         URL codeURL = new File(codeFileNameWritten).toURL();
                    //                         // Use Thread.currentThread() so that this code will
                    //                         // work under WebStart.
                    //                         configuration.openModel(null, codeURL, codeURL
                    //                                 .toExternalForm());

                    //                     }
                    //                     // Don't write the file to the window
                    //                     //exec.updateStatusBar(code.toString());

                    exec.updateStatusBar("// Code generation " + "complete.");
                } catch (Exception ex) {
                    MessageHandler.error("Code generation failed.", ex);
                }
            }
        });
    }
}
