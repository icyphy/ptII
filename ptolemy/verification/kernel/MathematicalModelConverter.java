/* Base class for mathematical model converter.

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
package ptolemy.verification.kernel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Random;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.domains.fsm.kernel.fmv.FmvAutomaton;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.verification.gui.MathematicalModelConverterGUIFactory;

// ////////////////////////////////////////////////////////////////////////
// // MathematicalModelConverter

/**
 * 
 * @author Chihhong Patrick Cheng, Contributors: Edward A. Lee , Christopher Brooks
 * @version $Id: MathematicalModelConverter.java,v 1.4 2008/03/06 09:16:22
 *          patrickj Exp $
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (patrickj)
 * @Pt.AcceptedRating Red (patrickj)
 */
public class MathematicalModelConverter extends Attribute {
    /**
     * Create a new instance of the code generator.
     * 
     * @param container The container.
     * @param name The name of the code generator.
     * @exception IllegalActionException
     *                    If the super class throws the exception or error
     *                    occurs when setting the file path.
     * @exception NameDuplicationException
     *                    If the super class throws the exception or an error
     *                    occurs when setting the file path.
     */
    public MathematicalModelConverter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:pink\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nconvert system.</text></svg>");

        _model = (CompositeEntity) getContainer();

        new MathematicalModelConverterGUIFactory(this,
                "_codeGeneratorGUIFactory");

    }

    // /////////////////////////////////////////////////////////////////
    // // parameters ////

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Generate the model description for the system. This is the main entry 
     * point.
     * 
     * @param code The code buffer into which to generate the code.
     * @return Textual format of the converted model based on the specification 
     *         given. 
     * @exception KernelException
     *                    If a type conflict occurs or the model is running.
     */
    public StringBuffer generateCode(String modelType,
            String inputTemporalFormula, String formulaType,
            String variableSpanSize, String outputChoice, String FSMBufferSize)
            throws Exception {
        StringBuffer returnStringBuffer = new StringBuffer("");
        // Perform deep traversal in order to generate .smv files.
        _codeFile = null;

        if (_model instanceof Actor) {

            if (_model instanceof CompositeActor) {

                if (REDUtility
                        .isValidModelForVerification((CompositeActor) _model)
                        || SMVUtility
                                .isValidModelForVerification((CompositeActor) _model)) {

                    StringBuffer systemDescription = new StringBuffer("");

                    if (modelType
                            .equalsIgnoreCase("Kripke Structures (Acceptable by NuSMV under SR)")) {

                        systemDescription.append(SMVUtility
                                .advancedGenerateSMVDescription(
                                        (CompositeActor) _model,
                                        inputTemporalFormula, formulaType,
                                        variableSpanSize));

                        if (outputChoice.equalsIgnoreCase("Text Only")) {
                            JFileChooser fileSaveDialog = new JFileChooser();
                            fileSaveDialog
                                    .setDialogType(JFileChooser.SAVE_DIALOG);
                            fileSaveDialog
                                    .setDialogTitle("Convert Ptolemy model into .smv file");
                            if (_directory != null) {
                                fileSaveDialog.setCurrentDirectory(_directory);
                            } else {
                                String cwd = StringUtilities
                                        .getProperty("user.dir");

                                if (cwd != null) {
                                    fileSaveDialog
                                            .setCurrentDirectory(new File(cwd));
                                }
                            }

                            int returnValue = fileSaveDialog
                                    .showSaveDialog(null);

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
                                            smvFileWriter
                                                    .write(systemDescription
                                                            .toString());
                                            _codeFile = smvFile;
                                        }
                                    } else {
                                        smvFileWriter = new FileWriter(smvFile);
                                        smvFileWriter.write(systemDescription
                                                .toString());
                                        _codeFile = smvFile;
                                    }

                                } finally {
                                    if (smvFileWriter != null) {
                                        smvFileWriter.close();
                                    }
                                }
                            }

                        } else {
                            // Invoke NuSMV. Create a temporal file and
                            // later delete it. We first create a new folder 
                            // which contains nothing. Then generate the System
                            // in format .smv, and perform model checking.
                            // If the system fails, all information would be
                            // stored in the folder. We can delete everything 
                            // in the folder then delete the folder.
                            // The temporal file uses a random number generator
                            // to generate its name.

                            Random rd = new Random();
                            String folderName = "SystemGeneratedTempFolder"
                                    + Integer.toString(rd.nextInt(10000)) + "/";
                            File smvFolder = new File(folderName);
                            if (smvFolder.exists()) {
                                while (smvFolder.exists() == true) {
                                    folderName = "SystemGeneratedTempFolder"
                                            + Integer.toString(rd
                                                    .nextInt(10000)) + "/";
                                    smvFolder = new File(folderName);
                                }
                                // Now create the directory.
                                boolean isOpened = smvFolder.mkdir();
                                if (isOpened == false) {
                                    MessageHandler
                                            .warning("Failed to invoke NuSMV correctly: \nUnable to open a temp folder.");
                                }
                            } else {
                                boolean isOpened = smvFolder.mkdir();
                                if (isOpened == false) {
                                    MessageHandler
                                            .warning("Failed to invoke NuSMV correctly:\nUnable to open a temp folder.");
                                }

                            }
                            // Now establish the file.
                            File smvFile = new File(folderName + "System.smv");
                            FileWriter smvFileWriter = null;
                            String fileAbsolutePath = smvFile.getAbsolutePath();

                            try {
                                smvFileWriter = new FileWriter(smvFile);
                                smvFileWriter.write(systemDescription
                                        .toString());

                            } finally {
                                if (smvFileWriter != null) {
                                    smvFileWriter.close();
                                }
                            }

                            StringBuffer str = new StringBuffer("");
                            BufferedReader reader = null;
                            try {
                                Runtime rt = Runtime.getRuntime();
                                Process pr = rt.exec("NuSMV " + "\""
                                        + fileAbsolutePath + "\"");
                                InputStreamReader inputStream = new InputStreamReader(
                                        pr.getInputStream());
                                reader = new BufferedReader(inputStream);
                                String line = null;
                                while ((line = reader.readLine()) != null) {
                                    str.append(line + "\n");
                                }

                            } catch (IOException ex) {
                                MessageHandler
                                        .warning("Failed to invoke NuSMV correctly: "
                                                + ex);

                            } finally {
                                reader.close();
                            }
                            returnStringBuffer.append(str);
                            _deleteFolder(smvFolder);
                            return returnStringBuffer;

                        }
                    } else { // Communicating Timed Automata for RED

                        systemDescription.append(REDUtility
                                .generateREDDescription(
                                        (CompositeActor) _model,
                                        inputTemporalFormula, formulaType,
                                        variableSpanSize, FSMBufferSize));

                        if (outputChoice.equalsIgnoreCase("Text Only")) {
                            JFileChooser fileSaveDialog = new JFileChooser();
                            fileSaveDialog
                                    .setDialogType(JFileChooser.SAVE_DIALOG);
                            fileSaveDialog
                                    .setDialogTitle("Convert Ptolemy model into .d file");
                            if (_directory != null) {
                                fileSaveDialog.setCurrentDirectory(_directory);
                            } else {

                                String cwd = StringUtilities
                                        .getProperty("user.dir");

                                if (cwd != null) {
                                    fileSaveDialog
                                            .setCurrentDirectory(new File(cwd));
                                }
                            }

                            int returnValue = fileSaveDialog
                                    .showSaveDialog(null);

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
                                            smvFileWriter
                                                    .write(systemDescription
                                                            .toString());
                                            _codeFile = smvFile;
                                        }
                                    } else {
                                        smvFileWriter = new FileWriter(smvFile);
                                        smvFileWriter.write(systemDescription
                                                .toString());
                                        _codeFile = smvFile;
                                    }

                                } finally {
                                    if (smvFileWriter != null) {
                                        smvFileWriter.close();
                                    }
                                }
                            }
                        } else {
                            MessageHandler
                                    .error("The functionality for invoking RED is not implemented.\n");
                        }
                    }

                } else {
                    MessageHandler
                            .error("The execution director is not SR or DE.\nCurrently it is beyond our scope of analysis.");
                }

            } else if (_model instanceof FSMActor) {

                // Retrieve the FSMActor and perform strong type conversion 
                // into FmvAutomaton.
                FmvAutomaton model = (FmvAutomaton) _model;

                StringBuffer fmvFormat = new StringBuffer("");
                FileWriter smvFileWriter = null;

                if (modelType
                        .equalsIgnoreCase("Kripke Structures (Acceptable by NuSMV under SR)")) {

                    if (outputChoice.equalsIgnoreCase("Text Only")) {
                        fmvFormat.append(model.convertToSMVFormat(
                                inputTemporalFormula, formulaType,
                                variableSpanSize));
                        JFileChooser fileSaveDialog = new JFileChooser();

                        fileSaveDialog.setDialogType(JFileChooser.SAVE_DIALOG);
                        fileSaveDialog
                                .setDialogTitle("Convert Ptolemy model into .smv file");
                        if (_directory != null) {
                            fileSaveDialog.setCurrentDirectory(_directory);
                        } else {
                            String cwd = StringUtilities
                                    .getProperty("user.dir");

                            if (cwd != null) {
                                fileSaveDialog
                                        .setCurrentDirectory(new File(cwd));
                            }
                        }

                        int returnValue = fileSaveDialog.showOpenDialog(null);
                        try {
                            if (returnValue == JFileChooser.APPROVE_OPTION) {
                                _directory = fileSaveDialog
                                        .getCurrentDirectory();

                                File smvFile = fileSaveDialog.getSelectedFile()
                                        .getCanonicalFile();

                                if (smvFile.exists()) {
                                    String queryString = "Overwrite "
                                            + smvFile.getName() + "?";
                                    int selected = JOptionPane
                                            .showOptionDialog(
                                                    null,
                                                    queryString,
                                                    "Overwrite?",
                                                    JOptionPane.YES_NO_OPTION,
                                                    JOptionPane.QUESTION_MESSAGE,
                                                    null, null, null);
                                    if (selected == 0) {
                                        smvFileWriter = new FileWriter(smvFile);
                                        smvFileWriter.write(fmvFormat
                                                .toString());
                                        _codeFile = smvFile;
                                    }
                                } else {
                                    smvFileWriter = new FileWriter(smvFile);
                                    smvFileWriter.write(fmvFormat.toString());
                                    _codeFile = smvFile;
                                }

                            }

                        } catch (IOException ex) {
                            MessageHandler
                                    .error("Failed to perform the file closing process:\n"
                                            + ex.getMessage());
                        } finally {
                            if (smvFileWriter != null)
                                smvFileWriter.close();
                        }

                    } else {
                        // Invoke NuSMV
                        fmvFormat.append(model.convertToSMVFormat(
                                inputTemporalFormula, formulaType,
                                variableSpanSize));
                        // Also invoke NuSMV. Create a temporal file and later
                        // delete it. We first create a new folder which contains 
                        // nothing. Then generate the System.smv file, perform model
                        // checking. If the system fails, all information would be 
                        // stored in the folder. We can delete everything in the 
                        // folder then delete the folder. The temporal file uses 
                        // a random number generator to generate its name.
                        Random rd = new Random();
                        String folderName = "SystemGeneratedTempFolder"
                                + Integer.toString(rd.nextInt(10000)) + "/";
                        File smvFolder = new File(folderName);
                        if (smvFolder.exists()) {
                            while (smvFolder.exists() == true) {

                                folderName = "SystemGeneratedTempFolder"
                                        + Integer.toString(rd.nextInt(10000))
                                        + "/";
                                smvFolder = new File(folderName);
                            }
                            // Now create the directory.
                            boolean isOpened = smvFolder.mkdir();
                            if (isOpened == false) {
                                MessageHandler
                                        .warning("Failed to invoke NuSMV correctly: \nUnable to open a temp folder.");
                            }
                        } else {
                            boolean isOpened = smvFolder.mkdir();
                            if (isOpened == false) {
                                MessageHandler
                                        .warning("Failed to invoke NuSMV correctly: \nUnable to open a temp folder.");
                            }
                        }
                        // Now establish the file.
                        File smvFile = new File(folderName + "System.smv");
                        String fileAbsolutePath = smvFile.getAbsolutePath();

                        try {
                            smvFileWriter = new FileWriter(smvFile);
                            smvFileWriter.write(fmvFormat.toString());

                        } finally {
                            if (smvFileWriter != null) {
                                smvFileWriter.close();
                            }
                        }

                        //StringBuffer str = new StringBuffer("");
                        BufferedReader reader = null;
                        try {
                            Runtime rt = Runtime.getRuntime();
                            Process pr = rt.exec("NuSMV " + "\""
                                    + fileAbsolutePath + "\"");
                            InputStreamReader inputStream = new InputStreamReader(
                                    pr.getInputStream());
                            reader = new BufferedReader(inputStream);
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                //str.append(line + "\n");
                                returnStringBuffer.append(line + "\n");
                            }

                        } catch (IOException ex) {
                            MessageHandler
                                    .warning("Failed to invoke NuSMV correctly: "
                                            + ex.getMessage());

                        } finally {
                            reader.close();
                        }
                        //returnStringBuffer.append(str);
                        _deleteFolder(smvFolder);

                        return returnStringBuffer;
                    }
                }

            }

        }

        return returnStringBuffer;
    }

    /** 
     * This is the main entry point to generate the graphical spec of the system.
     * It would invoke SMVUtility.generateGraphicalSpecification and return
     * the spec.
     * 
     * @param formulaType The type of the graphical spec. It may be either "Risk"
     *                    or "Reachability".
     * @return The textual format of the graphical spec.
     * @throws IllegalActionException
     */
    public String generateGraphicalSpec(String formulaType)
            throws IllegalActionException {

        if (_model instanceof CompositeActor) {
            return SMVUtility.generateGraphicalSpecification(
                    (CompositeActor) _model, formulaType);
        } else {
            throw new IllegalActionException(
                    "SMVUtility.generateGraphicalSpec error:\nModel not instance of CompositeActor");
        }
    }

    public File getCodeFile() {
        return _codeFile;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** The name of the file that was written. If no file was written, then the
     * value is null.
     */
    protected File _codeFile = null;

    protected File _directory;

    /** The model we for which we are generating code. */
    protected CompositeEntity _model;

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

    /** This is used to delete recursively the folder and files within.
     */
    private void _deleteFolder(File folder) throws Exception {

        if (folder.list() == null || folder.list().length <= 0) {
            boolean isDeleted = folder.delete();
            if (isDeleted == false) {
                throw new IllegalActionException(
                        "Temporary subfolder delete unsuccessful");
            }
        } else {
            for (int i = 0; i < folder.list().length; i++) {
                String childName = folder.list()[i];
                String childPath = folder.getPath() + File.separator
                        + childName;
                File filePath = new File(childPath);
                if (filePath.exists() && filePath.isFile()) {
                    boolean isDeleted = filePath.delete();
                    if (isDeleted == false) {
                        throw new IllegalActionException(
                                "Temporary file delete unsuccessful");
                    }
                } else if (filePath.exists() && filePath.isDirectory()) {
                    _deleteFolder(filePath);
                }
            }
            boolean isDeleted = folder.delete();
            if (isDeleted == false) {
                throw new IllegalActionException(
                        "Temporary folder delete unsuccessful");
            }
        }

    }

}
