/* A parser for DocML (Doc Markup Language).

 Copyright (c) 1998-2005 The Regents of the University of California.
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
import java.util.Stack;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;

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
 FIXME: Implement this!
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
 
 FIXME: Document the XML format.
 
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
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
        _className = _targetClass.getName();
        Attribute instanceDoc = _target.getAttribute(DOC_ATTRIBUTE_NAME);
        // FIXME: Try to populate from attribute.
    }

    /** Construct a manager to handle documentation for the specified target
     *  class.
     *  @param targetClass The class to be documented.
     */
    public DocManager(Class targetClass) {
        super();
        _targetClass = targetClass;
        _className = _targetClass.getName();
    }
    
    /** Construct a manager for documentation at the specified URL.
     *  @param url The URL.
     */
    public DocManager(URL url) {
        super();
        try {
            parse(null, url.openStream());
        } catch (Exception ex) {
            ex.printStackTrace();
            _exception = "Error reading URL: "
                    + url.toExternalForm()
                    + "\n<pre>\n"
                    + ex
                    + "\n</pre>\n";
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
        if (elementName.equals("description")) {
            // Append "see also" information.
            _currentCharData.append(_getSeeAlso());
            _description = _currentCharData.toString();
        } else if (elementName.equals("port")) {
            _ports.put(_name, _currentCharData.toString());
        }
        // FIXME: Other tags.
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
                if (_nextTier != null) {
                    return _nextTier.getDescription();
                } else {
                    // FIXME: create _nextTier and delegate.
                    return "No description";
                }
            }
        }
        return _description;
    }
    
    /** Return the documentation for the specified port, or null
     *  if there is none.
     *  @param name The name of the port.
     *  @return The documentation for the specified port, or null
     *   if there is none.
     */
    public String getPortDoc(String name) {
        _readDocFile();
        String result = (String)_ports.get(name);
        if (result == null) {
            if (_nextTier != null) {
                return _nextTier.getPortDoc(name);
            } else {
                // FIXME: create _nextTier and delegate.
                return "No port description";
            }
        }
        return result;
    }
    
    /** Return true if an exception was encountered parsing
     *  the DocML data.
     *  @return True if an exception was encountered.
     */
    public boolean hadException() {
        return _exception != null;
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
            // This is the generic MoML DTD.
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
            if (elementName.equals("description")) {
                _currentCharData = new StringBuffer();
            } else if (elementName.equals("doc")) {
                _className = (String) _attributes.get("class");
                _checkForNull(_className, "No class argument for element \"doc\"");
                Class specifiedClass = Class.forName(_className);
                if (_targetClass != null && _targetClass != specifiedClass) {
                    throw new Exception("Classes don't match: "
                            + _targetClass
                            + "\n and \n"
                            + specifiedClass);
                }
                _targetClass = specifiedClass;
            } else if (elementName.equals("port")) {
                _currentCharData = new StringBuffer();
                _name = (String) _attributes.get("name");
                _checkForNull(_name, "No name argument for element \"port\"");
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

    /** The attribute name for a doc attribute, if it is present. */
    public static final String DOC_ATTRIBUTE_NAME = "_docAttribute";
    
    /** The standard DocML DTD, represented as a string.  This is used
     *  to parse DocML data when a compatible PUBLIC DTD is specified.
     */
    // FIXME:
    public static String DocML_DTD_1 = "<!ELEMENT doc (barGraph | bin | dataset | default | noColor | noGrid | size | title | wrap | xLabel | xLog | xRange | xTicks | yLabel | yLog | yRange | yTicks)*><!ELEMENT barGraph EMPTY><!ATTLIST barGraph width CDATA #IMPLIED offset CDATA #IMPLIED><!ELEMENT bin EMPTY><!ATTLIST bin width CDATA #IMPLIED offset CDATA #IMPLIED><!ELEMENT dataset (m | move | p | point)*><!ATTLIST dataset connected (yes | no) #IMPLIED marks (none | dots | points | various) #IMPLIED name CDATA #IMPLIED stems (yes | no) #IMPLIED><!ELEMENT default EMPTY><!ATTLIST default connected (yes | no) \"yes\" marks (none | dots | points | various) \"none\" stems (yes | no) \"no\"><!ELEMENT noColor EMPTY><!ELEMENT noGrid EMPTY><!ELEMENT reuseDatasets EMPTY><!ELEMENT size EMPTY><!ATTLIST size height CDATA #REQUIRED width CDATA #REQUIRED><!ELEMENT title (#PCDATA)><!ELEMENT wrap EMPTY><!ELEMENT xLabel (#PCDATA)><!ELEMENT xLog EMPTY><!ELEMENT xRange EMPTY><!ATTLIST xRange min CDATA #REQUIRED max CDATA #REQUIRED><!ELEMENT xTicks (tick)+><!ELEMENT yLabel (#PCDATA)><!ELEMENT yLog EMPTY><!ELEMENT yRange EMPTY><!ATTLIST yRange min CDATA #REQUIRED max CDATA #REQUIRED><!ELEMENT yTicks (tick)+><!ELEMENT tick EMPTY><!ATTLIST tick label CDATA #REQUIRED position CDATA #REQUIRED><!ELEMENT m EMPTY><!ATTLIST m x CDATA #IMPLIED y CDATA #REQUIRED lowErrorBar CDATA #IMPLIED highErrorBar CDATA #IMPLIED><!ELEMENT move EMPTY><!ATTLIST move x CDATA #IMPLIED y CDATA #REQUIRED lowErrorBar CDATA #IMPLIED highErrorBar CDATA #IMPLIED><!ELEMENT p EMPTY><!ATTLIST p x CDATA #IMPLIED y CDATA #REQUIRED lowErrorBar CDATA #IMPLIED highErrorBar CDATA #IMPLIED><!ELEMENT point EMPTY><!ATTLIST point x CDATA #IMPLIED y CDATA #REQUIRED lowErrorBar CDATA #IMPLIED highErrorBar CDATA #IMPLIED>";

    // NOTE: The master file for the above DTD is at
    // $PTII/ptolemy/doc/docml/docml.dtd.  If modified, it needs to be also
    // updated at ptweb/archive/docml.dtd.
    // FIXME: Update above with the right location.
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
        Class superClass = _targetClass.getSuperclass();
        if (_isNamedObj(superClass)) {
            _nextTier = new DocManager(superClass);
        }
    }
    
    /** Return "see also" information. */
    private StringBuffer _getSeeAlso() {
        StringBuffer result = new StringBuffer();
        
        // See whether there is Javadoc, and link to it if there is.
        result.append("\n<p><i>See Also:</i> ");
        String className = _targetClass.getName();
        String docName = "doc.codeDoc." + className;
        try {
            URL toRead = getClass().getClassLoader().getResource(
                    docName.replace('.', '/') + ".html");
            
            if (toRead != null) {
                result.append("<a href=\""
                    + toRead.toExternalForm()
                    + "\">Javadoc documentation</a>");
            } else {
                // FIXME: Make this a hyperlink to a doc on how
                // to create the javadocs.
                result.append("No javadocs found");
            }
        } catch (Exception ex) {
            result.append("Error opening javadoc file:\n<pre>"
                    + ex
                    + "/n</pre>\n");
        }
        
        // See whether the base class has a doc file, and if so, link to it.
        try {
            String baseClassName = _targetClass.getSuperclass().getName();
            URL toRead = getClass().getClassLoader().getResource(
                    baseClassName.replace('.', '/') + "Doc.xml");
            
            if (toRead != null) {
                result.append(", <a href=\""
                    + toRead.toExternalForm()
                    + "\">Base class (" + baseClassName + ")</a>");
            }
        } catch (Exception ex) {
            result.append("Error opening javadoc file:\n<pre>"
                    + ex
                    + "/n</pre>\n");
        }
        result.append("</p>");
        return result;
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
        if (_docFileHasBeenRead) {
            return;
        }
        // FIXME: If file is not found, then instead of an
        // exception, probably want to delegate to the base class.
        String className = _target.getClassName();
        URL toRead = getClass().getClassLoader().getResource(
                className.replace('.', '/') + "Doc.xml");
        try {
            if (toRead != null) {
                parse(null, toRead.openStream());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            _exception = "Error reading URL: "
                    + toRead.toExternalForm()
                    + "\n<pre>\n"
                    + ex
                    + "\n</pre>\n";
        }
        _docFileHasBeenRead = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Attributes associated with an entity. */
    private HashMap _attributes;
    
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

    /** The name associated with the current port, parameter, etc. */
    private String _name;
    
    /** The next tier in the class hierarchy. */
    private DocManager _nextTier;

    /** The parser. */
    private XmlParser _parser = new XmlParser();
    
    /** A table of port documents. */
    private HashMap _ports = new HashMap();

    /** The object to be documented. */
    private NamedObj _target;
    
    /** The class of object to be documented. */
    private Class _targetClass;
}
