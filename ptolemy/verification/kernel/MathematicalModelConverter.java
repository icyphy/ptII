/* Base class for mathematical model converter.

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
package ptolemy.verification.kernel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import ptolemy.actor.CompositeActor;
import ptolemy.data.expr.ChoiceParameter;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.fmv.FmvAutomaton;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringBufferExec;
import ptolemy.verification.gui.MathematicalModelConverterGUIFactory;
import ptolemy.verification.kernel.maude.RTMaudeUtility;

///////////////////////////////////////////////////////////////////
//// MathematicalModelConverter

/**
 * @deprecated ptolemy.de.lib.TimedDelay is deprecated, use ptolemy.actor.lib.TimeDelay.
 * @author Chihhong Patrick Cheng   (modified by: Kyungmin Bae)   Contributors: Edward A. Lee , Christopher Brooks,
 * @version $Id: MathematicalModelConverter.java,v 1.5 2008/03/06 09:16:22
 *          patrickj Exp $
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (patrickj)
 * @Pt.AcceptedRating Red (patrickj)
 */
@Deprecated
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

        target = new FileParameter(this, "target", true);
        target.setDisplayName("Target File");

        template = new FileParameter(this, "template", true);
        template.setDisplayName("Template File");

        modelType = new ChoiceParameter(this, "modelType", ModelType.class);
        modelType.setExpression(ModelType.Maude.toString());
        modelType.setDisplayName("Model Type");

        formulaType = new ChoiceParameter(this, "Formula Type",
                FormulaType.class);
        formulaType.setExpression(FormulaType.CTL.toString());
        formulaType.setDisplayName("Formula Type");

        outputType = new ChoiceParameter(this, "Output Type", OutputType.class);
        outputType.setExpression(OutputType.Text.toString());
        outputType.setDisplayName("Output Type");

        formula = new StringParameter(this, "formula");
        formula.setDisplayName("Temporal Formula");

        span = new Parameter(this, "span");
        span.setTypeEquals(BaseType.INT);
        span.setExpression("0");
        span.setDisplayName("Variable Span Size");

        buffer = new Parameter(this, "buffer");
        buffer.setTypeEquals(BaseType.INT);
        buffer.setExpression("1");
        buffer.setDisplayName("DelayedActor Buffer Size");

        _attachText("_iconDescription", "<svg>n"
                + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:pink\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nconvert system.</text></svg>");

        _model = (CompositeEntity) getContainer();

        new MathematicalModelConverterGUIFactory(this,
                "_codeGeneratorGUIFactory");

    }

    ///////////////////////////////////////////////////////////////////
    // public methods ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new object.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MathematicalModelConverter newObject = (MathematicalModelConverter) super
                .clone(workspace);
        // setContainer() should be called after cloning.
        newObject._model = null;
        return newObject;
    }

    public StringBuffer generateCode(ModelType modelType,
            String inputTemporalFormula, FormulaType formulaType,
            int variableSpanSize, int delayActorBufferSize)
                    throws IllegalActionException, NameDuplicationException,
                    CloneNotSupportedException {
        StringBuffer systemDescription = new StringBuffer("");

        switch (modelType) {
        case Kripke:
            if (_model instanceof CompositeActor) {
                systemDescription.append(SMVUtility.generateSMVDescription(
                        (CompositeActor) _model.clone(), inputTemporalFormula,
                        formulaType.toString(),
                        String.valueOf(variableSpanSize)));
            } else {
                systemDescription.append(((FmvAutomaton) _model.clone())
                        .convertToSMVFormat(inputTemporalFormula, formulaType,
                                variableSpanSize));
            }
            break;
        case CTA:
            systemDescription.append(REDUtility.generateREDDescription(
                    (CompositeActor) _model.clone(), inputTemporalFormula,
                    formulaType, variableSpanSize, delayActorBufferSize));
            break;
        case Maude:
            if (_model instanceof CompositeActor) {
                if (template.getExpression().trim().equals("")) {
                    systemDescription.append(RTMaudeUtility
                            .generateRTMDescription((CompositeActor) _model,
                                    inputTemporalFormula, true));
                } else {
                    systemDescription.append(RTMaudeUtility
                            .generateRTMDescription(template.openForReading(),
                                    (CompositeActor) _model,
                                    inputTemporalFormula));
                }
            }
            break;
        }

        return systemDescription;
    }

    /**
     * Generate the model description for the system. This is the main entry
     * point.
     *
     * @return Textual format of the converted model based on the specification
     *         given.
     */
    public StringBuffer generateFile(File file, ModelType modelType,
            String inputTemporalFormula, FormulaType formulaType,
            int variableSpanSize, OutputType outputChoice, int FSMBufferSize)
                    throws IllegalActionException, NameDuplicationException,
                    CloneNotSupportedException, IOException {
        StringBuffer returnStringBuffer = new StringBuffer("");
        _codeFile = null;

        if (_model instanceof CompositeActor || _model instanceof FSMActor) {

            if (REDUtility.isValidModelForVerification((CompositeActor) _model)
                    || SMVUtility
                    .isValidModelForVerification((CompositeActor) _model)
                    || _model instanceof FSMActor) {

                StringBuffer systemDescription = generateCode(modelType,
                        inputTemporalFormula, formulaType, variableSpanSize,
                        FSMBufferSize);
                if (outputChoice == OutputType.Text) {
                    FileWriter writer = null;
                    try {
                        writer = new FileWriter(file);
                        writer.write(systemDescription.toString());
                        _codeFile = file;
                    } finally {
                        if (writer != null) {
                            writer.close();
                        }
                    }
                    System.out.println("Wrote " + file);
                } else {
                    if (modelType == ModelType.Kripke) {
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
                                        + Integer.toString(rd.nextInt(10000))
                                        + "/";
                                smvFolder = new File(folderName);
                            }
                            // Now create the directory.
                            boolean isOpened = smvFolder.mkdir();
                            if (isOpened == false) {
                                throw new IllegalActionException(
                                        "Failed to "
                                                + "invoke NuSMV correctly: \nUnable to "
                                                + "open a temp folder.");
                            }
                        } else {
                            boolean isOpened = smvFolder.mkdir();
                            if (isOpened == false) {
                                throw new IllegalActionException("Failed to "
                                        + "invoke NuSMV correctly:\nUnable to "
                                        + "open a temp folder.");
                            }

                        }
                        // Now establish the file.
                        File smvFile = new File(folderName + "System.smv");
                        String fileAbsolutePath = smvFile.getAbsolutePath();

                        FileWriter writer = null;
                        try {
                            writer = new FileWriter(smvFile);
                            writer.write(systemDescription.toString());

                        } finally {
                            if (writer != null) {
                                writer.close();
                            }
                        }

                        List execCommands = new LinkedList();
                        execCommands.add("NuSMV " + "\"" + fileAbsolutePath
                                + "\"");
                        StringBufferExec exec = new StringBufferExec();

                        System.out
                                .println("MathematicalModelConverter: About to execute: "
                                        + execCommands);
                        exec.setCommands(execCommands);
                        exec.setWaitForLastSubprocess(true);
                        try {
                            exec.start();
                        } catch (Throwable throwable) {
                            StringBuffer errorMessage = new StringBuffer();
                            Iterator<String> allCommands = execCommands
                                    .iterator();
                            while (allCommands.hasNext()) {
                                errorMessage.append(allCommands.next() + "\n");
                            }
                            throw new IllegalActionException(
                                    "Problem executing the " + "commands:\n"
                                            + errorMessage + "\n" + throwable);
                        }

                        returnStringBuffer = exec.buffer;
                        if (exec.getLastSubprocessReturnCode() != 0) {
                            System.err.println("Executing " + execCommands
                                    + " returned non-zero: "
                                    + exec.getLastSubprocessReturnCode()
                                    + ".  Not deleting " + smvFolder);
                        } else {
                            _deleteFolder(smvFolder);
                        }

                        return returnStringBuffer;
                    } else {
                        MessageHandler
                        .error("The functionality for invoking RED is not implemented.\n");
                    }
                }

            } else {
                MessageHandler
                .error("The execution director is not SR or DE.\nCurrently it is beyond our scope of analysis.");
            }
        }

        return returnStringBuffer;
    }

    /**
     * This is the main entry point to generate the graphical specification of the system.
     * It would invoke SMVUtility.generateGraphicalSpecification and return
     * the specification.
     *
     * @param formulaType The type of the graphical specification. It may be either "Risk"
     *                    or "Reachability".
     * @return The textual format of the graphical specification.
     * @exception IllegalActionException
     */
    public String generateGraphicalSpec(FormulaType formulaType)
            throws IllegalActionException {

        if (_model instanceof CompositeActor) {
            return SMVUtility.generateGraphicalSpecification(
                    (CompositeActor) _model, formulaType.toString());
        } else {
            throw new IllegalActionException(
                    "SMVUtility.generateGraphicalSpec error:\nModel not instance of CompositeActor");
        }
    }

    public File getCodeFile() {
        return _codeFile;
    }

    public enum ModelType {
        CTA {
            @Override
            public String toString() {
                return "Communicating Timed Automata (Acceptable by RED "
                        + "under DE)";
            }
        },
        Kripke {
            @Override
            public String toString() {
                return "Kripke Structures (Acceptable by NuSMV under SR)";
            }
        },
        Maude {
            @Override
            public String toString() {
                return "Real-time Maude Translation(under SR or DE)";
            }
        }
    }

    public enum FormulaType {
        CTL, LTL, TCTL, Buffer {
            @Override
            public String toString() {
                return "Buffer Overflow";
            }
        },
        Risk, Reachability
    }

    public enum OutputType {
        Text {
            @Override
            public String toString() {
                return "Text Only";
            }
        },
        SMV {
            @Override
            public String toString() {
                return "Invoke NuSMV";
            }
        }
    }

    /** Set the model to the container.
     *  @param container
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace, or
     *   if the protected method _checkContainer() throws it, or if
     *   a contained Settable becomes invalid and the error handler
     *   throws it.
     *  @exception NameDuplicationException If the name of this entity
     *   collides with a name already in the container.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);
        _model = container;
    }

    ///////////////////////////////////////////////////////////////////
    // protected variables ////

    /** The name of the file that was written. If no file was written, then the
     * value is null.
     */
    protected File _codeFile = null;

    protected File _directory;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** The model we for which we are generating code. */
    protected CompositeEntity _model;

    /** This is used to delete recursively the folder and files within.
     */
    private void _deleteFolder(File folder) throws IllegalActionException,
    IOException {

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

    public FileParameter target;

    public FileParameter template;

    public ChoiceParameter modelType;

    public ChoiceParameter formulaType;

    public ChoiceParameter outputType;

    public StringParameter formula;

    public Parameter span;

    public Parameter buffer;
}
