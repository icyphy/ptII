/* A FMU ModelDescription.

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
package ptolemy.actor.lib.fmi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// FMUModelDescription

/**
 * An object that represents the fmiModelDescription element of a 
 * Functional Mock-up Interface file.
 * 
 * <p>FMI documentation may be found at
 * <a href="http://www.modelisar.com/fmi.html">http://www.modelisar.com/fmi.html</a>.
 * </p>
 * 
 * @author Christopher Brooks, Edward A. Lee, 
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FmiModelDescription {

    /** Add a ScalarVariable to the list of ModelVariables.
     *  @param modelVariable the ScalarVariable to be added.
     *  #getModelVariables()
     */
    public void addModelVariable(ScalarVariable modelVariable) {
        _modelVariables.add(modelVariable);
    }

    /** Get the fmiVersion.
     *  @return The fmiVersion
     *  @see #setFmiVersion(String)
     */
    public String getFmiVersion() {
        return _fmiVersion;
    }

    /** Get the FMI guid.
     *  @return The guid.
     *  @see #getModelName(String)
     */
    public String getGuid() {
        return _guid;
    }

    /** Get the FMI modelIdentifier.
     *  @return The modelIdentifier.
     *  @see #getModelName(String)
     */
    public String getModelIdentifier() {
        return _modelIdentifier;
    }

    /** Get the FMI modelName.
     *  @return The modelName.
     *  @see #getModelName(String)
     */
    public String getModelName() {
        return _modelName;
    }

    /** Return the list of ModelVariables for this modelDescription.
     *  @return The list of modelVariables.    
     *  @see #addModelVariables(ModelVariable)
     */   
    public List<ScalarVariable> getModelVariables() {
        // FIXME: should we return a copy?
        return _modelVariables;
    }

    /** Set the fmiVersion.
     *  @param version the new fmi version.
     *  @see #getFmiVersion()
     */
    public void setFmiVersion(String version) {
        _fmiVersion = version;
    }

    /** Set the FMI guid.
     *  @param guid The FMI guid
     *  @see #getModelName()
     */
    public void setGuid(String guid) {
        _guid = guid;
    }

    /** Set the FMI modelIdentifier.
     *  @param modelIdentifier The FMI modelIdentifier
     *  @see #getModelName()
     */
    public void setModelIdentifier(String modelIdentifier) {
        _modelIdentifier = modelIdentifier;
    }

    /** Set the FMI modelName.
     *  @param modelName The FMI modelName
     *  @see #getModelName()
     */
    public void setModelName(String modelName) {
        _modelName = modelName;
    }

    /** Set the FMI ModelVariables
     *  @param modelVariables The ModelVariables
     *  @see #getModelVariables()
     */
    public void setModelVariables(List<ScalarVariable> modelVariables) {
        _modelVariables = modelVariables;
    }

    // FIXME: numberOfContinuousStates, numberOfEventIndicators and anything else.

    /** The fmiVersion, typically the value of the fmiVersion
     * attribute from a .fmu file.
     */
    private String _fmiVersion;
    
    /** The FMI guid, typically the value of the guid
     * attribute from a .fmu file.
     */
    private String _guid;

    /** The FMI modelIdentifier, typically the value of the modelIdentifier
     * attribute from a .fmu file.
     */
    private String _modelIdentifier;

    /** The FMI modelName, typically the value of the modelName
     * attribute from a .fmu file.
     */
    private String _modelName;

    private List<ScalarVariable> _modelVariables = new LinkedList<ScalarVariable>();
}
