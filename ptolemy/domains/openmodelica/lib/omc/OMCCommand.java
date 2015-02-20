/* OMCCommand executes Modelica commands.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.omg.CORBA.ORB;

import ptolemy.domains.openmodelica.lib.omc.corba.OmcCommunication;
import ptolemy.domains.openmodelica.lib.omc.corba.OmcCommunicationHelper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.plot.compat.PxgraphApplication;
import ptolemy.util.StringUtilities;

/**
  <p> OMCCommand is composed of the implementation of the commands for initializing the communication with
      the OpenModelica compiler(OMC) server, building the Modelica model, fetching the error-information of
      current run, loading the Modelica file and library and modifying parameter(s) and variable(s) of the
      Modelica model before building the Modelica model in both interactive and non-interactive processing
      mode. In addition to executing all these Modelica commands, it plots the result file of the simulation
      that is generated in plt format.
  </p>

      @author Mana Mirzaei, Based on OMCProxy by Adrian Pop, Elmir Jagudin, Andreas Remar
      @version $Id$
      @since Ptolemy II 10.0
      @Pt.ProposedRating Red (cxh)
      @Pt.AcceptedRating Red (cxh)
 */
public class OMCCommand implements IOMCCommand {
    /** Construct an OpenModelica Compiler(OMC) proxy.
     *  This constructor has no parameter.
     *  Private constructor prevents a class from being explicitly instantiated by its callers.
     */
    private OMCCommand() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Check if the communication with OpenModelica Compiler(OMC) is set up. */
    public boolean hasInitialized = false;

    /** The CORBA object for OpenModelica Compiler(OMC) communication.*/
    public OmcCommunication omcCommunication;

    /**
       OMCThread runs the OMC binary file by taking environmental variables
       OPENMODELICAHOME,OPENMODELICALIBRARY and working directory.
     */
    class OMCThread extends Thread {
        /** Construct an OpenModelica compiler thread
         *  with a new Thread object <i>OpenModelica Interactive Compiler Thread</i>.
         */
        public OMCThread() {
            super("OpenModelica Interactive Compiler Thread");
        }

