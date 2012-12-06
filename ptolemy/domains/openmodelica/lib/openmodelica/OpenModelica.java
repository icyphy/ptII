/* An actor that executes a Modelica script. 

 Copyright (c) 1998-2012 The Regents of the University of California and
 Research in Motion Limited.
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
package ptolemy.actor.lib.openmodelica;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import ptolemy.actor.Director;
import ptolemy.actor.gui.style.TextStyle;
import ptolemy.actor.IOPort;
import ptolemy.actor.lib.openmodelica.core.CompilerResult;
import ptolemy.actor.lib.openmodelica.core.compiler.ConnectException;
import ptolemy.actor.lib.openmodelica.omc.OMCProxy;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.openmodelica.kernel.OpenModelicaDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 *   An actor that executes a Modelica script. it translates the model and 
 *   simulate that. There is one  actor provided in the Vergil 
 *   libraries Under OpenModelica directory. It is called "simulate" has no 
 *   input and output port; to view or edit its Modelica script, look inside the actor.
    
     The simulate actor works for the model which is composed of only one class,
     Adding the functionality of working for the multiple classes
     which all will be displayed in the className Parameter will be added in the next version. 
     
     dcmotor.mo should be selected as the fileParameter and dcmotor as the model name.
     loadModel(Modelica) is needed for the simulation and should be set in the preScript parameter.
     The rest of the settings are optional.
     The simulation result is saved as a (mat,csv or plt)file and the log file as the txt file in the temp folder.
     
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

        _iteration = new Variable(this, "iteration", new IntToken(0));
        //output = new TypedIOPort(this, "output", false, true);
        //output.setTypeEquals(BaseType.STRING);
        preScript = new StringParameter(this, "preScript (Write OM Command) ");
        new TextStyle(preScript, "preScript");
        fileNamePar = new FileParameter(this, "fileNamePar");
        //className = new StringParameter(this, "className");
        //className.setTypeEquals(BaseType.STRING);
        modelName = new StringParameter(this, "modelName");
        modelName.setTypeEquals(BaseType.STRING);
        simStartTime = new Parameter(this, "simStartTime", new DoubleToken(0.0));
        simStartTime.setTypeEquals(BaseType.DOUBLE);
        simStopTime = new Parameter(this, "simStopTime", new DoubleToken(0.1));
        simStopTime.setTypeEquals(BaseType.DOUBLE);
        noOfIntervals = new Parameter(this, "noOfIntervals", new IntToken(500));
        noOfIntervals.setTypeEquals(BaseType.INT);
        tolerance = new Parameter(this, "tolerance", new DoubleToken(0.0001));
        tolerance.setTypeEquals(BaseType.DOUBLE);
        method = new StringParameter(this, "method");
        method.setTypeEquals(BaseType.STRING);
        fileNamePrefix = new StringParameter(this, "fileNamePrefix");
        fileNamePrefix.setTypeEquals(BaseType.STRING);
        outputFormat = new StringParameter(this,
                "outputFormat (select format : mat , plt , csv , empty)");
        outputFormat.setTypeEquals(BaseType.STRING);
        variableFilter = new StringParameter(this, "variableFilter");
        variableFilter.setTypeEquals(BaseType.STRING);
        cflags = new StringParameter(this, "cflags");
        cflags.setTypeEquals(BaseType.STRING);
        simflags = new StringParameter(this, "simflags");
        simflags.setTypeEquals(BaseType.STRING);
    }

    /////////////////////////////////////////////////////////////////////////
    ////                    public ports and parameters                  ////

    /** TODO : Add description for Cflags */
    public StringParameter cflags;
    //public StringParameter className;
    /** File which the model should be loaded from */
    public FileParameter fileNamePar;
    /** User preferable name for the result file instead of modelName */
    public StringParameter fileNamePrefix;
    /** Integration method used for simulation */
    public StringParameter method;
    /** Name of the model which should be built */
    public StringParameter modelName;
    /** Number of intervals in the result file */
    public Parameter noOfIntervals;
    //public TypedIOPort output;
    /** Format for the result file */
    public StringParameter outputFormat;
    /** Modelica command */
    public StringParameter preScript;
    /** Simulation flags */
    public StringParameter simflags;
    /** The start time of the simulation */
    public Parameter simStartTime;
    /** The stop time of the simulation */
    public Parameter simStopTime;
    /** Tolerance used by the integration method */
    public Parameter tolerance;
    /** Filter for variables that should store in result file*/
    public StringParameter variableFilter;

    /////////////////////////////////////////////////////////////////////////
    ////                    public methods                               ////

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
        //Initialize the private and protected members
        _iterationCount = 1;
        _iteration.setToken(new IntToken(_iterationCount));

    }

    /** Increment the iteration count.
     *  @exception IllegalActionException If the superclass throws it.
     */

    @Override
    public boolean postfire() throws IllegalActionException {
        _iterationCount++;
        _iteration.setToken(new IntToken(_iterationCount));

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
            IOPort port = (IOPort) (inputPorts.next());

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
    ////                         private methods                    ////

    /** First, load the model from the file. Second, build the model. Finally, 
     *  run the simulation executable result of buildModel() 
     *  method in order to generate the simulation result.
     * @exception ConnectException If commands couldn't 
     *  be sent to the OMC.
     * @throws IOException If the executable result of buildModel()
     * couldn't be executed.
     * */

    private void simulate() throws ConnectException, IOException {

        String str = null;

        /*setting fileNamePar to the path of the testmodel which is called dcmotor.mo*/

        String systemPath = System.getProperty("java.class.path");
        String[] pathDirs = systemPath.split(File.pathSeparator);
        systemPath = pathDirs[0];
        String filePath = systemPath
                + "\\ptolemy\\domains\\openmodelica\\lib\\test\\auto\\dcmotor.mo";
        filePath = filePath.replace("\\", "/");
        fileNamePar.setExpression(filePath);

        /*load models from the file parameter */

        _result = OpenModelicaDirector._omcPr.loadFile(fileNamePar
                .getExpression());
        if (_result.getError().compareTo("") == 0)
            OpenModelicaDirector._ptLogger.getInfo("Model is loaded from "
                    + fileNamePar.getExpression() + " successfully.");

        /*
         * preScript expression is set to the loadModel() method which loads the file 
         * corresponding to the class, using the Modelica class.
         */

        _result = OpenModelicaDirector._omcPr.sendCommand(preScript
                .getExpression());
        if (_result.getError().compareTo("") == 0)
            OpenModelicaDirector._ptLogger
                    .getInfo("Modelica model is loaded successfully.");

        /* optional settings of buildModel() method  */

        if (simStartTime.getExpression().compareTo("") == 0)
            simStartTime.setExpression("0.0");

        if (simStopTime.getExpression().compareTo("") == 0)
            simStopTime.setExpression("0.1");

        if (noOfIntervals.getExpression().compareTo("") == 0)
            noOfIntervals.setExpression("500");

        if (tolerance.getExpression().compareTo("") == 0)
            tolerance.setExpression("0.0001");

        if (method.getExpression().compareTo("") == 0)
            method.setExpression("dassl");

        if (outputFormat.getExpression().compareTo("") == 0)
            outputFormat.setExpression("mat");

        if (variableFilter.getExpression().compareTo("") == 0)
            variableFilter.setExpression(".*");

        if (cflags.getExpression().compareTo("") == 0)
            cflags.setExpression("");

        if (simflags.getExpression().compareTo("") == 0)
            simflags.setExpression("");
        if (fileNamePrefix.getExpression().compareTo("") == 0) {
            OpenModelicaDirector._ptLogger
                    .getInfo("Model without fileNamePrefix.");
            str = modelName.getExpression() + ",startTime="
                    + Float.valueOf(simStartTime.getExpression()).floatValue()
                    + ",stopTime="
                    + Float.valueOf(simStopTime.getExpression()).floatValue()
                    + ",numberOfIntervals="
                    + Integer.parseInt(noOfIntervals.getExpression())
                    + ",tolerance="
                    + Float.valueOf(tolerance.getExpression()).floatValue()
                    + ",method=\"" + method.getExpression()
                    + "\",outputFormat=\"" + outputFormat.getExpression()
                    + "\",variableFilter=\"" + variableFilter.getExpression()
                    + "\",cflags=\"" + cflags.getExpression()
                    + "\",simflags=\"" + simflags.getExpression() + "\"";
        } else {
            OpenModelicaDirector._ptLogger
                    .getInfo("Model with fileNamePrefix.");
            str = modelName.getExpression() + ",startTime="
                    + Float.valueOf(simStartTime.getExpression()).floatValue()
                    + ",stopTime="
                    + Float.valueOf(simStopTime.getExpression()).floatValue()
                    + ",numberOfIntervals="
                    + Integer.parseInt(noOfIntervals.getExpression())
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
        if (fileNamePrefix.getExpression().compareTo("") == 0
                && _result.getError().compareTo("") == 0)
            OpenModelicaDirector._ptLogger.getInfo(modelName.getExpression()
                    + " Model is built successfully.");
        if (fileNamePrefix.getExpression().compareTo("") != 0)
            OpenModelicaDirector._ptLogger.getInfo(fileNamePrefix
                    .getExpression() + " Model is built successfully.");

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
        }

        Runtime.getRuntime().exec(command, OMCProxy.env, OMCProxy.workDir);

        if (fileNamePrefix.getExpression().compareTo("") == 0)
            OpenModelicaDirector._ptLogger.getInfo(modelName.getExpression()
                    + " is executed successfully.");
        else
            OpenModelicaDirector._ptLogger.getInfo(fileNamePrefix
                    .getExpression() + " is executed successfully.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Variable _iteration;

    private int _iterationCount = 1;
    CompilerResult _result;
}
