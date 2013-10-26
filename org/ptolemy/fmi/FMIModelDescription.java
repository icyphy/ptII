/* A Function Mock-up Interface ModelDescription.

 Copyright (c) 2012 The Regents of the University of California.
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;

///////////////////////////////////////////////////////////////////
//// FMUModelDescription

/**
 * An object that represents the fmiModelDescription element of a
 * modelDescription.xml file contained within a
 * Functional Mock-up Interface (.fmu) file.
 *
 * <p>A Functional Mock-up Unit file is a .fmu file in zip format that
 * contains a .xml file named "modelDescription.xml".  This class is a
 * representation of the elements of that file.</p>
 *
 * <p>FMI documentation may be found at
 * <a href="http://www.modelisar.com/fmi.html">http://www.modelisar.com/fmi.html</a>.
 * </p>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMIModelDescription {

    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////

    /** For FMI 2.0 and greater, the XML file may specify that the FMU
     *  supports getting and setting its state. This defaults to false
     *  if not present in the XML file.
     */
    public boolean canGetAndSetFMUstate = false;

    /** For the IBM/UCB proposed extension to FMI 2.0, there is a
     *  capability flag canProvideMaxStepSize that indicates that
     *  the FMU implements the procedure fmiGetMaxStepSize().
     */
    public boolean canProvideMaxStepSize = false;

    /** The list of files that were extracted from the .fmu file. */
    public List<File> files;

    /** The fmiVersion, typically the value of the fmiVersion
     * attribute from a .fmu file.  The fmiVersion field is set to 1.0
     * for FMI 1.0.
     */
    public String fmiVersion;

    /** The absolute path to the resources directory.
     *  In FMI-2.0, the fmiInstantiateXXX() method has a fmuResourceLocation
     *  parameter.  This value of this parameter typically starts with "file://"
     *  but may start with "http://", "https://" or "ftp://".
     */
    public String fmuResourceLocation;

    /** The FMI guid, typically the value of the guid attribute from a
     * .fmu file.  The value of guid in the modelDescription.xml file
     * must match the guid in the shared library.
     */
    public String guid;

    /** If true, then the FMU is intended for model exchange, not
     *  co-simulation.
     */
    public boolean modelExchange;

    /** The FMI modelIdentifier, typically the value of the
     * modelIdentifier attribute from a .fmu file.  The
     * modelIdentifier is the basename for the shared library.
     */
    public String modelIdentifier;

    /** The FMI modelName, typically the value of the modelName
     * attribute from a .fmu file.  The modelName may have spaces in
     * it.
     */
    public String modelName;

    /** The list of ScalarVariable elements. */
    public List<FMIScalarVariable> modelVariables = new LinkedList<FMIScalarVariable>();

    /** Number of continuous states. */
    public int numberOfContinuousStates;

    /** Number of event indicators. */
    public int numberOfEventIndicators;

    /** A map from TypeDefinition type name declarations to the
     * defined type name.
     */
    public Map<String, String> typeDefinitions = new HashMap<String, String>();

    /** The capabilities for co-simulation.
     */
    public FMICoSimulationCapabilities capabilities;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Unload the native library and free up any Java references
     *  to memory allocated by the allocate memory callback.
     */
    public void dispose() {
        if (_fmuAllocateMemory != null) {
            // Prevent a memory leak by releasing Memory and Pointer objects to the GC.
            // FIXME: This is wrong!  This releases all instances of Memory and Pointer that have been created!
            // It should only release those for this FMU.
            FMULibrary.FMUAllocateMemory.pointers.clear();
        }
        if (_nativeLibrary != null) {
            _nativeLibrary.dispose();
        }
    }

    /** Return a class that provides a callback function
     *  that allocates memory, but retains a reference
     *  so that the memory does not get gc'd.
     *  @return The class that provides a callback function that
     *  allocates memory.
     */
    public FMULibrary.FMUAllocateMemory getFMUAllocateMemory() {
        return _fmuAllocateMemory;
    }


    /** Return the canonical native library path.
     *  If the shared library names by
     *  {@link org.ptolemy.fmi.FMUFile#fmuSharedLibrary(FMIModelDescription)}
     *  exists, then it is returned.  If it does not exist, then
     *  {@link org.ptolemy.fmi.FMUBuilder#build(File)}
     *  is invoked, which may build the shared library.
     *  @return The canonical native library path.
     *  @exception IOException If the FMU file does not contain binaries
     *   for the current platform.
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
                    IOException exception = new IOException("Failed to build \""
                            + sharedLibraryFile + "\".\nThe build was:\n"
                            + builder.buffer + "\n" + message);
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

    /** Get the native function from the native library.
     *
     *  <p>A FMI 1.0 FMU will have functions like MyModel_fmiGetReal().</p>
     *
     *  <p>A FMI 2.0 FMU that is shipped with C source code or with a
     *  static library, will have functions like MyModel_fmiGetReal().</p>
     *
     *  <p>However, a FMI 2.0 FMU that is shipped with a shared
     *  library (and without C source code), will have functions like
     *  fmiGetReal().</p>
     *
     *  <p>This method tries both formats.  The leading modelIdentifier is
     *  tried first because we believe that FMUs should be shipped with source code.
     *  If the function name with the leading modelIdentifier is not found, then
     *  just the functionName is tried.</p>
     *
     *  @param functionName The name of the function, without a leading underscore.
     *  @return The function.
     *  @exception UnsatisfiedLinkError If the function is not found using either format.
     *  @exception IOException If the native library cannot be found.
     */
    public  Function getFmiFunction(String functionName) throws UnsatisfiedLinkError, IOException {
        // A different implementation would try to guess which
        // function is named depending on if there is C code present
        // and whether there is a dynamic library present.  However,
        // for Java, it is unlikely that a static library is being
        // accessed and determining whether source is present to
        // determine the function name is asinine (cxh, 7/16/13)
        if (_nativeLibrary == null) {
            getNativeLibrary();
        }
        Function function = null;
        String name1 = modelIdentifier + "_" + functionName;
        try {
            function = _nativeLibrary.getFunction(name1);
        } catch (UnsatisfiedLinkError error) {
            try {
                function = _nativeLibrary
                    .getFunction(functionName);
            } catch (UnsatisfiedLinkError error2) {
                UnsatisfiedLinkError linkError = new UnsatisfiedLinkError("Could not find the function, \""
                                               + name1 + "\" or \""
                                               + function + "\" in " + _nativeLibrary);
                linkError.initCause(error);
                throw linkError;
            }
        }
        return function;
    }

    /** Get the native library of C functions for the current platform.
     *  A side effect is that if the native library does not exist, then
     *  {@link org.ptolemy.fmi.FMUBuilder#build(File)} is invoked,
     *  which may build the shared library.
     *  @return The library of functions for the current platform.
     *  @exception IOException If the FMU file does not contain binaries
     *   for the current platform.
     */
    public NativeLibrary getNativeLibrary() throws IOException {
        if (_nativeLibrary != null) {
            return _nativeLibrary;
        }
        String sharedLibrary = getNativeLibraryPath();
        try {
            _nativeLibrary = NativeLibrary
                .getInstance(sharedLibrary);
        } catch (Throwable throwable3) {
            // Java 1.5 does not support
            // IOException(String, Throwable).  We
            // sometimes compile this with gcj, which is
            // Java 1.5
            IOException exception = new IOException("Attempted to build shared "
                                + "library for the current "
                                + "platform because " + sharedLibrary
                                + " was not found.");
                        exception.initCause(throwable3);
                        throw exception;
        }
        return _nativeLibrary;
    }

    /** Return the value of the FMI modelName element.
     *  @return The model name.
     */
    public String toString() {
        return modelName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The NativeLibrary associated with the platform-dependent
     * shared library in the .fmu file.
     */
    private NativeLibrary _nativeLibrary;

    /** A class that allocates memory, but retains a reference
     *  so that the memory does not get gc'd.
     */
    private FMULibrary.FMUAllocateMemory _fmuAllocateMemory = new FMULibrary.FMUAllocateMemory();
}
