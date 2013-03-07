/* The XML element handler that builds the XML tree.

 Copyright (c) 2005-2013 The Regents of the University of California.
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
package ptolemy.backtrack.xmlparser;

import java.io.StringReader;
import java.util.Hashtable;

import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// XmlHandler
/**
 The XML element handler that builds the XML tree.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class XmlHandler implements com.microstar.xml.XmlHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle an attribute. Its name and value will be recorded in the hash
     *  table of attributes.
     *
     *  @param name The name of the attribute.
     *  @param value The value of the attribute.
     *  @param isSpecified true if the value was specified, false if it was
     *   defaulted from the DTD.
     *  @exception Exception Not thrown in this base class.
     */
    public void attribute(String name, String value, boolean isSpecified)
            throws Exception {
        if (value != null) {
            _currentAttributes.put(name, value);
        }
    }

    /** Handle a chunk of char data.
     *
     *  @param ch The character data.
     *  @param start The starting position in the array.
     *  @param length The number of characters available.
     *  @exception Exception Not thrown in this base class.
     */
    public void charData(char[] ch, int start, int length) throws Exception {
    }

    /** Handle a document type declaration.
     *
     *  @param name The document type name.
     *  @param publicId The public identifier, or null if unspecified.
     *  @param systemId The system identifier, or null if unspecified.
     *  @exception Exception Not thrown in this base class.
     */
    public void doctypeDecl(String name, String publicId, String systemId)
            throws Exception {
    }

    /** Handle the end of the XML document.
     */
    public void endDocument() throws Exception {
    }

    /** Handle the end of an XML element.
     *
     *  @param elementName The name of the element.
     *  @exception Exception Not thrown in this base class.
     */
    public void endElement(String elementName) throws Exception {
        _currentTree = _currentTree.getParent();
    }

    /** Handle the end of an external entity.
     *
     *  @param systemId The system ID of the external entity.
     *  @exception Exception Not thrown in this base class.
     */
    public void endExternalEntity(String systemId) throws Exception {
    }

    /** Signal an error message.
     *
     *  @param message The error message.
     *  @param systemId The system ID of the XML document that contains the
     *   error.
     *  @param line The line number of the error.
     *  @param column The column number of the error.
     *  @exception Exception Not thrown in this base class.
     */
    public void error(String message, String systemId, int line, int column)
            throws Exception {
    }

    /** Return the current XML tree.
     *
     *  @return The current XML tree.
     */
    public ConfigXmlTree getCurrentTree() {
        return _currentTree;
    }

    /** Return the system ID of the XML document.
     *
     *  @return The system ID.
     */
    public String getSystemId() {
        return _systemId;
    }

    /** Handle consecutive ignorable white spaces.
     *
     *  @param ch The literal whitespace characters.
     *  @param start The starting position in the array.
     *  @param length The number of whitespace characters available.
     *  @exception Exception Not thrown in this base class.
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws Exception {
    }

    /** Handle a processing instruction.
     *
     *  @param target The target (the name at the start of the processing
     *   instruction).
     *  @param data The data, if any (the rest of the processing instruction).
     *  @exception Exception Not thrown in this base class.
     */
    public void processingInstruction(String target, String data)
            throws Exception {
    }

    /** Resolve an external entity.
     *
     *  @param publicId The public ID, or null if none was supplied.
     *  @param systemId The system ID.
     *  @return The replacement system identifier, or null to use the default.
     *  @exception Exception Not thrown in this base class.
     */
    public Object resolveEntity(String publicId, String systemId)
            throws Exception {
        if (publicId != null && publicId.equals(MoML_PUBLIC_ID_1)) {
            return new StringReader(MoML_DTD_1);
        } else {
            return null;
        }
    }

    /** Handle the start of the XML document.
     *
     *  @exception Exception Not thrown in this base class.
     */
    public void startDocument() throws Exception {
    }

    /** Handle the start of an XML element.
     *
     *  @param elementName The name of the XML element.
     *  @exception Exception Not thrown in this base class.
     */
    public void startElement(String elementName) throws Exception {
        ConfigXmlTree newtree = new ConfigXmlTree(elementName);
        newtree._setParent(_currentTree);
        _currentTree = newtree;

        for (String attribute : _currentAttributes.keySet()) {
            _currentTree.setAttribute(attribute, _currentAttributes
                    .get(attribute));
        }

        _currentAttributes.clear();
    }

    /** Handle the start of an external entity.
     *
     *  @param systemId The system ID of the external entity.
     *  @exception Exception Not thrown in this base class.
     */
    public void startExternalEntity(String systemId) throws Exception {
    }

    ///////////////////////////////////////////////////////////////////
    ////                        public fields                      ////

    /** The standard MoML DTD, represented as a string.  This is used
     *  to parse MoML data when a compatible PUBLIC DTD is specified.
     *  NOTE: This DTD includes a number of elements that are deprecated.
     *  They are included here for backward compatibility.  See the MoML
     *  chapter of the Ptolemy II design document for a view of the
     *  current (nondeprecated) DTD.
     */
    public static final String MoML_DTD_1 = MoMLParser.MoML_DTD_1;

    /** The public ID for version 1 MoML.
     */
    public static final String MoML_PUBLIC_ID_1 = MoMLParser.MoML_PUBLIC_ID_1;

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Construct an XML handler with the given XML tree as the current tree.
     *
     *  @param tree The initial XML tree.
     *  @param systemId The system ID of the document type.
     */
    XmlHandler(ConfigXmlTree tree, String systemId) {
        _currentTree = tree;
        this._systemId = systemId;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The attributes of the current XML node.
     */
    private Hashtable<String, String> _currentAttributes = new Hashtable<String, String>();

    /** XML tree starting from the current node.
     */
    private ConfigXmlTree _currentTree;

    /** The system ID of the XML document.
     */
    private String _systemId;
}
