/* A parser for DocML (Plot Markup Language) supporting PlotBoxML commands.

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
import java.util.Hashtable;
import java.util.Stack;

import ptolemy.kernel.util.NamedObj;

import com.microstar.xml.HandlerBase;
import com.microstar.xml.XmlException;
import com.microstar.xml.XmlParser;

//////////////////////////////////////////////////////////////////////////
//// DocManager

/**
 FIXME

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class DocManager extends HandlerBase {
    
    /** Construct an manager to handle documentation for the specified target.
     *  @param target The object to be documented.
     */
    public DocManager(NamedObj target) {
        super();
        _target = target;
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

    /** End an element. For most elements this method
     *  calls the appropriate PlotBox method.
     *  &AElig;lfred will call this method at the end of each element
     *  (including EMPTY elements).
     *  @param elementName The element type name.
     */
    public void endElement(String elementName) throws Exception {
        if (elementName.equals("description")) {
            _description = _currentCharData.toString();
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
    
    /** Return the description, or none if null if none has been given.
     *  @return The description.
     */
    public String getDescription() {
        return _description;
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
        _attributes = new Hashtable();
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
            } else if (elementName.equals("port")) {
                String spec = (String) _attributes.get("name");
                _checkForNull(spec, "No name argument for element \"port\"");
               // FIXME: Deal with this.
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
    // FIXME:
    public static String DocML_DTD_1 = "<!ELEMENT plot (barGraph | bin | dataset | default | noColor | noGrid | size | title | wrap | xLabel | xLog | xRange | xTicks | yLabel | yLog | yRange | yTicks)*><!ELEMENT barGraph EMPTY><!ATTLIST barGraph width CDATA #IMPLIED offset CDATA #IMPLIED><!ELEMENT bin EMPTY><!ATTLIST bin width CDATA #IMPLIED offset CDATA #IMPLIED><!ELEMENT dataset (m | move | p | point)*><!ATTLIST dataset connected (yes | no) #IMPLIED marks (none | dots | points | various) #IMPLIED name CDATA #IMPLIED stems (yes | no) #IMPLIED><!ELEMENT default EMPTY><!ATTLIST default connected (yes | no) \"yes\" marks (none | dots | points | various) \"none\" stems (yes | no) \"no\"><!ELEMENT noColor EMPTY><!ELEMENT noGrid EMPTY><!ELEMENT reuseDatasets EMPTY><!ELEMENT size EMPTY><!ATTLIST size height CDATA #REQUIRED width CDATA #REQUIRED><!ELEMENT title (#PCDATA)><!ELEMENT wrap EMPTY><!ELEMENT xLabel (#PCDATA)><!ELEMENT xLog EMPTY><!ELEMENT xRange EMPTY><!ATTLIST xRange min CDATA #REQUIRED max CDATA #REQUIRED><!ELEMENT xTicks (tick)+><!ELEMENT yLabel (#PCDATA)><!ELEMENT yLog EMPTY><!ELEMENT yRange EMPTY><!ATTLIST yRange min CDATA #REQUIRED max CDATA #REQUIRED><!ELEMENT yTicks (tick)+><!ELEMENT tick EMPTY><!ATTLIST tick label CDATA #REQUIRED position CDATA #REQUIRED><!ELEMENT m EMPTY><!ATTLIST m x CDATA #IMPLIED y CDATA #REQUIRED lowErrorBar CDATA #IMPLIED highErrorBar CDATA #IMPLIED><!ELEMENT move EMPTY><!ATTLIST move x CDATA #IMPLIED y CDATA #REQUIRED lowErrorBar CDATA #IMPLIED highErrorBar CDATA #IMPLIED><!ELEMENT p EMPTY><!ATTLIST p x CDATA #IMPLIED y CDATA #REQUIRED lowErrorBar CDATA #IMPLIED highErrorBar CDATA #IMPLIED><!ELEMENT point EMPTY><!ATTLIST point x CDATA #IMPLIED y CDATA #REQUIRED lowErrorBar CDATA #IMPLIED highErrorBar CDATA #IMPLIED>";

    // NOTE: The master file for the above DTD is at
    // $PTII/ptolemy/plot/docml/docml.dtd.  If modified, it needs to be also
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
    ////                         protected members                 ////
    // NOTE: Do not use HashMap here to maintain Java 1.1 compatibility.

    /** Attributes associated with an entity. */
    protected Hashtable _attributes;

    /** The current character data for the current element. */
    protected StringBuffer _currentCharData = new StringBuffer();
    
    /** The description field. */
    protected String _description;

    /** The parser. */
    protected XmlParser _parser = new XmlParser();

    /** The object to be documented. */
    protected NamedObj _target;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
   
    // The external entities being parsed.
    private Stack _externalEntities = new Stack();

    // Indicator of whether we are parsing x ticks or y ticks.
    private boolean _xtick;
}
