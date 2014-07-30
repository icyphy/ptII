/* An editor to configure and run a mathematical model converter.

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
package ptolemy.verification.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Configurer;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.IntToken;
import ptolemy.gui.JTextAreaExec;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.MessageHandler;
import ptolemy.verification.kernel.MathematicalModelConverter;
import ptolemy.verification.kernel.MathematicalModelConverter.FormulaType;
import ptolemy.verification.kernel.MathematicalModelConverter.ModelType;
import ptolemy.verification.kernel.MathematicalModelConverter.OutputType;

//////////////////////////////////////////////////////////////////////////
//// MathematicalModelConverter

/**
 This is an attribute that creates an editor for configuring and
 running a code generator.  This is designed to be contained by
 an instance of CodeGenerator or a subclass of CodeGenerator.
 It customizes the user interface for "configuring" the code
 generator. This UI will be invoked when you double click on the
 code generator.

 @deprecated ptolemy.de.lib.TimedDelay is deprecated, use ptolemy.actor.lib.TimeDelay.
 @author Chih-Hong Cheng, Thomas Huining Feng, Edward A. Lee, Christopher Brooks
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (patrickj)
 @Pt.AcceptedRating Red (eal)
 */
@Deprecated
@SuppressWarnings("serial")
public class MathematicalModelConverterGUI extends PtolemyFrame {

    /** Construct a frame to control code generation for the specified
     *  Ptolemy II model. After constructing this, it is necessary to
     *  call setVisible(true) to make the frame appear. This is
     *  typically accomplished by calling show() on enclosing tableau.
     *
     *  @param modelConverter The modelConverter to put in this frame,
     *  or null if none.
     *  @param tableau The tableau responsible for this frame.
     *  @exception IllegalActionException If the model rejects the
     *   configuration attribute.
     *  @exception NameDuplicationException If a name collision occurs.
     * @deprecated ptolemy.de.lib.TimedDelay is deprecated, use ptolemy.actor.lib.TimeDelay.
     */
    @Deprecated
    public MathematicalModelConverterGUI(
            final MathematicalModelConverter modelConverter, Tableau tableau)
                    throws IllegalActionException, NameDuplicationException {
        super(modelConverter, tableau);

        setTitle(modelConverter.getName());

        // Caveats panel.
        JPanel caveatsPanel = new JPanel();
        caveatsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        caveatsPanel.setLayout(new BoxLayout(caveatsPanel, BoxLayout.X_AXIS));

        JTextArea messageArea = new JTextArea("NOTE: This is a highly "
                + "preliminary facility for\n      verification with many "
                + "limitations. It is \n      best viewed as a concept "
                + "demonstration.", 2, 10);
        messageArea.setEditable(false);
        messageArea.setBorder(BorderFactory.createEtchedBorder());
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        caveatsPanel.add(messageArea);

        JPanel upper = new JPanel();
        upper.setLayout(new BoxLayout(upper, BoxLayout.Y_AXIS));
        caveatsPanel.setMaximumSize(new Dimension(500, 100));
        upper.add(caveatsPanel);

        // Panel for push buttons.
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3));

        JButton goButton = new JButton("Convert");
        goButton.setToolTipText("Convert Model");
        buttonPanel.add(goButton, BorderLayout.CENTER);

        JButton clearButton = new JButton("Clear");
        clearButton.setToolTipText("Clear Log");
        buttonPanel.add(clearButton);

        JButton moreInfoButton = new JButton("More Info");
        moreInfoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    Configuration configuration = getConfiguration();

                    // FIXME: Customize to the particular code generator.
                    // Use Thread.currentThread() so that this code will
                    // work under WebStart.
                    URL infoURL = Thread.currentThread()
                            .getContextClassLoader()
                            .getResource("ptolemy/verification/README.html");

                    if (configuration != null) {
                        configuration.openModel(null, infoURL,
                                infoURL.toExternalForm());
                    }
                } catch (Exception ex) {
                    throw new InternalErrorException(modelConverter, ex,
                            "Failed to open ptolemy/verification/README.html.");
                }
            }
        });
        buttonPanel.add(moreInfoButton);

        buttonPanel.setMaximumSize(new Dimension(500, 50));
        upper.add(buttonPanel);

        Configurer configurer = new Configurer(modelConverter);
        JPanel controlPanel = new JPanel();
        controlPanel.add(configurer);

        JScrollPane scrollPane = new JScrollPane(controlPanel);

        upper.add(scrollPane, BorderLayout.CENTER);

        // Create a JTextAreaExec without Start and Cancel buttons.

        // FIXME: We use a JTextArea because eventually we should pass
        // this to the modelConverter.generateFile() method.

        final JTextAreaExec exec = new JTextAreaExec(
                "Terminal (Verification Results)", false);

        exec.setPreferredSize(new Dimension(500, 300));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upper,
                exec);
        splitPane.setOneTouchExpandable(true);

        // Adjust the divider so that the control panel does not have a
        // horizontal scrollbar.
        Dimension preferred = upper.getPreferredSize();
        splitPane.setDividerLocation(preferred.height + 20);

        getContentPane().add(splitPane, BorderLayout.CENTER);

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                exec.clear();
            }
        });

        goButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    ModelType modelType = (ModelType) modelConverter.modelType
                            .getChosenValue();

                    File file = modelConverter.target.asFile();
                    exec.stdout("File to be generated is " + file);

                    exec.updateStatusBar("// Starting " + modelConverter
                            + "model converting process.");

                    String inputTemporalFormula = modelConverter.formula
                            .getExpression();
                    FormulaType formulaType = (FormulaType) modelConverter.formulaType
                            .getChosenValue();
                    int span = ((IntToken) modelConverter.span.getToken())
                            .intValue();
                    OutputType outputType = (OutputType) modelConverter.outputType
                            .getChosenValue();
                    int bufferSize = ((IntToken) modelConverter.buffer
                            .getToken()).intValue();
                    if (formulaType == FormulaType.Risk
                            || formulaType == FormulaType.Reachability) {
                        inputTemporalFormula = modelConverter
                                .generateGraphicalSpec(formulaType);
                        formulaType = FormulaType.CTL;
                    }

                    StringBuffer code = new StringBuffer("");

                    try {
                        code.append(modelConverter.generateFile(file,
                                modelType, inputTemporalFormula, formulaType,
                                span, outputType, bufferSize));
                    } catch (Exception e) {
                        MessageHandler.error("Failed to output result to the "
                                + "file.", e);
                        return;
                    }

                    File codeFileNameWritten = modelConverter.getCodeFile();

                    if (codeFileNameWritten != null) {
                        Configuration configuration = getConfiguration();

                        URL codeURL = codeFileNameWritten.toURI().toURL();
                        // Use Thread.currentThread() so that this code will
                        // work under WebStart.
                        if (configuration != null) {
                            configuration.openModel(null, codeURL,
                                    codeURL.toExternalForm());
                        }
                    }

                    exec.updateStatusBar(code.toString());
                    exec.updateStatusBar("// Model conversion " + "complete.");
                } catch (Exception ex) {
                    MessageHandler.error("Conversion failed.", ex);
                }
            }
        });
    }
}
