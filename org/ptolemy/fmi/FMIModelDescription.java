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
    // This file intentionally has no getters and setters so that we
    // can easily access the elements in a very lightweight manner.

    // The files field is not in the xml, but as we pass around this object,
    // add a field here for easier access.
    
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

    /** The NativeLibrary associated with the platform-dependent
     * shared library in the .fmu file.
     */
    public NativeLibrary nativeLibrary;

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

    /** Return the value of the FMI modelName element.
     *  @return The model name.
     */
    public String toString() {
        return modelName;
    }
}
