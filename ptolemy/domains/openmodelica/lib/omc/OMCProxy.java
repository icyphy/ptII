/* OMCProxy is the glue between OpenModelica Compiler(OMC) and Ptolemy II.
 *
 * Copyright (c) 2012-2013,
 * Programming Environment Laboratory (PELAB),
 * Department of Computer and getInformation Science (IDA),
 * Linkoping University (LiU).
 *
 * All rights reserved.
 *
 * (The new BSD license, see also
 *  http://www.opensource.org/licenses/bsd-license.php)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * * Neither the name of Authors nor the name of Linkopings University nor
 *   the names of its contributors may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 * 
 */

package ptolemy.domains.openmodelica.lib.omc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.omg.CORBA.ORB;

import ptolemy.data.IntToken;
import ptolemy.domains.openmodelica.lib.omc.corba.OmcCommunication;
import ptolemy.domains.openmodelica.lib.omc.corba.OmcCommunicationHelper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.plot.compat.PxgraphApplication;
import ptolemy.util.StringUtilities;

/**    
  <p> OMCProxy is the glue between the OpenModelica Compiler(OMC) server and Ptolemy II.
      The OMCProxy object acts as a stand-in for the OMC server object 
      that calls the server by sending a command to the OMC server and fetches the result.</p>

      @author Mana Mirzaei, Based on OMCProxy by Adrian Pop, Elmir Jagudin, Andreas Remar
      @version $Id$
      @since Ptolemy II 9.1
      @Pt.ProposedRating Red (cxh)
      @Pt.AcceptedRating Red (cxh)
 */
public class OMCProxy implements IOMCProxy {
    /** Construct an OpenModelica Compiler(OMC) proxy.
     *  This constructor has no parameter. 
     *  This private Constructor prevents other class from instantiating. 
     */
    private OMCProxy() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Check if we've setup the communication with OpenModelica Compiler(OMC). */
    public boolean hasInitialized = false;

    /** The CORBA object for OpenModelica Compiler(OMC) communication.*/
    public OmcCommunication omcc;

    /** 
     *  OpenModelica Compiler(OMC) thread runs the OMC binary file by taking environmental variables
     *  OPENMODELICAHOME,OPENMODELICALIBRARY and working directory.
     */
    class OMCThread extends Thread {
        /** Construct an OpenModelica compiler thread
         *  with a new Thread object <i>OpenModelica Interactive Compiler Thread</i>.
         */
        public OMCThread() {
            super("OpenModelica Interactive Compiler Thread");

        }

