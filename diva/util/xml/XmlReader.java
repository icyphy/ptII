/*
 Copyright (c) 1998-2001 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
 */
package diva.util.xml;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import diva.resource.DefaultBundle;
import diva.util.LoggableOp;

import com.microstar.xml.XmlException;
import com.microstar.xml.XmlHandler;
import com.microstar.xml.XmlParser;

/**
 * An XmlReader reads a character stream and constructs the internal
 * data of an XmlDocument. Internally, it contains an implementation
 * of the Aelfred XmlHandler interface to parse the character stream
 * and construct a tree of XmlElements.
 *
 * <P> Typically, when you parse an XML document, you do not want the
 * parse to barf on badly-formed XML with an exception. XmlReader
 * therefore prints error and warning messages to an output stream,
 * which is by default System.out, but can be set to another stream to
 * allow an application to capture this output (to display it in a
 * GUI, for example). A "verbose" flag can be used to make the XML
 * reader print out lots and lots of other information as well. Once
 * the parse is complete, the caller should use the getErrorCount()
 * method to see if there were any errors.
 *
 * <P> This parser is capable of resolving external entities using
 * either public IDs or system IDs.  System IDs are usually given as a
 * complete URL to the given file.  Public IDs are given as partial
 * pathnames to which the parser prepends a locally known location for
 * libraries of XML files. In Diva, the partial pathname (eg
 * "graph.dtd") is looked up in the default resource bundle in the
 * diva.resource package.  DTDs that can be recognized as "public"
 * must therefore be entered into diva/resource/Defaults.properties
 * file.  If both IDs are given, this parser tries to use the public
 * ID first.
 *
 * @author Steve Neuendorffer, John Reekie
 * @version $Id$
 */
public class XmlReader extends LoggableOp {

    /** The current Aelfred parser
     */
    private XmlParser _parser = null;

    /** The current document being parsed into
     */
    private XmlDocument _document = null;

    /** The root of the current parse tree.
     */
    private XmlElement _root;

    /** Get the current line number.
     */
    public int getLineNumber () {
        return _parser.getLineNumber();
    }

    /**
     * Parse the given document from the URL it contains. If any
     * errors or warnings occur in the XML, they will have already
     * been printed to the error stream (see setErrorStream()). After
     * calling this method, the caller must check getErrorCount() to
     * see if any errors occurred, otherwise the XmlDocument may not
     * be well-formed.  Note that for XML errors, this method will not
     * throw an exception; however, for parser internal errors or I/O
     * exceptions, this method will throw an exception so parsing stops
     * immediately.
     *
     * @exception Exception If the parser fails internally. This indicates
     * a severe error, such as an I/O error, not an XML error.
     */
    public void parse (XmlDocument document) throws Exception {
        URL url = document.getURL();
        if (url != null) {
            parse(document, url, null, null, null, null);
        } else {
            // Parse from a file
            File file = document.getFile();
            if (file == null) {
                throw new XmlException("Document contains no URL or File", "", 0, 0);
            }
            FileReader in = new FileReader(file);
            parse(document, in);
        }
    }

    /**
     * Parse the given document from the given input stream, but
     * using the given URL to resolve external references.
     *
     * @see #parse(XmlDocument)
     * @exception Exception If the parser fails internally. This indicates
     * a severe error, such as an I/O error, not an XML error.
     */
    public void parse (XmlDocument document, InputStream in) throws Exception {
        URL url = document.getURL();
        parse(document, url, null, null, in, null);
    }

    /**
     * Parse the given document from the given reader, but
     * using the given URL to resolve external references.
     *
     * @see #parse(XmlDocument)
     * @exception Exception If the parser fails internally. This indicates
     * a severe error, such as an I/O error, not an XML error.
     */
    public void parse (XmlDocument document, Reader in) throws Exception {
        URL url = document.getURL();
        parse(document, url, null, in, null, null);
    }

    /**
     * Print the type of an entity.
     */
    public String printEntityType (String name) {
        int type = _parser.getEntityType(name);
        switch (type) {
        case XmlParser.ENTITY_INTERNAL:
            return "ENTITY_INTERNAL";
        case XmlParser.ENTITY_NDATA:
            return "ENTITY_NDATA";
        case XmlParser.ENTITY_TEXT:
            return "ENTITY_TEXT";
        case XmlParser.ENTITY_UNDECLARED:
            return "ENTITY_DECLARED";
        default:
            return "Unknown entity type";
        }
    }

