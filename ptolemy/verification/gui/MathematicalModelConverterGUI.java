/* An editor to configure and run a mathematical model converter.

 Copyright (c) 2008 The Regents of the University of California.
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.gui.JTextAreaExec;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.MessageHandler;
import ptolemy.verification.kernel.MathematicalModelConverter;

//////////////////////////////////////////////////////////////////////////
//// MathematicalModelConverter

/**
 This is an attribute that creates an editor for configuring and
 running a code generator.  This is designed to be contained by
 an instance of CodeGenerator or a subclass of CodeGenerator.
 It customizes the user interface for "configuring" the code
 generator. This UI will be invoked when you double click on the
 code generator.

 @author Chihhong Patrick Cheng, Edward A. Lee, Christopher Brooks
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (patrickj)
 @Pt.AcceptedRating Red (eal)
 */
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
     */
    public MathematicalModelConverterGUI(
            final MathematicalModelConverter modelConverter, Tableau tableau)
            throws IllegalActionException, NameDuplicationException {
        super(modelConverter, tableau);

        setTitle(modelConverter.getName());

        // FIXME: The following is pretty lame.  If the model hasn't been saved,
        // then we throw an exception as below, but why can't we just make
        // it work?
        if ((getEffigy() == null) || (getEffigy().uri == null)
                || (getEffigy().uri.getURI() == null)) {
            throw new IllegalActionException(
                    "No effigy: Please save the model to a file before generating code.");
        }

        // Caveats panel.
        caveatsPanel = new JPanel();
        caveatsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        caveatsPanel.setLayout(new BoxLayout(caveatsPanel, BoxLayout.X_AXIS));

        messageArea = new JTextArea("NOTE: This is a highly preliminary "
                + "facility for\n      verification with many "
                + "limitations. It is \n      best viewed as "
                + "a concept demonstration.", 2, 10);
        messageArea.setEditable(false);
        messageArea.setBorder(BorderFactory.createEtchedBorder());
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        caveatsPanel.add(messageArea);

        left = new JPanel();
        left.setSize(500, 400);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        caveatsPanel.setMaximumSize(new Dimension(500, 100));
        left.add(caveatsPanel);

        // Panel for push buttons.
        buttonPanel = new JPanel();
        goButton = new JButton("Convert");
        goButton.setToolTipText("Convert Model");
        buttonPanel.add(goButton, BorderLayout.CENTER);

        clearButton = new JButton("Clear");
        clearButton.setToolTipText("Clear Log");
        buttonPanel.add(clearButton);

        moreInfoButton = new JButton("More Info");
        moreInfoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    Configuration configuration = getConfiguration();

                    // FIXME: Customize to the particular code generator.
                    // Use Thread.currentThread() so that this code will
                    // work under WebStart.
                    URL infoURL = Thread.currentThread()
                            .getContextClassLoader().getResource(
                                    "ptolemy/verification/README.html");

                    configuration.openModel(null, infoURL, infoURL
                            .toExternalForm());
                } catch (Exception ex) {
                    throw new InternalErrorException(modelConverter, ex,
                            "Failed to open ptolemy/verification/README.html: ");
                }
            }
        });
        buttonPanel.add(moreInfoButton);

        buttonPanel.setMaximumSize(new Dimension(500, 50));
        left.add(buttonPanel);

        controlPanel = new JPanel();
        // controlPanel.setLayout(new SpringLayout());
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        JPanel controlPanel1 = new JPanel();
        controlPanel1.setSize(400, 30);
        controlPanel1.add(new JLabel("Model Type"));

        String[] modelTypes = {
                "Kripke Structures (Acceptable by NuSMV under SR)",
                "Communicating Timed Automata (Acceptable by RED under DE)" };
        modelTypeList = new JComboBox(modelTypes);
        modelTypeList.setSelectedIndex(0);
        controlPanel1.add(modelTypeList);
        controlPanel.add(controlPanel1);

        JPanel controlPanel2 = new JPanel();
        controlPanel2.setSize(400, 30);
        controlPanel2.add(new JLabel("Formula Type"));
        String[] formulaTypes = { "CTL", "LTL", "TCTL", "Buffer Overflow", "Risk", "Reachability" };
        formulaTypeList = new JComboBox(formulaTypes);
        formulaTypeList.setSelectedIndex(0);
        controlPanel2.add(formulaTypeList);
        controlPanel2.add(new JLabel("Output Choice"));
        String[] outputChoiceTypes = { "Text Only", "Invoke NuSMV" };
        outputChoiceTypeList = new JComboBox(outputChoiceTypes);
        outputChoiceTypeList.setSelectedIndex(0);
        controlPanel2.add(outputChoiceTypeList);
        controlPanel.add(controlPanel2);

        JPanel controlPanel3 = new JPanel();
        controlPanel3.setSize(400, 30);
        controlPanel3.add(new JLabel("Temporal Formula    "));
        formula = new JTextField(30);
        controlPanel3.add(formula);
        controlPanel.add(controlPanel3);

        JPanel controlPanel4 = new JPanel();
        controlPanel4.setSize(400, 30);
        controlPanel4.add(new JLabel("Variable Span Size  "));
        variableSpanSize = new JTextField(30);
        variableSpanSize.setText("0");
        controlPanel4.add(variableSpanSize);
        controlPanel.add(controlPanel4);

        JPanel controlPanel5 = new JPanel();
        controlPanel5.setSize(400, 30);
        controlPanel5.add(new JLabel("FSMActor Buffer Size"));
        bufferSize = new JTextField(30);
        bufferSize.setText("5");
        controlPanel5.add(bufferSize);
        controlPanel.add(controlPanel5);

        JScrollPane scrollPane = new JScrollPane(controlPanel);

        left.add(scrollPane, BorderLayout.CENTER);

        // Create a JTextAreaExec without Start and Cancel buttons.
        final JTextAreaExec exec = new JTextAreaExec(
                "Terminal (Verification Results)", false);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                left, exec);
        splitPane.setOneTouchExpandable(true);

        // Adjust the divider so that the control panel does not
        // have a horizontal scrollbar.
        Dimension preferred = left.getPreferredSize();
        splitPane.setDividerLocation(preferred.width + 20);

        getContentPane().add(splitPane, BorderLayout.CENTER);

        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                exec.clear();
            }
        });

        goButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    exec.updateStatusBar("// Starting " + modelConverter
                            + "model converting process.");

                    String modelType = (String) modelTypeList.getSelectedItem();
                    String inputTemporalFormula = formula.getText() == null ? ""
                            : formula.getText().trim();
                    String formulaType = (String) formulaTypeList
                            .getSelectedItem();
                    String varSpanSize = variableSpanSize.getText() == null ? ""
                            : variableSpanSize.getText().trim();
                    String outputChoice = (String) outputChoiceTypeList
                            .getSelectedItem();
                    String FSMBufferSize = bufferSize.getText() == null ? ""
                            : bufferSize.getText().trim();
                    if(formulaType.equalsIgnoreCase("Risk") || formulaType.equalsIgnoreCase("Reachability")){
                        inputTemporalFormula = modelConverter.generateGraphicalSpec(formulaType);
                        formulaType = "CTL";
                    }
                    
                    StringBuffer code = new StringBuffer("");
                    code.append(modelConverter.generateCode(modelType,
                            inputTemporalFormula, formulaType, varSpanSize,
                            outputChoice, FSMBufferSize));
                    
                    File codeFileNameWritten = modelConverter.getCodeFile();

                    if (codeFileNameWritten != null) {
                        Configuration configuration = getConfiguration();

                        URL codeURL = codeFileNameWritten.toURI().toURL();
                        // Use Thread.currentThread() so that this code will
                        // work under WebStart.
                        configuration.openModel(null, codeURL, codeURL
                                .toExternalForm());

                    }
                    
                    exec.updateStatusBar(code.toString());
                    exec.updateStatusBar("// Model conversion " + "complete.");
                } catch (Exception ex) {
                    MessageHandler.error("Conversion failed.", ex);
                }
            }
        });
    }

    // Private variable for GUI components 
    private JTextField bufferSize;
    private JPanel buttonPanel;
    private JPanel caveatsPanel;
    private JButton clearButton;
    private JPanel controlPanel;
    private JTextField formula;
    private JComboBox formulaTypeList;
    private JButton goButton;
    private JPanel left;
    private JTextArea messageArea;
    private JComboBox modelTypeList;
    private JButton moreInfoButton;
    private JComboBox outputChoiceTypeList;
    private JTextField variableSpanSize;

}