        public void run() {
            File tmp[] = null;
            try {
                tmp = _getOmcBinaryPaths();
            } catch (ConnectException e) {

                _omcLogger
                .getSever("Unable to get the omc binary path! Server quit.");
                hasInitialized = false;
                return;
            }

            File omcBinary = tmp[0];
            final File workingDirectory = tmp[1];
            Process proc = null;

            // Start OpenModelica Compiler(OMC) as a server listening on the CORBA interface by setting +d=interactiveCorba flag.
            // Set the name of the CORBA session by +c because of using +d=interactiveCorba.
            String command[] = { omcBinary.getAbsolutePath(),
                    "+c=" + _corbaSessionName, "+d=interactiveCorba" };

            ArrayList<String> both = new ArrayList<String>(command.length);
            Collections.addAll(both, command);

            String cmd[] = new String[both.size()];
            int nonNull = 0;
            for (int i = 0; i < both.size(); i++) {
                String str = both.get(i);
                if (str != null) {
                    cmd[nonNull] = str;
                    nonNull++;
                }
            }

            StringBuffer bufferCMD = new StringBuffer();
            for (int i = 0; i < nonNull; i++) {
                bufferCMD.append(cmd[i] + " ");
            }
            String fullCMD = bufferCMD.toString();
            String loggerInfo = "Running command: " + fullCMD;
            _omcLogger.getInfo(loggerInfo);
            loggerInfo = "Setting working directory to: "
                    + workingDirectory.getAbsolutePath();
            _omcLogger.getInfo(loggerInfo);

            try {
                if (System.getenv("OPENMODELICAHOME") == null) {
                    Map<String, String> environmentalVariablesMap = System
                            .getenv();
                    Set<Entry<String, String>> entrySet = environmentalVariablesMap
                            .entrySet();
                    Collection<String> lst = new ArrayList<String>();
                    String x = "OPENMODELICAHOME="
                            + omcBinary.getParentFile().getParentFile()
                            .getAbsolutePath();
                    lst.add(x);

                    if (System.getenv("OPENMODELICALIBRARY") == null) {
                        String y = "OPENMODELICALIBRARY="
                                + omcBinary.getParentFile().getParentFile()
                                .getAbsolutePath() + "/lib/omlibrary";
                        lst.add(y);
                    }

                    Iterator<Entry<String, String>> i = entrySet.iterator();
                    while (i.hasNext()) {
                        Entry<String, String> z = i.next();
                        lst.add(z.getKey() + "=" + z.getValue());
                    }
                    _environmentalVariables = lst
                            .toArray(new String[lst.size()]);
                }
                proc = Runtime.getRuntime().exec(cmd, _environmentalVariables,
                        workingDirectory);

                _workDir = workingDirectory;
            } catch (IOException e) {
                loggerInfo = "Failed to run command: " + fullCMD;
                _omcLogger.getInfo(loggerInfo);
                hasInitialized = false;
                return;
            }
            loggerInfo = "Command run successfully.";
            _omcLogger.getInfo(loggerInfo);
            loggerInfo = "Waiting for OMC CORBA object reference to appear on disk.";
            _omcLogger.getInfo(loggerInfo);
            loggerInfo = "OMC object reference found.";
            _omcLogger.getInfo(loggerInfo);

            try {
                proc.waitFor();
            } catch (InterruptedException e) {
                String loggerSever = "OpenModelica compiler interrupted:"
                        + e.getMessage()
                        + (proc == null ? " process was null, Perhaps it was not initialized."
                                : " process exited with code "
                                + proc.exitValue());
                _omcLogger.getSever(loggerSever);
                hasInitialized = false;
                return;
            }

            if (proc != null) {
                if (_omcLogger != null) {
                    loggerInfo = "OpenModelica compiler exited with code: "
                            + proc.exitValue();
                    _omcLogger.getInfo(loggerInfo);
                } else {
                    throw new RuntimeException(
                            "OpenModelicaDirector.getOMCLogger was null! OpenModelica subprocess exited with code "
                                    + proc.exitValue());
                }
            }

            hasInitialized = false;
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                   ////

    /**  Build the Modelica model by sending buildModel(className) to the OMC.
     *   @param modelName The Name of the model which should be built.
     *   @return CompilerResult The result of sending buildModel(className) command to the OMC.
     *   @throws ConnectException If buildModel command couldn't
     *   be sent to the OMC.
     */
    public CompilerResult buildModel(String modelName) throws ConnectException {
        CompilerResult buildModelResult = sendCommand("buildModel(" + modelName
                + ")");
        return buildModelResult;
    }

    /** Read a result file and return a matrix corresponding to the variables and size given.
     *  @param fileName The executable result file of simulation in CSV format.
     *  @param modelName Name of the model which should be built.
     *  @return The value of the variables in the simulation file.
     *  @throws ConnectException If commands couldn't
     *   be sent to the (OpenModelica Compiler)OMC. 
     *  @throws IllegalActionException 
     */
    public String displaySimulationResult(String fileName, String modelName)
            throws ConnectException, IllegalActionException {

        // Return the variables in the simulation result file.
        CompilerResult readSimulationResultVars = sendCommand("readSimulationResultVars(\""
                + modelName + "_res.csv\")");

        String variableList = readSimulationResultVars.getFirstResult();

        // Delete the first and last "{". 
        StringBuffer variableBuffer = new StringBuffer(variableList);
        variableBuffer.deleteCharAt(0);
        variableList = variableBuffer.deleteCharAt(variableBuffer.length() - 1)
                .toString();

        // Split the result by "," in order to have access to each variable.
        String[] variables = variableList.split(",");
        CompilerResult readSimulationResult = null;
        String simulationResult = null;

        for (String variable : variables) {

            // Delete the first and last quotation.
            variableBuffer = new StringBuffer(variable.toString());
            variableBuffer.deleteCharAt(0);
            variableList = variableBuffer.deleteCharAt(
                    variableBuffer.length() - 1).toString();

            if (variableList.compareTo("time\"") == 0)
                variableList = variableBuffer.deleteCharAt(
                        variableBuffer.length() - 1).toString();

            // Read a result file and return a matrix corresponding to the variables and given size.
            readSimulationResult = sendCommand("readSimulationResult(\""
                    + modelName + "_res.csv\",{" + variableList + "}," + 2
                    + ")");

            if (simulationResult == null)
                simulationResult = "Value of " + variableList + " is: "
                        + readSimulationResult.getFirstResult();
            else
                simulationResult += "Value of " + variableList + " is: "
                        + readSimulationResult.getFirstResult();
        }

        // The matrix of the variable's values which is read from the simulation result file. 
        System.out.println(simulationResult);

        return simulationResult;
    }

    /** Create an instance of OMCProxy object in order to provide a global point of access to the instance.
     *  It provides a unique source of OMCProxy instance.
     *  @return An OMCProxy object representing the instance value.
     */
    public static synchronized OMCProxy getInstance() {

        if (_omcProxyInstance == null)
            _omcProxyInstance = new OMCProxy();
        return _omcProxyInstance;
    }

    /** Fetch the type of Operating System(OS).
     *  @return osType The name of the operating system(OS). 
     */
    public static _osType getOs() {
        String osName = System.getProperty("os.name");
        if (osName.contains("Linux")) {
            return _osType.UNIX;
        } else if (osName.contains("Windows")) {
            return _osType.WINDOWS;
        } else if (osName.contains("Mac")) {
            return _osType.MAC;
        } else {
            return _osType.UNIX;
        }
    }

    /** Initialize the communication with the OpenModelica compiler(OMC) server.
     *  @throws ConnectException If we're unable to start communicating with
     *  the server.
     *  @throws InterruptedException 
     */
    public synchronized void initServer() throws ConnectException {

        _os = getOs();

        // Set time as _corbaSession.
        String strDate = "";
        Date date = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
        strDate = timeFormat.format(date);
        _corbaSessionName = strDate;

        // Check if an OMC server is already started. 
        File f = new File(_getPathToObject());
        String stringifiedObjectReference = null;

        if (!f.exists()) {
            String loggerInfo = "No OMC object reference found, starting server.";
            // If a server is not already started, start it.
            _omcLogger.getInfo(loggerInfo);
            _startServer();
        } else {
            String loggerInfo = "Old OMC CORBA object reference present, assuming OMC is running.";
            _omcLogger.getInfo(loggerInfo);
        }
        try {
            stringifiedObjectReference = _readObjectFromFile();
            _setupOmcc(stringifiedObjectReference);
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to set up Omcc "
                    + stringifiedObjectReference, throwable);
        }

        hasInitialized = true;
    }

