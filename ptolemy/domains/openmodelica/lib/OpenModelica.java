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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.openmodelica.lib.exception.ConnectException;
import ptolemy.domains.openmodelica.lib.omc.CompilerResult;
import ptolemy.domains.openmodelica.lib.omc.OMCProxy;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.plot.compat.PxgraphApplication;

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

        modelicaScript = new StringParameter(this, "modelicaScript");
        modelicaScript.setDisplayName("Write OpenModelica Command");
        modelicaScript.setExpression("loadModel(Modelica)");

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

    /** Format of the result file.  
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

        try {
            if (_debugging) {
                _debug("OpenModelica Actor Called fire().");
            }

            //  Create a unique instance of OMCLogger and OMCProxy.
            _omcProxy = OMCProxy.getInstance();

            //  Simulate the modelica model with the selected parameters.
            _omcProxy.simulateModel(fileName.getExpression(),
                    modelicaScript.getExpression(), modelName.getExpression(),
                    fileNamePrefix.getExpression(),
                    simulationStartTime.getExpression(),
                    simulationStopTime.getExpression(),
                    Integer.parseInt(numberOfIntervals.getExpression()),
                    tolerance.getExpression(), method.getExpression(),
                    outputFormat.getExpression(),
                    variableFilter.getExpression(), cflags.getExpression(),
                    simflags.getExpression());

            // Plot plt file.
            _plotPltFile(fileNamePrefix.getExpression());
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Unable to simulate the model!");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /**Plot the plt file by calling PxgraphApplication.main(dcmotor_res.plt).
     * @throws ConnectException  If commands couldn't
       be sent to the OMC.*/
    private void _plotPltFile(String fileNamePrefix) throws ConnectException {
        
        // Array for saving the file path.  
        String[] _pltPath = new String[1];

            //Send cd() command to the OpenModelica Compiler(OMC) and fetch working directory of OMC as a result.
            CompilerResult omcInvokingResult = _omcProxy.sendCommand("cd()");
            _openModelicaWorkingDirectory = omcInvokingResult.getFirstResult();
            _openModelicaWorkingDirectory = _openModelicaWorkingDirectory
                    .replace('"', ' ').trim();

            // Save file path in the string array for invoking main() of PxgraphApplication.

            if (fileNamePrefix.compareTo("") == 0)
                _pltPath[0] = _openModelicaWorkingDirectory + "/"
                        + modelName.getExpression() + "_res.plt";
            else
                _pltPath[0] = _openModelicaWorkingDirectory + "/"
                        + fileNamePrefix + "_res.plt";

            PxgraphApplication.main(_pltPath);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // OMCProxy Object for accessing a unique source of instance.
    private OMCProxy _omcProxy;

    // The return result from invoking sendExpression("cd()") to OpenModelica Compiler(OMC).
    // The return result is the working directory of OMC.
    private String _openModelicaWorkingDirectory = null;

}
