/* An actor runs the Modelica model.

 Below is the copyright agreement for the Ptolemy II system.

 Copyright (c) 2012-2014 The Regents of the University of California
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

import java.io.IOException;
import java.net.UnknownHostException;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.openmodelica.lib.omc.ConnectException;
import ptolemy.domains.openmodelica.lib.omc.OMCCommand;
import ptolemy.domains.openmodelica.lib.omc.OMCLogger;
import ptolemy.domains.openmodelica.lib.omc.OMIThread;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
    <p>One actor provided in the Vergil, <i>MoreLibraries</i> Under <i>OpenModelica</i>.
    It is called <i>OpenModelica</i>; To view or edit its Modelica script, look inside the actor. </p>
    <p>OpenModelica actor starts the OpenModelica Compiler(OMC) server listening on the CORBA interface
    in initialize(). In fire(), upon modifying the value of variable(s) and parameter(s) by input port or actor's
    parameter, the Modelica model is built in <i>non-interactive</i> or <i>interactive</i> mode.
    <p>Upon building the model in an interactive mode, client and servers are created, IP and ports of the servers
    are set and streams for transferring information between client and servers are set up. Afterwards,
    the simulation result is sent from the Transfer Server to the Ptolemy II. The simulation result
    is displayed step by step according to the parameters of the OpenModelica actor.
    The formula for calculating the step time is step time = (stop time - start time) / number of intervals.
    There is one issue, the simulation does not stop automatically at the stop time that is selected as one of the
    OpenModelica actors' parameters. So during fetching the result back from Transfer server, stop time should be
    checked, this checking is occurred in OMIThread. The simulation result is sent in the string format to
    the output port of the OpenModelica actor to be displayed by Display actor.</p>
    <p>In case of building the model in a non-interactive mode, it's not possible to have the result step by step, the
    whole simulation result is displayed according to the start and stop time of simulation that are set as
    OpenModelica actors' parameters.</p>
    <p>In the final phase, wrapup(), the OMC server is stopped.</p>

   @author Mana Mirzaei
   @version $Id$
   @since Ptolemy II 10.0
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

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);

        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(true);

        fileName = new FileParameter(this, "fileName");
        fileName.setDisplayName("File name");

        dependencies = new FileParameter(this, "dependencies");
        dependencies.setDisplayName("Dependency(ies)");

        processingMode = new StringParameter(this, "processingMode");
        processingMode.setDisplayName("Select the processing mode ");
        processingMode.setExpression("non-interactive");
        processingMode.addChoice("non-interactive");
        processingMode.addChoice("interactive");

        subModel = new StringParameter(this, "subModel");
        subModel.setDisplayName("Model name");

        baseModel = new StringParameter(this, "baseModel");
        baseModel.setDisplayName("Inherits from ");

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

        outputFormat = new StringParameter(this, "outputFormat");
        outputFormat.setDisplayName("Output format");
        outputFormat.setExpression("csv");
        outputFormat.addChoice("csv");
        outputFormat.addChoice("plt");

        parameter = new StringParameter(this, "parameter");
        parameter
                .setDisplayName("Initialized model parameter(s), seperate by '#'");

        initialValue = new StringParameter(this, "initialValue");
        initialValue.setDisplayName("Initial value(s), seperate by ','");

        variableFilter = new StringParameter(this, "variableFilter");
        variableFilter
                .setDisplayName("Filter for displaying simulation result, seperate by '#'");
    }

    ///////////////////////////////////////////////////////////////////
    ////                    public ports and parameters           ////

    /** The base-model that should be built.
     *  The default value of this parameter is null.
     */
    public StringParameter baseModel;

    /** The file that the base-model should be loaded from.
     *  The default value of this parameter is null.
     */
    public FileParameter dependencies;

    /** The file that the (sub-)model should be loaded from.
     *  The default value is "dcmotor.mo".
     */
    public FileParameter fileName;

    /** New value for changing the current value of variable(s) and parameter(s) prior to running the model.
     *  The default value of this parameter is null.
     */
    public StringParameter initialValue;

    /** Input port of the OpenModelica actor, that gets input from Ramp. */
    public TypedIOPort input;

    /** Number of intervals in the result file.
     *  The default value of this parameter is the integer 500.
     */
    public Parameter numberOfIntervals;

    /** Output port sends the simulation result from Ptolemy II
     *  to other actors. */
    public TypedIOPort output;

    /** Format of the generated result file.
     *  The default value of this parameter is string "csv".
     */
    public StringParameter outputFormat;

    /** Parameter(s) and variable(s) of the Modelica model.
     *  The default value of this parameter is null.
     */
    public StringParameter parameter;

    /** Type of processing for running the executable result file of building the Modelica model.
     *  The default value of this parameter is "non-interactive".
     */
    public StringParameter processingMode;

    /** The start time of the simulation.
     *  The default value of this parameter is double 0.0.
     */
    public Parameter simulationStartTime;

    /** The stop time of the simulation.
     *  The default value of this parameter is double 0.1.
     */
    public Parameter simulationStopTime;

    /** The (sub-)model that should be built.
     *  The default value is "dcmotor".
     */
    public StringParameter subModel;

    /** Filter for displaying result of simulation.
     *  The default value of this parameter is null.
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
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        OpenModelica newObject = (OpenModelica) super.clone(workspace);
        try {
            newObject._omcLogger = OMCLogger.getInstance();
            newObject._omcCommand = OMCCommand.getInstance();
        } catch (Throwable throwable) {
            throw new CloneNotSupportedException("Could not clone "
                    + getFullName() + ": " + throwable);
        }
        return newObject;
    }

    /** Invoke the fire() of the super class. Then, Modelica library and model(s) are loaded.
     *  Upon modifying the value of variable(s) and parameter(s) by input port or actors' parameters,
     *  the Modelica model is built in <i>non-interactive</i> or <i>interactive</i> mode.
     *  <p>After building the model in an interactive mode, the simulation result
     *  is calculated step by step according to the parameters of the OpenModelica actor.
     *  The result is sent in the string format to the output port of the OpenModelica actor to be
     *  displayed by Display actor.</p>
     *  @exception IllegalActionException If the evaluation of the expression
     *  triggers it, or the evaluation yields a null result, or the evaluation
     *  yields an incompatible type, or if there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        // Load Modelica library and model(s).
        try {
            _omcCommand.loadModelicaFile(fileName.getExpression(),
                    subModel.getExpression());
            // If the model is inherited from a base model,
            // that base model should be loaded in advance to the derived model.
            // Otherwise, the derived one could not be built.
            if (!(dependencies.getExpression().isEmpty() && baseModel
                    .getExpression().isEmpty())) {
                _omcCommand.loadModelicaFile(dependencies.getExpression(),
                        baseModel.getExpression());
            }
        } catch (ConnectException e) {
            throw new IllegalActionException(
                    "Unable to load Modelica file/library!" + e.getMessage());
        }

        // There is a value to be passed to the OpenModelica actor's port.
        if (input.getWidth() > 0) {
            // Get the token from input port of OpenModelica actor.
            IntToken inputPort = (IntToken) input.get(0);
            try {
                // Modify components of the Modelica model prior to running the model.
                if (!(parameter.getExpression().isEmpty() && initialValue
                        .getExpression().isEmpty())) {
                    if (!(baseModel.getExpression().isEmpty())) {
                        _omcCommand.modifyComponents(inputPort.toString(),
                                baseModel.getExpression(),
                                parameter.getExpression());
                    } else {
                        _omcCommand.modifyComponents(inputPort.toString(),
                                subModel.getExpression(),
                                parameter.getExpression());
                    }
                } else {
                    _omcLogger
                            .getInfo("There is no component to modify prior to running the model!");
                }
            } catch (ConnectException e) {
                throw new IllegalActionException(
                        "Unable to modify components' values!" + e.getMessage());
            }
            // There is no value to be passed to the OpenModelica actor's port and the new value is set by
            // actors' parameters.
        } else if (!(input.getWidth() > 0)) {
            if (!(parameter.getExpression().isEmpty() && initialValue
                    .getExpression().isEmpty())) {
                try {
                    if (baseModel.getExpression().isEmpty()) {
                        _omcCommand.modifyComponents(
                                initialValue.getExpression(),
                                subModel.getExpression(),
                                parameter.getExpression());
                    } else {
                        _omcCommand.modifyComponents(
                                initialValue.getExpression(),
                                baseModel.getExpression(),
                                parameter.getExpression());
                    }
                } catch (ConnectException e) {
                    throw new IllegalActionException(
                            "Unable to modify components' values of "
                                    + baseModel.getExpression() + " !"
                                    + e.getMessage());
                }
            } else {
                _omcLogger
                        .getInfo("There is no components to modify prior to running the model!");
            }
        }

        // Build the Modelica model and run the executable result file.
        // Plot the result file of the simulation that is generated in plt format.
        try {
            if (!(dependencies.getExpression().isEmpty() && baseModel
                    .getExpression().isEmpty())) {
                _omcCommand.runModel(dependencies.getExpression(),
                        baseModel.getExpression(),
                        simulationStartTime.getExpression(),
                        simulationStopTime.getExpression(),
                        Integer.parseInt(numberOfIntervals.getExpression()),
                        outputFormat.getExpression(),
                        processingMode.getExpression());

                if (outputFormat.getExpression().equalsIgnoreCase("plt")
                        && processingMode.getExpression().equalsIgnoreCase(
                                "non-interactive")) {
                    _omcCommand.plotPltFile(baseModel.getExpression());
                }
            } else {
                _omcCommand.runModel(fileName.getExpression(),
                        subModel.getExpression(),
                        simulationStartTime.getExpression(),
                        simulationStopTime.getExpression(),
                        Integer.parseInt(numberOfIntervals.getExpression()),
                        outputFormat.getExpression(),
                        processingMode.getExpression());

                if (outputFormat.getExpression().equalsIgnoreCase("plt")
                        && processingMode.getExpression().equalsIgnoreCase(
                                "non-interactive")) {
                    _omcCommand.plotPltFile(subModel.getExpression());
                }
            }

            // In case of building the model in an interactive mode, client and servers are created,
            // IP and ports of the servers are set and streams for transferring information between
            // client and servers are set up all in the constructor of the thread.
            // Through starting the thread, the simulation result is sent from the server to the
            // Ptolemy II in the string format.
            if (processingMode.getExpression().equalsIgnoreCase("interactive")) {
                _omiThread = new OMIThread(variableFilter.getExpression(),
                        simulationStopTime.getExpression(), output);
                // FIXME: This method explicitly invokes run() on an object.  In general, classes implement the Runnable
                // interface because they are going to have their
                // run() method invoked in a new thread, in which case Thread.start() is the right method to call.
                _omiThread.run();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new IllegalActionException("Host Exception: "
                    + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalActionException("Socket Connection Error: "
                    + e.getMessage());
        } catch (ConnectException e) {
            e.printStackTrace();
            throw new IllegalActionException("ServerError: " + e.getMessage());
        }
    }

    /** Invoke the initialize() of the super class. It Starts OpenModelica Compiler(OMC)
     *  as a server listening on the CORBA interface by setting +d=interactiveCorba flag.
     *  @exception IllegalActionException If OMC server is unable to start.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        try {
            // Create a unique instance of OMCProxy.
            _omcCommand = OMCCommand.getInstance();
            _omcCommand.initializeServer();

            // Create a unique instance of OMCLogger.
            _omcLogger = OMCLogger.getInstance();
            String loggerInfo = "OpenModelica Server started!";
            _omcLogger.getInfo(loggerInfo);
        } catch (ConnectException e) {
            throw new IllegalActionException(
                    "ServerError : OMC is unable to start!" + e.getMessage());
        }
    }

    /** Invoke the wrapup() of the super class. Then, quit OpenModelica environment.
     *  @exception IllegalActionException If OMC server is unable to stop.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        try {
            // Generating WebStart calls wrapup() after preinitialize(),
            // so the model might not have been initialized.
            if (_omcCommand != null) {
                _omcCommand.stopServer();
                String loggerInfo = "OpenModelica Server stopped!";
                _omcLogger.getInfo(loggerInfo);
            }
        } catch (ConnectException e) {
            // FIXME org.omg.CORBA.COMM_FAILURE:
            // vmcid: SUN  minor code: 211  completed: No : Unable to send quit()
            //new IllegalActionException("ServerError : OMC is unable to stop!"
            //      + e.getMessage()).printStackTrace();
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // OMCCommand Object for accessing a unique source of instance.
    private OMCCommand _omcCommand = null;

    // OMCLogger Object for accessing a unique source of instance.
    private OMCLogger _omcLogger = null;

    // OMIThread Object.
    private OMIThread _omiThread = null;
}