    /** Check if there is an error in the return value of sendCommand("command") method and
     *  fetch the error-information of current run.
     *  @param retval The string returned by the OpenModelica Compiler(OMC).
     *  @return Checks If the string is actually an error.
     */
    public boolean isError(String retval) {
        if (retval == null) {
            return false;
        }

        // See if there are parse error. An empty list {} also denotes error.
        return retval.toLowerCase().contains("error");
    }

    /** load the Modelica file and library.  
     *  @param fileName File which the model should be loaded from.
     *  @param modelName Name of the model which should be built.
     *  @throws ConnectException If commands couldn't
     *   be sent to the (OpenModelic Compiler)OMC.
     *  @throws IllegalActionException 
     */
    public void loadFile(String fileName, String modelName)
            throws ConnectException, IllegalActionException {
        String loggerInfo = null;

        System.out.println("SYSTEMPATH " + _systemPath);

        //FIXME
        _testFilePath = "C:/Users/mana/Documents/workspacePtII/ptII/ptolemy/domains/openmodelica/demo/OpenModelica/"
                + fileName;
        /*_testFilePath = _systemPath
                + "/ptolemy/domains/openmodelica/demo/OpenModelica/" + fileName;*/

        File file = new File(_testFilePath.toString());

        if (file.exists()) {
            if (_omcLogger == null) {
                throw new IllegalActionException(
                        "The OpenModelica actor only works within "
                                + "a OpenModelicaDirector because "
                                + "the actor requires a OMCLogger.");
            }
            loggerInfo = "Using " + modelName + " model at " + _testFilePath;
            _omcLogger.getInfo(loggerInfo);

            // Load the model from the file.
            CompilerResult loadFileInteractiveQualifiedResult = sendCommand("loadFileInteractiveQualified(\""
                    + _testFilePath + "\")");

            // Check if an error exists in the result of loadFile("command").
            if (loadFileInteractiveQualifiedResult.getFirstResult().compareTo(
                    "") != 0
                    && loadFileInteractiveQualifiedResult.getError().compareTo(
                            "") == 0) {
                loggerInfo = modelName + " model is loaded from "
                        + _testFilePath + " successfully.";
                _omcLogger.getInfo(loggerInfo);
            }

            if (loadFileInteractiveQualifiedResult.getError().compareTo("") != 0) {
                loggerInfo = "Error in loading " + modelName + " model!";
                _omcLogger.getInfo(loggerInfo);
                throw new ConnectException(loggerInfo);
            }
            // Load the Modelica library by sending loadModel(Modelica) to the OMC server.
            CompilerResult loadModelResult = sendCommand("loadModel(Modelica)");

            // Check if an error exists in the result of sending loadModel(Modelica) to the OMC server.
            if (loadModelResult.getFirstResult().compareTo("true\n") == 0) {
                loggerInfo = "Modelica library is loaded successfully.";
                _omcLogger.getInfo(loggerInfo);
            }
            if (loadModelResult.getError().compareTo("") != 0) {
                loggerInfo = "Error in loading Modelica library!";
                _omcLogger.getInfo(loggerInfo);
                throw new ConnectException(loggerInfo);
            }

        } else if (!file.exists()) {
            loggerInfo = "No file found at: " + _testFilePath
                    + " .Select the file for simulation!";
            _omcLogger.getInfo(loggerInfo);
            throw new ConnectException("No file found at: " + _testFilePath
                    + " .Select the file for simulation!");
        }

    }

