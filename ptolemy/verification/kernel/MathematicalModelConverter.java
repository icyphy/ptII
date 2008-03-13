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
import ptolemy.verification.gui.MathematicalModelConverterGUIFactory;

// ////////////////////////////////////////////////////////////////////////
// // MathematicalModelConverter

/**
 * 
 * @author Chihhong Patrick Cheng, Contributors: Edward A. Lee , Christopher
 *         Brooks
 * @version $Id: MathematicalModelConverter.java,v 1.4 2008/03/06 09:16:22
 *          patrickj Exp $
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Red (patrickj)
 * @Pt.AcceptedRating Red ()
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
     * Generate code. This is the main entry point.
     * 
     * @param code The code buffer into which to generate the code.
     * @return The return value of the last subprocess that was executed. or -1
     *         if no commands were executed.
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
                            // .showOpenDialog(ActorGraphFrame.this);

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
                            // Also invoke NuSMV. Create a temporal file and
                            // later delete it.
                            // We first create a new folder which contains
                            // nothing.
                            // Then generate the System.smv file, perform model
                            // checking.
                            // If the system fails, all information would be
                            // stored in the folder.
                            // We can delete everything in the folder then
                            // delete the folder.
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
                            // SMVFileFilter filter = new SMVFileFilter();
                            // fileSaveDialog.setFileFilter(filter);
                            fileSaveDialog
                                    .setDialogType(JFileChooser.SAVE_DIALOG);
                            fileSaveDialog
                                    .setDialogTitle("Convert Ptolemy model into .d file");
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
                                    .error("This is not executable by NuSMV.\n");
                        }
                    }

                } else {
                    MessageHandler
                            .error("The execution director is not SR or DE.\nCurrently it is beyond our scope of analysis.");
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

                    if (query.getStringValue("outputChoice").equalsIgnoreCase(
                            "Output to File")) {
                        // try {
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
                            // The default on Windows is to open at user.home,
                            // which is
                            // typically an absurd directory inside the O/S
                            // installation.
                            // So we use the current directory instead.
                            // FIXME: Could this throw a security exception in
                            // an
                            // applet?
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

                    } else if (query.getStringValue("outputChoice")
                            .equalsIgnoreCase("Open Text Editor")) {
                        // try {
                        fmvFormat.append(model.convertToSMVFormat(pattern,
                                finalChoice, span));

                        Query newQuery = new Query();
                        newQuery.setTextWidth(90);
                        newQuery.addTextArea("formula", _model.getName(),
                                fmvFormat.toString());
                        ComponentDialog newDialog = new ComponentDialog(null,
                                "Converted SMV Format", newQuery);

                        // } catch (Exception ex) {
                        // MessageHandler
                        // .error("Failed to perform the conversion process:\n"
                        // + ex.getMessage());
                        // }
                    } else {
                        // Also invoke NuSMV

                        fmvFormat.append(model.convertToSMVFormat(pattern,
                                finalChoice, span));

                        // Also invoke NuSMV. Create a temporal file and later
                        // delete it.
                        // We first create a new folder which contains nothing.
                        // Then generate the System.smv file, perform model
                        // checking.
                        // If the system fails, all information would be stored
                        // in the folder.
                        // We can delete everything in the folder then delete
                        // the folder.
                        // The temporal file uses a random number generator to
                        // generate its name.
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
                        // FileWriter smvFileWriter = null;
                        String fileAbsolutePath = smvFile.getAbsolutePath();

                        try {
                            smvFileWriter = new FileWriter(smvFile);
                            smvFileWriter.write(fmvFormat.toString());

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
                                            + ex.getMessage());

                        } finally {
                            reader.close();
                        }
                        returnStringBuffer.append(str);
                        _deleteFolder(smvFolder);

                        return returnStringBuffer;
                    }

                }
            }

        }

        return returnStringBuffer;
        // return returnValue;
    }

    public String generateGraphicalSpec(String formulaType) throws IllegalActionException{
        
        if(_model instanceof CompositeActor){
            return SMVUtility.generateGraphicalSpecification((CompositeActor)_model, formulaType);
        } else {
            throw new IllegalActionException("SMVUtility.generateGraphicalSpec error:\nModel not instance of CompositeActor");
        }
    }
    
    
    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** The model we for which we are generating code. */
    protected CompositeEntity _model;

    protected File _directory;

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

    public String getCodeFileName() {
        return _codeFileName;
    }

    public File getCodeFile() {
        return _codeFile;
    }

    /**
     * The name of the file that was written. If no file was written, then the
     * value is null.
     */
    protected String _codeFileName = null;
    protected File _codeFile = null;

}