    /**
     * Parse the given document given a bunch of parameters. Do some
     * messing around to figure out which version of parse to call in
     * the Aelfred parser.
     */
    private void parse (XmlDocument document,
            URL systemId, URL publicId,
            Reader reader, InputStream stream,
            String encoding) throws Exception {

        String pubString, sysString;
        if (systemId != null) {
            sysString = systemId.toString();
        } else {
            sysString = "";
        }
        if (publicId != null) {
            pubString = publicId.toString();
        } else {
            pubString = "";
        }
        _document = document;
        _root = null;
        _parser = new XmlParser();
        _document._parser = _parser;
        reset();

        try {
            _parser.setHandler(new Handler());
            if (reader != null) {
                _parser.parse(sysString, pubString, reader);
            } else if (stream != null) {
                _parser.parse(sysString, pubString, stream, encoding);
            } else {
                _parser.parse(sysString, pubString, encoding);
            }
        }
        finally {
            /* Who knows, maybe something was parsed at this point,
             * so allow the application to decide by setting everything
             * up here.
             */
            document.setRoot(_root);
        }
    }

    /** Handler is an inner class that implements the Aelfred XmlHandler
     * interface. It constructs XmlElements and also initializes the
     * current XmlDocument from things like the doctype declaration.
     * In verbose mode, it prints out lots of additional info during the parse.
     */
    public class Handler implements XmlHandler {
        /** A map for accumulating the XML attributes before the start
         * of the next entity.  This is a map from a String
         * representing the attribute's name to a string representing
         * the attribute's value.
         */
        private TreeMap _attributes = new TreeMap();

        /** The current element being created in the parse tree.
         */
        private XmlElement _currentElement;

        /** This linkedlist contains the current path in the tree of
         * XML external entities being parsed.  The current external
         * entity is first in the list.
         */
        private List _externalEntities = new ArrayList();

        /** Handle an attribute assignment that is part of an XML element.
         *  This method is called prior to the corresponding startElement()
         *  call, so it simply accumulates attributes in a hashtable for
         *  use by startElement().
         *
         *  @param name The name of the attribute.
         *  @param value The value of the attribute, or null if the attribute
         *   is <code>#IMPLIED</code> and not specified.
         *  @param specified True if the value is specified, false if the
         *   value comes from the default value in the DTD rather than from
         *   the XML file.
         *  @exception XmlException If the name or value is null.
         */
        public void attribute(String name, String value, boolean specified)
                throws Exception {
            if (isVerbose()) {
                logInfo("attr", name + "=\"" + value + "\" (" + specified + ")");
            }
            if (name == null) {
                logError("Attribute has no name");
            } else {
                _attributes.put(name, value);
            }
        }

        /**
         * Append the given character bytes to the character data of
         * the current XML element.
         */
        public void charData(char c[], int offset, int length)
                throws Exception {
            String s = new String(c, offset, length);
            if (isVerbose()) {
                String x;
                if (s.length() > 40) {
                    x = s.substring(0,40) + "...";
                } else {
                    x = s;
                }
                logInfo("cdata", "[" + offset + "," + length + "] " + s);
            }
            _currentElement.appendPCData(s);
        }

        /**
         * Handle a document type declaration. This sets the DTD external
         * identifiers in the XmlDocument.
         */
        public void doctypeDecl (String name, String publicId, String systemId)
                throws java.lang.Exception {
            if (isVerbose()) {
                logInfo("doctype", name + " \"" + publicId + "\" \"" + systemId + "\"");
            }
            _document.setDocType(name);
            _document.setDTDPublicID(publicId);
            _document.setDTDSystemID(systemId);
        }

        /**
         * End the document. If we've finished the parse and didn't get
         * back to the root of the parse tree, generate an error.
         */
        public void endDocument() throws Exception {
            if (isVerbose()) {
                unindent();
                logInfo("end", "");
            }
            // This never gets called anyway -- Alfred catches it
            //if (_currentElement != _root) {
            //    logError("Document tags do not match");
            //}
        }

        /**
         * Move up one level in the parse tree.
         */
        public void endElement(String name) throws Exception {
            if (isVerbose()) {
                unindent();
                logInfo("end", "</" + name + ">");
            }
            XmlElement parent = _currentElement.getParent();
            _currentElement = parent;
        }

