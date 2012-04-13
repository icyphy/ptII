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
import org.ptolemy.fmi.FMIBooleanType;
import org.ptolemy.fmi.FMIIntegerType;
import org.ptolemy.fmi.FMILibrary;
import org.ptolemy.fmi.FMILibrary.FMICallbackAllocateMemory;
import org.ptolemy.fmi.FMILibrary.FMICallbackFreeMemory;
import org.ptolemy.fmi.FMILibrary.FMICallbackLogger;
import org.ptolemy.fmi.FMILibrary.FMIStepFinished;
import org.ptolemy.fmi.FMIRealType;
import org.ptolemy.fmi.FMIStringType;
import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMIRealType;
import org.ptolemy.fmi.FMIScalarVariable;
import org.ptolemy.fmi.FMIScalarVariable.Alias;
import org.ptolemy.fmi.FMIScalarVariable.Causality;
import org.ptolemy.fmi.FMUFile;
import org.ptolemy.fmi.NativeSizeT;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.sun.jna.Function;
import com.sun.jna.Memory;
import com.sun.jna.NativeLibrary;
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

    /**
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        System.out.println("FMUImport.fire()");

        String modelIdentifier = _fmiModelDescription.modelIdentifier;

        // FIXME: Move this into _updateParameters for efficiency
        Function function = _nativeLibrary.getFunction(modelIdentifier + "_fmiDoStep");

        // FIXME: In FMI-1.0, time is double.  This is not right.
        double time = getDirector().getModelTime().getDoubleValue();

        // FIXME: depending on SDFDirector here.
        double stepSize = ((ptolemy.domains.sdf.kernel.SDFDirector)getDirector()).periodValue();

        System.out.println("FMIImport.fire(): about to call "
                + modelIdentifier + "_fmiDoStep(Component, /* time */ " + time
                + ", /* stepSize */" + stepSize + ", 1)");

        int fmiFlag = ((Integer)function.invokeInt(new Object[] {_fmiComponent, time, stepSize, (byte)1})).intValue();

        if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
            throw new IllegalActionException(this, "Could not simulate, " 
                    + modelIdentifier + "_fmiDoStep(Component, /* time */ " + time
                    + ", /* stepSize */" + stepSize + ", 1) returned " + fmiFlag);
        }

        System.out.println("FMUImport done calling " + modelIdentifier + "_fmiDoStep()");

        for (FMIScalarVariable scalarVariable : _fmiModelDescription.modelVariables) {
            System.out.println("FMUImport.fire(): " + scalarVariable.name);
            if (scalarVariable.alias != null
                    && scalarVariable.alias != Alias.noAlias) {
                // If the scalarVariable has an alias, then skip it.
                // In bouncingBall.fmu, g has an alias, so it is skipped.
                continue;
            }

            Token token = null;
            if (scalarVariable.variability != FMIScalarVariable.Variability.parameter) {
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
                    throw new IllegalActionException("Type " + scalarVariable.type
                            + " not supported.");
                }

                TypedIOPort port = (TypedIOPort)getPort(scalarVariable.name);

                System.out.println("FMUImport.fire(): " + scalarVariable.name + " " + token + " "
                                   + scalarVariable.causality + " " + Causality.output);
                // FIXME: Why is Causality none?
                if (scalarVariable.causality == Causality.none) {
                    port.send(0, token);
                } else if (scalarVariable.causality == Causality.input) {
                    token = port.get(0);
                }
            }
        }



//         String modelIdentifier = _fmiModelDescription.modelIdentifier;


