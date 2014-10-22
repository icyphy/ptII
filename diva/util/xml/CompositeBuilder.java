/*
 Copyright (c) 1998-2014 The Regents of the University of California
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

import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;

/**
 * CompositeBuilder is a non-validating parser that uses other
 * builders to parse and generate XML files from arbitrary collections
 * of objects.  (FIXME - more documentation here)
 *
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public class CompositeBuilder extends AbstractXmlBuilder {
    /**
     * The public identity of the RCL dtd file.
     */
    public static final String PUBLIC_ID = "-//UC Berkeley//DTD builder 1//EN";

    /**
     * The URL where the DTD is stored.
     */
    public static final String DTD_URL = "http://www.gigascale.org/diva/dtd/builder.dtd";

    /**
     * The DTD for builder declarations.
     */
    public static final String DTD_1 = "<!ELEMENT builderDecls (builder*)><!ATTLIST builderDecls ref CDATA #IMPLIED><!ATTLIST builder name CDATA #REQUIRED class CDATA #REQUIRED builder CDATA #REQUIRED>";

    /**
     * Indicates a recognizer class
     */
    public static final String CLASS_TAG = "class";

    /**
     * Indicates the tag of a recognizer
     */
    public static final String TAG_TAG = "tag";

    /**
     * Indicates a builder for a recognizer
     */
    public static final String BUILDER_TAG = "builder";

    /**
     * Indicates a group of builder declarations
     */
    public static final String BUILDER_DECLS_TAG = "builderDecls";

    /**
     * Store builder declarations as [tag, (class, builder)] for
     * [class, (class, builder)] aliases.
     */
    private HashMap _builders = new HashMap();

    /** Add all of the builder declarations in the given
     * XML document to the builder map.
     */
    public void addBuilderDecls(Reader in) throws Exception {
        XmlDocument doc = new XmlDocument();
        doc.setDTDPublicID(PUBLIC_ID);
        doc.setDTD(DTD_1);

        XmlReader reader = new XmlReader();
        reader.parse(doc, in);

        if (reader.getErrorCount() > 0) {
            throw new Exception("errors encountered during parsing");
        }

        for (Iterator i = doc.getRoot().elements(); i.hasNext();) {
            XmlElement builder = (XmlElement) i.next();
            String[] val = new String[2];
            val[0] = builder.getAttribute(CLASS_TAG);
            val[1] = builder.getAttribute(BUILDER_TAG);
            _builders.put(builder.getAttribute(TAG_TAG), val);
            _builders.put(builder.getAttribute(CLASS_TAG), val);
            debug("Adding: " + builder.getAttribute(TAG_TAG) + "=>"
                    + builder.getAttribute(BUILDER_TAG));
        }
    }

    private void debug(String out) {
        System.out.println(out);
    }

    /** Build an object based on the XML element by looking up the
     * appropriate builder and calling that builder on the element.
     */
    @Override
    public Object build(XmlElement elt, String type) throws Exception {
        String[] val = (String[]) _builders.get(type);

        if (val == null) {
            if (getDelegate() == null) {
                String err = "Unknown type: " + type;
                throw new Exception(err);
            }

            return getDelegate().build(elt, type);
        }

        XmlBuilder builder = (XmlBuilder) Class.forName(val[1]).newInstance();
        builder.setDelegate(this);
        return builder.build(elt, val[0]);
    }

    /** Build an XML element based on given object by looking up
     * the appropriate builder based on the object's class name
     * and calling that builder's generate method on the object.
     */
    @Override
    public XmlElement generate(Object in) throws Exception {
        String[] val = (String[]) _builders.get(in.getClass().getName());

        if (val == null) {
            if (getDelegate() == null) {
                String err = "Unknown type: " + in.getClass().getName();
                throw new Exception(err);
            }

            return getDelegate().generate(in);
        }

        XmlBuilder builder = (XmlBuilder) Class.forName(val[1]).newInstance();
        builder.setDelegate(this);
        return builder.generate(in);
    }

    /**
     * Simple test of this class.
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err
                    .println("java CompositeBuilder <builderDeclsURI> <fileURI>");
            System.exit(1);
        } else {
            XmlDocument doc = new XmlDocument();
            XmlReader reader = new XmlReader();
            CompositeBuilder builder = new CompositeBuilder();
            builder.addBuilderDecls(new FileReader(args[0]));
            reader.parse(doc, new FileReader(args[1]));
            System.out.println("out = "
                    + builder.build(doc.getRoot(), doc.getRoot().getType()));
        }
    }
}
