/* Base class for code generators.

   Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.verification.kernel;

import java.io.File;
import java.io.FileWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.domains.fsm.kernel.fmv.FmvAutomaton;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.verification.gui.CodeGeneratorGUIFactory;



//////////////////////////////////////////////////////////////////////////
//// CodeGenerator

/** Base class for code generator.
 *
 *  @author  Chihhong Patrick Cheng, Contributors: Edward A. Lee , Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 6.0
 *  @Pt.ProposedRating Red (patrickj)
 *  @Pt.AcceptedRating Red ()
 */
public class CodeGenerator extends Attribute {
    /** Create a new instance of the code generator.
     *  @param container The container.
     *  @param name The name of the code generator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public CodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:pink\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\ngenerate code.</text></svg>");

        _model = (CompositeEntity) getContainer();

        new CodeGeneratorGUIFactory(this, "_codeGeneratorGUIFactory");

    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate code.  This is the main entry point.
     *  @param code The code buffer into which to generate the code.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If a type conflict occurs or the model
     *  is running.
     */
    public int generateCode() throws Exception {

        //int returnValue = -1;

        // Perform deep traversal in order to generate .smv files.

        if (_model instanceof Actor) {

            if (_model instanceof CompositeActor) {

                if (SMVUtility
                        .isValidModelForVerification((CompositeActor) _model)) {
                    StringBuffer smvDescritpion = new StringBuffer("");

                    Query query = new Query();

                    query.addLine("formula", "Input temporal formula", "");
                    String[] possibleFormulaChoice = new String[2];
                    possibleFormulaChoice[0] = "CTL";
                    possibleFormulaChoice[1] = "LTL";
                    query.addRadioButtons("formulaChoice", "Formula Type",
                            possibleFormulaChoice, "CTL");

                    query.addLine("span", "Size of span", "0");
                    String[] possibleOutputChoice = new String[3];
                    possibleOutputChoice[0] = "Output to File";
                    possibleOutputChoice[1] = "Open Text Editor";
                    possibleOutputChoice[2] = "Invoke NuSMV";
                    query.addRadioButtons("outputChoice", "Output Choice",
                            possibleOutputChoice, "Output to File");
                    //query.addDisplay("note", "", "To invoke NuSMV, the system must have it properly installed.");
                    ComponentDialog dialog = new ComponentDialog(null,
                            "Input Formula", query);

                    String pattern = "";
                    String finalFormulaChoice = "";
                    String finalOutputChoice = "";
                    String span = "";
                    if (dialog.buttonPressed().equals("OK")) {
                        pattern = query.getStringValue("formula");
                        finalFormulaChoice = query
                                .getStringValue("formulaChoice");
                        span = query.getStringValue("span");
                        smvDescritpion.append(SMVUtility
                                .generateSMVDescription(
                                        (CompositeActor) _model, pattern,
                                        finalFormulaChoice, span));

                        if (query.getStringValue("outputChoice")
                                .equalsIgnoreCase("Output to File")) {
                            JFileChooser fileSaveDialog = new JFileChooser();
                            // SMVFileFilter filter = new SMVFileFilter();
                            // fileSaveDialog.setFileFilter(filter);
                            fileSaveDialog
                                    .setDialogType(JFileChooser.SAVE_DIALOG);
                            fileSaveDialog
                                    .setDialogTitle("Convert Ptolemy model into .smv file");
                            if (_directory != null) {
                                fileSaveDialog.setCurrentDirectory(_directory);
                            } else {
                                // The default on Windows is to open at
                                // user.home, which is typically an absurd
                                // directory inside the O/S installation.
                                // So we use the current directory instead.
                                // FIXME: Could this throw a security
                                // exception in an applet?
                                String cwd = StringUtilities
                                        .getProperty("user.dir");

                                if (cwd != null) {
                                    fileSaveDialog
                                            .setCurrentDirectory(new File(cwd));
                                }
                            }

                            int returnValue = fileSaveDialog
                                    .showSaveDialog(null);
                            //.showOpenDialog(ActorGraphFrame.this);

                            if (returnValue == JFileChooser.APPROVE_OPTION) {
                                _directory = fileSaveDialog
                                        .getCurrentDirectory();

                                FileWriter smvFileWriter = null;
                                try {
                                    File smvFile = fileSaveDialog
                                            .getSelectedFile()
                                            .getCanonicalFile();

                                    if (smvFile.exists()) {
                                        String queryString = "Overwrite "
                                                + smvFile.getName() + "?";
                                        int selected = JOptionPane
                                                .showOptionDialog(
                                                        null,
                                                        queryString,
                                                        "Save Changes?",
                                                        JOptionPane.YES_NO_OPTION,
                                                        JOptionPane.QUESTION_MESSAGE,
                                                        null, null, null);
                                        if (selected == 0) {
                                            smvFileWriter = new FileWriter(
                                                    smvFile);
                                            smvFileWriter.write(smvDescritpion
                                                    .toString());
                                        }
                                    }

                                } finally {
                                    if (smvFileWriter != null) {
                                        smvFileWriter.close();
                                    }
                                }
                            }
                        } else if (query.getStringValue("outputChoice")
                                .equalsIgnoreCase("Open Text Editor")) {
                           
                        } else {
                            // Also invoke NuSMV. Create a temporal file and later delete it.
                            // The temporal file uses a random number generator to generate its name.
                        }

                    }
                } else {
                    MessageHandler
                            .error("The execution director is not SR.\nCurrently it is beyond our scope.");
                }

            } else if (_model instanceof FSMActor) {

                Query query = new Query();
                query.addLine("formula", "Temporal formula", "");
                String[] possibleFormulaChoice = new String[2];
                possibleFormulaChoice[0] = "CTL";
                possibleFormulaChoice[1] = "LTL";
                query.addRadioButtons("choice", "Formula Type",
                        possibleFormulaChoice, "CTL");
                query.addLine("span", "Size of span", "0");
                String[] possibleOutputChoice = new String[3];
                possibleOutputChoice[0] = "Output to File";
                possibleOutputChoice[1] = "Open Text Editor";
                possibleOutputChoice[2] = "Invoke NuSMV";
                query.addRadioButtons("outputChoice", "Output Choice",
                        possibleOutputChoice, "Output to File");

                ComponentDialog dialog = new ComponentDialog(null,
                        "Input Formula", query);

                String pattern = "";
                String finalChoice = "";
                String span = "";
                if (dialog.buttonPressed().equals("OK")) {
                    pattern = query.getStringValue("formula");
                    finalChoice = query.getStringValue("choice");
                    span = query.getStringValue("span");
                    // Retrieve the Fmv Automaton
                    FmvAutomaton model = (FmvAutomaton) _model;
                    // StringBuffer = model

                    StringBuffer fmvFormat = new StringBuffer("");
                    FileWriter smvFileWriter = null;
                    try {

                        fmvFormat.append(model.convertToSMVFormat(pattern,
                                finalChoice, span));
                        JFileChooser fileSaveDialog = new JFileChooser();
                        // SMVFileFilter filter = new SMVFileFilter();
                        // fileSaveDialog.setFileFilter(filter);
                        fileSaveDialog.setDialogType(JFileChooser.SAVE_DIALOG);
                        fileSaveDialog
                                .setDialogTitle("Convert Ptolemy model into .smv file");
                        if (_directory != null) {
                            fileSaveDialog.setCurrentDirectory(_directory);
                        } else {
                            // The default on Windows is to open at user.home, which is
                            // typically an absurd directory inside the O/S
                            // installation.
                            // So we use the current directory instead.
                            // FIXME: Could this throw a security exception in an
                            // applet?
                            String cwd = StringUtilities
                                    .getProperty("user.dir");

                            if (cwd != null) {
                                fileSaveDialog
                                        .setCurrentDirectory(new File(cwd));
                            }
                        }

                        int returnValue = fileSaveDialog.showOpenDialog(null);

                        if (returnValue == JFileChooser.APPROVE_OPTION) {
                            _directory = fileSaveDialog.getCurrentDirectory();

                            File smvFile = fileSaveDialog.getSelectedFile()
                                    .getCanonicalFile();

                            if (smvFile.exists()) {
                                String queryString = "Overwrite "
                                        + smvFile.getName() + "?";
                                int selected = JOptionPane.showOptionDialog(
                                        null, queryString, "Overwrite?",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE, null,
                                        null, null);
                                if (selected == 0) {
                                    smvFileWriter = new FileWriter(smvFile);
                                    smvFileWriter.write(fmvFormat.toString());
                                }
                            }

                        }

                    } catch (Exception ex) {
                        MessageHandler
                                .error("Failed to perform the conversion process:\n"
                                        + ex.getMessage());
                    }
                    try {
                        if (smvFileWriter != null)
                            smvFileWriter.close();
                    } catch (Exception ex) {
                        MessageHandler
                                .error("Failed to perform the file closing process:\n"
                                        + ex.getMessage());
                    }

                }
            }

        }

        return 0;
        //return returnValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The model we for which we are generating code. */
    protected CompositeEntity _model;

    protected File _directory;

}