        /**
         * Move up one level in the external entity tree.
         *
         * @exception XmlException If given URI was not the URI that was expected,
         * based on the external entity tree.
         */
        public void endExternalEntity(String URI) throws Exception {
            if (isVerbose()) {
                logInfo("end ext", URI);
            }
            // The doesn't work because of a bug in Aelfred that
            // appears if you resolve the external entity to an input stream
            //String _currentElement = _currentExternalEntity();
            //if (!_currentElement.equals(URI)) {
            //    logError("Entities out of order in " + _currentElement);
            //}
            _externalEntities.remove(0);
        }

        /**
         * Print an error message to the error stream.
         */
        public void error(String message, String sysid,
                int line, int column) throws Exception {
            if (sysid != null) {
                logError("[" + sysid + "] " + message);
            } else {
                logError(message);
            }
        }

        /**
         * Handle ignorable whitespace.
         * <p>The default implementation does nothing.
         * @see com.microstar.xml.XmlHandler#ignorableWhitespace
         * @exception java.lang.Exception Derived methods may throw exceptions.
         */
        public void ignorableWhitespace (char ch[], int start, int length)
                throws java.lang.Exception
        {
        }

        /**
         * Handle a processing instruction.
         * <p>The default implementation does nothing.
         * @see com.microstar.xml.XmlHandler#processingInstruction
         * @exception java.lang.Exception Derived methods may throw exceptions.
         */
        public void processingInstruction (String target, String data)
                throws java.lang.Exception {
            ; // ?
        }

        /**
         * Attempt resolve the public ID representing an XML external
         * entity into a valid string url.  If the public ID is non-null
         * and matches the public ID of the document and the document
         * has a DTD specified, return that DTD in a reader.
         * Otherwise, non-null public DTD's are looked up in the
         * default resource bundle, diva/resource/Defaults.properties.
         */
        public Object resolveEntity (String pubID, String sysID)
                throws Exception {
            if (isVerbose()) {
                logInfo("resolve", "\"" + pubID + "\" \"" + sysID + "\"");
            }

            // By default, the result is the System ID
            Object result = sysID;
            if (pubID != null && pubID.equals(_document.getDTDPublicID())) {
                String dtd = _document.getDTD();
                if (dtd != null) {
                    return new java.io.StringReader(dtd);
                }
            }
            if (pubID != null && !pubID.equals("")) {
                /* To find the DTD from the public ID, create a DefaultBundle
                 * and look up the ID in that. If it is not null, then it
                 * is an input stream that the parser can use.
                 */
                DefaultBundle resources = new DefaultBundle();
                try {
                    Object str = resources.getResourceAsStream(pubID);
                    if (str != null) {
                        result = str;
                    }
                } catch (Exception ex) {
                    // if the resource is not found, then ignore.
                }
            }
            if (isVerbose()) {
                logInfo("resolve", "=> " + result);
            }
            return result;
        }

        /**
         * Start a document.  This method is called just before the parser
         * attempts to read the first entity (the root of the document).
         * It is guaranteed that this will be the first method called.
         * Initialize the parse tree to contain no elements.
         */
        public void startDocument() {
            if (isVerbose()) {
                logInfo("start", "");
                indent();
            }
            _attributes.clear();
            _root = null;
        }

        /**
         * Start an element.
         * This is called at the beginning of each XML
         * element.  By the time it is called, all of the attributes
         * for the element will already have been reported using the
         * attribute() method.
         * Create a new XmlElement to represent the element.
         * Set the attributes of the new XmlElement equal
         * to the attributes that have been accumulated since the last
         * call to this method.  If this is the first element encountered
         * during this parse, set the root of the parse tree equal to the
         * newly created element.
         * Descend the parse tree into the new element
         *
         * @param name the element type of the element that is beginning.
         */
        public void startElement(String name) {
            if (isVerbose()) {
                logInfo("start", "<" + name + "> (" + printEntityType(name) + ")");
                indent();
            }
            XmlElement e = new XmlElement(name, _attributes);
            e.setParent(_currentElement);
            if (_currentElement == null)
                _root = e;
            else
                _currentElement.addElement(e);
            _currentElement = e;
            _attributes.clear();
        }

        /**
         * Move down one level in the entity tree.
         */
        public void startExternalEntity(String URI) throws Exception {
            if (isVerbose()) {
                logInfo("start ext", URI);
            }
            _externalEntities.add(0, URI);
        }

        protected String _currentExternalEntity() {
            //if (isVerbose())
            //    System.out.println("currentExternalEntity: URI=\"" +
            //            (String)_externalEntities.get(0) + "\"\n");
            return (String)_externalEntities.get(0);
        }
    }
}


