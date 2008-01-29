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

import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

import ptolemy.verification.gui.CodeGeneratorGUIFactory;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;

import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;

import ptolemy.kernel.CompositeEntity;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

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
                    String[] possibleChoice = new String[2];
                    possibleChoice[0] = "CTL";
                    possibleChoice[1] = "LTL";
                    query.addRadioButtons("choice", "Formula Type",
                            possibleChoice, "CTL");
                    query.addLine("span", "Size of span", "0");
                    ComponentDialog dialog = new ComponentDialog(null,
                            "Input Formula", query);

                    String pattern = "";
                    String finalChoice = "";
                    String span = "";
                    if (dialog.buttonPressed().equals("OK")) {
                        pattern = query.getStringValue("formula");
                        finalChoice = query.getStringValue("choice");
                        span = query.getStringValue("span");
                        smvDescritpion.append(SMVUtility
                                .generateSMVDescription(
                                        (CompositeActor) _model, pattern,
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

                        int returnValue = fileSaveDialog.showSaveDialog(null);
                        //.showOpenDialog(ActorGraphFrame.this);

                        if (returnValue == JFileChooser.APPROVE_OPTION) {
                            _directory = fileSaveDialog.getCurrentDirectory();

                            FileWriter smvFileWriter = null;
                            try {
                                File smvFile = fileSaveDialog.getSelectedFile()
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
                                    if (selected == 1) {
                                        smvFileWriter = new FileWriter(smvFile);
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

                    }
                } else {
                    MessageHandler
                            .error("The execution director is not SR.\nCurrently it is beyond our scope.");
                }

            }

        }

        return 0;
        //return returnValue;
    }

    /** Generate code and append it to the given string buffer.

     *  Write the code to the directory specified by the codeDirectory
     *  parameter.  The file name is a sanitized version of the model
     *  name with a suffix that is based on last package name of the
     *  <i>generatorPackage</i> parameter.  Thus if the
     *  <i>codeDirectory</i> is <code>$HOME</code>, the name of the
     *  model is <code>Foo</code> and the <i>generatorPackage</i>
     *  is <code>ptolemy.codegen.c</code>, then the file that is
     *  written will be <code>$HOME/Foo.c</code>
     *  This method is the main entry point.
     *  @param code The given string buffer.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If the target file cannot be overwritten
     *   or write-to-file throw any exception.
     */
    public int _generateCode(StringBuffer code) throws KernelException {
        return 3;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The model we for which we are generating code. */
    protected CompositeEntity _model;

    protected File _directory;

}
