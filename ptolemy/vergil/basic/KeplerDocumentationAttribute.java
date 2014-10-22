/* A documentation attribute for Kepler.

 Copyright (c) 2007-2014 The Regents of the University of California.
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

package ptolemy.vergil.basic;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.StringUtilities;

/**
 A Documentation attribute for actors.
 This class is used by Kepler so that the DocViewer can access kepler
 specific actor metadata based documentation.
 @author Chad Berkley
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class KeplerDocumentationAttribute extends Attribute implements
        Configurable {

    /** Construct a Kepler documentation attribute.  */
    public KeplerDocumentationAttribute() {
        super();
    }

    /**
     * Construct a Kepler documentation attribute.
     *
     *@param container The container.
     *@param name The name of the Kepler documentation attribute.
     *@exception IllegalActionException If thrown by the superclass.
     *@exception NameDuplicationException  If thrown by the superclass.
     */
    public KeplerDocumentationAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * Construct a Kepler documentation attribute.
     *
     *@param workspace The workspace in which the object is created.
     */
    public KeplerDocumentationAttribute(Workspace workspace) {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add port to the port hashtable.
     *  @param name The name of the port.
     *  @param value A String representing the port.
     *  @exception NameDuplicationException If thrown while creating the port.
     *  @exception Exception If thrown while configuring the port
     */
    public void addPort(String name, String value)
            throws NameDuplicationException, Exception {

        _portHash.put(name, value);
        ConfigurableAttribute port = new ConfigurableAttribute(this, "port:"
                + name);
        port.configure(null, null, value);
    }

    /** Add a property to the property hashtable.
     *  @param name The name of the property.
     *  @param value A string representing the property.
     *  @exception NameDuplicationException If thrown while creating the property
     *  @exception Exception If thrown while configuring the attribute
     *  @see #getProperty(String)
     */
    public void addProperty(String name, String value)
            throws NameDuplicationException, Exception {
        _propertyHash.put(name, value);
        ConfigurableAttribute attribute = new ConfigurableAttribute(this,
                "prop:" + name);
        attribute.configure(null, null, value);
    }

    /** Configure this documentation attribute.
     *  @param base Currently ignored.
     *  @param source The source of this configuration.
     *  @param text The configuration text.
     */
    @Override
    public void configure(java.net.URL base, String source, String text) {
        this.source = source;
        this.text = text;
    }

    /**
     * Create empty fields for the main attribute as well as any
     * params or ports that exist in the target.
     * @param target the namedobj to create the empty attributes for
     */
    public void createEmptyFields(NamedObj target) {
        try {
            /*ConfigurableAttribute authorAtt =*/new ConfigurableAttribute(
                    this, "author");
            /*ConfigurableAttribute versionAtt =*/new ConfigurableAttribute(
                    this, "version");
            /*ConfigurableAttribute descriptionAtt =*/new ConfigurableAttribute(
                    this, "description");
            /*ConfigurableAttribute uldAtt =*/new ConfigurableAttribute(this,
                    "userLevelDocumentation");

            _author = "";
            _version = "";
            _description = "";
            _userLevelDocumentation = "";

            Iterator attributes = target.attributeList().iterator();
            while (attributes.hasNext()) {
                Attribute attribute = (Attribute) attributes.next();
                String attributeName = attribute.getName();
                if (!attributeName.substring(0, 1).equals("_")
                        && !attributeName.equals("KeplerDocumentation")) {
                    _propertyHash.put(attribute.getName(), "");
                }
            }

            if (target instanceof Entity) {
                Iterator ports = ((Entity) target).portList().iterator();
                while (ports.hasNext()) {
                    Port p = (Port) ports.next();
                    _portHash.put(p.getName(), "");
                }
            }

            // Generate a generic change request so the KeplerLSID is updated
            String caStr = "ptolemy.kernel.util.ConfigurableAttribute";

            String updateMoml = "<property name=\"author\" " + "class=\""
                    + caStr + "\" value=\"\"/>";
            MoMLChangeRequest updateRequest = new MoMLChangeRequest(this,
                    target, updateMoml);
            this.requestChange(updateRequest);
        } catch (Exception ex) {
            throw new InternalErrorException(this, ex,
                    "Could not add KeplerDocumentation internal attributes.");
        }
    }

    /**
     * Populate the members of KeplerDocumentationAttribute from
     * another given KeplerDocumentationAttribute.
     * @param documentationAttribute The DocumentationAttribute from which to copy attributes.
     */
    public void createInstanceFromExisting(
            KeplerDocumentationAttribute documentationAttribute) {
        if (documentationAttribute != null
                && documentationAttribute.attributeList() != null) {
            Iterator attributes = documentationAttribute.attributeList()
                    .iterator();
            while (attributes.hasNext()) {
                ConfigurableAttribute attribute = (ConfigurableAttribute) attributes
                        .next();
                String attributeName = attribute.getName();
                if (attributeName.equals("description")) {
                    _description = attribute.getConfigureText();
                } else if (attributeName.equals("author")) {
                    _author = attribute.getConfigureText();
                } else if (attributeName.equals("version")) {
                    _version = attribute.getConfigureText();
                } else if (attributeName.equals("userLevelDocumentation")) {
                    _userLevelDocumentation = attribute.getConfigureText();
                } else if (attributeName.indexOf("port:") != -1) { //add to the port hash
                    String portName = attributeName.substring(
                            attributeName.indexOf(":") + 1,
                            attributeName.length());
                    String portDescription = attribute.getConfigureText();
                    if (portName != null) {
                        if (portDescription == null) {
                            portDescription = "";
                        }
                        _portHash.put(portName, portDescription);
                    }

                } else if (attributeName.indexOf("prop:") != -1) { //add to the prop hash
                    String propertyName = attributeName.substring(
                            attributeName.indexOf(":") + 1,
                            attributeName.length());
                    String propertyDescription = attribute.getConfigureText();
                    if (propertyName != null) {
                        if (propertyDescription == null) {
                            propertyDescription = "";
                        }
                        _propertyHash.put(propertyName, propertyDescription);
                    }
                }
            }
        }
    }

    /** Write a MoML description of this object with the specified
     *  indentation depth and with the specified name substituting
     *  for the name of this object.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use in the exported MoML.
     *  @exception IOException If an I/O error occurs.
     */
    @Override
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        createInstanceFromExisting(this);
        StringBuffer results = new StringBuffer(
                "<property name=\""
                        + name
                        + "\" class=\""
                        + getClassName()
                        + "\">\n"
                        + "<property name=\"description\" class=\"ptolemy.kernel.util.ConfigurableAttribute\">"
                        + "<configure>"
                        + StringUtilities.escapeForXML(_description)
                        + "</configure>"
                        + "</property>\n"
                        + "<property name=\"author\" class=\"ptolemy.kernel.util.ConfigurableAttribute\">"
                        + "<configure>"
                        + StringUtilities.escapeForXML(_author)
                        + "</configure>"
                        + "</property>\n"
                        + "<property name=\"version\" class=\"ptolemy.kernel.util.ConfigurableAttribute\">"
                        + "<configure>"
                        + StringUtilities.escapeForXML(_version)
                        + "</configure>"
                        + "</property>\n"
                        + "<property name=\"userLevelDocumentation\" class=\"ptolemy.kernel.util.ConfigurableAttribute\">"
                        + "<configure>"
                        + StringUtilities.escapeForXML(_userLevelDocumentation)
                        + "</configure>" + "</property>\n");

        Enumeration portKeys = _portHash.keys();
        while (portKeys.hasMoreElements()) {
            String key = (String) portKeys.nextElement();
            String val = (String) _portHash.get(key);
            results.append("<property name=\"port:" + key
                    + "\" class=\"ptolemy.kernel.util.ConfigurableAttribute\">"
                    + "<configure>" + StringUtilities.escapeForXML(val)
                    + "</configure>" + "</property>\n");
        }

        Enumeration propKeys = _propertyHash.keys();
        while (propKeys.hasMoreElements()) {
            String key = (String) propKeys.nextElement();
            String val = (String) _propertyHash.get(key);
            results.append("<property name=\"prop:" + key
                    + "\" class=\"ptolemy.kernel.util.ConfigurableAttribute\">"
                    + "<configure>" + StringUtilities.escapeForXML(val)
                    + "</configure>" + "</property>\n");
        }

        results.append("</property>");
        output.write(results.toString());
    }

    /** Return the author.
     * @return the author
     *  @see #setAuthor(String)
     */
    public String getAuthor() {
        if (_author == null) {
            return "";
        }

        if (!_author.equals("null")) {
            return _author;
        } else {
            return "";
        }
    }

    /** Get the configuration source.
     *  @return The configuration source.
     */
    @Override
    public String getConfigureSource() {
        return source;
    }

    /** Get the configuration text.
     *  @return The configuration text
     */
    @Override
    public String getConfigureText() {
        return text;
    }

    /** Return the description.
     *  @return the description
     *  @see #setDescription(String)
     */
    public String getDescription() {
        if (_description == null) {
            return "";
        }

        if (!_description.equals("null")) {
            return _description;
        } else {
            return "";
        }
    }

    /**
     * Return a docAttribute with the available kepler documentation.
     * Returns null if an error prevents the doc attribute from being
     * created.
     * @param target The container for the DocAttribute
     * @return The DocAttribute.
     */
    public DocAttribute getDocAttribute(NamedObj target) {
        createInstanceFromExisting(this);
        try {
            DocAttribute documentationAttribute = new DocAttribute(
                    target.workspace());
            documentationAttribute.setContainer(target);
            //documentationAttribute.setName("keplerFormattedPTIIDocumentation");
            documentationAttribute.author = new StringAttribute(
                    documentationAttribute, "author");
            documentationAttribute.author.setExpression(_author);
            documentationAttribute.version = new StringAttribute(
                    documentationAttribute, "version");
            documentationAttribute.version.setExpression(_version);
            documentationAttribute.since = new StringAttribute(
                    documentationAttribute, "since");
            documentationAttribute.since.setExpression("");
            documentationAttribute.description = new StringAttribute(
                    documentationAttribute, "description");
            documentationAttribute.description
                    .setExpression(_userLevelDocumentation);

            //add ports and params
            Enumeration ports = _portHash.keys();
            while (ports.hasMoreElements()) {
                String name = (String) ports.nextElement();
                String description = (String) _portHash.get(name);
                StringAttribute attribute = new StringAttribute(
                        documentationAttribute, name + " (port)");
                attribute.setExpression(description);
            }

            Enumeration propItt = _propertyHash.keys();
            while (propItt.hasMoreElements()) {
                String name = (String) propItt.nextElement();
                String description = (String) _propertyHash.get(name);
                StringParameter parameter = new StringParameter(
                        documentationAttribute, name + " (parameter)");
                parameter.setExpression(description);
            }

            return documentationAttribute;
        } catch (Exception ex) {
            throw new InternalErrorException(this, ex,
                    "Error creating docAttribute.");
        }

    }

    /** Return the document class.
     *  @return the document class or the empty string.
     *  @see #setDocClass(String)
     */
    public String getDocClass() {
        if (!_docClass.equals("null")) {
            return _docClass;
        } else {
            return "";
        }
    }

    /** Return the document name.
     *  @return the document name
     *  @see #setDocName(String)
     */
    public String getDocName() {
        if (!_docName.equals("null")) {
            return _docName;
        } else {
            return "";
        }
    }

    /** Return the port documentation.
     *  @param name The name of the port.
     * @return the port documentation.
     */
    public String getPort(String name) {
        return (String) _portHash.get(name);
    }

    /** Return the port hash.
     *  @return the port hash
     *  @see #setPortHash(Hashtable)
     */
    public Hashtable getPortHash() {
        return _portHash;
    }

    /** Return the property documentation.
     *  @param name The name of the property.
     *  @return the property docs
     *  @see #addProperty(String, String)
     */
    public String getProperty(String name) {
        return (String) _propertyHash.get(name);
    }

    /** Return the property hash.
     *  @return the property hash
     *  @see #setPropertyHash(Hashtable)
     */
    public Hashtable getPropertyHash() {
        return _propertyHash;
    }

    /** Return the user level documentation.
     *  @return the user level documentation
     *  @see #setUserLevelDocumentation(String)
     */
    public String getUserLevelDocumentation() {
        if (_userLevelDocumentation == null) {
            return "";
        }

        if (!_userLevelDocumentation.equals("null")) {
            return _userLevelDocumentation;
        } else {
            return "";
        }
    }

    /** Return the version.
     *  @return the version
     *  @see #setVersion(String)
     */
    public String getVersion() {
        if (_version == null) {
            return "";
        }

        if (!_version.equals("null")) {
            return _version;
        } else {
            return "";
        }
    }

    /** Remove a port from the port hashtable.
     *  @param name The name of the port.
     *  @return The value of the port.
     *  @exception IllegalActionException If an error occurs removing the
     *  ConfigurableAttribute.
     *  @exception NameDuplicationException If an error occurs removing the
     *  ConfigurableAttribute.
     *  @see #addPort(String, String)
     */
    public String removePort(String name) throws IllegalActionException,
            NameDuplicationException {
        String retval = (String) _portHash.remove(name);
        if (retval != null) {
            Attribute attribute = getAttribute("port:" + name);
            attribute.setContainer(null);
        }
        return retval;
    }

    /** Remove a property from the property hashtable.
     *  @param name The name of the property.
     *  @return The value of the property.
     *  @exception IllegalActionException If an error occurs removing the
     *  ConfigurableAttribute.
     *  @exception NameDuplicationException If an error occurs removing the
     *  ConfigurableAttribute.
     *  @see #addProperty(String, String)
     */
    public String removeProperty(String name) throws IllegalActionException,
            NameDuplicationException {
        String retval = (String) _propertyHash.remove(name);
        if (retval != null) {
            Attribute attribute = getAttribute("prop:" + name);
            attribute.setContainer(null);
        }
        return retval;
    }

    /** Set the author.
     *  @param author The author.
     *  @see #getAuthor()
     */
    public void setAuthor(String author) {
        _author = author;
    }

    /** Set the description.
     *  @param description The description.
     *  @see #getDescription()
     */
    public void setDescription(String description) {
        _description = description;
    }

    /** Set the name of this docClass.
     *  @param className The name of this docClass.
     *  @see #getDocClass()
     */
    public void setDocClass(String className) {
        _docClass = className;
    }

    /** Set the name of this document.
     *  @param name The name of this document.
     *  @see #getDocName()
     */
    public void setDocName(String name) {
        _docName = name;
    }

    /** Set the port hash.
     *  @param portHash The port hash.
     *  @see #getPortHash()
     */
    public void setPortHash(Hashtable portHash) {
        _portHash = portHash;
    }

    /** Set the property hashtable.
     *  @param propertyHash The property hashtable.
     *  @see #getPropertyHash()
     *
     *  FIXME: need to remove all existing ConfigurableAttributes
     *  for properties and add new ones for new hash table.
     */
    public void setPropertyHash(Hashtable propertyHash) {
        _propertyHash = propertyHash;
    }

    /** Set the user level documentation.
     *  @param userLevelDocumentation The user level documentation.
     *  @see #getUserLevelDocumentation()
     */
    public void setUserLevelDocumentation(String userLevelDocumentation) {
        _userLevelDocumentation = userLevelDocumentation;
    }

    /** Set the version.
     *  @param version The version.
     *  @see #getVersion()
     */
    public void setVersion(String version) {
        _version = version;
    }

    /**
     * Exports this documentation attribute as docML.
     * @return The docML
     */
    public String toDocML() {
        createInstanceFromExisting(this);
        StringBuffer results = new StringBuffer(
                "<?xml version=\"1.0\" standalone=\"yes\"?>\n"
                        + "<!DOCTYPE doc PUBLIC \"-//UC Berkeley//DTD DocML 1//EN\""
                        + "\"http://ptolemy.eecs.berkeley.edu/xml/dtd/DocML_1.dtd\">\n"
                        + "<doc name=\"" + _docName + "\" class=\"" + _docClass
                        + "\">\n" + "<description>\n" + _userLevelDocumentation
                        + "\n</description>\n" + "<author>" + _author
                        + "</author>\n");

        Enumeration ports = _portHash.keys();
        while (ports.hasMoreElements()) {
            String name = (String) ports.nextElement();
            String desc = (String) _portHash.get(name);
            results.append("<port name=\"" + name + "\">" + desc + "</port>\n");
        }

        Enumeration propItt = _propertyHash.keys();
        while (propItt.hasMoreElements()) {
            String name = (String) propItt.nextElement();
            String desc = (String) _propertyHash.get(name);
            results.append("<property name=\"" + name + "\">" + desc
                    + "</property>\n");
        }

        results.append("</doc>\n");
        return results.toString();
    }

    /**
     * Method for configurable.
     * In this class, we do nothing.
     */
    @Override
    public void updateContent() throws InternalErrorException {
        //do nothing
    }

    /** Update the documentation fields of this object from another
     * KeplerDocumentationAttribute. A documentation field in
     * this object is updated if it is empty and the corresponding field
     * in the given object is not empty. However, if a field does not
     * exist in this object, but is present in the given object, the
     * field is *not* created in this object.
     *
     * @param oldDoc
     *            The KeplerDocumentationAttribute from which to copy
     *            attributes.
     * @param printWhenReplacing
     *            If true, print when the values are overwritten.
     *
     * @exception Exception
     *             if there is an error updating the fields.
     */
    public void updateFromExisting(KeplerDocumentationAttribute oldDoc,
            boolean printWhenReplacing) throws Exception {

        if (oldDoc != null && oldDoc.attributeList() != null) {
            Iterator<?> attributes = oldDoc.attributeList().iterator();
            while (attributes.hasNext()) {
                ConfigurableAttribute attribute = (ConfigurableAttribute) attributes
                        .next();
                String attributeName = attribute.getName();
                boolean replaced = false;
                if (attributeName.equals("description")) {
                    String oldDescription = attribute.getConfigureText();
                    if (_isEmpty(_description) && !_isEmpty(oldDescription)) {
                        _description = oldDescription;
                        replaced = true;
                    }
                } else if (attributeName.equals("author")) {
                    String oldAuthor = attribute.getConfigureText();
                    if (_isEmpty(_author) && !_isEmpty(oldAuthor)) {
                        _author = oldAuthor;
                        replaced = true;
                    }
                } else if (attributeName.equals("version")) {
                    String oldVersion = attribute.getConfigureText();
                    if (_isEmpty(_version) && !_isEmpty(oldVersion)) {
                        _version = oldVersion;
                        replaced = true;
                    }
                } else if (attributeName.equals("userLevelDocumentation")) {
                    String oldUserLevelDocumentation = attribute
                            .getConfigureText();
                    if (_isEmpty(_userLevelDocumentation)
                            && !_isEmpty(oldUserLevelDocumentation)) {
                        _userLevelDocumentation = oldUserLevelDocumentation;
                        replaced = true;
                    }
                } else if (attributeName.indexOf("port:") != -1) { // add to the
                    // port hash
                    String portName = attributeName.substring(
                            attributeName.indexOf(":") + 1,
                            attributeName.length());
                    String portDescription = attribute.getConfigureText();
                    if (portName != null) {
                        if (portDescription == null) {
                            portDescription = "";
                        }
                        String newPortDoc = (String) _portHash.get(portName);
                        if (newPortDoc != null && _isEmpty(newPortDoc)
                                && !_isEmpty(portDescription)) {
                            // Attribute clonedAttribute = (Attribute)
                            // attribute.clone();
                            _portHash.put(portName, portDescription);
                            replaced = true;
                        }
                    }

                } else if (attributeName.indexOf("prop:") != -1) { // add to the
                    // prop hash
                    String propertyName = attributeName.substring(
                            attributeName.indexOf(":") + 1,
                            attributeName.length());
                    String propertyDescription = attribute.getConfigureText();
                    if (propertyName != null) {
                        if (propertyDescription == null) {
                            propertyDescription = "";
                        }
                        String newPropDoc = (String) _propertyHash
                                .get(propertyName);
                        if (newPropDoc != null && _isEmpty(newPropDoc)
                                && !_isEmpty(propertyDescription)) {
                            _propertyHash
                                    .put(propertyName, propertyDescription);
                            replaced = true;
                        }
                    }
                }

                if (replaced) {

                    if (printWhenReplacing) {
                        System.out.println("WARNING: using old docs for "
                                + attributeName
                                + " since the new ones appear empty.");
                    }
                    // Update the attribute too, creating one if it does not exist.
                    ConfigurableAttribute myAttribute = (ConfigurableAttribute) getAttribute(attributeName);
                    if (myAttribute == null) {
                        myAttribute = new ConfigurableAttribute(this,
                                attributeName);
                    }
                    myAttribute.configure(attribute.getBase(),
                            attribute.getConfigureSource(),
                            attribute.getConfigureText());
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** A utility method to determine if a field's string is empty.
     * @return Returns true if the field is null or the content appears to be
     *         empty.
     */
    private static boolean _isEmpty(String string) {

        if (string == null) {
            return true;
        }
        String trimmed = string.trim();
        if (trimmed.isEmpty() || trimmed.equalsIgnoreCase("null")) {
            return true;
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    //members for Configurable
    private String source;

    private String text;

    //members for DocumenationAttribute
    private String _docName;

    private String _docClass;

    private String _description;

    private String _author;

    private String _version;

    private String _userLevelDocumentation;

    private Hashtable _portHash = new Hashtable();

    private Hashtable _propertyHash = new Hashtable();
}
