/* An actor that executes a Modelica script.
 
 Below is the copyright agreement for the Ptolemy II system.
 
 Copyright (c) 2012 The Regents of the University of California
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
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;

import ptolemy.domains.openmodelica.kernel.OpenModelicaDirector;
import ptolemy.domains.openmodelica.lib.compiler.CompilerResult;
import ptolemy.domains.openmodelica.lib.compiler.ConnectException;
import ptolemy.domains.openmodelica.lib.omc.OMCProxy;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;

/**
    An actor that executes a Modelica script. it translates and
    simulates the model.  There is one actor provided in the Vergil,
    <i>MoreLibraries</i> Under <i>OpenModelica</i>.  It is called
    <i>OpenModelica</i>; To view or edit its Modelica script, look
    inside the actor.

    <p>The OpenModelica actor works for the model which is composed of only one class.</p>

    // FIXME: I'm not sure what you are saying here.  Are you describing
    // the default values?

    <p> <i>dcmotor.mo</i> should be selected as the fileParameter and
    <i>dcmotor</i> as the model name.  <i>loadModel(Modelica)</i> is
    needed for the simulation and should be set in the ModelicaScript
    parameter.  The rest of the settings are optional.  The simulation
    result is saved as a <i>mat</i>,<i>csv</i> or <i>plt</i> file and
    also there is another alternative <i>empty</i> which is used when
    there is no need for users to have the result file.</p>

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

        // FIXME: is this used?
        _iteration = new Variable(this, "iteration", new IntToken(0));

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

    /** Format for the result file.  
     *  The default value of this parameter is the string "mat".
     */
    public StringParameter outputFormat;

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
            newObject._iteration = (Variable) newObject
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
            //  Load the model from the file in the first step. 
            //  Build the model. 
            //  Run the simulation executable result of buildModel() method in order to generate the simulation result.
            simulate();
        } catch (Throwable throwable) {
            throwable = new IllegalActionException(
                    "Unable to simulate the model!");
            throwable.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // FIXME: it is best to use javadoc comments for private methods
    // so that the documentation can be found from within Eclipse.

    // Load the model from the file in the first step. Then, build the
    //  model. Finally, run the simulation executable result of
    //  buildModel() in order to generate the simulation result.
    // @exception ConnectException If commands couldn't
    //  be sent to the OMC.
    // @exception IOException If the executable result of buildModel()
    // couldn't be executed.

    // FIXME: Rename to _simulate().  Protected and private methods and fields
    // start with an underscore.
    private void simulate() throws ConnectException, IOException {

        // FIXME: We don't use short names like 'str'.  What is the meaning
        // of the contents of this string?  Is it a message?  Then maybe
        // use the variable name "message".  It looks like commands that are
        // passed to OMC, so maybe "commands" would be a good variable name.
        String str = null;

        // Set fileName to the path of the testmodel(dcmotor.mo)
        String systemPath = StringUtilities.getProperty("ptolemy.ptII.dir");

        String filePath = null;
        // FIXME: it probably is not necessary to use backslashes here, Java
        // handles forward slashes fine under Windows.
        // Also this code sets the filePath to the same thing for all platforms.
        switch (OMCProxy.getOs()) {
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

        File file = new File(filePath);

        if (file.exists()) {
            OpenModelicaDirector.getOMCLogger().getInfo(
                    "Using model at '" + filePath + "'");
        } else {

            OpenModelicaDirector.getOMCLogger().getInfo(
                    "No model found at: [" + filePath + "]");
        }

        // FIXME: comments should be complete sentences and end in a period.

        // Load the model from the selected file
        _result = OpenModelicaDirector.getOMCProxy().loadFile(
                fileName.getExpression());

        // Check if there an error exists in the result of the loadFile command
        if (_result.getFirstResult().compareTo("") != 0
                && _result.getError().compareTo("") == 0) {
            OpenModelicaDirector.getOMCLogger().getInfo(
                    "Model is loaded from " + fileName.getExpression()
                            + " successfully.");
        }
        if (_result.getError().compareTo("") != 0) {
            OpenModelicaDirector.getOMCLogger().getInfo(
                    "There is an error in loading the model!");
        }

        // The loadModel command loads the file corresponding to the class.
        if (modelicaScript.getExpression().compareTo("") == 0)
            modelicaScript.setExpression("loadModel(Modelica)");

        _result = OpenModelicaDirector.getOMCProxy().sendCommand(
                modelicaScript.getExpression());

        // Check if there is an error in the result of the loadModel command
        if (_result.getFirstResult().compareTo("true\n") == 0) {
            OpenModelicaDirector.getOMCLogger().getInfo(
                    "Modelica model is loaded successfully.");
        }
        if (_result.getError().compareTo("") != 0) {
            OpenModelicaDirector.getOMCLogger().getInfo(
                    "There is an error in loading Modelica model!");
        }

        // FIXME: use // style comments inside method bodies.
        /* Set the default value of buildModel parameters */

        // FIXME: This is a little unusual.  If the parameters are empty,
        // you are setting defaults.  Should the defaults be set in the
        // constructor?
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

        // FIXME: these two if statements have no effect?
        if (cflags.getExpression().compareTo("") == 0) {
            cflags.setExpression("");
        }

        if (simflags.getExpression().compareTo("") == 0) {
            simflags.setExpression("");
        }

        // Set the buildModel expression according to the user needs
        // if user wants the result with the new name 
        if (fileNamePrefix.getExpression().compareTo("") == 0) {
            OpenModelicaDirector.getOMCLogger().getInfo(
                    "Build the model without fileNamePrefix.");
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
            OpenModelicaDirector.getOMCLogger().getInfo(
                    "Build the model with fileNamePrefix.");
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

        _result = OpenModelicaDirector.getOMCProxy().buildModel(str);

        // Check if there is an error in the result of buildModel()
        if (_result.getFirstResult().compareTo("") != 0
                && _result.getError().compareTo("") == 0) {
            OpenModelicaDirector.getOMCLogger()
                    .getInfo(
                            modelName.getExpression()
                                    + " Model is built successfully.");
        }
        if (_result.getError().compareTo("") != 0) {
            // FIXME: shouldn't this throw an exception in Ptolemy?
            // If not, then document why not.
            OpenModelicaDirector.getOMCLogger().getInfo(
                    "There is an error in building the model.");
        }

        String command = null;

        switch (OMCProxy.getOs()) {
        case WINDOWS:
            // FIXME: you probably don't need the backslash here, but
            // you do need the FIXME.
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

        // Run the executable result of buildModel() 
        Runtime.getRuntime().exec(command, OMCProxy.environmentalVariables,
                OMCProxy.workDir);

        // Check if user wants the new name for the result file 
        if (fileNamePrefix.getExpression().compareTo("") == 0) {
            OpenModelicaDirector.getOMCLogger().getInfo(
                    modelName.getExpression() + " is executed successfully.");
            if (_debugging) {
                _debug("Simulation of " + modelName + " is done.\n"
                        + "The result file is located in " + OMCProxy.workDir);
            }
        } else {
            OpenModelicaDirector.getOMCLogger().getInfo(
                    fileNamePrefix.getExpression()
                            + " is executed successfully.");
            if (_debugging) {
                _debug("Simulation of " + fileNamePrefix + " is done.\n"
                        + "The result file is located in " + OMCProxy.workDir);
            }
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // FIXME: is this used?  If not, then remove it.  If it is, document it.
    private Variable _iteration;

    // FIXME: I would say "The return result from invoking..."

    /** Returning result of invoking sendExpression("command") to
     *  OpenModelica Compiler(OMC).
     */
    private CompilerResult _result;
}
