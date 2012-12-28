/* An actor that executes a Modelica script.
 
 Below is the copyright agreement for the Ptolemy II system.
 Version: $Id$

 Copyright (c) 1998-2012 The Regents of the University of California
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
import java.util.Iterator;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.domains.openmodelica.kernel.OpenModelicaDirector;
import ptolemy.domains.openmodelica.lib.core.CompilerResult;
import ptolemy.domains.openmodelica.lib.core.compiler.ConnectException;
import ptolemy.domains.openmodelica.lib.omc.OMCProxy;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 *   An actor that executes a Modelica script. it translates the model and
 *   simulate that. There is one  actor provided in the Vergil
 *   libraries Under OpenModelica directory. It is called "simulate" has no
 *   input and output port; to view or edit its Modelica script, look inside the actor.

     The simulate actor works for the model which is composed of only one class,
     Adding the functionality of working for the multiple classes
     which all will be displayed in the className Parameter will be added in the next version.

     dcmotor.mo should be selected as the fileParameter and dcmotor as the model name.
     loadModel(Modelica) is needed for the simulation and should be set in the ModelicaScript parameter.
     The rest of the settings are optional.
     The simulation result is saved as a (mat,csv or plt)file and also there is another alternative 
     empty which is used when there is no need for users to have the result file. 
     The log file as the text format is saved in the temp folder.

    @author Mana Mirzaei
    @version $Id$

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

        iteration = new Variable(this, "iteration", new IntToken(0));

        modelicaScript = new StringParameter(this, "modelicaScript");
        modelicaScript.setDisplayName("Write OM Command");

        fileName = new FileParameter(this, "fileName");
        fileName.setDisplayName("File name");

        //className = new StringParameter(this, "className");
        //className.setTypeEquals(BaseType.STRING);

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

        fileNamePrefix = new StringParameter(this, "fileNamePrefix");
        fileNamePrefix.setTypeEquals(BaseType.STRING);
        fileNamePrefix.setDisplayName("File name prefix");

        outputFormat = new StringParameter(this, "outputFormat");
        outputFormat.setDisplayName("Output format");
        outputFormat.addChoice("mat");
        outputFormat.addChoice("csv");
        outputFormat.addChoice("plt");
        outputFormat.addChoice("empty");

        variableFilter = new StringParameter(this, "variableFilter");
        variableFilter.setTypeEquals(BaseType.STRING);
        variableFilter.setDisplayName("Variable filter");

        cflags = new StringParameter(this, "cflags");
        cflags.setTypeEquals(BaseType.STRING);

        simflags = new StringParameter(this, "simflags");
        simflags.setTypeEquals(BaseType.STRING);
        simflags.setDisplayName("Simulation flag");
    }

    ///////////////////////////////////////////////////////////////////
    ////                    public ports and parameters                  ////

    /** Any standard c language flags,  If this value is changed
     *  during execution, then the new value will be the output on the
     *  next iteration.  The default value of this parameter is empty.
     */
    public StringParameter cflags;

    //public StringParameter className;

    /** File which the model should be loaded from .  If this value is
     *  changed during execution, then the new value will be the
     *  output on the next iteration.  There is no default value, file should be selected.
     */

    public FileParameter fileName;

    /** User preferable name for the result file instead of modelName.
     *  If this value is changed during execution, then the new value
     *  will be the output on the next iteration.  The default value
     *  of this parameter is null.
     */
    public StringParameter fileNamePrefix;

    // FIXME: alphabetize the parameters.

    /** Integration method used for simulation.  If this value is
     *  changed during execution, then the new value will be the
     *  output on the next iteration.  The default value of this
     *  parameter is the string "dassl".
     */
    public StringParameter method;

    /** Name of the model which should be built.  If this value is
     *  changed during execution, then the new value will be the
     *  output on the next iteration.  The default value of this
     *  parameter is the string dcmotor.
     */

    public StringParameter modelName;

    /** Number of intervals in the result file.  If this value is
     *  changed during execution, then the new value will be the
     *  output on the next iteration.  The default value of this
     *  parameter is the integer 500.
     */
    public Parameter numberOfIntervals;

    /** Format for the result file.  If this value is changed during
     *  execution, then the new value will be the output on the next
     *  iteration.  The default value of this parameter is the string
     *  "mat".
     */
    public StringParameter outputFormat;

    /** Modelica command.  If this value is changed during execution,
     *  then the new value will be the output on the next iteration.
     *  The default value of this parameter is the string
     *  loadModel(Modelica).
     */
    public StringParameter modelicaScript;

    /** Simulation flags.  If this value is changed during execution,
     *  then the new value will be the output on the next iteration.
     *  The default value of this parameter is the string "".
     */
    public StringParameter simflags;

    /** The start time of the simulation.  If this value is changed
     *  during execution, then the new value will be the output on the
     *  next iteration.  The default value of this parameter is the
     *  double 0.0.
     */

    public Parameter simulationStartTime;

    /** The stop time of the simulation.  If this value is changed
     *  during execution, then the new value will be the output on the
     *  next iteration.  The default value of this parameter is the
     *  double 0.1.
     */
    public Parameter simulationStopTime;

    /** Tolerance used by the integration method.  If this value is
     *  changed during execution, then the new value will be the
     *  output on the next iteration.  The default value of this
     *  parameter is the double 0.0001.
     */

    public Parameter tolerance;

    /** Filter for variables that should store in result file.  If
     *  this value is changed during execution, then the new value
     *  will be the output on the next iteration.  The default value
     *  of this parameter is the string ".*".
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
            newObject.iteration = (Variable) newObject
                    .getAttribute("iteration");
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
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        Director director = getDirector();

        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }

        try {
            /*Load the model from the file in the first step. Then, build the
              model. Finally, run the simulation executable result of
              buildModel() method in order to generate the simulation
              result.*/
            simulate();
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Unable to simulate the model!");
        }
    }

    /** Initialize the iteration count to 1.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        //It initializes the private and protected members
        _iterationcount = 1;
        iteration.setToken(new IntToken(_iterationcount));

    }

    /** Increment the iteration count.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _iterationcount++;
        iteration.setToken(new IntToken(_iterationcount));

        // This actor never requests termination.
        return true;
    }

    /** Return true if all input ports have at least one token.
     *  @return True if this actor is ready for firing, false otherwise.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        Iterator inputPorts = inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) inputPorts.next();

            if (!port.hasToken(0)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
    }

    /**
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Load the model from the file in the first step. Then, build the
     *  model. Finally, run the simulation executable result of
     *  buildModel() method in order to generate the simulation
     *  result.
     * @exception ConnectException If commands couldn't
     *  be sent to the OMC.
     * @exception IOException If the executable result of buildModel()
     * couldn't be executed.
     * */

    private void simulate() throws ConnectException, IOException {

        String str = null;

        /*Set fileName to the path of the testmodel(dcmotor.mo)*/

        String systemPath = System.getProperty("java.class.path");
        String[] pathDirs = systemPath.split(File.pathSeparator);
        systemPath = pathDirs[0];

        String filePath = null;
        switch (OMCProxy.os) {
        case WINDOWS:
            filePath = systemPath
                    + "\\ptolemy\\domains\\openmodelica\\demo\\OpenModelica\\dcmotor.mo";
            filePath = filePath.replace("\\", "/");
            break;
        case UNIX:
            filePath = systemPath
                    + "/ptolemy/domains/openmodelica/demo/OpenModelica/dcmotor.mo";
            break;
        case MAC:
            filePath = systemPath
                    + "/ptolemy/domains/openmodelica/demo/OpenModelica/dcmotor.mo";
            break;
        }
        fileName.setExpression(filePath);

        /*Check if the file exists*/

        File file = new File(filePath);

        if (file.exists()) {
            OpenModelicaDirector._ptLogger.getInfo("Using model at '"
                    + filePath + "'");
        } else {

            OpenModelicaDirector._ptLogger.getInfo("No model found at: ["
                    + filePath + "]");
        }

        /*Load the model from the selected file*/

        _result = OpenModelicaDirector._omcPr
                .loadFile(fileName.getExpression());

        /*Check if there is an error in the result of the loadFile command*/
        if (_result.getFirstResult().compareTo("") != 0
                && _result.getError().compareTo("") == 0) {
            OpenModelicaDirector._ptLogger.getInfo("Model is loaded from "
                    + fileName.getExpression() + " successfully.");
        }
        if (_result.getError().compareTo("") != 0) {
            OpenModelicaDirector._ptLogger
                    .getInfo("There is an error in loading the model!");
        }

         // Set the modelicaScript expression to the loadModel
         // command which loads the file corresponding to the class.
        if (modelicaScript.getExpression().compareTo("") == 0)
            modelicaScript.setExpression("loadModel(Modelica)");

        _result = OpenModelicaDirector._omcPr.sendCommand(modelicaScript
                .getExpression());

        /*Check if there is an error in the result of the loadModel command*/
        if (_result.getFirstResult().compareTo("true\n") == 0) {
            OpenModelicaDirector._ptLogger
                    .getInfo("Modelica model is loaded successfully.");
        }
        if (_result.getError().compareTo("") != 0) {
            OpenModelicaDirector._ptLogger
                    .getInfo("There is an error in loading Modelica model!");
        }

        /* Set the default value of buildModel expression*/

        if (simulationStartTime.getExpression().compareTo("") == 0) {
            simulationStartTime.setExpression("0.0");
        }

        if (simulationStopTime.getExpression().compareTo("") == 0) {
            simulationStopTime.setExpression("0.1");
        }

        if (numberOfIntervals.getExpression().compareTo("") == 0) {
            numberOfIntervals.setExpression("500");
        }

        if (tolerance.getExpression().compareTo("") == 0) {
            tolerance.setExpression("0.0001");
        }

        if (method.getExpression().compareTo("") == 0) {
            method.setExpression("dassl");
        }

        if (outputFormat.getExpression().compareTo("") == 0) {
            outputFormat.setExpression("mat");
        }

        if (variableFilter.getExpression().compareTo("") == 0) {
            variableFilter.setExpression(".*");
        }

        if (cflags.getExpression().compareTo("") == 0) {
            cflags.setExpression("");
        }

        if (simflags.getExpression().compareTo("") == 0) {
            simflags.setExpression("");
        }

        /* Set the buildModel expression according to the user needs
         * if user wants the result with the new name or not */

        if (fileNamePrefix.getExpression().compareTo("") == 0) {
            OpenModelicaDirector._ptLogger
                    .getInfo("Build the model without fileNamePrefix.");
            str = modelName.getExpression()
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
                    + "\",variableFilter=\"" + variableFilter.getExpression()
                    + "\",cflags=\"" + cflags.getExpression()
                    + "\",simflags=\"" + simflags.getExpression() + "\"";
        } else {
            OpenModelicaDirector._ptLogger
                    .getInfo("Build the model with fileNamePrefix.");
            str = modelName.getExpression()
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
                    + "\",fileNamePrefix=\"" + fileNamePrefix.getExpression()
                    + "\",outputFormat=\"" + outputFormat.getExpression()
                    + "\",variableFilter=\"" + variableFilter.getExpression()
                    + "\",cflags=\"" + cflags.getExpression()
                    + "\",simflags=\"" + simflags.getExpression() + "\"";
        }

        _result = OpenModelicaDirector._omcPr.buildModel(str);

        /*Check if there is an error in the result of the buildModel command*/

        if (_result.getFirstResult().compareTo("") != 0
                && _result.getError().compareTo("") == 0) {
            OpenModelicaDirector._ptLogger.getInfo(modelName.getExpression()
                    + " Model is built successfully.");
        }
        if (_result.getError().compareTo("") != 0) {
            OpenModelicaDirector._ptLogger
                    .getInfo("There is an error in building the model.");
        }

        String command = null;
        OMCProxy.os = OMCProxy.getOs();
        switch (OMCProxy.os) {
        case WINDOWS:
            command = OMCProxy.workDir.getPath() + "\\"
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

        /* Run the executable result of buildModel command */

        Runtime.getRuntime().exec(command, OMCProxy.env, OMCProxy.workDir);

        /* Check if user wants the new name for the result file */

        if (fileNamePrefix.getExpression().compareTo("") == 0) {
            OpenModelicaDirector._ptLogger.getInfo(modelName.getExpression()
                    + " is executed successfully.");
        } else {
            OpenModelicaDirector._ptLogger.getInfo(fileNamePrefix
                    .getExpression() + " is executed successfully.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Variable iteration;
    /** Count of the number of iterations. */
    private int _iterationcount = 1;
    /** Returning result from the omc compiler. */
    CompilerResult _result;
}