//         Function status = _nativeLibrary.getFunction(modelIdentifier + "_fmiGetStringStatus");
//         System.out.println("FMIImport.fire(): about to call getStatus: " + _fmiComponent);
//                 int fmiFlag2 = ((Integer)status.invokeInt(new Object[] {_fmiComponent, "getStatus", "test..."}));

    }

    /** Initialize the slave FMU.
     *  @exception IllegalActionException If the slave FMU cannot be
     *  initialized.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        System.out.println("FMIImport.initialize() START");

        String modelIdentifier = _fmiModelDescription.modelIdentifier;

        Function status = _nativeLibrary.getFunction(modelIdentifier + "_fmiGetStringStatus");
        System.out.println("FMIImport.initialize(): about to call getStatus: " + _fmiComponent);
        int fmiFlag2 = ((Integer)status.invokeInt(new Object[] {_fmiComponent, FMILibrary.FMIStatusKind.fmiDoStepStatus, "test..."}));
        if (fmiFlag2 > FMILibrary.FMIStatus.fmiWarning) {
            System.out.println("Failed to get status " + status + " returned " + fmiFlag2);
        }

        //if (_debugging) {
            System.out.println("FMUCoSimulation: about to call " + modelIdentifier + "_fmiInitializeSlave");
            //}
        Function function = _nativeLibrary.getFunction(modelIdentifier + "_fmiInitializeSlave");

        // FIXME: FMI-1.0 uses doubles for times.
        double startTime = getDirector().getModelStartTime().getDoubleValue();
        double stopTime = getDirector().getModelStopTime().getDoubleValue();
        int fmiFlag = ((Integer)function.invoke(Integer.class,new Object[] {_fmiComponent, startTime, (byte)1, stopTime})).intValue();
        if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
            throw new IllegalActionException(this, "Could not simulate, " 
                    + modelIdentifier + "_fmiInitializeSlave(Component, /* startTime */ " + startTime
                    + ", 1, /* stopTime */" + stopTime + ") returned " + fmiFlag);
        }
        System.out.println("FMIImport.initialize() END");
    }

    /** Instantiate the slave FMU component.
     *  @exception IllegalActionException if it cannot be instantiated.
     */   
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        System.out.println("FMUImport.preinitialize()");
        // The modelName may have spaces in it.   
        String modelIdentifier = _fmiModelDescription.modelIdentifier;
        
        String fmuLocation = null;
        try {
            // The URL of the fmu file.
            String fmuFileName = fmuFile.asFile().getCanonicalPath();

            fmuLocation = new File(fmuFileName).toURI().toURL().toString();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to get the value of \""
                    + fmuFile + "\"");
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
                new FMULibrary.FMULogger(),
                new FMULibrary.FMUAllocateMemory(),
                new FMULibrary.FMUFreeMemory(),
                new FMULibrary.FMUStepFinished());

        // FIXME: We should send logging messages to the debug listener.
        byte loggingOn = (_debugging ? (byte)1 : (byte)0);

        // FIXME: Logging tends to cause segfaults because of vararg callbacks so we ignore it.
        loggingOn = (byte)0;

        //if (_debugging) {
            System.out.println("FMUCoSimulation: about to call " + modelIdentifier + "_fmiInstantiateSlave");
            //}

        Function instantiateSlave = _nativeLibrary.getFunction(modelIdentifier + "_fmiInstantiateSlave");
        _fmiComponent = (Pointer) instantiateSlave.invoke(Pointer.class,
                new Object [] {
                    modelIdentifier,
                    _fmiModelDescription.guid,
                    fmuLocation,
                    mimeType,
                    timeout,
                    visible,
                    interactive,
                    callbacks,
                    loggingOn});

        Function status = _nativeLibrary.getFunction(modelIdentifier + "_fmiGetStringStatus");
        System.out.println("FMIImport.preinitialize(): about to call getStatus: " + _fmiComponent);
        int fmiFlag2 = ((Integer)status.invokeInt(new Object[] {_fmiComponent, "getStatus", "test..."}));


        if (_fmiComponent.equals(Pointer.NULL)) {
            throw new RuntimeException("Could not instantiate Functional Mock-up Unit (FMU).");
        }
    }

    public interface FMULibrary extends FMILibrary {
        // We need a class that implement the interface because
        // certain methods require interfaces as arguments, yet we
        // need to have method bodies, so we need an actual class.
        public class FMULogger implements FMICallbackLogger {
            // What to do about jni callbacks with varargs?  
            // See http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JNA#fmiCalbackLogger
            public void apply(Pointer fmiComponent, String instanceName, int status, String category, String message/*, Pointer ... parameters*/) {
                System.out.println("Java FMULogger, status: " + status);
                System.out.println("Java FMULogger, message: " + message/*.getString(0)*/);
            }
        }
        //http://markmail.org/message/6ssggt4q6lkq3hen

        public class FMUAllocateMemory implements FMICallbackAllocateMemory {
            public Pointer apply(NativeSizeT nobj, NativeSizeT size) {
                int numberOfObjects = nobj.intValue();
                if (numberOfObjects <= 0) {
                    // instantiateModel() in fmuTemplate.c
                    // will try to allocate 0 reals, integers, booleans or strings.
                    // However, instantiateModel() later checks to see if
                    // any of the allocated spaces are null and fails with
                    // "out of memory" if they are null.
                    numberOfObjects = 1;
                }
                Memory memory = new Memory(numberOfObjects * size.intValue());
                Memory alignedMemory = memory.align(4);
                memory.clear();
                Pointer pointer = alignedMemory.share(0);

//                 System.out.println("Java fmiAllocateMemory " + nobj + " " + size
//                          + "\n        memory: " + memory + " " +  + memory.SIZE + " " + memory.SIZE % 4
//                          + "\n alignedMemory: " + alignedMemory + " " + alignedMemory.SIZE + " " + alignedMemory.SIZE %4
//                          + "\n       pointer: " + pointer + " " + pointer.SIZE + " " + pointer.SIZE % 4
//                                     );

                // Need to keep a reference so the memory does not get gc'd.
                _pointers.add(pointer);

                return pointer;
            }
        }

        public class FMUFreeMemory implements FMICallbackFreeMemory {
            public void apply(Pointer pointer) {
                //System.out.println("Java fmiFreeMemory " + pointer);
                _pointers.remove(pointer)
            }
        }
	public class FMUStepFinished implements FMIStepFinished {
            public void apply(Pointer c, int status) {
                System.out.println("Java fmiStepFinished: " + c + " " + status);
            }
	};
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
    private void _updateParameters()
            throws IllegalActionException, NameDuplicationException {

        System.out.println("FMUImport.updateParameters() START");
        // Unzip the fmuFile.  We probably need to do this
        // because we will need to load the shared library later.
        String fmuFileName = null;
        try {
            // FIXME: Use URLs, not files so that we can work from JarZip files.
            
            // Only read the file if the name has changed from the last time we
            // read the file or if the modification time has changed.
            fmuFileName = fmuFile.asFile().getCanonicalPath();
            if (fmuFileName == _fmuFileName) {
                return;
            }
            _fmuFileName = fmuFileName;
            long modificationTime = new File(fmuFileName).lastModified();
            if (_fmuFileModificationTime == modificationTime) {
                return;
            }
            _fmuFileModificationTime = modificationTime;

            _fmiModelDescription = FMUFile.parseFMUFile(fmuFileName);
            
//             // Instantiate ports and parameters.
//             for (FMIScalarVariable scalar : _fmiModelDescription.modelVariables) {
//                 if (scalar.type instanceof FMIRealType) {
//                     if (scalar.variability == FMIScalarVariable.Variability.parameter) {
//                         Parameter parameter = new Parameter(this, scalar.name);
//                         parameter.setExpression(Double.toString(((FMIRealType)scalar.type).start));
//                         // Prevent exporting this to MoML unless it has
//                         // been overridden.
//                         parameter.setDerivedLevel(1);
//                     } {
//                         // FIXME: All output ports?
//                         TypedIOPort port = new TypedIOPort(this, scalar.name, false, true);
//                         port.setDerivedLevel(1);
//                         // FIXME: set the type
//                         port.setTypeEquals(BaseType.DOUBLE);
//                     }
//                 } else {
//                     throw new IllegalActionException("Don't know how to handle "
//                             + scalar.type);
//                 }
//             }
            
            String library = null;
            try {
                library = FMUFile.fmuSharedLibrary(_fmiModelDescription);
                System.out.println("About to load " + library);
                _nativeLibrary = NativeLibrary.getInstance(library);
            } catch (Throwable throwable) {
                List<String> binariesFiles = new LinkedList<String>();
                for (File file : _fmiModelDescription.files) {
                    if (file.toString().indexOf("binaries") != -1) {
                        binariesFiles.add(file.toString() + "\n");
                    }
                }
                String message = "Failed to load the \""
                        + library + "\" shared library, which was created "
                        + "by unzipping \"" +
                        fmuFile.asFile()
                        + "\". Usually, this is because the .fmu file does "
                        + "not contain a shared library for the current "
                        + "architecture.  The fmu file contained the "
                        + "following files with 'binaries' in the path:\n" 
                        + binariesFiles;
                System.out.println(this.getFullName() + ": "
                        + message + "\n Original error:\n " 
                        + throwable);
                // Note that Variable.propagate() will handle this error and
                // hide it.
                throw new IllegalActionException(this, throwable, message);
            }


        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to unzip, read in or process \"" + fmuFileName + "\".");
        }
        System.out.println("FMUImport.updateParameters() END");
    }

    /** The FMI component created by the
     * modelIdentifier_fmiInstantiateSlave() method.
     */
    private Pointer _fmiComponent = null;

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

    /** A representation of the fmiModelDescription element of a
     *  Functional Mock-up Unit (FMU) file.
     */
    FMIModelDescription _fmiModelDescription;

    /** A reference to the shared library that was loaded from the
     * .fmu file.
     */   
    NativeLibrary _nativeLibrary;

    private static Set<Pointer> _pointers = new HashSet<Pointer>();

}
