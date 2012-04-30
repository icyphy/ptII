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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.ptolemy.fmi.FMICallbackFunctions;
import org.ptolemy.fmi.FMILibrary;
import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMIScalarVariable;
import org.ptolemy.fmi.FMIScalarVariable.Alias;
import org.ptolemy.fmi.FMIScalarVariable.Causality;
import org.ptolemy.fmi.FMUFile;
import org.ptolemy.fmi.NativeSizeT;
import org.ptolemy.fmi.type.FMIBooleanType;
import org.ptolemy.fmi.type.FMIIntegerType;
import org.ptolemy.fmi.type.FMIRealType;
import org.ptolemy.fmi.type.FMIStringType;
import org.ptolemy.fmi.type.FMIType;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

import com.sun.jna.Function;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

///////////////////////////////////////////////////////////////////
//// FMUImport

/**
 * Invoke a Functional Mock-up Interface (FMI) 1.0 Model Exchange 
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
 * <p>To use this actor from within Vergil, use Import -&gt; Import
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
public class FMUImport extends TypedAtomicActor {
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

    /** The Functional Mock-up Unit (FMU) file.
     *  The FMU file is a zip file that contains a file named "modelDescription.xml"
     *  and any necessary shared libraries.  The file is read when this
     *  actor is instantiated or when the file name changes.  The initial default
     *  value is "fmuImport.fmu".
     */
    public FileParameter fmuFile;

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

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

    /** Read data from input ports, fire the slave fmu and then write data
     *  to the output ports.    
     *    
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_debugging) {
            _debug("FMUImport.fire()");
        }

        String modelIdentifier = _fmiModelDescription.modelIdentifier;

        // FIXME: Iterate through the scalarVariables and set all the parameters

        // Iterate through the scalarVariables and set all the inputs.
        for (FMIScalarVariable scalarVariable : _fmiModelDescription.modelVariables) {
            if (_debugging) {
                _debug("FMUImport.fire(): " + scalarVariable.name);
            }
            if (scalarVariable.alias != null
                    && scalarVariable.alias != Alias.noAlias) {
                // If the scalarVariable has an alias, then skip it.
                // In bouncingBall.fmu, g has an alias, so it is skipped.
                continue;
            }

            if (scalarVariable.variability != FMIScalarVariable.Variability.parameter) {
                if (scalarVariable.causality != Causality.input) {
                    continue;
                }
                TypedIOPort port = (TypedIOPort) getPort(scalarVariable.name);

                Token token = port.get(0);

                // FIXME: What about arrays?
                if (scalarVariable.type instanceof FMIBooleanType) {
                    scalarVariable.setBoolean(_fmiComponent,
                            ((BooleanToken)token).booleanValue());
                } else if (scalarVariable.type instanceof FMIIntegerType) {
                    // FIXME: handle Enumerations?
                    scalarVariable.setInt(_fmiComponent,
                            ((IntToken)token).intValue());
                } else if (scalarVariable.type instanceof FMIRealType) {
                    scalarVariable.setDouble(_fmiComponent,
                            ((DoubleToken)token).doubleValue());
                } else if (scalarVariable.type instanceof FMIStringType) {
                    scalarVariable.setString(_fmiComponent,
                            ((StringToken)token).stringValue());
                } else {
                    throw new IllegalActionException("Type "
                            + scalarVariable.type + " not supported.");
                }
            }
        }

        // Call fmiDoStep() with the current data.

        // FIXME: In FMI-1.0, time is double. This is not right.
        double time = getDirector().getModelTime().getDoubleValue();

        // FIXME: depending on SDFDirector here.
        double stepSize = ((ptolemy.domains.sdf.kernel.SDFDirector) getDirector())
                .periodValue();

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

        // FIXME: should we update all the Parameters?


        // Iterate through the scalarVariables and get all the outputs.
        for (FMIScalarVariable scalarVariable : _fmiModelDescription.modelVariables) {
            if (_debugging) {
                _debug("FMUImport.fire(): " + scalarVariable.name);
            }
            if (scalarVariable.alias != null
                    && scalarVariable.alias != Alias.noAlias) {
                // If the scalarVariable has an alias, then skip it.
                // In bouncingBall.fmu, g has an alias, so it is skipped.
                continue;
            }

            if (scalarVariable.variability != FMIScalarVariable.Variability.parameter) {
                TypedIOPort port = (TypedIOPort) getPort(scalarVariable.name);

                if (port == null || port.getWidth() <= 0) {
                    continue;
                }

                Token token = null;

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
                    _debug("FMUImport.fire(): " + scalarVariable.name + " "
                            + token + " " + scalarVariable.causality + " "
                            + Causality.output);
                }
                port.send(0, token);
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
        // This method is called by the gui to import a fmu file and create the
        // actor.
        // The primary issue is that we need to define the ports early on and
        // handle
        // changes to the ports.
        FMIModelDescription fmiModelDescription = FMUFile
                .parseFMUFile(fmuFileName);

        // FIXME: Use URLs, not files so that we can work from JarZip files.

        // If a location is given as a URL, construct MoML to
        // specify a "source".
        String source = "";
        // FIXME: not sure about this
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

                // FIXME: Need to sanitize the name.
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

    /** An interface that contains JNA callbacks.
     */
    public interface FMULibrary extends FMILibrary {
        /** The logging function.
         * We need a class that implement the interface because
         * certain methods require interfaces as arguments, yet we
         * need to have method bodies, so we need an actual class.
         */
        public class FMULogger implements FMICallbackLogger {
            /** Log a message.
             *  Note that arguments after the message are currently ignored.   
             *  @param fmiComponent The component that was instantiated.
             *  @param instanceName The name of the instance of the FMU.
             *  @param status The fmiStatus, see
             *  {@link org.ptolemy.fmi.FMILibrary.FMIStatus}
             *  @param category The category, typically "log" or "error".
             *  @param message The message
             */

            public void apply(Pointer fmiComponent, String instanceName,
                    int status, String category, String message, Pointer /*...*/ parameters) {
                // What to do about jni callbacks with varargs?
                // See
                // http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JNA#fmiCallbackLogger
                //System.out.println("Java FMULogger, status: " + status);
                //System.out.println("Java FMULogger, message: " + message);


                // FIXME: Need to handle the fmi-specific # format:
                // #<Type><valueReference#, where <Type> is one of
                // r, i, b or s. To print a #, use ##.

                if (parameters != null ) {
                    StringTokenizer tokenizer = new StringTokenizer(message, "%", false /* Return delimiters */); 
                    ArrayList <Object> parameterList = new ArrayList<Object>();
                    long nativeLong = Pointer.nativeValue(parameters);
                    int offset = 0;
                    if (!message.startsWith("%") && tokenizer.hasMoreTokens()) {
                        // Throw away the first token.
                        tokenizer.nextToken();
                    }
                    while (tokenizer.hasMoreTokens()) {
                        String token = tokenizer.nextToken();
                        boolean foundType = false;
                        for(int i = 0 ; i < token.length() && !foundType; i++) {
                            char type = token.charAt(i);
                            //System.out.println("Token: " + token + " " + type + " " + offset);
                            switch (type) {
                            case 'd':
                            case 'i':
                            case 'o': // Unsigned octal
                            case 'u': // Unsigned decimal
                            case 'x': // Unsigned hex
                            case 'X': // Unsigned hex
                                foundType = true;
                                //String s = parameters.toString();
                                //int [] value0 = newPointer.getIntArray(offset, 1);

                                //int value = newPointer.getInt(offset);
                                //System.out.println("Token: " + token + " " + conversion + ": " + type + " " + value + " " + value0[0] + " " + Integer.toHexString(value) + " " + s);
                                //parameterList.add(Integer.valueOf(value));
                                if (!_printedMessage) {
                                    _printedMessage = true;
                                    System.out.println("FIXME: logger: don't know how to get integers, using 666 instead.");
                                }
                                parameterList.add(Integer.valueOf(666));
                                offset += 4;
                                break;
                            case 'f': // Doubles
                            case 'e':
                            case 'E':
                            case 'g':
                            case 'G':
                                foundType = true;
                                if (!_printedMessage) {
                                    _printedMessage = true;
                                    System.out.println("FIXME: logger: don't know how to get doubles, using 666.666 instead.");
                                }
                                //parameterList.add(Double.valueOf(parameters.getDouble(offset++)));
                                parameterList.add(Double.valueOf(666.666));
                                offset += 4;
                                break;
                            case 'c': // Unsigned char
                                foundType = true;
                                //parameterList.add(new Character(parameters.getChar(offset++)));
                                if (!_printedMessage) {
                                    _printedMessage = true;
                                    System.out.println("FIXME: logger: don't know how to get chars, using ! instead.");
                                }
                                parameterList.add(new Character('!'));
                                offset += 1;
                                break;
                            case 's': // String
                                foundType = true;
                                String result = "";
                                if (offset == 0) {
                                    result = new String(parameters.getString(offset));
                                } else {
                                    if (!_printedMessage) {
                                        _printedMessage = true;
                                        System.out.println("FIXME: logger: don't know how to get string other than the first string, using FIXME instead.");                                    
                                    }
                                    result = "FIXME";
                                }
                                offset += 4;
                                //System.out.println("Token: " + token + " " + conversion + ": " + type + " " + result);
                                parameterList.add(result);
                                break;
                            case 'p': // Pointer
                                foundType = true;
                                //parameterList.add(parameters.getPointer(offset++).toString());
                                if (!_printedMessage) {
                                    _printedMessage = true;
                                    System.out.println("FIXME: logger: don't know how to get long other than the first string, using 666 instead.");                                    
                                }
                                parameterList.add(Long.valueOf(666));
                                break;
                            case 'n':
                                // FIXME: Don't know how to handle this:
                                // "The argument shall be a
                                // pointer to an integer into which is
                                // written the number of characters
                                // written to the output stream so far
                                // by this call to fprintf .  No
                                // argument is converted."
                                foundType = true;
                                break;
                            case '%':
                                // FIXME: what about %%?
                                foundType = true;
                                break;
                            default:
                                break;
                            }                                
                        }
                    }
                    // Java format does not handle %u.  Lamers.
                    message = message.replace("%u", "%d");
                    //System.out.println("Java FMULogger, message0: " + message + " " + parameterList.size() + " " + parameterList);
                    System.out.format("Java FMULogger: " + message, parameterList.toArray());
                    System.out.println("");
                }
            }
        }

        /** Allocate memory. */

        public class FMUAllocateMemory implements FMICallbackAllocateMemory {
            // See http://markmail.org/message/6ssggt4q6lkq3hen

            /** Allocate memory.
             *  @param numberOfObjects The number of objects to allocate.
             *  @param size The size of the object in bytes.
             *  @return a Pointer to the allocated memory.
             */
            public Pointer apply(NativeSizeT numberOfObjects, NativeSizeT size) {
                int numberOfObjectsValue = numberOfObjects.intValue();
                if (numberOfObjectsValue <= 0) {
                    // instantiateModel() in fmuTemplate.c
                    // will try to allocate 0 reals, integers, booleans or
                    // strings.
                    // However, instantiateModel() later checks to see if
                    // any of the allocated spaces are null and fails with
                    // "out of memory" if they are null.
                    numberOfObjectsValue = 1;
                }
                Memory memory = new Memory(numberOfObjectsValue * size.intValue());
                Memory alignedMemory = memory.align(4);
                memory.clear();
                Pointer pointer = alignedMemory.share(0);

                // System.out.println("Java fmiAllocateMemory " + numberOfObjectsValue + " " +
                // size
                // + "\n        memory: " + memory + " " + + memory.SIZE + " " +
                // memory.SIZE % 4
                // + "\n alignedMemory: " + alignedMemory + " " +
                // alignedMemory.SIZE + " " + alignedMemory.SIZE %4
                // + "\n       pointer: " + pointer + " " + pointer.SIZE + " " +
                // pointer.SIZE % 4
                // );

                // Need to keep a reference so the memory does not get gc'd.
                _pointers.add(pointer);

                return pointer;
            }
        }

        /** A callback that frees memory.
         */
        public class FMUFreeMemory implements FMICallbackFreeMemory {
            /** Free memory.
             *  @param pointer A JNA Pointer to the object to be freed.
             */
            public void apply(Pointer pointer) {
                _pointers.remove(pointer);
            }
        }

        /** A callback for when the step is finished. */
        public class FMUStepFinished implements FMIStepFinished {
            /** The step is finished.
             *  @param fmiComponent The FMI component that was instantiate.
             *  @param status The status flag.  See the FMI documentation.
             */
            public void apply(Pointer fmiComponent, int status) {
                System.out.println("Java fmiStepFinished: " + fmiComponent
                        + " " + status);
            }
        };
    }

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
            _fmiModelDescription = FMUFile.parseFMUFile(fmuFileName);

            _fmiDoStep = _fmiModelDescription.nativeLibrary
                    .getFunction(_fmiModelDescription.modelIdentifier
                            + "_fmiDoStep");
            _fmiInstantiateSlave = _fmiModelDescription.nativeLibrary
                    .getFunction(_fmiModelDescription.modelIdentifier
                            + "_fmiInstantiateSlave");

        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to unzip, read in or process \"" + fmuFileName
                            + "\".");
        }
        if (_debugging) {
            _debug("FMUImport.updateParameters() END");
        }
    }

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
    Function _fmiInstantiateSlave;

    /** A representation of the fmiModelDescription element of a
     *  Functional Mock-up Unit (FMU) file.
     */
    FMIModelDescription _fmiModelDescription;

    /** Keep references to memory that has been allocated and
     *  avoid problems with the memory being garbage collected.   
     */
    private static Set<Pointer> _pointers = new HashSet<Pointer>();

    private static String printMemory(Pointer pointer, int size) {
        // FIXME: This method is copied from the JNA distribution.
        // It should be properly credited or removed.
        final int BYTES_PER_ROW = 4;
        String LS = System.getProperty("line.separator");
        byte[] buf = pointer.getByteArray(0, size);
        StringBuffer contents = new StringBuffer(LS);
        for (int i=0;i < buf.length;i++) {
            if ((i % BYTES_PER_ROW) == 0) {
                contents.append("[");
            }
            if (buf[i] >=0 && buf[i] < 16) {
                contents.append("0");
            }
            contents.append(Integer.toHexString(buf[i] & 0xFF));
            if ((i % BYTES_PER_ROW) == BYTES_PER_ROW-1 && i < buf.length-1) {
                contents.append("]" + LS);
            }
        }

        contents.append("]");

        for (int i=0;i < buf.length;i++) {
            if ((i % BYTES_PER_ROW) == 0) {
                contents.append("<");
            }
            //if (buf[i] >=0 && buf[i] < 16) {
            //    contents.append("0");
            //}
            contents.append(new Character((char)(buf[i] & 0xff)));
            if ((i % BYTES_PER_ROW) == BYTES_PER_ROW-1 && i < buf.length-1) {
                contents.append(">" + LS);
            }
        }
        contents.append(">");
        return contents.toString();
    }

    // True if we printed the fixme message.
    private static boolean _printedMessage = false;
}
