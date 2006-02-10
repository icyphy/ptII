/* A parser for DocML (Doc Markup Language).

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.vergil.actor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Instantiable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.vergil.basic.DocAttribute;

import com.microstar.xml.HandlerBase;
import com.microstar.xml.XmlException;
import com.microstar.xml.XmlParser;

//////////////////////////////////////////////////////////////////////////
//// DocManager

/**
 A manager for documentation for an associated Ptolemy II object.
 The constructor specifies the associated Ptolemy II object, and
 then various methods provide access to portions of the documentation.
 For example, the getDescription() method returns a description of
 the object. The getPortDoc() method returns a description of the
 specified port.
 <p>
 The documentation is constructed by a multi-tiered method.
 The first level of information is provided by an
 attribute named DOC_ATTRIBUTE_NAME contained by the object.
 The second level of information is provided by an XML file
 in the same package as the class of the associated object.
 The name of the XML file is "xDoc.xml", where x is the name
 of the class of the object.
 The third level of information is provided by an XML file
 associated with the base class of the associated object.
 The remaining levels are provided by searching up the
 inheritance hierarchy.
 When a method of the DocManager class is invoked to get
 documentation information, this class looks first in the
 first tier for the information. If the information is
 not present in the first tier, then it looks in the second
 tier, etc. If the information is not present in any tier,
 then it returns a string indicating that there is no
 information. Except for the first tier, the
 documentation information is constructed
 lazily, only when the methods to access the information
 are invoked, and only if the first tier has not
 provided the information.
 <p>
 If the information is found but is malformed, then
 all information methods return a description of the error.
 <p>
 At all tiers, the documentation information is given in XML
 with a specified DTD.
 <p>
 A doc file should be an XML file beginning with
 <pre>
 &lt;?xml version="1.0" standalone="yes"?&gt;
 &lt;!DOCTYPE doc PUBLIC "-//UC Berkeley//DTD DocML 1//EN"
 "http://ptolemy.eecs.berkeley.edu/xml/dtd/DocML_1.dtd"&gt;
 </pre>
 and should then have a top-level element of the form
 <pre>
 &lt;doc name="<i>actorName</i>" class="<i>actorClass</i>"&gt;
 </pre>
 The main description is text included within the description
 element, as in
 <pre>
 &lt;description&gt;
 <i>description here</i>
 &lt;/description&gt;
 </pre>
 The description can include HTML formatting, although any
 &lt; and &gt; should be escaped and represented as &amp;lt;
 and &amp;gt;.
 <p>
 Additional information can be provided in the author, version,
 since, Pt.ProposedRating, and Pt.AcceptedRating elements.
 These are, like the description, simple text that gets rendered
 (and HTML formatted) in the documentation.
 <p>
 Documentation for ports and parameters is given using the
 following forms:
 <pre>
 &lt;port name="<i>portName</i>">
 <i>documentation</i>
 &lt/port&gt;
 &lt;property name="<i>parameterName</i>">
 <i>documentation</i>
 &lt/property&gt;
 </pre>
 The use of the "property" keyword matches MoML.
 
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class DocManager extends HandlerBase {

    /** Construct a manager to handle documentation for the specified target.
     *  @param target The object to be documented.
     */
    public DocManager(NamedObj target) {
        super();
        _target = target;
        _targetClass = target.getClass();
        _className = target.getClassName();
        _isInstanceDoc = false;
        try {
            List docAttributes = _target.attributeList(DocAttribute.class);
            // Get the last doc attribute.
            if (docAttributes.size() > 0) {
                DocAttribute instanceDoc = (DocAttribute) docAttributes
                        .get(docAttributes.size() - 1);
                // Populate fields from the attribute.
                String descriptionValue = instanceDoc.description.stringValue();
                if (!descriptionValue.trim().equals("")) {
                    _isInstanceDoc = true;
                    _description = descriptionValue;
                }
                /* No rating fields in instance documentation.
                 String acceptedRatingValue = instanceDoc.acceptedRating.getExpression();
                 if (!acceptedRatingValue.trim().equals("")) {
                 _isInstanceDoc = true;
                 _ptAcceptedRating = acceptedRatingValue;
                 }
                 */
                String authorValue = instanceDoc.author.getExpression();
                if (!authorValue.trim().equals("")) {
                    _isInstanceDoc = true;
                    _author = authorValue;
                }
                /* No rating fields in instance documentation.
                 String proposedRatingValue = instanceDoc.proposedRating.getExpression();
                 if (!proposedRatingValue.trim().equals("")) {
                 _isInstanceDoc = true;
                 _ptProposedRating = proposedRatingValue;
                 }
                 */
                String sinceValue = instanceDoc.since.getExpression();
                if (!sinceValue.trim().equals("")) {
                    _isInstanceDoc = true;
                    _since = sinceValue;
                }
                String versionValue = instanceDoc.version.getExpression();
                if (!versionValue.trim().equals("")) {
                    _isInstanceDoc = true;
                    _version = versionValue;
                }

                // Next look for attributes.
                Iterator attributes = target.attributeList(Settable.class)
                        .iterator();
                while (attributes.hasNext()) {
                    NamedObj attribute = (NamedObj) attributes.next();
                    if (((Settable) attribute).getVisibility() != Settable.NONE) {
                        String attributeDoc = instanceDoc
                                .getParameterDoc(attribute.getName());
                        if (attributeDoc != null
                                && !attributeDoc.trim().equals("")) {
                            _isInstanceDoc = true;
                            _properties.put(attribute.getName(), attributeDoc);
                        }
                    }
                }
                // Next look for ports.
                if (target instanceof Entity) {
                    Iterator ports = ((Entity) target).portList().iterator();
                    while (ports.hasNext()) {
                        Port port = (Port) ports.next();
                        String portDoc = instanceDoc.getPortDoc(port.getName());
                        if (portDoc != null && !portDoc.trim().equals("")) {
                            _isInstanceDoc = true;
                            _ports.put(port.getName(), portDoc);
                        }
                    }
                }
            }
        } catch (IllegalActionException e) {
            _exception = "Error evaluating DocAttribute parameter:\n" + e;
        }
    }

    /** Construct a manager to handle documentation for the specified target
     *  class.
     *  @param targetClass The class to be documented.
     */
    public DocManager(Class targetClass) {
        super();
        _targetClass = targetClass;
        _className = _targetClass.getName();
        _isInstanceDoc = false;
    }

    /** Construct a manager for documentation at the specified URL.
     *  @param url The URL.
     */
    public DocManager(URL url) {
        super();
        _isInstanceDoc = false;
        try {
            parse(null, url.openStream());
        } catch (Exception ex) {
            ex.printStackTrace();
            _exception = "Error reading URL: " + url.toExternalForm()
                    + "\n<pre>\n" + ex + "\n</pre>\n";
        }
        _docFileHasBeenRead = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle an attribute assignment that is part of an XML element.
     *  This method is called prior to the corresponding startElement()
     *  call, so it simply accumulates attributes in a hashtable for
     *  use by startElement().
     *  @param name The name of the attribute.
     *  @param value The value of the attribute, or null if the attribute
     *   is <code>#IMPLIED</code> and not specified.
     *  @param specified True if the value is specified, false if the
     *   value comes from the default value in the DTD rather than from
     *   the XML file.
     *  @exception XmlException If the name or value is null.
     */
    public void attribute(String name, String value, boolean specified)
            throws XmlException {
        if (name == null) {
            throw new XmlException("Attribute has no name",
                    _currentExternalEntity(), _parser.getLineNumber(), _parser
                            .getColumnNumber());
        }

        // NOTE: value may be null if attribute default is #IMPLIED.
        if (value != null) {
            _attributes.put(name, value);
        }
    }

    /** Handle character data.  In this implementation, the
     *  character data is accumulated in a buffer until the
     *  end element.
     *  &AElig;lfred will call this method once for each chunk of
     *  character data found in the contents of elements.  Note that
     *  the parser may break up a long sequence of characters into
     *  smaller chunks and call this method once for each chunk.
     *  @param chars The character data.
     *  @param offset The starting position in the array.
     *  @param length The number of characters available.
     */
    public void charData(char[] chars, int offset, int length) {
        _currentCharData.append(chars, offset, length);
    }

    /** End the document.  In this implementation, do nothing.
     *  &AElig;lfred will call this method once, when it has
     *  finished parsing the XML document.
     *  It is guaranteed that this will be the last method called.
     */
    public void endDocument() throws Exception {
    }

    /** End an element.
     *  &AElig;lfred will call this method at the end of each element
     *  (including EMPTY elements).
     *  @param elementName The element type name.
     */
    public void endElement(String elementName) throws Exception {
        if (elementName.equals("author")) {
            _author = _currentCharData.toString();
        } else if (elementName.equals("description")) {
            _description = _currentCharData.toString();
        } else if (elementName.equals("port")) {
            _ports.put(_name, _currentCharData.toString());
        } else if (elementName.equals("property")) {
            _properties.put(_name, _currentCharData.toString());
        } else if (elementName.equals("Pt.AcceptedRating")) {
            _ptAcceptedRating = _currentCharData.toString();
        } else if (elementName.equals("Pt.ProposedRating")) {
            _ptProposedRating = _currentCharData.toString();
        } else if (elementName.equals("since")) {
            _since = _currentCharData.toString();
        } else if (elementName.equals("version")) {
            _version = _currentCharData.toString();
        }
    }

    /** Indicate a fatal XML parsing error.
     *  &AElig;lfred will call this method whenever it encounters
     *  a serious error.  This method simply throws an XmlException.
     *  @param message The error message.
     *  @param systemID The URI of the entity that caused the error.
     *  @param line The approximate line number of the error.
     *  @param column The approximate column number of the error.
     *  @exception XmlException If called.
     */
    public void error(String message, String systemID, int line, int column)
            throws XmlException {
        throw new XmlException(message, _currentExternalEntity(), line, column);
    }

    /** Return the Pt.AcceptedRating field, or null
     *  if none has been given. Note that unlike some of the other
     *  fields, this does not delegate to the next tier if no
     *  since field has been given.
     *  @return The Pt.AcceptedRating field.
     */
    public String getAcceptedRating() {
        if (_ptAcceptedRating == null) {
            _readDocFile();
        }
        return _ptAcceptedRating;
    }

    /** Return the author field, or the string "No author given"
     *  if none has been given. Note that unlike some of the other
     *  fields, this does not delegate to the next tier if no
     *  author has been given.
     *  @return The author field.
     */
    public String getAuthor() {
        if (_author == null) {
            _readDocFile();
            if (_author == null) {
                return "No author given";
            }
        }
        return _author;
    }

    /** Return the class name, or null if none has been given.
     *  @return The class name.
     */
    public String getClassName() {
        if (_className == null && _exception != null) {
            return _exception;
        }
        return _className;
    }

    /** Return the description, or null if none has been given.
     *  @return The description.
     */
    public String getDescription() {
        if (_exception != null) {
            return _exception;
        } else if (_description == null) {
            _readDocFile();
            if (_description == null) {
                _createNextTier();
                if (_nextTier != null) {
                    return _nextTier.getDescription();
                } else {
                    return "No description";
                }
            }
        }
        return _description;
    }

    /** Return next tier, if there is one.
     *  If this is an instance, then the next tier
     *  is the documentation for the class. If it is a
     *  class, then the next tier is the documentation for
     *  the superclass.
     *  @return The next tier, or null if there isn't one.
     */
    public DocManager getNextTier() {
        _createNextTier();
        return _nextTier;
    }

    /** Return the documentation for the specified port, or null
     *  if there is none.
     *  @param name The name of the port.
     *  @return The documentation for the specified port, or null
     *   if there is none.
     */
    public String getPortDoc(String name) {
        _readDocFile();
        String result = (String) _ports.get(name);
        if (result == null) {
            result = (String) _ports.get(name + " (port)");
            if (result == null) {
                _createNextTier();
                if (_nextTier != null) {
                    return _nextTier.getPortDoc(name);
                }
            }
        }
        return result;
    }

    /** Return the documentation for the specified property
     *  (parameter or attribute), or null if there is none.
     *  @param name The name of the property.
     *  @return The documentation for the specified property, or null
     *   if there is none.
     */
    public String getPropertyDoc(String name) {
        _readDocFile();
        String result = (String) _properties.get(name);
        if (result == null) {
            result = (String) _properties.get(name + " (parameter)");
            if (result == null) {
                _createNextTier();
                if (_nextTier != null) {
                    return _nextTier.getPropertyDoc(name);
                }
            }
        }
        return result;
    }

    /** Return the Pt.ProposedRating field, or null
     *  if none has been given. Note that unlike some of the other
     *  fields, this does not delegate to the next tier if no
     *  since field has been given.
     *  @return The Pt.ProposedRating field.
     */
    public String getProposedRating() {
        if (_ptProposedRating == null) {
            _readDocFile();
        }
        return _ptProposedRating;
    }

    /** Return the since field, or null
     *  if none has been given. Note that unlike some of the other
     *  fields, this does not delegate to the next tier if no
     *  since field has been given.
     *  @return The since field.
     */
    public String getSince() {
        if (_since == null) {
            _readDocFile();
        }
        return _since;
    }

    /** Return the class of the target.
     *  @return The class of the target.
     */
    public Class getTargetClass() {
        return _targetClass;
    }

    /** Return the version field, or null
     *  if none has been given. Note that unlike some of the other
     *  fields, this does not delegate to the next tier if no
     *  version has been given. If the version field is the standard
     *  CVS version, then return only the version number and date.
     *  @return The version field.
     */
    public String getVersion() {
        if (_version == null) {
            _readDocFile();
        }
        if (_version != null) {
            if (_version.startsWith("$Id:")) {
                // Standard CVS version. Extract the version number and date.
                int index = _version.indexOf(",v ");
                if (index > 4) {
                    String tail = _version.substring(index + 3);
                    // Find the first space after the start.
                    index = tail.indexOf(" ");
                    if (index > 0) {
                        // Find the second space.
                        index = tail.indexOf(" ", index + 1);
                        if (index > 0) {
                            _version = tail.substring(0, index);
                        }
                    }
                }
            }
        }
        return _version;
    }

    /** Return "see also" information. This includes a link
     *  to the javadoc documentation, the source code, and the
     *  superclass information.
     *  @return The "see also" information.
     */
    public String getSeeAlso() {
        StringBuffer result = new StringBuffer();

        // See whether there is Javadoc, and link to it if there is.
        result.append("<b>See Also:</b><ul>\n");

        // The class name is either the name of the target class
        // or the class name provided by the target.
        String className;
        if (_target == null) {
            className = _targetClass.getName();
        } else {
            className = _target.getClassName();
        }
        String docName = "doc.codeDoc." + className;
        if (_isInstanceDoc) {
            // Create a link to the class documentation,
            // if there is some. First not that the superclass
            // may itself be an instance with instance documentation.
            // In that case, the hyperlink is special and must be
            // intercepted by the DocViewer class.
            if (_target instanceof Instantiable
                    && ((Instantiable) _target).getParent() != null
                    && ((NamedObj) ((Instantiable) _target).getParent())
                            .attributeList(DocAttribute.class).size() > 0) {
                result
                        .append("<li><a href=\"#parentClass\">Class documentation</a></li>");
            }
            try {
                URL toRead = getClass().getClassLoader().getResource(
                        docName.replace('.', '/') + ".xml");

                if (toRead != null) {
                    result.append("<li><a href=\"" + toRead.toExternalForm()
                            + "\">Class documentation</a></li>");
                } else {
                    // Link to the Javadoc instead for the class.
                    try {
                        toRead = getClass().getClassLoader().getResource(
                                docName.replace('.', '/') + ".html");

                        if (toRead != null) {
                            result.append("<li><a href=\""
                                    + toRead.toExternalForm()
                                    + "\">Class documentation</a></li>");
                        }
                    } catch (Exception ex) {
                        result.append("<li>Error opening javadoc file:\n<pre>"
                                + ex + "/n</pre></li>\n");
                    }
                }
            } catch (Exception ex) {
                result.append("<li>Error opening javadoc file:\n<pre>" + ex
                        + "/n</pre></li>\n");
            }
        } else {
            try {
                URL toRead = getClass().getClassLoader().getResource(
                        docName.replace('.', '/') + ".html");

                if (toRead != null) {
                    result.append("<li><a href=\"" + toRead.toExternalForm()
                            + "\">Javadoc documentation</a></li>");
                } else {
                    // FIXME: Make this a hyperlink to a doc on how
                    // to create the javadocs.
                    result.append("<li>No javadocs found</li>");
                }
            } catch (Exception ex) {
                result.append("<li>Error opening javadoc file:\n<pre>" + ex
                        + "/n</pre></li>\n");
            }

            // See whether the base class has a doc file, and if so, link to it.
            // If not, try to link to the Javadoc for the base class.
            try {
                String baseClassName = _targetClass.getSuperclass().getName();
                docName = "doc.codeDoc." + baseClassName;
                URL toRead = getClass().getClassLoader().getResource(
                        docName.replace('.', '/') + ".xml");

                // Display only the unqualified class name for compactness.
                int lastDot = baseClassName.lastIndexOf(".");
                if (lastDot >= 0) {
                    baseClassName = baseClassName.substring(lastDot + 1);
                }
                if (toRead != null) {
                    result.append("<li><a href=\"" + toRead.toExternalForm()
                            + "\">Base class (" + baseClassName + ")</a></li>");
                } else {
                    // Try the Javadoc.
                    try {
                        toRead = getClass().getClassLoader().getResource(
                                docName.replace('.', '/') + ".html");

                        if (toRead != null) {
                            result.append("<li><a href=\""
                                    + toRead.toExternalForm()
                                    + "\">Base class Javadoc (" + baseClassName
                                    + ")</a></li>");
                        }
                    } catch (Exception ex) {
                        // Ignore and leave blank.
                    }
                }
            } catch (Exception ex) {
                result.append("<li>Error opening javadoc file:\n<pre>" + ex
                        + "/n</pre></li>\n");
            }

            // Link to the source code, if present.
            try {
                URL toRead = getClass().getClassLoader().getResource(
                        className.replace('.', '/') + ".java");

                if (toRead != null) {
                    result.append("<li><a href=\"" + toRead.toExternalForm()
                            + "\">Source code</a></li>");
                }
            } catch (Exception ex) {
                // Do not report anything.
            }
        }

        // FIXME: Need see also fields from the doclet analysis.
        // FIXME: Include demos? How?

        try {
            URL toRead = getClass().getClassLoader().getResource(
                    "doc/codeDoc/" + className.replace('.', '/') + "Idx.htm");
            if (toRead != null) {
                result.append("<li><a href=\"" + toRead.toExternalForm()
                        + "\">Demo Usage</a></li>");
            } else {
                result.append("<li>Not used in any demos</li>");
            }
        } catch (Exception ex) {
            // Do not report anything.

        }
        result.append("</ul>");
        return result.toString();
    }

    /** Return true if an exception was encountered parsing
     *  the DocML data.
     *  @return True if an exception was encountered.
     */
    public boolean hadException() {
        return _exception != null;
    }

    /** Return true if the primary source of documentation is
     *  the instance. That is, return true if the target has
     *  an instance of DocAttribute in it, and at least one
     *  of the fields of the DocAttribute is not empty.
     *  @return True if this documents an instance (vs. a class).
     */
    public boolean isInstanceDoc() {
        return _isInstanceDoc;
    }

    /** Return true if the target class is a subclass of Attribute
     *  that has a two-argument constructor compatible where the
     *  first argument is a CompositeEntity and the second is a
     *  String. This will return true if the target is itself
     *  an instance of Attribute or a subclass.
     *  @return True if the target is an instantiable attribute.
     */
    public boolean isTargetInstantiableAttribute() {
        if (_target != null) {
            return _target instanceof Attribute;
        } else {
            Class targetClass = _targetClass;
            while (targetClass != null) {
                if (targetClass.equals(Attribute.class)) {
                    return _hasMoMLConstructor();
                }
                targetClass = targetClass.getSuperclass();
            }
            return false;
        }
    }

    /** Return true if the target class is a subclass of Entity
     *  that has a two-argument constructor compatible where the
     *  first argument is a CompositeEntity and the second is a
     *  String. This will return true if the target is itself
     *  an instance of Entity or a subclass.
     *  @return True if the target is an instantiable entity.
     */
    public boolean isTargetInstantiableEntity() {
        if (_target != null) {
            return _target instanceof Entity;
        } else {
            Class targetClass = _targetClass;
            while (targetClass != null) {
                if (targetClass.equals(Entity.class)) {
                    return _hasMoMLConstructor();
                }
                targetClass = targetClass.getSuperclass();
            }
            return false;
        }
    }

    /** Return true if the target class is a subclass of Port
     *  that has a two-argument constructor compatible where the
     *  first argument is a CompositeEntity and the second is a
     *  String. This will return true if the target is itself
     *  an instance of Port or a subclass.
     *  @return True if the target is an instantiable port.
     */
    public boolean isTargetInstantiablePort() {
        if (_target != null) {
            return _target instanceof Port;
        } else {
            Class targetClass = _targetClass;
            while (targetClass != null) {
                if (targetClass.equals(Port.class)) {
                    return _hasMoMLConstructor();
                }
                targetClass = targetClass.getSuperclass();
            }
            return false;
        }
    }

    /** Parse the given stream as a DocML file.
     *  For example, a user might use this method as follows:
     *  <pre>
     *     DocManager parser = new DocManager();
     *     URL xmlFile = new URL(null, docURL);
     *     parser.parse(xmlFile.openStream());
     *  </pre>
     *  A variety of exceptions might be thrown if the parsed
     *  data does not represent a valid DocML file.
     *  @param input The stream from which to read XML.
     *  @exception Exception If the parser fails.
     */
    public void parse(URL base, InputStream input) throws Exception {
        parse(base, new InputStreamReader(input));
    }

    /** Parse the given stream as a DocML file.
     *  A variety of exceptions might be thrown if the parsed
     *  data does not represent a valid DocML file.
     *  @param reader The stream from which to read XML.
     *  @exception Exception If the parser fails.
     */
    public void parse(URL base, Reader reader) throws Exception {
        _parser.setHandler(this);

        Reader buffered = new BufferedReader(reader);

        if (base == null) {
            _parser.parse(null, null, buffered);
        } else {
            _parser.parse(base.toExternalForm(), null, buffered);
        }
    }

    /** Parse the given text as DocML.
     *  A variety of exceptions might be thrown if the parsed
     *  data does not represent valid DocML data.
     *  @param text The DocML data.
     *  @exception Exception If the parser fails.
     */
    public void parse(String text) throws Exception {
        parse(null, new StringReader(text));
    }

    /** Resolve an external entity. If the first argument is the
     *  name of the DocML PUBLIC DTD ("-//UC Berkeley//DTD DocML 1//EN"),
     *  then return a StringReader
     *  that will read the locally cached version of this DTD
     *  (the public variable DocML_DTD_1). Otherwise, return null,
     *  which has the effect of deferring to &AElig;lfred for
     *  resolution of the URI.  Derived classes may return a
     *  a modified URI (a string), an InputStream, or a Reader.
     *  In the latter two cases, the input character stream is
     *  provided.
     *  @param publicID The public identifier, or null if none was supplied.
     *  @param systemID The system identifier.
     *  @return Null, indicating to use the default system identifier.
     */
    public Object resolveEntity(String publicID, String systemID) {
        if ((publicID != null)
                && publicID.equals("-//UC Berkeley//DTD DocML 1//EN")) {
            // This is the generic DocML DTD.
            return new StringReader(DocML_DTD_1);
        } else {
            return null;
        }
    }

    /** Start a document.  This method is called just before the parser
     *  attempts to read the first entity (the root of the document).
     *  It is guaranteed that this will be the first method called.
     */
    public void startDocument() {
        _attributes = new HashMap();
    }

    /** Start an element.
     *  This is called at the beginning of each XML
     *  element.  By the time it is called, all of the attributes
     *  for the element will already have been reported using the
     *  attribute() method.  Unrecognized elements are ignored.
     *  @param elementName The element type name.
     *  @exception XmlException If the element produces an error
     *   in constructing the model.
     */
    public void startElement(String elementName) throws XmlException {
        try {
            // NOTE: The elements are alphabetical below...
            if (elementName.equals("author")
                    || elementName.equals("description")
                    || elementName.equals("Pt.AcceptedRating")
                    || elementName.equals("Pt.ProposedRating")
                    || elementName.equals("since")
                    || elementName.equals("version")) {
                _currentCharData = new StringBuffer();
            } else if (elementName.equals("doc")) {
                _className = (String) _attributes.get("class");
                _checkForNull(_className,
                        "No class argument for element \"doc\"");
                Class specifiedClass = Class.forName(_className);
                if (_targetClass != null && _targetClass != specifiedClass) {
                    throw new Exception("Classes don't match: " + _targetClass
                            + "\n and \n" + specifiedClass);
                }
                _targetClass = specifiedClass;
            } else if (elementName.equals("port")) {
                _currentCharData = new StringBuffer();
                _name = (String) _attributes.get("name");
                _checkForNull(_name, "No name argument for element \"port\"");
            } else if (elementName.equals("property")) {
                _currentCharData = new StringBuffer();
                _name = (String) _attributes.get("name");
                _checkForNull(_name,
                        "No name argument for element \"property\"");
            }
        } catch (Exception ex) {
            if (ex instanceof XmlException) {
                throw (XmlException) ex;
            } else {
                String msg = "XML element \"" + elementName
                        + "\" triggers exception:\n  " + ex.toString();
                throw new XmlException(msg, _currentExternalEntity(), _parser
                        .getLineNumber(), _parser.getColumnNumber());
            }
        }
        _attributes.clear();
    }

    /** Handle the start of an external entity.  This pushes the stack so
     *  that error reporting correctly reports the external entity that
     *  causes the error.
     *  @param systemID The URI for the external entity.
     */
    public void startExternalEntity(String systemID) {
        _externalEntities.push(systemID);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** The standard DocML DTD, represented as a string.  This is used
     *  to parse DocML data when a compatible PUBLIC DTD is specified.
     */
    public static String DocML_DTD_1 = "<!ELEMENT doc (author | description | port | property | Pt.AcceptedRating | Pt.ProposedRating | since | version)*><!ATTLIST doc name CDATA #REQUIRED class CDATA #REQUIRED><!ELEMENT author (#PCDATA)><!ELEMENT description (#PCDATA)><!ELEMENT port (#PCDATA)><!ATTLIST port name CDATA #REQUIRED><!ELEMENT property (#PCDATA)><!ATTLIST property name CDATA #REQUIRED><!ELEMENT Pt.acceptedRating (#PCDATA)><!ELEMENT Pt.proposedRating (#PCDATA)><!ELEMENT since (#PCDATA)><!ELEMENT version (#PCDATA)>";

    // NOTE: The master file for the above DTD is at
    // $PTII/ptolemy/vergil/basic/DocML_1.dtd.  If modified, it needs to be also
    // updated at http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** If the argument is null, throw an exception with the given message.
     *  @param object The reference to check for null.
     *  @param message The message to issue if the reference is null.
     */
    protected void _checkForNull(Object object, String message)
            throws XmlException {
        if (object == null) {
            throw new XmlException(message, _currentExternalEntity(), _parser
                    .getLineNumber(), _parser.getColumnNumber());
        }
    }

    /** Get the the URI for the current external entity.
     *  @return A string giving the URI of the external entity being read,
     *   or null if none.
     */
    protected String _currentExternalEntity() {
        return (String) _externalEntities.peek();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create next tier, if possible. */
    private void _createNextTier() {
        if (_nextTier != null) {
            return;
        }
        if (_isInstanceDoc) {
            _nextTier = new DocManager(_targetClass);
        } else {
            Class superClass = _targetClass.getSuperclass();
            if (_isNamedObj(superClass)) {
                _nextTier = new DocManager(superClass);
            }
        }
    }

    /** Return true if the target class has a two argument
     *  constructor compatible with MoML instantiation.
     *  @return True if the target class can be instanted
     *   by MoML in a CompositeEntity.
     */
    private boolean _hasMoMLConstructor() {
        // Check for a suitable constructor.
        Class[] parameters = { TypedCompositeActor.class, String.class };
        while (parameters[0] != null) {
            try {
                _targetClass.getConstructor(parameters);
                // If we get here, then there is such a constructor.
                return true;
            } catch (Exception e) {
                // Ignore and try the superclass.
            }
            if (parameters[0].equals(NamedObj.class)) {
                break;
            }
            parameters[0] = parameters[0].getSuperclass();
        }
        return false;
    }

    /** Return true if the specified class is either equal to
     *  NamedObj or is a subclass of NamedObj.
     */
    private boolean _isNamedObj(Class candidate) {
        if (candidate == NamedObj.class) {
            return true;
        } else {
            candidate = candidate.getSuperclass();
            if (candidate == null) {
                return false;
            }
            return _isNamedObj(candidate);
        }
    }

    /** Read the doc file, if one is found, for the target. */
    private void _readDocFile() {
        if (_docFileHasBeenRead || _isInstanceDoc) {
            return;
        }
        // FIXME: If file is not found, then instead of an
        // exception, probably want to delegate to the base class.
        String docName = "doc.codeDoc." + _className;
        URL toRead = getClass().getClassLoader().getResource(
                docName.replace('.', '/') + ".xml");
        try {
            if (toRead != null) {
                parse(null, toRead.openStream());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            _exception = "Error reading URL: " + toRead.toExternalForm()
                    + "\n<pre>\n" + ex + "\n</pre>\n";
        }
        _docFileHasBeenRead = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Attributes associated with an entity. */
    private HashMap _attributes;

    /** The author field. */
    private String _author;

    /** The class name. */
    private String _className;

    /** The current character data for the current element. */
    private StringBuffer _currentCharData = new StringBuffer();

    /** The description field. */
    private String _description;

    /** Indicator that the doc file has been read. */
    private boolean _docFileHasBeenRead = false;

    /** If an exception is encountered parsing, it is described here. */
    private String _exception;

    /** The external entities being parsed. */
    private Stack _externalEntities = new Stack();

    /** Indicator that the primary source of documentation is the instance. */
    private boolean _isInstanceDoc = false;

    /** The name associated with the current port, parameter, etc. */
    private String _name;

    /** The next tier in the class hierarchy. */
    private DocManager _nextTier;

    /** The parser. */
    private XmlParser _parser = new XmlParser();

    /** A table of property documents. */
    private HashMap _properties = new HashMap();

    /** A table of port documents. */
    private HashMap _ports = new HashMap();

    /** The Pt.AcceptedRating field. */
    private String _ptAcceptedRating;

    /** The Pt.ProposedRating field. */
    private String _ptProposedRating;

    /** The since field. */
    private String _since;

    /** The object to be documented. */
    private NamedObj _target;

    /** The class of object to be documented. */
    private Class _targetClass;

    /** The version field. */
    private String _version;
}