    /** Return the components which the model is composed of and modify the value of simulation variables.
     *  @param inputPortValue The value of OpenModelica actor input port which reads init value of the Ramp actor.
     *  @param modelName Name of the model which should be built. 
     *  @throws ConnectException If commands couldn't
     *   be sent to the (OpenModelica Compiler)OMC. 
     *  @throws IllegalActionException 
     */
    public void modifyVariables(IntToken inputPortValue, String modelName)
            throws IllegalActionException, ConnectException {

        String[] componentList = null;
        String ComponentNames = null;
        String[] individualComponent = null;
        String[] variableList = null;
        String parameterNames = null;

        try {

            // List all components of the model.
            CompilerResult getComponentsResult = sendCommand("getComponents("
                    + modelName + ")");

            ComponentNames = getComponentsResult.getFirstResult();

            // Delete the first "{".
            StringBuffer componentsBuffer = new StringBuffer(ComponentNames);
            componentsBuffer.delete(0, 1);
            ComponentNames = componentsBuffer.toString();

            // Split the getComponents result by "}," in order to access each component.
            componentList = ComponentNames.split("},");

            for (String component : componentList) {

                componentsBuffer = new StringBuffer(component);
                ComponentNames = componentsBuffer.deleteCharAt(0).toString();

                // Split the component by "," in order to access each property of the component.
                individualComponent = ComponentNames.split(",");

                // The second element is the name of the component.
                _componentName = individualComponent[1];

                //The 9th element can have four possible values constant, parameter, discrete or unspecified.
                _parameterOrVariable = individualComponent[8];

                // Delete the first space and quotation.
                componentsBuffer = new StringBuffer(_parameterOrVariable);
                componentsBuffer.delete(0, 2);

                // Delete the last quotation.
                _parameterOrVariable = componentsBuffer.deleteCharAt(
                        componentsBuffer.length() - 1).toString();

                // In Modelica variables store results of computations performed when solving the equations
                // of a class together with equations from other classes. During solution of timedependent problems,
                // the variables store results of the solution process at the current time instance.

                // "unspecified" means no variability is set for the variable.
                if (_parameterOrVariable.compareTo("unspecified") == 0) {

                    // Return list of simulation parameters.
                    CompilerResult getComponentModifierNames = sendCommand("getComponentModifierNames("
                            + modelName + "," + individualComponent[1] + ")");

                    System.out.println("getComponentModifierNames("
                            + getComponentModifierNames.getFirstResult().trim()
                            + ")");

                    if (getComponentModifierNames.getFirstResult().trim()
                            .compareTo("{}") == 0) {

                        sendCommand("setComponentModifierValue(" + modelName
                                + ", " + _componentName + ", $Code(="
                                + inputPortValue + "))");

                        System.out.println("setComponentModifierValue("
                                + modelName + ", " + _componentName
                                + ", $Code(=" + inputPortValue + "))");

                    } else {

                        componentsBuffer = new StringBuffer(
                                getComponentModifierNames.getFirstResult());

                        // Delete the first and last "{".
                        componentsBuffer.deleteCharAt(0);
                        parameterNames = componentsBuffer.deleteCharAt(
                                componentsBuffer.length() - 2).toString();

                        // Split the result by "," in order to have access to each simulation parameter's value.
                        variableList = parameterNames.split(",");

                        for (String variable : variableList) {

                            individualComponent[1] = individualComponent[1]
                                    .trim();

                            sendCommand("setComponentModifierValue("
                                    + modelName + ", " + _componentName + "."
                                    + variable + ", $Code(=" + inputPortValue
                                    + "))");

                            System.out.println("setComponentModifierValue("
                                    + modelName + ", " + _componentName + "."
                                    + variable + ", $Code(=" + inputPortValue
                                    + "))");
                        }

                    }

                    //  The keyword parameter specifies that the variable is constant during a simulation run,
                    //  but can have its value initialized before a run, or between runs. 
                    //  This means that parameter is a special kind of constant,
                    //  which is implemented as a static variable that is initialized once and 
                    //  never changes its value during a specific execution. A parameter is a constant variable that makes 
                    //  it simple for a user to modify the behavior of a model.

                    //  "parameter" indicates this variable is simulation parameter. 
                } else if (_parameterOrVariable.compareTo("parameter") == 0) {

                    sendCommand("setParameterValue(" + modelName + ","
                            + _componentName + "," + inputPortValue + ")");

                    System.out.println("setParameterValue(" + modelName + ","
                            + _componentName + "," + inputPortValue + ")");
                }
            }
        } catch (ConnectException e) {
            throw new ConnectException(
                    "Unable to modify variables value due to connection problem with OMC!");
        }
    }

    /** Plot the plt file by calling PxgraphApplication.main(dcmotor_res.plt).
     *  @param fileNamePrefix User preferable name for the result file.
     *  @param modelName Name of the model which should be built.
     *  @throws ConnectException If commands could not be sent to the OMC.
     */
    public void plotPltFile(String fileNamePrefix, String modelName)
            throws ConnectException {

        // Array for saving the file path.  
        String[] _pltPath = new String[1];

        // Send cd() command to the (OpenModelica Compiler)OMC and fetch working directory of OMC as a result.
        CompilerResult cdResult = sendCommand("cd()");
        _openModelicaWorkingDirectory = cdResult.getFirstResult();
        _openModelicaWorkingDirectory = _openModelicaWorkingDirectory.replace(
                '"', ' ').trim();

        // Save file path in the string array for invoking main() of PxgraphApplication.
        if (fileNamePrefix.compareTo("") == 0)
            _pltPath[0] = _openModelicaWorkingDirectory + "/" + modelName
            + "_res.plt";
        else
            _pltPath[0] = _openModelicaWorkingDirectory + "/" + fileNamePrefix
            + "_res.plt";

        PxgraphApplication.main(_pltPath);
    }

    /** Leave and quit OpenModelica environment.
     *  Deallocate OMCProxy and OMCLogger objects.
     *  @throws ConnectException If quit command couldn't
     *  be sent to OMC.
     */
    public void quitServer() throws ConnectException {

        if (hasInitialized = true) {
            sendCommand("quit()");
            _omcLogger = null;
            _omcProxyInstance = null;
        }
    }

