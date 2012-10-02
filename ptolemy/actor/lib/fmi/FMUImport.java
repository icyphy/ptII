/* Instantiate a Functional Mock-up Unit (FMU).

   Copyright (c) 2011-2012 The Regents of the University of California.
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
package ptolemy.actor.lib.fmi;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.ptolemy.fmi.FMICallbackFunctions;
import org.ptolemy.fmi.FMILibrary;
import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMIScalarVariable;
import org.ptolemy.fmi.FMIScalarVariable.Alias;
import org.ptolemy.fmi.FMIScalarVariable.Causality;
import org.ptolemy.fmi.FMUFile;
import org.ptolemy.fmi.FMULibrary;
import org.ptolemy.fmi.type.FMIBooleanType;
import org.ptolemy.fmi.type.FMIIntegerType;
import org.ptolemy.fmi.type.FMIRealType;
import org.ptolemy.fmi.type.FMIStringType;
import org.ptolemy.fmi.type.FMIType;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.continuous.kernel.ContinuousDirector;
import ptolemy.domains.continuous.kernel.ContinuousStepSizeController;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

import com.sun.jna.Function;
import com.sun.jna.Pointer;

///////////////////////////////////////////////////////////////////
//// FMUImport

/**
 * Invoke a Functional Mock-up Interface (FMI) 1.0 Co-Simulation
 * Functional Mock-up Unit (FMU).
 *
 * <p>Read in a <code>.fmu</code> file named by the
 * <i>fmuFile</i> parameter.  The <code>.fmu</code> file is a zipped
 * file that contains a file named <code>modelDescription.xml</code>
 * that describes the ports and parameters that are created.
 * At run time, method calls are made to C functions that are
 * included in shared libraries included in the <code>.fmu</code>
 * file.</p>
 *
 * <p>To use this actor from within Vergil, use File -&gt; Import -&gt; Import
 * FMU, which will prompt for a .fmu file. This actor is <b>not</b>
 * available from the actor pane via drag and drop. The problem is
 * that dragging and dropping this actor ends up trying to read
 * fmuImport.fmu, which does not exist.  If we added such a file, then
 * dragging and dropping the actor would create an arbitrary actor
 * with arbitrary ports.</p>
 *
 * <p>FMI documentation may be found at
 * <a href="http://www.modelisar.com/fmi.html">http://www.modelisar.com/fmi.html</a>.
 * </p>
 *
 * @author Christopher Brooks, Michael Wetter, Edward A. Lee,
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMUImport extends TypedAtomicActor implements ContinuousStepSizeController {
    // FIXME: For FMI Co-simulation, we want to extend TypedAtomicActor.
    // For model exchange, we want to extend TypedCompositeActor.

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FMUImport(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        fmuFile = new FileParameter(this, "fmuFile");
        fmuFile.setExpression("fmuImport.fmu");
    }

    /** The Functional Mock-up Unit (FMU) file.  The FMU file is a zip
     *  file that contains a file named "modelDescription.xml" and any
     *  necessary shared libraries.  The file is read when this actor
     *  is instantiated or when the file name changes.  The initial
     *  default value is "fmuImport.fmu".
     */
    public FileParameter fmuFile;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>fmuFile</i>, then unzip
     *  the file and load in the .xml file, creating and deleting parameters
     *  as necessary.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *  is <i>fmuFile</i> and the file cannot be opened or there
     *  is a problem creating or destroying the parameters
     *  listed in thile.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fmuFile) {
            try {
                _updateParameters();
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(this, e, "Name duplication");
            }
        }
        super.attributeChanged(attribute);
    }

    /** Set the dependency between all output ports and all input
     *  ports of this actor. By default, each
     *  output port is assumed to have a dependency on all input
     *  ports. If the FMU explicitly declares input dependencies
     *  for a particular output, then then it only depends on those
     *  inputs that it declares.
     *  @exception IllegalActionException Not thrown in this base
     *  class, derived classes should throw this exception if the
     *  delay dependency cannot be computed.
     *  @see #getCausalityInterface()
     *  @see #_declareDelayDependency(IOPort, IOPort, double)
     */
    public void declareDelayDependency() throws IllegalActionException {
        // Iterate through the outputs, and for any output that declares
        // dependencies, indicate a delay dependency for any inputs that
        // it does not mention.
        for (Output output : _getOutputs()) {
            if (output.dependencies == null) {
                // There are no dependencies declared for this output,
                // so the output depends on all inputs.
                continue;
            }
            List<TypedIOPort> inputs = inputPortList();
            for (TypedIOPort input : inputs) {
                if (!output.dependencies.contains(input)) {
                    _declareDelayDependency(input, output.port, 0.0);
                }
            }
        }
    }

    /** Read data from output ports, set the input ports and invoke
     * fmiDoStep() of the slave fmu.
     *
     * <p>Note that we get the outputs <b>before</b> invoking
     * fmiDoStep() of the slave fmu so that we can get the data for
     * time 0.  This is done so that FMUs can share initialization
     * data if necessary.  For details, see the Section 3.4, Pseudo
     * Code Example in the FMI-1.0 Co-simulation Specification at
     * <a href="http://www.modelisar.com/specifications/FMI_for_CoSimulation_v1.0.pdf">http://www.modelisar.com/specifications/FMI_for_CoSimulation_v1.0.pdf</a>.
     * For an explanation, see figure 4 of
     * <br>
     * Michael Wetter,
     * "<a href="http://dx.doi.org/10.1080/19401493.2010.518631">Co-simulation of building energy and control systems with the Building Controls Virtual Test Bed</a>,"
     * Journal of Building Performance Simulation, Volume 4, Issue 3, 2011.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_debugging) {
            _debug("FMUImport.fire()");
        }

        String modelIdentifier = _fmiModelDescription.modelIdentifier;

        // Ptolemy parameters are read in initialize() because the fmi
        // version of the parameters must be written before
        // fmiInitializeSlave() is called.

        boolean foundUnknownInputOrOutput = false;

        ////////////////
        // Iterate through the scalarVariables and set all the inputs
        // that are known.
        // FIXME: Here, we are iterating over a potentially very large number
        // of scalar variables to find a few that are relevant (the input
        // ports with some connection). It would be better to construct _once_
        // a list of such input ports, and then use that list here.
        for (FMIScalarVariable scalarVariable : _fmiModelDescription.modelVariables) {
            // If this variable has an alias, then we operate
            // only on the real version, not the alias.
            // In bouncingBall.fmu, g has an alias, so it is skipped.
            if (scalarVariable.alias != null
                    && scalarVariable.alias != Alias.noAlias) {
                continue;
            }
            // NOTE: Page 27 of the FMI-1.0 CS spec says that for
            // variability==parameter and causality==input, we can
            // only call fmiSet* between fmiInstantiateSlave() and
            // fmiInitializeSlave(). Same for inputs that have
            // variability==constant.
            if (scalarVariable.variability != FMIScalarVariable.Variability.parameter
                    && scalarVariable.variability != FMIScalarVariable.Variability.constant
                    && scalarVariable.causality == Causality.input) {
                TypedIOPort port = (TypedIOPort) getPort(scalarVariable.name);
                if (port != null) {
                    if (port.isKnown(0)) {
                        if (port.hasToken(0)) {
                            Token token = port.get(0);
                            _setScalarVariable(scalarVariable, token);
                            if (_debugging) {
                                _debug("FMUImport.fire(): set input variable "
                                        + scalarVariable.name + " to " + token);
                            }
                        } else {
                            // Port is known to be absent, but FMI
                            // does not support absent values.
                            throw new IllegalActionException(this,
                                    "Input "
                                    + scalarVariable.name
                                    + " has value 'absent', but FMI does not "
                                    + "support a notion of absent inputs.");
                        }
                    } else {
                        // Port value is not known.
                        foundUnknownInputOrOutput = true;
                    }
                } else {
                    throw new IllegalActionException(this,
                            "Expected an input port named "
                            + scalarVariable.name
                            + ", but there is no such port.");
                }
            }
        }

        ////////////////
        // Iterate through the outputs.
        // See the method comment for why we do this before calling fmiDoStep()
        for (Output output : _getOutputs()) {

            TypedIOPort port = output.port;

            // If the output port has already been set, then
            // skip it.
            // FIXME: This will not work with SDF because the port
            // will likely be known but not have a token in it.
            // We have to also check to make sure that the destination
            // ports have a token.
            // Even better, we should keep track locally of whether
            // we've produced an output in this iteration.
            if (port.isKnown(0)) {
                continue;
            }

            // Next, we need to check whether the input ports that
            // the output depends on are known. By default, an
            // output depends on _all_ inputs, but if there is
            // a DirectDependency element in the XML file, then
            // the output may depend on only _some_ inputs.
            boolean foundUnknownInputOnWhichOutputDepends = false;
            if (output.dependencies != null) {
                // The output port has some declared dependencies.
                // Check only those ports.
                for (TypedIOPort inputPort : output.dependencies) {
                    if (!inputPort.isKnown(0)) {
                        // Skip this output port. It depends on
                        // unknown inputs.
                        if (_debugging) {
                            _debug("FMUImport.fire(): "
                                    + "FMU declares that output port "
                                    + port.getName()
                                    + " depends directly on input port "
                                    + inputPort.getName()
                                    + ", but the input is not yet known.");
                        }
                        foundUnknownInputOrOutput = true;
                        foundUnknownInputOnWhichOutputDepends = true;
                        break;
                    }
                }
            } else {
                // No directDependency is given.
                // This means that the output depends on all
                // inputs, so all inputs must be known.
                List<TypedIOPort> inputPorts = inputPortList();
                for (TypedIOPort inputPort : inputPorts) {
                    if (inputPort.getWidth() < 0 || !inputPort.isKnown(0)) {
                        // Input port value is not known.
                        foundUnknownInputOrOutput = true;
                        foundUnknownInputOnWhichOutputDepends = true;
                        break;
                    }
                }
            }
            if (!foundUnknownInputOnWhichOutputDepends) {
                // Ok to get the output. All the inputs on which
                // it depends are known.
                Token token = null;
                FMIScalarVariable scalarVariable = output.scalarVariable;

                if (scalarVariable.type instanceof FMIBooleanType) {
                    boolean result = scalarVariable.getBoolean(_fmiComponent);
                    token = new BooleanToken(result);
                } else if (scalarVariable.type instanceof FMIIntegerType) {
                    // FIXME: handle Enumerations?
                    int result = scalarVariable.getInt(_fmiComponent);
                    token = new IntToken(result);
                } else if (scalarVariable.type instanceof FMIRealType) {
                    double result = scalarVariable.getDouble(_fmiComponent);
                    token = new DoubleToken(result);
                } else if (scalarVariable.type instanceof FMIStringType) {
                    String result = scalarVariable.getString(_fmiComponent);
                    token = new StringToken(result);
                } else {
                    throw new IllegalActionException("Type "
                            + scalarVariable.type + " not supported.");
                }

                if (_debugging) {
                    _debug("FMUImport.fire(): Output " + scalarVariable.name
                            + " sends value "
                            + token);
                }
                port.send(0, token);
            }
        }

        ////////////////
        // Call fmiDoStep() with the current data.
        // But only call it once all inputs are known and all outputs
        // for the current time step have been produced.
        // FIXME: Some FMUs may not allow fmuDoStep to be called
        // again if it has previously succeeded.
        // Check the Capabilities part of the XML file.
        // Some FMUs may not be compatible with some directors.
        if (!foundUnknownInputOrOutput) {
            // NOTE: FMI-1.0 uses doubles for time.
            double time = getDirector().getModelTime().getDoubleValue();

            // FIXME: depending on ContinuousDirector here.
            double stepSize = ((ContinuousDirector) getDirector()).getCurrentStepSize();

            if (_debugging) {
                _debug("FMIImport.fire(): about to call " + modelIdentifier
                        + "_fmiDoStep(Component, /* time */ " + time
                        + ", /* stepSize */" + stepSize + ", 1)");

            }

            int fmiFlag = ((Integer) _fmiDoStep.invokeInt(new Object[] {
                    _fmiComponent, time, stepSize, (byte) 1 })).intValue();

            if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                throw new IllegalActionException(this, "Could not simulate, "
                        + modelIdentifier + "_fmiDoStep(Component, /* time */ "
                        + time + ", /* stepSize */" + stepSize + ", 1) returned "
                        + fmiFlag);
            }

            if (_debugging) {
                _debug("FMUImport done calling " + modelIdentifier + "_fmiDoStep()");
            }
        }
    }

   /** Initialize the slave FMU.
    *  @exception IllegalActionException If the slave FMU cannot be
    *  initialized.
    */
   public void initialize() throws IllegalActionException {
       super.initialize();
       if (_debugging) {
           _debug("FMIImport.initialize() START");
       }

       // Loop through the scalar variables and find a scalar
       // variable that has variability == "parameter" and is not an
       // input or output.  We can't do this in attributeChanged()
       // because setting a scalar variable requires that
       // _fmiComponent be non-null, which happens in
       // preinitialize();
       for (FMIScalarVariable scalar : _fmiModelDescription.modelVariables) {
           if (scalar.variability == FMIScalarVariable.Variability.parameter
                   && scalar.causality != Causality.input
                   && scalar.causality != Causality.output) {
               String sanitizedName = StringUtilities.sanitizeName(scalar.name);
               Parameter parameter = (Parameter)getAttribute(sanitizedName, Parameter.class);
               if (parameter != null) {
                   _setScalarVariable(scalar, parameter.getToken());
               }
           }
       }

       String modelIdentifier = _fmiModelDescription.modelIdentifier;

       if (_debugging) {
           _debug("FMUCoSimulation: about to call " + modelIdentifier
                   + "_fmiInitializeSlave");
       }
       Function function = _fmiModelDescription.nativeLibrary
               .getFunction(modelIdentifier + "_fmiInitializeSlave");

       // FIXME: FMI-1.0 uses doubles for times.
       double startTime = getDirector().getModelStartTime().getDoubleValue();
       double stopTime = getDirector().getModelStopTime().getDoubleValue();
       int fmiFlag = ((Integer) function.invoke(Integer.class, new Object[] {
               _fmiComponent, startTime, (byte) 1, stopTime })).intValue();
       if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
           throw new IllegalActionException(this, "Could not simulate, "
                   + modelIdentifier
                   + "_fmiInitializeSlave(Component, /* startTime */ "
                   + startTime + ", 1, /* stopTime */" + stopTime
                   + ") returned " + fmiFlag);
       }
       if (_debugging) {
           _debug("FMIImport.initialize() END");
       }
   }

    /** Import a FMUFile.
     *  @param originator The originator of the change request.
     *  @param fmuFileName The .fmuFile
     *  @param context The context in which the FMU actor is created.
     *  @param x The x-axis value of the actor to be created.
     *  @param y The y-axis value of the actor to be created.
     *  @exception IllegalActionException If there is a problem instantiating the actor.
     *  @exception IOException If there is a problem parsing the fmu file.
     */
    public static void importFMU(Object originator, String fmuFileName,
            NamedObj context, double x, double y)
            throws IllegalActionException, IOException {
        System.out.println("FMUImport.importFMU(): " + fmuFileName);
        // This method is called by the gui to import a fmu file and create the
        // actor.
        // The primary issue is that we need to define the ports early on and
        // handle
        // changes to the ports.

        // FIXME: ignore errors loading shared libraries.
        // This should be made a parameter.
        FMIModelDescription fmiModelDescription = FMUFile
            .parseFMUFile(fmuFileName, true);

        // FIXME: Use URLs, not files so that we can work from JarZip files.

        // If a location is given as a URL, construct MoML to
        // specify a "source".
        String source = "";
        // FIXME: not sure about this.
        if (fmuFileName.startsWith("http://")) {
            source = " source=\"" + fmuFileName.trim() + "\"";
        }

        String rootName = new File(fmuFileName).getName();
        int index = rootName.lastIndexOf('.');
        if (index != -1) {
            rootName = rootName.substring(0, index);
        }

        // Instantiate ports and parameters.
        int maximumNumberOfPortsToDisplay = 20;
        int modelVariablesLength = fmiModelDescription.modelVariables.size();
        String hide = "  <property name=\"_hide\" class=\"ptolemy.data.expr.SingletonParameter\" value=\"true\"/>\n";
        if (modelVariablesLength > maximumNumberOfPortsToDisplay) {
            MessageHandler.message("Importing \"" + fmuFileName
                    + "\" resulted in an actor with " + modelVariablesLength
                    + "ports.  To show ports, right click and "
                    + "select Customize -> Ports.");
        }

        int portCount = 0;
        StringBuffer parameterMoML = new StringBuffer();
        StringBuffer portMoML = new StringBuffer();
        for (FMIScalarVariable scalar : fmiModelDescription.modelVariables) {
            if (scalar.variability == FMIScalarVariable.Variability.parameter) {
                // Parameters
                // Parameter parameter = new Parameter(this, scalar.name);
                // parameter.setExpression(Double.toString(((FMIRealType)scalar.type).start));
                // // Prevent exporting this to MoML unless it has
                // // been overridden.
                // parameter.setDerivedLevel(1);

                // FIXME: Need to sanitize the value.
                parameterMoML.append("  <property name=\""
                        + StringUtilities.sanitizeName(scalar.name)
                        + "\" class=\"ptolemy.data.expr.Parameter\" value =\""
                        + scalar.type + "\"/>\n");
            } else {
                // Ports

                // // FIXME: All output ports?
                // TypedIOPort port = new TypedIOPort(this, scalar.name, false,
                // true);
                // port.setDerivedLevel(1);
                // // FIXME: set the type
                // port.setTypeEquals(BaseType.DOUBLE);

                String causality = "";
                switch (scalar.causality) {
                case input:
                    portCount++;
                    causality = "input";
                    break;
                case none:
                    // FIXME: Not sure what to do with causality == none.
                    continue;
                case output:
                    portCount++;
                    // Drop through to internal
                case internal:
                    // Internal ports get hidden.
                    causality = "output";
                    break;
                }

                portMoML.append("  <port name=\""
                        + StringUtilities.sanitizeName(scalar.name)
                        + "\" class=\"ptolemy.actor.TypedIOPort\">\n"
                        + "    <property name=\"" + causality
                        + "\"/>\n"
                        + "    <property name=\"_type\" "
                        + "class=\"ptolemy.actor.TypeAttribute\" value=\""
                        + _fmiType2PtolemyType(scalar.type)
                        + "\"/>\n"
                        // Hide the port if we have lots of ports or it is
                        // internal.
                        + (portCount > maximumNumberOfPortsToDisplay
                                || scalar.causality == Causality.internal ? hide
                                : "") + "  </port>\n");
            }
        }

        // FIXME: Get Undo/Redo working.

        // Use the "auto" namespace group so that name collisions
        // are automatically avoided by appending a suffix to the name.
        String moml = "<group name=\"auto\">\n" + " <entity name=\"" + rootName
                + "\" class=\"ptolemy.actor.lib.fmi.FMUImport\"" + source
                + ">\n" + "  <property name=\"_location\" "
                + "class=\"ptolemy.kernel.util.Location\" value=\"" + x + ", "
                + y + "\">\n" + "  </property>\n"
                + "  <property name=\"fmuFile\""
                + "class=\"ptolemy.data.expr.FileParameter\"" + "value=\""
                + fmuFileName + "\">\n" + "  </property>\n" + parameterMoML
                + portMoML + " </entity>\n</group>\n";
        MoMLChangeRequest request = new MoMLChangeRequest(originator, context,
                moml);
        context.requestChange(request);
    }

    /** Implementations of this method should return
     *  true if the current integration step size
     *  is sufficiently small for this actor to give accurate
     *  results.
     *  @return True if the current step is accurate.
     */
    public boolean isStepSizeAccurate() {
        // FIXME: Do something smarter depending on what fmiDoStep() returns;
        return true;
    }

    /** Instantiate the slave FMU component.
     *  @exception IllegalActionException if it cannot be instantiated.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        if (_debugging) {
            _debug("FMUImport.preinitialize()");
        }

        // The modelName may have spaces in it.
        String modelIdentifier = _fmiModelDescription.modelIdentifier;

        String fmuLocation = null;
        try {
            // The URL of the fmu file.
            String fmuFileName = fmuFile.asFile().getCanonicalPath();

            fmuLocation = new File(fmuFileName).toURI().toURL().toString();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to get the value of \"" + fmuFile + "\"");
        }
        // The tool to use if we have tool coupling.
        String mimeType = "application/x-fmu-sharedlibrary";
        // Timeout in ms., 0 means wait forever.
        double timeout = 1000;
        // There is no simulator UI.
        byte visible = 0;
        // Run the simulator without user interaction.
        byte interactive = 0;
        // Callbacks
        FMICallbackFunctions.ByValue callbacks = new FMICallbackFunctions.ByValue(
                new FMULibrary.FMULogger(), new FMULibrary.FMUAllocateMemory(),
                new FMULibrary.FMUFreeMemory(),
                new FMULibrary.FMUStepFinished());

        // FIXME: We should send logging messages to the debug listener.
        byte loggingOn = _debugging ? (byte) 1 : (byte) 0;
        loggingOn = 1;
        if (_debugging) {
            _debug("FMUCoSimulation: about to call " + modelIdentifier
                    + "_fmiInstantiateSlave");
        }

        _fmiComponent = (Pointer) _fmiInstantiateSlave.invoke(Pointer.class,
                new Object[] { modelIdentifier, _fmiModelDescription.guid,
                        fmuLocation, mimeType, timeout, visible, interactive,
                        callbacks, loggingOn });

        if (_fmiComponent.equals(Pointer.NULL)) {
            throw new RuntimeException(
                    "Could not instantiate Functional Mock-up Unit (FMU).");
        }
    }

    /** Implementations of this method should return
     *  the suggested refined step size for restarting the current integration.
     *  If any actor returns false when isStepSizeAccurate() is called,
     *  then this method will be called on all actors that implement this
     *  interface. The minimum of their returned value will be the new step size.
     *  If the actor does not need a smaller step size, then
     *  this method should return the current step size.
     *  @return The suggested refined step size.
     *  @exception IllegalActionException If the step size cannot be further refined.
     */
    public double refinedStepSize() throws IllegalActionException {
        // FIXME: Do something smarter.
        Director director = getDirector();
        if (director instanceof ContinuousDirector) {
            return ((ContinuousDirector)director).getCurrentStepSize();
        }
        return Double.MAX_VALUE;
    }

    /** Implementations of this method should return
     *  the suggested next step size. If the current integration
     *  step is accurate, each actor will be asked for its suggestion
     *  for the next step size. If the actor that implements this interface
     *  does not care what the next step size is, it should
     *  return java.lang.Double.MAX_VALUE.
     *  @return The suggested next step size.
     *  @exception IllegalActionException If an actor suggests an illegal step size.
     */
    public double suggestedStepSize() throws IllegalActionException {
        return java.lang.Double.MAX_VALUE;
    }

    /** Terminate and free the slave fmu.
     *  @exception IllegalActionException If the slave fmu cannot be
     *  terminated or freed.
     */
    public void wrapup() throws IllegalActionException {
        String modelIdentifier = _fmiModelDescription.modelIdentifier;
        Function fmiTerminateSlave = _fmiModelDescription.nativeLibrary
                .getFunction(modelIdentifier + "_fmiTerminateSlave");
        int fmiFlag = ((Integer) fmiTerminateSlave.invokeInt(new Object[] {
                _fmiComponent})).intValue();
        if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
            throw new IllegalActionException(this,
                    "Could not terminate slave: " + fmiFlag);
        }

        Function fmiFreeSlaveInstance = _fmiModelDescription.nativeLibrary
                .getFunction(modelIdentifier + "_fmiFreeSlaveInstance");
        fmiFlag = ((Integer) fmiFreeSlaveInstance.invokeInt(new Object[] {
                _fmiComponent})).intValue();
        if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
            if (_debugging) {
                _debug("Could not free slave instance: "
                        + fmiFlag);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Given a FMIType object, return a string suitable for setting
     *  the TypeAttribute.
     *  @param type The FMIType object.
     *  @return a string suitable for ptolemy.actor.TypeAttribute.
     *  @exception IllegalActionException If the type is not supported.
     */
    private static String _fmiType2PtolemyType(FMIType type)
            throws IllegalActionException {
        if (type instanceof FMIBooleanType) {
            return "boolean";
        } else if (type instanceof FMIIntegerType) {
            // FIXME: handle Enumerations?
            return "int";
        } else if (type instanceof FMIRealType) {
            return "double";
        } else if (type instanceof FMIStringType) {
            return "string";
        } else {
            throw new IllegalActionException("Type " + type + " not supported.");
        }
    }

    /** Return a collection of scalar variables for which there is
     *  a connected output port, and for each such variable,
     *  a set of input ports on which it declares it depends,
     *  if any.
     *  @return A map, where the keys consist of all the scalar
     *   variables of the FMU corresponding to a connected output
     *   port, and the value of the map is the collection of input
     *   ports that the output declares it depends on (if any).
     *  @exception IllegalActionException If an expected output is not
     *   found, or if the width of the output cannot be determined.
     */
    private List<Output> _getOutputs() throws IllegalActionException {
        if (workspace().getVersion() == _outputsVersion) {
            return _outputs;
        }
        // The _outputs variable is out of date. Reconstruct it.
        _outputs = new LinkedList<Output>();
        for (FMIScalarVariable scalarVariable : _fmiModelDescription.modelVariables) {
            // If this variable has an alias, then we operate
            // only on the real version, not the alias.
            // In bouncingBall.fmu, g has an alias, so it is skipped.
            if (scalarVariable.alias != null
                    && scalarVariable.alias != Alias.noAlias) {
                continue;
            }

            // An "output" may be an internal variable in the FMU.
            // FMI seems to have the odd notion of being able to "observe"
            // an internal variable but not call it an output.
            if (scalarVariable.causality == FMIScalarVariable.Causality.output
                    || scalarVariable.causality == FMIScalarVariable.Causality.internal) {
                TypedIOPort port = (TypedIOPort) getPort(scalarVariable.name);

                if (port == null || port.getWidth() <= 0) {
                    // Either it is not a port or not connected.
                    // Check to see if we should update the parameter.
                    // FIXME: Huh? What parameter? Does it make sense for
                    // the output of an FMU to go to a parameter?
                    /*
                    String sanitizedName = StringUtilities.sanitizeName(scalarVariable.name);
                    Parameter parameter = (Parameter)getAttribute(sanitizedName, Parameter.class);
                    if (parameter != null) {
                        _setParameter(parameter, scalarVariable);
                    } else {
                        throw new IllegalActionException(this,
                                "Expected an output named "
                                + scalarVariable.name
                                + " or a parameter named "
                                + sanitizedName
                                + ", but this actor has neither.");
                    }
                    */
                    continue;
                }
                Output output = new Output();
                output.scalarVariable = scalarVariable;
                output.port = port;

                // Next, we need to find the dependencies that the
                // FMU XML file says the port depends on. By default, an
                // output depends on _all_ inputs, but if there is
                // a DirectDependency element in the XML file, then
                // the output may depend on only _some_ inputs.
                if (scalarVariable.directDependency != null) {
                    // The output has declared direct dependence on some
                    // inputs. Make sure those are known before retrieving
                    // output values.
                    Set<TypedIOPort> dependencies = new HashSet<TypedIOPort>();
                    for (String inputName : scalarVariable.directDependency) {
                        TypedIOPort inputPort = (TypedIOPort) getPort(inputName);
                        if (inputPort == null) {
                            throw new IllegalActionException(this,
                                    "FMU declares that output port "
                                    + port.getName()
                                    + " depends directly on input port "
                                    + inputName
                                    + ", but there is no such input port.");
                        }
                        dependencies.add(inputPort);
                    }
                }
                _outputs.add(output);
            }
        }
        return _outputs;
    }

    /** Set a Ptolemy II Parameter to the value of a FMI
     *  ScalarVariable.
     *  @param parameter The Ptolemy parameter to be set.
     *  @param scalar The FMI scalar variable that contains the value
     *  to be set
     *  @exception IllegalActionException If the scalar is of a type
     *  that is not handled.
     */
    private void _setParameter(Parameter parameter, FMIScalarVariable scalar)
            throws IllegalActionException {
        // FIXME: What about arrays?
        if (scalar.type instanceof FMIBooleanType) {
            parameter.setToken(new BooleanToken(scalar.getBoolean(_fmiComponent)));
        } else if (scalar.type instanceof FMIIntegerType) {
            // FIXME: handle Enumerations?
            parameter.setToken(new IntToken(scalar.getInt(_fmiComponent)));
        } else if (scalar.type instanceof FMIRealType) {
            parameter.setToken(new DoubleToken(scalar.getDouble(_fmiComponent)));
        } else if (scalar.type instanceof FMIStringType) {
            parameter.setToken(new StringToken(scalar.getString(_fmiComponent)));
        } else {
            throw new IllegalActionException("Type "
                    + scalar.type + " not supported.");
        }
    }

    /** Set a FMI scalar variable to the value of a Ptolemy token.
     *  @param scalar the FMI scalar to be set.
     *  @param token the Ptolemy token that contains the value to be set.
     *  @exception IllegalActionException If the scalar is of a type
     *  that is not handled or if the type of the token does not match
     *  the type of the scalar.
     */
    private void _setScalarVariable(FMIScalarVariable scalar, Token token)
        throws IllegalActionException {
        try {
            // FIXME: What about arrays?
            if (scalar.type instanceof FMIBooleanType) {
                scalar.setBoolean(_fmiComponent,
                        ((BooleanToken)token).booleanValue());
            } else if (scalar.type instanceof FMIIntegerType) {
                // FIXME: handle Enumerations?
                scalar.setInt(_fmiComponent,
                        ((IntToken)token).intValue());
            } else if (scalar.type instanceof FMIRealType) {
                scalar.setDouble(_fmiComponent,
                        ((DoubleToken)token).doubleValue());
            } else if (scalar.type instanceof FMIStringType) {
                scalar.setString(_fmiComponent,
                        ((StringToken)token).stringValue());
            } else {
                throw new IllegalActionException("Type "
                        + scalar.type + " not supported.");
            }
        } catch (ClassCastException ex) {
            throw new IllegalActionException(this, ex,
                    "Could not cast a token \"" + token
                    + "\" of type " + token.getType()
                    + " to an FMI scalar variable of type "
                    + scalar.type);
        }
    }

    /** Update the parameters listed in the modelDescription.xml file
     *  contained in the zipped file named by the <i>fmuFile</i>
     *  parameter
     *  @exception IllegalActionException If the file named by the
     *  <i>fmuFile<i> parameter cannot be unzipped or if there
     *  is a problem deleting any pre=existing parameters or
     *  creating new parameters.
     *  @exception NameDuplicationException If a paramater to be created
     *  has the same name as a pre-existing parameter.
     */
    private void _updateParameters() throws IllegalActionException,
            NameDuplicationException {

        if (_debugging) {
            _debug("FMUImport.updateParameters() START");
        }
        // Unzip the fmuFile. We probably need to do this
        // because we will need to load the shared library later.
        String fmuFileName = null;
        try {
            // FIXME: Use URLs, not files so that we can work from JarZip files.

            // Only read the file if the name has changed from the last time we
            // read the file or if the modification time has changed.
            fmuFileName = fmuFile.asFile().getCanonicalPath();
            if (fmuFileName.equals(_fmuFileName)) {
                return;
            }
            _fmuFileName = fmuFileName;
            long modificationTime = new File(fmuFileName).lastModified();
            if (_fmuFileModificationTime == modificationTime) {
                return;
            }
            _fmuFileModificationTime = modificationTime;

            // Calling parseFMUFile also loads the share library.
            // FIXME: ignore errors loading shared libraries.
            // This should be made a parameter.
            _fmiModelDescription = FMUFile.parseFMUFile(fmuFileName, true);

            if (_fmiModelDescription.nativeLibrary != null) {
                _fmiDoStep = _fmiModelDescription.nativeLibrary
                    .getFunction(_fmiModelDescription.modelIdentifier
                            + "_fmiDoStep");
                _fmiInstantiateSlave = _fmiModelDescription.nativeLibrary
                    .getFunction(_fmiModelDescription.modelIdentifier
                            + "_fmiInstantiateSlave");
            }

        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to unzip, read in or process \"" + fmuFileName
                            + "\".");
        }
        if (_debugging) {
            _debug("FMUImport.updateParameters() END");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** The FMI component created by the
     * modelIdentifier_fmiInstantiateSlave() method.
     */
    private Pointer _fmiComponent = null;

    /** The _fmoDoStep() function. */
    private Function _fmiDoStep;

    /** The name of the fmuFile.
     *  The _fmuFileName field is set the first time we read
     *  the file named by the <i>fmuFile</i> parameter.  The
     *  file named by the <i>fmuFile</i> parameter is only read
     *  if the name has changed or if the modification time of
     *  the file is later than the time the file was last read.
     */
    private String _fmuFileName = null;

    /** The modification time of the file named by the
     *  <i>fmuFile</i> parameter the last time the file was read.
     */
    private long _fmuFileModificationTime = -1;

    /** The _fmiInstantiateSlave function. */
    private Function _fmiInstantiateSlave;

    /** A representation of the fmiModelDescription element of a
     *  Functional Mock-up Unit (FMU) file.
     */
    private FMIModelDescription _fmiModelDescription;

    /** A collection of scalar variables for which there is
     *  a connected output port, and for each such variable,
     *  a set of input ports on which it declares it depends,
     *  if any.
     */
    private List<Output> _outputs;

    /** The workspace version at which the _outputs variable was last updated. */
    private long _outputsVersion = -1;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A data structure representing an output from the FMU. */
    private class Output {
        /** The FMI scalar variable for this output. */
        public FMIScalarVariable scalarVariable;

        /** The Ptolemy output port for this output. */
        public TypedIOPort port;

        /** The set of input ports on which the output declares it depends. */
        public Set<TypedIOPort> dependencies;
    }
}
