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
import java.net.URL;

import com.microstar.xml.XmlParser;

/**
 * An XMLDocument is an in-memory representation of an XML document.
 * It contains an XML element as its root, and other information
 * relevant to the document as a whole, such as its URL, its DTD, and
 * so on. The document can be parsed from or written out to its URL or
 * File, or to some other arbitrary Writer. Documents do not parse
 * themselves, but are passed to an XmlReader.
 *
 * <p>See the <a href="package-summary.html">package summary</a> for a
 * brief description of how to use this class.
 *
 * @author Steve Neuendorffer, John Reekie
 * @version $Id$
 */
public class XmlDocument {

    // This document's DTD, as a complete string
    String _dtd = null;

    // This document's type
    String _docType = null;

    // The public ID of this document's DTD, if it has one
    String _dtdPublicID = null;

    // The system ID of this document's DTD, if it has one
    String _dtdSystemID = null;

    // The location of this document as a URL
    private URL _url = null;

    // The location of this document as a file
    private File _file = null;

    // The root of the XML tree
    private XmlElement _root = null;

    // The parser that most recently generated this document
    XmlParser _parser = null;

    /** Create an XML document with no idea of where it exists.
     */
    public XmlDocument() {
    }

    /** Create an XML document with the given URL.
     */
    public XmlDocument(URL url) {
        _url = url;
    }

    /** Create an XML document located in the given file.
     */
    public XmlDocument(File file) {
        _file = file;
    }

    /** Create an XML document with the given URL and the DTD system ID.
     */
    public XmlDocument(URL url, String dtdPublicID) {
        _url = url;
        _dtdPublicID = dtdPublicID;
    }

    /** Get the DTD of this document. If a DTD has most recently been
     * explicitly set with setDTD(), return that. If the document has most
     * recently been parsed, then return the DTD that was used to parse
     * the document. If neither of these has ever been done, return
     * null.
     */
    public String getDTD() {
        // FIXME
        return _dtd;
    }

    /** Get the public ID of the DTD of this document. This field can be
     * set explicitly by the setDTDPublicID() method or by parsing
     * the document. Null if it does not have one.
     */
    public String getDTDPublicID() {
        // FIXME
        return _dtdPublicID;
    }

    /** Get the system ID of the DTD of this document. This field can be
     * set explicitly by the setDTDSystemID() method or by parsing
     * the document. Null if it does not have one.
     */
    public String getDTDSystemID() {
        // FIXME
        return _dtdSystemID;
    }

    /** Get the URL of this document. This may be null.
     */
    public URL getURL() {
        return _url;
    }

    /** Get the file corresponding to this document. This may be null.
     */
    public File getFile() {
        return _file;
    }

    /** Get the root element of this document. Null if the document
     * has never been parsed or had its root element explicitly set.
     */
    public XmlElement getRoot() {
        return _root;
    }

    /** Set the DTD of this document. The DTD is a string that will
     * be printed when the document is printed. If set to null, the
     * DTD will be set to the DTD system and/or public external identifiers.
     */
    public void setDTD(String dtd) {
        // FIXME
        _dtd = dtd;
    }

    /** Set the type of this document.
     */
    public void setDocType (String dt) {
        _docType = dt;
    }

    /** Set the DTD of this document by its public ID.
     */
    public void setDTDPublicID (String id) {
        // FIXME
        _dtdPublicID = id;
    }

    /** Set the DTD of this document by its public ID.
     */
    public void setDTDSystemID (String id) {
        // FIXME
        _dtdSystemID = id;
    }

    /** Set the root element of this document. This method replaces
     * any existing XML content in the document.
     */
    public void setRoot (XmlElement root) {
        _root = root;
    }

    /** Set the file that this document corresponds to. When this document is
     * printed or parsed, this is where this document will
     * be printed to or parsed from. If the document also has
     * a non-null URL, then the file location will be ignored.
     */
    public void setFile(File file) {
        _file = file;
    }

    /** Set the URL of this document. When this document is
     * printed or parsed, this is where this document will
     * be printed to or parsed from. If the document also has
     * a file object, then it will be ignored once the URL
     * has been set non-null.
     */
    public void setURL(URL url) {
        _url = url;
    }
}


