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
// From aelfred's demo classes:
// DtdDemo.java: demonstration application showing DTD queries.
// NO WARRANTY! See README, and copyright below.
// $Id$

package diva.util.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Enumeration;

import diva.util.LoggableOp;

import com.microstar.xml.XmlParser;

/**
 * Given a tree of XmlElements, an object of this class generates
 * the equivalent XML into an output stream.
 *
 * @author Copyright (c) 1997, 1998 by Microstar Software Ltd.;
 * @author written by David Megginson &lt;dmeggins@microstar.com&gt;
 * @author Steve Neuendorffer, John Reekie
 * @version $Id$
 */
public class XmlWriter extends LoggableOp {

    // The version of XML we are writing -- currently 1.0 only
    private String _xmlVersion = "1.0";

    /* The current parser (used for writing DTDs)
     */
    private XmlParser _parser;

    /** Set the XML version string. The default is 1.0.
     */
    public void setXMLVersion (String v) {
        _xmlVersion = v;
    }

    /** Write the given XmlDocument to its URL. If the file or URL of
     * the document cannot be opened for writing, then an IOException
     * will be thrown.
     */
    public void write (XmlDocument document) throws IOException {
        Writer out;
        URL url = document.getURL();
        if (url != null) {
            if (!url.getProtocol().equals("file")) {
                throw new IOException("XmlWriter can only write to file:/ URLs");
            }
            out = new FileWriter(url.getFile());
        } else {
            File file = document.getFile();
            if (file != null) {
                out = new FileWriter(file);
            } else {
                throw new IOException("XmlDocument has no URL or File");
            }
        }
        write(document, out);
    }

    /** Write the given XmlDocument to a given Writer. If an error
     * occurs while writing, then an IOException will be thrown.
     */
    public void write (XmlDocument document, Writer out) throws IOException {
        out.write("<?xml version=\"" + _xmlVersion + "\" standalone=\"no\"?>");
        out.write("\n");
        out.write("<!DOCTYPE " + document.getRoot().getType());

        String pubid = document.getDTDPublicID();
        String sysid = document.getDTDSystemID();

        if (pubid == null) {
            if (sysid == null) {
                if (document.getDTD() != null) {
                    out.write(" [\n" + document.getDTD() + "\n]");
                } else {
                    // No DTD specification
                }
            } else {
                out.write(" SYSTEM \"" + sysid + "\"");
            }
        } else {
            out.write(" PUBLIC \"" + pubid + "\"\n\t\"" + sysid + "\"");
        }
        out.write(">\n\n");
        write(document.getRoot(), out, "");
        out.flush();
    }

    /** Write a single XML element out to the given writer. Prefix
     * each line with the given string. The writer is not flushed -- the
     * caller must do this if necessary.
     * @deprecated Use XmlElement.writeXML instead.
     */
    public void write(XmlElement e, Writer out, String prefix)
            throws IOException {
        e.writeXML(out, prefix);
    }

    /** Write the DTD of the given XmlDocument to a given Writer. If an error
     * occurs while writing, then an IOException will be thrown.
     */
    public void writeDTD (XmlDocument document, Writer out) throws IOException {
        String dtd = document.getDTD();
        if (dtd != null) {
            out.write(dtd); // that was easy!
            return;
        }
        _parser = document._parser;
        writeDTDNotations(out);
        writeDTDEntities(out);
        writeDTDElements(out);
        out.flush();
    }

    /**
     * Produce normalised declarations for all notations.
     */
    public void writeDTDNotations (Writer out) throws IOException {
        Enumeration notationNames = _parser.declaredNotations();
        String nname;
        String extId;

        // Mark the beginning of a new section.
        // out.write("<-- Notation Declarations -->\n");

        // Loop through all declared notations.
        while (notationNames.hasMoreElements()) {
            nname = (String)notationNames.nextElement();
            extId =
                makeExternalIdentifiers(_parser.getNotationPublicId(nname),
                        _parser.getNotationSystemId(nname).toString());
            out.write("<!NOTATION " + nname + + ' ' + extId + ">\n");
        }
    }

    /**
     * Produce normalised declarations for all general entities.
     */
    public void writeDTDEntities (Writer out) throws IOException {
        Enumeration entityNames = _parser.declaredEntities();
        String ename;
        String value;

        // Mark the beginning of a new section.
        // out.write("<-- Entity Declarations -->\n");

        // Loop through all the declared entities.
        while (entityNames.hasMoreElements()) {
            ename = (String)entityNames.nextElement();
            // Skip parameter entities.
            if (ename.startsWith("%")) {
                continue;
            }

            // Construct a value based on the class of entity.
            value = null;
            switch (_parser.getEntityType(ename)) {
                // Internal text entity
            case XmlParser.ENTITY_INTERNAL:
                value = makeLiteral(_parser.getEntityValue(ename));
                break;
                // External binary entity
            case XmlParser.ENTITY_NDATA:
                value =
                    makeExternalIdentifiers(_parser.getEntityPublicId(ename),
                            _parser.getEntitySystemId(ename).toString())
                    + "NDATA " + _parser.getEntityNotationName(ename);
                break;
                // External text entity
            case XmlParser.ENTITY_TEXT:
                value =
                    makeExternalIdentifiers(_parser.getEntityPublicId(ename),
                            _parser.getEntitySystemId(ename).toString());
                break;
            }
            // Print a normalised declaration.
            out.write("<!ENTITY " + ename + ' ' + value + ">\n");
        }
    }