    /** Send a command to the OpenModelica Compiler(OMC) server and fetches the string result.
     *  @param modelicaCommand The command which should be sent to the OMC.
     *  @return CompilerResult The result of sendExpression("modelicaCommand") to the OMC.
     *  @throws ConnectException If commands couldn't be sent to the OMC.
     */
    public CompilerResult sendCommand(String modelicaCommand)
            throws ConnectException {
        String error = null;
        String[] retval = { "" };

        if (_couldNotStartOMC)
            return CompilerResult.makeResult(retval, error);

        if (_numberOfErrors > _showMaxErrors)
            return CompilerResult.makeResult(retval, error);

        // Trim the start and end spaces.
        modelicaCommand = modelicaCommand.trim();

        if (hasInitialized == false)
            initServer();

        try {
            // Fetch the error string from OpenModelica Compiler(OMC). 
            // This should be called after an "Error"
            // is received or whenever the queue of errors are emptied.

            retval[0] = omcc.sendExpression(modelicaCommand);

            if (!modelicaCommand.equalsIgnoreCase("quit()"))
                error = omcc.sendExpression("getErrorString()");

            // Make sure the error string is not empty.
            if (error != null && error.length() > 2) {
                error = error.trim();
                error = error.substring(1, error.length() - 1);
            } else {
                error = null;
            }

            return CompilerResult.makeResult(retval, error);

        } catch (org.omg.CORBA.COMM_FAILURE x) {
            _numberOfErrors++;

            // Lose connection to OMC(OpenModelica Compiler) server.
            throw new ConnectException(
                    "Couldn't send command to the OpenModelica Compiler. Tried sending: "
                            + modelicaCommand + x.getMessage());
        }
    }

