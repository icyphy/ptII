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

import java.util.LinkedList;
import java.util.List;

///////////////////////////////////////////////////////////////////
//// FMUModelDescription

/**
 * An object that represents the fmiModelDescription element of a 
 * Functional Mock-up Interface (FMI) file.
 * 
 * <p>A Functional Mock-up Unit file is a .fmu file in zip format that contains a 
 * .xml file named "modelDescription.xml".  This class is a representation
 * of the elements of that file.</p>
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

    // FIXME: numberOfContinuousStates, numberOfEventIndicators and anything else.

    /** The fmiVersion, typically the value of the fmiVersion
     * attribute from a .fmu file.
     */
    public String fmiVersion;
    
    /** The FMI guid, typically the value of the guid
     * attribute from a .fmu file.
     */
    public String guid;

    /** The FMI modelIdentifier, typically the value of the modelIdentifier
     * attribute from a .fmu file.
     */
    public String modelIdentifier;

    /** The FMI modelName, typically the value of the modelName
     * attribute from a .fmu file.
     */
    public String modelName;

    public List<FMIScalarVariable> modelVariables = new LinkedList<FMIScalarVariable>();
}