    /**
     * Produce normalised declarations for all elements.
     */
    public void writeDTDElements (Writer out) throws IOException {
        Enumeration elementNames = _parser.declaredElements();
        String elname;

        // Mark the beginning of a new section.
        // out.write("<-- Element Type Declarations -->\n");

        // Loop through all of the declared elements.
        while (elementNames.hasMoreElements()) {
            String contentSpec = "ANY";

            elname = (String)elementNames.nextElement();

            // Construct a content spec based on the element's content type.
            switch (_parser.getElementContentType(elname)) {
            case XmlParser.CONTENT_EMPTY:
                contentSpec = "EMPTY";
                break;
            case XmlParser.CONTENT_ANY:
                contentSpec = "ANY";
                break;
            case XmlParser.CONTENT_ELEMENTS:
            case XmlParser.CONTENT_MIXED:
                contentSpec = _parser.getElementContentModel(elname);
                break;
            }

            // Print a normalised element type declaration.
            out.write("\n<!ELEMENT " + elname + ' ' + contentSpec + ">\n");

            // Print the ATTLIST declaration, if any.
            writeDTDAttributes(elname, out);

            // Blank line.
            out.write("");
        }
    }

    /**
     * Dump attributes for an element.
     * @see #makeAttributeType(String, String)
     * @see #makeAttributeValue(String, String)
     */
    void writeDTDAttributes (String elname, Writer out) throws IOException {
        Enumeration attributeNames = _parser.declaredAttributes(elname);
        String aname;
        String type;
        String value;

        // Skip if there are no declared attributes for this element type.
        if (attributeNames == null) {
            return;
        }

        // Print the start of the ATTLIST declaration.
        out.write("<!ATTLIST " + elname);

        // Loop through all of the declared attributes.
        while (attributeNames.hasMoreElements()) {
            aname = (String)attributeNames.nextElement();
            type = makeAttributeType(elname, aname);
            value = makeAttributeValue(elname, aname);

            // Print the declaration for a single attribute.
            out.write("\n          " + aname + ' ' + type + ' ' + value);
        }

        // Finish the ATTLIST declaration.
        out.write(">\n");
    }


    /**
     * Generate the attribute type as a normalised string.
     */
    String makeAttributeType (String elname, String aname) {
        switch (_parser.getAttributeType(elname, aname)) {
        case XmlParser.ATTRIBUTE_CDATA:
            return "CDATA";
        case XmlParser.ATTRIBUTE_ID:
            return "ID";
        case XmlParser.ATTRIBUTE_IDREF:
            return "IDREF";
        case XmlParser.ATTRIBUTE_IDREFS:
            return "IDREFS";
        case XmlParser.ATTRIBUTE_ENTITY:
            return "ENTITY";
        case XmlParser.ATTRIBUTE_ENTITIES:
            return "ENTITIES";
        case XmlParser.ATTRIBUTE_NMTOKEN:
            return "NMTOKEN";
        case XmlParser.ATTRIBUTE_NMTOKENS:
            return "NMTOKENS";
        case XmlParser.ATTRIBUTE_ENUMERATED:
            // An enumeration.
            return _parser.getAttributeEnumeration(elname, aname);
        case XmlParser.ATTRIBUTE_NOTATION:
            // An enumeration of notations.
            return "NOTATION " + _parser.getAttributeEnumeration(elname, aname);
        }
        return null;
    }

    /**
     * Generate a full attribute default value.
     * @see #makeLiteral
     */
    String makeAttributeValue (String elname, String aname) {
        // Generate a default value based on the type.
        switch (_parser.getAttributeDefaultValueType(elname, aname)) {
        case XmlParser.ATTRIBUTE_DEFAULT_IMPLIED:
            return "#IMPLIED";
        case XmlParser.ATTRIBUTE_DEFAULT_SPECIFIED:
            return makeLiteral(_parser.getAttributeDefaultValue(elname, aname));
        case XmlParser.ATTRIBUTE_DEFAULT_REQUIRED:
            return "#REQUIRED";
        case XmlParser.ATTRIBUTE_DEFAULT_FIXED:
            return "#FIXED " +
                makeLiteral(_parser.getAttributeDefaultValue(elname,aname));
        }
        return null;
    }

    /**
     * Construct a string equivalent of external identifiers.
     * @see #makeLiteral
     */
    String makeExternalIdentifiers(String pubid, String sysid) {
        String extId = "";

        if (pubid != null) {
            extId = "PUBLIC " + makeLiteral(pubid);
            if (sysid != null) {
                extId = extId + ' ' + makeLiteral(sysid);
            }
        } else {
            extId = "SYSTEM " + makeLiteral(sysid);
        }
        return extId;
    }

    /**
     * Quote a literal, and escape any '"' or non-ASCII characters within it.
     */
    String makeLiteral(String data) {
        char ch[] = data.toCharArray();
        StringBuffer buf = new StringBuffer();

        buf.append('"');
        for (int i = 0; i < ch.length; i++) {
            if (ch[i] == '"') {
                buf.append("&#22;");
            } else if ((int)ch[i] > 0x7f) {
                buf.append("&#" + (int)ch[i] + ";");
            } else {
                buf.append(ch[i]);
            }
        }
        buf.append('"');

        return buf.toString();
    }
}