    /** Build the Modelica model. Then, run the executable result file of
     *  buildModel() in both interactive and non-interactive processing mode
     *  in order to generate the simulation result file.
     *  @param fileName File which the model should be loaded from.
     *  @param modelName Name of the model which should be built.
     *  @param fileNamePrefix User preferable name for the result file.
     *  @param startTime The start time of simulation.
     *  @param stopTime The stop time of simulation.
     *  @param numberOfIntervals Number of intervals in the result file.
     *  @param tolerance Tolerance used by the integration method.
     *  @param method Integration method used for simulation.
     *  @param outputFormat Format of the result file.
     *  @param variableFilter Filter for variables that should be stored in the result file.
     *  @param cflags Any standard C language flags.
     *  @param simflags Simulation flags.
     *  @param processingType Type of processing for running the executable result file of building the Modelica model.
     *  @throws ConnectException If commands couldn't
     *   be sent to the (OpenModelic Compiler)OMC.
     *  @throws IOException If the executable result file of buildModel()
     *   couldn't be executed.
     *  @throws IllegalActionException 
     */
    public void simulateModel(String fileName, String modelName,
            String fileNamePrefix, String startTime, String stopTime,
            int numberOfIntervals, String tolerance, String method,
            String outputFormat, String variableFilter, String cflags,
            String simflags, String processingType) throws ConnectException,
            IOException, IllegalActionException {

        // Command which is sent to the OMC for building the model.
        String commands = null;

        String loggerInfo = null;

        // Set command of buildModel("command") with Model Name as the name of executable result file.
        if (fileNamePrefix.compareTo("") == 0) {
            commands = modelName + ",startTime="
                    + Float.valueOf(startTime).floatValue() + ",stopTime="
                    + Float.valueOf(stopTime).floatValue()
                    + ",numberOfIntervals=" + numberOfIntervals + ",tolerance="
                    + Float.valueOf(tolerance).floatValue() + ",method=\""
                    + method + "\",outputFormat=\"" + outputFormat
                    + "\",variableFilter=\"" + variableFilter + "\",cflags=\""
                    + cflags + "\",simflags=\"" + simflags + "\"";

            loggerInfo = "Building "
                    + modelName
                    + " model without the preferable name for the executable result file.";
            _omcLogger.getInfo(loggerInfo);
        }

        // Set command of buildModel("command") with the preferable name for the executable result file.
        else {
            commands = fileNamePrefix + ",startTime="
                    + Float.valueOf(startTime).floatValue() + ",stopTime="
                    + Float.valueOf(stopTime).floatValue()
                    + ",numberOfIntervals=" + numberOfIntervals + ",tolerance="
                    + Float.valueOf(tolerance).floatValue() + ",method=\""
                    + method + "\",fileNamePrefix=\"" + fileNamePrefix
                    + "\",outputFormat=\"" + outputFormat
                    + "\",variableFilter=\"" + variableFilter + "\",cflags=\""
                    + cflags + "\",simflags=\"" + simflags + "\"";

            loggerInfo = "Building "
                    + modelName
                    + " model with the preferable name for the executable result file.";
            _omcLogger.getInfo(loggerInfo);
        }

        // Build the Modelica model by sending buildModel() to the OMC server.
        CompilerResult buildModelResult = buildModel(commands);

        // Check if an error exists in the result of buildModel("command").
        if (buildModelResult.getFirstResult().compareTo("") != 0
                && buildModelResult.getError().compareTo("") == 0) {
            loggerInfo = modelName + " model is built successfully.";
            _omcLogger.getInfo(loggerInfo);
        }
        if (buildModelResult.getError().compareTo("") != 0) {
            loggerInfo = "Error in building " + modelName + " model.";
            _omcLogger.getInfo(loggerInfo);
            throw new ConnectException(loggerInfo);
        }

        if (!outputFormat.equals("empty")) {

            if (processingType.compareTo("batch") == 0) {

                loggerInfo = "Running non-interactive simulation.";
                _omcLogger.getInfo(loggerInfo);

                if (fileNamePrefix.compareTo("") == 0) {
                    switch (getOs()) {
                    case WINDOWS:
                        commands = _temp + _username + "/OpenModelica/"
                                + modelName + ".exe";
                        break;
                    case UNIX:
                        commands = _temp + "/" + _username + "/OpenModelica/"
                                + modelName;
                        break;
                    case MAC:
                        commands = _temp + _username + "/OpenModelica/"
                                + modelName;
                        break;
                    }
                } else {
                    switch (getOs()) {
                    case WINDOWS:
                        commands = _temp + _username + "/OpenModelica/"
                                + fileNamePrefix + ".exe";
                        break;
                    case UNIX:
                        commands = _temp + "/" + _username + "/OpenModelica/"
                                + fileNamePrefix;
                        break;
                    case MAC:
                        commands = _temp + _username + "/OpenModelica/"
                                + fileNamePrefix;
                        break;
                    }
                }

                // Run the executable result file of buildModel("command").
                try {
                    Runtime.getRuntime().exec(commands,
                            _environmentalVariables, _workDir);
                } catch (IOException e) {
                    loggerInfo = "Failed to run the command: " + commands;
                    _omcLogger.getInfo(loggerInfo);
                    hasInitialized = false;
                    StringUtilities.exit(1);
                }

                loggerInfo = "The executable file is run successfully in a non-interactive mode!";
                _omcLogger.getInfo(loggerInfo);

                if (fileNamePrefix.compareTo("") == 0) {
                    // When users do not select File Name Prefix as the name of executable result file.
                    switch (getOs()) {
                    case WINDOWS:
                        loggerInfo = modelName + "_res." + outputFormat
                        + " is generated in " + _temp + _username
                        + "/OpenModelica/";
                        _omcLogger.getInfo(loggerInfo);
                        break;
                    case UNIX:
                        loggerInfo = modelName + "_res." + outputFormat
                        + " is generated in " + _temp + "/" + _username
                        + "/OpenModelica/";
                        _omcLogger.getInfo(loggerInfo);
                        break;
                    case MAC:
                        loggerInfo = modelName + "_res." + outputFormat
                        + " is generated in " + _temp + _username
                        + "/OpenModelica/";
                        _omcLogger.getInfo(loggerInfo);
                        break;
                    }
                } else if (fileNamePrefix.length() != 0) {
                    // When users select File Name Prefix as the name of executable result file.
                    switch (getOs()) {
                    case WINDOWS:
                        loggerInfo = fileNamePrefix + "_res." + outputFormat
                        + " is generated in " + _temp + _username
                        + "/OpenModelica/";
                        _omcLogger.getInfo(loggerInfo);
                        break;
                    case UNIX:
                        loggerInfo = fileNamePrefix + "_res." + outputFormat
                        + " is generated in " + _temp + "/" + _username
                        + "/OpenModelica/";
                        _omcLogger.getInfo(loggerInfo);
                        break;
                    case MAC:
                        loggerInfo = fileNamePrefix + "_res." + outputFormat
                        + " is generated in " + _temp + _username
                        + "/OpenModelica/";
                        _omcLogger.getInfo(loggerInfo);
                        break;
                    }
                }
            } else {

                loggerInfo = "Running interactive simulation.";
                _omcLogger.getInfo(loggerInfo);

                switch (getOs()) {
                case WINDOWS:
                    commands = _temp + _username + "/OpenModelica/" + modelName
                    + ".exe";
                    break;
                case UNIX:
                    commands = _temp + "/" + _username + "/OpenModelica/"
                            + modelName;
                    break;
                case MAC:
                    commands = _temp + _username + "/OpenModelica/" + modelName;
                    break;
                }

                // BuildModel("modelName") results in an executable file
                // that is run by -interactive flag.

                commands = commands + " -interactive";

                try {
                    loggerInfo = "Command " + commands + " is running!";
                    _omcLogger.getInfo(loggerInfo);
                    Runtime.getRuntime().exec(commands,
                            _environmentalVariables, _workDir);
                } catch (IOException e) {
                    loggerInfo = "Failed to run command: " + commands;
                    _omcLogger.getInfo(loggerInfo);
                    hasInitialized = false;
                    return;
                }

                loggerInfo = "The executable file is run successfully in an interactive mode!";
                _omcLogger.getInfo(loggerInfo);
            }
        } else {
            loggerInfo = modelName
                    + " model is built without generating any result files!";
            _omcLogger.getInfo(loggerInfo);

        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /** Find the OpenModelica Compiler(OMC) executable file by using path variables.
     *  @param executableName The name of the executable file
     *  @return File The OMC executable file
     */
    private File _findExecutableOnPath(String executableName) {
        String systemPath = System.getenv("PATH");

        // Try path with small letters.
        if (systemPath == null) {
            systemPath = System.getenv("path");
        }
        String[] pathDirs = systemPath.split(File.pathSeparator);

        File fullyQualifiedExecutable = null;
        for (String pathDir : pathDirs) {
            File file = new File(pathDir, executableName);
            if (file.isFile()) {
                fullyQualifiedExecutable = file;
                break;
            }
        }
        return fullyQualifiedExecutable;
    }

    /** Determine the path to the
     *  (OpenModelica Compiler)OMC binary that user (probably) wants to use and the working
     *  directory of where that binary (most likely) should be started in.
     *  This will returns for example 'c:\openmodelica132\omc.exe'
     *  or '/usr/local/share/openmodelica/omc' depending on
     *  such factors as: OS type, environmental Variables settings,
     *  where the first matching binary found.
     *  @return full path to the OMC binary and the working folder.
     *  @throws ConnectException If OPENMODELICAHOME is not set 
     *  and we could not find binary file in the path.
     */
    private File[] _getOmcBinaryPaths() throws ConnectException {
        String binaryName = "omc";

        if (_os == _osType.WINDOWS) {
            binaryName += ".exe";
        }

        File omcBinary = null;
        File omcWorkingDirectory = null;
        File openModelicaHomeDirectory = null;

        // User specified that standard path to (OpenModelica Compiler)OMC should be used.
        // Try to determine OMC path via the OPENMODELICAHOME and
        // by checking in it's various subdirectory for OMC binary file.
        String loggerInfo = "Using OPENMODELICAHOME environmental variable to find omc-binary";
        _omcLogger.getInfo(loggerInfo);

        // Standard path to (OpenModelica Compiler)OMC binary is encoded in OPENMODELICAHOME
        // variable. 
        String openModelicaHome = System.getenv("OPENMODELICAHOME");
        if (openModelicaHome == null) {
            loggerInfo = "OPENMODELICAHOME environmental variable is NULL, trying the PATH variable";
            _omcLogger.getInfo(loggerInfo);
            File omc = _findExecutableOnPath(binaryName);
            if (omc != null) {
                loggerInfo = "Found omc executable in the path here: "
                        + omc.getAbsolutePath();
                _omcLogger.getInfo(loggerInfo);

                openModelicaHome = omc.getParentFile().getParentFile()
                        .getAbsolutePath();
            } else {
                final String m = "Environmental variable OPENMODELICAHOME is not set and we could not find: "
                        + binaryName + " in the PATH";
                _omcLogger.getInfo(m);
                throw new ConnectException(m);
            }
        }

        openModelicaHomeDirectory = new File(openModelicaHome);

        // The subdirectories where (OpenModelica Compiler)OMC binary is located. 
        // adrpo 2012-06-12 It does not support the old ways! "/omc" and "Compiler/omc".
        String[] subdirs = { "bin" };

        for (String subdir : subdirs) {
            String path = openModelicaHomeDirectory.getAbsolutePath()
                    + File.separator;
            path += subdir.equals("") ? binaryName : subdir + File.separator
                    + binaryName;

            File file = new File(path);

            if (file.exists()) {
                omcBinary = file;
                loggerInfo = "Using omc-binary at '"
                        + omcBinary.getAbsolutePath() + "'";
                _omcLogger.getInfo(loggerInfo);
                break;
            } else {
                loggerInfo = "No omc binary at: [" + path + "]";
                _omcLogger.getInfo(loggerInfo);
            }
        }

        if (omcBinary == null) {
            loggerInfo = "Could not find omc-binary on the OPENMODELICAHOME path or in the PATH variable";
            _omcLogger.getInfo(loggerInfo);
            throw new ConnectException(loggerInfo);
        }

        // Create the user directory in temp.
        switch (getOs()) {
        case WINDOWS:
            if (_username == null) {
                System.err
                .println("Could not get user.name property?  Using 'nobody'.");
                omcWorkingDirectory = new File(_temp + "nobody/OpenModelica/");
            } else {
                omcWorkingDirectory = new File(_temp + _username
                        + "/OpenModelica/");
            }

            break;
        case UNIX:
            if (_username == null) {
                System.err
                .println("Could not get user.name property?  Using 'nobody'.");
                omcWorkingDirectory = new File(_temp + "/nobody/OpenModelica/");
            } else {
                omcWorkingDirectory = new File(_temp + "/" + _username
                        + "/OpenModelica/");
            }

            break;
        case MAC:
            if (_username == null) {
                System.err
                .println("Could not get user.name property?  Using 'nobody'.");
                omcWorkingDirectory = new File(_temp + "nobody/OpenModelica/");
            } else {
                omcWorkingDirectory = new File(_temp + _username
                        + "/OpenModelica/");
            }

            break;
        }

        String workingDirectory = "Using working directory '"
                + omcWorkingDirectory.getAbsolutePath() + "'";

        loggerInfo = workingDirectory;
        _omcLogger.getInfo(loggerInfo);

        return new File[] { omcBinary, omcWorkingDirectory };
    }

    /** Return the path to the (OpenModelica Compiler)OMC CORBA object that is stored on a disk.
     *  @return String The path to the OMC CORBA object.
     */
    private String _getPathToObject() {

        String fileName = null;

        switch (getOs()) {

        // Add _corbaSession to the end of (OpenModelica Compiler)OMC CORBA object reference name to make it unique.
        case UNIX:
            if (_username == null) {
                System.err
                .println("Could not get user.name property?  Using 'nobody'.");
                _username = "nobody";
            }
            if (_corbaSessionName == null
                    || _corbaSessionName.equalsIgnoreCase("")) {
                fileName = _temp + "/openmodelica." + _username + ".objid";
            } else {
                fileName = _temp + "/openmodelica." + _username + ".objid"
                        + "." + _corbaSessionName;
            }
            break;
        case WINDOWS:
            if (_corbaSessionName == null
            || _corbaSessionName.equalsIgnoreCase("")) {
                fileName = _temp + "openmodelica.objid";
            } else {
                fileName = _temp + "openmodelica.objid" + "."
                        + _corbaSessionName;
            }
            break;
        case MAC:
            if (_username == null) {
                System.err
                .println("Could not get user.name property?  Using 'nobody'.");
                _username = "nobody";
            }
            if (_corbaSessionName == null
                    || _corbaSessionName.equalsIgnoreCase("")) {
                fileName = _temp + "openmodelica." + _username + ".objid";
            } else {
                fileName = _temp + "openmodelica." + _username + ".objid" + "."
                        + _corbaSessionName;
            }
            break;
        }

        if (_omcLogger == null) {
            new Exception("Warning, _omcLogger was null?").printStackTrace();
            _omcLogger = OMCLogger.getInstance();
        }

        String loggerInfo = "Will look for OMC object reference in '"
                + fileName + "'.";
        if (_omcLogger == null) {
            new Exception("Warning, _omcLogger was null?").printStackTrace();
            _omcLogger = OMCLogger.getInstance();
        }
        _omcLogger.getInfo(loggerInfo);

        return fileName;
    }

    /** Read the (OpenModelica Compiler)OMC CORBA object reference from a file on a disk.
     *  @return The object reference as a String.
     */
    private String _readObjectFromFile() throws IOException {

        String path = _getPathToObject();
        File f = new File(path);
        String stringifiedObjectReference = null;

        BufferedReader br = null;

        try {
            FileReader fr = new FileReader(f);
            br = new BufferedReader(fr);
            stringifiedObjectReference = br.readLine();
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(
                    "The OpenModelica Compiler CORBA object path '" + path
                    + "' does not exist!");
        } catch (IOException e) {
            throw new IOException(
                    "Unable to read OpenModelica Compiler CORBA object from '"
                            + path + "'");

        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                throw new IOException(
                        "Very weird error indeed, IOException when closing BufferedReader for file '"
                                + path + "'.");

            }
        }

        return stringifiedObjectReference;
    }

    /** Initialize an ORB, convert the stringified OpenModelica
     *  Compiler(OMC) object to a real CORBA object and then narrow
     *  that object to an OmcCommunication object.
     */
    private synchronized void _setupOmcc(String stringifiedObjectReference) {

        String args[] = { null };

        // Set the CORBA read timeout to a larger value as we send huge amounts of data
        // from (OpenModelica Compiler)OMC to (Modelica Development Tooling)MDT.
        System.setProperty("com.sun.CORBA.transport.ORBTCPReadTimeouts",
                "1:60000:300:1");

        ORB orb;
        orb = ORB.init(args, null);

        try {
            // Convert string to CORBA object. 
            org.omg.CORBA.Object obj = orb
                    .string_to_object(stringifiedObjectReference);

            // Convert object to OmcCommunication object. 
            omcc = OmcCommunicationHelper.narrow(obj);
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to convert string \""
                    + stringifiedObjectReference + "\" to an object.",
                    throwable);
        }
    }

