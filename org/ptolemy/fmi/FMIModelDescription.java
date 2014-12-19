/* A Function Mock-up Interface ModelDescription.

   Copyright (c) 2012-2014 The Regents of the University of California.
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
package org.ptolemy.fmi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.ptolemy.fmi.FMIScalarVariable.Causality;
import org.ptolemy.fmi.type.FMIRealType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.NativeLibrary;

///////////////////////////////////////////////////////////////////
//// FMUModelDescription

/**
 * An object that represents the fmiModelDescription element of a
 * modelDescription.xml file contained within a Functional Mock-up Interface
 * (.fmu) file.
 *
 * <p>
 * A Functional Mock-up Unit file is a .fmu file in zip format that contains a
 * .xml file named "modelDescription.xml". This class is a representation of the
 * elements of that file.
 * </p>
 *
 * <p>
 * FMI documentation may be found at <a
 * href="http://www.modelisar.com/fmi.html">
 * http://www.modelisar.com/fmi.html</a>.
 * </p>
 *
 * @author Christopher Brooks, Thierry S. Nouidui
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMIModelDescription {

    // /////////////////////////////////////////////////////////////////
    // // public fields ////

    /**
     * For FMI 2.0 and greater, the XML file may specify that the FMU supports
     * getting and setting its state. This defaults to false if not present in
     * the XML file.
     */
    public boolean canGetAndSetFMUstate = false;

    /**
     * For the IBM/UCB proposed extension to FMI 2.0, there is a capability flag
     * canProvideMaxStepSize that indicates that the FMU implements the
     * procedure fmiGetMaxStepSize().
     */
    public boolean canProvideMaxStepSize = false;

    /**
     * For FMI 2.0 and greater, the XML file may specify that the FMU supports
     * providing directional derivatives state. This defaults to false if not
     * present in the XML file.
     */
    public boolean providesDirectionalDerivative = false;

    /** The list of files that were extracted from the .fmu file. */
    public List<File> files;

    /**
     * The fmiVersion, typically the value of the fmiVersion attribute from a
     * .fmu file. The fmiVersion field is set to 1.0 for FMI 1.0.
     */
    public String fmiVersion;

    /**
     * The absolute path to the resources directory. In FMI-2.0, the
     * fmiInstantiateXXX() method has a fmuResourceLocation parameter. This
     * value of this parameter typically starts with "file://" but may start
     * with "http://", "https://" or "ftp://".
     */
    public String fmuResourceLocation;

    /**
     * The FMI guid, typically the value of the guid attribute from a .fmu file.
     * The value of guid in the modelDescription.xml file must match the guid in
     * the shared library.
     */
    public String guid;

    /**
     * If true, then the FMU is intended for model exchange, not co-simulation.
     */
    public boolean modelExchange;

    /**
     * If true, then the FMU is intended for model exchange, with QSS
     * integrator.
     */
    public boolean qssIntegration;

    /**
     * The FMI modelIdentifier, typically the value of the modelIdentifier
     * attribute from a .fmu file. The modelIdentifier is the basename for the
     * shared library.
     */
    public String modelIdentifier;

    /**
     * The FMI modelName, typically the value of the modelName attribute from a
     * .fmu file. The modelName may have spaces in it.
     */
    public String modelName;

    /** The list of ScalarVariable elements. */
    public List<FMIScalarVariable> modelVariables = new LinkedList<FMIScalarVariable>();

    /** The list of continuous states. */
    public List<ContinuousState> continuousStates = new LinkedList<ContinuousState>();

    /** The list of state derivatives. */
    public List<FMI20ContinuousStateDerivative> continousStateDerivatives = new LinkedList<FMI20ContinuousStateDerivative>();

    /** The list of output variables. */
    public List<FMI20Output> outputs = new LinkedList<FMI20Output>();

    /** Number of continuous states. */
    public int numberOfContinuousStates;

    /** Number of event indicators. */
    public int numberOfEventIndicators;

    /**
     * A map from TypeDefinition type name declarations to the defined type
     * name.
     */
    public Map<String, String> typeDefinitions = new HashMap<String, String>();

    /**
     * The capabilities for co-simulation. FMIModelDescription has a field for
     * Cosimulation capabilities and a field for Model Exchange capabilities. We
     * need both because JModelica fmus define both capabilities. We use a
     * baseclass here so that the FMIModelDescription class does not change as
     * we support other capabilities.
     */
    public FMICapabilities cosimulationCapabilities;

    /**
     * The capabilities for model exchange.
     */
    public FMICapabilities modelExchangeCapabilities;

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Create the state vector. This should only be called on fmis with a
     * fmiVersion greater than 1.5.
     */
    public void createStateVector() throws IOException {
        // Create the state vector.
        int count = 0;
        _continuousStates = new HashMap<Integer, ContinuousState>();

        for (int i = 0; i < continousStateDerivatives.size(); i++) {
            ContinuousState state = new ContinuousState();
            int index = continousStateDerivatives.get(i).index;
            // Substract the index by 1 since the numbering of scalar variables
            // starts with 1 in the FMU model description file whereas the get()
            // starts by 0.
            FMIScalarVariable scalar = modelVariables.get(index - 1);

            if (scalar.type instanceof FMIRealType
                    && ((FMIRealType) scalar.type).indexState > 0) {
                // Mark this variable as a state.
                modelVariables.get(((FMIRealType) scalar.type).indexState - 1).isState = true;
                state.name = modelVariables
                        .get(((FMIRealType) scalar.type).indexState - 1).name;
                state.start = ((FMIRealType) modelVariables
                        .get(((FMIRealType) scalar.type).indexState - 1).type).start;
                state.nominal = ((FMIRealType) modelVariables
                        .get(((FMIRealType) scalar.type).indexState - 1).type).nominal;
                state.dependentScalarVariables = continousStateDerivatives
                        .get(i).dependentScalarVariables;
                state.scalarVariable = modelVariables
                        .get(((FMIRealType) scalar.type).indexState - 1);
                _continuousStates.put(i, state);
                count++;
            }
        }
        if (continousStateDerivatives.size() != count) {
            throw new IOException("Number of state derivatives "
                    + continousStateDerivatives.size()
                    + " does not match the number of continuous states "
                    + count);
        }
        // Store the state vector in a list.
        Iterator<ContinuousState> valueIterator = _continuousStates.values()
                .iterator();
        while (valueIterator.hasNext()) {
            continuousStates.add((ContinuousState) valueIterator.next());
        }
        numberOfContinuousStates = continuousStates.size();
    }

    /**
     * Unload the native library and free up any Java references to memory
     * allocated by the allocate memory callback.
     */
    public void dispose() {
	if (_fmuAllocateMemory != null) {
	    // Prevent a memory leak by releasing Memory and Pointer objects to
	    // the GC.
	    // FIXME: This is wrong! This releases all instances of Memory and
	    // Pointer that have been created!
	    // It should only release those for this FMU.
	    FMULibrary.FMUAllocateMemory.pointers.clear();
	}
	if (_nativeLibrary != null) {
	    _nativeLibrary.dispose();
	}
    }

    /**
     * Return a string describing the specified fmiStatus.
     *
     * @param fmiStatus
     *            The status returned by an FMI procedure.
     * @return a String describing the status.
     */
    public static String fmiStatusDescription(int fmiStatus) {
	// FIXME: FMI 2.0 has apparently lost fmiWarning and fmiFatal.
	// What is the new encoding? Need the header file.
	switch (fmiStatus) {
	case 0:
	    return "fmiOK";
	case 1:
	    return "fmiWarning";
	case 2:
	    return "fmiDiscard";
	case 3:
	    return "fmiError";
	case 4:
	    return "fmiFatal";
	default:
	    return "fmiPending";
	}
    }

    /**
     * Return a class that provides a callback function that allocates memory,
     * but retains a reference so that the memory does not get gc'd.
     *
     * @return The class that provides a callback function that allocates
     *         memory.
     */
    public FMULibrary.FMUAllocateMemory getFMUAllocateMemory() {
	return _fmuAllocateMemory;
    }

    /**
     * Return the canonical native library path. If the shared library names by
     * {@link org.ptolemy.fmi.FMUFile#fmuSharedLibrary(FMIModelDescription)}
     * exists, then it is returned. If it does not exist, then
     * {@link org.ptolemy.fmi.FMUBuilder#build(File)} is invoked, which may
     * build the shared library.
     *
     * @return The canonical native library path.
     * @exception IOException
     *                If the FMU file does not contain binaries for the current
     *                platform.
     */
    public String getNativeLibraryPath() throws IOException {
	// The following should not be done until at least the modelIdentifier
	// has been set in fmiModelDescription.
	if (modelIdentifier == null || modelIdentifier.trim().equals("")) {
	    // FIXME: Don't use runtime exception.
	    throw new RuntimeException("No modelIdentifier is given");
	}

	// Get the name of the shared library file for the current platform.
	String sharedLibrary = FMUFile.fmuSharedLibrary(this);

	File sharedLibraryFile = new File(sharedLibrary);
	if (!sharedLibraryFile.exists()) {
	    List<String> binariesFiles = new LinkedList<String>();
	    for (File file : files) {
		if (file.toString().indexOf("binaries") != -1) {
		    binariesFiles.add(file.toString() + "\n");
		}
	    }
	    String message = "Current platform not supported by this FMU. "
		    + "Attempted to load \"" + sharedLibrary
		    + "\" shared library, " + "The fmu file contains the "
		    + "following files with 'binaries' in the path:\n"
		    + binariesFiles;
	    if (!sharedLibraryFile.exists()) {
		FMUBuilder builder = new FMUBuilder();
		boolean isBuildOK = false;
		try {
		    isBuildOK = builder.build(sharedLibraryFile);
		    System.out.println("FMU Builder messages:\n"
			    + builder.buffer);
		} catch (Throwable throwable2) {
		    // Java 1.5 does not support IOException(String, Throwable).
		    // We sometimes compile this with gcj, which is Java 1.5
		    IOException exception = new IOException(
			    "Failed to build \"" + sharedLibraryFile
			            + "\".\nThe build was:\n" + builder.buffer
			            + "\n" + message);
		    exception.initCause(throwable2);
		    throw exception;

		}
		if (!isBuildOK) {
		    throw new IOException("It was not possible to build \""
			    + sharedLibraryFile + "\": " + builder.buffer
			    + "\n" + message);
		}
	    }
	}
	return sharedLibraryFile.getCanonicalPath();
    }

    /**
     * Get the native function from the native library.
     *
     * <p>
     * A FMI 1.0 FMU will have functions like MyModel_fmiGetReal().
     * </p>
     *
     * <p>
     * A FMI 2.0 FMU that is shipped with C source code or with a static
     * library, will have functions like MyModel_fmiGetReal().
     * </p>
     *
     * <p>
     * However, a FMI 2.0 FMU that is shipped with a shared library (and without
     * C source code), will have functions like fmiGetReal().
     * </p>
     *
     * <p>
     * This method tries both formats. The leading modelIdentifier is tried
     * first because we believe that FMUs should be shipped with source code. If
     * the function name with the leading modelIdentifier is not found, then
     * just the functionName is tried.
     * </p>
     *
     * @param functionName
     *            The name of the function, without a leading underscore.
     * @return The function.
     * @exception UnsatisfiedLinkError
     *                If the function is not found using either format.
     * @exception IOException
     *                If the native library cannot be found.
     */
    public Function getFmiFunction(String functionName)
	    throws UnsatisfiedLinkError, IOException {
	// A different implementation would try to guess which
	// function is named depending on if there is C code present
	// and whether there is a dynamic library present. However,
	// for Java, it is unlikely that a static library is being
	// accessed and determining whether source is present to
	// determine the function name is asinine (cxh, 7/16/13)
	if (_nativeLibrary == null) {
	    getNativeLibrary();
	}
	Function function = null;
	// FMI-2.0
	String name1 = modelIdentifier + "_"
	        + functionName.replace("fmi", "fmi2");
	String name2 = functionName.replace("fmi", "fmi2");
	// FMI-2.0RC1
	String name3 = modelIdentifier + "_" + functionName;
	// FMI-1.0?
	String name4 = functionName;
	try {
	    function = _nativeLibrary.getFunction(name1);
	} catch (UnsatisfiedLinkError error) {
	    try {
		function = _nativeLibrary.getFunction(name2);
	    } catch (UnsatisfiedLinkError error2) {
		try {
		    function = _nativeLibrary.getFunction(name3);
		} catch (UnsatisfiedLinkError error3) {
		    try {
			function = _nativeLibrary.getFunction(name4);
		    } catch (UnsatisfiedLinkError error4) {
			UnsatisfiedLinkError linkError = new UnsatisfiedLinkError(
			        "Could not find the function, \"" + name1
			                + "\" or \"" + name2 + "\" or \""
			                + name3 + "\" or \"" + name4 + "\" in "
			                + _nativeLibrary);
			// linkError.initCause(error);
			throw linkError;
		    }
		}
	    }
	}
	return function;
    }

    /**
     * Get the native library of C functions for the current platform. A side
     * effect is that if the native library does not exist, then
     * {@link org.ptolemy.fmi.FMUBuilder#build(File)} is invoked, which may
     * build the shared library.
     *
     * @return The library of functions for the current platform.
     * @exception IOException
     *                If the FMU file does not contain binaries for the current
     *                platform.
     */
    public NativeLibrary getNativeLibrary() throws IOException {
	if (_nativeLibrary != null) {
	    return _nativeLibrary;
	}
	String sharedLibrary = getNativeLibraryPath();
	try {
            String osName = System.getProperty("os.name").toLowerCase(
                    Locale.getDefault());
            if (osName.startsWith("linux")) {
                // Call dlopen() with RTLD_LAZY and not with RTLD_GLOBAL, which is the
                // default. 
                // See http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/FMU#LinuxSymbolProblems
                // See https://github.com/twall/jna/issues/44
                // One symptom of this failing is that if we run different values fmus,
                // then "fmiSetString: Illegal call sequence." may appear.
                // What's happening is that the values.c file has multiple definitions
                // of setString() and if we load with RTLD_GLOBAL, then we might
                // get the setString() from another FMU.
                Map options = new HashMap();

                // We load with RTLD_LAZY here. RTLD_LOCAL is define
                // in /usr/local/bits/dlfcn.h as 0 and gets or'd.
                Integer RTLD_LAZY = new Integer(1);
                options.put(Library.OPTION_OPEN_FLAGS, RTLD_LAZY);

                _nativeLibrary = NativeLibrary.getInstance(sharedLibrary, options);
            } else {
                // Under other platforms, such as Windows,
                // use the defaults.  The Dymola fmus require
                // this or we get Invalid Memory Access.
                _nativeLibrary = NativeLibrary.getInstance(sharedLibrary);
            }
	} catch (Throwable throwable3) {
	    // Java 1.5 does not support
	    // IOException(String, Throwable). We
	    // sometimes compile this with gcj, which is
	    // Java 1.5
	    IOException exception = new IOException(
		    "Error loading \""
		            + sharedLibrary
		            + "\" shared library.  "
		            + "To debug loading errors, "
		            + "Restart Java with \"-Djna.debug_load=true\".  "
		            + "See http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JNA#JNADebugging.");
	    exception.initCause(throwable3);
	    throw exception;
	}
	return _nativeLibrary;
    }

    /**
     * Return the value of the FMI modelName element.
     *
     * @return The model name.
     */
    @Override
    public String toString() {
	return modelName;
    }

    /**
     * Parse the ModelStructure to catch the I/O direct dependencies
     *
     */
    public void parseDependenciese(Node node) {
	NamedNodeMap attributes = node.getAttributes();
	Long valueReference = Long.parseLong(attributes.getNamedItem("index")
	        .getNodeValue());

	Node dependencyNode = attributes.getNamedItem("dependencies");
	if (dependencyNode != null
	        && dependencyNode.getNodeValue().trim().length() != 0) {
	    String[] dependencies = dependencyNode.getNodeValue().trim()
		    .split(" ");

	    for (int i = 0; i < modelVariables.size(); i++) {
		if (modelVariables.get(i).valueReference == valueReference) {
		    modelVariables.get(i).directDependency.clear();
		    for (int j = 0; j < dependencies.length; j++) {

			for (int k = 0; k < modelVariables.size(); k++) {
			    try {
				if (modelVariables.get(k).valueReference == Long
				        .parseLong(dependencies[j])
				        && modelVariables.get(k).causality
				                .equals(Causality.input)) {
				    modelVariables.get(i).directDependency
					    .add(modelVariables.get(k).name);
				    break;
				}
			    } catch (NumberFormatException ex) {
				NumberFormatException nfx = new NumberFormatException(
				        "Failed to parse \"" + dependencies[j]
				                + "\", which is the " + j
				                + " (0-based) dependency.");
				nfx.initCause(ex);
				throw nfx;
			    }
			}
		    }
		}
	    }
	}
    }

    /**
     * Add direct dependency to each output variables from all input variables
     *
     */
    public void addDefaultInputDependencies() {
	List<String> inputVariables = new ArrayList<String>();

	// Get the list of all the input variables
	for (int i = 0; i < modelVariables.size(); i++) {
	    if (modelVariables.get(i).causality.equals(Causality.input)) {
		inputVariables.add(modelVariables.get(i).name);
	    }
	}

	// Set default dependencies
	for (int i = 0; i < modelVariables.size(); i++) {
	    if (modelVariables.get(i).causality.equals(Causality.output)
		    && inputVariables != null) {
		for (int j = 0; j < inputVariables.size(); j++) {
		    modelVariables.get(i).directDependency.add(inputVariables
			    .get(j));
		}
	    }
	}
    }
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A data structure representing a continuous state of the FMU. */
    public static class ContinuousState {
        // FindBugs indicates that this should be a static class.
        /** The name of the continuous state variable. */
        public String name;

        /** The start value for this variable, or null if it is not given. */
        public Double start;

        /** The nominal value for this variable, or null if it is not given. */
        public Double nominal;

        /** The list of dependent ScalarVariable elements. */
        public LinkedList<FMIScalarVariable> dependentScalarVariables;

        /** The FMI scalar variable for this state. */
        public FMIScalarVariable scalarVariable;

        /** The set of input ports on which the state depends. */
        public Set<TypedIOPort> dependencies;

        /** The Ptolemy state port for this state. */
        public TypedIOPort port;
        
        /** The flag which indicates a change of a state. */
        public boolean hasChanged;
    }

    // /////////////////////////////////////////////////////////////////
    // // private fields ////

    /** Record of continuous state variables. */
    private HashMap<Integer, String> _continuousStates = new HashMap<Integer, String>();

    /**
     * A class that allocates memory, but retains a reference so that the memory
     * does not get gc'd.
     */
    private FMULibrary.FMUAllocateMemory _fmuAllocateMemory = new FMULibrary.FMUAllocateMemory();

    /**
     * The NativeLibrary associated with the platform-dependent shared library
     * in the .fmu file.
     */
    private NativeLibrary _nativeLibrary;
}