        @Override
        public void run() {

            _omcLogger = OMCLogger.getInstance();

            File temp[] = null;
            try {
                temp = _getOmcBinaryPaths();
            } catch (ConnectException e) {
                new ConnectException(e.getMessage()).printStackTrace();
                return;
            }

            File omcBinary = temp[0];
            final File workingDirectory = temp[1];

            // Start OpenModelica Compiler(OMC) as a server listening on the CORBA interface by setting +d=interactiveCorba flag.
            // Because of using +d=interactiveCorba, CORBA session should be set by +c flag.
            String command[] = { omcBinary.getAbsolutePath(),
                    "+c=" + _corbaSession, "+d=interactiveCorba" };

            ArrayList<String> both = new ArrayList<String>(command.length);
            Collections.addAll(both, command);

            String commandArray[] = new String[both.size()];
            int nonNull = 0;
            for (int i = 0; i < both.size(); i++) {
                String str = both.get(i);
                if (str != null) {
                    commandArray[nonNull] = str;
                    nonNull++;
                }
            }

            StringBuffer bufferCommand = new StringBuffer();
            for (int i = 0; i < nonNull; i++) {
                bufferCommand.append(commandArray[i] + " ");
            }
            String fullCommand = bufferCommand.toString();
            String loggerInfo = "Command " + fullCommand
                    + " running for starting OMC ...";
            _omcLogger.getInfo(loggerInfo);

            loggerInfo = "Working directory is set to: "
                    + workingDirectory.getAbsolutePath() + ".";
            _omcLogger.getInfo(loggerInfo);

            try {
                if (System.getenv("OPENMODELICAHOME") == null) {
                    Map<String, String> environmentalVariablesMap = System
                            .getenv();
                    Set<Entry<String, String>> entrySet = environmentalVariablesMap
                            .entrySet();
                    Collection<String> variableList = new ArrayList<String>();
                    String x = "OPENMODELICAHOME="
                            + omcBinary.getParentFile().getParentFile()
                                    .getAbsolutePath();
                    variableList.add(x);

                    if (System.getenv("OPENMODELICALIBRARY") == null) {
                        String libraryPath = "OPENMODELICALIBRARY="
                                + omcBinary.getParentFile().getParentFile()
                                        .getAbsolutePath() + "/lib/omlibrary";
                        variableList.add(libraryPath);
                    }

                    Iterator<Entry<String, String>> iterator = entrySet
                            .iterator();
                    while (iterator.hasNext()) {
                        Entry<String, String> entry = iterator.next();
                        variableList.add(entry.getKey() + "="
                                + entry.getValue());
                    }
                    _environmentalVariables = variableList
                            .toArray(new String[variableList.size()]);
                }
                _omcProcess = Runtime.getRuntime().exec(commandArray,
                        _environmentalVariables, workingDirectory);

                _workDir = workingDirectory;
            } catch (IOException e) {
                new IOException(e.getMessage()).printStackTrace();
                return;
            }

            loggerInfo = "Command for starting OMC runs successfully.";
            _omcLogger.getInfo(loggerInfo);

            loggerInfo = "Waiting for OpenModelica object reference to appear on disk...";
            _omcLogger.getInfo(loggerInfo);

            try {
                // Cause this process to stop until process proc is terminated, 0 indicates normal termination.
                _omcProcess.waitFor();
            } catch (InterruptedException e) {
                // FIXME: The exception should be caught, not the stack printed
                new InterruptedException(
                        "OMC process interrupted: "
                                + e.getMessage()
                                + (_omcProcess == null ? " process was null, perhaps it was not initialized."
                                        : " process exited with code "
                                                + _omcProcess.exitValue()))
                        .printStackTrace();
                return;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check if the (base-)model inherits from other classes.
     *  @param modelName The (base-)model that should be built.
     *  @return Check Return true, if the number of inherited classes is more than zero.
     */
    @Override
    public boolean getInheritanceCount(String modelName) {

        CompilerResult getInheritanceCountResult = null;
        String inheritanceCount = null;

        try {
            // Return the number (as a string) of inherited classes of the (base-)model.
            getInheritanceCountResult = sendCommand("getInheritanceCount("
                    + modelName + ")");
        } catch (ConnectException e) {
            // FIXME: The exception should be caught, not the stack printed
            new ConnectException(e.getMessage()).printStackTrace();
        }
        if (getInheritanceCountResult != null
                && getInheritanceCountResult.getError().isEmpty()) {
            inheritanceCount = getInheritanceCountResult.getFirstResult()
                    .toString();
        }
        if (inheritanceCount != null
                && !(inheritanceCount.compareTo("0\n") == 0)) {
            return true;
        } else {
            return false;
        }
    }

    /** Create an instance of OMCCommand object in order to provide a global point of access to the instance.
     *  It provides a unique source of OMCCommand instance.
     *  @return An OMCCommand object representing the instance value.
     */
    public static synchronized OMCCommand getInstance() {
        if (_omcCommandInstance == null) {
            _omcCommandInstance = new OMCCommand();
        }
        return _omcCommandInstance;
    }

    /** Return the list of component declarations within (base-)model.
     *  @param modelName The (base-)model that should be built.
     *  @return HashMap The list of component declarations within (base-)model.
     */
    @Override
    public HashMap getModelComponent(String modelName) {

        String componentNames = null;
        String[] componentList = null;
        CompilerResult getComponentsResult = null;
        HashMap<String, String> componentMap = null;
        String getComponentResultDelimiter = "},";
        String componentsDelimiter = ",";

        // Get all components of the model.e.g. variable, parameter, discrete and etc.

        try {
            getComponentsResult = sendCommand("getComponents(" + modelName
                    + ")");
        } catch (ConnectException e) {
            new ConnectException(e.getMessage()).printStackTrace();
        }

        if (getComponentsResult != null
                && getComponentsResult.getError().isEmpty()) {
            componentNames = getComponentsResult.getFirstResult();

            // Delete the first "{".
            StringBuffer componentsBuffer = new StringBuffer(componentNames);
            componentsBuffer.delete(0, 1);
            componentNames = componentsBuffer.toString();
            String componentName = null;
            String componentType = null;
            String[] components = null;
            componentMap = new HashMap<String, String>();

            // Split the getComponents result by "}," in order to access each component.
            components = componentNames.split(getComponentResultDelimiter);

            for (String component : components) {

                componentsBuffer = new StringBuffer(component);
                componentNames = componentsBuffer.deleteCharAt(0).toString();

                // Split the component by "," in order to access each property of the component.
                componentList = componentNames.split(componentsDelimiter);

                // The second element is the name of the component.
                componentName = componentList[1];

                //The 9th element can have four possible values constant, parameter, discrete or unspecified.
                componentType = componentList[8];

                // Delete the first space and quotation.
                // Delete the last quotation.
                componentsBuffer = new StringBuffer(componentType);
                componentType = componentsBuffer.delete(0, 2).toString();
                componentType = componentsBuffer.deleteCharAt(
                        componentsBuffer.length() - 1).toString();

                componentMap.put(componentName, componentType);
            }
        } else {
            String loggerInfo = getComponentsResult.getError();
            _omcLogger.getInfo(loggerInfo);
        }
        return componentMap;
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
     *  @exception ConnectException If we're unable to start communicating with
     *  the server.
     */
    @Override
    public synchronized void initializeServer() throws ConnectException {

        _os = getOs();

        String stringifiedObjectReference = null;
        _startServer();

        try {
            stringifiedObjectReference = _readObjectFromFile();
        } catch (FileNotFoundException e) {
            throw new ConnectException(e.getMessage());
        } catch (IOException e) {
            throw new ConnectException(e.getMessage());
        }

        _setupOmcc(stringifiedObjectReference);

        hasInitialized = true;
    }

    /** Check if there is an error in the return value of sendCommand("command") method and
     *  fetch the error-information of current run.
     *  @param retval The string returned by the OpenModelica Compiler(OMC).
     *  @return Checks If the string is actually an error.
     */
    @Override
    public boolean isError(String retval) {
        if (retval == null) {
            return false;
        }
        // See if there are parse error. An empty list {} also denotes error.
        return retval.toLowerCase().contains("error");
    }

    /** load the Modelica file and library.
     *  @param fileName File that the (base-)model should be loaded from.
     *  @param modelName Name of the (base-)model that should be built.
     *  @exception ConnectException If commands couldn't
     *   be sent to the (OpenModelic Compiler)OMC.
     *  @exception ConnectException If no file found at the selective path for loading.
     */
    @Override
    public void loadModelicaFile(String fileName, String modelName)
            throws ConnectException {

        String loggerInfo = null;
        _filePath = _ptIISystemPath
                + "/ptolemy/domains/openmodelica/demo/OpenModelica/" + fileName;

        File file = new File(_filePath.toString());

        if (file.exists()) {
            loggerInfo = fileName + " found at " + _filePath;
            _omcLogger.getInfo(loggerInfo);

            // Load the Modelica model.
            CompilerResult loadFileInteractiveQualifiedResult = sendCommand("loadFileInteractiveQualified(\""
                    + _filePath + "\")");

            // Check if an error exists in the result of loadFile("command").
            if (!loadFileInteractiveQualifiedResult.getFirstResult().isEmpty()
                    && loadFileInteractiveQualifiedResult.getError().isEmpty()) {
                loggerInfo = modelName + " model is loaded from " + _filePath
                        + " successfully.";
                _omcLogger.getInfo(loggerInfo);
            } else if (!loadFileInteractiveQualifiedResult.getError().isEmpty()) {
                loggerInfo = loadFileInteractiveQualifiedResult.getError();
                _omcLogger.getInfo(loggerInfo);
                throw new ConnectException(loggerInfo);
            }
            // Load the Modelica library by sending loadModel(Modelica) to the OMC server.
            CompilerResult loadModelResult = sendCommand("loadModel(Modelica)");

            // Check if an error exists in the result of sending loadModel(Modelica) to the OMC server.
            if (loadModelResult.getFirstResult().compareTo("true\n") == 0) {
                loggerInfo = "Modelica library is loaded successfully.";
                _omcLogger.getInfo(loggerInfo);
            } else if (!loadModelResult.getError().isEmpty()) {
                loggerInfo = loadModelResult.getError();
                _omcLogger.getInfo(loggerInfo);
                throw new ConnectException(loggerInfo);
            }
        } else {
            throw new ConnectException("No file found at: " + _filePath);
        }
    }

    /** Modify parameter(s) and variable(s) of the Modelica model before building the model.
     *  @param values The new values to change the value of the components.
     *  @param modelName The (base-)model that should be built.
     *  @param components The name of the components to change.
     *  @exception ConnectException If commands couldn't
     *   be sent to the (OpenModelica Compiler)OMC.
     *  @exception IllegalActionException
     */
    @Override
    public void modifyComponents(String values, String modelName,
            String components) throws IllegalActionException, ConnectException {

        String childModel = modelName;
        String childKey = null;
        String loggerInfo = null;
        boolean found = false;
        HashMap<String, String> baseIndividualComponent = null;
        HashMap<String, String> childIndividualComponent = null;
        CompilerResult getComponentModifierNames = null;
        CompilerResult getNthInheritanceClassResult = null;
        String parameterDelimiter = "#";
        String valueDelimiter = ",";
        String[] parameterList = components.split(parameterDelimiter);
        String[] valueList = values.split(valueDelimiter);
        String[] variableList = null;
        //int inheritanceCount = 0;

        try {
            // Return the list of component declarations within (base-)model.
            childIndividualComponent = getModelComponent(childModel);

            if (childIndividualComponent != null) {
                if (parameterList.length == valueList.length) {
                    for (int i = 0; i < parameterList.length; i++) {
                        if (getInheritanceCount(childModel)) {
                            for (int j = 1; j <= 2; j++) {

                                // Return the name of the jth inherited class of the (base-)model.
                                getNthInheritanceClassResult = sendCommand("getNthInheritedClass("
                                        + childModel + "," + j + ")");

                                if (getNthInheritanceClassResult.getError()
                                        .isEmpty()
                                        && !getNthInheritanceClassResult
                                                .getFirstResult().trim()
                                                .toString().contains("Error")) {
                                    baseIndividualComponent = getModelComponent(getNthInheritanceClassResult
                                            .getFirstResult().toString());

                                    if (baseIndividualComponent != null) {
                                        Iterator baseIterator = baseIndividualComponent
                                                .keySet().iterator();
                                        while (baseIterator.hasNext()) {
                                            String key = baseIterator.next()
                                                    .toString();
                                            if (parameterList[i]
                                                    .equalsIgnoreCase(key)) {
                                                found = true;

                                                CompilerResult extendModifierValueResult = sendCommand("setExtendsModifierValue("
                                                        + childModel
                                                        + ","
                                                        + getNthInheritanceClassResult
                                                                .getFirstResult()
                                                                .trim()
                                                                .toString()
                                                        + ","
                                                        + key
                                                        + ",$Code(="
                                                        + valueList[i] + "))");

                                                if (extendModifierValueResult
                                                        .getError().isEmpty()
                                                        && !extendModifierValueResult
                                                                .getFirstResult()
                                                                .trim()
                                                                .toString()
                                                                .contains(
                                                                        "Error")) {

                                                    loggerInfo = "Extended: Component "
                                                            + key
                                                            + " of model "
                                                            + getNthInheritanceClassResult
                                                                    .getFirstResult()
                                                                    .trim()
                                                                    .toString()
                                                            + " is set to "
                                                            + valueList[i]
                                                            + ".";
                                                    _omcLogger
                                                            .getInfo(loggerInfo);
                                                } else {
                                                    loggerInfo = extendModifierValueResult
                                                            .getError();
                                                    _omcLogger
                                                            .getInfo(loggerInfo);
                                                    throw new ConnectException(
                                                            loggerInfo);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // In Modelica, component declaration has variability prefixes discrete,
                        // parameter and constant,that defines in which situation the variable
                        // values of a component are initialized and when they are changed in transient analysis.
                        Iterator childIterator = childIndividualComponent
                                .keySet().iterator();

                        while (childIterator.hasNext()) {
                            childKey = childIterator.next().toString();
                            if (childKey.equalsIgnoreCase(parameterList[i])) {

                                found = true;

                                // "unspecified" means no variability prefix is set for the variable.
                                if (childIndividualComponent.get(childKey)
                                        .compareTo("unspecified") == 0) {

                                    // Return the value of a
                                    // component.
                                    // getComponentModifierNames() now
                                    // takes its second argument with
                                    // double quotes.
                                    String command = "getComponentModifierNames("
                                        + modelName + ",\"" + childKey + "\")";
                                    getComponentModifierNames = sendCommand(command);
                                    if (!getComponentModifierNames.getError()
                                            .toString().isEmpty()) {
                                        _omcLogger
                                                .getInfo(getComponentModifierNames
                                                        .getError().toString());
                                    }

                                    // The variable does not have any parameters.
                                    if (getComponentModifierNames
                                            .getFirstResult().trim()
                                            .compareTo("{}") == 0
                                            || getComponentModifierNames
                                            .getFirstResult()
                                            .trim().length() == 0) {
                                        String oldCommand = command;
                                        // Set the modifier value of a component.
                                        command = "setComponentModifierValue("
                                                + modelName
                                                + ", "
                                                + childKey
                                                + ", $Code(="
                                                + valueList[i]
                                            + "))";

                                        loggerInfo = "The variable has no parameters, the command was \""
                                            + oldCommand + "\".  Now setting the modifier value of a component with \""
                                            + command + "\".";
                                        _omcLogger.getInfo(loggerInfo);

                                        CompilerResult unspecifiedModifier = sendCommand(command);

                                        if (unspecifiedModifier.getError()
                                                .toString().isEmpty()) {
                                            _omcLogger
                                                    .getInfo("Unspecified : Component "
                                                            + childKey
                                                            + " of "
                                                            + modelName
                                                            + " is set to "
                                                            + valueList[i]
                                                            + ".");
                                        }
                                    } else {
                                        StringBuffer componentsBuffer = new StringBuffer(
                                                getComponentModifierNames
                                                        .getFirstResult()
                                                        .trim().toString());
                                        // Stdout was getting: "INFO: [<interactive>:1:1-1:0:writable] Error: Class getComponentModifierNames not found in scope <global scope> (looking for a function or record)."
                                        // and componentsBuffer had length 0.
                                        if (componentsBuffer.length() == 0) {
                                            throw new IllegalActionException("Failed to get the component modifier names, "
                                                    + "the first results was the empty string.  Check standard out for messages.  "
                                                    + "The command was \"" + command
                                                    + "\".  The CompilerResult: " + getComponentModifierNames);
                                        }

                                        // Delete the first and last "{".
                                        componentsBuffer.deleteCharAt(0);
                                        String variableNames = componentsBuffer
                                                .deleteCharAt(
                                                        componentsBuffer
                                                                .length() - 1)
                                                .toString();

                                        // Split the result by "," in order to have access to each simulation parameter's value.
                                        variableList = variableNames
                                                .split(valueDelimiter);
                                        for (String variables : variableList) {
                                            String tempParameter = childKey
                                                    + "." + variables;
                                            // Set the modifier value of a component.
                                            CompilerResult setComponentModifierValueResult = sendCommand("setComponentModifierValue("
                                                    + modelName
                                                    + ", "
                                                    + tempParameter
                                                    + ", $Code(="
                                                    + valueList[i] + "))");

                                            if (setComponentModifierValueResult
                                                    .getError().toString()
                                                    .isEmpty()) {
                                                loggerInfo = "Unspecified : Component "
                                                        + childKey
                                                        + "."
                                                        + variables
                                                        + " of "
                                                        + modelName
                                                        + " is set to "
                                                        + valueList[i] + ".";
                                                _omcLogger.getInfo(loggerInfo);
                                            } else {
                                                loggerInfo = setComponentModifierValueResult
                                                        .getError();
                                                _omcLogger.getInfo(loggerInfo);
                                            }
                                        }
                                    }
                                }
                                //  The "parameter" prefix specifies that the variable is constant during a simulation run,
                                //  but can have its value initialized before a run, or between runs.
                                if (childIndividualComponent.get(childKey)
                                        .compareTo("parameter") == 0) {
                                    // Set the modifier value of a component.
                                    CompilerResult parameterChange = sendCommand("setParameterValue("
                                            + modelName
                                            + ","
                                            + childKey
                                            + ","
                                            + valueList[i] + ")");

                                    if (parameterChange.getError().toString()
                                            .isEmpty()) {
                                        loggerInfo = "Parameter : Component "
                                                + childKey + " of " + modelName
                                                + " is set to " + valueList[i]
                                                + ".";
                                        _omcLogger.getInfo(loggerInfo);
                                    } else {
                                        loggerInfo = getNthInheritanceClassResult
                                                .getError();
                                        _omcLogger.getInfo(loggerInfo);
                                    }
                                } else if (childIndividualComponent.get(
                                        childKey).compareTo("als") == 0) {
                                    // A discrete-time variable is a piecewise constant
                                    // signal which changes its values only at event instants during simulation.
                                    _omcLogger
                                            .getInfo("It's not possible to change the value of "
                                                    + components
                                                    + " in the "
                                                    + modelName
                                                    + ", because of its variablity prefix[discrete].");
                                }
                            }
                        }
                    }
                    if (!found) {
                        _omcLogger.getInfo(components
                                + " does not found in the " + modelName + ".");
                    }
                } else {
                    _omcLogger
                            .getInfo("There is no compatibility between number of parameter(s)/variable(s) and values.");
                }
            }
        } catch (Exception e) {
            throw new IllegalActionException(null, e, e.getMessage());
        }
    }

    /** Plot the plt file by calling PxgraphApplication.main(modelName).
     *  @param modelName Name of the model which should be built.
     *  @exception ConnectException If commands could not be sent to the OMC.
     */
    @Override
    public void plotPltFile(String modelName) throws ConnectException {

        // Array for saving the file path.
        String[] _pltPath = new String[1];

        // Send cd() command to the (OpenModelica Compiler)OMC and fetch working directory of OMC as a result.
        CompilerResult cdResult = sendCommand("cd()");
        _openModelicaWorkingDirectory = cdResult.getFirstResult();
        _openModelicaWorkingDirectory = _openModelicaWorkingDirectory.replace(
                '"', ' ').trim();

        // Save file path in the string array for invoking main() of PxgraphApplication.
        _pltPath[0] = _openModelicaWorkingDirectory + "/" + modelName
                + "_res.plt";

        PxgraphApplication.main(_pltPath);
    }

    /** Build the Modelica model. Then, run the executable result file of
     *  buildModel() in both interactive and non-interactive processing mode
     *  in order to generate the simulation result file.
     *  @param fileName File which the model should be loaded from.
     *  @param modelName Name of the (base-)model which should be built.
     *  @param startTime The start time of simulation.
     *  @param stopTime The stop time of simulation.
     *  @param numberOfIntervals Number of intervals in the result file.
     *  @param outputFormat Format of the result file.
     *  @param processingMode The mode of processing for running the executable result file of building the Modelica model.
     *  @exception ConnectException If commands couldn't
     *   be sent to the (OpenModelic Compiler)OMC.
     *  @exception IOException If the executable result file of buildModel()
     *   couldn't be executed.
     *  @exception IllegalActionException
     */
    @Override
    public void runModel(String fileName, String modelName, String startTime,
            String stopTime, int numberOfIntervals, String outputFormat,
            String processingMode) throws ConnectException, IOException,
            IllegalActionException {

        // Command which is sent to the OMC for building the model.
        String commands = null;

        String loggerInfo = null;

        // Set command of buildModel("command") with Model Name as the name of executable result file.
        commands = modelName + ",startTime="
                + Float.valueOf(startTime).floatValue() + ",stopTime="
                + Float.valueOf(stopTime).floatValue() + ",numberOfIntervals="
                + numberOfIntervals + ",tolerance=1.0E-4"
                + ",method=\"dassl\",outputFormat=\"" + outputFormat
                + "\",variableFilter=\".*\",cflags=\"\",simflags=\"\"";

        loggerInfo = "Building of " + modelName + " model is in progress...";
        _omcLogger.getInfo(loggerInfo);

        // Build the Modelica model by sending buildModel() to the OMC server.
        String command = "buildModel(" + commands + ")";
        CompilerResult buildModelResult = sendCommand(command);

        // Check if an error exists in the result of buildModel("command").
        if (!buildModelResult.getFirstResult().isEmpty()
                && (!buildModelResult.getError().isEmpty() && buildModelResult
                        .getError().contains("Warning"))) {
            loggerInfo = modelName + " model is built successfully.";
            _omcLogger.getInfo(loggerInfo);
        }
        // Error occurred while flattening model BouncingBall
        if (!buildModelResult.getError().isEmpty()
                && !(buildModelResult.getError().contains("Warning"))) {
            loggerInfo = buildModelResult.getError()
                + "The command that failed was \""
                + command + "\"";
            _omcLogger.getInfo(loggerInfo);
            throw new ConnectException(loggerInfo);
        }

        if (!outputFormat.equalsIgnoreCase("quit()")) {

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

            if (processingMode.equalsIgnoreCase("non-interactive")) {

                // Run the executable result file of buildModel("command").
                try {
                    loggerInfo = "Command " + commands
                            + " is running in a non-interactive mode ...";
                    _omcLogger.getInfo(loggerInfo);

                    Runtime.getRuntime().exec(commands,
                            _environmentalVariables, _workDir);
                    loggerInfo = "The executable file runs successfully in non-interactive mode!";
                    _omcLogger.getInfo(loggerInfo);
                } catch (IOException e) {
                    System.err
                            .println("Failed to run the command: " + commands);
                    StringUtilities.exit(1);
                }

                // When users do not select fileNamePrefix for name of executable result file.
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
            } else {
                // BuildModel("modelName") generates an executable file
                // that runs by -interactive flag.

                commands = commands + " -interactive";
                try {
                    loggerInfo = "Command " + commands
                            + " is running in an interactive mode ...";
                    _omcLogger.getInfo(loggerInfo);

                    Runtime.getRuntime().exec(commands,
                            _environmentalVariables, _workDir);
                    loggerInfo = "The executable file runs successfully in an interactive mode!";
                    _omcLogger.getInfo(loggerInfo);
                } catch (IOException e) {
                    new IOException(e.getMessage()).printStackTrace();
                    return;
                }
            }
        }
    }

    /** Send a command to the OpenModelica Compiler(OMC) server and fetches the string result.
     *  @param modelicaCommand The command which should be sent to the OMC.
     *  @return CompilerResult The result of sendExpression("modelicaCommand") to the OMC.
     *  @exception ConnectException If commands couldn't be sent to the OMC.
     */
    public CompilerResult sendCommand(String modelicaCommand)
            throws ConnectException {
        String errorMessage = null;
        String[] returnValue = { "" };

        _omcLogger.getInfo("OMCCommand.sendCommand: " + modelicaCommand);

        if (_couldNotStartOMC) {
            return CompilerResult.makeResult(returnValue, errorMessage);
        }

        if (_numberOfErrors > _showMaxErrors) {
            return CompilerResult.makeResult(returnValue, errorMessage);
        }

        // Trim the start and end spaces.
        modelicaCommand = modelicaCommand.trim();

        if (hasInitialized == false) {
            initializeServer();
        }

        try {
            // Fetch the error string from OpenModelica Compiler(OMC).
            // This should be called after an "Error"
            // is received or whenever the queue of errors are emptied.

            returnValue[0] = omcCommunication.sendExpression(modelicaCommand);

            // Process of starting OMC is terminated normally after creating OpenModelica object reference on disk.

            if (!modelicaCommand.equalsIgnoreCase("quit()")) {
                errorMessage = omcCommunication
                        .sendExpression("getErrorString()");
            } else {
                hasInitialized = false;
            }

            // Make sure the error string is not empty.
            if (errorMessage != null && errorMessage.length() > 2) {
                errorMessage = errorMessage.trim();
                errorMessage = errorMessage.substring(1,
                        errorMessage.length() - 1);
            } else {
                errorMessage = null;
            }

            return CompilerResult.makeResult(returnValue, errorMessage);

        } catch (org.omg.CORBA.COMM_FAILURE x) {

            // FIXME vmcid: SUN  minor code: 211  completed: No
            // Lose connection to OMC(OpenModelica Compiler) server.

            // Generally, this exception occurs when there is significant difference between client code and server code.
            // Client code may have been changed or recompiled while server may not be reflecting same. This may be the
            // version discrepancy too i.e. client and server compiled by different java versions.
            // In my case, it was the server code was changed. Classes on server were changed due to re-compilation of code at server.
            // However, client was still having the old version.
            // COMM_FAILURE is thrown when there is a communication failure.

            _numberOfErrors++;
            throw new ConnectException(x.getCause() + " : Unable to send "
                    + modelicaCommand);
        }
    }

    /** Leave OpenModelica environment, destroy the OMC process and
     *  deallocate OMCCommand object.
     *  @exception ConnectException If quit command couldn't be sent to OMC.
     */
    @Override
    public void stopServer() throws ConnectException {
        if (hasInitialized) {

            sendCommand("quit()");

            _omcCommandInstance = null;
            _omcLogger = null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /** Find the OpenModelica Compiler(OMC) executable file by using path variables.
     *  @param executableName The name of the executable file
     *  @return File The OMC executable file
     */
    private File _findExecutableOnPath(String executableName) {

        // Try path with small letters.
        if (_openModelicaSystemPath == null) {
            _openModelicaSystemPath = System.getenv("path");
        }
        String[] pathDirs = _openModelicaSystemPath.split(File.pathSeparator);

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
     *  @exception ConnectException If OPENMODELICAHOME is not set
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
        String loggerInfo = "Using OPENMODELICAHOME environmental variable to find OMC binary";
        _omcLogger.getInfo(loggerInfo);

        // Standard path to (OpenModelica Compiler)OMC binary is encoded in OPENMODELICAHOME
        // variable.
        if (_openModelicaHome == null) {
            loggerInfo = "OPENMODELICAHOME environmental variable is NULL, trying the PATH variable";
            _omcLogger.getInfo(loggerInfo);
            File omc = _findExecutableOnPath(binaryName);
            if (omc != null) {
                loggerInfo = "Found omc executable in the path here: "
                        + omc.getAbsolutePath();
                _omcLogger.getInfo(loggerInfo);

                _openModelicaHome = omc.getParentFile().getParentFile()
                        .getAbsolutePath();
            } else {
                final String m = "Environmental variable OPENMODELICAHOME is not set and we could not find: "
                        + binaryName + " in the PATH";
                _omcLogger.getInfo(m);
                throw new ConnectException(m);
            }
        }

        openModelicaHomeDirectory = new File(_openModelicaHome);

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
                loggerInfo = "OMC binary is located at '"
                        + omcBinary.getAbsolutePath() + "'";
                _omcLogger.getInfo(loggerInfo);
                break;
            } else {
                loggerInfo = "No OMC binary at: [" + path + "]";
                _omcLogger.getInfo(loggerInfo);
            }
        }

        if (omcBinary == null) {
            loggerInfo = "Could not find OMC binary on the OPENMODELICAHOME path or in the PATH variable";
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
        return new File[] { omcBinary, omcWorkingDirectory };
    }

    /** Return the path to the OpenModelica(OM) object reference that is stored on a disk.
     *  @return String The path to the OM object reference.
     */
    private String _getPathToObject() {

        String pathName = null;

        switch (getOs()) {

        // Add _corbaSession to the end of OM object reference name to make it unique.
        case UNIX:
            if (_username == null) {
                System.err
                        .println("Could not get user.name property?  Using 'nobody'.");
                _username = "nobody";
            }
            if (_corbaSession == null || _corbaSession.equalsIgnoreCase("")) {
                pathName = _temp + "/openmodelica." + _username + ".objid";
            } else {
                pathName = _temp + "/openmodelica." + _username + ".objid"
                        + "." + _corbaSession;
            }
            break;
        case WINDOWS:
            if (_corbaSession == null || _corbaSession.equalsIgnoreCase("")) {
                pathName = _temp + "openmodelica.objid";
            } else {
                pathName = _temp + "openmodelica.objid" + "." + _corbaSession;
            }
            break;
        case MAC:
            if (_username == null) {
                System.err
                        .println("Could not get user.name property?  Using 'nobody'.");
                _username = "nobody";
            }
            if (_corbaSession == null || _corbaSession.equalsIgnoreCase("")) {
                pathName = _temp + "openmodelica." + _username + ".objid";
            } else {
                pathName = _temp + "openmodelica." + _username + ".objid" + "."
                        + _corbaSession;
            }
            break;
        }

        return pathName;
    }

    /** Read OpenModelica(OM) object reference from a file on a disk.
     *  @return The object reference as a String.
     *  @exception ConnectException If there is an error in reading OM object reference.
     */
    private String _readObjectFromFile() throws FileNotFoundException,
            IOException {

        File file = new File(_getPathToObject());

        String stringifiedObjectReference = null;
        BufferedReader bufferReader = null;
        FileReader fileReader = null;

        try {
            fileReader = new FileReader(file);
            String loggerInfo = "OpenModelica object reference found at "
                    + _temp;
            _omcLogger.getInfo(loggerInfo);
        } catch (FileNotFoundException e) {
            // FIXME: The exception should be caught, not the stack printed
            new FileNotFoundException(
                    "Unable to find OpenModelica object reference located at "
                            + _temp + " !").printStackTrace();
        }

        if (fileReader == null) {
            // Not likely to happen
            new FileNotFoundException(
                    "Unable to find OpenModelica object reference located at "
                            + _temp + " !").printStackTrace();
        } else {
            bufferReader = new BufferedReader(fileReader);

            try {
                stringifiedObjectReference = bufferReader.readLine();
                bufferReader.close();
                String loggerInfo = "OpenModelica Object reference at " + _temp
                        + " is read successfuly!";
                _omcLogger.getInfo(loggerInfo);
            } catch (IOException e) {
                // FIXME: The exception should be caught, not the stack printed
                new IOException(
                        "Unable to read OpenModelica object reference from "
                                + _temp + " !").printStackTrace();
            }
            return stringifiedObjectReference;
        }
        return null;
    }

    /** Initialize an ORB, convert the stringified OpenModelica
     *  Compiler(OMC) object to a real CORBA object and then narrow
     *  that object to an OmcCommunication object.
     */
    private synchronized void _setupOmcc(String stringifiedObjectReference) {

        String arguments[] = { null };

        // Set the CORBA read timeout to a larger value as we send huge amounts of data
        // from (OpenModelica Compiler)OMC to (Modelica Development Tooling)MDT.
        System.setProperty("com.sun.CORBA.transport.ORBTCPReadTimeouts",
                "1:60000:300:1");

        ORB orb;
        orb = ORB.init(arguments, null);

        try {
            // Convert string to CORBA object.
            org.omg.CORBA.Object obj = orb
                    .string_to_object(stringifiedObjectReference);

            // Convert object to OmcCommunication object.
            omcCommunication = OmcCommunicationHelper.narrow(obj);
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to convert string \""
                    + stringifiedObjectReference + "\" to an object.",
                    throwable);
        }
    }

    /** Start the (OpenModelica Compiler)OMC server by starting OMCThread.
     *  @exception ConnectException If OPENMODELICAHOME is not set
     *  and we could not find binary file in the path.
     */
    private synchronized void _startServer() throws ConnectException {

        if (!_fOMCThreadHasBeenScheduled) {

            _fOMCThread.start();
            _fOMCThreadHasBeenScheduled = true;

            synchronized (_fOMCThread) {
                try {
                    _fOMCThread.wait(5000);
                } catch (InterruptedException e) {
                    throw new ConnectException(e.getMessage());
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Initialize _corbaSession.
    private String _corbaSession = new SimpleDateFormat("HHmmss")
            .format(new Date());

    // Indicate if we give up on running OpenModelica Compiler(OMC) as it is unable to start.
    private boolean _couldNotStartOMC = false;

    // Environmental variables which should be used for running the result of buildModel.
    private String[] _environmentalVariables = null;

    // Set file parameter to the path of dcmotor.mo.
    private String _filePath = null;

    // This object is used for starting OpenModelica Compiler(OMC)'s thread.
    private final OMCThread _fOMCThread = new OMCThread();

    // Flag which indicates whether the server should start or not.
    private boolean _fOMCThreadHasBeenScheduled = false;

    // Initialization of the number of errors.
    private int _numberOfErrors = 0;

    // OMCCommand Object for accessing a unique source of instance.
    private static OMCCommand _omcCommandInstance = null;

    // OMCLogger Object for accessing a unique source of instance.
    private OMCLogger _omcLogger = null;

    // Process that starts OMC.
    private Process _omcProcess = null;

    // Environmental variable OPENMODELICAHOME.
    private String _openModelicaHome = System.getenv("OPENMODELICAHOME");

    // The system path of OpenModelica.
    private String _openModelicaSystemPath = System.getenv("PATH");

    // The working directory of the OMC is fetched from sending cd() command to the OMC.
    private String _openModelicaWorkingDirectory = null;

    // The (Operating System)OS we are running on.
    private _osType _os;

    // Different types of Operating System(OS).
    private enum _osType {
        UNIX, WINDOWS, MAC
    }

    // Maximum number of compiler errors to display.
    private int _showMaxErrors = 10;

    // The system path of PTII.
    private String _ptIISystemPath = StringUtilities
            .getProperty("ptolemy.ptII.dir");

    // Temp directory.
    private String _temp = System.getProperty("java.io.tmpdir");

    // The name of the user.
    private String _username = StringUtilities.getProperty("user.name");

    // The working directory of the process.
    private File _workDir = null;

}
