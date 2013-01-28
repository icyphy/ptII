/* An actor that executes a Modelica script.

 Below is the copyright agreement for the Ptolemy II system.

 Copyright (c) 2012-2013 The Regents of the University of California
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.domains.openmodelica.lib;

import java.io.File;
import java.io.IOException;
import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.openmodelica.lib.compiler.CompilerResult;
import ptolemy.domains.openmodelica.lib.compiler.ConnectException;
import ptolemy.domains.openmodelica.lib.omc.OMCLogger;
import ptolemy.domains.openmodelica.lib.omc.OMCProxy;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.plot.compat.PxgraphApplication;
import ptolemy.util.StringUtilities;

/**
    An actor that executes a Modelica script. it translates and
    simulates the model.  There is one actor provided in the Vergil,
    <i>MoreLibraries</i> Under <i>OpenModelica</i>.  It is called
    <i>OpenModelica</i>; To view or edit its Modelica script, look
    inside the actor.

    <p>The OpenModelica actor works for the model which is composed of only one class.</p>

   @author Mana Mirzaei
   @version $Id$
   @since Ptolemy II 9.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
 */
public class OpenModelica extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public OpenModelica(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output = new TypedIOPort(this, "input", false, true);

        modelicaScript = new StringParameter(this, "modelicaScript");
        modelicaScript.setDisplayName("Write OpenModelica Command");

        fileName = new FileParameter(this, "fileName");
        fileName.setDisplayName("File name");

        modelName = new StringParameter(this, "modelName");
        modelName.setTypeEquals(BaseType.STRING);
        modelName.setDisplayName("Model name");

        simulationStartTime = new Parameter(this, "simulationStartTime",
                new DoubleToken(0.0));
        simulationStartTime.setTypeEquals(BaseType.DOUBLE);
        simulationStartTime.setDisplayName("Simulation start time");

        simulationStopTime = new Parameter(this, "simulationStopTime",
                new DoubleToken(0.1));
        simulationStopTime.setTypeEquals(BaseType.DOUBLE);
        simulationStopTime.setDisplayName("Simulation stop time");

        numberOfIntervals = new Parameter(this, "numberOfIntervals",
                new IntToken(500));
        numberOfIntervals.setTypeEquals(BaseType.INT);
        numberOfIntervals.setDisplayName("Number of intervals");

        tolerance = new Parameter(this, "tolerance", new DoubleToken(0.0001));
        tolerance.setTypeEquals(BaseType.DOUBLE);
        tolerance.setDisplayName("Tolerance");

        method = new StringParameter(this, "method");
        method.setTypeEquals(BaseType.STRING);
        method.setDisplayName("Method");
        method.setExpression("dassl");

        fileNamePrefix = new StringParameter(this, "fileNamePrefix");
        fileNamePrefix.setTypeEquals(BaseType.STRING);
        fileNamePrefix.setDisplayName("File name prefix");

        outputFormat = new StringParameter(this, "outputFormat");
        outputFormat.setDisplayName("Output format");
        outputFormat.setExpression("mat");
        outputFormat.addChoice("mat");
        outputFormat.addChoice("csv");
        outputFormat.addChoice("plt");
        outputFormat.addChoice("empty");

        variableFilter = new StringParameter(this, "variableFilter");
        variableFilter.setTypeEquals(BaseType.STRING);
        variableFilter.setDisplayName("Variable filter");
        variableFilter.setExpression(".*");

        cflags = new StringParameter(this, "cflags");
        cflags.setTypeEquals(BaseType.STRING);

        simflags = new StringParameter(this, "simflags");
        simflags.setTypeEquals(BaseType.STRING);
        simflags.setDisplayName("Simulation flag");
    }

    ///////////////////////////////////////////////////////////////////
    ////                    public ports and parameters           ////

    /** Any standard C language flags.
     *  The default value of this parameter is empty.
     */
    public StringParameter cflags;

    /** File which the model should be loaded from.  
     *  There is no default value, file should be selected.
     */
    public FileParameter fileName;

    /** User preferable name for the result file.
     *  The default value of this parameter is null.
     */
    public StringParameter fileNamePrefix;

    /** Integration method used for simulation.  
     *  The default value of this parameter is the string "dassl".
     */
    public StringParameter method;

    /** Modelica command.  
     *  The default value of this parameter is the string "loadModel(Modelica)".
     */
    public StringParameter modelicaScript;

    /** Name of the model which should be built. 
     *  The default value of this parameter is the string "dcmotor".
     */
    public StringParameter modelName;

    /** Number of intervals in the result file.  
     *  The default value of this parameter is the integer 500.
     */
    public Parameter numberOfIntervals;

    /** The input port for TODO
     */
    public TypedIOPort output;

    /** Format for the result file.  
     *  The default value of this parameter is the string "mat".
     */
    public static StringParameter outputFormat;

    /** Simulation flags.  
     *  The default value of this parameter is the string "".
     */
    public StringParameter simflags;

    /** The start time of simulation.    
     *  The default value of this parameter is the double 0.0.
     */
    public Parameter simulationStartTime;

    /** The stop time of simulation.  
     *  The default value of this parameter is the double 0.1.
     */
    public Parameter simulationStopTime;

    /** Tolerance used by the integration method.  
     *  The default value of this parameter is the double 0.0001.
     */
    public Parameter tolerance;

    /** Filter for variables that should be stored in the result file.  
     *  The default value of this parameter is the string ".*".
     */
    public StringParameter variableFilter;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        OpenModelica newObject = (OpenModelica) super.clone(workspace);
        try {
            newObject._omcLogger = OMCLogger.getInstance();
            newObject._omcProxy = OMCProxy.getInstance();
        } catch (Throwable throwable) {
            throw new CloneNotSupportedException("Could not clone "
                    + getFullName() + ": " + throwable);
        }
        return newObject;
    }

    /** Evaluate the expression and send its result to the output.
     *  @exception IllegalActionException If the evaluation of the expression
     *   triggers it, or the evaluation yields a null result, or the evaluation
     *   yields an incompatible type, or if there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        Director director = getDirector();

        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }

        try {
            if (_debugging) {
                _debug("OpenModelica Actor Called fire().");
            }

            //  Load the model from the file. 
            //  Build the model. 
            //  Run the executable result file of buildModel("command") to generate the simulation result file.
            _simulate();

            // Plot the plt file by calling PxgraphApplication.main(dcmotor_res.plt)
            _plot();
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Unable to simulate the model!");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**Fetch the file output format.
     * Default output format is mat.
     * @return _fileType The output format of the file. 
     */
    private static _fileType _getOutputFormat() {

        String fileFormat = outputFormat.getExpression();
        if (fileFormat.contains("plt")) {
            return _fileType.plt;
        } else if (fileFormat.contains("csv")) {
            return _fileType.csv;
        } else if (fileFormat.contains("mat")) {
            return _fileType.mat;
        } else
            return _fileType.mat;
    }

    /**Plot the plt file by calling PxgraphApplication.main(dcmotor_res.plt).
     * @throws ConnectException  If commands couldn't
       be sent to the OMC.*/
    private void _plot() throws ConnectException {

        // Fetch the format of simulation result file.
        _outputFormat = _getOutputFormat();

        // Array for saving the file path.  
        String[] _pltPath = new String[1];

        switch (_outputFormat) {
        case plt:

            //Send cd() command to the OpenModelica Compiler(OMC) and fetch working directory of OMC as a result.
            CompilerResult omcInvokingResult = _omcProxy.sendCommand("cd()");
            _openModelicaWorkingDirectory = omcInvokingResult.getFirstResult();
            _openModelicaWorkingDirectory = _openModelicaWorkingDirectory
                    .replace('"', ' ').trim();

            // Save file path in the string array for invoking main() of PxgraphApplication.

            if (fileNamePrefix.getExpression().compareTo("") == 0)
                _pltPath[0] = _openModelicaWorkingDirectory + "/"
                        + modelName.getExpression() + "_res.plt";
            else
                _pltPath[0] = _openModelicaWorkingDirectory + "/"
                        + fileNamePrefix.getExpression() + "_res.plt";

            PxgraphApplication.main(_pltPath);
            break;

        /*  case csv:
              break;
          case mat:
              break;*/

        default:
            break;
        }
    }

    /**Load the model from the file in the first step. Then, build the
       model. Finally, run the simulation executable result of
       buildModel() in order to generate the simulation result.
       @exception ConnectException If commands couldn't
       be sent to the OMC.
       @exception IOException If the executable result of buildModel()
       couldn't be executed.
     */
    private void _simulate() throws ConnectException, IOException,
            IllegalActionException {

        // Commands which are sent to the buildModel("command").
        String commands = null;

        // Set file parameter to the path of the testmodel(dcmotor.mo).
        String systemPath = StringUtilities.getProperty("ptolemy.ptII.dir");

        String testFilePath = null;

        testFilePath = systemPath
                + "/ptolemy/domains/openmodelica/demo/OpenModelica/"
                + fileName.getExpression();

        File file = new File(testFilePath);
        if (file.exists()) {
            if (_omcLogger == null) {
                throw new IllegalActionException(this,
                        "The OpenModelica actor only works within "
                                + "a OpenModelicaDirector because "
                                + "the actor requires a OMCLogger.");
            }
            String loggerInfo = "Using model at '" + testFilePath + "'";
            _omcLogger.getInfo(loggerInfo);

            // Load the model from the file parameter.
            _result = _omcProxy.loadFile(testFilePath);

            // Check if an error exists in the result of loadFile("command").
            if (_result.getFirstResult().compareTo("") != 0
                    && _result.getError().compareTo("") == 0) {
                loggerInfo = "Model is loaded from " + fileName.getExpression()
                        + " successfully.";
                _omcLogger.getInfo(loggerInfo);
            }

            if (_result.getError().compareTo("") != 0) {
                loggerInfo = "There is an error in loading the model!";
                _omcLogger.getInfo(loggerInfo);
                throw new ConnectException(loggerInfo);
            }

            // The loadModel("command") loads the file corresponding to the class.
            if (modelicaScript.getExpression().compareTo("") == 0)
                modelicaScript.setExpression("loadModel(Modelica)");

            _result = _omcProxy.sendCommand(modelicaScript.getExpression());

            // Check if an error exists in the result of the loadModel("command").
            if (_result.getFirstResult().compareTo("true\n") == 0) {
                loggerInfo = "Modelica model is loaded successfully.";
                _omcLogger.getInfo(loggerInfo);
            }
            if (_result.getError().compareTo("") != 0) {
                loggerInfo = "There is an error in loading Modelica model!";
                _omcLogger.getInfo(loggerInfo);
                throw new ConnectException(loggerInfo);
            }

            // Set command of buildModel() with Model Name as the name of executable result file.
            if (fileNamePrefix.getExpression().compareTo("") == 0) {
                commands = modelName.getExpression()
                        + ",startTime="
                        + Float.valueOf(simulationStartTime.getExpression())
                                .floatValue()
                        + ",stopTime="
                        + Float.valueOf(simulationStopTime.getExpression())
                                .floatValue() + ",numberOfIntervals="
                        + Integer.parseInt(numberOfIntervals.getExpression())
                        + ",tolerance="
                        + Float.valueOf(tolerance.getExpression()).floatValue()
                        + ",method=\"" + method.getExpression()
                        + "\",outputFormat=\"" + outputFormat.getExpression()
                        + "\",variableFilter=\""
                        + variableFilter.getExpression() + "\",cflags=\""
                        + cflags.getExpression() + "\",simflags=\""
                        + simflags.getExpression() + "\"";
            }

            // Set command of buildModel() with File Name Prefix as the name of executable result file.
            else {
                commands = modelName.getExpression()
                        + ",startTime="
                        + Float.valueOf(simulationStartTime.getExpression())
                                .floatValue()
                        + ",stopTime="
                        + Float.valueOf(simulationStopTime.getExpression())
                                .floatValue() + ",numberOfIntervals="
                        + Integer.parseInt(numberOfIntervals.getExpression())
                        + ",tolerance="
                        + Float.valueOf(tolerance.getExpression()).floatValue()
                        + ",method=\"" + method.getExpression()
                        + "\",fileNamePrefix=\""
                        + fileNamePrefix.getExpression() + "\",outputFormat=\""
                        + outputFormat.getExpression() + "\",variableFilter=\""
                        + variableFilter.getExpression() + "\",cflags=\""
                        + cflags.getExpression() + "\",simflags=\""
                        + simflags.getExpression() + "\"";
            }

            _result = _omcProxy.buildModel(commands);

            // Check if an error exists in the result of buildModel("command").
            if (_result.getFirstResult().compareTo("") != 0
                    && _result.getError().compareTo("") == 0) {
                loggerInfo = modelName.getExpression()
                        + " Model is built successfully.";
                _omcLogger.getInfo(loggerInfo);
            }
            if (_result.getError().compareTo("") != 0) {
                loggerInfo = "There is an error in building the model.";
                _omcLogger.getInfo(loggerInfo);
                throw new ConnectException(loggerInfo);
            }

            String command = null;

            switch (OMCProxy.getOs()) {
            case WINDOWS:
                //FIXME: you probably don't need the backslash here, but
                // you do need the FIXME.
                // I remove the backslash. By "but
                // you do need the FIXME.". you mean I should correct something else?
                command = OMCProxy.workDir.getPath() + "/"
                        + modelName.getExpression() + ".exe";
                break;
            case UNIX:
                command = OMCProxy.workDir.getPath() + "/"
                        + modelName.getExpression();
                break;
            case MAC:
                command = OMCProxy.workDir.getPath() + "/"
                        + modelName.getExpression();
                break;
            }

            // Run the executable result file of buildModel("command"). 
            Runtime.getRuntime().exec(command, OMCProxy.environmentalVariables,
                    OMCProxy.workDir);

            // When users do not select File Name Prefix as the name of executable result file
            // and Model Name is set as the name of executable result file.
            if (fileNamePrefix.getExpression().compareTo("") == 0) {
                loggerInfo = modelName.getExpression()
                        + " is executed successfully.";
                _omcLogger.getInfo(loggerInfo);
                if (_debugging) {
                    if (System.getenv("USERNAME") == null)
                        _debug("Simulation of " + fileNamePrefix
                                + " is done.\n"
                                + "The result file is located in "
                                + OMCProxy.workDir + "/nobody/OpenModelica/");
                    else
                        _debug("Simulation of " + fileNamePrefix
                                + " is done.\n"
                                + "The result file is located in "
                                + OMCProxy.workDir + "/"
                                + System.getenv("USERNAME") + "/OpenModelica/");
                }
            }

            // When users select File Name Prefix as the name of executable result file.
            else {
                loggerInfo = fileNamePrefix.getExpression()
                        + " is executed successfully.";
                _omcLogger.getInfo(loggerInfo);
                if (_debugging) {
                    if (System.getenv("USERNAME") == null)
                        _debug("Simulation of " + fileNamePrefix
                                + " is done.\n"
                                + "The result file is located in "
                                + OMCProxy.workDir + "/nobody/OpenModelica/");
                    else
                        _debug("Simulation of " + fileNamePrefix
                                + " is done.\n"
                                + "The result file is located in "
                                + OMCProxy.workDir + "/"
                                + System.getenv("USERNAME") + "/OpenModelica/");
                }
            }
        } else {
            String loggerInfo = "No file found at: [" + testFilePath
                    + "]. Select the file for simulation!";
            _omcLogger.getInfo(loggerInfo);
            throw new ConnectException("No file found at: [" + testFilePath
                    + "]. Select the file for simulation!");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Different output formats of the simulation executable result file.
    private static enum _fileType {
        csv, mat, plt
    }

    // An instance of OMCLogger in order to provide a unique source of OMCLogger instance.
    private OMCLogger _omcLogger = OMCLogger.getInstance();

    // An instance of OMCProxy in order to provide a unique source of OMCProxy instance.
    private OMCProxy _omcProxy = OMCProxy.getInstance();

    // The return result from invoking sendExpression("cd()") to OpenModelica Compiler(OMC).
    // The return result is the working directory of OMC.
    private String _openModelicaWorkingDirectory = null;

    // The output format of the simulation executable result file.
    private _fileType _outputFormat;

    // The return result from invoking sendExpression("command") to OpenModelica Compiler(OMC).
    private CompilerResult _result;

}
