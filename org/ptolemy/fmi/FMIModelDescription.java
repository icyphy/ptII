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

    /** The list of files that were extracted from the .fmu file. */
    public List<File> files;

    /** The fmiVersion, typically the value of the fmiVersion
     * attribute from a .fmu file.  The fmiVersion field is set to 1.0
     * for FMI 1.0.
     */
    public String fmiVersion;

    /** The FMI guid, typically the value of the guid attribute from a
     * .fmu file.  The value of guid in the modelDescription.xml file
     * must match the guid in the shared library.
     */
    public String guid;

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

    /** Get the native library of C functions for the current platform.
     *  @return The library of functions for the current platform.
     *  @exception IOException If the FMU file does not contain binaries
     *   for the current platform.
     */
    public NativeLibrary getNativeLibrary() throws IOException {
        if (_nativeLibrary != null) {
            return _nativeLibrary;
        }
        // The following should not be done until at least the modelIdentifier
        // has been set in fmiModelDescription.
        if (modelIdentifier == null || modelIdentifier.trim().equals("")) {
            // FIXME: Don't use runtime exception.
            throw new RuntimeException("No modelIdentifier is given");
        }

        // Get the name of the shared library file for the current platform.
        String sharedLibrary = FMUFile.fmuSharedLibrary(this);

        // Load the shared library
        try {
            _nativeLibrary = NativeLibrary.getInstance(sharedLibrary);
        } catch (Throwable throwable) {
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
            File sharedLibraryFile = new File(sharedLibrary);
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
                            + builder.buffer + "\n" + message
			    + "\nThe initial exception was: " + throwable);
		    exception.initCause(throwable2);
		    throw exception;

                }
                if (!isBuildOK) {
		    // Java 1.5 does not support IOException(String, Throwable).
		    // We sometimes compile this with gcj, which is Java 1.5
                    IOException exception = new IOException("It was not possible to build \""
                            + sharedLibraryFile + "\": " + builder.buffer
                            + "\n" + message);
		    exception.initCause(throwable);
		    throw exception;
                } else {
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
                                + " was not found.  "
                                + "However, loading the library failed?\n"
                                + "The original error was: " + message + "\n"
							      + throwable);
			exception.initCause(throwable3);
			throw exception;
                    }
                }
            }
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
}