    /** Start the (OpenModelica Compiler)OMC server by starting OMCThread.
     *  @throws ConnectException If OPENMODELICAHOME is not set 
     *  and we could not find binary file in the path.
     */
    private synchronized void _startServer() throws ConnectException {

        if (!_fOMCThreadHasBeenScheduled) {

            if (_fOMCThread == null) {
                _fOMCThread = new OMCThread();
            }
            _fOMCThread.start();
            _fOMCThreadHasBeenScheduled = true;

            // FIXME: FindBugs says:
            // OMCProxy.java:1217, ML_SYNC_ON_UPDATED_FIELD, Priority: Normal
            // Method synchronizes on an updated field
            // This method synchronizes on an object referenced from a
            // mutable field. This is unlikely to have useful
            // semantics, since different threads may be synchronizing
            // on different objects.
            synchronized (_fOMCThread) {
                try {
                    _fOMCThread.wait(10000);
                } catch (InterruptedException e) {

                }
            }
        }
        synchronized (_fOMCThread) {
            _fOMCThread.notify();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The name of the Modelica model component.
    private String _componentName = null;

    // Initialize _corbaSession.
    private String _corbaSessionName = null;

    // Indicate if we give up on running OpenModelica Compiler(OMC) as it is unable to start. 
    private boolean _couldNotStartOMC = false;

    // Environmental variables which should be used for running the result of buildModel.
    private String[] _environmentalVariables = null;

    // This object is used for starting OpenModelica Compiler(OMC)'s thread. 
    private OMCThread _fOMCThread = null;

    // Flag which indicates whether the server should start or not. 
    private boolean _fOMCThreadHasBeenScheduled = false;

    // OMCLogger Object for accessing a unique source of instance.
    private OMCLogger _omcLogger = OMCLogger.getInstance();

    // OMCProxy Object for accessing a unique source of instance. 
    private static OMCProxy _omcProxyInstance = null;

    // The working directory of the OMC is fetched from sending cd() command to the OMC.
    private String _openModelicaWorkingDirectory = null;

    // The (Operating System)OS we are running on.
    private _osType _os;

    // Different types of Operating System(OS).
    private enum _osType {
        UNIX, WINDOWS, MAC
    }

    // Initialization of the number of errors.
    private int _numberOfErrors = 0;

    // Indicates if the component is variable or parameter.
    private String _parameterOrVariable = null;

    // Maximum number of compiler errors to display. 
    private int _showMaxErrors = 10;

    // The system path of PTII. 
    private String _systemPath = StringUtilities
            .getProperty("ptolemy.ptII.dir");

    // Temp directory.
    private String _temp = System.getProperty("java.io.tmpdir");

    // Set file parameter to the path of dcmotor.mo.
    private String _testFilePath = null;

    // The name of the user.
    private String _username = StringUtilities.getProperty("user.name");

    // The working directory of the process.
    private File _workDir = null;

}
