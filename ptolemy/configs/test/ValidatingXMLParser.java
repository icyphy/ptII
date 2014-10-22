/* A validating parser.

 Copyright (c) 2010-2014 The Regents of the University of California.
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
package ptolemy.configs.test;

import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import ptolemy.moml.MoMLParser;

/**
 A validating XML parser.
 <p>Note that MoML files are not always valid XML because of the
 &lt;configure&gt; tag, so that tag should be removed.</p>

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ValidatingXMLParser extends DefaultHandler {
    /** Parse an XML parser using a validating parser.
     *  The DTD http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd
     *  is replaced with the value of MoMLParser.MoML_1.dtd.
     *  @param args A single element array that names the xml file to
     *  be parsed.  The file should have
     *  &lt;configure&gt;...&lt;configure&gt; removed.  One workaround
     *  is to remove the configure tags with a script before
     *  validating, see $PTII/ptolemy/moml/test/removeconfigure.
     *  See also <a href="http://ptolemy.eecs.berkeley.edu/ptolemyII/ptIIfaq.htm#MoML">http://ptolemy.eecs.berkeley.edu/ptolemyII/ptIIfaq.htm#MoML</a>
     */
    public static void main(String[] args) {
        try {
            parse(args[0]);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    /** Parse an XML parser using a validating parser.
     *  The DTD http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd
     *  is replaced with the value of MoMLParser.MoML_1.dtd.
     *  @param fileName  The xml file to be parsed.  The file should
     *  have &lt;configure&gt;...&lt;configure&gt; removed.
     *  @exception Throwable If the XML is invalid XML.
     */
    public static void parse(String fileName) throws Throwable {
        DefaultHandler handler = new ValidatingXMLParser();

        // Use the validating parser
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        try {
            SAXParser saxParser = factory.newSAXParser();
            // Instead of using saxParser.parser, we create our own
            // entity resolver so that we do not beat up the ptolemy
            // web server.
            //saxParser.parse(new File(fileName), handler);

            XMLReader xmlReader = saxParser.getXMLReader();
            MoMLEntityResolver resolver = new MoMLEntityResolver();
            xmlReader.setEntityResolver(resolver);
            xmlReader.setContentHandler(handler);
            xmlReader.parse(new InputSource(fileName));
        } catch (SAXParseException ex) {
            System.out.println("\n Error parsing line " + ex.getLineNumber()
                    + ", uri " + ex.getSystemId());
            System.out.println("   " + ex.getMessage());
            throw ex;
        }
    }

    /** Resolve an entity by checking for
     *  http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd
     *  and, if found, return the value of MoMLParser.MoML_1.dtd.
     */
    public static class MoMLEntityResolver implements EntityResolver {
        /** Resolve the entity.
         *  @param publicID Ignored.
         *  @param systemID The systemID.
         *  @return If systemID equals http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd
         *  then return an InputSource based on the value of MoMLParser.MoML_DTD_1,
         *  otherwise return null.
         *  @exception SAXException If the MoML DTD cannot be created.
         */
        @Override
        public InputSource resolveEntity(String publicID, String systemID)
                throws SAXException {
            if (systemID
                    .equals("http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd")) {
                return new InputSource(new StringReader(MoMLParser.MoML_DTD_1));
            }
            return null;
        }
    }

    //===========================================================
    // SAX DocumentHandler methods
    //===========================================================
    @Override
    public void setDocumentLocator(Locator l) {
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startElement(String namespaceURI, String sName, // simple name
            String qName, // qualified name
            Attributes attrs) throws SAXException {
    }

    @Override
    public void endElement(String namespaceURI, String sName, // simple name
            String qName // qualified name
    ) throws SAXException {
    }

    @Override
    public void characters(char[] buf, int offset, int len) throws SAXException {
    }

    @Override
    public void ignorableWhitespace(char[] buf, int offset, int len)
            throws SAXException {
    }

    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
    }

    @Override
    public void error(SAXParseException exception) throws SAXParseException {
        warning(exception);
        throw exception;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXParseException {
        System.out.println("Warning: line " + exception.getLineNumber()
                + ", uri " + exception.getSystemId());
        System.out.println("   " + exception.getMessage());
    }
}
